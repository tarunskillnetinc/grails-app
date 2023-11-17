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

        if (sort.equals("transactionAmount")) {
            results = receiptsCriteria.list {
                eq("retailerId", springSecurityService.principal.retailerId)

                if (springSecurityService.principal.storeNumber != null) {
                    eq("storeId", springSecurityService.principal.storeNumber)
                }

                gte("dateGenerated", fromDate)
                lt("dateGenerated", toDate)

                if (tillId != null) {
                    eq("tillId", tillId)
                }

                if (transactionId != null) {
                    eq("transactionId", transactionId)
                }
            }

            results = results.sort { it?.receiptLines?.find{ it.type.name() == 'TOTAL' }?.total ?: BigDecimal.ZERO }

            if (order == "desc") {
                results = results.reverse()
            }

            totalCount = results.size()

            results = offset < results.size() ? results.subList(offset, (offset + max < results.size() ? offset + max : results.size())) : []
        } else {
            results = receiptsCriteria.list([offset: offset, max: max, sort: sort, order: order]) {
                eq("retailerId", springSecurityService.principal.retailerId)

                if (springSecurityService.principal.storeNumber != null) {
                    eq("storeId", springSecurityService.principal.storeNumber)
                }

                gte("dateGenerated", fromDate)
                lt("dateGenerated", toDate)

                if (tillId != null) {
                    eq("tillId", tillId)
                }

                if (transactionId != null) {
                    eq("transactionId", transactionId)
                }
            }
            
            totalCount = results.totalCount
        }

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