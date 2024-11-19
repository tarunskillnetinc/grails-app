package uk.co.wonderlane.wlpos

class SelType {
    int id
    String name
    int retailerId

    static mapping = {
        table "seltype"
        id column: "id"
        name column: "name"
        retailerId column: "retailerId"
        version false
    }

    static constraints = {
        name nullable: false, blank: false
        retailerId nullable: true
    }
}