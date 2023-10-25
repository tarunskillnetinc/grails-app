package uk.co.wonderlane.wlpos.reporting

import org.joda.time.DateTime
import uk.co.wonderlane.wlpos.Store

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
        datasources (["reporting", "reportingReadOnly"])
        table "paypointsale"
        version false

        id column : "id"
        retailerId column: "retailerId", sqlType: "tinyint"
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
        return Store.findById(storeId).storeId
    }
}
