package uk.co.wonderlane.wlpos

import groovy.json.JsonOutput
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

            int configuredRetryAttempts = shiftService.getConfiguredRecountAttempts(springSecurityService.principal.retailerId, springSecurityService.principal.storeId)

            render(template: "shiftViewerResults", model: [shiftMap: sortedShiftMap, isFinancialWeekExists: isFinancialWeekExists, lastRefreshDate: new DateTime(), configuredRetryAttempts: configuredRetryAttempts,
                                                           successMessage: successMessage, errorMessage: errorMessage])

        } catch (Exception ex) {
            log.error(String.format("Shift loading error for tillId: %d error: %s", tillId, ex.getMessage()), ex)
            if (errorMessage == null || errorMessage == ''){
                errorMessage = "Unexpected error loading shifts"
            }
            render(template: "shiftViewerResults", model: [errorMessage: errorMessage])
        }
    }

    def ajaxGetCashDetails(int shiftId, boolean isFinalise) {
         try {
             def shift = shiftService.getShift(shiftId, -1, -1)
             if (shift != null && (shift.getShiftStatus() == ShiftStatus.UNRECONCILED || shift.getShiftStatus() == ShiftStatus.RECONCILED)) {
                 if ((shift.getShiftStatus() == ShiftStatus.RECONCILED) && (!shiftService.isShiftRecountAmountNotExceed(shift) || isFinalise)){
                     render(template: "cashUpSummaryModal", model: [shift: shift, isShiftFinalizeMode: true])
                     return
                 }
                 render(template: "cashUpModal", model: [shift: shift])
             }
        } catch (Exception ex) {
             log.error(String.format("Shift cash detail loading error for shift id: %d error: %s", shiftId, ex.getMessage()), ex)
             render (status: 400, contentType: 'application/json', text: JsonOutput.toJson([error: String.format("Action failed for shift id: %d has exceeded", shiftId)]))
        }
    }

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
                    cashUpCommand.cashTotal = cashUpCommand.fiftyPounds + cashUpCommand.twentyPounds + cashUpCommand.tenPounds +
                            cashUpCommand.fivePounds + cashUpCommand.twoPounds + cashUpCommand.onePounds + cashUpCommand.fiftyPences +
                            cashUpCommand.twentyPences + cashUpCommand.tenPences + cashUpCommand.fivePences + cashUpCommand.twoPences +
                            cashUpCommand.onePences
                } else if (cashUpCommand.cashUpBy == "DENOMINATION") { //Switching from denomination to totals.
                    cashUpCommand.cashTotal = cashUpCommand.fiftyPounds * 50 + cashUpCommand.twentyPounds * 20 +
                            cashUpCommand.tenPounds * 10 + cashUpCommand.fivePounds * 5 + cashUpCommand.twoPounds * 2 +
                            cashUpCommand.onePounds * 1 + cashUpCommand.fiftyPences * 0.50 + cashUpCommand.twentyPences * 0.20 +
                            cashUpCommand.tenPences * 0.10 + cashUpCommand.fivePences * 0.05 + cashUpCommand.twoPences * 0.02 +
                            cashUpCommand.onePences * 0.01
                }
            }
            render(template: template, model: [values: cashUpCommand])
        } catch (Exception ex) {
            log.error(String.format("Shift cash up type change error for shift id: %d error: %s", cashUpCommand.shiftId, ex.getMessage()), ex)
            return null
        }
    }

    def ajaxSaveCash(CashUpCommand cashUpCommand) {
        try {
            def shift = shiftService.getShift(cashUpCommand.shiftId, -1, -1)
            if (shift != null && (shift.getShiftStatus() == ShiftStatus.UNRECONCILED || shift.getShiftStatus() == ShiftStatus.RECONCILED)){
                if (shift.getShiftStatus() == ShiftStatus.RECONCILED && !shiftService.isShiftRecountAmountNotExceed(shift)){
                    render(template: "cashUpSummaryModal", model: [shift: shift, isShiftFinalizeMode: true])
                    return
                }
                def safeLocations = locationService.getStoreSafeLocations()
                shiftService.processShiftCashSave(cashUpCommand, shift)
                safeLocations =  shiftService.updateSafeLocation(shift,safeLocations)
                response.status = 200
                //Here this will load cash up summary with on hold data because that hasn't save into shift's reconciliationTotals values
                render(template: "cashUpSummaryModal", model: [ shift: shift, varianceReasons: TenderReconciliationVarianceReason.values(), safeLocations: safeLocations, isShiftFinalizeMode: false ])
            } else if (shift != null && !(shift.getShiftStatus() == ShiftStatus.UNRECONCILED || shift.getShiftStatus() == ShiftStatus.RECONCILED)) {
                render (status: 400, contentType: 'application/json', text: JsonOutput.toJson([error: String.format("Action not allowed for shift id: %d shift status: %s ", cashUpCommand.shiftId, shift.getShiftStatus())]))
            } else {
                render (status: 400, contentType: 'application/json', text: JsonOutput.toJson([error: String.format("Action not allowed for shift id: %d", cashUpCommand.shiftId)]))
            }
        } catch (Exception ex) {
            log.error(String.format("Shift cash save error for shift id: %d error: %s", cashUpCommand.shiftId, ex.getMessage()), ex)
            render(status: 400, contentType: 'application/json', text: "Unable to process shift cash save. Shift may not exist or is not in UNRECONCILED status.")
        }
    }

    def ajaxSaveShift(SaveShiftCommand saveShiftCommand) {
        try {
            def shift = shiftService.getShift(saveShiftCommand.shiftId, -1, -1)
            if (shift != null && (shift.getShiftStatus() == ShiftStatus.UNRECONCILED || shift.getShiftStatus() == ShiftStatus.RECONCILED)){
                shiftService.processShiftDataPopulation(saveShiftCommand, shift)
                if (saveShiftCommand.isFinalise){ //Only update this if it is finalized
                    shiftService.processTakeSnapshot(shift) //Take snapshot
                    shiftService.updateTenderMovement(shift) //Move into update tender movement
                }
                //Here this will load cash up summary with actual shift's reconciliationTotals values because that is now confirmed
                render(template: "cashUpSummaryModal", model: [ shift: shift, isShiftFinalizeMode: true ])
            } else if (shift != null && !(shift.getShiftStatus() == ShiftStatus.UNRECONCILED || shift.getShiftStatus() == ShiftStatus.RECONCILED)) {
                render (status: 400, contentType: 'application/json', text: JsonOutput.toJson([error: String.format("Action not allowed for shift id: %d shift status: %s ", saveShiftCommand.shiftId, shift.getShiftStatus())]))
            } else {
                render (status: 400, contentType: 'application/json', text: JsonOutput.toJson([error: String.format("Action not allowed for shift id: %d", saveShiftCommand.shiftId)]))
            }
        } catch (Exception ex) {
            log.error(String.format("Shift reconciliation error for shift id: %d error: %s", saveShiftCommand.shiftId, ex.getMessage()), ex)
            render(status: 400, contentType: 'application/json', text: "Unable to process shift cash save. Shift may not exist or is not in UNRECONCILED status.")
        }
    }

    def ajaxOpenShift(){ // This is method to functioning action button of shift open
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
            def shift = shiftService.getOpenShift(retailerId, storeId, tillId) //Load existing open shift
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

    def ajaxCloseShift(){ // This is method to functioning action button of shift close
        Integer retailerId = null
        Integer storeId = null
        Integer tillId = null
        Integer tillIdFilter = null //If any till id added into filter then pass it
        try {
            shiftService.validateParams(params)
            retailerId  = Integer.parseInt(params.retailerId)
            storeId  = Integer.parseInt(params.storeId)
            tillId  = Integer.parseInt(params.tillId)
            int shiftId  = params.shiftId ? Integer.parseInt(params.shiftId) : -1
            tillIdFilter  = params.tillIdFilter ? Integer.parseInt(params.tillIdFilter) : null //If any till id added into filter then pass it
            def shift = shiftService.getShift(shiftId, retailerId, storeId) //Load existing open shift
            if (shift != null && shift.getShiftStatus() == ShiftStatus.OPEN) { // Check shift is null or not open if so then proceed to create new shift
                shiftService.processShiftClose(shift) //call function to open shift
                boolean isNewShiftOpen =  shiftService.postTillControlEventProcess(shift) //Check if shift auto open is configured if yes then open new one
                flash.message = String.format("Shift %d for Till %d has been successfully closed.", shift.getId(), tillId)
                if (isNewShiftOpen) {
                    flash.message = String.format("Shift %d for Till %d has been successfully closed, and a new shift has been opened.", shift.getId(), tillId)
                }
            } else if (shift != null && !(shift.getShiftStatus() == ShiftStatus.OPEN)){ //If there is no open shift mean shift should already be closed
                flash.message = String.format("Shift %d for Till %d has already been closed.", shiftId, tillId)
            }
        } catch (Exception ex) {
            flash.error = String.format("Till %d's shift close failed", tillId)
            log.error(String.format("Shift close error: %d store: %d tillId: %d error: %s", retailerId, storeId, tillId, ex.getMessage()), ex)
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
    boolean isRecount
    boolean isFinalise
    Integer safeLocationId
    TenderReconciliationVarianceReason tenderReconciliationVarianceReason
    String tenderReconciliationVarianceReasonText
}
