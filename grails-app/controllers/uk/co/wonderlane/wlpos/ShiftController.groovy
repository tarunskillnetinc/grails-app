package uk.co.wonderlane.wlpos

import grails.converters.JSON
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import org.joda.time.format.DateTimeFormat
import org.joda.time.format.DateTimeFormatter
import uk.co.wonderlane.wlpos.entities.cash.ReconciliationTotal
import uk.co.wonderlane.wlpos.entities.cash.Shift
import uk.co.wonderlane.wlpos.entities.cash.Snapshot
import uk.co.wonderlane.wlpos.entities.cash.TenderTotal
import uk.co.wonderlane.wlpos.enums.LocationType
import uk.co.wonderlane.wlpos.enums.ShiftStatus
import uk.co.wonderlane.wlpos.enums.TenderMovementType
import uk.co.wonderlane.wlpos.enums.TenderReconciliationVarianceReason
import uk.co.wonderlane.wlpos.enums.TenderType
import uk.co.wonderlane.wlpos.reporting.Location

class ShiftController {

    def springSecurityService
    def shiftService
    def snapshotService
    def reportingService
    def locationService

    def index() {
        if (!springSecurityService.principal.storeId) {
            flash.error = "You do not have access to this page."
            redirect(uri: "/")
            return
        }

        DateTimeFormatter formatter = DateTimeFormat.forPattern("dd/MM/yyyy")
        String startDate = (DateTime.now(DateTimeZone.UTC).withTimeAtStartOfDay().minusDays(7)).toString(formatter)
        String endDate = (DateTime.now(DateTimeZone.UTC).withTimeAtStartOfDay()).toString(formatter)

        [startDate: params.startDate ?: startDate , endDate: params.endDate ?: endDate, tillId: params.tillId]
    }

    def ajaxGetShifts() { //Method to load all shifts
        def successMessage = params.successMessage
        def errorMessage = params.errorMessage
        Integer tillId = null
        try {
            if (params.tillId) { //Check request contains till number and try to pass it
                try {
                    tillId = Integer.parseInt(params.tillId)
                } catch (Exception ex) {
                    // Non-numeric input added, do nothing.
                    log.error(String.format("Non numeric till number added for tillId: %d error: %s", tillId, ex.getMessage()), ex)
                }
            }

            List<Shift> shiftList = shiftService.getShifts(tillId) //Load existing active shifts

            //This will load shifts for tills currently which do not have any existing tills on `shift` table
            //1. This will load all tills in `tillConfiguration` table
            //2. Then it will check any till is not having current shift
            //3. Then return dummy shift list which do not have active shift
            List<Shift> shiftNonExistsList = shiftService.getShiftsForNonExistingTills(shiftList, tillId)

            //Add previously return dummy shift to existing list
            if (shiftNonExistsList != null && !shiftNonExistsList.isEmpty()){
                shiftList.addAll(shiftNonExistsList)
            }
            //Check any financial week available for shifts
            boolean isFinancialWeekExists = shiftList.any { shift -> shift.financialWeek != null }

            //Group shifts by tillId and sort each group by shiftNumber
            //If shift number is null then push them into bottom of the list
            def shiftMap = shiftList.groupBy { it.tillId }
                    ?.collectEntries { entryTillId, shifts ->
                        [(entryTillId): shifts.sort { a, b ->
                            if (a.shiftNumber == null && b.shiftNumber == null) return 0
                            if (a.shiftNumber == null) return 1
                            if (b.shiftNumber == null) return -1
                            return a.shiftNumber <=> b.shiftNumber
                        }]
                    }

            // Sort the map by tillId
            def sortedShiftMap = shiftMap.sort { it.key }

            render(template: "shiftViewerResults", model: [shiftMap: sortedShiftMap, isFinancialWeekExists: isFinancialWeekExists, lastRefreshDate: new DateTime(), successMessage: successMessage, errorMessage: errorMessage])

        } catch (Exception ex) {
            log.error(String.format("Shift loading error for tillId: %d error: %s", tillId, ex.getMessage()), ex)
            if (errorMessage == null || errorMessage == ''){
                errorMessage = "Unexpected error loading tills"
            }
            render(template: "shiftViewerResults", model: [errorMessage: errorMessage])
        }
    }

