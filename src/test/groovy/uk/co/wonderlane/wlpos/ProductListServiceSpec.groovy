package uk.co.wonderlane.wlpos

import grails.plugin.springsecurity.SpringSecurityService
import grails.testing.gorm.DataTest
import grails.testing.services.ServiceUnitTest
import org.grails.datastore.mapping.query.api.BuildableCriteria
import org.joda.time.DateTime
import spock.lang.Specification
import uk.co.wonderlane.wlpos.dataaccess.DatabaseCredentials
import uk.co.wonderlane.wlpos.enums.ProductHistoryType
import uk.co.wonderlane.wlpos.enums.wlim.ProductListStatus
import uk.co.wonderlane.wlpos.enums.wlim.ProductListType
import uk.co.wonderlane.wlpos.helpers.HibernateTestMockCriteria
import uk.co.wonderlane.wlpos.helpers.ProductListServiceHelperService

import java.sql.CallableStatement
import java.sql.Connection
import java.sql.ResultSet

class ProductListServiceSpec extends Specification implements ServiceUnitTest<ProductListServiceHelperService>, DataTest {

    Class<?>[] getDomainClassesToMock() {
        [StoreSettings, ProductList] as Class<?>[]
    }

    ProductListServiceHelperService productListServiceHelperService
    Connection mockConnection
    DatabaseCredentials databaseCredentials

    def setup() {
        databaseCredentials = Mock(DatabaseCredentials)
        mockConnection = Mock(Connection)
        productListServiceHelperService = new ProductListServiceHelperService(databaseCredentials, mockConnection);
    }

    //-------------------------------getCentralCounts function Unit tests----------------------------//

    void "should retrieve central counts results"() {
        given:
        ProductList testProductList = new ProductList(userId: "testUser", retailerId: 9,
                type: ProductListType.SCHEDULED_COUNT, status: ProductListStatus.PENDING, stockAdjustedOnCompletion: false)

        HibernateTestMockCriteria mockCriteria = new HibernateTestMockCriteria()
        mockCriteria.getResponses().add(testProductList)
        BuildableCriteria defaultCriteria = ProductList.createCriteria() // keep the default behavior
        ProductList.metaClass.static.createCriteria = { return mockCriteria }

        productListServiceHelperService.springSecurityService = getFakeSpringSecurityService()

        when: 'getCentralCounts action is executed'
        def serviceResponse = productListServiceHelperService.getCentralCounts("Test", 0, 100)

        ProductList.metaClass.static.createCriteria = { return defaultCriteria } // set the default value back to the class

        then: 'getCentralCounts action response is correct'
        serviceResponse != null
    }

    //-------------------------------getOrders function Unit tests----------------------------//

    void "should retrieve orders results"() {
        given:
        ProductList testProductList = new ProductList(userId: "testUser", retailerId: 9,
                type: ProductListType.ORDER, status: ProductListStatus.PENDING, stockAdjustedOnCompletion: false)

        HibernateTestMockCriteria mockCriteria = new HibernateTestMockCriteria()
        mockCriteria.getResponses().add(testProductList)
        BuildableCriteria defaultCriteria = ProductList.createCriteria() // keep the default behavior
        ProductList.metaClass.static.createCriteria = { return mockCriteria }

        productListServiceHelperService.springSecurityService = getFakeSpringSecurityService()

        when: 'getOrders action is executed'
        def serviceResponse = productListServiceHelperService.getOrders(storeId, supplierId, DateTime.now(), DateTime.now())

        ProductList.metaClass.static.createCriteria = { return defaultCriteria } // set the default value back to the class

        then: 'getOrders action response is correct'
        serviceResponse != null

        where:
        ID | storeId | supplierId
        1  | 100     | 1
        1  | null    | null
    }

    //-------------------------------getOrder function Unit tests----------------------------//

