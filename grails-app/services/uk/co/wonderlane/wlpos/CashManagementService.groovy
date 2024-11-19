package uk.co.wonderlane.wlpos

import grails.gorm.transactions.Transactional
import uk.co.wonderlane.wlpos.dataaccess.DatabaseCredentials
import uk.co.wonderlane.wlpos.dataaccess.MySqlDal
import uk.co.wonderlane.wlpos.entities.cashmanagement.CashManagementConfig

import java.sql.CallableStatement
import java.sql.Connection
import java.sql.ResultSet
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

    def deleteStoreLevelConfig(int storeId) {
        Connection conn = getConnection()
        CallableStatement cstmt = conn.prepareCall("{ call deleteStoreLevelCashManagement(?, ?) }")

        try {
            cstmt.setInt(1, springSecurityService.principal.retailerId)
            cstmt.setInt(2, storeId)
            cstmt.executeUpdate()
        } finally {
            cstmt.close()
            conn.close()
        }
    }

    CashManagementConfig getCashManagementConfig(int retailerId, int storeId) {
        try (Connection conn = getConnection();
            CallableStatement cstmt = conn.prepareCall("{ call getCashManagementConfiguration(?, ?) }")) {
            cstmt.setInt(1, retailerId);
            cstmt.setInt(2, storeId);

            try (ResultSet rs = cstmt.executeQuery()) {
                if (rs.next()) {
                    String configJson = rs.getString("config");
                    return gsonProvider.gson.fromJson(configJson, CashManagementConfig.class);
                }
            }
        } catch (SQLException ex) {
            log.error("Error checking if till shifts auto open for retailerId: " + retailerId + " and storeId: " + storeId, ex);
            throw new RuntimeException("Sql error checking till shifts auto open status", ex);
        } catch (Exception ex) {
            log.error("Error parsing JSON config for retailerId: " + retailerId + " and storeId: " + storeId, ex);
            throw new RuntimeException("Unexpected error parsing cash management configuration", ex);
        }
        return null;
    }
}
