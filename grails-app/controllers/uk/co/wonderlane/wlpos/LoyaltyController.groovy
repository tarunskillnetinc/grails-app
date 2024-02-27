package uk.co.wonderlane.wlpos

import org.joda.time.DateTime
import org.joda.time.DateTimeZone


class LoyaltyController {

    def loyaltyService

    def index() { }

    def loyaltySegment(){}

    def ajaxSearchLoyaltySegment() {
        try {
            def segment = loyaltyService.getSegment(params.searchTerm, params.searchBy, params.max ? Integer.parseInt(params.max) : 20,
                    params.offset ? Integer.parseInt(params.offset) : 0, "id", "asc")

            render(template: "loyaltySegmentSearchResults", model: [segments    : segment?.segments,
                                                                    loyaltySegmentTerm  : params.loyaltySegmentTerm,
                                                                    loyaltySegmentSearchBy    : params.loyaltySegmentSearchBy,
                                                                    max         : params.max ?: 20,
                                                                    offset      : params.offset,
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
}
