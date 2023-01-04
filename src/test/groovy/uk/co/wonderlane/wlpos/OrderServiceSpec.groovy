package uk.co.wonderlane.wlpos

import grails.plugin.springsecurity.SpringSecurityService
import grails.testing.gorm.DataTest
import grails.testing.services.ServiceUnitTest
import spock.lang.Specification
import uk.co.wonderlane.wlpos.dataaccess.DatabaseCredentials
import uk.co.wonderlane.wlpos.enums.Role
import uk.co.wonderlane.wlpos.enums.wlim.ProductListType
import uk.co.wonderlane.wlpos.helpers.ConnectionTestHelper
import uk.co.wonderlane.wlpos.helpers.OrderServiceHelper
import uk.co.wonderlane.wlpos.supplier.Supplier
import uk.co.wonderlane.wlpos.supplier.SymbolGroup

import java.sql.CallableStatement
import java.sql.ResultSet
import java.sql.Timestamp
import java.time.Instant

class OrderServiceSpec extends Specification implements ServiceUnitTest<OrderService>, DataTest {

    Class<?>[] getDomainClassesToMock() {
        return [Supplier] as Class[]
    }

    ConnectionTestHelper connectionHelper
    OrderService orderService
    ResultSet resultSetMock
    CallableStatement callableStatement

    def setup() {
        callableStatement = Mock(CallableStatement)
        resultSetMock = Mock(ResultSet)
        callableStatement.getResultSet() >> resultSetMock
        callableStatement.execute() >> resultSetMock
        callableStatement.executeQuery() >> resultSetMock
        connectionHelper = new ConnectionTestHelper(callableStatement, resultSetMock)
        orderService = new OrderServiceHelper(Mock(DatabaseCredentials), connectionHelper)
    }

    def cleanup() {
        connectionHelper.reset();
    }

    //-------------------------------create product list function Unit tests----------------------------//

    void 'should handle null product list returned from DB on create new product list'() {
        given:
        orderService.springSecurityService = getFakeSpringSecurityService()

        Supplier testSupplier = new Supplier(name: "Test Supplier", retailerId: 9, storeId: 100)
        testSupplier.setId(100)
        testSupplier.save(flush: true, failOnError: true)

        User testUser = new User(
                username: "testUser", password: "password", defaultStoreId: 234,
                name: "Test", active: true, role: Role.ENGINEER, dateOfBirth: new Date()
        )
        testUser.setId(100)
        orderService.userService = Stub(UserService) {
            getUser(_) >> testUser
        }

        when: 'createProductList action is executed'
        def serviceResponse = orderService.createProductList(null, ProductListType.ORDER, testSupplier)

        then: 'createProductList action response is correct'
        serviceResponse == null
        connectionHelper.isCommitted()
        connectionHelper.isClosed()
    }

