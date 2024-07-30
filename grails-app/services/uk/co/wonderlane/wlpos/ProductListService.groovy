package uk.co.wonderlane.wlpos

import grails.gorm.transactions.Transactional
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import org.joda.time.format.DateTimeFormat
import org.joda.time.format.DateTimeFormatter
import uk.co.wonderlane.wlpos.enums.ProductHistoryType
import uk.co.wonderlane.wlpos.dataaccess.DatabaseCredentials
import uk.co.wonderlane.wlpos.dataaccess.MySqlDal
import uk.co.wonderlane.wlpos.enums.TransactionType
import uk.co.wonderlane.wlpos.enums.wlim.ProductListStatus
import uk.co.wonderlane.wlpos.enums.wlim.ProductListType
import uk.co.wonderlane.wlpos.requests.clientexport.ProductListStockTransaction
import uk.co.wonderlane.wlpos.requests.clientexport.StockTransaction
import uk.co.wonderlane.wlpos.supplier.Supplier

import java.sql.CallableStatement
import java.sql.Connection
import java.sql.ResultSet
import java.sql.SQLException
import java.sql.Types

@Transactional
class ProductListService extends MySqlDal {

    def springSecurityService
    def storeService
    def supplierService
    def productService
    def userService
    def rabbitService
    def gsonProvider

    ProductListService(DatabaseCredentials databaseCredentials) {
        super(databaseCredentials)
    }

    def getCentralCounts(String searchTerm = null, String searchBy = null, int offset = 0, int max = 50, String sort = "startDate", String order = "DESC") {
        return ProductList.createCriteria().list([offset: offset, max: max, sort: sort, order: order]) {
            eq("retailerId", springSecurityService.principal.retailerId)
            eq("type", ProductListType.SCHEDULED_COUNT)

            if (searchBy == "Everything" && searchTerm) {
                or {
                    like("description", "%$searchTerm%")
                    def matchingEnums = searchStatuses(searchTerm)

                    if (!matchingEnums.isEmpty()) {
                        matchingEnums.each { matchingEnum ->
                            eq("status", ProductListStatus.valueOf(matchingEnum.toString()))
                        }
                    }

                    if (searchTerm == "N/A") {
                        isNull("ownerUsersName")
                    } else {
                        like("ownerUsersName", "%$searchTerm%")
                    }
                }
            } else if (searchBy == "Description" && searchTerm) {
                like("description", "%$searchTerm%")
            } else if (searchBy == "Status" && searchTerm) {
                or {
                    def matchingEnums = searchStatuses(searchTerm)

                    if (matchingEnums.isEmpty()) {
                        eq("status", null)
                        return
                    }

                    matchingEnums.each { matchingEnum ->
                        eq("status", ProductListStatus.valueOf(matchingEnum.toString()))
                    }
                }
            } else if (searchBy == "Current Owner" && searchTerm) {
                if (searchTerm == "N/A") {
                    isNull("ownerUsersName")
                } else {
                    like("ownerUsersName", "%$searchTerm%")
                }
            }
        }
    }

    private static def searchStatuses(String searchTerm) {
        def statuses =[]
        ProductListStatus.values().each {status ->
            if (status.getFriendlyName().toString().toLowerCase().contains(searchTerm.toLowerCase())){
                statuses.add(status)
            }
        }

        return statuses
    }

    def getOrders(Integer storeId, Integer supplierId, DateTime startDate, DateTime endDate) {
        return getOrder(null, storeId, supplierId, startDate, endDate)
    }

    def getOrder(Integer productListId, Integer storeId, Integer supplierId, DateTime startDate, DateTime endDate, int offset = 0, int max = 50, String sort = "dateStarted", String order = "DESC") {
        return ProductList.createCriteria().list([offset: offset, max: max, sort: sort, order: order]) {
            eq("retailerId", springSecurityService.principal.retailerId)
            eq("type", ProductListType.ORDER)

            if (productListId != null) {
                eq("id", productListId)
            }

            if (storeId != null) {
                store {
                    eq("id", storeId)
                }

            }

            if (supplierId != null) {
                eq("supplierId", String.valueOf(supplierId))
            }

            between("dateStarted", startDate, endDate)
        }
    }

