package uk.co.wonderlane.wlpos

class ProductGroupProduct implements Serializable {

    def springSecurityService

    static belongsTo = [productGroupId: ProductGroup]

    long sku

    static transients = [ "productVariantId", "productId", "productDescription", "itemCode" ]

    int productVariantId
    int productId
    String productDescription
    String itemCode

    // Need this parameterless constructor or else dependency injection (SpringSecurityService) breaks.
    public ProductGroupProduct() {}

    static mapping = {
        table "productgroupproduct"
        version false

        id composite: ['productGroupId', 'sku']

        productGroupId column: "productGroupId"
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

        productGroupProduct.setProductGroupId(tag.id)
        productGroupProduct.setSku(sku)

        return productGroupProduct
    }

    @Override
    boolean equals(that) {
        if (this.is(that)) return true
        if (getClass() != that.class) return false

        ProductGroupProduct productGroupProduct = (ProductGroupProduct) that
        if (sku != productGroupProduct.sku || productVariantId != productGroupProduct.productVariantId
                || productId != productGroupProduct.productId || tag?.id != productGroupProduct.tag?.id || itemCode != productGroupProduct.itemCode) {
            return false
        }

        return true
    }

    @Override
    int hashCode() {
        return sku.hashCode() + productVariantId.hashCode() + productId.hashCode() + (tag?.id?.hashCode() ?: 123)
    }
}