package uk.co.wonderlane.wlpos

import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import groovy.json.JsonSlurper
import uk.co.wonderlane.wlpos.enums.ProductAttributeType
import uk.co.wonderlane.wlpos.usertypes.BooleanTypeAdapter

import javax.inject.Inject
import java.lang.reflect.Type

class ProductAttributes {

    int id
    Integer retailerId
    ProductAttributeType type
    String name
    String defaultValue
    String listValues
    Boolean displayAttribute

    public ProductAttributes() {}

    static mapping = {

        table "productattributes"
        version false

        retailerId column: "retailerId", sqlType: "tinyint"
        type column: "type", sqlType: "enum", enumType: 'string'
        name column: "name"
        defaultValue column: "defaultValue"
        listValues column: "listValues", type: "uk.co.wonderlane.wlpos.usertypes.JsonType", sqlType: "json"
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
            } else if (val.length() > 50) {
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

    List<String> getListValues() {
        if (this.listValues == null || this.listValues.trim().isEmpty()) {
            return []
        }
        try {
            return new JsonSlurper().parseText(this.listValues) as List<String>
        } catch (Exception e) {
            log.error("Error parsing listValues JSON: ${e.message}", e)
            return []
        }
    }

    void setListValues(List<String> list) {
        if (list == null) {
            this.listValues = null
        } else {
            this.listValues = JsonOutput.toJson(list)
        }
    }
    void setListValues(String listValues) {
        this.listValues = listValues
    }
}
