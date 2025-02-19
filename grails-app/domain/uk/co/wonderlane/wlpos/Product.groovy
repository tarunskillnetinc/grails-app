package uk.co.wonderlane.wlpos

import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import org.springframework.context.i18n.LocaleContextHolder
import uk.co.wonderlane.wlpos.enums.ProductStatus
import uk.co.wonderlane.wlpos.enums.ProductMessageType

import java.math.RoundingMode

class Product {

    def springSecurityService
    def messageSource

    int id
    int retailerId
    String itemCode
    String description
    String receiptDescription
    Category category
    boolean weightedItem
    boolean openPrice
    boolean zeroPrice
    boolean pricePerKg
    boolean snappyProduct
    boolean deliItem
    VatCode vatCode
    BigDecimal vatPercentageOverride
    Restrictions restrictions
    String discreetMessage
    ProductStatus status
    String retailerProductId
    Long preferredSku
    boolean ownLabel
    String extras

    Collection<ProductVariant> variants = new ArrayList<>()
    Collection<ProductAttributeValues> productAttributeValues = new ArrayList<>()

    BigDecimal retailPrice
    BigDecimal costPrice

    String selDescription
    SelType selType
    String productImgUrl

    static hasMany = [ variants: ProductVariant, productAttributeValues: ProductAttributeValues ]
    static belongsTo = [selType: SelType]

    static transients = ['retailPrice', 'costPrice']

    // This constructor is required or dependency injection (springSecurityService) breaks. Don't forget "autowire true" in the mappings as well.
    public Product() { }

    static mapping = {
        autowire true
        table "product"
        version false

        retailerId column: "retailerId", sqlType: "tinyint"
        itemCode column: "itemCode", sqlType: "char"
        description column: "`description`"
        receiptDescription column: "receiptDescription"
        category column: "categoryId"
        pricePerKg column: "pricePerKg"
        snappyProduct column: "snappyProduct"
        deliItem column: "deliItem"
        weightedItem column: "weightedItem"
        openPrice column: "openPrice"
        zeroPrice column: "zeroPrice"
        vatCode column: "vatCodeId"
        vatPercentageOverride column: "vatPercentageOverride"
        restrictions column: "restrictionsId", cascade: "save-update"
        discreetMessage column: "discreetMessage"
        status column: "`status`", sqlType: "enum", enumType: "string"
        retailerProductId column: "retailerProductId"
        variants cascade: "save-update,delete"
        productAttributeValues cascade: "save-update,delete"
        selDescription column: "selDescription"
        selType column: "selType"
        productImgUrl column: "productImgUrl"
        preferredSku column: "preferredSku"
        ownLabel column: "ownLabel"
        extras column: "extras", sqlType: "json"
    }

    static constraints = {
        itemCode size: 1..20, blank: true, nullable: true, validator: { val, obj ->
            return Product.countByRetailerIdAndItemCodeAndIdNotEqual(obj.retailerId, obj.itemCode, obj.id) > 0 ? ["error.product.duplicateItemCode"] : true
        }
        description size: 1..100, blank: false, nullable: false
        receiptDescription size: 1..50, blank: false, nullable: false
        discreetMessage size: 0..50, blank: true, nullable: true
        vatPercentageOverride min:0 as BigDecimal, max: 100 as BigDecimal, blank: true, nullable: true, scale: 2
        vatCode nullable: false
        status nullable: false
        category nullable: false, validator: {val, obj ->
            if (val?.retailerCategoryCode == null) {
                return ["error.Product.retailerCategoryCode"]
            }

            return val?.validate()
        }
        retailerProductId nullable: true
        restrictions validator: {val, obj ->
            return val?.validate() ? true : ["error.Product.badRestrictions"]
        }
        variants minSize: 1, validator: {val, obj ->
//            boolean noError = true;
//            List<ProductVariant> variants = val.collect()
//
//            def allFields = ProductVariant.declaredFields.collectMany {!it.synthetic ? [it.name] : []}
//            def allFieldsButExclusion = allFields - ['product']

//            for (ProductVariant productVariant : variants) {
//                if(!productVariant.validate(allFieldsButExclusion)) {
//                    noError = false;
//                }
//            }
//            return noError ? true : ["error.Product.badVariants"]
            return true
        }
        selDescription nullable: true, blank: true
        selType nullable: true
        productImgUrl nullable: true, blank: true, url: true
        preferredSku nullable: true
        ownLabel nullable: false
        extras nullable: true
    }

