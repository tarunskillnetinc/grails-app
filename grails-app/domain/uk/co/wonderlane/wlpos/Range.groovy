package uk.co.wonderlane.wlpos

class Range {

    int id
    int retailerId
    String description

    static mapping = {
        table "`range`"
        version false

        retailerId column: "retailerId", sqlType: "tinyint"
        description column: "description"
    }

    static constraints = {
        retailerId nullable: false
        description size: 1..100, blank: false, nullable: false
    }
}