package uk.co.wonderlane.wlpos

import grails.gorm.transactions.Transactional

@Transactional
class StoreNumberValidatorService implements StoreNumberValidator {

    def storeService

    @Override
    Store getStore(int retailerId, Integer storeNumber) {
        return storeService.getStoreByStoreNumber(retailerId, storeNumber)
    }
}