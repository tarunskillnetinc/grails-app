package uk.co.wonderlane.wlpos

import org.grails.web.util.WebUtils

import java.math.RoundingMode
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import uk.co.wonderlane.wlpos.supplier.Pack

class ProductVariant implements Serializable {

    def springSecurityService
    def productService

    static belongsTo = [product: Product]

    int id
    Integer storeId
    Integer defaultSupplierId
    long sku
    BigDecimal retailPrice
    BigDecimal costPrice
    String size
    String colour
    int minimumStockLevel
    DateTime effectiveDate
    boolean delete
    Integer shelfLifeDays
    Integer shelfCapacity
    Integer minimumDisplayQuantity

    Collection<Pack> packs = new ArrayList<>()
//    Collection<Tag> tags = new ArrayList<>()

    Collection<Barcode> barcodez = new ArrayList<>()
    Collection<Location> locationz = new ArrayList<>()

    static transients = ['delete', 'barcodez', 'locationz']

    static hasMany = [packs: Pack]

    // This constructor is required or dependency injection (springSecurityService) breaks. Don't forget "autowire true" in the mappings as well.
    public ProductVariant() { }

    static mapping = {
        autowire true
        table "productvariant"
        version false
        sort effectiveDate: "desc"

        product column: "productId"
        storeId column: "storeId", sqlType: "smallint"
        defaultSupplierId column: "defaultSupplierId"
        sku column: "sku"
        retailPrice column: "price"
        costPrice column: "costPrice"
        size column:"size"
        colour column:"colour"
        shelfLifeDays column: "shelfLifeDays"
        minimumStockLevel column: "minimumStockLevel"
        effectiveDate column: "effectiveDate"
        packs cascade: "all-delete-orphan"
        shelfCapacity column: "shelfCapacity"
        minimumDisplayQuantity column: "minimumDisplayQuantity"
    }

    static constraints = {
        storeId nullable: true
        sku nullable: false, min: 1L, validator: {val, obj ->
            if (val > 0) {
                def existingVariants = obj.productService.getProductVariants([val]).find { obj.product.id != it.product.id }.collect()
                return existingVariants.isEmpty() ? true : ['productVariant.sku.validator.error']
            } else {
                return false
            }
        }
        defaultSupplierId nullable: true
        retailPrice min: 0.00 as BigDecimal, max: 99999.99 as BigDecimal, nullable: true, scale: 2
        costPrice min: 0.00 as BigDecimal, max: 99999.99 as BigDecimal, nullable: true, scale: 2
        size size: 0..45, blank: true, nullable: true
        colour size: 0..45, blank: true, nullable: true
        shelfLifeDays nullable: true
        effectiveDate nullable: false
        packs nullable: true
        shelfCapacity nullable: true
        minimumDisplayQuantity nullable: true
        delete bindable: true
        barcodez bindable: true
        locationz bindable: true
    }

    List<ProductPrice> getPrices() {
        return ProductPrice.findAllBySkuAndEffectiveDateLessThanEquals(sku, getSessionEffectiveDate(), [sort: "effectiveDate", order: "desc"])?.unique { it.priceBand }
    }

    List<ProductPrice> getAllPrices() {
        return ProductPrice.findAllBySku(sku, [sort: "effectiveDate", order: "desc"])
    }

    BigDecimal getCostPrice() {
        if (costPrice != null) {
            return costPrice
        } else if (packs == null || packs.isEmpty()) {
            return BigDecimal.ZERO.setScale(2)
        } else {
            // TODO Need to return the preferred supplier/pack.
            return packs.first().price.divide(BigDecimal.valueOf(packs.first().quantity), 2, RoundingMode.HALF_UP)
        }
    }

    BigDecimal getCurrentPrice() {
        getCurrentPrice(null)
    }

    BigDecimal getCurrentPrice(PriceBand priceBand) {
        if (retailPrice != null) {
            return retailPrice
        } else {
            def now = DateTime.now(DateTimeZone.UTC)

            def productPrice = ProductPrice.findBySkuAndPriceBandAndEffectiveDateLessThanEquals(
                    sku,
                    priceBand != null ? priceBand : springSecurityService.principal.priceBand,
                    effectiveDate < now ? now : effectiveDate, // If the variant effective date is in the past we might still have a more recent price entry so use the current time.
                    [sort: "effectiveDate", order: "desc", max: 1]
            )

            return productPrice?.price ?: BigDecimal.ZERO.setScale(2)
        }
    }

    public List<Barcode> getBarcodes() {
        // Load all barcodes based on sku.
        def barcodesOnSku = Barcode.findAllBySkuAndRetailerIdAndEffectiveDateLessThanEquals(sku, springSecurityService.principal.retailerId, getSessionEffectiveDate(), [sort: "effectiveDate", order: "desc"])

        // Declare list to populate displaying barcodes.
        def barcodesToShow = new ArrayList<Barcode>()

        // Group by barcodes based on barcode value.
        def barcodesMap = barcodesOnSku?.groupBy {it.barcode }

        // They're already sorted in effective date, so if the first is valid then display it, if not then it's deleted and shouldn't be displayed.
        barcodesMap?.each {
            if (it.value?.first()?.recordStatus == ('C' as char)) {
                barcodesToShow.add(it.value?.first())
            }
        }

        return barcodesToShow
    }

    public List<Barcode> getAllBarcodes() {
        return Barcode.findAllBySkuAndRetailerId(sku, springSecurityService.principal.retailerId)
    }

    public List<Location> getLocations() {
        return Location.findAllBySkuAndStoreId(sku, springSecurityService.principal.storeId)
    }

    public DateTime getSessionEffectiveDate() {
        def sessionEffectiveDate = WebUtils.retrieveGrailsWebRequest().session.getAttribute("effectiveDate")

        return sessionEffectiveDate != null && sessionEffectiveDate.size() > 0 ? sessionEffectiveDate[1] : DateTime.now(DateTimeZone.UTC)
    }

    public uk.co.wonderlane.wlpos.entities.ProductVariant getProductVariant() {
        getProductVariant(null)
    }

    public uk.co.wonderlane.wlpos.entities.ProductVariant getProductVariant(PriceBand priceBand) {
        uk.co.wonderlane.wlpos.entities.ProductVariant productVariant = new uk.co.wonderlane.wlpos.entities.ProductVariant()

        productVariant.setId(id)
        productVariant.setProductId(product.id)
        productVariant.setStoreId(storeId)
        productVariant.setSku(sku)
        productVariant.setRetailPrice(getCurrentPrice(priceBand))
        productVariant.setCostPrice(getCostPrice())
        productVariant.setSize(size)
        productVariant.setColour(colour)
        productVariant.setMinimumStockLevel(minimumStockLevel)
        productVariant.setEffectiveDate(effectiveDate)
        productVariant.setMinimumDisplayQuantity(minimumDisplayQuantity)
        productVariant.setShelfCapacity(shelfCapacity)

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

        getLocations()?.each {
            productVariant.getLocations().add(it.getCommonLocation())
        }

        return productVariant
    }

    public ProductStock getProductStock(Integer storeId) {
        return ProductStock.findBySkuAndStoreId(sku, storeId)
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