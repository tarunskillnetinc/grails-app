package uk.co.wonderlane.wlpos


import groovy.json.JsonSlurper
import org.joda.time.DateTime

// This is a class used for reading the productgroupview. This is a view onto productgroup and provides a couple of derived fields that are useful in the Product Group Management list page.
class ProductGroupView {
    // Fields from ProductGroup
    int id
    int retailerId
    String description
    DateTime startDate
    DateTime endDate
    String timeRestriction
    Integer maxSellQuantity
    boolean active

    // Fields derived from the data.
    String restrictionTypes
    Integer restrictionTypesNumber
    Long productCount

    static mapping = {
        table "productgroupview"
        version false

        retailerId column: "retailerId", sqlType: "tinyint unsigned"
        description column: "description"
        startDate column: "startDate"
        endDate column: "endDate"
        timeRestriction column: "timeRestriction", sqlType: "json"
        maxSellQuantity column: "maxSellQuantity"
        active column: "active"
        hidden column: "hidden"
        restrictionTypes column: "restrictionTypes"
        restrictionTypesNumber column: "restrictionTypesNumber"
        productCount column: "productCount"
    }

    static constraints = {
        description nullable: false
        startDate nullable: false
        endDate nullable: true
        timeRestriction nullable: true
        maxSellQuantity nullable: true, min: 1
        active nullable: false
    }
}
