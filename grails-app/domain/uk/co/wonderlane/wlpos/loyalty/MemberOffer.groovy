package uk.co.wonderlane.wlpos.loyalty;

import org.joda.time.DateTime;
import uk.co.wonderlane.wlpos.LoyaltyOffer;
import uk.co.wonderlane.wlpos.enums.MemberOfferStatus

class MemberOffer {

    int id
    int memberId
    LoyaltyOffer offer
    DateTime visibleFromDate
    DateTime startDate
    DateTime endDate
    Integer currentRedemptions
    Integer maxRedemptions
    MemberOfferStatus status
    DateTime dateCreated
    DateTime dateModified

    static mapping = {
        datasources(["loyalty"])

        table '`member_offer`'
        version false

        id column: "id"
        memberId column: "member_id"
        visibleFromDate column: "visible_from_date"
        startDate column: "start_date"
        endDate column: "end_date"
        currentRedemptions column: "current_redemptions"
        maxRedemptions colum: "max_redemptions"
        status column: "status", sqlType: "enum", enumType: "string"
        dateCreated column: "date_created"
        dateModified column: "date_modified"
    }

    static constraints = {
        currentRedemptions nullable: true, defaultValue: 0
        maxRedemptions nullable: true, defaultValue: 1
        dateModified nullable: true
    }
}
