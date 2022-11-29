package uk.co.wonderlane.wlpos

import grails.plugin.springsecurity.SpringSecurityService
import grails.testing.gorm.DataTest
import grails.testing.services.ServiceUnitTest
import spock.lang.Specification
import uk.co.wonderlane.wlpos.enums.PrintReceiptOption

class StoreSettingsServiceSpec extends Specification implements ServiceUnitTest<StoreSettingsService>, DataTest{

    Class<?>[] getDomainClassesToMock(){
        return [StoreSettings, PriceBand, Range] as Class[]
    }

    def 'Test the save store settings action for successfully save store settings'() {

        given:

        Integer principalStoreId = storeId
        Integer principalRetailerId = retailerId
        int priceBandId = 1
        int rangeId = 1

        def springSecurityService = Stub(SpringSecurityService) {

            getPrincipal() >>new HashMap(){{
                put("retailerId", principalRetailerId);
                put("storeId", principalStoreId);
                put("storeNumber", 100)
            }}
        }


        PriceBand priceBandOriginal = new PriceBand(retailerId : 9, description: "Dummy description")
        priceBandOriginal.setId(priceBandId)

        Range rangeOriginal = new Range(retailerId: 9, description: "Dummy description")
        rangeOriginal.setId(rangeId)

        StoreSettings storeSettings1 = new StoreSettings()
        storeSettings1.setType(StoreType.STORE.getValue())
        if (storeId){
            storeSettings1.setId(storeId)
            storeSettings1.setStoreId(storeId)
        }
        storeSettings1.setRetailerId(retailerId)
        storeSettings1.setPrintReceiptOption(PrintReceiptOption.ALWAYS_PRINT)
        storeSettings1.setPriceBand(priceBandOriginal)
        storeSettings1.setRange(rangeOriginal)
        storeSettings1.setCountIncrement(new BigDecimal(0.01))
        storeSettings1.springSecurityService = springSecurityService

        when: 'The save store settings action is executed'
        service.saveStoreSettings(storeSettings1)

        StoreSettings insertStoreSetting = principalStoreId ? StoreSettings.findById(principalStoreId) : StoreSettings.findByRetailerId(principalRetailerId)

        then: 'successfully save store settings'
        insertStoreSetting
        if (storeId){
            insertStoreSetting.id == storeId
        } else {
            insertStoreSetting.id == 0
        }
        insertStoreSetting.type == StoreType.STORE.getValue()
        insertStoreSetting.retailerId == principalRetailerId
        insertStoreSetting.printReceiptOption == PrintReceiptOption.ALWAYS_PRINT
        insertStoreSetting.priceBand.id == priceBandId
        insertStoreSetting.range.id == rangeId
        insertStoreSetting.countIncrement.compareTo(new BigDecimal(0.01)) == 0

        where: 'Pass following input parameters'
        retailerId    || storeId
        9             || 234
        9             || null
    }


}
