package uk.co.wonderlane.wlpos

import grails.plugin.springsecurity.SpringSecurityService
import grails.testing.gorm.DataTest
import grails.testing.web.controllers.ControllerUnitTest
import org.joda.time.DateTime
import org.springframework.http.HttpStatus
import spock.lang.Specification
import uk.co.wonderlane.wlpos.enums.PrintReceiptOption
import uk.co.wonderlane.wlpos.enums.ProductStatus
import uk.co.wonderlane.wlpos.enums.wlim.ProductListStatus
import uk.co.wonderlane.wlpos.enums.wlim.ProductListType

class ProductListControllerSpec extends Specification implements ControllerUnitTest<ProductListController>, DataTest {

    Class<?>[] getDomainClassesToMock() {
        [StoreSettings, ProductList] as Class<?>[]
    }

    //-------------------------------index function Unit tests----------------------------//

    void "should show index page"() {
        given:

        when: 'index action is executed'
        controller.index()

        then: 'index action response is correct'
        response.status == HttpStatus.FOUND.value()
        response.redirectUrl == "/productList/listCentralCounts"
    }

    //-------------------------------listCentralCounts function Unit tests----------------------------//

    void "should retrieve listCentralCounts"() {
        given:
        controller.productListService = Stub(ProductListService) {
            getCentralCounts() >> new ArrayList()
        }

        when: 'listCentralCounts action is executed'
        def controllerResponse = controller.listCentralCounts()

        then: 'listCentralCounts action response is correct'
        response.status == HttpStatus.OK.value()
        controllerResponse.productLists != null
    }

    //-------------------------------showCentralCount function Unit tests----------------------------//

    void "should retrieve showCentralCount response"() {
        given:
        controller.productListService = Stub(ProductListService) {
            getProductList(_, _) >> new ArrayList()
        }

        controller.springSecurityService = getFakeSpringSecurityService()

        when: 'showCentralCount action is executed'
        def controllerResponse = controller.showCentralCount(100)

        then: 'showCentralCount action response is correct'
        response.status == HttpStatus.OK.value()
        controllerResponse.productList != null
    }

    //-------------------------------ajaxGetCentralCounts function Unit tests----------------------------//

    void "should retrieve ajaxGetCentralCounts response"() {
        given:
        controller.productListService = Stub(ProductListService) {
            getCentralCounts(_) >> new Object()
        }

        def mockView = '<div">DUMMY HTML</div>'
        views['/productList/_centralCountSearchResults.gsp'] = mockView

        when: 'ajaxGetCentralCounts action is executed'
        controller.ajaxGetCentralCounts("TestSearchTerm")

        then: 'ajaxGetCentralCounts action response is correct'
        response.status == HttpStatus.OK.value()
        model.productLists != null
        model.searchTerm != null
    }

    //-------------------------------addCentralCount function Unit tests----------------------------//

    void "should run addCentralCount request"() {
        given:
        controller.productListService = Stub(ProductListService) {
            getCentralCounts(_) >> new Object()
        }

        StoreSettings testStoreSettings = new StoreSettings(retailerId: 9,
                storeId: 100,
                printReceiptOption: PrintReceiptOption.NO_PRINT,
                type: "test",
                priceBand: new PriceBand(retailerId: 9, description: "Test"),
                range: new Range(retailerId: 9, description: "Test"),
                countIncrement: BigDecimal.ONE)

        testStoreSettings.springSecurityService = getFakeSpringSecurityService()

        testStoreSettings.save(flush: true, failOnError: true)

        controller.springSecurityService = getFakeSpringSecurityService()

        when: 'addCentralCount action is executed'
        def controllerResponse = controller.addCentralCount()

        then: 'addCentralCount action response is correct'
        response.status == HttpStatus.OK.value()
        controllerResponse.availableStores != null
        !controllerResponse.availableStores.isEmpty()
    }

    //-------------------------------saveCentralCount function Unit tests----------------------------//

    void "should return product list with errors on invalid saveCentralCount request"() {
        given:
        controller.productListService = Stub(ProductListService) {
            getCentralCounts(_) >> new Object()
        }

        StoreSettings testStoreSettings = new StoreSettings(retailerId: 9,
                storeId: 100,
                printReceiptOption: PrintReceiptOption.NO_PRINT,
                type: "test",
                priceBand: new PriceBand(retailerId: 9, description: "Test"),
                range: new Range(retailerId: 9, description: "Test"),
                countIncrement: BigDecimal.ONE)

        testStoreSettings.springSecurityService = getFakeSpringSecurityService()
        testStoreSettings.save(flush: true, failOnError: true)
        controller.springSecurityService = getFakeSpringSecurityService()

        when: 'saveCentralCount action is executed'
        controller.saveCentralCount(command)

        then: 'saveCentralCount action response is correct'
        response.status == HttpStatus.OK.value()
        view == "/productList/addCentralCount"
        model.productList.hasErrors()

        where:
        ID | command
        1  | new SaveCentralCountCommand(description: "Test", startDate: new Date(), endDate: new Date(), storeIdList: List.of(100))
        2  | new SaveCentralCountCommand(description: "Test", startDate: new Date(), endDate: new Date(), productVariantId: 100)
        3  | new SaveCentralCountCommand(description: "Test", endDate: new Date(), productVariantId: 100, storeIdList: List.of(1))
    }

