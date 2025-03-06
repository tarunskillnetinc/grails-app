package uk.co.wonderlane.wlpos

import grails.gorm.transactions.Transactional
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import org.joda.time.format.DateTimeFormat
import org.joda.time.format.DateTimeFormatter
import uk.co.wonderlane.wlpos.dataaccess.DatabaseCredentials
import uk.co.wonderlane.wlpos.dataaccess.MySqlDal
import uk.co.wonderlane.wlpos.enums.ProductHistoryType
import uk.co.wonderlane.wlpos.supplier.*

import java.sql.CallableStatement
import java.sql.Connection
import java.sql.Date
import java.sql.ResultSet
import java.sql.Types

@Transactional
class SupplierService extends MySqlDal {

    def springSecurityService
    def sessionFactory
    def snsService

    SupplierService(DatabaseCredentials databaseCredentials) {
        super(databaseCredentials)
    }

    def getSuppliers() {
        def suppliers = Supplier.findAllByRetailerIdAndDeleted(springSecurityService.principal.retailerId, false, [sort: "name", order: "asc"])
        if (springSecurityService.principal.storeId) {
            suppliers.removeAll { it.storeId != null && it.storeId != springSecurityService.principal.storeId}
        }
        
        return suppliers
    }

    def getSortedRetailerSuppliers(Map sorting) {
        return Supplier.findAllByRetailerIdAndDeleted(springSecurityService.principal.retailerId, false, sorting)
    }

    def getSupplier(int id) {
        return Supplier.findByIdAndRetailerId(id, springSecurityService.principal.retailerId)
    }

    def saveSupplier(Supplier supplier) {
        def id = supplier.id
        supplier.save()
        if (id == 0) {
            snsService.publishSupplierAdd(supplier.getSupplier())
        }
    }

    def deleteSupplier(Supplier supplier) {
        supplier.delete()
        snsService.publishSupplierDelete(supplier.getSupplier())
    }

