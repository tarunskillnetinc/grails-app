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

    boolean delete

    static transients = ['delete']

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
    }

    static constraints = {
        storeId nullable: false
        sku nullable: false
        aisle nullable: true
        bay nullable: true
        shelf nullable: true
        position nullable: true
        location nullable: true
        shelfCapacity nullable: false
        minimumDisplayQuantity nullable: false
    }

    public uk.co.wonderlane.wlpos.entities.Location getCommonLocation() {
        uk.co.wonderlane.wlpos.entities.Location loc = new uk.co.wonderlane.wlpos.entities.Location()

        loc.setId(id)
        loc.setStoreId(storeId)
        loc.setSku(sku)
        loc.setAisle(aisle)
        loc.setBay(bay)
        loc.setShelf(shelf)
        loc.setPosition(position)
        loc.setLocation(location)
        loc.setShelfCapacity(shelfCapacity)
        loc.setMinimumDisplayQuantity(minimumDisplayQuantity)

        return loc
    }

}
