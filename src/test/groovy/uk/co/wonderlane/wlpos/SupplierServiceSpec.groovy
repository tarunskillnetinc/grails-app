package uk.co.wonderlane.wlpos

import grails.plugin.springsecurity.SpringSecurityService
import grails.testing.gorm.DataTest
import grails.testing.services.ServiceUnitTest
import org.grails.datastore.mapping.query.api.BuildableCriteria
import org.joda.time.DateTime
import spock.lang.Specification
import uk.co.wonderlane.wlpos.dataaccess.DatabaseCredentials
import uk.co.wonderlane.wlpos.enums.SymbolGroupSubscriptionStatus
import uk.co.wonderlane.wlpos.helpers.HibernateTestMockCriteria
import uk.co.wonderlane.wlpos.helpers.SupplierServiceHelperService
import uk.co.wonderlane.wlpos.supplier.*

import java.sql.CallableStatement
import java.sql.Connection
import java.sql.ResultSet

class SupplierServiceSpec extends Specification implements ServiceUnitTest<SupplierServiceHelperService>, DataTest {

    Class<?>[] getDomainClassesToMock() {
        return [Supplier, SymbolGroupSubscription, Pack, SupplierPriceUpdate] as Class[]
    }

    //-------------------------------getSuppliers function Unit tests----------------------------//

    void "should retrieve suppliers"() {
        given:
        service.springSecurityService = getFakeSpringSecurityService()
        Supplier testSupplier = new Supplier(name: "Test Supplier", retailerId: 9, storeId: 100)
        testSupplier.setId(100)

        testSupplier.save()

        when: 'getSuppliers action is executed'
        def serviceResponse = service.getSuppliers()

        then: 'getSuppliers action response is correct'
        serviceResponse != null
        serviceResponse.find { it.id == 100 } != null
    }

    //-------------------------------getSupplier by id function Unit tests----------------------------//

    void "should retrieve suppliers by id"() {
        given:
        service.springSecurityService = getFakeSpringSecurityService()
        Supplier testSupplier = new Supplier(name: "Test Supplier", retailerId: 9, storeId: 100)
        testSupplier.setId(100)

        testSupplier.save()

        when: 'getSupplier action is executed'
        def serviceResponse = service.getSupplier(100)

        then: 'getSupplier action response is correct'
        serviceResponse != null
        serviceResponse.id == 100
    }

    //-------------------------------saveSupplier function Unit tests----------------------------//

    void "should save supplier"() {
        given:
        service.springSecurityService = getFakeSpringSecurityService()
        Supplier testSupplier = new Supplier(name: "Test Supplier", retailerId: 9, storeId: 100)
        testSupplier.setId(100)

        when: 'saveSupplier action is executed'
        def serviceResponse = service.saveSupplier(testSupplier)

        then: 'saveSupplier action response is correct'
        serviceResponse != null
        serviceResponse.id == 100
        !serviceResponse.hasErrors()
        Supplier.findById(100) != null
    }

    //-------------------------------deleteSupplier function Unit tests----------------------------//

    void "should delete supplier"() {
        given:
        service.springSecurityService = getFakeSpringSecurityService()
        Supplier testSupplier = new Supplier(name: "Test Supplier", retailerId: 9, storeId: 100)
        testSupplier.setId(100)

        when: 'deleteSupplier action is executed'
        def serviceResponse = service.deleteSupplier(testSupplier)

        then: 'deleteSupplier action response is correct'
        serviceResponse == null
        Supplier.findById(100) == null
    }

    //-------------------------------getSymbolGroupSubscriptions function Unit tests----------------------------//

    void "should retrieve symbol group subscriptions"() {
        given:
        service.springSecurityService = getFakeSpringSecurityService()
        SymbolGroupSubscription testSubscription = new SymbolGroupSubscription(retailerId: 9, storeId: 100,
                symbolGroup: new SymbolGroup(), status: SymbolGroupSubscriptionStatus.ACTIVE)
        testSubscription.setId(100)

        testSubscription.save()

        when: 'getSymbolGroupSubscriptions action is executed'
        def serviceResponse = service.getSymbolGroupSubscriptions()

        then: 'getSymbolGroupSubscriptions action response is correct'
        serviceResponse != null
        serviceResponse.find { it.id == 100 } != null
    }

