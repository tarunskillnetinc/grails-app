package uk.co.wonderlane.wlpos

import grails.gorm.transactions.Transactional

@Transactional
class StoreNumberValidatorService implements StoreNumberValidator {

    @Override
    int getStoreId(int retailerId, int storeId) {
        return StoreSettings.findByRetailerIdAndStoreId(retailerId, storeId)?.id
    }
}