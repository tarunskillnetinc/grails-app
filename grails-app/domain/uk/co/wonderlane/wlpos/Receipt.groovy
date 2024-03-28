package uk.co.wonderlane.wlpos

import org.joda.time.DateTime
import uk.co.wonderlane.wlpos.enums.TransactionPaymentMethodType

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
    TransactionPaymentMethodType paymentMethod
    BigDecimal transactionAmount

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
        paymentMethod column: "paymentMethod"
        transactionAmount column: "transactionAmount"
    }

    static constraints = {

    }
}