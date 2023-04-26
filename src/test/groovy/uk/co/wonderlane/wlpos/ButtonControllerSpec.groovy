package uk.co.wonderlane.wlpos

import grails.plugin.springsecurity.SpringSecurityService
import grails.testing.gorm.DataTest
import grails.testing.web.controllers.ControllerUnitTest
import org.springframework.http.HttpStatus
import spock.lang.Specification
import uk.co.wonderlane.wlpos.enums.ButtonGridType
import uk.co.wonderlane.wlpos.enums.ButtonType
import uk.co.wonderlane.wlpos.enums.TenderType

class ButtonControllerSpec extends Specification implements ControllerUnitTest<ButtonController>, DataTest {

    Class<?>[] getDomainClassesToMock() {
        [ButtonGrid, Button] as Class<?>[]
    }

    //-------------------------------edit function Unit tests----------------------------//

    def 'if the button is already available and product sku not available, return the button'() {
        given:
        createTestButton(100, 1, ButtonType.PRODUCT, false)
        controller.productService = Stub(ProductService) {
            getProductVariant(_) >> null
        }
        controller.buttonService = Stub(ButtonService) {}
        controller.params.id = "100"

        when: 'edit button action is executed'
        def controllerResponse = controller.edit()

        then: 'edit button response is correct'
        response.status == HttpStatus.OK.value()
        controllerResponse.button != null
        controllerResponse.buttonImage == null
        controllerResponse.productSku == null
        controllerResponse.productDescription == null
    }

    def 'if the button is already available, return the button'() {
        given:
        def testSku = 1
        createTestButton(100, 1, ButtonType.PRODUCT, false)
        controller.productService = Stub(ProductService) {
            getProductVariant(_) >> new ProductVariant(sku: testSku, product: new Product(description: "TEST PRODUCT"))
        }
        controller.buttonService = Stub(ButtonService) {}

        controller.params.id = "100"

        when: 'edit button action is executed'
        def controllerResponse = controller.edit()

        then: 'edit button response is correct'
        response.status == HttpStatus.OK.value()
        controllerResponse.button != null
        controllerResponse.buttonImage == null
        controllerResponse.productSku == 1
        controllerResponse.productDescription == "TEST PRODUCT"
    }

    def 'if the button is already available and no product variant available for button sku, return the button'() {
        given:
        Button testBtn = createTestButton(100, 1, btnType, false)
        testBtn.setSku(sku)
        testBtn.save()

        controller.productService = Stub(ProductService) {
            getProductVariant(_) >> null
        }
        controller.buttonService = Stub(ButtonService) {}
        controller.params.id = "100"

        when: 'edit button action is executed'
        def controllerResponse = controller.edit()

        then: 'edit button response is correct'
        response.status == HttpStatus.OK.value()
        controllerResponse.button != null
        controllerResponse.buttonImage == null
        controllerResponse.productSku == null
        controllerResponse.productDescription == null

        where:
        ID | btnType            | sku
        1  | ButtonType.PRODUCT | 100
        1  | ButtonType.TENDER  | null
        1  | ButtonType.TENDER  | 100
    }

    def 'if the button is already available and button image available, return the button'() {
        given:
        createTestButton(100, 1, ButtonType.PRODUCT, true)
        controller.imageService = getFakeImageService()
        controller.buttonService = Stub(ButtonService) {}
        controller.springSecurityService = getFakeSpringSecurityService()
        controller.productService = Stub(ProductService) {
            getProductVariant(_) >> null
        }
        controller.params.id = "100"

        when: 'edit button action is executed'
        def controllerResponse = controller.edit()

        then: 'edit button response is correct'
        response.status == HttpStatus.OK.value()
        controllerResponse.button != null
        controllerResponse.buttonImage != null
        controllerResponse.productSku == null
        controllerResponse.productDescription == null
    }

