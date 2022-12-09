package uk.co.wonderlane.wlpos

import grails.plugin.springsecurity.SpringSecurityService
import grails.testing.gorm.DataTest
import grails.testing.web.controllers.ControllerUnitTest
import org.springframework.http.HttpStatus
import spock.lang.Specification
import uk.co.wonderlane.wlpos.supplier.Supplier
import uk.co.wonderlane.wlpos.supplier.SupplierSortParams
import uk.co.wonderlane.wlpos.supplier.SymbolGroup
import uk.co.wonderlane.wlpos.supplier.SymbolGroupSubscription

class SupplierControllerSpec extends Specification implements ControllerUnitTest<SupplierController>, DataTest {

    Class<?>[] getDomainClassesToMock() {
        return [Supplier, SymbolGroupSubscription] as Class[]
    }

    //-------------------------------index function Unit tests----------------------------//

    void "should do nothing on index page"() {
        given:

        when: 'index action is executed'
        def controllerResponse = controller.index()

        then: 'index action response is correct'
        response.status == HttpStatus.OK.value()
        controllerResponse == null
    }

    //-------------------------------subscriptions function Unit tests----------------------------//

    void "should do nothing on subscriptions page"() {
        given:

        when: 'subscriptions action is executed'
        def controllerResponse = controller.subscriptions()

        then: 'subscriptions action response is correct'
        response.status == HttpStatus.OK.value()
        controllerResponse == null
    }


    //-------------------------------ajaxGetSearchSupplier function Unit tests----------------------------//

    class FakeSupplierResponse {
        int totalCount
        List<Supplier> suppliers

        FakeSupplierResponse(int totalCount, List<Supplier> suppliers) {
            this.totalCount = totalCount
            this.suppliers = suppliers
        }
    }

    void "should retrieve search suppliers"() {
        given:
        SupplierSortParams sortParams = new SupplierSortParams(max: max, offset: offset, sortColumn: sortColumn, sortOrder: sortOrder)
        params.searchBy = "Test"
        params.searchTerm = "Test"
        controller.supplierService = Stub(SupplierService) {
            getSuppliers(_, _, _, _, _, _) >> suppliers
        }

        def mockView = '<div>DUMMY HTML</div>'
        views['/supplier/_supplierSearchResults.gsp'] = mockView

        when: 'ajaxGetSearchSupplier action is executed'
        controller.ajaxGetSearchSupplier(sortParams)

        then: 'ajaxGetSearchSupplier action response is correct'
        response.status == HttpStatus.OK.value()
        model.suppliers != null
        model.searchTerm != null
        model.searchBy != null
        model.max != null
        model.offset != null
        model.sortParams != null
        suppliers == null || model.totalCount != null

        where:
        ID | max   | offset | sortColumn      | sortOrder       | suppliers
        1  | 100   | 10     | "name"          | "asc"           | [new FakeSupplierResponse(0, [])]
        2  | -1    | -1     | "name"          | "asc"           | [new FakeSupplierResponse(1, [new Supplier()])]
        3  | 10000 | 10000  | "name"          | "asc"           | [new FakeSupplierResponse(1, [new Supplier()])]
        4  | 10000 | 10000  | null            | null            | [new FakeSupplierResponse(1, [new Supplier()])]
        5  | 10000 | 10000  | "invalid_value" | "invalid_value" | [new FakeSupplierResponse(1, [new Supplier()])]
        5  | 10000 | 10000  | "invalid_value" | "invalid_value" | null
    }

    //-------------------------------ajaxGetSymbolGroupSubscriptions function Unit tests----------------------------//

    void "should retrieve symbol group subscriptions"() {
        given:

        controller.springSecurityService = Stub(SpringSecurityService) {
            getPrincipal() >> new HashMap() {
                {
                    put("retailer", new HashMap() {
                        {
                            put("snappyShopperEnabled", true)
                        }
                    })
                }
            }
        }

        SymbolGroup symbolGroup = new SymbolGroup()
        symbolGroup.id = 100;

        controller.supplierService = Stub(SupplierService) {
            getSymbolGroupSubscriptions() >> [symbolGroup]
            getSymbolGroups() >> [symbolGroup]
        }

        def mockView = '<div>DUMMY HTML</div>'
        views['/supplier/_symbolGroupSubscriptionsSearchResults.gsp'] = mockView

        when: 'ajaxGetSymbolGroupSubscriptions action is executed'
        controller.ajaxGetSymbolGroupSubscriptions()

        then: 'ajaxGetSymbolGroupSubscriptions action response is correct'
        response.status == HttpStatus.OK.value()
        model.symbolGroupSubscriptions != null
        !model.symbolGroupSubscriptions.isEmpty()
    }

