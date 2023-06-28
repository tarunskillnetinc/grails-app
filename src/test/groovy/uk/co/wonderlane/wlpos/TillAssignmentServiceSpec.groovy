package uk.co.wonderlane.wlpos

import grails.plugin.springsecurity.SpringSecurityService
import grails.testing.gorm.DataTest
import grails.testing.services.ServiceUnitTest
import org.joda.time.DateTime
import spock.lang.Specification

class TillAssignmentServiceSpec extends Specification implements ServiceUnitTest<TillAssignmentService>, DataTest{

    TillAssignmentService tillAssignmentService;

    def setup() {
        tillAssignmentService = new TillAssignmentService()
        tillAssignmentService.springSecurityService = Stub(SpringSecurityService) {
            getPrincipal() >> new HashMap() {{
                put("id", 9)
                put("retailerId", 17)
                put("storeId", 103)
                put("tillId", 1100)
                put("serialNumber", "TESTSN")
            }}
        }
    }

    def cleanup() {
    }

    Class<?>[] getDomainClassesToMock(){
        return [TillConfiguration, TillStock] as Class[]
    }

    def 'Test GetTills'() {
        given:
        var tillConfiguration = createTillConfiguration()

        when: 'Save Till is called'
        tillAssignmentService.saveTill(tillConfiguration)

        then: 'Check what we have saved and fetched successfully'
        def tillConfigs = tillAssignmentService.getTills()

        assert tillConfigs != null
    }

    def 'Test GetTillsByAllFilters'() {
        given:
        var tillConfiguration = createTillConfiguration()

        when: 'Save Till is called'
        tillAssignmentService.saveTill(tillConfiguration)

        then: 'Check that the retrieved till matches the storeId, tillId, and Serial Number'
        def tillConfigs = tillAssignmentService.getTillsByAllFilters(tillAssignmentService.springSecurityService.principal.storeId, tillAssignmentService.springSecurityService.principal.tillId, tillAssignmentService.springSecurityService.principal.serialNumber)

        assert tillConfigs != null
        assert tillConfigs.first().storeId == tillAssignmentService.springSecurityService.principal.storeId
        assert tillConfigs.first().tillId == tillAssignmentService.springSecurityService.principal.tillId
        assert tillConfigs.first().serialNumber == tillAssignmentService.springSecurityService.principal.serialNumber
    }

    def 'Test GetTillsByStoreIdAndTillId'() {
        given:
        var tillConfiguration = createTillConfiguration()

        when: 'Save Till is called'
        tillAssignmentService.saveTill(tillConfiguration)

        then: 'Check that the retrieved till matches the storeId, tillId'
        def tillConfigs = tillAssignmentService.getTillsByStoreIdAndTillId(tillAssignmentService.springSecurityService.principal.storeId, tillAssignmentService.springSecurityService.principal.tillId)

        assert tillConfigs != null
        assert tillConfigs.first().storeId == tillAssignmentService.springSecurityService.principal.storeId
        assert tillConfigs.first().tillId == tillAssignmentService.springSecurityService.principal.tillId
    }

    def 'Test GetTillsByStoreIdAndSerialNumber'() {
        given:
        var tillConfiguration = createTillConfiguration()

        when: 'Save Till is called'
        tillAssignmentService.saveTill(tillConfiguration)

        then: 'Check that the retrieved till matches the storeId and Serial Number'
        def tillConfigs = tillAssignmentService.getTillsByStoreIdAndSerialNumber(tillAssignmentService.springSecurityService.principal.storeId, tillAssignmentService.springSecurityService.principal.serialNumber)

        assert tillConfigs != null
        assert tillConfigs.first().storeId == tillAssignmentService.springSecurityService.principal.storeId
        assert tillConfigs.first().serialNumber == tillAssignmentService.springSecurityService.principal.serialNumber
    }

    def 'Test GetTillsByTillIdAndSerialNumber'() {
        given:
        var tillConfiguration = createTillConfiguration()

        when: 'Save Till is called'
        tillAssignmentService.saveTill(tillConfiguration)

        then: 'Check that the retrieved till matches the tillId, and Serial Number'
        def tillConfigs = tillAssignmentService.getTillsByTillIdAndSerialNumber(tillAssignmentService.springSecurityService.principal.tillId, tillAssignmentService.springSecurityService.principal.serialNumber)

        assert tillConfigs != null
        assert tillConfigs.first().tillId == tillAssignmentService.springSecurityService.principal.tillId
        assert tillConfigs.first().serialNumber == tillAssignmentService.springSecurityService.principal.serialNumber
    }

    def 'Test GetTillsByStoreId'() {
        given:
        var tillConfiguration = createTillConfiguration()

        when: 'Save Till is called'
        tillAssignmentService.saveTill(tillConfiguration)

        then: 'Check that the retrieved till matches the storeId'
        def tillConfigs = tillAssignmentService.getTillsByStoreId(tillAssignmentService.springSecurityService.principal.storeId)

        assert tillConfigs != null
        assert tillConfigs.first().storeId == tillAssignmentService.springSecurityService.principal.storeId
    }

    def 'Test GetTillsBySerialNumber'() {
        given:
        var tillConfiguration = createTillConfiguration()

        when: 'Save Till is called'
        tillAssignmentService.saveTill(tillConfiguration)

        then: 'Check that the retrieved till matches the Serial Number'
        def tillConfigs = tillAssignmentService.getTillsBySerialNumber(tillAssignmentService.springSecurityService.principal.serialNumber)

        assert tillConfigs != null
        assert tillConfigs.first().serialNumber == tillAssignmentService.springSecurityService.principal.serialNumber
    }

