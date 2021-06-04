package uk.co.wonderlane.wlpos

import org.joda.time.DateTime

class Barcode {

    static belongsTo = [ productVariant: ProductVariant ]

    int id
    String barcode
    DateTime effectiveDate
    char recordStatus

    static mapping = {
        table "barcode"
        version false

        productVariant column: "productVariantId"
        barcode column: "barcode"
        effectiveDate column: "effectiveDate"
        recordStatus column: "recordStatus"
    }

    static constraints = {
        barcode size: 1..20, blank: false, nullable: false
        effectiveDate nullable: false
        recordStatus nullable: false
    }
}