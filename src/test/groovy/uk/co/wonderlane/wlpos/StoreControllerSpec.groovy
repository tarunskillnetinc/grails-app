package uk.co.wonderlane.wlpos

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import grails.plugin.springsecurity.SpringSecurityService
import grails.testing.gorm.DataTest
import grails.testing.web.controllers.ControllerUnitTest
import org.springframework.security.core.authority.SimpleGrantedAuthority
import spock.lang.Specification
import uk.co.wonderlane.wlpos.enums.PrintReceiptOption

class StoreControllerSpec extends Specification implements ControllerUnitTest<StoreController>, DataTest{

    Class<?>[] getDomainClassesToMock(){
        return [Store, PriceBand, Range] as Class[]
    }

    //------------------Calling Index Action -------------//

    def 'Test the store setting save action'() {

        given:

        Integer principalStoreId = storeId
        Integer principalRetailerId = retailerId

        controller.springSecurityService = Stub(SpringSecurityService) {

            Collection authorities = null

            SimpleGrantedAuthority simpleGrantedAuthority1 = authority1
            SimpleGrantedAuthority simpleGrantedAuthority2 = authority2

            /* This is only for test manipulation since there is no way of generating null authority of empty authority
            *   Logic is created only for test If one of authority1 and authority2 is null then we set empty list
            *   if both authority1 and authority2 is null then we set null list */
            if (authority1 && authority2){
                authorities = Arrays.asList(simpleGrantedAuthority1, simpleGrantedAuthority2)
            } else if (!authority1 && !authority2){
                authority1 = null
            } else if (!authority2 || !authority2){
                authorities = Arrays.asList()
            }

            getPrincipal() >>new HashMap(){{
                put("retailerId", principalRetailerId);
                put("storeId", principalStoreId);
                put("storeNumber", 100)
                put("authorities",authorities)
            }}
        }

        PriceBand priceBandOriginal = new PriceBand(retailerId : 9, description: "Dummy description")
        priceBandOriginal.setId(1)

        Range rangeOriginal = new Range(retailerId: 9, description: "Dummy description")
        rangeOriginal.setId(1)

        Store storeSettings1 = new Store()
        storeSettings1.setType(type)
        if (storeId){
            storeSettings1.setId(storeId)
            storeSettings1.setStoreId(storeId)
        }
        storeSettings1.setRetailerId(retailerId)
        storeSettings1.setPrintReceiptOption(PrintReceiptOption.ALWAYS_PRINT)
        storeSettings1.setPriceBand(priceBandOriginal)
        storeSettings1.setRange(rangeOriginal)
        storeSettings1.setCountIncrement(new BigDecimal(0.01))
        storeSettings1.springSecurityService = controller.springSecurityService

        storeSettings1.save(flush: true, failOnError: true)
        priceBandOriginal.save(flush: true, failOnError: true)
        rangeOriginal.save(flush: true, failOnError: true)

        mockDomain(Store, [storeSettings1, priceBandOriginal, rangeOriginal])

        when: 'The index action is executed'
        HashMap model = controller.index()

        then: 'The model index render'
        assert view == '/storeSettings/index.gsp'
        assert model.storeSettings
        assert model.availablePriceBands
        assert model.availableProductRanges
        if (type == StoreType.STORE.getValue()){
            assert model.availableParentStores
        }
        assert model.viewOptions
        assert model.viewOptions.showUISettings == showUISettings
        assert model.viewOptions.showParentStoreSettings == showParentSettings

        where: 'Pass following input parameters'
        retailerId || storeId ||type                       || authority1                                      || authority2                                    || showUISettings || showParentSettings
        9          || 234     ||StoreType.STORE.getValue() || new SimpleGrantedAuthority("ROLE_HEAD_OFFICE")  || new SimpleGrantedAuthority("ROLE_ENGINEER")   || true           || false
        9          || 234     ||StoreType.CAFE.getValue()  || new SimpleGrantedAuthority("ROLE_HEAD_OFFICE")  || new SimpleGrantedAuthority("ROLE_HEAD_OFFICE")|| true           || true
        9          || 234     ||StoreType.CAFE.getValue()  || new SimpleGrantedAuthority("ROLE_ENGINEER")     || new SimpleGrantedAuthority("ROLE_ENGINEER")   || true           || true
        9          || 234     ||StoreType.STORE.getValue() || null                                            || new SimpleGrantedAuthority("ROLE_ENGINEER")   || true           || false
        9          || 234     ||StoreType.CAFE.getValue()  || new SimpleGrantedAuthority("ROLE_ENGINEER")     || null                                          || true           || false
        9          || 234     ||StoreType.CAFE.getValue()  || null                                            || null                                          || true           || false
        9          || null    ||StoreType.STORE.getValue() || null                                            || null                                          || false          || false
        9          || null    ||StoreType.CAFE.getValue()  || null                                            || null                                          || false          || false
    }

