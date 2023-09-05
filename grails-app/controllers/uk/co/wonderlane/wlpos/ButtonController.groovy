package uk.co.wonderlane.wlpos

import org.apache.tomcat.util.http.fileupload.impl.SizeLimitExceededException
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
        def button = getButton(params.id, params.buttonGridId, params.row, params.column)
        def productVariant = null
        def buttonImage = null

        if (button.type == ButtonType.PRODUCT && button.sku) {
            productVariant = productService.getProductVariant(button.sku)
        }

        if (button.imageDisplay) {
            buttonImage = imageService.getButtonImage(button.id)
        }

        [button: button,
         buttonImage: buttonImage,
         availableProcesses: buttonService.getAvailableProcesses(button.buttonGrid.type),
         availableSubPages: buttonService.getOtherButtonGrids(),
         availableTenderTypes: TenderType.values().findAll { it != TenderType.CASHBACK },
         productSku: productVariant?.sku,
         productDescription: productVariant?.product?.description,
         storeId: getStoreId()]
    }

    private Button getButton(String idS, String buttonGridIdS, String rowS, String columnS) {
        def id = 0
        if (idS) {
            id = Integer.parseInt(idS)
        }
        def buttonGridId = 0
        if (buttonGridIdS) {
            buttonGridId = Integer.parseInt(buttonGridIdS)
        }
        def row = 0
        if (rowS) {
            row = Integer.parseInt(rowS)
        }
        def column = 0
        if (columnS) {
            column = Integer.parseInt(columnS)
        }

        def button
        if (id > 0) {
            button = Button.get(id)
        } else {
            def buttonGrid = ButtonGrid.get(buttonGridId)

            button = new Button(row: row, column: column, buttonGrid: buttonGrid, type: buttonGrid.type == ButtonGridType.TENDER ?  ButtonType.TENDER : ButtonType.PRODUCT, bgColour: "#FFFFFF", textColour: "#000000", imageDisplay: false, textDisplay: true)
        }

        return button
    }

    def save(SaveButtonFormCommand form) {
        def fileSizeError = request.getAttribute(MaxFileUploadSizeResolver.FILE_SIZE_EXCEEDED_ERROR)
        if (fileSizeError != null && fileSizeError instanceof SizeLimitExceededException) {
            form.errors.reject('button.error.fileSize.message')
            renderError(getButton(params.id, params.buttonGridId, params.row, params.column), form)
            return
        }

        def button
        def existingButton = true

        if (form.id > 0) {
            button = Button.get(form.id)
        } else {
            button = new Button()
            button.buttonGrid = ButtonGrid.get(form.buttonGridId)
            existingButton = false
        }

        bindData(button, form)

        if (form.hasErrors()) {
            renderError(button, form)
            return
        }

        boolean isHeadOffice = springSecurityService.principal.storeId == null

        if (springSecurityService.principal.storeId != null) {
            button.storeId = springSecurityService.principal.storeId
        }

        if (button.type == ButtonType.BLANK) {
            button.setBlankFields()
            if (existingButton) {
                imageService.deleteButtonImage(button.id)
            }
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

            if (form.removeImage) {
                imageService.deleteButtonImage(button.id)
                button.imageDisplay = false
                button.textDisplay = true
                if (singularButtonUpdate) {
                    buttonService.saveButton(button)
                } else {
                    buttonService.saveButtonGrid(button.buttonGrid)
                }
            } else {
                if (form.image) {
                    byte[] image = form.image.bytes

                    if (image.length > 0 && form.image.contentType == "image/png") {
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
                } else {
                    button.buttonGrid.buttons?.forEach({ iteratedButton ->
                        buildAndSendButtonImageMessage(iteratedButton)
                    })
                }

                SyncMessage syncMessage
                if (singularButtonUpdate) {
                    // Creating a temporary button grid just for the purpose of telling the till app which button grid this button belongs to.
                    uk.co.wonderlane.wlpos.entities.ButtonGrid tempButtonGrid = new uk.co.wonderlane.wlpos.entities.ButtonGrid()
                    tempButtonGrid.setId(button.buttonGrid.id)

                    syncMessage = buildButtonSyncMessage(SyncMessageType.BUTTON)
                    syncMessage.setButton(button.getButton())
                    syncMessage.setButtonGrid(tempButtonGrid)
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
                def productVariant = null
                def buttonImage = null

                if (button.type == ButtonType.PRODUCT && button.sku) {
                    productVariant = productService.getProductVariant(button.sku)
                }

                if (button.imageDisplay) {
                    buttonImage = imageService.getButtonImage(button.id)
                }

                // TODO Populate an error to display on screen.
                render (view: "edit", model: [
                        button: button,
                        buttonImage: buttonImage,
                        availableProcesses: buttonService.getAvailableProcesses(button.buttonGrid?.type),
                        availableSubPages: buttonService.getOtherButtonGrids(),
                        availableTenderTypes: TenderType.values().findAll { it != TenderType.CASHBACK },
                        productSku: productVariant?.sku,
                        productDescription: productVariant?.product?.description,
                        storeId: getStoreId()
                ])
            }
        } else {
            renderError(button, form)
        }
    }

    private void renderError(Button button, SaveButtonFormCommand form) {
        def productVariant = null
        def buttonImage = null

        if (button.type == ButtonType.PRODUCT && button.sku) {
            productVariant = productService.getProductVariant(button.sku)
        }

        if (button.imageDisplay) {
            buttonImage = imageService.getButtonImage(button.id)
        }

        render (view: "edit", model: [
                button: button,
                buttonImage: buttonImage,
                availableProcesses: buttonService.getAvailableProcesses(button.buttonGrid?.type),
                availableSubPages: buttonService.getOtherButtonGrids(),
                availableTenderTypes: TenderType.values().findAll { it != TenderType.CASHBACK },
                productSku: productVariant?.sku,
                productDescription: productVariant?.product?.description,
                storeId: getStoreId(),
                form: form
        ])
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