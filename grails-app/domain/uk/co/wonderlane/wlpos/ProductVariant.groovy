package uk.co.wonderlane.wlpos

import org.grails.web.util.WebUtils
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import uk.co.wonderlane.wlpos.enums.LocationsType
import uk.co.wonderlane.wlpos.enums.StockManagementType
import uk.co.wonderlane.wlpos.supplier.Pack

import java.math.RoundingMode
import java.util.stream.Collectors

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
    BigDecimal weightedAverageCostPrice
    int minimumStockLevel
    DateTime effectiveDate
    boolean delete
    Integer shelfLifeDays
    Integer shelfCapacity
    Integer minimumDisplayQuantity
    String description
    String receiptDescription
    boolean priceMarked
    BigDecimal unitSize
    UnitOfMeasure unitOfMeasure
    Integer itemsInUnit
    BigDecimal heightCm
    BigDecimal widthCm
    BigDecimal depthCm
    StockManagementType stockManagementType
    BigDecimal minAlcoholUnitPrice
    String extras

    Collection<Pack> packs = new ArrayList<>()
//    Collection<ProductGroup> tags = new ArrayList<>()

    Collection<Barcode> barcodez = new ArrayList<>()
    Collection<Location> locationz = new ArrayList<>()

    static transients = ['delete', 'barcodez', 'locationz']

    static hasMany = [packs: Pack]

    // This constructor is required or dependency injection (springSecurityService) breaks. Don't forget "autowire true" in the mappings as well.
    public ProductVariant() { }

    boolean getPreferredSku() {
        // Indicates if this variant has the preferred sku for the product
        return sku == product?.preferredSku
    }

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
        weightedAverageCostPrice column: "weightedAverageCostPrice"
        shelfLifeDays column: "shelfLifeDays"
        minimumStockLevel column: "minimumStockLevel"
        effectiveDate column: "effectiveDate"
        packs cascade: "all-delete-orphan"
        shelfCapacity column: "shelfCapacity"
        minimumDisplayQuantity column: "minimumDisplayQuantity"
        description column: "`description`"
        receiptDescription column: "receiptDescription"
        priceMarked column: "priceMarked"
        unitSize column: "unitSize"
        unitOfMeasure column: "unitOfMeasure"
        itemsInUnit column: "itemsInUnit", sqlType: "smallint"
        heightCm column: "heightCm"
        widthCm column: "widthCm"
        depthCm column: "depthCm"
        extras column: "extras", sqlType: "json"
        stockManagementType column: "stockManagementType", sqlType: "enum", enumType: 'string'
        minAlcoholUnitPrice column: "minAlcoholUnitPrice"
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
        weightedAverageCostPrice nullable: true
        shelfLifeDays nullable: true
        effectiveDate nullable: false
        packs nullable: true
        shelfCapacity nullable: true
        minimumDisplayQuantity nullable: true
        description nullable: true
        receiptDescription nullable: true
        priceMarked nullable: false
        unitSize nullable: true, max: 9999.999 as BigDecimal, scale: 3
        unitOfMeasure nullable: true
        itemsInUnit nullable: false, min: 1, max: 9999
        heightCm nullable: true
        widthCm nullable: true
        depthCm nullable: true
        stockManagementType nullable: false
        minAlcoholUnitPrice nullable: true
        extras nullable: true
        delete bindable: true
        barcodez bindable: true
        locationz bindable: true
    }

    static int countMatchingSkusForRetailer(Long sku, Integer retailerId) {
        def query = "FROM ProductVariant pv JOIN pv.product p WHERE pv.sku = :sku AND p.retailerId = :retailerId"
        def params = [sku: sku, retailerId: retailerId]

        def count = ProductVariant.executeQuery(query, params).size()

        return count
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
        def barcodesOnSku = Barcode.findAllBySkuAndRetailerIdAndEffectiveDateLessThanEquals(
                sku, springSecurityService.principal.retailerId, getSessionEffectiveDate(), [sort: "effectiveDate", order: "desc"])

        // Declare list to populate displaying barcodes.
        def barcodesToShow = new ArrayList<Barcode>()

        // Sort the list by id in descending order in case if barcode deleted in same date as created list might not be in
        def sortedBarcodes = barcodesOnSku.stream()
                .sorted((b1, b2) -> {
                    int compareEffectiveDate = b2.getEffectiveDate().compareTo(b1.getEffectiveDate())
                    if (compareEffectiveDate != 0) {
                        return compareEffectiveDate
                    }
                    return b2.getId().compareTo(b1.getId())
                })
                .collect(Collectors.toList())

        // Group by barcode, ensuring each group is sorted by id in descending order
        // Collect into linkedHashMap to ensure the map maintains insertion order
        def groupedByBarcodeValue = sortedBarcodes.stream().collect(Collectors.groupingBy(
                { it -> it.barcode ?: "NULL" },  // Use "NULL" as key for null barcodes
                { -> new LinkedHashMap<>() },
                Collectors.toList()
        ))

        // They're already sorted in effective date, so if the first is valid then display it, if not then it's deleted and shouldn't be displayed.
        groupedByBarcodeValue?.each {
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
        return Location.findAllBySkuAndStoreIdAndDeleted(sku, springSecurityService.principal.storeId, false)
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
        productVariant.setSize(null)
        productVariant.setColour(null)
        productVariant.setMinimumStockLevel(minimumStockLevel)
        productVariant.setEffectiveDate(effectiveDate)
        productVariant.setMinimumDisplayQuantity(minimumDisplayQuantity)
        productVariant.setShelfCapacity(shelfCapacity)
        productVariant.setMinAlcoholUnitPrice(minAlcoholUnitPrice)
        productVariant.setStockManagementType(stockManagementType)

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

    List getLocationsHierarchy() {
        List locationHierarchy = new ArrayList();
        def locationsType = springSecurityService.principal.retailer.config.locationsType.name()
        def locations = locationz ? locationz : locations
        if (locationsType == LocationsType.ADVANCED.name()){
            for (int i=1; i <= locations.size() ; i++){
                locationHierarchy.add(i)
            }
        }
        return locationHierarchy;
    }

    String getSelUnitSize() {
        if (unitOfMeasure == null) {
            return "EACH"
        }

        String exponent = ""
        if( (itemsInUnit?:1) > 1) {
            exponent = itemsInUnit + "x"
        }

        BigDecimal perUnit = null
        if (unitSize != null) {
            perUnit = unitSize.divide(itemsInUnit?:1, 3, RoundingMode.HALF_UP)
        }

        return exponent + (perUnit?:"") + unitOfMeasure?.symbol?:"EACH"
    }
}
