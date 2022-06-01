package uk.co.wonderlane.wlpos

import grails.gorm.transactions.Transactional

import org.hibernate.Session
import org.hibernate.Transaction
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import uk.co.wonderlane.wlpos.dataaccess.DatabaseCredentials
import uk.co.wonderlane.wlpos.dataaccess.MySqlDal
import uk.co.wonderlane.wlpos.enums.ProductStatus

import java.sql.CallableStatement
import java.sql.Connection
import java.sql.ResultSet
import java.sql.SQLException
import java.sql.Types

@Transactional
class ProductService extends MySqlDal {

    def springSecurityService
    def sessionFactory

    ProductService(DatabaseCredentials databaseCredentials) {
        super(databaseCredentials)
    }

    def getProductVariant(int id) {
        return ProductVariant.withCriteria(sort: "effectiveDate", order: "desc") {
            eq ("id", id)
            or {
                isNull("storeId")
                eq("storeId", springSecurityService.principal.storeId)
            }
            product {
                eq ("retailerId", springSecurityService.principal.retailerId)
            }
        }?.first() ?: null
    }

    def getProductVariant(long sku) {
        return ProductVariant.withCriteria(sort: "effectiveDate", order: "desc") {
            eq ("sku", sku)
            or {
                isNull("storeId")
                eq("storeId", springSecurityService.principal.storeId)
            }
            lte ("effectiveDate", DateTime.now(DateTimeZone.UTC))
            product {
                eq ("retailerId", springSecurityService.principal.retailerId)
            }
        }?.first() ?: null
    }

    // TODO make this method only return the current effective date. Currently it will return any which exist (sorted so that the active one is first (unless the description has changed)).
    def getProductVariants(List<Long> skus) {
        def criteria = ProductVariant.createCriteria()

        return criteria.list {
            "in" ("sku", skus)
            or {
                isNull("storeId")
                eq("storeId", springSecurityService.principal.storeId)
            }
            lte ("effectiveDate", DateTime.now(DateTimeZone.UTC))
            product {
                eq ("retailerId", springSecurityService.principal.retailerId)
            }

            and {
                product {
                    order ("description", "asc")
                }
                order ("effectiveDate", "desc")
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

    def saveBarcodes(Product product) {
        product?.variants?.each { variant ->
            variant.barcodez?.each { barcode ->
                if (barcode.delete) {
                    barcode.delete()
                } else {
                    barcode.save()
                }
            }
        }
    }

    def saveProductVariant(ProductVariant productVariant) {
        productVariant.save()
    }

    def saveProductPrices(List<ProductPrice> productPrices) {
        Session session = sessionFactory.openSession()
        Transaction transaction = session.beginTransaction()

        productPrices.eachWithIndex { productPrice, index ->
            session.save(productPrice)

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
        def productSearchCriteria = Product.createCriteria()

        def now = DateTime.now(DateTimeZone.UTC)

        def results = productSearchCriteria.list([offset: startIndex, max: maxResults]) {
            eq ("retailerId", springSecurityService.principal.retailerId)
            variants {
                or {
                    isNull("storeId")
                    eq("storeId", springSecurityService.principal.storeId)
                }
                lte ("effectiveDate", now)
            }

            if (searchBy == "everything") {
                or {
                    like ("itemCode", "%$searchTerm%")
                    if (searchTerm.isNumber()) {
                        variants {
                            eq ("sku", Long.parseLong(searchTerm))
                        }
                    }
                    like ("description", "%$searchTerm%")
                }
            } else if (searchBy == "itemCode") {
                or {
                    like("itemCode", "%$searchTerm%")
                    if (searchTerm.isNumber()) {
                        variants {
                            eq ("sku", Long.parseLong(searchTerm))
                        }
                    }
                }
            } else if (searchBy == "description") {
                like ("description", "%$searchTerm%")
            }

            if (sortColumn == "id" || sortColumn == "description") {
                order (sortColumn, sortOrder)
            } else if (sortColumn == "price") {
                order ("variants.price", sortOrder)
            }
        }

        // Criteria.list() with max and offset returns a totalCount, but for some reason I am having to read that value otherwise an error is thrown when trying to use it back in the controller.
        // I believe this may be related to the domain class being in an alternate datasource, but I think it's a bug in Grails. Actually, I think it's because the totalCount is lazily loaded
        // to prevent the double query immediately. But it's throwing a Hibernate session error if I don't request it here.
        int totalCount = results.totalCount

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
}