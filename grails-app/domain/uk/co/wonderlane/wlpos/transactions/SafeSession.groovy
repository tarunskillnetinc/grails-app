package uk.co.wonderlane.wlpos.transactions

import org.joda.time.DateTime

class SafeSession {

    def gsonProvider

    int id
    int retailerId
    int storeId
    int safeId
    int sessionNumber
    String sessionStatus
    String sessionJson
    String versionId
    DateTime dateCreated
    DateTime dateUpdated

    // This constructor is required or dependency injection
    public SafeSession() {}
    static mapping = {
        autowire true
        datasources(["transactions"])
        table "safesession"
        version false

        retailerId column: "retailerId", sqlType: "tinyint"
        storeId column: "storeId", sqlType: "smallint"
        safeId column: "safeId"
        sessionNumber column: "sessionNumber"
        sessionStatus column: "sessionStatus"
        sessionJson column: "session", type: "uk.co.wonderlane.wlpos.usertypes.JsonType", sqlType: "json"
        versionId column: "versionId"
        dateCreated column: "createDate"
        dateUpdated column: "updateDate"
    }

    static constraints = {}

    public uk.co.wonderlane.wlpos.entities.cash.SafeSession getSafeSession() {
        return gsonProvider.gson.fromJson(sessionJson, uk.co.wonderlane.wlpos.entities.cash.SafeSession.class)
    }
}