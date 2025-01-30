package uk.co.wonderlane.wlpos

import grails.plugin.springsecurity.SpringSecurityService
import grails.testing.gorm.DataTest
import grails.testing.services.ServiceUnitTest
import org.grails.datastore.mapping.query.api.BuildableCriteria
import org.joda.time.DateTime
import spock.lang.Specification
import uk.co.wonderlane.wlpos.enums.ProductStatus
import uk.co.wonderlane.wlpos.enums.PromotionGroupType
import uk.co.wonderlane.wlpos.enums.PromotionType
import uk.co.wonderlane.wlpos.helpers.HibernateTestMockCriteria

class PromotionServiceSpec extends Specification implements ServiceUnitTest<PromotionService>, DataTest {

    Class<?>[] getDomainClassesToMock() {
        [Promotion, Product, Category, ProductVariant, ProductGroup] as Class<?>[]
    }

    //-------------------------------savePromotion function Unit tests----------------------------//

    void "should save promotion"() {
        given:
        Promotion testPromotion = new Promotion(retailerId: 9, description: "Test",
                receiptDescription: "Test", startDate: DateTime.now(), endDate: DateTime.now(),
                type: PromotionType.FIXED_PRICE, amount: BigDecimal.TEN, updateDatetime: DateTime.now())

        when: 'savePromotion action is executed'
        Promotion response = service.savePromotion(testPromotion)

        then: 'savePromotion action response is correct'
        response != null
        !response.hasErrors()
    }

    //-------------------------------getPromotion function Unit tests----------------------------//

    void "should retrieve promotion by id"() {
        given:
        Promotion testPromotion = new Promotion(retailerId: 9, description: "Test",
                receiptDescription: "Test", startDate: DateTime.now(), endDate: DateTime.now(),
                type: PromotionType.FIXED_PRICE, amount: BigDecimal.TEN, updateDatetime: DateTime.now())
        testPromotion.setId(100)

        testPromotion.save(flush: true, failOnError: true)

        service.springSecurityService = getFakeSpringSecurityService()

        when: 'getPromotion action is executed'
        def response = service.getPromotion(100)

        then: 'getPromotion action response is correct'
        response != null
    }

    //-------------------------------getPromotionsForProduct function Unit tests----------------------------//

    void "should retrieve promotion by product id"() {
        given:
        Promotion testPromotion = new Promotion(retailerId: 9, description: "Test",
                receiptDescription: "Test", startDate: DateTime.now(), endDate: DateTime.now(),
                type: PromotionType.FIXED_PRICE, amount: BigDecimal.TEN, updateDatetime: DateTime.now())
        testPromotion.setId(100)

        PromotionGroup promotionGroupSkuMatch = new PromotionGroup(type: PromotionGroupType.OFFER, sku: 250,
                categoryId: null, productGroupId: 150, requiredQuantity: 10, requiredValue: 10, promotion: testPromotion)
        testPromotion.groups.add(promotionGroupSkuMatch)

        PromotionGroup promotionGroupCategoryMatch = new PromotionGroup(type: PromotionGroupType.REQUIRED, sku: null,
                categoryId: 100, productGroupId: null, requiredQuantity: 10, requiredValue: 10, promotion: testPromotion)
        testPromotion.groups.add(promotionGroupCategoryMatch)

        PromotionGroup promotionGroupProductGroupMatch = new PromotionGroup(type: PromotionGroupType.REQUIRED, sku: null,
                categoryId: null, productGroupId: 150, requiredQuantity: 10, requiredValue: 10, promotion: testPromotion)
        testPromotion.groups.add(promotionGroupProductGroupMatch)

        testPromotion.save(flush: true, failOnError: true)

        Category category = new Category(restrictions: new Restrictions(), description: "Test", shortDescription: "Test")
        category.setId(100)
        category.save(flush: true, failOnError: true)

        ProductGroup productGroup = new ProductGroup(name: "Test")
        productGroup.setId(150)
        ProductGroupProduct productGroupProduct = new ProductGroupProduct(sku: 250, productGroupId: productGroup)
        productGroupProduct.save(flush: true, failOnError: true)

        Product product = new Product(itemCode: "100", description: "Test", receiptDescription: "Test", retailerId: 9,
                sku: 100, unitSize: "10", vatCode: new VatCode(), status: ProductStatus.ACTIVE, category: category,
                restrictions: new Restrictions())
        product.setId(100)
        product.variants.add(new ProductVariant(product: product, effectiveDate: DateTime.now(), sku: 250))
        product.save(flush: true, failOnError: true)

        service.springSecurityService = getFakeSpringSecurityService()

        HibernateTestMockCriteria mockCriteria = new HibernateTestMockCriteria()
        mockCriteria.getResponses().add(testPromotion)
        BuildableCriteria defaultCriteria = Promotion.createCriteria() // keep the default behavior
        Promotion.metaClass.static.createCriteria = { return mockCriteria }

        when: 'getPromotionsForProduct action is executed'
        def response = service.getPromotionsForProduct(100)

        Promotion.metaClass.static.createCriteria = { return defaultCriteria } // set the default value back to the class

        then: 'getPromotionsForProduct action response is correct'
        response != null
    }

