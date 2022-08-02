package uk.co.wonderlane.wlpos

import org.joda.time.DateTime
import uk.co.wonderlane.wlpos.enums.ProductHistoryType

class ProductHistory {

    Integer id
    Integer productId
    Integer retailerId
    Integer storeId
    DateTime updateDate
    DateTime effectiveDate
    ProductHistoryType productHistoryType
    Integer priceBandId
    String field
    String fromValue
    String toValue
    Integer userId
    String usersName
    Integer productVariantId

    static mapping = {
        table "producthistory"
        version false

        productId column: "productId", sqlType: "smallint"
        retailerId column: "retailerId", sqlType: "tinyint"
        storeId column: "storeId", sqlType: "smallint"
        updateDate column: "updateDate"
        effectiveDate column: "effectiveDate"
        productHistoryType column: "type", sqlType: "enum", enumType: "string"
        priceBandId column: "priceBandId"
        field column: "field"
        fromValue column: "fromValue"
        toValue column: "toValue"
        userId column: "userId"
        usersName column: "usersName"
        productVariantId column: "productVariantId"
    }

    static constraints = {
        priceBandId nullable: true
        storeId nullable: true
        field nullable: true, maxSize: 45
        fromValue nullable: true, maxSize: 100
        toValue nullable: true, maxSize: 100
        userId nullable: true
        usersName nullable: true, maxSize: 45
        productVariantId nullable: true
    }
}