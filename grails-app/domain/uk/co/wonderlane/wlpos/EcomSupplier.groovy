package uk.co.wonderlane.wlpos

class EcomSupplier implements Serializable{

    Integer id
    Integer retailerId
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
        ecomSupplierCategoryMappings cascade: 'save-update,delete', key: 'ecomsupplierId'
    }


    static constraints = {
    }

    public uk.co.wonderlane.wlpos.entities.EcomSupplier getEcomSupplier(){
        uk.co.wonderlane.wlpos.entities.EcomSupplier ecomSupplier = new uk.co.wonderlane.wlpos.entities.EcomSupplier()
        ecomSupplier.setId(id)
        ecomSupplier.setRetailerId(retailerId)
        ecomSupplier.setName(name)
        ecomSupplier.setDeleted(deleted)
        return ecomSupplier
    }
}
