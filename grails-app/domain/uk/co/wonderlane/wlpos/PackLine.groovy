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
        pack nullable: true
    }

    def getTotalQuantity() {
        int quantity = 1 //For singles
        if (pack){ //If pack exist means get quantity from pack
            quantity = pack.getQuantity()
        }
        return quantity.multiply(productListItem.getFillQuantity())
    }

    def getTotalValue() {
        int quantity = 1 //For singles
        if (pack){ //If pack exist means get quantity from pack
            quantity = pack.getQuantity()
        }
        def price = quantity.multiply(productListItem?.productVariant?.getCurrentPrice())
        return price.multiply(productListItem?.getFillQuantity())
    }
}
