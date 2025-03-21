package uk.co.wonderlane.wlpos

import grails.gorm.transactions.Transactional
import uk.co.wonderlane.wlpos.dataaccess.MySqlPoolDal
import uk.co.wonderlane.wlpos.reporting.BasketTransaction

@Transactional("reporting")
class BasketTransactionService extends MySqlPoolDal {

    def gsonProvider
    def springSecurityService

    BasketTransactionService(databaseCredentials) {
        super(databaseCredentials)
    }

    def getBasketTransactionByReceipt(Receipt receipt, Store store) {
        BasketTransaction basketTransactionDB = BasketTransaction.findByRetailerIdAndStoreIdAndTillIdAndTransactionId(receipt.retailerId, store.id, receipt.tillId, receipt.transactionId)

        uk.co.wonderlane.wlpos.entities.transactionv2.BasketTransaction basketTransaction = gsonProvider.getGson().fromJson(basketTransactionDB.transactionObject, uk.co.wonderlane.wlpos.entities.transactionv2.BasketTransaction.class);

        return basketTransaction
    }
}
