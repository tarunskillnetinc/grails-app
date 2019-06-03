package uk.co.wonderlane.wlpos

import grails.gorm.transactions.Transactional

@Transactional
class ProductService {

    def getProducts(int max, int offset) {
        return Product.list(max: max, offset: offset)
    }

    def getProduct(int id) {
        return Product.get(id)
    }

    def searchProducts(String searchTerm, String searchBy = "everything", int maxResults = 50, int startIndex = 0, String sortColumn = "id", String orderBy = "asc") {
        def productSearchCriteria = Product.createCriteria()

        // TODO Add retailer ID and store ID to search query.

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

            order(sortColumn, orderBy)
        }

        return products
    }
}