    //------------------Calling Save Action (Validation Fail Scenario)-------------//

    def 'Test the save action for unsuccessfully validation result in rendering index page'() {

        given:

        BigDecimal countIncrement = new BigDecimal(2)

        controller.retailerId = retailerId
        controller.storeId = storeId
        controller.storeNumber = 100

        controller.springSecurityService = Stub(SpringSecurityService) {

            SimpleGrantedAuthority simpleGrantedAuthority1 = new SimpleGrantedAuthority(role1)
            SimpleGrantedAuthority simpleGrantedAuthority2 = new SimpleGrantedAuthority(role2)

            Collection authorities = Arrays.asList(simpleGrantedAuthority1, simpleGrantedAuthority2)

            getPrincipal() >>new HashMap(){{
                put("retailerId", retailerId);
                put("storeId", storeId);
                put("storeNumber", 100)
                put("authorities",authorities)

            }}
        }

        PriceBand priceBandOriginal = new PriceBand(retailerId : 9, description: "Dummy description")
        priceBandOriginal.setId(1)

        Range rangeOriginal = new Range(retailerId: 9, description: "Dummy description")
        rangeOriginal.setId(1)

        Store storeSettings1 = new Store()
        storeSettings1.setType(StoreType.STORE.getValue())
        storeSettings1.setId(storeIdInDb)
        storeSettings1.setStoreId(storeId)
        storeSettings1.setRetailerId(retailerId)
        storeSettings1.setPrintReceiptOption(PrintReceiptOption.ALWAYS_PRINT)
        storeSettings1.setPriceBand(priceBandOriginal)
        storeSettings1.setRange(rangeOriginal)
        storeSettings1.setCountIncrement(new BigDecimal(0.01))
        storeSettings1.springSecurityService = controller.springSecurityService

        mockDomain(Store, [storeSettings1, priceBandOriginal, rangeOriginal])

        storeSettings1.save(flush: true, failOnError: true)
        priceBandOriginal.save(flush: true, failOnError: true)
        rangeOriginal.save(flush: true, failOnError: true)

        when: 'The index action is executed'
        params['countIncrement'] = countIncrement
        controller.save()

        then: 'The model index render'
        assert view == '/storeSettings/index'
        assert model.availablePriceBands
        assert model.availableProductRanges
        assert model.availableParentStores
        assert model.availablePrintReceiptOptions
        assert model.availablePrintReceiptOptions.size() == 3
        assert model.viewOptions
        assert model.viewOptions.showUISettings == false
        assert model.viewOptions.showParentStoreSettings == false


        where: 'Pass following input parameters'
        retailerId    || storeId || storeIdInDb    || role1                   || role2
        9             || 234     || 234            || "ROLE_HEAD_OFFICE"      || "ROLE_ENGINEER"
        9             || null    || 234            || "ROLE_ENGINEER"         || "ROLE_ENGINEER"
    }

    //------------------Calling Save Action (Validation Successful Scenario)-------------//

