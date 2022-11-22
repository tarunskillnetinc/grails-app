package uk.co.wonderlane.wlpos

import grails.gorm.transactions.Transactional

import org.hibernate.Session
import org.hibernate.Transaction
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import uk.co.wonderlane.wlpos.dataaccess.DatabaseCredentials
import uk.co.wonderlane.wlpos.dataaccess.MySqlDal
import uk.co.wonderlane.wlpos.entities.SyncMessage
import uk.co.wonderlane.wlpos.enums.ProductStatus
import uk.co.wonderlane.wlpos.enums.SyncMessageType
import uk.co.wonderlane.wlpos.reporting.ReportColumns
import uk.co.wonderlane.wlpos.reporting.ReportType

import java.sql.CallableStatement
import java.sql.Connection
import java.sql.ResultSet
import java.sql.SQLException
import java.sql.Types
import java.util.stream.Collectors

@Transactional
class ProductService extends MySqlDal {

    def springSecurityService
    def sessionFactory
    def rabbitService
    def gsonProvider

    ProductService(DatabaseCredentials databaseCredentials) {
        super(databaseCredentials)
    }

    def getProductVariant(int id) {
        return ProductVariant.withCriteria(sort: "effectiveDate", order: "desc") {
            eq("id", id)
            or {
                isNull("storeId")
                eq("storeId", springSecurityService.principal.storeId)
            }
            product {
                eq("retailerId", springSecurityService.principal.retailerId)
            }
        }?.first() ?: null
    }

    def getProductVariant(long sku) {
        return ProductVariant.withCriteria(sort: "effectiveDate", order: "desc") {
            eq("sku", sku)
            or {
                isNull("storeId")
                eq("storeId", springSecurityService.principal.storeId)
            }
            lte("effectiveDate", DateTime.now(DateTimeZone.UTC))
            product {
                eq("retailerId", springSecurityService.principal.retailerId)
            }
        }?.first() ?: null
    }

    // TODO make this method only return the current effective date. Currently it will return any which exist (sorted so that the active one is first (unless the description has changed)).
    def getProductVariants(List<Long> skus) {
        def criteria = ProductVariant.createCriteria()

        return criteria.list {
            "in"("sku", skus)
            or {
                isNull("storeId")
                eq("storeId", springSecurityService.principal.storeId)
            }
            lte("effectiveDate", DateTime.now(DateTimeZone.UTC))
            product {
                eq("retailerId", springSecurityService.principal.retailerId)
            }

            and {
                product {
                    order("description", "asc")
                }
                order("effectiveDate", "desc")
            }
        }
    }

    def getProduct(int id) {
        def product = Product.findByIdAndRetailerId(id, springSecurityService.principal.retailerId)

        return product
    }

    def saveProduct(Product product) {
        product.save()
    }

    def saveProduct(Product product, List<ProductVariant> productVariantList) {
        if (productVariantList != null && productVariantList.size() > 0) {
            productVariantList.each { pv -> product.addToVariants(pv) }
        }

        product.save(flush: true)
    }

    def saveBarcodes(Product product) {
        product?.variants?.each { variant ->
            variant.barcodez?.each { barcode ->
                if (barcode.hasProperty('delete') && barcode.delete) {
                    Barcode deletedBarcode = new Barcode()
                    deletedBarcode.sku = barcode.sku
                    deletedBarcode.retailerId = barcode.retailerId
                    deletedBarcode.barcode = barcode.barcode
                    deletedBarcode.effectiveDate = barcode.effectiveDeleteDate
                    deletedBarcode.recordStatus = 'D'
                    deletedBarcode.save()
                } else if (barcode instanceof Barcode) {
                    barcode.save()
                }
            }
        }
    }

    def saveProductVariant(ProductVariant productVariant) {
        productVariant.save()
    }

    def saveProductPrices(List<ProductPrice> productPrices, List<ProductHistory> productHistories) {
        Session session = sessionFactory.openSession()
        Transaction transaction = session.beginTransaction()

        productPrices.unique { [it.sku, it.effectiveDate, it.price] }

        productPrices.eachWithIndex { productPrice, index ->
            if (productPrice?.price) {
                session.saveOrUpdate(productPrice)

                // Clear the session for speed purposes.
                if (index.mod(100) == 0) {
                    session.flush()
                    session.clear()
                }
            }
        }

        productHistories.eachWithIndex { productHistory, index ->
            session.save(productHistory)

            // Clear the session for speed purposes.
            if (index.mod(100) == 0) {
                session.flush()
                session.clear()
            }
        }

        transaction.commit()
        session.close()
    }