    def 'if the button is already available and product variant available, return the button'() {
        given:
        createTestButton(100, 1, ButtonType.PRODUCT, false)
        controller.imageService = getFakeImageService()
        controller.buttonService = Stub(ButtonService) {}
        controller.springSecurityService = getFakeSpringSecurityService()
        controller.productService = Stub(ProductService) {
            getProductVariant(_) >> new ProductVariant(sku: 100)
        }
        controller.params.id = "100"

        when: 'edit button action is executed'
        def controllerResponse = controller.edit()

        then: 'edit button response is correct'
        response.status == HttpStatus.OK.value()
        controllerResponse.button != null
        controllerResponse.buttonImage == null
        controllerResponse.productSku != null
        controllerResponse.productDescription == null
    }

    def 'if the button is not available, return a new button with params'() {
        given:
        ButtonGrid btnGrid = new ButtonGrid(retailerId: 9, type: gridType, rows: 2, columns: 2)
        btnGrid.setId(gridId)
        btnGrid.save(flush: true, failOnError: true)
        controller.params.row = 100
        controller.params.column = 100
        controller.params.id = testId
        controller.params.buttonGridId = gridId

        controller.imageService = getFakeImageService()
        controller.buttonService = Stub(ButtonService) {}
        controller.springSecurityService = getFakeSpringSecurityService()
        controller.productService = Stub(ProductService) {
            getProductVariant(_) >> null
        }

        when: 'edit button action is executed'
        def controllerResponse = controller.edit()

        then: 'edit button response is correct'
        response.status == HttpStatus.OK.value()
        controllerResponse.button != null
        controllerResponse.button.buttonGrid != null
        controllerResponse.button.row == 100
        controllerResponse.button.column == 100
        controllerResponse.button.type == buttonType
        controllerResponse.button.bgColour == "#FFFFFF"
        controllerResponse.button.textColour == "#000000"
        !controllerResponse.button.imageDisplay
        controllerResponse.button.textDisplay
        controllerResponse.availableSubPages != null
        controllerResponse.buttonImage == null
        controllerResponse.productSku == null
        controllerResponse.productDescription == null

        where:
        gridType              | buttonType         | testId | gridId
        ButtonGridType.TENDER | ButtonType.TENDER  | null   | 1
        ButtonGridType.SALES  | ButtonType.PRODUCT | null   | 2
        ButtonGridType.TENDER | ButtonType.TENDER  | "-1"   | 3
    }

    //-------------------------------unassign function Unit tests----------------------------//

    def 'delete the button if the button is already available, and redirect to button grid'() {
        given:
        createTestButton(btnId, gridId, ButtonType.PRODUCT, false)
        controller.rabbitService = Stub(BackOfficeRabbitService) {

        }
        controller.imageService = Stub(ImageService) {

        }
        controller.springSecurityService = getFakeSpringSecurityService()
        controller.buttonService = Stub(ButtonService) {
            ButtonGrid grid = new ButtonGrid()
            grid.setId(gridId)
            getButton(_) >> new Button(id: 100, type: ButtonType.PRODUCT, imageDisplay: false,
                    buttonGrid: grid)
            getButtonGridFromId(_) >> new ButtonGrid()
        }

        when: 'unassign button action is executed'
        controller.unassign(btnId)

        then: 'unassign button response is correct'
        response.status == HttpStatus.FOUND.value()
        response.redirectUrl == "/buttonGrid/show/" + gridId

        where:
        ID | gridId | btnId
        1  | 1      | 1
        2  | 2      | 2
        3  | 3      | 3
    }

    //-------------------------------save function Unit tests----------------------------//

