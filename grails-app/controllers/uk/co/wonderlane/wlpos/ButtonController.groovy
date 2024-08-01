package uk.co.wonderlane.wlpos

import io.micronaut.http.MediaType
import org.apache.tomcat.util.http.fileupload.impl.SizeLimitExceededException
import org.codehaus.groovy.runtime.InvokerHelper
import uk.co.wonderlane.wlpos.entities.ImageRecord
import uk.co.wonderlane.wlpos.entities.SyncMessage
import uk.co.wonderlane.wlpos.enums.*

class ButtonController {

    def springSecurityService
    def productService
    def buttonService
    IImageService imageService
    def imageRecordService
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
         storeId: getStoreId(),
         displayExactOption: button.tenderType != null && button.tenderType == TenderType.CASH,
         displayManualOption: button.tenderType != null]
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

        if (!form.exact && !form.manual && (form.amount == null || (form.amount != null && form.amount.compareTo(BigDecimal.ZERO) <= 0))) {
            form.errors.reject(form.tenderType == TenderType.CASH ? 'button.error.amount.min.message.exact' : 'button.error.amount.min.message.noExact')
        }

        def button
        def existingButton = true

        if (form.overrideId == form.id) {
            form.id = 0
        }

        if (form.id > 0) {
            button = Button.get(form.id)
        } else {
            button = new Button()
            button.buttonGrid = ButtonGrid.get(form.buttonGridId)
            existingButton = false

            if (springSecurityService.principal.storeId != null && (form.overrideId == null || form.overrideId == 0)) {
                // CORE-2813 - editing an unassigned button at store level:
                // need to create a blank at head office level so that we have something to override
                def parent = createBlankToOverride(form.buttonGridId, form.row, form.column)
                if (parent == null) {
                    form.errors.reject('button.error.noParent')
                    renderError(button, form)
                    return
                }
                button.overrideId = parent.id
                form.overrideId = parent.id
                button.storeId = springSecurityService.principal.storeId
                form.storeId = springSecurityService.principal.storeId
            }
        }

        bindData(button, form)

        if (form.exact) {
            button.amount = BigDecimal.ZERO
        }

        if (form.hasErrors()) {
            renderError(button, form)
            return
        }

        boolean isHeadOffice = springSecurityService.principal.storeId == null

        if (!isHeadOffice) {
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
            } else if (form.image.bytes.length != 0) {
                byte[] image = form.image.bytes
                if (image.length > 0 && form.image.contentType == MediaType.IMAGE_PNG) {
                    button.imageDisplay = true
                    if (image != null) {
                        saveButton(button, image, singularButtonUpdate)
                    }
                }
            } else if (!existingButton && !isHeadOffice) {
                // if store override grab image from s3 and save it again
                if (button.imageDisplay) {
                    byte[] image = imageService.getButtonImage(form.overrideId)
                    saveButton(button, image, singularButtonUpdate)
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
                        storeId: getStoreId(),
                        displayExactOption: button?.tenderType != null && button?.tenderType == TenderType.CASH,
                        displayManualOption: button?.tenderType != null
                ])
            }
        } else {
            renderError(button, form)
        }
    }

    private void renderError(Button button, SaveButtonFormCommand form) {
        def productVariant = null
        def buttonImage = null
        def uploadedImage = false
        if (form.image != null && !form.image.empty && form.image.contentType == MediaType.IMAGE_PNG) {
            buttonImage = form.image.bytes
            uploadedImage = true
        } else if (button.imageDisplay) {
            buttonImage = imageService.getButtonImage(button.id)
        }

        if (button.type == ButtonType.PRODUCT && button.sku) {
            productVariant = productService.getProductVariant(button.sku)
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
                form: form,
                previousImage: uploadedImage,
                displayExactOption: button.tenderType != null && button.tenderType == TenderType.CASH,
                displayManualOption: button.tenderType != null
        ])
    }

    private void buildAndSendButtonImageMessage(Button button) {
        SyncMessage syncMessage = buildButtonSyncMessage(SyncMessageType.IMAGE_SYNC)
        syncMessage.setTransactionId(button.id)

        if (button.imageDisplay) {
            syncMessage.setInsert(true)
            syncMessage.setImageRecord(imageRecordService.getImageRecordByImageId(ImageType.BUTTON, button.id))
        } else {
            syncMessage.setImageRecord(new ImageRecord(
                    imageId: button.id,
                    type: ImageType.BUTTON
            ))
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

    def saveButton(Button button, byte[] image, boolean singularButtonUpdate){
        if (image != null) {
            imageService.saveButtonImage(button.id, image)
            button.imageDisplay = true
        }

        if (singularButtonUpdate) {
            buttonService.saveButton(button)
        } else {
            buttonService.saveButtonGrid(button.buttonGrid)
        }
    }

    private Button createBlankToOverride(int gridId, int row, int column) {
        try {
            Button blank = new Button()
            blank.setBlankFields()
            blank.type = ButtonType.BLANK
            blank.row = row
            blank.column = column
            blank.storeId = null
            blank.overrideId = null

            def now = new Date()
            blank.createdDatetime = now
            blank.createdUserId = springSecurityService.principal.id
            blank.updateDatetime = now
            blank.updatedUserId = springSecurityService.principal.id

            def grid = ButtonGrid.get(gridId)
            blank.buttonGrid = grid
            grid.addToButtons(blank)
            buttonService.saveButtonGrid(grid)
            return blank
        } catch (Exception ignored) {
            return null
        }
    }
}