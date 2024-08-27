package uk.co.wonderlane.wlpos

import grails.gorm.transactions.Transactional

@Transactional
class CashManagementService {

    def sessionFactory

    def getCashManagement(int retailerId, Integer storeId) {
        if (storeId != null) {
            return CashManagement.findByRetailerIdAndStoreId(retailerId, storeId)
        } else {
            return CashManagement.findByRetailerId(retailerId)
        }
    }
}
