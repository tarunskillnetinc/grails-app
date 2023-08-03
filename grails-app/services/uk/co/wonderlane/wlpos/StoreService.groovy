package uk.co.wonderlane.wlpos

import grails.gorm.transactions.Transactional
import uk.co.wonderlane.wlpos.dataaccess.DatabaseCredentials
import uk.co.wonderlane.wlpos.dataaccess.MySqlDal

import java.sql.CallableStatement
import java.sql.Connection
import java.sql.Types

@Transactional
class StoreService extends MySqlDal {

    def springSecurityService

    StoreService(DatabaseCredentials databaseCredentials) {
        super(databaseCredentials)
    }

    def getStore(int retailerId, int storeId) {
        return Store.findByRetailerIdAndId(retailerId, storeId)
    }

    def getStoreByStoreNumber(int retailerId, Integer storeNumber) {
        return Store.find("FROM Store s WHERE s.retailerId = :retailerId AND (JSON_EXTRACT(config, '\$.storeNumber') = :storeNumber OR (:storeNumber IS NULL AND JSON_TYPE(JSON_EXTRACT(config, '\$.storeNumber')) = 'NULL')) ORDER BY s.id DESC", [retailerId: retailerId, storeNumber: storeNumber])
    }

    def getStores(int retailerId) {
        return Store.findAll("FROM Store s WHERE s.retailerId = :retailerId AND JSON_EXTRACT(config, '\$.storeType') != 'HEAD_OFFICE' ORDER BY s.id DESC", [retailerId: retailerId])
    }

    def getStoresByType(int retailerId, StoreType storeType) {
        return Store.findAll("FROM Store s WHERE s.retailerId = :retailerId AND JSON_EXTRACT(config, '\$.storeType') = :storeType ORDER BY s.id DESC", [retailerId: retailerId, storeType: storeType.name()])
    }

    def getStoresByRange(int retailerId, Range range) {
        return Store.findAll("FROM Store s WHERE s.retailerId = :retailerId AND s.rangeId = :rangeId AND JSON_EXTRACT(config, '\$.storeType') != 'HEAD_OFFICE' ORDER BY s.id DESC", [retailerId: retailerId, rangeId: range.id])
    }

    def getStoresByPriceBand(int retailerId, PriceBand priceBand) {
        return Store.findAll("FROM Store s WHERE s.retailerId = :retailerId AND s.priceBandId = :priceBandId AND JSON_EXTRACT(config, '\$.storeType') != 'HEAD_OFFICE' ORDER BY s.id DESC", [retailerId: retailerId, priceBandId: priceBand.id])
    }

    def saveStoreSettings(StoreCommand store, String configString) {
        Connection conn = getConnection()
        CallableStatement cstmt = conn.prepareCall("{ call saveStore(?, ?, ?, ?, ?, ?, ?) }")

        try {
            cstmt.setInt(1, store.id)
            cstmt.setInt(2, springSecurityService.principal.retailerId)
            if (store.parentStoreId) {
                cstmt.setInt(3, store.parentStoreId)
            } else {
                cstmt.setNull(3, Types.INTEGER)
            }
            cstmt.setInt(4, springSecurityService.principal.id)
            cstmt.setInt(5, store.priceBand.id)
            cstmt.setInt(6, store.range.id)
            cstmt.setString(7, configString)

            cstmt.executeUpdate()
        } finally {
            cstmt.close()
            conn.close()
        }
    }
}