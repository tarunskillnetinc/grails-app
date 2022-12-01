package uk.co.wonderlane.wlpos.helpers;

import uk.co.wonderlane.wlpos.ShiftService;
import uk.co.wonderlane.wlpos.dataaccess.DatabaseCredentials;

import java.sql.Connection;
import java.sql.SQLException;

public class ShiftServiceHelper extends ShiftService {
    Connection fakeConnection;

    public ShiftServiceHelper(DatabaseCredentials databaseCredentials, Connection fakeConnection) {
        super(databaseCredentials);
        this.fakeConnection = fakeConnection;
    }

    protected void testConnection() throws SQLException {

    }

    protected Connection getConnection() throws SQLException {
        return fakeConnection;
    }
}
