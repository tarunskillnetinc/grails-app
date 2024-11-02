package uk.co.wonderlane.wlpos

import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import org.joda.time.format.DateTimeFormat
import org.joda.time.format.DateTimeFormatter
import uk.co.wonderlane.wlpos.entities.cash.Shift
import uk.co.wonderlane.wlpos.enums.ReasonCodeType
import uk.co.wonderlane.wlpos.enums.ShiftStatus

import java.text.NumberFormat

class ShiftController {

    def springSecurityService
    def shiftService
    def snapshotService
    def reportingService
    def cashManagementService
    def reasonCodeService
    def safeService

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
                    def safes = safeService.getStoreSafes()
                    def varianceReasons = reasonCodeService.getReasonCodesByType(shift.getRetailerId(), ReasonCodeType.TENDER_RECONCILIATION_VARIANCE)
                    render(template: "cashUpSummaryModal", model: [shift: shift, isShiftFinalizeMode: true, safes: safes, varianceReasons:varianceReasons])
                    return
                }
                render(template: "cashUpModal", model: [shift: shift])
            } else if (shift != null && !isRecount && !isFinalise && shift.getShiftStatus() != ShiftStatus.UNRECONCILED) {
                // Request is for reconcile but already reconciled
                render(status: 400, contentType: 'application/json', message: "Failed to reconcile shift. Already reconciled.")
            } else if (shift != null && isRecount && shift.getShiftStatus() != ShiftStatus.RECONCILED) {
                // Request is for recount but already recounted
                render(status: 400, contentType: 'application/json', message: "Failed to recount shift. Already recounted.")
            } else if (shift != null && isFinalise && shift.getShiftStatus() != ShiftStatus.RECONCILED) {
                // Request is for finalise but already finalised
                render(status: 400, contentType: 'application/json', message: "Failed to finalise shift. Already finalised.")
            } else {
                render(status: 400, contentType: 'application/json', message: "Action failed.")
            }
        } catch (Exception ex) {
            log.error(String.format("Shift cash detail loading error for shift id: %d error: %s", shiftId, ex.getMessage()), ex)
            render(status: 400, contentType: 'application/json', message: "Action failed.")
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

    // This will store values added in cash up model into temporary variable `pending` cash and voucher total's in shift object
    // Secondary this will check any available locations available if not added default `Safe 1` location
    def ajaxSaveCash(CashUpCommand cashUpCommand) {
        try {
            def shift = shiftService.getShift(cashUpCommand.shiftId, -1, -1)
            if (shift != null && ((!cashUpCommand.isRecount && shift.getShiftStatus() == ShiftStatus.UNRECONCILED) || (cashUpCommand.isRecount && shift.getShiftStatus() == ShiftStatus.RECONCILED))) {
                def varianceReasons = reasonCodeService.getReasonCodesByType(shift.getRetailerId(), ReasonCodeType.TENDER_RECONCILIATION_VARIANCE)
                if (shift.getShiftStatus() == ShiftStatus.RECONCILED && !shiftService.isShiftRecountAmountNotExceed(shift)) {
                    def safes = safeService.getStoreSafes()
                    render(template: "cashUpSummaryModal", model: [shift: shift, isShiftFinalizeMode: true, safes: safes, varianceReasons:varianceReasons])
                    return
                }
                shiftService.processShiftCashSave(cashUpCommand, shift)
                def safes = safeService.getStoreSafes()
                def cashManagementConfig = cashManagementService.getCashManagementConfig(shift.getRetailerId(), shift.getStoreId())
                def tillShiftVarianceLimit = cashManagementConfig?new BigDecimal(cashManagementConfig.getTillShiftVarianceLimit()).movePointLeft(2):0.00
                response.status = 200
                //Here this will load cash up summary with on hold data because that hasn't save into shift's reconciliationTotals values
                render(template: "cashUpSummaryModal", model: [shift: shift, varianceReasons: varianceReasons, safes: safes, isShiftFinalizeMode: false,
                                                               tillShiftVarianceLimit : tillShiftVarianceLimit])
            } else if (shift != null && !cashUpCommand.isRecount && shift.getShiftStatus() != ShiftStatus.UNRECONCILED) {
                // Request is for reconcile but already reconciled
                render(status: 400, contentType: 'application/json', message: "Failed to reconcile shift. Already reconciled.")
            } else if (shift != null && cashUpCommand.isRecount && shift.getShiftStatus() != ShiftStatus.RECONCILED) {
                // Request is for recount but already recounted
                render(status: 400, contentType: 'application/json', message: "Failed to recount shift. Already recounted.")
            } else {
                render(status: 400, contentType: 'application/json', message: "Action failed for shift.")
            }
        } catch (Exception ex) {
            log.error(String.format("Shift cash save error for shift id: %d error: %s", cashUpCommand.shiftId, ex.getMessage()), ex)
            render(status: 400, contentType: 'application/json', message: "Action failed for shift.")
        }
    }

    // If the request is reconcile, recount or finalise then this is to
    //    1. save shift to temporary save variable `pending` into actual cash and voucher total's in shift object
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
                    def cashManagementConfig = cashManagementService.getCashManagementConfig(shift.getRetailerId(), shift.getStoreId())
                    if (shift.reconciliationTotals.sum{ it.variance.abs() } > cashManagementConfig.tillShiftVarianceLimit &&
                            (saveShiftCommand.tenderReconciliationVarianceReason == null || saveShiftCommand.tenderReconciliationVarianceReason.isEmpty())) {
                        log.warn("No VarianceReason configured or selected.")
                    }
                    //If any till id added into filter then pass it
                    Integer tillIdFilter = saveShiftCommand.tillIdFilter ? Integer.parseInt(saveShiftCommand.tillIdFilter) : null
                    shiftService.processTakeSnapshot(shift, saveShiftCommand.safeLocationId) //Take snapshot
                    shiftService.updateFinaliseTenderMovement(shift, saveShiftCommand.safeLocationId) //Move into update tender movement
                    redirect(action: "ajaxGetShifts", params: [tillId: tillIdFilter, successMessage: String.format("Successfully finalised shift %d for till %d.", shift.getShiftNumber(), shift.getTillId())])
                    return
                }
                def safes = safeService.getStoreSafes()
                def varianceReasons = reasonCodeService.getReasonCodesByType(shift.getRetailerId(), ReasonCodeType.TENDER_RECONCILIATION_VARIANCE)
                //Here this will load cash up summary with actual shift's reconciliationTotals values because that is now confirmed
                render(template: "cashUpSummaryModal", model: [shift: shift,  safes: safes, isShiftFinalizeMode: true, varianceReasons:varianceReasons])
            } else if (shift != null && !saveShiftCommand.isRecount && !saveShiftCommand.isFinalise && shift.getShiftStatus() != ShiftStatus.UNRECONCILED) {
                // Request is for reconcile but already reconciled
                render(status: 400, contentType: 'application/json', message: "Failed to reconcile shift. Already reconciled.")
            } else if (shift != null && saveShiftCommand.isRecount && shift.getShiftStatus() != ShiftStatus.RECONCILED) {
                // Request is for recount but already recounted
                render(status: 400, contentType: 'application/json', message: "Failed to recount shift. Already recounted.")
            } else if (shift != null && saveShiftCommand.isFinalise && shift.getShiftStatus() != ShiftStatus.RECONCILED) {
                // Request is for finalise but already finalised
                render(status: 400, contentType: 'application/json', message: "Failed to finalise shift. Already finalised.")
            } else {
                render(status: 400, contentType: 'application/json', message: "Action failed for shift.")
            }
        } catch (Exception ex) {
            log.error(String.format("Shift reconciliation error for shift id: %d error: %s", saveShiftCommand.shiftId, ex.getMessage()), ex)
            render(status: 400, contentType: 'application/json', message: "Action failed for shift.")
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

                boolean isDirectShiftFinalise = false
                //Directly process for reconcile and finalise actions if cash management flag is set
                if (shiftService.isCashManagementEnable(shift.tillId)){
                    Safe primarySafe = safeService.getPrimaryStoreSafes()
                    if (primarySafe !+ null) {
                        int primarySafeId = primarySafe.getId()
                        shiftService.processShiftReconcile(shift)
                        shiftService.processShiftFinalise(shift, primarySafeId)
                        isDirectShiftFinalise = true
                    } else {
                        log.warn("Direct reconcile and finalise process skipped for shift ${shift.shiftNumber}. No primary safe configured for the store.");
                    }
                }

                //Check if shift auto open is configured if yes then open new one
                boolean isNewShiftOpen = shiftService.handleShiftAutoOpen(shift)

                //Construct flash messages appropriately
                def messageBuilder = new StringBuilder()
                messageBuilder.append("Shift ${shift.getShiftNumber()} for Till ${tillId} has been successfully closed.")
                if (isDirectShiftFinalise) {messageBuilder.append(" The shift was reconciled and finalized directly.")}
                if (isNewShiftOpen) {messageBuilder.append(" A new shift has been opened.")}
                flash.message = messageBuilder.toString()

            } else if (shift != null && !(shift.getShiftStatus() == ShiftStatus.OPEN)) {
                //If there is no open shift mean shift should already be closed
                flash.message = String.format("Shift %d for Till %d has already been closed.", shift.getShiftNumber(), tillId)
            }
        } catch (Exception ex) {
            flash.error = String.format("Till %d's shift close failed", tillId)
            log.error(String.format("Shift close error: %d store: %d tillId: %d error: %s", retailerId, storeId, tillId, ex.getMessage()), ex)
        }
        //Once done redirect to process get shift action
        redirect(action: "ajaxGetShifts", params: [tillId: tillIdFilter, successMessage: flash.message, errorMessage: flash.error])
    }

    // This is method to spot check this will popup dialog box which have values each tender types
    def ajaxSpotCheck(){
        Integer retailerId = null
        Integer storeId = null
        Integer tillId = null
        Integer shiftId = null
        try {
            shiftService.validateParams(params)
            retailerId = Integer.parseInt(params.retailerId)
            storeId = Integer.parseInt(params.storeId)
            tillId = Integer.parseInt(params.tillId)
            shiftId = params.shiftId ? Integer.parseInt(params.shiftId) : -1
            def shift = shiftService.getShift(shiftId, retailerId, storeId) //Load existing open shift
            if (shift != null) { // If shift not exists then process the action
                shiftService.addSpotCheckAudit(shift) // Add audit for spot check
                render(template: "spotCheck", model: [shift: shift, fetchTime: new DateTime()]) //Load spot check template
            } else {
                render(status: 400, contentType: 'application/json', message: String.format("Spot check action failed. Shift not available anymore for till id: %d ", tillId))
            }
        } catch (Exception ex) {
            log.error(String.format("Spot check error for shift id: %d retailer id: %d till id: %d and for store id: %d error: %s", shiftId, retailerId, tillId, storeId, ex.getMessage()), ex)
            render(status: 400, contentType: 'application/json', message: String.format("Action failed for spot check for till id: %d ", tillId))
        }
    }

    //This will load either Add Float or Cash Lift popup based on button we clicked
    def ajaxCashUpdateModal(){
        boolean isAddFloat = false
        Integer retailerId = null
        Integer storeId = null
        Integer tillId = null
        Integer shiftId = null
        try {
            isAddFloat = Boolean.parseBoolean(params.isAddFloat)
            shiftService.validateParams(params)
            retailerId = Integer.parseInt(params.retailerId)
            storeId = Integer.parseInt(params.storeId)
            tillId = Integer.parseInt(params.tillId)
            shiftId = params.shiftId ? Integer.parseInt(params.shiftId) : -1
            BigDecimal cashAmount = params.cashAmount ? new BigDecimal(params.cashAmount) : null
            BigDecimal voucherAmount = params.voucherAmount ? new BigDecimal(params.voucherAmount) : null
            Integer safeId = params.safeId ? Integer.parseInt(params.safeId) : -1
            String error = params.error
            List<Safe> safeLocations = safeService.getStoreSafes() ?.findAll { it.active }
            if (safeLocations == null || safeLocations.isEmpty()) {
                flash.error = "No available safe. Please add safe and retry"
                throw new RuntimeException("No safe locations are configured.")
            }
            Safe primarySafe = safeLocations.find { it.primary }
            if (safeId > 0) {
                primarySafe = safeService.getSafeById(safeId)
            }
            render(template: "cashUpdateModal", model: [isAddFloat: isAddFloat, retailerId: retailerId, storeId: storeId,
                                                        tillId: tillId, shiftId: shiftId, safeLocations: safeLocations, primarySafe: primarySafe,
                                                        cashAmount:cashAmount, voucherAmount:voucherAmount , error: error])
        } catch (Exception ex) {
            log.error(String.format("${isAddFloat ? 'Add float ' : 'Cash lift '} modal loading error for shift id: %d retailer id: %d till id: %d and for store id: %d error: %s", shiftId, retailerId, tillId, storeId, ex.getMessage()), ex)
            String error =  "${isAddFloat ? 'Add float ' : 'Cash lift '} action failed. "
            if (flash.error) {
                error = error + flash.error
            }
            render(status: 400, contentType: 'application/json', message: error)
        }
    }

    // This will update cash based on add float and cash lift
    def ajaxSaveCashUpdate(){
        boolean isAddFloat = false
        Integer retailerId = null
        Integer storeId = null
        Integer tillId = null
        Integer shiftId = null
        BigDecimal cashAmount = null
        BigDecimal voucherAmount = null
        Integer safeId = null
        try {
            NumberFormat format = NumberFormat.getInstance(Locale.UK)
            isAddFloat = Boolean.parseBoolean(params.isAddFloat)
            shiftService.validateParams(params)
            retailerId = Integer.parseInt(params.retailerId)
            storeId = Integer.parseInt(params.storeId)
            tillId = Integer.parseInt(params.tillId)
            shiftId = params.shiftId ? Integer.parseInt(params.shiftId) : -1
            safeId = params.safeId ? Integer.parseInt(params.safeId) : -1
            cashAmount = params.cashTotal ? new BigDecimal(format.parse(params.cashTotal)?.toString()) : BigDecimal.ZERO
            voucherAmount = params.vouchersTotal ? new BigDecimal(format.parse(params.vouchersTotal)?.toString()) : BigDecimal.ZERO
            def shift = shiftService.getShift(shiftId, retailerId, storeId) //Load existing open shift
            if (shift != null) { // If shift not exists then process the action
                // Process save cash update based on cash lift and add float logic
                // This will
                // 1. Update shift balances
                //    (If add float -> add cash and voucher amounts in tender and cash drawer)
                //    (If cash lift -> deduct cash amounts in tender and cash drawer)
                // 2. Update snapshot balances
                //    (If add float -> deduct cash and voucher amounts from totals)
                //    (If cash lift -> add cash amounts from totals)
                // 3. Create tender movements
                // 4. Add audit
                if (snapshotService.getSnapshotForSafe(safeId)) {
                    shiftService.processShiftCashUpdate(shift, isAddFloat, cashAmount, voucherAmount, safeId)
                    render "OK"
                } else {
                    flash.error = "No snapshot available for safe id ${safeId}"
                    throw new RuntimeException("No snapshot location available for safe id ${safeId}")
                }
            } else {
                flash.error = "No shift exists anymore"
                throw new RuntimeException("No shift exists anymore")
            }
        } catch (Exception ex) {
            log.error(String.format("${isAddFloat ? 'Add float ' : 'Cash lift '} saving error for shift id: %d retailer id: %d till id: %d and for store id: %d error: %s", shiftId, retailerId, tillId, storeId, ex.getMessage()), ex)
            String error =  "${isAddFloat ? 'Add float ' : 'Cash lift '} action failed. "
            if (flash.error) {
                error = error + flash.error
            }
            redirect(action: "ajaxCashUpdateModal", params: [tillId: tillId, isAddFloat: isAddFloat, retailerId: retailerId, storeId: storeId, shiftId: shiftId, safeId: safeId, cashAmount: cashAmount, voucherAmount: voucherAmount, error: error])
        }
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
    String tenderReconciliationVarianceReason
    String tenderReconciliationVarianceReasonText
}
