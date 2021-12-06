package uk.co.wonderlane.wlpos

import grails.gorm.transactions.Transactional
import uk.co.wonderlane.wlpos.enums.PromotionType
import uk.co.wonderlane.wlpos.enums.TillControlEventType
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

    // For sales report grouped by department, no pagination on here as the results are grouped into categories. Needs to be moved into a procedure or HQL at some point.
    def getSales(Date startDate, Date endDate) {
        def salesCriteria = Sale.createCriteria()

        return salesCriteria.list() {
            eq ("retailerId", springSecurityService.principal.retailerId)
            if (springSecurityService.principal.storeId != null) {
                eq("storeId", springSecurityService.principal.storeId)
            }
            between ("dateCreated", startDate, endDate)
        }
    }

    // For sales report grouped by category, no pagination on here as the results can still be grouped into categories. Needs to be moved into a procedure or HQL at some point.
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

    def getSaleCategory(int categoryId) {
        def saleCategoryCriteria = SaleCategory.createCriteria()

        return saleCategoryCriteria.get() {
            eq ("id", categoryId)
        }
    }

    // For sales report product level. Paginated and filtered.
    def getSalesForProduct(int productId, Date startDate, Date endDate, int maxResults, int startIndex, String sortColumn, String sortOrder, String descriptionFilter) {
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
                                AND CONCAT(s.productItemCode, s.productDescription) LIKE :descriptionFilter
                                ORDER BY ${sort}"""

        return Sale.executeQuery(searchQuery, [productId: productId, retailerId: springSecurityService.principal.retailerId, storeId: springSecurityService.principal.storeId, descriptionFilter: "%"+descriptionFilter+"%", startDate: startDate, endDate: endDate, max: maxResults, offset: startIndex])
    }

    def countSalesForProduct(int productId, Date startDate, Date endDate, String descriptionFilter) {
        String searchQuery = """SELECT COUNT(s)
                                FROM Sale s
                                WHERE s.productId = :productId
                                AND s.retailerId = :retailerId
                                AND s.storeId = :storeId
                                AND s.dateCreated >= :startDate
                                AND s.dateCreated < :endDate
                                AND CONCAT(s.productItemCode, s.productDescription) LIKE :descriptionFilter"""

        return Sale.executeQuery(searchQuery, [productId: productId, retailerId: springSecurityService.principal.retailerId, storeId: springSecurityService.principal.storeId, descriptionFilter: "%"+descriptionFilter+"%", startDate: startDate, endDate: endDate])[0]
    }

    // For promotions grouped report. No pagination here as we're going to group them, but the filtering can be done in the database.
    def getPromotionSales(Date startDate, Date endDate, String descriptionFilter, PromotionType promotionTypeFilter) {
        def promotionsCriteria = PromotionSale.createCriteria()

        return promotionsCriteria.list() {
            eq ("retailerId", springSecurityService.principal.retailerId)
            eq ("storeId", springSecurityService.principal.storeId)
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
    def getPromotionSales(Date startDate, Date endDate, int promotionId, int maxResults, int startIndex, String sortColumn, String sortOrder) {
        def promotionsCriteria = PromotionSale.createCriteria()

        def results = promotionsCriteria.list([sort: sortColumn, order: sortOrder, offset: startIndex, max: maxResults]) {
            eq ("retailerId", springSecurityService.principal.retailerId)
            eq ("storeId", springSecurityService.principal.storeId)
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
    def getPromotionSaleProducts(int promotionSaleId, String productFilter, int maxResults, int startIndex, String sortColumn, String sortOrder) {
        def promotionProductsCriteria = PromotionSaleProduct.createCriteria()

        def results = promotionProductsCriteria.list([sort: sortColumn, order: sortOrder, offset: startIndex, max: maxResults]) {
            promotion {
                eq ("retailerId", springSecurityService.principal.retailerId)
                eq ("storeId", springSecurityService.principal.storeId)
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
            eq ("storeId", springSecurityService.principal.storeId)
        }
    }

    def getTillControlEvents(Date startDate, Date endDate) {
        def tillControlEventsCriteria = TillControlEvent.createCriteria()

        return tillControlEventsCriteria.list() {
            eq ("retailerId", springSecurityService.principal.retailerId)
            eq ("storeId", springSecurityService.principal.storeId)
            between ("dateCreated", startDate, endDate)
        }
    }

    def getTillControlEvents(Date startDate, Date endDate, TillControlEventType type, int maxResults, int startIndex, String sortColumn, String sortOrder) {
        def tillControlEventsCriteria = TillControlEvent.createCriteria()

        def results = tillControlEventsCriteria.list([sort: sortColumn, order: sortOrder, offset: startIndex, max: maxResults]) {
            eq ("type", type)
            eq ("retailerId", springSecurityService.principal.retailerId)
            eq ("storeId", springSecurityService.principal.storeId)
            between ("dateCreated", startDate, endDate)
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