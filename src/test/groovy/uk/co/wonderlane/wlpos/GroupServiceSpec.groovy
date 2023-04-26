package uk.co.wonderlane.wlpos

import grails.plugin.springsecurity.SpringSecurityService
import grails.testing.gorm.DataTest
import grails.testing.services.ServiceUnitTest
import spock.lang.Specification
import uk.co.wonderlane.wlpos.enums.PrintReceiptOption

class GroupServiceSpec extends Specification implements ServiceUnitTest<GroupService>, DataTest{

    Class<?>[] getDomainClassesToMock() {
        return [Group] as Class[]
    }

    //----------------------------------- Calling get stores in group Action -----------------------------------------//

    def 'Should retrieve stores in group'() {
        given:

        StoreSettings testStoreSettings1 = new StoreSettings(retailerId: 9,
                storeId: 100,
                printReceiptOption: PrintReceiptOption.NO_PRINT,
                type: "test",
                priceBand: new PriceBand(retailerId: 9, description: "Test"),
                range: new Range(retailerId: 9, description: "Test"),
                countIncrement: BigDecimal.ONE)

        testStoreSettings1.springSecurityService = getFakeSpringSecurityService()

        Group testGroup1 = new Group(retailerId: 9, name: "Test Group", level: new GroupLevel())
        testGroup1.setId(100)
        testGroup1.addToStores(testStoreSettings1)
        testGroup1.save(flush: true, failOnError: true)

        StoreSettings testStoreSettings2 = new StoreSettings(retailerId: 9,
                storeId: 200,
                printReceiptOption: PrintReceiptOption.NO_PRINT,
                type: "test",
                priceBand: new PriceBand(retailerId: 9, description: "Test"),
                range: new Range(retailerId: 9, description: "Test"),
                countIncrement: BigDecimal.ONE)

        testStoreSettings2.springSecurityService = getFakeSpringSecurityService()

        Group testGroup2 = new Group(retailerId: 9, name: "Test Group", level: new GroupLevel(), parentGroup: testGroup1, stores: new HashSet<>())
        testGroup2.setId(200)
        testGroup2.addToStores(testStoreSettings2)
        testGroup2.save(flush: true, failOnError: true)

        when: 'getStoresInGroup action is executed'
        def response = service.getStoresInGroup(200)

        then: 'getStoresInGroup results are correct'
        noExceptionThrown()
        response != null
        response.get(0) != null
        response.get(0).storeId == 200
    }

    //----------------------------------- Calling get first store in group Action ------------------------------------//

    def 'Should retrieve first store in group'() {
        given:

        StoreSettings testStoreSettings1 = new StoreSettings(retailerId: 9,
                storeId: 100,
                printReceiptOption: PrintReceiptOption.NO_PRINT,
                type: "test",
                priceBand: new PriceBand(retailerId: 9, description: "Test"),
                range: new Range(retailerId: 9, description: "Test"),
                countIncrement: BigDecimal.ONE)

        testStoreSettings1.springSecurityService = getFakeSpringSecurityService()

        Group testGroup1 = new Group(retailerId: 9, name: "Test Group", level: new GroupLevel())
        testGroup1.setId(100)
        testGroup1.addToStores(testStoreSettings1)
        testGroup1.save(flush: true, failOnError: true)

        StoreSettings testStoreSettings2 = new StoreSettings(retailerId: 9,
                storeId: 200,
                printReceiptOption: PrintReceiptOption.NO_PRINT,
                type: "test",
                priceBand: new PriceBand(retailerId: 9, description: "Test"),
                range: new Range(retailerId: 9, description: "Test"),
                countIncrement: BigDecimal.ONE)

        testStoreSettings2.springSecurityService = getFakeSpringSecurityService()

        Group testGroup2 = new Group(retailerId: 9, name: "Test Group", level: new GroupLevel(), parentGroup: testGroup1, stores: new HashSet<>())
        testGroup2.setId(200)
        testGroup2.addToStores(testStoreSettings2)
        testGroup2.save(flush: true, failOnError: true)

        when: 'getFirstStoreInGroupHierarchy action is executed'
        def response = service.getFirstStoreInGroupHierarchy(200)

        then: 'getFirstStoreInGroupHierarchy results are correct'
        noExceptionThrown()
        response != null
        response.storeId == 200
    }

