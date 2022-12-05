package uk.co.wonderlane.wlpos

import grails.plugin.springsecurity.SpringSecurityService
import grails.testing.gorm.DataTest
import grails.testing.web.controllers.ControllerUnitTest
import org.joda.time.DateTime
import org.springframework.http.HttpStatus
import spock.lang.Specification
import uk.co.wonderlane.wlpos.enums.ProductStatus
import uk.co.wonderlane.wlpos.enums.PromotionGroupType
import uk.co.wonderlane.wlpos.enums.PromotionType
import uk.co.wonderlane.wlpos.supplier.SymbolGroup

class PromotionControllerSpec extends Specification implements ControllerUnitTest<PromotionController>, DataTest {

    Class<?>[] getDomainClassesToMock() {
        [SymbolGroup, Promotion, Category, Product, Tag, TagProduct, PromotionGroup] as Class<?>[]
    }

    //-------------------------------index function Unit tests----------------------------//

    void "should retrieve promotion types and symbol groups on index page - index"() {
        given:
        SymbolGroup testSymbolGroup = new SymbolGroup()
        testSymbolGroup.setName("Test Group")
        testSymbolGroup.setApiUrl("http://fake.fakedomain.fke/sg")

        testSymbolGroup.save(flush: true, failOnError: true)

        when: 'index action is executed'
        controller.index()

        then: 'index action response is correct'
        response.status == HttpStatus.OK.value()
        model.types != null
        model.symbolGroups != null
        List.of(PromotionType.BOGOF, PromotionType.FIXED_AMOUNT_DISCOUNT, PromotionType.PERCENTAGE_DISCOUNT, PromotionType.X_FOR_Y,
                PromotionType.FIXED_PRICE).containsAll(model.types)
        model.symbolGroups.size() > 0
    }

    //-------------------------------maintenance function Unit tests----------------------------//

    void "should retrieve promotion on maintenance page"() {
        given:
        Promotion testPromotion = new Promotion()
        testPromotion.setRetailerId(9)
        testPromotion.setDescription("TEST PROMO")
        testPromotion.setReceiptDescription("TEST PROMO")
        testPromotion.setStartDate(DateTime.now())
        testPromotion.setEndDate(DateTime.now())
        testPromotion.setType(PromotionType.FIXED_PRICE)
        testPromotion.setAmount(BigDecimal.TEN)
        testPromotion.setUpdateDatetime(DateTime.now())

        testPromotion.save(flush: true, failOnError: true)

        when: 'maintenance action is executed'
        controller.maintenance()

        then: 'maintenance action response is correct'
        response.status == HttpStatus.OK.value()
        model.promotion != null
    }

    void "should retrieve promotion with required products on maintenance page"() {
        given:
        Promotion testPromotion = new Promotion(retailerId: 9, description: "Test",
                receiptDescription: "Test", startDate: DateTime.now(), endDate: DateTime.now(),
                type: PromotionType.FIXED_PRICE, amount: BigDecimal.TEN, updateDatetime: DateTime.now())

        PromotionGroup promotionGroup = new PromotionGroup(type: PromotionGroupType.REQUIRED, sku: sku,
                categoryId: categoryId, tagId: tagId, requiredQuantity: 10, requiredValue: 10, promotion: testPromotion)
        testPromotion.groups.add(promotionGroup)
        testPromotion.save(flush: true, failOnError: true)

        Map principal = new HashMap()
        principal.put("storeId", 100)
        principal.put("storeNumber", 100)
        principal.put("retailerId", retailerId)
        controller.springSecurityService = Stub(SpringSecurityService) {
            getPrincipal() >> principal
        }


        controller.productService = Stub(ProductService) {
            getProductVariant(_) >> new ProductVariant(product: new Product())
        }

        when: 'maintenance action is executed'
        controller.maintenance()

        then: 'maintenance action response is correct'
        response.status == HttpStatus.OK.value()
        model.promotion != null
        model.productsRequired != null
        model.productsRequired.size() == 1
        model.productItemType == "product"

        where:
        ID | sku | categoryId | tagId | retailerId
        1  | 100 | null       | null  | 9
        1  | 100 | null       | null  | 10
    }

    void "should retrieve promotion with offer products on maintenance page"() {
        given:
        Promotion testPromotion = new Promotion(retailerId: 9, description: "Test",
                receiptDescription: "Test", startDate: DateTime.now(), endDate: DateTime.now(),
                type: PromotionType.FIXED_PRICE, amount: BigDecimal.TEN, updateDatetime: DateTime.now())

        PromotionGroup promotionGroup = new PromotionGroup(type: PromotionGroupType.OFFER, sku: sku,
                categoryId: categoryId, tagId: tagId, requiredQuantity: 10, requiredValue: 10, promotion: testPromotion)
        testPromotion.groups.add(promotionGroup)
        testPromotion.save(flush: true, failOnError: true)

        Map principal = new HashMap()
        principal.put("storeId", 100)
        principal.put("storeNumber", 100)
        principal.put("retailerId", retailerId)
        controller.springSecurityService = Stub(SpringSecurityService) {
            getPrincipal() >> principal
        }


        controller.productService = Stub(ProductService) {
            getProductVariant(_) >> new ProductVariant(product: new Product())
        }

        when: 'maintenance action is executed'
        controller.maintenance()

        then: 'maintenance action response is correct'
        response.status == HttpStatus.OK.value()
        model.promotion != null
        model.productsOffer != null
        model.productsOffer.size() == 1
        model.productItemType == "product"

        where:
        ID | sku | categoryId | tagId | retailerId
        1  | 100 | null       | null  | 9
        1  | 100 | null       | null  | 10
    }

    void "should retrieve promotion with required categories on maintenance page"() {
        given:
        Promotion testPromotion = new Promotion(retailerId: 9, description: "Test",
                receiptDescription: "Test", startDate: DateTime.now(), endDate: DateTime.now(),
                type: PromotionType.FIXED_PRICE, amount: BigDecimal.TEN, updateDatetime: DateTime.now())

        PromotionGroup promotionGroup = new PromotionGroup(type: PromotionGroupType.REQUIRED, sku: sku,
                categoryId: categoryId, tagId: tagId, requiredQuantity: 10, requiredValue: 10, promotion: testPromotion)
        testPromotion.groups.add(promotionGroup)
        testPromotion.save(flush: true, failOnError: true)

        Category category = new Category(restrictions: new Restrictions(), description: "Test", shortDescription: "Test")
        category.setId(100)
        category.save(flush: true, failOnError: true)

        Map principal = new HashMap()
        principal.put("storeId", 100)
        principal.put("storeNumber", 100)
        principal.put("retailerId", retailerId)
        controller.springSecurityService = Stub(SpringSecurityService) {
            getPrincipal() >> principal
        }

        when: 'maintenance action is executed'
        controller.maintenance()

        then: 'maintenance action response is correct'
        response.status == HttpStatus.OK.value()
        model.promotion != null
        model.categoriesRequired != null
        model.categoriesRequired.size() == 1
        model.productItemType == "category"

        where:
        ID | sku  | categoryId | tagId | retailerId
        2  | null | 100        | null  | 9
        2  | null | 100        | null  | 10
    }

