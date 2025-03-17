package uk.co.wonderlane.wlpos

import grails.plugin.springsecurity.SpringSecurityService
import grails.testing.gorm.DataTest
import grails.testing.services.ServiceUnitTest
import org.hibernate.Session
import org.hibernate.SessionFactory
import org.hibernate.Transaction
import org.joda.time.DateTime
import spock.lang.Specification
import uk.co.wonderlane.wlpos.dataaccess.DatabaseCredentials
import uk.co.wonderlane.wlpos.enums.PrintReceiptOption
import uk.co.wonderlane.wlpos.enums.ProductStatus
import uk.co.wonderlane.wlpos.helpers.HibernateTestMockCriteria
import uk.co.wonderlane.wlpos.helpers.ProductServiceHelperService
import uk.co.wonderlane.wlpos.reporting.ReportColumns
import uk.co.wonderlane.wlpos.reporting.ReportType
import uk.co.wonderlane.wlpos.supplier.Supplier

import java.sql.CallableStatement
import java.sql.Connection
import java.sql.ResultSet

class ProductServiceSpec extends Specification implements ServiceUnitTest<ProductServiceHelperService>, DataTest {

    Class<?>[] getDomainClassesToMock() {
        [VatCode, Retailer, PriceBand, Range, Supplier, Store,
         RangeProduct, Product, Barcode, ProductVariant, ProductPrice, ReportColumns] as Class<?>[]
    }

    //-------------------------------getProductVariant by id function Unit tests----------------------------//

    void "should return product variant by id"() {
        given:
        service.springSecurityService = getFakeSpringSecurityService()

        Product product = new Product(itemCode: "100", description: "Test", receiptDescription: "Test", retailerId: 9,
                sku: 100, unitSize: "10", vatCode: new VatCode(percentage: BigDecimal.TEN), status: ProductStatus.ACTIVE, category: new Category(),
                restrictions: new Restrictions())
        product.setId(100)
        ProductVariant testProductVariant = new ProductVariant(sku: 110, effectiveDate: DateTime.now(), product: product)
        testProductVariant.setId(100)
        product.variants.add(testProductVariant)
        product.save(flush: true, failOnError: true)
        testProductVariant.save(flush: true, failOnError: true)

        when: 'getProductVariant action is executed'
        def serviceResponse = service.getProductVariant(100)

        then: 'getProductVariant action response is correct'
        serviceResponse != null
        serviceResponse instanceof ProductVariant
    }

    //-------------------------------getProductVariants by sku function Unit tests----------------------------//

    void "should return products variant by sku list"() {
        given:
        service.springSecurityService = getFakeSpringSecurityService()

        Product product = new Product(itemCode: "100", description: "Test", receiptDescription: "Test", retailerId: 9,
                sku: 100, unitSize: "10", vatCode: new VatCode(percentage: BigDecimal.TEN), status: ProductStatus.ACTIVE, category: new Category(),
                restrictions: new Restrictions())
        product.setId(100)
        ProductVariant testProductVariant = new ProductVariant(sku: 110, effectiveDate: DateTime.now(), product: product)
        testProductVariant.setId(100)
        product.variants.add(testProductVariant)
        product.save(flush: true, failOnError: true)
        testProductVariant.save(flush: true, failOnError: true)

        HibernateTestMockCriteria mockCriteria = new HibernateTestMockCriteria()
        mockCriteria.getResponses().add(testProductVariant)
        def defaultCriteria = ProductVariant.createCriteria() // keep the default behavior
        ProductVariant.metaClass.static.createCriteria = { return mockCriteria }

        when: 'getProductVariants action is executed'
        def serviceResponse = service.getProductVariants([100, 200])

        ProductVariant.metaClass.static.createCriteria = { return defaultCriteria } // set the default value back to the class

        then: 'getProductVariants action response is correct'
        serviceResponse != null
        serviceResponse instanceof List
    }

    //-------------------------------getProductVariant by sku function Unit tests----------------------------//

    void "should return product variant by sku"() {
        given:
        service.springSecurityService = getFakeSpringSecurityService()

        Product product = new Product(itemCode: "100", description: "Test", receiptDescription: "Test", retailerId: 9,
                sku: 100, unitSize: "10", vatCode: new VatCode(percentage: BigDecimal.TEN), status: ProductStatus.ACTIVE, category: new Category(),
                restrictions: new Restrictions())
        product.setId(100)
        ProductVariant testProductVariant = new ProductVariant(sku: 110, effectiveDate: DateTime.now(), product: product)
        testProductVariant.setId(100)
        product.variants.add(testProductVariant)
        product.save(flush: true, failOnError: true)
        testProductVariant.save(flush: true, failOnError: true)

        ProductVariant.metaClass.static.withCriteria = { Map sortParams, Closure closure -> return callClosure(closure, testProductVariant) }

        when: 'getProductVariant action is executed'
        def serviceResponse = service.getProductVariant(110L)

        then: 'getProductVariant action response is correct'
        serviceResponse != null
        serviceResponse instanceof ProductVariant
    }

    def callClosure(Closure closure, def returnValue) {
        HibernateTestMockCriteria mockCriteria = new HibernateTestMockCriteria()
        mockCriteria.getResponses().add(returnValue)
        closure.setDelegate(mockCriteria)
        closure.call()
        return mockCriteria.getResponses()

    }

