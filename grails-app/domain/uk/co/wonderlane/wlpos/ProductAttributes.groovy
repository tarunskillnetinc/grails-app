package uk.co.wonderlane.wlpos

import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import uk.co.wonderlane.wlpos.entities.cashmanagement.CashManagementConfig
import uk.co.wonderlane.wlpos.enums.ProductAttributeType
import uk.co.wonderlane.wlpos.usertypes.BooleanTypeAdapter

import java.lang.reflect.Type

class ProductAttributes {

    int id
    Integer retailerId
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
        retailerId validator: { val, obj ->
            if (val == null) {
                return ['productAttribute.retailerId.empty']
            }
        }
        type validator: { val, obj ->
            if (val == null) {
                return ['productAttribute.type.empty']
            }
        }
        name validator: { val, obj ->
            if (val == null || val.isEmpty() || val.isBlank()) {
                return ['productAttribute.name.empty']
            } else if (val.length() > 50 ) {
                return ['productattributes.name.charLength']
            }
        }
        defaultValue nullable: true
        listValues nullable: true
        displayAttribute validator: { val, obj ->
            if (val == null) {
                return ['productAttribute.displayAttribute.empty']
            }
        }
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
