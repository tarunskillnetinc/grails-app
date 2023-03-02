package uk.co.wonderlane.wlpos


import grails.testing.gorm.DataTest
import grails.testing.services.ServiceUnitTest
import spock.lang.Specification

class RestrictionsServiceSpec extends Specification implements ServiceUnitTest<RestrictionsService>, DataTest {

    Class<?>[] getDomainClassesToMock() {
        [Restrictions] as Class<?>[]
    }

    //-------------------------------saveRestrictions function Unit tests----------------------------//

    void "should save restrictions"() {
        given:
        Restrictions restrictions = new Restrictions()
        restrictions.setId(100)

        when: 'saveRestrictions action is executed'
        def serviceResponse = service.saveRestrictions(restrictions)

        then: 'saveRestrictions action response is correct'
        serviceResponse != null
        serviceResponse instanceof Restrictions
        Restrictions.findById(100) != null
    }

}
