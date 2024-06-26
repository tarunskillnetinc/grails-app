package uk.co.wonderlane.wlpos

import grails.gorm.transactions.Transactional
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import org.joda.time.format.DateTimeFormat
import org.joda.time.format.DateTimeFormatter
import uk.co.wonderlane.wlpos.enums.ProductHistoryType
import uk.co.wonderlane.wlpos.dataaccess.DatabaseCredentials
import uk.co.wonderlane.wlpos.dataaccess.MySqlDal
import uk.co.wonderlane.wlpos.enums.wlim.ProductListStatus
import uk.co.wonderlane.wlpos.enums.wlim.ProductListType

import java.sql.CallableStatement
import java.sql.Connection
import java.sql.ResultSet
import java.sql.Types

@Transactional
class ProductListService extends MySqlDal {

    def springSecurityService

    ProductListService(DatabaseCredentials databaseCredentials) {
        super(databaseCredentials)
    }

    def getCentralCounts(String searchTerm = null, String searchBy = null, int offset = 0, int max = 50, String sort = "startDate", String order = "DESC") {
        return ProductList.createCriteria().list([offset: offset, max: max, sort: sort, order: order]) {
            eq("retailerId", springSecurityService.principal.retailerId)
            eq("type", ProductListType.SCHEDULED_COUNT)

            if (searchBy == "Everything" && searchTerm) {
                or {
                    like("description", "%$searchTerm%")
                    def matchingEnums =[]
                    ProductListStatus.values().each {status ->
                        if (status.name().toString().toLowerCase().contains(searchTerm.toLowerCase())){
                            matchingEnums.add(status)
                        }
                    }
                    if (matchingEnums.size() > 0) {
                        matchingEnums.each { matchingEnum ->
                            eq("status", ProductListStatus.valueOf(matchingEnum.toString()))
                        }
                    }

                    if (searchTerm == "N/A") {
                        isNull("ownerUsersName")
                    } else {
                        like("ownerUsersName", "%$searchTerm%")
                    }
                }
            } else if (searchBy == "Description" && searchTerm) {
                like("description", "%$searchTerm%")
            } else if (searchBy == "Status" && searchTerm) {
                or {
                    def matchingEnums = []
                    ProductListStatus.values().each { status ->
                        if (status.getFriendlyName().toString().toLowerCase().contains(searchTerm.toLowerCase())) {
                            matchingEnums.add(status)
                        }
                    }

                    if ((matchingEnums.size() <= 0)) {
                        eq("status", null)
                        return
                    }

                    matchingEnums.each { matchingEnum ->
                        eq("status", ProductListStatus.valueOf(matchingEnum.toString()))
                    }
                }
            } else if (searchBy == "Current Owner" && searchTerm) {
                if (searchTerm == "N/A") {
                    isNull("ownerUsersName")
                } else {
                    like("ownerUsersName", "%$searchTerm%")
                }
            }
        }
    }

    def getOrders(Integer storeId, Integer supplierId, DateTime startDate, DateTime endDate) {
        return getOrder(null, storeId, supplierId, startDate, endDate)
    }

    def getOrder(Integer productListId, Integer storeId, Integer supplierId, DateTime startDate, DateTime endDate, int offset = 0, int max = 50, String sort = "dateStarted", String order = "DESC") {
        return ProductList.createCriteria().list([offset: offset, max: max, sort: sort, order: order]) {
            eq("retailerId", springSecurityService.principal.retailerId)
            eq("type", ProductListType.ORDER)

            if (productListId != null) {
                eq("id", productListId)
            }

            if (storeId != null) {
                store {
                    eq("id", storeId)
                }

            }

            if (supplierId != null) {
                eq("supplierId", String.valueOf(supplierId))
            }

            between("dateStarted", startDate, endDate)
        }
    }

