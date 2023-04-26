package uk.co.wonderlane.wlpos

import grails.plugin.springsecurity.SpringSecurityService
import grails.testing.gorm.DataTest
import grails.testing.web.controllers.ControllerUnitTest
import org.springframework.http.HttpStatus
import spock.lang.Specification
import uk.co.wonderlane.wlpos.enums.PrintReceiptOption
import uk.co.wonderlane.wlpos.enums.Role

class GroupControllerSpec extends Specification implements ControllerUnitTest<GroupController>, DataTest {

    Class<?>[] getDomainClassesToMock() {
        [GroupLevel, Group] as Class<?>[]
    }

    def setup() {
    }

    def cleanup() {
    }

    //-------------------------------index function Unit tests----------------------------//

    void "should retrieve groups on index"() {
        given:
        GroupLevel groupLevel = new GroupLevel(retailerId: 9, level: 1, name: "Test Level")
        groupLevel.setId(150)
        groupLevel.save(flush: true, failOnError: true)

        controller.springSecurityService = getFakeSpringSecurityService()

        controller.groupService = Stub(GroupService) {
            getGroupsByLevel(_) >> new ArrayList()
        }

        when: 'index action is executed'
        def controllerResponse = controller.index()

        then: 'index action response is correct'
        response.status == HttpStatus.OK.value()
        controllerResponse.groups != null
    }

    //-------------------------------get child groups function Unit tests----------------------------//

    void "should retrieve child groups by parent id"() {
        given:

        GroupLevel groupLevel1 = new GroupLevel(retailerId: 9, level: 1, name: "Test Level 1")
        groupLevel1.setId(150)
        groupLevel1.save(flush: true, failOnError: true)


        GroupLevel groupLevel2 = new GroupLevel(retailerId: 9, level: 2, name: "Test Level 2")
        groupLevel2.setId(250)
        groupLevel2.save(flush: true, failOnError: true)

        StoreSettings testStoreSettings1 = new StoreSettings(retailerId: 9,
                storeId: 100,
                printReceiptOption: PrintReceiptOption.NO_PRINT,
                type: "test",
                priceBand: new PriceBand(retailerId: 9, description: "Test"),
                range: new Range(retailerId: 9, description: "Test"),
                countIncrement: BigDecimal.ONE)

        testStoreSettings1.springSecurityService = getFakeSpringSecurityService()

        Group testGroup1 = new Group(retailerId: 9, name: "Test Group", level: groupLevel1)
        testGroup1.setId(100)
        testGroup1.addToStores(testStoreSettings1)
        testGroup1.save(flush: true, failOnError: true)

        StoreSettings testStoreSettings2 = new StoreSettings(retailerId: 9,
                storeId: 200,
                printReceiptOption: PrintReceiptOption.NO_PRINT,
                type: "test",
                priceBand: new PriceBand(retailerId: 9, description: "Test"),
                range: new Range(retailerId: 9, description: "Test"),
                countIncrement: BigDecimal.ONE)

        testStoreSettings2.springSecurityService = getFakeSpringSecurityService()

        Group testGroup2 = new Group(retailerId: 9, name: "Test Group", level: groupLevel2, parentGroup: testGroup1)
        testGroup2.setId(200)
        testGroup2.addToStores(testStoreSettings2)
        testGroup2.save(flush: true, failOnError: true)

        controller.springSecurityService = getFakeSpringSecurityService()

        controller.groupService = Stub(GroupService) {
            getGroupsByLevel(_) >> new ArrayList()
        }

        def mockView = '<div>DUMMY HTML</div>'
        views['/group/_groupResults.gsp'] = mockView
        views['/group/_storeResults.gsp'] = mockView

        when: 'ajaxGetChildGroups action is executed'
        controller.ajaxGetChildGroups(parentId)

        then: 'ajaxGetChildGroups action response is correct'
        response.status == HttpStatus.OK.value()
        model.parentId != null

        where:
        ID | parentId
        1  | 200
        2  | -1
        3  | 100
    }

    //-------------------------------get available stores function Unit tests----------------------------//

