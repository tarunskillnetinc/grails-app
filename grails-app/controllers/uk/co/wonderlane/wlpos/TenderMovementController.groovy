package uk.co.wonderlane.wlpos

import groovy.json.JsonOutput
import uk.co.wonderlane.wlpos.entities.cash.SafeSession
import uk.co.wonderlane.wlpos.entities.cash.Shift
import uk.co.wonderlane.wlpos.entities.cash.TenderTotal
import uk.co.wonderlane.wlpos.enums.ReasonCodeType
import uk.co.wonderlane.wlpos.enums.SafeSessionAction
import uk.co.wonderlane.wlpos.enums.ShiftAction
import uk.co.wonderlane.wlpos.enums.TenderMovementType
import uk.co.wonderlane.wlpos.enums.TenderType

import java.text.NumberFormat

class TenderMovementController {

    def tenderMovementService
    def safeService
    def shiftService
    def reasonCodeService
    def springSecurityService
    def safeManagementService

    private static final BigDecimal MIN_AMOUNT_PAYOUT = new BigDecimal("0.01")
    private static final BigDecimal MAX_AMOUNT_PAYOUT = new BigDecimal("9999.99")

    def index() {}

    def issueFloat(){
        String success = params.success
        String error = params.error
        List<Safe> safeLocations = safeService.getStoreSafes() ?.findAll { it.active }
        Safe primarySafe = safeLocations?.find { it.primary }
        // Place primary safe at the top and sort remaining safes by id
        if (primarySafe) {
            safeLocations = [primarySafe] + (safeLocations - primarySafe)?.sort { it.id }
        } else {
            safeLocations = safeLocations?.sort { it.id }
        }
        //Load and return tills having  open shift + Cash management enable + Serial number available
        List<TillConfiguration> tills =  tenderMovementService.returnAllActiveOpenTills()
        List<TenderType> tenders = tenderMovementService.getEligibleTendersForTenderLift()
        [safeLocations: safeLocations, primarySafe: primarySafe, tills: tills, tenders:tenders, success: success, error: error]
    }

    def tenderLift(){
        String success = params.success
        String error = params.error
        List<Safe> safeLocations = safeService.getStoreSafes() ?.findAll { it.active }
        Safe primarySafe = safeLocations?.find { it.primary }
        // Place primary safe at the top and sort remaining safes by id
        if (primarySafe) {
            safeLocations = [primarySafe] + (safeLocations - primarySafe)?.sort { it.id }
        } else {
            safeLocations = safeLocations?.sort { it.id }
        }
        //Load and return tills having  open shift + Cash management enable + Serial number available
        List<TillConfiguration> tills =  tenderMovementService.returnAllActiveOpenTills()
        List<TenderType> tenders = tenderMovementService.getEligibleTendersForTenderLift()
        [safeLocations: safeLocations, primarySafe: primarySafe, tills: tills, tenders:tenders, success: success, error: error]
    }

    def payIn(){}

    def payOut(){
        String success = params.success
        String error = params.error
        List<Safe> safes = safeService.getStoreSafes() ?.findAll { it.active }
        Safe primarySafe = safes?.find { it.primary }
        // Place primary safe at the top and sort remaining safes by id
        if (primarySafe) {
            safes = [primarySafe] + (safes - primarySafe)?.sort { it.id }
        } else {
            safes = safes?.sort { it.id }
        }
        def varianceReasons = reasonCodeService.getReasonCodesByType(springSecurityService.principal.retailerId, ReasonCodeType.PAID_OUT)
        List<TenderType> tenders = tenderMovementService.getEligibleTendersForPayOut()
        [safes: safes, primarySafe: primarySafe, tenders:tenders, varianceReasons:varianceReasons, success: success, error: error]
    }

    def bankDeposit(){}

    def bankReceipt(){}

    def getTillAvailableBalance() {
        try {
            TenderType tender = TenderType.valueOf(params.tender)
            List<Integer> tillNos = tenderMovementService.returnRequestedTillIds(params)
            BigDecimal enteredAmount = new BigDecimal(params.enteredAmount) //Get entered amount

            BigDecimal totalAvailableBalance = BigDecimal.ZERO
            boolean isTillAmountLessThanEntered = false
            List<String> errorMessages = []

            //Check balances and till exists
            for (tillNo in tillNos) {
                Shift shift = shiftService.getOpenShift(springSecurityService.principal.retailerId, springSecurityService.principal.storeId, tillNo)
                if (shift != null) {
                    BigDecimal tenderValue = shift.getTenderTotals().stream().filter(tt -> tt.getTenderType() == tender).findFirst()
                            .map(TenderTotal::getValue).orElse(BigDecimal.ZERO)
                    if (tenderValue.compareTo(enteredAmount) < 0) {
                        isTillAmountLessThanEntered = true
                        totalAvailableBalance = tenderValue
                    }
                } else {
                    errorMessages.add("Tills shift for till no ${tillNo} not in progress status to perform tender lift")
                }
            }

            // Prepare the response
            def response = [
                    success: errorMessages.isEmpty(),
                    availableAmount: totalAvailableBalance,
                    isTillAmountLessThanEntered: isTillAmountLessThanEntered,
                    errorMessages: errorMessages
            ]

            // Convert response to JSON string
            String jsonResponse = JsonOutput.toJson(response)

            // Return as plain JSON string with appropriate status code
            if (!errorMessages.isEmpty()) {
                render(status: 500, contentType: 'application/json', text: jsonResponse)
            } else {
                render(contentType: 'application/json', text: jsonResponse)
            }
        } catch (Exception ex) {
            render(status: 500, text: "Error fetching till balance: ${ex.message}")
        }
    }

