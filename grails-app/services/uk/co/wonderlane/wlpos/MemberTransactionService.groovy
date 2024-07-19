package uk.co.wonderlane.wlpos

import grails.gorm.transactions.Transactional
import org.hibernate.sql.JoinType
import org.joda.time.DateTime
import uk.co.wonderlane.wlpos.loyalty.MemberTransaction

@Transactional("loyalty")
class MemberTransactionService {

    /* Returns the Member Transaction for the supplied member id and transaction id */
    def findTransactionByMemberIdAndId(Integer memberId, Integer id) {
        MemberTransaction.findByMemberIdAndId(memberId, id)
    }

    /* Returns all Member Transactions for the passed in search parameters */
    def findAllTransactionsByMemberId(Integer id, String searchTerm, String searchBy, BigDecimal minAmount, BigDecimal maxAmount, DateTime startWindow, DateTime endWindow,
                                        Integer max, Integer offset, String sortColumn, String sortOrder) {
        max = max ?: 20
        offset = offset ?: 0

        def memberTransactions = MemberTransaction.createCriteria()

        def transactions = memberTransactions.list() {
            eq("memberId", id)

            if (searchTerm.length() > 0) {
                eq(searchBy, searchTerm.toInteger())
            }

            if (minAmount != 0 || maxAmount != 0) {
                if (minAmount != 0 && maxAmount != 0) {
                    between("transactionTotal", minAmount, maxAmount)
                } else if (minAmount != 0) {
                    ge("transactionTotal", minAmount)
                } else {
                    le("transactionTotal", maxAmount)
                }
            }

            if (startWindow != null || endWindow != null) {
                if (startWindow != null && endWindow != null) {
                    between("transactionTimestamp", startWindow, endWindow)
                } else if (startWindow != null) {
                    ge("transactionTimestamp", startWindow)
                } else {
                    le("transactionTimestamp", endWindow)
                }
            }

            if (sortColumn == "storeName") {
                createAlias("store", "s", JoinType.LEFT_OUTER_JOIN)
                order("s.name", sortOrder ?: "asc")
            } else {
                order(sortColumn ?: "storeId", sortOrder ?: "asc")
            }
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