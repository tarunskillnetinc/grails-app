package uk.co.wonderlane.wlpos

import grails.testing.gorm.DataTest;
import grails.testing.services.ServiceUnitTest
import org.grails.datastore.mapping.query.api.BuildableCriteria;
import org.joda.time.DateTime
import spock.lang.Specification
import uk.co.wonderlane.wlpos.enums.MemberTransactionStatus
import uk.co.wonderlane.wlpos.helpers.HibernateTestMockCriteria
import uk.co.wonderlane.wlpos.loyalty.MemberTransaction;

class MemberTransactionServiceSpec extends Specification implements ServiceUnitTest<MemberTransactionService>, DataTest {
    def setup() {}
    def cleanup() {}

    Class<?>[] getDomainClassesToMock() {
        return [MemberTransaction] as Class[]
    }

    def 'Should successfully find all transactions by member Id '() {
        given:
        def now = new DateTime()
        List<MemberTransaction> memberTransactions = [
                getMockMemberTransaction(1, 5, 7, 13, 1, 8),
                getMockMemberTransaction(2, 5, 7, 13, 1, 9),
                getMockMemberTransaction(3, 5, 7, 13, 1, 10)
        ]

        HibernateTestMockCriteria mockCriteria = new HibernateTestMockCriteria()
        mockCriteria.getResponses().addAll(memberTransactions)
        BuildableCriteria defaultCriteria = MemberTransaction.createCriteria()
        MemberTransaction.metaClass.static.createCriteria = { return mockCriteria }

        when: 'Get all transactions action is executed'
        def transactionsReturned= service.findAllTransactionsByMemberId(7, "", "", 0, 0,
                now.minusWeeks(1), now.plusWeeks(1), 20, 0, "", "asc")

        then: 'successfully return member transactions'
        transactionsReturned != null
        transactionsReturned instanceof Map
        transactionsReturned.totalResults == 3

        cleanup:
        MemberTransaction.metaClass.static.createCriteria = { return defaultCriteria }
    }

    def 'Should successfully find a transaction with the specified member Id and transaction Id'() {
        given:
        MemberTransaction expectedTransaction = getMockMemberTransaction(1, 5, 7, 13, 1, 8)

        MemberTransaction.metaClass.static.findByMemberIdAndTransactionId = { Integer memberId, Integer transactionId ->
            if (memberId == expectedTransaction.memberId && transactionId == expectedTransaction.transactionId) {
                return expectedTransaction
            } else {
                return null
            }
        }

        when: 'Get transaction by member Id and transaction Id action is executed'
        def transactionReturned = service.findTransactionByMemberIdAndId(expectedTransaction.memberId, expectedTransaction.transactionId)

        then: 'successfully return the transaction'
        transactionReturned != null
        transactionReturned instanceof MemberTransaction
        transactionReturned.memberId == expectedTransaction.memberId
        transactionReturned.transactionId == expectedTransaction.transactionId

        cleanup:
        MemberTransaction.metaClass = null
    }

    def getMockMemberTransaction(int id, int retailerId, int memberId, int storeId, int terminalId, int transactionId){
        MemberTransaction memberTransaction = new MemberTransaction(
            id: id,
            retailerId: retailerId,
            memberId: memberId,
            storeId: storeId,
            terminalId: terminalId,
            transactionId: transactionId,
            transactionTimestamp: new DateTime(),
            redeemableOffers: "Dummy redeemable offers",
            transactionTotal: 100.0,
            transactionDiscount: 10.0,
            transactionPoints: 50,
            transactionStamps: 5,
            status: MemberTransactionStatus.COMPLETED,
            dateCreated: new DateTime(),
            dateModified: new DateTime()
        )

        return memberTransaction
    }
}