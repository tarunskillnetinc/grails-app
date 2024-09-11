package uk.co.wonderlane.wlpos

import com.google.gson.GsonBuilder
import uk.co.wonderlane.wlpos.entities.cashmanagement.CashManagementConfig
import uk.co.wonderlane.wlpos.usertypes.BooleanTypeAdapter

class CashManagement implements Serializable {
    int id
    int retailerId
    Integer storeId
    String config;

    def gson = new GsonBuilder()
            .registerTypeAdapter(boolean.class, new BooleanTypeAdapter())
            .create()

    public CashManagement() {

    }

    static transients = [ "gson" ]

    static mapping = {
        table "cashmanagement"
        version false

        retailerId column: "retailerId", sqlType: "tinyint"
        storeId column: "storeId", sqlType: "smallint"
        config column: "config", type: "uk.co.wonderlane.wlpos.usertypes.JsonType", sqlType: "json"
    }


    static constraints = {
        id nullable: false
        retailerId nullable: false
        storeId nullable: true
        config nullable: false
    }

    CashManagementConfig getConfig() {
        CashManagementConfig cashManagementConfig = gson.fromJson(config, CashManagementConfig.class)
        return cashManagementConfig
    }

    void setConfig(CashManagementConfig cashManagementConfig) {
        config = gson.toJson(cashManagementConfig)
    }
}