    void "should retrieve order results"() {
        given:
        ProductList testProductList = new ProductList(userId: "testUser", retailerId: 9,
                type: ProductListType.ORDER, status: ProductListStatus.PENDING, stockAdjustedOnCompletion: false)

        HibernateTestMockCriteria mockCriteria = new HibernateTestMockCriteria()
        mockCriteria.getResponses().add(testProductList)
        BuildableCriteria defaultCriteria = ProductList.createCriteria() // keep the default behavior
        ProductList.metaClass.static.createCriteria = { return mockCriteria }

        productListServiceHelperService.springSecurityService = getFakeSpringSecurityService()

        when: 'getOrder action is executed'
        def serviceResponse = productListServiceHelperService.getOrder(productListId, storeId, supplierId, DateTime.now(), DateTime.now())

        ProductList.metaClass.static.createCriteria = { return defaultCriteria } // set the default value back to the class

        then: 'getOrder action response is correct'
        serviceResponse != null

        where:
        ID | productListId | storeId | supplierId
        1  | 100           | 100     | 1
        1  | null          | null    | null
    }

    //-------------------------------getDeliveries function Unit tests----------------------------//

    void "should retrieve deliveries results"() {
        given:
        ProductList testProductList = new ProductList(userId: "testUser", retailerId: 9,
                type: ProductListType.DELIVERY, status: ProductListStatus.PENDING, stockAdjustedOnCompletion: false)

        HibernateTestMockCriteria mockCriteria = new HibernateTestMockCriteria()
        mockCriteria.getResponses().add(testProductList)
        BuildableCriteria defaultCriteria = ProductList.createCriteria() // keep the default behavior
        ProductList.metaClass.static.createCriteria = { return mockCriteria }

        productListServiceHelperService.springSecurityService = getFakeSpringSecurityService()

        when: 'getDeliveries action is executed'
        def serviceResponse = productListServiceHelperService.getDeliveries(storeId, supplierId, DateTime.now(), DateTime.now())

        ProductList.metaClass.static.createCriteria = { return defaultCriteria } // set the default value back to the class

        then: 'getDeliveries action response is correct'
        serviceResponse != null

        where:
        ID | storeId | supplierId
        1  | 100     | 1
        1  | null    | null
    }

    //-------------------------------getDelivery function Unit tests----------------------------//

    void "should retrieve delivery results"() {
        given:
        ProductList testProductList = new ProductList(userId: "testUser", retailerId: 9,
                type: ProductListType.DELIVERY, status: ProductListStatus.PENDING, stockAdjustedOnCompletion: false)

        HibernateTestMockCriteria mockCriteria = new HibernateTestMockCriteria()
        mockCriteria.getResponses().add(testProductList)
        BuildableCriteria defaultCriteria = ProductList.createCriteria() // keep the default behavior
        ProductList.metaClass.static.createCriteria = { return mockCriteria }

        productListServiceHelperService.springSecurityService = getFakeSpringSecurityService()

        when: 'getDelivery action is executed'
        def serviceResponse = productListServiceHelperService.getDelivery(100, storeId, supplierId)

        ProductList.metaClass.static.createCriteria = { return defaultCriteria } // set the default value back to the class

        then: 'getDelivery action response is correct'
        serviceResponse != null

        where:
        ID | storeId | supplierId
        1  | 100     | 1
        1  | null    | null
    }

    //-------------------------------acceptDelivery function Unit tests----------------------------//

    void "should accept delivery"() {
        given:
        productListServiceHelperService.springSecurityService = getFakeSpringSecurityService()

        CallableStatement callableStatementMock = Mock(CallableStatement)
        mockConnection.prepareCall(_) >> callableStatementMock
        callableStatementMock.execute() >> true

        when: 'acceptDelivery action is executed'
        def serviceResponse = productListServiceHelperService.acceptDelivery(100, 100)

        then: 'acceptDelivery action response is correct'
        serviceResponse
    }

    //-------------------------------getAdHocBatches function Unit tests----------------------------//

