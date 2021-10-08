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

    public uk.co.wonderlane.wlpos.entities.TagProduct getTagProduct() {
        uk.co.wonderlane.wlpos.entities.TagProduct tagProduct = new uk.co.wonderlane.wlpos.entities.TagProduct()

        tagProduct.setTagId(tag.id)
        tagProduct.setSku(sku)

        return tagProduct
    }
}