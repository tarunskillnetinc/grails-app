package uk.co.wonderlane.wlpos

import grails.gorm.transactions.Transactional
import org.joda.time.DateTime

@Transactional
class ProductHistoryService {

    def springSecurityService

    //load product history by product id and retailer id
    def getProductHistory(int productId, DateTime dateTime) {
        def criteria = ProductHistory.createCriteria()
        return criteria.list {
            eq("productId", productId)
            eq ("retailerId", springSecurityService.principal.retailerId)
            lte("effectiveDate", dateTime)
        }
    }
}