    //-------------------------------getProduct by id function Unit tests----------------------------//

    void "should return product by id"() {
        given:
        service.springSecurityService = getFakeSpringSecurityService()

        Product product = new Product(itemCode: "100", description: "Test", receiptDescription: "Test", retailerId: 9,
                sku: 100, unitSize: "10", vatCode: new VatCode(percentage: BigDecimal.TEN), status: ProductStatus.ACTIVE, category: new Category(),
                restrictions: new Restrictions())
        product.setId(100)
        ProductVariant testProductVariant = new ProductVariant(sku: 110, effectiveDate: DateTime.now(), product: product)
        testProductVariant.setId(100)
        product.variants.add(testProductVariant)
        product.save(flush: true, failOnError: true)

        when: 'getProduct action is executed'
        def serviceResponse = service.getProduct(100)

        then: 'getProduct action response is correct'
        serviceResponse != null
        serviceResponse instanceof Product
    }

    //-------------------------------saveProduct function Unit tests----------------------------//

    void "should save product"() {
        given:
        service.springSecurityService = getFakeSpringSecurityService()

        Product product = new Product(itemCode: "100", description: "Test", receiptDescription: "Test", retailerId: 9,
                sku: 100, unitSize: "10", vatCode: new VatCode(percentage: BigDecimal.TEN), status: ProductStatus.ACTIVE, category: new Category(),
                restrictions: new Restrictions())
        product.setId(100)
        ProductVariant testProductVariant = new ProductVariant(sku: 110, effectiveDate: DateTime.now(), product: product)
        testProductVariant.setId(100)
        product.variants.add(testProductVariant)

        when: 'saveProduct action is executed'
        def serviceResponse = service.saveProduct(product)

        then: 'saveProduct action response is correct'
        serviceResponse != null
        serviceResponse instanceof Product
        Product.findById(100) != null
    }

    //-------------------------------saveProduct with variants list function Unit tests----------------------------//

    void "should save product with variants list"() {
        given:
        service.springSecurityService = getFakeSpringSecurityService()

        Product product = new Product(itemCode: "100", description: "Test", receiptDescription: "Test", retailerId: 9,
                sku: 100, unitSize: "10", vatCode: new VatCode(percentage: BigDecimal.TEN), status: ProductStatus.ACTIVE, category: new Category(),
                restrictions: new Restrictions())
        product.setId(100)
        ProductVariant testProductVariant = new ProductVariant(sku: 110, effectiveDate: DateTime.now(), product: product)
        testProductVariant.setId(100)

        ProductAttributeValues productAttributeValues = new ProductAttributeValues(retailerId: 9, productId: 100, productAttributeId: 1, value: 10, product: product)

        when: 'saveProduct action is executed'
        def serviceResponse = service.saveProduct(product, [testProductVariant], [productAttributeValues])

        then: 'saveProduct action response is correct'
        serviceResponse != null
        serviceResponse instanceof Product
        Product.findById(100) != null
    }

    //-------------------------------saveBarcodes function Unit tests----------------------------//

    void "should save barcodes"() {
        given:
        service.springSecurityService = getFakeSpringSecurityService()

        Product product = new Product(itemCode: "100", description: "Test", receiptDescription: "Test", retailerId: 9,
                sku: 100, unitSize: "10", vatCode: new VatCode(percentage: BigDecimal.TEN), status: ProductStatus.ACTIVE, category: new Category(),
                restrictions: new Restrictions())
        product.setId(100)
        ProductVariant testProductVariant = new ProductVariant(sku: 110, effectiveDate: DateTime.now(), product: product)
        testProductVariant.setId(100)

        product.getVariants().add(testProductVariant)

        Barcode.metaClass.static.findAllByRetailerIdAndBarcodeAndSkuNotEqualAndEffectiveDateLessThanEquals = { int retailerId, String barcode, long sku, DateTime dateTime, Map sortParams -> return [] }

        def barcode1 = new Barcode(barcode: "BC1", sku: 110, delete: false,
                effectiveDate: DateTime.now(), recordStatus: 'A' as char)
        barcode1.setId(110)

        def barcode2 = new Barcode(barcode: "BC2", sku: 120, delete: true,
                effectiveDate: DateTime.now(), recordStatus: 'A' as char)
        barcode2.setId(120)
        barcode2.setDelete(true)
        barcode2.setEffectiveDeleteDate(DateTime.now())

        testProductVariant.barcodez.add(barcode1)
        testProductVariant.barcodez.add(barcode2)

        when: 'saveBarcodes action is executed'
        service.saveBarcodes(product)

        then: 'saveProduct action response is correct'
        Barcode.findBySku(110) != null
        Barcode.findBySku(120) != null
        Barcode.findBySku(120).recordStatus == 'D' as char
    }

    //-------------------------------saveProductVariant function Unit tests----------------------------//

