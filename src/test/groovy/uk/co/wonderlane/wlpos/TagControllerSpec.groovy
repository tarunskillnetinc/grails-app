package uk.co.wonderlane.wlpos

import grails.plugin.springsecurity.SpringSecurityService
import grails.testing.gorm.DataTest
import grails.testing.web.controllers.ControllerUnitTest
import org.springframework.http.HttpStatus
import spock.lang.Specification

class TagControllerSpec extends Specification implements ControllerUnitTest<TagController>, DataTest {

    Class<?>[] getDomainClassesToMock() {
        [TagProduct, Tag] as Class<?>[]
    }

    def setup() {
    }

    def cleanup() {
    }

    //-------------------------------index function Unit tests----------------------------//

    void "should retrieve tags on index"() {
        given:
        List<Tag> tags = new ArrayList<>()
        tags.add(new Tag(id: 1))
        tags.add(new Tag(id: 2))
        controller.tagService = Stub(TagService) {
            getTags() >> tags
        }

        when: 'index action is executed'
        def controllerResponse = controller.index()

        then: 'index action response is correct'
        response.status == HttpStatus.OK.value()
        controllerResponse.tags != null
    }

    //-------------------------------show function Unit tests----------------------------//

    void "should flash error and redirect to index page if tag not found on show action"() {
        given:
        controller.tagService = Stub(TagService) {
            getTag(_) >> null
        }

        when: 'show action is executed'
        def controllerResponse = controller.show(1)

        then: 'show action response is correct'
        response.status == HttpStatus.FOUND.value()
        response.redirectUrl == "/tag/index"
        controller.flash.error == "Tag not found."
        controllerResponse == null
    }

    void "should return the tag with product variant id and product description on show action"() {
        given:
        TagProduct tagProduct = new TagProduct(sku: 100)
        tagProduct.save()
        controller.tagService = Stub(TagService) {
            getTag(_) >> new Tag(id: 1, tagProducts: Set.of(tagProduct))
        }

        controller.productService = Stub(ProductService) {
            getProductVariants(_) >> products
        }

        when: 'show action is executed'
        def controllerResponse = controller.show(1)

        then: 'show action response is correct'
        response.status == HttpStatus.OK.value()
        controllerResponse.tag != null
        controllerResponse.tag.tagProducts[0] != null

        where:
        ID | products
        1  | null
        2  | new ArrayList<>()
        2  | List.of(new ProductVariant(sku: 200))
        2  | List.of(new ProductVariant(sku: 100))
    }

    //-------------------------------ajaxGetTags function Unit tests----------------------------//

    class FakeTagSearchResultList<T> extends ArrayList<T> {
        public int totalCount = 0
    }

