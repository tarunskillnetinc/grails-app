package uk.co.wonderlane.wlpos

import org.joda.time.DateTime
import uk.co.wonderlane.wlpos.enums.ProductStatus

class Product {

    int id
    int retailerId
    String itemCode
    String description
    String receiptDescription
    Category category
    boolean dumpCode
    String unitSize
    boolean weightedItem
    boolean openPrice
    boolean zeroPrice
    VatCode vatCode
    BigDecimal vatPercentageOverride
    Restrictions restrictions
    String discreetMessage
    ProductStatus status
    String retailerProductId

    Collection<ProductData> productDatas = new ArrayList<>()
    Collection<Tag> tags = new ArrayList<>()
    Collection<Message> saleMessages = new ArrayList<>()
    Collection<Message> refundMessages = new ArrayList<>()
    Collection<DiscountRate> discountRates = new ArrayList<>()
    Collection<ProductVariant> variants = new ArrayList<>()

    ProductData currentProductData

    static hasMany = [ productDatas: ProductData, tags: Tag, saleMessages: Message, refundMessages: Message, discountRates: DiscountRate, variants: ProductVariant ]

//    static mappedBy = [ saleMessages: "saleProduct", refundMessages: "refundProduct" ]

    static transients = ['currentProductData']

    static mapping = {
        table "product"
        version false

        retailerId column: "retailerId"
        itemCode column: "itemCode"
        description column: "`description`"
        receiptDescription column: "receiptDescription"
        category column: "categoryId"
        dumpCode column: "dumpCode"
        unitSize column: "unitSize"
        weightedItem column: "weightedItem"
        openPrice column: "openPrice"
        zeroPrice column: "zeroPrice"
        vatCode column: "vatCodeId"
        vatPercentageOverride column: "vatPercentageOverride"
        restrictions column: "restrictionsId", cascade: "save-update"
        discreetMessage column: "discreetMessage"
        status column: "`status`", sqlType: "enum", enumType: "string"
        retailerProductId column: "retailerProductId"
        productDatas cascade: "delete"
        variants cascade: "all-delete-orphan"

        tags joinTable: [name: 'tagproduct', key: 'productId', column: 'tagId']
        saleMessages joinTable: [name: 'productmessage', key: 'productId', column: 'messageId']
        refundMessages joinTable: [name: 'productmessage', key: 'productId', column: 'messageId']
        discountRates joinTable: [name: 'productdiscount', key: 'productId', column: 'discountRateId']
    }

    static constraints = {
        itemCode blank: false, nullable: false
        description blank: false, nullable: false
        receiptDescription blank: false, nullable: false
        discreetMessage blank: true, nullable: true
        vatCode nullable: false
        status nullable: false
        category nullable: false
        variants minSize: 1
    }

    public uk.co.wonderlane.wlpos.entities.Product getProduct(Integer storeId) {
        uk.co.wonderlane.wlpos.entities.Product product = new uk.co.wonderlane.wlpos.entities.Product()
        ProductData productData = productDatas.sort { it.effectiveDate }.reverse().find { it.storeId == storeId && it.effectiveDate <= new Date() }

        product.setId(id)
        product.setRetailerId(retailerId)
        product.setStoreId(storeId)
        product.setItemCode(itemCode)
        product.setDescription(description)
        product.setReceiptDescription(receiptDescription)
        product.setCategory(category.getCategory())
        product.setDumpCode(false)
        product.setUnitSize(unitSize)
        product.setWeightedItem(weightedItem)
        product.setOpenPrice(openPrice)
        product.setZeroPrice(zeroPrice)
        product.setVatCode(vatCode.getVatCode())
        product.setVatPercentageOverride(vatPercentageOverride)
        product.setRetailPrice(productData.retailPrice)
        product.setCostPrice(productData.costPrice)
        product.setRestrictions(restrictions.getRestrictions())
        product.setDiscreetMessage(discreetMessage)
        product.setEffectiveDate(new DateTime(productData.effectiveDate))
        product.setStatus(status)
        tags.each {
            product.getTags().add(it.getTag())
        }
        variants.each {
            if (it.storeId == storeId) {
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