package uk.co.wonderlane.wlpos

import grails.plugin.springsecurity.SpringSecurityService
import grails.testing.gorm.DataTest
import grails.testing.services.ServiceUnitTest
import spock.lang.Specification
import uk.co.wonderlane.wlpos.enums.PrintReceiptOption

class RetailerServiceSpec extends Specification implements ServiceUnitTest<RetailerService>, DataTest{

    Class<?>[] getDomainClassesToMock() {
        return [Retailer] as Class[]
    }

    //-------------------------------------------- Calling get retailer Action --------------------------------------//

    def 'Should retrieve retailer'() {
        given:

        Retailer testRetailer = new Retailer(snappyShopperEnabled: false, twoStageSel: false)
        testRetailer.setId(9)
        testRetailer.save(flush: true, failOnError: true)

        when: 'getRetailer action is executed'
        def response = service.getRetailer(9)

        then: 'getRetailer results are correct'
        noExceptionThrown()
        response != null
        response.id == 9
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