    def getDeliveries(Integer storeId, Integer supplierId, DateTime startDate, DateTime endDate, int offset = 0, int max = 50, String sort = "dateStarted", String order = "DESC") {
        return ProductList.createCriteria().list([offset: offset, max: max, sort: sort, order: order]) {
            eq("retailerId", springSecurityService.principal.retailerId)
            eq("type", ProductListType.DELIVERY)

            if (storeId != null) {
                store {
                    eq("id", storeId)
                }
            }

            if (supplierId != null) {
                eq("supplierId", String.valueOf(supplierId))
            }

            or {
                // TODO CORE-1449 will address whether we use this or dateCreated or expectedDate.
                between("dateStarted", startDate, endDate)
                isNull ("dateStarted")
            }
        }
    }

    def getProductLists(Integer storeId, ProductListType type, DateTime startDate, DateTime endDate, int offset = 0, int max = 50, String sort = "dateStarted", String order = "DESC") {
        return ProductList.createCriteria().list([offset: offset, max: max, sort: sort, order: order]) {
            eq("retailerId", springSecurityService.principal.retailerId)

            if (storeId != null) {
                store {
                    eq("id", storeId)
                }
            }

            if (type != null) {
                eq("type", type)
            }

            or {
                between("dateStarted", startDate, endDate)
                isNull ("dateStarted")
            }
        }
    }

    def acceptDelivery(Integer productListId) {
        ProductList productList = getProductList(productListId)

        if (productList) {
            productList.productListItems?.each { it.quantity = it.quantity ?: it.fillQuantity }

            productList.status = productList.stockAdjustedOnCompletion ? ProductListStatus.COMPLETE : ProductListStatus.PARTIALLY_COMPLETE
            productList.dateStarted = productList.dateStarted ?: DateTime.now(DateTimeZone.UTC)
            productList.dateCompleted = DateTime.now(DateTimeZone.UTC)

            productList.save(deepValidate: false, flush: true) // deepValidate = false so it won't go through and validate every ProductVariant in every ProductListLine etc.
        }
    }

    def getAdHocBatches() {
        return ProductList.createCriteria().list([sort: "dateStarted", order: "DESC"]) {
            eq("retailerId", springSecurityService.principal.retailerId)
            store {
                eq("id", springSecurityService.principal.storeId)
            }
            "in"("type", [ProductListType.AD_HOC_SEL_BATCH, ProductListType.PRICE_CHECK])
            eq("status", ProductListStatus.PARTIALLY_COMPLETE)
        }
    }

    def getScheduledBatches(DateTime effectiveDate) {
        def results = [toBePrinted: [], toBeConfirmed: [], totalCount: 0]

        Connection conn = getConnection()
        CallableStatement cstmt = conn.prepareCall("{ call getScheduledChanges(?, ?, ?, ?, ?) }")

        try {
            // Call once for to be printed changes.
            cstmt.setInt(1, springSecurityService.principal.retailerId)

            if (springSecurityService.principal.storeId) {
                cstmt.setInt(2, springSecurityService.principal.storeId)
            } else {
                cstmt.setNull(2, Types.INTEGER)
            }

            cstmt.setNull(3, Types.TINYINT)

            if (effectiveDate) {
                cstmt.setString(4, effectiveDate.withTimeAtStartOfDay().toString(DATE_TIME_FORMAT))
                cstmt.setString(5, effectiveDate.plusDays(1).withTimeAtStartOfDay().toString(DATE_TIME_FORMAT))
            } else {
                cstmt.setNull(4, Types.VARCHAR)
                cstmt.setNull(5, Types.VARCHAR)
            }

            ResultSet rs = cstmt.executeQuery()

            DateTimeFormatter dateFormatter = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss").withZone(DateTimeZone.UTC)

            try {
                while (rs.next()) {
                    results.toBePrinted.add(mapProductHistoryResult(rs, dateFormatter))
                }
            } finally {
                rs.close()
            }

            // Call again for changes to be OKd if this is a two stage retailer.
            if (springSecurityService.principal.retailer.config.twoStageSel) {
                cstmt.clearParameters()
                cstmt.setInt(1, springSecurityService.principal.retailerId)
                cstmt.setInt(2, springSecurityService.principal.storeId)
                cstmt.setInt(3, 1)

                if (effectiveDate) {
                    cstmt.setString(4, effectiveDate.withTimeAtStartOfDay().toString(DATE_TIME_FORMAT))
                    cstmt.setString(5, effectiveDate.plusDays(1).withTimeAtStartOfDay().toString(DATE_TIME_FORMAT))
                } else {
                    cstmt.setNull(4, Types.VARCHAR)
                    cstmt.setNull(5, Types.VARCHAR)
                }

                rs = cstmt.executeQuery()

                try {
                    while (rs.next()) {
                        results.toBeConfirmed.add(mapProductHistoryResult(rs, dateFormatter))
                    }
                } finally {
                    rs.close()
                }
            }
        } finally {
            cstmt.close()
            conn.close()
        }

        return results
    }

