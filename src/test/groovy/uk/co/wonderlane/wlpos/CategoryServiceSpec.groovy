package uk.co.wonderlane.wlpos

import grails.plugin.springsecurity.SpringSecurityService
import grails.testing.gorm.DataTest
import grails.testing.services.ServiceUnitTest
import spock.lang.Specification

class CategoryServiceSpec extends Specification implements ServiceUnitTest<CategoryService>, DataTest {

    Class<?>[] getDomainClassesToMock() {
        [Category] as Class<?>[]
    }

    //-------------------------------getCategory by id function Unit tests----------------------------//

    void "should retrieve category by id"() {
        given:
        service.springSecurityService = getFakeSpringSecurityService()

        Category category = new Category(restrictions: new Restrictions(), description: "Test", shortDescription: "Test")
        category.setId(100)
        category.setRetailerId(9)
        category.save(flush: true, failOnError: true)

        when: 'getCategory action is executed'
        def serviceResponse = service.getCategory(100)

        then: 'getCategory action response is correct'
        serviceResponse != null
        serviceResponse instanceof Category
    }

    //-------------------------------getTopLevelCategories function Unit tests----------------------------//

    void "should retrieve category hierarchy"() {
        given:
        service.springSecurityService = getFakeSpringSecurityService()

        Category category1 = new Category(restrictions: new Restrictions(), description: "Test 1", shortDescription: "Test")
        category1.setId(100)
        category1.setRetailerId(9)
        category1.save(flush: true, failOnError: true)

        Category category2 = new Category(restrictions: new Restrictions(), description: "Test 2", shortDescription: "Test")
        category2.setId(120)
        category2.setRetailerId(9)
        category2.save(flush: true, failOnError: true)

        when: 'getTopLevelCategories action is executed'
        def serviceResponse = service.getTopLevelCategories()

        then: 'getTopLevelCategories action response is correct'
        serviceResponse != null
        serviceResponse instanceof List
        serviceResponse.size() == 2
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
