package uk.co.wonderlane.wlpos

import org.joda.time.DateTime

class Barcode {

    int id
    long sku
    int retailerId
    String barcode
    DateTime effectiveDate
    char recordStatus

    boolean delete

    static transients = ['delete']

    static mapping = {
        table "barcode"
        version false

        sku column: "sku"
        retailerId column: "retailerId", sqlType: "tinyint"
        barcode column: "barcode"
        effectiveDate column: "effectiveDate"
        recordStatus column: "recordStatus"
    }

    static constraints = {
        sku nullable: false
        retailerId nullable: false
        barcode size: 1..20, blank: false, nullable: false
        effectiveDate nullable: false
        recordStatus nullable: false
    }
}