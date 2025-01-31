package uk.co.wonderlane.wlpos

import org.joda.time.DateTime

class ProductGroup {

    int id
    int retailerId
    String name
    boolean hidden
    DateTime startDate
    DateTime endDate
    String timeRestriction
    Integer maxSellQuantity
    boolean active

    static hasMany = [productGroupProducts: ProductGroupProduct]

    static mapping = {
        table "productgroup"
        version false

        retailerId column: "retailerId", sqlType: "tinyint unsigned"
        name column: "name"
        hidden column: "hidden"
        startDate column: "startDate"
        endDate column: "endDate"
        timeRestriction column: "timeRestriction", type: "uk.co.wonderlane.wlpos.usertypes.JsonType", sqlType: "json"
        maxSellQuantity column: "maxSellQuantity"
        active column: "active"
        productGroupProducts cascade: "all,delete-orphan"
    }

    static constraints = {
        name nullable: false
        hidden nullable: false
        startDate nullable: false
        endDate nullable: true
        timeRestriction nullable: true
        maxSellQuantity nullable: true, min: 1
        active nullable: false
    }

    public uk.co.wonderlane.wlpos.entities.ProductGroup getProductGroup() {
        uk.co.wonderlane.wlpos.entities.ProductGroup productGroup = new uk.co.wonderlane.wlpos.entities.ProductGroup()
        productGroup.setId(id)
        productGroup.setRetailerId(retailerId)
        productGroup.setName(name)
        productGroup.setHidden(hidden)
        productGroup.setStartDate(startDate?.toDate())
        productGroup.setEndDate(endDate?.toDate())
        productGroup.setTimeRestriction(timeRestriction)
        productGroup.setMaxSellQuantity(maxSellQuantity)
        productGroup.setActive(active)

        productGroupProducts?.each {
            productGroup.getProductGroupProducts().add(it.getProductGroupProduct())
        }

        return productGroup
    }
}