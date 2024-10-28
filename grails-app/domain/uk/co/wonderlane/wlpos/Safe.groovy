package uk.co.wonderlane.wlpos

import grails.persistence.Entity
import org.joda.time.DateTime
import uk.co.wonderlane.wlpos.enums.SafeType

@Entity
class Safe {

    int id
    int storeId
    int retailerId
    Boolean primary
    String description
    SafeType type
    Boolean active
    DateTime dateCreated
    DateTime dateModified

    static constraints = {
        storeId nullable: false, validator: { val, obj ->
            if (val == null) {
                return ['safe.storeId.empty']
            }
        }
        retailerId nullable: false, validator: { val, obj ->
            if (val == null) {
                return ['safe.retailerId.empty']
            }
        }
        primary nullable: false , validator: { val, obj ->
            if (val == null) {
                return ['safe.primary.empty']
            }
        }
        description nullable: false, validator: { val, obj ->
            if (val == null || val.trim().isEmpty()) {
                return ['safe.description.empty']
            } else if (val != null && val.length() > 45) {
                return ['safe.description.charLength']
            } else if (withCriteria(uniqueResult: true) {
                eq('retailerId', obj.retailerId)
                eq('storeId', obj.storeId)
                eq('description', val)
                if (obj.id) {
                    ne('id', obj.id)
                }
            }) {
                return ['safe.description.unique', obj.retailerId, obj.storeId]
            }
        }
        type nullable: false , validator: { val, obj ->
            if (val == null) {
                return ['safe.type.empty']
            }
        }
        active nullable: false, validator: { val, obj ->
            if (val == null) {
                return ['safe.status.empty']
            }
        }
        dateCreated nullable: false, validator: { val, obj ->
            if (val == null) {
                return ['safe.date.create.empty']
            }
        }
        dateModified nullable: false, validator: { val, obj ->
            if (val == null) {
                return ['safe.modify.create.empty']
            }
        }
    }

    static mapping = {
        table "safe"
        version false

        storeId column: 'storeId', sqlType: 'smallint unsigned'
        retailerId column: 'retailerId', sqlType: 'tinyint unsigned'
        primary column: '`primary`', sqlType: 'bit(1)'
        type column: 'type', sqlType: "enum", enumType: 'string'
        active column: 'active', sqlType: 'bit(1)'
        dateCreated column: 'dateCreated'
        dateModified column: 'dateModified'
    }

    public uk.co.wonderlane.wlpos.entities.Safe getSafe() {
        uk.co.wonderlane.wlpos.entities.Safe safe = new uk.co.wonderlane.wlpos.entities.Safe()

        safe.setId(id)
        safe.setStoreId(storeId)
        safe.setRetailerId(retailerId)
        safe.setPrimary(primary)
        safe.setDescription(description)
        safe.setSafeType(type)
        safe.setActive(active)

        return safe
    }
}
