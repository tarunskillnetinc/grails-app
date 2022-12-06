package uk.co.wonderlane.wlpos

import grails.plugin.springsecurity.SpringSecurityService
import grails.testing.gorm.DataTest
import grails.testing.web.controllers.ControllerUnitTest
import spock.lang.Specification
import uk.co.wonderlane.wlpos.enums.ButtonGridType
import uk.co.wonderlane.wlpos.enums.ButtonType

class ButtonGridControllerSpec extends Specification implements ControllerUnitTest<ButtonGridController> , DataTest{

    def setup() {}

    def cleanup() {}

    Class<?>[] getDomainClassesToMock(){
        return [ButtonGrid, Button] as Class[]
    }

    //------------------- Calling Index Action ---------------------------------------------//

    def 'Test the index action for successfully render index page'() {
        given:

        when: 'The index action is executed'
        controller.index()

        then: 'The model index render'
        assert view == '/buttonGrid/index.gsp'

    }

    //------------------- Calling Show Action ---------------------------------------------//

    def 'Test the show action for existing button grid successfully render show page'() {
        given:
        String id = paramId
        String type = "SALES"
        controller.buttonService = Stub(ButtonService){
            getButtonGrid(1) >> new ButtonGrid()
        }

        when: 'The show action is executed'
        params['id'] = id
        params['type'] = type
        controller.show()

        then: 'The show page render'
        assert view == '/buttonGrid/show.gsp'

        where: 'Pass following input parameters'
        paramId  ||_
        1        ||_
        0        ||_
        null     ||_

    }

    def 'Test the show action for non existing button grid redirect index page with error'() {

        given:
        String id = 1;
        controller.buttonService = Stub(ButtonService){
            getButtonGrid(1) >> null
        }

        when: 'The show action is executed'
        params['id'] = id
        controller.show()

        then: 'Redirect to index model with flash error message'
        assert flash.error
        assert response.redirectedUrl.startsWith('/buttonGrid/index')
    }


    def 'Test the show action for button grid non existing type (Throwing exception) redirects index page'() {

        given:
        String id = 0
        String type = null

        when: 'The show action is executed'
        params['id'] = id
        params['type'] = type
        controller.show()

        then: 'Redirect to index model'
        assert response.redirectedUrl.startsWith('/buttonGrid/index')
    }

    //------------------- Calling Edit Action ---------------------------------------------//

    def 'Test the edit action for existing button grid renders add page'() {

        given:
        controller.buttonService = Stub(ButtonService){
            getButtonGrid(1) >> new ButtonGrid(id: 1 , retailerId : 9 , storeId : 234, type : 'SALES', rows : 4, columns : 4)
        }

        when: 'The edit action is executed'
        controller.edit(1)

        then: 'The model add page render'
        assert view == '/buttonGrid/add'
        assert model.buttonGrid
        assert model.buttonGrid.retailerId == 9
        assert model.buttonGrid.storeId == 234
        assert model.buttonGrid.type == ButtonGridType.SALES
        assert model.buttonGrid.rows == 4
        assert model.buttonGrid.columns == 4

    }

    def 'Test the edit action for non existing button grid redirect to index page with flash error'() {

        given:
        controller.buttonService = Stub(ButtonService){
            getButtonGrid(1) >> null
        }

        when: 'The edit action is executed'
        controller.edit(1)

        then: 'Redirect to index model with an error'
        assert flash.error
        assert response.redirectedUrl.startsWith('/buttonGrid/index')
    }

    //------------------- Calling Save Action ---------------------------------------------//

    def 'Test the save action for validation fail render add page'() {

        given:
        String id = 1;
        ButtonGrid buttonGrid = new ButtonGrid()
        controller.buttonService = Stub(ButtonService){
            getButtonGrid(1) >> buttonGrid
        }

        controller.springSecurityService = Stub(SpringSecurityService) {
            getPrincipal() >>new HashMap(){{
                put("retailerId", 9);
                put("storeId", 234);
                put("storeNumber", 100)}}
        }
        
        when: 'The save action is executed'
        params['id'] = id
        controller.save()

        then: 'Render add page'
        assert view == '/buttonGrid/add'
        assert model.buttonGrid
        assert model.buttonGrid.retailerId == 9
        assert model.buttonGrid.storeId == 234
        assert model.buttonGrid.type == null
        assert model.buttonGrid.rows == 0
        assert model.buttonGrid.columns == 0
    }