    def saveRangeProducts(List<RangeProduct> rangeProducts) {
        saveOrDeleteRangeProducts(rangeProducts, true, false)
    }

    def deleteRangeProducts(List<RangeProduct> rangeProducts) {
        saveOrDeleteRangeProducts(rangeProducts, false, true)
    }

    def saveProductHistories(List<ProductHistory> productHistories) {
        Session session = sessionFactory.openSession()
        Transaction transaction = session.beginTransaction()

        productHistories.eachWithIndex { productHistory, index ->
            log.info("" + productHistory.productId + " " + productHistory.fromValue + " " + productHistory.toValue + " " + productHistory.storeId + " " + productHistory.userId + " " + productHistory.usersName)
            session.save(productHistory)
            // Clear the session for speed purposes.
            if (index.mod(100) == 0) {
                session.flush()
                session.clear()
            }
        }

        transaction.commit()
        session.close()
    }

    private void saveOrDeleteRangeProducts(List<RangeProduct> rangeProducts, boolean save, boolean delete) {
        Session session = sessionFactory.openSession()
        Transaction transaction = session.beginTransaction()

        rangeProducts.eachWithIndex { rangeProduct, index ->
            if (save) {
                session.save(rangeProduct)
            } else if (delete) {
                session.delete(rangeProduct)
            }

            // Clear the session for speed purposes.
            if (index.mod(100) == 0) {
                session.flush()
                session.clear()
            }
        }

        transaction.commit()
        session.close()
    }

    def populateCurrentProductVariant(def product) {
        product.currentProductVariant = product.variants.sort { it.effectiveDate }.reverse().find { it.storeId == springSecurityService.principal.storeId && it.effectiveDate <= new Date() }
    }

    def searchProducts(String searchTerm, String searchBy, int maxResults, int startIndex, String sortColumn, String sortOrder) {
        def now = DateTime.now(DateTimeZone.UTC)

        def barcodeSkus = []

        if ((searchBy == "everything" || searchBy == "barcode") && searchTerm?.length() > 2) {
            barcodeSkus = Barcode.findAllByBarcodeLikeAndRetailerIdAndEffectiveDateLessThanEquals("%$searchTerm%", springSecurityService.principal.retailerId, now)?.collect { it.sku }?.unique()
        }

        def productSearchCriteria = Product.createCriteria()

        searchTerm = searchTerm ? searchTerm.trim() : ""

        def results = productSearchCriteria.list([offset: startIndex, max: maxResults]) {
            eq("retailerId", springSecurityService.principal.retailerId)
            variants {
                or {
                    isNull("storeId")
                    eq("storeId", springSecurityService.principal.storeId)
                }
                lte("effectiveDate", now)
            }

            if (searchBy == "everything") {
                or {
                    variants {
                        "in"("sku", barcodeSkus)
                    }
                    like("itemCode", "%$searchTerm%")
                    if (searchTerm.isNumber()) {
                        variants {
                            eq("sku", Long.parseLong(searchTerm))
                        }
                    }
                    like("description", "%$searchTerm%")
                }
            } else if (searchBy == "itemCode") {
                or {
                    like("itemCode", "%$searchTerm%")
                    if (searchTerm.isNumber()) {
                        variants {
                            eq("sku", Long.parseLong(searchTerm))
                        }
                    }
                }
            } else if (searchBy == "description") {
                like("description", "%$searchTerm%")
            } else if (searchBy == "barcode") {
                variants {
                    "in"("sku", barcodeSkus)
                }
            }

            if (sortColumn == "id" || sortColumn == "description") {
                order(sortColumn, sortOrder)
            } else if (sortColumn == "price") {
                order("variants.price", sortOrder)
            }
        }

        // Criteria.list() with max and offset returns a totalCount, but for some reason I am having to read that value otherwise an error is thrown when trying to use it back in the controller.
        // I believe this may be related to the domain class being in an alternate datasource, but I think it's a bug in Grails. Actually, I think it's because the totalCount is lazily loaded
        // to prevent the double query immediately. But it's throwing a Hibernate session error if I don't request it here.
        int totalCount = results.totalCount

        return results
    }

