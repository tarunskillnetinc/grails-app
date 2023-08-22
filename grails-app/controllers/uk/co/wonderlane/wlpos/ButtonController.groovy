package uk.co.wonderlane.wlpos

import org.codehaus.groovy.runtime.InvokerHelper
import uk.co.wonderlane.wlpos.entities.SyncMessage
import uk.co.wonderlane.wlpos.enums.SyncMessageType
import uk.co.wonderlane.wlpos.enums.ButtonType
import uk.co.wonderlane.wlpos.enums.ButtonGridType
import uk.co.wonderlane.wlpos.enums.TenderType

class ButtonController {

    def springSecurityService
    def productService
    def buttonService
    def imageService
    def rabbitService
    def gsonProvider

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
                buttonImage = imageService.getButtonImage(button.id)
            }
        } else {
            def buttonGrid = ButtonGrid.get(params.buttonGridId)

            button = new Button(row: params.row, column: params.column, buttonGrid: buttonGrid, type: buttonGrid.type == ButtonGridType.TENDER ?  ButtonType.TENDER : ButtonType.PRODUCT, bgColour: "#FFFFFF", textColour: "#000000", imageDisplay: false, textDisplay: true)
        }

        [button: button, buttonImage: buttonImage, availableProcesses: buttonService.getAvailableProcesses(button.buttonGrid.type), availableSubPages: buttonService.getOtherButtonGrids(), availableTenderTypes: TenderType.values(), productSku: productVariant?.sku, productDescription: productVariant?.product?.description, storeId: getStoreId()]
    }

    def save() {
        boolean isHeadOffice = springSecurityService.principal.storeId == null

        def button
        def existingButton = true

        if (params.id && Integer.parseInt(params.id) > 0) {
            button = Button.get(params.id)
        } else {
            button = new Button()
            button.buttonGrid = ButtonGrid.get(params.buttonGrid.id)
            existingButton = false
        }

        bindData(button, params)

        if (springSecurityService.principal.storeId != null) {
            button.storeId = springSecurityService.principal.storeId
        }

        if (button?.validate()) {
            boolean buttonGridExists     = button.buttonGrid?.getButtonGrid()
            boolean singularButtonUpdate = buttonGridExists && isHeadOffice

            if (singularButtonUpdate) {
                buttonService.saveButton(button)
            } else {
                if (button.overrideId != null) {
                    buttonService.saveButton(button)
                }
                button.buttonGrid?.addToButtons(button)
                buttonService.saveButtonGrid(button.buttonGrid)
            }

            if (params.removeImage) {
                imageService.deleteButtonImage(button.id)
                button.imageDisplay = false
                button.textDisplay = true
                if (singularButtonUpdate) {
                    buttonService.saveButton(button)
                } else {
                    buttonService.saveButtonGrid(button.buttonGrid)
                }
            } else {
                if (params.image) {
                    byte[] image = params.image.bytes

                    if (image.length > 0 && params.image.contentType.equals("image/png")) {
                        imageService.saveButtonImage(button.id, image)

                        button.imageDisplay = true
                        if (singularButtonUpdate) {
                            buttonService.saveButton(button)
                        } else {
                            buttonService.saveButtonGrid(button.buttonGrid)
                        }
                    }
                }
            }

            try {
                if (singularButtonUpdate) {
                    buildAndSendButtonImageMessage(button)
                }
                button.buttonGrid.buttons?.forEach({iteratedButton ->
                    buildAndSendButtonImageMessage(iteratedButton)
                })

                SyncMessage syncMessage
                if (singularButtonUpdate) {
                    syncMessage = buildButtonSyncMessage(SyncMessageType.BUTTON)
                    syncMessage.setInsert(true)
                } else {
                    syncMessage = buildButtonSyncMessage(SyncMessageType.BUTTON_GRID)
                    syncMessage.setInsert(true)
                    syncMessage.setButtonGrid(button.buttonGrid.getButtonGrid())
                }
                rabbitService.sendMessage(syncMessage)

                redirect(controller: "buttonGrid", action: "show", id: button.buttonGrid.id, storeId: getStoreId())
            } catch (Exception e) {
                e.printStackTrace()
                def productVariant
                def buttonImage = null

                if (button.type == ButtonType.PRODUCT && button.sku) {
                    productVariant = productService.getProductVariant(button.sku)
                }

                if (button.imageDisplay) {
                    buttonImage = imageService.getButtonImage(button.id)
                }

                // TODO Populate an error to display on screen.
                render (view: "edit", model: [button: button, buttonImage: buttonImage, availableProcesses: buttonService.getAvailableProcesses(button.buttonGrid?.type), availableSubPages: buttonService.getOtherButtonGrids(), availableTenderTypes: TenderType.values(), productSku: productVariant?.sku, productDescription: productVariant?.product?.description, storeId: getStoreId()])
            }
        } else {
            def productVariant
            def buttonImage = null

            if (button.type == ButtonType.PRODUCT && button.sku) {
                productVariant = productService.getProductVariant(button.sku)
            }

            if (button.imageDisplay) {
                buttonImage = imageService.getButtonImage(button.id)
            }

            render (view: "edit", model: [button: button, buttonImage: buttonImage, availableProcesses: buttonService.getAvailableProcesses(button.buttonGrid?.type), availableSubPages: buttonService.getOtherButtonGrids(), availableTenderTypes: TenderType.values(), productSku: productVariant?.sku, productDescription: productVariant?.product?.description, storeId: getStoreId()])
        }
    }

    private void buildAndSendButtonImageMessage(Button button) {
        SyncMessage syncMessage = buildButtonSyncMessage(SyncMessageType.BUTTON_IMAGE)
        syncMessage.setTransactionId(button.id)

        if (button.imageDisplay) {
            byte[] image = imageService.getButtonImage(button.id)

            syncMessage.setInsert(true)
            syncMessage.setByteArray(image)
        } else {
            syncMessage.setInsert(false)
        }

        rabbitService.sendMessage(syncMessage)
    }

    private SyncMessage buildButtonSyncMessage(SyncMessageType messageType) {
        return new SyncMessage(
            messageType,
            springSecurityService.principal.retailerId,
            springSecurityService.principal.storeNumber,
            springSecurityService.principal.storeId,
            null
        )
    }

    private Button copyButtonGrid(Button button, Boolean existingButton) {
        def newStoreButton = new Button()
        def storeButtonGrid = new ButtonGrid()
        InvokerHelper.setProperties(storeButtonGrid, button.buttonGrid.properties)
        storeButtonGrid.buttons = new ArrayList<>()
        button.buttonGrid.buttons.forEach({
            def storeButton = new Button()
            InvokerHelper.setProperties(storeButton, it.properties)
            storeButton.id = 0
            storeButton.buttonGrid = storeButtonGrid
            // Fix for copying buttons that are 0 amount in database as these are no longer valid.
            if (it.amount <=> new BigDecimal(0) == 0) {
                storeButton.amount = null
            }
            storeButtonGrid.buttons.add(storeButton)

            if (it.id == button.id) {
                newStoreButton = storeButton
            }
        })

        if (!existingButton) {
            newStoreButton = button
        }

        storeButtonGrid.storeId = springSecurityService.principal.storeId

        newStoreButton.buttonGrid = storeButtonGrid

        if (existingButton) {
            button.refresh()
        }

        return newStoreButton
    }

    def unassign(int id) {
        Button button = Button.get(id)
        int buttonGridId = button.buttonGrid.id
        imageService.deleteButtonImage(button.id)
        buttonService.deleteButton(button)
        buttonService.deleteOverrides(id)
        syncAfterBtnRemoval(id, buttonGridId)
        redirect (controller: "buttonGrid", action: "show", id: buttonGridId, storeId: getStoreId())
    }

    def deleteOverride(int id) {
        Button button = Button.get(id)
        int buttonGridId = button.buttonGrid.id
        imageService.deleteButtonImage(button.id)
        buttonService.deleteOverrideBtn(button.id)
        syncAfterBtnRemoval(id, buttonGridId)
        redirect (controller: "buttonGrid", action: "show", id: buttonGridId, storeId: getStoreId())
    }

    def syncAfterBtnRemoval(id, buttonGridId) {
        SyncMessage removeImageSyncMessage = new SyncMessage(SyncMessageType.BUTTON_IMAGE, springSecurityService.principal.retailerId, springSecurityService.principal.storeNumber, springSecurityService.principal.storeId, null)
        removeImageSyncMessage.setTransactionId(id)
        removeImageSyncMessage.setInsert(false)
        rabbitService.sendMessage(removeImageSyncMessage)

        SyncMessage syncMessage = new SyncMessage(SyncMessageType.BUTTON_GRID, springSecurityService.principal.retailerId, springSecurityService.principal.storeNumber, springSecurityService.principal.storeId, null)
        syncMessage.setInsert(true)
        syncMessage.setButtonGrid(ButtonGrid.get(buttonGridId).getButtonGrid())
        rabbitService.sendMessage(syncMessage)
    }

    def getStoreId() {
        return springSecurityService.principal.storeId
    }
}