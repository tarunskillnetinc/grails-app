package uk.co.wonderlane.wlpos

import grails.plugin.springsecurity.SpringSecurityService
import grails.testing.gorm.DataTest
import grails.testing.services.ServiceUnitTest
import spock.lang.Specification
import uk.co.wonderlane.wlpos.enums.PrintReceiptOption

class StoreServiceSpec extends Specification implements ServiceUnitTest<StoreSettingsService>, DataTest{

    Class<?>[] getDomainClassesToMock(){
        return [Store, PriceBand, Range] as Class[]
    }

    //------------------- Calling Save Store Settings Action ---------------------------------------------//

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

        Store storeSettings1 = new Store()
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

        Store insertStoreSetting = principalStoreId ? Store.findById(principalStoreId) : Store.findByRetailerId(principalRetailerId)

        then: 'successfully save store settings'
        assert insertStoreSetting != null
        if (storeId){
            assert insertStoreSetting.id == storeId
        } else {
            assert insertStoreSetting.id == 0
        }
        assert insertStoreSetting.type == StoreType.STORE.getValue()
        assert insertStoreSetting.retailerId == principalRetailerId
        assert insertStoreSetting.printReceiptOption == PrintReceiptOption.ALWAYS_PRINT
        assert insertStoreSetting.priceBand.id == priceBandId
        assert insertStoreSetting.range.id == rangeId
        assert insertStoreSetting.countIncrement.compareTo(new BigDecimal(0.01)) == 0

        where: 'Pass following input parameters'
        retailerId    || storeId
        9             || 234
        9             || null
    }


}
