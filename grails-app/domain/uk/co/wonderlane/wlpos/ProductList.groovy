package uk.co.wonderlane.wlpos

import org.joda.time.DateTime
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
    DateTime dateStarted
    DateTime dateCompleted
    String ownerUserId
    String ownerUsersName
    String description
    String reasonId
    String reasonDescription
    DateTime startDate
    DateTime endDate
    Integer orderId
    String supplierReference
    String supplierId

    static hasMany = [ productListItems: ProductListItem ]

    static transients = [ 'totalQuantity', 'totalValue', 'totalPackLines' ]

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
        orderId column: "orderId"
        supplierReference column: "supplierReference"
        supplierId column: "supplierId"
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
        orderId nullable: true
        supplierReference nullable: true
        supplierId nullable: true
    }

    def getTotalValue() {
        if (productListItems.isEmpty()) {
            return BigDecimal.ZERO.setScale(2)
        }

        return productListItems?.sum { ProductListItem productListItem ->
            if (!productListItem.packLines || productListItem.packLines.isEmpty()) {
                return BigDecimal.ZERO.setScale(2)
            }

            return productListItem?.packLines?.sum {
                it.pack?.price?.multiply(BigDecimal.valueOf(it.quantity)) ?: BigDecimal.ZERO.setScale(2)
            }
        }
    }

    def getTotalQuantity() {
        return productListItems?.sum {
            ProductListItem productListItem -> productListItem?.quantity ?: BigDecimal.ZERO.setScale(2)
        }
    }

    def getTotalPackLines() {
        ArrayList<PackLine> packLines = new ArrayList<>()
        for (int i = 0; i < productListItems.size(); i++) {
            packLines.addAll(productListItems.getAt(i)?.packLines)
        }
        return packLines
    }

    def getLabelCount() {
        return productListItems?.sum { it.quantity } ?: 0
    }
}