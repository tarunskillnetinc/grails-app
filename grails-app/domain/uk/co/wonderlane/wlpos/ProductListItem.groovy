package uk.co.wonderlane.wlpos

class ProductListItem {

    int id
    ProductVariant productVariant
    int productBalanceOnHand
    int quantity
    int fillQuantity = 0
    int parentQuantity

    static belongsTo = [ productList: ProductList ]

    static mapping = {
        table "productlistitem"
        version false

        productVariant column: "productVariantId", cascade: "save-update"
        productBalanceOnHand column: "productBalanceOnHand"
        quantity column: "quantity"
        fillQuantity column: "fillQuantity"
        parentQuantity column: "parentQuantity"

        productList column: "productListId"
    }

    static constraints = {
        productVariant nullable: false
        productBalanceOnHand nullable: true
        quantity nullable: true
        fillQuantity nullable: false
        parentQuantity nullable: true
    }
}