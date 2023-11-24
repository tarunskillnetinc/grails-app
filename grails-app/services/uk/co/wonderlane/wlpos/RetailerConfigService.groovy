package uk.co.wonderlane.wlpos

import grails.gorm.transactions.Transactional
import uk.co.wonderlane.wlpos.dataaccess.DatabaseCredentials
import uk.co.wonderlane.wlpos.dataaccess.MySqlDal
import uk.co.wonderlane.wlpos.entities.RetailerConfig

import java.sql.CallableStatement
import java.sql.Connection

@Transactional
class RetailerConfigService extends MySqlDal {

    def springSecurityService
    def gsonProvider

    protected RetailerConfigService(DatabaseCredentials databaseCredentials) {
        super(databaseCredentials)
    }

    def saveRetailerConfig(RetailerConfig config) {
        Connection conn = getConnection()
        CallableStatement cstmt = conn.prepareCall("{ call saveRetailerConfig(?, ?, ?) }")

        try {
            cstmt.setInt(1, springSecurityService.principal.retailerId)
            cstmt.setString(2, springSecurityService.principal.retailer.name)
            if (config) {
                cstmt.setString(3, gsonProvider.gson.toJson(config))
            } else {
                cstmt.setString(3, gsonProvider.gson.toJson(config))
            }
            cstmt.executeUpdate()
        } finally {
            cstmt.close()
            conn.close()
        }
    }
}