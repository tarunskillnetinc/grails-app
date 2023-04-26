package uk.co.wonderlane.wlpos


import grails.plugin.springsecurity.SpringSecurityService
import grails.testing.gorm.DataTest
import grails.testing.services.ServiceUnitTest
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import spock.lang.Specification
import uk.co.wonderlane.wlpos.dataaccess.DatabaseCredentials
import uk.co.wonderlane.wlpos.enums.PrintReceiptOption
import uk.co.wonderlane.wlpos.enums.ProductStatus
import uk.co.wonderlane.wlpos.enums.wlim.*
import uk.co.wonderlane.wlpos.helpers.ShelfEdgeLabelHelperService
import uk.co.wonderlane.wlpos.labelling.LabelTemplate
import uk.co.wonderlane.wlpos.labelling.LabelTemplateField
import uk.co.wonderlane.wlpos.labelling.LabelTemplateMapping

import java.sql.CallableStatement
import java.sql.Connection
import java.sql.ResultSet

class ShelfEdgeLabelServiceSpec extends Specification  implements ServiceUnitTest<ShelfEdgeLabelHelperService>, DataTest{

    ShelfEdgeLabelHelperService shelfEdgeLabelHelperService
    Connection mockConnection
    DatabaseCredentials databaseCredentials

    def setup() {
        databaseCredentials = Mock(DatabaseCredentials)
        mockConnection = Mock(Connection)
        shelfEdgeLabelHelperService = new ShelfEdgeLabelHelperService(databaseCredentials, mockConnection);
    }

    Class<?>[] getDomainClassesToMock(){
        return [ProductList, ProductListItem, ProductVariant, Product, Barcode, LabelTemplateMapping] as Class[]
    }

    //------------------Calling Load Label Template Mappings Action -------------//

    def 'Test the load label Template Mappings action'() {

        given:
        shelfEdgeLabelHelperService.springSecurityService = Stub(SpringSecurityService) {
            getPrincipal() >>new HashMap(){{
                put("id", 9);
                put("retailerId", 9);
                put("storeId", 234);
                put("storeNumber", 100)}}
        }

        LabelTemplateMapping labelTemplateMapping = new LabelTemplateMapping(retailerId : 9, printProcess :PrintProcess.PRODUCT_LABEL_BATCH, printType : PrintType.PDF)
        labelTemplateMapping.setLabelTemplate(new LabelTemplate())
        mockDomain(LabelTemplateMapping, [labelTemplateMapping])

        labelTemplateMapping.save(flush: true, failOnError: true)

        when: 'The getLabelTemplateMappings action is executed'
        List<LabelTemplateMapping> labelTemplateMappingReturnedList = shelfEdgeLabelHelperService.getLabelTemplateMappings(PrintProcess.PRODUCT_LABEL_BATCH, PrintType.PDF)

        then: 'The template mapping load successfully'
        assert labelTemplateMappingReturnedList.size() == 1
        assert labelTemplateMappingReturnedList.get(0).retailerId == 9
        assert labelTemplateMappingReturnedList.get(0).printProcess == PrintProcess.PRODUCT_LABEL_BATCH
        assert labelTemplateMappingReturnedList.get(0).printType == PrintType.PDF

    }

    //------------------Calling Load Label Templates Action -------------//

    def 'Test the load label Template action'() {

        given:
        shelfEdgeLabelHelperService.springSecurityService = Stub(SpringSecurityService) {
            getPrincipal() >>new HashMap(){{
                put("id", 9);
                put("retailerId", 9);
                put("storeId", 234);
                put("storeNumber", 100)}}
        }

        LabelTemplate labelTemplate = getLabelTemplate(1, 1, 1 , LabelTemplateFieldType.PRODUCT_DESCRIPTION, "1", 10)
        labelTemplate.setStationeryCode("ABC")
        labelTemplate.setName("Dummy label template")

        Set<LabelTemplateMapping> labelTemplateMappingSet = new ArrayList<>()
        LabelTemplateMapping labelTemplateMapping = new LabelTemplateMapping(retailerId : 9, printProcess :PrintProcess.PRODUCT_LABEL_BATCH, printType : PrintType.PDF)
        labelTemplateMapping.setLabelTemplate(labelTemplate)
        labelTemplateMappingSet.add(labelTemplateMapping)
        labelTemplate.setLabelTemplateMappings(labelTemplateMappingSet)
        mockDomain(LabelTemplate, [labelTemplate])

        labelTemplate.save(flush: true, failOnError: true)

        when: 'The getLabelTemplates action is executed'
        List<LabelTemplate> labelTemplateReturnedList = shelfEdgeLabelHelperService.getLabelTemplates(PrintProcess.PRODUCT_LABEL_BATCH, PrintType.PDF)

        then: 'The label template load successfully'
        assert labelTemplateReturnedList.size() == 1
        assert labelTemplateReturnedList.get(0).retailerId == 9
        assert labelTemplateReturnedList.get(0).getStationeryCode() == "ABC"
        assert labelTemplateReturnedList.get(0).getName() == "Dummy label template"
        assert labelTemplateReturnedList.get(0).getColumns() == 1
        assert labelTemplateReturnedList.get(0).getRows() == 1
        assert labelTemplateReturnedList.get(0).getMarginLeft() == new BigDecimal(1)
        assert labelTemplateReturnedList.get(0).getMarginTop() == new BigDecimal(1)
        assert labelTemplateReturnedList.get(0).getLabelHeight() == new BigDecimal(1)
        assert labelTemplateReturnedList.get(0).getLabelWidth() == new BigDecimal(1)

    }

