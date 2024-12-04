package uk.co.wonderlane.wlpos

import uk.co.wonderlane.wlpos.entities.cash.SafeSession
import uk.co.wonderlane.wlpos.entities.cash.Shift
import uk.co.wonderlane.wlpos.enums.SafeSessionAction
import uk.co.wonderlane.wlpos.enums.ShiftAction
import uk.co.wonderlane.wlpos.enums.TenderMovementType
import uk.co.wonderlane.wlpos.enums.TenderType

import java.text.NumberFormat

class TenderMovementController {

    def tenderMovementService
    def safeService
    def springSecurityService


    def index() {}

    def issueFloat(){}

    def tenderLift(){
        String success = params.success
        String error = params.error
        List<Safe> safeLocations = safeService.getStoreSafes() ?.findAll { it.active }
        Safe primarySafe = safeLocations.find { it.primary }

        List<TillConfiguration> tills =  tenderMovementService.getAllActiveTills()
        List<TenderType> tenders = tenderMovementService.getEligibleTendersForTenderLift()

        [safeLocations: safeLocations, primarySafe: primarySafe, tills: tills, tenders:tenders, success: success, error: error]
    }

    def payIn(){}

    def payOut(){}

    def bankDeposit(){}

    def bankReceipt(){}

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

            //create tender totals
            int tenderMovementId = tenderMovementService.tenderMovementUpdate(tillId, safeId, TenderMovementType.CASH_LIFT, tender, amount)

            //update safe session values
            //update safe session tender totals
            //add safe session audit
            tenderMovementService.updateTenderLiftSafeSessionTotals(SafeSessionAction.OPEN, tender, amount, tenderMovementId)

            //update shift values
            //update shift cash in drawer
            //update shift tender totals
            //add shift audit
            tenderMovementService.updateTenderLiftShiftTotals(ShiftAction.CASH_LIFT, tender, amount, tenderMovementId, tillId)

            redirect(action: "tenderLift", params: [success: "Successfully process tender lift"])
        } catch (Exception ex) {
            log.error("Tender lift saving error for safe id : ${safeId} till id: ${tillId} tender type: ${tender} error: ${ex.getMessage()}", ex)
            String error =  "Tender lift action failed. "
            redirect(action: "tenderLift", params: [error: error])
        }
    }


}
