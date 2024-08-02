package uk.co.wonderlane.wlpos

import org.joda.time.DateTime
import uk.co.wonderlane.wlpos.enums.ImageType

class ImageRecord {

    int id
    Integer retailerId
    String guid
    String type
    Integer imageId
    String name
    String storageKey
    DateTime creationTime
    DateTime updatedTime

    static mapping = {
        autowire true
        table "`imagerecord`"
        version false

        id column: "id",  sqlType: 'INT'
        retailerId column: "retailerId", sqlType: "tinyint"
        guid column: "guid", sqlType: "char(36)"
        type column: "type", sqlType: "VARCHAR(20)"
        imageId column: "imageId", sqlType: "int"
        name column: "name", sqlType: "VARCHAR(64)"
        storageKey column: "storageKey", sqlType: "VARCHAR(64)"
        creationTime column: "creationTime", sqlType: "timestamp"
        updatedTime column: "updatedTime", sqlType: "timestamp"
    }

    static constraints = {
        retailerId blank: true, nullable: true, validator: { val, obj ->
            if (val == null) {
                return ['common.retailerId.required']
            }
        }

        guid blank: true, nullable: true, validator: { val, obj ->
            if (val == null || !isValidGuid(val)) {
                return ['imagerecord.guid.validation.error']
            }
        }

        type blank: true, nullable: true, validator: { val, obj ->
            if (val == null || val.isEmpty()) {
                return ['imagerecord.type.required']
            } else {
                boolean matched = false;
                for (ImageType imageType:ImageType.values()) {
                    if (imageType.name() == val) {
                        matched = true;
                        break;
                    }
                }
                if (!matched) {
                    return ['imagerecord.type.invalid.type']
                }
            }
        }
        storageKey blank: true, nullable: true, validator: { val, obj ->
            if (val == null || val.isEmpty()) {
                return ['imagerecord.storage.key.required']
            }
        }
        name nullable: true
    }

    def toEntity() {
        return new uk.co.wonderlane.wlpos.entities.ImageRecord(
                id: this.id,
                retailerId: this.retailerId,
                guid: this.guid,
                type: this.type,
                imageId: this.imageId,
                name: this.name,
                storageKey: this.storageKey,
                creationTime: this.creationTime,
                updatedTime: this.updatedTime
        )
    }

    static boolean isValidGuid(String guid) {
        // Regular expression pattern to match all versions of UUIDs
        def uuidRegex = /^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[1-5][0-9a-fA-F]{3}-[89abAB][0-9a-fA-F]{3}-[0-9a-fA-F]{12}$/
        return guid ==~ uuidRegex
    }
}