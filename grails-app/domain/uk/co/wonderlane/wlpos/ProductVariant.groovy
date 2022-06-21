package uk.co.wonderlane.wlpos

import java.math.RoundingMode
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import uk.co.wonderlane.wlpos.supplier.Pack

class ProductVariant implements Serializable {

    static belongsTo = [product: Product]

    int id
    Integer storeId
    long sku
    BigDecimal retailPrice
    BigDecimal costPrice
    String size
    String colour
    int minimumStockLevel
    DateTime effectiveDate
    boolean delete
    Integer shelfLifeDays

    Collection<Pack> packs = new ArrayList<>()
//    Collection<Tag> tags = new ArrayList<>()

    Collection<Barcode> barcodez = new ArrayList<>()

    static transients = ['delete', 'barcodez']

    static hasMany = [packs: Pack]

    static mapping = {
        table "productvariant"
        version false

        product column: "productId"
        storeId column: "storeId", sqlType: "smallint"
        sku column: "sku"
        retailPrice column: "price"
        costPrice column: "costPrice"
        size column:"size"
        colour column:"colour"
        shelfLifeDays column: "shelfLifeDays"
        minimumStockLevel column: "minimumStockLevel"
        effectiveDate column: "effectiveDate"
        packs cascade: "all-delete-orphan"
    }

    static constraints = {
        storeId nullable: true
        sku nullable: false
        retailPrice min: 0.00 as BigDecimal, max: 99999.99 as BigDecimal, nullable: true, scale: 2
        costPrice min: 0.00 as BigDecimal, max: 99999.99 as BigDecimal, nullable: true, scale: 2
        size size: 0..45, blank: true, nullable: true
        colour size: 0..45, blank: true, nullable: true
        shelfLifeDays nullable: true
        effectiveDate nullable: false
        packs nullable: true
        delete bindable: true
        barcodez bindable: true
    }

    List<ProductPrice> getPrices() {
        return ProductPrice.findAllBySkuAndEffectiveDateLessThanEquals(sku, DateTime.now(DateTimeZone.UTC), [sort: "effectiveDate", order: "desc"])?.unique { it.priceBand }
    }

    BigDecimal getCurrentPrice() {
        if (retailPrice != null) {
            return retailPrice
        } else {
            def storeSettings = storeId ? StoreSettings.findById(storeId) : StoreSettings.findByRetailerIdAndStoreIdIsNull(product.retailerId)
            def productPrice = ProductPrice.findBySkuAndPriceBandAndEffectiveDateLessThanEquals(sku, storeSettings.priceBand, DateTime.now(DateTimeZone.UTC), [sort: "effectiveDate", order: "desc", max: 1])

            return productPrice?.price ?: BigDecimal.ZERO
        }
    }

    List<Barcode> getBarcodes() {
        return Barcode.findAllBySkuAndRetailerIdAndEffectiveDateLessThanEquals(sku, product.retailerId, DateTime.now(DateTimeZone.UTC))
    }

    public uk.co.wonderlane.wlpos.entities.ProductVariant getProductVariant() {
        uk.co.wonderlane.wlpos.entities.ProductVariant productVariant = new uk.co.wonderlane.wlpos.entities.ProductVariant()

        productVariant.setId(id)
        productVariant.setProductId(product.id)
        productVariant.setStoreId(storeId)
        productVariant.setSku(sku)
        productVariant.setRetailPrice(retailPrice)
        productVariant.setCostPrice(costPrice)
        productVariant.setSize(size)
        productVariant.setColour(colour)
        productVariant.setMinimumStockLevel(minimumStockLevel)
        productVariant.setEffectiveDate(effectiveDate)

        getBarcodes()?.each {
            productVariant.getBarcodes().add(it.barcode)
        }

        packs?.eachWithIndex { pack, i ->
            // If a cost price isn't set then take it from the first pack.
            // TODO This should be taken from the "preffered supplier/pack" when this exists.
            if (i == 0 && costPrice == null && pack.price != null && pack.quantity > 0) {
                productVariant.setCostPrice(pack.price.divide(BigDecimal.valueOf(pack.quantity), 2, RoundingMode.HALF_UP))
            }

            productVariant.getPacks().add(pack.getPack())
        }

        // TODO Set tags
//        productVariant.getTags().add(it.getTag())

        return productVariant
    }

    @Override
    boolean equals(Object obj) {
        ProductVariant that = (ProductVariant)obj

        return this.id == that.id && this.storeId == that.storeId && this.productId == that.productId && this.effectiveDate == that.effectiveDate
    }

    @Override
    int hashCode() {
        final int prime = 31
        int result = 1

        result = prime * result + id
        result = prime * result + (storeId ?: 0)
        result = prime * result + productId
        result = prime * result + effectiveDate.hashCode()

        return result
    }
}