    void "should save product variant"() {
        given:
        service.springSecurityService = getFakeSpringSecurityService()

        Product product = new Product(itemCode: "100", description: "Test", receiptDescription: "Test", retailerId: 9,
                sku: 100, unitSize: "10", vatCode: new VatCode(percentage: BigDecimal.TEN), status: ProductStatus.ACTIVE, category: new Category(),
                restrictions: new Restrictions())
        product.setId(100)
        ProductVariant testProductVariant = new ProductVariant(sku: 110, effectiveDate: DateTime.now(), product: product)
        testProductVariant.setId(100)
        product.variants.add(testProductVariant)
        product.save(flush: true, failOnError: true)

        when: 'saveProductVariant action is executed'
        def serviceResponse = service.saveProductVariant(testProductVariant)

        then: 'saveProductVariant action response is correct'
        serviceResponse != null
        ProductVariant.findById(100) != null
    }

    //-------------------------------saveProductPrices function Unit tests----------------------------//

    void "should save product prices"() {
        given:
        service.sessionFactory = Stub(SessionFactory) {
            openSession() >> Stub(Session) {
                beginTransaction() >> Mock(Transaction)
            }
        }

        def productPrices = []
        def productHistory = []
        productPrices.add(new ProductPrice(price: 100))
        for (int i = 0; i < 150; i++) {
            productPrices.add(new ProductPrice())
            productHistory.add(new ProductHistory())
        }
        when: 'saveProductPrices action is executed'
        service.saveProductPrices(productPrices, productHistory)

        then: 'saveProductPrices action response is correct'
        // method not throwing an exception is considered as pass criteria
    }

    //-------------------------------saveRangeProducts function Unit tests----------------------------//

    void "should save range products"() {
        given:
        service.sessionFactory = Stub(SessionFactory) {
            openSession() >> Stub(Session) {
                beginTransaction() >> Mock(Transaction)
            }
        }

        def rangeProducts = []
        for (int i = 0; i < 150; i++) {
            rangeProducts.add(new RangeProduct())
        }

        when: 'saveRangeProducts action is executed'
        service.saveRangeProducts(rangeProducts)

        then: 'saveRangeProducts action response is correct'
        // method not throwing an exception is considered as pass criteria
    }

    //-------------------------------deleteRangeProducts function Unit tests----------------------------//

    void "should delete range products"() {
        given:
        service.sessionFactory = Stub(SessionFactory) {
            openSession() >> Stub(Session) {
                beginTransaction() >> Mock(Transaction)
            }
        }

        def rangeProducts = []
        for (int i = 0; i < 150; i++) {
            rangeProducts.add(new RangeProduct())
        }

        when: 'deleteRangeProducts action is executed'
        service.deleteRangeProducts(rangeProducts)

        then: 'deleteRangeProducts action response is correct'
        // method not throwing an exception is considered as pass criteria
    }

    //-------------------------------saveProductHistories function Unit tests----------------------------//

    void "should save product histories"() {
        given:
        service.sessionFactory = Stub(SessionFactory) {
            openSession() >> Stub(Session) {
                beginTransaction() >> Mock(Transaction)
            }
        }

        def productHistory = []
        for (int i = 0; i < 150; i++) {
            productHistory.add(new ProductHistory())
        }

        when: 'saveProductHistories action is executed'
        service.saveProductHistories(productHistory)

        then: 'saveProductHistories action response is correct'
        // method not throwing an exception is considered as pass criteria
    }

    //-------------------------------populateCurrentProductVariant function Unit tests----------------------------//

    void "should populate current product variant"() {
        given:
        service.springSecurityService = getFakeSpringSecurityService()

        ProductVariant testProductVariant = new ProductVariant(sku: 110, effectiveDate: DateTime.now(), product: new Product())
        testProductVariant.setId(100)
        def product = [currentProductVariant: testProductVariant, variants: [testProductVariant]]

        when: 'populateCurrentProductVariant action is executed'
        service.populateCurrentProductVariant(product)

        then: 'populateCurrentProductVariant action response is correct'
        // method not throwing an exception is considered as pass criteria
    }

    //-------------------------------searchProducts function Unit tests----------------------------//

    void "should search products according to given parameters"() {
        given:
        service.springSecurityService = getFakeSpringSecurityService()

        Product product = new Product(itemCode: "100", description: "Test", receiptDescription: "Test", retailerId: 9,
                sku: 100, unitSize: "10", vatCode: new VatCode(percentage: BigDecimal.TEN), status: ProductStatus.ACTIVE, category: new Category(),
                restrictions: new Restrictions())
        product.setId(100)

        HibernateTestMockCriteria mockCriteria = new HibernateTestMockCriteria()
        mockCriteria.getResponses().add(product)
        def defaultCriteria = Product.createCriteria() // keep the default behavior
        Product.metaClass.static.createCriteria = { return mockCriteria }

        Barcode.metaClass.static.findAllByBarcodeLikeAndRetailerIdAndEffectiveDateLessThanEquals = { String barcode, int retailerId, DateTime date -> return [[sku: 100], [sku: 200]] }

        when: 'searchProducts action is executed'
        def serviceResponse = service.searchProducts(searchTerm, searchBy, 100, 0, sortColumn, sortOrder)

        Product.metaClass.static.createCriteria = { return defaultCriteria } // set the default value back to the class

        then: 'searchProducts action response is correct'
        serviceResponse != null
        serviceResponse instanceof List

        where:
        ID | searchTerm | searchBy      | sortColumn    | sortOrder
        1  | "Test"     | "everything"  | "id"          | "asc"
        2  | "Te"       | "everything"  | "id"          | "asc"
        3  | "1"        | "everything"  | "description" | "asc"
        4  | "Te"       | "itemCode"    | "price"       | "asc"
        5  | "1"        | "itemCode"    | "price"       | "asc"
        6  | "Test"     | "description" | "description" | "desc"
        7  | "12"       | "barcode"     | "price"       | "desc"
        8  | "1234"     | "barcode"     | "price"       | "desc"
        9  | "other"    | "other"       | "price"       | "desc"
        10 | null       | "other"       | "price"       | "desc"
    }

