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
        ecomSupplier  column: "ecomSupplierId"
        ecomSupplierCategoryMappings cascade: 'save-update,delete', key: 'ecomSupplierCategoryId'
    }

    static constraints = {
    }


    public uk.co.wonderlane.wlpos.entities.EcomSupplierCategory getEcomSupplierCategory(){
        uk.co.wonderlane.wlpos.entities.EcomSupplierCategory ecomSupplierCategory = new uk.co.wonderlane.wlpos.entities.EcomSupplierCategory()
        ecomSupplierCategory.setId(id)
        ecomSupplierCategory.setRetailerId(retailerId)
        ecomSupplierCategory.setDescription(description)
        ecomSupplierCategory.setDeleted(deleted)
        return ecomSupplierCategory
    }
}
