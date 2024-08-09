package uk.co.wonderlane.wlpos.reporting

import org.joda.time.DateTime

class CharitySale {

    int id
    int storeId
    int storeNumber
    int retailerId
    int tillId
    int transactionId
    BigDecimal basketTotal
    BigDecimal donationTotal
    DateTime dateCreated

    static mapping = {
        datasources (["reporting", "reportingReadOnly"])
        table "charitysales"
        version false

        id column : "id"
        storeId column : "storeId", sqlType: "smallint"
        storeNumber column : "storeNumber", sqlType: "smallint"
        retailerId column : "retailerId", sqlType: "tinyint"
        tillId column : "tillId"
        transactionId column : "transactionId"
        basketTotal column : "basketTotal"
        donationTotal column : "donationTotal"
        dateCreated column : "dateCreated"
    }

    static constraints = {
        id nullable: false
        storeId nullable: false
        storeNumber nullable: false
        retailerId nullable: false
        tillId nullable: false
        transactionId nullable: false
        basketTotal nullable: false
        donationTotal nullable: false
        dateCreated nullable: false
    }
}
