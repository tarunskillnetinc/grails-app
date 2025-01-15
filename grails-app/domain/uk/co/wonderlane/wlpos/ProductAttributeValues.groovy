package uk.co.wonderlane.wlpos

class ProductAttributeValues implements Serializable {

    Integer retailerId
    Integer productId
    Integer productAttributeId
    String value
    ProductAttributes productAttributes

    static belongsTo = [productAttributes: ProductAttributes, product: Product]

    static mapping = {
        autowire true
        table "productattributevalues"
        version false

        id composite: ['retailerId', 'productId', 'productAttributeId']
        retailerId column: "retailerId", sqlType: "tinyint"
        productId column: "productId"
        productAttributeId column: "productAttributeId"
        value column: "value"
        productAttributes insertable: false, updateable: false, column: "productAttributeId"
        product column: "productId"
    }

    static constraints = {
    }
}
