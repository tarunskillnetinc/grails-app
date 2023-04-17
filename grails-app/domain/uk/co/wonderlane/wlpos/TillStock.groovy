package uk.co.wonderlane.wlpos

import org.joda.time.DateTime

class TillStock {
    Integer id
    String serialNumber
    String model
    Integer retailerId
    Integer storeId
    DateTime dateUpdated

    // This constructor is required or dependency injection (springSecurityService) breaks. Don't forget "autowire true" in the mappings as well.
    public TillStock() { }

    static mapping = {
        autowire true
        table "tillstock"
        version false

        serialNumber column: "serialNumber"
        model column: "model"
        retailerId column: "retailerId", sqlType: "int"
        storeId column: "storeId", sqlType: "int"
        dateUpdated column: "dateUpdated"
    }

    static constraints = {
        serialNumber size:1..50, blank:false, nullable: false
        model size:1..50, blank: false, nullable: false
        dateUpdated nullable: false
        storeId nullable: true
        retailerId nullabe: true
    }
}