    //-------------------------------searchProductsHql by sku function Unit tests----------------------------//

    void "should search products by hql according to given parameters"() {
        given:
        service.springSecurityService = getFakeSpringSecurityService()

        Product product = new Product(itemCode: "100", description: "Test", receiptDescription: "Test", retailerId: 9,
                sku: 100, unitSize: "10", vatCode: new VatCode(percentage: BigDecimal.TEN), status: ProductStatus.ACTIVE, category: new Category(),
                restrictions: new Restrictions())
        product.setId(100)

        HibernateTestMockCriteria mockCriteria = new HibernateTestMockCriteria()
        mockCriteria.getResponses().add(product)
        def defaultCriteria = Product.createCriteria() // keep the default behavior
        Product.metaClass.static.createCriteria = { return mockCriteria }
        Product.metaClass.static.executeQuery = { CharSequence query, Map params -> return ["SUCCESS"] }
        Barcode.metaClass.static.findAllByBarcodeLikeAndRetailerIdAndEffectiveDateLessThanEquals = { String barcode, int retailerId, DateTime date -> return [new Barcode(sku: 110), new Barcode(sku: 120), new Barcode(sku: 200, recordStatus: 'D' as char)] }

        when: 'searchProductsHql action is executed'
        def serviceResponse = service.searchProductsHql(searchTerm, searchBy, 100, 0, sortColumn, sortOrder)

        Product.metaClass.static.createCriteria = { return defaultCriteria } // set the default value back to the class

        then: 'searchProductsHql action response is correct'
        serviceResponse != null

        where:
        ID | searchTerm | searchBy      | sortColumn    | sortOrder
        1  | "Test"     | "everything"  | "id"          | "asc"
        2  | "1"        | "everything"  | "description" | "asc"
        3  | "Te"       | "itemCode"    | "price"       | "asc"
        4  | "1"        | "itemCode"    | "price"       | "asc"
        5  | "Test"     | "description" | "description" | "desc"
        6  | "1234"     | "barcode"     | "price"       | "desc"
        7  | "other"    | "other"       | "price"       | "desc"
        8  | null       | "other"       | "price"       | "desc"
        9  | "Te"       | "everything"  | "id"          | "asc"
        10 | "12"       | "barcode"     | "price"       | "desc"
    }

    //-------------------------------searchProductPrices function Unit tests----------------------------//

    void "should return product prices according to parameters"() {
        given:
        def mockConnection = Mock(Connection)
        def productServiceHelper = new ProductServiceHelperService(Mock(DatabaseCredentials), mockConnection)
        def springSecService = Stub(SpringSecurityService) {
            HashMap map = new HashMap()
            map.put("retailerId", 9)
            map.put("storeId", storeId)

            getPrincipal() >> map
        }

        productServiceHelper.springSecurityService = springSecService

        CallableStatement callableStatementMock = Mock(CallableStatement)
        ResultSet resultSetMock = Mock(ResultSet)

        mockConnection.prepareCall(_) >> callableStatementMock
        callableStatementMock.executeQuery() >> resultSetMock
        resultSetMock.next() >>> resultSetNextResult
        resultSetMock.getInt("id") >> 100
        resultSetMock.getLong("sku") >> 100L
        resultSetMock.getString("productDescription") >> "Test Description"
        resultSetMock.getString("priceBandDescription") >> "Test Description"
        resultSetMock.getBigDecimal("price") >> BigDecimal.ZERO
        resultSetMock.getBigDecimal("costPrice") >> BigDecimal.ZERO

        when: 'searchProductPrices action is executed'
        def serviceResponse = productServiceHelper.searchProductPrices(searchTerm, categoryId, tagId)

        then: 'searchProductPrices action response is correct'
        serviceResponse != null

        where:
        ID | storeId | searchTerm | categoryId | tagId | resultSetNextResult
        1  | 100     | "Test"     | 100        | 100   | [true, false]
        2  | null    | ""         | null       | null  | [true, false]
        3  | null    | null       | null       | null  | [true, false]
        4  | null    | null       | null       | null  | [false]
    }

    //-------------------------------searchRangeProducts function Unit tests----------------------------//