    def searchProductsHql(String searchTerm, String searchBy, int maxResults, int startIndex, String sortColumn, String sortOrder) {
        def now = DateTime.now(DateTimeZone.UTC)

        def barcodeSkus = []

        searchTerm = searchTerm ? searchTerm.trim() : ""

        if ((searchBy == "everything" || searchBy == "barcode") && searchTerm?.length() > 2) {
            def validBarcodeSkus = [] //Declare valid barcode sku list

            //Get all barcodes which like search term (Ex : search term - 111 )
            def barcodes = Barcode.findAllByBarcodeLikeAndRetailerIdAndEffectiveDateLessThanEquals("%$searchTerm%", springSecurityService.principal.retailerId, now)

            //Group barcodes to map of sku --> {1 : [111(C) , 111 (D), 1114(C) ,1115(C), 1117(C)], 2:[1119(C)]}
            def skuMap = barcodes?.groupBy { it.sku }

            for (Map.Entry<Long, List<Barcode>> skuListEntry : skuMap?.entrySet()) {

                //Group sku list int map of barcode
                def barcodeMap = skuListEntry.getValue()?.groupBy { it.barcode }

                //Then loop over map of barcode to find out all active sku values
                for (Map.Entry<String, List<Barcode>> barcodeListEntry : barcodeMap?.entrySet()) {
                    int deletedBarcodeCount = 0
                    int activeBarcodeCount = 0

                    //For barcode belonging to particular sku check occurrence of active and deleted
                    barcodeListEntry.value?.forEach({ barcode ->
                        if (barcode.recordStatus == ('D' as char)) {
                            deletedBarcodeCount++
                        } else {
                            activeBarcodeCount++
                        }
                    })

                    //If active barcode count (Status = 'C') greater than of barcode count for deleted (Status = 'D') then we can assume that barcode is active
                    if (activeBarcodeCount > deletedBarcodeCount) {
                        validBarcodeSkus.add(skuListEntry.getKey())
                        break
                    }
                }
            }

            barcodeSkus = validBarcodeSkus?.unique()

        }

        def queryParams = [retailerId: springSecurityService.principal.retailerId, storeId: springSecurityService.principal.storeId, effectiveDate: now, max: maxResults, offset: startIndex]
        def countQueryParams = [retailerId: springSecurityService.principal.retailerId, storeId: springSecurityService.principal.storeId, effectiveDate: now]

        // TODO Definitely a better way to put this lot together rather than two separate queries and sets of query params.
        String searchQuery = """SELECT DISTINCT(p)
                                FROM Product p
                                JOIN ProductVariant pv ON p.id = pv.product AND (pv.storeId IS NULL OR pv.storeId = :storeId) AND pv.effectiveDate <= :effectiveDate
                                LEFT JOIN Barcode b ON pv.sku = b.sku AND b.retailerId = :retailerId
                                WHERE p.retailerId = :retailerId """

        String countQuery = """SELECT COUNT(DISTINCT p)
                                FROM Product p
                                JOIN ProductVariant pv ON p.id = pv.product AND (pv.storeId IS NULL OR pv.storeId = :storeId) AND pv.effectiveDate <= :effectiveDate
                                LEFT JOIN Barcode b ON pv.sku = b.sku AND b.retailerId = :retailerId
                                WHERE p.retailerId = :retailerId """

        if (searchBy == "everything") {
            queryParams.barcodeSkus = barcodeSkus
            queryParams.searchTerm = "%${searchTerm}%"
            countQueryParams.barcodeSkus = barcodeSkus
            countQueryParams.searchTerm = "%${searchTerm}%"

            searchQuery += """AND (pv.sku IN (:barcodeSkus)
                                   OR p.itemCode LIKE :searchTerm
                                   OR p.description LIKE :searchTerm """

            countQuery += """AND (pv.sku IN (:barcodeSkus)
                                   OR p.itemCode LIKE :searchTerm
                                   OR p.description LIKE :searchTerm """

            if (searchTerm.isNumber()) {
                queryParams.searchTermLong = Long.parseLong(searchTerm)
                countQueryParams.searchTermLong = Long.parseLong(searchTerm)

                searchQuery += """OR pv.sku = :searchTermLong) """
                countQuery += """OR pv.sku = :searchTermLong) """
            } else {
                searchQuery += """) """
                countQuery += """) """
            }
        } else if (searchBy == "itemCode") {
            queryParams.searchTerm = "%${searchTerm}%"
            countQueryParams.searchTerm = "%${searchTerm}%"

            searchQuery += """AND (p.itemCode LIKE :searchTerm """
            countQuery += """AND (p.itemCode LIKE :searchTerm """

            if (searchTerm.isNumber()) {
                queryParams.searchTermLong = Long.parseLong(searchTerm)
                countQueryParams.searchTermLong = Long.parseLong(searchTerm)

                searchQuery += """OR pv.sku = :searchTermLong) """
                countQuery += """OR pv.sku = :searchTermLong) """
            } else {
                searchQuery += """) """
                countQuery += """) """
            }
        } else if (searchBy == "description") {
            queryParams.searchTerm = "%${searchTerm}%"
            countQueryParams.searchTerm = "%${searchTerm}%"

            searchQuery += """AND p.description LIKE :searchTerm """
            countQuery += """AND p.description LIKE :searchTerm """
        } else if (searchBy == "barcode") {
            queryParams.barcodeSkus = barcodeSkus
            countQueryParams.barcodeSkus = barcodeSkus

            searchQuery += """AND pv.sku IN (:barcodeSkus) """
            countQuery += """AND pv.sku IN (:barcodeSkus) """
        }

        if (sortColumn == "id" || sortColumn == "description") {
            searchQuery += """ORDER BY p.${sortColumn} ${sortOrder}"""
        } else if (sortColumn == "price") {
            searchQuery += """ORDER BY pv.${sortColumn} ${sortOrder}"""
        }

        def results = [:]
        results.products = Product.executeQuery(searchQuery, queryParams)
        results.totalCount = Product.executeQuery(countQuery, countQueryParams)?.get(0) ?: 0

        return results
    }

