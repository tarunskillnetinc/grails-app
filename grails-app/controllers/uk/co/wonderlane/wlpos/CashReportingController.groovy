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
import uk.co.wonderlane.wlpos.enums.TenderType

class CashReportingController {

    def cashReportingService
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
    def ajaxGetTillsForStore(int storeNumber) {
        def tillIds = []
        def tills = tillAssignmentService.getTillsByStoreId(storeNumber)
        tills.forEach { tillIds.add(it.tillId) }
        render status: 200, contentType: 'application/json', text: JsonOutput.toJson([options: tillIds])
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
        shifts.forEach { shiftNumbers.add(it.shiftNumber) }
        render status: 200, contentType: 'application/json', text: JsonOutput.toJson([options: shiftNumbers])
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
            var reason = values.stream().filter { StringUtils.isNotBlank(it.varianceReason) }
                    .map { it.varianceReason }.findFirst().orElse(null)
            var additionalReason = values.stream().filter { StringUtils.isNotBlank(it.varianceReasonText) }
                    .map { it.varianceReasonText }.findFirst().orElse(null)

            render(status: 200, template: "shiftFinalisationReport", model: [
                    storeText: storeText, shift: shift, values: values, reason: reason, additionalReason: additionalReason
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
                values.add(total)
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

    private static String reformatDateTime(String dateTime) {
        try {
            DateTimeFormatter from = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss").withZoneUTC()
            DateTimeFormatter to = DateTimeFormat.forPattern("dd/MM/yyyy HH:mm").withZoneUTC()
            return from.parseDateTime(dateTime).toString(to)
        } catch (Exception ignored) {
            return dateTime
        }
    }
}