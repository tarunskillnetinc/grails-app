package uk.co.wonderlane.wlpos

import org.joda.time.DateTime
import uk.co.wonderlane.wlpos.enums.wlim.ProductListStatus
import uk.co.wonderlane.wlpos.enums.wlim.ProductListType

class ProductList {

    int id
    String userId
    int retailerId
    Store store
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

    Collection<ProductListItem> productListItems = new ArrayList<>()

    static hasMany = [ productListItems: ProductListItem ]

    static transients = [ 'totalQuantity', 'totalValue', 'totalPackLines', 'totalCost']

    static mapping = {
        table "productlist"
        version false

        userId column: "userId"
        retailerId column: "retailerId", sqlType: "tinyint"
        store column: "storeId", sqlType: "smallint"
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
        store nullable: true
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
                return productListItems?.sum {
                    it.totalCost
                }
            }
        } ?: BigDecimal.ZERO.setScale(2)
    }

    def getTotalQuantity() {
        return (productListItems?.sum { ProductListItem productListItem ->
            if (isWeightedItem(productListItem)) {
                BigDecimal.ONE
            } else if (type == ProductListType.DELIVERY) {
                // If product list item has quantity then only consider it if not consider fill quantity
                if (productListItem?.quantity){
                    productListItem?.quantity ?: BigDecimal.ZERO.setScale(2)
                } else {
                    productListItem?.fillQuantity ?: BigDecimal.ZERO.setScale(2)
                }
            } else {
                productListItem?.quantity ?: BigDecimal.ZERO.setScale(2)
            }
        } as BigDecimal ?: BigDecimal.ZERO).intValue()
    }

    def getTotalPackLines() {
        def packLines = []

        productListItems.each { item ->
            BigDecimal totalQuantity = item.quantity ?: BigDecimal.ZERO

            item?.packLines?.each { packLine ->
                packLines.add(packLine)

                totalQuantity = totalQuantity.subtract(packLines.totalQuantity)
            }

            if (totalQuantity.compareTo(0) > 0) {
                // Singles involved, add a dummy pack line.
                def dummyPack = [quantity: BigDecimal.ONE, price: item?.productVariant?.costPrice]

                packLines.add([productListId: item.productListId, quantity: totalQuantity, productListItem: item, pack: dummyPack])
            }
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

    private static def isWeightedItem(ProductListItem item) {
        return item?.getProductVariant()?.getProduct()?.isWeightedItem()
    }

    boolean equals(that) {
        if (this.is(that)) {
            return true
        }

        if (getClass() != that.class) {
            return false
        }

        ProductList productList = (ProductList)that

        if (id != productList.id) {
            return false
        }
        if (userId != productList.userId) {
            return false
        }
        if (retailerId != productList.retailerId) {
            return false
        }
        if (store?.id != productList.store?.id) {
            return false
        }
        if (type != productList.type) {
            return false
        }
        if (status != productList.status) {
            return false
        }

        return true
    }

    int hashCode() {
        return id.hashCode()
    }

    public uk.co.wonderlane.wlpos.entities.wlim.ProductList getProductList(PriceBand priceBand, Integer storeId) {
        uk.co.wonderlane.wlpos.entities.wlim.ProductList productList = new uk.co.wonderlane.wlpos.entities.wlim.ProductList()

        productList.setId(id)
        productList.setStoreId(store?.id?.toString() ?: null)
        productList.setUserId(userId)
        productList.setType(type)
        productList.setStatus(status)
        productList.setParentId(parentId ?: 0)
        productList.setParentType(null) // TODO

        // TODO ProductListItemGroups not yet implemented in CO?
//        productListItemGroups?.each {
//            productList.getProductListItemGroups().add(it.getProductListItemGroup())
//        }

        productListItems?.each {
            productList.getProductListItems().add(it.getProductListItem(priceBand, storeId))
        }

        productList.setDateStarted(dateStarted)
        productList.setDateCompleted(dateCompleted)
        productList.setOwnerUserId(ownerUserId)
        productList.setOwnerUsersName(ownerUsersName)
        productList.setDescription(description)
        productList.setReasonId(reasonId)
        productList.setReasonDescription(reasonDescription)
        productList.setSupplierId(Integer.parseInt(supplierId))
        productList.setSupplierReference(supplierReference)
        productList.setStockAdjustedOnCompletion(stockAdjustedOnCompletion)
        productList.setStartDate(startDate)
        productList.setEndDate(endDate)
        productList.setDestinationStore(destinationStoreId)
        productList.setOrderId(orderId ?: 0)

        // TODO Following items are not yet added to the CO ProductList object.
//        productList.setShipmentReference(shipmentReference)
//        productList.setRetailerListId(retailerListId)
//        productList.setProductListItemCount(productListItemCount)

        return productList
    }
}