    void "should not retrieve symbol group 4 when snappy not enabled with subscriptions"() {
        given:

        controller.springSecurityService = Stub(SpringSecurityService) {
            getPrincipal() >> new HashMap() {
                {
                    put("retailer", new HashMap() {
                        {
                            put("snappyShopperEnabled", false)
                        }
                    })
                }
            }
        }

        SymbolGroup symbolGroup1 = new SymbolGroup()
        symbolGroup1.id = 100
        SymbolGroupSubscription symbolGroupSubscription1 = new SymbolGroupSubscription()
        symbolGroupSubscription1.setSymbolGroup(symbolGroup1)

        SymbolGroup symbolGroup2 = new SymbolGroup()
        symbolGroup2.id = 4
        SymbolGroupSubscription symbolGroupSubscription2 = new SymbolGroupSubscription()
        symbolGroupSubscription2.setSymbolGroup(symbolGroup2)

        controller.supplierService = Stub(SupplierService) {
            getSymbolGroupSubscriptions() >> [symbolGroupSubscription1, symbolGroupSubscription2]
            getSymbolGroups() >> [symbolGroup1, symbolGroup2]
        }

        def mockView = '<div>DUMMY HTML</div>'
        views['/supplier/_symbolGroupSubscriptionsSearchResults.gsp'] = mockView

        when: 'ajaxGetSymbolGroupSubscriptions action is executed'
        controller.ajaxGetSymbolGroupSubscriptions()

        then: 'ajaxGetSymbolGroupSubscriptions action response is correct'
        response.status == HttpStatus.OK.value()
        model.symbolGroupSubscriptions != null
        !model.symbolGroupSubscriptions.isEmpty()
        model.symbolGroupSubscriptions.size() == 1
        model.symbolGroupSubscriptions.find { it.symbolGroup.id == 4 } == null
    }

    //-------------------------------ajaxAddSupplier function Unit tests----------------------------//

    void "should initially retrieve enable save for add supplier"() {
        given:

        when: 'ajaxAddSupplier action is executed'
        controller.ajaxAddSupplier()

        then: 'ajaxAddSupplier action response is correct'
        response.status == HttpStatus.OK.value()
        model.enableSave
    }

    //-------------------------------ajaxEditSupplier function Unit tests----------------------------//

    void "should not enable save if supplier not available on edit"() {
        given:
        controller.supplierService = Stub(SupplierService) {
            getSupplier(_) >> null
        }

        when: 'ajaxEditSupplier action is executed'
        controller.ajaxEditSupplier(1)

        then: 'ajaxEditSupplier action response is correct'
        response.status == HttpStatus.OK.value()
        !model.enableSave
        !model.supplier
    }

    void "should not enable save if supplier available and store id mismatch on edit"() {
        given:
        controller.supplierService = Stub(SupplierService) {
            getSupplier(_) >> new Supplier(storeId: 200)
        }

        controller.springSecurityService = getFakeSpringSecurityService()

        when: 'ajaxEditSupplier action is executed'
        controller.ajaxEditSupplier(1)

        then: 'ajaxEditSupplier action response is correct'
        response.status == HttpStatus.OK.value()
        !model.enableSave
        model.supplier != null
    }

    void "should enable save if supplier available and valid on edit"() {
        given:
        controller.supplierService = Stub(SupplierService) {
            getSupplier(_) >> new Supplier(storeId: 100)
        }

        controller.springSecurityService = getFakeSpringSecurityService()

        when: 'ajaxEditSupplier action is executed'
        controller.ajaxEditSupplier(1)

        then: 'ajaxEditSupplier action response is correct'
        response.status == HttpStatus.OK.value()
        model.enableSave
        model.supplier != null
    }

    //-------------------------------ajaxSaveSupplier function Unit tests----------------------------//