    def ajaxGetCashDetails(int shiftId) {
        def shift = shiftService.getShift(shiftId)

        if (shift == null) {
            render ""
            return
        }

        if (shift.reconciledDate == null) {
            render(template: "cashUpModal", model: [shift: shift])
        } else if (shift.reconciledDate != null) {
            render(template: "cashUpSummaryModal", model: [shift: shift])
        }
    }

    def ajaxChangeCashUpType(CashUpCommand cashUpCommand) {
        def template = ""

        if (cashUpCommand.type == "VALUE") {
            template = "cashUpByValue"

            // Switching from denomination to value.
            if (cashUpCommand.cashUpBy == "DENOMINATION") {
                cashUpCommand.fiftyPounds *= 50
                cashUpCommand.twentyPounds *= 20
                cashUpCommand.tenPounds *= 10
                cashUpCommand.fivePounds *= 5
                cashUpCommand.twoPounds *= 2
                cashUpCommand.onePounds *= 1
                cashUpCommand.fiftyPences *= 0.50
                cashUpCommand.twentyPences *= 0.20
                cashUpCommand.tenPences *= 0.10
                cashUpCommand.fivePences *= 0.05
                cashUpCommand.twoPences *= 0.02
                cashUpCommand.onePences *= 0.01
            }
        } else if (cashUpCommand.type == "DENOMINATION") {
            template = "cashUpByDenomination"

            // Switching from value to denomination.
            if (cashUpCommand.cashUpBy == "VALUE") {
                cashUpCommand.fiftyPounds /= 50
                cashUpCommand.twentyPounds /= 20
                cashUpCommand.tenPounds /= 10
                cashUpCommand.fivePounds /= 5
                cashUpCommand.twoPounds /= 2
                cashUpCommand.onePounds /= 1
                cashUpCommand.fiftyPences /= 0.50
                cashUpCommand.twentyPences /= 0.20
                cashUpCommand.tenPences /= 0.10
                cashUpCommand.fivePences /= 0.05
                cashUpCommand.twoPences /= 0.02
                cashUpCommand.onePences /= 0.01
            }
        } else if (cashUpCommand.type == "TOTALS") {
            template = "cashUpByTotals"

            // Switching from value to totals.
            if (cashUpCommand.cashUpBy == "VALUE") {
                cashUpCommand.cashTotal = cashUpCommand.fiftyPounds + cashUpCommand.twentyPounds + cashUpCommand.tenPounds + cashUpCommand.fivePounds + cashUpCommand.twoPounds + cashUpCommand.onePounds + cashUpCommand.fiftyPences + cashUpCommand.twentyPences + cashUpCommand.tenPences + cashUpCommand.fivePences + cashUpCommand.twoPences + cashUpCommand.onePences
            } else if (cashUpCommand.cashUpBy == "DENOMINATION") {
                // Switching from denomination to totals.
                cashUpCommand.cashTotal = cashUpCommand.fiftyPounds * 50 + cashUpCommand.twentyPounds * 20 + cashUpCommand.tenPounds * 10 + cashUpCommand.fivePounds * 5 + cashUpCommand.twoPounds * 2 + cashUpCommand.onePounds * 1 + cashUpCommand.fiftyPences * 0.50 + cashUpCommand.twentyPences * 0.20 + cashUpCommand.tenPences * 0.10 + cashUpCommand.fivePences * 0.05 + cashUpCommand.twoPences * 0.02 + cashUpCommand.onePences * 0.01
            }
        }

        render(template: template, model: [values: cashUpCommand])
    }

