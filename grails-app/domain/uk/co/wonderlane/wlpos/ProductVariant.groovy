package uk.co.wonderlane.wlpos

import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import uk.co.wonderlane.wlpos.supplier.Pack

class ProductVariant implements Serializable {

    static belongsTo = [product: Product]

    int id
    int storeId
    long sku
    BigDecimal retailPrice
    BigDecimal costPrice
    String size
    String colour
    int balanceOnHand
    int balanceOnOrder
    int minimumStockLevel
    DateTime effectiveDate
    DateTime createdDatetime
    int createdUserId
    DateTime updatedDatetime
    int updatedUserId
    boolean delete

    Collection<Pack> packs = new ArrayList<>()

    static transients = ['delete']

    static hasMany = [packs: Pack]

    static mapping = {
        table "productvariant"
        version false

        product column: "productId"
        storeId column: "storeId"
        sku column: "sku"
        retailPrice column: "price"
        costPrice column: "costPrice"
        size column:"size"
        colour column:"colour"
        balanceOnHand column:"balanceOnHand"
        balanceOnOrder column: "balanceOnOrder"
        minimumStockLevel column: "minimumStockLevel"
        effectiveDate column: "effectiveDate"
        createdDatetime column: "createdDatetime"
        createdUserId column: "createdUserId"
        updatedDatetime column: "updatedDatetime"
        updatedUserId column: "updatedUserId"
        packs cascade: "all-delete-orphan"
    }

    static constraints = {
        sku nullable: false
        retailPrice min: 0.00 as BigDecimal, max: 99999.99 as BigDecimal, nullable: true, scale: 2
        costPrice min: 0.00 as BigDecimal, max: 99999.99 as BigDecimal, nullable: true, scale: 2
        size size: 0..45, blank: true, nullable: true
        colour size: 0..45, blank: true, nullable: true
        effectiveDate nullable: false
        createdUserId nullable: true
        createdDatetime nullable: true
        updatedUserId nullable: true
        updatedDatetime nullable: true
        packs nullable: true
        delete bindable: true
    }

    List<ProductPrice> getPrices() {
        return ProductPrice.findAllBySkuAndEffectiveDateLessThanEquals(sku, DateTime.now(DateTimeZone.UTC))
    }

    BigDecimal getCurrentPrice() {
        if (retailPrice != null) {
            return retailPrice
        } else {
            def storeSettings = StoreSettings.findByStoreId(storeId)

            return ProductPrice.findAllBySkuAndPriceBandAndEffectiveDateLessThanEquals(sku, storeSettings.priceBand, DateTime.now(DateTimeZone.UTC), [sort: "effectiveDate", order: "desc", max: 1])?.first()?.price ?: BigDecimal.ZERO
        }
    }

    List<Barcode> getBarcodes() {
        return Barcode.findAllBySkuAndEffectiveDateLessThanEquals(sku, DateTime.now(DateTimeZone.UTC))
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
        productVariant.setBalanceOnHand(balanceOnHand)
        productVariant.setBalanceOnOrder(balanceOnOrder)
        productVariant.setMinimumStockLevel(minimumStockLevel)
        productVariant.setEffectiveDate(effectiveDate)

        getBarcodes()?.each {
            productVariant.getBarcodes().add(it.barcode)
        }

        packs?.each {
            productVariant.getPacks().add(it.getPack())
        }

        return productVariant
    }

    def beforeInsert() {
        def now = DateTime.now(DateTimeZone.UTC)

        createdDatetime = now
        updatedDatetime = now
    }

    def beforeUpdate() {
        updatedDatetime = DateTime.now(DateTimeZone.UTC)
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
        result = prime * result + storeId
        result = prime * result + productId
        result = prime * result + effectiveDate.hashCode()

        return result
    }
}