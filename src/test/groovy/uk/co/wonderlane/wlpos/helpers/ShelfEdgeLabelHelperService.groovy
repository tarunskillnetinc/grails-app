package uk.co.wonderlane.wlpos.helpers

import uk.co.wonderlane.wlpos.ShelfEdgeLabelService
import uk.co.wonderlane.wlpos.dataaccess.DatabaseCredentials

import java.sql.Connection
import java.sql.SQLException

class ShelfEdgeLabelHelperService extends ShelfEdgeLabelService {

    public Connection fakeConnection

    ShelfEdgeLabelHelperService(DatabaseCredentials databaseCredentials) {
        super(databaseCredentials)
    }

    ShelfEdgeLabelHelperService(DatabaseCredentials databaseCredentials, Connection fakeConnection) {
        super(databaseCredentials)
        this.fakeConnection = fakeConnection
    }

    @Override
    protected Connection getConnection() throws SQLException {
        return fakeConnection
    }
}