    //------------------Calling Load Label Template By Id Action -------------//

    def 'Test the load label Template by id action'() {

        given:
        shelfEdgeLabelHelperService.springSecurityService = Stub(SpringSecurityService) {
            getPrincipal() >>new HashMap(){{
                put("id", 9);
                put("retailerId", 9);
                put("storeId", 234);
                put("storeNumber", 100)}}
        }

        LabelTemplate labelTemplate = getLabelTemplate(1, 1, 1 , LabelTemplateFieldType.PRODUCT_DESCRIPTION, "1", 10)
        labelTemplate.setId(100)
        labelTemplate.setStationeryCode("ABC")
        labelTemplate.setName("Dummy label template")

        Set<LabelTemplateMapping> labelTemplateMappingSet = new ArrayList<>()
        LabelTemplateMapping labelTemplateMapping = new LabelTemplateMapping(retailerId : 9, printProcess :PrintProcess.PRODUCT_LABEL_BATCH, printType : PrintType.PDF)
        labelTemplateMapping.setLabelTemplate(labelTemplate)
        labelTemplateMappingSet.add(labelTemplateMapping)
        labelTemplate.setLabelTemplateMappings(labelTemplateMappingSet)
        mockDomain(LabelTemplate, [labelTemplate])

        labelTemplate.save(flush: true, failOnError: true)

        when: 'The getLabelTemplate action is executed'
        LabelTemplate labelTemplateReturned = shelfEdgeLabelHelperService.getLabelTemplate(100)

        then: 'The label template load successfully'
        assert labelTemplateReturned
        assert labelTemplateReturned.retailerId == 9
        assert labelTemplateReturned.getStationeryCode() == "ABC"
        assert labelTemplateReturned.getName() == "Dummy label template"
        assert labelTemplateReturned.getColumns() == 1
        assert labelTemplateReturned.getRows() == 1
        assert labelTemplateReturned.getMarginLeft() == new BigDecimal(1)
        assert labelTemplateReturned.getMarginTop() == new BigDecimal(1)
        assert labelTemplateReturned.getLabelHeight() == new BigDecimal(1)
        assert labelTemplateReturned.getLabelWidth() == new BigDecimal(1)

    }

    //------------------Calling Generate Pdf Action -------------//

    def 'Test generate pdf action'() {

        shelfEdgeLabelHelperService.springSecurityService = Stub(SpringSecurityService) {
            getPrincipal() >>new HashMap(){{
                put("retailerId", 9);
                put("storeId", 234);
                put("storeNumber", 100)
            }}
        }

        LabelTemplateFieldType labelTemplateFieldType = LabelTemplateFieldType.PROMOTION_END_DATE
        LabelTemplate labelTemplate =  getLabelTemplate(1, 1, 1, labelTemplateFieldType, "1", 10)

        CallableStatement callableStatementMock =  Mock(CallableStatement)
        ResultSet resultSetMock = Mock(ResultSet)
        mockConnection.prepareCall(_) >> callableStatementMock
        callableStatementMock.executeQuery() >> resultSetMock
        resultSetMock.next() >>> productNextResult
        resultSetMock.getInt("id") >> productId
        resultSetMock.getString("itemCode") >> "1010"
        resultSetMock.getString("description") >> "Dummy description"
        resultSetMock.getString("unitSize") >> "10"
        resultSetMock.getBoolean("weightedItem") >> true
        resultSetMock.getBoolean("pricePerKg") >> true

        ResultSet secondResultSetMock = Mock(ResultSet)
        ResultSet thirdResultSetMock = Mock(ResultSet)

        callableStatementMock.moreResults() >> null
        callableStatementMock.getResultSet() >>> [secondResultSetMock, thirdResultSetMock]

        secondResultSetMock.next() >>> variantNextResult
        secondResultSetMock.getInt("id") >> 1
        secondResultSetMock.getLong("sku") >> 1010
        secondResultSetMock.getInt("productId") >> variantProdId
        secondResultSetMock.getBigDecimal("retailPrice") >> new BigDecimal(1)
        secondResultSetMock.getBigDecimal("unitPrice") >> new BigDecimal(1)

        thirdResultSetMock.next() >>> barcodeNextResult
        thirdResultSetMock.getLong("sku") >> 1010
        thirdResultSetMock.getString("barcode") >> "10101010"

        when: 'The generatePdf action is executed'
        byte[] docReturnedByteArray =
                shelfEdgeLabelHelperService.generatePdf(DateTime.now(DateTimeZone.UTC), labelTemplate, PrintProcess.PRODUCT_LABEL_BATCH, PrintType.PDF, getStoreSetting("Store dummy Name"))

        then: 'Successfully assert pdf action execution'
        assert docReturnedByteArray != null

        where: 'Pass following input parameters'
        productNextResult || variantNextResult || barcodeNextResult   || productId || variantProdId
        [true, false]     || [true, false]     || [true, false]       || 1         || 1
        [true, false]     || [true, false]     || [true, false]       || 1         || 100

    }

