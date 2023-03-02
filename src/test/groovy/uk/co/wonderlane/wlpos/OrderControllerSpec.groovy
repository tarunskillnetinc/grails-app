package uk.co.wonderlane.wlpos

import grails.plugin.springsecurity.SpringSecurityService
import grails.testing.gorm.DataTest
import grails.testing.web.controllers.ControllerUnitTest
import org.joda.time.DateTime
import org.springframework.http.HttpStatus
import spock.lang.Specification
import uk.co.wonderlane.wlpos.enums.Role
import uk.co.wonderlane.wlpos.enums.wlim.ProductListStatus
import uk.co.wonderlane.wlpos.supplier.Pack
import uk.co.wonderlane.wlpos.supplier.Supplier
import uk.co.wonderlane.wlpos.supplier.SymbolGroup

class OrderControllerSpec extends Specification implements ControllerUnitTest<OrderController>, DataTest {

    Class<?>[] getDomainClassesToMock() {
        [Supplier] as Class<?>[]
    }

    //-------------------------------index function Unit tests----------------------------//

    void "should do nothing when index function called"() {
        given:

        when: 'index action is executed'
        controller.index()

        then: 'index action response is correct'
        response.status == HttpStatus.OK.value()
    }

    //-------------------------------product list function Unit tests----------------------------//

    void "should redirect to reporting view if head office login parameters invalid - productList"() {
        given:
        controller.springSecurityService = getFakeSpringSecurityService()
        controller.springSecurityService.principal.storeId = storeId
        controller.springSecurityService.principal.id = userId

        when: 'productList action is executed'
        controller.productList()

        then: 'productList action response is correct'
        response.status == HttpStatus.FOUND.value()
        response.redirectUrl == "/reporting/orders"

        where:
        ID | storeId | userId
        1  | null    | -1
        2  | 100     | -1
        3  | null    | 1
    }

    void "should render product list view with valid parameters"() {
        given:
        params.supplierId = supplierId
        params.isNew = isNew
        controller.springSecurityService = getFakeSpringSecurityService()

        uk.co.wonderlane.wlpos.entities.wlim.ProductList testProductList = new uk.co.wonderlane.wlpos.entities.wlim.ProductList()
        testProductList.getProductListItems().add(getMockProductListItem(100))
        testProductList.setStoreId("100")

        controller.orderService = Stub(OrderService) {
            getActiveProductList(_, _) >> testProductList
            createProductList(_, _, _) >> testProductList
        }

        User testUser = new User(
                username: "testUser", password: "password", defaultStoreId: 234,
                name: "Test", active: true, role: Role.ENGINEER, dateOfBirth: new Date()
        )
        testUser.setId(100)
        controller.userService = Stub(UserService) {
            getUser(_) >> testUser
        }

        Supplier testSupplier = new Supplier(name: "Test Supplier", retailerId: 9, storeId: 100)
        testSupplier.setId(100)

        controller.supplierService = Stub(SupplierService) {
            getSupplier(_) >> testSupplier
        }

        when: 'productList action is executed'
        controller.productList()

        then: 'productList action response is correct'
        response.status == HttpStatus.OK.value()
        view == "/order/_productList"
        if (params.isNew == "1" || testProductList.supplierId == 100) {
            model.supplier != null
            model.productList != null
            model.productListItems != null
        }

        where:
        ID | supplierId   | isNew        | productListSupplierId
        1  | null         | null         | null
        2  | "NOT_NUMBER" | "NOT_NUMBER" | null
        3  | "1234567890" | "1234567890" | null
        4  | "100"        | "1"          | null
        5  | "100"        | "1"          | 100
        6  | "100"        | "0"          | null
        7  | "100"        | "0"          | 100
    }

    //-------------------------------product list item function Unit tests----------------------------//

    void "should redirect to reporting view if head office login parameters invalid - productListItem"() {
        given:
        controller.springSecurityService = getFakeSpringSecurityService()
        controller.springSecurityService.principal.storeId = storeId
        controller.springSecurityService.principal.id = userId

        when: 'productListItem action is executed'
        controller.productListItem()

        then: 'productListItem action response is correct'
        response.status == HttpStatus.FOUND.value()
        response.redirectUrl == "/reporting/orders"

        where:
        ID | storeId | userId
        1  | null    | -1
        2  | 100     | -1
        3  | null    | 1
    }

    void "should redirect to product list view if product list status is not IN_PROGRESS"() {
        given:
        params.productListId = "100"
        controller.springSecurityService = getFakeSpringSecurityService()

        uk.co.wonderlane.wlpos.entities.wlim.ProductList testProductList = new uk.co.wonderlane.wlpos.entities.wlim.ProductList()
        testProductList.getProductListItems().add(getMockProductListItem(100))
        testProductList.setStoreId("100")
        testProductList.setStatus(productListStatus)

        controller.orderService = Stub(OrderService) {
            getProductListById(_) >> testProductList
        }

        when: 'productListItem action is executed'
        controller.productListItem()

        then: 'productListItem action response is correct'
        response.status == HttpStatus.FOUND.value()
        response.redirectUrl == "/order/productList"

        where:
        ID | productListStatus
        1  | ProductListStatus.PENDING
        2  | ProductListStatus.PARTIALLY_COMPLETE
        3  | ProductListStatus.COMPLETE
        4  | null
    }