    void "should retrieve promotion with offer categories on maintenance page"() {
        given:
        Promotion testPromotion = new Promotion(retailerId: 9, description: "Test",
                receiptDescription: "Test", startDate: DateTime.now(), endDate: DateTime.now(),
                type: PromotionType.FIXED_PRICE, amount: BigDecimal.TEN, updateDatetime: DateTime.now())

        PromotionGroup promotionGroup = new PromotionGroup(type: PromotionGroupType.OFFER, sku: sku,
                categoryId: categoryId, tagId: tagId, requiredQuantity: 10, requiredValue: 10, promotion: testPromotion)
        testPromotion.groups.add(promotionGroup)
        testPromotion.save(flush: true, failOnError: true)

        Category category = new Category(restrictions: new Restrictions(), description: "Test", shortDescription: "Test")
        category.setId(100)
        category.save(flush: true, failOnError: true)

        Map principal = new HashMap()
        principal.put("storeId", 100)
        principal.put("storeNumber", 100)
        principal.put("retailerId", retailerId)
        controller.springSecurityService = Stub(SpringSecurityService) {
            getPrincipal() >> principal
        }

        when: 'maintenance action is executed'
        controller.maintenance()

        then: 'maintenance action response is correct'
        response.status == HttpStatus.OK.value()
        model.promotion != null
        model.categoriesOffer != null
        model.categoriesOffer.size() == 1
        model.productItemType == "category"

        where:
        ID | sku  | categoryId | tagId | retailerId
        2  | null | 100        | null  | 9
        2  | null | 100        | null  | 10
    }

    void "should retrieve promotion with required tags on maintenance page"() {
        given:
        Promotion testPromotion = new Promotion(retailerId: 9, description: "Test",
                receiptDescription: "Test", startDate: DateTime.now(), endDate: DateTime.now(),
                type: PromotionType.FIXED_PRICE, amount: BigDecimal.TEN, updateDatetime: DateTime.now())

        PromotionGroup promotionGroup = new PromotionGroup(type: PromotionGroupType.REQUIRED, sku: sku,
                categoryId: categoryId, tagId: tagId, requiredQuantity: 10, requiredValue: 10, promotion: testPromotion)
        testPromotion.groups.add(promotionGroup)
        testPromotion.save(flush: true, failOnError: true)

        Category category = new Category(restrictions: new Restrictions(), description: "Test", shortDescription: "Test")
        category.setId(100)
        category.save(flush: true, failOnError: true)

        Product product = new Product(itemCode: "100", description: "Test", receiptDescription: "Test", retailerId: 9,
                sku: 100, unitSize: "10", vatCode: new VatCode(), status: ProductStatus.ACTIVE, category: category,
                restrictions: new Restrictions())
        product.variants.add(new ProductVariant(product: product, effectiveDate: DateTime.now()))
        product.save(flush: true, failOnError: true)

        Tag testTag = new Tag(description: "Test")
        testTag.setId(150)

        TagProduct tagProduct = new TagProduct(sku: 100, tag: testTag)
        tagProduct.save(flush: true, failOnError: true)

        testTag.tagProducts.add(tagProduct)
        testTag.save(flush: true, failOnError: true)

        Map principal = new HashMap()
        principal.put("storeId", 100)
        principal.put("storeNumber", 100)
        principal.put("retailerId", retailerId)
        controller.springSecurityService = Stub(SpringSecurityService) {
            getPrincipal() >> principal
        }

        when: 'maintenance action is executed'
        controller.maintenance()

        then: 'maintenance action response is correct'
        response.status == HttpStatus.OK.value()
        model.promotion != null
        model.tagsRequired != null
        model.tagsRequired.size() == 1
        model.productItemType == "tag"

        where:
        ID | sku  | categoryId | tagId | retailerId
        3  | null | null       | 150   | 9
        3  | null | null       | 150   | 10

    }

    void "should retrieve promotion with offer tags on maintenance page"() {
        given:
        Promotion testPromotion = new Promotion(retailerId: 9, description: "Test",
                receiptDescription: "Test", startDate: DateTime.now(), endDate: DateTime.now(),
                type: PromotionType.FIXED_PRICE, amount: BigDecimal.TEN, updateDatetime: DateTime.now())

        PromotionGroup promotionGroup = new PromotionGroup(type: PromotionGroupType.OFFER, sku: sku,
                categoryId: categoryId, tagId: tagId, requiredQuantity: 10, requiredValue: 10, promotion: testPromotion)
        testPromotion.groups.add(promotionGroup)
        testPromotion.save(flush: true, failOnError: true)

        Category category = new Category(restrictions: new Restrictions(), description: "Test", shortDescription: "Test")
        category.setId(100)
        category.save(flush: true, failOnError: true)

        Product product = new Product(itemCode: "100", description: "Test", receiptDescription: "Test", retailerId: 9,
                sku: 100, unitSize: "10", vatCode: new VatCode(), status: ProductStatus.ACTIVE, category: category,
                restrictions: new Restrictions())
        product.variants.add(new ProductVariant(product: product, effectiveDate: DateTime.now()))
        product.save(flush: true, failOnError: true)

        Tag testTag = new Tag(description: "Test")
        testTag.setId(150)

        TagProduct tagProduct = new TagProduct(sku: 100, tag: testTag)
        tagProduct.save(flush: true, failOnError: true)

        testTag.tagProducts.add(tagProduct)
        testTag.save(flush: true, failOnError: true)

        Map principal = new HashMap()
        principal.put("storeId", 100)
        principal.put("storeNumber", 100)
        principal.put("retailerId", retailerId)
        controller.springSecurityService = Stub(SpringSecurityService) {
            getPrincipal() >> principal
        }

        when: 'maintenance action is executed'
        controller.maintenance()

        then: 'maintenance action response is correct'
        response.status == HttpStatus.OK.value()
        model.promotion != null
        model.tagsOffer != null
        model.tagsOffer.size() == 1
        model.productItemType == "tag"

        where:
        ID | sku  | categoryId | tagId | retailerId
        3  | null | null       | 150   | 9
        3  | null | null       | 150   | 10
    }

    //-------------------------------maintenanceError function Unit tests----------------------------//

    void "should retrieve new promotion on maintenanceError page if flash does not have promotion"() {
        given:

        when: 'maintenance action is executed'
        controller.maintenanceError()

        then: 'maintenance action response is correct'
        response.status == HttpStatus.OK.value()
        model.promotion != null
        view == "/promotion/maintenance"
    }