    List<RangeProduct> getRanges() {
        return RangeProduct.findAllByProductIdAndDeleted(id, false)
    }

    BigDecimal getCostPrice() {
        if (costPrice) {
            return costPrice
        }

        def now = DateTime.now(DateTimeZone.UTC)

        // Only variants which have effective dates before now.
        def activeVariants = variants.findAll { it.effectiveDate < now }

        def sortedVariants = activeVariants.sort { a,b ->
            a.storeId <=> b.storeId ?: b.effectiveDate <=> a.effectiveDate
        }

        costPrice = sortedVariants?.find { it.storeId == springSecurityService.principal.storeId }?.costPrice

        // If logged in as a store and there wasn't an override for your store then go and find the HO level variant and take the cost price from there.
        if (!costPrice && springSecurityService.principal.storeId != null) {
            costPrice = sortedVariants?.find { it.storeId == null }?.costPrice
        }

        return costPrice ?: BigDecimal.ZERO.setScale(2)
    }

    BigDecimal getRetailPrice() {
        if (retailPrice) {
            return retailPrice
        }

        def now = DateTime.now(DateTimeZone.UTC)

        // Only variants which have effective dates before now.
        def activeVariants = variants.findAll { it.effectiveDate < now }

        def sortedVariants = activeVariants.sort { a,b ->
            a.storeId <=> b.storeId ?: b.effectiveDate <=> a.effectiveDate
        }

        retailPrice = sortedVariants?.find { it.storeId == springSecurityService.principal.storeId }?.currentPrice

        // If logged in as a store and there wasn't an override for your store then go and find the HO level variant and take the price from there.
        if (!retailPrice && springSecurityService.principal.storeId != null) {
            retailPrice = sortedVariants?.find { it.storeId == null }?.currentPrice
        }

        return retailPrice ?: BigDecimal.ZERO.setScale(2)
    }

    BigDecimal getVat() {
        BigDecimal divisor = BigDecimal.ONE.add(vatCode.percentage.divide(BigDecimal.valueOf(100)));
        BigDecimal price = getRetailPrice()

        return price.subtract(price.divide(divisor, 2, RoundingMode.HALF_UP));
    }

    BigDecimal getMargin() {
        BigDecimal netSellingPrice = getRetailPrice().subtract(getVat());

        return netSellingPrice.compareTo(BigDecimal.ZERO) > 0 ? netSellingPrice.subtract(getCostPrice()).divide(netSellingPrice, 4, RoundingMode.HALF_UP).movePointRight(2) : BigDecimal.ZERO.setScale(2);
    }

    boolean isCurrentProductVariant(DateTime effectiveDate, Integer variantId, Long sku) {
        return variants.stream()
                .filter({variant -> variant.effectiveDate <= effectiveDate && variant.sku == sku && (variant.storeId == null || variant.storeId == springSecurityService.principal.storeId)})
                .max({ a,b -> a.effectiveDate <=> b.effectiveDate ?: a.id <=> b.id})
                .filter({variant -> variant.id == variantId }).stream().findAny().present
    }

    List<ProductVariant> getCurrentVariants() {
        def allVariants = variants?.findAll { it.storeId == null || it.storeId == springSecurityService.principal.storeId }

        return allVariants?.sort{a, b -> b.effectiveDate <=> a.effectiveDate ?: b.id <=> a.id }?.unique { a, b -> a.sku <=> b.sku }
    }

