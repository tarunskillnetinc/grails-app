package uk.co.wonderlane.wlpos

import grails.gorm.transactions.Transactional
import uk.co.wonderlane.wlpos.dataaccess.DatabaseCredentials
import uk.co.wonderlane.wlpos.dataaccess.MySqlDal
import uk.co.wonderlane.wlpos.entities.cashmanagement.CashManagementConfig

import java.sql.CallableStatement
import java.sql.Connection
import java.sql.SQLException
import java.sql.Types

@Transactional
class CashManagementService extends MySqlDal{

    def springSecurityService
    def gsonProvider

    protected CashManagementService(DatabaseCredentials databaseCredentials) throws SQLException {
        super(databaseCredentials)
    }

    def getCashManagement(int retailerId, Integer storeId) {
        if (storeId != null) {
            return CashManagement.findByRetailerIdAndStoreId(retailerId, storeId)
        } else {
            return CashManagement.findByRetailerId(retailerId)
        }
    }

    def saveCashManagement(CashManagementConfig config, Integer storeId) {
        Connection conn = getConnection()
        CallableStatement cstmt = conn.prepareCall("{ call saveCashManagement(?, ?, ?) }")

        try {
            cstmt.setInt(1, springSecurityService.principal.retailerId)
            //TODO: this logic here will modified in the store level(STMP-68) but this is working for retailer level
            if (storeId) {
                cstmt.setInt(2, storeId)
            } else {
                cstmt.setNull(2, Types.INTEGER)
            }
            cstmt.setString(3, gsonProvider.gson.toJson(config))
            cstmt.executeUpdate()
        } finally {
            cstmt.close()
            conn.close()
        }
    }
}
