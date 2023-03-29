package uk.co.wonderlane.wlpos

class Location {

    int id
    int storeId
    long sku
    String aisle
    String bay
    String shelf
    String position
    String location
    int shelfCapacity
    int minimumDisplayQuantity
//    int productVariantId

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
        productVariant column: "productVariantId"
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
        productVariant nullable: true
    }

    public uk.co.wonderlane.wlpos.entities.Location getLocation1() {
        uk.co.wonderlane.wlpos.entities.Location location1 = new uk.co.wonderlane.wlpos.entities.Location()

        location1.setId(id)
        location1.setProductVariantId(productVariant?.id)
        location1.setStoreId(storeId)
        location1.setSku(sku)
        location1.setAisle(aisle)
        location1.setBay(bay)
        location1.setShelf(shelf)
        location1.setPosition(position)
        location1.setLocation(location)
        location1.setShelfCapacity(shelfCapacity)
        location1.setMinimumDisplayQuantity(minimumDisplayQuantity)

        return location1
    }

}