    //-------------------------------------------- Calling get group by id Action ------------------------------------//

    def 'Should retrieve group by id'() {
        given:

        StoreSettings testStoreSettings1 = new StoreSettings(retailerId: 9,
                storeId: 100,
                printReceiptOption: PrintReceiptOption.NO_PRINT,
                type: "test",
                priceBand: new PriceBand(retailerId: 9, description: "Test"),
                range: new Range(retailerId: 9, description: "Test"),
                countIncrement: BigDecimal.ONE)

        testStoreSettings1.springSecurityService = getFakeSpringSecurityService()
        service.springSecurityService = getFakeSpringSecurityService()

        Group testGroup1 = new Group(retailerId: 9, name: "Test Group", level: new GroupLevel())
        testGroup1.setId(100)
        testGroup1.addToStores(testStoreSettings1)
        testGroup1.save(flush: true, failOnError: true)

        when: 'getGroup action is executed'
        def response = service.getGroup(100)

        then: 'getGroup results are correct'
        noExceptionThrown()
        response != null
        response.id == 100
    }

    //----------------------------------------- Calling get group by level Action ------------------------------------//

    def 'Should retrieve group by level'() {
        given:

        StoreSettings testStoreSettings1 = new StoreSettings(retailerId: 9,
                storeId: 100,
                printReceiptOption: PrintReceiptOption.NO_PRINT,
                type: "test",
                priceBand: new PriceBand(retailerId: 9, description: "Test"),
                range: new Range(retailerId: 9, description: "Test"),
                countIncrement: BigDecimal.ONE)

        testStoreSettings1.springSecurityService = getFakeSpringSecurityService()
        service.springSecurityService = getFakeSpringSecurityService()

        GroupLevel groupLevel = new GroupLevel(retailerId: 9, level: 1, name: "Test Level")
        groupLevel.setId(150)
        groupLevel.save(flush: true, failOnError: true)

        Group testGroup1 = new Group(retailerId: 9, name: "Test Group", level: groupLevel)
        testGroup1.setId(100)
        testGroup1.addToStores(testStoreSettings1)
        testGroup1.save(flush: true, failOnError: true)

        when: 'getGroupsByLevel action is executed'
        def response = service.getGroupsByLevel(1)

        then: 'getGroupsByLevel results are correct'
        noExceptionThrown()
        response != null
        response.get(0) != null
        response.get(0).id == 100
    }

    //----------------------------------------- Calling save group Action --------------------------------------------//

    def 'Should save group'() {
        given:

        StoreSettings testStoreSettings1 = new StoreSettings(retailerId: 9,
                storeId: 100,
                printReceiptOption: PrintReceiptOption.NO_PRINT,
                type: "test",
                priceBand: new PriceBand(retailerId: 9, description: "Test"),
                range: new Range(retailerId: 9, description: "Test"),
                countIncrement: BigDecimal.ONE)

        testStoreSettings1.springSecurityService = getFakeSpringSecurityService()
        service.springSecurityService = getFakeSpringSecurityService()

        Group testGroup1 = new Group(retailerId: 9, name: "Test Group", level: new GroupLevel(level: 0))
        testGroup1.setId(100)
        testGroup1.addToStores(testStoreSettings1)

        when: 'saveGroup action is executed'
        def response = service.saveGroup(testGroup1)

        then: 'saveGroup results are correct'
        noExceptionThrown()
        response != null
        response.id == 100
        Group.findById(100) != null
    }

    def getFakeSpringSecurityService() {
        return Stub(SpringSecurityService) {
            getPrincipal() >> new HashMap() {
                {
                    put("storeId", 100)
                    put("storeNumber", 100)
                    put("retailerId", 9)
                    put("id", 9)
                }
            }
        }
    }
}