    void "should retrieve ad hoc batches"() {
        given:
        ProductList testProductList = new ProductList(userId: "testUser", retailerId: 9,
                type: ProductListType.DELIVERY, status: ProductListStatus.PENDING, stockAdjustedOnCompletion: false)

        HibernateTestMockCriteria mockCriteria = new HibernateTestMockCriteria()
        mockCriteria.getResponses().add(testProductList)
        BuildableCriteria defaultCriteria = ProductList.createCriteria() // keep the default behavior
        ProductList.metaClass.static.createCriteria = { return mockCriteria }

        productListServiceHelperService.springSecurityService = getFakeSpringSecurityService()

        when: 'getAdHocBatches action is executed'
        def serviceResponse = productListServiceHelperService.getAdHocBatches()

        ProductList.metaClass.static.createCriteria = { return defaultCriteria } // set the default value back to the class

        then: 'getAdHocBatches action response is correct'
        serviceResponse != null
    }

    //-------------------------------getScheduledBatches function Unit tests----------------------------//

    void "should retrieve scheduled batches"() {
        given:
        Map principal = new HashMap()
        Map retailer = new HashMap()
        retailer.put("twoStageSel", twoStageSel)
        principal.put("storeId", storeId)
        principal.put("storeNumber", 100)
        principal.put("retailerId", 9)
        principal.put("retailer", retailer)
        productListServiceHelperService.springSecurityService = Stub(SpringSecurityService) {
            getPrincipal() >> principal
        }

        CallableStatement callableStatementMock = Mock(CallableStatement)
        ResultSet resultSetMock = Mock(ResultSet)
        mockConnection.prepareCall(_) >> callableStatementMock
        callableStatementMock.executeQuery() >> resultSetMock
        resultSetMock.next() >>> resultSetNextResult
        resultSetMock.getInt("id") >> 100
        resultSetMock.getInt("productId") >> 150
        resultSetMock.getInt("storeId") >> 120
        resultSetMock.getString("updateDate") >> "2022-12-01 12:00:00"
        resultSetMock.getString("effectiveDate") >> "2022-12-01 12:00:00"
        resultSetMock.getString("type") >> ProductHistoryType.PRICE.name()
        resultSetMock.getInt("priceBandId") >> 1
        resultSetMock.getString("field") >> "field"
        resultSetMock.getString("fromValue") >> "1"
        resultSetMock.getString("toValue") >> "100"
        resultSetMock.getInt("userId") >> 100
        resultSetMock.getString("usersName") >> "testUser"
        resultSetMock.getInt("productVariantId") >> 100

        when: 'getScheduledBatches action is executed'
        def serviceResponse = productListServiceHelperService.getScheduledBatches(effectiveDate)

        then: 'getScheduledBatches action response is correct'
        serviceResponse

        where:
        ID | resultSetNextResult | effectiveDate  | storeId | twoStageSel
        1  | [true, false]       | DateTime.now() | 100     | true
        2  | [true, false]       | null           | null    | false
        3  | [false, false]      | null           | null    | false
    }

    //-------------------------------getProductList by id function Unit tests----------------------------//

    void "should retrieve product list by id"() {
        given:
        ProductList testProductList = new ProductList(userId: "testUser", retailerId: 9, storeId: 100,
                type: ProductListType.DELIVERY, status: ProductListStatus.PENDING, stockAdjustedOnCompletion: false)
        testProductList.setId(100)
        testProductList.save(flush: true, failOnError: true)

        productListServiceHelperService.springSecurityService = getFakeSpringSecurityService()

        when: 'getProductList action is executed'
        def serviceResponse = productListServiceHelperService.getProductList(100)

        then: 'getProductList action response is correct'
        serviceResponse != null
        serviceResponse.id == 100
    }

    //-------------------------------getProductList by id and retailer id function Unit tests----------------------------//