    void "should not enable save for existing supplier but not validating on save"() {
        given:
        controller.supplierService = Stub(SupplierService) {
            getSupplier(_) >> new Supplier(storeId: 100)
        }
        params.id = "100"

        when: 'ajaxSaveSupplier action is executed'
        controller.ajaxSaveSupplier()

        then: 'ajaxSaveSupplier action response is correct'
        response.status == HttpStatus.OK.value()
        model.enableSave
        model.supplier
    }

    void "should not enable save for new supplier but not validating on save"() {
        given:
        controller.springSecurityService = getFakeSpringSecurityService()

        when: 'ajaxSaveSupplier action is executed'
        controller.ajaxSaveSupplier()

        then: 'ajaxSaveSupplier action response is correct'
        response.status == HttpStatus.OK.value()
        model.enableSave
        model.supplier
    }

    void "should save valid supplier"() {
        given:
        controller.supplierService = Stub(SupplierService) {}
        controller.springSecurityService = getFakeSpringSecurityService()
        params.name = "Test"

        when: 'ajaxSaveSupplier action is executed'
        controller.ajaxSaveSupplier()

        then: 'ajaxSaveSupplier action response is correct'
        response.status == HttpStatus.OK.value()
    }

    //-------------------------------ajaxAddSymbolGroupSubscription function Unit tests----------------------------//

    void "should retrieve not subscribed symbol groups when snappy enabled"() {
        given:
        Map principal = new HashMap() {
            {
                put("retailer", new HashMap() {
                    {
                        put("snappyShopperEnabled", true)
                    }
                })
                put("storeId", 100)
            }
        }
        controller.springSecurityService = Stub(SpringSecurityService) {
            getPrincipal() >> principal
        }

        SymbolGroup symbolGroup1 = new SymbolGroup()
        symbolGroup1.id = 100
        SymbolGroupSubscription symbolGroupSubscription1 = new SymbolGroupSubscription()
        symbolGroupSubscription1.setSymbolGroup(symbolGroup1)

        SymbolGroup symbolGroup2 = new SymbolGroup()
        symbolGroup2.id = 4
        SymbolGroupSubscription symbolGroupSubscription2 = new SymbolGroupSubscription()
        symbolGroupSubscription2.setSymbolGroup(symbolGroup2)

        controller.supplierService = Stub(SupplierService) {
            getSupplier(_) >> new Supplier(storeId: 100)
            getSymbolGroups() >> [symbolGroup1, symbolGroup2]
            getSymbolGroupSubscriptions() >> []
        }

        when: 'ajaxAddSymbolGroupSubscription action is executed'
        controller.ajaxAddSymbolGroupSubscription()

        then: 'ajaxAddSymbolGroupSubscription action response is correct'
        response.status == HttpStatus.OK.value()
        model.symbolGroups != null
        model.symbolGroups.size() == 2
        model.symbolGroups.find { it.id == 4 } != null

    }

    void "should retrieve not subscribed symbol groups when snappy not enabled"() {
        given:
        Map principal = new HashMap() {
            {
                put("retailer", new HashMap() {
                    {
                        put("snappyShopperEnabled", false)
                    }
                })
                put("storeId", storeId)
            }
        }
        controller.springSecurityService = Stub(SpringSecurityService) {
            getPrincipal() >> principal
        }

        SymbolGroup symbolGroup1 = new SymbolGroup()
        symbolGroup1.id = 100
        SymbolGroupSubscription symbolGroupSubscription1 = new SymbolGroupSubscription()
        symbolGroupSubscription1.setSymbolGroup(symbolGroup1)

        SymbolGroup symbolGroup2 = new SymbolGroup()
        symbolGroup2.id = 4
        SymbolGroupSubscription symbolGroupSubscription2 = new SymbolGroupSubscription()
        symbolGroupSubscription2.setSymbolGroup(symbolGroup2)

        controller.supplierService = Stub(SupplierService) {
            getSupplier(_) >> new Supplier(storeId: 100)
            getSymbolGroups() >> [symbolGroup1, symbolGroup2]
            getSymbolGroupSubscriptions() >> []
        }

        when: 'ajaxAddSymbolGroupSubscription action is executed'
        controller.ajaxAddSymbolGroupSubscription()

        then: 'ajaxAddSymbolGroupSubscription action response is correct'
        response.status == HttpStatus.OK.value()
        model.symbolGroups != null
        model.symbolGroups.size() == 1
        model.symbolGroups.find { it.id == 4 } == null

        where:
        ID | storeId
        1  | 100
        2  | null
    }