    def processTenderLift(){
        Integer safeId = null
        Integer tillId = null
        TenderType tender = null
        try {
            NumberFormat format = NumberFormat.getInstance(Locale.UK)
            safeId = params.safeId ? Integer.parseInt(params.safeId) : -1
            tillId = Integer.parseInt(params.tillNo)
            tender = TenderType.valueOf(params.tender)
            BigDecimal amount = params.amount ? new BigDecimal(format.parse(params.amount)?.toString()) : BigDecimal.ZERO

            if (!tenderMovementService.isOpenShiftAvailable(tillId)){
                redirect(action: "tenderLift", params: [error: "Tills shift for till no ${tillId} not in progress status to perform tender lift"])
            } else if (!tenderMovementService.isSafeActive(safeId)){
                redirect(action: "tenderLift", params: [error: "Selected safe not active please try with another"])
            } else {
                //create tender totals
                Integer tenderMovementId = tenderMovementService.tenderMovementUpdate(tillId, safeId, TenderMovementType.CASH_LIFT, tender, amount)

                //update safe session values
                //update safe session tender totals
                //add safe session audit
                tenderMovementService.updateSafeSessionBalanceTotals(SafeSessionAction.CASH_LIFT, tender, amount, tenderMovementId, safeId)

                //update shift values
                //update shift cash in drawer
                //update shift tender totals
                //add shift audit
                tenderMovementService.updateShiftBalanceTotals(ShiftAction.CASH_LIFT, tender, amount.negate(), tenderMovementId, tillId)

                redirect(action: "tenderLift", params: [success: "Successfully process tender lift for till ${tillId}"])
            }
        } catch (Exception ex) {
            log.error("Tender lift saving error for safe id : ${safeId} till id: ${tillId} tender type: ${tender} error: ${ex.getMessage()}", ex)
            String error =  "Tender lift action failed. "
            redirect(action: "tenderLift", params: [error: error])
        }
    }

    def getSafeAvailableBalance() {
        try {
            TenderType tender = TenderType.valueOf(params.tender)
            BigDecimal totalAmountToBeDistributed = new BigDecimal(params.totalAmountToBeDistributed) //Get entered amount
            Integer safeId = params.safeId ? Integer.parseInt(params.safeId) : -1

            BigDecimal totalAvailableBalance = BigDecimal.ZERO
            boolean isSafeAmountLessThanEntered = false
            List<String> errorMessages = []

            SafeSession safeSession = safeManagementService.getActiveSession(safeId)
            if (safeSession != null) {
                BigDecimal amountLeftInSafeSession = safeSession.getTenderTotals().stream().filter(tt -> tt.getTenderType() == tender).findFirst()
                        .map(TenderTotal::getValue).orElse(BigDecimal.ZERO)
                if (totalAmountToBeDistributed > amountLeftInSafeSession) {
                    isSafeAmountLessThanEntered = true
                }
                totalAvailableBalance = amountLeftInSafeSession

            } else {
                errorMessages.add("No active safe session found for selected safe")
            }

            // Prepare the response
            def response = [
                    success: errorMessages.isEmpty(),
                    availableAmount: totalAvailableBalance,
                    isSafeAmountLessThanEntered: isSafeAmountLessThanEntered,
                    errorMessages: errorMessages
            ]

            // Convert response to JSON string
            String jsonResponse = JsonOutput.toJson(response)

            // Return as plain JSON string with appropriate status code
            if (!errorMessages.isEmpty()) {
                render(status: 500, contentType: 'application/json', text: jsonResponse)
            } else {
                render(contentType: 'application/json', text: jsonResponse)
            }
        } catch (Exception ex) {
            render(status: 500, text: "Error fetching till balance: ${ex.message}")
        }
    }