    void "should return product list with errors on saveCentralCount request with invalid product list params"() {
        given:
        controller.productListService = Stub(ProductListService) {
            getCentralCounts(_) >> new Object()
        }

        StoreSettings testStoreSettings = new StoreSettings(retailerId: 9,
                storeId: 100,
                printReceiptOption: PrintReceiptOption.NO_PRINT,
                type: "test",
                priceBand: new PriceBand(retailerId: 9, description: "Test"),
                range: new Range(retailerId: 9, description: "Test"),
                countIncrement: BigDecimal.ONE)

        testStoreSettings.springSecurityService = Stub(SpringSecurityService) {
            getPrincipal() >> new HashMap() {
                {
                    put("storeId", 100)
                    put("storeNumber", 100)
                    put("retailerId", 9)
                    put("id", null)
                }
            }
        }

        controller.springSecurityService = testStoreSettings.springSecurityService

        testStoreSettings.save(flush: true, failOnError: true)

        controller.productService = Stub(ProductService) {
            getProductVariant(_) >> null
        }

        SaveCentralCountCommand validCommand = new SaveCentralCountCommand(description: "Test",
                startDate: new Date(),
                endDate: new Date(),
                storeIdList: List.of(1), productVariantId: [1])

        when: 'saveCentralCount action is executed'
        controller.saveCentralCount(validCommand)

        then: 'saveCentralCount action response is correct'
        response.status == HttpStatus.OK.value()
        view == "/productList/addCentralCount"
        model.productList.hasErrors()
    }

    void "should return product list on saveCentralCount request with valid product list params"() {
        given:
        controller.productListService = Stub(ProductListService) {}

        StoreSettings testStoreSettings = new StoreSettings(retailerId: 9,
                storeId: 100,
                printReceiptOption: PrintReceiptOption.NO_PRINT,
                type: "test",
                priceBand: new PriceBand(retailerId: 9, description: "Test"),
                range: new Range(retailerId: 9, description: "Test"),
                countIncrement: BigDecimal.ONE)
        testStoreSettings.springSecurityService = getFakeSpringSecurityService()
        testStoreSettings.save(flush: true, failOnError: true)

        controller.springSecurityService = getFakeSpringSecurityService()
        controller.productService = Stub(ProductService) {
            getProductVariant(_) >> productVariant
        }

        SaveCentralCountCommand validCommand = new SaveCentralCountCommand(description: "Test",
                startDate: new Date(),
                endDate: new Date(),
                storeIdList: List.of(1), productVariantId: [1])

        validCommand.properties.userId = "testUser"
        validCommand.properties.retailerId = 9
        validCommand.properties.type = ProductListType.DELIVERY
        validCommand.properties.status = ProductListStatus.PENDING
        validCommand.properties.stockAdjustedOnCompletion = true

        when: 'saveCentralCount action is executed'
        controller.saveCentralCount(validCommand)

        then: 'saveCentralCount action response is correct'
        response.status == HttpStatus.FOUND.value()
        flash.message == "Central count saved successfully."

        where:
        ID | productVariant
        1  | new ProductVariant()
        2  | null
    }

    void "should handle exception when saving product lists"() {
        given:

        StoreSettings testStoreSettings = new StoreSettings(retailerId: 9,
                storeId: 100,
                printReceiptOption: PrintReceiptOption.NO_PRINT,
                type: "test",
                priceBand: new PriceBand(retailerId: 9, description: "Test"),
                range: new Range(retailerId: 9, description: "Test"),
                countIncrement: BigDecimal.ONE)
        testStoreSettings.springSecurityService = getFakeSpringSecurityService()
        testStoreSettings.save(flush: true, failOnError: true)

        controller.springSecurityService = getFakeSpringSecurityService()
        controller.productService = Stub(ProductService) {
            getProductVariant(_) >> productVariant
        }

        SaveCentralCountCommand validCommand = new SaveCentralCountCommand(description: "Test",
                startDate: new Date(),
                endDate: new Date(),
                storeIdList: List.of(1), productVariantId: [1])

        validCommand.properties.userId = "testUser"
        validCommand.properties.retailerId = 9
        validCommand.properties.type = ProductListType.DELIVERY
        validCommand.properties.status = ProductListStatus.PENDING
        validCommand.properties.stockAdjustedOnCompletion = true

        when: 'saveCentralCount action is executed'
        controller.saveCentralCount(validCommand)

        then: 'saveCentralCount action response is correct'
        response.status == HttpStatus.OK.value()

        where:
        ID | productVariant
        1  | getProductVariant()
        2  | null
    }

    //-------------------------------ajaxAddProduct function Unit tests----------------------------//

    void "should retrieve product variants on ajaxAddProduct"() {
        given:
        controller.productService = Stub(ProductService) {
            getProductVariant(_) >> new Object()
        }

        def mockView = '<div">DUMMY HTML</div>'
        views['/productList/_centralCountProductRow.gsp'] = mockView

        when: 'ajaxAddProduct action is executed'
        controller.ajaxAddProduct(100)

        then: 'ajaxAddProduct action response is correct'
        response.status == HttpStatus.OK.value()
        model.productVariant != null
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

    ProductVariant getProductVariant(){
        ProductVariant productVariant = new ProductVariant(productId : 1)
        productVariant.effectiveDate = new DateTime()
        Product product = new Product();
        product.vatCode = new VatCode();
        product.status = ProductStatus.ACTIVE
        product.category = new Category()
        productVariant.product = product
        return productVariant;
    }
}