    def 'if the button is already available and valid, add button to the button grid and redirect'() {
        given:
        controller.params.id = btnId as String
        createTestButton(btnId, gridId, ButtonType.PRODUCT, imageDisplay)

        controller.buttonService = Stub(ButtonService) {}
        controller.rabbitService = Stub(BackOfficeRabbitService) {}
        controller.springSecurityService = getFakeSpringSecurityService()
        controller.imageService = getFakeImageService()

        when: 'save button action is executed'
        controller.save()

        then: 'save button response is correct'
        response.status == HttpStatus.FOUND.value()
        response.redirectUrl == "/buttonGrid/show/" + gridId

        where:
        ID | gridId | btnId | imageDisplay
        1  | 1     | 1     | true
        2  | 1      | 2     | false
        3  | 1      | 3     | true
    }

    def 'if the button is already available and valid and removeImage param provided, remove image, add button to the button grid and redirect to show'() {
        given:
        controller.params.id = btnId as String
        controller.params.removeImage = true
        createTestButton(btnId, gridId, ButtonType.PRODUCT, imageDisplay)

        controller.buttonService = Stub(ButtonService) {}
        controller.rabbitService = Stub(BackOfficeRabbitService) {}
        controller.springSecurityService = getFakeSpringSecurityService()
        controller.imageService = getFakeImageService()

        when: 'save button action is executed'
        controller.save()

        then: 'save button response is correct'
        response.status == HttpStatus.FOUND.value()
        response.redirectUrl == "/buttonGrid/show/" + gridId

        where:
        ID | gridId | btnId | imageDisplay
        1  | 1      | 1     | true
        2  | 2      | 2     | false
        3  | 3      | 3     | true
    }

    def 'if the button is already available and valid and image param provided, save image, add button to the button grid and redirect to show'() {
        given:
        controller.params.id = btnId as String
        controller.params.image = new HashMap()
        controller.params.image.put("bytes", imageItem)
        controller.params.image.put("contentType", "image/png")
        createTestButton(btnId, gridId, ButtonType.PRODUCT, imageDisplay)

        controller.buttonService = Stub(ButtonService) {}
        controller.rabbitService = Stub(BackOfficeRabbitService) {}
        controller.springSecurityService = getFakeSpringSecurityService()
        controller.imageService = getFakeImageService()

        when: 'save button action is executed'
        controller.save()

        then: 'save button response is correct'
        response.status == HttpStatus.FOUND.value()
        response.redirectUrl == "/buttonGrid/show/" + gridId

        where:
        ID | gridId | btnId | imageDisplay | imageItem
        1  | 1      | 1     | true         | new byte[10]
        2  | 2      | 2     | false        | new byte[0]
        3  | 3      | 3     | true         | new byte[10]
    }

    def 'if the button is not available and no button params provided, add button to the button grid and redirect to edit screen'() {
        given:
        createTestButton(btnId, gridId, ButtonType.PRODUCT, imageDisplay)
        controller.params.buttonGrid = ButtonGrid.get(gridId)

        controller.buttonService = Stub(ButtonService) {}
        controller.rabbitService = Stub(BackOfficeRabbitService) {}
        controller.springSecurityService = getFakeSpringSecurityService()
        controller.imageService = getFakeImageService()

        when: 'save button action is executed'
        controller.save()

        then: 'save button response is correct'
        response.status == HttpStatus.OK.value()
        view == "/button/edit"
        model != null
        model.button != null

        where:
        ID | gridId | btnId | imageDisplay
        1  | 1      | 1     | true
        2  | 2      | 2     | false
        3  | 3      | 3     | true
    }

    def 'if the button is not available and product button params provided, add button to the button grid and redirect to edit screen'() {
        given:
        createTestButton(btnId, gridId, ButtonType.PRODUCT, imageDisplay)
        controller.params.buttonGrid = ButtonGrid.get(gridId)
        controller.params.type = ButtonType.PRODUCT
        controller.params.sku = 100

        controller.buttonService = Stub(ButtonService) {}
        controller.rabbitService = Stub(BackOfficeRabbitService) {}
        controller.springSecurityService = getFakeSpringSecurityService()
        controller.imageService = getFakeImageService()
        controller.productService = Stub(ProductService) {
            getProductVariant(_) >> new ProductVariant(sku: 100)
        }

        when: 'save button action is executed'
        controller.save()

        then: 'save button response is correct'
        response.status == HttpStatus.OK.value()
        view == "/button/edit"
        model != null
        model.button != null
        model.productSku != null

        where:
        ID | gridId | btnId | imageDisplay
        1  | 1      | 1     | true
        2  | 2      | 2     | false
        3  | 3      | 3     | true
    }

