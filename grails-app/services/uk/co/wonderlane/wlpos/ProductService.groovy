package uk.co.wonderlane.wlpos

import grails.gorm.transactions.Transactional
import org.apache.commons.lang3.StringUtils
import org.hibernate.Session
import org.hibernate.Transaction
import org.hibernate.criterion.Projections
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import uk.co.wonderlane.wlpos.dataaccess.DatabaseCredentials
import uk.co.wonderlane.wlpos.dataaccess.MySqlDal
import uk.co.wonderlane.wlpos.entities.SyncMessage
import uk.co.wonderlane.wlpos.enums.LocationsType
import uk.co.wonderlane.wlpos.enums.ProductAttributeType
import uk.co.wonderlane.wlpos.enums.ProductHistoryType
import uk.co.wonderlane.wlpos.enums.SyncMessageType
import uk.co.wonderlane.wlpos.reporting.ReportColumns
import uk.co.wonderlane.wlpos.reporting.ReportType
import uk.co.wonderlane.wlpos.utils.QuantityHelper

import java.sql.*
import java.util.Date
import java.util.stream.Collectors

@Transactional
class ProductService extends MySqlDal {

    def springSecurityService
    def sessionFactory
    def rabbitService
    def gsonProvider
    def pricingClassificationService

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
        }?.find()
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
        }?.find()
    }

    uk.co.wonderlane.wlpos.entities.ProductVariant getProductVariant(int storeId, int productVariantId) throws SQLException {
        Connection conn
        CallableStatement cstmt
        try {
            conn = getConnection()
            cstmt = conn.prepareCall("{ call getProductVariant(?, ?) }")
            cstmt.setInt(1, storeId)
            cstmt.setInt(2, productVariantId)
            ResultSet rs = cstmt.executeQuery()
            if (rs.next()) {
                return mapProductVariant(rs)
            }
            return null
        } catch (Exception ex) {
            log.error("Order create exception found when retrieving product variant from DB, Exception " + ex.getMessage())
            throw ex
        } finally {
            if (connection != null) {
                connection.close()
            }
        }
    }

    List<uk.co.wonderlane.wlpos.entities.ProductVariant> getAllProductVariantsForSku(long sku) throws SQLException {
        Connection conn
        CallableStatement cstmt
        try {
            conn = getConnection()
            cstmt = conn.prepareCall("{ call getAllProductVariantsForSku(?, ?) }")
            cstmt.setInt(1, springSecurityService.principal.retailerId)
            cstmt.setLong(2, sku)
            ResultSet rs = cstmt.executeQuery()
            List<uk.co.wonderlane.wlpos.entities.ProductVariant> variants = new ArrayList<>()
            while (rs.next()) {
                variants.add(mapProductVariant(rs))
            }
            return variants
        } catch (Exception ex) {
            log.error("Exception thrown when retrieving product variant from DB, Exception " + ex.getMessage())
            throw ex
        } finally {
            if (connection != null) {
                connection.close()
            }
        }
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

    List<Barcode> getBarcodesExists(String barcode, int supplierId, int excludedPackId, Integer excludedVariantId) {
        return getBarcodes(barcode, supplierId, excludedPackId, excludedVariantId, true)
    }

    List<Barcode> getBarcodes(String barcode, int supplierId, int excludedPackId, Integer excludedVariantId, boolean checkOnlyExists) {
        DateTime utcNow = DateTime.now(DateTimeZone.UTC);
         return Barcode.createCriteria().list {
            eq('barcode', barcode)
            eq('retailerId', springSecurityService.principal.retailerId)
            pack {
                not {
                    eq('id', excludedPackId)
                }

                supplier {
                    eq('id', supplierId)
                }

                if (excludedVariantId != null) {
                    productVariant {
                        not {
                            eq('id', excludedVariantId)
                        }
                    }
                }
            }
            if (checkOnlyExists) {
                le('effectiveDate', utcNow)

                // Use projections to stop us getting everything when we're just checking if any exist
                projections {
                    Projections.property("packId")
                    Projections.property("barcode")
                    Projections.property("recordStatus")
                }
            }
        } as List<Barcode>
    }

    def saveProduct(Product product) {
        product.save()
    }

    def saveProduct(Product product, List<ProductVariant> productVariantList, ArrayList<ProductAttributeValues> updatedAttributes) {
        if (productVariantList != null && productVariantList.size() > 0) {
            productVariantList.each { pv -> product.addToVariants(pv) }
        }

        if (updatedAttributes != null && updatedAttributes.size() > 0) {
            updatedAttributes.each { productAttributeValues -> product.addToProductAttributeValues(productAttributeValues)}
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
                    if (!StringUtils.isEmpty(barcode.barcode)) {
                        barcode.save()
                    }
                }
            }
            variant.packs.each { pack ->
                pack.barcodez.each { barcode ->
                    if (barcode.hasProperty('delete') && barcode.delete) {
                        Barcode deletedBarcode = new Barcode()
                        deletedBarcode.pack = barcode.pack
                        deletedBarcode.retailerId = barcode.retailerId
                        deletedBarcode.barcode = barcode.barcode
                        deletedBarcode.effectiveDate = barcode.effectiveDeleteDate
                        deletedBarcode.recordStatus = 'D'
                        deletedBarcode.save()
                    } else if (barcode instanceof Barcode) {
                        barcode.retailerId = product.retailerId
                        barcode.effectiveDate = variant.effectiveDate
                        barcode.pack = pack
                        barcode.save()
                    }
                }
            }
        }
    }

    def saveLocations(Product product) {
        product?.variants?.each { variant ->
            def variantLocations = Location.findAllByStoreIdAndSkuAndDeleted(springSecurityService.principal.storeId, variant.sku, false)
            variant.locationz?.each { location ->
                if (location.hasProperty('delete') && location.delete) {
                    Location deletedLocation = new Location()
                    deletedLocation.sku = location.sku
                    deletedLocation.storeId = location.storeId
                    deletedLocation.aisle = location.aisle
                    deletedLocation.bay = location.bay
                    deletedLocation.shelf = location.shelf
                    deletedLocation.position = location.position
                    deletedLocation.location = location.location
                    deletedLocation.shelfCapacity = location.shelfCapacity
                    deletedLocation.minimumDisplayQuantity = location.minimumDisplayQuantity
                    deletedLocation.locationHierarchy = location.locationHierarchy
                    deletedLocation.save()
                } else if (location instanceof Location) {
                    def existingLocation = variantLocations?.find { existingLocation -> existingLocation.id == location.id }
                    if (existingLocation && existingLocation.id > 0) {
                        updateLocation(existingLocation, location, location.sku)
                    } else {
                        location.save()
                    }
                }
            }
        }
    }

    boolean isLocationValid(Product product) {
        def isValid = true

        for (ProductVariant pv : product?.variants) {
            for (Location location : pv.locationz) {
                if (!location.validate()) {
                    product.errors.reject('product.location.validation.error', [String.valueOf(pv.sku)] as Object[],
                            'product.location.validation.error.default')
                    isValid = false
                    break
                }
            }
        }

        return isValid
    }


    def saveProductVariant(ProductVariant productVariant) {
        productVariant.save()
    }

    def saveProductPrices(Product product, List<ProductPrice> productPrices, List<ProductHistory> productHistories) {
        Session session = sessionFactory.openSession()
        Transaction transaction = session.beginTransaction()

        productPrices.eachWithIndex { productPrice, index ->
            if (productPrice?.price != null && productPrice.price.compareTo(BigDecimal.ZERO) >= 0) {
                if (!productPrice.validate()) {
                    if (productPrice.price.compareTo(BigDecimal.ZERO) <= 0 || productPrice.price.compareTo(BigDecimal.valueOf(99999.99)) >= 0) {
                        product.errors.reject('productPrice.price.range.error', ['0.01', '99,999.99', String.valueOf(productPrice.price)] as Object[],
                                'productPrice.price.range.default.error')
                    }

                    return product
                }

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

    def searchProductsHql(String searchTerm, String searchBy, int maxResults, int startIndex, String sortColumn, String sortOrder, Boolean filterWithPendingChanges = false) {
        def now = DateTime.now(DateTimeZone.UTC)

        def barcodeSkus = []
        def barcodePacks = []

        searchTerm = searchTerm ? searchTerm.trim() : ""

        if ((searchBy == "everything" || searchBy == "barcode") && searchTerm?.length() > 2) {
            def validBarcodeSkus = [] //Declare valid barcode sku list
            def validBarcodePacks = [] //Declare valid barcode pack list

            //Get all barcodes which like search term (Ex : search term - 111 )
            def barcodes = Barcode.findAllByBarcodeLikeAndRetailerIdAndEffectiveDateLessThanEquals("%$searchTerm%", springSecurityService.principal.retailerId, now)

            //Group barcodes to map of sku --> {1 : [111(C) , 111 (D), 1114(C) ,1115(C), 1117(C)], 2:[1119(C)]}
            def skuMap = barcodes?.findAll { it.sku != null }?.groupBy { it.sku }
            def packMap = barcodes?.findAll { it.pack != null }?.groupBy { it.pack.id }

            findActiveBarcodes(skuMap, validBarcodeSkus)
            findActiveBarcodes(packMap, validBarcodePacks)

            barcodeSkus = validBarcodeSkus?.unique()
            barcodePacks = validBarcodePacks?.unique()
        }

        def queryParams = [retailerId: springSecurityService.principal.retailerId, max: maxResults, offset: startIndex]
        def countQueryParams = [retailerId: springSecurityService.principal.retailerId]

        if (springSecurityService.principal.storeId) {
            queryParams.range = springSecurityService.principal.range
            countQueryParams.range = springSecurityService.principal.range

            queryParams.storeId = springSecurityService.principal.storeId
            countQueryParams.storeId = springSecurityService.principal.storeId
        }

        String querySelect = "SELECT DISTINCT(p) "
        String searchQuery = """FROM Product p """

        if (springSecurityService.principal.storeId) {
            // Store level.
            searchQuery += """JOIN ProductVariant pv ON p.id = pv.product AND (pv.storeId IS NULL OR pv.storeId = :storeId) """
        } else {
            // Head office level.
            searchQuery += """JOIN ProductVariant pv ON p.id = pv.product AND pv.storeId IS NULL """
        }

        searchQuery += """LEFT JOIN Pack pk ON pk.productVariant = pv.id
                          LEFT JOIN Barcode b ON pv.sku = b.sku AND b.retailerId = :retailerId """

        if (springSecurityService.principal.storeId) {
            // Store level.
            searchQuery += """LEFT JOIN RangeProduct rp ON p.id = rp.productId AND rp.range = :range """
        }
        searchQuery += """LEFT JOIN SelType st ON p.selType = st.id """

        searchQuery += """WHERE p.retailerId = :retailerId """

        if (springSecurityService.principal.storeId) {
            // Store level.
            searchQuery += """AND (rp.productId IS NOT NULL OR pv.storeId IS NOT NULL) """
        }

        if (searchBy == "everything") {
            queryParams.barcodeSkus = barcodeSkus
            queryParams.barcodePacks = barcodePacks
            queryParams.searchTerm = "%${searchTerm}%"
            countQueryParams.barcodeSkus = barcodeSkus
            countQueryParams.barcodePacks = barcodePacks
            countQueryParams.searchTerm = "%${searchTerm}%"

            searchQuery += """AND (pv.sku IN (:barcodeSkus)
                                   OR pk.id IN (:barcodePacks)
                                   OR p.itemCode LIKE :searchTerm
                                   OR p.description LIKE :searchTerm """

            if (searchTerm.isNumber()) {
                queryParams.searchTermLong = Long.parseLong(searchTerm)
                countQueryParams.searchTermLong = Long.parseLong(searchTerm)

                searchQuery += """OR pv.sku = :searchTermLong) """
            } else {
                searchQuery += """) """
            }
        } else if (searchBy == "itemCode") {
            queryParams.searchTerm = "%${searchTerm}%"
            countQueryParams.searchTerm = "%${searchTerm}%"

            searchQuery += """AND (p.itemCode LIKE :searchTerm """

            if (searchTerm.isNumber()) {
                queryParams.searchTermLong = Long.parseLong(searchTerm)
                countQueryParams.searchTermLong = Long.parseLong(searchTerm)

                searchQuery += """OR pv.sku = :searchTermLong) """
            } else {
                searchQuery += """) """
            }
        } else if (searchBy == "description") {
            queryParams.searchTerm = "%${searchTerm}%"
            countQueryParams.searchTerm = "%${searchTerm}%"

            searchQuery += """AND p.description LIKE :searchTerm """
        } else if (searchBy == "barcode") {
            queryParams.barcodeSkus = barcodeSkus
            queryParams.barcodePacks = barcodePacks
            countQueryParams.barcodeSkus = barcodeSkus
            countQueryParams.barcodePacks = barcodePacks

            searchQuery += """AND (pv.sku IN (:barcodeSkus)
                                     OR pk.id IN (:barcodePacks)) """
        }

        if (sortColumn == "id" || sortColumn == "description") {
            searchQuery += """ORDER BY p.${sortColumn} ${sortOrder}"""
        } else if (sortColumn == "price") {
            searchQuery += """ORDER BY pv.${sortColumn} ${sortOrder}"""
        }

        def products = Product.executeQuery(querySelect + searchQuery, queryParams)
        def allProducts = Product.executeQuery(querySelect + searchQuery, countQueryParams)

        if (filterWithPendingChanges) {
            products = products.findAll { product ->
                product?.getEffectiveDatesForFutureChanges()?.size() > 1
            }

            allProducts = allProducts.findAll { product ->
                product?.getEffectiveDatesForFutureChanges()?.size() > 1
            }
        }

        def results = [:]
        results.products = products
        results.totalCount = allProducts.size()

        return results
    }

    private static void findActiveBarcodes(Map<Long, List<Barcode>> idToBarcodeMap, ArrayList validBarcodes) {
        for (Map.Entry<Long, List<Barcode>> entry : idToBarcodeMap?.entrySet()) {

            //Group sku list int map of barcode
            def barcodeMap = entry.getValue()?.groupBy { it.barcode }

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
                    validBarcodes.add(entry.getKey())
                    break
                }
            }
        }
    }

    def searchProductPrices(String searchTerm, Integer categoryId, Integer productGroupId) {
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

            if (productGroupId != null) {
                cstmt.setInt(5, productGroupId)
            } else {
                cstmt.setNull(5, Types.INTEGER)
            }

            ResultSet rs = cstmt.executeQuery()

            try {
                while (rs.next()) {
                    def result = [:]
                    result.productId = rs.getInt("id")
                    result.sku = rs.getLong("sku")
                    result.itemCode = rs.getString("itemCode")
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

    def searchRangeProducts(String searchTerm, Integer categoryId, Integer productGroupId) {
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

            if (productGroupId != null) {
                cstmt.setInt(5, productGroupId)
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

    def undeleteRangeProduct(RangeProduct rangeProduct) {
        rangeProduct?.deleted = false
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

    def sendProductPriceUpdate(def prices, List<Store> stores) {
        if (isSingleStageSel()) {
            sendProductPriceUpdateToRabbitMq(prices, stores)
        }
    }

    def sendProductUpdate(List<Product> products, List<Store> stores) {
        sendProductUpdate(products, stores, true)
    }
    def sendProductUpdate(List<Product> products, List<Store> stores, boolean insert) {
        if (!rabbitService.isOpen()) {
            throw new Exception("Rabbit MQ not available")
        }

        stores?.each { Store store ->
            List<uk.co.wonderlane.wlpos.entities.Product> productEntities = new ArrayList<>()
            products.forEach({
                uk.co.wonderlane.wlpos.entities.Product productEntity = it.getProduct(store.id, store.priceBand)
                List<ProductVariant> variants = getFilteredProductVariantsWithPriceForStore(productEntity, store.id)
                if (!variants.isEmpty()) {
                    // Only send the update to the store if there are variants to send. This could mean the store has
                    // old variants that don't get deleted but the alternative is sending incomplete product data.
                    productEntity.setVariants(variants)
                    productEntities.add(productEntity)
                }
            })

            if (!productEntities.isEmpty()) {
                SyncMessage syncMessage = new SyncMessage(SyncMessageType.PRODUCT, springSecurityService.principal.retailerId, store.config.storeNumber, store.id, 0)
                syncMessage.setInsert(insert)
                syncMessage.setProducts(productEntities)

                log.println("Syncing ${productEntities.size()} product updates to store ${store.config.storeNumber} (insert: $insert)")

                rabbitService.sendMessage(syncMessage)

                productEntities.forEach({
                    def pricingClassificationId = it?.restrictions?.pricingClassificationId

                    if (pricingClassificationId != null) {
                        List<uk.co.wonderlane.wlpos.entities.PricingClassification> pricingClassificationList = new ArrayList<>()

                        def pricingClassification = pricingClassificationService.getPricingClassificationById(pricingClassificationId)
                        pricingClassificationList.add(pricingClassification.getPricingClassification())

                        SyncMessage pricingSyncMessage = new SyncMessage(SyncMessageType.PRICING_CLASSIFICATION, springSecurityService.principal.retailerId, 0, 0, 0)
                        pricingSyncMessage.setInsert(insert)
                        pricingSyncMessage.setPricingClassifications(pricingClassificationList)

                        log.println("Syncing ${pricingClassificationList.size()} pricing classification updates to store ${store.config.storeNumber} (insert: $insert)")
                        
                        rabbitService.sendMessage(pricingSyncMessage)
                    }
                })
            }
        }
    }

    def isSingleStageSel() {
        if (springSecurityService.principal.retailer && springSecurityService.principal.retailer.config.twoStageSel) {
            return false
        }
        return true
    }

    def syncProductUpdatesToAllStoresForRetailer(List<Integer> productIds) {
        if (isSingleStageSel()) {
            syncProductListUpdatesToStores(productIds, Store.findAllByRetailerId(springSecurityService.principal.retailerId))
        }
    }

    def syncProductUpdatesToSingleStore(List<Integer> productIds, Integer storeId) {
        syncProductListUpdatesToStores(productIds.unique(), [Store.findById(storeId)])
    }

    private void syncProductListUpdatesToStores(List<Integer> productIds, List<Store> stores) {
        def productIdsAsInt = productIds.findAll { it != null && it > 0 }.stream().map({ it.intValue() }).collect(Collectors.toSet())
        productIdsAsInt.removeAll(Collections.singleton(null))
        if (productIdsAsInt && productIdsAsInt?.size() > 0) {
            sendProductUpdate(Product.findAllByIdInList(new ArrayList<>(productIdsAsInt)), stores)
        }
    }

    private void sendProductPriceUpdateToRabbitMq(def prices, List<Store> stores) {
        if (!rabbitService.isOpen()) {
            throw new Exception("Rabbit MQ not available")
        }
        stores?.each { Store store ->
            SyncMessage syncMessage = new SyncMessage(SyncMessageType.PRICE_CHANGE, springSecurityService.principal.retailerId, store.config.storeNumber, store.id, 0)
            syncMessage.setInsert(true)
            syncMessage.setStoreId(store.config.storeNumber)
            syncMessage.setProductPrices(prices)

            log.println("Syncing ${prices.size()} price updates to store ${store.config.storeNumber}")

            rabbitService.sendMessage(syncMessage)
        }
    }

    private static List<ProductVariant> getFilteredProductVariantsWithPriceForStore(uk.co.wonderlane.wlpos.entities.Product product, Integer storeId) {
        if (product.isZeroPrice()) {
            return product.getVariants() // already retrieved using a store id so is fine to return the whole list
        }
        return product.variants.findAll {
            (it.storeId == null || it.storeId == storeId)
                    && it.getRetailPrice() != null && it.getRetailPrice() > BigDecimal.ZERO
        }
    }

    Location deepCopyExistingLocation(Location existingLocation) {
        Location newLocation = new Location()
        newLocation.id = existingLocation.id
        newLocation.storeId = existingLocation.storeId
        newLocation.sku = existingLocation.sku
        newLocation.aisle = existingLocation.aisle
        newLocation.bay = existingLocation.bay
        newLocation.shelf = existingLocation.shelf
        newLocation.position = existingLocation.position
        newLocation.location = existingLocation.location
        newLocation.shelfCapacity = existingLocation.shelfCapacity
        newLocation.minimumDisplayQuantity = existingLocation.minimumDisplayQuantity
        newLocation.locationHierarchy = existingLocation.locationHierarchy
        newLocation.locationDescription = existingLocation.locationDescription
        newLocation.locationNumber = existingLocation.locationNumber
        return newLocation
    }

    private void updateLocation(def locationToBeUpdated, def editedLocation, def sku) {
        def locationsType = springSecurityService.principal.retailer.config.locationsType
        locationToBeUpdated.storeId = springSecurityService.principal.storeId
        locationToBeUpdated.sku = sku

        if (locationsType == LocationsType.ADVANCED) {
            locationToBeUpdated.aisle = editedLocation.aisle
            locationToBeUpdated.bay = editedLocation.bay
            locationToBeUpdated.shelf = editedLocation.shelf
            locationToBeUpdated.position = editedLocation.position
            locationToBeUpdated.locationHierarchy = editedLocation.locationHierarchy
            locationToBeUpdated.locationDescription = editedLocation.locationDescription
            locationToBeUpdated.locationNumber = editedLocation.locationNumber
        } else if (locationsType == LocationsType.SIMPLE) {
            locationToBeUpdated.location = editedLocation.location
        }
        locationToBeUpdated.shelfCapacity = editedLocation.shelfCapacity
        locationToBeUpdated.minimumDisplayQuantity = editedLocation.minimumDisplayQuantity
    }

    private static uk.co.wonderlane.wlpos.entities.ProductVariant mapProductVariant(ResultSet resultSet) throws SQLException {
        uk.co.wonderlane.wlpos.entities.ProductVariant productVariant = new uk.co.wonderlane.wlpos.entities.ProductVariant()

        productVariant.setId(resultSet.getInt("id"))
        productVariant.setProductId(resultSet.getInt("productId"))
        productVariant.setStoreId(resultSet.getInt("storeId"))
        productVariant.setSku(resultSet.getLong("sku"))

        productVariant.setRetailPrice(resultSet.getBigDecimal("price"))
        if (resultSet.wasNull()) {
            productVariant.setRetailPrice(null)
        }

        productVariant.setCostPrice(resultSet.getBigDecimal("costPrice"))
        if (resultSet.wasNull()) {
            productVariant.setCostPrice(null)
        }

        productVariant.setWeightedAverageCostPrice(resultSet.getBigDecimal("weightedAverageCostPrice"))
        if (resultSet.wasNull()) {
            productVariant.setWeightedAverageCostPrice(null)
        }

        productVariant.setSize(resultSet.getString("size"))
        if (resultSet.wasNull()) {
            productVariant.setSize(null)
        }

        productVariant.setColour(resultSet.getString("colour"))
        if (resultSet.wasNull()) {
            productVariant.setColour(null)
        }
        productVariant.setQuantityInStock(QuantityHelper.quantityOrDefault(resultSet, "quantityInStock", BigDecimal.ZERO))
        productVariant.setQuantityOnOrder(QuantityHelper.quantityOrDefault(resultSet, "quantityOnOrder", BigDecimal.ZERO))
        productVariant.setMinimumStockLevel(resultSet.getInt("minimumStockLevel"))
        productVariant.setEffectiveDate(new DateTime(resultSet.getTimestamp("effectiveDate"), DateTimeZone.UTC))

        return productVariant
    }

    def getRetailerSelTypes(Long retailerId) {
        def session = sessionFactory.currentSession
        def query = session.createNativeQuery("CALL getRetailerSelTypes(:retailerId)")
        query.setParameter("retailerId", retailerId)

        def rawResults = query.list()

        def results = rawResults.collect { row ->
            def selType = new SelType()
            selType.id = row[0] as Integer
            selType.name = row[1] as String
            selType.retailerId = retailerId
            return selType
        }

        return results.sort { it.id }
    }

    def processedValueForNullEmpty(String value, ProductAttributeType productAttributeType) {
        if (value == null) {
            value = "";
        } else if (productAttributeType == ProductAttributeType.BOOLEAN && value == "false") {
            value = "";
        }

        return value;
    }

    List<ProductAttributeValues> getProductInformation(Product product) {
        List<ProductAttributeValues> returnedAttributeValuesList = new ArrayList<>()
        List<ProductAttributeValues> productAttributeValuesList = new ArrayList<>()
        int retailerId = springSecurityService.principal.retailerId
        if (product != null) {// If product id does not exists there can not be any history to return
            productAttributeValuesList = product?.productAttributeValues ?: new ArrayList<ProductAttributeValues>()
        }
        //Try to load from product attribute table

        //If it is empty then load from attribute table
        List<ProductAttributes> productAttributeList = ProductAttributes.findAllByRetailerIdAndDisplayAttribute(retailerId, true)

        HashMap<Integer, ProductAttributes> productAttributesMap = productAttributeList?.collectEntries {[(it.id): it]} ?: [:] as HashMap<Integer, ProductAttributes>

        productAttributeValuesList?.each {
            productAttribute -> {
                ProductAttributes productAttributes = productAttributesMap.get(productAttribute.productAttributeId)
                if (productAttributes) {
                    productAttribute.productAttributes = productAttributes
                    returnedAttributeValuesList.add(productAttribute)
                }
            }
        }

        def existingProductAttributeIds = productAttributeValuesList*.productAttributeId.toSet()
        def missingProductAttributes = productAttributeList.findAll {
            !existingProductAttributeIds.contains(it.id)
        }

        missingProductAttributes.each { productAttribute ->
            ProductAttributeValues dummyEntry = new ProductAttributeValues(
                    retailerId: retailerId,
                    productAttributeId: productAttribute?.id,
                    value: productAttribute?.defaultValue, // Use defaultValue if available
                    productAttributes: productAttribute
            )
            returnedAttributeValuesList << dummyEntry
        }
        return returnedAttributeValuesList?.sort { it?.productAttributeId }
    }

    ArrayList<ProductAttributeValues> getUpdatedProductAttributeValues(Product product, ProductCommand editedProduct, ProductHistoryBuilder builder, effectiveDate) {
        ArrayList<ProductAttributeValues> updatedOrNewAttributes = []

        if (builder == null){
            builder = new ProductHistoryBuilder(product.id, springSecurityService, effectiveDate)
        }

        // Create a map with composite keys for existing product overriden attributes
        def existingAttributesMap = product?.productAttributeValues?.collectEntries {
            ["${it.productAttributeId}_${it.productId}_${it.retailerId}": it]} ?: [:]

        // Create a map for all product attributes
        def productAttributesMap = ProductAttributes.findAllByRetailerId(
                springSecurityService.principal.retailerId)?.collectEntries { [(it.id): it] } ?: [:]

        // Loop through the edited product attributes
        editedProduct?.productAttributeValues?.each { editedAttr ->
            def key = "${editedAttr.productAttributeId}_${product.id}_${editedAttr.retailerId}"
            def existingAttr = existingAttributesMap.get(key)
            def productAttributes = productAttributesMap.get(editedAttr.productAttributeId)

            if (productAttributes) { //Check master product attribute exists

                if (productAttributes?.type == ProductAttributeType.BOOLEAN && !editedAttr?.value) {
                    // Set default value for BOOLEAN type attributes
                    // From UI when user deselect checkbox value will be null so assign edited value as false for those cases
                    editedAttr.value = 'false'
                }

                //server level validations
                //This include validation if type is text then it's length
                //If type is numeric then it's values
                boolean isValidationPassed = isProductAttributeUpdateValidationsPassed(productAttributes, editedAttr, product)

                if (isValidationPassed) {
                    if (existingAttr) {
                        //If updated attribute already on `productattributevalues` table
                        //If so then check updated value is change to current value
                        //If it does then update current value to new value
                        def existingAttrProcessedDefaultValue = processedValueForNullEmpty(existingAttr?.value, productAttributes?.type)
                        def editedAttrProcessedValue = processedValueForNullEmpty(editedAttr?.value, productAttributes?.type)

                        if (existingAttrProcessedDefaultValue != editedAttrProcessedValue) {
                            builder.compare(editedAttr?.attributeName, existingAttr?.value, editedAttr?.value, ProductHistoryType.PRODUCT_ATTRIBUTE)
                            existingAttr?.value = editedAttr?.value
                        }
                    } else {
                        def productAttrProcessedDefaultValue = processedValueForNullEmpty(productAttributes?.defaultValue, productAttributes?.type)
                        def editedAttrProcessedValue = processedValueForNullEmpty(editedAttr?.value, productAttributes?.type)

                        if (productAttrProcessedDefaultValue != editedAttrProcessedValue) {
                            // ignore matching attributes and close attributes values like ""/null.
                            def newAttr = new ProductAttributeValues(
                                    retailerId: editedAttr?.retailerId,
                                    productAttributeId: editedAttr?.productAttributeId,
                                    value: editedAttr?.value,
                                    id: editedAttr?.productAttributeId,
                                    attributeName: editedAttr?.attributeName,
                                    attributeType: editedAttr?.attributeType
                            )

                            // During the initial product creation, don't record the changes to product attributes.
                            builder.compare(editedAttr?.attributeName, productAttributes?.defaultValue, editedAttr?.value, ProductHistoryType.PRODUCT_ATTRIBUTE)
                            updatedOrNewAttributes << newAttr
                        }
                    }
                }
            }
        }
        return updatedOrNewAttributes
    }

    boolean isProductAttributeUpdateValidationsPassed(productAttributes, editedAttr, product){
        boolean isValidationPassed = true
        if (productAttributes?.type == ProductAttributeType.TEXT && editedAttr?.value != null) {
            if (editedAttr?.value?.length() > 50) {
                product.errors.reject('productAttributeValues.text.max.size', [productAttributes?.name] as Object[],
                        "Product attribute ${productAttributes?.name} validation failed")
                isValidationPassed = false
            }
        } else if (productAttributes?.type == ProductAttributeType.NUMERIC && editedAttr?.value != null) {
            try {
                // Try parsing the value as a BigDecimal
                BigDecimal numericValue = new BigDecimal(editedAttr?.value)

                // Check if the value exceeds the maximum allowed value
                if (numericValue.compareTo(BigDecimal.ZERO) < 0 || numericValue.compareTo(new BigDecimal("999999.99")) > 0) {
                    product.errors.reject('productAttributeValues.numeric.default.out.of.range', [productAttributes?.name] as Object[],
                            "Product attribute ${productAttributes?.name} validation failed")
                    isValidationPassed = false
                }
            } catch (Exception e) {
                // If the value is not a valid number, return the appropriate error message
                product.errors.reject('productAttributeValues.numeric.default.not.a.number', [productAttributes?.name] as Object[],
                        "Product attribute ${productAttributes?.name} validation failed")
                isValidationPassed = false
            }
        }
        return isValidationPassed
    }

    def getProducts(List<Long> skus) {
        def criteria = Product.createCriteria()

        return criteria.list {
            'in'("itemCode", skus)
            eq("retailerId", springSecurityService.principal.retailerId)
        }
    }

}
