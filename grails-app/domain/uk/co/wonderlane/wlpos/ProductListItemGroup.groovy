package uk.co.wonderlane.wlpos

class ProductListItemGroup {

    int id
    String uniqueIdentifier

    static belongsTo = [ productList: ProductList ]

    static hasMany = [ productListItems: ProductListItem ]

    static mapping = {
        table "productlistitemgroup"
        version false

        uniqueIdentifier column: "uniqueIdentifier"

        productList column: "productListId"
    }

    static constraints = {
        uniqueIdentifier nullable: true
    }
}