    def getDeliveries(Integer storeId, Integer supplierId, DateTime startDate, DateTime endDate, int offset = 0, int max = 50, String sort = "dateStarted", String order = "DESC") {
        return ProductList.createCriteria().list([offset: offset, max: max, sort: sort, order: order]) {
            eq("retailerId", springSecurityService.principal.retailerId)
            eq("type", ProductListType.DELIVERY)

            if (storeId != null) {
                store {
                    eq("id", storeId)
                }
            }

            if (supplierId != null) {
                eq("supplierId", String.valueOf(supplierId))
            }

            or {
                // TODO CORE-1449 will address whether we use this or dateCreated or expectedDate.
                between("dateStarted", startDate, endDate)
                isNull ("dateStarted")
            }
        }
    }

    def getProductLists(Integer storeId, ProductListType type, DateTime startDate, DateTime endDate, int offset = 0, int max = 50, String sort = "dateStarted", String order = "DESC") {
        return ProductList.createCriteria().list([offset: offset, max: max, sort: sort, order: order]) {
            eq("retailerId", springSecurityService.principal.retailerId)

            if (storeId != null) {
                store {
                    eq("id", storeId)
                }
            }

            if (type != null) {
                eq("type", type)
            }

            or {
                between("dateStarted", startDate, endDate)
                isNull ("dateStarted")
            }
        }
    }

    def acceptDelivery(Integer productListId) {
        ProductList productList = getProductList(productListId)

        if (productList) {
            productList.productListItems?.each { it.quantity = it.quantity ?: it.fillQuantity }

            productList.status = productList.stockAdjustedOnCompletion ? ProductListStatus.COMPLETE : ProductListStatus.PARTIALLY_COMPLETE
            productList.dateStarted = productList.dateStarted ?: DateTime.now(DateTimeZone.UTC)
            productList.dateCompleted = DateTime.now(DateTimeZone.UTC)

            Connection conn = getConnection()
            CallableStatement cstmt = conn.prepareCall("{ call saveProductStock(?, ?, ?, ?, ?) }")

            productList.productListItems?.each {
                def productStock = it.productVariant?.getProductStock(productList.store?.id)

                int quantityInStock = productStock?.quantityInStock ?: 0
                int quantityOnOrder = productStock?.quantityOnOrder ?: 0
                int quantityDelivered = productStock?.quantityDelivered ?: 0

                cstmt.setInt(1, productList.store?.id)
                cstmt.setLong(2, it.productVariant?.sku)
                cstmt.setInt(3, productList.stockAdjustedOnCompletion ? quantityInStock + it.quantity : quantityInStock)
                cstmt.setInt(4, Math.max(quantityOnOrder - it.quantity, 0))
                cstmt.setInt(5, productList.stockAdjustedOnCompletion ? quantityDelivered : quantityDelivered + it.quantity)

                cstmt.addBatch()
            }

            cstmt.executeBatch()

            productList.save(deepValidate: false) // deepValidate = false so it won't go through and validate every ProductVariant in every ProductListLine etc.
        }
    }

    def getAdHocBatches() {
        return ProductList.createCriteria().list([sort: "dateStarted", order: "DESC"]) {
            eq("retailerId", springSecurityService.principal.retailerId)
            store {
                eq("id", springSecurityService.principal.storeId)
            }
            "in"("type", [ProductListType.AD_HOC_SEL_BATCH, ProductListType.PRICE_CHECK])
            eq("status", ProductListStatus.PARTIALLY_COMPLETE)
        }
    }

