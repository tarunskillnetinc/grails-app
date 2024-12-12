package uk.co.wonderlane.wlpos

import grails.gorm.transactions.ReadOnly
import grails.gorm.transactions.Transactional
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import org.joda.time.format.DateTimeFormat
import org.joda.time.format.DateTimeFormatter
import uk.co.wonderlane.wlpos.enums.PromotionType
import uk.co.wonderlane.wlpos.enums.TenderMovementType
import uk.co.wonderlane.wlpos.enums.TenderType
import uk.co.wonderlane.wlpos.enums.TillControlEventType
import uk.co.wonderlane.wlpos.reporting.*

@Transactional("reporting")
class ReportingService {

    def springSecurityService

    // For sales report grouped by department, no pagination on here as the results are grouped into categories.
    @ReadOnly('reportingReadOnly')
    def getSales(DateTime startDate, DateTime endDate, Integer storeId) {
        def salesCriteria = Sale.withTransaction { Sale.createCriteria() }

        return salesCriteria.list() {
            eq("retailerId", springSecurityService.principal.retailerId)
            if (storeId != null) {
                eq("storeId", storeId)
            }
            between("dateCreated", startDate, endDate)
        }
    }

    // For sales report grouped by category, no pagination on here as the results can still be grouped into categories.
    @ReadOnly('reportingReadOnly')
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

