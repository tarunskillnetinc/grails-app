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
    def imageService
    def rabbitService

    def edit() {
        def button
        def productVariant
        def buttonImage = null

        if (params.id && Integer.parseInt(params.id) > 0) {
            button = Button.get(params.id)
            if (button.type == ButtonType.PRODUCT && button.sku) {
                productVariant = productService.getProductVariant(button.sku)
            }

            if (button.imageDisplay) {
                buttonImage = imageService.getImageFromFile(String.format("%s%s/", grailsApplication.config.getProperty('wlpos.buttonImageDirectory'), springSecurityService.principal.storeId), params.id + ".png")
            }
        } else {
            def buttonGrid = ButtonGrid.get(params.buttonGridId)

            button = new Button(row: params.row, column: params.column, buttonGrid: buttonGrid, type: buttonGrid.type <=> ButtonGridType.TENDER ?  ButtonType.TENDER: ButtonType.PRODUCT, bgColour: "#FFFFFF", textColour: "#000000", imageDisplay: false, textDisplay: true)
        }

        [button: button, buttonImage: buttonImage, availableProcesses: buttonService.getAvailableProcesses(), availableSubPages: buttonService.getAvailableSubPages(), availableTenderTypes: TenderType.values(), productSku: productVariant?.sku, productDescription: productVariant?.product?.description]
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
            button.buttonGrid.addToButtons(button)
            buttonService.saveButtonGrid(button.buttonGrid)

            if (params.removeImage) {
                imageService.deleteFile(String.format("%s%s/", grailsApplication.config.getProperty('wlpos.buttonImageDirectory'), springSecurityService.principal.storeId), button.id + ".png")
                button.imageDisplay = false
                button.textDisplay = true
                buttonService.saveButtonGrid(button.buttonGrid)
            } else {
                if (params.image) {
                    byte[] image = params.image.bytes

                    if (image.length > 0 && params.image.contentType.equals("image/png")) {
                        imageService.saveImageToFile(String.format("%s%s/", grailsApplication.config.getProperty('wlpos.buttonImageDirectory'), springSecurityService.principal.storeId), button.id + ".png", image)
                        button.imageDisplay = true
                        buttonService.saveButtonGrid(button.buttonGrid)
                    }
                }
            }

            Gson gson = new Gson()
            // Make sure the RabbitMQ connection is available, otherwise reject the save.
            try {
                if (!rabbitService.isOpen()) {
                    throw new Exception("Rabbit MQ not available")
                }

                button.buttonGrid.buttons.forEach({
                    SyncMessage syncMessage = new SyncMessage(SyncMessageType.BUTTON_IMAGE, springSecurityService.principal.retailerId, springSecurityService.principal.storeId, 0)
                    syncMessage.setTransactionId(it.id)
                    if (it.imageDisplay) {
                        byte[] image = imageService.getImageFromFile(String.format("%s%s/", grailsApplication.config.getProperty('wlpos.buttonImageDirectory'), springSecurityService.principal.storeId), it.id + ".png")
                        syncMessage.setInsert(true)
                        syncMessage.setByteArray(image)
                    } else {
                        syncMessage.setInsert(false)
                    }
                    try {
                        rabbitService.sendExchangeMessage(String.format("R%d_S%d", syncMessage.getRetailerId(), syncMessage.getStoreId()), gson.toJson(syncMessage))
                    } catch (Exception e) {
                        // TODO handle this better
                        e.printStackTrace()
                    }
                })

                SyncMessage syncMessage = new SyncMessage(SyncMessageType.BUTTON_GRID, springSecurityService.principal.retailerId, springSecurityService.principal.storeId, 0)
                syncMessage.setInsert(true)
                syncMessage.setButtonGrid(button.buttonGrid.getButtonGrid())
                rabbitService.sendExchangeMessage(String.format("R%d_S%d", syncMessage.getRetailerId(), syncMessage.getStoreId()), gson.toJson(syncMessage))

                redirect(controller: "buttonGrid", action: "show", id: button.buttonGrid.id)
            } catch (Exception e) {
                e.printStackTrace()
                def productVariant
                def buttonImage = null

                if (button.type == ButtonType.PRODUCT && button.sku) {
                    productVariant = productService.getProductVariant(button.sku)
                }

                if (button.imageDisplay) {
                    buttonImage = imageService.getImageFromFile(String.format("%s%s/", grailsApplication.config.getProperty('wlpos.buttonImageDirectory'), springSecurityService.principal.storeId), params.id + ".png")
                }

                // TODO Populate an error to display on screen.
                render (view: "edit", model: [button: button, buttonImage: buttonImage, availableProcesses: buttonService.getAvailableProcesses(), availableSubPages: buttonService.getAvailableSubPages(), availableTenderTypes: TenderType.values(), productSku: productVariant?.sku, productDescription: productVariant?.product?.description])
            }
        } else {
            def productVariant
            def buttonImage = null

            if (button.type == ButtonType.PRODUCT && button.sku) {
                productVariant = productService.getProductVariant(button.sku)
            }

            if (button.imageDisplay) {
                buttonImage = imageService.getImageFromFile(String.format("%s%s/", grailsApplication.config.getProperty('wlpos.buttonImageDirectory'), springSecurityService.principal.storeId), params.id + ".png")
            }

            render (view: "edit", model: [button: button, buttonImage: buttonImage, availableProcesses: buttonService.getAvailableProcesses(), availableSubPages: buttonService.getAvailableSubPages(), availableTenderTypes: TenderType.values(), productSku: productVariant?.sku, productDescription: productVariant?.product?.description])
        }
    }

    def unassign(int id) {
        Button button = Button.get(id)

        int buttonGridId = button.buttonGrid.id

        imageService.deleteFile(String.format("%s%s/", grailsApplication.config.getProperty('wlpos.buttonImageDirectory'), springSecurityService.principal.storeId), id + ".png")

        buttonService.deleteButton(button)

        // Make sure the RabbitMQ connection is available, otherwise reject the save.
        try {
            if (!rabbitService.isOpen()) {
                throw new Exception("Rabbit MQ not available")
            }

            Gson gson = new Gson()

            SyncMessage removeImageSyncMessage = new SyncMessage(SyncMessageType.BUTTON_IMAGE, springSecurityService.principal.retailerId, springSecurityService.principal.storeId, 0)
            removeImageSyncMessage.setTransactionId(id)
            removeImageSyncMessage.setInsert(false)

            rabbitService.sendExchangeMessage(String.format("R%d_S%d", removeImageSyncMessage.getRetailerId(), removeImageSyncMessage.getStoreId()), gson.toJson(removeImageSyncMessage))

            SyncMessage syncMessage = new SyncMessage(SyncMessageType.BUTTON_GRID, springSecurityService.principal.retailerId, springSecurityService.principal.storeId, 0)
            syncMessage.setInsert(true)
            syncMessage.setButtonGrid(ButtonGrid.get(buttonGridId).getButtonGrid())

            rabbitService.sendExchangeMessage(String.format("R%d_S%d", syncMessage.getRetailerId(), syncMessage.getStoreId()), gson.toJson(syncMessage))
        } catch (Exception e) {
            e.printStackTrace()
        }

        redirect (controller: "buttonGrid", action: "show", id: buttonGridId)
    }
}