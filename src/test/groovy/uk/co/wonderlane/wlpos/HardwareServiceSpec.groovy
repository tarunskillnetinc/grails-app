package uk.co.wonderlane.wlpos

import grails.plugin.springsecurity.SpringSecurityService
import grails.testing.gorm.DataTest
import grails.testing.services.ServiceUnitTest
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import spock.lang.Specification
import uk.co.wonderlane.wlpos.dataaccess.DatabaseCredentials

class HardwareServiceSpec extends Specification implements ServiceUnitTest<HardwareService>, DataTest {
    DatabaseCredentials databaseCredentials
    HardwareService hardwareService;

    def setup() {
        databaseCredentials = Mock(DatabaseCredentials)
        hardwareService = new HardwareService(databaseCredentials);

        hardwareService.springSecurityService = Stub(SpringSecurityService) {
            getPrincipal() >>new HashMap(){{
                put("id", 9);
                put("retailerId", 9);
                put("storeId", 234);
               }}
        }
    }

    def cleanup() {
    }

    Class<?>[] getDomainClassesToMock(){
        return [TillStock] as Class[]
    }

    def 'Test the save hardware action'() {
        given:

        var serialNumber = "Test serial"
        var model = "Test model"
        var dateUpdated = DateTime.now(DateTimeZone.UTC)

        var tillStock = new TillStock()
        tillStock.serialNumber = serialNumber
        tillStock.model = model
        tillStock.setDateUpdated(dateUpdated)

        when: 'The save hardware action is executed'
        hardwareService.saveHardware(tillStock)

        then: 'The hardware saved successfully'

        def savedTillStock = TillStock.withCriteria {
            eq("serialNumber", serialNumber)
        } ?: null

        assert savedTillStock != null
        assert savedTillStock[0].serialNumber == serialNumber
        assert savedTillStock[0].model == model
        assert savedTillStock[0].dateUpdated == dateUpdated
        assert savedTillStock[0].retailerId == 9
    }

    def 'Test save hardware does not allow the same serial number twice'() {
        given:

        var serialNumber = "Test serial"
        var model = "Test model"
        var dateUpdated = DateTime.now(DateTimeZone.UTC)

        var tillStock = new TillStock()
        tillStock.serialNumber = serialNumber
        tillStock.model = model
        tillStock.setDateUpdated(dateUpdated)

        var secondTillStock = new TillStock()
        secondTillStock.serialNumber = serialNumber
        secondTillStock.model = "2nd model"
        secondTillStock.setDateUpdated(dateUpdated)

        when: 'The save hardware action is executed'
        hardwareService.saveHardware(tillStock)
        hardwareService.saveHardware(secondTillStock)

        then: 'The hardware saved successfully'

        def savedTillStock = TillStock.withCriteria {
            eq("serialNumber", serialNumber)
        } ?: null

                assert savedTillStock != null
        assert savedTillStock[0].serialNumber == serialNumber
        assert savedTillStock[0].model == model
        assert savedTillStock[0].dateUpdated == dateUpdated
        assert savedTillStock[0].retailerId == 9
        assert savedTillStock.size() == 1
    }
}
