package uk.co.wonderlane.wlpos

import org.joda.time.DateTime
import uk.co.wonderlane.wlpos.enums.wlim.ProductListStatus
import uk.co.wonderlane.wlpos.enums.wlim.ProductListType

class ProductList {

    int id
    String userId
    int retailerId
    Integer storeId
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
    String supplierId
    String supplierReference
    boolean stockAdjustedOnCompletion
    Integer destinationStoreId
    List<ProductListItem> productListItems = new ArrayList<>()

    static hasMany = [ productListItems: ProductListItem ]

    static transients = [ 'totalQuantity', 'totalValue', 'totalPackLines', 'totalCost', 'productListItems' ]

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
        supplierId column: "supplierId"
        supplierReference column: "supplierReference"
        stockAdjustedOnCompletion column: "stockAdjustedOnCompletion"
        destinationStoreId column: "destinationStoreId", sqlType: "smallint"
    }

    static constraints = {
        userId nullable: false, blank: false, maxSize: 45
        retailerId nullable: false
        storeId nullable: true
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
        supplierId nullable: true
        supplierReference nullable: true
        stockAdjustedOnCompletion nullable: false
        destinationStoreId nullable: true
    }

    def getTotalValue() {
        if (productListItems.isEmpty()) {
            return BigDecimal.ZERO.setScale(2)
        }

        return productListItems?.sum { ProductListItem productListItem ->
            if (!productListItem.packLines || productListItem.packLines.isEmpty()) {
                return BigDecimal.ZERO.setScale(2)
            }

            if (type == ProductListType.DELIVERY) {
                return productListItems?.sum {
                    it.totalValue
                }
            } else {
                return productListItem?.packLines?.sum {
                    //If pack line is singles then get cost price for product variant
                    BigDecimal price = it?.productListItem?.productVariant?.costPrice ?: BigDecimal.ZERO
                    if (it.pack){ //If pack exists mean pack line is non singles
                        price = it.pack?.price ?: BigDecimal.ZERO
                    }
                    price.multiply(it.quantity) ?: BigDecimal.ZERO.setScale(2)
                }
            }
        }
    }

    def getTotalQuantity() {
        return productListItems?.sum { ProductListItem productListItem ->
            if (type == ProductListType.DELIVERY) {
                //If product list item has quantity then only consider it if not consider fill quantity
                if (productListItem?.quantity){
                    productListItem?.quantity ?: BigDecimal.ZERO.setScale(2)
                } else {
                    productListItem?.fillQuantity ?: BigDecimal.ZERO.setScale(2)
                }
            } else {
                productListItem?.quantity ?: BigDecimal.ZERO.setScale(2)
            }
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

    def getTotalCost() {
        return productListItems?.sum {
            it.getTotalCost()
        }
    }

}