    void "should retrieve promotion with required products on maintenanceError page"() {
        given:
        Promotion testPromotion = new Promotion(retailerId: 9, description: "Test",
                receiptDescription: "Test", startDate: DateTime.now(), endDate: DateTime.now(),
                type: PromotionType.FIXED_PRICE, amount: BigDecimal.TEN, updateDatetime: DateTime.now())

        PromotionGroup promotionGroup = new PromotionGroup(type: PromotionGroupType.REQUIRED, sku: 100,
                categoryId: null, tagId: null, requiredQuantity: 10, requiredValue: 10, promotion: testPromotion)
        testPromotion.groups.add(promotionGroup)
        testPromotion.save(flush: true, failOnError: true)

        controller.flash.promotion = testPromotion

        Map principal = new HashMap()
        principal.put("storeId", 100)
        principal.put("storeNumber", 100)
        principal.put("retailerId", 9)
        controller.springSecurityService = Stub(SpringSecurityService) {
            getPrincipal() >> principal
        }


        controller.productService = Stub(ProductService) {
            getProductVariant(_) >> new ProductVariant(product: new Product())
        }

        when: 'maintenanceError action is executed'
        controller.maintenanceError()

        then: 'maintenanceError action response is correct'
        response.status == HttpStatus.OK.value()
        model.promotion != null
        model.productsRequired != null
        model.productsRequired.size() == 1
        view == "/promotion/maintenance"
    }

    void "should retrieve promotion with offer products on maintenanceError page"() {
        given:
        Promotion testPromotion = new Promotion(retailerId: 9, description: "Test",
                receiptDescription: "Test", startDate: DateTime.now(), endDate: DateTime.now(),
                type: PromotionType.FIXED_PRICE, amount: BigDecimal.TEN, updateDatetime: DateTime.now())

        PromotionGroup promotionGroup = new PromotionGroup(type: PromotionGroupType.OFFER, sku: 100,
                categoryId: null, tagId: null, requiredQuantity: 10, requiredValue: 10, promotion: testPromotion)
        testPromotion.groups.add(promotionGroup)
        testPromotion.save(flush: true, failOnError: true)

        controller.flash.promotion = testPromotion

        Map principal = new HashMap()
        principal.put("storeId", 100)
        principal.put("storeNumber", 100)
        principal.put("retailerId", 9)
        controller.springSecurityService = Stub(SpringSecurityService) {
            getPrincipal() >> principal
        }


        controller.productService = Stub(ProductService) {
            getProductVariant(_) >> new ProductVariant(product: new Product())
        }

        when: 'maintenanceError action is executed'
        controller.maintenanceError()

        then: 'maintenanceError action response is correct'
        response.status == HttpStatus.OK.value()
        model.promotion != null
        model.productsOffer != null
        model.productsOffer.size() == 1
        view == "/promotion/maintenance"
    }

    void "should retrieve promotion with required categories on maintenanceError page"() {
        given:
        Promotion testPromotion = new Promotion(retailerId: 9, description: "Test",
                receiptDescription: "Test", startDate: DateTime.now(), endDate: DateTime.now(),
                type: PromotionType.FIXED_PRICE, amount: BigDecimal.TEN, updateDatetime: DateTime.now())

        PromotionGroup promotionGroup = new PromotionGroup(type: PromotionGroupType.REQUIRED, sku: null,
                categoryId: 100, tagId: null, requiredQuantity: 10, requiredValue: 10, promotion: testPromotion)
        testPromotion.groups.add(promotionGroup)
        testPromotion.save(flush: true, failOnError: true)

        Category category = new Category(restrictions: new Restrictions(), description: "Test", shortDescription: "Test")
        category.setId(100)
        category.save(flush: true, failOnError: true)

        controller.flash.promotion = testPromotion

        Map principal = new HashMap()
        principal.put("storeId", 100)
        principal.put("storeNumber", 100)
        principal.put("retailerId", 9)
        controller.springSecurityService = Stub(SpringSecurityService) {
            getPrincipal() >> principal
        }

        when: 'maintenanceError action is executed'
        controller.maintenanceError()

        then: 'maintenanceError action response is correct'
        response.status == HttpStatus.OK.value()
        model.promotion != null
        model.categoriesRequired != null
        model.categoriesRequired.size() == 1
        view == "/promotion/maintenance"
    }

    void "should retrieve promotion with offer categories on maintenanceError page"() {
        given:
        Promotion testPromotion = new Promotion(retailerId: 9, description: "Test",
                receiptDescription: "Test", startDate: DateTime.now(), endDate: DateTime.now(),
                type: PromotionType.FIXED_PRICE, amount: BigDecimal.TEN, updateDatetime: DateTime.now())

        PromotionGroup promotionGroup = new PromotionGroup(type: PromotionGroupType.OFFER, sku: null,
                categoryId: 100, tagId: null, requiredQuantity: 10, requiredValue: 10, promotion: testPromotion)
        testPromotion.groups.add(promotionGroup)
        testPromotion.save(flush: true, failOnError: true)

        Category category = new Category(restrictions: new Restrictions(), description: "Test", shortDescription: "Test")
        category.setId(100)
        category.save(flush: true, failOnError: true)

        controller.flash.promotion = testPromotion

        Map principal = new HashMap()
        principal.put("storeId", 100)
        principal.put("storeNumber", 100)
        principal.put("retailerId", 9)
        controller.springSecurityService = Stub(SpringSecurityService) {
            getPrincipal() >> principal
        }

        when: 'maintenanceError action is executed'
        controller.maintenanceError()

        then: 'maintenanceError action response is correct'
        response.status == HttpStatus.OK.value()
        model.promotion != null
        model.categoriesOffer != null
        model.categoriesOffer.size() == 1
        view == "/promotion/maintenance"
    }

    void "should retrieve promotion with required tags on maintenanceError page"() {
        given:
        Promotion testPromotion = new Promotion(retailerId: 9, description: "Test",
                receiptDescription: "Test", startDate: DateTime.now(), endDate: DateTime.now(),
                type: PromotionType.FIXED_PRICE, amount: BigDecimal.TEN, updateDatetime: DateTime.now())

        PromotionGroup promotionGroup = new PromotionGroup(type: PromotionGroupType.REQUIRED, sku: null,
                categoryId: null, tagId: 150, requiredQuantity: 10, requiredValue: 10, promotion: testPromotion)
        testPromotion.groups.add(promotionGroup)
        testPromotion.save(flush: true, failOnError: true)

        Category category = new Category(restrictions: new Restrictions(), description: "Test", shortDescription: "Test")
        category.setId(100)
        category.save(flush: true, failOnError: true)

        Product product = new Product(itemCode: "100", description: "Test", receiptDescription: "Test", retailerId: 9,
                sku: 100, unitSize: "10", vatCode: new VatCode(), status: ProductStatus.ACTIVE, category: category,
                restrictions: new Restrictions())
        product.variants.add(new ProductVariant(product: product, effectiveDate: DateTime.now()))
        product.save(flush: true, failOnError: true)

        controller.flash.promotion = testPromotion

        Tag testTag = new Tag(description: "Test")
        testTag.setId(150)

        TagProduct tagProduct = new TagProduct(sku: 100, tag: testTag)
        tagProduct.save(flush: true, failOnError: true)

        testTag.tagProducts.add(tagProduct)
        testTag.save(flush: true, failOnError: true)

        Map principal = new HashMap()
        principal.put("storeId", 100)
        principal.put("storeNumber", 100)
        principal.put("retailerId", 9)
        controller.springSecurityService = Stub(SpringSecurityService) {
            getPrincipal() >> principal
        }

        when: 'maintenanceError action is executed'
        controller.maintenanceError()

        then: 'maintenanceError action response is correct'
        response.status == HttpStatus.OK.value()
        model.promotion != null
        model.tagsRequired != null
        model.tagsRequired.size() == 1
        view == "/promotion/maintenance"
    }

