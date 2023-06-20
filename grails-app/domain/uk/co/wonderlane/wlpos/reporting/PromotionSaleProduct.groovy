package uk.co.wonderlane.wlpos.reporting

class PromotionSaleProduct {

    int id
    int productId
    String itemCode
    String description
    BigDecimal costPrice
    BigDecimal fullPrice
    BigDecimal fullPriceMargin
    BigDecimal fullPriceProfit
    BigDecimal discount
    BigDecimal discountedMargin
    BigDecimal discountedProfit
    BigDecimal vat
    BigDecimal discountedPrice // Transient calculated value.

    static belongsTo = [ promotion: PromotionSale ]

    static transients = ['discountedPrice']

    static mapping = {
        datasources (["reporting", "reportingReadOnly"])
        table "promotionsaleproduct"
        version false

        id column: "id"
        promotion column: "promotionSaleId"
        productId column: "productId"
        itemCode column: "itemCode"
        description column: "description"
        costPrice column: "costPrice"
        fullPrice column: "fullPrice"
        fullPriceMargin column: "fullPriceMargin"
        fullPriceProfit column: "fullPriceProfit"
        discount column: "discount"
        discountedMargin column: "discountedMargin"
        discountedProfit column: "discountedProfit"
        vat column: "vat"
    }

    static constraints = {
        id nullable: false
        promotionSaleId nullable: false
        productId nullable: false
        itemCode nullable: false, blank: false, maxSize: 50
        description nullable: false, blank: false, maxSize: 100
        costPrice nullable: false
        fullPrice nullable: false
        fullPriceMargin nullable: false
        fullPriceProfit nullable: false
        discount nullable: false
        discountedMargin nullable: false
        discountedProfit nullable: false
        vat nullable: false
    }

    BigDecimal getDiscountedPrice() {
        return fullPrice.subtract(discount)
    }
}
