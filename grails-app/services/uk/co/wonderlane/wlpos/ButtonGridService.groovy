package uk.co.wonderlane.wlpos

import grails.gorm.transactions.Transactional
import uk.co.wonderlane.wlpos.dataaccess.DatabaseCredentials
import uk.co.wonderlane.wlpos.dataaccess.MySqlDal
import uk.co.wonderlane.wlpos.enums.ButtonGridType

import java.sql.CallableStatement
import java.sql.Connection
import java.sql.Types


@Transactional
class ButtonGridService extends MySqlDal {

    def springSecurityService

    ButtonGridService(DatabaseCredentials databaseCredentials) {
        super(databaseCredentials)
    }

    def newButtonGrid(ButtonGridType buttonGridType, int rows, int columns, String description) {
        Connection conn = getConnection()
        CallableStatement stmt = conn.prepareCall("{ call saveButtonGrid(?, ?, ?, ?, ?, ?) }")

        try {
            stmt.setInt(1, springSecurityService.principal.retailerId)
            springSecurityService.principal.storeId ? stmt.setInt(2, springSecurityService.principal.storeId) : stmt.setNull(2, Types.INTEGER)
            stmt.setString(3, buttonGridType.name())
            stmt.setInt(4, rows)
            stmt.setInt(5, columns)
            stmt.setString(6, description)

            stmt.execute()
        } finally {
            stmt.close()
            conn.close()
        }
    }
}
