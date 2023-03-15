package uk.co.wonderlane.wlpos

class Location {

    int id
    int storeId
    int sku
    String aisle
    String bay
    String shelf
    String position
    String location
    int shelfCapacity
    int minimumDisplayQuantity
    int productVariantId

    static belongsTo = [productVariant: ProductVariant]

    public Location() {}

    static mapping = {
        table "location"
        version false

        id column: "id"
        storeId column: "storeId", sqlType: "smallint"
        sku column: "sku", sqlType: "bigint"
        aisle column: "aisle"
        bay column: "bay"
        shelf columm: "shelf"
        position column: "position"
        location column: "location"
        shelfCapacity column: "shelfCapacity"
        minimumDisplayQuantity column: "minimumDisplayQuantity"
        productVariantId column: "productVariantId"
    }

    static constraints = {
        storeId nullable: true
        sku nullable: true
        aisle nullable: true
        bay nullable: true
        shelf nullable: true
        position nullable: true
        location nullable: true
        shelfCapacity nullable: true
        minimumDisplayQuantity nullable: true
        productVariantId nullable: false
    }
}
