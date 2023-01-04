package uk.co.wonderlane.wlpos

import grails.plugin.springsecurity.SpringSecurityService
import grails.testing.gorm.DataTest
import grails.testing.web.controllers.ControllerUnitTest
import org.springframework.http.HttpStatus
import spock.lang.Specification
import uk.co.wonderlane.wlpos.enums.Role

class UserControllerSpec extends Specification implements ControllerUnitTest<UserController>, DataTest {

    Class<?>[] getDomainClassesToMock() {
        return [User] as Class[]
    }

    //--------------------------------------------- Calling index Action ---------------------------------------------//

    def 'Should retrieve index page without error'() {
        given:

        controller.userService = Stub(UserService) {
            getUsers(_, _, _) >> new ArrayList()
        }

        controller.springSecurityService = getFakeSpringSecurityService()

        when: 'index action is executed'
        controller.index()

        then: 'index results are correct'
        noExceptionThrown()
    }

    //-------------------------------------- Calling ajaxGetUsers Action ---------------------------------------------//

    def 'Should retrieve users by ajax without error'() {
        given:

        controller.userService = Stub(UserService) {
            getUsers(_, _, _) >> new ArrayList()
        }

        controller.springSecurityService = getFakeSpringSecurityService()

        when: 'ajaxGetUsers action is executed'
        controller.ajaxGetUsers("testUser", 0, 100)

        then: 'ajaxGetUsers results are correct'
        noExceptionThrown()
        model.users != null
    }

    //---------------------------------------------- Calling add Action ---------------------------------------------//

    def 'Should run add method without error'() {
        given:


        when: 'add action is executed'
        controller.add()

        then: 'add results are correct'
        noExceptionThrown()
    }

    //---------------------------------------------- Calling userEdit Action -----------------------------------------//

    def 'Should edit user without error'() {
        given:
        params.id = id

        User savedUser = new User(
                username: "testUser1", password: "password", defaultStoreId: 234,
                name: "Test", active: true, role: savedUserRole, dateOfBirth: new Date()
        )
        savedUser.setId(100)

        savedUser.save(flush: true, failOnError: true)

        User callerUser = new User(
                username: "testUser2", password: "password", defaultStoreId: 234,
                name: "Test", active: true, role: callerUserRole, dateOfBirth: new Date()
        )

        callerUser.setId(callerUserId)

        controller.userService = Stub(UserService) {
            getUser(_) >> callerUser
        }

        Map principal = new HashMap() {
            {
                put("storeId", storeId)
                put("storeNumber", 100)
                put("retailerId", 9)
                put("id", principalId)
            }
        }

        controller.springSecurityService = Stub(SpringSecurityService) {
            getPrincipal() >> principal
        }

        when: 'userEdit action is executed'
        controller.userEdit()

        then: 'userEdit results are correct'
        noExceptionThrown()

        where:
        id   | storeId | principalId | callerUserId | savedUserRole      | callerUserRole
        null | 100     | 100         | 100          | Role.USER          | Role.SUPERVISOR
        "A"  | 100     | 100         | 200          | Role.SUPERVISOR    | Role.USER
        "12" | 100     | 100         | 100          | Role.STORE_MANAGER | Role.ENGINEER
        "12" | 100     | 100         | 200          | Role.HEAD_OFFICE   | Role.SUPERVISOR
        "12" | 100     | 100         | 100          | Role.ENGINEER      | Role.ENGINEER
        "12" | 100     | 200         | 200          | Role.ENGINEER      | Role.STORE_MANAGER
        "12" | 100     | 200         | 200          | Role.ENGINEER      | null
    }

    def 'Should flash error if user not found when edit user'() {
        given:

        controller.userService = Stub(UserService) {
            getUser(_) >> null
        }

        controller.springSecurityService = getFakeSpringSecurityService()

        when: 'userEdit action is executed'
        controller.userEdit()

        then: 'userEdit results are correct'
        noExceptionThrown()
        flash.error == "User not found."
    }

    //--------------------------------------- Calling changePassword Action -----------------------------------------//

