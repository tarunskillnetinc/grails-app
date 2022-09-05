package uk.co.wonderlane.wlpos

import java.math.RoundingMode

class ProductListItem {

    int id
    ProductVariant productVariant
    int productQuantityShopFloor
    int productQuantityStockroom
    Integer quantity
    int fillQuantity = 0
    Integer parentQuantity

    static belongsTo = [ productList: ProductList ]

    static hasMany = [ packLines: PackLine ]

    static transients = [ 'totalValue', 'totalCost' ]

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

    def getTotalValue() {
        if (fillQuantity == 0) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP)
        }

        if (productVariant == null) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP)
        }

        return packLines?.sum {
            it.getTotalValue()
        }
    }

    def getTotalCost() {
        return packLines?.sum {
            fillQuantity.multiply(it.pack.price)
        }
    }
}