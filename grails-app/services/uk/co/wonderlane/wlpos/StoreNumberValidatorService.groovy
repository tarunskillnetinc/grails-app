package uk.co.wonderlane.wlpos

import grails.gorm.transactions.Transactional

@Transactional
class StoreNumberValidatorService implements StoreNumberValidator {

    @Override
    Integer getStoreId(int retailerId, Integer storeId) {
        return StoreSettings.findByRetailerIdAndStoreId(retailerId, storeId)?.id
    }
}