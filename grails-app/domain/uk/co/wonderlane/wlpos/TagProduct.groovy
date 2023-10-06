package uk.co.wonderlane.wlpos

class TagProduct implements Serializable {

    def springSecurityService

    static belongsTo = [ tag: Tag ]

    long sku

    static transients = [ "productVariantId", "productId", "productDescription", "itemCode" ]

    int productVariantId
    int productId
    String productDescription
    String itemCode

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

    String getItemCode() {
        if(itemCode == null) {
            setValues()
        }

        return itemCode
    }

    private void setValues() {
        ProductVariant productVariant = ProductVariant.findById(productVariantId)

        productId = productVariant?.product?.id
        productDescription = productVariant?.product?.receiptDescription
        itemCode = productVariant?.product?.itemCode
    }

    public uk.co.wonderlane.wlpos.entities.TagProduct getTagProduct() {
        uk.co.wonderlane.wlpos.entities.TagProduct tagProduct = new uk.co.wonderlane.wlpos.entities.TagProduct()

        tagProduct.setTagId(tag.id)
        tagProduct.setSku(sku)

        return tagProduct
    }

    @Override
    boolean equals(that) {
        if (this.is(that)) return true
        if (getClass() != that.class) return false

        TagProduct tagProduct = (TagProduct)that
        if (sku != tagProduct.sku || productVariantId != tagProduct.productVariantId
                || productId != tagProduct.productId || tag?.id != tagProduct.tag?.id || itemCode != tagProduct.itemCode) {
            return false
        }

        return true
    }

    @Override
    int hashCode() {
        return sku.hashCode() + productVariantId.hashCode() + productId.hashCode() + (tag?.id?.hashCode() ?: 123)
    }
}