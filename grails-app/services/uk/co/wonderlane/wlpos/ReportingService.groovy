package uk.co.wonderlane.wlpos

import grails.gorm.transactions.Transactional
import org.joda.time.DateTime
import uk.co.wonderlane.wlpos.enums.PromotionType
import uk.co.wonderlane.wlpos.enums.TillControlEventType
import uk.co.wonderlane.wlpos.reporting.PayPointSale
import uk.co.wonderlane.wlpos.reporting.PromotionSale
import uk.co.wonderlane.wlpos.reporting.PromotionSaleProduct
import uk.co.wonderlane.wlpos.reporting.ReportType
import uk.co.wonderlane.wlpos.reporting.Sale
import uk.co.wonderlane.wlpos.reporting.SaleCategory
import uk.co.wonderlane.wlpos.reporting.TillControlEvent
import uk.co.wonderlane.wlpos.reporting.ReportColumns

@Transactional("reporting")
class ReportingService {

    def springSecurityService

    // For sales report grouped by department, no pagination on here as the results are grouped into categories.
    def getSales(DateTime startDate, DateTime endDate, Integer storeId) {
        def salesCriteria = Sale.createCriteria()

        return salesCriteria.list() {
            eq ("retailerId", springSecurityService.principal.retailerId)
            if (storeId != null) {
                eq("storeId", storeId)
            }
            between ("dateCreated", startDate, endDate)
        }
    }

    // For sales report grouped by category, no pagination on here as the results can still be grouped into categories.
    def getSalesForCategory(int categoryId, DateTime startDate, DateTime endDate, Integer storeId) {
        String searchQuery = """SELECT s
                                FROM Sale s
                                JOIN SaleCategory sc ON s.id = sc.sales
                                WHERE sc.categoryId = :categoryId
                                AND s.retailerId = :retailerId """

        if (storeId != null) {
            searchQuery += """AND s.storeId = :storeId """
        }

        searchQuery += """AND s.dateCreated >= :startDate
                          AND s.dateCreated <= :endDate"""

        def queryParams = [categoryId: categoryId, retailerId: springSecurityService.principal.retailerId, startDate: startDate, endDate: endDate]

        if (storeId != null) {
            queryParams.storeId = storeId
        }

        return Sale.executeQuery(searchQuery, queryParams)
    }

    def getSales(Integer storeId, DateTime startDate, DateTime endDate) {
        String searchQuery = """SELECT s
                                FROM Sale s
                                WHERE s.retailerId = :retailerId """

        if (storeId != null) {
            searchQuery += """AND s.storeId = :storeId """
        }

        searchQuery += """AND s.dateCreated >= :startDate
                          AND s.dateCreated <= :endDate"""

        def queryParams = [retailerId: springSecurityService.principal.retailerId, startDate: startDate, endDate: endDate]

        if (storeId != null) {
            queryParams.storeId = storeId
        }

        return Sale.executeQuery(searchQuery, queryParams)
    }