    void 'should create new product list with valid parameters'() {
        given:
        orderService.springSecurityService = getFakeSpringSecurityService()

        callableStatement.execute() >> true
        callableStatement.getResultSet() >> resultSetMock

        resultSetMock.next() >>> [true, true, true, false]


        resultSetMock.wasNull() >> returnNullResults

        resultSetMock.getInt("orderId") >> orderId
        resultSetMock.getInt("id") >> 100
        resultSetMock.getString("storeId") >> "100"
        resultSetMock.getString("userId") >> "testUser"
        resultSetMock.getString("type") >> "ORDER"
        resultSetMock.getString("status") >> "IN_PROGRESS"
        resultSetMock.getString("parentId") >> "100"
        resultSetMock.getString("parentType") >> parentType
        resultSetMock.getTimestamp("dateStarted") >> Timestamp.from(Instant.now())
        resultSetMock.getTimestamp("dateCompleted") >> Timestamp.from(Instant.now())
        resultSetMock.getTimestamp("startDate") >> Timestamp.from(Instant.now())
        resultSetMock.getTimestamp("endDate") >> Timestamp.from(Instant.now())

        resultSetMock.getString("ownerUserId") >> "testUser"
        resultSetMock.getString("ownerUsersName") >> "testUser"
        resultSetMock.getString("description") >> "Test"
        resultSetMock.getString("reasonId") >> "Test"
        resultSetMock.getString("reasonDescription") >> "Test"
        resultSetMock.getString("supplierId") >> "100"
        resultSetMock.getString("supplierReference") >> "100"

        resultSetMock.getInt("productVariantId") >> 200
        resultSetMock.getString("sku") >> "200"
        resultSetMock.getString("description") >> "Test"
        resultSetMock.getString("receiptDescription") >> "Test"
        resultSetMock.getString("barcodes") >> "BC1,BC2,BC3"
        resultSetMock.getBigDecimal("price") >> BigDecimal.TEN
        resultSetMock.getInt("quantity") >> 100
        resultSetMock.getInt("fillQuantity") >> 50
        resultSetMock.getInt("parentQuantity") >> 200
        resultSetMock.getInt("packedQuantity") >> packQty
        resultSetMock.getInt("packId") >> 180
        resultSetMock.getString("orderCode") >> "132"

        Supplier testSupplier = new Supplier(name: "Test Supplier", retailerId: 9, storeId: 100)
        testSupplier.setId(100)
        testSupplier.save(flush: true, failOnError: true)

        User testUser = new User(
                username: userName, password: "password", defaultStoreId: 234,
                name: "Test", active: true, role: Role.ENGINEER, dateOfBirth: new Date()
        )
        testUser.setId(100)
        orderService.userService = Stub(UserService) {
            getUser(_) >> testUser
        }

        when: 'createProductList action is executed'
        def serviceResponse = orderService.createProductList(null, ProductListType.ORDER, testSupplier)

        then: 'createProductList action response is correct'
        serviceResponse != null
        connectionHelper.isCommitted()
        connectionHelper.isClosed()

        where:
        ID | orderId | parentType | returnNullResults | userName   | packQty
        1  | 100     | "ORDER"    | false             | "testUser" | 10
        2  | -1      | null       | true              | null       | 0
    }

    void 'should throw exception and rollback transaction on user service error'() {
        given:
        orderService.springSecurityService = getFakeSpringSecurityService()

        Supplier testSupplier = new Supplier(name: "Test Supplier", retailerId: 9, storeId: 100)
        testSupplier.setId(100)
        testSupplier.save(flush: true, failOnError: true)

        when: 'createProductList action is executed'
        def serviceResponse = orderService.createProductList(null, ProductListType.ORDER, testSupplier)

        then: 'createProductList action response is correct'
        serviceResponse == null
        Exception e = thrown()
        e != null
        !connectionHelper.isCommitted()
        connectionHelper.isClosed()
        connectionHelper.isRolledBack()
    }

    //-------------------------------save product order function Unit tests----------------------------//

    void 'should be able to save product pack lines'() {
        given:
        orderService.springSecurityService = getFakeSpringSecurityService()

        PackLineRequestCommand command = new PackLineRequestCommand()
        command.setPackLines(new ArrayList<>())
        command.getPackLines().add(getMockPackLine(100))
        command.getPackLines().add(getMockPackLine(200))

        callableStatement.execute() >> true
        callableStatement.getResultSet() >> resultSetMock

        resultSetMock.next() >>> [true, true]


        resultSetMock.wasNull() >> returnNullResults

        resultSetMock.getInt("id") >> 100
        resultSetMock.getInt("productId") >> 100
        resultSetMock.getInt("storeId") >> 100
        resultSetMock.getLong("sku") >> 100L
        resultSetMock.getBigDecimal("price") >> BigDecimal.TEN
        resultSetMock.getBigDecimal("costPrice") >> BigDecimal.TEN
        resultSetMock.getString("size") >> "EACH"
        resultSetMock.getString("colour") >> "#fffff"
        resultSetMock.getInt("quantityOnOrder") >> 100
        resultSetMock.getInt("minimumStockLevel") >> 100
        resultSetMock.getTimestamp("effectiveDate") >> Timestamp.from(Instant.now())

        resultSetMock.getInt("productListItemId") >> 120


        when: 'saveProductOrder action is executed'
        orderService.saveProductOrder(command)

        then: 'saveProductOrder action response is correct'
        connectionHelper.isCommitted()
        connectionHelper.isClosed()
        !connectionHelper.isRolledBack()

        where:
        ID | returnNullResults
        1  | false
        2  | true
    }

