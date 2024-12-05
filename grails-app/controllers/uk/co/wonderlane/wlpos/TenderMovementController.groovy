package uk.co.wonderlane.wlpos

import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import uk.co.wonderlane.wlpos.enums.SafeSessionAction
import uk.co.wonderlane.wlpos.enums.ShiftAction
import uk.co.wonderlane.wlpos.enums.TenderMovementType
import uk.co.wonderlane.wlpos.enums.TenderType

import java.text.NumberFormat

class TenderMovementController {

    def tenderMovementService
    def safeService


    def index() {}

    def issueFloat(){}

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
        //todo - make sure to load tills having  open shift
        List<TillConfiguration> tills =  tenderMovementService.getAllActiveTills()
        List<TenderType> tenders = tenderMovementService.getEligibleTendersForTenderLift()
        [safeLocations: safeLocations, primarySafe: primarySafe, tills: tills, tenders:tenders, success: success, error: error]
    }

    def payIn(){}

    def payOut(){}

    def bankDeposit(){}

    def bankReceipt(){}

    //This is generic method of checking till balances
    def getTillAvailableBalance(){
        try {
            TenderType tender = TenderType.valueOf(params.tender)
            List<Integer> tillNos = [] //Declare tillNos as a List of Integers
            if (params.tillNos) { //Parse tillNos into list of till nos
                if (params.tillNos instanceof String) {
                    // Parse JSON string into a list of integers
                    tillNos = new JsonSlurper().parseText(params.tillNos).collect { it.toInteger() } as List<Integer>
                } else if (params.tillNos instanceof Collection) {
                    // Convert collection to a list of integers
                    tillNos = params.tillNos.collect { it.toInteger() } as List<Integer>
                } else {
                    // Handle single string value as integer list
                    tillNos = [params.tillNos.toInteger()] as List<Integer>
                }
            } else if (params.tillNo) {
                // Handle single tillNo as integer
                tillNos = [params.tillNo.toInteger()] as List<Integer>
            }
            BigDecimal enteredAmount = new BigDecimal(params.enteredAmount) //Get entered amount

            BigDecimal totalAvailableBalance = 0
            boolean isTillAmountLessThanEntered = false

            //create tender totals
            for (tillNo in tillNos) { //Loop over passed till nos to check available till balance is less than of entered amount
                BigDecimal availableBalance = tenderMovementService.getAvailableTillBalance(tillNo, tender)
                if (availableBalance.compareTo(enteredAmount) < 0) {
                    isTillAmountLessThanEntered = true
                    totalAvailableBalance = availableBalance
                    break  // This will break the loop
                }
            }

            // Convert response to JSON string
            String jsonResponse = JsonOutput.toJson([
                    success: true,
                    availableAmount: totalAvailableBalance,
                    isTillAmountLessThanEntered: isTillAmountLessThanEntered
            ])

            // Return as plain JSON string
            render(contentType: 'application/json', text: jsonResponse)
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

            //todo check safe is active if not show safe is not active

            //create tender totals
            int tenderMovementId = tenderMovementService.tenderMovementUpdate(tillId, safeId, TenderMovementType.CASH_LIFT, tender, amount)

            //update safe session values
            //update safe session tender totals
            //add safe session audit
            tenderMovementService.updateTenderLiftSafeSessionTotals(SafeSessionAction.CASH_LIFT, tender, amount, tenderMovementId, safeId)

            //update shift values
            //update shift cash in drawer
            //update shift tender totals
            //add shift audit
            tenderMovementService.updateTenderLiftShiftTotals(ShiftAction.CASH_LIFT, tender, amount, tenderMovementId, tillId)

            redirect(action: "tenderLift", params: [success: "Successfully process tender lift for till ${tillId}"])
        } catch (Exception ex) {
            log.error("Tender lift saving error for safe id : ${safeId} till id: ${tillId} tender type: ${tender} error: ${ex.getMessage()}", ex)
            String error =  "Tender lift action failed. "
            redirect(action: "tenderLift", params: [error: error])
        }
    }


}
