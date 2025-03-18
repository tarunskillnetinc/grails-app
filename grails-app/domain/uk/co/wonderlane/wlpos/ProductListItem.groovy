package uk.co.wonderlane.wlpos

import org.joda.time.DateTime
import uk.co.wonderlane.wlpos.entities.supplier.Pack

import java.math.RoundingMode

class ProductListItem {

    int id
    ProductVariant productVariant
    BigDecimal productQuantityInStock
    BigDecimal quantity
    BigDecimal fillQuantity = BigDecimal.ZERO
    BigDecimal parentQuantity
    DateTime effectiveDate

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
        effectiveDate column: "effectiveDate"

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
        effectiveDate nullable:true
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

    public uk.co.wonderlane.wlpos.entities.wlim.ProductListItem getProductListItem(PriceBand priceBand, Integer storeId) {
        uk.co.wonderlane.wlpos.entities.wlim.ProductListItem productListItem = new uk.co.wonderlane.wlpos.entities.wlim.ProductListItem()

        productListItem.setId(id)
        productListItem.setProductVariantId(productVariant?.id)
        productListItem.setProductVariantItemCode(productVariant?.product?.itemCode)
        productListItem.setProductLongDescription(productVariant?.product?.description)
        productListItem.setProductShortDescription(productVariant?.product?.receiptDescription)
        productListItem.setProductBarcodes(productVariant?.barcodes?.collect{ it.barcode })
        productListItem.setProductPrice(productVariant?.getCurrentPrice(priceBand))
        productListItem.setUnitSize(productVariant?.product?.variants?.get(0)?.getSelUnitSize())
        productListItem.setProductQuantityInStock(productVariant?.getProductStock(storeId)?.quantityInStock)
        productListItem.setQuantity(quantity)
        productListItem.setFillQuantity(fillQuantity)
        productListItem.setParentQuantity(parentQuantity)
        productListItem.setProductStatus(productVariant?.product?.status?.name())
        productListItem.setProductListItemGroupId(productListItemGroup?.id)
        productListItem.setShelfCapacity(productVariant?.shelfCapacity)

        productListItem.setPackLines(new ArrayList<>())
        packLines?.each {
            productListItem.getPackLines().add(it.getPackLine())
        }

        productListItem.setAvailablePacks(null) // TODO
        productListItem.setEffectiveDate(effectiveDate)
//        productListItem.setLocation(location) // TODO not yet in CO domain.
        productListItem.setWeighted(productVariant?.product?.weightedItem)
        productListItem.setProductItemCode(productVariant?.product?.itemCode)

        return productListItem
    }
}