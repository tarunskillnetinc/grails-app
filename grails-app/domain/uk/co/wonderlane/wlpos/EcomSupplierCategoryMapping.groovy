package uk.co.wonderlane.wlpos

class EcomSupplierCategoryMapping implements Serializable{

    static belongsTo = [ecomSupplier: EcomSupplier, ecomSupplierCategory: EcomSupplierCategory]

    static hasOne = [category: Category]

    static mapping = {
        table "ecomsuppliercategorymapping"
        version false

        ecomSupplierCategory column: "ecomsuppliercategoryid"
        category column: 'categoryid'
        ecomSupplier  column: "ecomsupplierId"
        id composite: ['ecomSupplierCategory', 'ecomSupplier', 'category']
    }

    static constraints = {
    }
}
