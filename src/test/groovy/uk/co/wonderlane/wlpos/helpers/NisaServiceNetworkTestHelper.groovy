package uk.co.wonderlane.wlpos.helpers


import uk.co.wonderlane.wlpos.NisaService
import uk.co.wonderlane.wlpos.dataaccess.DatabaseCredentials

import java.sql.Connection
import java.sql.SQLException

class NisaServiceNetworkTestHelper extends NisaService {

    private Connection connection;

    NisaServiceNetworkTestHelper(DatabaseCredentials databaseCredentials, Connection connection) {
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
