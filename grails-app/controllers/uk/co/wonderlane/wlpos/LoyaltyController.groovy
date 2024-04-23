package uk.co.wonderlane.wlpos

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.joda.JodaModule
import grails.databinding.BindingFormat
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import org.joda.time.format.DateTimeFormat
import org.joda.time.format.DateTimeFormatter
import uk.co.wonderlane.wlpos.enums.LoyaltyOfferStatus
import uk.co.wonderlane.wlpos.enums.MemberOfferStatus
import uk.co.wonderlane.wlpos.loyalty.MemberOffer
import uk.co.wonderlane.wlpos.reporting.SortParams
import groovy.json.JsonOutput

class LoyaltyController {

    def loyaltyService
    def promotionService
    def springSecurityService
    def loyaltyMemberService
    def memberTransactionService

    def index() {}
    def loyaltyMembers() {}
    def loyaltySegment() {}

    def transactions(String cardNumber) {
        render(view: "transactions", model: [cardNumber: cardNumber])
    }

    def offers(String cardNumber) {
        render(view: "memberOffers", model: [cardNumber: cardNumber])
    }

    def addMemberOffer(String cardNumber) {
        def offers = loyaltyMemberService.searchForAvailableOffersForMember(cardNumber)

        // Create a new list including only id and offer_description
        def offersList = offers.collect { offer ->
            [id: offer.id, offerDescription: offer.offerDescription]
        }

        render(view: "addMemberOffer", model: [cardNumber: cardNumber, offers: offersList])
    }

    def showMemberDetails(String cardNumber) {
        def member = loyaltyMemberService.findByCardNumber(cardNumber)
        render (view: "loyaltyMemberDetails", model: [member: member])
    }

    def offerDetails(String cardNumber, Integer id) {
        def offer = loyaltyMemberService.getMemberOffer(id)
        render (view: "memberOfferDetails", model: [cardNumber: cardNumber, offer: offer])
    }

    def transactionDetails(String cardNumber, String memberId, String transactionId) {
        def transaction = memberTransactionService.findTransactionByMemberIdAndTransactionId(Integer.parseInt(memberId), Integer.parseInt(transactionId))
        render (view: "transactionDetails", model: [cardNumber: cardNumber, transaction: transaction])
    }

    def ajaxSelectedOffer(String id, String cardNumber) {
        def member = loyaltyMemberService.findByCardNumber(cardNumber)
        def offer = loyaltyService.getLoyaltyOfferById(Integer.parseInt(id))

        render(template: "addMemberOfferSelect", model: [offer: offer, member: member])
    }

    def ajaxSaveMemberOffer() {
        Integer memberId
        Integer offerId
        String cardNumber
        String description
        Integer remainingRedemptions
        Boolean status
        DateTime startDate, endDate
        DateTimeFormatter formatter = DateTimeFormat.forPattern("dd/MM/yyyy")

        try {
            memberId = params.memberId ? Integer.parseInt(params.memberId) : null
            offerId = params.offerId ? Integer.parseInt(params.offerId) : null
            cardNumber = params.cardNumber ? params.cardNumber : null
            description = params.description ? params.description : ""
            remainingRedemptions = params.remainingRedemptions ? Integer.parseInt(params.remainingRedemptions) : 0
            status = params.status != null ? params.status == "on" : false
            startDate = params.startDate ? formatter.parseDateTime(params.startDate) : null
            endDate = params.endDate ? formatter.parseDateTime(params.endDate) : null
        } catch (Exception e) {
            log.error("Error when attempting to save a new member offer, Exception " + e)
            response.status = 400
            return
        }

        def memberOffer = new MemberOffer(
            offerDescription: description,
            currentSavings: 0,
            visibleFromDate: startDate,
            startDate: startDate,
            endDate: endDate.plusDays(1).withTimeAtStartOfDay(),
            maxRedemptions: remainingRedemptions,
            currentRedemptions: 0,
            status: status ? MemberOfferStatus.ACTIVE : MemberOfferStatus.CLOSED,
        )

        def updated = loyaltyMemberService.saveMemberOffer(memberOffer, memberId, offerId)

        if (updated) {
            flash.message = "Member Offer created successfully"
        }

        offers(cardNumber)
    }

    def memberOfferUpdate() {
        Integer id
        String remainingRedemptions
        String status
        Boolean updated = false

        try {
            id = params.offerId ? Integer.parseInt(params.offerId) : null
            remainingRedemptions = params.remainingRedemptions ? params.remainingRedemptions : null
            status = params.status ? params.status : null
        } catch (Exception e) {
            log.error("Error when attempting to update loyalty member offer, Exception " + e)
            response.status = 400
            return
        }

        updated |= loyaltyMemberService.updateMemberOfferField(id, "remainingRedemptions", remainingRedemptions)
        updated |= loyaltyMemberService.updateMemberOfferField(id, "status", status)

        if (updated) {
            flash.message = "Member Offer updated successfully"
        }

        redirect(action: "loyaltyMembers")
    }

