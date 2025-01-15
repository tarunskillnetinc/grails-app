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

    void "should retrieve tags with search criteria"() {
        given:
        service.springSecurityService = getFakeSpringSecurityService()
        ProductGroupProduct tagProduct = new ProductGroupProduct(sku: 100)
        Set<ProductGroupProduct> tagProducts = new HashSet<>()
        tagProducts.add(tagProduct)
        ProductGroup testTag = new ProductGroup()
        testTag.setId(1)
        testTag.setTagProducts(tagProducts)
        testTag.setRetailerId(9)
        testTag.setHidden(false)
        testTag.setDescription("test prefixsearchkeywordsuffix other text")
        tagProduct.setTag(testTag)
        testTag.save()

        when: 'getTags action is executed'
        def serviceResponse = service.getTags(searchKeyword)

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
        ProductGroupProduct tagProduct = new ProductGroupProduct(sku: 100)
        Set<ProductGroupProduct> tagProducts = new HashSet<>()
        tagProducts.add(tagProduct)
        ProductGroup testTag = new ProductGroup()
        testTag.setId(1)
        testTag.setTagProducts(tagProducts)
        testTag.setRetailerId(100)
        testTag.setHidden(false)
        testTag.setDescription("test prefixsearchkeywordsuffix other text")
        tagProduct.setTag(testTag)
        testTag.save()

        when: 'getTags action is executed'
        def serviceResponse = service.getTags(searchKeyword)

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

    void "should retrieve tag with tag id"() {
        given:
        service.springSecurityService = getFakeSpringSecurityService()

        ProductGroup testTag = new ProductGroup(retailerId: 9)
        testTag.setDescription("test description")

        testTag.setId(100)

        mockDomain(ProductGroup, [testTag])

        when: 'getTag action is executed'
        ProductGroup serviceResponse = service.getTag(100)

        then: 'getTag action response is correct'
        serviceResponse != null
    }

    void "should retrieve empty results if tag for the tag id not exists"() {
        given:
        service.springSecurityService = getFakeSpringSecurityService()

        ProductGroup testTag = new ProductGroup(retailerId: 9)
        testTag.setDescription("test description")
        testTag.setId(100)

        mockDomain(ProductGroup, [testTag])

        when: 'getTag action is executed'
        ProductGroup serviceResponse = service.getTag(105)

        then: 'getTag action response is correct'
        serviceResponse == null
    }

    //-------------------------------saveTag function Unit tests----------------------------//

    void "should save tag correctly"() {
        given:
        ProductGroup testTag = new ProductGroup(retailerId: 9)
        testTag.setId(100)
        testTag.setDescription("test description")

        when: 'getTag action is executed'
        ProductGroup serviceResponse = service.saveTag(testTag)

        then: 'getTag action response is correct'
        serviceResponse
        serviceResponse.getId() == 100
    }

    //-------------------------------deleteTagProduct function Unit tests----------------------------//

    void "should delete tag product correctly"() {
        given:
        ProductGroup testTag = new ProductGroup(retailerId: 9)
        testTag.setId(100)
        testTag.setDescription("test description")
        ProductGroupProduct testTagProduct = new ProductGroupProduct()
        testTagProduct.setId(100)
        testTagProduct.setTag(testTag)

        testTag.save(flush: true, failOnError: true)
        testTagProduct.save(flush: true, failOnError: true)

        when: 'deleteTagProduct action is executed'
        ProductGroupProduct serviceResponse = service.deleteTagProduct(testTagProduct)

        then: 'deleteTagProduct action response is correct'
        !serviceResponse
    }

    //------------------------deleteTagProduct by Id and sku function Unit tests-----------------------//

    void "should delete tag product by id correctly"() {
        given:

        // mock the ProductGroupProduct.executeUpdate method since current GROM version doesn't support Hibernate queries
        ProductGroupProduct.metaClass.static.executeUpdate = { CharSequence ch, Map map -> return 1 }

        when: 'deleteTagProduct action is executed'
        int serviceResponse = service.deleteTagProduct(200, 150)

        then: 'deleteTagProduct action response is correct'
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
