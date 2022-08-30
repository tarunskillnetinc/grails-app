package uk.co.wonderlane.wlpos.supplier

import org.joda.time.DateTime
import uk.co.wonderlane.wlpos.ProductVariant
import uk.co.wonderlane.wlpos.enums.PackStatus

class Pack {

    static belongsTo = [ productVariant: ProductVariant ]

    int id
    Supplier supplier
    Integer quantity
    BigDecimal price
    String orderCode
    String barcode
    BigDecimal recommendedRetailPrice
    DateTime effectiveDate
    DateTime effectiveEndDate
    PackStatus status
    Integer maximumOrderQuantity
    boolean allowSubstitutes
    boolean priceMarked
    DateTime updateDatetime

    static mapping = {
        table "pack"
        version false

        productVariant column: "productVariantId"
        supplier column: "supplierId"
        quantity column: "quantity"
        price column: "price"
        orderCode column: "orderCode"
        barcode column: "barcode"
        recommendedRetailPrice column: "recommendedRetailPrice"
        effectiveDate column: "effectiveDate"
        effectiveEndDate column: "effectiveEndDate"
        status column: "status"
        maximumOrderQuantity column: "maximumOrderQuantity"
        allowSubstitutes column: "allowSubstitutes"
        priceMarked column: "priceMarked"
        updateDatetime column: "updateDatetime"
    }

    static constraints = {
        productVariant nullable: true
        supplier nullable: true
        quantity nullable: false, blank: false, min: 1 as Integer
        price nullable: false, blank: false, min: 0.01 as BigDecimal, scale: 2
        orderCode nullable: true
        barcode nullable: true
        recommendedRetailPrice nullable: true, min: 0.01 as BigDecimal, scale: 2
        effectiveDate nullable: true
        effectiveEndDate nullable: true
        status nullable: false
        maximumOrderQuantity nullable: true, min: 0 as Integer
        allowSubstitutes nullable: false
        priceMarked nullable: false
        updateDatetime nullable: false
    }

    public uk.co.wonderlane.wlpos.entities.supplier.Pack getPack() {
        uk.co.wonderlane.wlpos.entities.supplier.Pack pack = new uk.co.wonderlane.wlpos.entities.supplier.Pack()

        pack.setId(id)
        pack.setProductVariantId(productVariant?.id)
        pack.setSupplierId(supplier?.id)
        pack.setQuantity(quantity)
        pack.setPrice(price)
        pack.setOrderCode(orderCode)
        pack.setBarcode(barcode)
        pack.setRecommendedRetailPrice(recommendedRetailPrice)
        pack.setEffectiveDate(effectiveDate)
        pack.setEffectiveEndDate(effectiveEndDate)
        pack.setStatus(status)
        pack.setMaximumOrderQuantity(maximumOrderQuantity != null ? maximumOrderQuantity : 0)
        pack.setAllowSubstitutes(allowSubstitutes)
        pack.setPriceMarked(priceMarked)
        pack.setUpdateDate(updateDatetime)

        return pack
    }
}