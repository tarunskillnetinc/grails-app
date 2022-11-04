package uk.co.wonderlane.wlpos

import org.grails.web.util.WebUtils

import java.math.RoundingMode
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import uk.co.wonderlane.wlpos.supplier.Pack

class ProductVariant implements Serializable {

    def springSecurityService

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

    Collection<Pack> packs = new ArrayList<>()
//    Collection<Tag> tags = new ArrayList<>()

    Collection<Barcode> barcodez = new ArrayList<>()

    static transients = ['delete', 'barcodez']

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
    }

    static constraints = {
        storeId nullable: true
        sku nullable: false, validator: {val, obj ->
            if (val > 0) {
                def existingVariant = ProductVariant.findBySku(val)
                return (existingVariant != null && obj.productId != existingVariant.productId) ? ["error.ProductVariant.duplicateSku"] : true
            } else {
                return true
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
        delete bindable: true
        barcodez bindable: true
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
        if (retailPrice != null) {
            return retailPrice
        } else {
            def productPrice = ProductPrice.findBySkuAndPriceBandAndEffectiveDateLessThanEquals(sku, springSecurityService.principal.priceBand, getSessionEffectiveDate(), [sort: "effectiveDate", order: "desc", max: 1])

            return productPrice?.price ?: BigDecimal.ZERO.setScale(2)
        }
    }

    List<Barcode> getBarcodes() {

        //Load all barcodes based on sku
        def barcodesOnSku = Barcode.findAllBySkuAndRetailerIdAndEffectiveDateLessThanEquals(sku, springSecurityService.principal.retailerId, getSessionEffectiveDate(), [sort: "effectiveDate", order: "desc"])

        //Declare list to populate displaying barcodes
        def barcodesToShow = new ArrayList<Barcode>()

        //Group by barcodes based on barcode value
        def barcodesMap = barcodesOnSku?.groupBy {it.barcode}

        //Then loop over map of barcode to find out all active barcode
        for (Map.Entry<String, List<Barcode>> barcodeList : barcodesMap.entrySet()){

            int deletedBarcode = 0
            int activeBarcodes = 0

            //For barcode belonging to particular sku check occurrence of active and deleted
            barcodeList.getValue()?.forEach({ barcode ->
                if (barcode.recordStatus == ('D' as char)) {
                    deletedBarcode ++
                } else {
                    activeBarcodes ++
                }
            })

            //If active barcode count (Status = 'C') greater than of barcode count for deleted (Status = 'D') then we pick latest active barcode and add it to show item list
            if (activeBarcodes > deletedBarcode){
                int limit = activeBarcodes - deletedBarcode
                //Sort all active barcodes into descending order and pick top most item list
                def activeBarcodeList = barcodeList.getValue()?.findAll{it.getRecordStatus() == ('C' as char)}?.sort{it.effectiveDate}?.reverse()?.subList(0, limit)
                if (activeBarcodeList != null && activeBarcodeList.size() > 0){
                    barcodesToShow.addAll(activeBarcodeList)
                }
            }

        }

        return barcodesToShow
    }

    List<Barcode> getAllBarcodes() {
        return Barcode.findAllBySkuAndRetailerId(sku, springSecurityService.principal.retailerId)
    }

    private DateTime getSessionEffectiveDate() {
        def sessionEffectiveDate = WebUtils.retrieveGrailsWebRequest().session.getAttribute("effectiveDate")

        return sessionEffectiveDate != null && sessionEffectiveDate.size() > 0 ? sessionEffectiveDate[1] : DateTime.now(DateTimeZone.UTC)
    }

    public uk.co.wonderlane.wlpos.entities.ProductVariant getProductVariant() {
        uk.co.wonderlane.wlpos.entities.ProductVariant productVariant = new uk.co.wonderlane.wlpos.entities.ProductVariant()

        productVariant.setId(id)
        productVariant.setProductId(product.id)
        productVariant.setStoreId(storeId)
        productVariant.setSku(sku)
        productVariant.setRetailPrice(getCurrentPrice())
        productVariant.setCostPrice(getCostPrice())
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