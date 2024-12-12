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

    private static final MIN_AMOUNT_BANK_DEPOSIT_RECEIPT = new BigDecimal("0.01")
    private static final MAX_AMOUNT_BANK_DEPOSIT_RECEIPT = new BigDecimal("999999.99")

    def tenderMovementService
    def shiftService
    def reasonCodeService
    def springSecurityService
    def safeManagementService

    private static final BigDecimal MIN_AMOUNT_PAYOUT = new BigDecimal("0.01")
    private static final BigDecimal MAX_AMOUNT_PAYOUT = new BigDecimal("9999.99")

    private static final BigDecimal MIN_AMOUNT_PAYIN = new BigDecimal("0.01")
    private static final BigDecimal MAX_AMOUNT_PAYIN = new BigDecimal("9999.99")

    def index() {}

    def issueFloat(){
        String success = params.success
        String error = params.error
        def (List<Safe> safeLocations, Safe primarySafe) = tenderMovementService.fetchSafeLocations()
        //Load and return tills having  open shift + Cash management enable + Serial number available
        List<TillConfiguration> tills =  tenderMovementService.returnAllActiveOpenTills()
        List<TenderType> tenders = tenderMovementService.getEligibleTendersForTenderUpdate()
        [safeLocations: safeLocations, primarySafe: primarySafe, tills: tills, tenders:tenders, success: success, error: error]
    }

    def tenderLift(){
        String success = params.success
        String error = params.error
        def (List<Safe> safeLocations, Safe primarySafe) = tenderMovementService.fetchSafeLocations()
        //Load and return tills having  open shift + Cash management enable + Serial number available
        List<TillConfiguration> tills =  tenderMovementService.returnAllActiveOpenTills()
        List<TenderType> tenders = tenderMovementService.getEligibleTendersForTenderUpdate()
        [safeLocations: safeLocations, primarySafe: primarySafe, tills: tills, tenders:tenders, success: success, error: error]
    }

    def payIn(){
        String success = params.success
        String error = params.error
        def (List<Safe> safeLocations, Safe primarySafe) = tenderMovementService.fetchSafeLocations()
        //Load and return tills having  open shift + Cash management enable + Serial number available
        List<TillConfiguration> tills =  tenderMovementService.returnAllActiveOpenTills()
        List<TenderType> tenders = tenderMovementService.getCashOnlyTenders()
        List<ReasonCode> reasonCodes = reasonCodeService.getReasonCodesByType(springSecurityService.principal.retailerId, ReasonCodeType.PAID_IN)
        [safeLocations: safeLocations, primarySafe: primarySafe, tills: tills, tenders:tenders, reasonCodes:reasonCodes, success: success, error: error]
    }

    def payOut(){
        String success = params.success
        String error = params.error
        def (List<Safe> safeLocations, Safe primarySafe) = tenderMovementService.fetchSafeLocations()
        def varianceReasons = reasonCodeService.getReasonCodesByType(springSecurityService.principal.retailerId, ReasonCodeType.PAID_OUT)
        List<TenderType> tenders = tenderMovementService.getEligibleTendersForPayOut()
        [safes: safeLocations, primarySafe: primarySafe, tenders:tenders, varianceReasons:varianceReasons, success: success, error: error]
    }

    def bankDeposit(){
        String success = params.success
        String error = params.error
        def (List<Safe> safeLocations, Safe primarySafe) = tenderMovementService.fetchSafeLocations()
        List<TenderType> tenders = tenderMovementService.getCashTenders()
        [safes: safeLocations, primarySafe: primarySafe, tenders:tenders, success: success, error: error]
    }

    def bankReceipt(){
        String success = params.success
        String error = params.error
        def (List<Safe> safeLocations, Safe primarySafe) = tenderMovementService.fetchSafeLocations()
        List<TenderType> tenders = tenderMovementService.getCashTenders()
        [safes: safeLocations, primarySafe: primarySafe, tenders:tenders, success: success, error: error]
    }

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

    def processPayIn() {
        Integer safeId = null
        Integer tillId = null
        TenderType tender = null
        ReasonCode reasonCode = null
        Safe safe = null
        try {
            NumberFormat format = NumberFormat.getInstance(Locale.UK)
            safeId = params.safeId ? Integer.parseInt(params.safeId) : -1

            tender = TenderType.valueOf(params.tender)
            BigDecimal amount = params.amount ? new BigDecimal(format.parse(params.amount)?.toString()) : BigDecimal.ZERO

            // Check amount is within range.
            BigDecimal maxValue = BigDecimal.valueOf(MAX_AMOUNT_PAYIN)
            BigDecimal minValue = BigDecimal.valueOf(MIN_AMOUNT_PAYIN)
            if (amount < minValue) {
                redirect(action: "payIn", params: [error: "Tender value cannot be less than ${minValue}"])
            } else if (amount > maxValue) {
                redirect(action: "payIn", params: [error: "Tender value cannot be more than ${maxValue}"])
            }

            reasonCode = ReasonCode.findByIdAndRetailerId(params.reasoncodeId ? Integer.parseInt(params.reasoncodeId) : -1, springSecurityService.principal.retailerId) // validate the reasoncode exists within this retailer
            safe = Safe.findByIdAndRetailerId(params.safeId, springSecurityService.principal.retailerId) // validate the safe exists and is a safe within this retailer.

            if( reasonCode == null ) {
                redirect(action: "payIn", params: [error: "Selected reason code doesnt exist."])
            } else if( safe == null ) {
                redirect(action: "payIn", params: [error: "Selected safe doesnt exist."])
            } else if (!tenderMovementService.isSafeActive(safeId)){
                redirect(action: "payIn", params: [error: "Selected safe not active please try with another."])
            } else {
                //create tender totals
                Integer tenderMovementId = tenderMovementService.tenderMovementUpdate(safeId, TenderMovementType.PAID_IN, tender, reasonCode.description, amount)

                //update safe session values
                //update safe session tender totals
                //add safe session audit
                tenderMovementService.updateSafeSessionBalanceTotals(SafeSessionAction.PAID_IN, tender, amount, tenderMovementId, safeId)

                redirect(action: "payIn", params: [success: "Pay In successfully processed. Funds added to safe '${safe?.description}'"])
            }
        } catch (Exception ex) {
            ex.printStackTrace()
            log.error("Pay In saving error for safe id : ${safeId} reason code: ${reasonCode} tender type: ${tender} error: ${ex.getMessage()}", ex)
            String error =  "Pay In action failed. "
            redirect(action: "payIn", params: [error: error])
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
                redirect(action: "tenderLift", params: [error: "No open shift available for till ${tillId}"])
            } else if (!tenderMovementService.isSafeActive(safeId)){
                redirect(action: "tenderLift", params: [error: "Selected safe is not active please try with another"])
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

            //This will done all validations
            //1. Validate safe is selected
            //2. Validate enter amount is correct
            //3. Validate any selected tills
            //4. Validate tender is selected
            //5. Validate selected safe is active
            //6. Validate selected tills have open shift
            List<String> validationFailureMessages = tenderMovementService.preValidateIssueFloatRequest(safeId, tillNos, amount, tender)
            if (!validationFailureMessages.isEmpty() && validationFailureMessages.size() > 0) { //If safe trying to distribute money is inactive then throw error
                def errorParams  = validationFailureMessages.join("<br>")
                redirect(action: "issueFloat", params: [error: errorParams])
            } else {
                List<String> tillSuccessMessages = []
                List<String> tillFailureMessages = []
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

                        tillSuccessMessages.add("Successfully processed issue float for Till ${tillId}")
                    } catch (Exception ex) {
                        log.error("Issue float item saving error for safe id : ${safeId} till id: ${tillId} tender type: ${tender} error: ${ex.getMessage()}", ex)
                        failedTills.add(tillId)
                        tillFailureMessages.add("Failed to update balances for Till ${tillId}")
                    }
                }

                // Combine success and failure messages
                def resultParams = [:]
                if (!tillSuccessMessages.isEmpty()) {
                    resultParams.success = tillSuccessMessages.join("<br>")
                }
                if (!tillFailureMessages.isEmpty()) {
                    resultParams.error = tillFailureMessages.join("<br>")
                }

                redirect(action: "issueFloat", params: [success: resultParams.success, error: resultParams.error])
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

            def varianceReasons = reasonCodeService.getReasonCodesByType(springSecurityService.principal.retailerId, ReasonCodeType.PAID_OUT)

            if (!isAPayOutValidAmount(amount)) {
                def errorMessage = "Payout amount must be between ${MIN_AMOUNT_PAYOUT} and ${MAX_AMOUNT_PAYOUT}."
                log.error(errorMessage)
                redirect(action: "payOut", params: [error: errorMessage])
            } else if (!tenderMovementService.isSafeActive(safeId)){
                redirect(action: "payOut", params: [error: "Selected safe not active please try with another"])
            } else if(!isValidReasonCode(reasonCode, varianceReasons)) {
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

    private def isValidReasonCode(String code, List<ReasonCode> varianceReasons) {
        return varianceReasons.stream()
                .anyMatch(reasonCode -> reasonCode.getCode() != null &&
                        reasonCode.getCode().equals(code));
    }

    def processBankDeposit() {
        Integer safeId = null
        TenderType tender = null
        try {
            NumberFormat format = NumberFormat.getInstance(Locale.UK)
            safeId = params.safeId ? Integer.parseInt(params.safeId) : -1
            tender = TenderType.valueOf(params.tender)
            String bankingDate = params.bankingDate
            String bank = params.bank
            String bagReferenceNumber = params.bagReferenceNumber
            String comments = params.comments
            BigDecimal amount = params.amount ? new BigDecimal(format.parse(params.amount)?.toString()) : BigDecimal.ZERO
            List<String> validationFailureMessages = tenderMovementService.preValidateBankTransferRequest(safeId, bankingDate, amount, tender)
            if (!validationFailureMessages.isEmpty() && validationFailureMessages.size() > 0) {
                def errorParams  = validationFailureMessages.join("<br>")
                redirect(action: "bankDeposit", params: [error: errorParams])
            } else {
                // If all validations pass, add tender movement entry
                Integer tenderMovementId = tenderMovementService.tenderMovementUpdate(safeId.intValue(), TenderMovementType.BANKING, tender, bankingDate, bank, bagReferenceNumber, comments, amount)

                //update safe session values
                //update safe session tender totals
                //add safe session audit
                tenderMovementService.updateSafeSessionBalanceTotals(SafeSessionAction.BANK_DEPOSIT, tender, amount.negate(), tenderMovementId, safeId)

                redirect(action: "bankDeposit", params: [success: "Successfully completed bank deposit"])
            }
        } catch (Exception ex) {
            log.error("Bank deposit saving error for safe id : ${safeId} tender type: ${tender} error: ${ex.getMessage()}", ex)
            String error =  "Bank deposit action failed. "
            redirect(action: "bankDeposit", params: [error: error])
        }
    }

    def processBankReceipt() {
        Integer safeId = null
        TenderType tender = null
        try {
            NumberFormat format = NumberFormat.getInstance(Locale.UK)
            safeId = params.safeId ? Integer.parseInt(params.safeId) : -1
            tender = TenderType.valueOf(params.tender)
            String bankingDate = params.bankingDate
            String bank = params.bank
            String bagReferenceNumber = params.bagReferenceNumber
            String comments = params.comments
            BigDecimal amount = params.amount ? new BigDecimal(format.parse(params.amount)?.toString()) : BigDecimal.ZERO
            List<String> validationFailureMessages = tenderMovementService.preValidateBankTransferRequest(safeId, bankingDate, amount, tender)
            if (!validationFailureMessages.isEmpty() && validationFailureMessages.size() > 0) {
                def errorParams  = validationFailureMessages.join("<br>")
                redirect(action: "bankReceipt", params: [error: errorParams])
            } else {
                // If all validations pass, add tender movement entry
                Integer tenderMovementId = tenderMovementService.tenderMovementUpdate(safeId.intValue(), TenderMovementType.BANKING, tender, bankingDate, bank, bagReferenceNumber, comments, amount)

                //update safe session values
                //update safe session tender totals
                //add safe session audit
                tenderMovementService.updateSafeSessionBalanceTotals(SafeSessionAction.BANK_RECEIPT, tender, amount, tenderMovementId, safeId)

                redirect(action: "bankReceipt", params: [success: "Successfully completed bank receipt"])
            }
        } catch (Exception ex) {
            log.error("Bank receipt saving error for safe id : ${safeId} tender type: ${tender} error: ${ex.getMessage()}", ex)
            String error =  "Bank receipt action failed. "
            redirect(action: "bankReceipt", params: [error: error])
        }
    }

}
