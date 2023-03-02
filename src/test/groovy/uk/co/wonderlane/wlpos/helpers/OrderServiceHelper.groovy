package uk.co.wonderlane.wlpos.helpers

import uk.co.wonderlane.wlpos.OrderService
import uk.co.wonderlane.wlpos.dataaccess.DatabaseCredentials

import java.sql.Connection
import java.sql.SQLException

class OrderServiceHelper extends OrderService {

    private Connection connection

    OrderServiceHelper(DatabaseCredentials databaseCredentials, Connection connection) {
        super(databaseCredentials)
        this.connection = connection
    }

    protected void testConnection() throws SQLException {
        // do nothing
    }

    protected Connection getConnection() throws SQLException {
        return connection
    }

}