    //------------------Calling Generate Pdf For Label Field Type PRODUCT_DESCRIPTION Action -------------//

    def 'Test generate pdf for product description field type'() {

        given:

        shelfEdgeLabelHelperService.springSecurityService = Stub(SpringSecurityService) {
            getPrincipal() >>new HashMap(){{
                put("id", 9);
                put("retailerId", 9);
                put("storeId", 234);
                put("storeNumber", 100)
                put("priceBand", new PriceBand(id: 1))}}
        }

        LabelTemplateFieldType labelTemplateFieldType = LabelTemplateFieldType.PRODUCT_DESCRIPTION
        List<ProductPrice> mockProductPrice = getProductPrice()
        LabelTemplate labelTemplate =  getLabelTemplate(inputRow, inputColumn, maxCharacter, labelTemplateFieldType, "1", 10)
        List<Barcode> mockBarcodeList = getBarcodeList("101010110", false)
        ProductVariant productVariant = getMockProductVariant(isDesRequired, isEmptyDes, shelfEdgeLabelHelperService.springSecurityService, false, false, new BigDecimal(1), false, "1", variantEffectiveDate)
        mockDomain(ProductVariant, [productVariant])

        ProductVariant.metaClass.static.getSessionEffectiveDate = { return DateTime.now() }
        ProductVariant.metaClass.static.getBarcodes = { return mockBarcodeList }
        ProductVariant.metaClass.static.getAllPrices = { return mockProductPrice }

        ProductList mockProductList = getMockProductList(isProductListItemRequired, isEmptyDes, isDesRequired, shelfEdgeLabelHelperService.springSecurityService, false, false, new BigDecimal(1), false, "1", variantEffectiveDate )

        when: 'The generatePdf action is executed'
        byte[] docReturnedByteArray =
                shelfEdgeLabelHelperService.generatePdf(mockProductList, labelTemplate, PrintProcess.PRODUCT_LABEL_BATCH, printType, getStoreSetting("Store dummy Name"))

        then: 'Successfully assert pdf action execution'
        assert docReturnedByteArray != null

        where: 'Pass following input parameters'
        isProductListItemRequired || inputRow || inputColumn || printType                  || maxCharacter || isDesRequired || isEmptyDes || variantEffectiveDate
        true                      || 1        || 1           || PrintType.BLUETOOTH_PRINTER|| 10           || true          || false      || DateTime.now(DateTimeZone.UTC).minusDays(5)
        true                      || 0        || 1           || PrintType.PDF              || 0            || true          || false      || DateTime.now(DateTimeZone.UTC)
        true                      || 1        || 0           || PrintType.PDF              || 0            || true          || false      || DateTime.now(DateTimeZone.UTC)
        true                      || 0        || 0           || PrintType.PDF              || 0            || true          || false      || DateTime.now(DateTimeZone.UTC)
        true                      || 1        || 1           || PrintType.PDF              || 17           || true          || false      || DateTime.now(DateTimeZone.UTC)
        true                      || 1        || 1           || PrintType.PDF              || 0            || false         || true       || DateTime.now(DateTimeZone.UTC)
        true                      || 1        || 1           || PrintType.PDF              || 0            || false         || true       || DateTime.now(DateTimeZone.UTC)
        false                     || 1        || 1           || PrintType.BLUETOOTH_PRINTER|| 10           || true          || false      || DateTime.now(DateTimeZone.UTC)
        false                     || 0        || 0           || PrintType.PDF              || 0            || true          || false      || DateTime.now(DateTimeZone.UTC)
    }

