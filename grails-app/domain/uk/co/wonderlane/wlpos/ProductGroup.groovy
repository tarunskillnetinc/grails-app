package uk.co.wonderlane.wlpos

class ProductGroup {

    int id
    int retailerId
    String description
    Integer maxSellQuantity
    boolean hidden

    static hasMany = [productGroupProducts: ProductGroupProduct]

    static mapping = {
        table "productGroup"
        version false

        retailerId column: "retailerId", sqlType: "tinyint"
        description column: "`description`"
        maxSellQuantity column: "maxSellQuantity"
        hidden column: "hidden"
        productGroupProducts cascade: "all,delete-orphan"
    }

    static constraints = {
        maxSellQuantity nullable: true, min: 1, max: 999
    }

    public uk.co.wonderlane.wlpos.entities.ProductGroup getProductGroup() {
        uk.co.wonderlane.wlpos.entities.ProductGroup productGroup = new uk.co.wonderlane.wlpos.entities.ProductGroup()
        productGroup.setId(id)
        productGroup.setDescription(description)
        productGroup.setMaxSellQuantity(maxSellQuantity)

        productGroupProducts?.each {
            productGroup.getProductGroupProducts().add(it)
        }

        return productGroup
    }
}