    void "should retrieve promotion with offer tags on maintenanceError page"() {
        given:
        Promotion testPromotion = new Promotion(retailerId: 9, description: "Test",
                receiptDescription: "Test", startDate: DateTime.now(), endDate: DateTime.now(),
                type: PromotionType.FIXED_PRICE, amount: BigDecimal.TEN, updateDatetime: DateTime.now())

        PromotionGroup promotionGroup = new PromotionGroup(type: PromotionGroupType.OFFER, sku: null,
                categoryId: null, tagId: 150, requiredQuantity: 10, requiredValue: 10, promotion: testPromotion)
        testPromotion.groups.add(promotionGroup)
        testPromotion.save(flush: true, failOnError: true)

        Category category = new Category(restrictions: new Restrictions(), description: "Test", shortDescription: "Test")
        category.setId(100)
        category.save(flush: true, failOnError: true)

        Product product = new Product(itemCode: "100", description: "Test", receiptDescription: "Test", retailerId: 9,
                sku: 100, unitSize: "10", vatCode: new VatCode(), status: ProductStatus.ACTIVE, category: category,
                restrictions: new Restrictions())
        product.variants.add(new ProductVariant(product: product, effectiveDate: DateTime.now()))
        product.save(flush: true, failOnError: true)

        controller.flash.promotion = testPromotion

        Tag testTag = new Tag(description: "Test")
        testTag.setId(150)

        TagProduct tagProduct = new TagProduct(sku: 100, tag: testTag)
        tagProduct.save(flush: true, failOnError: true)

        testTag.tagProducts.add(tagProduct)
        testTag.save(flush: true, failOnError: true)

        Map principal = new HashMap()
        principal.put("storeId", 100)
        principal.put("storeNumber", 100)
        principal.put("retailerId", 9)
        controller.springSecurityService = Stub(SpringSecurityService) {
            getPrincipal() >> principal
        }

        when: 'maintenanceError action is executed'
        controller.maintenanceError()

        then: 'maintenanceError action response is correct'
        response.status == HttpStatus.OK.value()
        model.promotion != null
        model.tagsOffer != null
        model.tagsOffer.size() == 1
        view == "/promotion/maintenance"
    }

    //-------------------------------add function Unit tests----------------------------//

    void "should retrieve new promotion page on add"() {
        given:

        when: 'add action is executed'
        controller.add()

        then: 'add action response is correct'
        response.status == HttpStatus.OK.value()
        model.promotion == null
        model.productsRequired.isEmpty()
        model.productsOffer.isEmpty()
        model.categoriesRequired.isEmpty()
        model.categoriesOffer.isEmpty()
        model.tagsRequired.isEmpty()
        model.tagsOffer.isEmpty()
        view == "/promotion/maintenance"
    }

    //-------------------------------setupBasePromotion function Unit tests----------------------------//

    void "should retrieve new promotion page on setupBasePromotion"() {
        given:
        params."${promoType}-description" = "Test"
        params."${promoType}-receiptDescription" = "Test"
        params."${promoType}-startDate" = "Wednesday 30 November 2022"
        params."${promoType}-endDate" = "Sunday 02 January 2023"
        params."${promoType}-active" = true
        params."${promoType}-retailerPromoId" = "100"
        params."percentage-amount" = 100
        params."fixedAmount-amount" = 100
        params."fixedPrice-amount" = 100
        Promotion testPromotion = new Promotion(retailerId: 9, description: "Test",
                receiptDescription: "Test", startDate: DateTime.now(), endDate: DateTime.now(),
                amount: BigDecimal.TEN, updateDatetime: DateTime.now())

        when: 'setupBasePromotion action is executed'
        Promotion resultPromotion = controller.setupBasePromotion(testPromotion, promoType)

        then: 'setupBasePromotion action response is correct'
        response.status == HttpStatus.OK.value()
        resultPromotion != null
        resultPromotion.amount != null
        resultPromotion.type != null
        resultPromotion.description != null
        resultPromotion.receiptDescription != null
        resultPromotion.startDate != null
        resultPromotion.endDate != null
        resultPromotion.updateDatetime != null
        resultPromotion.active
        resultPromotion.retailerPromotionId != null

        where:
        ID | promoType
        1  | "bogof"
        2  | "xfory"
        3  | "percentage"
        4  | "fixedAmount"
        5  | "fixedPrice"

    }

    void "should retrieve new promotion page on add with minimal data"() {
        given:
        params."${promoType}-description" = "Test"
        params."${promoType}-receiptDescription" = "Test"
        params."${promoType}-startDate" = "Wednesday 30 November 2022"
        params."${promoType}-doesNotExpire" = true
        params."percentage-amount" = 100
        params."fixedAmount-amount" = 100
        params."fixedPrice-amount" = 100
        Promotion testPromotion = new Promotion(retailerId: 9, description: "Test",
                receiptDescription: "Test", startDate: DateTime.now(), endDate: DateTime.now(),
                amount: BigDecimal.TEN, updateDatetime: DateTime.now())

        when: 'setupBasePromotion action is executed'
        Promotion resultPromotion = controller.setupBasePromotion(testPromotion, promoType)

        then: 'setupBasePromotion action response is correct'
        response.status == HttpStatus.OK.value()
        resultPromotion != null
        resultPromotion.amount != null
        resultPromotion.type != null
        resultPromotion.description != null
        resultPromotion.receiptDescription != null
        resultPromotion.startDate != null
        resultPromotion.endDate == null
        resultPromotion.updateDatetime != null
        !resultPromotion.active
        resultPromotion.retailerPromotionId == null

        where:
        ID | promoType
        1  | "bogof"
        2  | "xfory"
        3  | "percentage"
        4  | "fixedAmount"
        5  | "fixedPrice"
    }

    //-------------------------------save function Unit tests----------------------------//

