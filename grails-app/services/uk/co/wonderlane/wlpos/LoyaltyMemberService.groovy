package uk.co.wonderlane.wlpos

import grails.gorm.transactions.Transactional
import org.joda.time.DateTime
import uk.co.wonderlane.wlpos.enums.MemberOfferStatus
import uk.co.wonderlane.wlpos.loyalty.Member
import uk.co.wonderlane.wlpos.loyalty.MemberOffer

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

    /* Returns a Member by the exact card number if one exists */
    def findByCardNumber(String cardNumber) {
        Member.findByCardNumber(cardNumber)
    }

    /* Updates the selected field with a new value for the Member with the supplied card number */
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

    /* Returns a Member Offer from the passed id */
    def getMemberOffer(Integer id) {
        MemberOffer.get(id)
    }

    /* Returns all Member Offers by the exact card number if any exist */
    def findAllMemberOffers(String cardNumber, String searchTerm, Boolean activeOffers, Boolean inactiveOffers, Integer max, Integer offset, String sortColumn, String sortOrder) {

        def totalCount = 0
        def filteredOffers = []

        def member = Member.createCriteria().get {
            eq("cardNumber", cardNumber)
        }

        if (member) {
            // Count total results before applying pagination
            totalCount = MemberOffer.createCriteria().count {

                eq("member.id", member.id)
                like ("offerDescription", "$searchTerm%")

                if (activeOffers || inactiveOffers) {
                    or {
                        if (activeOffers) {
                            eq("status", MemberOfferStatus.ACTIVE)
                            eq("status", MemberOfferStatus.OPEN)
                        }
                        if (inactiveOffers) {
                            eq("status", MemberOfferStatus.CLOSED)
                            eq("status", MemberOfferStatus.LIMITS)
                        }
                    }
                }
            }

            // Apply pagination and additional criteria
            filteredOffers = MemberOffer.createCriteria().list(max: max, offset: offset) {

                eq("member.id", member.id)
                like ("offerDescription", "$searchTerm%")

                if (activeOffers || inactiveOffers) {
                    or {
                        if (activeOffers) {
                            eq("status", MemberOfferStatus.ACTIVE)
                            eq("status", MemberOfferStatus.OPEN)
                        }
                        if (inactiveOffers) {
                            eq("status", MemberOfferStatus.CLOSED)
                            eq("status", MemberOfferStatus.LIMITS)
                        }
                    }
                }

                order(sortColumn ?: "offerDescription", sortOrder ?: "asc")
            }
        }

        [totalResults: totalCount, offers: filteredOffers]
    }

    /* Updates the selected field with a new value for the Member Offer with the supplied id */
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

    /* Returns all Offers for the current member that are not already associated ot a Member offer for them */
    def searchForAvailableOffersForMember(String cardNumber, String offerDescription) {
        def member = Member.findByCardNumber(cardNumber)

        def memberOfferList = MemberOffer.createCriteria().list {
            eq('member.id', member.id)
        }

        // Get the current date and time
        def currentDateTime = new DateTime()

        // Filter LoyaltyOffer objects by status and date range
        def activeOffers = LoyaltyOffer.findAllByStatusAndStartDateLessThanEqualAndEndDateGreaterThanEqualAndOfferDescriptionLike("ACTIVE", currentDateTime, currentDateTime, "${offerDescription}%")

        // Get the offers that are not linked to the member
        def offersNotLinkedToMember = activeOffers.findAll { offer ->
            !memberOfferList.find { memberOffer ->
                memberOffer.offer == offer
            }
        }

        // Return a max of 5 offers
        return offersNotLinkedToMember.take(5)
    }

    /* Creates a new Member Offer in the database */
    def saveMemberOffer(MemberOffer memberOffer, Integer memberId, Integer offerId) {
        def result = false

        def member = Member.get(memberId)
        def offer = LoyaltyOffer.get(offerId)

        memberOffer.member = member
        memberOffer.offer = offer

        if (memberOffer.validate()) {
            memberOffer.save(flush: true)
            result = true
        }

        return result
    }
}