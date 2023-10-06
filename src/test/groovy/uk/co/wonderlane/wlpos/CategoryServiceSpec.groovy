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

    void "should save a category and restriction to simulate category creation"() {
        given:
        service.springSecurityService = getFakeSpringSecurityService()
        var category = createCategory()

        when: 'Category + Restrictions are saved'
        service.saveRestriction(category.restrictions)
        service.saveCategory(category)

        then: 'Check that we can find the result'
        def categories = Category.findById(2)

        assert categories != null
        assert categories.first().id == 2
        assert categories.first().restrictions.id == 1

        def restrictions = Restrictions.findById(1)

        assert restrictions != null
        assert restrictions.first().id == 1
    }

    void "should delete a category to simulate category deletion"() {
        given:
        service.springSecurityService = getFakeSpringSecurityService()
        var category = createCategory()
        service.saveCategory(category)

        when: 'Category is deleted'
        service.deleteCategory(category)

        then: 'Check that we cannot find the result'
        def categories = Category.findById(2)

        assert categories == null
    }

    private Category createCategory() {
        var category = new Category()
        category.id = 2
        category.description = "TestDescription"
        category.shortDescription = "TestShortDescription"
        category.retailerCategoryCode = "1234"
        category.retailerId = service.springSecurityService.principal.retailerId
        category.restrictions = createRestrictions()
        return category
    }

    private Restrictions createRestrictions() {
        var restrictions = new Restrictions()
        restrictions.id = 1
        restrictions.minOpenPrice = 0.01
        restrictions.maxOpenPrice = 999.99
        restrictions.buyerIdRequired = true
        restrictions.buyerIdForced = true
        restrictions.buyerAgeRestriction = 18
        restrictions.buyerChallengeAge = 18
        restrictions.sellerAgeRestriction = 18
        restrictions.refundAllowed = true
        restrictions.markdownAllowed = true
        restrictions.discountAllowed = true
        restrictions.creditPaymentAllowed = true
        restrictions.quantityChangeAllowed = true
        restrictions.quantityChangeForced = true
        restrictions.receiptPrintForced = true
        return restrictions
    }

}
