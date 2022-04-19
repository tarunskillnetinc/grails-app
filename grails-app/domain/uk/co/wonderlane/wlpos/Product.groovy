package uk.co.wonderlane.wlpos

import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import uk.co.wonderlane.wlpos.enums.ProductStatus

class Product {

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

    ProductVariant currentProductVariant

    static hasMany = [ saleMessages: Message, refundMessages: Message, discountRates: DiscountRate, variants: ProductVariant ]

    static transients = ['currentProductVariant']

    static mapping = {
        table "product"
        version false

        retailerId column: "retailerId", sqlType: "tinyint"
        itemCode column: "itemCode"
        description column: "`description`"
        receiptDescription column: "receiptDescription"
        category column: "categoryId"
        unitSize column: "unitSize"
        pricePerKg column: "pricePerKg"
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
        itemCode size: 1..50, blank: false, nullable: false
        description size: 1..100, blank: false, nullable: false
        receiptDescription size: 1..50, blank: false, nullable: false
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