package uk.co.wonderlane.wlpos


import groovy.json.JsonSlurper
import uk.co.wonderlane.wlpos.enums.ProductAttributeType

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
            } else if (ProductAttributes.findByNameAndRetailerId(val, obj.retailerId) ) {
                return ['productAttribute.name.not.unique']
            } else {
                def allowedCharactersRegex= /^[a-zA-Z0-9 \\\\/.,()\-]*$/
                if (!(val ==~ allowedCharactersRegex)) {
                    return ['productAttribute.name.invalid.characters']
                }
            }
        }

        defaultValue nullable: true, validator: {val, obj ->
            if (obj.type == ProductAttributeType.NUMERIC && val != null) {
                if (!val.isNumber()) {
                    return ['productAttribute.numeric.default.not.a.number']
                } else if (val.toLong() > 999999999) {
                    return ['productAttribute.numeric.default.out.of.range']
                }
            }
        }

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
            List<String> results = new JsonSlurper().parseText(this.listValues) as List<String>
            Collections.sort(results, String.CASE_INSENSITIVE_ORDER);
            return results;
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
