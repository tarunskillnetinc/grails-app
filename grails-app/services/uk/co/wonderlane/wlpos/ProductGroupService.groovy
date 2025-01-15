package uk.co.wonderlane.wlpos

import grails.gorm.transactions.Transactional

@Transactional
class ProductGroupService {

    def springSecurityService

    def getTags(String searchTerm = null, String searchBy = "everything", int offset = 0, int max = 50, String sort = "description", String order = "ASC") {
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
                } else if (searchBy == "tagId") {
                    sqlRestriction "cast( id AS char( 256 )) like '%${searchTerm}%'"
                }
            }
        }
    }

    def getTag(int id) {
        return ProductGroup.findByIdAndRetailerId(id, springSecurityService.principal.retailerId)
    }

    def saveTag(ProductGroup productGroup) {
        productGroup.save()
    }

    def deleteTagProduct(ProductGroupProduct tagProduct) {
        tagProduct.delete()
    }

    def deleteTagProduct(int tagId, long sku) {
        ProductGroupProduct.executeUpdate("delete ProductGroupProduct tp where tp.tag.id = :tagId and tp.sku = :sku", [tagId: tagId, sku: sku])
    }
}
