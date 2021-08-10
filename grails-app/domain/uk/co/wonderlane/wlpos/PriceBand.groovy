package uk.co.wonderlane.wlpos

class PriceBand {

    int id
    int retailerId
    String description

    static mapping = {
        table "priceband"
        version false

        retailerId column: "retailerId"
        description column: "description"
    }

    static constraints = {
        retailerId nullable: false
        description size: 1..45, blank: false, nullable: false
    }
}