package uk.co.wonderlane.wlpos.loyalty;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import uk.co.wonderlane.wlpos.LoyaltyOffer
import uk.co.wonderlane.wlpos.enums.MemberOfferStatus

class MemberOffer {
    int id
    String offerDescription
    DateTime visibleFromDate
    DateTime startDate
    DateTime endDate
    Double currentSavings
    Integer currentRedemptions
    Integer maxRedemptions
    MemberOfferStatus status
    DateTime dateCreated
    DateTime dateModified

    Integer remainingRedemptions

    LoyaltyOffer offer
    Member member
    static belongsTo = [member: Member, offer: LoyaltyOffer]

    static mapping = {
        datasources(["loyalty"])

        table '`member_offer`'
        version false

        id column: "id"
        offerDescription column: "offer_description", type: "text"
        visibleFromDate column: "visible_from_date"
        startDate column: "start_date"
        endDate column: "end_date"
        currentSavings column: "current_savings"
        currentRedemptions column: "current_redemptions"
        maxRedemptions colum: "max_redemptions"
        status column: "status", sqlType: "enum", enumType: "string"
        dateCreated column: "date_created"
        dateModified column: "date_modified"

        remainingRedemptions formula: "(max_redemptions - current_redemptions)"
    }

    static constraints = {
        currentRedemptions nullable: true, defaultValue: 0
        maxRedemptions nullable: true, defaultValue: 1
    }

    def beforeUpdate() {
        dateModified = DateTime.now(DateTimeZone.UTC)
    }
}
