package uk.co.wonderlane.wlpos

import uk.co.wonderlane.wlpos.enums.VoucherType

class TenderType {

    int id
    int retailerId
    String name
    String receiptDescription
    boolean autoReconcile
    boolean eligibleForBanking
    boolean eligibleForFloat
    boolean eligibleForCashLift
    boolean cashTender
    boolean cardPayment
    VoucherType voucherType
    boolean deleted
    boolean isProtected

    static mapping = {
        table "`tendertype`"
        version false

        retailerId column: "retailerId", sqlType: "tinyint"
        name column: "name"
        receiptDescription column: "receiptDescription"
        autoReconcile column: "autoReconcile"
        eligibleForBanking column: "eligibleForBanking"
        eligibleForFloat column: "eligibleForFloat"
        eligibleForCashLift column: "eligibleForCashLift"
        cashTender column: "cashTender"
        cardPayment column: "cardPayment"
        voucherType column: "voucherType"
        deleted column: "deleted"
        isProtected column: "protected"
    }

    static constraints = {
        retailerId nullable: false
        name size: 1..24, blank: false, nullable: false
        receiptDescription size: 1..24, blank: false, nullable: false
        autoReconcile nullable: false
        eligibleForBanking nullable: false
        eligibleForFloat nullable: false
        eligibleForCashLift nullable: false
        cashTender nullable: false
        cardPayment nullable: false
        voucherType nullable: true
        deleted nullable: false
        isProtected nullable: false
    }
}