    void 'should rollback transaction on exception while save product pack lines'() {
        given:
        orderService.springSecurityService = null // try to trigger exception by removing this service

        PackLineRequestCommand command = new PackLineRequestCommand()
        command.setPackLines(new ArrayList<>())
        command.getPackLines().add(getMockPackLine(100))
        command.getPackLines().add(getMockPackLine(200))

        callableStatement.execute() >> true
        callableStatement.getResultSet() >> resultSetMock

        resultSetMock.next() >>> [true, false, true, false, true, false]


        resultSetMock.wasNull() >> returnNullResults

        resultSetMock.getInt("id") >> 100
        resultSetMock.getInt("productId") >> 100
        resultSetMock.getInt("storeId") >> 100
        resultSetMock.getLong("sku") >> 100L
        resultSetMock.getBigDecimal("price") >> BigDecimal.TEN
        resultSetMock.getBigDecimal("costPrice") >> BigDecimal.TEN
        resultSetMock.getString("size") >> "EACH"
        resultSetMock.getString("colour") >> "#fffff"
        resultSetMock.getInt("quantityOnOrder") >> 100
        resultSetMock.getInt("minimumStockLevel") >> 100
        resultSetMock.getTimestamp("effectiveDate") >> Timestamp.from(Instant.now())

        resultSetMock.getInt("productListItemId") >> 120


        when: 'saveProductOrder action is executed'
        orderService.saveProductOrder(command)

        then: 'saveProductOrder action response is correct'
        Exception e = thrown()
        e != null
        !connectionHelper.isCommitted()
        connectionHelper.isClosed()
        connectionHelper.isRolledBack()

        where:
        ID | returnNullResults
        1  | false
        2  | true
    }

    //-------------------------------confirm order function Unit tests----------------------------//