    def memberUpdateSave() {
        String cardNumber
        String firstName
        String lastName
        String email
        String mobile_no
        Boolean updated = false

        try {
            cardNumber = params.cardNumber
            firstName = params.firstName
            lastName = params.lastName
            email = params.email
            mobile_no = params.mobile_no
        } catch (Exception e) {
            log.error("Error when attempting to update loyalty member details, Exception " + e)
            response.status = 400
            return
        }

        def member = loyaltyMemberService.findByCardNumber(cardNumber)

        if (member) {
            if (member.firstName != firstName) {
                updated = true
                loyaltyMemberService.updateMemberField(cardNumber, "firstName", firstName)
            }

            if (member.lastName != lastName) {
                updated = true
                loyaltyMemberService.updateMemberField(cardNumber, "lastName", lastName)
            }

            if (member.email != email) {
                updated = true
                loyaltyMemberService.updateMemberField(cardNumber, "email", email)
            }

            if (member.mobile_no != mobile_no) {
                updated = true
                loyaltyMemberService.updateMemberField(cardNumber, "mobile_no", mobile_no)
            }
        }

        if (updated) {
            flash.message = "Member updated successfully"
        }

        redirect(action: "loyaltyMembers")
    }

    def ajaxMemberOffers() {
        String cardNumber
        String searchBy
        String searchTerm
        Boolean activeOffers
        Boolean inactiveOffers
        Integer max
        Integer offset
        String sortColumn
        String sortOrder

        try {
            cardNumber = params.cardNumber
            searchBy = params.searchBy ? params.searchBy : ""
            searchTerm = params.searchTerm ? params.searchTerm : ""
            activeOffers = params.activeOffers ? params.activeOffers.toBoolean() : false
            inactiveOffers = params.inactiveOffers ? params.inactiveOffers.toBoolean() : false
            max = params.max ? Integer.parseInt(params.max) : null
            offset = params.offset ? Integer.parseInt(params.offset) : null
            sortColumn = validateSortColumn(params.sortColumn)
            sortOrder = validateSortOrder(params.sortOrder)
        } catch (Exception e) {
            log.error("Error when retrieving loyalty member offers, Exception " + e)
            response.status = 400
            return
        }

        def offers = loyaltyMemberService.findAllMemberOffers(cardNumber, searchTerm, searchBy, activeOffers, inactiveOffers, max, offset, sortColumn, sortOrder)

        render(template: "memberOffersSearchResults", model: [cardNumber: params.cardNumber,
                                                              searchTerm: params.searchTerm,
                                                              searchBy  : params.searchBy,
                                                              offset    : params.offset,
                                                              max       : params.max,
                                                              sortColumn: params.sortColumn,
                                                              sortOrder : params.sortOrder,
                                                              offers: offers["offers"],
                                                              totalResults: offers["totalResults"]])
    }

    def ajaxMemberTransactions() {
        DateTimeFormatter dateFormatter = DateTimeFormat.forPattern("dd/MM/yyyy").withZone(DateTimeZone.UTC)
        String cardNumber
        String searchBy
        String searchTerm
        double minAmount
        double maxAmount
        DateTime startWindow
        DateTime endWindow
        Integer max
        Integer offset
        String sortColumn
        String sortOrder

        try {
            cardNumber = params.cardNumber
            searchBy = params.searchBy ? params.searchBy : ""
            searchTerm = params.searchTerm ? params.searchTerm : ""
            minAmount = params.minAmount ? Double.parseDouble(params.minAmount) : 0
            maxAmount = params.maxAmount ? Double.parseDouble(params.maxAmount) : 0
            startWindow = params.startWindow ? DateTime.parse(params.startWindow, dateFormatter) : null
            endWindow = params.endWindow ? DateTime.parse(params.endWindow, dateFormatter).plusDays(1) : null
            max = params.max ? Integer.parseInt(params.max) : null
            offset = params.offset ? Integer.parseInt(params.offset) : null
            sortColumn = validateSortColumn(params.sortColumn)
            sortOrder = validateSortOrder(params.sortOrder)
        } catch (Exception e) {
            log.error("Error when retrieving loyalty member transactions, Exception " + e)
            response.status = 400
            return
        }

        /* Get the member associated with the card number so can retrieve the transaction records */
        def member = loyaltyMemberService.findByCardNumber(cardNumber)

        def transactions = memberTransactionService.findAllTransactionsByMemberId(member.id, searchTerm, searchBy, minAmount, maxAmount,
                                                                                         startWindow, endWindow, max, offset, sortColumn, sortOrder)

        render(template: "transactionSearchResults", model: [cardNumber : params.cardNumber,
                                                             searchTerm : params.searchTerm,
                                                             searchBy   : params.searchBy,
                                                             startWindow: params.startWindow,
                                                             endWindow  : params.endWindow,
                                                             minAmount  : params.minAmount,
                                                             maxAmount  : params.maxAmount,
                                                             offset     : params.offset,
                                                             max        : params.max,
                                                             sortColumn : params.sortColumn,
                                                             sortOrder  : params.sortOrder,
                                                             transactions: transactions["transactions"],
                                                             totalResults: transactions["totalResults"]])
    }

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
            log.error("Error when searching for loyalty members, Exception " + e)
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
        def availableColumns = [ "cardNumber", "email", "firstName", "lastName", "storeId", "storeName", "transactionId", "status",
                                 "transactionTotal", "transactionTimestamp", "startDate", "endDate", "offerDescription", "currentRedemptions", "remainingRedemptions" ]

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
            DateTimeFormatter dateFormatter = DateTimeFormat.forPattern("dd/MM/yyyy")

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
            log.error("Error loading loyalty offer view window, Exception " + ex)
            flash.error = "Failed to load loyalty offer view"
            response.setStatus(302)
            redirect(controller: "loyalty", action: "loyaltyOffers")
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
                response.setStatus(302)
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
