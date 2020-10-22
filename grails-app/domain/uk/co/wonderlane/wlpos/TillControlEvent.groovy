package uk.co.wonderlane.wlpos

import uk.co.wonderlane.wlpos.enums.TillControlEventType

class TillControlEvent {

    int id
    int retailerId
    int storeId
    Integer tillId
    Integer transactionId
    TillControlEventType type
    String reason
    String reasonOther
    Integer docketNumber
    String docketBarcode
    Integer userId
    String usersName
    Integer overrideUserId
    String overrideUsersName
    BigDecimal amount
    Date dateCreated

    static mapping = {
        datasources (["reporting"])

        table "tillcontrolevent"
        version false

        id column: "id"
        retailerId column: "retailerId"
        storeId column: "storeId"
        tillId column: "tillId"
        transactionId column: "transactionId"
        type column: "`type`"
        reason column: "reason"
        reasonOther column: "reasonOther"
        docketNumber column: "docketNumber"
        docketBarcode column: "docketBarcode"
        userId column: "userId"
        usersName column: "usersName"
        overrideUserId column: "overrideUserId"
        overrideUsersName column: "overrideUsersName"
        amount column: "`amount`"
        dateCreated column: "dateCreated"
    }

    static constraints = {

    }
}