    //-------------------------------getSymbolGroupSubscription by id function Unit tests----------------------------//

    void "should retrieve symbol group subscriptions by id"() {
        given:
        service.springSecurityService = getFakeSpringSecurityService()
        SymbolGroupSubscription testSubscription = new SymbolGroupSubscription(retailerId: 9, storeId: 100,
                symbolGroup: new SymbolGroup(), status: SymbolGroupSubscriptionStatus.ACTIVE)
        testSubscription.setId(100)

        testSubscription.save()

        when: 'getSymbolGroupSubscription action is executed'
        def serviceResponse = service.getSymbolGroupSubscription(100)

        then: 'getSymbolGroupSubscription action response is correct'
        serviceResponse != null
        serviceResponse.id == 100
    }

    //-------------------------------getSymbolGroups by id function Unit tests----------------------------//

    void "should retrieve symbol groups"() {
        given:
        service.springSecurityService = getFakeSpringSecurityService()
        SymbolGroup testSymbolGroup = new SymbolGroup(retailerId: 9, storeId: 100, name: "Test", apiUrl: "http://fake.fakedomain.fke/api")
        testSymbolGroup.setId(100)

        testSymbolGroup.save()

        when: 'getSymbolGroups action is executed'
        def serviceResponse = service.getSymbolGroups()

        then: 'getSymbolGroups action response is correct'
        serviceResponse != null
        serviceResponse.find { it.id == 100 } != null
    }

    //-------------------------------saveSymbolGroupSubscription by id function Unit tests----------------------------//

    void "should save symbol group subscription"() {
        given:
        service.springSecurityService = getFakeSpringSecurityService()
        SymbolGroupSubscription testSubscription = new SymbolGroupSubscription(retailerId: 9, storeId: 100,
                symbolGroup: new SymbolGroup(), status: SymbolGroupSubscriptionStatus.ACTIVE)
        testSubscription.setId(100)

        when: 'saveSymbolGroupSubscription action is executed'
        def serviceResponse = service.saveSymbolGroupSubscription(testSubscription)

        then: 'saveSymbolGroupSubscription action response is correct'
        serviceResponse != null
        serviceResponse.id == 100
        SymbolGroupSubscription.findById(100) != null
    }

    //-------------------------------getPacksUpdatedSince by id function Unit tests----------------------------//

    void "should retrieve packs updated after given date"() {
        given:
        HibernateTestMockCriteria mockCriteria = new HibernateTestMockCriteria()
        BuildableCriteria defaultCriteria = Pack.createCriteria() // keep the default behavior
        Pack.metaClass.static.createCriteria = { return mockCriteria }

        when: 'getPacksUpdatedSince action is executed'
        def serviceResponse = service.getPacksUpdatedSince(DateTime.now())

        Pack.metaClass.static.createCriteria = { return defaultCriteria }

        then: 'getPacksUpdatedSince action response is correct'
        serviceResponse != null
    }

    //-------------------------------getSupplierPriceUpdates by id function Unit tests----------------------------//

    void "should retrieve supplier price updates"() {
        given:
        Connection mockConnection = Mock(Connection)
        DatabaseCredentials mockCredentials = Mock(DatabaseCredentials)
        SupplierServiceHelperService supplierServiceHelper = new SupplierServiceHelperService(mockCredentials, mockConnection)

        Map principal = new HashMap()
        principal.put("retailerId", 9)
        principal.put("storeId", storeId)
        supplierServiceHelper.springSecurityService = Stub(SpringSecurityService) {
            getPrincipal() >> principal
        }

        CallableStatement callableStatementMock = Mock(CallableStatement)
        ResultSet resultSetMock = Mock(ResultSet)

        mockConnection.prepareCall(_) >> callableStatementMock
        callableStatementMock.executeQuery() >> resultSetMock
        callableStatementMock.getResultSet() >> resultSetMock
        resultSetMock.next() >>> [true, false, true, false]
        resultSetMock.getInt("packId") >> 100
        resultSetMock.getLong("sku") >> 50
        resultSetMock.getString("description") >> "Test"
        resultSetMock.getInt("quantity") >> 100
        resultSetMock.getString("effectiveDate") >> "2022-12-05 12:00:00"
        resultSetMock.getBigDecimal("oldPackPrice") >> BigDecimal.ZERO
        resultSetMock.getBigDecimal("newPackPrice") >> BigDecimal.ZERO
        resultSetMock.getBigDecimal("retailPrice") >> BigDecimal.ZERO
        resultSetMock.getBigDecimal("recommendedRetailPrice") >> BigDecimal.ZERO
        resultSetMock.getBigDecimal("productId") >> BigDecimal.ZERO
        resultSetMock.getInt("totalCount") >> 1

        when: 'getSupplierPriceUpdates action is executed'
        def serviceResponse = supplierServiceHelper.getSupplierPriceUpdates(DateTime.now(), 100, supplierId, categoryId, 0, 100)

        then: 'getSupplierPriceUpdates action response is correct'
        serviceResponse != null
        serviceResponse.results != null
        !serviceResponse.results.isEmpty()
        serviceResponse.totalCount == 1

        where:
        ID | storeId | supplierId | categoryId | results
        1  | 100     | 150        | 200        | [true, false, true, false]
        2  | null    | null       | null       | [true, false]
    }

