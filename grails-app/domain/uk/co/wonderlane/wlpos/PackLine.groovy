package uk.co.wonderlane.wlpos

import uk.co.wonderlane.wlpos.supplier.Pack

class PackLine {

    int id
    String type
    int productListId
    Pack pack
    BigDecimal quantity
    String orderCode

    static belongsTo = [productListItem: ProductListItem]

    static transients = [ 'totalQuantity', 'totalCostPrice', 'totalValue' ]

    static mapping = {
        table "packlines"
        version false
        id column: "id"
        type column: "type"
        productListId column: "productListId"
        productListItem column: "productListItemId"
        pack column: "packId"
        quantity column: "quantity"
        orderCode column: "orderCode"
    }

    static constraints = {
        pack nullable: true
    }

    int getTotalQuantity() {
        return (quantity ?: BigDecimal.ZERO).multiply((pack?.quantity ?: BigDecimal.ZERO))?.intValue()
    }

    BigDecimal getTotalCostPrice() {
        return (quantity ?: BigDecimal.ZERO).multiply(pack?.price ?: BigDecimal.ZERO)
    }

    BigDecimal getTotalValue() {
        def retailPrice = productListItem?.productVariant?.currentPrice ?: BigDecimal.ZERO

        return retailPrice.multiply(getTotalQuantity())
    }
}