    //------------------Calling Generate Pdf For Label Field Type PRODUCT_PRICE Action -------------//

    def 'Test generate pdf for product price field type'() {

        given:

        shelfEdgeLabelHelperService.springSecurityService = Stub(SpringSecurityService) {
            getPrincipal() >>new HashMap(){{
                put("id", 9);
                put("retailerId", 9);
                put("storeId", 234);
                put("storeNumber", 100)
                put("priceBand", new PriceBand(id: 1))}}
        }

        LabelTemplateFieldType labelTemplateFieldType = LabelTemplateFieldType.PRODUCT_PRICE
        List<ProductPrice> mockProductPrice = getProductPrice()
        LabelTemplate labelTemplate =  getLabelTemplate(1, 1, 1, labelTemplateFieldType, "1", passingWidth)
        List<Barcode> mockBarcodeList = getBarcodeList("101010110", false)
        ProductVariant productVariant = getMockProductVariant(true, false, shelfEdgeLabelHelperService.springSecurityService, isWeighted, isPricePerKg, retailPrice, isZeroPrice, "1", DateTime.now(DateTimeZone.UTC))
        mockDomain(ProductVariant, [productVariant])

        ProductVariant.metaClass.static.getSessionEffectiveDate = { return DateTime.now() }
        ProductVariant.metaClass.static.getBarcodes = { return mockBarcodeList }
        ProductVariant.metaClass.static.getAllPrices = { return mockProductPrice }

        ProductList mockProductList = getMockProductList(true, false, true, shelfEdgeLabelHelperService.springSecurityService, isWeighted, isPricePerKg, retailPrice, isZeroPrice, "1", DateTime.now(DateTimeZone.UTC))

        when: 'The generatePdf action is executed'
        byte[] docReturnedByteArray =
                shelfEdgeLabelHelperService.generatePdf(mockProductList, labelTemplate, PrintProcess.PRODUCT_LABEL_BATCH, PrintType.PDF, getStoreSetting("Store dummy Name"))

        then: 'Successfully assert pdf action execution'
        assert docReturnedByteArray != null

        where: 'Pass following input parameters'
        isWeighted || isPricePerKg || retailPrice           || isZeroPrice || passingWidth
        false      || false        || new BigDecimal(0)     || false       || 2
        true       || false        || new BigDecimal(0)     || true        || 10
        false      || false        || new BigDecimal(-1000) || false       || 10
        false      || false        || new BigDecimal(10000) || false       || 10
        true       || false        || new BigDecimal(10000) || false       || 10
        true       || true         || new BigDecimal(10000) || true        || 10
        true       || true         || new BigDecimal(10000) || false       || 10
        true       || false        || new BigDecimal(10000) || true        || 10
        true       || true         || new BigDecimal(-1000) || false       || 10
        true       || false        || new BigDecimal(-10000)|| false       || 10
        true       || false        || new BigDecimal(-11111)|| false       || 10
        false      || false        || null                  || false       || 10
    }

    //------------------Calling Generate Pdf For Label Field Type BARCODE Action -------------//

    def 'Test generate pdf for barcode field type'() {

        given:

        shelfEdgeLabelHelperService.springSecurityService = Stub(SpringSecurityService) {
            getPrincipal() >>new HashMap(){{
                put("id", 9);
                put("retailerId", 9);
                put("storeId", 234);
                put("storeNumber", 100)
                put("priceBand", new PriceBand(id: 1))}}
        }

        LabelTemplateFieldType labelTemplateFieldType = LabelTemplateFieldType.BARCODE
        List<ProductPrice> mockProductPrice = getProductPrice()
        LabelTemplate labelTemplate =  getLabelTemplate(1, 1, 1, labelTemplateFieldType, "1", 10)
        List<Barcode> mockBarcodeList = inputBarcode
        ProductVariant productVariant = getMockProductVariant(true, false, shelfEdgeLabelHelperService.springSecurityService, false, false, new BigDecimal(1), false, "1", DateTime.now(DateTimeZone.UTC))
        mockDomain(ProductVariant, [productVariant])

        ProductVariant.metaClass.static.getSessionEffectiveDate = { return DateTime.now() }
        ProductVariant.metaClass.static.getBarcodes = { return mockBarcodeList }
        ProductVariant.metaClass.static.getAllPrices = { return mockProductPrice }

        ProductList mockProductList = getMockProductList(true, false, true, shelfEdgeLabelHelperService.springSecurityService, false, false, new BigDecimal(1), false, "1", DateTime.now(DateTimeZone.UTC))

        when: 'The generatePdf action is executed'
        byte[] docReturnedByteArray =
                shelfEdgeLabelHelperService.generatePdf(mockProductList, labelTemplate, PrintProcess.PRODUCT_LABEL_BATCH, PrintType.PDF, getStoreSetting("Store dummy Name"))

        then: 'Successfully assert pdf action execution'
        assert docReturnedByteArray != null

        where: 'Pass following input parameters'
        inputBarcode                           ||_
        getBarcodeList("101010110",false)      ||_
        getBarcodeList("101010110", true)      ||_
        getBarcodeList("", false)              ||_
        getBarcodeList("291010110", false)     ||_
        getBarcodeList("19101011", false)      ||_
        getBarcodeList("1010101101111", false) ||_
        getBarcodeList("101010118989", false)  ||_
        getBarcodeList("10101011898", false)   ||_

    }

