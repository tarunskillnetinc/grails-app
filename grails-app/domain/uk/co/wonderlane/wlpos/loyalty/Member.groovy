package uk.co.wonderlane.wlpos.loyalty

import uk.co.wonderlane.wlpos.enums.MemberStatus

class Member {
    int id
    int retailerId
    int cardType
    String firstName
    String lastName
    String email
    String mobile_no
    String password
    String resetToken
    Date resetTokenExpiry
    String cardNumber
    String postcode
    Date dateOfBirth
    Date lastTransaction
    Integer offersAvailable
    Integer currentPoints
    Double currentSpend
    Double currentSavings
    Integer currentStamps
    Integer maxStamps
    MemberStatus status
    Date dateCreated
    Date dateUpdated

    static hasMany = [offers: MemberOffer, transactions: MemberTransaction]

    static mapping = {
        datasources(["loyalty"])

        table '`member`'
        version false

        id column: "id"
        retailerId column: "retailer_id"
        cardType column: "card_type"
        firstName column: "first_name"
        lastName column: "last_name"
        email column: "email", unique: true
        mobile_no column: "mobile_no"
        password colum: "password"
        resetToken colum: "reset_token"
        resetTokenExpiry column: "reset_token_expiry"
        cardNumber column: "card_no", unique: true
        postcode column: "postcode"
        dateOfBirth column: "dob"
        lastTransaction column: "last_transaction"
        offersAvailable column: "offers_available"
        currentPoints column: "current_points"
        currentSpend column: "current_spend"
        currentSavings column: "current_savings"
        currentStamps column: "current_stamps"
        maxStamps column: "max_stamps"
        status column: "status", sqlType: "enum", enumType: "string"
        dateCreated column: "date_created"
        dateUpdated column: "date_updated"
    }

    static constraints = {
        firstName nullable: true
        lastName nullable: true
        mobile_no nullable: true
        password nullable: true
        resetToken nullable: true
        resetTokenExpiry nullable: true
        postcode nullable: true
        dateOfBirth nullable: true
        lastTransaction nullable: true
        offersAvailable nullable: true
        currentPoints nullable: true
        currentSpend nullable: true
        currentSavings nullable: true
        currentStamps nullable: true
        maxStamps nullable: true
        status nullable: true
        dateCreated nullable: true
        dateUpdated nullable: true
    }
}