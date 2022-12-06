package uk.co.wonderlane.wlpos

import grails.plugin.springsecurity.SpringSecurityService
import grails.testing.gorm.DataTest
import grails.testing.services.ServiceUnitTest
import spock.lang.Specification
import uk.co.wonderlane.wlpos.enums.ButtonGridType
import uk.co.wonderlane.wlpos.enums.ButtonType
import uk.co.wonderlane.wlpos.enums.ProcessType

class ButtonServiceSpec extends Specification  implements ServiceUnitTest<ButtonService>  , DataTest{

    def setup() {}

    def cleanup() {}



    Class<?>[] getDomainClassesToMock(){
        return [ButtonGrid, Button] as Class[]
    }

    //------------------- Calling Save Button Action ---------------------------------------------//

    def 'Test the save button action'() {

        given:

        service.springSecurityService = Stub(SpringSecurityService) {
            getPrincipal() >>new HashMap(){{
                put("id", 9);
                put("retailerId", 9);
                put("storeId", 234);
                put("storeNumber", 100)}}
        }
        ButtonGrid buttonGrid = new ButtonGrid(id: 1 , retailerId : 1 , storeId : 1, type : 'SALES', rows : 4, columns : 4)
        Collection<Button> buttonCollection = new ArrayList<>();
        buttonGrid.setButtons(buttonCollection)

        Button newButton1 = new Button(id :100, type : ButtonType.PRODUCT , row : 4, column : 4,  bgColour :'RED',
                textColour : 'RED', imageDisplay : true, textDisplay : true, description : 'Dummy Button 1', sku: 1, quantity : 5);
        newButton1.setButtonGrid(buttonGrid)
        newButton1.springSecurityService = service.springSecurityService

        when: 'The save button action is executed'
        Button savedButton = service.saveButton(newButton1)

        then: 'The button model save successfully'
        assert savedButton != null
        assert savedButton.type == ButtonType.PRODUCT
        assert savedButton.row == 4
        assert savedButton.column == 4
        assert savedButton.bgColour == 'RED'
        assert savedButton.textColour == 'RED'
        assert savedButton.imageDisplay == true
        assert savedButton.textDisplay == true
        assert savedButton.description == 'Dummy Button 1'

    }

    //------------------- Calling Save Button Grid Action ---------------------------------------------//

    def 'Test the save button grid action button '() {

        given:
        service.springSecurityService = Stub(SpringSecurityService) {
            getPrincipal() >>new HashMap(){{
                put("id", 9);
                put("retailerId", 9);
                put("storeId", 234);
                put("storeNumber", 100)}}
        }

        ButtonGrid buttonGrid = new ButtonGrid(retailerId : 1 , storeId : 1, type : 'SALES', rows : 4, columns : 4)
        buttonGrid.setId(100)

        when: 'The save button grid action is executed'
        ButtonGrid savedButtonGrid = service.saveButtonGrid(buttonGrid)

        then: 'The button grid successfully'
        assert savedButtonGrid != null
        assert savedButtonGrid.retailerId == 1
        assert savedButtonGrid.storeId == 1
        assert savedButtonGrid.type == ButtonGridType.SALES
        assert savedButtonGrid.rows == 4
        assert savedButtonGrid.columns == 4

    }

    //------------------- Calling Delete Button Grid Action ---------------------------------------------//

    def 'Test the delete button grid action '() {
        given:

        service.springSecurityService = Stub(SpringSecurityService) {
            getPrincipal() >>new HashMap(){{
                put("id", 9);
                put("retailerId", 9);
                put("storeId", 234);
                put("storeNumber", 100)}}
        }

        ButtonGrid buttonGrid = new ButtonGrid(id: 1 , retailerId : 1 , storeId : 1, type : 'SALES', rows : 4, columns : 4)
        Collection<Button> buttonCollection = new ArrayList<>();
        buttonGrid.setButtons(buttonCollection)

        Button newButton1 = new Button(id :100, type : ButtonType.PRODUCT , row : 4, column : 4,  bgColour :'RED',
                textColour : 'RED', imageDisplay : true, textDisplay : true, description : 'Dummy Button 1', sku: 1, quantity : 5);
        newButton1.springSecurityService = service.springSecurityService
        newButton1.setId(100)
        newButton1.setButtonGrid(buttonGrid)

        buttonGrid.save(flush: true, failOnError: true)
        newButton1.save(flush: true, failOnError: true)

        when: 'The delete button grid action is executed'
        service.deleteButton(newButton1)

        ButtonGrid returnedButton = Button.get(100)

        then: 'The button grid delete successfully'
        assert !returnedButton

    }

