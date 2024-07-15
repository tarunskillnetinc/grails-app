package uk.co.wonderlane.wlpos

class RangeProduct implements Serializable {

    Range range
    int productId
    boolean deleted

    static mapping = {
        table "rangeproduct"
        version false

        id composite: ['range', 'productId']

        productId column: "productId"
        range column: "rangeId"
        deleted column: "deleted"
    }

    static constraints = {
        productId nullable: false
        range nullable: false
        deleted nullable: false
    }

    @Override
    boolean equals(that) {
        if (this.is(that)) return true
        if (getClass() != that.class) return false

        RangeProduct rangeProduct = (RangeProduct)that
        if (range?.id != rangeProduct.range?.id || productId != rangeProduct.productId) {
            return false
        }

        return true
    }

    @Override
    int hashCode() {
        return (range?.id?.hashCode() ?: 123) + productId.hashCode()
    }

    static HashMap<Integer, RangeProduct> getExistingProductRanges(Integer productId) {
        return findAllByProductId(productId).collectEntries{[it.rangeId, it]} as HashMap<Integer, RangeProduct>
    }
}