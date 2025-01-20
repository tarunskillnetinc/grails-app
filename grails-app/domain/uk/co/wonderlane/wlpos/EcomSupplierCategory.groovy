package uk.co.wonderlane.wlpos

class EcomSupplierCategory implements Serializable{

    static belongsTo = [ecomSupplier: EcomSupplier]

    Integer id
    Integer retailerId
    String description
    Boolean deleted = false
    Collection<EcomSupplierCategoryMapping> ecomSupplierCategoryMappings = new ArrayList<>()

    static hasMany = [ecomSupplierCategoryMappings: EcomSupplierCategoryMapping]

    static mapping = {
        table "ecomsuppliercategory"
        version false

        id column: "id", type: "int"
        retailerId column: "retailerId", sqlType: "tinyint"
        description column: "description"
        deleted column: "deleted" , sqlType: "BIT(1)"
        ecomSupplier  column: "ecomsupplierid"
        ecomSupplierCategoryMappings cascade: 'all-delete-orphan', key: 'ecomsuppliercategoryid'
    }

    static constraints = {
    }
}
