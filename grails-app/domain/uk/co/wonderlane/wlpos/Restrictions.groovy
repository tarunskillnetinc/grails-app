package uk.co.wonderlane.wlpos

import uk.co.wonderlane.wlpos.enums.StockClassification

class Restrictions {

    int id
    BigDecimal minOpenPrice = 0.01
    BigDecimal maxOpenPrice = 99999.99
    Boolean buyerIdRequired
    Boolean buyerIdForced
    Integer buyerAgeRestriction
    Integer buyerChallengeAge
    Integer sellerAgeRestriction
    Boolean refundAllowed
    Boolean markdownAllowed
    Boolean discountAllowed
    Boolean creditPaymentAllowed
    Boolean quantityChangeAllowed
    Boolean quantityChangeForced
    Boolean receiptPrintForced
    Boolean allowsLoyaltyPointsCollection
    Boolean alwaysOpenCashDrawer
    Boolean excludedFromPromotion
    Boolean saleAllowed
    Boolean priceEntryRequired
    Boolean allowPriceChange
    BigDecimal maximumMarkdownPercentage
    Integer quantityChangeRestriction
    Boolean promptForMarkdown
    StockClassification stockClassification
    Integer promptedDaysFrom
    PricingClassification pricingClassification

    static mapping = {
        table "restrictions"
        version false

        minOpenPrice column: "minOpenPrice"
        maxOpenPrice column: "maxOpenPrice"
        buyerIdRequired column: "buyerIdRequired"
        buyerIdForced column: "buyerIdForced"
        buyerAgeRestriction column: "buyerAgeRestriction"
        buyerChallengeAge column: "buyerChallengeAge"
        sellerAgeRestriction column: "sellerAgeRestriction"
        refundAllowed column: "refundAllowed"
        markdownAllowed column: "markdownAllowed"
        discountAllowed column: "discountAllowed"
        creditPaymentAllowed column: "creditPaymentAllowed"
        quantityChangeAllowed column: "quantityChangeAllowed"
        quantityChangeForced column: "quantityChangeForced"
        receiptPrintForced column: "receiptPrintForced"
        allowsLoyaltyPointsCollection column: "allowsLoyaltyPointsCollection"
        alwaysOpenCashDrawer column: "alwaysOpenCashDrawer"
        excludedFromPromotion column: "excludedFromPromotion"
        saleAllowed column: "saleAllowed"
        priceEntryRequired column: "priceEntryRequired"
        allowPriceChange column: "allowPriceChange"
        maximumMarkdownPercentage column: "maximumMarkdownPercentage"
        quantityChangeRestriction column: "quantityChangeRestriction"
        promptForMarkdown column: "promptForMarkdown"
        stockClassification column: "stockClassification", sqlType: "text", enumType: "string"
        promptedDaysFrom column: "promptedDaysFrom"
        pricingClassification column: "pricingClassificationId"
    }

    static constraints = {
        minOpenPrice min: 0.01 as BigDecimal, max: 99999.99 as BigDecimal, blank: true, nullable: true, scale: 2
        maxOpenPrice min: 0.01 as BigDecimal, max: 99999.99 as BigDecimal, blank: true, nullable: true, scale: 2, validator: {val, obj ->
            return (val == null || obj.minOpenPrice == null) || (val.compareTo(obj.minOpenPrice) > 0) ? true : ["error.Restrictions.maxMoreThanMin"]
        }
        buyerAgeRestriction min: 0, max: 99, blank: true, nullable: true, validator: { val, obj ->
            if (obj.buyerIdRequired && val == null) {
                return ["restrictions.buyerAgeRestriction.nullable"]
            }
        }
        buyerChallengeAge min: 0, max: 99, blank: true, nullable: true
        buyerIdRequired nullable: true
        buyerIdForced nullable: true
        sellerAgeRestriction min: 0, max: 99, blank: true, nullable:true, validator: { val, obj ->
            if (obj.buyerIdRequired && val == null) {
                return ["restrictions.sellerAgeRestriction.nullable"]
            }
        }
        refundAllowed nullable: true
        markdownAllowed nullable: true
        discountAllowed nullable: true
        creditPaymentAllowed nullable: true
        quantityChangeAllowed nullable: true
        quantityChangeForced nullable: true
        receiptPrintForced nullable: true
        allowsLoyaltyPointsCollection nullable: true
        alwaysOpenCashDrawer nullable: true
        excludedFromPromotion nullable: true
        saleAllowed nullable: true
        priceEntryRequired nullable: true
        allowPriceChange nullable: true
        maximumMarkdownPercentage nullable: true
        quantityChangeRestriction nullable: true
        promptForMarkdown nullable: true
        stockClassification nullable: true
        promptedDaysFrom nullable: true
        pricingClassification nullable: true
    }

    public uk.co.wonderlane.wlpos.entities.Restrictions getRestrictions() {
        uk.co.wonderlane.wlpos.entities.Restrictions restrictions = new uk.co.wonderlane.wlpos.entities.Restrictions()

        restrictions.setId(id)
        restrictions.setMinOpenPrice(minOpenPrice)
        restrictions.setMaxOpenPrice(maxOpenPrice)
        restrictions.setBuyerIdRequired(buyerIdRequired)
        restrictions.setBuyerIdForced(buyerIdForced)
        restrictions.setBuyerAgeRestriction(buyerAgeRestriction)
        restrictions.setBuyerChallengeAge(buyerChallengeAge)
        restrictions.setSellerAgeRestriction(sellerAgeRestriction)
        restrictions.setRefundAllowed(refundAllowed)
        restrictions.setMarkdownAllowed(markdownAllowed)
        restrictions.setDiscountAllowed(discountAllowed)
        restrictions.setCreditPaymentAllowed(creditPaymentAllowed)
        restrictions.setQuantityChangeAllowed(quantityChangeAllowed)
        restrictions.setQuantityChangeForced(quantityChangeForced)
        restrictions.setReceiptPrintForced(receiptPrintForced)
        restrictions.setAllowsLoyaltyPointsCollection(allowsLoyaltyPointsCollection)
        restrictions.setAlwaysOpenCashDrawer(alwaysOpenCashDrawer)
        restrictions.setExcludedFromPromotion(excludedFromPromotion)
        restrictions.setSaleAllowed(saleAllowed)
        restrictions.setPriceEntryRequired(priceEntryRequired)
        restrictions.setAllowPriceChange(allowPriceChange)
        restrictions.setMaximumMarkdownPercentage(maximumMarkdownPercentage)
        restrictions.setQuantityChangeRestriction(quantityChangeRestriction)
        restrictions.setPromptForMarkdown(promptForMarkdown)
        restrictions.setStockClassification(stockClassification)
        restrictions.setPromptedDaysFrom(promptedDaysFrom)
        restrictions.setPricingClassificationId(pricingClassification?.getId())

        return restrictions
    }

    public BigDecimal getDefaultMinOpenPrice() {
        return 0.01 as BigDecimal
    }

    public BigDecimal getDefaultMaxOpenPrice() {
        return 99999.99 as BigDecimal
    }
}