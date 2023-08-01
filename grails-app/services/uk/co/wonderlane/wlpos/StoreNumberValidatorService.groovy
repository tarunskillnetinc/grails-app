package uk.co.wonderlane.wlpos

import grails.gorm.transactions.Transactional

@Transactional
class StoreNumberValidatorService implements StoreNumberValidator {

    def storeSettingsService

    @Override
    Store getStore(int retailerId, Integer storeNumber) {
        return storeSettingsService.getStoreByStoreNumber(retailerId, storeNumber)
    }
}