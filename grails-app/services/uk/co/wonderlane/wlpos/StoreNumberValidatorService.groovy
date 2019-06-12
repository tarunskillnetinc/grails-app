package uk.co.wonderlane.wlpos

import grails.gorm.transactions.Transactional

@Transactional
class StoreNumberValidatorService implements StoreNumberValidator {

    @Override
    boolean isValidStoreNumber(int retailerId, int storeId) {
        return (TillSettings.findByRetailerIdAndStoreId(retailerId, storeId) != null)
    }
}