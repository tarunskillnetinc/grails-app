package uk.co.wonderlane.wlpos

import grails.plugin.springsecurity.SpringSecurityService
import grails.testing.gorm.DataTest
import grails.testing.services.ServiceUnitTest
import spock.lang.Specification
import uk.co.wonderlane.wlpos.enums.PrintReceiptOption

class StoreNumberValidatorServiceSpec extends Specification implements ServiceUnitTest<StoreNumberValidatorService>, DataTest {

    Class<?>[] getDomainClassesToMock() {
        return [Store] as Class[]
    }

    //--------------------------------------------- Calling get store Action -----------------------------------------//

    def 'Should retrieve store settings'() {
        given:

        Store testStoreSettings = new Store(retailerId: 9,
                storeId: 100,
                printReceiptOption: PrintReceiptOption.NO_PRINT,
                type: "test",
                priceBand: new PriceBand(retailerId: 9, description: "Test"),
                range: new Range(retailerId: 9, description: "Test"),
                countIncrement: BigDecimal.ONE)

        testStoreSettings.springSecurityService = getFakeSpringSecurityService()

        testStoreSettings.save(flush: true, failOnError: true)

        when: 'getStore action is executed'
        def response = service.getStore(9, 100)

        then: 'getStore results are correct'
        noExceptionThrown()
        response != null
        response.storeId == 100
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
