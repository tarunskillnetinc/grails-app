package uk.co.wonderlane.wlpos

import grails.gorm.transactions.Transactional
import org.joda.time.DateTime
import uk.co.wonderlane.wlpos.enums.wlim.ProductListType

@Transactional
class ProductListService {

    def springSecurityService

    def getCentralCounts(String searchTerm = null, int offset = 0, int max = 50, String sort = "startDate", String order = "DESC") {
        return ProductList.createCriteria().list([offset: offset, max: max, sort: sort, order: order]) {
            eq ("retailerId", springSecurityService.principal.retailerId)
            eq ("storeId", springSecurityService.principal.storeId)
            eq ("type", ProductListType.SCHEDULED_COUNT)

            if (searchTerm) {
                like ("description", "%$searchTerm%")
            }
        }
    }

    def getOrders(DateTime startDate, DateTime endDate, Integer storeId, Integer supplierId, int offset = 0, int max = 50, String sort = "dateStarted", String order = "DESC") {
        return ProductList.createCriteria().list([offset: offset, max: max, sort: sort, order: order]) {
            eq ("retailerId", springSecurityService.principal.retailerId)

            if (storeId) {
                eq ("storeId", storeId)
            }

            if (supplierId) {
                eq ("supplierId", supplierId)
            }

            eq ("type", ProductListType.ORDER)

            between ("dateStarted", startDate, endDate)
        }
    }

    def getProductList(int id) {
        return ProductList.findByIdAndRetailerId(id, springSecurityService.principal.retailerId)
    }

    def saveProductList(ProductList productList) {
        productList.save()
    }
}