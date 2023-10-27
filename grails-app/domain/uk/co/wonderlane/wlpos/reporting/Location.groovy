package uk.co.wonderlane.wlpos.reporting

import uk.co.wonderlane.wlpos.enums.LocationType

class Location {

    int id
    int retailerId
    int storeId
    Integer tillId
    Integer safeId
    String description
    LocationType type

    static belongsTo = [TenderMovement]

    static mapping = {
        datasources(["reporting"])

        table "location"
        version false

        id column: "id"
        retailerId column: "retailerId", sqlType: "tinyint"
        storeId column: "storeId", sqlType:"smallint"
        tillId column: "tillId"
        safeId column: "safeId"
        description column: "description"
        type column: "`type`", sqlType: "enum", enumType: 'string'
    }

    static constraints = {
        id nullable: false
        retailerId nullable: false
        storeId nullable: false
        tillId nullable: true
        safeId nullable: true
        description nullable: false, maxSize: 45
        type nullable: false
    }
}
