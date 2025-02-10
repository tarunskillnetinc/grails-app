package uk.co.wonderlane.wlpos


import groovy.json.JsonSlurper
import org.joda.time.DateTime

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
        restrictionTypes column: "restrictionTypes"
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

    public uk.co.wonderlane.wlpos.entities.ProductGroup getProductGroup() {
        uk.co.wonderlane.wlpos.entities.ProductGroup productGroup = new uk.co.wonderlane.wlpos.entities.ProductGroup()
        productGroup.setId(id)
        productGroup.setRetailerId(retailerId)
        productGroup.setDescription(description)
        productGroup.setStartDate(startDate)
        productGroup.setEndDate(endDate)
        if (timeRestriction) {
            def jsonSlurper = new JsonSlurper()
            def timeRestrictionMap = jsonSlurper.parseText(timeRestriction)

            if (timeRestrictionMap.timeRestrictionDays instanceof List) {
                boolean[] days = new boolean[7]
                timeRestrictionMap.timeRestrictionDays.eachWithIndex { day, index ->
                    days[index] = day
                }
                productGroup.setTimeRestrictionDays(days)
            }

            if (timeRestrictionMap.startSellingTimeRestriction) {
                productGroup.setStartSellingTimeRestriction(timeRestrictionMap.startSellingTimeRestriction)
            }

            if (timeRestrictionMap.stopSellingTimeRestriction) {
                productGroup.setStopSellingTimeRestriction(timeRestrictionMap.stopSellingTimeRestriction)
            }
        }
        productGroup.setMaxSellQuantity(maxSellQuantity)
        productGroup.setActive(active)

        productGroupProducts?.each {
            productGroup.getProductGroupProducts().add(it.getProductGroupProduct())
        }

        return productGroup
    }
}
