package uk.co.wonderlane.wlpos

class TagProduct implements Serializable {

    static belongsTo = [ tag: Tag ]

    long sku

    static transients = [ "productVariantId", "productDescription" ]

    int productVariantId
    String productDescription

    static mapping = {
        table "tagproduct"
        version false

        id composite: ['tag', 'sku']

        tag column: "tagId"
        sku column: "sku"
    }
}