    def ajaxSaveCash(CashUpCommand cashUpCommand) {
        def shift = shiftService.getShift(cashUpCommand.shiftId)

        ReconciliationTotal cashTotal = shift.reconciliationTotals.find { it.tenderType == TenderType.CASH } ?: null

        if (cashTotal == null) {
            cashTotal = new ReconciliationTotal(TenderType.CASH)
            shift.reconciliationTotals.add(cashTotal)
        }

        if (cashUpCommand.cashUpBy == "VALUE") {
            cashTotal.value = cashUpCommand.fiftyPounds + cashUpCommand.twentyPounds + cashUpCommand.tenPounds + cashUpCommand.fivePounds + cashUpCommand.twoPounds + cashUpCommand.onePounds + cashUpCommand.fiftyPences + cashUpCommand.twentyPences + cashUpCommand.tenPences + cashUpCommand.fivePences + cashUpCommand.twoPences + cashUpCommand.onePences
        } else if (cashUpCommand.cashUpBy == "DENOMINATION") {
            cashTotal.value = cashUpCommand.fiftyPounds * 50 + cashUpCommand.twentyPounds * 20 + cashUpCommand.tenPounds * 10 + cashUpCommand.fivePounds * 5 + cashUpCommand.twoPounds * 2 + cashUpCommand.onePounds * 1 + cashUpCommand.fiftyPences * 0.50 + cashUpCommand.twentyPences * 0.20 + cashUpCommand.tenPences * 0.10 + cashUpCommand.fivePences * 0.05 + cashUpCommand.twoPences * 0.02 + cashUpCommand.onePences * 0.01
        } else {
            cashTotal.value = cashUpCommand.cashTotal
        }

        cashTotal.variance = (cashTotal.value ?: BigDecimal.ZERO) - (shift.cashInDrawer ?: BigDecimal.ZERO)

        ReconciliationTotal vouchersTotal = shift.reconciliationTotals?.find { it.tenderType == TenderType.VOUCHER }

        if (vouchersTotal == null) {
            vouchersTotal = new ReconciliationTotal(TenderType.VOUCHER)
            shift.reconciliationTotals.add(vouchersTotal)
        }

        vouchersTotal.value = cashUpCommand.vouchersTotal
        vouchersTotal.variance = (vouchersTotal.value ?: BigDecimal.ZERO) - (shift.tenderTotals.findAll { it.tenderType == TenderType.VOUCHER }?.sum { it.value } ?: BigDecimal.ZERO)

        shiftService.saveShift(shift)

        def safeLocations = locationService.getStoreSafeLocations()

        if (safeLocations.collect().isEmpty()) {
            Location location = new Location()
            location.safeId = 1
            location.retailerId = shift.retailerId
            location.storeId = shift.storeId
            location.type = LocationType.SAFE
            location.description = "Safe 1"
            location.save()
            safeLocations = locationService.getStoreSafeLocations()
        }

        render(template: "cashUpSummaryModal", model: [ shift: shift, varianceReasons: TenderReconciliationVarianceReason.values(), safeLocations: safeLocations ])
    }

    def ajaxSaveShift(SaveShiftCommand saveShiftCommand) {
        def shift = shiftService.getShift(saveShiftCommand.shiftId)

        if (saveShiftCommand.tenderReconciliationVarianceReason != null) {
            shift.reconciliationTotals.findAll { it.variance != BigDecimal.ZERO }?.each {
                it.varianceReason = saveShiftCommand.tenderReconciliationVarianceReason
                it.varianceReasonText = saveShiftCommand.tenderReconciliationVarianceReasonText
            }
        }

        if (shift.reconciledDate == null) {
            shift.reconciledDate = DateTime.now()
            shift.reconciledByUserId = springSecurityService.principal.id
            shift.reconciledByUsersName = springSecurityService.principal.usersName
        } else {
            shift.reReconciledDate = DateTime.now()
            shift.reReconciledByUserId = springSecurityService.principal.id
            shift.reReconciledByUsersName = springSecurityService.principal.usersName
        }

        shiftService.saveShift(shift)

        Snapshot latestSnapshot = snapshotService.getSnapshotForLocation(saveShiftCommand.safeLocationId)
        ReconciliationTotal cashTotal = shift.reconciliationTotals.find { it.tenderType == TenderType.CASH } ?: null

        if (cashTotal != null) {
            TenderTotal cashExpected = latestSnapshot.expectedTotals.find { it.tenderType == TenderType.CASH } ?: null
            if (cashExpected == null) {
                cashExpected = new TenderTotal(TenderType.CASH)
                latestSnapshot.expectedTotals.add(cashExpected)
            }

            cashExpected.value = cashExpected.value.add(cashTotal.value)
        }

        ReconciliationTotal voucherTotal = shift.reconciliationTotals.find { it.tenderType == TenderType.VOUCHER } ?: null
        if (voucherTotal != null) {
            TenderTotal voucherExpected = latestSnapshot.expectedTotals.find { it.tenderType == TenderType.VOUCHER } ?: null
            if (voucherExpected == null) {
                voucherExpected = new TenderTotal(TenderType.VOUCHER)
                latestSnapshot.expectedTotals.add(voucherExpected)
            }

            voucherExpected.value = voucherExpected.value.add(voucherTotal.value)
        }

        snapshotService.saveSnapshot(latestSnapshot)

        def tillLocation = locationService.getTillLocation(shift.tillId)
        def safeLocation = locationService.getLocation(saveShiftCommand.safeLocationId)

        shift.reconciliationTotals.each {
            if (it.value > BigDecimal.ZERO) {
                reportingService.saveTenderMovement(reportingService.createNewTenderMovement(TenderMovementType.CASH_UP,
                        it.tenderType,
                        tillLocation as Location,
                        safeLocation as Location,
                        it.value))
            }
        }

        render(template: "cashUpSummaryModal", model: [ shift: shift ])
    }