    def mapProductHistoryResult(ResultSet rs, DateTimeFormatter dateFormatter) {
        def result = [:]

        result.id = rs.getInt("id")
        result.productId = rs.getInt("productId")
        result.storeId = rs.getInt("storeId")
        result.updateDate = DateTime.parse(rs.getString("updateDate"), dateFormatter)
        result.effectiveDate = DateTime.parse(rs.getString("effectiveDate"), dateFormatter)
        result.type = ProductHistoryType.valueOf(rs.getString("type"))
        result.priceBandId = rs.getInt("priceBandId")
        result.field = rs.getString("field")
        result.fromValue = rs.getString("fromValue")
        result.toValue = rs.getString("toValue")
        result.userId = rs.getInt("userId")
        result.usersName = rs.getString("usersName")
        result.productVariantId = rs.getInt("productVariantId")

        return result
    }

    def getProductList(int id) {
        return ProductList.findByIdAndRetailerId(id, springSecurityService.principal.retailerId)
    }

    def getProductList(int id, int retailerId) {
        return ProductList.findByIdAndRetailerId(id, retailerId)
    }

    def getProductListItem(int productListItemId) {
        return ProductListItem.findById(productListItemId)
    }

    def saveProductList(ProductList productList) {
        productList.save()
    }

    def saveProductLists(List<ProductList> productListArray) {
        if (productListArray.size() == 0) {
            return
        }

        Connection connection

        try {
            connection = getConnection()
            connection.setAutoCommit(false)

            for (ProductList productList : productListArray) {
                productList.save()
            }

            connection.commit()
        } catch (Exception ex) {
            log.error("save productLists failed, Exception " + ex.getMessage())
            if (connection != null) {
                connection.rollback()
            }
            throw ex
        } finally {
            if (connection != null) {
                connection.close()
            }
        }
    }

    def deleteProductList(ProductList productList) {
        if (productList) {
            productList.delete()
        }
    }

