package uk.co.wonderlane.wlpos

import grails.converters.JSON
import groovy.json.JsonSlurper
import org.joda.time.DateTime

class ProductGroup {

    int id
    int retailerId
    String description
    DateTime startDate
    DateTime endDate
    String timeRestriction
    Integer maxSellQuantity

    boolean hidden
    boolean active

    static hasMany = [productGroupProducts: ProductGroupProduct]

    static mapping = {
        table "productgroup"
        version false

        retailerId column: "retailerId", sqlType: "tinyint unsigned"
        description column: "description"
        startDate column: "startDate"
        endDate column: "endDate"
        timeRestriction column: "timeRestriction", sqlType: "json"
        maxSellQuantity column: "maxSellQuantity"
        hidden column: "hidden"
        active column: "active"
        productGroupProducts cascade: "all,delete-orphan"
    }

    static constraints = {
        description nullable: false, size: 1..60, validator: { val, obj ->
            if (!val || val.trim().length() < 1 || val.trim().length() > 60) {
                return ['producthistory.description.size']
            }
        }
        startDate nullable: false, validator: { val, obj ->
            if (val == null) {
                return ['producthistory.startdate.empty']
            }
        }
        endDate nullable: true
        timeRestriction nullable: true
        maxSellQuantity nullable: true, validator: { val, obj ->
            if (val ?: 0 < 0 || val ?: 0 > 999999) {
                return ['producthistory.maxSellQuantity.invalid']
            }
        }
        active nullable: false, validator: { val, obj ->
            if (val == null) {
                return ['producthistory.active.null']
            }
        }
// temporarily disabled - will be used.
//        productGroupProducts nullable: false, validator: { val, obj ->
//            if( val?.size()==0 ) {
//                return ['producthistory.productgroupproducts.nullorempty']
//            }
//        }
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
        try {
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
        } catch (Exception ex) {
            ex.printStackTrace()
        }
    }
}