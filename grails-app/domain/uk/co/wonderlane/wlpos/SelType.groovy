package uk.co.wonderlane.wlpos

class SelType {
    int id
    String name

    static mapping = {
        table "seltype"
        id column: "id"
        name column: "name"
        version false
    }

    static constraints = {
        name nullable: false, blank: false, unique: true
    }
}