    def getScheduledBatches(DateTime effectiveDate) {
        def results = [toBePrinted: [], toBeConfirmed: [], totalCount: 0]

        Connection conn = getConnection()
        CallableStatement cstmt = conn.prepareCall("{ call getScheduledChanges(?, ?, ?, ?, ?) }")

        try {
            // Call once for to be printed changes.
            cstmt.setInt(1, springSecurityService.principal.retailerId)

            if (springSecurityService.principal.storeId) {
                cstmt.setInt(2, springSecurityService.principal.storeId)
            } else {
                cstmt.setNull(2, Types.INTEGER)
            }

            cstmt.setNull(3, Types.TINYINT)

            if (effectiveDate) {
                cstmt.setString(4, effectiveDate.withTimeAtStartOfDay().toString(DATE_TIME_FORMAT))
                cstmt.setString(5, effectiveDate.plusDays(1).withTimeAtStartOfDay().toString(DATE_TIME_FORMAT))
            } else {
                cstmt.setNull(4, Types.VARCHAR)
                cstmt.setNull(5, Types.VARCHAR)
            }

            ResultSet rs = cstmt.executeQuery()

            DateTimeFormatter dateFormatter = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss").withZone(DateTimeZone.UTC)

            try {
                while (rs.next()) {
                    results.toBePrinted.add(mapProductHistoryResult(rs, dateFormatter))
                }
            } finally {
                rs.close()
            }

            // Call again for changes to be OKd if this is a two stage retailer.
            if (springSecurityService.principal.retailer.config.twoStageSel) {
                cstmt.clearParameters()
                cstmt.setInt(1, springSecurityService.principal.retailerId)
                cstmt.setInt(2, springSecurityService.principal.storeId)
                cstmt.setInt(3, 1)

                if (effectiveDate) {
                    cstmt.setString(4, effectiveDate.withTimeAtStartOfDay().toString(DATE_TIME_FORMAT))
                    cstmt.setString(5, effectiveDate.plusDays(1).withTimeAtStartOfDay().toString(DATE_TIME_FORMAT))
                } else {
                    cstmt.setNull(4, Types.VARCHAR)
                    cstmt.setNull(5, Types.VARCHAR)
                }

                rs = cstmt.executeQuery()

                try {
                    while (rs.next()) {
                        results.toBeConfirmed.add(mapProductHistoryResult(rs, dateFormatter))
                    }
                } finally {
                    rs.close()
                }
            }
        } finally {
            cstmt.close()
            conn.close()
        }

        return results
    }

    def mapProductHistoryResult(ResultSet rs, DateTimeFormatter dateFormatter) {
        def result = [:]

        result.id = rs.getInt("id")
        result.productId = rs.getInt("productId")
        result.storeId = rs.getInt("storeId")
        result.updateDate = DateTime.parse(rs.getString("updateDate"), dateFormatter)
        result.effectiveDate = DateTime.parse(rs.getString("effectiveDate"), dateFormatter)
        result.type = ProductHistoryType.valueOf(rs.getString("type"))
        result.priceBandId = rs.getInt("priceBandId")
        result.field = rs.getString("field")
        result.fromValue = rs.getString("fromValue")
        result.toValue = rs.getString("toValue")
        result.userId = rs.getInt("userId")
        result.usersName = rs.getString("usersName")
        result.productVariantId = rs.getInt("productVariantId")

        return result
    }

    def getProductList(int id) {
        return ProductList.findByIdAndRetailerId(id, springSecurityService.principal.retailerId)
    }

    def getProductList(int id, int retailerId) {
        return ProductList.findByIdAndRetailerId(id, retailerId)
    }

    def getProductListItem(int productListItemId) {
        return ProductListItem.findById(productListItemId)
    }

    def saveProductList(ProductList productList) {
        productList.save()
    }

    def saveProductLists(List<ProductList> productListArray) {
        if (productListArray.size() == 0) {
            return
        }

        Connection connection

        try {
            connection = getConnection()
            connection.setAutoCommit(false)

            for (ProductList productList : productListArray) {
                productList.save()
            }

            connection.commit()
        } catch (Exception ex) {
            log.error("save productLists failed, Exception " + ex.getMessage())
            if (connection != null) {
                connection.rollback()
            }
            throw ex
        } finally {
            if (connection != null) {
                connection.close()
            }
        }
    }

    def deleteProductList(ProductList productList) {
        if (productList) {
            productList.delete()
        }
    }
}