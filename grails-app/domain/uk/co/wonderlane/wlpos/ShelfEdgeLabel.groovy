package uk.co.wonderlane.wlpos

class ShelfEdgeLabel {

    int id
    int storeId
    int printStatus

    static belongsTo = [ productHistory: ProductHistory ]

    static mapping = {
        table "shelfedgelabel"
        version false

        id column: "id"
        storeId column: "storeId", sqlType: "smallint"
        printStatus column: "printStatus", sqlType: "tinyint"
        productHistory column: "productHistoryId"
    }

    static constraints = {
        id nullable: true
        storeId nullable: false
        printStatus nullable: false
    }
}