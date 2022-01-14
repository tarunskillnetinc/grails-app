package uk.co.wonderlane.wlpos

import grails.gorm.transactions.Transactional
import org.joda.time.DateTime

@Transactional("transactions")
class ReceiptService {

    def springSecurityService

    def getReceipts(DateTime fromDate, DateTime toDate, Integer tillId, Integer transactionId, int offset, int max) {
        def receiptsCriteria = Receipt.createCriteria()

        def results = receiptsCriteria.list([offset: offset, max: max, sort: "dateGenerated", order: "DESC"]) {
            eq("retailerId", springSecurityService.principal.retailerId)

            if (springSecurityService.principal.storeId != null) {
                eq("storeId", springSecurityService.principal.storeId)
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

        // Criteria.list() with max and offset returns a totalCount, but for some reason I am having to read that value otherwise an error is thrown when trying to use it back in the controller.
        // I believe this may be related to the domain class being in an alternate datasource, but I think it's a bug in Grails. Actually, I think it's because the totalCount is lazily loaded
        // to prevent the double query immediately. But it's throwing a Hibernate session error if I don't request it here.
        int totalCount = results.totalCount

        return results
    }

    def getReceipt(int receiptId) {
        def receiptCriteria = Receipt.createCriteria()

        return receiptCriteria.get() {
            eq ("id", receiptId)
            eq ("retailerId", springSecurityService.principal.retailerId)

            if (springSecurityService.principal.storeId != null) {
                eq ("storeId", springSecurityService.principal.storeId)
            }
        }
    }
}