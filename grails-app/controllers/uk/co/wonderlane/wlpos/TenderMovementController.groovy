package uk.co.wonderlane.wlpos

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
        BigDecimal amount = null
        try {
            NumberFormat format = NumberFormat.getInstance(Locale.UK)
            safeId = params.safeId ? Integer.parseInt(params.safeId) : -1
            tillId = Integer.parseInt(params.tillNo)
            tender = TenderType.valueOf(params.tender)
            amount = params.amount ? new BigDecimal(format.parse(params.amount)?.toString()) : BigDecimal.ZERO

            //update shift values

            //update safe session value

            //create tender totals

            //create audit

            redirect(action: "tenderLift", params: [success: "Successfully process tender lift"])
        } catch (Exception ex) {
            log.error("Tender lift saving error for safe id : ${safeId} till id: ${tillId} error: ${ex.getMessage()}", ex)
            String error =  "Tender lift action failed. "
            redirect(action: "tenderLift", params: [error: error])
        }
    }


}
