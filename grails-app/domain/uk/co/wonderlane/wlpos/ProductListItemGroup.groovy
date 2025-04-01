package uk.co.wonderlane.wlpos

import org.joda.time.DateTime

class ProductListItemGroup {

    int id
    String uniqueIdentifier
    DateTime effectiveDate
    Collection<ProductListItem> productListItems = new ArrayList<>()

    static belongsTo = [ productList: ProductList ]

    static hasMany = [ productListItems: ProductListItem ]

    static mapping = {
        table "productlistitemgroup"
        version false

        uniqueIdentifier column: "uniqueIdentifier"
        effectiveDate column: "effectiveDate"

        productList column: "productListId"
    }

    static constraints = {
        uniqueIdentifier nullable: true
        effectiveDate nullable: true
    }
}