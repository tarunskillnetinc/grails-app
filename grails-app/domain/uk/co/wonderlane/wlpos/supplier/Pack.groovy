package uk.co.wonderlane.wlpos.supplier

import org.joda.time.DateTime
import uk.co.wonderlane.wlpos.ProductVariant
import uk.co.wonderlane.wlpos.entities.wlim.PackLine
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

    int getQuantity(List<PackLine> packLines){
        PackLine packLine = packLines?.find {it?.orderCode == this?.orderCode}
        if (packLine != null){
            return packLine.quantity
        }
        return 0;
    }

    static constraints = {
        productVariant nullable: true
        supplier nullable: false, blank: false
        quantity nullable: false, blank: false, min: 1 as Integer, max: 2147483647 as Integer
        price nullable: false, blank: false, min: 0.01 as BigDecimal, max: 9999.99 as BigDecimal, scale: 2
        orderCode nullable: true, size: 1..20
        barcode nullable: true, size: 1..20, validator: { val, obj ->
            if (val) {
                def existingPacks = Pack.findAllByBarcode(val)
                boolean isDuplicateBarcode = existingPacks?.stream().anyMatch({ pack -> pack.productVariantId != obj.productVariantId })

                return !isDuplicateBarcode
            } else {
                return true
            }
        }
        recommendedRetailPrice nullable: true, max: 9999.99 as BigDecimal, scale: 2
        effectiveDate nullable: true
        effectiveEndDate nullable: true
        status nullable: false
        maximumOrderQuantity nullable: true, min: 0 as Integer, max: 99999 as Integer
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