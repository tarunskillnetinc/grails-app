package uk.co.wonderlane.wlpos

import org.joda.time.DateTime
import uk.co.wonderlane.wlpos.enums.TillType

class TillConfiguration {

    def storeService

    int id
    int retailerId
    int storeId // Actually store number.
    int tillId
    String serialNumber
    TillType type
    String description
    String scpTxnEndIndicator
    String pposControlBar
    boolean pposAdmin
    boolean pposRefund
    boolean pposSmartToken
    Integer pin
    DateTime pinExpiry
    DateTime dateTimeCreated
    DateTime dateTimeUpdated
    int baudRate
    boolean printCardReceipts
    boolean cashManagementEnabled

    TillConfiguration() { }

    static mapping = {
        autowire true
        table "tillconfiguration"
        version false

        id column: "id"
        retailerId column: "retailerId", sqlType: "tinyint"
        storeId column: "storeId", sqlType: "smallint"
        tillId column: "tillId"
        serialNumber column: "serialNumber"
        type column: "type", sqlType: "text", enumType: "string"
        description column: "description"
        scpTxnEndIndicator column: "scpTxnEndIndicator"
        pposControlBar column: "pposControlBar"
        pposAdmin column: "pposAdmin"
        pposRefund column: "pposRefund"
        pposSmartToken column: "pposSmartToken"
        pin column: "pin"
        pinExpiry column: "pinExpiry", sqlType: "datetime"
        dateTimeCreated column: "dateTimeCreated", sqlType: "datetime"
        dateTimeUpdated column: "dateTimeUpdated", sqlType: "datetime"
        baudRate column: "baudRate"
        printCardReceipts column: "printCardReceipts"
        cashManagementEnabled column: "cashManagementEnabled", sqlType: "BIT(1)"
    }

    static constraints = {
        scpTxnEndIndicator maxSize: 100
        pposControlBar maxSize: 100
        serialNumber nullable: true
        pin nullable: true
        description nullable: true
        cashManagementEnabled: nullable: false
    }

    Store getStore() {
        return storeService.getStoreByStoreNumber(retailerId, storeId)
    }
}