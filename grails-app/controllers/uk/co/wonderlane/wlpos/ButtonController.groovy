package uk.co.wonderlane.wlpos

import com.google.gson.Gson
import grails.converters.JSON
import uk.co.wonderlane.wlpos.entities.SyncMessage
import uk.co.wonderlane.wlpos.enums.SyncMessageType

class ButtonController {

    def show() {
        [button: Button.get(params.id)]
    }

    def edit() {
        [button: Button.get(params.id)]
    }

    def save() {
        def button = params.id ? Button.get(params.id) : new Button()

        bindData(button, params)

        if (button.validate()) {
            // Make sure the RabbitMQ connection is available, otherwise reject the save.
            try {
                // TODO I think this service needs to be made into an injectable dependency if we go ahead with Grails implementation.
                BackOfficeRabbitService rabbitService = new BackOfficeRabbitService(grailsApplication.config.getProperty('rabbitmq.host'), grailsApplication.config.getProperty('rabbitmq.username'), grailsApplication.config.getProperty('rabbitmq.password'))
                rabbitService.init()

                if (!rabbitService.isOpen()) {
                    throw new Exception("Rabbit MQ not available")
                }

                button.buttonGrid.addToButtons(button)
                button.buttonGrid.save(flush: true)

                SyncMessage syncMessage = new SyncMessage()
                syncMessage.setType(SyncMessageType.BUTTON_GRID)
                syncMessage.setInsert(true)
                syncMessage.setButtonGrid(button.buttonGrid.getButtonGrid())

                Gson gson = new Gson()

                rabbitService.sendQueueMessage("Till01", gson.toJson(syncMessage)) // TODO Send to store exchange not till queue.

                redirect(action: "show", id: button.id)
            } catch (Exception e) {
                e.printStackTrace()

                // TODO Populate an error to display on screen.
                render (view: "edit", model: [button: button])
            }
        } else {
            render (view: "edit", model: [button: button])
        }
    }
}