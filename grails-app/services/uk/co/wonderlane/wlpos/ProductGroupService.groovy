package uk.co.wonderlane.wlpos

import grails.gorm.transactions.Transactional
import org.joda.time.DateTime

@Transactional
class ProductGroupService {

    def springSecurityService

    def getProductGroups(String searchTerm = null, String searchBy = "everything", DateTime startDate = null, DateTime endDate = null, Boolean status = null ,  int offset = 0, int max = 50,
                         String sort = "name", String order = "asc") {
        return ProductGroup.createCriteria().list([offset: offset, max: max, sort: sort, order: order]) {
            eq ("retailerId", springSecurityService.principal.retailerId)
            eq ("hidden", false)

            if (searchTerm) {
                if (searchBy == "everything") {
                    if (searchTerm.isNumber()) {
                        or {
                            sqlRestriction "cast( id AS char( 256 )) like '%${searchTerm}%'";
                            like("name", "%$searchTerm%")
                        }
                    } else {
                        like("name", "%$searchTerm%")
                    }
                } else if (searchBy == "name") {
                    like("name", "%$searchTerm%")
                } else if (searchBy == "productGroupId") {
                    sqlRestriction "cast( id AS char( 256 )) like '%${searchTerm}%'"
                }
            }

            if (startDate) {
                gte("startDate", startDate) // startDate >= given startDate
            }
            if (endDate) {
                le("endDate", endDate) // endDate <= given endDate
            }

            if (status != null) {  // Status Filtering
                eq("active", status) // Filters active/inactive records
            }
        }

    }

    def getProductGroup(int id) {
        return ProductGroup.findByIdAndRetailerId(id, springSecurityService.principal.retailerId)
    }

    def saveProductGroup(ProductGroup productGroup) {
        productGroup.save()
    }

    def deleteProductGroupProduct(ProductGroupProduct productGroupProduct) {
        productGroupProduct.delete()
    }

    def deleteProductGroupProduct(int productGroupId, long sku) {
        ProductGroupProduct.executeUpdate("delete ProductGroupProduct tp where tp.productGroup.id = :productGroupId and tp.sku = :sku", [productGroupId: productGroupId, sku: sku])
    }
}