    void "should save new bogof promotion"() {
        given:
        params.promotionType = "bogof"
        params.promotionId = ""
        params."bogof-description" = "Test"
        params."bogof-receiptDescription" = "Test"
        params."bogof-startDate" = "Wednesday 30 November 2022"
        params."bogof-endDate" = "Sunday 02 January 2023"
        params."bogof-active" = true
        params."bogof-doesNotExpire" = false
        params."bogof-product-required-1-sku" = 100
        params."bogof-category-required-1-categoryId" = 1
        params."bogof-tag-required-1-tagId" = 1

        Map principal = new HashMap()
        principal.put("storeId", 100)
        principal.put("storeNumber", 100)
        principal.put("retailerId", 9)
        controller.springSecurityService = Stub(SpringSecurityService) {
            getPrincipal() >> principal
        }

        controller.promotionService = Stub(PromotionService) {}

        when: 'save action is executed'
        controller.save()

        then: 'save action response is correct'
        response.status == HttpStatus.FOUND.value()
        response.redirectUrl == "/promotion/sendToTill?promotionId=0"
    }

    void "should save existing bogof promotion"() {
        given:
        params.promotionType = "bogof"
        params.promotionId = "100"
        params."bogof-description" = "Test"
        params."bogof-receiptDescription" = "Test"
        params."bogof-startDate" = "Wednesday 30 November 2022"
        params."bogof-endDate" = "Sunday 02 January 2023"
        params."bogof-active" = true
        params."bogof-doesNotExpire" = false
        params."bogof-product-required-1-sku" = 100
        params."bogof-category-required-1-categoryId" = 1
        params."bogof-tag-required-1-tagId" = 1

        Promotion testPromotion = new Promotion(retailerId: 9, description: "Test",
                receiptDescription: "Test", startDate: DateTime.now(), endDate: DateTime.now(),
                type: PromotionType.BOGOF, amount: BigDecimal.TEN, updateDatetime: DateTime.now())
        testPromotion.setId(100)
        testPromotion.save(flush: true, failOnError: true)

        Map principal = new HashMap()
        principal.put("storeId", 100)
        principal.put("storeNumber", 100)
        principal.put("retailerId", 9)
        controller.springSecurityService = Stub(SpringSecurityService) {
            getPrincipal() >> principal
        }

        controller.promotionService = Stub(PromotionService) {}

        when: 'save action is executed'
        controller.save()

        then: 'save action response is correct'
        response.status == HttpStatus.FOUND.value()
        response.redirectUrl == "/promotion/sendToTill?promotionId=100"
    }

    void "should return maintenance error page on invalid promotion"() {
        given:
        params.promotionType = "bogof"
        params.promotionId = "100"
        params."bogof-description" = "Test"
        params."bogof-receiptDescription" = "Test"
        params."bogof-startDate" = "Wednesday 30 November 2022"
        params."bogof-active" = true
        params."bogof-doesNotExpire" = true
        params."bogof-product-required-1-sku" = 100
        params."bogof-category-required-1-categoryId" = 1
        params."bogof-tag-required-1-tagId" = 1

        Promotion testPromotion = new Promotion(retailerId: 9, description: "Test",
                receiptDescription: "Test", startDate: DateTime.now(), endDate: DateTime.now(),
                type: PromotionType.BOGOF, amount: BigDecimal.TEN, updateDatetime: DateTime.now())
        testPromotion.setId(100)
        testPromotion.save(flush: true, failOnError: true)

        Map principal = new HashMap()
        principal.put("storeId", 100)
        principal.put("storeNumber", 100)
        principal.put("retailerId", 9)
        controller.springSecurityService = Stub(SpringSecurityService) {
            getPrincipal() >> principal
        }

        controller.promotionService = Stub(PromotionService) {}

        when: 'save action is executed'
        controller.save()

        then: 'save action response is correct'
        response.status == HttpStatus.FOUND.value()
        response.redirectUrl == "/promotion/maintenanceError"
        flash.promotion != null
        flash.badPromoMessage == "error.Promotion.badPromoValidation"
    }

    void "should return maintenance error page on invalid new promotion"() {
        given:
        params.promotionType = "bogof"
        params.promotionId = "100"
        params."bogof-description" = "Test"
        params."bogof-receiptDescription" = "Test"
        params."bogof-startDate" = "Wednesday 30 November 2022"
        params."bogof-active" = true
        params."bogof-doesNotExpire" = true
        params."bogof-product-required-1-sku" = 100
        params."bogof-category-required-1-categoryId" = 1
        params."bogof-tag-required-1-tagId" = 1

        Map principal = new HashMap()
        principal.put("storeId", 100)
        principal.put("storeNumber", 100)
        principal.put("retailerId", 9)
        controller.springSecurityService = Stub(SpringSecurityService) {
            getPrincipal() >> principal
        }

        controller.promotionService = Stub(PromotionService) {}

        when: 'save action is executed'
        controller.save()

        then: 'save action response is correct'
        response.status == HttpStatus.FOUND.value()
        response.redirectUrl == "/promotion/maintenanceError"
        flash.promotion != null
        flash.badPromoMessage == "error.Promotion.badPromoValidation"
    }

    void "should save new xfory promotion"() {
        given:
        params.promotionType = "xfory"
        params.promotionId = ""
        params."xfory-description" = "Test"
        params."xfory-receiptDescription" = "Test"
        params."xfory-startDate" = "Wednesday 30 November 2022"
        params."xfory-endDate" = "Sunday 02 January 2023"
        params."xfory-active" = true
        params."xfory-doesNotExpire" = false
        params."xfory-product-required-1-sku" = 100
        params."xfory-category-required-1-categoryId" = 1
        params."xfory-tag-required-1-tagId" = 1
        params."xfory-promotionItemsType" = "100"
        params."xfory-${params.'xfory-promotionItemsType'}-required-1-quantity" = 100

        Map principal = new HashMap()
        principal.put("storeId", 100)
        principal.put("storeNumber", 100)
        principal.put("retailerId", 9)
        controller.springSecurityService = Stub(SpringSecurityService) {
            getPrincipal() >> principal
        }

        controller.promotionService = Stub(PromotionService) {}

        when: 'save action is executed'
        controller.save()

        then: 'save action response is correct'
        response.status == HttpStatus.FOUND.value()
        response.redirectUrl == "/promotion/sendToTill?promotionId=0"
    }

    void "should return maintenance error page on save new xfory promotion if params are invalid"() {
        given:
        params.promotionType = "xfory"
        params.promotionId = ""
        params."xfory-description" = "Test"
        params."xfory-receiptDescription" = "Test"
        params."xfory-startDate" = "Wednesday 30 November 2022"
        params."xfory-endDate" = "Sunday 02 January 2023"
        params."xfory-active" = true
        params."xfory-doesNotExpire" = true
        params."xfory-product-required-1-sku" = 100
        params."xfory-category-required-1-categoryId" = 1
        params."xfory-tag-required-1-tagId" = 1
        params."xfory-promotionItemsType" = "100"
        params."xfory-${params.'xfory-promotionItemsType'}-required-1-quantity" = -1 // invalid param

        Map principal = new HashMap()
        principal.put("storeId", 100)
        principal.put("storeNumber", 100)
        principal.put("retailerId", 9)
        controller.springSecurityService = Stub(SpringSecurityService) {
            getPrincipal() >> principal
        }

        controller.promotionService = Stub(PromotionService) {}

        when: 'save action is executed'
        controller.save()

        then: 'save action response is correct'
        response.status == HttpStatus.FOUND.value()
        response.redirectUrl == "/promotion/maintenanceError"
        flash.promotion != null
        flash.badPromoMessage == "error.Promotion.badPromoValidation"
    }

