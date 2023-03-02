package uk.co.wonderlane.wlpos

import grails.gorm.transactions.Transactional

@Transactional
class StoreNumberValidatorService implements StoreNumberValidator {

    @Override
    StoreSettings getStore(int retailerId, Integer storeId) {
        return StoreSettings.findByRetailerIdAndStoreId(retailerId, storeId)
    }
}