    def 'Should change password without error'() {
        given:
        params.id = id
        params.name = "test"

        User savedUser = new User(
                username: "testUser1", password: "password", defaultStoreId: 234,
                name: "Test", active: true, role: savedUserRole, dateOfBirth: new Date()
        )
        savedUser.setId(100)

        savedUser.save(flush: true, failOnError: true)

        User callerUser = new User(
                username: "testUser2", password: "password", defaultStoreId: 234,
                name: "Test", active: true, role: callerUserRole, dateOfBirth: new Date()
        )

        callerUser.setId(callerUserId)

        controller.userService = Stub(UserService) {
            getUser(_) >> callerUser
        }

        Map principal = new HashMap() {
            {
                put("storeId", storeId)
                put("storeNumber", 100)
                put("retailerId", 9)
                put("id", principalId)
            }
        }

        controller.springSecurityService = Stub(SpringSecurityService) {
            getPrincipal() >> principal
        }

        when: 'changePassword action is executed'
        controller.changePassword()

        then: 'changePassword results are correct'
        noExceptionThrown()
        view == "/user/changePassword"
        model.userId != null
        model.name != null

        where:
        id   | storeId | principalId | callerUserId | savedUserRole      | callerUserRole
        null | 100     | 100         | 100          | Role.USER          | Role.SUPERVISOR
        "A"  | 100     | 100         | 200          | Role.SUPERVISOR    | Role.USER
        "12" | 100     | 100         | 100          | Role.STORE_MANAGER | Role.ENGINEER
        "12" | 100     | 100         | 200          | Role.HEAD_OFFICE   | Role.SUPERVISOR
        "12" | 100     | 100         | 100          | Role.ENGINEER      | Role.ENGINEER
        "12" | 100     | 200         | 200          | Role.ENGINEER      | Role.STORE_MANAGER
        "12" | 100     | 200         | 200          | Role.ENGINEER      | null
    }

    def 'Should flash error if user not found when changing password'() {
        given:

        controller.userService = Stub(UserService) {
            getUser(_) >> null
        }

        controller.springSecurityService = getFakeSpringSecurityService()

        when: 'changePassword action is executed'
        controller.changePassword()

        then: 'changePassword results are correct'
        noExceptionThrown()
        flash.error == "User not found."
    }

    //-------------------------------------------- Calling save Action -----------------------------------------------//

    def 'Should show add user view if invalid command on save user'() {
        given:

        SaveUserCommand command = new SaveUserCommand()

        controller.userService = Stub(UserService) {
            getUser(_) >> null
        }

        controller.springSecurityService = getFakeSpringSecurityService()

        when: 'save action is executed'
        controller.save(command)

        then: 'save results are correct'
        noExceptionThrown()
        view == "/user/add"
    }

    def 'Should save user and redirect to index page'() {
        given:

        User savedUser = new User(
                username: "testUser1", password: "password", defaultStoreId: 234,
                name: "Test", active: true, role: Role.ENGINEER, dateOfBirth: new Date()
        )
        savedUser.setId(100)

        savedUser.save(flush: true, failOnError: true)

        SaveUserCommand command = Stub(SaveUserCommand) {
            validate() >> true
            getId() >> userId
        }

        controller.userService = Stub(UserService) {}

        controller.springSecurityService = getFakeSpringSecurityService()

        controller.rabbitService = Stub(BackOfficeRabbitService) {}

        when: 'save action is executed'
        controller.save(command)

        then: 'save results are correct'
        noExceptionThrown()
        status == HttpStatus.FOUND.value()
        flash.message == "User saved successfully"

        where:
        ID | userId
        1  | 100
        2  | null
    }

    //--------------------------------------- Calling edit selected user Action --------------------------------------//

    def 'Should redirect to index page if save command is invalid when edit selected user'() {
        given:

        when: 'editSelectedUser action is executed'
        controller.editSelectedUser(command)

        then: 'editSelectedUser results are correct'
        noExceptionThrown()
        response.redirectUrl == "/user/index"

        where:
        ID | command
        1  | null
        2  | new SaveUserCommand()
    }

    def 'Should flash error and show edit page when selected user is invalid'() {
        given:

        User savedUser = new User(
                username: "testUser1", password: "password", defaultStoreId: 234,
                name: "Test", active: true, role: Role.HEAD_OFFICE, dateOfBirth: new Date()
        )
        savedUser.setId(100)
        savedUser.save(flush: true, failOnError: true)

        User loggedInUser = new User(
                username: "testUser2", password: "password", defaultStoreId: 234,
                name: "Test", active: true, role: loggedInRole, dateOfBirth: new Date()
        )
        loggedInUser.setId(200)
        loggedInUser.save(flush: true, failOnError: true)

        SaveUserCommand command = new SaveUserCommand()
        command.setId(userId)
        command.setRole(Role.HEAD_OFFICE)

        controller.springSecurityService = Stub(SpringSecurityService) {
            getPrincipal() >> new HashMap() {
                {
                    put("storeId", 100)
                    put("storeNumber", 100)
                    put("retailerId", 9)
                    put("id", 200)
                }
            }
        }

        when: 'editSelectedUser action is executed'
        controller.editSelectedUser(command)

        then: 'editSelectedUser results are correct'
        noExceptionThrown()
        view == "/user/userEdit"
        flash.error != null

        where:
        ID | userId | loggedInRole
        1  | 100    | Role.ENGINEER
        2  | 300    | Role.USER
        3  | 100    | Role.USER
        4  | 100    | Role.ENGINEER
    }

