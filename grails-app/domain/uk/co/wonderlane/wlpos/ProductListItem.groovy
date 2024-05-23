package uk.co.wonderlane.wlpos

import java.math.RoundingMode

class ProductListItem {

    int id
    ProductVariant productVariant
    BigDecimal productQuantityInStock
    BigDecimal quantity
    BigDecimal fillQuantity = BigDecimal.ZERO
    BigDecimal parentQuantity

    static belongsTo = [ productList: ProductList, productListItemGroup: ProductListItemGroup ]

    static hasMany = [ packLines: PackLine ]

    static transients = [ 'totalValue', 'totalCost' ]

    static mapping = {
        table "productlistitem"
        version false

        productVariant column: "productVariantId", cascade: "save-update"
        productQuantityInStock column: "productQuantityInStock"
        quantity column: "quantity"
        fillQuantity column: "fillQuantity"
        parentQuantity column: "parentQuantity"

        productList column: "productListId"
        productListItemGroup column: "productListItemGroupId"
    }

    static constraints = {
        productVariant nullable: false
        productQuantityInStock nullable: true
        quantity nullable: true
        fillQuantity nullable: false
        parentQuantity nullable: true
        productListItemGroup nullable: true
    }

    def getTotalValue() {
        if (fillQuantity == 0) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP)
        }

        if (productVariant == null) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP)
        }

        return packLines?.sum {
            it?.getTotalValue()
        }
    }

    def getTotalCost() {
        BigDecimal totalPackQuantity = packLines?.sum { it.totalQuantity } ?: BigDecimal.ZERO
        BigDecimal totalSinglesQuantity = BigDecimal.valueOf(quantity ?: fillQuantity) - totalPackQuantity

        // Get total pack cost.
        BigDecimal totalPackCost = packLines?.sum {it.totalCostPrice } ?: BigDecimal.ZERO

        // Get total singles cost.
        BigDecimal totalSinglesCost = BigDecimal.ZERO
        if (totalSinglesQuantity > BigDecimal.ZERO) {
            totalSinglesCost = totalSinglesQuantity * (productVariant?.costPrice ?: BigDecimal.ZERO)
        }

        // Estimated delivery cost = total pack cost + singles cost.
        return totalPackCost + totalSinglesCost
    }
}