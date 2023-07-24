package uk.co.wonderlane.wlpos

import com.google.gson.GsonBuilder
import uk.co.wonderlane.wlpos.entities.RetailerConfig
import uk.co.wonderlane.wlpos.usertypes.BooleanTypeAdapter

class Retailer implements Serializable {

    def gson = new GsonBuilder().registerTypeAdapter(boolean.class, new BooleanTypeAdapter()).create()

    int id
    String name
    String config

    static transients = [ "gson" ]

    static mapping = {
        table "retailers"
        version false

        id column: "id", sqlType: "tinyint"
        name column: "`name`"
        config column: "`config`", type: "uk.co.wonderlane.wlpos.usertypes.JsonType", sqlType: "json"
    }

    static constraints = {
        id nullable: false
        name nullable: false
        config nullable: false
    }

    RetailerConfig getConfig() {
        return gson.fromJson(config, RetailerConfig.class)
    }

    void setConfig(RetailerConfig retailerConfig) {
        config = gson.toJson(retailerConfig)
    }
}