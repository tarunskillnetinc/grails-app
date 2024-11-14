package uk.co.wonderlane.wlpos

import grails.gorm.transactions.Transactional
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import org.joda.time.format.DateTimeFormat
import org.joda.time.format.DateTimeFormatter
import org.xml.sax.SAXException
import uk.co.wonderlane.wlpos.dataaccess.DatabaseCredentials
import uk.co.wonderlane.wlpos.dataaccess.MySqlDal
import uk.co.wonderlane.wlpos.enums.wlim.ProductListStatus
import uk.co.wonderlane.wlpos.enums.wlim.ProductListType
import uk.co.wonderlane.wlpos.supplier.Supplier

import javax.xml.parsers.ParserConfigurationException
import java.sql.*

@Transactional
class OrderService extends MySqlDal {

    def springSecurityService
    def userService
    def nisaService
    def productService
    def productListService

    OrderService(DatabaseCredentials databaseCredentials) {
        super(databaseCredentials)
    }

    // This will create new product list
    def createProductList(uk.co.wonderlane.wlpos.entities.wlim.ProductList productList, ProductListType productListType, Supplier supplier) {
        Connection connection
        try {
            connection = getConnection()
            connection.setAutoCommit(false)

            User user = userService.getUser(springSecurityService.principal.id)
            productList = createNewProductList(connection, productList, productListType, user)
            if (productList != null) {
                updateListSupplier(connection, supplier, productList.getId())
            }

            connection.commit()
        } catch (Exception ex) {
            log.error("Order create exception found when creating new product list , Exception " + ex.getMessage())
            if (connection != null) {
                connection.rollback()
            }
            throw ex
        } finally {
            if (connection != null) {
                connection.close()
            }
        }
        return productList
    }

