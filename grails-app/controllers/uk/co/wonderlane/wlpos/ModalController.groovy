package uk.co.wonderlane.wlpos

import org.joda.time.DateTime
import org.joda.time.DateTimeZone

class ModalController {

    def springSecurityService
    def productService

    def ajaxSearchProducts() {
        session.PRODUCT_SEARCH_TERM = params.searchTerm
        session.effectiveDate = ["Current", DateTime.now(DateTimeZone.UTC)]

        def products = productService.searchProductsHql(params.searchTerm, params.searchBy, 50, params.offset ? Integer.parseInt(params.offset) : 0, "id", "asc")

        render(template: "productSearchResults", model: [products: products.products, totalResults: products.totalCount, storeId: springSecurityService.principal.storeId])
    }
}
