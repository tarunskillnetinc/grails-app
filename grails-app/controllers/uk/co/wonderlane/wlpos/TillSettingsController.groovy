package uk.co.wonderlane.wlpos

import com.google.gson.Gson
import uk.co.wonderlane.wlpos.entities.SyncMessage
import uk.co.wonderlane.wlpos.enums.PrintReceiptOption
import uk.co.wonderlane.wlpos.enums.SyncMessageType

class TillSettingsController {

    def springSecurityService

    def index() {
        def tillSettings = TillSettings.findByRetailerIdAndStoreId(springSecurityService.principal.retailerId, springSecurityService.principal.storeId)

        [tillSettings: tillSettings, availablePrintReceiptOptions: PrintReceiptOption.values()]
    }

    def save() {
        def tillSettings = TillSettings.findByRetailerIdAndStoreId(springSecurityService.principal.retailerId, springSecurityService.principal.storeId)

        bindData(tillSettings, params)

        tillSettings.retailerId = springSecurityService.principal.retailerId
        tillSettings.storeId = springSecurityService.principal.storeId

        if (tillSettings.validate()) {
            tillSettings.save(flush: true, failOnError: true)

            // TODO I think this service needs to be made into an injectable dependency if we go ahead with Grails implementation.
            BackOfficeRabbitService rabbitService = new BackOfficeRabbitService(grailsApplication.config.getProperty('rabbitmq.host'), grailsApplication.config.getProperty('rabbitmq.username'), grailsApplication.config.getProperty('rabbitmq.password'))
            rabbitService.init()

            if (!rabbitService.isOpen()) {
                throw new Exception("Rabbit MQ not available")
            }

            SyncMessage syncMessage = new SyncMessage(SyncMessageType.TILL_SETTINGS, springSecurityService.principal.retailerId, springSecurityService.principal.storeId, 0)
            syncMessage.setInsert(true)
            syncMessage.setTillSettings(tillSettings.getTillSettings());

            Gson gson = new Gson()

            rabbitService.sendExchangeMessage(String.format("R%d_S%d", syncMessage.getRetailerId(), syncMessage.getStoreId()), gson.toJson(syncMessage))

            flash.message = "Store settings saved successfully."

            redirect(action: "index")
        } else {
            render(view: "index", model: [tillSettings: tillSettings, availablePrintReceiptOptions: PrintReceiptOption.values()])
        }
    }
}