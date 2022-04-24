package uk.co.wonderlane.wlpos.supplier

import org.joda.time.DateTime
import uk.co.wonderlane.wlpos.ProductVariant
import uk.co.wonderlane.wlpos.enums.PackStatus

class Pack {

    static belongsTo = [ productVariant: ProductVariant ]

    int id
    Supplier supplier
    int quantity
    BigDecimal price
    String orderCode
    String barcode
    BigDecimal recommendedRetailPrice
    DateTime effectiveDate
    DateTime effectiveEndDate
    PackStatus status
    Integer maximumOrderQuantity
    boolean allowSubstitutes

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
    }

    static constraints = {
        productVariant nullable: true
        supplier nullable: true
        quantity nullable: false
        price nullable: true
        orderCode nullable: true
        barcode nullable: true
        recommendedRetailPrice nullable: true
        effectiveDate nullable: true
        effectiveEndDate nullable: true
        status nullable: false
        maximumOrderQuantity nullable: true
        allowSubstitutes nullable: false
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

        return pack
    }
}