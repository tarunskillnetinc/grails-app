package uk.co.wonderlane.wlpos

import grails.gorm.transactions.Transactional
import org.hibernate.criterion.CriteriaSpecification

@Transactional
class ProductService {

    def springSecurityService

    def getProducts(int max, int offset) {
        return Product.list(max: max, offset: offset)
    }

    def getProduct(int id) {
        return Product.get(id)
    }

    def searchProducts(String searchTerm, String searchBy = "everything", int maxResults = 50, int startIndex = 0, String sortColumn = "id", String orderBy = "asc") {
        def productSearchCriteria = Product.createCriteria()

        def products = productSearchCriteria.list(max: maxResults, offset: startIndex) {
            if (searchBy.equalsIgnoreCase("everything")) {
                or {
                    like("description", "%$searchTerm%")
                    like("itemCode", "%$searchTerm%")
                }
            } else if (searchBy.equalsIgnoreCase("description")) {
                like("description", "%$searchTerm%")
            } else if (searchBy.equalsIgnoreCase("itemCode")) {
                like("itemCode", "%$searchTerm%")
            }

            eq ("retailerId", springSecurityService.principal.retailerId)
            productDatas {
                eq ("storeId", springSecurityService.principal.storeId)
            }

            order(sortColumn, orderBy)
        }

        return products
    }

    def searchProductsChrisTest(String searchTerm, String searchBy = "everything", int maxResults = 50, int startIndex = 0, String sortColumn = "id", String sortOrder = "asc") {
        String searchQuery = """SELECT DISTINCT p
                                FROM Product p
                                JOIN ProductData pd ON p.id = pd.id
                                WHERE ((:searchBy = 'everything' OR :searchBy = 'itemCode') AND p.itemCode LIKE :searchTerm)
                                OR ((:searchBy = 'everything' OR :searchBy = 'description') AND p.description LIKE :searchTerm)
                                AND p.retailerId = :retailerId
                                AND pd.storeId = :storeId
                                AND pd.effectiveDate <= :effectiveDate
                                ORDER BY p.${sortColumn} ${sortOrder}"""

        def products = Product.executeQuery(searchQuery, [searchTerm: "%${searchTerm}%", searchBy: searchBy, retailerId: springSecurityService.principal.retailerId, storeId: springSecurityService.principal.storeId, effectiveDate: new Date()], [max: maxResults, offset: startIndex])

        // For quality of life let's keep the currently active product data for your store stored.
        products.each { product ->
            product.currentProductData = product.productDatas.sort { it.effectiveDate }.reverse().find { it.storeId == springSecurityService.principal.storeId && it.effectiveDate <= new Date() }
        }

        return products
    }
}