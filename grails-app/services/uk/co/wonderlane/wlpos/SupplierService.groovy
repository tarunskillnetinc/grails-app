package uk.co.wonderlane.wlpos

import grails.gorm.transactions.Transactional
import org.hibernate.Session
import org.hibernate.Transaction
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import uk.co.wonderlane.wlpos.dataaccess.DatabaseCredentials
import uk.co.wonderlane.wlpos.dataaccess.MySqlDal
import uk.co.wonderlane.wlpos.supplier.Pack
import uk.co.wonderlane.wlpos.supplier.Supplier
import uk.co.wonderlane.wlpos.supplier.SymbolGroup
import uk.co.wonderlane.wlpos.supplier.SymbolGroupSubscription

import java.math.RoundingMode
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

    def getSupplierPriceUpdates(DateTime sinceDate, int priceBandId, Integer supplierId, int offset, int max) {
        def results = [results: [], totalCount:0]

        Connection conn = getConnection()
        CallableStatement cstmt = conn.prepareCall("{ call getSupplierPriceUpdates(?, ?, ?, ?, ?, ?, ?) }")

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

            cstmt.setInt(6, offset)
            cstmt.setInt(7, max)

            ResultSet rs = cstmt.executeQuery()

            try {
                while (rs.next()) {
                    def result = [:]
                    result.sku = rs.getLong("sku")
                    result.description = rs.getString("description")
                    result.quantity = rs.getInt("quantity")
                    result.effectiveDate = new DateTime(rs.getTimestamp("effectiveDate"), DateTimeZone.UTC)
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

    def saveRecommendedRetailPrices(List supplierPriceUpdates, int priceBandId, DateTime effectiveDate) {
        PriceBand priceBand = PriceBand.findByIdAndRetailerId(priceBandId, springSecurityService.principal.retailerId)

        Session session = sessionFactory.openSession()
        Transaction transaction = session.beginTransaction()

        supplierPriceUpdates.eachWithIndex { priceUpdate, index ->
            if (priceUpdate.recommendedRetailPrice) {
                ProductPrice productPrice = new ProductPrice()
                productPrice.sku = priceUpdate.sku
                productPrice.price = priceUpdate.recommendedRetailPrice
                productPrice.effectiveDate = effectiveDate
                productPrice.priceBand = priceBand

                session.save(productPrice)
            }

            // Clear the session for speed purposes.
            if (index.mod(100) == 0) {
                session.flush()
                session.clear()
            }
        }

        transaction.commit()
        session.close()
    }
}