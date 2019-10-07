package uk.co.wonderlane.wlpos

import grails.databinding.BindingFormat

class Barcode {

    static belongsTo = [ productVariant: ProductVariant ]

    int id
    String barcode
    @BindingFormat('yyyy-MM-dd')
    Date effectiveDate
    char recordStatus
    boolean delete

    static transients = ['delete']

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
        delete bindable: true
    }
}