    void "should save new percentage promotion"() {
        given:
        params.promotionType = "percentage"
        params.promotionId = ""
        params."percentage-description" = "Test"
        params."percentage-receiptDescription" = "Test"
        params."percentage-startDate" = "Wednesday 30 November 2022"
        params."percentage-endDate" = "Sunday 02 January 2023"
        params."percentage-active" = true
        params."percentage-doesNotExpire" = false
        params."percentage-amount" = 100
        params."percentage-product-required-1-sku" = 100
        params."percentage-category-required-1-categoryId" = 1
        params."percentage-tag-required-1-tagId" = 1
        params."percentage-promotionItemsType" = "100"
        params."percentage-${params.'percentage-promotionItemsType'}-required-1-quantity" = 100

        Map principal = new HashMap()
        principal.put("storeId", 100)
        principal.put("storeNumber", 100)
        principal.put("retailerId", 9)
        controller.springSecurityService = Stub(SpringSecurityService) {
            getPrincipal() >> principal
        }

        controller.promotionService = Stub(PromotionService) {}

        when: 'save action is executed'
        controller.save()

        then: 'save action response is correct'
        response.status == HttpStatus.FOUND.value()
        response.redirectUrl == "/promotion/sendToTill?promotionId=0"
    }

    void "should return maintenance error page on save new percentage promotion if params are invalid"() {
        given:
        params.promotionType = "percentage"
        params.promotionId = ""
        params."percentage-description" = "Test"
        params."percentage-receiptDescription" = "Test"
        params."percentage-startDate" = "Wednesday 30 November 2022"
        params."percentage-endDate" = "Sunday 02 January 2023"
        params."percentage-active" = true
        params."percentage-doesNotExpire" = false
        params."percentage-amount" = 100
        params."percentage-product-required-1-sku" = 100
        params."percentage-category-required-1-categoryId" = 1
        params."percentage-tag-required-1-tagId" = 1
        params."percentage-promotionItemsType" = "100"
        params."percentage-${params.'percentage-promotionItemsType'}-required-1-quantity" = -1 // invalid param

        Map principal = new HashMap()
        principal.put("storeId", 100)
        principal.put("storeNumber", 100)
        principal.put("retailerId", 9)
        controller.springSecurityService = Stub(SpringSecurityService) {
            getPrincipal() >> principal
        }

        controller.promotionService = Stub(PromotionService) {}

        when: 'save action is executed'
        controller.save()

        then: 'save action response is correct'
        response.status == HttpStatus.FOUND.value()
        response.redirectUrl == "/promotion/maintenanceError"
        flash.promotion != null
        flash.badPromoMessage == "error.Promotion.badPromoValidation"
    }

    void "should save new fixedAmount promotion"() {
        given:
        params.promotionType = "fixedAmount"
        params.promotionId = ""
        params."fixedAmount-description" = "Test"
        params."fixedAmount-receiptDescription" = "Test"
        params."fixedAmount-startDate" = "Wednesday 30 November 2022"
        params."fixedAmount-endDate" = "Sunday 02 January 2023"
        params."fixedAmount-active" = true
        params."fixedAmount-doesNotExpire" = false
        params."fixedAmount-amount" = 100
        params."fixedAmount-product-required-1-sku" = 100
        params."fixedAmount-category-required-1-categoryId" = 1
        params."fixedAmount-tag-required-1-tagId" = 1
        params."fixedAmount-promotionItemsType" = "100"
        params."fixedAmount-${params.'fixedAmount-promotionItemsType'}-required-1-quantity" = 100
        params."fixedAmount-${params.'fixedAmount-promotionItemsType'}-required-1-value" = 100

        Map principal = new HashMap()
        principal.put("storeId", 100)
        principal.put("storeNumber", 100)
        principal.put("retailerId", 9)
        controller.springSecurityService = Stub(SpringSecurityService) {
            getPrincipal() >> principal
        }

        controller.promotionService = Stub(PromotionService) {}

        when: 'save action is executed'
        controller.save()

        then: 'save action response is correct'
        response.status == HttpStatus.FOUND.value()
        response.redirectUrl == "/promotion/sendToTill?promotionId=0"
    }

    void "should return maintenance error page on save new fixedAmount promotion if params are invalid"() {
        given:
        params.promotionType = "fixedAmount"
        params.promotionId = ""
        params."fixedAmount-description" = "Test"
        params."fixedAmount-receiptDescription" = "Test"
        params."fixedAmount-startDate" = "Wednesday 30 November 2022"
        params."fixedAmount-endDate" = "Sunday 02 January 2023"
        params."fixedAmount-active" = true
        params."fixedAmount-doesNotExpire" = false
        params."fixedAmount-amount" = 100
        params."fixedAmount-product-required-1-sku" = 100
        params."fixedAmount-category-required-1-categoryId" = 1
        params."fixedAmount-tag-required-1-tagId" = 1
        params."fixedAmount-promotionItemsType" = "100"
        params."fixedAmount-${params.'fixedAmount-promotionItemsType'}-required-1-quantity" = -1 // invalid param
        params."fixedAmount-${params.'fixedAmount-promotionItemsType'}-required-1-value" = 100


        Map principal = new HashMap()
        principal.put("storeId", 100)
        principal.put("storeNumber", 100)
        principal.put("retailerId", 9)
        controller.springSecurityService = Stub(SpringSecurityService) {
            getPrincipal() >> principal
        }

        controller.promotionService = Stub(PromotionService) {}

        when: 'save action is executed'
        controller.save()

        then: 'save action response is correct'
        response.status == HttpStatus.FOUND.value()
        response.redirectUrl == "/promotion/maintenanceError"
        flash.promotion != null
        flash.badPromoMessage == "error.Promotion.badPromoValidation"
    }

