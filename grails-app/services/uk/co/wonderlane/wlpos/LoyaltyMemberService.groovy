package uk.co.wonderlane.wlpos

import uk.co.wonderlane.wlpos.enums.MemberOfferStatus
import uk.co.wonderlane.wlpos.loyalty.Member
import uk.co.wonderlane.wlpos.loyalty.MemberOffer
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

    def findByCardNumber(String cardNumber) {
        Member.findByCardNumber(cardNumber)
    }

    def updateMemberField(String cardNumber, String fieldToUpdate, String updatedValue) {
        def member = Member.findByCardNumber(cardNumber)

        if (member) {
            // Perform validation and update based on the fieldToUpdate parameter
            switch (fieldToUpdate) {
                case "firstName":
                    member.firstName = updatedValue
                    break
                case "lastName":
                    member.lastName = updatedValue
                    break
                case "email":
                    member.email = updatedValue
                    break
                case "mobile_no":
                    member.mobile_no = updatedValue
                    break
                default:
                    throw new IllegalArgumentException("Invalid fieldToUpdate parameter.")
            }

            // Save the updated member
            if (member.validate()) {
                member.save(flush: true)
            }
        }
    }

    def getMemberOffer(Integer id) {
        MemberOffer.get(id)
    }

    def findAllMemberOffers(String cardNumber, String searchTerm, String searchBy, Boolean activeOffers, Boolean inactiveOffers, Integer max, Integer offset, String sortColumn, String sortOrder) {
        def memberCriteria = Member.createCriteria()

        def member = memberCriteria.list([max: max, offset: offset]) {
            eq ("cardNumber", "$cardNumber")
        }

        def totalCount = 0
        def filteredOffers = []

        if (member) {
            def offers = member.offers

            // Count total results before applying pagination
            totalCount = MemberOffer.createCriteria().count {
                if (searchBy == "description") {
                    like ("offerDescription", "%$searchTerm%")
                } else {
                    like ("id", "%$searchTerm%")
                }

                if (activeOffers) {
                    or {
                        eq("status", MemberOfferStatus.ACTIVE)
                        eq("status", MemberOfferStatus.OPEN)
                    }
                }

                if (inactiveOffers) {
                    or {
                        eq("status", MemberOfferStatus.CLOSED)
                        eq("status", MemberOfferStatus.LIMITS)
                    }
                }
            }

            // Creating another criteria query for filtering offers
            def offerCriteria = MemberOffer.createCriteria()

            // Apply pagination and additional criteria
            filteredOffers = offerCriteria.list(max: max, offset: offset) {
                if (searchBy == "description") {
                    like ("offerDescription", "%$searchTerm%")
                } else {
                    like ("id", "%$searchTerm%")
                }

                if (activeOffers) {
                    or {
                        eq("status", MemberOfferStatus.ACTIVE)
                        eq("status", MemberOfferStatus.OPEN)
                    }
                }

                if (inactiveOffers) {
                    or {
                        eq("status", MemberOfferStatus.CLOSED)
                        eq("status", MemberOfferStatus.LIMITS)
                    }
                }

                order(sortColumn ?: "offerDescription", sortOrder ?: "asc")
            }
        }

        [totalResults: totalCount, offers: filteredOffers]
    }

    def updateMemberOfferField(Integer id, String fieldToUpdate, String updatedValue) {
        def updated = false
        def memberOffer = MemberOffer.findById(id)

        if (memberOffer) {
            // Perform validation and update based on the fieldToUpdate parameter
            switch (fieldToUpdate) {
                case "remainingRedemptions":
                    def remainingRedemptions = updatedValue ? Integer.parseInt(updatedValue) : null

                    if (remainingRedemptions != memberOffer?.remainingRedemptions ?: null) {
                        memberOffer.maxRedemptions = remainingRedemptions + memberOffer.currentRedemptions
                        updated = true
                    }
                    break
                case "status":
                    def status = updatedValue ? MemberOfferStatus.valueOf(updatedValue) : null

                    if (status != memberOffer?.status ?: null) {
                        memberOffer.status =  status
                        updated = true
                    }
                    break
                default:
                    throw new IllegalArgumentException("Invalid fieldToUpdate parameter.")
            }

            if (updated) {
                if (memberOffer.validate()) {
                    memberOffer.save(flush: true)
                }
            }
        }

        return updated
    }
}