    void "should render product list item view for IN_PROGRESS product list"() {
        given:
        params.productListId = "100"
        params.variantId = "100"
        params.supplierId = "100"
        controller.springSecurityService = getFakeSpringSecurityService()

        uk.co.wonderlane.wlpos.entities.wlim.ProductList testProductList = new uk.co.wonderlane.wlpos.entities.wlim.ProductList()
        testProductList.getProductListItems().add(getMockProductListItem(100))
        testProductList.getProductListItems().add(getMockProductListItem(200))
        testProductList.setStoreId("100")
        testProductList.setStatus(ProductListStatus.IN_PROGRESS)

        controller.orderService = Stub(OrderService) {
            getProductListById(_) >> testProductList
        }

        Supplier testSupplier = new Supplier(name: "Test Supplier", retailerId: 9, storeId: 100)
        testSupplier.setId(100)

        controller.supplierService = Stub(SupplierService) {
            getSupplier(_) >> testSupplier
        }

        ProductVariant testVariant = new ProductVariant(sku: 100, product: new Product(description: "TEST PRODUCT"))
        testVariant.getPacks().add(getMockPack(100, testSupplier))
        testVariant.getPacks().add(getMockPack(200, testSupplier))

        controller.productService = Stub(ProductService) {
            getProductVariant(_) >> testVariant
        }

        when: 'productListItem action is executed'
        controller.productListItem()

        then: 'productListItem action response is correct'
        response.status == HttpStatus.OK.value()
        view == "/order/_productListItem"
        model.variants != null
        model.packs != null

    }

    //-------------------------------check active products function Unit tests----------------------------//

    void "should return response status 204 if product list has a supplier id on ajaxCheckActiveProducts"() {
        given:
        controller.springSecurityService = getFakeSpringSecurityService()

        User testUser = new User(
                username: "testUser", password: "password", defaultStoreId: 234,
                name: "Test", active: true, role: Role.ENGINEER, dateOfBirth: new Date()
        )
        testUser.setId(100)
        controller.userService = Stub(UserService) {
            getUser(_) >> testUser
        }

        uk.co.wonderlane.wlpos.entities.wlim.ProductList testProductList = new uk.co.wonderlane.wlpos.entities.wlim.ProductList()
        testProductList.setSupplierId(100)
        controller.orderService = Stub(OrderService) {
            getActiveProductList(_, _) >> testProductList
        }

        when: 'ajaxCheckActiveProducts action is executed'
        controller.ajaxCheckActiveProducts()

        then: 'ajaxCheckActiveProducts action response is correct'
        response.status == 204
        model.suppliers == null
    }

    void "should return response status 200 if product list does not have a supplier id on ajaxCheckActiveProducts"() {
        given:
        controller.springSecurityService = getFakeSpringSecurityService()

        User testUser = new User(
                username: "testUser", password: "password", defaultStoreId: 234,
                name: "Test", active: true, role: Role.ENGINEER, dateOfBirth: new Date()
        )
        testUser.setId(100)
        controller.userService = Stub(UserService) {
            getUser(_) >> testUser
        }

        uk.co.wonderlane.wlpos.entities.wlim.ProductList testProductList = new uk.co.wonderlane.wlpos.entities.wlim.ProductList()
        testProductList.setSupplierId(null)
        controller.orderService = Stub(OrderService) {
            getActiveProductList(_, _) >> testProductList
        }

        Supplier testSupplier = new Supplier(name: "Test Supplier", retailerId: 9, storeId: 100)
        testSupplier.setId(100)
        testSupplier.save(flush: true, failOnError: true)

        when: 'ajaxCheckActiveProducts action is executed'
        controller.ajaxCheckActiveProducts()

        then: 'ajaxCheckActiveProducts action response is correct'
        response.status == 200
        model.suppliers != null
    }

    //-------------------------------ajax search products function Unit tests----------------------------//

    void "should retrieve product search results"() {
        given:
        params.searchTerm = "TestTerm"
        controller.productService = Stub(ProductService) {
            searchProductsHql(_, _, _, _, _, _) >> [products: new ArrayList<>()]
        }

        controller.springSecurityService = getFakeSpringSecurityService()

        when: 'ajaxSearchProducts action is executed'
        controller.ajaxSearchProducts()

        then: 'ajaxSearchProducts action response is correct'
        response.status == HttpStatus.OK.value()
        model.products != null
        model.storeId != null
        session.PRODUCT_SEARCH_TERM == "TestTerm"
    }

    //-------------------------------ajax select variant function Unit tests----------------------------//