    void "should retrieve available stores by group id"() {
        given:

        GroupLevel groupLevel = new GroupLevel(retailerId: 9, level: 1, name: "Test Level 1")
        groupLevel.setId(150)
        groupLevel.save(flush: true, failOnError: true)

        StoreSettings testStoreSettings = new StoreSettings(retailerId: 9,
                storeId: 100,
                printReceiptOption: PrintReceiptOption.NO_PRINT,
                type: "test",
                priceBand: new PriceBand(retailerId: 9, description: "Test"),
                range: new Range(retailerId: 9, description: "Test"),
                countIncrement: BigDecimal.ONE)

        testStoreSettings.springSecurityService = getFakeSpringSecurityService()
        testStoreSettings.save(flush: true, failOnError: true)

        Group testGroup = new Group(retailerId: 9, name: "Test Group", level: groupLevel)
        testGroup.setId(100)
        testGroup.addToStores(testStoreSettings)
        testGroup.save(flush: true, failOnError: true)

        controller.springSecurityService = getFakeSpringSecurityService()

        when: 'ajaxGetAvailableStores action is executed'
        controller.ajaxGetAvailableStores(100)

        then: 'ajaxGetAvailableStores action response is correct'
        response.status == HttpStatus.OK.value()
        model.groupId != null
        model.stores != null
    }

    //-------------------------------add store to group function Unit tests----------------------------//

    void "should add store to an available group"() {
        given:

        GroupLevel groupLevel = new GroupLevel(retailerId: 9, level: 1, name: "Test Level 1")
        groupLevel.setId(150)
        groupLevel.save(flush: true, failOnError: true)

        StoreSettings testStoreSettings = new StoreSettings(retailerId: 9,
                storeId: 120,
                printReceiptOption: PrintReceiptOption.NO_PRINT,
                type: "test",
                priceBand: new PriceBand(retailerId: 9, description: "Test"),
                range: new Range(retailerId: 9, description: "Test"),
                countIncrement: BigDecimal.ONE)

        testStoreSettings.springSecurityService = getFakeSpringSecurityService()
        testStoreSettings.setId(120)
        testStoreSettings.save(flush: true, failOnError: true)

        Group testGroup = new Group(retailerId: 9, name: "Test Group", level: groupLevel)
        testGroup.setId(100)
        testGroup.save(flush: true, failOnError: true)

        controller.springSecurityService = getFakeSpringSecurityService()
        controller.groupService = Stub(GroupService) {}

        when: 'ajaxAddStoreToGroup action is executed'
        controller.ajaxAddStoreToGroup(120, 100)

        then: 'ajaxAddStoreToGroup action response is correct'
        response.status == HttpStatus.NO_CONTENT.value()
    }

    //-------------------------------remove store from group function Unit tests----------------------------//

    void "should remove store from an available group"() {
        given:

        GroupLevel groupLevel = new GroupLevel(retailerId: 9, level: 1, name: "Test Level 1")
        groupLevel.setId(150)
        groupLevel.save(flush: true, failOnError: true)

        StoreSettings testStoreSettings = new StoreSettings(retailerId: 9,
                storeId: 120,
                printReceiptOption: PrintReceiptOption.NO_PRINT,
                type: "test",
                priceBand: new PriceBand(retailerId: 9, description: "Test"),
                range: new Range(retailerId: 9, description: "Test"),
                countIncrement: BigDecimal.ONE)

        testStoreSettings.springSecurityService = getFakeSpringSecurityService()
        testStoreSettings.setId(120)
        testStoreSettings.save(flush: true, failOnError: true)

        Group testGroup = new Group(retailerId: 9, name: "Test Group", level: groupLevel)
        testGroup.setId(100)
        testGroup.addToStores(testStoreSettings)
        testGroup.save(flush: true, failOnError: true)

        controller.springSecurityService = getFakeSpringSecurityService()
        controller.groupService = Stub(GroupService) {}

        when: 'ajaxRemoveStoreFromGroup action is executed'
        controller.ajaxRemoveStoreFromGroup(120, 100)

        then: 'ajaxRemoveStoreFromGroup action response is correct'
        response.status == HttpStatus.NO_CONTENT.value()
    }

    //-----------------------------------select function Unit tests--------------------------------//

    void "should show index page when select groups as a store"() {
        given:

        GroupLevel groupLevel = new GroupLevel(retailerId: 9, level: 1, name: "Test Level 1")
        groupLevel.setId(150)
        groupLevel.save(flush: true, failOnError: true)

        Group testGroup = new Group(retailerId: 9, name: "Test Group", level: groupLevel)
        testGroup.setId(100)
        testGroup.save(flush: true, failOnError: true)

        controller.springSecurityService = getFakeSpringSecurityService()

        when: 'select action is executed'
        controller.select()

        then: 'select action response is correct'
        response.status == HttpStatus.OK.value()
        controller.session.LOGIN_TYPE == 'STORE'
    }

