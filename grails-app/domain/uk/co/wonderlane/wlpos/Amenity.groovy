package uk.co.wonderlane.wlpos

class Amenity {

    int id
    int retailerId
    String name

    static mapping = {
        autowire true
        table "amenity"
        version false

        id column: "id", sqlType: "int"
        retailerId column: "retailerId", sqlType: "tinyint"
        name column: "name", sqlType: "varchar(40)"
    }

    static constraints = {}
}