    //------------------- Calling Load Button Grid By Id Action ---------------------------------------------//

    def 'Test load button grid by id action'() {

        given:
        service.springSecurityService = Stub(SpringSecurityService) {
            getPrincipal() >>new HashMap(){{
                put("id", 9);
                put("retailerId", 9);
                put("storeId", 234);
                put("storeNumber", 100)}}
        }

        ButtonGrid buttonGrid = new ButtonGrid(retailerId : 9 , storeId : 1, type : 'SALES', rows : 4, columns : 4)
        buttonGrid.setId(100)

        mockDomain(ButtonGrid, [buttonGrid])

        when: 'The load button grid by id action is executed'
        ButtonGrid buttonGridReturned = service.getButtonGrid(100)

        then: 'The button grid load successfully'
        assert buttonGridReturned
        assert buttonGridReturned.retailerId == 9
        assert buttonGridReturned.storeId == 1
        assert buttonGridReturned.type == ButtonGridType.SALES
        assert buttonGridReturned.rows == 4
        assert buttonGridReturned.columns == 4

    }

    //------------------- Calling Load Button Grid By Type Action ---------------------------------------------//

    def 'Test load button grid by type action'() {

        given:
        service.springSecurityService = Stub(SpringSecurityService) {
            getPrincipal() >>new HashMap(){{
                put("id", 9);
                put("retailerId", 9);
                put("storeId", 234);
                put("storeNumber", 100)}}
        }

        ButtonGrid buttonGrid = new ButtonGrid(retailerId : 9 , storeId : 234, type : 'SALES', rows : 4, columns : 4)
        buttonGrid.setId(100)

        mockDomain(ButtonGrid, [buttonGrid])

        when: 'The load button grid by type action is executed'
        ButtonGrid buttonGridReturned = service.getButtonGrid(ButtonGridType.SALES)

        then: 'The button grid model load successfully'
        assert buttonGridReturned
        assert buttonGridReturned.retailerId == 9
        assert buttonGridReturned.storeId == 234
        assert buttonGridReturned.type == ButtonGridType.SALES
        assert buttonGridReturned.rows == 4
        assert buttonGridReturned.columns == 4

    }

    def 'Test load button grid by type action for non existing object'() {

        given:
        service.springSecurityService = Stub(SpringSecurityService) {
            getPrincipal() >>new HashMap(){{
                put("id", 9);
                put("retailerId", 9);
                put("storeId", 234);
                put("storeNumber", 100)}}
        }

        ButtonGrid buttonGrid = new ButtonGrid(retailerId : 9 , storeId : 234, type : 'OTHER', rows : 4, columns : 4)
        buttonGrid.setId(100)

        mockDomain(ButtonGrid, [buttonGrid])

        when: 'The load button grid by type action is executed'
        ButtonGrid buttonGridReturned = service.getButtonGrid(ButtonGridType.SALES)

        then: 'The valid button grid object should not return'
        assert !buttonGridReturned

    }

    //------------------- Calling Load All Button Grid Of Type Other Action ---------------------------------------------//

    def 'Test load button grid by other button grid type successfully return button grid'() {

        given:
        service.springSecurityService = Stub(SpringSecurityService) {
            getPrincipal() >>new HashMap(){{
                put("id", 9);
                put("retailerId", 9);
                put("storeId", 234);
                put("storeNumber", 100)}}
        }

        ButtonGrid buttonGrid = new ButtonGrid(retailerId : 9 , storeId : 234, type : 'OTHER', rows : 4, columns : 4)
        buttonGrid.setId(100)
        buttonGrid.setDescription("Dummy button grid")

        mockDomain(ButtonGrid, [buttonGrid])

        when: 'The load button grid by other button grid type action is executed'
        List<ButtonGrid> buttonGridReturned = service.getOtherButtonGrids()

        then: 'The button grid model list load successfully'
        assert buttonGridReturned
        assert buttonGridReturned.size() == 1
        assert buttonGridReturned.get(0).retailerId == 9
        assert buttonGridReturned.get(0).storeId == 234
        assert buttonGridReturned.get(0).type == ButtonGridType.OTHER
        assert buttonGridReturned.get(0).rows == 4
        assert buttonGridReturned.get(0).columns == 4

    }

    //------------------- Calling All Available Process Action ---------------------------------------------//

    def 'Test load available processes action'() {

        given:

        when: 'The calling available process action is executed'
        List<ProcessType> processTypeList = service.getAvailableProcesses()

        then: 'Load all available processes'
        assert processTypeList
        assert processTypeList.size() == 25

    }
}
