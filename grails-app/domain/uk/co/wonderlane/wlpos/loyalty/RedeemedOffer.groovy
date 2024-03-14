package uk.co.wonderlane.wlpos.loyalty

import uk.co.wonderlane.wlpos.LoyaltyOffer

class RedeemedOffer {

    int id
    Integer itemId
    int numberRedeemed
    Double awardValue
    Date dateCreated
    Date dateModified
    LoyaltyOffer loyaltyOffer

    static belongsTo = [memberTransaction: MemberTransaction, loyaltyOffer: LoyaltyOffer]

    static mapping = {
        datasources(["loyalty"])

        table '`redeemed_offer`'
        version false

        id column: "id"
        itemId column: "item_id"
        memberTransaction column: "transaction_id", unique: true
        numberRedeemed column: "number_redeemed"
        awardValue column: "award_value"
        dateCreated column: "date_created"
        dateModified column: "date_modified"
        loyaltyOffer column: 'offer_id', unique: true
    }

    static constraints = {
        itemId nullable: true, defaultValue: null
        transactionId nullable: true, defaultValue: 0
        numberRedeemed nullable: true, defaultValue: 0
        awardValue nullable: true, defaultValue: 0
        dateModified nullable: true
        loyaltyOffer nullable: true
    }
}
