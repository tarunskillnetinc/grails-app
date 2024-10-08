package uk.co.wonderlane.wlpos


import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import org.joda.time.format.DateTimeFormat
import org.joda.time.format.DateTimeFormatter
import uk.co.wonderlane.wlpos.entities.cash.Shift
import uk.co.wonderlane.wlpos.enums.ShiftStatus
import uk.co.wonderlane.wlpos.enums.TenderReconciliationVarianceReason

class ShiftController {

    def springSecurityService
    def shiftService
    def snapshotService
    def reportingService

    def index() {
        if (!springSecurityService.principal.storeId) {
            flash.error = "You do not have access to this page."
            redirect(uri: "/")
            return
        }

        DateTimeFormatter formatter = DateTimeFormat.forPattern("dd/MM/yyyy")
        String startDate = (DateTime.now(DateTimeZone.UTC).withTimeAtStartOfDay().minusDays(7)).toString(formatter)
        String endDate = (DateTime.now(DateTimeZone.UTC).withTimeAtStartOfDay()).toString(formatter)

        [startDate: params.startDate ?: startDate, endDate: params.endDate ?: endDate, tillId: params.tillId]
    }

    // This will load all available shifts based on tills
    // If shift is available then load shift along with till if it does not then add extra button to open shift
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
            if (shiftNonExistsList != null && !shiftNonExistsList.isEmpty()) {
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

            int configuredRetryAttempts = shiftService.getConfiguredRecountAttempts(springSecurityService.principal.retailerId, springSecurityService.principal.storeId)

            render(template: "shiftViewerResults", model: [shiftMap      : sortedShiftMap, isFinancialWeekExists: isFinancialWeekExists, lastRefreshDate: new DateTime(), configuredRetryAttempts: configuredRetryAttempts,
                                                           successMessage: successMessage, errorMessage: errorMessage])

        } catch (Exception ex) {
            log.error(String.format("Shift loading error for tillId: %d error: %s", tillId, ex.getMessage()), ex)
            if (errorMessage == null || errorMessage == '') {
                errorMessage = "Unexpected error loading shifts"
            }
            render(template: "shiftViewerResults", model: [errorMessage: errorMessage])
        }
    }

    // This is method to load either cash up model or cash summary based on requested action
    // If action is either reconcile or recount --> then popup cash up mode
    // If action is finalise --> then pop up cash summary mode
    def ajaxGetCashDetails(int shiftId, boolean isRecount, boolean isFinalise) {
        try {
            def shift = shiftService.getShift(shiftId, -1, -1)
            //To process
            // 1. Shift should exists
            // 2. If it is RECONCILE request -> Shift status must be UNRECONCILED
            // 3. If it is RECOUNT or FINALISED request -> Shift status must be RECONCILED
            if (shift != null && ((!isRecount && !isFinalise && shift.getShiftStatus() == ShiftStatus.UNRECONCILED) || ((isRecount || isFinalise) && shift.getShiftStatus() == ShiftStatus.RECONCILED))) {
                if ((shift.getShiftStatus() == ShiftStatus.RECONCILED) && (!shiftService.isShiftRecountAmountNotExceed(shift) || isFinalise)) {
                    // When we move into finalise view we need to pass safe location to summary view to select
                    // For that select if no have create safe location
                    def safeLocations = shiftService.getSafeLocation(shift)
                    render(template: "cashUpSummaryModal", model: [shift: shift, isShiftFinalizeMode: true, safeLocations: safeLocations])
                    return
                }
                render(template: "cashUpModal", model: [shift: shift])
            } else if (shift != null && !isRecount && !isFinalise && shift.getShiftStatus() != ShiftStatus.UNRECONCILED) {
                // Request is for reconcile but already reconciled
                render(status: 400, contentType: 'application/json', message: String.format("Failed to reconcile shift %s. Already reconciled.", shiftId))
            } else if (shift != null && isRecount && shift.getShiftStatus() != ShiftStatus.RECONCILED) {
                // Request is for recount but already recounted
                render(status: 400, contentType: 'application/json', message: String.format("Failed to recount shift %s. Already recounted.", shiftId))
            } else if (shift != null && isFinalise && shift.getShiftStatus() != ShiftStatus.RECONCILED) {
                // Request is for finalise but already finalised
                render(status: 400, contentType: 'application/json', message: String.format("Failed to finalise shift %s. Already finalised.", shiftId))
            } else {
                render(status: 400, contentType: 'application/json', message: String.format("Action failed for shift id: %d", shiftId))
            }
        } catch (Exception ex) {
            log.error(String.format("Shift cash detail loading error for shift id: %d error: %s", shiftId, ex.getMessage()), ex)
            render(status: 400, contentType: 'application/json', message: String.format("Action failed for shift id: %d", shiftId))
        }
    }

    // This will use to move between each cash up views (value, denomination or totals)
    def ajaxChangeCashUpType(CashUpCommand cashUpCommand) {
        try {
            def template = ""
            if (cashUpCommand.type == "VALUE") {
                template = "cashUpByValue"
                if (cashUpCommand.cashUpBy == "DENOMINATION") {  //Switching from denomination to value.
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
                if (cashUpCommand.cashUpBy == "VALUE") { //Switching from value to denomination.
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
                if (cashUpCommand.cashUpBy == "VALUE") { //Switching from value to totals.
                    cashUpCommand.cashTotal = cashUpCommand.fiftyPounds + cashUpCommand.twentyPounds + cashUpCommand.tenPounds + cashUpCommand.fivePounds + cashUpCommand.twoPounds + cashUpCommand.onePounds + cashUpCommand.fiftyPences + cashUpCommand.twentyPences + cashUpCommand.tenPences + cashUpCommand.fivePences + cashUpCommand.twoPences + cashUpCommand.onePences
                } else if (cashUpCommand.cashUpBy == "DENOMINATION") { //Switching from denomination to totals.
                    cashUpCommand.cashTotal = cashUpCommand.fiftyPounds * 50 + cashUpCommand.twentyPounds * 20 + cashUpCommand.tenPounds * 10 + cashUpCommand.fivePounds * 5 + cashUpCommand.twoPounds * 2 + cashUpCommand.onePounds * 1 + cashUpCommand.fiftyPences * 0.50 + cashUpCommand.twentyPences * 0.20 + cashUpCommand.tenPences * 0.10 + cashUpCommand.fivePences * 0.05 + cashUpCommand.twoPences * 0.02 + cashUpCommand.onePences * 0.01
                }
            }
            render(template: template, model: [values: cashUpCommand])
        } catch (Exception ex) {
            log.error(String.format("Shift cash up type change error for shift id: %d error: %s", cashUpCommand.shiftId, ex.getMessage()), ex)
            return null
        }
    }

    // This will store values added in cash up model into temporary variable `onhold` cash and voucher total's in shift object
    // Secondary this will check any available locations available if not added default `Safe 1` location
    def ajaxSaveCash(CashUpCommand cashUpCommand) {
        try {
            def shift = shiftService.getShift(cashUpCommand.shiftId, -1, -1)
            if (shift != null && ((!cashUpCommand.isRecount && shift.getShiftStatus() == ShiftStatus.UNRECONCILED) || (cashUpCommand.isRecount && shift.getShiftStatus() == ShiftStatus.RECONCILED))) {
                if (shift.getShiftStatus() == ShiftStatus.RECONCILED && !shiftService.isShiftRecountAmountNotExceed(shift)) {
                    def safeLocations = shiftService.getSafeLocation(shift)
                    render(template: "cashUpSummaryModal", model: [shift: shift, isShiftFinalizeMode: true, safeLocations: safeLocations])
                    return
                }
                shiftService.processShiftCashSave(cashUpCommand, shift)
                def safeLocations = shiftService.getSafeLocation(shift)
                response.status = 200
                //Here this will load cash up summary with on hold data because that hasn't save into shift's reconciliationTotals values
                render(template: "cashUpSummaryModal", model: [shift: shift, varianceReasons: TenderReconciliationVarianceReason.values(), safeLocations: safeLocations, isShiftFinalizeMode: false])
            } else if (shift != null && !cashUpCommand.isRecount && shift.getShiftStatus() != ShiftStatus.UNRECONCILED) {
                // Request is for reconcile but already reconciled
                render(status: 400, contentType: 'application/json', message: String.format("Failed to reconcile shift %s. Already reconciled.", cashUpCommand.shiftId))
            } else if (shift != null && cashUpCommand.isRecount && shift.getShiftStatus() != ShiftStatus.RECONCILED) {
                // Request is for recount but already recounted
                render(status: 400, contentType: 'application/json', message: String.format("Failed to recount shift %s. Already recounted.", cashUpCommand.shiftId))
            } else {
                render(status: 400, contentType: 'application/json', message: String.format("Action failed for shift id: %d", cashUpCommand.shiftId))
            }
        } catch (Exception ex) {
            log.error(String.format("Shift cash save error for shift id: %d error: %s", cashUpCommand.shiftId, ex.getMessage()), ex)
            render(status: 400, contentType: 'application/json', message: String.format("Action failed for shift id: %d", cashUpCommand.shiftId))
        }
    }

    // If the request is reconcile, recount or finalise then this is to
    //    1. save shift to temporary save variable `onhold` into actual cash and voucher total's in shift object
    //    2. Add audit entry
    // If the request is for finalise then specifically need to
    //    1. Create safe snapshot
    //    2. update tender movements
    def ajaxSaveShift(SaveShiftCommand saveShiftCommand) {
        try {
            def shift = shiftService.getShift(saveShiftCommand.shiftId, -1, -1)
            if (shift != null && ((!saveShiftCommand.isRecount && !saveShiftCommand.isFinalise && shift.getShiftStatus() == ShiftStatus.UNRECONCILED) || ((saveShiftCommand.isRecount || saveShiftCommand.isFinalise) && shift.getShiftStatus() == ShiftStatus.RECONCILED))) {
                shiftService.processShiftDataSave(saveShiftCommand, shift)
                if (saveShiftCommand.isFinalise) { //Only update this if it is finalized
                    //If any till id added into filter then pass it
                    Integer tillIdFilter = saveShiftCommand.tillIdFilter ? Integer.parseInt(saveShiftCommand.tillIdFilter) : null
                    shiftService.processTakeSnapshot(shift, saveShiftCommand) //Take snapshot
                    shiftService.updateTenderMovement(shift, saveShiftCommand) //Move into update tender movement
                    redirect(action: "ajaxGetShifts", params: [tillId: tillIdFilter, successMessage: String.format("Successfully finalised shift %s.", saveShiftCommand.shiftId)])
                    return
                }
                def safeLocations = shiftService.getSafeLocation(shift)
                //Here this will load cash up summary with actual shift's reconciliationTotals values because that is now confirmed
                render(template: "cashUpSummaryModal", model: [shift: shift,  safeLocations: safeLocations, isShiftFinalizeMode: true])
            } else if (shift != null && !saveShiftCommand.isRecount && !saveShiftCommand.isFinalise && shift.getShiftStatus() != ShiftStatus.UNRECONCILED) {
                // Request is for reconcile but already reconciled
                render(status: 400, contentType: 'application/json', message: String.format("Failed to reconcile shift %s. Already reconciled.", saveShiftCommand.shiftId))
            } else if (shift != null && saveShiftCommand.isRecount && shift.getShiftStatus() != ShiftStatus.RECONCILED) {
                // Request is for recount but already recounted
                render(status: 400, contentType: 'application/json', message: String.format("Failed to recount shift %s. Already recounted.", saveShiftCommand.shiftId))
            } else if (shift != null && saveShiftCommand.isFinalise && shift.getShiftStatus() != ShiftStatus.RECONCILED) {
                // Request is for finalise but already finalised
                render(status: 400, contentType: 'application/json', message: String.format("Failed to finalise shift %s. Already finalised.", saveShiftCommand.shiftId))
            } else {
                render(status: 400, contentType: 'application/json', message: String.format("Action failed for shift id: %d", saveShiftCommand.shiftId))
            }
        } catch (Exception ex) {
            log.error(String.format("Shift reconciliation error for shift id: %d error: %s", saveShiftCommand.shiftId, ex.getMessage()), ex)
            render(status: 400, contentType: 'application/json', message: String.format("Action failed for shift id: %d", saveShiftCommand.shiftId))
        }
    }

    // This is method to functioning action button of shift open
    def ajaxOpenShift() {
        Integer retailerId = null
        Integer storeId = null
        Integer tillId = null
        Integer tillIdFilter = null //If any till id added into filter then pass it
        try {
            shiftService.validateParams(params)
            retailerId = Integer.parseInt(params.retailerId)
            storeId = Integer.parseInt(params.storeId)
            tillId = Integer.parseInt(params.tillId)
            tillIdFilter = params.tillIdFilter ? Integer.parseInt(params.tillIdFilter) : null
            //If any till id added into filter then pass it
            def shift = shiftService.getOpenShift(retailerId, storeId, tillId) //Load existing open shift
            if (shift == null || !(shift.getShiftStatus() == ShiftStatus.OPEN)) {
                // Check shift is null or not open if so then proceed to create new shift
                shift = shiftService.createNewShift(retailerId, storeId, tillId, false) //call function to open shift
                flash.message = String.format("Shift %d has successfully been opened for till %d", shift.getShiftNumber(), tillId)
            } else {
                flash.message = String.format("Till %d's shift was already open", tillId)
            }
        } catch (Exception ex) {
            flash.error = String.format("Till %d's shift open failed", tillId)
            log.error(String.format("Shift create error: %d store: %d tillId: %d error: %s", retailerId, storeId, tillId, ex.getMessage()), ex)
        }
        //Once done redirect to process get shift action
        redirect(action: "ajaxGetShifts", params: [tillId: tillIdFilter, successMessage: flash.message, errorMessage: flash.error])

    }

    // This is method to functioning action button of shift close
    def ajaxCloseShift() {
        Integer retailerId = null
        Integer storeId = null
        Integer tillId = null
        Integer tillIdFilter = null //If any till id added into filter then pass it
        try {
            shiftService.validateParams(params)
            retailerId = Integer.parseInt(params.retailerId)
            storeId = Integer.parseInt(params.storeId)
            tillId = Integer.parseInt(params.tillId)
            int shiftId = params.shiftId ? Integer.parseInt(params.shiftId) : -1
            tillIdFilter = params.tillIdFilter ? Integer.parseInt(params.tillIdFilter) : null
            //If any till id added into filter then pass it
            def shift = shiftService.getShift(shiftId, retailerId, storeId) //Load existing open shift
            if (shift != null && shift.getShiftStatus() == ShiftStatus.OPEN) {
                // Check shift is null or not open if so then proceed to create new shift
                shiftService.processShiftClose(shift) //call function to open shift
                boolean isNewShiftOpen = shiftService.postTillControlEventProcess(shift)
                //Check if shift auto open is configured if yes then open new one
                flash.message = String.format("Shift %d for Till %d has been successfully closed.", shift.getShiftNumber(), tillId)
                if (isNewShiftOpen) {
                    flash.message = String.format("Shift %d for Till %d has been successfully closed, and a new shift has been opened.", shift.getShiftNumber(), tillId)
                }
            } else if (shift != null && !(shift.getShiftStatus() == ShiftStatus.OPEN)) {
                //If there is no open shift mean shift should already be closed
                flash.message = String.format("Shift %d for Till %d has already been closed.", shiftId, tillId)
            }
        } catch (Exception ex) {
            flash.error = String.format("Till %d's shift close failed", tillId)
            log.error(String.format("Shift close error: %d store: %d tillId: %d error: %s", retailerId, storeId, tillId, ex.getMessage()), ex)
        }
        redirect(action: "ajaxGetShifts", params: [tillId: tillIdFilter, successMessage: flash.message, errorMessage: flash.error])
        //Once done redirect to process get shift action
    }

}

class CashUpCommand {

    int shiftId
    boolean isRecount
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
    boolean isRecount
    boolean isFinalise
    Integer safeLocationId
    String tillIdFilter
    TenderReconciliationVarianceReason tenderReconciliationVarianceReason
    String tenderReconciliationVarianceReasonText
}
