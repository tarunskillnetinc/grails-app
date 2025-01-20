package uk.co.wonderlane.wlpos

import grails.plugin.springsecurity.SpringSecurityService
import grails.testing.gorm.DataTest
import grails.testing.services.ServiceUnitTest
import spock.lang.Specification

class ProductGroupServiceSpec extends Specification implements ServiceUnitTest<ProductGroupService>, DataTest {

    Class<?>[] getDomainClassesToMock() {
        [ProductGroupProduct, ProductGroup] as Class<?>[]
    }

    //-------------------------------getTags function Unit tests----------------------------//

    void "should retrieve product groups with search criteria"() {
        given:
        service.springSecurityService = getFakeSpringSecurityService()
        ProductGroupProduct productGroupProduct = new ProductGroupProduct(sku: 100)
        Set<ProductGroupProduct> productGroupProducts = new HashSet<>()
        productGroupProducts.add(productGroupProduct)
        ProductGroup productGroup = new ProductGroup()
        productGroup.setId(1)
        productGroup.setProductGroupProducts(productGroupProducts)
        productGroup.setRetailerId(9)
        productGroup.setHidden(false)
        productGroup.setDescription("test prefixsearchkeywordsuffix other text")
        productGroupProduct.setProductGroupId(productGroup)
        productGroup.save()

        when: 'getTags action is executed'
        def serviceResponse = service.getProductGroups(searchKeyword)

        then: 'getTags action response is correct'
        serviceResponse != null
        serviceResponse.size() > 0

        where:
        ID | searchKeyword
        0  | "search"
        1  | "searchkeyword"
        2  | "prefixsearchkeyword"
        3  | "searchkeywordsuffix"
        4  | null
    }

    void "should retrieve empty results with search criteria for incorrect retailer ID"() {
        given:
        service.springSecurityService = getFakeSpringSecurityService()
        ProductGroupProduct productGroupProduct = new ProductGroupProduct(sku: 100)
        Set<ProductGroupProduct> productGroupProducts = new HashSet<>()
        productGroupProducts.add(productGroupProduct)
        ProductGroup productGroup = new ProductGroup()
        productGroup.setId(1)
        productGroup.setProductGroupProducts(productGroupProducts)
        productGroup.setRetailerId(100)
        productGroup.setHidden(false)
        productGroup.setDescription("test prefixsearchkeywordsuffix other text")
        productGroupProduct.setProductGroupId(productGroup)
        productGroup.save()

        when: 'getTags action is executed'
        def serviceResponse = service.getProductGroups(searchKeyword)

        then: 'getTags action response is correct'
        serviceResponse != null
        serviceResponse.size() == 0

        where:
        ID | searchKeyword
        0  | "search"
        1  | "searchkeyword"
        2  | "prefixsearchkeyword"
        3  | "searchkeywordsuffix"
        4  | null

    }

    //-------------------------------getTag function Unit tests----------------------------//

    void "should retrieve productGroup with productGroup id"() {
        given:
        service.springSecurityService = getFakeSpringSecurityService()

        ProductGroup testTag = new ProductGroup(retailerId: 9)
        testTag.setDescription("test description")

        testTag.setId(100)

        mockDomain(ProductGroup, [testTag])

        when: 'getTag action is executed'
        ProductGroup serviceResponse = service.getProductGroup(100)

        then: 'getTag action response is correct'
        serviceResponse != null
    }

    void "should retrieve empty results if productGroup for the productGroup id not exists"() {
        given:
        service.springSecurityService = getFakeSpringSecurityService()

        ProductGroup testTag = new ProductGroup(retailerId: 9)
        testTag.setDescription("test description")
        testTag.setId(100)

        mockDomain(ProductGroup, [testTag])

        when: 'getTag action is executed'
        ProductGroup serviceResponse = service.getProductGroup(105)

        then: 'getTag action response is correct'
        serviceResponse == null
    }

    //-------------------------------saveTag function Unit tests----------------------------//

    void "should save productGroup correctly"() {
        given:
        ProductGroup testTag = new ProductGroup(retailerId: 9)
        testTag.setId(100)
        testTag.setDescription("test description")

        when: 'getTag action is executed'
        ProductGroup serviceResponse = service.saveProductGroup(testTag)

        then: 'getTag action response is correct'
        serviceResponse
        serviceResponse.getId() == 100
    }

    //-------------------------------deleteProductGroupProduct function Unit tests----------------------------//

    void "should delete productGroup product correctly"() {
        given:
        ProductGroup productGroup = new ProductGroup(retailerId: 9)
        productGroup.setId(100)
        productGroup.setDescription("test description")
        ProductGroupProduct productGroupProduct = new ProductGroupProduct()
        productGroupProduct.setId(100)
        productGroupProduct.setProductGroupId(productGroup)

        productGroup.save(flush: true, failOnError: true)
        productGroupProduct.save(flush: true, failOnError: true)

        when: 'deleteProductGroupProduct action is executed'
        ProductGroupProduct serviceResponse = service.deleteProductGroupProduct(productGroupProduct)

        then: 'deleteProductGroupProduct action response is correct'
        !serviceResponse
    }

    //------------------------deleteProductGroupProduct by Id and sku function Unit tests-----------------------//

    void "should delete productGroup product by id correctly"() {
        given:

        // mock the ProductGroupProduct.executeUpdate method since current GROM version doesn't support Hibernate queries
        ProductGroupProduct.metaClass.static.executeUpdate = { CharSequence ch, Map map -> return 1 }

        when: 'deleteProductGroupProduct action is executed'
        int serviceResponse = service.deleteProductGroupProduct(200, 150)

        then: 'deleteProductGroupProduct action response is correct'
        serviceResponse == 1
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
