package uk.co.wonderlane.wlpos

import grails.plugin.springsecurity.SpringSecurityService
import grails.testing.gorm.DataTest
import grails.testing.web.controllers.ControllerUnitTest
import org.springframework.http.HttpStatus
import spock.lang.Specification

class ProductGroupControllerSpec extends Specification implements ControllerUnitTest<ProductGroupController>, DataTest {

    Class<?>[] getDomainClassesToMock() {
        [ProductGroupProduct, ProductGroup] as Class<?>[]
    }

    def setup() {
    }

    def cleanup() {
    }

    //-------------------------------index function Unit tests----------------------------//

    void "should retrieve productGroups on index"() {
        given:
        List<ProductGroup> productGroups = new ArrayList<>()
        productGroups.add(new ProductGroup(id: 1))
        productGroups.add(new ProductGroup(id: 2))
        controller.productGroupService = Stub(ProductGroupService) {
            getProductGroups() >> productGroups
        }

        when: 'index action is executed'
        def controllerResponse = controller.index()

        then: 'index action response is correct'
        response.status == HttpStatus.OK.value()
        controllerResponse.productGroups != null
    }

    //-------------------------------show function Unit tests----------------------------//

    void "should flash error and redirect to index page if Product Group not found on show action"() {
        given:
        controller.productGroupService = Stub(ProductGroupService) {
            getProductGroup(_) >> null
        }

        when: 'show action is executed'
        def controllerResponse = controller.show(1)

        then: 'show action response is correct'
        response.status == HttpStatus.FOUND.value()
        response.redirectUrl == "/productGroup/index"
        controller.flash.error == "Product Group not found."
        controllerResponse == null
    }

    void "should return the productgroup with product variant id and product description on show action"() {
        given:
        ProductGroupProduct productGroupProduct = new ProductGroupProduct(sku: 100)
        productGroupProduct.save()
        controller.productGroupService = Stub(ProductGroupService) {
            getProductGroup(_) >> new ProductGroup(id: 1, productGroupProducts: Set.of(productGroupProduct))
        }

        controller.productService = Stub(ProductService) {
            getProductVariants(_) >> products
        }

        when: 'show action is executed'
        def controllerResponse = controller.show(1)

        then: 'show action response is correct'
        response.status == HttpStatus.OK.value()
        controllerResponse.productGroup != null
        controllerResponse.productGroup.productGroupProducts[0] != null

        where:
        ID | products
        1  | null
        2  | new ArrayList<>()
        2  | List.of(new ProductVariant(sku: 200))
        2  | List.of(new ProductVariant(sku: 100))
    }

    //-------------------------------ajaxGetProductGroups function Unit tests----------------------------//

    class FakeProductGroupSearchResultList<T> extends ArrayList<T> {
        public int totalCount = 0
    }

    void "should return productgroups when ajaxGetProductGroups action called"() {
        given:
        FakeProductGroupSearchResultList<ProductGroup> productGroups = new FakeProductGroupSearchResultList<>()
        ProductGroupProduct productGroupProduct = new ProductGroupProduct(sku: 100)
        productGroupProduct.save()
        productGroups.add(new ProductGroup(id: 1, productGroupProducts: Set.of(productGroupProduct)))
        productGroups.properties.put("totalCount", 1)
        controller.productGroupService = Stub(ProductGroupService) {
            getProductGroups(_) >> productGroups
        }

        when: 'ajaxGetProductGroups action is executed'
        controller.ajaxGetProductGroups("search term", "everything")

        then: 'ajaxGetProductGroups action response is correct'
        response.status == HttpStatus.OK.value()
        model != null
        model.productGroups != null
        model.searchTerm != null
    }

    //-------------------------------add function Unit tests----------------------------//

    void "should do nothing when add action called"() {
        given:

        when: 'add action is executed'
        controller.add()

        then: 'add action response is correct'
        response.status == HttpStatus.OK.value()
    }

    //-------------------------------edit function Unit tests----------------------------//

    void "should flash error and redirect to index page if Product Group not found on edit action"() {
        given:
        controller.productGroupService = Stub(ProductGroupService) {
            getProductGroup(_) >> null
        }

        when: 'edit action is executed'
        def controllerResponse = controller.edit(1)

        then: 'edit action response is correct'
        response.status == HttpStatus.FOUND.value()
        response.redirectUrl == "/productGroup/index"
        controller.flash.error == "Product Group not found."
        controllerResponse == null
    }

    void "should return the productgroups with product variant id and product description on edit action"() {
        given:
        ProductGroupProduct productGroupProduct = new ProductGroupProduct(sku: 100)
        productGroupProduct.save()
        controller.productGroupService = Stub(ProductGroupService) {
            getProductGroup(_) >> new ProductGroup(id: 1, productGroupProducts: Set.of(productGroupProduct))
        }

        controller.productService = Stub(ProductService) {
            getProductVariants(_) >> products
        }

        when: 'edit action is executed'
        controller.edit(1)

        then: 'edit action response is correct'
        response.status == HttpStatus.OK.value()
        view == "/productGroup/add"
        model.productGroup.productGroupProducts[0] != null

        where:
        ID | products
        1  | null
        2  | new ArrayList<>()
        2  | List.of(new ProductVariant(sku: 200))
        2  | List.of(new ProductVariant(sku: 100))
    }

    //-------------------------------ajaxAddProduct function Unit tests----------------------------//

    void "should do nothing when ajaxAddProduct action called"() {
        given:

        when: 'ajaxAddProduct action is executed'
        controller.ajaxAddProduct(0, 0, "Test Description")

        then: 'ajaxAddProduct action response is correct'
        response.status == HttpStatus.OK.value()
        model.productGroupProduct != null
    }

