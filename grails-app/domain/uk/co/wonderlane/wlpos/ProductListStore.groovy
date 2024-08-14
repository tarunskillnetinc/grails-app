package uk.co.wonderlane.wlpos

class ProductListStore implements Serializable{

    ProductList productList
    Store store

    static mapping = {
        table "productliststore"
        version false

        id composite: ['productList', 'store']

        productList column: "productListId", sqlType: "int"
        store column: "storeId", sqlType: "smallint"
    }

    static constraints = {
        productList nullable: false
        store nullable: false
    }
}