    def 'Should show user edit view if save user command is not valid when edit user'() {
        given:

        User savedUser = new User(
                username: "testUser1", password: "password", defaultStoreId: 234,
                name: "Test", active: true, role: Role.HEAD_OFFICE, dateOfBirth: new Date()
        )
        savedUser.setId(100)
        savedUser.save(flush: true, failOnError: true)

        SaveUserCommand command = new SaveUserCommand()
        command.setId(100)
        command.setRole(Role.HEAD_OFFICE)

        controller.userService = Stub(UserService) {}

        controller.springSecurityService = Stub(SpringSecurityService) {
            getPrincipal() >> new HashMap() {
                {
                    put("storeId", null)
                    put("storeNumber", 100)
                    put("retailerId", 9)
                    put("id", 100)
                }
            }
        }

        when: 'editSelectedUser action is executed'
        controller.editSelectedUser(command)

        then: 'editSelectedUser results are correct'
        noExceptionThrown()
        view == "/user/userEdit"
    }

    def 'Should flash message and redirect to index page when selected user valid'() {
        given:

        User savedUser = new User(
                username: "testUser1", password: "password", defaultStoreId: 234,
                name: "Test", active: true, role: Role.HEAD_OFFICE, dateOfBirth: new Date()
        )
        savedUser.setId(100)
        savedUser.save(flush: true, failOnError: true)

        SaveUserCommand command = Stub(SaveUserCommand) {
            validate() >> true
            getId() >> 100
            getRole() >> Role.HEAD_OFFICE
        }

        controller.userService = Stub(UserService) {}
        controller.rabbitService = Stub(BackOfficeRabbitService) {}

        controller.springSecurityService = Stub(SpringSecurityService) {
            getPrincipal() >> new HashMap() {
                {
                    put("storeId", null)
                    put("storeNumber", 100)
                    put("retailerId", 9)
                    put("id", 100)
                }
            }
        }

        when: 'editSelectedUser action is executed'
        controller.editSelectedUser(command)

        then: 'editSelectedUser results are correct'
        noExceptionThrown()
        response.redirectUrl == "/user/index"
        flash.message == "User saved successfully"
    }

    //------------------------------------------- Calling delete user Action -----------------------------------------//

    def 'Should flash error and show edit page when selected user is invalid on delete'() {
        given:

        User savedUser = new User(
                username: "testUser1", password: "password", defaultStoreId: 234,
                name: "Test", active: true, role: Role.HEAD_OFFICE, dateOfBirth: new Date()
        )
        savedUser.setId(100)
        savedUser.save(flush: true, failOnError: true)

        User loggedInUser = new User(
                username: "testUser2", password: "password", defaultStoreId: 234,
                name: "Test", active: true, role: loggedInRole, dateOfBirth: new Date()
        )
        loggedInUser.setId(200)
        loggedInUser.save(flush: true, failOnError: true)

        params.id = userId

        controller.springSecurityService = Stub(SpringSecurityService) {
            getPrincipal() >> new HashMap() {
                {
                    put("storeId", 100)
                    put("storeNumber", 100)
                    put("retailerId", 9)
                    put("id", 200)
                }
            }
        }

        when: 'deleteUser action is executed'
        controller.deleteUser()

        then: 'deleteUser results are correct'
        noExceptionThrown()
        view == "/user/userEdit"
        flash.error != null

        where:
        ID | userId | loggedInRole
        1  | 100    | Role.ENGINEER
        2  | 300    | Role.USER
        3  | 100    | Role.USER
        4  | 100    | Role.ENGINEER
    }

