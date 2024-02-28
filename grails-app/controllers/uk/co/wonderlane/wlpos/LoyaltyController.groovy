package uk.co.wonderlane.wlpos


import uk.co.wonderlane.wlpos.reporting.SortParams

class LoyaltyController {

    def loyaltyService
    def promotionService
    def springSecurityService

    def index() { }

    def loyaltySegment(){}

    def loyaltyOffers(){}

    def ajaxSearchLoyaltySegment() {
        int defaultPagination = 20
        int defaultOffSet = 0
        try {
            def segment = loyaltyService.getSegment(params.searchTerm, params.searchBy, params.max ? Integer.parseInt(params.max) : defaultPagination,
                    params.offset ? Integer.parseInt(params.offset) : defaultOffSet, "id", "asc")

            render(template: "loyaltySegmentSearchResults", model: [segments    : segment?.segments,
                                                                    loyaltySegmentTerm  : params.loyaltySegmentTerm,
                                                                    loyaltySegmentSearchBy    : params.loyaltySegmentSearchBy,
                                                                    max         : params.max ?: defaultPagination,
                                                                    offset      : params.offset ?: defaultOffSet,
                                                                    totalCount  : segment?.totalCount
            ])
        }catch(Exception ex){
            ex.printStackTrace()
            log.error("Error when loading loyalty segment search results, Search by " + params.searchBy + " search term " + params.searchTerm + " Exception "  + ex)
            response.setStatus(500)
            render (view: "_loyaltyGenericError", contentType: "text/html", model: [
                                                                                    error_header : "Loyalty Segment Search Error",
                                                                                    error_body   : "Error when loading loyalty segment"
            ])
        }

    }


    def ajaxSearchLoyaltyOffers(SortParams sortParams){
        int defaultPagination = 20
        int defaultOffSet = 0
        try {
            def offer = loyaltyService.getLoyaltyOffers(params.searchTerm, params.searchBy, params.max ? Integer.parseInt(params.max) : defaultPagination,
                    params.offset ? Integer.parseInt(params.offset) : defaultOffSet, sortParams.sortColumn, sortParams.sortOrder)

            render(template: "loyaltyOffersSearchResults", model: [offers    : offer?.offers,
                                                                   loyaltyOffersTerm  : params.loyaltyOffersTerm,
                                                                   loyaltyOffersSearchBy    : params.loyaltyOffersSearchBy,
                                                                   max         : params.max ?: defaultPagination,
                                                                   offset      : params.offset ?: defaultOffSet,
                                                                   totalCount  : offer?.totalCount,
                                                                   sortParams  : sortParams
            ])
        }catch(Exception ex){
            ex.printStackTrace()
            log.error("Error when loading loyalty offers search results, Search by " + params.searchBy + " search term " + params.searchTerm + " Exception "  + ex)
            response.setStatus(500)
            render (view: "_loyaltyGenericError", contentType: "text/html", model: [
                    error_header : "Loyalty Offers Search Error",
                    error_body   : "Error when loading loyalty offers"
            ])
        }
    }

    def addLoyaltyOffer(){
        //load all promotions for retailer
        List<Promotion> promotionList = promotionService.getPromotionForRetailer(springSecurityService.principal.retailerId)
        Map<String, String> promotions = promotionList.collectEntries { promotion -> [promotion.id, promotion.description] }

        //load all segments for retailer
        def segments = loyaltyService.getLoyaltySegmentForRetailer(springSecurityService.principal.retailerId)



        //pass them into view

        render(view: "addLoyaltyOffer", model: [
                                                promotions : promotions
        ])

    }
}