    def saveSupplierCaseRate(SupplierCaseRate supplierCaseRate) {
        // This isnt a standard save as we need to delete any future suppliercaserates.
        try (Connection conn = getConnection(); CallableStatement cstmt = conn.prepareCall("{ call upsertCaseRate(?, ?, ?, ?) }")) {
            try {
                cstmt.setInt(1, springSecurityService.principal.retailerId)
                cstmt.setInt(2, supplierCaseRate.supplier.id)
                cstmt.setBigDecimal(3, supplierCaseRate.caseRate)
                cstmt.setDate(4, new java.sql.Date(supplierCaseRate.caseRateEffectiveDate.getTime()))

                cstmt.executeUpdate()
            }
            catch (Exception ex) {
                ex.printStackTrace()
            }
            finally {
                cstmt.close()
                conn.close()
            }
        }
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

    def getSupplierPriceUpdates(DateTime sinceDate, Integer priceBandId, Integer supplierId, Integer categoryId, int offset, int max) {
        def results = [results: [], totalCount: 0]

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

            if (priceBandId != null) {
                cstmt.setInt(4, priceBandId)
            } else {
                cstmt.setNull(4, Types.INTEGER)
            }


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
                    result.newPackPrice = rs.getBigDecimal("newPackPrice")
                    result.retailPrice = rs.getBigDecimal("retailPrice")
                    result.recommendedRetailPrice = rs.getBigDecimal("recommendedRetailPrice")
                    result.productId = rs.getBigDecimal("productId")

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
        CallableStatement supplierPriceUpdateStmt = conn.prepareCall("{ call saveSupplierPriceUpdate(?, ?, ?, ?, ?, ?, ?) }")
        CallableStatement saveProductHistoryStmt = conn.prepareCall("{ call saveProductHistoryItem(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) }")

        def savedSkus = []

        try {
            supplierPriceUpdates.eachWithIndex { priceUpdate, index ->
                // There could be multiple packs for the same SKU in the list which causes a duplicate primary key due to lack of transaction here, so bypass the rest of the save and mark this pack as dealt with.
                if (savedSkus.contains(priceUpdate.sku)) {
                    SupplierPriceUpdate supplierPriceUpdate = new SupplierPriceUpdate(packId: priceUpdate.packId, priceBandId: priceBand.id, storeId: springSecurityService.principal.storeId, updateDatetime: now)
                    supplierPriceUpdate.save()

                    return
                }

                supplierPriceUpdateStmt.clearParameters()
                saveProductHistoryStmt.clearParameters()

                saveProductHistoryStmt.setInt(1, priceUpdate.productId.intValue())
                saveProductHistoryStmt.setDate(4, new java.sql.Date(effectiveDate.toDateTime().getMillis()))
                saveProductHistoryStmt.setString(5, ProductHistoryType.PRICE.toString())
                saveProductHistoryStmt.setNull(6, Types.VARCHAR)
                saveProductHistoryStmt.setInt(9, springSecurityService.principal.id)
                saveProductHistoryStmt.setString(10, springSecurityService.principal.usersName)

                if (priceUpdate.oldPrice) {
                    saveProductHistoryStmt.setString(7, priceUpdate.oldPrice.toString())
                } else {
                    saveProductHistoryStmt.setNull(7, Types.VARCHAR)
                }

                supplierPriceUpdateStmt.setLong(1, priceUpdate.sku)
                supplierPriceUpdateStmt.setString(2, effectiveDate.toString(DATE_TIME_FORMAT))
                supplierPriceUpdateStmt.setInt(3, priceBand.id)
                saveProductHistoryStmt.setInt(11, priceBand.id)

                if (priceUpdate instanceof PriceChangeCommand) {
                    supplierPriceUpdateStmt.setBigDecimal(4, priceUpdate.price)
                    saveProductHistoryStmt.setString(8, priceUpdate.price.toString())
                } else if (priceUpdate.recommendedRetailPrice) {
                    supplierPriceUpdateStmt.setBigDecimal(4, priceUpdate.recommendedRetailPrice)
                    saveProductHistoryStmt.setString(8, priceUpdate.recommendedRetailPrice.toString())
                } else {
                    supplierPriceUpdateStmt.setNull(4, Types.DECIMAL)
                    saveProductHistoryStmt.setNull(8, Types.VARCHAR)
                }

                supplierPriceUpdateStmt.setInt(5, priceUpdate.packId)

                if (springSecurityService.principal.storeId) {
                    supplierPriceUpdateStmt.setInt(6, springSecurityService.principal.storeId)
                    saveProductHistoryStmt.setInt(2, springSecurityService.principal.storeId)
                } else {
                    supplierPriceUpdateStmt.setNull(6, Types.INTEGER)
                    saveProductHistoryStmt.setNull(2, Types.INTEGER)
                }

                supplierPriceUpdateStmt.setString(7, now.toString(DATE_TIME_FORMAT))
                saveProductHistoryStmt.setString(3, now.toString(DATE_TIME_FORMAT))
                saveProductHistoryStmt.setInt(12, springSecurityService.principal.retailerId)

                supplierPriceUpdateStmt.addBatch()
                saveProductHistoryStmt.addBatch()

                savedSkus.add(priceUpdate.sku)

                // Clear the session for speed purposes.
                if (index.mod(200) == 0) {
                    supplierPriceUpdateStmt.executeBatch()
                    saveProductHistoryStmt.executeBatch()
                }
            }

            supplierPriceUpdateStmt.executeBatch()
            saveProductHistoryStmt.executeBatch()
        } finally {
            supplierPriceUpdateStmt.close()
            saveProductHistoryStmt.close()
            conn.close()
        }
    }

    //This method will load suppliers based on provided arguments
    def getSuppliers(String supplierNameTerm, String supplierReferenceTerm, String customerReferenceTerm,String includeDeletedSuppliers, int offset, int max, String sortColumn, String sortOrder) {
        Integer storeId

        if (springSecurityService.principal.storeId) {
            storeId = springSecurityService.principal.storeId
        } //load store id if it exists
        //Load supplier by db
        def result = Supplier.createCriteria().list([offset: offset, max: max, sort: sortColumn, order: sortOrder]) {
            eq("retailerId", springSecurityService.principal.retailerId)
            if (storeId != null) {
                //If store id exists then load all suppliers corresponding to store id and all suppliers who do not have supplier id
                or {
                    isNull("storeId")
                    eq("storeId", storeId)
                }
            } else {
                //If store id does not exists then load only suppliers who do not have supplier id
                isNull("storeId")
            }
            or {
                and {
                    if (supplierNameTerm && supplierNameTerm.trim()) {
                        like("name", "%$supplierNameTerm%")
                    }
                    if (supplierReferenceTerm && supplierReferenceTerm.trim()) {
                        like("reference", "%$supplierReferenceTerm%")
                    }
                    if (customerReferenceTerm && customerReferenceTerm.trim()) {
                        like("customerReference", "%$customerReferenceTerm%")
                    }
                }
            }
            if (includeDeletedSuppliers != "true") {
                eq("deleted", false)
            }
        }
        def results = [:]
        results.suppliers = result //Add to supplier
        results.totalCount = result?.totalCount >= 0 ? result.totalCount : 0 //Add to total count
        return results
    }
}
