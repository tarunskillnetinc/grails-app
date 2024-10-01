package uk.co.wonderlane.wlpos.loyalty

import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import uk.co.wonderlane.wlpos.enums.LoyaltyOfferStatus
import uk.co.wonderlane.wlpos.enums.LoyaltyOfferType

class Offer {

    int id
    String offerDescription
    String marketingText
    String termsText
    int retailerOfferId
    int retailerId
    LoyaltyOfferType type = LoyaltyOfferType.STANDARD
    LoyaltyOfferStatus status
    DateTime visibleFromDate
    Date startDate
    Date endDate
    Integer maxAllocations
    int currentAllocations
    BigDecimal maxBudget
    BigDecimal currentBudget = BigDecimal.ZERO
    Integer maxCustomers
    int currentCustomers
    Integer maxRedemptions
    int currentRedemptions
    int redemptionDefault
    Integer alertThreshold
    Integer weighting
    int requiresActivation
    String customAttributes
    DateTime dateCreated
    DateTime dateModified
    Collection<OfferSegment> loyaltyOfferSegments = new ArrayList<>()
    Integer remainingRedemptions

    static hasMany = [loyaltyOfferSegments: OfferSegment, redeemedOffers: RedeemedOffer, memberOffer: MemberOffer]

    static constraints = {
        offerDescription(nullable: false, validator: { val, obj ->
            if (!val) {
                return ["loyaltyOffer.offerDescription.nullable"]
            } else if (val.length() < 1 || val.length() > 100) {
                return ["loyaltyOffer.offerDescription.size.invalid"]
            }
            return true
        })
        marketingText(nullable: true, validator: { val, obj ->
            if (val && (val.length() < 1 || val.length() > 200)) {
                return ["loyaltyOffer.marketingText.size.invalid"]
            }
            return true
        })
        termsText(nullable: true, validator: { val, obj ->
            if (val && (val.length() < 1 || val.length() > 600)) {
                return ["loyaltyOffer.termsText.size.invalid"]
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
        maxBudget (nullable: true, validator: { val, obj ->
            if (val < 1) {
                return ["loyaltyOffer.maxBudget.minimum"]
            }
            return true
        })
        currentBudget nullable: true
        maxCustomers nullable: true
        currentCustomers nullable: true
        maxRedemptions (nullable: true, validator: { val, obj ->
            if (val < 1) {
                return ["loyaltyOffer.maxRedemptions.minimum"]
            }
            return true
        })
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
        marketingText column: "marketing_text"
        termsText column: "terms_text"
        retailerOfferId column: "retailer_offer_id"
        retailerId column: "retailer_id"
        type column: "type" , sqlType: "enum", enumType: 'string'
        status column: "status" , sqlType: "enum", enumType: 'string'
        visibleFromDate column: "visible_from_date"
        startDate column: "start_date", sqlType: "datetime"
        endDate column: "end_date", sqlType: "datetime"
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

        remainingRedemptions formula: "(max_redemptions - current_redemptions)"
    }

    uk.co.wonderlane.wlpos.entities.LoyaltyOffer getLoyaltyOffer(){
        uk.co.wonderlane.wlpos.entities.LoyaltyOffer loyaltyOffer = new uk.co.wonderlane.wlpos.entities.LoyaltyOffer();
        loyaltyOffer.setId(id)
        loyaltyOffer.setOfferDescription(offerDescription)
        loyaltyOffer.setMarketingText(marketingText)
        loyaltyOffer.setTermsText(termsText)
        loyaltyOffer.setRetailerOfferId(retailerOfferId)
        loyaltyOffer.setRetailerId(retailerId)
        loyaltyOffer.setType(type)
        loyaltyOffer.setStatus(status)
        loyaltyOffer.setVisibleFromDate(visibleFromDate)
        loyaltyOffer.setStartDate(startDate)
        loyaltyOffer.setEndDate(endDate)
        loyaltyOffer.setMaxAllocations(maxAllocations)
        loyaltyOffer.setCurrentAllocations(currentAllocations)
        loyaltyOffer.setMaxBudget(maxBudget)
        loyaltyOffer.setCurrentBudget(currentBudget)
        loyaltyOffer.setMaxCustomers(maxCustomers)
        loyaltyOffer.setCurrentCustomers(currentCustomers)
        loyaltyOffer.setMaxRedemptions(maxRedemptions)
        loyaltyOffer.setCurrentRedemptions(currentRedemptions)
        loyaltyOffer.setRedemptionDefault(redemptionDefault)
        loyaltyOffer.setAlertThreshold(alertThreshold)
        loyaltyOffer.setWeighting(weighting)
        loyaltyOffer.setRequiresActivation(requiresActivation)
        loyaltyOffer.setCustomAttributes(customAttributes)
        loyaltyOfferSegments.each {
            loyaltyOffer.getLoyaltyOfferSegments().add(it.getLoyaltyOfferSegments())
        }
        return loyaltyOffer;
    }

    def beforeUpdate() {
        dateModified = DateTime.now(DateTimeZone.UTC)
    }
}