    void "should not retrieve already subscribed symbol groups when snappy not enabled"() {
        given:
        Map principal = new HashMap() {
            {
                put("retailer", new HashMap() {
                    {
                        put("snappyShopperEnabled", false)
                    }
                })
                put("storeId", storeId)
            }
        }
        controller.springSecurityService = Stub(SpringSecurityService) {
            getPrincipal() >> principal
        }

        SymbolGroup symbolGroup1 = new SymbolGroup()
        symbolGroup1.id = 100
        SymbolGroupSubscription symbolGroupSubscription1 = new SymbolGroupSubscription()
        symbolGroupSubscription1.setSymbolGroup(symbolGroup1)

        SymbolGroup symbolGroup2 = new SymbolGroup()
        symbolGroup2.id = 200
        SymbolGroupSubscription symbolGroupSubscription2 = new SymbolGroupSubscription()
        symbolGroupSubscription2.setSymbolGroup(symbolGroup2)

        controller.supplierService = Stub(SupplierService) {
            getSupplier(_) >> new Supplier(storeId: 100)
            getSymbolGroups() >> [symbolGroup1, symbolGroup2]
            getSymbolGroupSubscriptions() >> [symbolGroupSubscription1]
        }

        when: 'ajaxAddSymbolGroupSubscription action is executed'
        controller.ajaxAddSymbolGroupSubscription()

        then: 'ajaxAddSymbolGroupSubscription action response is correct'
        response.status == HttpStatus.OK.value()
        model.symbolGroups != null
        model.symbolGroups.size() == 1
        model.symbolGroups.find { it.id == 100 } == null
        model.symbolGroups.find { it.id == 200 } != null


        where:
        ID | storeId
        1  | 100
        2  | null
    }

    //-------------------------------ajaxEditSymbolGroupSubscription function Unit tests----------------------------//

    void "should retrieve not subscribed symbol groups when snappy enabled - edit subscription"() {
        given:
        Map principal = new HashMap() {
            {
                put("retailer", new HashMap() {
                    {
                        put("snappyShopperEnabled", true)
                    }
                })
                put("storeId", 100)
            }
        }
        controller.springSecurityService = Stub(SpringSecurityService) {
            getPrincipal() >> principal
        }

        SymbolGroup symbolGroup1 = new SymbolGroup()
        symbolGroup1.id = 100
        SymbolGroupSubscription symbolGroupSubscription1 = new SymbolGroupSubscription()
        symbolGroupSubscription1.setSymbolGroup(symbolGroup1)

        SymbolGroup symbolGroup2 = new SymbolGroup()
        symbolGroup2.id = 200
        SymbolGroupSubscription symbolGroupSubscription2 = new SymbolGroupSubscription()
        symbolGroupSubscription2.setSymbolGroup(symbolGroup2)

        controller.supplierService = Stub(SupplierService) {
            getSupplier(_) >> new Supplier(storeId: 100)
            getSymbolGroupSubscription(_) >> symbolGroupSubscription2
            getSymbolGroups() >> [symbolGroup1, symbolGroup2]
            getSymbolGroupSubscriptions() >> [symbolGroupSubscription1]
        }

        when: 'ajaxEditSymbolGroupSubscription action is executed'
        controller.ajaxEditSymbolGroupSubscription(100)

        then: 'ajaxEditSymbolGroupSubscription action response is correct'
        response.status == HttpStatus.OK.value()
        model.symbolGroups != null
        model.symbolGroups.size() == 1
        model.symbolGroups.find { it.id == 200 } != null
        model.symbolGroups.find { it.id == 100 } == null

    }

