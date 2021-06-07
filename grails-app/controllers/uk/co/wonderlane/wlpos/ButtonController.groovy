package uk.co.wonderlane.wlpos

import com.google.gson.Gson
import uk.co.wonderlane.wlpos.entities.SyncMessage
import uk.co.wonderlane.wlpos.enums.SyncMessageType
import uk.co.wonderlane.wlpos.enums.ButtonType
import uk.co.wonderlane.wlpos.enums.ProcessType
import uk.co.wonderlane.wlpos.enums.ButtonGridType
import uk.co.wonderlane.wlpos.enums.TenderType

class ButtonController {

    def springSecurityService
    def productService
    def buttonService

    def edit() {
        def button
        def productVariant

        if (params.id && Integer.parseInt(params.id) > 0) {
            button = Button.get(params.id)

            if (button.type == ButtonType.PRODUCT) {
                productVariant = productService.getProductVariant(button.productVariantId)
            }
        } else {
            def buttonGrid = ButtonGrid.get(params.buttonGridId)

            button = new Button(row: params.row, column: params.column, buttonGrid: buttonGrid)
        }

        def availableProcesses = [ProcessType.NAVIGATE_SALES, ProcessType.NAVIGATE_QUICK_SELL, ProcessType.NAVIGATE_SEARCH, ProcessType.NAVIGATE_RECEIPTS, ProcessType.NAVIGATE_MANAGER_FUNCTIONS,
                                  ProcessType.NAVIGATE_CUSTOMER_REFUSAL, ProcessType.NAVIGATE_BACK, ProcessType.NAVIGATE_REFUND, ProcessType.NAVIGATE_ADD_FLOAT, ProcessType.NAVIGATE_CASH_LIFT,
                                  ProcessType.NAVIGATE_PAID_OUT, ProcessType.NAVIGATE_TRAINING, ProcessType.NAVIGATE_CREATE_DOCKET, ProcessType.NAVIGATE_COMPLETE_DOCKET, ProcessType.NAVIGATE_DISCOUNT,
                                  ProcessType.SAVE_BASKET, ProcessType.NAVIGATE_RETRIEVE_BASKET, ProcessType.LOCK_TILL, ProcessType.VOID_BASKET, ProcessType.NO_SALE, ProcessType.LOG_OFF, ProcessType.NAVIGATE_TO_WLIM]

        def availableSubPages = ButtonGrid.findAllByTypeAndRetailerIdAndStoreId(ButtonGridType.OTHER, springSecurityService.principal.retailerId, springSecurityService.principal.storeId)
        def availableTenderTypes = TenderType.values()

        [button: button, availableProcesses: availableProcesses, availableSubPages: availableSubPages, productItemCode: productVariant?.itemCode, productDescription: productVariant?.product?.description, availableTenderTypes: availableTenderTypes]
    }