    void "should be able to show product variants by product and supplier id"() {
        given:
        params.productId = "100"
        params.supplierId = "100"

        Supplier testSupplier = new Supplier(name: "Test Supplier", retailerId: 9, storeId: 100)
        testSupplier.setId(100)
        testSupplier.setSymbolGroup(symbolGroup)

        controller.supplierService = Stub(SupplierService) {
            getSupplier(_) >> testSupplier
        }

        Product testProduct = new Product()
        ProductVariant testVariant = new ProductVariant(product: testProduct, effectiveDate: DateTime.now())
        Pack testPack = new Pack()

        testPack.setSupplier(testSupplier)
        testVariant.packs.add(testPack)
        testProduct.variants.add(testVariant)

        controller.productService = Stub(ProductService) {
            getProduct(_) >> testProduct
        }

        controller.springSecurityService = getFakeSpringSecurityService()

        when: 'ajaxSelectVariant action is executed'
        controller.ajaxSelectVariant()

        then: 'ajaxSelectVariant action response is correct'
        response.status == HttpStatus.OK.value()
        model.product != null
        model.variants != null
        !model.variants.isEmpty()

        where:
        ID | symbolGroup
        1  | new SymbolGroup()
        1  | null
    }

    //-------------------------------save pack lines function Unit tests----------------------------//

    void "should save pack lines successfully and redirect to product list view"() {
        given:
        PackLineRequestCommand packLineRequestCommand = new PackLineRequestCommand()

        controller.orderService = Stub(OrderService) {}

        when: 'ajaxSavePackLines action is executed'
        controller.ajaxSavePackLines(packLineRequestCommand)

        then: 'ajaxSavePackLines action response is correct'
        response.status == HttpStatus.FOUND.value()
        response.redirectUrl == "/order/productList"
    }

    void "should handle error when saving pack lines and return error response"() {
        given:
        PackLineRequestCommand packLineRequestCommand = new PackLineRequestCommand()

        when: 'ajaxSavePackLines action is executed'
        controller.ajaxSavePackLines(packLineRequestCommand)

        then: 'ajaxSavePackLines action response is correct'
        response.status == HttpStatus.INTERNAL_SERVER_ERROR.value()
    }

    //-------------------------------confirm order function Unit tests----------------------------//

    void "should confirm order successfully and return order response"() {
        given:
        params.supplierId = "100"
        params.productListId = "100"
        Supplier testSupplier = new Supplier(name: "Test Supplier", retailerId: 9, storeId: 100)
        testSupplier.setId(100)
        controller.supplierService = Stub(SupplierService) {
            getSupplier(_) >> testSupplier
        }
        controller.orderService = Stub(OrderService) {
            confirmOrder(_,_) >> new Object()
        }

        when: 'confirmOrder action is executed'
        controller.confirmOrder()

        then: 'confirmOrder action response is correct'
        response.status == HttpStatus.OK.value()
        model.orderResponse != null
    }

    void "should handle error when confirm order and return error response"() {
        given:

        when: 'confirmOrder action is executed'
        controller.confirmOrder()

        then: 'confirmOrder action response is correct'
        response.status == HttpStatus.INTERNAL_SERVER_ERROR.value()
    }

    //-------------------------------delete order function Unit tests----------------------------//

    void "should delete order successfully and redirect to product list page"() {
        given:
        params.productListId = "100"
        controller.orderService = Stub(OrderService) {}

        when: 'deleteOrder action is executed'
        controller.deleteOrder()

        then: 'deleteOrder action response is correct'
        response.status == HttpStatus.FOUND.value()
        response.redirectUrl == "/order/productList"
    }

    void "should handle error when delete order and return error response"() {
        given:

        when: 'deleteOrder action is executed'
        controller.deleteOrder()

        then: 'deleteOrder action response is correct'
        response.status == HttpStatus.INTERNAL_SERVER_ERROR.value()
    }

    //-------------------------------add product function Unit tests----------------------------//

    void "should return product search view on ajaxAddProduct"() {
        given:

        when: 'ajaxAddProduct action is executed'
        controller.ajaxAddProduct()

        then: 'ajaxAddProduct action response is correct'
        response.status == HttpStatus.OK.value()
        view == "/order/productSearch"
        model == [:]
    }

    private uk.co.wonderlane.wlpos.entities.wlim.ProductListItem getMockProductListItem(int id) {
        uk.co.wonderlane.wlpos.entities.wlim.ProductListItem productListItem = new uk.co.wonderlane.wlpos.entities.wlim.ProductListItem()

        productListItem.setId(id)
        productListItem.setProductVariantId(id)
        productListItem.setProductQuantityInStock(10)
        productListItem.setFillQuantity(10)

        uk.co.wonderlane.wlpos.entities.wlim.PackLine packLine = new uk.co.wonderlane.wlpos.entities.wlim.PackLine()
        packLine.setOrderCode("100")
        packLine.setQuantity(BigDecimal.ONE)
        productListItem.setPackLines(new ArrayList<>())
        productListItem.getPackLines().add(packLine)

        return productListItem
    }

    private Pack getMockPack(int id, Supplier supplier) {
        Pack pack = new Pack()

        pack.setId(id)
        pack.setQuantity(1)
        pack.setOrderCode(String.valueOf(id))
        pack.setSupplier(supplier)
        return pack
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
