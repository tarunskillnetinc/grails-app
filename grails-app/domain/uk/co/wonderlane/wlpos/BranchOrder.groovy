package uk.co.wonderlane.wlpos


import uk.co.wonderlane.wlpos.enums.wlim.ProductListType

class BranchOrder {

    String supplierReference
    String type

    // This constructor is required or dependency injection (springSecurityService) breaks. Don't forget "autowire true" in the mappings as well.
    public BranchOrder() { }

    static mapping = {
        autowire true
        table "productlist"
        version false

        supplierReference column: "supplierReference"
        type column: "`type`"
    }

    static constraints = {
        type nullable: false
        supplierReference nullable: true
    }
}