    def searchProductPrices(String searchTerm, Integer categoryId, Integer tagId) {
        def results = []

        Connection conn = getConnection()
        CallableStatement cstmt = conn.prepareCall("{ call getProductPrices(?, ?, ?, ?, ?) }")

        try {
            cstmt.setInt(1, springSecurityService.principal.retailerId)

            if (springSecurityService.principal.storeId != null) {
                cstmt.setInt(2, springSecurityService.principal.storeId)
            } else {
                cstmt.setNull(2, Types.INTEGER)
            }

            if (searchTerm != null && !searchTerm.isEmpty()) {
                cstmt.setString(3, searchTerm)
            } else {
                cstmt.setNull(3, Types.VARCHAR)
            }

            if (categoryId != null) {
                cstmt.setInt(4, categoryId)
            } else {
                cstmt.setNull(4, Types.INTEGER)
            }

            if (tagId != null) {
                cstmt.setInt(5, tagId)
            } else {
                cstmt.setNull(5, Types.INTEGER)
            }

            ResultSet rs = cstmt.executeQuery()

            try {
                while (rs.next()) {
                    def result = [:]
                    result.productId = rs.getInt("id")
                    result.sku = rs.getLong("sku")
                    result.productDescription = rs.getString("productDescription")
                    result.price = rs.getBigDecimal("price")
                    result.priceBandDescription = rs.getString("priceBandDescription")
                    result.costPrice = rs.getBigDecimal("costPrice")

                    results.add(result)
                }
            } finally {
                rs.close()
            }
        } finally {
            cstmt.close()
            conn.close()
        }

        return results.groupBy { it.sku }
    }

    def searchRangeProducts(String searchTerm, Integer categoryId, Integer tagId) {
        def results = []

        Connection conn = getConnection()
        CallableStatement cstmt = conn.prepareCall("{ call getRangeProducts(?, ?, ?, ?, ?) }")

        try {
            cstmt.setInt(1, springSecurityService.principal.retailerId)

            if (springSecurityService.principal.storeId != null) {
                cstmt.setInt(2, springSecurityService.principal.storeId)
            } else {
                cstmt.setNull(2, Types.INTEGER)
            }

            if (searchTerm != null && !searchTerm.isEmpty()) {
                cstmt.setString(3, searchTerm)
            } else {
                cstmt.setNull(3, Types.VARCHAR)
            }

            if (categoryId != null) {
                cstmt.setInt(4, categoryId)
            } else {
                cstmt.setNull(4, Types.INTEGER)
            }

            if (tagId != null) {
                cstmt.setInt(5, tagId)
            } else {
                cstmt.setNull(5, Types.INTEGER)
            }

            ResultSet rs = cstmt.executeQuery()

            try {
                while (rs.next()) {
                    def result = [:]
                    result.productId = rs.getInt("id")
                    result.productItemCode = rs.getString("productItemCode")
                    result.productDescription = rs.getString("productDescription")
                    result.rangeId = rs.getInt("rangeId")

                    results.add(result)
                }
            } finally {
                rs.close()
            }
        } finally {
            cstmt.close()
            conn.close()
        }

        return results.groupBy { it.productId }
    }

