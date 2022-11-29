package uk.co.wonderlane.wlpos


import grails.plugin.springsecurity.SpringSecurityService
import grails.testing.gorm.DataTest
import grails.testing.services.ServiceUnitTest
import org.grails.datastore.mapping.query.api.BuildableCriteria
import org.joda.time.DateTime
import spock.lang.Specification
import uk.co.wonderlane.wlpos.helpers.HibernateTestMockCriteria

class ReceiptServiceSpec extends Specification implements ServiceUnitTest<ReceiptService>, DataTest {

    Class<?>[] getDomainClassesToMock() {
        [Receipt] as Class<?>[]
    }

    //-------------------------------getReceipts function Unit tests----------------------------//

    void "should retrieve receipts according to input parameters"() {
        given:
        Map principal = new HashMap()
        principal.put("storeId", storeId)
        principal.put("storeNumber", 100)
        principal.put("retailerId", 9)
        service.springSecurityService = Stub(SpringSecurityService) {
            getPrincipal() >> principal
        }

        Receipt testReceipt = new Receipt()
        testReceipt.setDateGenerated(DateTime.now())
        testReceipt.setRetailerId(9)
        testReceipt.setStoreId(100)
        testReceipt.setTillId(150)
        testReceipt.setUsersName("TEST USER")
        testReceipt.setBarcode("TEST_BARCODE")
        testReceipt.setTransactionId(1500)

        testReceipt.save(flush: true, failOnError: true)

        HibernateTestMockCriteria mockCriteria = new HibernateTestMockCriteria()
        mockCriteria.getResponses().add(mockCriteria)
        BuildableCriteria defaultCriteria = Receipt.createCriteria() // keep the default behavior
        Receipt.metaClass.static.createCriteria = { return mockCriteria }

        when: 'getReceipts action is executed'
        def controllerResponse = service.getReceipts(fromDate, toDate, tillId, txnId, offset, max)

        Receipt.metaClass.static.createCriteria = { return defaultCriteria } // set the default value back to the class

        then: 'getReceipts action response is correct'
        controllerResponse != null
        controllerResponse.size() == 1

        where:
        ID | fromDate                    | toDate                     | tillId | txnId | offset | max | storeId
        1  | DateTime.now().minusDays(1) | DateTime.now().plusDays(1) | 150    | 1500  | 0      | 10  | 100
        2  | DateTime.now().minusDays(1) | DateTime.now().plusDays(1) | null   | 1500  | 0      | 10  | null
        3  | DateTime.now().minusDays(1) | DateTime.now().plusDays(1) | null   | null  | 0      | 10  | 100
        4  | DateTime.now().minusDays(1) | DateTime.now().plusDays(1) | 150    | null  | 0      | 10  | null
    }

    //-------------------------------getReceipt by id function Unit tests----------------------------//

    void "should retrieve receipt by id according to input parameters"() {
        given:
        Map principal = new HashMap()
        principal.put("storeId", storeId)
        principal.put("storeNumber", 100)
        principal.put("retailerId", 9)
        service.springSecurityService = Stub(SpringSecurityService) {
            getPrincipal() >> principal
        }

        Receipt testReceipt = new Receipt()
        testReceipt.setId(100)
        testReceipt.setDateGenerated(DateTime.now())
        testReceipt.setRetailerId(9)
        testReceipt.setStoreId(100)
        testReceipt.setTillId(150)
        testReceipt.setUsersName("TEST USER")
        testReceipt.setBarcode("TEST_BARCODE")
        testReceipt.setTransactionId(1500)

        testReceipt.save(flush: true, failOnError: true)

        when: 'getReceipt by id action is executed'
        def controllerResponse = service.getReceipt(100)

        then: 'getReceipt by id action response is correct'
        controllerResponse != null

        where:
        ID | storeId
        1  | 100
        2  | null
    }

}
