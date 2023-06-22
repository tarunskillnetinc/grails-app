package uk.co.wonderlane.wlpos.reporting

import org.joda.time.DateTime
import uk.co.wonderlane.wlpos.enums.PromotionType

class PromotionSale {

    int id
    int retailerId
    int storeId
    int tillId
    int promotionId
    PromotionType type
    String description
    BigDecimal fullPrice
    BigDecimal discount
    BigDecimal margin
    BigDecimal profit
    BigDecimal vat
    DateTime dateCreated
    int quantity // transient field to pass the count into the grouped level report.

    static hasMany = [ products: PromotionSaleProduct ]

    static transients = ['quantity']

    static mapping = {
        datasources (["reporting", "reportingReadOnly"])
        table "promotionsale"
        version false

        id column: "id"
        retailerId column: "retailerId", sqlType: "tinyint"
        storeId column: "storeId", sqlType: "smallint"
        tillId column: "tillId"
        promotionId column: "promotionId"
        type column: "`type`", sqlType: "enum", enumType: 'string'
        description column: "`description`"
        fullPrice column: "fullPrice"
        discount column: "discount"
        margin column: "margin"
        profit column: "profit"
        vat column: "vat"
        dateCreated column: "dateCreated"
    }

    static constraints = {
        id nullable: false
        retailerId nullable: false
        storeId nullable: false
        tillId nullable: false
        promotionId nullable: false
        type nullable: false
        description nullable: false, blank: false, maxSize: 200
        fullPrice nullable: false
        discount nullable: false
        margin nullable: false
        profit nullable: false
        vat nullable: false
    }
}
