package uk.co.wonderlane.wlpos

import com.google.gson.Gson
import uk.co.wonderlane.wlpos.entities.SyncMessage
import uk.co.wonderlane.wlpos.enums.PrintReceiptOption
import uk.co.wonderlane.wlpos.enums.SyncMessageType

class StoreSettingsController {

    def springSecurityService
    def storeSettingsService

    def index() {
        def storeSettings = StoreSettings.findByRetailerIdAndStoreId(springSecurityService.principal.retailerId, springSecurityService.principal.storeId)

        [storeSettings: storeSettings, availablePrintReceiptOptions: PrintReceiptOption.values()]
    }

    def save() {
        def storeSettings = StoreSettings.findByRetailerIdAndStoreId(springSecurityService.principal.retailerId, springSecurityService.principal.storeId)

        bindData(storeSettings, params)

        storeSettings.retailerId = springSecurityService.principal.retailerId
        storeSettings.storeId = springSecurityService.principal.storeId

        if (storeSettings.validate()) {
            storeSettingsService.saveStoreSettings(storeSettings)

            // TODO I think this service needs to be made into an injectable dependency if we go ahead with Grails implementation.
            BackOfficeRabbitService rabbitService = new BackOfficeRabbitService(grailsApplication.config.getProperty('rabbitmq.host'), Integer.parseInt(grailsApplication.config.getProperty('rabbitmq.port')), grailsApplication.config.getProperty('rabbitmq.username'), grailsApplication.config.getProperty('rabbitmq.password'))
            rabbitService.init()

            if (!rabbitService.isOpen()) {
                throw new Exception("Rabbit MQ not available")
            }

            SyncMessage syncMessage = new SyncMessage(SyncMessageType.STORE_SETTINGS, springSecurityService.principal.retailerId, springSecurityService.principal.storeId, 0)
            syncMessage.setInsert(true)
            syncMessage.setStoreSettings(storeSettings.getStoreSettings());

            Gson gson = new Gson()

            rabbitService.sendExchangeMessage(String.format("R%d_S%d", syncMessage.getRetailerId(), syncMessage.getStoreId()), gson.toJson(syncMessage))

            flash.message = "Store settings saved successfully."

            redirect(action: "index")
        } else {
            render(view: "index", model: [storeSettings: storeSettings, availablePrintReceiptOptions: PrintReceiptOption.values()])
        }
    }
}