    void "should save new fixedPrice promotion"() {
        given:
        params.promotionType = "fixedPrice"
        params.promotionId = ""
        params."fixedPrice-description" = "Test"
        params."fixedPrice-receiptDescription" = "Test"
        params."fixedPrice-startDate" = "Wednesday 30 November 2022"
        params."fixedPrice-endDate" = "Sunday 02 January 2023"
        params."fixedPrice-active" = true
        params."fixedPrice-doesNotExpire" = false
        params."fixedPrice-amount" = 100
        params."fixedPrice-promotionItemsType" = promoItemsType

        // iteration 1
        params."fixedPrice-${params.'fixedPrice-promotionItemsType'}-required-0-sku" = 100
        params."fixedPrice-category-required-0-categoryId" = 1
        params."fixedPrice-tag-required-0-tagId" = 1
        params."fixedPrice-${params.'fixedPrice-promotionItemsType'}-required-0-quantity" = 100
        params."fixedPrice-${params.'fixedPrice-promotionItemsType'}-required-0-value" = 100

        // iteration 2
        params."fixedPrice-${params.'fixedPrice-promotionItemsType'}-required-2-sku" = 100
        params."fixedPrice-product-required-2-productId" = 100
        params."fixedPrice-category-required-2-categoryId" = 1
        params."fixedPrice-tag-required-2-tagId" = 1
        params."fixedPrice-${params.'fixedPrice-promotionItemsType'}-required-2-quantity" = 100
        params."fixedPrice-${params.'fixedPrice-promotionItemsType'}-required-2-value" = 100

        params."fixedPrice-product-required-1-sku" = ""
        params."fixedPrice-other-required-5-otherId" = ""
        params."fixedPrice-other-required-10-otherId" = ""

        params."fixedPrice-count-required" = "2"

        Map principal = new HashMap()
        principal.put("storeId", 100)
        principal.put("storeNumber", 100)
        principal.put("retailerId", 9)
        controller.springSecurityService = Stub(SpringSecurityService) {
            getPrincipal() >> principal
        }

        controller.promotionService = Stub(PromotionService) {}

        when: 'save action is executed'
        controller.save()

        then: 'save action response is correct'
        response.status == HttpStatus.FOUND.value()
        response.redirectUrl == "/promotion/sendToTill?promotionId=0"

        where:
        ID | promoItemsType
        1  | "product"
        2  | "other"
    }

    void "should return maintenance error page on save new fixedPrice promotion if params are invalid"() {
        given:
        params.promotionType = "fixedPrice"
        params.promotionId = ""
        params."fixedPrice-description" = "Test"
        params."fixedPrice-receiptDescription" = "Test"
        params."fixedPrice-startDate" = "Wednesday 30 November 2022"
        params."fixedPrice-endDate" = "Sunday 02 January 2023"
        params."fixedPrice-active" = true
        params."fixedPrice-doesNotExpire" = false
        params."fixedPrice-amount" = 100
        params."fixedPrice-promotionItemsType" = promoItemsType

        // iteration 1
        params."fixedPrice-${params.'fixedPrice-promotionItemsType'}-required-0-sku" = 100
        params."fixedPrice-category-required-0-categoryId" = 1
        params."fixedPrice-tag-required-0-tagId" = 1
        params."fixedPrice-${params.'fixedPrice-promotionItemsType'}-required-1-quantity" = -1 // invalid param
        params."fixedPrice-${params.'fixedPrice-promotionItemsType'}-required-1-value" = 100

        params."fixedPrice-count-required" = numberOfItems // test params includes zero for testing invalid

        params."fixedPrice-product-required-1-sku" = ""
        params."fixedPrice-other-required-1-otherId" = ""

        Map principal = new HashMap()
        principal.put("storeId", 100)
        principal.put("storeNumber", 100)
        principal.put("retailerId", 9)
        controller.springSecurityService = Stub(SpringSecurityService) {
            getPrincipal() >> principal
        }

        controller.promotionService = Stub(PromotionService) {}

        when: 'save action is executed'
        controller.save()

        then: 'save action response is correct'
        response.status == HttpStatus.FOUND.value()
        response.redirectUrl == "/promotion/maintenanceError"
        flash.promotion != null
        flash.badPromoMessage == "error.Promotion.badPromoValidation"

        where:
        ID | promoItemsType | numberOfItems
        1  | "product"      | "1"
        1  | "product"      | "0"
        2  | "other"        | "1"
        2  | "other"        | "0"
    }

    //-------------------------------productSearch function Unit tests----------------------------//

    class FakePromotionProductSearchResult {
        def products = new ArrayList()
        def totalCount = 0
    }

    void "should retrieve promotion product search results"() {
        given:

        controller.productService = Stub(ProductService) {
            searchProductsHql(_, _, _, _, _, _) >> new FakePromotionProductSearchResult()
        }

        controller.springSecurityService = getFakeSpringSecurityService()

        when: 'productSearch action is executed'
        controller.productSearch()

        then: 'productSearch action response is correct'
        response.status == HttpStatus.OK.value()
        model.products != null
        model.storeId != null
    }

    //-------------------------------categorySearch function Unit tests----------------------------//

    void "should retrieve category search results"() {
        given:
        controller.springSecurityService = getFakeSpringSecurityService()
        params.searchBy = searchBy
        params.searchTerm = "Test"

        Category category = new Category(restrictions: new Restrictions(), description: "prefixTestsuffix",
                shortDescription: "prefixTestsuffix", retailerCategoryCode: "prefixTestsuffix", retailerId: 9)
        category.setId(100)
        category.save(flush: true, failOnError: true)

        when: 'categorySearch action is executed'
        controller.categorySearch()

        then: 'categorySearch action response is correct'
        response.status == HttpStatus.OK.value()
        model.categories != null
        model.storeId != null
        model.totalResults == 1

        where:
        ID | searchBy
        1  | "description"
        2  | "other"
    }

    //-------------------------------tagSearch function Unit tests----------------------------//

    void "should retrieve tag search results"() {
        given:
        controller.springSecurityService = getFakeSpringSecurityService()
        params.searchTerm = "Test"

        Tag testTag = new Tag(description: "prefixTestsuffix", hidden: false)
        testTag.setId(150)
        testTag.save(flush: true, failOnError: true)

        when: 'tagSearch action is executed'
        controller.tagSearch()

        then: 'tagSearch action response is correct'
        response.status == HttpStatus.OK.value()
        model.tags != null
        model.storeId != null
        model.totalResults == 1
    }

    //-------------------------------promotionSearch function Unit tests----------------------------//

    class FakePromotionSearchResults extends ArrayList<Promotion> {
        Integer totalCount = 0;

        boolean add(Promotion promotion) {
            super.add(promotion)
            totalCount++
        }
    }

    void "should retrieve promotion search results"() {
        given:
        controller.springSecurityService = getFakeSpringSecurityService()
        params.validDate = "30/11/2022"
        params.updatedSince = "30/11/2022"
        params.type = "BOGOF"
        params.supplier = "1"
        params.max = "10"
        params.offset = "0"
        params.sortColumn = sortColumn
        params.sortOrder = sortOrder
        params.searchTerm = "Test"
        params.searchBy = "Test"
        params.status = "Active"

        Promotion promotion = new Promotion()
        FakePromotionSearchResults result = new FakePromotionSearchResults()
        result.add(promotion)
        controller.promotionService = Stub(PromotionService) {
            searchPromotions(_, _, _, _, _, _, _, _, _, _, _) >> result
        }

        Tag testTag = new Tag(description: "prefixTestsuffix", hidden: false)
        testTag.setId(150)
        testTag.save(flush: true, failOnError: true)

        def mockView = '<div">DUMMY HTML</div>'
        views['/promotion/_promotionSearchResults.gsp'] = mockView

        when: 'promotionSearch action is executed'
        controller.promotionSearch()

        then: 'promotionSearch action response is correct'
        response.status == HttpStatus.OK.value()
        model.promotions != null
        model.storeId != null
        model.totalResults == 1

        where:
        ID | sortOrder | sortColumn
        1  | "asc"     | "retailerPromotionId"
        2  | "desc"    | "description"
        3  | null      | "updateDatetime"
    }

