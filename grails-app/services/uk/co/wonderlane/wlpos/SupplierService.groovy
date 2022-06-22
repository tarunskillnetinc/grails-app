package uk.co.wonderlane.wlpos

import grails.gorm.transactions.Transactional
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import org.joda.time.format.DateTimeFormat
import org.joda.time.format.DateTimeFormatter
import uk.co.wonderlane.wlpos.dataaccess.DatabaseCredentials
import uk.co.wonderlane.wlpos.dataaccess.MySqlDal
import uk.co.wonderlane.wlpos.supplier.Pack
import uk.co.wonderlane.wlpos.supplier.Supplier
import uk.co.wonderlane.wlpos.supplier.SymbolGroup
import uk.co.wonderlane.wlpos.supplier.SymbolGroupSubscription

import java.sql.CallableStatement
import java.sql.Connection
import java.sql.ResultSet
import java.sql.Types

@Transactional
class SupplierService extends MySqlDal {

    def springSecurityService
    def sessionFactory

    SupplierService(DatabaseCredentials databaseCredentials) {
        super(databaseCredentials)
    }

    def getSuppliers() {
        return Supplier.findAllByRetailerIdAndStoreId(springSecurityService.principal.retailerId, springSecurityService.principal.storeId, [sort: "name", order: "asc"])
    }

    def getSupplier(int id) {
        return Supplier.findByIdAndRetailerIdAndStoreId(id, springSecurityService.principal.retailerId, springSecurityService.principal.storeId)
    }

    def saveSupplier(Supplier supplier) {
        supplier.save()
    }

    def deleteSupplier(Supplier supplier) {
        supplier.delete()
    }

    def getSymbolGroupSubscriptions() {
        return SymbolGroupSubscription.findAllByRetailerIdAndStoreId(springSecurityService.principal.retailerId, springSecurityService.principal.storeId)
    }

    def getSymbolGroupSubscription(int id) {
        return SymbolGroupSubscription.findByIdAndRetailerIdAndStoreId(id, springSecurityService.principal.retailerId, springSecurityService.principal.storeId)
    }

    def getSymbolGroups() {
        return SymbolGroup.listOrderByName()
    }

    def saveSymbolGroupSubscription(SymbolGroupSubscription symbolGroupSubscription) {
        symbolGroupSubscription.save()
    }

    def getPacksUpdatedSince(DateTime sinceDate) {
        def criteria = Pack.createCriteria()

        return criteria.list {
            supplier {
                isNotNull("symbolGroup")
            }
            productVariant {
                or {
                    isNull("storeId")
                    eq ("storeId", springSecurityService.principal.storeId)
                }
            }
            ge ("updateDatetime", sinceDate)
            order ("updateDatetime", "asc")
        }
    }

    def getSupplierPriceUpdates(DateTime sinceDate, int priceBandId, Integer supplierId, Integer categoryId, int offset, int max) {
        def results = [results: [], totalCount:0]

        Connection conn = getConnection()
        CallableStatement cstmt = conn.prepareCall("{ call getSupplierPriceUpdates(?, ?, ?, ?, ?, ?, ?, ?) }")

        try {
            cstmt.setInt(1, springSecurityService.principal.retailerId)

            if (springSecurityService.principal.storeId != null) {
                cstmt.setInt(2, springSecurityService.principal.storeId)
            } else {
                cstmt.setNull(2, Types.INTEGER)
            }

            cstmt.setString(3, sinceDate.toString(DATE_TIME_FORMAT))

            cstmt.setInt(4, priceBandId)

            if (supplierId != null) {
                cstmt.setInt(5, supplierId)
            } else {
                cstmt.setNull(5, Types.INTEGER)
            }

            if (categoryId != null) {
                cstmt.setInt(6, categoryId)
            } else {
                cstmt.setNull(6, Types.INTEGER)
            }

            cstmt.setInt(7, offset)
            cstmt.setInt(8, max)

            ResultSet rs = cstmt.executeQuery()

            try {
                DateTimeFormatter dateFormatter = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss").withZone(DateTimeZone.UTC)

                while (rs.next()) {
                    def result = [:]
                    result.packId = rs.getInt("packId")
                    result.sku = rs.getLong("sku")
                    result.description = rs.getString("description")
                    result.quantity = rs.getInt("quantity")
                    result.effectiveDate = DateTime.parse(rs.getString("effectiveDate"), dateFormatter)
                    result.priceMarked = rs.getBoolean("priceMarked")
                    result.oldPackPrice = rs.getBigDecimal("oldPackPrice")
                    result.newPackPrice = rs. getBigDecimal("newPackPrice")
                    result.retailPrice = rs. getBigDecimal("retailPrice")
                    result.recommendedRetailPrice = rs.getBigDecimal("recommendedRetailPrice")

                    results.results.add(result)
                }
            } finally {
                rs.close()
            }

            cstmt.getMoreResults()
            rs = cstmt.getResultSet()

            if (rs.next()) {
                results.totalCount = rs.getInt("totalCount")
            }
        } finally {
            cstmt.close()
            conn.close()
        }

        return results
    }

    def saveSupplierPriceUpdates(List supplierPriceUpdates, PriceBand priceBand, DateTime effectiveDate) {
        DateTime now = DateTime.now(DateTimeZone.UTC)

        Connection conn = getConnection()
        CallableStatement cstmt = conn.prepareCall("{ call saveSupplierPriceUpdate(?, ?, ?, ?, ?, ?, ?) }")

        try {
            supplierPriceUpdates.eachWithIndex { priceUpdate, index ->
                cstmt.clearParameters()

                cstmt.setLong(1, priceUpdate.sku)
                cstmt.setString(2, effectiveDate.toString(DATE_TIME_FORMAT))
                cstmt.setInt(3, priceBand.id)

                if (priceUpdate instanceof PriceChangeCommand) {
                    cstmt.setBigDecimal(4, priceUpdate.price)
                } else if (priceUpdate.recommendedRetailPrice) {
                    cstmt.setBigDecimal(4, priceUpdate.recommendedRetailPrice)
                } else {
                    cstmt.setNull(4, Types.DECIMAL)
                }

                cstmt.setInt(5, priceUpdate.packId)

                if (springSecurityService.principal.storeId) {
                    cstmt.setInt(6, springSecurityService.principal.storeId)
                } else {
                    cstmt.setNull(6, Types.INTEGER)
                }

                cstmt.setString(7, now.toString(DATE_TIME_FORMAT))

                cstmt.addBatch()

                // Clear the session for speed purposes.
                if (index.mod(200) == 0) {
                    cstmt.executeBatch()
                }
            }

            cstmt.executeBatch()
        } finally {
            cstmt.close()
            conn.close()
        }
    }
}