    void "should return range products according to parameters"() {
        given:
        def mockConnection = Mock(Connection)
        def productServiceHelper = new ProductServiceHelperService(Mock(DatabaseCredentials), mockConnection)
        def springSecService = Stub(SpringSecurityService) {
            HashMap map = new HashMap()
            map.put("retailerId", 9)
            map.put("storeId", storeId)

            getPrincipal() >> map
        }

        productServiceHelper.springSecurityService = springSecService

        CallableStatement callableStatementMock = Mock(CallableStatement)
        ResultSet resultSetMock = Mock(ResultSet)

        mockConnection.prepareCall(_) >> callableStatementMock
        callableStatementMock.executeQuery() >> resultSetMock
        resultSetMock.next() >>> resultSetNextResult
        resultSetMock.getInt("id") >> 100
        resultSetMock.getInt("rangeId") >> 100
        resultSetMock.getString("productDescription") >> "Test Description"
        resultSetMock.getString("productItemCode") >> "Test Code"

        when: 'searchRangeProducts action is executed'
        def serviceResponse = productServiceHelper.searchRangeProducts(searchTerm, categoryId, tagId)

        then: 'searchRangeProducts action response is correct'
        serviceResponse != null

        where:
        ID | storeId | searchTerm | categoryId | tagId | resultSetNextResult
        1  | 100     | "Test"     | 100        | 100   | [true, false]
        2  | null    | ""         | null       | null  | [true, false]
        3  | null    | null       | null       | null  | [true, false]
        4  | null    | null       | null       | null  | [false]
    }

    //-------------------------------deleteRangeProduct function Unit tests----------------------------//

    void "should delete range product"() {
        given:
        Range testRange = new Range(retailerId: 9, description: "Test")
        testRange.setId(120)
        testRange.save(flush: true, failOnError: true)

        RangeProduct testRangeProduct = new RangeProduct(productId: 100, range: testRange)
        testRangeProduct.setId(120)
        testRangeProduct.save(flush: true, failOnError: true)

        when: 'deleteRangeProduct action is executed'
        def serviceResponse = service.deleteRangeProduct(testRangeProduct)

        then: 'deleteRangeProduct action response is correct'
        serviceResponse == null
        RangeProduct.findById(120) == null
    }

    void "should do nothing if range product is null when deleting"() {
        given:

        when: 'deleteRangeProduct action is executed'
        def serviceResponse = service.deleteRangeProduct(null)

        then: 'deleteRangeProduct action response is correct'
        serviceResponse == null
    }

    //-------------------------------saveRangeProduct function Unit tests----------------------------//

    void "should save range product"() {
        given:
        Range testRange = new Range(retailerId: 9, description: "Test")
        testRange.setId(120)
        testRange.save(flush: true, failOnError: true)

        RangeProduct testRangeProduct = new RangeProduct(productId: 100, range: testRange)
        testRangeProduct.setId(120)

        when: 'saveRangeProduct action is executed'
        def serviceResponse = service.saveRangeProduct(testRangeProduct)

        then: 'saveRangeProduct action response is correct'
        serviceResponse != null
        RangeProduct.findById(120) != null
    }

    void "should do nothing if range product is null when saving"() {
        given:

        when: 'saveRangeProduct action is executed'
        def serviceResponse = service.saveRangeProduct(null)

        then: 'saveRangeProduct action response is correct'
        serviceResponse == null
    }

    //-------------------------------getColumns function Unit tests----------------------------//

    void "should retrieve report columns"() {
        given:
        service.springSecurityService = getFakeSpringSecurityService()

        ReportColumns reportColumns = new ReportColumns(userId: 1, reportType: ReportType.PRODUCT_SEARCH)
        reportColumns.setId(100)
        reportColumns.save(flush: true, failOnError: true)

        when: 'getColumns action is executed'
        def serviceResponse = service.getColumns()

        then: 'getColumns action response is correct'
        serviceResponse != null
        serviceResponse instanceof ReportColumns
    }

    //-------------------------------sendProductPriceUpdate function Unit tests----------------------------//

    void "should send product price update"() {
        given:
        service.springSecurityService = Stub(SpringSecurityService) {
            getPrincipal() >> [storeId: 100, retailerId: 9, retailer: [twoStageSel: false]]
        }

        service.rabbitService = Stub(BackOfficeRabbitService) {
            isOpen() >> true
        }

        service.gsonProvider = new GsonProvider()

        Store testStoreSettings = new Store(retailerId: 9,
                storeId: 100,
                printReceiptOption: PrintReceiptOption.NO_PRINT,
                type: "test",
                priceBand: new PriceBand(retailerId: 9, description: "Test"),
                range: new Range(retailerId: 9, description: "Test"),
                countIncrement: BigDecimal.ONE)

        testStoreSettings.springSecurityService = service.springSecurityService


        when: 'sendProductPriceUpdate action is executed'
        service.sendProductPriceUpdate([], [testStoreSettings])

        then: 'sendProductPriceUpdate action response is correct'
        // method run without exceptions is considered as test pass criteria
    }

    void "should not send product price update if twoStageSel"() {
        given:
        service.springSecurityService = Stub(SpringSecurityService) {
            getPrincipal() >> [storeId: 100, retailerId: 9, retailer: [twoStageSel: true]]
        }

        when: 'sendProductPriceUpdate action is executed'
        service.sendProductPriceUpdate([], [])

        then: 'sendProductPriceUpdate action response is correct'
        // method run without exceptions is considered as test pass criteria
    }