    List<String> getEffectiveDatesForFutureChanges() {
        def now = DateTime.now(DateTimeZone.UTC)

        def futureEffectiveDates = [ messageSource.getMessage('product.effective.date.current', null, "Current", LocaleContextHolder.getLocale()) ]

        boolean containsStoreLevelVariants = springSecurityService.principal.storeId && variants?.collect { it.storeId }?.contains(springSecurityService.principal.storeId)

        variants.each { ProductVariant pv ->
            if (pv.effectiveDate > now) {
                // For the purpose of this check, if there are any store level variants, then we only want to include those dates at this stage.
                if (containsStoreLevelVariants) {
                    if (pv.storeId == springSecurityService.principal.storeId) {
                        futureEffectiveDates.add(pv.effectiveDate.toString("dd MMMM yyyy"))
                    }
                } else {
                    if (pv.storeId == null) {
                        futureEffectiveDates.add(pv.effectiveDate.toString("dd MMMM yyyy"))
                    }
                }
            }

            pv.getAllBarcodes()?.each { Barcode b ->
                if (b.effectiveDate > now) {
                    futureEffectiveDates.add(b.effectiveDate.toString("dd MMMM yyyy"))
                }
            }

            pv.getAllPrices()?.each { ProductPrice pp ->
                if (pp.effectiveDate > now) {
                    futureEffectiveDates.add(pp.effectiveDate.toString("dd MMMM yyyy"))
                }
            }

            // TODO Packs? Weren't currently handled so I haven't changed the functionality.
        }

        return futureEffectiveDates.unique()
    }

    public uk.co.wonderlane.wlpos.entities.Product getProduct(Integer storeId, PriceBand priceBand) {
        uk.co.wonderlane.wlpos.entities.Product product = new uk.co.wonderlane.wlpos.entities.Product()

        product.setId(id)
        product.setRetailerId(retailerId)
        product.setItemCode(itemCode)
        product.setDescription(description)
        product.setReceiptDescription(receiptDescription)
        product.setCategory(category.getCategory())
        product.setUnitSize(variants?.sort {a,b -> -(a.getEffectiveDate() <=> b.getEffectiveDate())}?.find {it.storeId == storeId || it.storeId == null}?.getSelUnitSize()?: "EACH")
        product.setWeightedItem(weightedItem)
        product.setPricePerKg(pricePerKg)
        product.setOpenPrice(openPrice)
        product.setZeroPrice(zeroPrice)
        product.setVatCode(vatCode.getVatCode())
        product.setVatPercentageOverride(vatPercentageOverride)
        product.setRestrictions(restrictions.getRestrictions())
        product.setDiscreetMessage(discreetMessage)
        product.setStatus(status)

        variants.each {
            if (it.storeId == null || it.storeId == storeId) {
                product.getVariants().add(it.getProductVariant(priceBand))
            }
        }
        getSaleMessages().each {
            product.getSaleMessages().add(it.getMessage())
        }
        getRefundMessages().each {
            product.getRefundMessages().add(it.getMessage())
        }
        getScoMessages().each {
            product.getScoMessages().add(it.getMessage())
       }
        product.setRetailerItemId(retailerProductId)
        product.setLocal(false)

        product.setSelDescription(selDescription)
        uk.co.wonderlane.wlpos.entities.SelType selTypeCommon = new uk.co.wonderlane.wlpos.entities.SelType();
        selTypeCommon.setId(selType?.id)
        selTypeCommon.setName(selType?.name)
        product.setSelType(selTypeCommon)
        product.setProductImgUrl(productImgUrl)

        return product
    }

    def getSaleMessages() {
        def saleMessages = []
        
        if (id != 0) {
            saleMessages = ProductMessage.findAllByProductAndType(this, ProductMessageType.SALE)*.message
        }

        return saleMessages
    }
    
    def getRefundMessages() {
        def refundMessages = []
        
        if (id != 0) {
            refundMessages = ProductMessage.findAllByProductAndType(this, ProductMessageType.REFUND)*.message
        }

        return refundMessages
    }
    
    def getScoMessages() {
        def scoMessages = []
        
        if (id != 0) {
            scoMessages = ProductMessage.findAllByProductAndType(this, ProductMessageType.SCO)*.message
        }

        return scoMessages
    }
}
