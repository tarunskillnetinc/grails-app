package uk.co.wonderlane.wlpos

import grails.gorm.transactions.Transactional
import org.joda.time.DateTime

@Transactional
class ProductGroupService {

    def springSecurityService

    def getProductGroups(String searchTerm = null, String searchBy = "everything", DateTime startDate = null, DateTime endDate = null, String status = null ,  int offset = 0, int max = 50,
                         String sort = "description", String order = "asc") {
        def productGroups =  ProductGroup.createCriteria().list([offset: offset, max: max]) {
            eq ("retailerId", springSecurityService.principal.retailerId)

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

            if (startDate) {
                gte("startDate", startDate) // startDate >= given startDate
            }

            if (endDate) {
                lte("endDate", endDate) // endDate <= given endDate
            }

            if (status != null) {  // Status Filtering
                Boolean activeStatus = status != null ? status.toString().equalsIgnoreCase("ACTIVE") : null
                eq("active", activeStatus) // Filters active/inactive records
            }
        }

        // **Sorting in Memory** (if sorting by product count is required)
        if (sort == "productCount") {
            productGroups = productGroups?.sort { it?.productGroupProducts?.size() }
            if (order == "desc") {
                productGroups = productGroups?.reverse()
            }
        } else if (sort == "restrictiontype") {
            productGroups = productGroups?.sort { it?.restrictionType?.size() }
            if (order == "desc") {
                productGroups = productGroups?.reverse()
            }
        } else {
            productGroups.sort { it."${sort}" }
            if (order == "desc") {
                productGroups = productGroups?.reverse()
            }
        }

        return productGroups

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