    def deleteRangeProduct(RangeProduct rangeProduct) {
        rangeProduct?.delete()
    }

    def saveRangeProduct(RangeProduct rangeProduct) {
        rangeProduct?.save()
    }

    @Transactional("reporting")
    def getColumns() {
        return ReportColumns.findByUserIdAndReportType(springSecurityService.principal.id, ReportType.PRODUCT_SEARCH)
    }

    @Transactional("reporting")
    def saveColumns(ReportColumns reportColumns) {
        reportColumns.save()
    }

    def searchProductsNew(String searchTerm, String searchBy, int maxResults, int startIndex, String sortColumn, String sortOrder) {
        Map<Integer, uk.co.wonderlane.wlpos.entities.Product> products = new LinkedHashMap<>()

        Connection conn = getConnection()
        CallableStatement cstmt = conn.prepareCall("{ call searchProducts(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) }")

        try {
            cstmt.setInt(1, springSecurityService.principal.retailerId)
            cstmt.setInt(2, springSecurityService.principal.storeId)
            cstmt.setString(3, searchTerm ?: "")
            cstmt.setString(4, searchBy ?: "everything")
            cstmt.setNull(5, Types.INTEGER)
            cstmt.setNull(6, Types.INTEGER)
            cstmt.setInt(7, maxResults)
            cstmt.setInt(8, startIndex)
            cstmt.setString(9, sortColumn ?: "id")
            cstmt.setString(10, sortOrder ?: "asc")
            cstmt.setString(11, new DateTime().toString(DATE_TIME_FORMAT))

            // Read in our VAT codes.
            Map<Integer, uk.co.wonderlane.wlpos.entities.VatCode> vatCodes = new HashMap<>()

            ResultSet rs = cstmt.executeQuery()

            try {
                while (rs.next()) {
                    uk.co.wonderlane.wlpos.entities.VatCode vatCode = mapVatCode(rs)

                    vatCodes.put(vatCode.getId(), vatCode)
                }
            } finally {
                rs.close()
            }

            cstmt.getMoreResults()

            // Read in our categories.
            Map<Integer, uk.co.wonderlane.wlpos.entities.Category> categories = new HashMap<>()

            rs = cstmt.getResultSet()

            try {
                while (rs.next()) {
                    uk.co.wonderlane.wlpos.entities.Category category = mapCategory(rs)

                    categories.put(category.getId(), category)
                }
            } finally {
                rs.close()
            }

            cstmt.getMoreResults()

            // Read in the products.
            rs = cstmt.getResultSet()

            try {
                while (rs.next()) {
                    uk.co.wonderlane.wlpos.entities.Product product = mapProduct(rs, vatCodes, categories)

                    products.put(product.getId(), product)
                }
            } finally {
                rs.close()
            }

            cstmt.getMoreResults()

            Map<Integer, uk.co.wonderlane.wlpos.entities.ProductVariant> productVariants = new HashMap<>()

            // Read in product variants.
            rs = cstmt.getResultSet()

            try {
                while (rs.next()) {
                    uk.co.wonderlane.wlpos.entities.ProductVariant productVariant = new uk.co.wonderlane.wlpos.entities.ProductVariant()
                    productVariant.setId(rs.getInt("id"))
                    productVariant.setSku(rs.getString("sku"))
                    productVariant.setProductId(rs.getInt("productId"))
                    productVariant.setStoreId(rs.getInt("storeId"))
                    productVariant.setSize(rs.getString("size"))
                    productVariant.setColour(rs.getString("colour"))
                    productVariant.setMinimumStockLevel(rs.getInt("minimumStockLevel"))

                    productVariants.put(productVariant.getId(), productVariant)
                }
            } finally {
                rs.close()
            }

            cstmt.getMoreResults()

            // Read in the barcodes.
            rs = cstmt.getResultSet()

            try {
                while (rs.next()) {
                    int productVariantId = rs.getInt("productVariantId")

                    productVariants.get(productVariantId).getBarcodes().add(rs.getString("barcode"))
                }
            } finally {
                rs.close()
            }

            // Now we have finished with the variants add them into their respective products
            for (Map.Entry<Integer, uk.co.wonderlane.wlpos.entities.ProductVariant> productVariantEntry : productVariants.entrySet()) {
                for (uk.co.wonderlane.wlpos.entities.Product product : products.get(productVariantEntry.getValue().getProductId())) {
                    product.getVariants().add(productVariantEntry.getValue())
                }
            }

            cstmt.getMoreResults()

            // Read in tags.
            rs = cstmt.getResultSet()

            try {
                while (rs.next()) {
                    int productId = rs.getInt("productId")

                    uk.co.wonderlane.wlpos.entities.Tag tag = new uk.co.wonderlane.wlpos.entities.Tag()
                    tag.setId(rs.getInt("id"))
                    tag.setDescription(rs.getString("description"))

                    for (uk.co.wonderlane.wlpos.entities.Product product : products.get(productId)) {
                        product.getTags().add(tag)
                    }
                }
            } finally {
                rs.close()
            }

            cstmt.getMoreResults()

            // Read in the sale messages.
            rs = cstmt.getResultSet()

            try {
                while (rs.next()) {
                    int productId = rs.getInt("productId")

                    for (uk.co.wonderlane.wlpos.entities.Product product : products.get(productId)) {
                        product.getSaleMessages().add(mapMessage(rs))
                    }
                }
            } finally {
                rs.close()
            }

            cstmt.getMoreResults()

            // Read in the refund messages.
            rs = cstmt.getResultSet()

            try {
                while (rs.next()) {
                    int productId = rs.getInt("productId")

                    for (uk.co.wonderlane.wlpos.entities.Product product : products.get(productId)) {
                        product.getRefundMessages().add(mapMessage(rs))
                    }
                }
            } finally {
                rs.close()
            }

            cstmt.getMoreResults()

            // Read in the discount rates.
            rs = cstmt.getResultSet()

            try {
                while (rs.next()) {
                    int productId = rs.getInt("productId")

                    for (uk.co.wonderlane.wlpos.entities.Product product : products.get(productId)) {
                        product.getDiscountRates().add(new uk.co.wonderlane.wlpos.entities.DiscountRate(rs))
                    }
                }
            } finally {
                rs.close()
            }

            cstmt.getMoreResults()

            // Finally read the total count.
            rs = cstmt.getResultSet()

            try {
                if (rs.next()) {
                    uk.co.wonderlane.wlpos.entities.Product product = new uk.co.wonderlane.wlpos.entities.Product()
                    product.setId(rs.getInt("totalProducts"))

                    products.put(-1, product)
                }
            } finally {
                rs.close()
            }
        } finally {
            cstmt.close()
            conn.close();
        }

        return new ArrayList(products.values())
    }

