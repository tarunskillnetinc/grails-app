package uk.co.wonderlane.wlpos

class Restrictions {

    int id
    BigDecimal minOpenPrice
    BigDecimal maxOpenPrice
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
    }

    static constraints = {
        minOpenPrice min: 0.00 as BigDecimal, max: 99999.99 as BigDecimal, blank: true, nullable: true, scale: 2
        maxOpenPrice min: 0.01 as BigDecimal, max: 99999.99 as BigDecimal, blank: true, nullable: true, scale: 2, validator: {val, obj ->
            return val != null && obj.minOpenPrice != null && val.compareTo(obj.minOpenPrice) > 0 ? true : ["error.Restrictions.maxMoreThanMin"]
        }
        buyerAgeRestriction min: 1, max: 25, blank: true, nullable: true, validator: { val, obj ->
            if (obj.buyerIdRequired && val == null) {
                return ["restrictions.buyerAgeRestriction.nullable"]
            }
        }
        buyerChallengeAge min: 1, max: 50, blank: true, nullable: true, validator: { val, obj ->
            if (obj.buyerIdRequired && val == null) {
                return ["restrictions.buyerChallengeAge.nullable"]
            }
        }
        buyerIdRequired nullable: true
        buyerIdForced nullable: true
        sellerAgeRestriction min: 16, max: 21, blank: true, nullable:true, validator: { val, obj ->
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
        return restrictions
    }
}