package uk.co.wonderlane.wlpos

class DiscountRate {

    int id
    int cardTypeId
    int retailerId
    int rate
    String description

    static mapping = {
        table "discountrate"
        version false

        cardTypeId column: "cardTypeId"
        retailerId column: "retailerId"
        rate column: "`rate`"
        description column: "`description`"
    }
}