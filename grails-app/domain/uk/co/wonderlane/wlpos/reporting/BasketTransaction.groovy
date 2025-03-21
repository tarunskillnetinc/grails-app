package uk.co.wonderlane.wlpos.reporting

class BasketTransaction {

    Integer id
    String transactionObject
    Date createdDateTime
    Date transactionDateTime
    Integer retailerId
    Integer storeId
    Integer tillId
    String transactionSource

    public BasketTransaction() {}

    static mapping = {
        autowire true
        table "baskettransaction"
        version false

        id column: "id", sqlType: "smallint"
        transactionObject column: "`transactionObject`", type: "uk.co.wonderlane.wlpos.usertypes.JsonType", sqlType: "json"
        createdDateTime column: "createdDateTime"
        transactionDateTime column: "transactionDateTime"
        retailerId column: "retailerId", sqlType: "tinyint"
        storeId column: "storeId", sqlType: "smallint"
        tillId column: "tillId", sqlType: "int"
        transactionSource column: "transactionSource"
    }

    def getBasket() {
        return transactionObject.basket
    }

    def getBasketItems() {
        return transactionObject.basketItems
    }

    def getVatTotals() {
        return transactionObject.vatTotals
    }

    def getUser() {
        return transactionObject.user
    }
}
