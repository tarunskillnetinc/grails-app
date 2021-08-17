package uk.co.wonderlane.wlpos

import uk.co.wonderlane.wlpos.enums.ReceiptLineType

class ReceiptLine {

    long id
    ReceiptLineType type
    BigDecimal quantity
    String text
    BigDecimal total

    static belongsTo = [ receipt: Receipt ]

    static mapping = {
        datasources (["transactions"])

        table "receiptline"
        version false

        type column: "`type`"
        quantity column: "quantity"
        text column: "`text`", type: "text"
        total column: "total"
        receipt column: "receiptId"
    }

    static constraints = {

    }
}