    def 'Should flash message and redirect to index page on successful user delete'() {
        given:

        User savedUser = new User(
                username: "testUser1", password: "password", defaultStoreId: 234,
                name: "Test", active: true, role: Role.HEAD_OFFICE, dateOfBirth: new Date()
        )
        savedUser.setId(100)
        savedUser.save(flush: true, failOnError: true)

        controller.userService = Stub(UserService) {}
        controller.rabbitService = Stub(BackOfficeRabbitService) {}

        controller.springSecurityService = Stub(SpringSecurityService) {
            getPrincipal() >> new HashMap() {
                {
                    put("storeId", null)
                    put("storeNumber", 100)
                    put("retailerId", 9)
                    put("id", 100)
                }
            }
        }

        params.id = 100

        when: 'deleteUser action is executed'
        controller.deleteUser()

        then: 'deleteUser results are correct'
        noExceptionThrown()
        flash.message == "User deleted successfully"
        response.redirectUrl == "/user/index"
    }

    //--------------------------------------- Calling edit user password Action --------------------------------------//

    def 'Should redirect to index page if save command is invalid when edit user password'() {
        given:

        when: 'editUserPassword action is executed'
        controller.editUserPassword(editCommand)

        then: 'editUserPassword results are correct'
        noExceptionThrown()
        response.redirectUrl == "/user/index"

        where:
        ID | editCommand
        1  | null
        2  | new SaveUserPasswordCommand()
    }

    def 'Should flash error and show edit page when selected user is invalid on edit password'() {
        given:

        User savedUser = new User(
                username: "testUser1", password: "password", defaultStoreId: 234,
                name: "Test", active: true, role: Role.HEAD_OFFICE, dateOfBirth: new Date()
        )
        savedUser.setId(100)
        savedUser.save(flush: true, failOnError: true)

        User loggedInUser = new User(
                username: "testUser2", password: "password", defaultStoreId: 234,
                name: "Test", active: true, role: loggedInRole, dateOfBirth: new Date()
        )
        loggedInUser.setId(200)
        loggedInUser.save(flush: true, failOnError: true)

        SaveUserPasswordCommand command = new SaveUserPasswordCommand()
        command.setId(userId)

        controller.springSecurityService = Stub(SpringSecurityService) {
            getPrincipal() >> new HashMap() {
                {
                    put("storeId", 100)
                    put("storeNumber", 100)
                    put("retailerId", 9)
                    put("id", 200)
                }
            }
        }

        when: 'editUserPassword action is executed'
        controller.editUserPassword(command)

        then: 'editUserPassword results are correct'
        noExceptionThrown()
        view == "/user/changePassword"
        flash.error != null

        where:
        ID | userId | loggedInRole
        1  | 100    | Role.ENGINEER
        2  | 300    | Role.USER
        3  | 100    | Role.USER
        4  | 100    | Role.ENGINEER
    }

    def 'Should show user edit view if save user command is not valid when edit password'() {
        given:

        User savedUser = new User(
                username: "testUser1", password: "password", defaultStoreId: 234,
                name: "Test", active: true, role: Role.HEAD_OFFICE, dateOfBirth: new Date()
        )
        savedUser.setId(100)
        savedUser.save(flush: true, failOnError: true)

        SaveUserPasswordCommand command = new SaveUserPasswordCommand()
        command.setId(100)

        controller.userService = Stub(UserService) {}

        controller.springSecurityService = Stub(SpringSecurityService) {
            getPrincipal() >> new HashMap() {
                {
                    put("storeId", null)
                    put("storeNumber", 100)
                    put("retailerId", 9)
                    put("id", 100)
                }
            }
        }

        when: 'editUserPassword action is executed'
        controller.editUserPassword(command)

        then: 'editUserPassword results are correct'
        noExceptionThrown()
        view == "/user/changePassword"
    }

    def 'Should flash message and redirect to index page when selected user valid on edit password'() {
        given:

        User savedUser = new User(
                username: "testUser1", password: "password", defaultStoreId: 234,
                name: "Test", active: true, role: Role.HEAD_OFFICE, dateOfBirth: new Date()
        )
        savedUser.setId(100)
        savedUser.save(flush: true, failOnError: true)

        SaveUserPasswordCommand command = Stub(SaveUserPasswordCommand) {
            validate() >> true
            getId() >> 100
        }

        controller.userService = Stub(UserService) {}
        controller.rabbitService = Stub(BackOfficeRabbitService) {}

        controller.springSecurityService = Stub(SpringSecurityService) {
            getPrincipal() >> new HashMap() {
                {
                    put("storeId", null)
                    put("storeNumber", 100)
                    put("retailerId", 9)
                    put("id", 100)
                }
            }
        }

        when: 'editUserPassword action is executed'
        controller.editUserPassword(command)

        then: 'editUserPassword results are correct'
        noExceptionThrown()
        response.redirectUrl == "/user/index"
        flash.message == "User password saved successfully"
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
