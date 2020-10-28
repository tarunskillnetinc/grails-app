package uk.co.wonderlane.wlpos

import grails.gorm.transactions.Transactional
import uk.co.wonderlane.wlpos.enums.TillControlEventType
import uk.co.wonderlane.wlpos.reporting.PromotionSale
import uk.co.wonderlane.wlpos.reporting.PromotionSaleProduct
import uk.co.wonderlane.wlpos.reporting.ReportType
import uk.co.wonderlane.wlpos.reporting.Sale
import uk.co.wonderlane.wlpos.reporting.TillControlEvent
import uk.co.wonderlane.wlpos.reporting.ReportColumns

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

        return tillControlEventsCriteria.list([sort: sortColumn, order: sortOrder, offset: startIndex, max: maxResults]) {
            eq ("type", type)
            eq ("retailerId", springSecurityService.principal.retailerId)
            eq ("storeId", springSecurityService.principal.storeId)
            between ("dateCreated", startDate, endDate)
        }
    }

    def getPromotionSales(Date startDate, Date endDate) {
        def promotionsCriteria = PromotionSale.createCriteria()

        return promotionsCriteria.list() {
            eq ("retailerId", springSecurityService.principal.retailerId)
            eq ("storeId", springSecurityService.principal.storeId)
            between ("dateCreated", startDate, endDate)
        }
    }

    def getPromotionSales(Date startDate, Date endDate, int promotionId) {
        def promotionsCriteria = PromotionSale.createCriteria()

        return promotionsCriteria.list() {
            eq ("retailerId", springSecurityService.principal.retailerId)
            eq ("storeId", springSecurityService.principal.storeId)
            between ("dateCreated", startDate, endDate)
            eq("promotionId", promotionId)
        }
    }

    def getPromotionSaleProducts(int promotionSaleId) {
        def promotionProductsCriteria = PromotionSaleProduct.createCriteria()

        return promotionProductsCriteria.list() {
            promotion {
                eq("retailerId", springSecurityService.principal.retailerId)
                eq("storeId", springSecurityService.principal.storeId)
                eq("id", promotionSaleId)
            }
        }
    }

    def getReportColumns(ReportType reportType) {
        return ReportColumns.findByUserIdAndReportType(springSecurityService.principal.id, reportType)
    }

    def saveReportColumns(ReportColumns reportColumns) {
        reportColumns.save()
    }
}