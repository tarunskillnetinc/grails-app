package uk.co.wonderlane.wlpos

import org.joda.time.DateTime
import uk.co.wonderlane.wlpos.enums.wlim.ProductListStatus
import uk.co.wonderlane.wlpos.enums.wlim.ProductListType

class BranchOrder implements Serializable {

    String supplierReference
    ProductListType type
    DateTime endDate
    ProductListStatus status

    // This constructor is required or dependency injection (springSecurityService) breaks. Don't forget "autowire true" in the mappings as well.
    public BranchOrder() { }

    static mapping = {
        autowire true
        table "productlist"
        version false

        supplierReference column: "supplierReference"
        type column: "`type`"
        endDate column: "endDate"
        status column: "`status`"
    }

    static constraints = {
        type nullable: false
        supplierReference nullable: true
        endDate nullable: true
        status nullable: false
    }
}
