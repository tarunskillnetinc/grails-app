package uk.co.wonderlane.wlpos

import grails.plugin.springsecurity.annotation.Secured
import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import org.apache.commons.lang3.StringUtils
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import org.joda.time.format.DateTimeFormat
import org.joda.time.format.DateTimeFormatter
import uk.co.wonderlane.wlpos.entities.TenderType
import uk.co.wonderlane.wlpos.entities.cash.ReconciliationTotal
import uk.co.wonderlane.wlpos.entities.cash.TenderTotal
import uk.co.wonderlane.wlpos.enums.ReasonCodeType
import uk.co.wonderlane.wlpos.enums.SafeSessionAction
import uk.co.wonderlane.wlpos.enums.ShiftAction
import uk.co.wonderlane.wlpos.enums.TenderMovementType
import uk.co.wonderlane.wlpos.reporting.ReportColumn
import uk.co.wonderlane.wlpos.reporting.ReportColumns
import uk.co.wonderlane.wlpos.reporting.ReportType
import uk.co.wonderlane.wlpos.reporting.SortParams
import uk.co.wonderlane.wlpos.reporting.TenderMovement

class CashReportingController {

    def cashReportingService
    def reasonCodeService
    def safeService
    def safeSessionService
    def storeService
    def locationService
    def tillAssignmentService
    def springSecurityService
    def tenderTypeService

    private static final TENDER_MOVEMENT_REPORT_SORT_COLUMNS = ["timestamp", "storeId", "fromLocation", "toLocation", "amount", "type", "reason", "userName"]
    private static final BANKING_REPORT_SORT_COLUMNS = ["financialWeekNumber", "bankingDate", "type", "bankName", "usersRealName", "amount", "comment"]

    @Secured(['ROLE_ENGINEER', 'ROLE_HEAD_OFFICE', 'ROLE_STORE_MANAGER', 'ROLE_SUPERVISOR'])
    def index() {

    }

    @Secured(['ROLE_ENGINEER', 'ROLE_HEAD_OFFICE', 'ROLE_STORE_MANAGER', 'ROLE_SUPERVISOR'])
    def shiftFinalisation() {
        DateTimeFormatter dateFormatter = DateTimeFormat.forPattern("dd/MM/yyyy").withZoneUTC()
        DateTime startDate = params.startDate ? DateTime.parse(params.startDate, dateFormatter) : DateTime.now(DateTimeZone.UTC).minusDays(7)
        DateTime endDate = params.endDate ? DateTime.parse(params.endDate, dateFormatter) : DateTime.now(DateTimeZone.UTC)
        def stores = storeService.getStores(springSecurityService.principal.retailerId)
        def tills = []
        if (springSecurityService.principal.storeNumber) {
            tills = tillAssignmentService.getTillsByStoreId(springSecurityService.principal.storeNumber)
        }

        [stores: stores, tills: tills, startDate: startDate, endDate: endDate]
    }

    @Secured(['ROLE_ENGINEER', 'ROLE_HEAD_OFFICE', 'ROLE_STORE_MANAGER', 'ROLE_SUPERVISOR'])
    def safeFinalisation() {
        DateTimeFormatter dateFormatter = DateTimeFormat.forPattern("dd/MM/yyyy").withZoneUTC()
        DateTime startDate = params.startDate ? DateTime.parse(params.startDate, dateFormatter) : DateTime.now(DateTimeZone.UTC).minusDays(7)
        DateTime endDate = params.endDate ? DateTime.parse(params.endDate, dateFormatter) : DateTime.now(DateTimeZone.UTC)
        def stores = storeService.getStores(springSecurityService.principal.retailerId)
        def safes = []
        if (springSecurityService.principal.storeId) {
            safes = safeService.getStoreSafes()
        }

        [stores: stores, safes: safes, startDate: startDate, endDate: endDate]
    }

    @Secured(['ROLE_ENGINEER', 'ROLE_HEAD_OFFICE', 'ROLE_STORE_MANAGER', 'ROLE_SUPERVISOR'])
    def tillActivity() {
        DateTimeFormatter dateFormatter = DateTimeFormat.forPattern("dd/MM/yyyy").withZoneUTC()
        DateTime startDate = params.startDate ? DateTime.parse(params.startDate, dateFormatter) : DateTime.now(DateTimeZone.UTC).minusDays(7)
        DateTime endDate = params.endDate ? DateTime.parse(params.endDate, dateFormatter) : DateTime.now(DateTimeZone.UTC)
        def stores = storeService.getStores(springSecurityService.principal.retailerId)
        def tills = []
        if (springSecurityService.principal.storeNumber) {
            tills = tillAssignmentService.getTillsByStoreId(springSecurityService.principal.storeNumber)
        }

        [stores: stores, tills: tills, startDate: startDate, endDate: endDate]
    }