    //------------------Calling Generate Pdf For Label Field Type STORE_NAME Action -------------//

    def 'Test generate pdf for store name field type'() {

        given:

        shelfEdgeLabelHelperService.springSecurityService = Stub(SpringSecurityService) {
            getPrincipal() >>new HashMap(){{
                put("id", 9);
                put("retailerId", 9);
                put("storeId", 234);
                put("storeNumber", 100)
                put("priceBand", new PriceBand(id: 1))}}
        }

        LabelTemplateFieldType labelTemplateFieldType = LabelTemplateFieldType.STORE_NAME
        List<ProductPrice> mockProductPrice = getProductPrice()
        LabelTemplate labelTemplate =  getLabelTemplate(1, 1, 1, labelTemplateFieldType, "1", 10)
        List<Barcode> mockBarcodeList = getBarcodeList("101010110", false)
        StoreSettings mockStoreSettings = getStoreSetting(inputStoreName)
        ProductVariant productVariant = getMockProductVariant(true, false, shelfEdgeLabelHelperService.springSecurityService, false, false, new BigDecimal(1), false, "1", DateTime.now(DateTimeZone.UTC))
        mockDomain(ProductVariant, [productVariant])

        ProductVariant.metaClass.static.getSessionEffectiveDate = { return DateTime.now() }
        ProductVariant.metaClass.static.getBarcodes = { return mockBarcodeList }
        ProductVariant.metaClass.static.getAllPrices = { return mockProductPrice }

        ProductList mockProductList = getMockProductList(true, false, true, shelfEdgeLabelHelperService.springSecurityService, false, false, new BigDecimal(1), false, "1", DateTime.now(DateTimeZone.UTC))

        when: 'The generatePdf action is executed'
        byte[] docReturnedByteArray =
                shelfEdgeLabelHelperService.generatePdf(mockProductList, labelTemplate, PrintProcess.PRODUCT_LABEL_BATCH, PrintType.PDF, mockStoreSettings)

        then: 'Successfully assert pdf action execution'
        assert docReturnedByteArray != null

        where: 'Pass following input parameters'
        inputStoreName       ||_
        "Store dummy Name"   ||_
        null                 ||_

    }

    //------------------Calling Generate Pdf For Multiple Label Field Type
    //                  1. EFFECTIVE_DATE
    //                  2. WAS_PRICE -------------//
    //                  3. PROMOTION_END_DATE -------------//
    //                  4. RTC_TITLE -------------//
    //                  5. STRAPLINE -------------//
    //                  6. LINE -------------//
    //                  7. BARKER_TEXT -------------//
    //                  8. BARKER_TITLE -------------//
    //                  9. UNIT_SIZE -------------//