    def sendProductPriceUpdate(def prices, List<StoreSettings> stores) {
        if (isSingleStageSel()) {
            sendProductPriceUpdateToRabbitMq(prices, stores)
        }
    }

    def sendProductUpdate(List<Product> products, List<StoreSettings> stores) {
        if (!rabbitService.isOpen()) {
            throw new Exception("Rabbit MQ not available")
        }
        stores?.each { StoreSettings store ->
            List<uk.co.wonderlane.wlpos.entities.Product> productEntities = new ArrayList<>()
            products.forEach({
                def productEntity = it.getProduct(store.storeId)
                if (checkProductHasPriceForStore(productEntity, store.storeId)) {
                    productEntities.add(productEntity)
                }
            })
            SyncMessage syncMessage = new SyncMessage(SyncMessageType.PRODUCT, springSecurityService.principal.retailerId, store.storeId, store.id, 0)
            syncMessage.setInsert(true)
            syncMessage.setProducts(productEntities)

            log.println("Syncing ${productEntities.size()} product updates to store ${store.storeId}")

            // TODO Just declaring the exchange doesn't help us, we also need to declare all of the till queues and bind them to the exchange, otherwise the message we're about to send goes nowhere.
            rabbitService.declareExchange(String.format("R%d_S%d", syncMessage.getRetailerId(), syncMessage.getStoreNumber()))
            rabbitService.sendExchangeMessage(String.format("R%d_S%d", syncMessage.getRetailerId(), syncMessage.getStoreNumber()), gsonProvider.gson.toJson(syncMessage))
        }
    }

    def isSingleStageSel() {
        if (springSecurityService.principal.retailer && springSecurityService.principal.retailer.twoStageSel) {
            return false
        }
        return true
    }

    def syncProductUpdatesToAllStoresForRetailer(List<Integer> productIds) {
        if (isSingleStageSel()) {
            syncProductListUpdatesToStores(productIds, StoreSettings.findAllByRetailerId(springSecurityService.principal.retailerId))
        }
    }

    def syncProductUpdatesToSingleStore(List<Integer> productIds, Integer storeId) {
        syncProductListUpdatesToStores(productIds.unique(), [StoreSettings.findById(storeId)])
    }

