package uk.co.wonderlane.wlpos.reporting;

import java.math.BigDecimal;

class TransactionBasketItem {
    // Each line here is a column within the details.gsp
    Integer seqNum
    String type
    String entryMethod
    String productCode
    String productDescription
    String barcode
    String qty
    BigDecimal unitPrice
    BigDecimal totalPrice
    BigDecimal vat
    String ageVerification
    String returnReason
    BigDecimal priceChange
    String rtc
    String promotionsType
}

