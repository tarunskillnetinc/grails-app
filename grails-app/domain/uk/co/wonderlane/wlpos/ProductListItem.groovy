package uk.co.wonderlane.wlpos

class ProductListItem {

    int id
    ProductVariant productVariant
    int productQuantityShopFloor
    int productQuantityStockroom
    int quantity
    int fillQuantity = 0
    int parentQuantity

    static belongsTo = [ productList: ProductList ]

    static mapping = {
        table "productlistitem"
        version false

        productVariant column: "productVariantId", cascade: "save-update"
        productQuantityShopFloor column: "productQuantityShopFloor"
        productQuantityStockroom column: "productQuantityStockroom"
        quantity column: "quantity"
        fillQuantity column: "fillQuantity"
        parentQuantity column: "parentQuantity"

        productList column: "productListId"
    }

    static constraints = {
        productVariant nullable: false
        productQuantityShopFloor nullable: true
        productQuantityStockroom nullable: true
        quantity nullable: true
        fillQuantity nullable: false
        parentQuantity nullable: true
    }
}