    // This will save product list items and packs
    def saveProductOrder(PackLineRequestCommand packLineRequestCommand) {
        Connection connection
        try {
            connection = getConnection()
            connection.setAutoCommit(false)

            saveProductListsAndPackLines(connection, packLineRequestCommand)

            connection.commit()
        } catch (Exception ex) {
            log.error("Order create exception found when saving product order, request is rollback , Exception " + ex.getMessage())
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

    // This will work on confirming order request
    def confirmOrder(ProductList productList, Supplier supplier) {
        String response = null
        Connection connection

        try {
            connection = getConnection()
            connection.setAutoCommit(false)

            response = confirmProductList(connection, productList, supplier)

            connection.commit()
        } catch (Exception ex) {
            log.error("Order create exception found when saving product order, request is rollback , Exception " + ex.getMessage())
            if (connection != null) {
                connection.rollback()
            }
            throw ex
        } finally {
            if (connection != null) {
                connection.close()
            }
        }
        return response
    }

    // Delete product list from database.
    def deleteProductList(int productListId) {
        Connection connection
        try {
            connection = getConnection()
            connection.setAutoCommit(false)

            uk.co.wonderlane.wlpos.entities.wlim.ProductList productList = getProductListById(productListId)

            if (productList.getType().IsIn(ProductListType.ORDER)) {
                for (uk.co.wonderlane.wlpos.entities.wlim.ProductListItem item : productList.getProductListItems()) {
                    deletePackLinesId(connection, item.getId())
                    deleteProductListItemId(connection, item.getId())
                }
            }

            deleteProductListById(connection, productListId)

            connection.commit()
        } catch (Exception ex) {
            log.error("Order create exception found when deleting product list, request is rollback , Exception " + ex.getMessage())
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

    def deleteProductListItem(int productListId, int productItemId) {
        Connection connection
        try {
            connection = getConnection()
            connection.setAutoCommit(false)

            uk.co.wonderlane.wlpos.entities.wlim.ProductList productList = getProductListById(productListId)

            def foundItem = productList.getProductListItems().find(item -> item.id == productItemId)
            if (foundItem) {
                deletePackLinesId(connection, foundItem.getId())
                deleteProductListItemId(connection, foundItem.getId())
            }

            connection.commit()
        } catch (Exception ex) {
            log.error("Order exception found when deleting product item, request is rollback , Exception " + ex.getMessage())
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

    /** =================================== Start product list creation methods =========================================================== **/

    private createNewProductList(Connection connection, uk.co.wonderlane.wlpos.entities.wlim.ProductList productList, ProductListType productListType, User user) {
        if (productList == null) {
            productList = addNewProductList(connection, productListType, ProductListStatus.IN_PROGRESS, -1, user.getUsername())
        }
        return productList
    }

    private addNewProductList(Connection connection, ProductListType productListType, ProductListStatus productListStatus, int parentProductListId, String usersName) throws SQLException {
        uk.co.wonderlane.wlpos.entities.wlim.ProductList productList = null
        HashMap<Integer, uk.co.wonderlane.wlpos.entities.wlim.ProductListItem> productListItemHashMap = new HashMap<>()
        CallableStatement cstmt
        try {
            cstmt = connection.prepareCall("{ call createProductList(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) }")
            cstmt.setString(1, productListType.name())
            cstmt.setInt(2, springSecurityService.principal.retailerId)
            cstmt.setString(3, String.valueOf(springSecurityService.principal.storeId))
            cstmt.setString(4, productListStatus.name())

            if (parentProductListId < 0) {
                cstmt.setNull(5, Types.INTEGER)
            } else {
                cstmt.setInt(5, parentProductListId)
            }

            if (usersName != null) {
                cstmt.setString(6, usersName)
            } else {
                cstmt.setNull(6, Types.VARCHAR)
            }

            if (usersName != null) {
                cstmt.setString(7, usersName)
            } else {
                cstmt.setNull(7, Types.VARCHAR)
            }

            cstmt.setString(8, null)
            cstmt.setString(9, null)
            cstmt.setString(10, null)
            cstmt.setString(11, null)
            cstmt.setString(12, null)
            cstmt.setBoolean(13, false)

            if (cstmt.execute()) {
                ResultSet rs = cstmt.getResultSet()

                if (rs.next()) {
                    productList = productListService.mapProductList(rs)
                }

                cstmt.getMoreResults()
                rs = cstmt.getResultSet()

                while (rs.next()) {
                    productListService.mapProductListItem(rs, productListItemHashMap)
                    productList.setProductListItems(new ArrayList<uk.co.wonderlane.wlpos.entities.wlim.ProductListItem>(productListItemHashMap.values()))
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace()
            log.error("Order create exception found when creating product list, Exception " + ex.getMessage())
            throw ex
        }
        return productList
    }

    uk.co.wonderlane.wlpos.entities.wlim.ProductList getActiveProductList(ProductListType productListType, String userName) {
        uk.co.wonderlane.wlpos.entities.wlim.ProductList productList = null
        Map<Integer, uk.co.wonderlane.wlpos.entities.wlim.ProductListItemGroup> productListItemGroupMap = new HashMap<>()
        Map<Integer, Map<Integer, uk.co.wonderlane.wlpos.entities.wlim.ProductListItem>> productListItemHashMap = new HashMap<>()
        Connection conn
        CallableStatement cstmt
        try {
            conn = getConnection()
            cstmt = conn.prepareCall("{ call getActiveProductList(?, ?, ?, ?) }")
            cstmt.setInt(1, springSecurityService.principal.retailerId)
            cstmt.setString(2, String.valueOf(springSecurityService.principal.storeId))
            cstmt.setString(3, userName)
            cstmt.setString(4, productListType.name())

            if (cstmt.execute()) {
                ResultSet rs = cstmt.getResultSet()

                while (rs.next()) {
                    productList = productListService.mapProductList(rs)
                }

                cstmt.getMoreResults()
                rs = cstmt.getResultSet()

                // Item groups (cages).
                while (rs.next()) {
                    uk.co.wonderlane.wlpos.entities.wlim.ProductListItemGroup productListItemGroup = productListService.mapProductListItemGroup(rs)
                    productList.getProductListItemGroups().add(productListItemGroup)
                    productListItemGroupMap.put(productListItemGroup.getId(), productListItemGroup)
                }

                cstmt.getMoreResults()
                rs = cstmt.getResultSet()

                while (rs.next()) {
                    int productListId = rs.getInt("productListId")
                    int productListItemGroupId = rs.getInt("productListItemGroupId")
                    int itemKey = productListItemGroupId > 0 ? productListItemGroupId : productListId

                    if (!productListItemHashMap.containsKey(itemKey)) {
                        productListItemHashMap.put(itemKey, new HashMap<>())
                    }

                    productListService.mapProductListItem(rs, productListItemHashMap.get(itemKey))

                    if (productListItemGroupId > 0 && productListItemGroupMap.get(productListItemGroupId) != null) {
                        productListItemGroupMap.get(productListItemGroupId).setProductListItems(new ArrayList<>(productListItemHashMap.get(itemKey).values()))
                    } else {
                        productList.setProductListItems(new ArrayList<>(productListItemHashMap.get(itemKey).values()))
                    }
                }
            }
        } catch (Exception ex) {
            log.error("Order create exception found when checking for active product list, Exception " + ex.getMessage())
            throw ex
        } finally {
            if (conn != null) {
                conn.close()
            }
        }
        return productList
    }

    private boolean updateListSupplier(Connection connection, Supplier supplier, int productListId) throws SQLException {
        CallableStatement cstmt
        try {
            cstmt = connection.prepareCall("{ call updateListSupplier(?, ?, ?) }")
            cstmt.setInt(1, productListId)
            cstmt.setString(2, String.valueOf(supplier.getId()))
            cstmt.setString(3, supplier.getReference())
            cstmt.executeUpdate()
        } catch (Exception ex) {
            ex.printStackTrace()
            log.error("Order create exception found when updating product list supplier id, Exception " + ex.getMessage())
            throw ex
        }
        return true
    }

    /** =================================== End product list creation methods =========================================================== **/


    /** =================================== Start confirm product list methods =========================================================== **/

    private confirmProductList(Connection connection, ProductList productList, Supplier supplier) throws SQLException, IOException, ParserConfigurationException, SAXException {
        if (productList?.getType() == ProductListType.ORDER) {
            // Update product list to relevant status.
            productList.dateCompleted = DateTime.now(DateTimeZone.UTC)
            productList.status = getStatusToUpdate(productList.getType(), productList.getParentId())
            productList.save()

            def commonProductList = productList.getProductList(springSecurityService.principal.priceBand, springSecurityService.principal.storeId)

            // Update entry to product stock.
            productListService.sendProductListExportRequest(commonProductList)

            if (supplier?.symbolGroup?.id > 0) {
                // In case of symbol group order send request NISA
                return nisaService.generateXMLForOrder(connection, commonProductList)
            } else {
                //For non symbol group order requests --> Create deliveries
                //Insert delivery row to product list
                //Insert product list items
                //Insert pack lines
                return saveProductDeliveries(connection, commonProductList, ProductListType.DELIVERY.toString(), ProductListStatus.PENDING.toString(), supplier)
            }
        }
    }

    private ProductListStatus getStatusToUpdate(ProductListType type, Integer parentId) throws SQLException {
        // This function replicates the logic that was previously in saveProductList stored procedure
        if (type != null && type.IsIn(ProductListType.AD_HOC_SEL_BATCH, ProductListType.PRICE_CHECK)) {
            return ProductListStatus.PARTIALLY_COMPLETE
        } else if (type == ProductListType.INVENTORY_ADJUSTMENT || parentId == null || parentId == 0 || doesParentAndChildrenProductListItemCountsMatch(parentId)) {
            return ProductListStatus.COMPLETE
        }

        return ProductListStatus.PARTIALLY_COMPLETE
    }

    private boolean doesParentAndChildrenProductListItemCountsMatch(int parentId) throws SQLException {
        boolean result = false

        try (Connection conn = getConnection(); CallableStatement cstmt = conn.prepareCall("{ call doesParentAndChildrenProductListItemCountsMatch(?) }")) {
            cstmt.setInt(1, parentId)

            if (cstmt.execute()) {
                ResultSet rs = cstmt.getResultSet()

                if (rs.next()) {
                    result = rs.getBoolean("result")
                }
            }
        }
        return result
    }

    uk.co.wonderlane.wlpos.entities.wlim.ProductList getProductListById(int productListId) throws SQLException {
        productListService.getProductListById(productListId)
    }

    def saveProductDeliveries(Connection connection, uk.co.wonderlane.wlpos.entities.wlim.ProductList productList, String type, String status, Supplier supplier) {
        int productListId = saveDeliveryProduct(connection, productList, type, status, supplier)
        HashMap<Integer, Integer> productDeliveryListItemMap = saveDeliveryProductItemList(connection, productList, productListId)
        saveDeliveryPackLines(connection, productList, productListId, productDeliveryListItemMap)
        return null
    }

    def saveDeliveryProduct(Connection connection, uk.co.wonderlane.wlpos.entities.wlim.ProductList productList, String type, String status, Supplier supplier) throws SQLException {
        int productListId = saveDeliveryProductList(connection, productList, type, status)
        updateListSupplier(connection, supplier, productListId)
        return productListId
    }


    def saveDeliveryProductList(Connection connection, uk.co.wonderlane.wlpos.entities.wlim.ProductList productList, String type, String status) throws SQLException {
        CallableStatement stmt
        DateTimeFormatter dateTimeFormatter = DateTimeFormat.forPattern("yyyy-MM-dd").withZoneUTC()
        String deliveryDate = DateTime.now().toString(dateTimeFormatter)
        try {
            stmt = connection.prepareCall("call createDeliveryProductList(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)")
            stmt.setString(1, type)
            stmt.setInt(2, springSecurityService.principal.retailerId)
            stmt.setInt(3, springSecurityService.principal.storeId)
            stmt.setString(4, status)
            stmt.setNull(5, Types.NULL)
            stmt.setString(6, productList.getUserId())
            stmt.setString(7, productList.getUserId())
            stmt.setNull(8, Types.NULL)
            stmt.setInt(9, productList.getId())
            stmt.setString(10, deliveryDate)
            stmt.setBoolean(11, true)
            stmt.setBoolean(11, true)

            ResultSet resultSet = stmt.executeQuery()

            if (resultSet.next()) {
                return resultSet.getInt("id") // This returns the id of the product list record created
            }
        } catch (Exception ex) {
            log.error("Order create exception found when saving delivery product list for non symbol group, Exception " + ex.getMessage())
            throw ex
        }
        return null
    }

    private HashMap<Integer, Integer> saveDeliveryProductItemList(Connection connection, uk.co.wonderlane.wlpos.entities.wlim.ProductList productList, int deliveryListId) throws SQLException {
        CallableStatement stmt
        HashMap<Integer, Integer> productDeliveryListItemMap = new HashMap<>()
        try {
            stmt = connection.prepareCall("{ call saveProductListItem(?, ?, ?, ?, ?, ?, ?, ?) }")
            for (uk.co.wonderlane.wlpos.entities.wlim.ProductListItem listItem : productList.getProductListItems()) {
                uk.co.wonderlane.wlpos.entities.ProductVariant productVariant = productService.getProductVariant(Integer.parseInt(productList.getStoreId()), listItem.getProductVariantId())
                BigDecimal stockInQuantity = productVariant.getQuantityInStock()
                Integer locationId = listItem.getLocation() == null ? null : listItem.getLocation().getId()
                populateListItemInsertStatement(stmt, deliveryListId, -1, -1, listItem.getProductVariantId(), stockInQuantity, listItem.getQuantity() != null ? listItem.getQuantity() : BigDecimal.ZERO, listItem.getFillQuantity(), locationId)
                if (stmt.execute()) {
                    ResultSet rs = stmt.getResultSet()
                    if (rs.next()) {
                        int productListItemId = rs.getInt("productListItemId")
                        productDeliveryListItemMap.put(listItem.id, productListItemId)
                    }
                }
            }
        } catch (Exception ex) {
            log.error("Order create exception found when saving delivery product list items for non symbol group, Exception " + ex.getMessage())
            throw ex
        }
        return productDeliveryListItemMap
    }

    private saveDeliveryPackLines(Connection connection, uk.co.wonderlane.wlpos.entities.wlim.ProductList productList, int deliveryListId, HashMap<Integer, Integer> productDeliveryListItemMap) throws SQLException {
        CallableStatement stmt
        try {
            stmt = connection.prepareCall("{ call savePackLine(?, ?, ?, ?, ?, ?) }")
            for (uk.co.wonderlane.wlpos.entities.wlim.ProductListItem listItem : productList.getProductListItems()) {
                for (uk.co.wonderlane.wlpos.entities.wlim.PackLine packLines : listItem.getPackLines()) {
                    int deliveryListItemId = productDeliveryListItemMap.get(listItem.getId())
                    populatePackLinesInsertStatement(stmt, deliveryListId, packLines.getPackId(), packLines.getQuantity() != null ? packLines.getQuantity() : BigDecimal.ZERO, packLines.getOrderCode(), ProductListType.DELIVERY.toString(), deliveryListItemId)
                    stmt.addBatch()
                    stmt.clearParameters()
                }
                stmt.executeBatch()
            }
        } catch (Exception ex) {
            log.error("Order create exception found when saving delivery pack Lines for non symbol group, Exception " + ex.getMessage())
            throw ex
        }
    }

    /** =================================== End confirm product list methods =========================================================== **/

    /** =================================== Start saving product list items and packs method =========================================================== **/

    private saveProductListsAndPackLines(Connection connection, PackLineRequestCommand packLineRequestCommand) {
        int productListItemId = saveProductListItems(connection, packLineRequestCommand)
        saveOrderedPacks(connection, packLineRequestCommand, ProductListType.ORDER.toString(), productListItemId)
    }

    private saveProductListItems(Connection connection, PackLineRequestCommand packLinesCommand) {

        uk.co.wonderlane.wlpos.entities.ProductVariant productVariant = productService.getProductVariant(springSecurityService.principal.storeId, packLinesCommand.getProductVariantId())
        BigDecimal quantityInStock = productVariant.getQuantityInStock()

        // Save and retrieve the item.
        return saveProductListItem(connection, packLinesCommand, quantityInStock)
    }


    private saveProductListItem(Connection connection, PackLineRequestCommand packLineRequestCommand, BigDecimal quantityInStock) throws SQLException {
        CallableStatement cstmt
        int productListItemId = -1
        try {
            cstmt = connection.prepareCall("{ call saveProductListItem(?, ?, ?, ?, ?, ?, ?, ?) }")
            populateListItemInsertStatement(cstmt, packLineRequestCommand.getProductListId(), packLineRequestCommand.getProductItemId(), -1, packLineRequestCommand.getProductVariantId(), quantityInStock,
                    packLineRequestCommand.getQuantity(), packLineRequestCommand.getFillQuantity(), null)
            if (cstmt.execute()) {
                ResultSet rs = cstmt.getResultSet()
                if (rs.next()) {
                    productListItemId = rs.getInt("productListItemId")
                }
            }
        } catch (Exception ex) {
            log.error("Order create exception found when saving product list items, Exception " + ex.getMessage())
            throw ex
        }

        return productListItemId
    }

    private saveOrderedPacks(Connection connection, PackLineRequestCommand packLineRequest, String type, int productListItemId) throws SQLException {
        CallableStatement cstmt
        try {
            cstmt = connection.prepareCall("{ call savePackLine(?, ?, ?, ?, ?, ?) }")
            for (PackLinesCommand packLineCommand : packLineRequest.getPackLines()) {
                populatePackLinesInsertStatement(cstmt, packLineRequest.getProductListId(), packLineCommand.getId(), packLineCommand.getQuantity(), packLineCommand.getOrderCode(), type, productListItemId)
                cstmt.addBatch()
                cstmt.clearParameters()
            }
            cstmt.executeBatch()
        } catch (Exception ex) {
            log.error("Order create exception found when saving order pack lines, Exception " + ex.getMessage())
            throw ex
        }
    }

    /** =================================== End saving product list items and packs method =========================================================== **/


    /** =================================== Start product list delete methods =========================================================== **/

    private int deletePackLinesId(Connection connection, int productListItemId) throws SQLException {
        CallableStatement cstmt
        try {
            cstmt = connection.prepareCall("{ call deletePackLines(?) }")
            cstmt.setInt(1, productListItemId)
            cstmt.execute()
        } catch (Exception ex) {
            log.error("Order create exception found when deleting product pack lines by id, request is rollback , Exception " + ex.getMessage())
            throw ex
        }
        return productListItemId
    }

    private int deleteProductListItemId(Connection connection, int productListItemId) throws SQLException {
        CallableStatement cstmt
        try {
            cstmt = connection.prepareCall("{ call deleteProductListItem(?) }")
            cstmt.setInt(1, productListItemId)
            cstmt.execute()
        } catch (Exception ex) {
            log.error("Order create exception found when deleting product list item by id, request is rollback , Exception " + ex.getMessage())
            throw ex
        }
        return productListItemId
    }

    private void deleteProductListById(Connection connection, int productListId) throws SQLException {
        CallableStatement cstmt
        try {
            cstmt = connection.prepareCall("{ call deleteProductList(?) }")
            cstmt.setInt(1, productListId)
            cstmt.executeUpdate()
        } catch (Exception ex) {
            log.error("Order create exception found when deleting order list by id, request is rollback , Exception " + ex.getMessage())
            throw ex
        }
    }

    /** =================================== End product list delete methods =========================================================== **/

    private populateListItemInsertStatement(CallableStatement cstmt, int productListId, int productItemList, int productListItemGroupId, int productVariantId, BigDecimal productQuantityInStore, BigDecimal quantity, BigDecimal fillQuantity, Integer locationId) {
        cstmt.setInt(1, productListId)
        cstmt.setInt(2, productItemList)
        cstmt.setInt(3, productListItemGroupId)
        cstmt.setInt(4, productVariantId)
        if (productQuantityInStore != null) {
            cstmt.setBigDecimal(5, productQuantityInStore)
        } else {
            cstmt.setNull(5, Types.DECIMAL)
        }

        if (quantity != null) {
            cstmt.setBigDecimal(6, quantity)
        } else {
            cstmt.setNull(6, Types.DECIMAL)
        }

        if (fillQuantity != null) {
            cstmt.setBigDecimal(7, fillQuantity)
        } else {
            cstmt.setBigDecimal(7, BigDecimal.ZERO)
        }

        if (locationId != null) {
            cstmt.setInt(8, locationId)
        } else {
            cstmt.setNull(8, Types.INTEGER)
        }
    }

    private populatePackLinesInsertStatement(CallableStatement cstmt, int productListId, int packId, BigDecimal packQuantity, String orderCode, String type, int productListItemId) {
        cstmt.setInt(1, productListId)
        cstmt.setInt(2, productListItemId)

        if (packId > 0) {
            cstmt.setInt(3, packId)
        } else {
            cstmt.setNull(3, Types.INTEGER)
        }

        if (packQuantity != null) {
            cstmt.setBigDecimal(4, packQuantity)
        } else {
            cstmt.setNull(4, Types.DECIMAL)
        }

        if (orderCode == null || orderCode.length() > 0) {
            cstmt.setString(5, orderCode)
        } else {
            cstmt.setNull(5, Types.VARCHAR)
        }

        cstmt.setString(6, type)
    }
}
