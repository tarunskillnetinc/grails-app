package uk.co.wonderlane.wlpos

import org.joda.time.DateTime

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

        retailerId column: "retailerId", sqlType: "tinyint"
        message column: "`message`"
        retailerMessageCode column: "retailerMessageCode"
        startDate column: "startDate"
        endDate column: "endDate"
        displayOncePerTransaction column: "displayOncePerTransaction"
        displayOncePerItem column: "displayOncePerItem"

//        saleProduct column: "productId"
//        refundProduct column: "productId"
    }

    public uk.co.wonderlane.wlpos.entities.Message getMessage() {
        uk.co.wonderlane.wlpos.entities.Message message = new uk.co.wonderlane.wlpos.entities.Message()
        message.setId(id)
        message.setRetailerId(retailerId)
        message.setMessage(this.message)
        message.setRetailerMessageCode(retailerMessageCode)
        message.setStartDate(new DateTime(startDate))
        message.setEndDate(new DateTime(endDate))
        message.setDisplayOncePerItem(displayOncePerItem)
        message.setDisplayOncePerTransaction(displayOncePerTransaction)
        return message
    }
}