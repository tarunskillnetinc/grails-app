package uk.co.wonderlane.wlpos

class TagProduct implements Serializable {

    def springSecurityService

    static belongsTo = [ tag: Tag ]

    long sku

    static transients = [ "productVariantId", "productId", "productDescription" ]

    int productVariantId
    int productId
    String productDescription

    // Need this parameterless constructor or else dependency injection (SpringSecurityService) breaks.
    public TagProduct() { }

    static mapping = {
        table "tagproduct"
        version false

        id composite: ['tag', 'sku']

        tag column: "tagId"
        sku column: "sku"
    }

    int getProductId() {
        if (productDescription == null) {
            setValues()
        }

        return productId
    }

    String getProductDescription() {
        if (productDescription == null) {
            setValues()
        }

        return productDescription
    }

    private void setValues() {
        // TODO For some reason springSecurityService is null, need to add the storeId check to the ProductVariant.findBySkuAndStoreId....
        ProductVariant productVariant = ProductVariant.findBySku(sku)

        productId = productVariant?.product?.id
        productDescription = productVariant?.product?.receiptDescription
    }

    public uk.co.wonderlane.wlpos.entities.TagProduct getTagProduct() {
        uk.co.wonderlane.wlpos.entities.TagProduct tagProduct = new uk.co.wonderlane.wlpos.entities.TagProduct()

        tagProduct.setTagId(tag.id)
        tagProduct.setSku(sku)

        return tagProduct
    }
}