    def save() {
        def button

        if (params.id && Integer.parseInt(params.id) > 0) {
            button = Button.get(params.id)
        } else {
            button = new Button()
            button.buttonGrid = ButtonGrid.get(params.buttonGrid.id)
        }

        bindData(button, params)

        if (button.validate()) {
            // Make sure the RabbitMQ connection is available, otherwise reject the save.
            try {
                // TODO I think this service needs to be made into an injectable dependency if we go ahead with Grails implementation.
                BackOfficeRabbitService rabbitService = new BackOfficeRabbitService(grailsApplication.config.getProperty('rabbitmq.host'), Integer.parseInt(grailsApplication.config.getProperty('rabbitmq.port')), grailsApplication.config.getProperty('rabbitmq.username'), grailsApplication.config.getProperty('rabbitmq.password'))
                rabbitService.init()

                if (!rabbitService.isOpen()) {
                    throw new Exception("Rabbit MQ not available")
                }

                button.buttonGrid.addToButtons(button)
                buttonService.saveButtonGrid(button.buttonGrid)

                SyncMessage syncMessage = new SyncMessage(SyncMessageType.BUTTON_GRID, springSecurityService.principal.retailerId, springSecurityService.principal.storeId, 0)
                syncMessage.setInsert(true)
                syncMessage.setButtonGrid(button.buttonGrid.getButtonGrid())

                Gson gson = new Gson()

                rabbitService.sendExchangeMessage(String.format("R%d_S%d", syncMessage.getRetailerId(), syncMessage.getStoreId()), gson.toJson(syncMessage))

                redirect(controller: "buttonGrid", action: "show", id: button.buttonGrid.id)
            } catch (Exception e) {
                e.printStackTrace()

                def availableProcesses = [ProcessType.NAVIGATE_SALES, ProcessType.NAVIGATE_QUICK_SELL, ProcessType.NAVIGATE_SEARCH, ProcessType.NAVIGATE_RECEIPTS, ProcessType.NAVIGATE_MANAGER_FUNCTIONS,
                                          ProcessType.NAVIGATE_CUSTOMER_REFUSAL, ProcessType.NAVIGATE_BACK, ProcessType.NAVIGATE_REFUND, ProcessType.NAVIGATE_ADD_FLOAT, ProcessType.NAVIGATE_CASH_LIFT,
                                          ProcessType.NAVIGATE_PAID_OUT, ProcessType.NAVIGATE_TRAINING, ProcessType.NAVIGATE_CREATE_DOCKET, ProcessType.NAVIGATE_COMPLETE_DOCKET, ProcessType.NAVIGATE_DISCOUNT,
                                          ProcessType.SAVE_BASKET, ProcessType.NAVIGATE_RETRIEVE_BASKET, ProcessType.LOCK_TILL, ProcessType.VOID_BASKET, ProcessType.NO_SALE, ProcessType.LOG_OFF, ProcessType.NAVIGATE_TO_WLIM]

                def availableSubPages = ButtonGrid.findAllByTypeAndRetailerIdAndStoreId(ButtonGridType.OTHER, springSecurityService.principal.retailerId, springSecurityService.principal.storeId)
                def availableTenderTypes = TenderType.values()

                def productVariant

                if (button.type == ButtonType.PRODUCT && button.productVariantId) {
                    productVariant = productService.getProductVariant(button.productVariantId)
                }

                // TODO Populate an error to display on screen.
                render (view: "edit", model: [button: button, availableProcesses: availableProcesses, availableSubPages: availableSubPages, availableTenderTypes: availableTenderTypes, productItemCode: productVariant?.itemCode, productDescription: productVariant?.product?.description])
            }
        } else {
            def availableProcesses = [ProcessType.NAVIGATE_SALES, ProcessType.NAVIGATE_QUICK_SELL, ProcessType.NAVIGATE_SEARCH, ProcessType.NAVIGATE_RECEIPTS, ProcessType.NAVIGATE_MANAGER_FUNCTIONS,
                                      ProcessType.NAVIGATE_CUSTOMER_REFUSAL, ProcessType.NAVIGATE_BACK, ProcessType.NAVIGATE_REFUND, ProcessType.NAVIGATE_ADD_FLOAT, ProcessType.NAVIGATE_CASH_LIFT,
                                      ProcessType.NAVIGATE_PAID_OUT, ProcessType.NAVIGATE_TRAINING, ProcessType.NAVIGATE_CREATE_DOCKET, ProcessType.NAVIGATE_COMPLETE_DOCKET, ProcessType.NAVIGATE_DISCOUNT,
                                      ProcessType.LOCK_TILL, ProcessType.VOID_BASKET, ProcessType.NO_SALE, ProcessType.LOG_OFF, ProcessType.NAVIGATE_TO_WLIM]

            def availableSubPages = ButtonGrid.findAllByTypeAndRetailerIdAndStoreId(ButtonGridType.OTHER, springSecurityService.principal.retailerId, springSecurityService.principal.storeId)
            def availableTenderTypes = TenderType.values()

            def productVariant

            if (button.type == ButtonType.PRODUCT && button.productVariantId) {
                product = productService.getProductVariant(button.productVariantId)
            }

            render (view: "edit", model: [button: button, availableProcesses: availableProcesses, availableSubPages: availableSubPages, availableTenderTypes: availableTenderTypes, productItemCode: productVariant?.itemCode, productDescription: productVariant?.product?.description])
        }
    }

    def unassign(int id) {
        Button button = Button.get(id)

        int buttonGridId = button.buttonGrid.id

        buttonService.deleteButton(button)

        // Make sure the RabbitMQ connection is available, otherwise reject the save.
        try {
            // TODO I think this service needs to be made into an injectable dependency if we go ahead with Grails implementation.
            BackOfficeRabbitService rabbitService = new BackOfficeRabbitService(grailsApplication.config.getProperty('rabbitmq.host'), Integer.parseInt(grailsApplication.config.getProperty('rabbitmq.port')), grailsApplication.config.getProperty('rabbitmq.username'), grailsApplication.config.getProperty('rabbitmq.password'))
            rabbitService.init()

            if (!rabbitService.isOpen()) {
                throw new Exception("Rabbit MQ not available")
            }

            SyncMessage syncMessage = new SyncMessage(SyncMessageType.BUTTON_GRID, springSecurityService.principal.retailerId, springSecurityService.principal.storeId, 0)
            syncMessage.setInsert(true)
            syncMessage.setButtonGrid(ButtonGrid.get(buttonGridId).getButtonGrid())

            Gson gson = new Gson()

            rabbitService.sendExchangeMessage(String.format("R%d_S%d", syncMessage.getRetailerId(), syncMessage.getStoreId()), gson.toJson(syncMessage))
        } catch (Exception e) {
            e.printStackTrace()
        }

        redirect (controller: "buttonGrid", action: "show", id: buttonGridId)
    }
}