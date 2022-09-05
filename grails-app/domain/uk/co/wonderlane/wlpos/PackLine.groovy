package uk.co.wonderlane.wlpos

import uk.co.wonderlane.wlpos.supplier.Pack

class PackLine {

    int id
    String type
    int productListId
    Pack pack
    int quantity
    String orderCode

    static belongsTo = [productListItem: ProductListItem]

    static transients = [ 'totalQuantity', 'totalValue' ]

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
    }

    def getTotalQuantity() {
        return pack.getQuantity().multiply(productListItem.getFillQuantity())
    }

    def getTotalValue() {
        def price = pack.quantity.multiply(productListItem.productVariant.getCurrentPrice())
        return price.multiply(productListItem.getFillQuantity())
    }
}
