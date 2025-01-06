package uk.co.wonderlane.wlpos.reporting

import org.joda.time.DateTime
import uk.co.wonderlane.wlpos.Store
import uk.co.wonderlane.wlpos.enums.TenderMovementType

class TenderMovement {

    int id
    int retailerId
    int storeId
    TenderMovementType type
    String reason
    String reasonOther
    Integer tenderTypeId
    String tenderTypeName
    Location fromLocation
    Location toLocation
    BigDecimal amount
    Integer userId
    String userName
    DateTime timestamp
    String bankName
    String bankReference
    DateTime bankingDate  // Changed from String to DateTime
    String comment

    static transients = ['store']

    static mapping = {
        datasources(["reporting"])

        table "tendermovement"
        version false

        id column: "id"
        retailerId column: "retailerId", sqlType: "tinyint"
        storeId column: "storeId", sqlType: "smallint"
        type column: "type"
        reason column: "reason"
        reasonOther column: "reasonOther"
        tenderTypeId column: "tenderTypeId"
        tenderTypeName column: "tenderTypeName"
        fromLocation column: "fromLocation", cascade: "evict"
        toLocation column: "toLocation", cascade: "evict"
        amount column: "amount"
        userId column: "userId"
        userName column: "userName"
        timestamp column: "timestamp"
        bankName column: "bankName"
        bankReference column: "bankReference"
        bankingDate column: "bankingDate"  // Added mapping for bankingDate
        comment column: "comment"
    }

    static constraints = {
        id nullable: true
        retailerId nullable: false
        storeId nullable: false
        type nullable: false, blank: false, maxSize: 45
        reason nullable: true, maxSize: 50
        reasonOther nullable: true, maxSize: 200
        tenderTypeId nullable: false
        tenderTypeName nullable: false, blank: false, maxSize: 24
        fromLocation nullable: true
        toLocation nullable: true
        amount nullable: false
        userId nullable: false
        userName nullable: false, blank: false, maxSize: 45
        timestamp nullable: true
        bankName nullable: true
        bankReference nullable: true
        bankingDate nullable: true  // Added constraint for bankingDate
        comment nullable: true
    }

    Store getStore() {
        return Store.findById(storeId)
    }
}