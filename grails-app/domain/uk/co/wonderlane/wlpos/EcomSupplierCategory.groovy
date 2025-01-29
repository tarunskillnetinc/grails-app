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
        ecomSupplier  column: "ecomSupplierId" , fetch: 'join'
        ecomSupplierCategoryMappings cascade: 'all-delete-orphan', key: 'ecomSupplierCategoryId'
    }

    static constraints = {
        ecomSupplier nullable: false, validator: { val, obj ->
            if (!val) {
                return ['partnerCategory.ecom.supplier.not.nullable']
            }
        }
        retailerId nullable: false, validator: { val, obj ->
            if (!val && val > 0) {
                return ['partnerCategory.ecom.partner.category.valid.retailer']
            }
        }
        description nullable: false, validator: { val, obj ->
            if (!val || val.trim().isEmpty()) {
                return ['partnerCategory.ecom.partner.category.not.nullable']
            }

            def existingCategory = EcomSupplierCategory.createCriteria().get { // check any description there for same name
                eq('retailerId', obj.retailerId)
                eq('ecomSupplier', obj.ecomSupplier)
                eq('deleted', false)
                eq('description', val.trim())
                if (obj.id) {
                    ne('id', obj.id) // Exclude the current object from the uniqueness check
                }
            }
            if (existingCategory) {
                return ['partnerCategory.ecom.partner.category.description.unique', val.trim(), obj?.ecomSupplier?.name]
            }
        }
    }

    def getMappedCategories(){
        def partnerCategoryList = []
        // Loop through all mappings
        ecomSupplierCategoryMappings?.each { mapping ->
            def category = mapping.category
            while (category) {
                if (!partnerCategoryList.contains(category.id)) {// Add the current category ID to the list if not already added
                    partnerCategoryList << category.id
                }
                category = category.parentCategory // Move to the parent category
            }
        }
        return partnerCategoryList
    }


    uk.co.wonderlane.wlpos.entities.EcomSupplierCategory getEcomSupplierCategory(){
        uk.co.wonderlane.wlpos.entities.EcomSupplierCategory ecomSupplierCategory = new uk.co.wonderlane.wlpos.entities.EcomSupplierCategory()
        ecomSupplierCategory.setId(id)
        ecomSupplierCategory.setRetailerId(retailerId)
        ecomSupplierCategory.setDescription(description)
        ecomSupplierCategory.setDeleted(deleted)
        return ecomSupplierCategory
    }
}
