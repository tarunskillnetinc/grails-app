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

    ProductHistoryBuilder(Integer productId, SpringSecurityService springSecurityService) {
        this.productId = productId
        this.springSecurityService = springSecurityService
    }

    def compare(Integer productVariantId, String property, Object left, Object right) {
        if (left != right) {
            def productHistory = new ProductHistory();
            productHistory.fromValue = left.toString().substring(0, left.toString().length() > 100 ? 99 : left.toString().length())
            productHistory.toValue = right.toString().substring(0, right.toString().length() > 100 ? 99 : right.toString().length())
            productHistory.field = property
            productHistory.productHistoryType = ProductHistoryType.FIELD
            productHistory.productId = productId
            productHistory.usersName = springSecurityService.principal.username
            productHistory.userId = springSecurityService.principal.id
            productHistory.storeId = springSecurityService.principal.storeId
            productHistory.updateDate = now
            productHistory.effectiveDate = now
            productHistory.productVariantId = productVariantId
            productHistories.add(productHistory)
        }
    }

    def compare(String property, Object left, Object right) {
        compare(null, property, left, right)
    }
}
