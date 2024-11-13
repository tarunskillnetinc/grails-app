package uk.co.wonderlane.wlpos

import uk.co.wonderlane.wlpos.supplier.Pack

import java.math.RoundingMode

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

    boolean isWeighted() {
        return productListItem?.productVariant?.product?.weightedItem ?: false
    }

    BigDecimal getTotalQuantity() {
        BigDecimal result = quantity ?: BigDecimal.ZERO
        if (pack) {
            result *= (pack?.quantity ?: BigDecimal.ZERO)
        }
        return result.setScale(isWeighted() ? 3 : 0, RoundingMode.HALF_UP)
    }

    BigDecimal getTotalCostPrice() {
        if (pack) {
            return (quantity ?: BigDecimal.ZERO) * (pack?.price ?: BigDecimal.ZERO)
        }
        return (quantity ?: BigDecimal.ZERO) * (productListItem?.productVariant?.currentPrice ?: BigDecimal.ZERO)
    }

    BigDecimal getTotalValue() {
        def retailPrice = productListItem?.productVariant?.currentPrice ?: BigDecimal.ZERO

        return retailPrice.multiply(getTotalQuantity())
    }

    BigDecimal getPackCost() {
        if (pack) {
            return pack?.price ?: BigDecimal.ZERO
        }
        return productListItem?.productVariant?.currentPrice ?: BigDecimal.ZERO
    }

    BigDecimal getPackSize() {
        if (pack) {
            return pack?.quantity?.setScale(isWeighted() ? 3 : 0, RoundingMode.HALF_UP) ?: BigDecimal.ZERO
        }
        return 1 // singles
    }

    public uk.co.wonderlane.wlpos.entities.wlim.PackLine getPackLine() {
        uk.co.wonderlane.wlpos.entities.wlim.PackLine packLine = new uk.co.wonderlane.wlpos.entities.wlim.PackLine()

        packLine.setPackId(pack?.id)
        packLine.setQuantity(quantity)
        packLine.setOrderCode(orderCode)

        return packLine
    }
}