    def 'Test generate pdf for effective date field type'() {

        given:

        shelfEdgeLabelHelperService.springSecurityService = Stub(SpringSecurityService) {
            getPrincipal() >>new HashMap(){{
                put("id", 9);
                put("retailerId", 9);
                put("storeId", 234);
                put("storeNumber", 100)
                put("priceBand", new PriceBand(id: 1))}}
        }

        LabelTemplateFieldType labelTemplateFieldType = labelTemplateType
        List<ProductPrice> mockProductPrice = getProductPrice()
        LabelTemplate labelTemplate =  getLabelTemplate(1, 1, 1, labelTemplateFieldType, defaultValue, 10)
        List<Barcode> mockBarcodeList = getBarcodeList("101010110", false)
        StoreSettings mockStoreSettings = getStoreSetting("Store dummy Name")
        ProductVariant productVariant = getMockProductVariant(true, false, shelfEdgeLabelHelperService.springSecurityService, false, false, new BigDecimal(1), false, unitSize, DateTime.now(DateTimeZone.UTC))
        mockDomain(ProductVariant, [productVariant])

        ProductVariant.metaClass.static.getSessionEffectiveDate = { return DateTime.now() }
        ProductVariant.metaClass.static.getBarcodes = { return mockBarcodeList }
        ProductVariant.metaClass.static.getAllPrices = { return mockProductPrice }

        ProductList mockProductList = getMockProductList(true, false, true, shelfEdgeLabelHelperService.springSecurityService, false, false, new BigDecimal(1), false, unitSize, DateTime.now(DateTimeZone.UTC))

        when: 'The generatePdf action is executed'
        byte[] docReturnedByteArray =
                shelfEdgeLabelHelperService.generatePdf(mockProductList, labelTemplate, PrintProcess.PRODUCT_LABEL_BATCH, PrintType.PDF, mockStoreSettings)

        then: 'Successfully assert pdf action execution'
        assert docReturnedByteArray != null

        where: 'Pass following input parameters'
        labelTemplateType                         || unitSize || defaultValue
        LabelTemplateFieldType.EFFECTIVE_DATE     || "1"      || "1"
        LabelTemplateFieldType.WAS_PRICE          || "1"      || "1"
        LabelTemplateFieldType.PROMOTION_END_DATE || "1"      || "1"
        LabelTemplateFieldType.RTC_TITLE          || "1"      || "1"
        LabelTemplateFieldType.STRAPLINE          || "1"      || "1"
        LabelTemplateFieldType.LINE               || "1"      || "1"
        LabelTemplateFieldType.BARKER_TEXT        || "1"      || "1"
        LabelTemplateFieldType.BARKER_TITLE       || "1"      || "1"
        LabelTemplateFieldType.BARKER_TITLE       || "1"      || null
        LabelTemplateFieldType.UNIT_SIZE          || "1"      || "1"
        LabelTemplateFieldType.UNIT_SIZE          || null     || "1"
        null                                      || "1"      || "1"

    }

    //------------------Calling Load Shelf Edge Labels For Date Action -------------//

    def 'Test the load shelf edge label for date successfully'() {

        given:

        shelfEdgeLabelHelperService.springSecurityService = Stub(SpringSecurityService) {
            getPrincipal() >>new HashMap(){{
                put("retailerId", 9);
                put("storeId", 234);
                put("storeNumber", 100)
            }}
        }

        CallableStatement callableStatementMock =  Mock(CallableStatement)
        ResultSet resultSetMock = Mock(ResultSet)
        mockConnection.prepareCall(_) >> callableStatementMock
        callableStatementMock.executeQuery() >> resultSetMock
        resultSetMock.next() >>> productNextResult
        resultSetMock.getInt("id") >> productId
        resultSetMock.getString("itemCode") >> "1010"
        resultSetMock.getString("description") >> "Dummy description"
        resultSetMock.getString("unitSize") >> "10"
        resultSetMock.getBoolean("weightedItem") >> true
        resultSetMock.getBoolean("pricePerKg") >> true

        ResultSet secondResultSetMock = Mock(ResultSet)
        ResultSet thirdResultSetMock = Mock(ResultSet)

        callableStatementMock.moreResults() >> null
        callableStatementMock.getResultSet() >>> [secondResultSetMock, thirdResultSetMock]

        secondResultSetMock.next() >>> variantNextResult
        secondResultSetMock.getInt("id") >> 1
        secondResultSetMock.getLong("sku") >> 1010
        secondResultSetMock.getInt("productId") >> variantProdId
        secondResultSetMock.getBigDecimal("retailPrice") >> new BigDecimal(1)
        secondResultSetMock.getBigDecimal("unitPrice") >> new BigDecimal(1)

        thirdResultSetMock.next() >>> barcodeNextResult
        thirdResultSetMock.getLong("sku") >> 1010
        thirdResultSetMock.getString("barcode") >> "10101010"

        when: 'The getShelfEdgeLabelsForDate action is executed'
        ArrayList result = shelfEdgeLabelHelperService.getShelfEdgeLabelsForDate(DateTime.now(DateTimeZone.UTC), PrintProcess.PRODUCT_LABEL_BATCH)

        then: 'successfully get snapshot'
        if((productId == variantProdId) && isVariantAvailable){
            assert result.size() == 1
            assert result.get(0).productId == productId
            assert result.get(0).itemCode == "1010"
            assert result.get(0).description == "Dummy description"
            assert result.get(0).unitSize == "10"
            assert result.get(0).weightedItem == true
            assert result.get(0).pricePerKg == true
            assert result.get(0).quantity == 1
            assert result.get(0).printProcess ==  PrintProcess.PRODUCT_LABEL_BATCH
        } else {
            assert result.size() == 0
        }

        where: 'Pass following input parameters'
        productNextResult || variantNextResult || barcodeNextResult   || productId || variantProdId || isVariantAvailable
        [true, false]     || [true, false]     || [true, false]       || 1         || 1             || true
        [true, false]     || [true, false]     || [true, false]       || 100       || 1             || false
        [true, false]     || [false]           || [true, false]       || 100       || 100           || false
        [true, false]     || [true, false]     || [false]             || 100       || 100           || true

    }