    def 'if the button is not available and image display button params provided, add button to the button grid and redirect to edit screen'() {
        given:
        createTestButton(btnId, gridId, ButtonType.PRODUCT, imageDisplay)
        controller.params.buttonGrid = ButtonGrid.get(gridId)
        controller.params.imageDisplay = imageDisplay

        controller.buttonService = Stub(ButtonService) {}
        controller.rabbitService = Stub(BackOfficeRabbitService) {}
        controller.springSecurityService = getFakeSpringSecurityService()
        controller.imageService = getFakeImageService()
        controller.productService = Stub(ProductService) {
            getProductVariant(_) >> new ProductVariant(sku: 100)
        }

        when: 'save button action is executed'
        controller.save()

        then: 'save button response is correct'
        response.status == HttpStatus.OK.value()
        view == "/button/edit"
        model != null
        model.button != null
        model.productSku == null
        model.buttonImage != null

        where:
        ID | gridId | btnId | imageDisplay
        1  | 1      | 1     | true
        2  | 2      | 2     | true
        3  | 3      | 3     | true
    }

    def 'if the button is available and on exception, add button to the button grid and redirect to edit screen'() {
        given:
        createTestButton(btnId, gridId, buttonType, imageDisplay)
        controller.params.buttonGrid = ButtonGrid.get(gridId)
        controller.params.id = btnId as String

        controller.buttonService = Stub(ButtonService) {}
        controller.springSecurityService = getFakeSpringSecurityService()
        controller.imageService = getFakeImageService()
        controller.productService = Stub(ProductService) {
            getProductVariant(_) >> new ProductVariant(sku: 100)
        }

        when: 'save button action is executed'
        controller.save()

        then: 'save button response is correct'
        response.status == HttpStatus.OK.value()
        view == "/button/edit"
        model != null
        model.button != null
        (sku != null && buttonType == ButtonType.PRODUCT) ? model.productSku != null : true
        imageDisplay ? model.buttonImage != null : true

        where:
        ID | gridId | btnId | imageDisplay | sku  | buttonType
        1  | 1      | 1     | true         | null | ButtonType.TENDER
        1  | 1      | 1     | true         | 100  | ButtonType.TENDER
        2  | 2      | 2     | false        | 1    | ButtonType.PRODUCT
        3  | 3      | 3     | true         | 100  | ButtonType.PRODUCT
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

    def getFakeImageService() {
        return Stub(ImageService) {
            getImageFromFile(_) >> new byte[0]
        }
    }

    def createTestButton(int buttonId, int buttonGridId, ButtonType btnType, boolean imageDisplay) {
        ButtonGrid btnGrid = new ButtonGrid(retailerId: 9, type: ButtonGridType.SALES, rows: 2, columns: 2)
        btnGrid.storeId = 100
        btnGrid.setId(buttonGridId)
        Button btn = new Button(row: 1, column: 1, sku: 100, buttonGrid: btnGrid, quantity: 100, description: "Test", type: btnType,
                bgColour: "#FFFFFF", textColour: "#000000", imageDisplay: imageDisplay, textDisplay: true);
        btn.setId(buttonId)
        btn.setTenderType(TenderType.CASH)
        btn.springSecurityService = getFakeSpringSecurityService()
        btnGrid.save(flush: true, failOnError: true)
        btn.save(flush: true, failOnError: true)
        return btn;
    }

}