    def 'Test GetTillsBySerialNumber Wildcard'() {
        given:
        var tillConfiguration = createTillConfiguration()

        when: 'Save Till is called'
        tillAssignmentService.saveTill(tillConfiguration)

        then: 'Check that the retrieved till fetches a Serial Number with wildcard'
        def tillConfigs = tillAssignmentService.getTillsBySerialNumber("EST")

        assert tillConfigs != null
        assert tillConfigs.first().serialNumber == tillAssignmentService.springSecurityService.principal.serialNumber
    }

    def 'Test GetSingularTillBySerialNumber'() {
        given:
        var tillConfiguration = createTillConfiguration()

        when: 'Save Till is called'
        tillAssignmentService.saveTill(tillConfiguration)

        then: 'Check that the retrieved till matches the Serial Number'
        def tillConfigs = tillAssignmentService.getTillBySerialNumber(tillAssignmentService.springSecurityService.principal.serialNumber)

        assert tillConfigs != null
        assert tillConfigs.first().serialNumber == tillAssignmentService.springSecurityService.principal.serialNumber
    }

    def 'Test GetTillsByTillId'() {
        given:
        var tillConfiguration = createTillConfiguration()

        when: 'Save Till is called'
        tillAssignmentService.saveTill(tillConfiguration)

        then: 'Check that the retrieved till matches the TillId'
        def tillConfigs = tillAssignmentService.getTillsByTillId(tillAssignmentService.springSecurityService.principal.tillId)

        assert tillConfigs != null
        assert tillConfigs.first().tillId == tillAssignmentService.springSecurityService.principal.tillId
    }

    def 'Test UpdatingTillConfiguration'() {
        given:
        var tillConfiguration = createTillConfiguration()

        when: 'Save Till is called and a PIN is generated'
        tillAssignmentService.saveTill(tillConfiguration)
        var newPin = 12345678
        var newExpiryDate = DateTime.now().plusHours(1)
        tillAssignmentService.updateTillConfiguration(tillAssignmentService.springSecurityService.principal.serialNumber, newPin, newExpiryDate)

        then: 'Check that the till has been updated correctly'
        def tillConfigs = tillAssignmentService.getTills()

        assert tillConfigs != null
        assert tillConfigs.first().pin == newPin
        assert tillConfigs.first().pinExpiry == newExpiryDate
    }

    def 'Test UpdatingTillStock after till allocation'() {
        given:
        var tillConfiguration = createTillConfiguration()
        createTillStock()

        when: 'Till Stock is updated with a Store Id + Till Id for the specific serial'
        tillAssignmentService.saveTill(tillConfiguration)
        tillAssignmentService.updateTillStock(tillConfiguration)

        then: 'Check that the stock has been updated correctly'
        def tillStockEntry = TillStock.findBySerialNumber(tillAssignmentService.springSecurityService.principal.serialNumber)

        assert tillStockEntry != null
        assert tillStockEntry.storeId == tillAssignmentService.springSecurityService.principal.storeId
        assert tillStockEntry.tillId == tillAssignmentService.springSecurityService.principal.tillId
    }

    def 'Test UpdatingTillStock after till config deletion'() {
        given:
        var tillConfiguration = createTillConfiguration()
        createTillStock()

        when: 'Till Stock is updated removing the Store ID + Till Id for the specific serial'
        tillAssignmentService.saveTill(tillConfiguration)
        tillAssignmentService.updateTillStock(tillAssignmentService.springSecurityService.principal.serialNumber)

        then: 'Check that the stock has been updated correctly'
        def tillStockEntry = TillStock.findBySerialNumber(tillAssignmentService.springSecurityService.principal.serialNumber)

        assert tillStockEntry != null
        assert tillStockEntry.storeId == null
        assert tillStockEntry.tillId == null
    }

    private TillConfiguration createTillConfiguration() {
        var tillConfiguration = new TillConfiguration()
        tillConfiguration.id = 1
        tillConfiguration.retailerId = tillAssignmentService.springSecurityService.principal.retailerId
        tillConfiguration.storeId = tillAssignmentService.springSecurityService.principal.storeId
        tillConfiguration.tillId = tillAssignmentService.springSecurityService.principal.tillId
        tillConfiguration.description = ""
        tillConfiguration.serialNumber = tillAssignmentService.springSecurityService.principal.serialNumber
        tillConfiguration.scpTxnEndIndicator = ""
        tillConfiguration.pposControlBar = ""
        tillConfiguration.pposAdmin = false
        tillConfiguration.pposRefund = false
        tillConfiguration.pposSmartToken = false
        tillConfiguration.printCardReceipts = false
        tillConfiguration.baudRate = 0
        tillConfiguration.pin = 0
        tillConfiguration.pinExpiry = DateTime.now()
        tillConfiguration.dateTimeCreated = DateTime.now()
        tillConfiguration.dateTimeUpdated = DateTime.now()
        return tillConfiguration
    }

    private void createTillStock() {
        var tillStock = new TillStock()
        tillStock.id = 1
        tillStock.serialNumber = tillAssignmentService.springSecurityService.principal.serialNumber
        tillStock.tillId = null
        tillStock.storeId = null
        tillStock.model = "TESTMODEL"
        tillStock.retailerId = tillAssignmentService.springSecurityService.principal.retailerId
        tillStock.dateUpdated = DateTime.now()
        tillStock.save(flush: true)
    }
}
