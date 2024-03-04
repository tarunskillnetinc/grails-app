package uk.co.wonderlane.wlpos

import com.fasterxml.jackson.annotation.JsonIgnore
import org.joda.time.DateTime
import uk.co.wonderlane.wlpos.enums.LoyaltyOfferStatus
import uk.co.wonderlane.wlpos.enums.LoyaltyOfferType

class LoyaltyOffer {

    int id
    String offerDescription
    int retailerOfferId
    int retailerId
    LoyaltyOfferType type = LoyaltyOfferType.STANDARD
    LoyaltyOfferStatus status
    DateTime visibleFromDate
    Date startDate
    Date endDate
    int maxAllocations
    int currentAllocations
    BigDecimal maxBudget
    BigDecimal currentBudget = BigDecimal.ZERO
    int maxCustomers
    int currentCustomers
    int maxRedemptions
    int currentRedemptions
    int redemptionDefault
    int alertThreshold
    int weighting
    int requiresActivation
    String customAttributes
    DateTime dateCreated
    DateTime dateModified
    @JsonIgnore
    Collection<LoyaltyOfferSegment> loyaltyOfferSegments = new ArrayList<>()

    static hasMany = [ loyaltyOfferSegments: LoyaltyOfferSegment ]

    static constraints = {
        customAttributes nullable: true
        visibleFromDate nullable: true
        loyaltyOfferSegments minSize: 1, validator: {val, obj ->
            return true
        }
    }

    static mapping = {
        autowire true
        datasources (["loyalty"])

        table "offer"
        version false

        id column: "id", sqlType: "int"
        offerDescription column: "offer_description", sqlType: "text"
        retailerOfferId column: "retailer_offer_id"
        retailerId column: "retailer_id"
        type column: "type" , sqlType: "enum", enumType: 'string'
        status column: "status" , sqlType: "enum", enumType: 'string'
        visibleFromDate column: "visible_from_date"
        startDate column: "start_date", sqlType: "datetime"
        endDate column: "end_date" , sqlType: "datetime"
        maxAllocations column: "max_allocations"
        currentAllocations column: "current_allocations"
        maxBudget column: "max_budget"
        currentBudget column: "current_budget"
        maxCustomers column: "max_customers"
        currentCustomers column: "current_customers"
        maxRedemptions column: "max_redemptions"
        currentRedemptions column: "current_redemptions"
        redemptionDefault column: "redemption_default"
        alertThreshold column: "alert_threshold"
        weighting column: "weighting"
        requiresActivation column: "requires_activation" , sqlType: "tinyint"
        customAttributes column: "custom_attributes" , sqlType: "text"
        dateCreated column: "date_created"
        dateModified column: "date_modified"
        loyaltyOfferSegments cascade: 'none'
    }
}
