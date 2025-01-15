package uk.co.wonderlane.wlpos

class ProductGroupProduct implements Serializable {

    def springSecurityService

    static belongsTo = [tag: ProductGroup]

    long sku

    static transients = [ "productVariantId", "productId", "productDescription", "itemCode" ]

    int productVariantId
    int productId
    String productDescription
    String itemCode

    // Need this parameterless constructor or else dependency injection (SpringSecurityService) breaks.
    public ProductGroupProduct() {}

    static mapping = {
        table "tagproduct" // TODO: After the database refactor
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

    public uk.co.wonderlane.wlpos.entities.ProductGroupProduct getProductGroupProduct() {
        uk.co.wonderlane.wlpos.entities.ProductGroupProduct productGroupProduct = new uk.co.wonderlane.wlpos.entities.ProductGroupProduct()

        productGroupProduct.setTagId(tag.id)
        productGroupProduct.setSku(sku)

        return productGroupProduct
    }

    @Override
    boolean equals(that) {
        if (this.is(that)) return true
        if (getClass() != that.class) return false

        ProductGroupProduct tagProduct = (ProductGroupProduct) that
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