package uk.co.wonderlane.wlpos

class Segment {


    Integer id
    int retailerId
    String description
    String segmentSql
    int count

    static constraints = {
    }

    static mapping = {
        datasources (["loyalty"])

        table "segment"
        version false

        id column: "id", sqlType: "int"
        retailerId column: "retailer_id"
        description column: "description"
        segmentSql column: "segment_sql"
        count column: "count"
    }
}
