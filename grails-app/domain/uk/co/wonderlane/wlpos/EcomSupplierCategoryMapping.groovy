package uk.co.wonderlane.wlpos

class EcomSupplierCategoryMapping implements Serializable{

    static belongsTo = [ecomSupplier: EcomSupplier, ecomSupplierCategory: EcomSupplierCategory]

    static hasOne = [category: Category]

    static mapping = {
        table "ecomsuppliercategorymapping"
        version false

        ecomSupplierCategory column: "ecomSupplierCategoryId"
        category column: 'categoryId'
        ecomSupplier  column: "ecomSupplierId"
        id composite: ['ecomSupplierCategory', 'ecomSupplier', 'category']
    }

    static constraints = {
    }
}
