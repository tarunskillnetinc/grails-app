package uk.co.wonderlane.wlpos

import com.google.gson.Gson
import uk.co.wonderlane.wlpos.entities.SyncMessage
import uk.co.wonderlane.wlpos.enums.PrintReceiptOption
import uk.co.wonderlane.wlpos.enums.SyncMessageType

class TillSettingsController {

    def index() {
        def tillSettings = TillSettings.findByRetailerIdAndStoreId(1, 23034)

        [tillSettings: tillSettings, availablePrintReceiptOptions: PrintReceiptOption.values()]
    }

    def save() {
        def tillSettings = TillSettings.findByRetailerIdAndStoreId(1, 23034)

        bindData(tillSettings, params)

        tillSettings.retailerId = 1 // TODO
        tillSettings.storeId = 23034 // TODO

        if (tillSettings.validate()) {
            tillSettings.save(flush: true, failOnError: true)

            SyncMessage syncMessage = new SyncMessage(SyncMessageType.TILL_SETTINGS, 1, 23034, 0) // TODO Retailer ID and store ID from session.
            syncMessage.setInsert(true)
            syncMessage.setTillSettings(tillSettings.getTillSettings());

            Gson gson = new Gson()

            rabbitService.sendExchangeMessage(String.format("R%d_S%d", syncMessage.getRetailerId(), syncMessage.getStoreId()), gson.toJson(syncMessage))

            redirect(action: "index")
        } else {
            render(view: "index", model: [tillSettings: tillSettings, availablePrintReceiptOptions: PrintReceiptOption.values()])
        }
    }
}