    //-------------------------------saveSupplierPriceUpdates by id function Unit tests----------------------------//

    void "should save supplier price updates"() {
        given:
        Connection mockConnection = Mock(Connection)
        DatabaseCredentials mockCredentials = Mock(DatabaseCredentials)
        SupplierServiceHelperService supplierServiceHelper = new SupplierServiceHelperService(mockCredentials, mockConnection)

        Map principal = new HashMap()
        principal.put("retailerId", 9)
        principal.put("storeId", storeId)
        principal.put("usersName", "testUser")
        principal.put("id", 1)
        supplierServiceHelper.springSecurityService = Stub(SpringSecurityService) {
            getPrincipal() >> principal
        }

        CallableStatement callableStatementMock = Mock(CallableStatement)
        mockConnection.prepareCall(_) >> callableStatementMock

        List priceUpdates = new ArrayList<>()
        for (int i = 1; i < 202; i++) { // using 205 to test batch size of 200
            def update = [sku: i, productId: 100, oldPrice: oldPrice, recommendedRetailPrice: recRetailPx, packId: i]
            priceUpdates.add(update)
        }
        def updateDuplicate = [sku: 1, productId: 100, oldPrice: oldPrice, recommendedRetailPrice: recRetailPx]
        priceUpdates.add(updateDuplicate)

        when: 'getSupplierPriceUpdates action is executed'
        supplierServiceHelper.saveSupplierPriceUpdates(priceUpdates, new PriceBand(), DateTime.now())

        then: 'getSupplierPriceUpdates action response is correct'
        // nothing to assert, pass if test does not throw any exception

        where:
        ID | storeId | supplierId | oldPrice | recRetailPx
        1  | 100     | 150        | true     | 100
        2  | null    | null       | false    | null
    }

    //-------------------------------getSuppliers by id function Unit tests----------------------------//

    void "should load suppliers based on provided arguments"() {
        given:

        Supplier testSupplier1 = new Supplier(name: "prefixTest Suppliersuffix", retailerId: 9, storeId: 100)
        testSupplier1.setId(100)
        testSupplier1.save()

        Supplier testSupplier2 = new Supplier(name: "prefixTest Suppliersuffix", retailerId: 9, storeId: null)
        testSupplier2.setId(200)
        testSupplier2.save()

        Map principal = new HashMap()
        principal.put("retailerId", 9)
        principal.put("storeId", storeId)
        service.springSecurityService = Stub(SpringSecurityService) {
            getPrincipal() >> principal
        }

        when: 'getSupplierPriceUpdates action is executed'
        def serviceResponse = service.getSuppliers(searchTerm, searchBy, 0, 100, "name", "asc")

        then: 'getSupplierPriceUpdates action response is correct'
        serviceResponse != null
        serviceResponse.suppliers.size() > 0
        serviceResponse.totalCount > 0

        where:
        ID | storeId | searchTerm | searchBy
        1  | 100     | "Test"     | "Name"
        2  | null    | "Test"     | null
    }

    def getFakeSpringSecurityService() {
        return Stub(SpringSecurityService) {
            getPrincipal() >> new HashMap() {
                {
                    put("storeId", 100)
                    put("storeNumber", 100)
                    put("retailerId", 9)
                }
            }
        }
    }
}
