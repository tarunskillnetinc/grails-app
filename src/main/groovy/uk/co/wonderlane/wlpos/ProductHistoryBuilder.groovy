package uk.co.wonderlane.wlpos

import grails.plugin.springsecurity.SpringSecurityService
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import uk.co.wonderlane.wlpos.enums.ProductHistoryType

class ProductHistoryBuilder {

    def productHistories = new ArrayList<ProductHistory>()

    def productId

    def springSecurityService

    def now = DateTime.now(DateTimeZone.UTC)

    def effectiveDate

    def productChanged

    def productVariantChanged

    ProductHistoryBuilder(Integer productId, SpringSecurityService springSecurityService, DateTime effectiveDate) {
        this.productId = productId
        this.springSecurityService = springSecurityService
        this.effectiveDate = effectiveDate
        this.productChanged = false
        this.productVariantChanged = new ArrayList<Integer>()
    }

    def compare(Integer productVariantId, String property, Object left, Object right, ProductHistoryType productHistoryType) {
        if (left != right) {
            if (productVariantId == null) {
                productChanged = true
            } else {
                productVariantChanged.add(productVariantId)
            }

            def productHistory = new ProductHistory()

            productHistory.retailerId = springSecurityService.principal.retailerId
            productHistory.fromValue = left.toString().substring(0, left.toString().length() > 100 ? 99 : left.toString().length())
            productHistory.toValue = right.toString().substring(0, right.toString().length() > 100 ? 99 : right.toString().length())
            productHistory.field = property
            productHistory.productHistoryType = productHistoryType
            productHistory.productId = productId
            productHistory.usersName = springSecurityService.principal.usersName
            productHistory.userId = springSecurityService.principal.id
            productHistory.storeId = springSecurityService.principal.storeId
            productHistory.updateDate = now
            productHistory.effectiveDate = this.effectiveDate
            productHistory.productVariantId = productVariantId

            productHistories.add(productHistory)
        }
    }

    def compare(String property, Object left, Object right) {
        compare(null, property, left, right)
    }

    def compare(String property, Object left, Object right, ProductHistoryType productHistoryType) {
        compare(null, property, left, right, productHistoryType)
    }

    def compare(Integer productVariantId, String property, Object left, Object right) {
        compare(productVariantId, property, left, right, ProductHistoryType.FIELD)
    }

    def isProductChanged() {
        return productChanged
    }

    def ArrayList<Integer> getChangedProductVariantIds() {
        return productVariantChanged
    }
}