        return Sale.withTransaction { Sale.executeQuery(searchQuery, queryParams) }
    }

    @ReadOnly('reportingReadOnly')
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

        return Sale.withTransaction { Sale.executeQuery(searchQuery, queryParams) }
    }

    // For sales report product level. Paginated and filtered.
    @ReadOnly('reportingReadOnly')
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

        def queryParams = [productId: productId, retailerId: springSecurityService.principal.retailerId, descriptionFilter: "%" + descriptionFilter + "%", startDate: startDate, endDate: endDate, max: maxResults, offset: startIndex]

        if (storeId != null) {
            queryParams.storeId = storeId
        }

        return Sale.withTransaction { Sale.executeQuery(searchQuery, queryParams) }
    }

    @ReadOnly('reportingReadOnly')
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

        def queryParams = [productId: productId, retailerId: springSecurityService.principal.retailerId, descriptionFilter: "%" + descriptionFilter + "%", startDate: startDate, endDate: endDate]

        if (storeId != null) {
            queryParams.storeId = storeId
        }

        return Sale.withTransaction { Sale.executeQuery(searchQuery, queryParams)[0] }
    }

    // For promotions grouped report. No pagination here as we're going to group them, but the filtering can be done in the database.
    @ReadOnly('reportingReadOnly')
    def getPromotionSales(DateTime startDate, DateTime endDate, String descriptionFilter, PromotionType promotionTypeFilter, Integer storeId) {
        def promotionsCriteria = PromotionSale.withTransaction { PromotionSale.createCriteria() }

        return promotionsCriteria.list() {
            eq("retailerId", springSecurityService.principal.retailerId)
            if (storeId != null) {
                eq("storeId", storeId)
            }
            if (descriptionFilter) {
                like("description", "%" + descriptionFilter + "%")
            }
            if (promotionTypeFilter) {
                eq("type", promotionTypeFilter)
            }
            between("dateCreated", startDate, endDate)
        }
    }

    // For promotions report at promotion level. Filtered and paginated.
    @ReadOnly('reportingReadOnly')
    def getPromotionSales(DateTime startDate, DateTime endDate, int promotionId, int maxResults, int startIndex, String sortColumn, String sortOrder, Integer storeId) {
        def promotionsCriteria = PromotionSale.withTransaction { PromotionSale.createCriteria() }

        def results = promotionsCriteria.list([sort: sortColumn, order: sortOrder, offset: startIndex, max: maxResults]) {
            eq("retailerId", springSecurityService.principal.retailerId)
            if (storeId != null) {
                eq("storeId", storeId)
            }
            between("dateCreated", startDate, endDate)
            eq("promotionId", promotionId)
        }

        // Criteria.list() with max and offset returns a totalCount, but for some reason I am having to read that value otherwise an error is thrown when trying to use it back in the controller.
        // I believe this may be related to the domain class being in an alternate datasource, but I think it's a bug in Grails. Actually, I think it's because the totalCount is lazily loaded
        // to prevent the double query immediately. But it's throwing a Hibernate session error if I don't request it here.
        int totalCount = PromotionSale.withTransaction { results.totalCount }
        return results
    }

    // For promotion report product level. Filtered and paginated.
    @ReadOnly('reportingReadOnly')
    def getPromotionSaleProducts(int promotionSaleId, String productFilter, int maxResults, int startIndex, String sortColumn, String sortOrder, Integer storeId) {
        def promotionProductsCriteria = PromotionSale.withTransaction { PromotionSaleProduct.createCriteria() }

        def results = promotionProductsCriteria.list([sort: sortColumn, order: sortOrder, offset: startIndex, max: maxResults]) {
            promotion {
                eq("retailerId", springSecurityService.principal.retailerId)
                if (storeId != null) {
                    eq("storeId", storeId)
                }
                eq("id", promotionSaleId)
            }
            if (productFilter) {
                or {
                    like("itemCode", "%" + productFilter + "%")
                    like("description", "%" + productFilter + "%")
                }
            }
        }

        // Criteria.list() with max and offset returns a totalCount, but for some reason I am having to read that value otherwise an error is thrown when trying to use it back in the controller.
        // I believe this may be related to the domain class being in an alternate datasource, but I think it's a bug in Grails. Actually, I think it's because the totalCount is lazily loaded
        // to prevent the double query immediately. But it's throwing a Hibernate session error if I don't request it here.
        int totalCount = PromotionSale.withTransaction { results.totalCount }
        return results
    }

    @ReadOnly('reportingReadOnly')
    def getPromotionSale(int promotionSaleId) {
        def promotionSaleCriteria = PromotionSale.withTransaction { PromotionSale.createCriteria() }

        return promotionSaleCriteria.get() {
            eq("id", promotionSaleId)
            eq("retailerId", springSecurityService.principal.retailerId)
            if (springSecurityService.principal.storeId != null) {
                eq("storeId", springSecurityService.principal.storeId)
            }
        }
    }

    @ReadOnly('reportingReadOnly')
    def getTillControlEvents(DateTime startDate, DateTime endDate) {
        def tillControlEventsCriteria = TillControlEvent.withTransaction { TillControlEvent.createCriteria() }

        return tillControlEventsCriteria.list() {
            eq("retailerId", springSecurityService.principal.retailerId)
            if (springSecurityService.principal.storeId != null) {
                eq("storeId", springSecurityService.principal.storeId)
            }
            between("dateCreated", startDate, endDate)
        }
    }

    @ReadOnly('reportingReadOnly')
    def getTillControlEvents(DateTime startDate, DateTime endDate, TillControlEventType type, int maxResults, int startIndex, String sortColumn, String sortOrder) {
        def tillControlEventsCriteria = TillControlEvent.withTransaction { TillControlEvent.createCriteria() }

        def results = tillControlEventsCriteria.list([sort: sortColumn, order: sortOrder, offset: startIndex, max: maxResults]) {
            eq("type", type)
            eq("retailerId", springSecurityService.principal.retailerId)

            if (springSecurityService.principal.storeId != null) {
                // TODO OR storeId is passed in as a filter (to be added).
                eq("storeId", springSecurityService.principal.storeId)
            }

            between("dateCreated", startDate, endDate)
        }

        // Criteria.list() with max and offset returns a totalCount, but for some reason I am having to read that value otherwise an error is thrown when trying to use it back in the controller.
        // I believe this may be related to the domain class being in an alternate datasource, but I think it's a bug in Grails. Actually, I think it's because the totalCount is lazily loaded
        // to prevent the double query immediately. But it's throwing a Hibernate session error if I don't request it here.
        int totalCount = TillControlEvent.withTransaction { results.totalCount }
        return results
    }

    @ReadOnly('reportingReadOnly')
    def getPayPointSales(DateTime startDate, DateTime endDate, Integer storeId, String status, String description, int maxResults, int startIndex, String sortColumn, String sortOrder) {
        def payPointCriteria = PayPointSale.withTransaction { PayPointSale.createCriteria() }

        def results = payPointCriteria.list([sort: sortColumn, order: sortOrder, offset: startIndex, max: maxResults]) {
            eq("retailerId", springSecurityService.principal.retailerId)
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
        int totalCount = PayPointSale.withTransaction { results.totalCount }
        return results
    }

    @ReadOnly('reportingReadOnly')
    def getCharityDonations(DateTime startDate, DateTime endDate, Integer storeId, int maxResults, int startIndex, String sortColumn, String sortOrder) {
        def charityDonationsCriteria = CharitySale.withTransaction { CharitySale.createCriteria() }

        def results = charityDonationsCriteria.list() {
            eq("retailerId", springSecurityService.principal.retailerId)

            if (storeId != null) {
                eq("storeId", storeId)
            }

            between("dateCreated", startDate, endDate)
        }

        return results
    }

    @ReadOnly('reportingReadOnly')
    def getTenderMovements(DateTime startDate, DateTime endDate, TenderMovementType tenderMovementType, TenderType tenderType, Integer storeId, int maxResults, int startIndex, String sortColumn, String sortOrder) {
        def tenderMovementCriteria = TenderMovement.withTransaction { TenderMovement.createCriteria() }

        def results = tenderMovementCriteria.list([sort: sortColumn, order: sortOrder, offset: startIndex, max: maxResults]) {
            eq ("retailerId", springSecurityService.principal.retailerId)

            if (springSecurityService.principal.storeId != null) {
                eq ("storeId", springSecurityService.principal.storeId)
            } else if (storeId) {
                eq ("storeId", storeId)
            }

            if (tenderType) {
                eq("tenderType", tenderType)
            }

            if (tenderMovementType) {
                eq("type", tenderMovementType)
            }

            between ("timestamp", startDate, endDate)
        }

        // Criteria.list() with max and offset returns a totalCount, but for some reason I am having to read that value otherwise an error is thrown when trying to use it back in the controller.
        // I believe this may be related to the domain class being in an alternate datasource, but I think it's a bug in Grails. Actually, I think it's because the totalCount is lazily loaded
        // to prevent the double query immediately. But it's throwing a Hibernate session error if I don't request it here.
        int totalCount = TenderMovement.withTransaction { results.totalCount }
        return [totalCount: totalCount, tenderMovements: results]
    }

    def createNewTenderMovement(TenderMovementType movementType, TenderType tenderType, uk.co.wonderlane.wlpos.reporting.Location fromLocation, uk.co.wonderlane.wlpos.reporting.Location toLocation, BigDecimal amount) {
        TenderMovement tenderMovement = new TenderMovement()
        tenderMovement.retailerId = springSecurityService.principal.retailerId
        tenderMovement.storeId = springSecurityService.principal.storeId
        tenderMovement.userId = springSecurityService.principal.id
        tenderMovement.userName = springSecurityService.principal.usersName
        tenderMovement.type = movementType
        tenderMovement.tenderType = tenderType
        tenderMovement.fromLocation = fromLocation
        tenderMovement.toLocation = toLocation
        tenderMovement.amount = amount
        tenderMovement.timestamp = DateTime.now(DateTimeZone.UTC)
        return tenderMovement
    }

    def createNewTenderMovement(TenderMovementType movementType, TenderType tenderType, uk.co.wonderlane.wlpos.reporting.Location fromLocation, String reasonCode, BigDecimal amount) {
        TenderMovement tenderMovement = new TenderMovement()
        tenderMovement.retailerId = springSecurityService.principal.retailerId
        tenderMovement.storeId = springSecurityService.principal.storeId
        tenderMovement.userId = springSecurityService.principal.id
        tenderMovement.userName = springSecurityService.principal.usersName
        tenderMovement.type = movementType
        tenderMovement.tenderType = tenderType
        tenderMovement.fromLocation = fromLocation
        tenderMovement.reason = reasonCode
        tenderMovement.amount = amount
        tenderMovement.timestamp = DateTime.now(DateTimeZone.UTC)
        return tenderMovement
    }

    TenderMovement createNewTenderMovement(TenderMovementType movementType, TenderType tenderType, uk.co.wonderlane.wlpos.reporting.Location location, String bankingDate,
                                           String bank, String bankReferenceNumber, String comments, BigDecimal updatedAmount) {
        TenderMovement tenderMovement = new TenderMovement()
        tenderMovement.retailerId = springSecurityService.principal.retailerId
        tenderMovement.storeId = springSecurityService.principal.storeId
        tenderMovement.userId = springSecurityService.principal.id
        tenderMovement.userName = springSecurityService.principal.usersName
        tenderMovement.type = movementType
        tenderMovement.tenderType = tenderType
        tenderMovement.fromLocation = location
        tenderMovement.amount = updatedAmount
        tenderMovement.timestamp = DateTime.now(DateTimeZone.UTC)
        tenderMovement.bankName = bank
        tenderMovement.bankReference = bankReferenceNumber
        tenderMovement.comment = comments
        DateTimeFormatter formatter = DateTimeFormat.forPattern("dd/MM/yyyy")
        try {
            if (!bankingDate.isEmpty() && bankingDate != null){
                tenderMovement.bankingDate = formatter.parseDateTime(bankingDate)
            }
        } catch (IllegalArgumentException e) {
            log.error("Failed to parse banking date: " + bankingDate, e);
            throw new RuntimeException("Invalid banking date format. Expected dd/MM/yyyy", e);
        }
        return tenderMovement
    }

    def saveTenderMovement(TenderMovement tenderMovement) {
        if (tenderMovement.validate()) {
            tenderMovement.save(flush: true)
            return Integer.valueOf(tenderMovement.id)
        } else {
            tenderMovement.errors.each {
                System.out.println(it.toString())
            }
            return -1
        }
    }

    @ReadOnly('reportingReadOnly')
    def getReportColumns(ReportType reportType) {
        return ReportColumns.withTransaction { ReportColumns.findByUserIdAndReportType(springSecurityService.principal.id, reportType) }
    }

    def saveReportColumns(ReportColumns reportColumns) {
        reportColumns.save()
    }

}
