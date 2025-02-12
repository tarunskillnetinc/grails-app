package uk.co.wonderlane.wlpos

import grails.gorm.transactions.Transactional
import org.joda.time.DateTime

@Transactional
class TillAssignmentService {

    def springSecurityService

    def getTills() {
        return TillConfiguration.findAllByRetailerId(springSecurityService.principal.retailerId)
    }

    def getTill(int id) {
        return TillConfiguration.findByRetailerIdAndId(springSecurityService.principal.retailerId, id)
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

    def getTillBySerialNumber(String serialNumber) {
        return TillConfiguration.findByRetailerIdAndSerialNumber(springSecurityService.principal.retailerId, serialNumber)
    }

    def getUnassignedTillStock() {
        return TillStock.findAll("FROM TillStock ts WHERE NOT EXISTS (SELECT 1 FROM TillConfiguration tc WHERE tc.serialNumber = ts.serialNumber AND ts.retailerId = tc.retailerId) AND ts.retailerId = :retailerId ORDER BY ts.serialNumber ASC", [retailerId: springSecurityService.principal.retailerId])
    }

    def deleteEntryForStoreIdAndTillId(int storeIdValue, int tillIdValue) {
        def tills = getTillsByStoreIdAndTillId(storeIdValue, tillIdValue)
        tills.first().delete()
    }

    def saveTill(TillConfiguration newTill) {
        newTill.save()
    }

    def updateTillStockBySerial(String serialNumber) {
        // Update this serial number in Till Stock to remove the Store ID + Till ID for reallocation
        def tillStockEntry = TillStock.findBySerialNumber(serialNumber)
        if (tillStockEntry != null) {
            tillStockEntry.storeId = null
            tillStockEntry.tillId = null
            tillStockEntry.dateUpdated = DateTime.now()
            tillStockEntry.save(flush: true)
        }
    }

    def updateTillStock(TillConfiguration newTill) {
        // Update this serial number in Till Stock to allocate it to the Store ID + Till ID
        def tillStockEntry = TillStock.findBySerialNumber(newTill.serialNumber)
        tillStockEntry.storeId = newTill.storeId
        tillStockEntry.tillId = newTill.tillId
        tillStockEntry.dateUpdated = DateTime.now()
        tillStockEntry.save(flush: true)
    }

    def updateTillConfiguration(String serialNumber, int pin, DateTime expiry) {
        def tillConfiguration = getTillBySerialNumber(serialNumber)
        tillConfiguration.pin = pin
        tillConfiguration.pinExpiry = expiry
        tillConfiguration.dateTimeUpdated = DateTime.now()
        tillConfiguration.save()
    }
}
