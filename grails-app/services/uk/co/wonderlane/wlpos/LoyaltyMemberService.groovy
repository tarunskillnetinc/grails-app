package uk.co.wonderlane.wlpos

import uk.co.wonderlane.wlpos.loyalty.Member
import grails.gorm.transactions.Transactional

@Transactional("loyalty")
class LoyaltyMemberService {

    /* Performs a search for a member by email address */
    def findByEmail(String emailAddress) {
        def members = Member.findAllByEmail(emailAddress)

        [totalResults: members.size(), members: members]
    }

    /* Performs a wildcard search for a member by card number */
    def findByCardNumber(String cardNumber, Integer max, Integer offset, String sortColumn, String sortOrder) {
        max = max ?: 20
        offset = offset ?: 0

        def memberCriteria = Member.createCriteria()
        def totalCount = Member.findAllByCardNumberLike("%$cardNumber%").size()

        def members = memberCriteria.list([max: max, offset: offset]) {
            like ("cardNumber", "%$cardNumber%")
            order(sortColumn ?: "cardNumber", sortOrder ?: "asc")
        }

        [totalResults: totalCount, members: members]
    }
}