    private void syncProductListUpdatesToStores(List<Integer> productIds, List<StoreSettings> stores) {
        def productIdsAsInt = productIds.findAll { it != null && it > 0 }.stream().map({ it.intValue() }).collect(Collectors.toSet())
        productIdsAsInt.removeAll(Collections.singleton(null))
        if (productIdsAsInt && productIdsAsInt?.size() > 0) {
            sendProductUpdate(Product.findAllByIdInList(new ArrayList<>(productIdsAsInt)), stores)
        }
    }

    private void sendProductPriceUpdateToRabbitMq(def prices, List<StoreSettings> stores) {
        if (!rabbitService.isOpen()) {
            throw new Exception("Rabbit MQ not available")
        }
        stores?.each { StoreSettings store ->
            SyncMessage syncMessage = new SyncMessage(SyncMessageType.PRICE_CHANGE, springSecurityService.principal.retailerId, store.storeId, store.id, 0)
            syncMessage.setInsert(true)
            syncMessage.setStoreId(store.storeId)
            syncMessage.setProductPrices(prices)

            log.println("Syncing ${prices.size()} price updates to store ${store.storeId}")

            // TODO Just declaring the exchange doesn't help us, we also need to declare all of the till queues and bind them to the exchange, otherwise the message we're about to send goes nowhere.
            rabbitService.declareExchange(String.format("R%d_S%d", syncMessage.getRetailerId(), syncMessage.getStoreNumber()))
            rabbitService.sendExchangeMessage(String.format("R%d_S%d", syncMessage.getRetailerId(), syncMessage.getStoreNumber()), gsonProvider.gson.toJson(syncMessage))
        }
    }

    private uk.co.wonderlane.wlpos.entities.Product mapProduct(ResultSet resultSet, Map<Integer, uk.co.wonderlane.wlpos.entities.VatCode> vatCodes, Map<Integer, uk.co.wonderlane.wlpos.entities.Category> categories) throws SQLException {
        uk.co.wonderlane.wlpos.entities.Product product = new uk.co.wonderlane.wlpos.entities.Product()

        product.setId(resultSet.getInt("id"))
        product.setRetailerId(resultSet.getInt("retailerId"))
        product.setItemCode(resultSet.getString("itemCode"))
        product.setDescription(resultSet.getString("description"))
        product.setReceiptDescription(resultSet.getString("receiptDescription"))
        product.setCategory(categories.get(resultSet.getInt("categoryId")))
        product.setOpenPrice(resultSet.getBoolean("openPrice"))
        product.setZeroPrice(resultSet.getBoolean("zeroPrice"))
        product.setVatCode(vatCodes.get(resultSet.getInt("vatCodeId")))
        product.setUnitSize(resultSet.getString("unitSize"))
        product.setWeightedItem(resultSet.getBoolean("weightedItem"))
        product.setVatPercentageOverride(resultSet.getBigDecimal("vatPercentageOverride"))
        product.setDiscreetMessage(resultSet.getString("discreetMessage"))
        product.setStatus(ProductStatus.valueOf(resultSet.getString("status")))
        product.setRestrictions(mapRestrictions(resultSet))
        product.setLocal(false)

        return product
    }

    private uk.co.wonderlane.wlpos.entities.VatCode mapVatCode(ResultSet resultSet) throws SQLException {
        uk.co.wonderlane.wlpos.entities.VatCode vatCode = new uk.co.wonderlane.wlpos.entities.VatCode()

        vatCode.setId(resultSet.getInt("id"))
        vatCode.setRetailerId(resultSet.getInt("retailerId"))
        vatCode.setCode(resultSet.getString("code").charAt(0))
        vatCode.setDescription(resultSet.getString("description"))
        vatCode.setPercentage(resultSet.getBigDecimal("percentage"))
        vatCode.setRetailerVatCode(resultSet.getString("retailerVatCode"))

        return vatCode
    }

    private uk.co.wonderlane.wlpos.entities.Category mapCategory(ResultSet resultSet) throws SQLException {
        uk.co.wonderlane.wlpos.entities.Category category = new uk.co.wonderlane.wlpos.entities.Category()

        category.setId(resultSet.getInt("id"))
        category.setRetailerId(resultSet.getInt("retailerId"))
        category.setParentId(resultSet.getInt("parentId"))
        category.setDescription(resultSet.getString("description"))
        category.setShortDescription(resultSet.getString("shortDescription"))
        category.setRetailerCategoryCode(resultSet.getString("retailerCategoryCode"))
        category.setRestrictions(mapRestrictions(resultSet))

        return category
    }

