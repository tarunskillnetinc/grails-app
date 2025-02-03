package uk.co.wonderlane.wlpos

import org.joda.time.DateTime

class Message {

    int id
    int retailerId
    String text
    String retailerMessageCode
    Date startDate
    Date endDate
    boolean displayOncePerTransaction
    boolean displayOncePerItem

    static mapping = {
        table "message"
        version false

        retailerId column: "retailerId", sqlType: "tinyint"
        text column: "`text`"
        retailerMessageCode column: "retailerMessageCode"
        startDate column: "startDate"
        endDate column: "endDate"
        displayOncePerTransaction column: "displayOncePerTransaction"
        displayOncePerItem column: "displayOncePerItem"
    }

    static constraints = {
        retailerMessageCode nullable: true
        startDate nullable: true
        endDate nullable: true
    }

    uk.co.wonderlane.wlpos.entities.Message getMessage() {
        uk.co.wonderlane.wlpos.entities.Message message = new uk.co.wonderlane.wlpos.entities.Message()

        message.setId(id)
        message.setRetailerId(retailerId)
        message.setText(text)
        message.setRetailerMessageCode(retailerMessageCode)
        message.setStartDate(new DateTime(startDate))
        message.setEndDate(new DateTime(endDate))
        message.setDisplayOncePerItem(displayOncePerItem)
        message.setDisplayOncePerTransaction(displayOncePerTransaction)

        return message
    }
}