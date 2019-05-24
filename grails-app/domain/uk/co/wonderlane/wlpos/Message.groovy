package uk.co.wonderlane.wlpos

class Message {

    int id
    int retailerId
    String message
    String retailerMessageCode
    Date startDate
    Date endDate
    boolean displayOncePerTransaction
    boolean displayOncePerItem

//    Product saleProduct
//    Product refundProduct

    static mapping = {
        table "message"
        version false

        retailerId column: "retailerId"
        message column: "`message`"
        retailerMessageCode column: "retailerMessageCode"
        startDate column: "startDate"
        endDate column: "endDate"
        displayOncePerTransaction column: "displayOncePerTransaction"
        displayOncePerItem column: "displayOncePerItem"

//        saleProduct column: "productId"
//        refundProduct column: "productId"
    }
}