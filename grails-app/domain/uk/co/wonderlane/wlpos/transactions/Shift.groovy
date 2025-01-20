package uk.co.wonderlane.wlpos.transactions

import org.joda.time.DateTime

class Shift {

    def gsonProvider

    int id
    int retailerId
    int storeId
    int tillId
    int shiftNumber
    String shiftStatus
    String shiftJson
    DateTime dateCreated
    DateTime dateUpdated

    // This constructor is required or dependency injection
    public Shift() {}
    static mapping = {
        autowire true
        datasources(["transactions"])
        table "shift"
        version false

        retailerId column: "retailerId", sqlType: "tinyint"
        storeId column: "storeId", sqlType: "smallint"
        tillId column: "tillId"
        shiftNumber column: "shiftNumber"
        shiftStatus column: "shiftStatus"
        shiftJson column: "shift", type: "uk.co.wonderlane.wlpos.usertypes.JsonType", sqlType: "json"
        dateCreated column: "createDate"
        dateUpdated column: "updateDate"
    }

    static constraints = {}

    public uk.co.wonderlane.wlpos.entities.cash.Shift getShift() {
        return gsonProvider.gson.fromJson(shiftJson, uk.co.wonderlane.wlpos.entities.cash.Shift.class)
    }
}