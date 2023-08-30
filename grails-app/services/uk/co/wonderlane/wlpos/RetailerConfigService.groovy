package uk.co.wonderlane.wlpos

import grails.gorm.transactions.Transactional
import uk.co.wonderlane.wlpos.dataaccess.DatabaseCredentials
import uk.co.wonderlane.wlpos.dataaccess.MySqlDal
import uk.co.wonderlane.wlpos.entities.RetailerConfig
import uk.co.wonderlane.wlpos.entities.RetailerIMConfig

import java.sql.CallableStatement
import java.sql.Connection
import java.sql.SQLException


@Transactional
class RetailerConfigService extends MySqlDal {

    def springSecurityService

    protected RetailerConfigService(DatabaseCredentials databaseCredentials) throws SQLException {
        super(databaseCredentials)
    }

    def saveRetailerConfig(RetailerConfig configString, RetailerIMConfig imConfigString) {
        Connection conn = getConnection()
        CallableStatement cstmt = conn.prepareCall("{ call saveRetailerConfig(?, ?, ?, ?) }")

        try {
            cstmt.setInt(1, springSecurityService.principal.retailerId)
            cstmt.setString(2, springSecurityService.principal.retailer.name)
            if (configString) {
                cstmt.setString(3, configString as String)
            } else {
                cstmt.setString(3, springSecurityService.principal.retailer.config)
            }
            if (imConfigString) {
                cstmt.setString(4, imConfigString as String)
            } else {
                cstmt.setString(4, springSecurityService.principal.retailer.imConfig)
            }

            cstmt.executeUpdate()
        } finally {
            cstmt.close()
            conn.close()
        }
    }
}