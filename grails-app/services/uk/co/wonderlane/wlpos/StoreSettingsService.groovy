package uk.co.wonderlane.wlpos

import grails.gorm.transactions.Transactional

@Transactional
class StoreSettingsService {

    def saveStoreSettings(StoreSettings storeSettings) {
        storeSettings.save()
    }
}