package uk.co.wonderlane.wlpos

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.joda.JodaModule
import grails.databinding.BindingFormat
import org.springframework.transaction.annotation.Transactional
import uk.co.wonderlane.wlpos.enums.LoyaltyOfferStatus
import uk.co.wonderlane.wlpos.reporting.SortParams
import groovy.json.JsonOutput

class LoyaltyController {

    def loyaltyService
    def promotionService
    def springSecurityService

    def index() {}

    def loyaltySegment() {}

    def loyaltyOffers() {}

    def ajaxSearchLoyaltySegment() {
        int defaultPagination = 20
        int defaultOffSet = 0
        try {
            def segment = loyaltyService.getSegment(params.searchTerm, params.searchBy, params.max ? Integer.parseInt(params.max) : defaultPagination,
                    params.offset ? Integer.parseInt(params.offset) : defaultOffSet, "id", "asc")

            render(template: "loyaltySegmentSearchResults", model: [segments              : segment?.segments,
                                                                    loyaltySegmentTerm    : params.loyaltySegmentTerm,
                                                                    loyaltySegmentSearchBy: params.loyaltySegmentSearchBy,
                                                                    max                   : params.max ?: defaultPagination,
                                                                    offset                : params.offset ?: defaultOffSet,
                                                                    totalCount            : segment?.totalCount
            ])
        } catch (Exception ex) {
            ex.printStackTrace()
            log.error("Error when loading loyalty segment search results, Search by " + params.searchBy + " search term " + params.searchTerm + " Exception " + ex)
            response.setStatus(500)
            render(view: "_loyaltyGenericError", contentType: "text/html", model: [
                    error_header: "Loyalty Segment Search Error",
                    error_body  : "Error when loading loyalty segment"
            ])
        }

    }


    def ajaxSearchLoyaltyOffers(SortParams sortParams) {
        int defaultPagination = 20
        int defaultOffSet = 0
        try {
            def offer = loyaltyService.getLoyaltyOffers(params.searchTerm, params.searchBy, params.max ? Integer.parseInt(params.max) : defaultPagination,
                    params.offset ? Integer.parseInt(params.offset) : defaultOffSet, sortParams.sortColumn, sortParams.sortOrder)

            render(template: "loyaltyOffersSearchResults", model: [offers               : offer?.offers,
                                                                   loyaltyOffersTerm    : params.loyaltyOffersTerm,
                                                                   loyaltyOffersSearchBy: params.loyaltyOffersSearchBy,
                                                                   max                  : params.max ?: defaultPagination,
                                                                   offset               : params.offset ?: defaultOffSet,
                                                                   totalCount           : offer?.totalCount,
                                                                   sortParams           : sortParams
            ])
        } catch (Exception ex) {
            ex.printStackTrace()
            log.error("Error when loading loyalty offers search results, Search by " + params.searchBy + " search term " + params.searchTerm + " Exception " + ex)
            response.setStatus(500)
            render(view: "_loyaltyGenericError", contentType: "text/html", model: [
                    error_header: "Loyalty Offers Search Error",
                    error_body  : "Error when loading loyalty offers"
            ])
        }
    }

    def showLoyaltyOffer(){
        try {
            LoyaltyOffer originalLoyaltyOffer = null
            List<Integer> selectedSegmentIds = new ArrayList<>();

            if (params.id && params.id.isNumber()) {
                int offerId = Integer.parseInt(params.id)

                originalLoyaltyOffer = loyaltyService.getLoyaltyOfferById(offerId)

                // Get selected segment IDs
                selectedSegmentIds = originalLoyaltyOffer?.loyaltyOfferSegments?.findAll{it.offerId = offerId }
                        ?.collect { it.segmentId }
            }

            //load all promotions for retailer
            List<Promotion> promotions = promotionService.getPromotionForRetailer(springSecurityService.principal.retailerId)

            //load all segments for retailer
            List<Segment> segments = loyaltyService.getLoyaltySegmentForRetailer(springSecurityService.principal.retailerId)

            // Serialize promotions list into JSON string
            ObjectMapper objectMapper = new ObjectMapper()
            objectMapper.registerModule(new JodaModule())
            String promotionsJson = objectMapper.writeValueAsString(promotions)
            String segmentsJson = objectMapper.writeValueAsString(segments)

            //Load eligible offer status
            List eligibleOfferStatus = loyaltyService.getEligibleOfferStatus()

            render(view: "/loyalty/addLoyaltyOffer", model: [
                    loyaltyOffer : originalLoyaltyOffer,
                    promotions: promotions,
                    segments  : segments,
                    promotionsJson: promotionsJson,
                    segmentsJson: segmentsJson,
                    selectedSegmentIds: selectedSegmentIds,
                    eligibleOfferStatus: eligibleOfferStatus
            ])
        }catch(Exception ex){
            ex.printStackTrace()
            log.error("Error loading loyalty offer view window, Exception " + ex)
        }
    }

    @Transactional
    def ajaxSaveLoyaltyOffers(LoyaltyOfferCommand loyaltyOfferCommand) {
        try {
            if (loyaltyOfferCommand != null){
                LoyaltyOffer originalLoyaltyOffer = null
                originalLoyaltyOffer = loyaltyService.getLoyaltyOfferById(loyaltyOfferCommand.getId()) //Load current loyalty offer value if exists
                if (originalLoyaltyOffer == null){
                    originalLoyaltyOffer = new LoyaltyOffer();
                }
                LoyaltyOffer updatedOffer = loyaltyService.populateUpdatedOffer(originalLoyaltyOffer, loyaltyOfferCommand) //Populate updated loyalty offer values
                List<LoyaltyOfferSegment> updatedLoyaltySegments = loyaltyService.updateLoyaltySegments(loyaltyOfferCommand, originalLoyaltyOffer) //Get updated loyalty segments
                loyaltyService.loyaltyOfferSave(originalLoyaltyOffer, updatedLoyaltySegments) //Save loyalty offers + loyalty offer segments
                loyaltyService.pushLoyaltyOfferIntoRabbitMQ() //Push loyalty offer details into rabbitmq
                flash.message = "Successfully Save Offer"
                redirect("controller": "loyalty", action:"loyaltyOffers")
            } else {
                log.error("Invalid request found for save loyalty offer")
                response.setStatus(500)
                render status: 500, contentType: 'application/json', text: JsonOutput.toJson([error: "Loyalty Invalid Request Found"])
            }
        }catch(Exception ex){
            ex.printStackTrace()
            log.error("Error when saving loyalty offers, Exception " + ex)
            response.setStatus(500)
            flash.error = "Loyalty Save Error"
            render status: 500, contentType: 'application/json', text: JsonOutput.toJson([error: "Loyalty Save Error"])
        }

    }

}

class LoyaltyOfferCommand {
    int id
    String offerDescription
    int retailerOfferId
    int retailerId
    LoyaltyOfferStatus status
    @BindingFormat('dd/MM/yyyy')
    Date startDate
    @BindingFormat('dd/MM/yyyy')
    Date endDate
    BigDecimal maxBudget
    int maxRedemptions
    Collection<LoyaltyOfferSegmentCommand> loyaltyOfferSegments = new ArrayList<>();

}

class LoyaltyOfferSegmentCommand {
    int id
    int offerId
    int segmentId
    int count = 0
}
