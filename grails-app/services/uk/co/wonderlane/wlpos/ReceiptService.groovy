package uk.co.wonderlane.wlpos

import grails.gorm.transactions.Transactional
import org.joda.time.DateTime

@Transactional("transactions")
class ReceiptService {

    def springSecurityService

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
            lt("dateGenerated", toDate)

            if (tillId) {
                eq("tillId", tillId)
            }

            if (transactionId) {
                eq("transactionId", transactionId)
            }
        }

        totalCount = results.totalCount

        return [results, totalCount]
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
}