    void "should retrieve promotion search results with supplier name sort"() {
        given:
        controller.springSecurityService = getFakeSpringSecurityService()
        params.validDate = "30/11/2022"
        params.updatedSince = "30/11/2022"
        params.type = "BOGOF"
        params.supplier = "1"
        params.max = "10"
        params.offset = "0"
        params.sortColumn = "supplierName"
        params.sortOrder = sortOrder
        params.searchTerm = "Test"
        params.searchBy = "Test"
        params.status = "Active"

        Promotion promotion1 = new Promotion()
        SymbolGroupPromotion symbolGroupPromotion1 = new SymbolGroupPromotion()
        symbolGroupPromotion1.setIsLeaflet(true)
        promotion1.setSymbolGroupPromotion(symbolGroupPromotion1)

        Promotion promotion2 = new Promotion()
        SymbolGroupPromotion symbolGroupPromotion2 = new SymbolGroupPromotion()
        symbolGroupPromotion2.setIsLeaflet(false)
        promotion2.setSymbolGroupPromotion(symbolGroupPromotion2)

        FakePromotionSearchResults result = new FakePromotionSearchResults()
        result.add(promotion1)
        result.add(promotion2)
        controller.promotionService = Stub(PromotionService) {
            searchPromotions(_, _, _, _, _, _, _, _, _, _, _) >> result
        }

        Tag testTag = new Tag(description: "prefixTestsuffix", hidden: false)
        testTag.setId(150)
        testTag.save(flush: true, failOnError: true)

        def mockView = '<div">DUMMY HTML</div>'
        views['/promotion/_promotionSearchResults.gsp'] = mockView

        when: 'promotionSearch action is executed'
        controller.promotionSearch()

        then: 'promotionSearch action response is correct'
        response.status == HttpStatus.OK.value()
        model.promotions != null
        model.storeId != null
        model.totalResults == 2

        where:
        ID | sortOrder
        1  | "asc"
        2  | "desc"
        3  | null
    }

    void "should return 400 bad request on invalid data"() {
        given:
        controller.springSecurityService = getFakeSpringSecurityService()
        params.validDate = "30/11/2022"
        params.updatedSince = "30/11/2022"
        params.type = "BOGOF"
        params.supplier = "1"
        params.max = "10"
        params.offset = "0"
        params.sortColumn = sortColumn
        params.sortOrder = sortOrder
        params.searchTerm = "Test"
        params.searchBy = "Test"
        params.status = "Active"

        when: 'promotionSearch action is executed'
        controller.promotionSearch()

        then: 'promotionSearch action response is correct'
        response.status == HttpStatus.BAD_REQUEST.value()

        where:
        ID | sortOrder       | sortColumn
        1  | "asc"           | "invalid_sup_name_123"
        2  | "desc"          | "invalid_supplier"
        3  | "invalid"       | "updateDatetime"
        3  | "invalid_value" | "updateDatetime"
    }

    //-------------------------------sendToTill function Unit tests----------------------------//

    void "should send message to till"() {
        given:
        Promotion testPromotion = new Promotion(retailerId: 9, description: "Test",
                receiptDescription: "Test", startDate: DateTime.now(), endDate: DateTime.now(),
                type: PromotionType.BOGOF, amount: BigDecimal.TEN, updateDatetime: DateTime.now())
        testPromotion.setId(100)

        PromotionGroup promotionOfferGroup = new PromotionGroup(type: PromotionGroupType.OFFER, sku: 100,
                categoryId: 100, tagId: 150, requiredQuantity: 10, requiredValue: 10, promotion: testPromotion)
        testPromotion.addToGroups(promotionOfferGroup)

        PromotionGroup promotionOfferGroupNoTag = new PromotionGroup(type: PromotionGroupType.OFFER, sku: 100,
                categoryId: 100, tagId: null, requiredQuantity: 10, requiredValue: 10, promotion: testPromotion)
        testPromotion.addToGroups(promotionOfferGroupNoTag)

        PromotionGroup promotionRequiredGroup = new PromotionGroup(type: PromotionGroupType.REQUIRED, sku: 100,
                categoryId: 100, tagId: 250, requiredQuantity: 10, requiredValue: 10, promotion: testPromotion)
        testPromotion.addToGroups(promotionRequiredGroup)

        PromotionGroup promotionRequiredGroupNoTag = new PromotionGroup(type: PromotionGroupType.REQUIRED, sku: 100,
                categoryId: 100, tagId: null, requiredQuantity: 10, requiredValue: 10, promotion: testPromotion)
        testPromotion.addToGroups(promotionRequiredGroupNoTag)


        testPromotion.save(flush: true, failOnError: true)

        Tag testTag1 = new Tag(description: "Test 1")
        testTag1.setId(150)
        TagProduct tagProduct1 = new TagProduct(sku: 100, tag: testTag1)
        tagProduct1.save(flush: true, failOnError: true)
        testTag1.tagProducts.add(tagProduct1)
        testTag1.save(flush: true, failOnError: true)

        Tag testTag2 = new Tag(description: "Test 2")
        testTag2.setId(250)

        TagProduct tagProduct2 = new TagProduct(sku: 150, tag: testTag2)
        tagProduct2.save(flush: true, failOnError: true)

        testTag2.tagProducts.add(tagProduct2)
        testTag2.save(flush: true, failOnError: true)

        controller.rabbitService = Stub(BackOfficeRabbitService) {}

        Map principal = new HashMap()
        principal.put("storeId", storeId)
        principal.put("storeNumber", 100)
        principal.put("retailerId", 9)
        controller.springSecurityService = Stub(SpringSecurityService) {
            getPrincipal() >> principal
        }

        controller.gsonProvider = new GsonProvider()

        params.promotionId = "100"

        when: 'sendToTill action is executed'
        controller.sendToTill()

        then: 'sendToTill action response is correct'
        response.status == HttpStatus.FOUND.value()
        response.redirectUrl == "/promotion/index?promotionId=100"
        flash.message == "Promotion saved successfully"

        where:
        ID | storeId
        1  | 100
        2  | 200
        3  | null
    }

    //-------------------------------ajaxGetPromotionsForProduct function Unit tests----------------------------//

    void "should retrieve promotion types and symbol groups on index page - ajaxGetPromotionsForProduct"() {
        given:
        params.productId = productId

        controller.promotionService = Stub(PromotionService) {
            getPromotionsForProduct(_) >> promotionsList
        }

        when: 'ajaxGetPromotionsForProduct action is executed'
        controller.ajaxGetPromotionsForProduct()

        then: 'ajaxGetPromotionsForProduct action response is correct'
        response.status == HttpStatus.OK.value()
        view == "/product/_promotions"
        model.promotions != null

        where:
        ID | promotionsList | productId
        1  | []             | "100"
        2  | null           | null
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
