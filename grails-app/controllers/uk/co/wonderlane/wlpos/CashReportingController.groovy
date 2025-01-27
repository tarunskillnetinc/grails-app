package uk.co.wonderlane.wlpos

import grails.plugin.springsecurity.annotation.Secured
import groovy.json.JsonOutput
import org.apache.commons.lang3.StringUtils
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import org.joda.time.format.DateTimeFormat
import org.joda.time.format.DateTimeFormatter
import uk.co.wonderlane.wlpos.entities.cash.ReconciliationTotal
import uk.co.wonderlane.wlpos.entities.cash.TenderTotal
import uk.co.wonderlane.wlpos.enums.ReasonCodeType
import uk.co.wonderlane.wlpos.enums.TenderType

class CashReportingController {

    def cashReportingService
    def financialWeekService
    def reasonCodeService
    def safeService
    def safeSessionService
    def storeService
    def tillAssignmentService
    def springSecurityService

    @Secured(['ROLE_ENGINEER', 'ROLE_HEAD_OFFICE', 'ROLE_STORE_MANAGER', 'ROLE_SUPERVISOR'])
    def index() {}

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
    def ajaxGetFinalisedShiftReport(Integer storeNumber, int tillId, int shiftNumber) {
        def shift = cashReportingService.getShiftForShiftNumber(storeNumber, tillId, shiftNumber).getShift()
        if (shift) {
            def storeConfig = storeService.getStoreByStoreNumber(springSecurityService.principal.retailerId,
                    storeNumber?:springSecurityService.principal.storeNumber).getConfig()
            var storeText = storeConfig.storeNumber + ' - ' + storeConfig.storeName

            // As we made these strings and not dates we have to reformat them for the reports
            shift.setShiftOpenTime(reformatDateTime(shift.getShiftOpenTime()))
            shift.setShiftCloseTime(reformatDateTime(shift.getShiftCloseTime()))

            def values = forcePopulateReconciledValues(
                    shift.getReconciliationTotals(), shift.getTenderTotals()
            )
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
            def storeConfig = storeService.getStoreByStoreNumber(springSecurityService.principal.retailerId,
                    storeNumber?:springSecurityService.principal.storeNumber).getConfig()
            var storeText = storeConfig.storeNumber + ' - ' + storeConfig.storeName
            var safeText = safeService.getSafeById(safeId)?.selectionText + " - " + safeId

            // As we made these strings and not dates we have to reformat them for the reports
            safeSession.setOpenTime(reformatDateTime(safeSession.getOpenTime()))

            def values = forcePopulateReconciledValues(
                    safeSession.getReconciliationTotals(), safeSession.getTenderTotals()
            )
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

    private List<ReconciliationTotal> forcePopulateReconciledValues(
            List<ReconciliationTotal> reconciliationTotals, List<TenderTotal> tenderTotals
    ) {
        // todo - Update to use tender config
        def values = reconciliationTotals?: []
        // add totals that didnt get reconciled
        tenderTotals?.forEach { total ->
            if (!values.stream().filter { value -> total.tenderType == value.tenderType }.findFirst().isPresent()) {
                def recTotal = new ReconciliationTotal(total.tenderType)
                recTotal.setValue(total.value)
                values.add(recTotal)
            }
        }
        // now force all tender types except cashback
        for (TenderType type : TenderType.values()) {
            if (type != TenderType.CASHBACK) {
                if (!values.stream().filter { value -> type == value.tenderType }.findFirst().isPresent()) {
                    values.add(new ReconciliationTotal(type))
                }
            }
        }
        return values.toSorted { it.tenderType }
    }

    private String getReasonText(List<ReconciliationTotal> values, ReasonCodeType type) {
        var code = values.stream().filter { StringUtils.isNotBlank(it.varianceReason) }
                .map { it.varianceReason }.findFirst().orElse(null)
        if (code) {
            var reasonCode = reasonCodeService.findByTypeAndCode(
                    springSecurityService.principal.retailerId, type, code)
            return reasonCode?.description?: code
        }
        return null
    }

    private static String reformatDateTime(String dateTime) {
        try {
            DateTimeFormatter from = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss").withZoneUTC()
            DateTimeFormatter to = DateTimeFormat.forPattern("dd/MM/yyyy HH:mm").withZoneUTC()
            return from.parseDateTime(dateTime).toString(to)
        } catch (Exception ignored) {
            return dateTime
        }
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
        DateTime start = startDate ? DateTime.parse(startDate, dateFormatter).withTimeAtStartOfDay() : DateTime.now(DateTimeZone.UTC).withTimeAtStartOfDay()
        DateTime end = endDate ? DateTime.parse(endDate, dateFormatter).withTimeAtStartOfDay() : DateTime.now(DateTimeZone.UTC).withTimeAtStartOfDay()

        /*  Get all safes for the store */
        def storeSafes = safeService.getSafesForStore(store.id)
        def safesMap = storeSafes.collectEntries { [(it.id): it.description] }

        /* Get all safe sessions for the store in the selected time period */
        def safeSessions = safeSessionService.getSafeSessions(store.id, start, end, "safeId", "asc")
        def sessions = safeSessions.collect { it.getSafeSession() }

        /* Filter out safe sessions for safes that are not of interest */
        def filterMap = safesMap.findAll { entry -> safes.contains(entry.value) }
        def filteredSessions = sessions.findAll { obj -> filterMap.containsKey(obj.safeId) }

        def varianceReasons = filteredSessions.collectMany { session -> session.reconciliationTotals*.varianceReason}.findAll { it != null }

        def reasonMap = null
        if (varianceReasons.size() > 0) {
            def reasons = reasonCodeService.findReasonCodesByCodes(springSecurityService.principal.retailerId, varianceReasons);
            reasonMap = reasons.collectEntries { [(it.code): (it.description)] }
        }

        render(status: filteredSessions ? 200 : 204, template: "safeVarianceReport", model: [store: store, safeSessions: filteredSessions, startDate: startDate, endDate: endDate, safes: safesMap, reasonCodes: reasonMap])
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
        DateTime start = startDate ? DateTime.parse(startDate, dateFormatter).withTimeAtStartOfDay() : DateTime.now(DateTimeZone.UTC).withTimeAtStartOfDay()
        DateTime end = endDate ? DateTime.parse(endDate, dateFormatter).withTimeAtStartOfDay() : DateTime.now(DateTimeZone.UTC).withTimeAtStartOfDay()

        def store = storeService.getStoreByStoreNumber(springSecurityService.principal.retailerId, storeNumber)

        def tills = null

        if (selectedTills != null && selectedTills.length() > 0) {
            def splitTills = selectedTills.split(',')
            tills = splitTills.collect { it.trim().toInteger() }
        }

        def shiftRecords = cashReportingService.getShiftsForStoreAndTillIds(store.id, tills, start, end, "tillId", "asc")
        def shifts = (shiftRecords ?: []).collect { it.getShift() }

        def varianceReasons = shifts.collectMany { shift -> shift.reconciliationTotals*.varianceReason}.findAll { it != null }

        def reasonMap = null
        if (varianceReasons.size() > 0) {
            def reasons = reasonCodeService.findReasonCodesByCodes(springSecurityService.principal.retailerId, varianceReasons);
            reasonMap = reasons.collectEntries { [(it.code): (it.description)] }
        }
        
        render(status: shifts ? 200 : 204, template: "shiftVarianceReport", model: [store: store, shifts: shifts, startDate: startDate, endDate: endDate, reasonCodes: reasonMap])
    }
}