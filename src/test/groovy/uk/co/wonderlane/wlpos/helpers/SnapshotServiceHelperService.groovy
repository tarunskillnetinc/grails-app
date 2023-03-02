package uk.co.wonderlane.wlpos.helpers

import uk.co.wonderlane.wlpos.SnapshotService
import uk.co.wonderlane.wlpos.dataaccess.DatabaseCredentials

import java.sql.Connection
import java.sql.SQLException

class SnapshotServiceHelperService extends SnapshotService {

    public DatabaseCredentials fakeDatabaseCredentials
    public Connection fakeConnection

    @Override
    protected Connection getConnection() throws SQLException {
        return fakeConnection
    }

    SnapshotServiceHelperService(DatabaseCredentials databaseCredentials) {
        super(databaseCredentials)
    }

    SnapshotServiceHelperService(DatabaseCredentials databaseCredentials, Connection fakeConnection) {
        super(databaseCredentials)
        this.fakeConnection = fakeConnection
    }

}