    void "should retrieve flattened groups when select groups as head office"() {
        given:

        GroupLevel groupLevel = new GroupLevel(retailerId: 9, level: 1, name: "Test Level 1")
        groupLevel.setId(150)
        groupLevel.save(flush: true, failOnError: true)

        Group testGroup = new Group(retailerId: 9, name: "Test Group", level: groupLevel)
        testGroup.setId(100)
        testGroup.save(flush: true, failOnError: true)

        controller.springSecurityService = getFakeSpringSecurityService()
        controller.springSecurityService.principal.storeId = null
        List<Group> testGroups = new ArrayList<>()
        testGroups.add(testGroup)
        controller.userService = Stub(UserService) {
            getUser(_) >> [groups: testGroups]
        }

        when: 'select action is executed'
        def controllerResponse = controller.select()

        then: 'select action response is correct'
        response.status == HttpStatus.OK.value()
        controller.session.LOGIN_TYPE == 'GROUP'
        controllerResponse != null
        controllerResponse.groups != null
    }

    //-----------------------------------select group function Unit tests--------------------------------//

    void "should show index page when select group by id"() {
        given:

        GroupLevel groupLevel = new GroupLevel(retailerId: 9, level: 1, name: "Test Level 1")
        groupLevel.setId(150)
        groupLevel.save(flush: true, failOnError: true)

        Group testGroup = new Group(retailerId: 9, name: "Test Group", level: groupLevel)
        testGroup.setId(100)
        testGroup.save(flush: true, failOnError: true)

        controller.springSecurityService = getFakeSpringSecurityService()
        controller.groupService = Stub(GroupService) {
            getFirstStoreInGroupHierarchy(_) >> [storeId: 150]
        }

        when: 'selectGroup action is executed'
        controller.selectGroup(100)

        then: 'selectGroup action response is correct'
        response.status == HttpStatus.OK.value()
        controller.session.GROUP_ID == 100
        controller.session.GROUP_NAME == 'Test Group'
        controller.session.STORE_ID == 150
    }

    //-----------------------------------add function Unit tests--------------------------------//

    void "should return all group levels for retailer when add"() {
        given:

        GroupLevel groupLevel = new GroupLevel(retailerId: 9, level: 1, name: "Test Level 1")
        groupLevel.setId(150)
        groupLevel.save(flush: true, failOnError: true)

        controller.springSecurityService = getFakeSpringSecurityService()

        when: 'add action is executed'
        def controllerResponse = controller.add()

        then: 'add action response is correct'
        response.status == HttpStatus.OK.value()
        controllerResponse != null
        controllerResponse.groupLevels != null
    }

    //-----------------------------------save function Unit tests--------------------------------//

    void "should be able to save group successfully"() {
        given:

        GroupLevel groupLevel = new GroupLevel(retailerId: 9, level: 1, name: "Test Level 1")
        groupLevel.setId(150)
        groupLevel.save(flush: true, failOnError: true)

        params.setProperty("retailerId", 9)
        params.setProperty("name", "Test Group")
        params.setProperty("level", groupLevel)

        controller.springSecurityService = getFakeSpringSecurityService()

        User user = new User(
                username: "testUser", password: "password", defaultStoreId: 234, retailerId: 9,
                name: "Test", active: true, role: Role.ENGINEER, dateOfBirth: new Date()
        )
        user.setId(100)

        user.save(flush: true, failOnError: true)

        controller.groupService = Stub(GroupService) {}

        when: 'save action is executed'
        controller.save()

        then: 'save action response is correct'
        response.status == HttpStatus.FOUND.value()
        flash.message == 'Group successfully added'
    }

    //-----------------------------------get parent groups function Unit tests--------------------------------//

    void "should be able to retrieve parent groups"() {
        given:
        GroupLevel groupLevel = new GroupLevel(retailerId: 9, level: 1, name: "Test Level 1")
        groupLevel.setId(150)
        groupLevel.save(flush: true, failOnError: true)

        Group testGroup = new Group(retailerId: 9, name: "Test Group", level: groupLevel)
        testGroup.setId(100)
        testGroup.save(flush: true, failOnError: true)

        List<Group> testGroups = new ArrayList<>()
        testGroups.add(testGroup)
        controller.groupService = Stub(GroupService) {
            getGroupsByLevel(_) >> testGroups
        }

        when: 'ajaxGetParentGroups action is executed'
        controller.ajaxGetParentGroups(0)

        then: 'ajaxGetParentGroups action response is correct'
        response.status == HttpStatus.OK.value()
    }

    def getFakeSpringSecurityService() {
        return Stub(SpringSecurityService) {
            getPrincipal() >> new HashMap() {
                {
                    put("storeId", 100)
                    put("storeNumber", 100)
                    put("retailerId", 9)
                    put("id", 100)
                }
            }
        }
    }

}
