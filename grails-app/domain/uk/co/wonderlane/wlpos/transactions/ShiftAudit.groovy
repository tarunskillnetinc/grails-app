package uk.co.wonderlane.wlpos.transactions

import com.google.gson.reflect.TypeToken
import org.apache.commons.lang3.StringUtils
import org.joda.time.DateTime
import uk.co.wonderlane.wlpos.entities.cash.TenderTotal

import java.lang.reflect.Type

class ShiftAudit {

    def gsonProvider

    int id
    int shiftId
    String action
    Boolean backoffice
    int userId
    String username
    String usersRealName
    DateTime timestamp
    String shiftJson
    String tenderMovementJson
    Integer safeId
    String safeDescription

    // This constructor is required or dependency injection
    public ShiftAudit() {}
    static mapping = {
        autowire true
        datasources(["transactions"])
        table "shiftAudit"
        version false

        shiftId column: "shiftId"
        action column: "action"
        backoffice column: "backoffice"
        userId column: "userId"
        username column: "username"
        usersRealName column: "usersRealName"
        timestamp column: "timestamp"
        shiftJson column: "extras", type: "uk.co.wonderlane.wlpos.usertypes.JsonType", sqlType: "json"
        tenderMovementJson column: "tenderMovementValue", type: "uk.co.wonderlane.wlpos.usertypes.JsonType", sqlType: "json"
        safeId column: "safeId"
        safeDescription column: "safeDescription"
    }

    static constraints = {}

    public uk.co.wonderlane.wlpos.entities.cash.Shift getShiftValues() {
        if (StringUtils.isNotBlank(shiftJson)) {
            return gsonProvider.gson.fromJson(shiftJson, uk.co.wonderlane.wlpos.entities.cash.Shift.class)
        }
        return null
    }

    public List<TenderTotal> getTenderMovementValues() {
        if (StringUtils.isNotBlank(tenderMovementJson)) {
            Type listType = new TypeToken<ArrayList<TenderTotal>>() {}.getType()
            return gsonProvider.gson.fromJson(tenderMovementJson, listType)
        }
        return null
    }
}