package uk.co.wonderlane.wlpos

import uk.co.wonderlane.wlpos.enums.ProductAttributeType

class ProductAttributeValues implements Serializable {

    static belongsTo = [product: Product]

    Integer id
    Integer retailerId
    Integer storeId
    long sku
    Integer productAttributeId
    String value
    ProductAttributes productAttributes
    String attributeName
    ProductAttributeType attributeType
    String listValues

    static transients = ['productAttributes', 'attributeName', 'attributeType', 'listValues']

    static mapping = {
        autowire true
        table "productattributevalues"
        version false

        retailerId column: "retailerId", sqlType: "tinyint"
        storeId column: "storeId", sqlType: "smallint"
        sku column: "sku"
        productAttributeId column: "productAttributeId"
        value column: "value"
        product column: "productId"
    }

    static constraints = {

        retailerId nullable: false , validator: { val, obj ->
            if (val == null) {
                return ['productAttributeValues.retailerId.empty', [obj?.attributeName]]
            }
        }

        productId nullable: false , validator: { val, obj ->
            if (val == null) {
                return ['productAttributeValues.productId.empty', [obj?.attributeName]]
            }
        }

        sku nullable: false , validator: { val, obj ->
            if (val == null) {
                return ['productAttributeValues.sku.empty', [obj?.attributeName]]
            }
        }

        productAttributeId nullable: false , validator: { val, obj ->
            if (val == null) {
                return ['productAttributeValues.attributeId.empty', [obj?.attributeName]]
            }
        }

        value nullable: true, validator: {val, obj ->
            if (obj?.attributeType == ProductAttributeType.NUMERIC && val != null) {
                try {
                    // Try parsing the value as a BigDecimal
                    BigDecimal numericValue = new BigDecimal(val)

                    // Check if the value exceeds the maximum allowed value
                    if (numericValue.compareTo(BigDecimal.ZERO) < 0 || numericValue.compareTo(new BigDecimal("999999.99")) > 0) {
                        return ['productAttributeValues.numeric.default.out.of.range', [obj?.attributeName]]
                    }
                } catch (Exception e) {
                    // If the value is not a valid number, return the appropriate error message
                    return ['productAttributeValues.numeric.default.not.a.number', [obj?.attributeName]]
                }
            } else if (obj?.attributeType == ProductAttributeType.TEXT && val != null){
                if (val.length() > 50) {
                    return ['productAttributeValues.text.max.size', [obj?.attributeName]]
                }
            }
        }

        storeId nullable: true
        product nullable: true
        productAttributes bindable: true
        attributeName bindable: true
        attributeType bindable: true
        listValues bindable: true
    }
}
