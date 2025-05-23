package uk.co.wonderlane.wlpos

import org.joda.time.DateTime

import java.util.UUID
import java.util.Date

class Job {

    UUID uuid
    String type
    Integer retailerId
    String status
    DateTime dateCreated
    Integer productListId
    Integer storeId

    static constraints = {
        type maxSize: 20
        retailerId nullable: false
        status maxSize: 10
        productListId nullable: true
        storeId nullable: true
    }

    static mapping = {
        table "job"
        id name: 'uuid', column: 'uuid', generator: 'assigned', type: 'uuid-binary'

        version false

        type column: 'type'
        retailerId column: 'retailerId', sqlType: 'tinyint'
        status column: 'status'
        dateCreated column: 'dateCreated'
        productListId column: 'productListId'
        storeId column: 'storeId', sqlType: 'smallint'
    }
}
