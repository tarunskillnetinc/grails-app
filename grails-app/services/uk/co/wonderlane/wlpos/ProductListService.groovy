package uk.co.wonderlane.wlpos

import grails.gorm.transactions.Transactional
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import org.joda.time.format.DateTimeFormat
import org.joda.time.format.DateTimeFormatter
import uk.co.wonderlane.wlpos.dataaccess.DatabaseCredentials
import uk.co.wonderlane.wlpos.dataaccess.MySqlDal
import uk.co.wonderlane.wlpos.enums.ProductHistoryType
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

    def getCentralCounts(String searchTerm = null, int offset = 0, int max = 50, String sort = "startDate", String order = "DESC") {
        return ProductList.createCriteria().list([offset: offset, max: max, sort: sort, order: order]) {
            eq ("retailerId", springSecurityService.principal.retailerId)
            eq ("storeId", springSecurityService.principal.storeId)
            eq ("type", ProductListType.SCHEDULED_COUNT)

            if (searchTerm) {
                like ("description", "%$searchTerm%")
            }
        }
    }

    def getOrders(Integer storeId, Integer supplierId, DateTime startDate, DateTime endDate, int offset = 0, int max = 50, String sort = "dateStarted", String order = "DESC") {
        return ProductList.createCriteria().list([offset: offset, max: max, sort: sort, order: order]) {
            eq("retailerId", springSecurityService.principal.retailerId)
            eq("type", ProductListType.ORDER)

            if (storeId > 0) {
                eq("storeId", storeId)
            }

            if (supplierId > 0) {
                eq("supplierId", String.valueOf(supplierId))
            }

            between("dateStarted", startDate, endDate)
        }
    }

    def getAdHocBatches() {
        return ProductList.createCriteria().list([sort: "dateStarted", order: "DESC"]) {
            eq ("retailerId", springSecurityService.principal.retailerId)
            eq ("storeId", springSecurityService.principal.storeId)
            eq ("type", ProductListType.AD_HOC_SEL_BATCH)
            "in" ("status", [ProductListStatus.IN_PROGRESS, ProductListStatus.PARTIALLY_COMPLETE])
        }
    }

    def getScheduledBatches(DateTime effectiveDate) {
        def results = [toBePrinted: [], toBeConfirmed: [], totalCount: 0]

        Connection conn = getConnection()
        CallableStatement cstmt = conn.prepareCall("{ call getScheduledChanges(?, ?, ?, ?, ?) }")

        try {
            // Call once for to be printed changes.
            cstmt.setInt(1, springSecurityService.principal.retailerId)
            cstmt.setInt(2, springSecurityService.principal.storeId)
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
            if (springSecurityService.principal.retailer.twoStageSel) {
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
        result.field = rs. getString("field")
        result.fromValue = rs. getString("fromValue")
        result.toValue = rs. getString("toValue")
        result.userId = rs.getInt("userId")
        result.usersName = rs.getString("usersName")
        result.productVariantId = rs.getInt("productVariantId")

        return result
    }

    def getProductList(int id) {
        return ProductList.findByIdAndRetailerIdAndStoreId(id, springSecurityService.principal.retailerId, springSecurityService.principal.storeId)
    }

    def saveProductList(ProductList productList) {
        productList.save()
    }
}