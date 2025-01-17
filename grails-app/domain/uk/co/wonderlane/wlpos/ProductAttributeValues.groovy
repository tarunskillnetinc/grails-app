package uk.co.wonderlane.wlpos

import uk.co.wonderlane.wlpos.enums.ProductAttributeType

class ProductAttributeValues implements Serializable {

    static belongsTo = [product: Product]

    Integer id
    Integer retailerId
    Integer productId
    Integer productAttributeId
    String value
    ProductAttributes productAttributes
    String attributeName
    ProductAttributeType attributeType

    static transients = ['productAttributes', 'attributeName', 'attributeType']

    static mapping = {
        autowire true
        table "productattributevalues"
        version false

        productId column: "productId"
        id composite: ['retailerId', 'productId', 'productAttributeId']
        retailerId column: "retailerId", sqlType: "tinyint"
        productAttributeId column: "productAttributeId"
        value column: "value"
        product column: "productId", insertable: false, updateable: false
    }

    static constraints = {

        retailerId nullable: false , validator: { val, obj ->
            if (val == null) {
                return ['productAttributeValues.retailerId.empty', obj.productAttributes.name]
            }
        }

        productId nullable: false , validator: { val, obj ->
            if (val == null) {
                return ['productAttributeValues.productId.empty', obj.productAttributes.name]
            }
        }

        productAttributeId nullable: false , validator: { val, obj ->
            if (val == null) {
                return ['productAttributeValues.attributeId.empty', obj.productAttributes.name]
            }
        }

        value nullable: true, validator: {val, obj ->
            if (val == null) {
                return ['productAttributeValues.numeric.default.out.of.range', obj?.attributeName]
            } else if (obj?.attributeType == ProductAttributeType.NUMERIC && val != null) {
                try {
                    // Try parsing the value as a BigDecimal
                    BigDecimal numericValue = new BigDecimal(val)

                    // Check if the value exceeds the maximum allowed value
                    if (numericValue.compareTo(999999.99) > 0) {
                        return ['productAttributeValues.numeric.default.out.of.range', obj?.attributeName]
                    }
                } catch (Exception e) {
                    // If the value is not a valid number, return the appropriate error message
                    return ['productAttributeValues.numeric.default.not.a.number', obj?.attributeName]
                }
            } else if (obj?.attributeType == ProductAttributeType.TEXT && val != null){
                if (val.length() > 50) {
                    return ['productAttributeValues.numeric.default.not.a.number', obj?.attributeName]
                }
            }
        }

        product nullable: true
        productAttributes bindable: true
        attributeName bindable: true
        attributeType bindable: true
    }
}
