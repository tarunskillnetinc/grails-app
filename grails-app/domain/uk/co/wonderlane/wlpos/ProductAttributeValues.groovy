package uk.co.wonderlane.wlpos

class ProductAttributeValues implements Serializable {

    static belongsTo = [product: Product]

    Integer id
    Integer retailerId
    Integer productId
    Integer productAttributeId
    String value
    ProductAttributes productAttributes

    static transients = ['productAttributes']

    public ProductAttributeValues() {}

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
        retailerId nullable: false
        productAttributeId nullable: false
        value nullable: false
        product nullable: true
        productAttributes bindable: true
    }
}
