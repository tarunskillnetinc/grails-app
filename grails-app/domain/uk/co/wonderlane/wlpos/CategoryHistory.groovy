package uk.co.wonderlane.wlpos

import org.joda.time.DateTime
import uk.co.wonderlane.wlpos.enums.CategoryHistoryType

class CategoryHistory {

    int id
    int retailerId
    int categoryId
    Integer storeId
    DateTime updateDate
    DateTime effectiveDate
    CategoryHistoryType type
    String field
    String fromValue
    String toValue
    Integer userId
    String usersName
    
    static mapping = {
        table "categoryhistory"
        version false
        
        retailerId column: "retailerId", sqlType: "tinyint"
        categoryId column: "categoryId"
        storeId column: "storeId", sqlType: "smallint"
        updateDate column: "updateDate"
        effectiveDate column: "effectiveDate"
        type column: "type", sqlType: "text", enumType: "string"
        field column: "field"
        fromValue column: "fromValue"
        toValue column: "toValue"
        userId column: "userId"
        usersName column: "usersName"
    }
    
    static constraints = {
        retailerId nullable: false
        categoryId nullable: false
        storeId nullable: true
        updateDate nullable: false
        effectiveDate nullable: false
        type  nullable: false
        field nullable: true, maxSize: 45
        fromValue nullable: true, maxSize: 100
        toValue nullable: true, maxSize: 100
        userId nullable: true
        usersName nullable: true, maxSize: 45
    }
}