    @Secured(['ROLE_ENGINEER', 'ROLE_HEAD_OFFICE', 'ROLE_STORE_MANAGER', 'ROLE_SUPERVISOR'])
    def shiftVariance() {
        DateTimeFormatter dateFormatter = DateTimeFormat.forPattern("dd/MM/yyyy").withZoneUTC()
        DateTime startDate = params.startDate ? DateTime.parse(params.startDate, dateFormatter).withTimeAtStartOfDay() : DateTime.now(DateTimeZone.UTC).withTimeAtStartOfDay()
        DateTime endDate = params.endDate ? DateTime.parse(params.endDate, dateFormatter).withTimeAtStartOfDay() : DateTime.now(DateTimeZone.UTC).withTimeAtStartOfDay()

        def stores = []
        def tills = null

        if (springSecurityService.principal.storeId) {
            stores = storeService.getStore(springSecurityService.principal.retailerId, springSecurityService.principal.storeId)
            tills = tillAssignmentService.getTillsByStoreId(springSecurityService.principal.storeNumber)
            tills.sort { it.tillId }
        } else {
            stores = storeService.getStores(springSecurityService.principal.retailerId)
        }

        [startDate: startDate, endDate: endDate, stores: stores, tills: tills]
    }

    @Secured(['ROLE_ENGINEER', 'ROLE_HEAD_OFFICE', 'ROLE_STORE_MANAGER', 'ROLE_SUPERVISOR'])    
    def safeActivity() {
        DateTimeFormatter dateFormatter = DateTimeFormat.forPattern("dd/MM/yyyy").withZoneUTC()
        DateTime startDate = params.startDate ? DateTime.parse(params.startDate, dateFormatter) : DateTime.now(DateTimeZone.UTC).minusDays(7)
        DateTime endDate = params.endDate ? DateTime.parse(params.endDate, dateFormatter) : DateTime.now(DateTimeZone.UTC)
        def stores = storeService.getStores(springSecurityService.principal.retailerId)
        def safes = []
        if (springSecurityService.principal.storeId) {
            safes = safeService.getStoreSafes()
        }

        [stores: stores, safes: safes, startDate: startDate, endDate: endDate]
    }

    @Secured(['ROLE_ENGINEER', 'ROLE_HEAD_OFFICE', 'ROLE_STORE_MANAGER', 'ROLE_SUPERVISOR'])
    def safeVariance() {
        DateTimeFormatter dateFormatter = DateTimeFormat.forPattern("dd/MM/yyyy").withZoneUTC()
        DateTime startDate = params.startDate ? DateTime.parse(params.startDate, dateFormatter).withTimeAtStartOfDay() : DateTime.now(DateTimeZone.UTC).withTimeAtStartOfDay()
        DateTime endDate = params.endDate ? DateTime.parse(params.endDate, dateFormatter).withTimeAtStartOfDay() : DateTime.now(DateTimeZone.UTC).withTimeAtStartOfDay()

        def stores = []
        def safes = null

        if (springSecurityService.principal.storeId) {
            stores = storeService.getStore(springSecurityService.principal.retailerId, springSecurityService.principal.storeId)
            safes = safeService.getSafesForStore(springSecurityService.principal.storeId)
        } else {
            stores = storeService.getStores(springSecurityService.principal.retailerId)
        }

        [startDate: startDate, endDate: endDate, stores: stores, safes: safes]
    }

    @Secured(['ROLE_ENGINEER', 'ROLE_HEAD_OFFICE', 'ROLE_STORE_MANAGER', 'ROLE_SUPERVISOR'])
    def tenderMovements() {
        DateTimeFormatter dateFormatter = DateTimeFormat.forPattern("dd/MM/yyyy").withZoneUTC()
        DateTime startDate = params.startDate ? DateTime.parse(params.startDate, dateFormatter).withTimeAtStartOfDay() : DateTime.now(DateTimeZone.UTC).withTimeAtStartOfDay()
        DateTime endDate = params.endDate ? DateTime.parse(params.endDate, dateFormatter).withTimeAtStartOfDay() : DateTime.now(DateTimeZone.UTC).withTimeAtStartOfDay()

        Integer storeId
        if (springSecurityService.principal.storeId) {
            storeId = springSecurityService.principal.storeId
        } else {
            storeId = params.storeFilter ? Integer.parseInt(params.storeFilter) : null
        }

        def stores = storeService.getStores(springSecurityService.principal.retailerId)

        def movementTypes = TenderMovementType.values().sort { it.name() }
        def tenderTypes

        if (springSecurityService.principal.storeId) {
            tenderTypes = tenderTypeService.getApplicableTenderTypes()
        } else {
            int totalCount
            (tenderTypes, totalCount) = tenderTypeService.getTenderTypes(null, false, null, null, 0, 9999)
        }
        tenderTypes.removeAll { it.autoReconcile }

        [reportType: ReportType.TENDER_MOVEMENTS, tenderTypes: tenderTypes, tenderMovementTypes: movementTypes, stores: stores, startDate: startDate, endDate: endDate, storeId: storeId, userColumns: cashReportingService.getReportColumns(ReportType.TENDER_MOVEMENTS)]
    }