    void 'should be able to confirm order product list'() {
        given:
        orderService.springSecurityService = getFakeSpringSecurityService()
        orderService.nisaService = Stub(NisaService) {
            generateXMLForOrder(_, _) >> null
        }

        Supplier testSupplier = new Supplier(name: "Test Supplier", retailerId: 9, storeId: 100)
        testSupplier.setId(100)
        testSupplier.save(flush: true, failOnError: true)

        SymbolGroup testSymbolGroup = new SymbolGroup()
        testSymbolGroup.setId(symbolGroupId)

        testSupplier.setSymbolGroup(testSymbolGroup)

        callableStatement.execute() >> true
        callableStatement.getResultSet() >> resultSetMock

        resultSetMock.next() >>> [true, false, true, false, true, false, true, true, true]

        resultSetMock.wasNull() >> returnNullResults

        resultSetMock.getInt("orderId") >> 100
        resultSetMock.getInt("id") >> 100
        resultSetMock.getString("storeId") >> "100"
        resultSetMock.getString("userId") >> "testUser"
        resultSetMock.getString("type") >> "ORDER"
        resultSetMock.getString("status") >> "IN_PROGRESS"
        resultSetMock.getString("parentId") >> "100"
        resultSetMock.getString("parentType") >> "ORDER"
        resultSetMock.getTimestamp("dateStarted") >> Timestamp.from(Instant.now())
        resultSetMock.getTimestamp("dateCompleted") >> Timestamp.from(Instant.now())
        resultSetMock.getTimestamp("startDate") >> Timestamp.from(Instant.now())
        resultSetMock.getTimestamp("endDate") >> Timestamp.from(Instant.now())

        resultSetMock.getString("ownerUserId") >> "testUser"
        resultSetMock.getString("ownerUsersName") >> "testUser"
        resultSetMock.getString("description") >> "Test"
        resultSetMock.getString("reasonId") >> "Test"
        resultSetMock.getString("reasonDescription") >> "Test"
        resultSetMock.getString("supplierId") >> "100"
        resultSetMock.getString("supplierReference") >> "100"

        resultSetMock.getInt("productVariantId") >> 200
        resultSetMock.getString("sku") >> "200"
        resultSetMock.getString("description") >> "Test"
        resultSetMock.getString("receiptDescription") >> "Test"
        resultSetMock.getString("barcodes") >> "BC1,BC2,BC3"
        resultSetMock.getBigDecimal("price") >> BigDecimal.TEN
        resultSetMock.getInt("quantity") >> itemQty
        resultSetMock.getInt("fillQuantity") >> 50
        resultSetMock.getInt("parentQuantity") >> 200
        resultSetMock.getInt("packedQuantity") >> 10
        resultSetMock.getInt("packId") >> 180
        resultSetMock.getString("orderCode") >> "132"

        resultSetMock.getInt("productListId") >> 100
        resultSetMock.getInt("productListItemGroupId") >> itemGroupId


        when: 'confirmOrder action is executed'
        def serviceResponse = orderService.confirmOrder(100, testSupplier)

        then: 'confirmOrder action response is correct'
        serviceResponse == null
        connectionHelper.isCommitted()
        connectionHelper.isClosed()
        !connectionHelper.isRolledBack()

        where:
        ID | returnNullResults | itemGroupId | itemQty | symbolGroupId
        1  | false             | 100         | 100     | 100
        2  | false             | -1          | 10      | -1
    }

    void 'should rollback transaction on exception while confirm order product list'() {
        given:
        orderService.nisaService = Stub(NisaService) {
            generateXMLForOrder(_, _) >> null
        }

        Supplier testSupplier = new Supplier(name: "Test Supplier", retailerId: 9, storeId: 100)
        testSupplier.setId(100)
        testSupplier.save(flush: true, failOnError: true)

        SymbolGroup testSymbolGroup = new SymbolGroup()
        testSymbolGroup.setId(symbolGroupId)

        testSupplier.setSymbolGroup(testSymbolGroup)

        callableStatement.execute() >> true
        callableStatement.getResultSet() >> resultSetMock

        resultSetMock.next() >>> [true, false, true, false, true, false, true, true, true]

        resultSetMock.wasNull() >> returnNullResults

        resultSetMock.getInt("orderId") >> 100
        resultSetMock.getInt("id") >> 100
        resultSetMock.getString("storeId") >> "100"
        resultSetMock.getString("userId") >> "testUser"
        resultSetMock.getString("type") >> "ORDER"
        resultSetMock.getString("status") >> "IN_PROGRESS"
        resultSetMock.getString("parentId") >> "100"
        resultSetMock.getString("parentType") >> "NON_EXISTING_TYPE" // try to trigger exception by passing non existing value

        when: 'confirmOrder action is executed'
        def serviceResponse = orderService.confirmOrder(100, testSupplier)

        then: 'confirmOrder action response is correct'
        Exception e = thrown()
        e != null
        serviceResponse == null
        !connectionHelper.isCommitted()
        connectionHelper.isClosed()
        connectionHelper.isRolledBack()

        where:
        ID | returnNullResults | itemGroupId | itemQty | symbolGroupId
        1  | false             | 100         | 100     | 100
        2  | false             | -1          | 10      | -1
    }

    //-------------------------------delete product list function Unit tests----------------------------//