    def 'Test the save action for successfully validation result in return index page with successful flash message'() {

        given:

        BigDecimal countIncrement = new BigDecimal(0.05)
        PrintReceiptOption printReceiptOption = PrintReceiptOption.PROMPT

        PriceBand priceBandNew = new PriceBand(retailerId : 9, description: "Dummy description")
        priceBandNew.setId(newPriceBandId)

        Range rangeNew = new Range(retailerId: 9, description: "Dummy description")
        rangeNew.setId(newRangeId)

        Gson gson = new GsonBuilder().create()

        controller.retailerId = retailerId
        controller.storeId = storeId
        controller.storeNumber = 100

        Integer principalStoreId = storeId
        Integer principalRetailerId = retailerId

        SimpleGrantedAuthority simpleGrantedAuthority1 = new SimpleGrantedAuthority("ROLE_HEAD_OFFICE")
        Collection authorities = Arrays.asList(simpleGrantedAuthority1)

        controller.storeService = Stub(StoreService){}

        controller.springSecurityService = Stub(SpringSecurityService) {

            getPrincipal() >>new HashMap(){{
                put("retailerId", principalRetailerId)
                put("storeId", principalStoreId)
                put("storeNumber", 100)
                put("authorities",authorities)

            }}
        }

        controller.rabbitService = Stub(RabbitService){
            sendExchangeMessage(_,_,_) >> null
        }

        controller.gsonProvider = Stub(GsonProvider){
            getGson() >> gson
        }

        PriceBand priceBandOriginal = new PriceBand(retailerId : 9, description: "Dummy description")
        priceBandOriginal.setId(originalPriceBandId)


        Range rangeOriginal = new Range(retailerId: 9, description: "Dummy description")
        rangeOriginal.setId(originalRangeId)

        Store storeSettings1 = new Store()
        storeSettings1.setType(StoreType.STORE.getValue())
        storeSettings1.setId(storeIdInDb)
        storeSettings1.setStoreId(storeId)
        storeSettings1.setRetailerId(retailerId)
        storeSettings1.setPrintReceiptOption(PrintReceiptOption.ALWAYS_PRINT)
        storeSettings1.setPriceBand(priceBandOriginal)
        storeSettings1.setRange(rangeOriginal)
        storeSettings1.setCountIncrement(new BigDecimal(0.01))
        storeSettings1.springSecurityService = controller.springSecurityService

        mockDomain(Store, [storeSettings1, priceBandOriginal, rangeOriginal])

        storeSettings1.save(flush: true, failOnError: true)
        priceBandOriginal.save(flush: true, failOnError: true)
        rangeOriginal.save(flush: true, failOnError: true)

        when: 'The index action is executed'
        params['countIncrement'] = countIncrement
        params['printReceiptOption'] = printReceiptOption
        params['priceBand'] = priceBandNew
        params['range'] = rangeNew
        controller.save()

        then: 'The model index render'
        if (originalPriceBandId != newPriceBandId || originalRangeId != newRangeId) {
            assert flash.message ==
                    ["Store settings saved successfully.", "As the store's range or price band have changed, the store's tills need to be synced in order to receive the necessary product changes.", "Please perform this operation from the Till Connectivity page in the Monitoring menu."]
        } else {
            assert flash.message ==
                    ["Store settings saved successfully."]
        }
        assert response.redirectedUrl.startsWith('/storeSettings/index')

        where: 'Pass following input parameters'
        retailerId || storeId || storeIdInDb || originalPriceBandId || originalRangeId || newPriceBandId || newRangeId
        9          || 234     || 234         || 1                   || 1               || 1              || 1
        9          || 234     || 234         || 1                   || 1               || 2              || 1
        9          || 234     || 234         || 1                   || 1               || 2              || 2
        9          || 234     || 234         || 1                   || 1               || 1              || 2
        9          || null    || 234         || 1                   || 1               || 2              || 1

    }
}
