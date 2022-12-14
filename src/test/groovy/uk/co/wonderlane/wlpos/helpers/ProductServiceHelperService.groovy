package uk.co.wonderlane.wlpos.helpers


import uk.co.wonderlane.wlpos.ProductService
import uk.co.wonderlane.wlpos.dataaccess.DatabaseCredentials

import java.sql.Connection
import java.sql.SQLException

class ProductServiceHelperService extends ProductService {

    public Connection fakeConnection

    @Override
    protected Connection getConnection() throws SQLException {
        return fakeConnection
    }

    ProductServiceHelperService() {
        super(new DatabaseCredentials("", "", ""))
    }

    ProductServiceHelperService(DatabaseCredentials databaseCredentials) {
        super(databaseCredentials)
    }

    ProductServiceHelperService(DatabaseCredentials databaseCredentials, Connection fakeConnection) {
        super(databaseCredentials)
        this.fakeConnection = fakeConnection
    }

}
