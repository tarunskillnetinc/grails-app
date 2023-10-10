package uk.co.wonderlane.wlpos

import org.joda.time.DateTime

class TillConfiguration {

    int id
    int retailerId
    int storeId
    int tillId
    String serialNumber
    String description
    String scpTxnEndIndicator
    String pposControlBar
    boolean pposAdmin
    boolean pposRefund
    boolean pposSmartToken
    int pin
    DateTime pinExpiry
    DateTime dateTimeCreated
    DateTime dateTimeUpdated
    int baudRate
    boolean printCardReceipts

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
    }

    static constraints = {
        scpTxnEndIndicator maxSize: 100
        pposControlBar maxSize: 100
        serialNumber nullable: true
        pin nullable: true
    }
}