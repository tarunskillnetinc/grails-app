package uk.co.wonderlane.wlpos

import grails.converters.JSON
import groovy.json.JsonSlurper
import org.joda.time.DateTime

import javax.persistence.Column

class ProductGroup {

    int id
    int retailerId
    String description
    DateTime startDate
    DateTime endDate
    String timeRestriction
    Integer maxSellQuantity
    boolean active

    //Category category

    static hasMany = [productGroupProducts: ProductGroupProduct]

    static mapping = {
        table "productgroup"
        version false

        retailerId column: "retailerId", sqlType: "tinyint unsigned"
        description column: "description"
        startDate column: "startDate"
        endDate column: "endDate"
        //category column: "categoryId", type:"join", cascade: "none"
        timeRestriction column: "timeRestriction", sqlType: "json"
        maxSellQuantity column: "maxSellQuantity"
        active column: "active"
        productGroupProducts cascade: "all,delete-orphan"
    }

    static constraints = {
        description nullable: false
        startDate nullable: false
        endDate nullable: true
        timeRestriction nullable: true
        maxSellQuantity nullable: true, min: 1
        active nullable: false
        //category nullable: true
    }

    def beforeUpdate() {
        if (timeRestriction && !(timeRestriction instanceof String)) {
            timeRestriction = new JSON(timeRestriction).toString()
        }
    }

    def beforeInsert() {
        if (timeRestriction && !(timeRestriction instanceof String)) {
            timeRestriction = new JSON(timeRestriction).toString()
        }
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