    @Secured(['ROLE_ENGINEER', 'ROLE_HEAD_OFFICE', 'ROLE_STORE_MANAGER', 'ROLE_SUPERVISOR'])
    def banking() {
        DateTimeFormatter dateFormatter = DateTimeFormat.forPattern("dd/MM/yyyy").withZoneUTC()
        DateTime startDate = params.startDate ? DateTime.parse(params.startDate, dateFormatter).withTimeAtStartOfDay() : DateTime.now(DateTimeZone.UTC).withTimeAtStartOfDay()
        DateTime endDate = params.startDate ? DateTime.parse(params.endDate, dateFormatter).withTimeAtStartOfDay().plusDays(1).minusMillis(1) : DateTime.now(DateTimeZone.UTC).withTimeAtStartOfDay().plusDays(1).minusMillis(1)

        def stores = []
        if (springSecurityService.principal.storeId) {
            def store = storeService.getStore(springSecurityService.principal.retailerId, springSecurityService.principal.storeId)
            stores = [store]
        } else {
            stores = storeService.getStores(springSecurityService.principal.retailerId)
        }

        def bankingType = ['Bank Deposit', 'Bank Receipt']

        [reportType         : ReportType.BANKING_REPORT,
         startDate          : startDate,
         endDate            : endDate,
         userColumns        : cashReportingService.getReportColumns(ReportType.BANKING_REPORT),
         stores             : stores.sort { it.config.storeNumber },
         bankingType        : bankingType]
    }

    @Secured(['ROLE_ENGINEER', 'ROLE_HEAD_OFFICE', 'ROLE_STORE_MANAGER', 'ROLE_SUPERVISOR'])
    def ajaxGetTillsForStore(int storeNumber) {
        def tillIds = []
        def tills = tillAssignmentService.getTillsByStoreId(storeNumber)
        tills.forEach { tillIds.add(text: it.tillId, value: it.tillId) }
        render status: 200, contentType: 'application/json', text: JsonOutput.toJson([options: tillIds])
    }

    @Secured(['ROLE_ENGINEER', 'ROLE_HEAD_OFFICE', 'ROLE_STORE_MANAGER', 'ROLE_SUPERVISOR'])
    def ajaxGetSafesForStore(int storeNumber) {
        def safeOptions = []
        def safes = safeService.getSafesByStoreNumber(storeNumber)
        safes.forEach { safeOptions.add(text: it.selectionText, value: it.id) }
        render status: 200, contentType: 'application/json', text: JsonOutput.toJson([options: safeOptions])
    }


    @Secured(['ROLE_ENGINEER', 'ROLE_HEAD_OFFICE', 'ROLE_STORE_MANAGER', 'ROLE_SUPERVISOR'])
    def ajaxGetAllShiftsForTill(Integer storeNumber, int tillId, String startDate, String endDate) {
        if (!startDate || !endDate) {
            render status: 500
        }
        DateTimeFormatter dateFormatter = DateTimeFormat.forPattern("dd/MM/yyyy").withZoneUTC()
        DateTime startDateTime = DateTime.parse(startDate, dateFormatter).withTimeAtStartOfDay()
        DateTime endDateTime = DateTime.parse(endDate, dateFormatter).withTimeAtStartOfDay().plusDays(1)

        def shifts = cashReportingService.getAllShiftsForTill(storeNumber, tillId, startDateTime, endDateTime)
        def shiftNumbers = []
        shifts?.forEach { shiftNumbers.add(text: it.shiftNumber, value: it.shiftNumber) }

        render status: 200, contentType: 'application/json', text: JsonOutput.toJson([options: shiftNumbers])
    }

    @Secured(['ROLE_ENGINEER', 'ROLE_HEAD_OFFICE', 'ROLE_STORE_MANAGER', 'ROLE_SUPERVISOR'])
    def ajaxGetFinalisedShiftsForTill(Integer storeNumber, int tillId, String startDate, String endDate) {
        if (!startDate || !endDate) {
            render status: 500
        }
        DateTimeFormatter dateFormatter = DateTimeFormat.forPattern("dd/MM/yyyy").withZoneUTC()
        DateTime startDateTime = DateTime.parse(startDate, dateFormatter).withTimeAtStartOfDay()
        DateTime endDateTime = DateTime.parse(endDate, dateFormatter).withTimeAtStartOfDay().plusDays(1)

        def shifts = cashReportingService.getFinalisedShiftsForTill(storeNumber, tillId, startDateTime, endDateTime)
        def shiftNumbers = []
        shifts.forEach { shiftNumbers.add(text: it.shiftNumber, value: it.shiftNumber) }
        render status: 200, contentType: 'application/json', text: JsonOutput.toJson([options: shiftNumbers])
    }

    @Secured(['ROLE_ENGINEER', 'ROLE_HEAD_OFFICE', 'ROLE_STORE_MANAGER', 'ROLE_SUPERVISOR'])
    def ajaxGetFinalisedSafeSessionsForSafe(Integer storeNumber, int safeId, String startDate, String endDate) {
        if (!startDate || !endDate) {
            render status: 500
        }
        DateTimeFormatter dateFormatter = DateTimeFormat.forPattern("dd/MM/yyyy").withZoneUTC()
        DateTime startDateTime = DateTime.parse(startDate, dateFormatter).withTimeAtStartOfDay()
        DateTime endDateTime = DateTime.parse(endDate, dateFormatter).withTimeAtStartOfDay().plusDays(1)

        def safeSessions = cashReportingService.getFinalisedSafeSessionsForSafe(storeNumber, safeId, startDateTime, endDateTime)
        def sessionNumbers = []
        safeSessions.forEach { sessionNumbers.add(text: it.sessionNumber, value: it.sessionNumber) }
        render status: 200, contentType: 'application/json', text: JsonOutput.toJson([options: sessionNumbers])
    }