    void "should return tags when ajaxGetTags action called"() {
        given:
        FakeTagSearchResultList<Tag> tags = new FakeTagSearchResultList<>()
        TagProduct tagProduct = new TagProduct(sku: 100)
        tagProduct.save()
        tags.add(new Tag(id: 1, tagProducts: Set.of(tagProduct)))
        tags.properties.put("totalCount", 1)
        controller.tagService = Stub(TagService) {
            getTags(_) >> tags
        }

        when: 'ajaxGetTags action is executed'
        controller.ajaxGetTags("search term")

        then: 'ajaxGetTags action response is correct'
        response.status == HttpStatus.OK.value()
        model != null
        model.tags != null
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

    void "should flash error and redirect to index page if tag not found on edit action"() {
        given:
        controller.tagService = Stub(TagService) {
            getTag(_) >> null
        }

        when: 'edit action is executed'
        def controllerResponse = controller.edit(1)

        then: 'edit action response is correct'
        response.status == HttpStatus.FOUND.value()
        response.redirectUrl == "/tag/index"
        controller.flash.error == "Tag not found."
        controllerResponse == null
    }

    void "should return the tag with product variant id and product description on edit action"() {
        given:
        TagProduct tagProduct = new TagProduct(sku: 100)
        tagProduct.save()
        controller.tagService = Stub(TagService) {
            getTag(_) >> new Tag(id: 1, tagProducts: Set.of(tagProduct))
        }

        controller.productService = Stub(ProductService) {
            getProductVariants(_) >> products
        }

        when: 'edit action is executed'
        controller.edit(1)

        then: 'edit action response is correct'
        response.status == HttpStatus.OK.value()
        view == "/tag/add"
        model.tag.tagProducts[0] != null

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
        model.tagProduct != null
    }

    //-------------------------------save function Unit tests----------------------------//

    void "should flash error when tag is not found"() {
        given:
        controller.tagService = Stub(TagService) {
            getTag(_) >> null
        }

        when: 'save action is executed'
        SaveTagCommand saveTagCommand = new SaveTagCommand()
        saveTagCommand.setId(100)
        controller.save(saveTagCommand)

        then: 'save action response is correct'
        response.status == HttpStatus.OK.value()
        controller.flash.error == "Tag not found."
    }

    void "save tags correctly on valid input and valid tag details - happy path"() {
        given:
        TagProduct tagProduct = new TagProduct(sku: 100)
        TagProduct tagProductNotIncl = new TagProduct(sku: 300)
        Set<TagProduct> tagProducts = new HashSet<>()
        tagProducts.add(tagProduct)
        tagProducts.add(tagProductNotIncl)
        Tag testTag = new Tag(tagProducts: tagProducts)
        testTag.setId(1)
        tagProduct.setTag(testTag)
        tagProductNotIncl.setTag(testTag)
        testTag.save()
        controller.tagService = Stub(TagService) {
            getTag(_) >> testTag
        }

        controller.rabbitService = Stub(BackOfficeRabbitService) {
            isOpen() >> true
        }

        controller.springSecurityService = getFakeSpringSecurityService()

        when: 'save action is executed'
        SaveTagCommand saveTagCommand = new SaveTagCommand()
        saveTagCommand.setId(100)
        Long[] skus = new Long[2]
        skus[0] = 100
        skus[1] = 200
        saveTagCommand.setSku(skus)
        saveTagCommand.setDescription("Test Command")
        controller.save(saveTagCommand)

        then: 'save action response is correct'
        response.status == HttpStatus.FOUND.value()
        controller.flash.message == "Tag saved successfully."
    }

    void "create new tage and save tags correctly on valid input and valid tag details - happy path"() {
        given:
        controller.tagService = Stub(TagService) {}

        controller.rabbitService = Stub(BackOfficeRabbitService) {
            isOpen() >> true
        }

        controller.springSecurityService = getFakeSpringSecurityService()

        when: 'save action is executed'
        SaveTagCommand saveTagCommand = new SaveTagCommand()
        Long[] skus = new Long[0]
        saveTagCommand.setSku(skus)
        saveTagCommand.setDescription("Test Command")
        controller.save(saveTagCommand)

        then: 'save action response is correct'
        response.status == HttpStatus.FOUND.value()
        controller.flash.message == "Tag saved successfully."
    }

    void "save tags correctly on valid input and valid tag and rabbit service error - partial error path"() {
        given:
        TagProduct tagProduct = new TagProduct(sku: 100)
        Set<TagProduct> tagProducts = new HashSet<>()
        tagProducts.add(tagProduct)
        Tag testTag = new Tag(tagProducts: tagProducts)
        testTag.setId(1)
        tagProduct.setTag(testTag)
        testTag.save()
        controller.tagService = Stub(TagService) {
            getTag(_) >> testTag
        }

        controller.rabbitService = Stub(BackOfficeRabbitService) {
            isOpen() >> false
        }

        controller.springSecurityService = getFakeSpringSecurityService()

        when: 'save action is executed'
        SaveTagCommand saveTagCommand = new SaveTagCommand()
        Long[] skus = new Long[1]
        skus[0] = 100
        saveTagCommand.setSku(skus)
        saveTagCommand.setDescription("Test Command")
        controller.save(saveTagCommand)

        then: 'save action response is correct'
        response.status == HttpStatus.FOUND.value()
        controller.flash.message == "Tag saved successfully."
    }

    void "return validation error messages on validation error is found - error path"() {
        given:
        TagProduct tagProduct = new TagProduct(sku: 100)
        Set<TagProduct> tagProducts = new HashSet<>()
        tagProducts.add(tagProduct)
        Tag testTag = new Tag()
        testTag.setId(1)
        testTag.setTagProducts(tagProducts)
        tagProduct.setTag(testTag)
        testTag.save()
        controller.tagService = Stub(TagService) {
            getTag(_) >> testTag
        }

        controller.rabbitService = Stub(BackOfficeRabbitService) {
            isOpen() >> false
        }


        controller.productService = Stub(ProductService) {
            getProductVariants(_) >> products
        }

        controller.springSecurityService = getFakeSpringSecurityService()

        when: 'save action is executed'
        SaveTagCommand saveTagCommand = new SaveTagCommand()
        saveTagCommand.setId(100)
        Long[] skus = new Long[1]
        skus[0] = 100
        saveTagCommand.setSku(skus)
        controller.save(saveTagCommand)

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
