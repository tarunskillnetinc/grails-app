package uk.co.wonderlane.wlpos

class CurrentSalesForecast implements Serializable {
    int id
    Product product
    Store store
    BigDecimal currentForecast

    static mapping = {
        autowire: true
        table "currentsalesforecast"
        version false

        store column: "storeId", sqlType: "smallint"
        product column: "productId"
        currentForecast column: "currentForecast"
    }

    static constraints = {
        store nullable: false
        product nullable: false
        currentForecast: false
    }
}
