package uk.co.wonderlane.wlpos

import grails.gorm.transactions.Transactional
import uk.co.wonderlane.wlpos.dataaccess.DatabaseCredentials
import uk.co.wonderlane.wlpos.dataaccess.MySqlDal

@Transactional
class HardwareService extends MySqlDal {
    def springSecurityService

    HardwareService(DatabaseCredentials databaseCredentials) {
        super(databaseCredentials)
    }

    def saveHardware(Hardware hardware) {
        hardware.setStoreId(springSecurityService.principal.storeId)
        hardware.setRetailerId(springSecurityService.principal.retailerId)
        hardware.save()
    }

    def getHardwareBySerialNumber(String serialNumber) {
        return Hardware.withCriteria {
            eq("serialNumber", serialNumber)
        }?.first() ?: null
    }
}
