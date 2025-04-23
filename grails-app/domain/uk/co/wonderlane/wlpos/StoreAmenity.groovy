package uk.co.wonderlane.wlpos

class StoreAmenity implements Serializable {

    String additionalDetail
    Integer count
    String availability
    static belongsTo = [amenity: Amenity, store: Store]

    static mapping = {
        autowire true
        table "storeamenity"
        version false

        id composite: ['amenity', 'store']
        store column: "storeId", sqlType: "smallint"
        amenity  column: "amenityId" , fetch: 'join'
        additionalDetail column: "additionalDetail", sqlType: "varchar(60)"
        count column: "count", sqlType: "int"
        availability column: "availability", sqlType: "JSON"
    }

    static constraints = {
        additionalDetail nullable: true
        availability nullable: true
    }
}
