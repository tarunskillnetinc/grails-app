package uk.co.wonderlane.wlpos

import grails.plugin.springsecurity.annotation.Secured
import groovy.json.JsonOutput
import uk.co.wonderlane.wlpos.entities.cash.SafeSession
import uk.co.wonderlane.wlpos.entities.cash.Shift
import uk.co.wonderlane.wlpos.entities.cash.TenderTotal
import uk.co.wonderlane.wlpos.enums.ReasonCodeType
import uk.co.wonderlane.wlpos.enums.SafeSessionAction
import uk.co.wonderlane.wlpos.enums.ShiftAction

import java.text.NumberFormat

@Secured(['ROLE_ENGINEER', 'ROLE_HEAD_OFFICE', 'ROLE_STORE_MANAGER', 'ROLE_SUPERVISOR'])
class TenderMovementController {

    def tenderMovementService
    def shiftService
    def reasonCodeService
    def springSecurityService
    def safeManagementService
    def tenderTypeService

    private static final MIN_AMOUNT_PAYOUT = new BigDecimal("0.01")
    private static final MAX_AMOUNT_PAYOUT = new BigDecimal("9999.99")

    public static final MIN_AMOUNT_PAYIN = new BigDecimal("0.01")
    public static final MAX_AMOUNT_PAYIN = new BigDecimal("9999.99")

    def index() { }

    def addFloat() {
        String success = params.success
        String error = params.error

        def (List<Safe> safeLocations, Safe primarySafe) = tenderMovementService.fetchSafeLocations()

        // Load and return tills having  open shift + Cash management enable + Serial number available
        List<TillConfiguration> tills =  tenderMovementService.returnAllOpenTillsSupportingCashManagement()
        List<TenderType> tenders = tenderTypeService.getApplicableTenderTypes()
        tenders.removeAll { !it.eligibleForFloat }

        [safeLocations: safeLocations, primarySafe: primarySafe, tills: tills, tenders: tenders, success: success, error: error]
    }

    def tenderLift() {
        String success = params.success
        String error = params.error

        def (List<Safe> safeLocations, Safe primarySafe) = tenderMovementService.fetchSafeLocations()

        //Load and return tills having  open shift + Cash management enable + Serial number available
        List<TillConfiguration> tills =  tenderMovementService.returnAllOpenTillsSupportingCashManagement()
        List<TenderType> tenders = tenderTypeService.getApplicableTenderTypes()
        tenders.removeAll { !it.eligibleForCashLift }

        [safeLocations: safeLocations, primarySafe: primarySafe, tills: tills, tenders: tenders, success: success, error: error]
    }

    def payIn() {
        String success = params.success
        String error = params.error

        def (List<Safe> safeLocations, Safe primarySafe) = tenderMovementService.fetchSafeLocations()

        //Load and return tills having  open shift + Cash management enable + Serial number available
        List<ReasonCode> reasonCodes = reasonCodeService.getReasonCodesByType(springSecurityService.principal.retailerId, ReasonCodeType.PAID_IN)
        List<TenderType> tenders = tenderTypeService.getApplicableTenderTypes()
        tenders.removeAll { !it.cashTender }

        [safeLocations: safeLocations, primarySafe: primarySafe, tenders: tenders, reasonCodes: reasonCodes, success: success, error: error]
    }

    def payOut() {
        String success = params.success
        String error = params.error

        def (List<Safe> safeLocations, Safe primarySafe) = tenderMovementService.fetchSafeLocations()

        def reasonCodes = reasonCodeService.getReasonCodesByType(springSecurityService.principal.retailerId, ReasonCodeType.PAID_OUT)
        List<TenderType> tenders = tenderTypeService.getApplicableTenderTypes()
        tenders.removeAll { !it.cashTender }

        [safes: safeLocations, primarySafe: primarySafe, tenders: tenders, reasonCodes: reasonCodes, success: success, error: error]
    }

    def bankDeposit() {
        String success = params.success
        String error = params.error

        def (List<Safe> safeLocations, Safe primarySafe) = tenderMovementService.fetchActiveSafeLocationsAndInactiveSafesWithTenderValues()

        List<TenderType> tenders = tenderTypeService.getApplicableTenderTypes()
        tenders.removeAll { !it.eligibleForBanking }

        [safes: safeLocations, primarySafe: primarySafe, tenders: tenders, success: success, error: error]
    }

