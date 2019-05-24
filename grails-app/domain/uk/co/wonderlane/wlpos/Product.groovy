package uk.co.wonderlane.wlpos

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
    boolean discreet
    ProductStatus status
    String retailerProductId

    Collection<ProductData> productDatas = new ArrayList<>()
    Collection<Tag> tags = new ArrayList<>()
    Collection<Barcode> barcodes = new ArrayList<>()
    Collection<Message> saleMessages = new ArrayList<>()
    Collection<Message> refundMessages = new ArrayList<>()
    Collection<DiscountRate> discountRates = new ArrayList<>()

    static hasMany = [ productDatas: ProductData, tags: Tag, barcodes: Barcode, saleMessages: Message, refundMessages: Message, discountRates: DiscountRate ]

//    static mappedBy = [ saleMessages: "saleProduct", refundMessages: "refundProduct" ]

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
        restrictions column: "restrictionsId"
        discreet column: "discreet"
        status column: "`status`", sqlType: "enum", enumType: "string"
        retailerProductId column: "retailerProductId"

        tags joinTable: [name: 'tagproduct', key: 'productId', column: 'tagId']
        saleMessages joinTable: [name: 'productmessage', key: 'productId', column: 'messageId']
        refundMessages joinTable: [name: 'productmessage', key: 'productId', column: 'messageId']
        discountRates joinTable: [name: 'productdiscount', key: 'productId', column: 'discountRateId']
    }
}