package uk.co.wonderlane.wlpos.reporting

import org.joda.time.DateTime
import uk.co.wonderlane.wlpos.StoreSettings

class PayPointSale {

    int id
    int retailerId
    int storeId
    int wlTransactionId
    int ppTransactionId
    String terminalId
    String description
    String type
    BigDecimal value
    String status
    DateTime transactionDate

    Integer visibleStoreId

    static transients = ['visibleStoreId']

    static mapping = {
        datasources (["reporting"])

        table "paypointsale"
        version false

        id column : "id"
        retailerId column: "retailerId"
        storeId column: "storeId"
        wlTransactionId column: "wlTransactionId"
        ppTransactionId column: "ppTransactionId"
        terminalId column: "terminalId"
        description column: "description"
        type column: "type"
        value column: "value"
        status column: "status", sqlType: "enum", enumType: 'string'
        transactionDate column: "transactionDate"
    }

    static constraints = {
    }

    Integer getVisibleStoreId() {
        return StoreSettings.findById(storeId).storeId
    }
}
