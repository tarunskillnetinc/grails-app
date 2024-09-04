package uk.co.wonderlane.wlpos

import uk.co.wonderlane.wlpos.enums.SegmentStatus
import uk.co.wonderlane.wlpos.enums.SegmentType

class Segment {

    Integer id
    int retailerId
    String description
    int count
    String name
    SegmentType type = SegmentType.SPEND
    BigDecimal min = BigDecimal.ZERO
    BigDecimal max = BigDecimal.ZERO
    SegmentStatus status = SegmentStatus.ACTIVE

    static constraints = {
    }

    static mapping = {
        datasources (["loyalty"])

        table "segment"
        version false

        id column: "id", sqlType: "int"
        retailerId column: "retailer_id"
        description column: "description"
        count column: "count"
        name column: "name"
        type column: "type", sqlType: "enum", enumType: 'string'
        min column: "min"
        max column: "max"
        status column: "status", sqlType: "enum", enumType: 'string'
    }
}
