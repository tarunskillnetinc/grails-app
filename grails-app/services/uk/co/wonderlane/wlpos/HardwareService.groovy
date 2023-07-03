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

    def saveHardware(TillStock hardware) {
        hardware.setStoreId(springSecurityService.principal.storeId)
        hardware.setRetailerId(springSecurityService.principal.retailerId)
        hardware.save(flush: true)
    }

    def getHardwareBySerialNumber(String serialNumber) {
        return TillStock.withCriteria {
            eq("serialNumber", serialNumber)
        } ?: null
    }

    // This just returns a list of strings (serial numbers).
    def getSerialsInStock() {
        def tillStockCriteria = TillStock.createCriteria()

        def serialsInStock = tillStockCriteria.list {
            projections {
                property("serialNumber")
            }
        }

        return serialsInStock
    }
}