    //-------------------------------save function Unit tests----------------------------//

    void "should flash error when productgroup is not found"() {
        given:
        controller.productGroupService = Stub(ProductGroupService) {
            getProductGroup(_) >> null
        }

        when: 'save action is executed'
        SaveProductGroupCommand saveProductGroupCommand = new SaveProductGroupCommand()
        saveProductGroupCommand.setId(100)
        controller.save(saveProductGroupCommand)

        then: 'save action response is correct'
        response.status == HttpStatus.OK.value()
        controller.flash.error == "Product Group not found."
    }

    void "save ProductGroups correctly on valid input and valid product group details - happy path"() {
        given:
        ProductGroupProduct productGroupProduct = new ProductGroupProduct(sku: 100)
        ProductGroupProduct productGroupProductNotIncl = new ProductGroupProduct(sku: 300)
        Set<ProductGroupProduct> productGroupProducts = new HashSet<>()
        productGroupProducts.add(productGroupProduct)
        productGroupProducts.add(productGroupProductNotIncl)
        ProductGroup testProductGroup = new ProductGroup(productGroupProducts: productGroupProducts)
        testProductGroup.setId(1)
        productGroupProduct.setProductGroupId(testProductGroup)
        productGroupProductNotIncl.setProductGroupId(testProductGroup)
        testProductGroup.save()
        controller.productGroupService = Stub(ProductGroupService) {
            getProductGroup(_) >> testProductGroup
        }

        controller.rabbitService = Stub(BackOfficeRabbitService) {
            isOpen() >> true
        }

        controller.springSecurityService = getFakeSpringSecurityService()

        when: 'save action is executed'
        SaveProductGroupCommand saveProductGroupCommand = new SaveProductGroupCommand()
        saveProductGroupCommand.setId(100)
        Long[] skus = new Long[2]
        skus[0] = 100
        skus[1] = 200
        saveProductGroupCommand.setSku(skus)
        saveProductGroupCommand.setDescription("Test Command")
        controller.save(saveProductGroupCommand)

        then: 'save action response is correct'
        response.status == HttpStatus.FOUND.value()
        controller.flash.message == "Product Group saved successfully."
    }

    void "create new productgroup and save productgroups correctly on valid input and valid productgroup details - happy path"() {
        given:
        controller.productGroupService = Stub(ProductGroupService) {}

        controller.rabbitService = Stub(BackOfficeRabbitService) {
            isOpen() >> true
        }

        controller.springSecurityService = getFakeSpringSecurityService()

        when: 'save action is executed'
        SaveProductGroupCommand saveProductGroupCommand = new SaveProductGroupCommand()
        Long[] skus = new Long[0]
        saveProductGroupCommand.setSku(skus)
        saveProductGroupCommand.setDescription("Test Command")
        controller.save(saveProductGroupCommand)

        then: 'save action response is correct'
        response.status == HttpStatus.FOUND.value()
        controller.flash.message == "Product Group saved successfully."
    }

    void "save ProductGroups correctly on valid input and valid product group and rabbit service error - partial error path"() {
        given:
        ProductGroupProduct productGroupProduct = new ProductGroupProduct(sku: 100)
        Set<ProductGroupProduct> productGroupProducts = new HashSet<>()
        productGroupProducts.add(productGroupProduct)
        ProductGroup testProductGroup = new ProductGroup(productGroupProducts: productGroupProducts)
        testProductGroup.setId(1)
        productGroupProduct.setProductGroupId(testProductGroup)
        testProductGroup.save()
        controller.productGroupService = Stub(ProductGroupService) {
            getProductGroup(_) >> testProductGroup
        }

        controller.rabbitService = Stub(BackOfficeRabbitService) {
            isOpen() >> false
        }

        controller.springSecurityService = getFakeSpringSecurityService()

        when: 'save action is executed'
        SaveProductGroupCommand saveProductGroupCommand = new SaveProductGroupCommand()
        Long[] skus = new Long[1]
        skus[0] = 100
        saveProductGroupCommand.setSku(skus)
        saveProductGroupCommand.setDescription("Test Command")
        controller.save(saveProductGroupCommand)

        then: 'save action response is correct'
        response.status == HttpStatus.FOUND.value()
        controller.flash.message == "Product Group saved successfully."
    }

    void "return validation error messages on validation error is found - error path"() {
        given:
        ProductGroupProduct productGroupProduct = new ProductGroupProduct(sku: 100)
        Set<ProductGroupProduct> productGroupProducts = new HashSet<>()
        productGroupProducts.add(productGroupProduct)
        ProductGroup testProductGroup = new ProductGroup()
        testProductGroup.setId(1)
        testProductGroup.setProductGroupProducts(productGroupProducts)
        productGroupProduct.setProductGroupId(testProductGroup)
        testProductGroup.save()
        controller.productGroupService = Stub(ProductGroupService) {
            getProductGroup(_) >> testProductGroup
        }

        controller.rabbitService = Stub(BackOfficeRabbitService) {
            isOpen() >> false
        }


        controller.productService = Stub(ProductService) {
            getProductVariants(_) >> products
        }

        controller.springSecurityService = getFakeSpringSecurityService()

        when: 'save action is executed'
        SaveProductGroupCommand saveProductGroupCommand = new SaveProductGroupCommand()
        saveProductGroupCommand.setId(100)
        Long[] skus = new Long[1]
        skus[0] = 100
        saveProductGroupCommand.setSku(skus)
        controller.save(saveProductGroupCommand)

        then: 'save action response is correct'
        response.status == HttpStatus.OK.value()

        where:
        ID | products
        1  | null
        2  | new ArrayList<>()
        2  | List.of(new ProductVariant(sku: 200))
        2  | List.of(new ProductVariant(sku: 100))

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
