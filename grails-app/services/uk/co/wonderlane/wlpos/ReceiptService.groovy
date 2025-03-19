package uk.co.wonderlane.wlpos

import grails.gorm.transactions.Transactional
import org.hibernate.Session
import org.hibernate.Transaction
import org.joda.time.DateTime

@Transactional("transactions")
class ReceiptService {

    def springSecurityService
    def sessionFactory

    def getReceipts(DateTime fromDate, DateTime toDate, Integer tillId, Integer transactionId, String sort, String order, int offset, int max) {
        def receiptsCriteria = Receipt.createCriteria()

        def results
        def totalCount = 0

        results = receiptsCriteria.list([offset: offset, max: max, sort: sort, order: order]) {
            eq("retailerId", springSecurityService.principal.retailerId)

            if (springSecurityService.principal.storeNumber != null) {
                eq("storeId", springSecurityService.principal.storeNumber)
            }

            gte("dateGenerated", fromDate)
            lte("dateGenerated", toDate)

            if (tillId) {
                eq("tillId", tillId)
            }

            if (transactionId) {
                eq("transactionId", transactionId)
            }
        }

        totalCount = results.totalCount

        // Fetch all stores for this retailer
        def stores = Store.withNewSession { session ->
            Store.findAllByRetailerId(springSecurityService.principal.retailerId)
        }

        // Combine Receipt and Store data
        def combinedResults = results.collect { receipt ->
            def store = stores.find { store ->
                def storeConfig = store.getConfig()
                storeConfig.storeNumber == receipt.storeId
            }
            [receipt: receipt, store: store]
        }

        return [combinedResults, totalCount]
    }

    def getReceipt(int receiptId) {
        def receiptCriteria = Receipt.createCriteria()

        return receiptCriteria.get() {
            eq ("id", receiptId)
            eq ("retailerId", springSecurityService.principal.retailerId)

            if (springSecurityService.principal.storeNumber != null) {
                eq ("storeId", springSecurityService.principal.storeNumber)
            }
        }
    }

    def getReceipt(int transactionId, int storeId, int tillId) {
        def receiptCriteria = Receipt.createCriteria()

        return receiptCriteria.get() {
            eq ("transactionId", transactionId)
            eq ("storeId", storeId)
            eq("tillId", tillId)
            eq ("retailerId", springSecurityService.principal.retailerId)
        }
    }
    def saveReceiptPrinted(int receiptId) {
            Session session = sessionFactory.openSession()
            Transaction transaction = session.beginTransaction()
            Receipt receipt = Receipt.get(receiptId)

        if(receipt){
            receipt.printed = 1
            receipt.save(flush: true)
        }

            transaction.commit()
            session.close()

    }
}