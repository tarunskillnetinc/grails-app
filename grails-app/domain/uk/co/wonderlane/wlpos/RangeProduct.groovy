package uk.co.wonderlane.wlpos

class RangeProduct implements Serializable {

    Range range
    int productId

    static mapping = {
        table "rangeproduct"
        version false

        id composite: ['range', 'productId']

        productId column: "productId"
        range column: "rangeId"
    }

    static constraints = {
        productId nullable: false
        range nullable: false
    }
}