    void 'should be able to delete order product list'() {
        given:
        orderService.springSecurityService = getFakeSpringSecurityService()

        callableStatement.execute() >> true
        callableStatement.getResultSet() >> resultSetMock

        resultSetMock.next() >>> [true, false, true, false, true, false]

        resultSetMock.wasNull() >> returnNullResults

        resultSetMock.getInt("orderId") >> 100
        resultSetMock.getInt("id") >> 100
        resultSetMock.getString("storeId") >> "100"
        resultSetMock.getString("userId") >> "testUser"
        resultSetMock.getString("type") >> "ORDER"
        resultSetMock.getString("status") >> "IN_PROGRESS"
        resultSetMock.getString("parentId") >> "100"
        resultSetMock.getString("parentType") >> "ORDER"
        resultSetMock.getTimestamp("dateStarted") >> Timestamp.from(Instant.now())
        resultSetMock.getTimestamp("dateCompleted") >> Timestamp.from(Instant.now())
        resultSetMock.getTimestamp("startDate") >> Timestamp.from(Instant.now())
        resultSetMock.getTimestamp("endDate") >> Timestamp.from(Instant.now())

        resultSetMock.getString("ownerUserId") >> "testUser"
        resultSetMock.getString("ownerUsersName") >> "testUser"
        resultSetMock.getString("description") >> "Test"
        resultSetMock.getString("reasonId") >> "Test"
        resultSetMock.getString("reasonDescription") >> "Test"
        resultSetMock.getString("supplierId") >> "100"
        resultSetMock.getString("supplierReference") >> "100"

        resultSetMock.getInt("productVariantId") >> 200
        resultSetMock.getString("sku") >> "200"
        resultSetMock.getString("description") >> "Test"
        resultSetMock.getString("receiptDescription") >> "Test"
        resultSetMock.getString("barcodes") >> "BC1,BC2,BC3"
        resultSetMock.getBigDecimal("price") >> BigDecimal.TEN
        resultSetMock.getInt("quantity") >> itemQty
        resultSetMock.getInt("fillQuantity") >> 50
        resultSetMock.getInt("parentQuantity") >> 200
        resultSetMock.getInt("packedQuantity") >> 10
        resultSetMock.getInt("packId") >> 180
        resultSetMock.getString("orderCode") >> "132"

        resultSetMock.getInt("productListId") >> 100
        resultSetMock.getInt("productListItemGroupId") >> itemGroupId


        when: 'deleteProductList action is executed'
        def serviceResponse = orderService.deleteProductList(100)

        then: 'deleteProductList action response is correct'
        serviceResponse == null
        connectionHelper.isCommitted()
        connectionHelper.isClosed()
        !connectionHelper.isRolledBack()

        where:
        ID | returnNullResults | itemGroupId | itemQty
        1  | false             | 100         | 100
        2  | false             | -1          | 10
    }

    void 'should rollback transaction on exception while delete order product list'() {
        given:
        callableStatement.execute() >> true
        callableStatement.getResultSet() >> resultSetMock

        resultSetMock.next() >>> [true, false, true, false, true, false]

        resultSetMock.wasNull() >> returnNullResults

        resultSetMock.getInt("orderId") >> 100
        resultSetMock.getInt("id") >> 100
        resultSetMock.getString("storeId") >> "100"
        resultSetMock.getString("userId") >> "testUser"
        resultSetMock.getString("type") >> "ORDER"
        resultSetMock.getString("status") >> "IN_PROGRESS"
        resultSetMock.getString("parentId") >> "100"
        resultSetMock.getString("parentType") >> "NON_EXISTING_TYPE" // try to trigger exception by passing non existing value

        when: 'deleteProductList action is executed'
        def serviceResponse = orderService.deleteProductList(100)

        then: 'deleteProductList action response is correct'
        Exception e = thrown()
        e != null
        serviceResponse == null
        !connectionHelper.isCommitted()
        connectionHelper.isClosed()
        connectionHelper.isRolledBack()

        where:
        ID | returnNullResults | itemGroupId | itemQty
        1  | false             | 100         | 100
        2  | false             | -1          | 10
    }