    uk.co.wonderlane.wlpos.entities.wlim.ProductList getProductListById(int productListId) throws SQLException {
        uk.co.wonderlane.wlpos.entities.wlim.ProductList productList = null
        HashMap<Integer, uk.co.wonderlane.wlpos.entities.wlim.ProductListItem> productListItemHashMap = new HashMap<>()
        Map<Integer, uk.co.wonderlane.wlpos.entities.wlim.ProductListItemGroup> productListItemGroupMap = new HashMap<>()
        Connection conn
        CallableStatement cstmt
        try {
            conn = getConnection()
            cstmt = conn.prepareCall("{ call getProductList(?) }")
            cstmt.setInt(1, productListId)
            if (cstmt.execute()) {
                ResultSet rs = cstmt.getResultSet()

                while (rs.next()) {
                    productList = mapProductList(rs)
                }

                cstmt.getMoreResults()
                rs = cstmt.getResultSet()

                // Item groups (cages).
                while (rs.next()) {
                    uk.co.wonderlane.wlpos.entities.wlim.ProductListItemGroup productListItemGroup = mapProductListItemGroup(rs)
                    productList.getProductListItemGroups().add(productListItemGroup)
                    productListItemGroupMap.put(productListItemGroup.getId(), productListItemGroup)
                }

                cstmt.getMoreResults()
                rs = cstmt.getResultSet()

                while (rs.next()) {
                    int itemProductListId = rs.getInt("productListId")
                    int productListItemGroupId = rs.getInt("productListItemGroupId")
                    int itemKey = productListItemGroupId > 0 ? productListItemGroupId : itemProductListId

                    if (!productListItemHashMap.containsKey(itemKey)) {
                        productListItemHashMap.put(itemKey, new HashMap<>())
                    }

                    mapProductListItem(rs, productListItemHashMap.get(itemKey))

                    if (productListItemGroupId > 0 && productListItemGroupMap.get(productListItemGroupId) != null) {
                        productListItemGroupMap.get(productListItemGroupId).setProductListItems(new ArrayList<>(productListItemHashMap.get(itemKey).values()))
                    } else {
                        productList.setProductListItems(new ArrayList<>(productListItemHashMap.get(itemKey).values()))
                    }
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace()
            log.error("Order create exception found when loading product list by id , Exception " + ex.getMessage())
            throw ex
        } finally {
            if (connection != null) {
                connection.close()
            }
        }
        return productList
    }

    uk.co.wonderlane.wlpos.entities.wlim.ProductList mapProductList(ResultSet rs) throws SQLException {
        uk.co.wonderlane.wlpos.entities.wlim.ProductList productList = new uk.co.wonderlane.wlpos.entities.wlim.ProductList()
        productList.setOrderId(rs.getInt("orderId") != 0 ? rs.getInt("orderId") : 0)
        productList.setId(rs.getInt("id"))
        productList.setStoreId(rs.getString("storeId"))
        productList.setUserId(rs.getString("userId"))
        productList.setType(ProductListType.valueOf(rs.getString("type")))
        productList.setStatus(ProductListStatus.valueOf(rs.getString("status")))
        productList.setParentId(rs.getInt("parentId"))

        String parentTypeString = rs.getString("parentType")
        productList.setParentType(parentTypeString != null ? ProductListType.valueOf(parentTypeString) : null)

        productList.setDateStarted(new DateTime(rs.getTimestamp("dateStarted")).withZoneRetainFields(DateTimeZone.UTC))
        if (rs.wasNull()) {
            productList.setDateStarted(null)
        }

        productList.setDateCompleted(new DateTime(rs.getTimestamp("dateCompleted")).withZoneRetainFields(DateTimeZone.UTC))
        if (rs.wasNull()) {
            productList.setDateCompleted(null)
        }

        productList.setOwnerUserId(rs.getString("ownerUserId"))

        // Check for nullable int ownerUserId.
        if (rs.wasNull()) {
            productList.setOwnerUserId(null)
        }

        productList.setOwnerUsersName(rs.getString("ownerUsersName"))
        productList.setProductListItems(new ArrayList<uk.co.wonderlane.wlpos.entities.wlim.ProductListItem>())
        productList.setDescription(rs.getString("description"))

        productList.setReasonId(rs.getString("reasonId"))
        // Check for nullable reasonId.
        if (rs.wasNull()) {
            productList.setReasonId(null)
        }

        productList.setReasonDescription(rs.getString("reasonDescription"))

        // Check for nullable reasonId.
        if (rs.wasNull()) {
            productList.setReasonDescription(null)
        }

        productList.setStockAdjustedOnCompletion(rs.getBoolean("stockAdjustedOnCompletion"))
        productList.setStartDate(new DateTime(rs.getTimestamp("startDate")).withZoneRetainFields(DateTimeZone.UTC))
        if (rs.wasNull()) {
            productList.setStartDate(null)
        }

        productList.setEndDate(new DateTime(rs.getTimestamp("endDate")).withZoneRetainFields(DateTimeZone.UTC))
        if (rs.wasNull()) {
            productList.setEndDate(null)
        }

        productList.setSupplierId(rs.getString("supplierId") as Integer)
        if (rs.wasNull()) {
            productList.setSupplierId(null)
        }

        productList.setSupplierReference(rs.getString("supplierReference"))
        if (rs.wasNull()) {
            productList.setSupplierReference(null)
        }

        productList.setDestinationStore(rs.getInt("destinationStoreId"))
        productList.setRetailerListId(rs.getString("retailerListId"))
        if (rs.wasNull()) {
            productList.setRetailerListId(null)
        }

        return productList
    }

    uk.co.wonderlane.wlpos.entities.wlim.ProductListItemGroup mapProductListItemGroup(ResultSet rs) throws SQLException {
        uk.co.wonderlane.wlpos.entities.wlim.ProductListItemGroup productListItemGroup = new uk.co.wonderlane.wlpos.entities.wlim.ProductListItemGroup()
        productListItemGroup.setId(rs.getInt("id"))
        productListItemGroup.setProductListId(rs.getInt("productListId"))
        productListItemGroup.setUniqueIdentifier(rs.getString("uniqueIdentifier"))
        return productListItemGroup
    }

    uk.co.wonderlane.wlpos.entities.wlim.ProductListItem mapProductListItem(ResultSet rs, Map<Integer, uk.co.wonderlane.wlpos.entities.wlim.ProductListItem> productListItemMap) throws SQLException {
        uk.co.wonderlane.wlpos.entities.wlim.ProductListItem productListItem = null
        int productItemId = rs.getInt("id")
        //Populate product item details
        if (!productListItemMap.containsKey(productItemId)) {
            productListItem = new uk.co.wonderlane.wlpos.entities.wlim.ProductListItem()
            productListItem.setId(rs.getInt("id"))
            productListItem.setProductVariantId(rs.getInt("productVariantId"))
            productListItem.setProductVariantItemCode(String.valueOf(rs.getString("sku")))
            productListItem.setProductLongDescription(rs.getString("description"))
            productListItem.setProductShortDescription(rs.getString("receiptDescription"))
            String barcodes = rs.getString("barcodes")
            if (barcodes != null) {
                productListItem.setProductBarcodes(Arrays.asList(barcodes.split(",")))
            }
            productListItem.setProductPrice(rs.getBigDecimal("price"))
            productListItem.setQuantity(rs.getBigDecimal("quantity") ?: BigDecimal.ZERO)
            productListItem.setPackLines(new ArrayList<>())
            // Check for nullable int quantity.
            if (rs.wasNull()) {
                productListItem.setQuantity(null)
            }
            productListItem.setFillQuantity(rs.getBigDecimal("fillQuantity") ?: BigDecimal.ZERO)
            productListItem.setParentQuantity(rs.getBigDecimal("parentQuantity") ?: BigDecimal.ZERO)
        } else {
            productListItem = productListItemMap.get(productItemId)
        }

        //populate pack line details if exists --> Check by pack quantity since singles do not have packId or Order Code
        int packLineQuantity = rs.getInt("packedQuantity")
        if (packLineQuantity > 0) { //If id returns greater than of -1 pack line exists
            uk.co.wonderlane.wlpos.entities.wlim.PackLine packLine = new uk.co.wonderlane.wlpos.entities.wlim.PackLine()
            packLine.setPackId(rs.getInt("packId"))
            packLine.setQuantity(rs.getInt("packedQuantity"))
            packLine.setOrderCode(rs.getString("orderCode"))
            productListItem.getPackLines().add(packLine)
        }
        productListItemMap.put(productItemId, productListItem)
        return productListItem
    }

    def sendProductListExportRequest(int productListId) {
        uk.co.wonderlane.wlpos.entities.wlim.ProductList productListToSend = getProductListById(productListId)
        sendProductListExportRequest(productListToSend)
    }

    def sendProductListExportRequest(uk.co.wonderlane.wlpos.entities.wlim.ProductList productList) {
        String retailerStoreId = storeService.getStore(springSecurityService.principal.retailerId, springSecurityService.principal.storeId).retailerStoreId
        ProductListStockTransaction productListExportRequest = new ProductListStockTransaction()

        setProductListExportFields(productList, retailerStoreId, productListExportRequest)
        productListExportRequest.getProductList().setProductListItems(addUnitSizeToProduct(springSecurityService.principal.storeId, productListExportRequest.getProductList().getProductListItems()))

        String stockTransactionJson = gsonProvider.gson.toJson(productListExportRequest, StockTransaction.class)
        rabbitService.sendSenderExchangeMessage(stockTransactionJson)
    }

    private void setProductListExportFields(uk.co.wonderlane.wlpos.entities.wlim.ProductList productList, String retailerStoreId, ProductListStockTransaction productListExportRequest) throws SQLException {
        productListExportRequest.setType(TransactionType.STOCK_TRANSACTION)
        productListExportRequest.setTransactionDateTime(DateTime.now())
        productListExportRequest.setRetailerId(springSecurityService.principal.retailerId)
        productListExportRequest.setProductList(productList)

        if (productList.getParentId() > 0) {
            uk.co.wonderlane.wlpos.entities.wlim.ProductList parentProductList = getProductListById(productList.getParentId())
            productListExportRequest.setParentProductList(parentProductList)
        }

        productListExportRequest.setRetailerStoreId(retailerStoreId)

        if (productList.getSupplierId() != null && productList.getSupplierId() > 0) {
            Supplier supplier = supplierService.getSupplier(productList.getSupplierId())
            productListExportRequest.setRetailerSupplierId(String.valueOf(supplier.getRetailerSupplierId()))
        }

        if (productList.getUserId() != null && !productList.getUserId().isBlank()) {
            User user = userService.getUserByUsername(productList.getUserId())
            if (user != null) { // Central Counts may not find an active username as it's not created on the App
                productListExportRequest.setUserId(user.getId())
            }
        }
    }

    private List<uk.co.wonderlane.wlpos.entities.wlim.ProductListItem> addUnitSizeToProduct(int storeId, List<uk.co.wonderlane.wlpos.entities.wlim.ProductListItem> items) throws SQLException {
        for (uk.co.wonderlane.wlpos.entities.wlim.ProductListItem item : items) {
            if (item.getUnitSize() == null) {
                uk.co.wonderlane.wlpos.entities.ProductVariant variant = productService.getProductVariant(storeId, item.getProductVariantId())
                if (variant != null) {
                    Product product = productService.getProduct(variant.getProductId())
                    if (product != null) {
                        item.setUnitSize(product.getUnitSize())
                    }
                }
            }
        }
        return items
    }
}