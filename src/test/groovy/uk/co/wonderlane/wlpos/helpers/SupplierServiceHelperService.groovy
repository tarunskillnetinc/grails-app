package uk.co.wonderlane.wlpos.helpers


import uk.co.wonderlane.wlpos.SupplierService
import uk.co.wonderlane.wlpos.dataaccess.DatabaseCredentials

import java.sql.Connection
import java.sql.SQLException

class SupplierServiceHelperService extends SupplierService {

    public Connection fakeConnection

    @Override
    protected Connection getConnection() throws SQLException {
        return fakeConnection
    }

    SupplierServiceHelperService() {
        super(new DatabaseCredentials("http://fake.fakedomain.fke/db", "testUser", "testPwd"))
    }

    SupplierServiceHelperService(DatabaseCredentials databaseCredentials) {
        super(databaseCredentials)
    }

    SupplierServiceHelperService(DatabaseCredentials databaseCredentials, Connection fakeConnection) {
        super(databaseCredentials)
        this.fakeConnection = fakeConnection
    }

}