    // For sales report product level. Paginated and filtered.
    def getSalesForProduct(int productId, DateTime startDate, DateTime endDate, int maxResults, int startIndex,
                           String sortColumn, String sortOrder, String descriptionFilter, Integer storeId) {
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
                                AND s.retailerId = :retailerId """

        if (storeId != null) {
            searchQuery += """AND s.storeId = :storeId """
        }

        searchQuery += """AND s.dateCreated >= :startDate
                          AND s.dateCreated < :endDate
                          AND CONCAT(s.productItemCode, s.productDescription) LIKE :descriptionFilter
                          ORDER BY ${sort}"""

        def queryParams = [productId: productId, retailerId: springSecurityService.principal.retailerId, descriptionFilter: "%"+descriptionFilter+"%", startDate: startDate, endDate: endDate, max: maxResults, offset: startIndex]

        if (storeId != null) {
            queryParams.storeId = storeId
        }

        return Sale.executeQuery(searchQuery, queryParams)
    }

    def countSalesForProduct(int productId, DateTime startDate, DateTime endDate, String descriptionFilter, Integer storeId) {
        String searchQuery = """SELECT COUNT(s)
                                FROM Sale s
                                WHERE s.productId = :productId
                                AND s.retailerId = :retailerId """

        if (storeId != null) {
            searchQuery += """AND s.storeId = :storeId """
        }

        searchQuery += """AND s.dateCreated >= :startDate
                          AND s.dateCreated < :endDate
                          AND CONCAT(s.productItemCode, s.productDescription) LIKE :descriptionFilter"""

        def queryParams = [productId: productId, retailerId: springSecurityService.principal.retailerId, descriptionFilter: "%"+descriptionFilter+"%", startDate: startDate, endDate: endDate]

        if (storeId != null) {
            queryParams.storeId = storeId
        }

        return Sale.executeQuery(searchQuery, queryParams)[0]
    }

    // For promotions grouped report. No pagination here as we're going to group them, but the filtering can be done in the database.
    def getPromotionSales(DateTime startDate, DateTime endDate, String descriptionFilter, PromotionType promotionTypeFilter, Integer storeId) {
        def promotionsCriteria = PromotionSale.createCriteria()

        return promotionsCriteria.list() {
            eq ("retailerId", springSecurityService.principal.retailerId)
            if (storeId != null) {
                eq("storeId", storeId)
            }
            if (descriptionFilter) {
                like ("description", "%"+descriptionFilter+"%")
            }
            if (promotionTypeFilter) {
                eq ("type", promotionTypeFilter)
            }
            between ("dateCreated", startDate, endDate)
        }
    }

    // For promotions report at promotion level. Filtered and paginated.
    def getPromotionSales(DateTime startDate, DateTime endDate, int promotionId, int maxResults, int startIndex, String sortColumn, String sortOrder, Integer storeId) {
        def promotionsCriteria = PromotionSale.createCriteria()

        def results = promotionsCriteria.list([sort: sortColumn, order: sortOrder, offset: startIndex, max: maxResults]) {
            eq ("retailerId", springSecurityService.principal.retailerId)
            if (storeId != null) {
                eq("storeId", storeId)
            }
            between ("dateCreated", startDate, endDate)
            eq ("promotionId", promotionId)
        }

        // Criteria.list() with max and offset returns a totalCount, but for some reason I am having to read that value otherwise an error is thrown when trying to use it back in the controller.
        // I believe this may be related to the domain class being in an alternate datasource, but I think it's a bug in Grails. Actually, I think it's because the totalCount is lazily loaded
        // to prevent the double query immediately. But it's throwing a Hibernate session error if I don't request it here.
        int totalCount = results.totalCount
        return results
    }

    // For promotion report product level. Filtered and paginated.
    def getPromotionSaleProducts(int promotionSaleId, String productFilter, int maxResults, int startIndex, String sortColumn, String sortOrder, Integer storeId) {
        def promotionProductsCriteria = PromotionSaleProduct.createCriteria()

        def results = promotionProductsCriteria.list([sort: sortColumn, order: sortOrder, offset: startIndex, max: maxResults]) {
            promotion {
                eq ("retailerId", springSecurityService.principal.retailerId)
                if (storeId != null) {
                    eq("storeId", storeId)
                }
                eq ("id", promotionSaleId)
            }
            if (productFilter) {
                or {
                    like("itemCode", "%"+productFilter+"%")
                    like("description", "%"+productFilter+"%")
                }
            }
        }

        // Criteria.list() with max and offset returns a totalCount, but for some reason I am having to read that value otherwise an error is thrown when trying to use it back in the controller.
        // I believe this may be related to the domain class being in an alternate datasource, but I think it's a bug in Grails. Actually, I think it's because the totalCount is lazily loaded
        // to prevent the double query immediately. But it's throwing a Hibernate session error if I don't request it here.
        int totalCount = results.totalCount
        return results
    }

    def getPromotionSale(int promotionSaleId) {
        def promotionSaleCriteria = PromotionSale.createCriteria()

        return promotionSaleCriteria.get() {
            eq ("id", promotionSaleId)
            eq ("retailerId", springSecurityService.principal.retailerId)
            if (springSecurityService.principal.storeId != null) {
                eq("storeId", springSecurityService.principal.storeId)
            }
        }
    }

    def getTillControlEvents(DateTime startDate, DateTime endDate) {
        def tillControlEventsCriteria = TillControlEvent.createCriteria()

        return tillControlEventsCriteria.list() {
            eq ("retailerId", springSecurityService.principal.retailerId)
            if (springSecurityService.principal.storeId != null) {
                eq ("storeId", springSecurityService.principal.storeId)
            }
            between ("dateCreated", startDate, endDate)
        }
    }

    def getTillControlEvents(DateTime startDate, DateTime endDate, TillControlEventType type, int maxResults, int startIndex, String sortColumn, String sortOrder) {
        def tillControlEventsCriteria = TillControlEvent.createCriteria()

        def results = tillControlEventsCriteria.list([sort: sortColumn, order: sortOrder, offset: startIndex, max: maxResults]) {
            eq ("type", type)
            eq ("retailerId", springSecurityService.principal.retailerId)

            if (springSecurityService.principal.storeId != null) { // TODO OR storeId is passed in as a filter (to be added).
                eq("storeId", springSecurityService.principal.storeId)
            }

            between ("dateCreated", startDate, endDate)
        }

        // Criteria.list() with max and offset returns a totalCount, but for some reason I am having to read that value otherwise an error is thrown when trying to use it back in the controller.
        // I believe this may be related to the domain class being in an alternate datasource, but I think it's a bug in Grails. Actually, I think it's because the totalCount is lazily loaded
        // to prevent the double query immediately. But it's throwing a Hibernate session error if I don't request it here.
        int totalCount = results.totalCount
        return results
    }

    def getPayPointSales(DateTime startDate, DateTime endDate, Integer storeId, String status, String description, int maxResults, int startIndex, String sortColumn, String sortOrder) {
        def payPointCriteria = PayPointSale.createCriteria()

        def results = payPointCriteria.list([sort: sortColumn, order: sortOrder, offset: startIndex, max: maxResults]) {
            eq ("retailerId", springSecurityService.principal.retailerId)
            between("transactionDate", startDate, endDate)

            if (storeId != null) {
                eq("storeId", storeId)
            }

            if (status != null) {
                eq("status", status)
            }

            if (description != null) {
                like("description", "%" + description + "%")
            }
        }

        // Criteria.list() with max and offset returns a totalCount, but for some reason I am having to read that value otherwise an error is thrown when trying to use it back in the controller.
        // I believe this may be related to the domain class being in an alternate datasource, but I think it's a bug in Grails. Actually, I think it's because the totalCount is lazily loaded
        // to prevent the double query immediately. But it's throwing a Hibernate session error if I don't request it here.
        int totalCount = results.totalCount
        return results
    }

    def getReportColumns(ReportType reportType) {
        return ReportColumns.findByUserIdAndReportType(springSecurityService.principal.id, reportType)
    }

    def saveReportColumns(ReportColumns reportColumns) {
        reportColumns.save()
    }
}