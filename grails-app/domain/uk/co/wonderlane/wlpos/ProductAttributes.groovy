package uk.co.wonderlane.wlpos

import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import uk.co.wonderlane.wlpos.entities.cashmanagement.CashManagementConfig
import uk.co.wonderlane.wlpos.enums.ProductAttributeType
import uk.co.wonderlane.wlpos.usertypes.BooleanTypeAdapter

import java.lang.reflect.Type

class ProductAttributes {

    int id
    int retailerId
    ProductAttributeType type
    String name
    String defaultValue
    String listValues
    Boolean displayAttribute

    def gson = new GsonBuilder().create()

    static mapping = {

        table "productattributes"
        version false

        retailerId column: "retailerId", sqlType: "tinyint"
        type column: "type", sqlType: "enum", enumType: 'string'
        name column: "name"
        defaultValue column: "defaultValue"
        listValues column: "listValues", sqlType: "jsonb"
        displayAttribute column: "displayAttribute"
    }

    static constraints = {
        id nullable: false
        retailerId nullable: false
        type nullable: false
        name nullable: false
        defaultValue nullable: false
        listValues nullable: true
        displayAttribute nullable: false
    }

    List<String> ListEntries() {
        if (this.listValues == null || this.listValues.trim().isEmpty()) {
            return null
        }
        Type listType = new TypeToken<List<String>>(){}.getType()
        return gson.fromJson(this.listValues, listType)
    }

/*    void setListValues(List<String> listValues) {
        if (listValues == null) {
            this.listValues = null
        } else {
            this.listValues = gson.toJson(listValues)
        }
    }

    List<String> getListValues() {
        if (this.listValues == null || this.listValues.trim().isEmpty()) {
            return null
        }
        Type listType = new TypeToken<List<String>>(){}.getType()
        return gson.fromJson(this.listValues, listType)
    }*/
}