    def ajaxOpenShift(){ // This is method to functioning action button of shift
        Integer retailerId = null
        Integer storeId = null
        Integer tillId = null
        Integer tillIdFilter = null //If any till id added into filter then pass it
        try {
            shiftService.validateParams(params)
            retailerId  = Integer.parseInt(params.retailerId)
            storeId  = Integer.parseInt(params.storeId)
            tillId  = Integer.parseInt(params.tillId)
            tillIdFilter  = params.tillIdFilter ? Integer.parseInt(params.tillIdFilter) : null //If any till id added into filter then pass it
            def shift = shiftService.getOpenShift(retailerId, storeId, tillId) //Load existing shift
            if (shift == null || !(shift.getShiftStatus() == ShiftStatus.OPEN)) { // Check shift is null or not open if so then proceed to create new shift
                shift = shiftService.createNewShift(retailerId, storeId, tillId, false) //call function to open shift
                flash.message = String.format("Shift %d has successfully been opened for till %d", shift.getId(), tillId)
            } else {
                flash.message = String.format("Till %d's shift was already open", tillId)
            }
        } catch (Exception ex) {
            flash.error = String.format("Till %d's shift open failed", tillId)
            log.error(String.format("Shift create error: %d store: %d tillId: %d error: %s", retailerId, storeId, tillId, ex.getMessage()), ex)
        }
        redirect(action: "ajaxGetShifts", params: [tillId: tillIdFilter, successMessage: flash.message, errorMessage: flash.error]) //Once done redirect to process get shift action
    }
}

class CashUpCommand {

    int shiftId
    String type // Type being navigated TO.
    String cashUpBy // Type being navigated FROM.
    BigDecimal fiftyPounds = BigDecimal.ZERO
    BigDecimal twentyPounds = BigDecimal.ZERO
    BigDecimal tenPounds = BigDecimal.ZERO
    BigDecimal fivePounds = BigDecimal.ZERO
    BigDecimal twoPounds = BigDecimal.ZERO
    BigDecimal onePounds = BigDecimal.ZERO
    BigDecimal fiftyPences = BigDecimal.ZERO
    BigDecimal twentyPences = BigDecimal.ZERO
    BigDecimal tenPences = BigDecimal.ZERO
    BigDecimal fivePences = BigDecimal.ZERO
    BigDecimal twoPences = BigDecimal.ZERO
    BigDecimal onePences = BigDecimal.ZERO
    BigDecimal cashTotal = BigDecimal.ZERO
    BigDecimal chequesTotal = BigDecimal.ZERO
    BigDecimal vouchersTotal = BigDecimal.ZERO
}

class SaveShiftCommand {

    int shiftId
    Integer safeLocationId
    TenderReconciliationVarianceReason tenderReconciliationVarianceReason
    String tenderReconciliationVarianceReasonText
}
