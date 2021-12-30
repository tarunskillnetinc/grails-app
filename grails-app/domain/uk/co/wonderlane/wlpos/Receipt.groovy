package uk.co.wonderlane.wlpos

import org.joda.time.DateTime

class Receipt {

    int id
    int retailerId
    int storeId
    int tillId
    int transactionId
    String usersName
    String barcode
    boolean printed
    DateTime dateGenerated

    static hasMany = [ receiptLines: ReceiptLine ]

    static mapping = {
        datasources (["transactions"])

        table "receipt"
        version false

        retailerId column: "retailerId", sqlType: "tinyint"
        storeId column: "storeId", sqlType: "smallint"
        tillId column: "tillId"
        transactionId column: "transactionId"
        usersName column: "usersName"
        barcode column: "barcode"
        printed column: "printed"
        dateGenerated column: "dateGenerated"
    }

    static constraints = {

    }
}