    def 'Test the successful save action for redirect to show page'() {

        given:

        controller.springSecurityService = Stub(SpringSecurityService) {
            getPrincipal() >>new HashMap(){{
                put("id", 9);
                put("retailerId", 9);
                put("storeId", 234);
                put("storeNumber", 100)}}
        }

        String type = 'SALES'
        String id = inputId
        int retailerId = 9
        int storeId = 234
        int rows = inputRow
        int columns = inputColumn


        ButtonGrid buttonGrid = new ButtonGrid(id: 1 , retailerId : 9 , storeId : 234, type : 'SALES', rows : bgPreviousRow, columns : bgPreviousColumn)
        Collection<Button> buttonCollection = new ArrayList<>();
        buttonGrid.setButtons(buttonCollection)

        Button newButton1 = new Button(type : ButtonType.PRODUCT , row : 4, column : 4,  bgColour :'RED',
                textColour : 'RED', imageDisplay : true, textDisplay : true, description : 'Dummy Button 1', sku: 1, quantity : 5);
        newButton1.setButtonGrid(buttonGrid)
        newButton1.springSecurityService = controller.springSecurityService

        Button newButton2 = new Button(type : ButtonType.PRODUCT , row : 2, column : 2,  bgColour :'RED',
                textColour : 'RED', imageDisplay : true, textDisplay : true, description : 'Dummy Button 2', sku: 1, quantity : 5);
        newButton2.setButtonGrid(buttonGrid)
        newButton2.springSecurityService = controller.springSecurityService

        Button newButton3 = new Button(type : ButtonType.PRODUCT , row : 2, column : 4,  bgColour :'RED',
                textColour : 'RED', imageDisplay : true, textDisplay : true, description : 'Dummy Button 2', sku: 1, quantity : 5);
        newButton3.setButtonGrid(buttonGrid)
        newButton3.springSecurityService = controller.springSecurityService

        Button newButton4 = new Button(type : ButtonType.PRODUCT , row : 4, column : 2,  bgColour :'RED',
                textColour : 'RED', imageDisplay : true, textDisplay : true, description : 'Dummy Button 2', sku: 1, quantity : 5);
        newButton4.setButtonGrid(buttonGrid)
        newButton4.springSecurityService = controller.springSecurityService


        buttonCollection.add(newButton1)
        buttonCollection.add(newButton2)
        buttonCollection.add(newButton3)
        buttonCollection.add(newButton4)

        buttonGrid.save(flush: true, failOnError: true)

        controller.buttonService = Stub(ButtonService){
            getButtonGrid(1) >> buttonGrid
        }

        when: 'The edit action is executed'

        params['id'] = id
        params['retailerId'] = retailerId
        params['storeId'] = storeId
        params['type'] = type
        params['rows'] = rows
        params['columns'] = columns
        controller.save()

        then: 'Redirect to index model'
        assert response.redirectedUrl.startsWith('/buttonGrid/show')
        assert buttonGrid.id == 0
        assert buttonGrid.buttons.size() == remainingButton

        where: 'Pass following input parameters'
        inputId       || inputRow  || inputColumn   || bgPreviousRow   || bgPreviousColumn  || remainingButton
        1             || 3         || 3             || 4               || 4                 || 1
        1             || 3         || 3             || 4               || 2                 || 1
        1             || 3         || 3             || 2               || 4                 || 1
        1             || 3         || 3             || 2               || 2                 || 4
        0             || 3         || 3             || 2               || 2                 || 4
        null          || 3         || 3             || 2               || 2                 || 4
    }

    def 'Test the save action for non existing button grid redirect index page with flash error'() {

        given:
        String id =1;

        controller.buttonService = Stub(ButtonService){
            getButtonGrid(1) >> null
        }

        when: 'The edit action is executed'
        params['id'] = id
        controller.save()

        then: 'Redirect to index model'
        assert flash.error
        assert response.redirectedUrl.startsWith('/buttonGrid/index')

    }


}
