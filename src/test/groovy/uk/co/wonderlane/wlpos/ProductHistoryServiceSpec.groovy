package uk.co.wonderlane.wlpos

import grails.plugin.springsecurity.SpringSecurityService
import grails.testing.gorm.DataTest
import grails.testing.services.ServiceUnitTest
import org.joda.time.DateTime
import spock.lang.Specification
import uk.co.wonderlane.wlpos.helpers.HibernateTestMockCriteria

class ProductHistoryServiceSpec extends Specification implements ServiceUnitTest<ProductHistoryService>, DataTest {

    Class<?>[] getDomainClassesToMock() {
        [ProductHistory] as Class<?>[]
    }

    //-------------------------------getProductHistory function Unit tests----------------------------//

    void "should retrieve product history"() {
        given:
        service.springSecurityService = getFakeSpringSecurityService()

        HibernateTestMockCriteria mockCriteria = new HibernateTestMockCriteria()
        mockCriteria.getResponses().add(new ProductHistory())
        def defaultCriteria = ProductHistory.createCriteria() // keep the default behavior
        ProductHistory.metaClass.static.createCriteria = { return mockCriteria }

        when: 'getProductHistory action is executed'
        def serviceResponse = service.getProductHistory(0, DateTime.now())
        ProductHistory.metaClass.static.createCriteria = { return defaultCriteria } // set the default value back to the class

        then: 'getProductHistory action response is correct'
        serviceResponse != null
        serviceResponse instanceof List
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
