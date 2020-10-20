package uk.co.wonderlane.wlpos

import grails.gorm.transactions.Transactional
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

    def getProductList(int id) {
        return ProductList.findByIdAndRetailerId(id, springSecurityService.principal.retailerId)
    }

    def saveProductList(ProductList productList) {
        productList.save()
    }
}