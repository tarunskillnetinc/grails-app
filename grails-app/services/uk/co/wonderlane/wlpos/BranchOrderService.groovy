package uk.co.wonderlane.wlpos

import grails.gorm.transactions.Transactional
import org.hibernate.Session
import org.hibernate.Transaction
import uk.co.wonderlane.wlpos.dataaccess.DatabaseCredentials
import uk.co.wonderlane.wlpos.dataaccess.MySqlDal

@Transactional
class BranchOrderService extends MySqlDal {

    def springSecurityService
    def sessionFactory

    BranchOrderService(DatabaseCredentials databaseCredentials) {
        super(databaseCredentials)
    }

    /*def saveBranchOrder(TillStock hardware) {
        hardware.setStoreId(springSecurityService.principal.storeId)
        hardware.setRetailerId(springSecurityService.principal.retailerId)
        hardware.save(flush: true)
    }

    def saveBranchOrder(List<TillStock> tillStocks) {
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
    }*/

    def getBranchOrderBySupplierReference(String supplierReference) {
        return BranchOrder.withCriteria {
            eq("supplierReference", supplierReference)
            eq("type", "BRANCH_ORDER")
        } ?: null
    }
}
