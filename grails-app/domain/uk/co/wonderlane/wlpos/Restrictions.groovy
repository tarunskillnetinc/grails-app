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
    Boolean discountAllowed;
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
        minOpenPrice blank: false, nullable: false
        maxOpenPrice blank:false, nullable: false
        buyerAgeRestriction blank: true, nullable: true
        buyerChallengeAge blank: true, nullable: true
        sellerAgeRestriction blank: true, nullable:true
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