    void "should not send product price update if no store settings in parameters"() {
        given:
        service.springSecurityService = Stub(SpringSecurityService) {
            getPrincipal() >> [storeId: 100, retailerId: 9, retailer: [twoStageSel: false]]
        }

        service.rabbitService = Stub(BackOfficeRabbitService) {
            isOpen() >> true
        }

        when: 'sendProductPriceUpdate action is executed'
        service.sendProductPriceUpdate([], [])

        then: 'sendProductPriceUpdate action response is correct'
        // method run without exceptions is considered as test pass criteria
    }

    void "should not send product price update and throw exception if rabbit service is closed"() {
        given:
        service.springSecurityService = Stub(SpringSecurityService) {
            getPrincipal() >> [storeId: 100, retailerId: 9, retailer: [twoStageSel: false]]
        }

        service.rabbitService = Stub(BackOfficeRabbitService) {
            isOpen() >> false
        }

        when: 'sendProductPriceUpdate action is executed'
        service.sendProductPriceUpdate([], [])

        then: 'sendProductPriceUpdate action response is correct'
        Exception e = thrown()
        e.message == "Rabbit MQ not available"
    }

    //-------------------------------sendProductUpdate function Unit tests----------------------------//

    void "should send product update"() {
        given:
        service.springSecurityService = Stub(SpringSecurityService) {
            getPrincipal() >> [storeId: 100, retailerId: 9, retailer: [twoStageSel: false], priceBand: new PriceBand()]
        }

        service.rabbitService = Stub(BackOfficeRabbitService) {
            isOpen() >> true
        }

        service.gsonProvider = new GsonProvider()

        Store testStoreSettings = new Store(retailerId: 9,
                storeId: 150,
                printReceiptOption: PrintReceiptOption.NO_PRINT,
                type: "test",
                priceBand: new PriceBand(retailerId: 9, description: "Test"),
                range: new Range(retailerId: 9, description: "Test"),
                countIncrement: BigDecimal.ONE)

        testStoreSettings.springSecurityService = service.springSecurityService

        Product product = new Product(itemCode: "100", description: "Test", receiptDescription: "Test", retailerId: 9,
                sku: 100, unitSize: "10",
                vatCode: new VatCode(percentage: BigDecimal.TEN), status: ProductStatus.ACTIVE)
        product.setRestrictions(new Restrictions())
        product.setCategory(new Category(restrictions: new Restrictions()))
        product.setId(100)
        ProductVariant testProductVariant = new ProductVariant(sku: 110, effectiveDate: DateTime.now(), product: product)
        testProductVariant.springSecurityService = service.springSecurityService
        testProductVariant.setId(100)
        testProductVariant.setStoreId(100)
        product.variants.add(testProductVariant)

        when: 'sendProductUpdate action is executed'
        service.sendProductUpdate([product], [testStoreSettings])

        then: 'sendProductUpdate action response is correct'
        // method run without exceptions is considered as test pass criteria
    }

    void "should not send product update if no store settings in parameters"() {
        given:
        service.springSecurityService = Stub(SpringSecurityService) {
            getPrincipal() >> [storeId: 100, retailerId: 9, retailer: [twoStageSel: false]]
        }

        service.rabbitService = Stub(BackOfficeRabbitService) {
            isOpen() >> true
        }

        when: 'sendProductUpdate action is executed'
        service.sendProductUpdate([], [])

        then: 'sendProductUpdate action response is correct'
        // method run without exceptions is considered as test pass criteria
    }

    void "should not send product update and throw exception if rabbit service is closed"() {
        given:
        service.springSecurityService = Stub(SpringSecurityService) {
            getPrincipal() >> [storeId: 100, retailerId: 9, retailer: [twoStageSel: false]]
        }

        service.rabbitService = Stub(BackOfficeRabbitService) {
            isOpen() >> false
        }

        when: 'sendProductUpdate action is executed'
        service.sendProductUpdate([], [])

        then: 'sendProductUpdate action response is correct'
        Exception e = thrown()
        e.message == "Rabbit MQ not available"
    }

    //-------------------------------syncProductUpdatesToAllStoresForRetailer function Unit tests----------------------------//

    void "should sync product updates to all stores for retailer"() {
        given:
        service.springSecurityService = Stub(SpringSecurityService) {
            getPrincipal() >> [storeId: 100, retailerId: 9, retailer: [twoStageSel: false], priceBand: new PriceBand()]
        }

        service.rabbitService = Stub(BackOfficeRabbitService) {
            isOpen() >> true
        }

        service.gsonProvider = new GsonProvider()

        Store testStoreSettings = new Store(retailerId: 9,
                storeId: 150,
                printReceiptOption: PrintReceiptOption.NO_PRINT,
                type: "test",
                priceBand: new PriceBand(retailerId: 9, description: "Test"),
                range: new Range(retailerId: 9, description: "Test"),
                countIncrement: BigDecimal.ONE)

        testStoreSettings.springSecurityService = service.springSecurityService

        Product product = new Product(itemCode: "100", description: "Test", receiptDescription: "Test", retailerId: 9,
                sku: 100, unitSize: "10",
                vatCode: new VatCode(percentage: BigDecimal.TEN), status: ProductStatus.ACTIVE)
        product.setRestrictions(new Restrictions())
        product.setCategory(new Category(restrictions: new Restrictions()))
        product.setId(100)
        ProductVariant testProductVariant = new ProductVariant(sku: 110, effectiveDate: DateTime.now(), product: product)
        testProductVariant.springSecurityService = service.springSecurityService
        testProductVariant.setId(100)
        testProductVariant.setStoreId(100)
        product.variants.add(testProductVariant)

        product.save(flush: true, failOnError: true)
        testStoreSettings.save(flush: true, failOnError: true)

        when: 'syncProductUpdatesToAllStoresForRetailer action is executed'
        service.syncProductUpdatesToAllStoresForRetailer([100])

        then: 'syncProductUpdatesToAllStoresForRetailer action response is correct'
        // method run without exceptions is considered as test pass criteria
    }

