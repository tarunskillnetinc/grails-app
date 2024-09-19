package uk.co.wonderlane.wlpos

import org.joda.time.DateTime
import uk.co.wonderlane.wlpos.enums.SegmentStatus
import uk.co.wonderlane.wlpos.enums.SegmentType

class Segment {

    Integer id
    int retailerId
    String description
    transient int count
    String name
    SegmentType type = SegmentType.SPEND
    Integer min = 0
    Integer max = 0
    SegmentStatus status = SegmentStatus.ACTIVE
    DateTime dateModified

    public Segment(Integer id, int retailerId, String name, String description, SegmentType type, Integer min, Integer max, int count, SegmentStatus status) {
        this.id = id
        this.retailerId = retailerId
        this.name = name
        this.description = description
        this.type = type
        this.min = min
        this.max = max
        this.count = count
        this.status = status
    }

    static constraints = {
        retailerId nullable: false
        description nullable: false
        name nullable: false
        type nullable: false
        min nullable: false
        max nullable: false
        status nullable: false
        dateModified nullable: true
    }

    static mapping = {
        datasources (["loyalty"])

        table "segment"
        version false

        id column: "id", sqlType: "int"
        retailerId column: "retailer_id"
        description column: "description"
        name column: "name"
        type column: "type", sqlType: "enum", enumType: 'string'
        min column: "min"
        max column: "max"
        status column: "status", sqlType: "enum", enumType: 'string'
        dateModified column: "date_modified"
    }

    String getSegmentValue() {
        String value
        switch (type) {
            case SegmentType.AGE:
                value = "Age group ${min} to ${max}"
                break
            case SegmentType.POINTS:
                value = "Loyalty Points ${min} - ${max}"
                break
            case SegmentType.SPEND:
                value = "Amount in £${min} - £${max}"
                break
        }

        return value
    }
}