    void "should retrieve product list by id and and retailer id"() {
        given:
        ProductList testProductList = new ProductList(userId: "testUser", retailerId: 9, storeId: 100,
                type: ProductListType.DELIVERY, status: ProductListStatus.PENDING, stockAdjustedOnCompletion: false)
        testProductList.setId(100)
        testProductList.save(flush: true, failOnError: true)

        productListServiceHelperService.springSecurityService = getFakeSpringSecurityService()

        when: 'getProductList action is executed'
        def serviceResponse = productListServiceHelperService.getProductList(100, 9)

        then: 'getProductList action response is correct'
        serviceResponse != null
        serviceResponse.id == 100
    }

    //-------------------------------saveProductList function Unit tests----------------------------//

    void "should save product list correctly"() {
        given:
        ProductList testProductList = new ProductList(userId: "testUser", retailerId: 9, storeId: 100,
                type: ProductListType.DELIVERY, status: ProductListStatus.PENDING, stockAdjustedOnCompletion: false)
        testProductList.setId(100)

        when: 'saveProductList action is executed'
        productListServiceHelperService.saveProductList(testProductList)

        then: 'saveProductList action response is correct'
        !testProductList.hasErrors()
        ProductList.findById(100) != null
    }

    //-------------------------------saveProductLists function Unit tests----------------------------//

    void "should return null on empty array when save product lists"() {
        given:
        productListServiceHelperService.springSecurityService = getFakeSpringSecurityService()

        when: 'saveProductLists action is executed'
        def serviceResponse = productListServiceHelperService.saveProductLists(new ArrayList<>())

        then: 'saveProductLists action response is correct'
        serviceResponse == null
    }

    void "should throw back exception when save product lists"() {
        given:
        productListServiceHelperService.springSecurityService = getFakeSpringSecurityService()
        List<ProductList> inputList = new ArrayList<>()
        inputList.add(null)

        when: 'saveProductLists action is executed'
        productListServiceHelperService.saveProductLists(inputList)

        then: 'saveProductLists action response is correct'
        thrown(Exception)
    }

    void "should save product lists correctly"() {
        given:
        productListServiceHelperService.springSecurityService = getFakeSpringSecurityService()
        List<ProductList> inputList = new ArrayList<>()
        ProductList testProductList1 = new ProductList(userId: "testUser", retailerId: 9, storeId: 100,
                type: ProductListType.DELIVERY, status: ProductListStatus.PENDING, stockAdjustedOnCompletion: false)
        testProductList1.setId(100)
        ProductList testProductList2 = new ProductList(userId: "testUser", retailerId: 9, storeId: 100,
                type: ProductListType.ORDER, status: ProductListStatus.PENDING, stockAdjustedOnCompletion: false)
        testProductList2.setId(150)
        inputList.add(testProductList1)
        inputList.add(testProductList2)

        when: 'saveProductLists action is executed'
        productListServiceHelperService.saveProductLists(inputList)

        then: 'saveProductLists action response is correct'
        !testProductList1.hasErrors()
        !testProductList2.hasErrors()
    }

    //-------------------------------deleteProductList function Unit tests----------------------------//

    void "should delete product lists correctly"() {
        given:
        ProductList testProductList = new ProductList(userId: "testUser", retailerId: 9, storeId: 100,
                type: ProductListType.DELIVERY, status: ProductListStatus.PENDING, stockAdjustedOnCompletion: false)
        testProductList.setId(100)
        testProductList.save(flush: true, failOnError: true)

        when: 'deleteProductList action is executed'
        def serviceResponse = productListServiceHelperService.deleteProductList(testProductList)

        then: 'deleteProductList action response is correct'
        serviceResponse == null
        ProductList.findById(100) == null
    }

    void "should do nothing if product list is null when delete"() {
        given:

        when: 'deleteProductList action is executed'
        def serviceResponse = productListServiceHelperService.deleteProductList(null)

        then: 'deleteProductList action response is correct'
        serviceResponse == null
    }


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
