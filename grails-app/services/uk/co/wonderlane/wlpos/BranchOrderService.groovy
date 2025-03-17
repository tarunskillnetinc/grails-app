package uk.co.wonderlane.wlpos

import grails.gorm.transactions.Transactional
import org.hibernate.Session
import org.hibernate.Transaction
import org.joda.time.DateTime
import uk.co.wonderlane.wlpos.dataaccess.DatabaseCredentials
import uk.co.wonderlane.wlpos.dataaccess.MySqlDal
import uk.co.wonderlane.wlpos.enums.wlim.ProductListStatus
import uk.co.wonderlane.wlpos.enums.wlim.ProductListType

@Transactional
class BranchOrderService extends MySqlDal {

    def springSecurityService
    def sessionFactory

    BranchOrderService(DatabaseCredentials databaseCredentials) {
        super(databaseCredentials)
    }

    def getBranchOrderBySupplierReference(String supplierReference) {
        return BranchOrder.withCriteria {
            eq("supplierReference", supplierReference)
            eq("type", ProductListType.BRANCH_ORDER)
        } ?: null
    }

    def setBranchOrdersScheduled(List<BranchOrder> branchOrders) {
        Session session = sessionFactory.openSession()
        Transaction transaction = session.beginTransaction()

        branchOrders.each { branchOrder ->
            branchOrder.endDate = DateTime.now()
            branchOrder.status = ProductListStatus.SCHEDULED
            session.update(branchOrder)
        }

        transaction.commit()
        session.close()
    }
}
