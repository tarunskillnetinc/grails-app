package uk.co.wonderlane.wlpos

import grails.gorm.transactions.Transactional

@Transactional
class ProductGroupService {

    def springSecurityService

    def getProductGroups(String searchTerm = null, String searchBy = "everything", int offset = 0, int max = 50, String sort = "description", String order = "ASC") {
        return ProductGroup.createCriteria().list([offset: offset, max: max, sort: sort, order: order]) {
            eq ("retailerId", springSecurityService.principal.retailerId)
            eq ("hidden", false)

            if (searchTerm) {
                if (searchBy == "everything") {
                    if (searchTerm.isNumber()) {
                        or {
                            sqlRestriction "cast( id AS char( 256 )) like '%${searchTerm}%'";
                            like("description", "%$searchTerm%")
                        }
                    } else {
                        like("description", "%$searchTerm%")
                    }
                } else if (searchBy == "description") {
                    like("description", "%$searchTerm%")
                } else if (searchBy == "productGroupId") {
                    sqlRestriction "cast( id AS char( 256 )) like '%${searchTerm}%'"
                }
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
        ProductGroupProduct.executeUpdate("delete ProductGroupProduct pgp where pgp.productgroup.id = :productgroupId and pgp.sku = :sku", [productgroupId: productGroupId, sku: sku])
    }
}