    @Secured(['ROLE_ENGINEER', 'ROLE_HEAD_OFFICE', 'ROLE_STORE_MANAGER', 'ROLE_SUPERVISOR'])
    def ajaxGetSafeSessionsForSafe(Integer storeNumber, int safeId, String startDate, String endDate) {
        if (!startDate || !endDate) {
            render status: 500
        }
        DateTimeFormatter dateFormatter = DateTimeFormat.forPattern("dd/MM/yyyy").withZoneUTC()
        DateTime startDateTime = DateTime.parse(startDate, dateFormatter).withTimeAtStartOfDay()
        DateTime endDateTime = DateTime.parse(endDate, dateFormatter).withTimeAtStartOfDay().plusDays(1)

        def safeSessions = cashReportingService.getSafeSessionsForSafe(storeNumber, safeId, startDateTime, endDateTime)
        def sessionNumbers = []
        safeSessions.forEach { sessionNumbers.add(text: it.sessionNumber, value: it.sessionNumber) }
        render status: 200, contentType: 'application/json', text: JsonOutput.toJson([options: sessionNumbers])
    }

    @Secured(['ROLE_ENGINEER', 'ROLE_HEAD_OFFICE', 'ROLE_STORE_MANAGER', 'ROLE_SUPERVISOR'])
    def ajaxGetFinalisedShiftReport(Integer storeNumber, int tillId, int shiftNumber) {
        def shift = cashReportingService.getShiftForShiftNumber(storeNumber, tillId, shiftNumber).getShift()

        if (shift) {
            def applicableTenderTypes = tenderTypeService.getApplicableTenderTypes()
            applicableTenderTypes.removeAll { it.autoReconcile }

            def storeConfig = storeService.getStoreByStoreNumber(springSecurityService.principal.retailerId, storeNumber?:springSecurityService.principal.storeNumber).getConfig()
            var storeText = storeConfig.storeNumber + ' - ' + storeConfig.storeName

            // As we made these strings and not dates we have to reformat them for the reports
            shift.setShiftOpenTime(reformatDateTime(shift.getShiftOpenTime()))
            shift.setShiftCloseTime(reformatDateTime(shift.getShiftCloseTime()))

            def values = forcePopulateReconciledValues(shift.getReconciliationTotals(), shift.getTenderTotals(), applicableTenderTypes)
            var reason = getReasonText(values, ReasonCodeType.TENDER_RECONCILIATION_VARIANCE)
            var additionalReason = values.stream().filter { StringUtils.isNotBlank(it.varianceReasonText) }
                    .map { it.varianceReasonText }.findFirst().orElse(null)

            render(status: 200, template: "shiftFinalisationReport", model: [
                    storeText: storeText, shift: shift, values: values, reason: reason, additionalReason: additionalReason
            ])
        } else {
            render status: 500
        }
    }

    @Secured(['ROLE_ENGINEER', 'ROLE_HEAD_OFFICE', 'ROLE_STORE_MANAGER', 'ROLE_SUPERVISOR'])
    def ajaxGetFinalisedSafeSessionReport(Integer storeNumber, int safeId, int sessionNumber) {
        def safeSession = cashReportingService.getSafeSessionForSessionNumber(storeNumber, safeId, sessionNumber).getSafeSession()

        if (safeSession) {
            def applicableTenderTypes = tenderTypeService.getApplicableTenderTypes()
            applicableTenderTypes.removeAll { it.autoReconcile }
            def storeConfig = storeService.getStoreByStoreNumber(springSecurityService.principal.retailerId, storeNumber?:springSecurityService.principal.storeNumber).getConfig()
            var storeText = storeConfig.storeNumber + ' - ' + storeConfig.storeName
            var safeText = safeService.getSafeById(safeId)?.selectionText + " - " + safeId

            // As we made these strings and not dates we have to reformat them for the reports
            safeSession.setOpenTime(reformatDateTime(safeSession.getOpenTime()))

            def values = forcePopulateReconciledValues(safeSession.getReconciliationTotals(), safeSession.getTenderTotals(), applicableTenderTypes)
            var reason = getReasonText(values, ReasonCodeType.TENDER_RECONCILIATION_SAFE_VARIANCE)
            var additionalReason = values.stream().filter { StringUtils.isNotBlank(it.varianceReasonText) }
                    .map { it.varianceReasonText }.findFirst().orElse(null)

            render(status: 200, template: "safeFinalisationReport", model: [
                    storeText: storeText, safeText: safeText, safeSession: safeSession, values: values, reason: reason, additionalReason: additionalReason
            ])
        } else {
            render status: 500
        }
    }

    @Secured(['ROLE_ENGINEER', 'ROLE_HEAD_OFFICE', 'ROLE_STORE_MANAGER', 'ROLE_SUPERVISOR'])
    def ajaxGetSafeActivityReport(Integer storeNumber, int safeId, int sessionNumber) {
        def safeSession = cashReportingService.getSafeSessionForSessionNumber(storeNumber, safeId, sessionNumber).getSafeSession()

        if (safeSession) {
            def storeConfig = storeService.getStoreByStoreNumber(springSecurityService.principal.retailerId, storeNumber?:springSecurityService.principal.storeNumber).getConfig()

            var storeText = storeConfig.storeNumber + ' - ' + storeConfig.storeName
            var safeText = safeId + " - " + safeService.getSafeById(safeId)?.selectionText

            // As we made these strings and not dates we have to reformat them for the reports
            safeSession.setOpenTime(reformatDateTime(safeSession.getOpenTime()))

            // easier to unpack data here than on the page!
            def reportLines = getSafeSessionAuditReportLines(safeSession.getId())

            render(status: 200, template: "safeActivityReport", model: [storeText: storeText, safeText: safeText, safeSession: safeSession, reportLines: reportLines])
        } else {
            render status: 500
        }
    }

