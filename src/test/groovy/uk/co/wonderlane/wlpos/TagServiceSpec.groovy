package uk.co.wonderlane.wlpos

import grails.plugin.springsecurity.SpringSecurityService
import grails.testing.gorm.DataTest
import grails.testing.services.ServiceUnitTest
import spock.lang.Specification

class TagServiceSpec extends Specification implements ServiceUnitTest<TagService>, DataTest {

    Class<?>[] getDomainClassesToMock() {
        [TagProduct, Tag] as Class<?>[]
    }

    //-------------------------------getTags function Unit tests----------------------------//

    void "should retrieve tags with search criteria"() {
        given:
        service.springSecurityService = getFakeSpringSecurityService()
        TagProduct tagProduct = new TagProduct(sku: 100)
        Set<TagProduct> tagProducts = new HashSet<>()
        tagProducts.add(tagProduct)
        Tag testTag = new Tag()
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
        TagProduct tagProduct = new TagProduct(sku: 100)
        Set<TagProduct> tagProducts = new HashSet<>()
        tagProducts.add(tagProduct)
        Tag testTag = new Tag()
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

        Tag testTag = new Tag(retailerId: 9)
        testTag.setDescription("test description")

        testTag.setId(100)

        mockDomain(Tag, [testTag])

        when: 'getTag action is executed'
        Tag serviceResponse = service.getTag(100)

        then: 'getTag action response is correct'
        serviceResponse != null
    }

    void "should retrieve empty results if tag for the tag id not exists"() {
        given:
        service.springSecurityService = getFakeSpringSecurityService()

        Tag testTag = new Tag(retailerId: 9)
        testTag.setDescription("test description")
        testTag.setId(100)

        mockDomain(Tag, [testTag])

        when: 'getTag action is executed'
        Tag serviceResponse = service.getTag(105)

        then: 'getTag action response is correct'
        serviceResponse == null
    }

    //-------------------------------saveTag function Unit tests----------------------------//

    void "should save tag correctly"() {
        given:
        Tag testTag = new Tag(retailerId: 9)
        testTag.setId(100)
        testTag.setDescription("test description")

        when: 'getTag action is executed'
        Tag serviceResponse = service.saveTag(testTag)

        then: 'getTag action response is correct'
        serviceResponse
        serviceResponse.getId() == 100
    }

    //-------------------------------deleteTagProduct function Unit tests----------------------------//

    void "should delete tag product correctly"() {
        given:
        Tag testTag = new Tag(retailerId: 9)
        testTag.setId(100)
        testTag.setDescription("test description")
        TagProduct testTagProduct = new TagProduct()
        testTagProduct.setId(100)
        testTagProduct.setTag(testTag)

        testTag.save(flush: true, failOnError: true)
        testTagProduct.save(flush: true, failOnError: true)

        when: 'deleteTagProduct action is executed'
        TagProduct serviceResponse = service.deleteTagProduct(testTagProduct)

        then: 'deleteTagProduct action response is correct'
        !serviceResponse
    }

    //------------------------deleteTagProduct by Id and sku function Unit tests-----------------------//

    void "should delete tag product by id correctly"() {
        given:

        // mock the TagProduct.executeUpdate method since current GROM version doesn't support Hibernate queries
        TagProduct.metaClass.static.executeUpdate = { CharSequence ch, Map map -> return 1 }

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
