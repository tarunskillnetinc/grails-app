package uk.co.wonderlane.wlpos

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.joda.JodaModule
import grails.databinding.BindingFormat
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import org.joda.time.format.DateTimeFormat
import org.joda.time.format.DateTimeFormatter
import org.springframework.context.MessageSource
import org.springframework.transaction.annotation.Transactional
import org.springframework.validation.Errors
import uk.co.wonderlane.wlpos.enums.LoyaltyOfferStatus
import uk.co.wonderlane.wlpos.reporting.SortParams
import groovy.json.JsonOutput

class LoyaltyController {

    def loyaltyService
    def promotionService
    def springSecurityService
    def loyaltyMemberService

    def index() {}
    def loyaltyMembers() {}
    def loyaltySegment(){}   

    /* Called from the membership management page when searching for loyalty members */
    def ajaxSearchMembers() {
        String searchBy
        String searchTerm
        Integer max
        Integer offset
        String sortColumn
        String sortOrder
        def results

        try {
            searchBy = params.searchBy
            searchTerm = params.searchTerm
            max = params.max ? Integer.parseInt(params.max) : null
            offset = params.offset ? Integer.parseInt(params.offset) : null
            sortColumn = validateSortColumn(params.sortColumn)
            sortOrder = validateSortOrder(params.sortOrder)
        } catch (Exception e) {
            e.printStackTrace()
            response.status = 400
            return
        }

        if (searchBy == "email") {
            results = loyaltyMemberService.findByEmail(searchTerm)
        } else {
            results = loyaltyMemberService.findByCardNumber(searchTerm, max, offset, sortColumn, sortOrder)
        }

        render(template: "loyaltySearchResults", model: [members: results["members"],
                                                         searchTerm: params.searchTerm,
                                                         offset: params.offset,
                                                         max: params.max,
                                                         sortColumn: params.sortColumn,
                                                         sortOrder: params.sortOrder,
                                                         totalResults: results["totalResults"]])
    }

    private String validateSortColumn(String sortColumn) {
        def availableColumns = [ "cardNumber", "email", "firstName", "lastName" ]

        if (!sortColumn) {
            return null
        } else if (availableColumns.contains(sortColumn)) {
            return sortColumn
        } else {
            throw new RuntimeException("Bad request")
        }
    }

    private String validateSortOrder(String sortOrder) {
        def availableOrders = [ "asc", "desc" ]

        if (!sortOrder) {
            return null
        } else if (availableOrders.contains(sortOrder.toLowerCase())) {
            return sortOrder
        } else {
            throw new RuntimeException("Bad request")
        }
    }

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
            boolean isUpdate = false
            LoyaltyOffer originalLoyaltyOffer = null
            List<Integer> selectedSegmentIds = new ArrayList<>()
            DateTimeFormatter dateFormatter = DateTimeFormat.forPattern("dd/MM/yyyy").withZoneUTC()

            if (params.id && params.id.isNumber()) {
                isUpdate = true
                int offerId = Integer.parseInt(params.id)

                originalLoyaltyOffer = loyaltyService.getLoyaltyOfferById(offerId)

                // Get selected segment IDs
                selectedSegmentIds = originalLoyaltyOffer?.loyaltyOfferSegments?.findAll{it.offerId = offerId }
                        ?.collect { it.segmentId }
            }

            DateTime startDate = originalLoyaltyOffer?.startDate ? dateFormatter.parseDateTime(dateFormatter.print(new DateTime(originalLoyaltyOffer?.startDate.getTime()))) : DateTime.now(DateTimeZone.UTC)
            DateTime endDate = originalLoyaltyOffer?.endDate ? dateFormatter.parseDateTime(dateFormatter.print(new DateTime(originalLoyaltyOffer?.endDate.getTime()))) : DateTime.now(DateTimeZone.UTC).plusDays(7)

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
                    eligibleOfferStatus: eligibleOfferStatus,
                    isUpdate: isUpdate,
                    defaultStatus : LoyaltyOfferStatus.PENDING,
                    startDate : startDate,
                    endDate : endDate
            ])
        }catch(Exception ex){
            ex.printStackTrace()
            log.error("Error loading loyalty offer view window, Exception " + ex)
            flash.error = "Failed to load loyalty offer view"
            redirect(action: "loyaltyOffers")
        }
    }

    def ajaxSaveLoyaltyOffers(LoyaltyOfferCommand loyaltyOfferCommand) {
        LoyaltyOffer updatedLoyaltyOffer
        List<String> errorList = new ArrayList<>()
        try {
            if (loyaltyOfferCommand != null){
                LoyaltyOffer originalLoyaltyOffer = null
                originalLoyaltyOffer = loyaltyService.getLoyaltyOfferById(loyaltyOfferCommand.getId()) //Load current loyalty offer value if exists
                if (originalLoyaltyOffer == null){ //If no current loyalty exists create new one
                    originalLoyaltyOffer = new LoyaltyOffer()
                }
                List<LoyaltyOfferSegment> originalLoyaltyOfferSegments = loyaltyService.getLoyaltyOfferSegmentsById(originalLoyaltyOffer.id) //Load current loyalty offer segments
                updatedLoyaltyOffer = loyaltyService.populateUpdatedOffer(originalLoyaltyOffer, loyaltyOfferCommand) //Populate updated loyalty offer values
                List<LoyaltyOfferSegment> updatedLoyaltySegments = loyaltyService.updateLoyaltySegments(originalLoyaltyOfferSegments,
                        originalLoyaltyOffer.getLoyaltyOfferSegments()) //Get updated loyalty segments
                //Save loyalty offers + loyalty offer segments + push saved loyalty offer into rabbitMQ
                loyaltyService.loyaltyOfferSave(updatedLoyaltyOffer, updatedLoyaltySegments)
                flash.message = "Successfully Save Offer"
                redirect("controller": "loyalty", action:"loyaltyOffers")
            } else {
                errorList.add("Loyalty Invalid Request Found")
                log.error("Invalid request found for save loyalty offer")
                response.setStatus(500)
                render status: 500, contentType: 'application/json', text: JsonOutput.toJson([error: errorList])
            }
        }catch(Exception ex){
            errorList.add("Loyalty Save Error")
            log.error("Error when saving loyalty offers, Exception " + ex)
            response.setStatus(500)
            if (updatedLoyaltyOffer != null && updatedLoyaltyOffer.errors != null && updatedLoyaltyOffer.errors.allErrors.size() > 0){
                errorList.clear()
                errorList.addAll(loyaltyService.extractErrorMessages(updatedLoyaltyOffer.errors))
                log.error("Error when saving loyalty offers, Exception " + updatedLoyaltyOffer.errors)
            }
            render status: 500, contentType: 'application/json', text: JsonOutput.toJson([error: errorList])
        }
    }


    def ajaxShowOfferCancelWindow(){
        render(view: "_loyaltyGenericError", contentType: "text/html", model: [
                                                error_header: "Cancel Loyalty Offer",
                                                error_body  : "Are you sure you want to cancel? All unsaved changes will be lost"
        ])
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