    @Secured(['ROLE_ENGINEER', 'ROLE_HEAD_OFFICE', 'ROLE_STORE_MANAGER', 'ROLE_SUPERVISOR'])
    def ajaxGetTillActivityReport(Integer storeNumber, int tillId, int shiftNumber) {
        def shift = cashReportingService.getShiftForShiftNumber(storeNumber, tillId, shiftNumber).getShift()
        if (shift) {
            def storeConfig = storeService.getStoreByStoreNumber(springSecurityService.principal.retailerId,
                    storeNumber?:springSecurityService.principal.storeNumber).getConfig()
            var storeText = storeConfig.storeNumber + ' - ' + storeConfig.storeName

            // As we made these strings and not dates we have to reformat them for the reports
            shift.setShiftOpenTime(reformatDateTime(shift.getShiftOpenTime()))
            shift.setShiftCloseTime(reformatDateTime(shift.getShiftCloseTime()))

            // easier to unpack data here than on the page!
            def reportLines = getShiftAuditReportLines(shift.id)

            render(status: 200, template: "tillActivityReport", model: [
                    storeText: storeText, shift: shift, reportLines: reportLines
            ])
        } else {
            render status: 500
        }
    }

    @Secured(['ROLE_ENGINEER', 'ROLE_HEAD_OFFICE', 'ROLE_STORE_MANAGER', 'ROLE_SUPERVISOR'])
    def ajaxGetSafeListForStore(Integer storeNumber) {
        def storeId = storeService.getStoreIdByStoreNumber(storeNumber)
        def safes = safeService.getSafesForStore(storeId)

        def safeEntries = safes.collect { safe ->
            [id: safe.id, description: safe.description, active: safe.active]
        }

        render(status: 200, contentType: 'application/json', text: JsonOutput.toJson([options: safeEntries]))
    }

    @Secured(['ROLE_ENGINEER', 'ROLE_HEAD_OFFICE', 'ROLE_STORE_MANAGER', 'ROLE_SUPERVISOR'])
    def ajaxGetSafeSessionVarianceReport(Integer storeNumber, String selectedSafes, String startDate, String endDate) {
        def safes = null
        def store = storeService.getStoreByStoreNumber(springSecurityService.principal.retailerId, storeNumber)

        if (selectedSafes != null && selectedSafes.length() > 0) {
            def splitSafes = selectedSafes.split(',')
            safes = splitSafes.collect { it.trim() }
        }

        DateTimeFormatter dateFormatter = DateTimeFormat.forPattern("dd/MM/yyyy").withZoneUTC()
        DateTime start = DateTime.parse(startDate, dateFormatter).withTimeAtStartOfDay()
        DateTime end = DateTime.parse(endDate, dateFormatter).withTimeAtStartOfDay().plusDays(1)

        /*  Get all safes for the store */
        def storeSafes = safeService.getSafesForStore(store.id)
        def safesMap = storeSafes.collectEntries { [(it.id): it.description] }

        /* Get all safe sessions for the store in the selected time period */
        def safeSessions = safeSessionService.getSafeSessions(store.id, start, end, "safeId", "asc")
        def sessions = safeSessions.collect { it.getSafeSession() }

        /* Filter out safe sessions for safes that are not of interest */
        def filterMap = safesMap.findAll { entry -> safes.contains(entry.value) }
        def varianceSessions = sessions.findAll { obj -> filterMap.containsKey(obj.safeId) }

        /* Remove all safes witout a variance */
        def filteredSessions = varianceSessions.findAll { filteredSession ->
            !filteredSession.reconciliationTotals.every { it.variance == 0 }
        }

        def varianceReasons = filteredSessions.collectMany { session -> session.reconciliationTotals*.varianceReason}.findAll { it != null }

        def reasonMap = null
        if (varianceReasons.size() > 0) {
            def reasons = reasonCodeService.findReasonCodesByCodes(springSecurityService.principal.retailerId, varianceReasons);
            reasonMap = reasons.collectEntries { [(it.code): (it.description)] }
        }

        render(status: filteredSessions ? 200 : 204, template: "safeVarianceReport", model: [store: store, safeSessions: filteredSessions, startDate: startDate, endDate: endDate, safes: safesMap, reasonCodes: reasonMap])
    }

    @Secured(['ROLE_ENGINEER', 'ROLE_HEAD_OFFICE', 'ROLE_STORE_MANAGER', 'ROLE_SUPERVISOR'])
    def ajaxGetTillsForStoreNumber(Integer storeNumber) {
        def tills = tillAssignmentService.getTillsByStoreId(storeNumber)

        def tillEntries = tills.collect { till ->
            [id: till.id, description: till.tillId]
        }.sort { it.description }

        render(status: 200, contentType: 'application/json', text: JsonOutput.toJson([options: tillEntries]))
    }

