package uk.co.wonderlane.wlpos

import grails.databinding.BindingFormat
import uk.co.wonderlane.wlpos.enums.wlim.ProductListStatus
import uk.co.wonderlane.wlpos.enums.wlim.ProductListType

class ProductList {

    int id
    String userId
    int retailerId
    int storeId
    ProductListType type
    ProductListStatus status
    Integer parentId
    Date dateStarted
    Date dateCompleted
    String ownerUserId
    String ownerUsersName
    String description
    String reasonId
    String reasonDescription
    @BindingFormat('dd/MM/yyyy')
    Date startDate
    @BindingFormat('dd/MM/yyyy')
    Date endDate

    static hasMany = [ productListItems: ProductListItem ]

    static mapping = {
        table "productlist"
        version false

        userId column: "userId"
        retailerId column: "retailerId", sqlType: "tinyint"
        storeId column: "storeId", sqlType: "smallint"
        type column: "`type`"
        status column: "`status`"
        parentId column: "parentId"
        dateStarted column: "dateStarted"
        dateCompleted column: "dateCompleted"
        ownerUserId column: "ownerUserId"
        ownerUsersName column: "ownerUsersName"
        description column: "`description`"
        reasonId column: "reasonId"
        reasonDescription column: "reasonDescription"
        startDate column: "startDate"
        endDate column: "endDate"
    }

    static constraints = {
        userId nullable: false, blank: false, maxSize: 45
        retailerId nullable: false
        storeId nullable: false
        type nullable: false
        status nullable: false
        parentId nullable: true
        dateStarted nullable: true
        dateCompleted nullable: true
        ownerUserId nullable: true, maxSize: 45
        ownerUsersName nullable: true, maxSize: 45
        description nullable: true, maxSize: 100
        reasonId nullable: true, maxSize: 45
        reasonDescription nullable: true, maxSize: 45
        startDate nullable: true
        endDate nullable: true
    }

    def getLabelCount() {
        return productListItems?.sum { it.quantity } ?: 0
    }
}