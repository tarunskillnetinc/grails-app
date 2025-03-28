package uk.co.wonderlane.wlpos.reporting

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import uk.co.wonderlane.wlpos.User

class BasketTransaction {

    Integer id
    String transactionObject
    Date createdDateTime
    Date transactionDateTime
    Integer retailerId
    Integer storeId
    Integer tillId
    Integer transactionId
    String transactionSource
    User user

    public BasketTransaction() {}

    static constraints = {
        id(readOnly: true)
        transactionObject(readOnly: true)
        createdDateTime(readOnly: true)
        transactionDateTime(readOnly: true)
        retailerId(readOnly: true)
        storeId(readOnly: true)
        tillId(readOnly: true)
        transactionId(readOnly: true)
        transactionObject(readOnly: true)
        transactionSource(readOnly: true)
        user(readOnly: true)
    }

    static mapping = {
        datasources(["reporting", "reportingReadOnly"])
        autowire true
        table "baskettransaction"
        version false

        id column: "id", sqlType: "smallint"
        createdDateTime column: "createdDateTime"
        transactionDateTime column: "transactionDateTime"
        retailerId column: "retailerId", sqlType: "tinyint"
        storeId column: "storeId", sqlType: "smallint"
        tillId column: "tillId", sqlType: "int"
        transactionId column: "transactionId", sqlType: "int"
        transactionObject column: "`transactionObject`", type: "uk.co.wonderlane.wlpos.usertypes.JsonType", sqlType: "json"
        transactionSource column: "transactionSource"
    }

    static transients = ["user"]


}