    void "should return null if product not found for id"() {
        given:
        service.springSecurityService = getFakeSpringSecurityService()

        when: 'getPromotionsForProduct action is executed'
        def response = service.getPromotionsForProduct(100)


        then: 'getPromotionsForProduct action response is correct'
        response == null
    }

    //-------------------------------searchPromotions function Unit tests----------------------------//

    void "should search promotions"() {
        given:
        HibernateTestMockCriteria mockCriteria = new HibernateTestMockCriteria()
        BuildableCriteria defaultCriteria = Promotion.createCriteria() // keep the default behavior
        Promotion.metaClass.static.createCriteria = { return mockCriteria }

        service.springSecurityService = getFakeSpringSecurityService()

        when: 'getPromotionsForProduct action is executed'
        def response = service.searchPromotions(validDate, updatedSince, promoType, searchTerm,
                descriptionSearch, max, offset, sortColumn, sortOrder, supplierId, status)

        Promotion.metaClass.static.createCriteria = { return defaultCriteria } // set the default value back to the class

        then: 'getPromotionsForProduct action response is correct'
        response != null

        where:
        ID | validDate      | updatedSince   | promoType           | searchTerm | descriptionSearch | max  | offset | sortColumn    | sortOrder | supplierId | status
        1  | DateTime.now() | DateTime.now() | PromotionType.BOGOF | "Test"     | false             | 100  | 0      | "other"       | "asc"     | null       | "active"
        2  | DateTime.now() | DateTime.now() | PromotionType.BOGOF | "Test"     | true              | 100  | 0      | "other"       | "asc"     | null       | "active"
        3  | DateTime.now() | DateTime.now() | PromotionType.BOGOF | null       | true              | 100  | 0      | "other"       | "asc"     | null       | "active"
        4  | DateTime.now() | DateTime.now() | PromotionType.BOGOF | ""         | true              | 100  | 0      | "other"       | "asc"     | null       | "active"
        5  | DateTime.now() | DateTime.now() | PromotionType.BOGOF | "Test"     | true              | 100  | 0      | "description" | "desc"    | null       | "active"
        6  | DateTime.now() | DateTime.now() | PromotionType.BOGOF | "Test"     | true              | 100  | 0      | "description" | "desc"    | -1         | "active"
        7  | DateTime.now() | DateTime.now() | null                | "Test"     | true              | 100  | 0      | "description" | "desc"    | -1         | "active"
        8  | null           | null           | null                | null       | true              | 100  | 0      | "description" | "desc"    | -1         | ""
        9  | null           | null           | null                | null       | true              | null | null   | "description" | "desc"    | -1         | null
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
