package uk.co.wonderlane.wlpos

import uk.co.wonderlane.wlpos.enums.ReasonCodeType

class ReasonCode {

    int id
    ReasonCodeType type
    String code
    String description
    boolean deleted
    boolean additionalFunctionality
    boolean promptForText
    Integer retailerId
    boolean preferredReasonCode
    String secret

    static mapping = {
        autowire true
        table "reasoncode"
        version false

        id column:"id"
        type column:"reasonCodeType", sqlType: "enum", enumType: "string"
        code column:"code"
        description column:"description"
        deleted column: "deleted"
        additionalFunctionality column: "additionalFunctionality"
        promptForText column: "promptForText"
        retailerId column: "retailerId", sqlType: "tinyint"
        preferredReasonCode column: "preferredReasonCode"
        secret column: "secret"
    }

    static constraints = {
        type nullable: false
        code maxSize: 20, nullable: true, blank: true
        description maxSize: 100, nullable: false
        retailerId nullable: true, blank: true
        secret maxSize: 20, nullable: true, blank: true
    }

    uk.co.wonderlane.wlpos.entities.ReasonCode getReasonCode() {
        return new uk.co.wonderlane.wlpos.entities.ReasonCode(
                id,
                type,
                code,
                description,
                deleted,
                additionalFunctionality,
                promptForText,
                retailerId,
                preferredReasonCode,
                secret
        )
    }
}
