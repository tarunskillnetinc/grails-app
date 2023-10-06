package uk.co.wonderlane.wlpos

import grails.gorm.transactions.Transactional
import org.hibernate.Session
import org.hibernate.Transaction
import uk.co.wonderlane.wlpos.dataaccess.DatabaseCredentials
import uk.co.wonderlane.wlpos.dataaccess.MySqlDal

@Transactional
class HardwareService extends MySqlDal {

    def springSecurityService
    def sessionFactory

    HardwareService(DatabaseCredentials databaseCredentials) {
        super(databaseCredentials)
    }

    def saveHardware(TillStock hardware) {
        hardware.setStoreId(springSecurityService.principal.storeId)
        hardware.setRetailerId(springSecurityService.principal.retailerId)
        hardware.save(flush: true)
    }

    def saveHardware(List<TillStock> tillStocks) {
        Session session = sessionFactory.openSession()
        Transaction transaction = session.beginTransaction()

        tillStocks.eachWithIndex { tillStock, index ->
            tillStock.setStoreId(springSecurityService.principal.storeId)
            tillStock.setRetailerId(springSecurityService.principal.retailerId)
            session.saveOrUpdate(tillStock)

            // Clear the session for speed purposes.
            if (index.mod(500) == 0) {
                session.flush()
                session.clear()
            }
        }

        transaction.commit()
        session.close()
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
