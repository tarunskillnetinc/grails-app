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
}