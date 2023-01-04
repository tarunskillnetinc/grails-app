package uk.co.wonderlane.wlpos

import grails.testing.gorm.DataTest
import grails.testing.services.ServiceUnitTest
import org.springframework.security.core.userdetails.UserDetails
import spock.lang.Specification
import uk.co.wonderlane.wlpos.enums.Role

class UserDetailsServiceSpec extends Specification implements ServiceUnitTest<WonderLaneUserDetailsService>, DataTest {

    Class<?>[] getDomainClassesToMock() {
        return [User] as Class[]
    }

    //----------------------------- Calling Load user by username Action ---------------------------------------------//

    def 'Should retrieve the user by username if user exists'() {
        given:
        User user = new User(
                username: "testUser", password: "password", defaultStoreId: 234,
                name: "Test", active: true, role: role, dateOfBirth: new Date()
        )

        user.save(flush: true, failOnError: true)

        when: 'loadUserByUsername action is executed'
        UserDetails userDetails = service.loadUserByUsername("testUser", true)

        then: 'loadUserByUsername results are correct'
        userDetails != null
        userDetails.username == "testUser"

        where: 'Pass following input parameters'
        ID | role
        1  | Role.ENGINEER
        2  | Role.HEAD_OFFICE
        3  | Role.STORE_MANAGER
        4  | Role.SUPERVISOR
        5  | Role.USER
    }

    def 'Should throw exception when retrieve the user by username if user does not exist'() {
        given:

        when: 'loadUserByUsername action is executed'
        service.loadUserByUsername("unknownUser", true)

        then: 'loadUserByUsername results are correct'
        Exception e = thrown()
        e != null

        where: 'Pass following input parameters'
        ID | role
        1  | Role.ENGINEER
        2  | Role.HEAD_OFFICE
        3  | Role.STORE_MANAGER
        4  | Role.SUPERVISOR
        5  | Role.USER
    }


}
