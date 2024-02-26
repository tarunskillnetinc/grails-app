package uk.co.wonderlane.wlpos

import org.joda.time.DateTime
import org.joda.time.DateTimeZone


class LoyaltyController {

    def index() { }

    def loyaltySegment(){}

    def ajaxSearchLoyaltySegment() {
        session.LOYALTY_SEGMENT_SEARCH_TERM = params.searchTerm
        session.effectiveDate = ["Current", DateTime.now(DateTimeZone.UTC)]

        def products = productService.searchProductsHql(params.searchTerm, params.searchBy, params.max ? Integer.parseInt(params.max) : 50, params.offset ? Integer.parseInt(params.offset) : 0, "id", "asc")

        render(template: "loyaltySegmentSearchResults", model: [products    : products.products,
                                                         storeId     : springSecurityService.principal.storeId,
                                                         loyaltySegmentTerm  : params.loyaltySegmentTerm,
                                                         loyaltySegmentSearchBy    : params.loyaltySegmentSearchBy,
                                                         max         : params.max ?: 20,
                                                         offset      : params.offset,
                                                         totalResults: products.totalCount])
    }
}
