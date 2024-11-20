package uk.co.wonderlane.wlpos

import javax.persistence.Column

class SelType {
    int id
    String name
    Integer retailerId

    static mapping = {
        table "seltype"
        id column: "id"
        name column: "name"
        retailerId column: "retailerId", sqlType: "tinyint"
        version false
    }

    static constraints = {
        name nullable: false, blank: false
        retailerId nullable: true
    }
}