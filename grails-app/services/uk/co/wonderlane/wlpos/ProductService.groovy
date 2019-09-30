package uk.co.wonderlane.wlpos

import grails.gorm.transactions.Transactional
import org.hibernate.criterion.CriteriaSpecification
import org.springframework.validation.BindingResult

@Transactional
class ProductService {

    def springSecurityService

    def getProducts(int max, int offset) {
        return Product.list(max: max, offset: offset)
    }

    def getProductVariant(int id) {
        return ProductVariant.get(id)
    }

    def getProduct(int id) {
        return Product.get(id)
    }

    def searchProducts(String searchTerm, String searchBy = "everything", int maxResults = 50, int startIndex = 0, String sortColumn = "id", String orderBy = "asc") {
        String searchQuery = """SELECT DISTINCT p
                                FROM Product p
                                JOIN ProductData pd ON p.id = pd.id
                                JOIN ProductVariant pv ON p = pv.product AND pv.storeId = :storeId
                                WHERE ((:searchBy = 'everything' OR :searchBy = 'itemCode') AND (pv.itemCode LIKE :searchTerm OR p.itemCode LIKE :searchTerm))
                                OR ((:searchBy = 'everything' OR :searchBy = 'description') AND p.description LIKE :searchTerm)
                                AND p.retailerId = :retailerId
                                AND pd.storeId = :storeId
                                AND pd.effectiveDate <= :effectiveDate
                                ORDER BY p.${sortColumn} ${orderBy}"""

        def products = Product.executeQuery(searchQuery, [searchTerm: "%${searchTerm}%", searchBy: searchBy, retailerId: springSecurityService.principal.retailerId, storeId: springSecurityService.principal.storeId, effectiveDate: new Date()], [max: maxResults, offset: startIndex])

        // For quality of life let's keep the currently active product data for your store stored.
        products.each { product ->
            product.currentProductData = product.productDatas.sort { it.effectiveDate }.reverse().find {
                it.storeId == springSecurityService.principal.storeId && it.effectiveDate <= new Date()
            }
        }

        return products
    }

    def countProductSearch(String searchTerm, String searchBy = "everything") {
        String searchQuery = """SELECT DISTINCT COUNT(p)
                                FROM Product p
                                JOIN ProductData pd ON p.id = pd.id
                                JOIN ProductVariant pv ON p = pv.product AND pv.storeId = :storeId
                                WHERE ((:searchBy = 'everything' OR :searchBy = 'itemCode') AND pv.itemCode LIKE :searchTerm)
                                OR ((:searchBy = 'everything' OR :searchBy = 'description') AND p.description LIKE :searchTerm)
                                AND p.retailerId = :retailerId
                                AND pd.storeId = :storeId
                                AND pd.effectiveDate <= :effectiveDate"""
        def count = Product.executeQuery(searchQuery, [searchTerm: "%${searchTerm}%", searchBy: searchBy, retailerId: springSecurityService.principal.retailerId, storeId: springSecurityService.principal.storeId, effectiveDate: new Date()])
        return count[0]
    }

    def populateCurrentProductData(def product) {
        product.currentProductData = product.productDatas.sort { it.effectiveDate }.reverse().find { it.storeId == springSecurityService.principal.storeId && it.effectiveDate <= new Date() }
    }

    def searchPaginationNumbers(Integer page, Integer pageCount){
        List<Integer> numbers = new ArrayList<>()
        if (pageCount < 10) {
            (1..pageCount).each {
                numbers.add(it)
            }
            return numbers
        } else {
            if (page < 6) {
                (1..10).each {
                    numbers.add(it)
                }
                return numbers
            } else if (pageCount - page < 6) {
                (pageCount - 10..pageCount).each{
                    numbers.add(it)
                }
                return numbers
            } else {
                (page - 4..page + 5).each {
                    numbers.add(it)
                }
                return numbers
            }
        }
    }
}