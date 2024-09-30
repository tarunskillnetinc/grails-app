package uk.co.wonderlane.wlpos.transactions

import org.joda.time.DateTime

class Shift {

    int id
    String shift
    int retailerId
    int storeId
    int tillId
    int financialWeekId
    int shiftNumber
    String shiftStatus
    DateTime updateDate
    DateTime createDate

    static mapping = {
        datasources(["transactions"])

        table "shift"
        version false

        id column: "id"
        shift column: "shift", type: "uk.co.wonderlane.wlpos.usertypes.JsonType", sqlType: "json"
        retailerId column: "retailerId" , sqlType: "tinyint"
        storeId column: "storeId", sqlType: "smallint"
        tillId column: "tillId"
        financialWeekId column: "financialWeekId"
        shiftNumber column: "shiftNumber"
        shiftStatus column: "shiftStatus" , sqlType: "enum", enumType: 'string'
        updateDate column: "updateDate"
        createDate column: "createDate"
    }

    static constraints = {
        id nullable: false
    }

}