    @Secured(['ROLE_ENGINEER', 'ROLE_HEAD_OFFICE', 'ROLE_STORE_MANAGER', 'ROLE_SUPERVISOR'])
    def ajaxGetShiftVarianceReport(Integer storeNumber, String selectedTills, String startDate, String endDate) {
        DateTimeFormatter dateFormatter = DateTimeFormat.forPattern("dd/MM/yyyy").withZoneUTC()
        DateTime start = DateTime.parse(startDate, dateFormatter).withTimeAtStartOfDay()
        DateTime end = DateTime.parse(endDate, dateFormatter).withTimeAtStartOfDay().plusDays(1)

        def store = storeService.getStoreByStoreNumber(springSecurityService.principal.retailerId, storeNumber)

        def tills = null

        if (selectedTills != null && selectedTills.length() > 0) {
            def splitTills = selectedTills.split(',')
            tills = splitTills.collect { it.trim().toInteger() }
        }

        def shiftRecords = cashReportingService.getShiftsForStoreAndTillIds(store.id, tills, start, end, "tillId", "asc")
        def allShifts = (shiftRecords ?: []).collect { it.getShift() }

        /* Remove all shifts witout a variance */
        def shifts = allShifts.findAll { allShift ->
            !allShift.reconciliationTotals.every { it.variance == 0 }
        }

        def varianceReasons = shifts.collectMany { shift -> shift.reconciliationTotals*.varianceReason}.findAll { it != null }

        def reasonMap = null
        if (varianceReasons.size() > 0) {
            def reasons = reasonCodeService.findReasonCodesByCodes(springSecurityService.principal.retailerId, varianceReasons);
            reasonMap = reasons.collectEntries { [(it.code): (it.description)] }
        }

        render(status: shifts ? 200 : 204, template: "shiftVarianceReport", model: [store: store, shifts: shifts, startDate: startDate, endDate: endDate, reasonCodes: reasonMap])
    }

    @Secured(['ROLE_ENGINEER', 'ROLE_HEAD_OFFICE', 'ROLE_STORE_MANAGER', 'ROLE_SUPERVISOR'])
    def ajaxGetTenderMovementsReport(SortParams sortParams) {
        if (sortParams?.sortColumn == "id") {
            sortParams.sortColumn = "timestamp"
            sortParams.sortOrder = "desc"
        }

        sortParams.validateParams(TENDER_MOVEMENT_REPORT_SORT_COLUMNS)

        DateTimeFormatter dateFormatter = DateTimeFormat.forPattern("dd/MM/yyyy").withZoneUTC()
        DateTime startDate = params.startDate ? DateTime.parse(params.startDate, dateFormatter).withTimeAtStartOfDay() : DateTime.now(DateTimeZone.UTC).withTimeAtStartOfDay()
        DateTime endDate = params.endDate ? DateTime.parse(params.endDate, dateFormatter).withTimeAtStartOfDay() : DateTime.now(DateTimeZone.UTC).withTimeAtStartOfDay()

        TenderMovementType tenderMovementType = params.tenderMovementType ? TenderMovementType.valueOf(params.tenderMovementType) : null
        Integer tenderTypeId = params.tenderTypeId ? Integer.parseInt(params.tenderTypeId) : null
        Integer storeId = params.storeFilter ? getIntegerParam(params.storeFilter) : null

        uk.co.wonderlane.wlpos.TenderType tenderType = tenderTypeId ? tenderTypeService.getTenderType(tenderTypeId) : null

        def tenderMovements = cashReportingService.getTenderMovements(startDate, endDate.plusDays(1), tenderMovementType, tenderType, storeId, sortParams.max, sortParams.offset, sortParams.sortColumn, sortParams.sortOrder)

        if (params.csv != null && params.csv == "true") {
            def fileName = "TenderMovements-" + new Date().format("yyyy_MM_dd_HH_mm_ss") + ".csv"
            response.setHeader("Content-Disposition", "attachment; filename=${fileName}")
            response.setHeader("Content-Type", "text/csv;")
            render getTenderMovementsCsv(tenderMovements?.tenderMovements?.toList())
        } else {
            render (template: "tenderMovementsResults", model: [tenderMovements: tenderMovements?.tenderMovements?.toList(),
                                                                userColumns: cashReportingService.getReportColumns(ReportType.TENDER_MOVEMENTS),
                                                                sortParams: sortParams,
                                                                startDate: startDate,
                                                                endDate: endDate,
                                                                tenderMovementType: tenderMovementType,
                                                                tenderType: tenderType,
                                                                storeId: storeId,
                                                                totalResults: tenderMovements?.totalCount])
        }
    }

