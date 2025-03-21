package uk.co.wonderlane.wlpos

import grails.gorm.transactions.Transactional
import uk.co.wonderlane.wlpos.reporting.BasketTransaction

@Transactional
class BasketTransactionService {

    def BasketTransaction getBasketTransactionByReceipt(Receipt receipt) {
        BasketTransaction.findByRetailerIdAndStoreIdAndTillIdAndTransactionId(receipt.retailerId, receipt.storeId, receipt.tillId, receipt.transactionId)
    }
}