    private uk.co.wonderlane.wlpos.entities.Restrictions mapRestrictions(ResultSet resultSet) throws SQLException {
        uk.co.wonderlane.wlpos.entities.Restrictions restrictions = new uk.co.wonderlane.wlpos.entities.Restrictions()

        restrictions.setId(resultSet.getInt("restrictionsId"))

        restrictions.setMinOpenPrice(resultSet.getBigDecimal("minOpenPrice"))
        if (resultSet.wasNull()) {
            restrictions.setMinOpenPrice(null)
        }

        restrictions.setMaxOpenPrice(resultSet.getBigDecimal("maxOpenPrice"))
        if (resultSet.wasNull()) {
            restrictions.setMaxOpenPrice(null)
        }

        restrictions.setBuyerIdRequired(resultSet.getBoolean("buyerIdRequired"))
        if (resultSet.wasNull()) {
            restrictions.setBuyerIdRequired(null)
        }

        restrictions.setBuyerIdForced(resultSet.getBoolean("buyerIdForced"))
        if (resultSet.wasNull()) {
            restrictions.setBuyerIdForced(null)
        }

        restrictions.setBuyerAgeRestriction(resultSet.getInt("buyerAgeRestriction"))
        if (resultSet.wasNull()) {
            restrictions.setBuyerAgeRestriction(null)
        }

        restrictions.setBuyerChallengeAge(resultSet.getInt("buyerChallengeAge"))
        if (resultSet.wasNull()) {
            restrictions.setBuyerChallengeAge(null)
        }

        restrictions.setSellerAgeRestriction(resultSet.getInt("sellerAgeRestriction"))
        if (resultSet.wasNull()) {
            restrictions.setSellerAgeRestriction(null)
        }

        restrictions.setRefundAllowed(resultSet.getBoolean("refundAllowed"))
        if (resultSet.wasNull()) {
            restrictions.setRefundAllowed(null)
        }

        restrictions.setMarkdownAllowed(resultSet.getBoolean("markdownAllowed"))
        if (resultSet.wasNull()) {
            restrictions.setMarkdownAllowed(null)
        }

        restrictions.setDiscountAllowed(resultSet.getBoolean("discountAllowed"))
        if (resultSet.wasNull()) {
            restrictions.setDiscountAllowed(null)
        }

        restrictions.setCreditPaymentAllowed(resultSet.getBoolean("creditPaymentAllowed"))
        if (resultSet.wasNull()) {
            restrictions.setCreditPaymentAllowed(null)
        }

        restrictions.setQuantityChangeAllowed(resultSet.getBoolean("quantityChangeAllowed"))
        if (resultSet.wasNull()) {
            restrictions.setQuantityChangeAllowed(null)
        }

        restrictions.setQuantityChangeForced(resultSet.getBoolean("quantityChangeForced"))
        if (resultSet.wasNull()) {
            restrictions.setQuantityChangeForced(null)
        }

        restrictions.setReceiptPrintForced(resultSet.getBoolean("receiptPrintForced"))
        if (resultSet.wasNull()) {
            restrictions.setReceiptPrintForced(null)
        }

        return restrictions
    }

    private uk.co.wonderlane.wlpos.entities.Message mapMessage(ResultSet resultSet) throws SQLException {
        uk.co.wonderlane.wlpos.entities.Message message = new uk.co.wonderlane.wlpos.entities.Message()

        message.setId(resultSet.getInt("id"))
        message.setRetailerId(resultSet.getInt("retailerId"))
        message.setText(resultSet.getString("text"))
        message.setRetailerMessageCode(resultSet.getString("retailerMessageCode"))
        message.setStartDate(new DateTime(resultSet.getTimestamp("startDate"), DateTimeZone.UTC))
        message.setEndDate(new DateTime(resultSet.getTimestamp("endDate"), DateTimeZone.UTC))
        message.setDisplayOncePerTransaction(resultSet.getBoolean("displayOncePerTransaction"))
        message.setDisplayOncePerItem(resultSet.getBoolean("displayOncePerItem"))

        return message
    }

    private boolean checkProductHasPriceForStore(def product, def storeId) {
        return product.variants.findAll { it.storeId == null || it.storeId == storeId }
                .stream().map({ it.getRetailPrice() })
                .collect(Collectors.toList()).findAll({ it != null && it > BigDecimal.ZERO }).size() > 0
    }
}