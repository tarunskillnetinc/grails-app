package uk.co.wonderlane.wlpos.loyalty

import org.joda.time.DateTime
import uk.co.wonderlane.wlpos.enums.MemberTransactionStatus

class MemberTransaction {

    int id
    int retailerId
    int memberId
    int storeId
    int terminalId
    int transactionId
    DateTime transactionTimestamp
    String redeemableOffers
    Double transactionTotal
    Double transactionDiscount
    Integer transactionPoints
    Integer transactionStamps
    MemberTransactionStatus status
    DateTime dateCreated
    DateTime dateModified

    Member member
    Store store
    static belongsTo = [store: Store, member: Member]

    static hasOne = [redeemedOffer: RedeemedOffer]

    static mapping = {
        datasources(["loyalty"])

        table '`member_transaction`'
        version false

        id column: "id"
        retailerId column: "retailer_id"
        memberId column: "member_id", updatable: false, insertable: false
        storeId column: "store_id", updatable: false, insertable: false
        terminalId column: "terminal_id"
        transactionId column: "transaction_id"
        transactionTimestamp column: "transaction_timestamp"
        redeemableOffers column: "redeemable_offers", type: "text"
        transactionTotal column: "transaction_total"
        transactionDiscount column: "transaction_discount"
        transactionPoints column: "transaction_points"
        transactionStamps column: "transaction_stamps"
        status column: "status", sqlType: "enum", enumType: "string"
        dateCreated column: "date_created"
        dateModified column: "date_modified"
    }

    static constraints = {
        transactionTimestamp nullable: true, defaultValue: null
        transactionTotal nullable: true, defaultValue: 0
        transactionDiscount nullable: true, defaultValue: 0
        transactionPoints nullable: true, defaultValue: 0
        transactionStamps nullable: true, defaultValue: 0
        dateModified nullable: true, defaultValue: null
    }
}
