package uk.co.wonderlane.wlpos

import grails.gorm.transactions.Transactional
import org.joda.time.DateTime
import uk.co.wonderlane.wlpos.loyalty.MemberTransaction

@Transactional("loyalty")
class MemberTransactionService {

    /* Returns the Member Transaction for the supplied member id and transaction id */
    def findTransactionByMemberIdAndTransactionId(Integer memberId, Integer transactionId) {
        MemberTransaction.findByMemberIdAndTransactionId(memberId, transactionId)
    }

    /* Returns all Member Transactions for the passed in search parameters */
    def findAllTransactionsByMemberId(Integer id, String searchTerm, String searchBy, Double minAmount, Double maxAmount, DateTime startWindow, DateTime endWindow,
                                        Integer max, Integer offset, String sortColumn, String sortOrder) {
        max = max ?: 20
        offset = offset ?: 0

        def memberTransactions = MemberTransaction.createCriteria()

        def transactions = memberTransactions.list() {
            eq("memberId", id)

            if (searchTerm.length() > 0) {
                eq(searchBy, searchTerm.toInteger())
            }

            if (minAmount != 0 && maxAmount != 0) {
                between("transactionTotal", minAmount, maxAmount)
            }

            if (startWindow != null && endWindow != null) {
                between("transactionTimestamp", startWindow, endWindow)
            }

            order(sortColumn ?: "storeId", sortOrder ?: "asc")
        }

        def totalCount = transactions.size()

        if (max + offset < totalCount) {
            transactions = transactions.subList(offset, offset + max)
        } else {
            transactions = transactions.subList(offset, offset + (totalCount - offset))
        }

        [totalResults: totalCount, transactions: transactions]
    }
}