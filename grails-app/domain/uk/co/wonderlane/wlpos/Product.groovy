package uk.co.wonderlane.wlpos

import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import org.joda.time.format.DateTimeFormat
import org.joda.time.format.DateTimeFormatter
import uk.co.wonderlane.wlpos.enums.ProductStatus

import java.math.RoundingMode
import java.util.stream.Collectors

import org.springframework.context.i18n.LocaleContextHolder

class Product {

    def springSecurityService
    def messageSource

    int id
    int retailerId
    String itemCode
    String description
    String receiptDescription
    Category category
    String unitSize
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

    Collection<Message> saleMessages = new ArrayList<>()
    Collection<Message> refundMessages = new ArrayList<>()
    Collection<DiscountRate> discountRates = new ArrayList<>()
    Collection<ProductVariant> variants = new ArrayList<>()

    BigDecimal retailPrice

    static hasMany = [ saleMessages: Message, refundMessages: Message, discountRates: DiscountRate, variants: ProductVariant ]

    static transients = ['retailPrice']

    // This constructor is required or dependency injection (springSecurityService) breaks. Don't forget "autowire true" in the mappings as well.
    public Product() { }

    static mapping = {
        autowire true
        table "product"
        version false

        retailerId column: "retailerId", sqlType: "tinyint"
        itemCode column: "itemCode"
        description column: "`description`"
        receiptDescription column: "receiptDescription"
        category column: "categoryId"
        unitSize column: "unitSize"
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

        saleMessages joinTable: [name: 'productmessage', key: 'productId', column: 'messageId']
        refundMessages joinTable: [name: 'productmessage', key: 'productId', column: 'messageId']
        discountRates joinTable: [name: 'productdiscount', key: 'productId', column: 'discountRateId']
    }

    static constraints = {
        itemCode size: 1..18, blank: false, nullable: false, validator: { val, obj ->
            return Product.countByRetailerIdAndItemCodeAndIdNotEqual(obj.retailerId, obj.itemCode, obj.id) > 0 ? ["error.product.duplicateItemCode"] : true
        }
        description size: 1..100, blank: false, nullable: false
        receiptDescription size: 1..25, blank: false, nullable: false
        discreetMessage size: 0..50, blank: true, nullable: true
        unitSize size: 1..50, blank: false, nullable:false
        vatPercentageOverride min:0 as BigDecimal, max: 100 as BigDecimal, blank: true, nullable: true, scale: 2
        vatCode nullable: false
        status nullable: false
        category nullable: false
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
    }

    List<RangeProduct> getRanges() {
        return RangeProduct.findAllByProductId(id)
    }

    BigDecimal getCostPrice() {
        def sortedVariants = variants.sort { a,b ->
            a.storeId <=> b.storeId ?: b.effectiveDate <=> a.effectiveDate
        }

        return sortedVariants?.find { it.storeId == springSecurityService.principal.storeId }?.costPrice ?: BigDecimal.ZERO.setScale(2)
    }

    BigDecimal getRetailPrice() {
        if (retailPrice) {
            return retailPrice
        }

        def sortedVariants = variants.sort { a,b ->
            a.storeId <=> b.storeId ?: b.effectiveDate <=> a.effectiveDate
        }

        retailPrice = sortedVariants?.find { it.storeId == springSecurityService.principal.storeId }?.currentPrice

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
        def allVariants = variants?.findAll { it.storeId == null || it.storeId == springSecurityService.principal.storeId}
        return allVariants?.sort{a, b -> b.effectiveDate <=> a.effectiveDate ?: b.id <=> a.id}
                ?.unique { a, b -> a.sku <=> b.sku }
    }

    private Set<DateTime> getEffectiveDates() {
        def now = DateTime.now(DateTimeZone.UTC)
        def effectiveDates = new HashSet<DateTime>()

        variants.forEach(
                {variant ->
                    variant.getAllBarcodes().stream().filter({barcode -> barcode.effectiveDate > now}).forEach(
                            {barcode ->
                                effectiveDates.add(barcode.effectiveDate)
                            })
                    if (variant.effectiveDate > now) {
                        effectiveDates.add(variant.effectiveDate)
                    }
                    variant.getAllPrices().stream().filter({ price -> price.effectiveDate > now}).forEach(
                            {
                                price -> effectiveDates.add(price.effectiveDate)
                            }
                    )
                })
        return effectiveDates.sort()
    }

    List<String> getEffectiveDatesForFutureChanges() {
        DateTimeFormatter formatter = DateTimeFormat.forPattern("dd MMMM yyyy")

        def results = getEffectiveDates().stream().map({date -> date.toString(formatter)}).collect(Collectors.toList())
        results.add(0, messageSource.getMessage('product.effective.date.current', null, "Current", LocaleContextHolder.getLocale()))

        return results
    }

    public uk.co.wonderlane.wlpos.entities.Product getProduct(Integer storeId) {
        uk.co.wonderlane.wlpos.entities.Product product = new uk.co.wonderlane.wlpos.entities.Product()
//        ProductVariant productVariant = variants.sort { it.effectiveDate }.reverse().find { it.storeId == storeId && it.effectiveDate <= DateTime.now(DateTimeZone.UTC) }

        product.setId(id)
        product.setRetailerId(retailerId)
        product.setItemCode(itemCode)
        product.setDescription(description)
        product.setReceiptDescription(receiptDescription)
        product.setCategory(category.getCategory())
        product.setUnitSize(unitSize)
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
                product.getVariants().add(it.getProductVariant())
            }
        }
        saleMessages.each {
            product.getSaleMessages().add(it.getMessage())
        }
        refundMessages.each {
            product.getRefundMessages().add(it.getMessage())
        }
        discountRates.each {
            product.getDiscountRates().add(it.getDiscountRate())
        }
        product.setRetailerItemId(retailerProductId)
        product.setLocal(false)

        return product
    }
}