    @Secured(['ROLE_ENGINEER', 'ROLE_HEAD_OFFICE', 'ROLE_STORE_MANAGER', 'ROLE_SUPERVISOR'])
    def ajaxGetBankingReport(SortParams sortParams) {
        sortParams.validateParams(BANKING_REPORT_SORT_COLUMNS)

        DateTimeFormatter dateFormatter = DateTimeFormat.forPattern("dd/MM/yyyy").withZoneUTC()
        DateTime startDate = params.startDate ? DateTime.parse(params.startDate, dateFormatter).withTimeAtStartOfDay() : DateTime.now(DateTimeZone.UTC).withTimeAtStartOfDay()
        DateTime endDate = params.endDate ? DateTime.parse(params.endDate, dateFormatter).withTimeAtStartOfDay().plusDays(1).minusMillis(1) : DateTime.now(DateTimeZone.UTC).withTimeAtStartOfDay()

        Integer storeId = getIntegerParam(params.storeFilter)

        def tenderMovementTypes = []
        def bankingType = params.bankingType ?: ""

        switch (bankingType) {
            case "Bank Deposit":
                tenderMovementTypes << TenderMovementType.BANKING
                break
            case "Bank Receipt":
                tenderMovementTypes << TenderMovementType.CASH_INBOUND
                break
            default:
                tenderMovementTypes << TenderMovementType.BANKING
                tenderMovementTypes << TenderMovementType.CASH_INBOUND
                break
        }

        def locations = null
        def store = null

        if (springSecurityService.principal.storeId) {
            store = storeService.getStore(springSecurityService.principal.retailerId, springSecurityService.principal.storeId)
            locations = locationService.getLocationsByStoreId(springSecurityService.principal.storeId)
        } else {
            store = storeService.getStore(springSecurityService.principal.retailerId, storeId)
            locations = locationService.getLocationsByStoreId(storeId)
        }

        def locationMap = locations.collectEntries { [(it.id): it.description] }

        /* Currently this should always only filter on the bankingDate anr return the results in descending order */
        def tenderMovements = cashReportingService.getBankingTenderMovements(startDate, endDate, tenderMovementTypes, storeId, sortParams.max, sortParams.offset, "bankingDate", "desc")

        render (template: "bankingReportResults", model: [bankingReports: tenderMovements?.tenderMovements?.toList(),
                                                          userColumns: cashReportingService.getReportColumns(ReportType.BANKING_REPORT),
                                                          sortParams: sortParams,
                                                          startDate: startDate,
                                                          endDate: endDate,
                                                          locationMap: locationMap,
                                                          storeNumber: store?.config?.storeNumber,
                                                          storeName: store?.config?.storeName,
                                                          totalResults: tenderMovements?.totalCount])
    }

    def ajaxSaveReportColumns() {
        try {
            if (params.reportColumns && params.reportType) {
                def userReportColumns = new JsonSlurper().parseText(params.reportColumns)
                def reportType = ReportType.valueOf(params.reportType)

                def reportColumns = cashReportingService.getReportColumns(reportType)

                if (!reportColumns) {
                    reportColumns = new ReportColumns(userId: springSecurityService.principal.id, reportType: reportType)
                }

                userReportColumns?.each { userReportColumn ->
                    if (reportColumns?.columns?.find { it.column == userReportColumn.key }) {
                        reportColumns?.columns?.find { it.column == userReportColumn.key }?.enabled = userReportColumn.value
                    } else {
                        reportColumns.addToColumns(new ReportColumn(column: userReportColumn.key, enabled: userReportColumn.value))
                    }
                }

                cashReportingService.saveReportColumns(reportColumns)

                render(status: 200)
            }
        } catch (Exception e) {
            e.printStackTrace()
            render(status: 500, text: "An error occurred saving your report column preferences.")
        }
    }

    private getSafeSessionAuditReportLines(int shiftId) {
        def reportLines = []
        def auditRecords = cashReportingService.getAuditEventsForSafe(shiftId)

        def applicableTenderTypes = tenderTypeService.getApplicableTenderTypes()
        applicableTenderTypes.removeAll { it.autoReconcile }

        auditRecords?.forEach { audit ->

            def tenderValues = []
            def showReconciled = [SafeSessionAction.RECONCILE.toString(), SafeSessionAction.RECOUNT.toString(), SafeSessionAction.FINALISE.toString()].contains(audit.action)

            if (audit.action == SafeSessionAction.SPOT_CHECK.toString()) {
                try {
                    forcePopulateReconciledValues(null, audit.safeSessionValues?.transferPendingTotals(), applicableTenderTypes).forEach { recTotal ->
                        tenderValues.add([tenderTypeId: recTotal.tenderTypeId, tenderTypeName: recTotal.tenderTypeName, value: recTotal.value])
                    }
                } catch (Exception e) {
                    log.error("Invalid shift 'extras' on shift audit " + audit.id, e)
                }
            } else if (showReconciled) {
                // show saved shift values
                try {
                    forcePopulateReconciledValues(audit.safeSessionValues?.reconciliationTotals, audit.safeSessionValues?.tenderTotals, applicableTenderTypes).forEach { recTotal ->
                        tenderValues.add([tenderTypeId: recTotal.tenderTypeId, tenderTypeName: recTotal.tenderTypeName, value: recTotal.value])
                    }
                } catch (Exception e) {
                    log.error("Invalid shift 'extras' on shift audit " + audit.id, e)
                }
            } else {
                // show specific tender movement values(s) if recorded
                try {
                    audit.tenderMovementValues?.forEach { tenderTotal ->
                        tenderValues.add([tenderTypeId: tenderTotal.tenderTypeId, tenderTypeName: tenderTotal.tenderTypeName, value: tenderTotal.value])
                    }
                } catch (Exception e) {
                    log.error("Invalid tender movement on shift audit " + audit.id, e)
                }
            }

            reportLines.add([id : audit.id,timestamp: audit.timestamp, name: audit.usersRealName, username: audit.userName, transactionType: audit.action,
                             rowspan: Math.max(tenderValues.size(), 1), tenderValues: tenderValues.toSorted { value -> value.type }])
        }

        return reportLines.toSorted { line -> line.id }
    }