    def bankReceipt() {
        String success = params.success
        String error = params.error

        def (List<Safe> safeLocations, Safe primarySafe) = tenderMovementService.fetchSafeLocations()

        List<TenderType> tenders = tenderTypeService.getApplicableTenderTypes()
        tenders.removeAll { !it.eligibleForBanking }

        [safes: safeLocations, primarySafe: primarySafe, tenders: tenders, success: success, error: error]
    }

    def getTillAvailableBalance() {
        try {
            Integer tenderTypeId = params.tenderTypeId ? Integer.parseInt(params.tenderTypeId) : null
            List<Integer> tillNos = tenderMovementService.returnRequestedTillIds(params)
            BigDecimal enteredAmount = new BigDecimal(params.enteredAmount) //Get entered amount

            BigDecimal totalAvailableBalance = BigDecimal.ZERO
            boolean isTillAmountLessThanEntered = false
            List<String> errorMessages = []

            TenderType tenderType = tenderTypeService.getTenderType(tenderTypeId)

            if (springSecurityService.principal.storeId == null) {
                errorMessages.add("You must be logged in at a store level.")
            }

            if (errorMessages.empty) {
                //Check balances and till exists
                for (tillNo in tillNos) {
                    // Ensure the till actually support cash management - someone may have logged in and disabled it.
                    if (!shiftService.isCashManagementEnable(tillNo)) {
                        errorMessages.add("Cash Management is not enabled for till ${tillNo}")
                        continue
                    }

                    Shift shift = shiftService.getOpenShift(springSecurityService.principal.retailerId, springSecurityService.principal.storeId, tillNo)

                    if (shift != null) {
                        BigDecimal tenderValue = shift.getTenderTotals().stream().filter(tt -> tt.getTenderTypeId() == tenderTypeId).findFirst().map(TenderTotal::getValue).orElse(BigDecimal.ZERO)

                        if (tenderValue.compareTo(enteredAmount) < 0) {
                            isTillAmountLessThanEntered = true
                            totalAvailableBalance = tenderValue
                        }
                    } else {
                        errorMessages.add("Tills shift for till no ${tillNo} not in progress status to perform tender lift")
                    }
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
            List<String> errorMessages = []
            errorMessages.add("Cannot fetch the till balance.")
            def response = [
                    success: false,
                    errorMessages: errorMessages
            ]
            log.error("getTillAvailableBalance error: ${ex.getMessage()}", ex)

            render(status: 500, contentType: 'application/json', text: JsonOutput.toJson(response))
        }
    }

    def processAddFloat() {
        Integer safeId = null
        Integer tenderTypeId = null

        try {
            NumberFormat format = NumberFormat.getInstance(Locale.UK)
            safeId = params.safeId ? Integer.parseInt(params.safeId) : -1
            tenderTypeId = params.tenderTypeId ? Integer.parseInt(params.tenderTypeId) : -1

            BigDecimal amount = params.amount ? new BigDecimal(format.parse(params.amount)?.toString()) : BigDecimal.ZERO
            List<Integer> tillNos = tenderMovementService.returnRequestedTillIds(params)

            TenderType tenderType = tenderTypeService.getTenderType(tenderTypeId)

            //This will done all validations
            //1. Validate safe is selected
            //2. Validate enter amount is correct
            //3. Validate any selected tills
            //4. Validate tender is selected
            //5. Validate selected safe is active
            List<String> validationFailureMessages = tenderMovementService.preValidateAddFloatRequest(safeId, tillNos, amount, tenderType)

            if (!validationFailureMessages.isEmpty() && validationFailureMessages.size() > 0) { //If safe trying to distribute money is inactive then throw error
                def errorParams  = validationFailureMessages.join("<br>")

                redirect(action: "addFloat", params: [error: errorParams])
            } else {
                List<String> tillSuccessMessages = []
                List<String> tillFailureMessages = []

                for (Integer tillId : tillNos) {
                    try {
                        if (!tenderMovementService.isOpenShiftAvailable(tillId)) {
                            tillFailureMessages.add("No open shift available for till ${tillId}.")
                        } else {
                            // Create tender totals
                            Integer tenderMovementId = tenderMovementService.recordTenderTransfer(tillId, safeId, tenderType.id, tenderType.name, amount, false)

                            //update safe session values
                            //update safe session tender totals
                            //add safe session audit
                            tenderMovementService.updateSafeSessionBalanceTotals(SafeSessionAction.ADD_FLOAT, tenderType.id, tenderType.name, tenderType.cashTender, amount.negate(), tenderMovementId, safeId)

                            //update shift values
                            //update shift cash in drawer
                            //update shift tender totals
                            //add shift audit
                            tenderMovementService.updateShiftBalanceTotals(ShiftAction.ADD_FLOAT, tenderType.id, tenderType.name, tenderType.cashTender, amount, tenderMovementId, tillId, safeId)

                            tillSuccessMessages.add("Successfully processed add float for Till ${tillId}")
                        }
                    } catch (Exception ex) {
                        log.error("Add float item saving error for safe id : ${safeId} till id: ${tillId} tender type: ${tenderTypeId} error: ${ex.getMessage()}", ex)
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

                redirect(action: "addFloat", params: [success: resultParams.success, error: resultParams.error])
            }
        } catch (Exception ex) {
            log.error("Add float action failed error for safe id : ${safeId}  tender type: ${tenderTypeId} error: ${ex.getMessage()}", ex)
            String error =  "Add float action failed. "
            redirect(action: "addFloat", params: [error: error])
        }
    }

    def processTenderLift() {
        Integer safeId = null
        Integer tillId = null
        Integer tenderTypeId = null

        try {
            NumberFormat format = NumberFormat.getInstance(Locale.UK)
            safeId = params.safeId ? Integer.parseInt(params.safeId) : null
            tillId = params.tillNo ? Integer.parseInt(params.tillNo) : null
            tenderTypeId = params.tenderTypeId ? Integer.parseInt(params.tenderTypeId) : null

            BigDecimal amount = params.amount ? new BigDecimal(format.parse(params.amount)?.toString()) : BigDecimal.ZERO

            TenderType tenderType = tenderTypeService.getTenderType(tenderTypeId)

            if (!tenderMovementService.isOpenShiftAvailable(tillId)){
                redirect(action: "tenderLift", params: [error: "No open shift available for till ${tillId}"])
            } else if (!tenderMovementService.isSafeActive(safeId)){
                redirect(action: "tenderLift", params: [error: "Selected safe is not active please try with another"])
            } else {
                //create tender totals
                Integer tenderMovementId = tenderMovementService.recordTenderTransfer(tillId, safeId, tenderType?.id, tenderType?.name, amount, true)

                //update safe session values
                //update safe session tender totals
                //add safe session audit
                tenderMovementService.updateSafeSessionBalanceTotals(SafeSessionAction.CASH_LIFT, tenderType?.id, tenderType?.name, tenderType?.cashTender, amount, tenderMovementId, safeId)

                //update shift values
                //update shift cash in drawer
                //update shift tender totals
                //add shift audit
                tenderMovementService.updateShiftBalanceTotals(ShiftAction.CASH_LIFT, tenderType?.id, tenderType?.name, tenderType?.cashTender, amount.negate(), tenderMovementId, tillId, safeId)

                redirect(action: "tenderLift", params: [success: "Successfully processed tender lift for till ${tillId}"])
            }
        } catch (Exception ex) {
            log.error("Tender lift saving error for safe id : ${safeId} till id: ${tillId} tender type: ${tenderTypeId} error: ${ex.getMessage()}", ex)
            String error =  "Tender lift action failed. "
            redirect(action: "tenderLift", params: [error: error])
        }
    }

    def processPayIn() {
        Integer safeId = null
        Integer tillId = null
        Integer tenderTypeId = null
        ReasonCode reasonCode = null
        Safe safe = null

        try {
            NumberFormat format = NumberFormat.getInstance(Locale.UK)
            safeId = params.safeId ? Integer.parseInt(params.safeId) : null
            tenderTypeId = params.tenderTypeId ? Integer.parseInt(params.tenderTypeId) : null

            TenderType tenderType = tenderTypeService.getTenderType(tenderTypeId)
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

            if (reasonCode == null) {
                redirect(action: "payIn", params: [error: "Selected reason code doesnt exist."])
            } else if (safe == null) {
                redirect(action: "payIn", params: [error: "Selected safe doesnt exist."])
            } else if (!tenderMovementService.isSafeActive(safeId)) {
                redirect(action: "payIn", params: [error: "Selected safe not active please try with another."])
            } else {
                //create tender totals
                Integer tenderMovementId = tenderMovementService.recordSafeTenderMovement(safeId, tenderType?.id, tenderType?.name, amount, reasonCode.description, true)

                //update safe session values
                //update safe session tender totals
                //add safe session audit
                tenderMovementService.updateSafeSessionBalanceTotals(SafeSessionAction.PAID_IN, tenderType?.id, tenderType?.name, tenderType?.cashTender, amount, tenderMovementId, safeId)

                redirect(action: "payIn", params: [success: "Pay In successfully processed. Funds added to safe '${safe?.description}'"])
            }
        } catch (Exception ex) {
            log.error("Pay In saving error for safe id : ${safeId} reason code: ${reasonCode} tender type: ${tenderTypeId} error: ${ex.getMessage()}", ex)
            String error =  "Pay In action failed. "

            redirect(action: "payIn", params: [error: error])
        }
    }

    def processPayOut() {
        Integer safeId = null
        Integer tenderTypeId = null
        String reasonCode = null

        try {
            NumberFormat format = NumberFormat.getInstance(Locale.UK)
            safeId = params.safeId ? Integer.parseInt(params.safeId) : -1
            tenderTypeId = params.tenderTypeId ? Integer.parseInt(params.tenderTypeId) : null
            reasonCode = params.reasonCode

            TenderType tenderType = tenderTypeService.getTenderType(tenderTypeId)
            BigDecimal amount = params.amount ? new BigDecimal(format.parse(params.amount)?.toString()) : BigDecimal.ZERO

            if (!isAPayOutValidAmount(amount)) {
                def errorMessage = "Payout amount must be between ${MIN_AMOUNT_PAYOUT} and ${MAX_AMOUNT_PAYOUT}."
                log.error(errorMessage)

                redirect(action: "payOut", params: [error: errorMessage])
            } else if (!tenderMovementService.isSafeActive(safeId)){
                redirect(action: "payOut", params: [error: "Selected safe not active please try with another"])
            } else if (reasonCode == null) {
                redirect(action: "payOut", params: [error: "Invalid reason code."])
            } else if (!tenderType?.cashTender) {
                redirect(action: "payOut", params: [error: "Invalid tender type."])
            } else {
                Integer tenderMovementId = tenderMovementService.recordSafeTenderMovement(safeId.intValue(), tenderType?.id, tenderType?.name, amount, reasonCode, false)

                //update safe session values
                //update safe session tender totals
                //add safe session audit
                tenderMovementService.updateSafeSessionBalanceTotals(SafeSessionAction.PAID_OUT, tenderType?.id, tenderType?.name, tenderType?.cashTender, amount.negate(), tenderMovementId, safeId)

                redirect(action: "payOut", params: [success: "Pay Out successfully processed. Funds deducted from safe."])
            }
        } catch (Exception ex) {
            log.error("Pay Out saving error for safe id : ${safeId} tender type: ${tenderTypeId} reason code: ${reasonCode}  error: ${ex.getMessage()}", ex)
            String error =  "Pay Out action failed. "

            redirect(action: "payOut", params: [error: error])
        }
    }

    def processBankDeposit() {
        Integer safeId = null
        Integer tenderTypeId = null

        try {
            NumberFormat format = NumberFormat.getInstance(Locale.UK)
            safeId = params.safeId ? Integer.parseInt(params.safeId) : -1
            tenderTypeId = params.tenderTypeId ? Integer.parseInt(params.tenderTypeId) : null

            String bankingDate = params.bankingDate
            String bank = params.bank
            String bagReferenceNumber = params.bagReferenceNumber
            String comments = params.comments

            BigDecimal amount = params.amount ? new BigDecimal(format.parse(params.amount)?.toString()) : BigDecimal.ZERO
            TenderType tenderType = tenderTypeService.getTenderType(tenderTypeId)

            List<String> validationFailureMessages = tenderMovementService.preValidateBankTransferRequest(safeId, bankingDate, amount, tenderType)

            if (!validationFailureMessages.isEmpty() && validationFailureMessages.size() > 0) {
                def errorParams  = validationFailureMessages.join("<br>")
                redirect(action: "bankDeposit", params: [error: errorParams])
            } else {
                Safe safe = Safe.findByIdAndRetailerId(safeId, springSecurityService.principal.retailerId)

                // If all validations pass, add tender movement entry
                Integer tenderMovementId = tenderMovementService.recordBankingMovement(safeId.intValue(), tenderType?.id, tenderType?.name, amount, false, bankingDate, bank, bagReferenceNumber, comments)

                //update safe session values
                //update safe session tender totals
                //add safe session audit
                tenderMovementService.updateSafeSessionBalanceTotals(SafeSessionAction.BANK_DEPOSIT, tenderType?.id, tenderType?.name, tenderType?.cashTender, amount.negate(), tenderMovementId, safeId)

                redirect(action: "bankDeposit", params: [success: "Successfully completed bank deposit. Funds moved from ${safe.description} to bank"])
            }
        } catch (Exception ex) {
            log.error("Bank deposit saving error for safe id : ${safeId} tender type: ${tenderTypeId} error: ${ex.getMessage()}", ex)
            String error =  "Bank deposit action failed. "

            redirect(action: "bankDeposit", params: [error: error])
        }
    }

    def processBankReceipt() {
        Integer safeId = null
        Integer tenderTypeId = null

        try {
            NumberFormat format = NumberFormat.getInstance(Locale.UK)
            safeId = params.safeId ? Integer.parseInt(params.safeId) : -1
            tenderTypeId = params.tenderTypeId ? Integer.parseInt(params.tenderTypeId) : null

            String bankingDate = params.bankingDate
            String bank = params.bank
            String bagReferenceNumber = params.bagReferenceNumber
            String comments = params.comments

            BigDecimal amount = params.amount ? new BigDecimal(format.parse(params.amount)?.toString()) : BigDecimal.ZERO
            TenderType tenderType = tenderTypeService.getTenderType(tenderTypeId)

            List<String> validationFailureMessages = tenderMovementService.preValidateBankTransferRequest(safeId, bankingDate, amount, tenderType)

            if (!validationFailureMessages.isEmpty() && validationFailureMessages.size() > 0) {
                def errorParams  = validationFailureMessages.join("<br>")
                redirect(action: "bankReceipt", params: [error: errorParams])
            } else {
                Safe safe = Safe.findByIdAndRetailerId(safeId, springSecurityService.principal.retailerId)

                // If all validations pass, add tender movement entry
                Integer tenderMovementId = tenderMovementService.recordBankingMovement(safeId.intValue(), tenderType?.id, tenderType?.name, amount, true, bankingDate, bank, bagReferenceNumber, comments)

                //update safe session values
                //update safe session tender totals
                //add safe session audit
                tenderMovementService.updateSafeSessionBalanceTotals(SafeSessionAction.BANK_RECEIPT, tenderType?.id, tenderType?.name, tenderType?.cashTender, amount, tenderMovementId, safeId)

                redirect(action: "bankReceipt", params: [success: "Successfully completed bank receipt. Funds added to ${safe.description} from bank"])
            }
        } catch (Exception ex) {
            log.error("Bank receipt saving error for safe id : ${safeId} tender type: ${tenderTypeId} error: ${ex.getMessage()}", ex)
            String error =  "Bank receipt action failed. "

            redirect(action: "bankReceipt", params: [error: error])
        }
    }

    def getSafeAvailableBalance() {
        try {
            Integer tenderTypeId = params.tenderTypeId ? Integer.parseInt(params.tenderTypeId) : -1
            BigDecimal totalAmountToBeDistributed = new BigDecimal(params.totalAmountToBeDistributed) //Get entered amount
            Integer safeId = params.safeId ? Integer.parseInt(params.safeId) : -1

            TenderType tenderType = tenderTypeService.getTenderType(tenderTypeId)
            BigDecimal totalAvailableBalance = BigDecimal.ZERO
            boolean isSafeAmountLessThanEntered = false
            List<String> errorMessages = []

            SafeSession safeSession = safeManagementService.getActiveSession(safeId)

            if (safeSession != null) {
                BigDecimal amountLeftInSafeSession = safeSession.getTenderTotals().stream().filter(tt -> tt.getTenderTypeId() == tenderType?.id).findFirst().map(TenderTotal::getValue).orElse(BigDecimal.ZERO)
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

    private boolean isAPayOutValidAmount(BigDecimal amount) {
        amount >= MIN_AMOUNT_PAYOUT && amount <= MAX_AMOUNT_PAYOUT
    }
}