    void "should retrieve not subscribed symbol groups when snappy not enabled - edit subscriptions"() {
        given:
        Map principal = new HashMap() {
            {
                put("retailer", new HashMap() {
                    {
                        put("snappyShopperEnabled", false)
                    }
                })
                put("storeId", storeId)
            }
        }
        controller.springSecurityService = Stub(SpringSecurityService) {
            getPrincipal() >> principal
        }

        SymbolGroup symbolGroup1 = new SymbolGroup()
        symbolGroup1.id = 100
        SymbolGroupSubscription symbolGroupSubscription1 = new SymbolGroupSubscription()
        symbolGroupSubscription1.setSymbolGroup(symbolGroup1)

        SymbolGroup symbolGroup2 = new SymbolGroup()
        symbolGroup2.id = 4
        SymbolGroupSubscription symbolGroupSubscription2 = new SymbolGroupSubscription()
        symbolGroupSubscription2.setSymbolGroup(symbolGroup2)

        controller.supplierService = Stub(SupplierService) {
            getSupplier(_) >> new Supplier(storeId: 100)
            getSymbolGroupSubscription(_) >> symbolGroupSubscription2
            getSymbolGroups() >> [symbolGroup1, symbolGroup2]
            getSymbolGroupSubscriptions() >> [symbolGroupSubscription1]
        }

        when: 'ajaxEditSymbolGroupSubscription action is executed'
        controller.ajaxEditSymbolGroupSubscription(4)

        then: 'ajaxEditSymbolGroupSubscription action response is correct'
        response.status == HttpStatus.OK.value()
        model.symbolGroups != null
        model.symbolGroups.size() == 0
        model.symbolGroups.find { it.id == 4 } == null // Filtered off Snappy
        model.symbolGroups.find { it.id == 100 } == null // Filtered off by ID

        where:
        ID | storeId
        1  | 100
        2  | null
    }

    //-------------------------------ajaxSymbolGroupAction function Unit tests----------------------------//

    void "do nothing if not snappy symbol group id on ajaxSymbolGroupAction"() {
        given:
        params.symbolGroupId = "0"
        controller.springSecurityService = getFakeSpringSecurityService()

        when: 'ajaxSymbolGroupAction action is executed'
        controller.ajaxSymbolGroupAction()

        then: 'ajaxSymbolGroupAction action response is correct'
        response.status == HttpStatus.OK.value()
        response.text != null
        response.text == "Sync should begin shortly for Snappy Service in Store 100."
    }

    void "do send sync message for snappy symbol group id on ajaxSymbolGroupAction"() {
        given:
        params.symbolGroupId = "4"
        controller.springSecurityService = getFakeSpringSecurityService()
        controller.rabbitService = Stub(BackOfficeRabbitService) {}
        controller.gsonProvider = new GsonProvider()

        when: 'ajaxSymbolGroupAction action is executed'
        controller.ajaxSymbolGroupAction()

        then: 'ajaxSymbolGroupAction action response is correct'
        response.status == HttpStatus.OK.value()
        response.text != null
        response.text == "Sync should begin shortly for Snappy Service in Store 100."
    }

    //-------------------------------ajaxGetSymbolGroupForm function Unit tests----------------------------//

    void "do nothing if not snappy or nisa symbol group id on ajaxGetSymbolGroupForm"() {
        given:
        controller.springSecurityService = getFakeSpringSecurityService()

        SymbolGroup symbolGroup1 = new SymbolGroup()
        symbolGroup1.id = 100
        SymbolGroupSubscription symbolGroupSubscription1 = new SymbolGroupSubscription()
        symbolGroupSubscription1.setSymbolGroup(symbolGroup1)

        SymbolGroup symbolGroup2 = new SymbolGroup()
        symbolGroup2.id = 200
        SymbolGroupSubscription symbolGroupSubscription2 = new SymbolGroupSubscription()
        symbolGroupSubscription2.setSymbolGroup(symbolGroup2)

        controller.supplierService = Stub(SupplierService) {
            getSymbolGroupSubscription(_) >> null
            getSymbolGroups() >> [symbolGroup1, symbolGroup2]
            getSymbolGroupSubscriptions() >> [symbolGroupSubscription1]
        }

        when: 'ajaxGetSymbolGroupForm action is executed'
        controller.ajaxGetSymbolGroupForm(100)

        then: 'ajaxGetSymbolGroupForm action response is correct'
        response.status == HttpStatus.OK.value()
        !model.symbolGroupSubscription
        !model.symbolGroups
    }