    private getShiftAuditReportLines(int shiftId) {
        def reportLines = []
        def auditRecords = cashReportingService.getShiftAuditRecords(shiftId)

        def applicableTenderTypes = tenderTypeService.getApplicableTenderTypes()
        applicableTenderTypes.removeAll { it.autoReconcile }

        auditRecords?.forEach {audit ->
            def tenderValues = []
            def showReconciled = [ShiftAction.RECONCILE.toString(), ShiftAction.RECOUNT.toString(), ShiftAction.FINALISE.toString()].contains(audit.action)

            if (showReconciled || audit.action == ShiftAction.SPOT_CHECK.toString()) {
                // show saved shift values
                try {
                    forcePopulateReconciledValues(showReconciled ? audit.shiftValues?.reconciliationTotals : null, audit.shiftValues?.tenderTotals, applicableTenderTypes).forEach { recTotal ->
                        tenderValues.add([tenderTypeId: recTotal.tenderTypeId, tenderTypeName: recTotal.tenderTypeName, value: recTotal.value])
                    }
                } catch (Exception e) {
                    log.error("Invalid shift 'extras' on shift audit " + audit.id, e)
                }
            } else {
                // show specific tender movement values(s) if recorded
                try {
                    audit.tenderMovementValues?.forEach { tenderTotal ->
                        tenderValues.add([tenderTypeId: tenderTotal.tenderTypeId, tenderTypeName: tenderTotal.tenderTypeName, value: tenderTotal.value])
                    }
                } catch (Exception e) {
                    log.error("Invalid tender movement on shift audit " + audit.id, e)
                }
            }

            reportLines.add([
                    id          : audit.id, timestamp: audit.timestamp,
                    name        : audit.usersRealName, username: audit.username,
                    action      : audit.action, source: audit.backoffice ? "Back Office" : "Till",
                    rowspan     : Math.max(tenderValues.size(), 1),
                    tenderValues: tenderValues.toSorted { value -> value.tenderTypeName }
            ])
        }

        return reportLines.toSorted { line -> line.id }
    }

    private List<ReconciliationTotal> forcePopulateReconciledValues(List<ReconciliationTotal> reconciliationTotals, List<TenderTotal> tenderTotals, List<TenderType> applicableTenderTypes) {
        // todo - Update to use tender config (Warning: used on multiple reports and not just as reconciled values)
        def values = reconciliationTotals ?: []

        // Add totals that didnt get reconciled.
        tenderTotals?.forEach { total ->
            if (!values.any { total.tenderTypeId == it.tenderTypeId }) {
                def recTotal = new ReconciliationTotal(total.tenderTypeId, total.tenderTypeName, total.cashTender)
                recTotal.setValue(total.value)

                values.add(recTotal)
            }
        }

        // Now force all tender types except cashback.
        applicableTenderTypes?.each {tenderType ->
            if (!values.any { it.tenderTypeId == tenderType.id }) {
                values.add(new ReconciliationTotal(tenderType.id, tenderType.name, tenderType.cashTender))
            }
        }

        return values.sort { it.tenderTypeName }
    }

    private String getReasonText(List<ReconciliationTotal> values, ReasonCodeType type) {
        var code = values.stream().filter { StringUtils.isNotBlank(it.varianceReason) }
                .map { it.varianceReason }.findFirst().orElse(null)
        if (code) {
            var reasonCode = reasonCodeService.findByTypeAndCode(springSecurityService.principal.retailerId, type, code)

            return reasonCode?.description ?: code
        }

        return null
    }

    /**
     * Perform some sense checking on our input parameters.
     *
     * @param productId
     * @return
     */
    private static int getIntegerParam(paramValue) {
        if (!paramValue || !paramValue.isNumber() || paramValue.length() > 9) {
            return -1
        }

        return Integer.parseInt(paramValue)
    }

    private static String reformatDateTime(String dateTime) {
        if (dateTime == null) {
            return null
        }

        try {
            DateTimeFormatter from = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss").withZoneUTC()
            DateTimeFormatter to = DateTimeFormat.forPattern("dd/MM/yyyy HH:mm").withZoneUTC()
            return from.parseDateTime(dateTime).toString(to)
        } catch (Exception ignored) {
            return dateTime
        }
    }

    private String getTenderMovementsCsv(List<TenderMovement> tenderMovementList) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("Timestamp,Store,From Location,To Location,Amount,Type,Reason,User\n")

        tenderMovementList?.each { item ->
            stringBuilder.append(item?.timestamp)
            stringBuilder.append(",")
            stringBuilder.append(item?.storeId)
            stringBuilder.append(",")
            stringBuilder.append(item.fromLocation?.description)
            stringBuilder.append(",")
            stringBuilder.append(item.toLocation?.description)
            stringBuilder.append(",")
            stringBuilder.append(item.amount)
            stringBuilder.append(",")
            stringBuilder.append(item.type)
            stringBuilder.append(",")
            stringBuilder.append(item.reason)
            stringBuilder.append(",")
            stringBuilder.append(item.userName)
            stringBuilder.append("\n")
        }

        return stringBuilder.toString()
    }
}