    //------------------Calling Updating Product History Print Status Action -------------//

    def 'Test update action for product history print status successfully'() {

        given:

        shelfEdgeLabelHelperService.springSecurityService = Stub(SpringSecurityService) {
            getPrincipal() >>new HashMap(){{
                put("retailerId", 9)
                put("storeId", 234)
                put("storeNumber", 100)
            }}
        }

        CallableStatement callableStatementMock =  Mock(CallableStatement)
        mockConnection.prepareCall(_) >> callableStatementMock
        callableStatementMock.executeUpdate() >> 1

        when: 'The setProductHistoryPrintStatus is executed'
        shelfEdgeLabelHelperService.setProductHistoryPrintStatus(DateTime.now(DateTimeZone.UTC), 1)

        then: 'successfully get snapshot'
        //Assert for calling method successfully without throwing any error
    }



    ProductList getMockProductList(boolean isProductListItemRequired, boolean isEmptyDes, boolean isDescriptionRequired, SpringSecurityService springSecurityService,
                                   boolean isWeighted, boolean isPricePerKg,  BigDecimal retailPrice, boolean isZeroPrice,  String unitSize, DateTime effectiveDate){
        ProductList productList = new ProductList();
        productList.setUserId("1")
        productList.setType(ProductListType.ORDER)
        productList.setStatus(ProductListStatus.COMPLETE)
        productList.setStockAdjustedOnCompletion(true)
        if (isProductListItemRequired){
            productList.setProductListItems(getMockProductListItem(isDescriptionRequired, isEmptyDes, springSecurityService, isWeighted, isPricePerKg, retailPrice, isZeroPrice, unitSize, effectiveDate))
        }
        return productList
    }

    List<ProductListItem> getMockProductListItem(boolean isDescriptionRequired, boolean isEmptyDes, SpringSecurityService springSecurityService, boolean isWeighted,
                                                 boolean isPricePerKg, BigDecimal retailPrice, boolean isZeroPrice, String unitSize, DateTime effectiveDate){
        List<ProductListItem> productListItemList = new ArrayList<>();
        ProductListItem productListItem = new ProductListItem()
        productListItem.setProductVariant(getMockProductVariant(isDescriptionRequired, isEmptyDes, springSecurityService, isWeighted, isPricePerKg, retailPrice, isZeroPrice, unitSize, effectiveDate))
        productListItem.setFillQuantity(10)
        productListItem.setQuantity(10)
        productListItemList.add(productListItem)
        return productListItemList
    }

    ProductVariant getMockProductVariant(boolean isDescriptionRequired, boolean isEmptyDes , SpringSecurityService springSecurityService,
                                         boolean isWeighted, boolean isPricePerKg, BigDecimal retailPrice, boolean isZeroPrice, String unitSize,
                                         DateTime effectiveDate){
        ProductVariant productVariant = new ProductVariant()
        productVariant.setSku(1010)
        productVariant.setSku(1010)
        productVariant.setEffectiveDate(effectiveDate)
        productVariant.setProduct(getMockProduct(isDescriptionRequired, isEmptyDes, isWeighted, isPricePerKg, isZeroPrice, unitSize))
        productVariant.setSpringSecurityService(springSecurityService)
        productVariant.setRetailPrice(retailPrice)
        return productVariant
    }

    Product getMockProduct(boolean isDescriptionRequired, boolean isEmptyDes, boolean isWeighted, boolean isPricePerKg, boolean isZeroPrice, String unitSize){
        Product product = new Product()
        product.setId(1)
        product.setItemCode("1010")
        if(isDescriptionRequired){
            product.setDescription("Dummy description")
        } else if (isEmptyDes){
            product.setDescription("")
        }
        product.setReceiptDescription("Dummy receipt description")
        product.setUnitSize("5")
        product.setVatCode(new VatCode())
        product.setStatus(ProductStatus.ACTIVE)
        product.setCategory(new Category())
        product.setWeightedItem(isWeighted)
        product.setPricePerKg(isPricePerKg)
        product.setZeroPrice(isZeroPrice)
        product.setUnitSize(unitSize)
        return product
    }

    List<ProductPrice> getProductPrice(){

        PriceBand priceBand = new PriceBand(retailerId : 9, description : "Dummy price band")
        priceBand.setId(1)

        List<ProductPrice> productPricesList = new ArrayList<>();
        ProductPrice productPrice = new ProductPrice();
        productPrice.setSku(1010)
        productPrice.setEffectiveDate(DateTime.now(DateTimeZone.UTC))
        productPrice.setPrice(new BigDecimal(1))
        productPrice.setPriceBand(priceBand)
        productPricesList.add(productPrice)
        return productPricesList

    }