    void "do nothing if snappy symbol group id but snappy disabled for retailer on ajaxGetSymbolGroupForm"() {
        given:
        Map principal = new HashMap() {
            {
                put("retailer", new HashMap() {
                    {
                        put("snappyShopperEnabled", false)
                    }
                })
            }
        }
        controller.springSecurityService = Stub(SpringSecurityService) {
            getPrincipal() >> principal
        }
        SymbolGroup symbolGroup1 = new SymbolGroup()
        symbolGroup1.id = 100
        SymbolGroupSubscription symbolGroupSubscription1 = new SymbolGroupSubscription()
        symbolGroupSubscription1.setSymbolGroup(symbolGroup1)

        SymbolGroup symbolGroup2 = new SymbolGroup()
        symbolGroup2.id = 200
        SymbolGroupSubscription symbolGroupSubscription2 = new SymbolGroupSubscription()
        symbolGroupSubscription2.setSymbolGroup(symbolGroup2)

        controller.supplierService = Stub(SupplierService) {
            getSymbolGroupSubscription(_) >> null
            getSymbolGroups() >> [symbolGroup1, symbolGroup2]
            getSymbolGroupSubscriptions() >> [symbolGroupSubscription1]
        }

        when: 'ajaxGetSymbolGroupForm action is executed'
        controller.ajaxGetSymbolGroupForm(4)

        then: 'ajaxGetSymbolGroupForm action response is correct'
        response.status == HttpStatus.OK.value()
        !model.symbolGroupSubscription
        !model.symbolGroups
    }

    void "return symbol groups and subscriptions for nisa symbol group id on ajaxGetSymbolGroupForm"() {
        given:
        controller.springSecurityService = getFakeSpringSecurityService()

        SymbolGroup symbolGroup1 = new SymbolGroup()
        symbolGroup1.id = 1
        SymbolGroupSubscription symbolGroupSubscription1 = new SymbolGroupSubscription()
        symbolGroupSubscription1.setSymbolGroup(symbolGroup1)

        SymbolGroup symbolGroup2 = new SymbolGroup()
        symbolGroup2.id = 200
        SymbolGroupSubscription symbolGroupSubscription2 = new SymbolGroupSubscription()
        symbolGroupSubscription2.setSymbolGroup(symbolGroup2)

        controller.supplierService = Stub(SupplierService) {
            getSymbolGroupSubscription(_) >> symbolGroupSubscription2
            getSymbolGroups() >> [symbolGroup1, symbolGroup2]
            getSymbolGroupSubscriptions() >> [symbolGroupSubscription1]
        }

        when: 'ajaxGetSymbolGroupForm action is executed'
        controller.ajaxGetSymbolGroupForm(1)

        then: 'ajaxGetSymbolGroupForm action response is correct'
        response.status == HttpStatus.OK.value()
        model.symbolGroupSubscription != null
        model.symbolGroups != null
    }

    void "return symbol groups and subscriptions for snappy symbol group id on ajaxGetSymbolGroupForm"() {
        given:
        Map principal = new HashMap() {
            {
                put("retailer", new HashMap() {
                    {
                        put("snappyShopperEnabled", true)
                    }
                })
            }
        }
        controller.springSecurityService = Stub(SpringSecurityService) {
            getPrincipal() >> principal
        }

        SymbolGroup symbolGroup1 = new SymbolGroup()
        symbolGroup1.id = 4
        SymbolGroupSubscription symbolGroupSubscription1 = new SymbolGroupSubscription()
        symbolGroupSubscription1.setSymbolGroup(symbolGroup1)

        SymbolGroup symbolGroup2 = new SymbolGroup()
        symbolGroup2.id = 200
        SymbolGroupSubscription symbolGroupSubscription2 = new SymbolGroupSubscription()
        symbolGroupSubscription2.setSymbolGroup(symbolGroup2)

        controller.supplierService = Stub(SupplierService) {
            getSymbolGroupSubscription(_) >> symbolGroupSubscription2
            getSymbolGroups() >> [symbolGroup1, symbolGroup2]
            getSymbolGroupSubscriptions() >> [symbolGroupSubscription1]
        }

        when: 'ajaxGetSymbolGroupForm action is executed'
        controller.ajaxGetSymbolGroupForm(4)

        then: 'ajaxGetSymbolGroupForm action response is correct'
        response.status == HttpStatus.OK.value()
        model.symbolGroupSubscription != null
        model.symbolGroups != null
    }

    //-------------------------------ajaxSaveSymbolGroupSubscription function Unit tests----------------------------//

