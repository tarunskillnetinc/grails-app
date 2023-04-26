package uk.co.wonderlane.wlpos

import grails.plugin.springsecurity.SpringSecurityService
import grails.testing.gorm.DataTest
import grails.testing.services.ServiceUnitTest
import spock.lang.Specification
import uk.co.wonderlane.wlpos.enums.Role

class UserServiceSpec extends Specification implements ServiceUnitTest<UserService>, DataTest {

    Class<?>[] getDomainClassesToMock() {
        return [User] as Class[]
    }

    //----------------------------------- Calling get user by id Action ---------------------------------------------//

    def 'Should retrieve the user by id if user exists'() {
        given:
        User user = new User(
                username: "testUser", password: "password", defaultStoreId: 234, retailerId: 9,
                name: "Test", active: true, role: Role.USER, dateOfBirth: new Date()
        )
        user.setId(100)

        user.save(flush: true, failOnError: true)

        service.springSecurityService = getFakeSpringSecurityService()

        when: 'getUser action is executed'
        User userResponse = service.getUser(100)

        then: 'getUser results are correct'
        userResponse != null
        userResponse.username == "testUser"

    }

    //---------------------------------------- Calling get users Action ---------------------------------------------//

    def 'Should retrieve users for the search terms'() {
        given:

        User user = new User(
                username: "testUser", password: "password", defaultStoreId: 234, retailerId: 9,
                name: "Test", active: true, role: Role.USER, dateOfBirth: new Date()
        )

        user.save(flush: true, failOnError: true)

        service.springSecurityService = getFakeSpringSecurityService()

        when: 'loadUserByUsername action is executed'
        def users = service.getUsers(searchTerm, 0, 100)

        then: 'loadUserByUsername results are correct'
        users != null
        users.size() > 0

        where: 'Pass following input parameters'
        ID | searchTerm
        1  | 'testUser'
        2  | 'test'
        3  | 'User'
        4  | 't'
        5  | 'U'
    }

    //---------------------------------------- Calling save user Action ---------------------------------------------//

    def 'Should save user'() {
        given:
        User user = new User(
                username: "testUser", password: "password", defaultStoreId: 234, retailerId: 9,
                name: "Test", active: true, role: Role.USER, dateOfBirth: new Date()
        )
        user.setId(100)

        user.save(flush: true, failOnError: true)

        service.springSecurityService = getFakeSpringSecurityService()

        when: 'saveUser action is executed'
        User userResponse = service.saveUser(user)

        then: 'saveUser results are correct'
        userResponse != null
        userResponse.username == "testUser"
        !userResponse.hasErrors()
        User.findByUsername("testUser") != null
    }

    //---------------------------------------- Calling delete user Action --------------------------------------------//

    def 'Should delete user'() {
        given:
        User user = new User(
                username: "testUser", password: "password", defaultStoreId: 234, retailerId: 9,
                name: "Test", active: true, role: Role.USER, dateOfBirth: new Date()
        )
        user.setId(100)

        service.springSecurityService = getFakeSpringSecurityService()

        when: 'deleteUser action is executed'
        User userResponse = service.deleteUser(user)

        then: 'deleteUser results are correct'
        userResponse == null
        User.findByUsername("testUser") == null
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
