package uk.co.wonderlane.wlpos

class EcomSupplier implements Serializable{

    int retailerId
    String name
    Boolean deleted = false
    Collection<EcomSupplierCategory> ecomSupplierCategories = new ArrayList<>()
    Collection<EcomSupplierCategoryMapping> ecomSupplierCategoryMappings = new ArrayList<>()

    static hasMany = [ecomSupplierCategories: EcomSupplierCategory, ecomSupplierCategoryMappings: EcomSupplierCategoryMapping]

    static mapping = {
        table "ecomsupplier"
        version false

        id column: "id", type: "int"
        retailerId column: "retailerId", sqlType: "tinyint"
        name column: "`name`"
        deleted column: "deleted" , sqlType: "BIT(1)"
        ecomSupplierCategories cascade: 'all-delete-orphan', key: 'ecomsupplierid'
        ecomSupplierCategoryMappings cascade: 'all-delete-orphan', key: 'ecomsupplierId'
    }


    static constraints = {
    }
}