    void "should not sync product updates to all stores for retailer if twoStageSel enabled for user"() {
        given:
        service.springSecurityService = Stub(SpringSecurityService) {
            getPrincipal() >> [storeId: 100, retailerId: 9, retailer: [twoStageSel: true], priceBand: new PriceBand()]
        }

        when: 'syncProductUpdatesToAllStoresForRetailer action is executed'
        service.syncProductUpdatesToAllStoresForRetailer([100])

        then: 'syncProductUpdatesToAllStoresForRetailer action response is correct'
        // method run without exceptions is considered as test pass criteria
    }

    void "should not sync product updates to all stores for retailer if product id list is empty"() {
        given:
        service.springSecurityService = Stub(SpringSecurityService) {
            getPrincipal() >> [storeId: 100, retailerId: 9, retailer: [twoStageSel: false], priceBand: new PriceBand()]
        }

        service.rabbitService = Stub(BackOfficeRabbitService) {
            isOpen() >> true
        }

        service.gsonProvider = new GsonProvider()

        Store testStoreSettings = new Store(retailerId: 9,
                storeId: 150,
                printReceiptOption: PrintReceiptOption.NO_PRINT,
                type: "test",
                priceBand: new PriceBand(retailerId: 9, description: "Test"),
                range: new Range(retailerId: 9, description: "Test"),
                countIncrement: BigDecimal.ONE)

        testStoreSettings.springSecurityService = service.springSecurityService
        testStoreSettings.save(flush: true, failOnError: true)

        when: 'syncProductUpdatesToAllStoresForRetailer action is executed'
        service.syncProductUpdatesToAllStoresForRetailer([])

        then: 'syncProductUpdatesToAllStoresForRetailer action response is correct'
        // method run without exceptions is considered as test pass criteria
    }

    //-------------------------------syncProductUpdatesToSingleStore function Unit tests----------------------------//

    void "should sync product updates to a given store"() {
        given:
        service.springSecurityService = Stub(SpringSecurityService) {
            getPrincipal() >> [storeId: 100, retailerId: 9, retailer: [twoStageSel: false], priceBand: new PriceBand()]
        }

        service.rabbitService = Stub(BackOfficeRabbitService) {
            isOpen() >> true
        }

        service.gsonProvider = new GsonProvider()

        Store testStoreSettings = new Store(retailerId: 9,
                storeId: 150,
                printReceiptOption: PrintReceiptOption.NO_PRINT,
                type: "test",
                priceBand: new PriceBand(retailerId: 9, description: "Test"),
                range: new Range(retailerId: 9, description: "Test"),
                countIncrement: BigDecimal.ONE)
        testStoreSettings.setId(150)

        testStoreSettings.springSecurityService = service.springSecurityService

        Product product = new Product(itemCode: "100", description: "Test", receiptDescription: "Test", retailerId: 9,
                sku: 100, unitSize: "10",
                vatCode: new VatCode(percentage: BigDecimal.TEN), status: ProductStatus.ACTIVE)
        product.setRestrictions(new Restrictions())
        product.setCategory(new Category(restrictions: new Restrictions()))
        product.setId(100)
        ProductVariant testProductVariant = new ProductVariant(sku: 110, effectiveDate: DateTime.now(), product: product)
        testProductVariant.springSecurityService = service.springSecurityService
        testProductVariant.setId(100)
        testProductVariant.setStoreId(100)
        product.variants.add(testProductVariant)

        product.save(flush: true, failOnError: true)
        testStoreSettings.save(flush: true, failOnError: true)

        when: 'syncProductUpdatesToSingleStore action is executed'
        service.syncProductUpdatesToSingleStore([100], 150)

        then: 'syncProductUpdatesToSingleStore action response is correct'
        // method run without exceptions is considered as test pass criteria
    }

