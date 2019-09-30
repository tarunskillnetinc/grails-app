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

    public uk.co.wonderlane.wlpos.entities.DiscountRate getDiscountRate() {
        uk.co.wonderlane.wlpos.entities.DiscountRate discountRate = new uk.co.wonderlane.wlpos.entities.DiscountRate()
        discountRate.setId(id)
        discountRate.setRetailerId(retailerId)
        discountRate.setCardTypeId(cardTypeId)
        discountRate.setRate(rate)
        discountRate.setDescription(description)
        return discountRate
    }
}