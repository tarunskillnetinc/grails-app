package uk.co.wonderlane.wlpos

class EcomSupplierCategoryMapping implements Serializable{

    Integer id
    Category category

    static belongsTo = [ecomSupplier: EcomSupplier, ecomSupplierCategory: EcomSupplierCategory]

    static mapping = {
        table "ecomsuppliercategorymapping"
        version false

        id column: "id", type: "int"
        ecomSupplierCategory column: "ecomSupplierCategoryId"
        category column: 'categoryId' , fetch: 'join', cascade: "none"
        ecomSupplier  column: "ecomSupplierId"
    }

    static constraints = {
        ecomSupplier nullable: false
        category nullable: false
        ecomSupplierCategory nullable: false

        // Custom validator for uniqueness
        category validator: { val, obj ->
            EcomSupplierCategoryMapping ecomSupplierCategoryMapping = EcomSupplierCategoryMapping.findByIdNotEqualAndCategoryAndEcomSupplier(obj?.id, val,  obj.ecomSupplier)
            if (ecomSupplierCategoryMapping) {
                return ['partnerCategory.ecom.partner.category.mapping.category.unique', val?.description, obj?.ecomSupplier?.name]
            }
        }

    }
}
