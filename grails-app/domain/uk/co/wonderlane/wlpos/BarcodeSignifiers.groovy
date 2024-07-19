package uk.co.wonderlane.wlpos

class BarcodeSignifiers {

    int id
    int retailerId
    String pattern
    Integer startIndex
    Integer length
    String type

    static mapping = {
        table 'barcodesignifiers'
        version false

        id column: "id" , sqlType: "smallint"
        retailerId column: "retailerId"  , sqlType: "tinyint"
        pattern column: "pattern"
        startIndex column: "startIndex"
        length column: "length"
        type column: "type"

    }

    static constraints = {
        retailerId nullable: false
        pattern nullable: false
        startIndex nullable: true
        length nullable: true
        type nullable: false
    }
}
