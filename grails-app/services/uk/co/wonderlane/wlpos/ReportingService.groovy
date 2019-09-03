package uk.co.wonderlane.wlpos

import grails.gorm.transactions.Transactional

@Transactional("reporting")
class ReportingService {

    def springSecurityService

    def getSales(Date startDate, Date endDate) {
        def salesCriteria = Sale.createCriteria()

        return salesCriteria.list() {
            eq ("retailerId", springSecurityService.principal.retailerId)
            eq ("storeId", springSecurityService.principal.storeId)
            between ("dateCreated", startDate, endDate)
        }

        /*String searchQuery = """SELECT s
                                FROM Sale s
                                JOIN SaleCategory sc ON s.id = sc.sales
                                WHERE s.retailerId = :retailerId
                                AND s.storeId = :storeId
                                AND s.dateCreated >= :startDate
                                AND s.dateCreated <= :endDate"""

        return Sale.executeQuery(searchQuery, [retailerId: springSecurityService.principal.retailerId, storeId: springSecurityService.principal.storeId, startDate: startDate, endDate: endDate])*/
    }

    def getSalesForCategory(int categoryId, Date startDate, Date endDate) {
        String searchQuery = """SELECT s
                                FROM Sale s
                                JOIN SaleCategory sc ON s.id = sc.sales
                                WHERE sc.categoryId = :categoryId
                                AND s.retailerId = :retailerId
                                AND s.storeId = :storeId
                                AND s.dateCreated >= :startDate
                                AND s.dateCreated <= :endDate"""

        return Sale.executeQuery(searchQuery, [categoryId: categoryId, retailerId: springSecurityService.principal.retailerId, storeId: springSecurityService.principal.storeId, startDate: startDate, endDate: endDate])
    }

    def getSalesForProduct(int productId, Date startDate, Date endDate, int maxResults, int startIndex, String sortColumn, String sortOrder) {
        String sort

        if (sortColumn == "description") {
            sort = "s.productItemCode ${sortOrder}, s.productDescription ${sortOrder}"
        } else if (sortColumn == "netTotal") {
            sort = "(s.retailPrice - s.vatAmount) ${sortOrder}"
        } else if (sortColumn == "category") {
            sort = "sc.categoryDescription ${sortOrder}"
        } else if (sortColumn == "profit") {
            sort = "(s.retailPrice - s.costPrice) ${sortOrder}"
        } else {
            sort = "s.${sortColumn} ${sortOrder}"
        }

        String searchQuery = """SELECT s
                                FROM Sale s
                                WHERE s.productId = :productId
                                AND s.retailerId = :retailerId
                                AND s.storeId = :storeId
                                AND s.dateCreated >= :startDate
                                AND s.dateCreated < :endDate
                                ORDER BY ${sort}"""

        return Sale.executeQuery(searchQuery, [productId: productId, retailerId: springSecurityService.principal.retailerId, storeId: springSecurityService.principal.storeId, startDate: startDate, endDate: endDate])
    }
}