    //-------------------------------get active product list function Unit tests----------------------------//

    void 'should be able to retrieve active product list for user'() {
        given:
        orderService.springSecurityService = getFakeSpringSecurityService()

        callableStatement.execute() >> true
        callableStatement.getResultSet() >> resultSetMock

        resultSetMock.next() >>> [true, false, true, false, true, false]

        resultSetMock.wasNull() >> returnNullResults

        resultSetMock.getInt("orderId") >> 100
        resultSetMock.getInt("id") >> 100
        resultSetMock.getString("storeId") >> "100"
        resultSetMock.getString("userId") >> "testUser"
        resultSetMock.getString("type") >> "ORDER"
        resultSetMock.getString("status") >> "IN_PROGRESS"
        resultSetMock.getString("parentId") >> "100"
        resultSetMock.getString("parentType") >> "ORDER"
        resultSetMock.getTimestamp("dateStarted") >> Timestamp.from(Instant.now())
        resultSetMock.getTimestamp("dateCompleted") >> Timestamp.from(Instant.now())
        resultSetMock.getTimestamp("startDate") >> Timestamp.from(Instant.now())
        resultSetMock.getTimestamp("endDate") >> Timestamp.from(Instant.now())

        resultSetMock.getString("ownerUserId") >> "testUser"
        resultSetMock.getString("ownerUsersName") >> "testUser"
        resultSetMock.getString("description") >> "Test"
        resultSetMock.getString("reasonId") >> "Test"
        resultSetMock.getString("reasonDescription") >> "Test"
        resultSetMock.getString("supplierId") >> "100"
        resultSetMock.getString("supplierReference") >> "100"

        resultSetMock.getInt("productVariantId") >> 200
        resultSetMock.getString("sku") >> "200"
        resultSetMock.getString("description") >> "Test"
        resultSetMock.getString("receiptDescription") >> "Test"
        resultSetMock.getString("barcodes") >> "BC1,BC2,BC3"
        resultSetMock.getBigDecimal("price") >> BigDecimal.TEN
        resultSetMock.getInt("quantity") >> itemQty
        resultSetMock.getInt("fillQuantity") >> 50
        resultSetMock.getInt("parentQuantity") >> 200
        resultSetMock.getInt("packedQuantity") >> 10
        resultSetMock.getInt("packId") >> 180
        resultSetMock.getString("orderCode") >> "132"

        resultSetMock.getInt("productListId") >> 100
        resultSetMock.getInt("productListItemGroupId") >> itemGroupId


        when: 'getActiveProductList action is executed'
        def serviceResponse = orderService.getActiveProductList(ProductListType.ORDER, "testUser")

        then: 'getActiveProductList action response is correct'
        serviceResponse != null
        !connectionHelper.isCommitted()
        connectionHelper.isClosed()
        !connectionHelper.isRolledBack()

        where:
        ID | returnNullResults | itemGroupId | itemQty
        1  | false             | 100         | 100
        2  | false             | -1          | 10
    }

    void 'should throw error and close db connection if user principal unavailable when retrieve active product list for user'() {
        given:
        orderService.springSecurityService = null // set this service null to trigger exception

        when: 'getActiveProductList action is executed'
        def serviceResponse = orderService.getActiveProductList(ProductListType.ORDER, "testUser")

        then: 'getActiveProductList action response is correct'
        Exception e = thrown()
        e != null
        serviceResponse == null
        !connectionHelper.isCommitted()
        connectionHelper.isClosed()
        !connectionHelper.isRolledBack()

    }

    private PackLinesCommand getMockPackLine(int id) {
        PackLinesCommand packLine = new PackLinesCommand()

        packLine.setId(id)
        packLine.setQuantity(100)
        packLine.setOrderCode("100")

        return packLine
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
