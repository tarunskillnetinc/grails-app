package uk.co.wonderlane.wlpos

class Barcode {

    static belongsTo = [ product: Product ]

    int id
    String barcode
    Date effectiveDate
    char recordStatus

    static mapping = {
        table "barcode"
        version false

        product column: "productId"
        barcode column: "barcode"
        effectiveDate column: "effectiveDate"
        recordStatus column: "recordStatus"
    }
}