    void "should handle invalid parameters when saving symbol group subscription"() {
        given:
        params.id = subscriptionId
        params.symbolGroup = new SymbolGroup()
        params.username = "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA" +
                "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA"
        // trigger validation error username > 50

        SymbolGroup symbolGroup1 = new SymbolGroup()
        symbolGroup1.id = 4
        SymbolGroupSubscription symbolGroupSubscription1 = new SymbolGroupSubscription()
        symbolGroupSubscription1.setSymbolGroup(symbolGroup1)

        SymbolGroup symbolGroup2 = new SymbolGroup()
        symbolGroup2.id = 200
        SymbolGroupSubscription symbolGroupSubscription2 = new SymbolGroupSubscription()
        symbolGroupSubscription2.setSymbolGroup(symbolGroup2)

        controller.supplierService = Stub(SupplierService) {
            getSymbolGroupSubscription(_) >> new SymbolGroupSubscription()
            getSymbolGroups() >> [symbolGroup1, symbolGroup2]
            getSymbolGroupSubscriptions() >> [symbolGroupSubscription1]
        }

        controller.springSecurityService = getFakeSpringSecurityService()

        when: 'ajaxSaveSymbolGroupSubscription action is executed'
        controller.ajaxSaveSymbolGroupSubscription()

        then: 'ajaxSaveSymbolGroupSubscription action response is correct'
        response.status == HttpStatus.OK.value()
        model.symbolGroupSubscription != null
        model.symbolGroups != null

        where:
        ID | subscriptionId
        1  | "100"
        2  | null
        3  | "-1"
    }

    void "should handle closed rabbit service when saving symbol group subscription"() {
        given:
        params.symbolGroup = new SymbolGroup()

        controller.rabbitService = Stub(BackOfficeRabbitService) {
            isOpen() >> false
        }

        SymbolGroup symbolGroup1 = new SymbolGroup()
        symbolGroup1.id = 4
        SymbolGroupSubscription symbolGroupSubscription1 = new SymbolGroupSubscription()
        symbolGroupSubscription1.setSymbolGroup(symbolGroup1)

        SymbolGroup symbolGroup2 = new SymbolGroup()
        symbolGroup2.id = 200
        SymbolGroupSubscription symbolGroupSubscription2 = new SymbolGroupSubscription()
        symbolGroupSubscription2.setSymbolGroup(symbolGroup2)

        controller.supplierService = Stub(SupplierService) {
            getSymbolGroupSubscription(_) >> symbolGroupSubscription2
            getSymbolGroups() >> [symbolGroup1, symbolGroup2]
            getSymbolGroupSubscriptions() >> [symbolGroupSubscription1]
        }

        controller.springSecurityService = getFakeSpringSecurityService()

        when: 'ajaxSaveSymbolGroupSubscription action is executed'
        controller.ajaxSaveSymbolGroupSubscription()

        then: 'ajaxSaveSymbolGroupSubscription action response is correct'
        response.status == HttpStatus.OK.value()
        model.symbolGroupSubscription != null
        model.symbolGroups != null

    }

    void "should send symbol group messages to Nisa and Snappy suppliers when saving symbol group subscription"() {
        given:

        controller.rabbitService = Stub(BackOfficeRabbitService) {
            isOpen() >> true
        }

        SymbolGroup symbolGroup1 = new SymbolGroup()
        symbolGroup1.id = symbolGroupId
        SymbolGroupSubscription symbolGroupSubscription1 = new SymbolGroupSubscription()
        symbolGroupSubscription1.setSymbolGroup(symbolGroup1)

        controller.supplierService = Stub(SupplierService) {
            getSymbolGroupSubscription(_) >> symbolGroupSubscription1
            getSymbolGroups() >> [symbolGroup1]
            getSymbolGroupSubscriptions() >> [symbolGroupSubscription1]
        }

        controller.gsonProvider = new GsonProvider()

        params.symbolGroup = symbolGroup1

        controller.springSecurityService = getFakeSpringSecurityService()

        when: 'ajaxSaveSymbolGroupSubscription action is executed'
        controller.ajaxSaveSymbolGroupSubscription()

        then: 'ajaxSaveSymbolGroupSubscription action response is correct'
        response.status == HttpStatus.OK.value()
        response.text == "OK"

        where:
        ID | subscriptionId | symbolGroupId
        1  | "4"            | 4
        2  | "1"            | 1
        3  | null           | 100
        4  | -1             | 200
    }

    def getFakeSpringSecurityService() {
        return Stub(SpringSecurityService) {
            getPrincipal() >> new HashMap() {
                {
                    put("storeId", 100)
                    put("storeNumber", 100)
                    put("retailerId", 9)
                }
            }
        }
    }
}