    //-------------------------------searchProductsNew function Unit tests----------------------------//

//    void "should return product search - new results according to parameters"() {
//        given:
//        def mockConnection = Mock(Connection)
//        def productServiceHelper = new ProductServiceHelperService(Mock(DatabaseCredentials), mockConnection)
//        def springSecService = Stub(SpringSecurityService) {
//            HashMap map = new HashMap()
//            map.put("retailerId", 9)
//            map.put("storeId", storeId)
//
//            getPrincipal() >> map
//        }
//
//        productServiceHelper.springSecurityService = springSecService
//
//        CallableStatement callableStatementMock = Mock(CallableStatement)
//        ResultSet resultSetMock = Mock(ResultSet)
//
//        mockConnection.prepareCall(_) >> callableStatementMock
//        callableStatementMock.executeQuery() >> resultSetMock
//        callableStatementMock.getResultSet() >> resultSetMock
//        resultSetMock.next() >>> resultSetNextResult
//        resultSetMock.getInt("id") >> 100
//        resultSetMock.getString("code") >> "A"
//        resultSetMock.getString("description") >> "Test Description"
//        resultSetMock.getString("retailerVatCode") >> "1_5_3"
//        resultSetMock.getBigDecimal("percentage") >> BigDecimal.TEN
//
//        resultSetMock.getInt("retailerId") >> 9
//        resultSetMock.getInt("storeId") >> 100
//        resultSetMock.getInt("parentId") >> 200
//        resultSetMock.getString("shortDescription") >> "Test Short Description"
//        resultSetMock.getString("retailerCategoryCode") >> "5"
//
//        resultSetMock.getInt("restrictionsId") >> 100
//        resultSetMock.getInt("buyerAgeRestriction") >> 18
//        resultSetMock.getInt("buyerChallengeAge") >> 21
//        resultSetMock.getInt("sellerAgeRestriction") >> 22
//        resultSetMock.getBigDecimal("minOpenPrice") >> BigDecimal.TEN
//        resultSetMock.getBoolean("buyerIdRequired") >> true
//        resultSetMock.getBoolean("buyerIdForced") >> true
//        resultSetMock.getBoolean("refundAllowed") >> true
//        resultSetMock.getBoolean("markdownAllowed") >> true
//        resultSetMock.getBoolean("discountAllowed") >> true
//        resultSetMock.getBoolean("creditPaymentAllowed") >> true
//        resultSetMock.getBoolean("quantityChangeAllowed") >> true
//        resultSetMock.getBoolean("quantityChangeForced") >> true
//        resultSetMock.getBoolean("receiptPrintForced") >> true
//
//        resultSetMock.getString("itemCode") >> "100"
//        resultSetMock.getString("unitSize") >> "EACH"
//        resultSetMock.getString("receiptDescription") >> "Test Description"
//        resultSetMock.getString("discreetMessage") >> "Test Msg"
//        resultSetMock.getString("status") >> "ACTIVE"
//        resultSetMock.getInt("categoryId") >> 100
//        resultSetMock.getInt("vatCodeId") >> 100
//        resultSetMock.getInt("totalProducts") >> 1
//        resultSetMock.getBoolean("openPrice") >> true
//        resultSetMock.getBoolean("zeroPrice") >> true
//        resultSetMock.getBoolean("weightedItem") >> true
//        resultSetMock.getBoolean("displayOncePerTransaction") >> true
//        resultSetMock.getBoolean("displayOncePerItem") >> true
//        resultSetMock.getBigDecimal("vatPercentageOverride") >> BigDecimal.TEN
//
//        resultSetMock.getLong("sku") >> 100L
//        resultSetMock.getString("retailerMessageCode") >> "100"
//        resultSetMock.getString("size") >> "1"
//        resultSetMock.getString("colour") >> "#00000"
//        resultSetMock.getString("text") >> "Test"
//        resultSetMock.getInt("productId") >> 120
//        resultSetMock.getInt("cardTypeId") >> 100
//        resultSetMock.getInt("rate") >> 5
//        resultSetMock.getInt("productVariantId") >> 100
//        resultSetMock.getString("barcode") >> "10000000"
//        resultSetMock.getInt("minimumStockLevel") >> 100
//        resultSetMock.getTimestamp("startDate") >> Timestamp.from(Instant.now())
//        resultSetMock.getTimestamp("endDate") >> Timestamp.from(Instant.now())
//        resultSetMock.wasNull() >> includeNull
//
//
//        when: 'searchRangeProducts action is executed'
//        def serviceResponse = productServiceHelper.searchProductsNew(searchTerm, searchBy, 100, 0, sortColumn, sortOrder)
//
//        then: 'searchRangeProducts action response is correct'
//        serviceResponse != null
//
//        where:
//        ID | storeId | searchTerm | searchBy | sortColumn | sortOrder | resultSetNextResult                                                                                                                | includeNull
//        1  | 100     | "Test"     | "test"   | "test"     | "asc"     | [true, false, true, false, true, false, true, false, true, false, true, false, true, false, true, false, true, false, true, false] | true
//        2  | null    | ""         | ""       | "id"       | "desc"    | [true, false, true, false, true, false, true, false, true, false, true, false, true, false, true, false, true, false, true, false] | false
//        3  | null    | null       | null     | null       | null      | [true, false, true, false, true, false, true, false, true, false, true, false, true, false, true, false, true, false, true, false] | true
//        4  | null    | null       | null     | null       | null      | [false]                                                                                                                            | false
//    }


    def getFakeSpringSecurityService() {
        return Stub(SpringSecurityService) {
            getPrincipal() >> new HashMap() {
                {
                    put("storeId", 100)
                    put("storeNumber", 100)
                    put("retailerId", 9)
                    put("id", 1)
                }
            }
        }
    }

}
