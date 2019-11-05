package uk.co.wonderlane.wlpos

import grails.gorm.transactions.Transactional
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import uk.co.wonderlane.wlpos.dataaccess.MySqlDal
import uk.co.wonderlane.wlpos.enums.ProductStatus

import java.sql.CallableStatement
import java.sql.Connection
import java.sql.ResultSet
import java.sql.SQLException

@Transactional
class ProductService extends MySqlDal {

    def springSecurityService

    ProductService(String host, int port, String database, String username, String password) {
        super(host, port, database, username, password)
    }

    def getProductVariant(int id) {
        return ProductVariant.get(id)
    }

    def getProduct(int id) {
        return Product.get(id)
    }

    def populateCurrentProductData(def product) {
        product.currentProductData = product.productDatas.sort { it.effectiveDate }.reverse().find { it.storeId == springSecurityService.principal.storeId && it.effectiveDate <= new Date() }
    }

    def searchProductsNew(String searchTerm, String searchBy, int maxResults, int startIndex, String sortColumn, String sortOrder) {
        Map<Integer, uk.co.wonderlane.wlpos.entities.Product> products = new LinkedHashMap<>()

        Connection conn = getConnection()
        CallableStatement cstmt = conn.prepareCall("{ call searchProducts(?, ?, ?, ?, ?, ?, ?, ?, ?) }")

        try {
            cstmt.setInt(1, springSecurityService.principal.retailerId)
            cstmt.setInt(2, springSecurityService.principal.storeId)
            cstmt.setString(3, searchTerm ?: "")
            cstmt.setString(4, searchBy ?: "everything")
            cstmt.setInt(5, maxResults)
            cstmt.setInt(6, startIndex)
            cstmt.setString(7, sortColumn ?: "id")
            cstmt.setString(8, sortOrder ?: "asc")
            cstmt.setString(9, new Date().format(DATE_TIME_FORMAT))

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
                    productVariant.setItemCode(rs.getString("itemCode"))
                    productVariant.setProductId(rs.getInt("productId"))
                    productVariant.setStoreId(rs.getInt("storeId"))
                    productVariant.setSize(rs.getString("size"))
                    productVariant.setColour(rs.getString("colour"))
                    productVariant.setBalanceOnHand(rs.getInt("balanceOnHand"))
                    productVariant.setBalanceOnOrder(rs.getInt("balanceOnOrder"))

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
        product.setStoreId(resultSet.getInt("storeId"))
        product.setItemCode(resultSet.getString("itemCode"))
        product.setDescription(resultSet.getString("description"))
        product.setReceiptDescription(resultSet.getString("receiptDescription"))
        product.setCategory(categories.get(resultSet.getInt("categoryId")))
        product.setDumpCode(resultSet.getBoolean("dumpCode"))
        product.setOpenPrice(resultSet.getBoolean("openPrice"))
        product.setZeroPrice(resultSet.getBoolean("zeroPrice"))
        product.setVatCode(vatCodes.get(resultSet.getInt("vatCodeId")))
        product.setUnitSize(resultSet.getString("unitSize"))
        product.setWeightedItem(resultSet.getBoolean("weightedItem"))
        product.setVatPercentageOverride(resultSet.getBigDecimal("vatPercentageOverride"))
        product.setDiscreetMessage(resultSet.getString("discreetMessage"))
        product.setRetailPrice(resultSet.getBigDecimal("price"))
        product.setCostPrice(resultSet.getBigDecimal("costPrice"))
        product.setStatus(ProductStatus.valueOf(resultSet.getString("status")))
        product.setEffectiveDate(new DateTime(resultSet.getTimestamp("effectiveDate"), DateTimeZone.UTC)) //LocalDateTime.fromDateFields(resultSet.getTimestamp("effectiveDate")).toDateTime())
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
        category.setRetailerParentCategoryCode(resultSet.getString("retailerParentCategoryCode"))
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
        message.setMessage(resultSet.getString("message"))
        message.setRetailerMessageCode(resultSet.getString("retailerMessageCode"))
        message.setStartDate(new DateTime(resultSet.getTimestamp("startDate"), DateTimeZone.UTC))
        message.setEndDate(new DateTime(resultSet.getTimestamp("endDate"), DateTimeZone.UTC))
        message.setDisplayOncePerTransaction(resultSet.getBoolean("displayOncePerTransaction"))
        message.setDisplayOncePerItem(resultSet.getBoolean("displayOncePerItem"))

        return message
    }
}