    LabelTemplate getLabelTemplate(int inputRow, int inputColumn, int maxCharacter, LabelTemplateFieldType labelTemplateFieldType, String defaultValue, int width){
        LabelTemplate labelTemplate = new LabelTemplate()
        labelTemplate.setId(1)
        labelTemplate.setRetailerId(9)
        labelTemplate.setMarginLeft(new BigDecimal(1))
        labelTemplate.setMarginTop(new BigDecimal(1))
        labelTemplate.setLabelHeight(new BigDecimal(1))
        labelTemplate.setLabelWidth(new BigDecimal(1))
        labelTemplate.setMarginBetweenColumns(new BigDecimal(1))
        labelTemplate.setMarginBetweenRows(new BigDecimal(1))
        labelTemplate.setRows(inputRow)
        labelTemplate.setColumns(inputColumn)
        labelTemplate.setLabelTemplateFields(getLabelTemplateField(maxCharacter, labelTemplateFieldType, defaultValue, width))
        return labelTemplate
    }

    Set<LabelTemplateField> getLabelTemplateField(int maxCharacter, LabelTemplateFieldType labelTemplateFieldType, String defaultValue, int width){
        Set<LabelTemplateField> labelTemplateFieldList = new ArrayList<>();
        LabelTemplateField labelTemplateField = new LabelTemplateField()
        labelTemplateField.setType(labelTemplateFieldType)
        labelTemplateField.setX(1)
        labelTemplateField.setY(1)
        labelTemplateField.setWidth(width)
        labelTemplateField.setHeight(8)
        labelTemplateField.setPreferredTextSize(5)
        labelTemplateField.setPreferredSmallTextSize(5)
        labelTemplateField.setCentrallyAligned(true)
        labelTemplateField.setDefaultValue(defaultValue)
        labelTemplateField.setMaxCharacters(maxCharacter)
        labelTemplateFieldList.add(labelTemplateField)
        return labelTemplateFieldList
    }

    StoreSettings getStoreSetting(String storeName){
        StoreSettings storeSettings = new StoreSettings()
        storeSettings.setPrintReceiptOption(PrintReceiptOption.ALWAYS_PRINT)
        storeSettings.setPriceBand(new PriceBand())
        storeSettings.setRange(new Range())
        storeSettings.setCountIncrement(new BigDecimal(1))
        storeSettings.setSelMarginLeft(new BigDecimal(1))
        storeSettings.setSelMarginTop(new BigDecimal(1))
        storeSettings.setStoreName(storeName)
        return storeSettings
    }

    List<Barcode> getBarcodeList(String barcode,boolean isEmptyBarcodeList){
        List<Barcode> mockBarcodeList = new ArrayList<>();
        if (!isEmptyBarcodeList){
            Barcode mockBarcode = new Barcode(sku : 1010, retailerId : 9, barcode : barcode, recordStatus : 'A')
            mockBarcode.setEffectiveDate(DateTime.now())
            mockBarcodeList.add(mockBarcode)
        }
        return mockBarcodeList
    }


    List<uk.co.wonderlane.wlpos.entities.wlim.ShelfEdgeLabel> getShelfEdgeLabelList(){
        List<uk.co.wonderlane.wlpos.entities.wlim.ShelfEdgeLabel> shelfEdgeLabels = new ArrayList<>()
        uk.co.wonderlane.wlpos.entities.wlim.ShelfEdgeLabel shelfEdgeLabel = new uk.co.wonderlane.wlpos.entities.wlim.ShelfEdgeLabel()
        shelfEdgeLabel.setProductId(1)
        shelfEdgeLabel.setItemCode("1010")
        shelfEdgeLabel.setDescription("Dummy product description")
        shelfEdgeLabel.setUnitSize("!")
        shelfEdgeLabel.setEanCode("1010101010")
        shelfEdgeLabel.setPrice(new BigDecimal(1))
        shelfEdgeLabel.setWasPrice(null)
        shelfEdgeLabel.setUnitPrice(new BigDecimal(1))
        shelfEdgeLabel.setEmbeddedBarcode(false)
        shelfEdgeLabel.setWeightedItem(false)
        shelfEdgeLabel.setPricePerKg(false)
        shelfEdgeLabel.setEffectiveDate(DateTime.now(DateTimeZone.UTC))
        shelfEdgeLabel.setPromotionEndDate(null)
        shelfEdgeLabel.setWasPriceEffectiveDate(null)
        shelfEdgeLabel.setQuantity(10)
        shelfEdgeLabel.setPrintProcess(PrintProcess.PRODUCT_LABEL_BATCH)
        shelfEdgeLabels.add(shelfEdgeLabel)
        return shelfEdgeLabels
    }



}