    def processIssueFloat(){
        Integer safeId = null
        TenderType tender = null
        try {
            NumberFormat format = NumberFormat.getInstance(Locale.UK)
            safeId = params.safeId ? Integer.parseInt(params.safeId) : -1
            tender = TenderType.valueOf(params.tender)
            BigDecimal amount = params.amount ? new BigDecimal(format.parse(params.amount)?.toString()) : BigDecimal.ZERO
            List<Integer> tillNos = tenderMovementService.returnRequestedTillIds(params)
            if (!tenderMovementService.isSafeActive(safeId)) { //If safe trying to distribute money is inactive then throw error
                redirect(action: "issueFloat", params: [error: "Selected safe not active please try with another"])
                return
            }

            String shiftOpenError = tenderMovementService.checkOpenShiftAvailability(tillNos)
            if (!shiftOpenError.isEmpty()) { //Check if any selected tills have close shifts
                redirect(action: "issueFloat", params: [error: shiftOpenError])
            } else {
                List<Integer> failedTills = new ArrayList<>()
                for (Integer tillId : tillNos) {
                    try {
                        //create tender totals
                        Integer tenderMovementId = tenderMovementService.tenderMovementUpdate(tillId, safeId, TenderMovementType.ADD_FLOAT, tender, amount)

                        //update safe session values
                        //update safe session tender totals
                        //add safe session audit
                        tenderMovementService.updateSafeSessionBalanceTotals(SafeSessionAction.ADD_FLOAT, tender, amount.negate(), tenderMovementId, safeId)

                        //update shift values
                        //update shift cash in drawer
                        //update shift tender totals
                        //add shift audit
                        tenderMovementService.updateShiftBalanceTotals(ShiftAction.ADD_FLOAT, tender, amount, tenderMovementId, tillId)
                    } catch (Exception ex) {
                        log.error("Issue float item saving error for safe id : ${safeId} till id: ${tillId} tender type: ${tender} error: ${ex.getMessage()}", ex)
                        failedTills.add(tillId)
                    }
                }

                if (!failedTills.isEmpty()) {
                    String failedTillNumbers = failedTills.join(", ")
                    String error = "Failed to update balances for till no ${failedTillNumbers}"
                    redirect(action: "issueFloat", params: [error: error])
                } else {
                    redirect(action: "issueFloat", params: [success: "Successfully processed issue float"])
                }
            }
        } catch (Exception ex) {
            log.error("Issue float action failed error for safe id : ${safeId}  tender type: ${tender} error: ${ex.getMessage()}", ex)
            String error =  "Issue float action failed. "
            redirect(action: "issueFloat", params: [error: error])
        }
    }

    def processPayOut() {
        Integer safeId = null
        TenderType tender = null
        String reasonCode = null

        try {
            NumberFormat format = NumberFormat.getInstance(Locale.UK)
            safeId = params.safeId ? Integer.parseInt(params.safeId) : -1
            tender = TenderType.valueOf(params.tender)
            reasonCode = params.reasonCode
            BigDecimal amount = params.amount ? new BigDecimal(format.parse(params.amount)?.toString()) : BigDecimal.ZERO
            if (!isAPayOutValidAmount(amount)) {
                def errorMessage = "Payout amount must be between ${MIN_AMOUNT_PAYOUT} and ${MAX_AMOUNT_PAYOUT}."
                log.error(errorMessage)
                redirect(action: "payOut", params: [error: errorMessage])
            } else if (!tenderMovementService.isSafeActive(safeId)){
                redirect(action: "payOut", params: [error: "Selected safe not active please try with another"])
            } else if(!isValidReasonCode(reasonCode)) {
                redirect(action: "payOut", params: [error: "Invalid reason code."])
            } else if (tender != TenderType.CASH) {
                redirect(action: "payOut", params: [error: "Invalid tender type."])
            } else {
                Integer tenderMovementId = tenderMovementService.tenderMovementUpdate(safeId.intValue(),
                        TenderMovementType.PAID_OUT, tender, reasonCode, amount)

                //update safe session values
                //update safe session tender totals
                //add safe session audit
                tenderMovementService.updateSafeSessionBalanceTotals(SafeSessionAction.CASH_LIFT, tender,
                        amount.negate(), tenderMovementId, safeId)

                redirect(action: "payOut", params: [success: "Pay Out successfully processed. Funds deducted from safe."])
            }
        } catch (Exception ex) {
            def errorMessage = "Payout amount must be between ${MIN_AMOUNT_PAYOUT} and ${MAX_AMOUNT_PAYOUT}."
            log.error("Pay Out saving error for safe id : ${safeId} tender type: ${tender} reason code: ${reasonCode}  error: ${ex.getMessage()}", ex)
            String error =  "Pay Out action failed. "
            redirect(action: "payOut", params: [error: error])
        }

    }


    private boolean isAPayOutValidAmount(BigDecimal amount) {
        amount >= MIN_AMOUNT_PAYOUT && amount <= MAX_AMOUNT_PAYOUT
    }

    private def isValidReasonCode(String code) {
        def varianceReasons = reasonCodeService.getReasonCodesByType(springSecurityService.principal.retailerId, ReasonCodeType.PAID_OUT)
        return varianceReasons.stream()
                .anyMatch(reasonCode -> reasonCode.getCode() != null &&
                        reasonCode.getCode().equals(code));
    }

}
