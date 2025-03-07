package uk.co.wonderlane.wlpos

import grails.converters.JSON
import org.joda.time.DateTimeZone
import org.springframework.security.access.annotation.Secured

class DeliveryController {

    static def timeZone = DateTimeZone.forID("Europe/London")

    def springSecurityService
    def branchOrderService
    List<BranchOrderResult> deliveries = []

    @Secured(['ROLE_ENGINEER', 'ROLE_HEAD_OFFICE'])
    def index() {
        deliveries = []
        redirect(action: "deliveries")
    }

    def deliveries() {
    }

    def ajaxCheckValidDeliveries(BranchOrderValidCommand command) {
        log.println(command)
        log.println(command.supplierReferences)
        def supplierReferences = JSON.parse(command.supplierReferences)
        for (String supplierReference : supplierReferences) {
            log.println(supplierReference)
            checkValidDelivery(supplierReference)

            break // TODO: Remove debug break
        }

        log.println(deliveries)
        for (def delivery : deliveries) {
            log.println(delivery.supplierReference)
        }
        render (template: "deliveryImportResults", model: [deliveries: deliveries])
//        response.setStatus(200)
//        render status: 200
    }

    def checkValidDelivery(String supplierReference) {
        ArrayList<BranchOrder> branchOrderList = branchOrderService.getBranchOrderBySupplierReference(supplierReference)
        log.println(branchOrderList)
        def result = BranchOrderResult.newInstance([
            supplierReference: supplierReference,
            valid: branchOrderList != null && !branchOrderList.isEmpty()
        ])
        deliveries.add(result)
    }
}

class BranchOrderValidCommand {
    String supplierReferences
}

class BranchOrderResult {
    String supplierReference
    boolean valid
}

/*Offer updatedLoyaltyOffer
List<String> errorList = new ArrayList<>()
try {
    if (command != null) {
        Offer originalLoyaltyOffer = null
        originalLoyaltyOffer = loyaltyService.getLoyaltyOfferById(command.getId())
        //Load current loyalty offer value if exists
        if (originalLoyaltyOffer == null) { //If no current loyalty exists create new one
            originalLoyaltyOffer = new Offer()
        }
        List<OfferSegment> originalLoyaltyOfferSegments = loyaltyService.getLoyaltyOfferSegmentsById(originalLoyaltyOffer.id)
        //Load current loyalty offer segments
        updatedLoyaltyOffer = loyaltyService.populateUpdatedOffer(originalLoyaltyOffer, command)
        //Populate updated loyalty offer values
        List<OfferSegment> updatedLoyaltySegments = loyaltyService.updateLoyaltySegments(originalLoyaltyOfferSegments,
                originalLoyaltyOffer.getLoyaltyOfferSegments()) //Get updated loyalty segments
        //Save loyalty offers + loyalty offer segments + push saved loyalty offer into rabbitMQ
        loyaltyService.loyaltyOfferSave(updatedLoyaltyOffer, updatedLoyaltySegments)
        flash.message = "Successfully Save Offer"
        response.setStatus(302)
        redirect("controller": "loyalty", action: "loyaltyOffers")
    } else {
        errorList.add("Loyalty Invalid Request Found")
        log.error("Invalid request found for save loyalty offer")
        response.setStatus(500)
        render status: 500, contentType: 'application/json', text: JsonOutput.toJson([error: errorList])
    }
} catch (Exception ex) {
    errorList.add("Loyalty Save Error")
    log.error("Error when saving loyalty offers, Exception " + ex)
    response.setStatus(500)
    if (updatedLoyaltyOffer != null && updatedLoyaltyOffer.errors != null && updatedLoyaltyOffer.errors.allErrors.size() > 0) {
        errorList.clear()
        errorList.addAll(loyaltyService.extractErrorMessages(updatedLoyaltyOffer.errors))
        log.error("Error when saving loyalty offers, Exception " + updatedLoyaltyOffer.errors)
    }
    render status: 500, contentType: 'application/json', text: JsonOutput.toJson([error: errorList])
}*/