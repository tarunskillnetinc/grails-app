package uk.co.wonderlane.wlpos

import grails.gorm.transactions.Transactional

@Transactional
class TillAssignmentService {

    def springSecurityService

    def getTills() {
        return TillConfiguration.findAllByRetailerId(springSecurityService.principal.retailerId)
    }

    def getTillsByAllFilters(int storeIdValue, int tillIdValue, String serialNumberValue) {
        return TillConfiguration.findAllByRetailerIdAndStoreIdAndTillIdAndSerialNumberLike(springSecurityService.principal.retailerId, storeIdValue, tillIdValue, "%"+serialNumberValue+"%")
    }

    def getTillsByStoreIdAndSerialNumber(int storeIdValue, String serialNumberValue) {
        return TillConfiguration.findAllByRetailerIdAndStoreIdAndSerialNumberLike(springSecurityService.principal.retailerId, storeIdValue, "%"+serialNumberValue+"%")
    }

    def getTillsByTillIdAndSerialNumber(int tillIdValue, String serialNumberValue) {
        return TillConfiguration.findAllByRetailerIdAndTillIdAndSerialNumberLike(springSecurityService.principal.retailerId, tillIdValue, "%"+serialNumberValue+"%")
    }

    def getTillsByStoreId(int storeIdValue) {
        return TillConfiguration.findAllByRetailerIdAndStoreId(springSecurityService.principal.retailerId, storeIdValue)
    }

    def getTillsByStoreIdAndTillId(int storeIdValue, int tillIdValue) {
        return TillConfiguration.findAllByRetailerIdAndStoreIdAndTillId(springSecurityService.principal.retailerId, storeIdValue, tillIdValue)
    }

    def getTillsByTillId(int tillIdValue) {
        return TillConfiguration.findAllByRetailerIdAndTillId(springSecurityService.principal.retailerId, tillIdValue)
    }

    def getTillsBySerialNumber(String serialNumber) {
        return TillConfiguration.findAllByRetailerIdAndSerialNumberLike(springSecurityService.principal.retailerId, "%" + serialNumber + "%")
    }
}
