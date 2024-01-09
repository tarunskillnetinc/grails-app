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
    int locationHierarchy
    String locationDescription
    String locationNumber

    boolean delete
    String locationsType

    static transients = ['delete', 'locationsType']

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
        locationHierarchy column: "locationHierarchy"
        locationDescription column: "locationDescription"
        locationNumber column: "locationNumber"
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
        locationHierarchy nullable: false
        locationDescription size: 0..40, blank: true, nullable: true
        locationNumber size: 0..8, blank: true, nullable: true
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
        loc.setLocationHierarchy(locationHierarchy)

        return loc
    }

}
