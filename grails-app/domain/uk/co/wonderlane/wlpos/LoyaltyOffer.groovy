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
        offerDescription(nullable: false, validator: { val, obj ->
            if (!val) {
                return ["loyaltyOffer.offerDescription.nullable"]
            } else if (val.length() < 1 || val.length() > 100) {
                return ["loyaltyOffer.offerDescription.size.invalid"]
            }
            return true
        })
        retailerOfferId(nullable: false, validator: { val, obj ->
            if (!val) {
                return ["loyaltyOffer.retailerOfferId.nullable"]
            }
            return true
        })
        retailerId(nullable: false, validator: { val, obj ->
            if (!val) {
                return ["loyaltyOffer.retailerId.nullable"]
            }
            return true
        })
        type(nullable: false, validator: { val, obj ->
            if (!val) {
                return ["loyaltyOffer.type.nullable"]
            }
            return true
        })
        status(nullable: false, validator: { val, obj ->
            if (!val) {
                return ["loyaltyOffer.status.nullable"]
            }
            return true
        })
        visibleFromDate nullable: true
        startDate(nullable: false, validator: { val, obj ->
            if (!val) {
                return ["loyaltyOffer.startDate.nullable"]
            }
            return true
        })
        endDate(nullable: false, validator: { val, obj ->
            if (!val) {
                return ["loyaltyOffer.endDate.nullable"]
            }
            return true
        })
        maxAllocations nullable: true
        currentAllocations nullable: true
        maxBudget nullable: true
        currentBudget nullable: true
        maxCustomers nullable: true
        currentCustomers nullable: true
        maxRedemptions nullable: true
        currentRedemptions nullable: true
        redemptionDefault nullable: true
        alertThreshold nullable: true
        weighting nullable: true
        requiresActivation nullable: true
        customAttributes nullable: true
        dateCreated(nullable: false, validator: { val, obj ->
            if (!val) {
                return ["loyaltyOffer.dateCreated.nullable"]
            }
            return true
        })
        dateModified(nullable: false, validator: { val, obj ->
            if (!val) {
                return ["loyaltyOffer.dateModified.nullable"]
            }
            return true
        })
        loyaltyOfferSegments validator: {val, obj ->
            if (val.size() < 1){
                return ["loyaltyOffer.loyaltyOfferSegments.minimum"]
            }
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
