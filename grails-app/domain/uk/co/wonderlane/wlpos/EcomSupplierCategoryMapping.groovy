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
    }
}
