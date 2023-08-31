package uk.co.wonderlane.wlpos

import org.apache.commons.lang3.EnumUtils
import uk.co.wonderlane.wlpos.entities.SyncMessage
import uk.co.wonderlane.wlpos.enums.ButtonGridType
import uk.co.wonderlane.wlpos.enums.SyncMessageType

class ButtonGridController {

    def springSecurityService
    def buttonService
    def rabbitService

    def index() {

    }

    def show() {
        if (springSecurityService.principal.storeId) {
            flash.error = "You cannot access this page when logged in as a store."
            redirect(uri: "/")
            return
        }

        def buttonGrid

        if (params.id && Integer.parseInt(params.id) > 0) {
            buttonGrid = buttonService.getButtonGrid(Integer.parseInt(params.id))

            if (buttonGrid == null) {
                flash.error = "Button grid not found."
                redirect(action: "index")
                return
            }
        } else {
            if (!EnumUtils.isValidEnum(ButtonGridType.class, params.type)) {
                flash.error = "Button grid not found. "
                redirect(action: "index")
                return
            }
            ButtonGridType type = ButtonGridType.valueOf(params.type)
            if (type.isIn(ButtonGridType.SCO_QUICK_SELL, ButtonGridType.SCO_MANAGER_FUNCTIONS) && !springSecurityService.principal.retailer.config.scoEnabled) {
                flash.error = "Button grid SCO not enabled for current retailer. "
                redirect(action: "index")
                return
            }
            buttonGrid = buttonService.getButtonGrid(type, null, true)
            if (!buttonGrid) {
                ButtonGrid btnGridTemp = new ButtonGrid()
                btnGridTemp.setRetailerId(springSecurityService.principal.retailerId)
                btnGridTemp.setType(type)
                btnGridTemp.setDescription(null)
                btnGridTemp.setButtons(null)
                switch (type) {
                    case 'SALES':
                        btnGridTemp.setRows(1)
                        btnGridTemp.setColumns(4)
                        break
                    case 'TENDER':
                        btnGridTemp.setRows(3)
                        btnGridTemp.setColumns(4)
                        break
                    case 'QUICK_SELL':
                        btnGridTemp.setRows(3)
                        btnGridTemp.setColumns(3)
                        break
                    case 'SCO_QUICK_SELL':
                        btnGridTemp.setRows(3)
                        btnGridTemp.setColumns(3)
                        break
                    case 'MANAGER_FUNCTIONS':
                        btnGridTemp.setRows(4)
                        btnGridTemp.setColumns(2)
                        break
                    case 'SCO_MANAGER_FUNCTIONS':
                        btnGridTemp.setRows(4)
                        btnGridTemp.setColumns(2)
                        break
                    case 'OTHER':
                        btnGridTemp.setRows(4)
                        btnGridTemp.setColumns(4)
                        break
                }
                if (btnGridTemp.validate()) {
                    buttonService.saveButtonGrid(btnGridTemp)
                    buttonGrid = buttonService.getButtonGrid(type, null, true)
                } else {
                    flash.error = "Button grid not found."
                    redirect(action: "index")
                    return
                }
            }
        }

        [buttonGrid: buttonGrid, storeId: getStoreId()]
    }

    def add() {

    }

    def edit(int id) {
        def buttonGrid = buttonService.getButtonGrid(id)

        if (buttonGrid) {
            render (view: "add", model: [buttonGrid: buttonGrid, storeId: getStoreId()])
        } else {
            flash.error = "Button grid not found."
            redirect(action: "index")
        }
    }

    def delete(int id) {
        def buttonGrid = buttonService.getButtonGrid(id)
        try {
            buttonService.deleteButtonGrid(buttonGrid)
            redirect(uri: "/")
        } catch (Exception ex) {
            flash.error = "Error deleting button grid"
            render (view: "add", model: [buttonGrid: buttonGrid, storeId: getStoreId()])
        }
    }

    def save() {
        ButtonGrid buttonGrid = getButtonGrid()

        int previousColumns = buttonGrid.columns
        int previousRows = buttonGrid.rows

        buttonGrid = buttonService.getButtonGrid(buttonGrid.type, buttonGrid.description, true)
        if (!buttonGrid) {
            buttonGrid = new ButtonGrid()
        }
        bindData(buttonGrid, params)

        buttonGrid.retailerId = springSecurityService.principal.retailerId
        buttonGrid.storeId = springSecurityService.principal.storeId


        if (buttonGrid.validate()) {

            int buttonGridNewColumns = buttonGrid.columns
            int buttonGridNewRows = buttonGrid.rows

            List<Button> gridButtonList = buttonGrid?.buttons

            // If we made the button grid smaller, remove any buttons which were on the row/column which no longer exists.
            if (buttonGridNewColumns < previousColumns || buttonGridNewRows < previousRows) {
                def buttonsToRemove = []

                gridButtonList?.each {
                    if (it.row >= buttonGrid.rows || it.column >= buttonGrid.columns) {
                        buttonsToRemove.add(Button.get(it.id))
                    }
                }

                buttonsToRemove?.each {
                    buttonGrid.removeFromButtons(it)
                    buttonService.deleteButton(it)
                }
            }

            buttonService.saveButtonGrid(buttonGrid)

            redirect(controller: "buttonGrid", action: "show", id: buttonGrid.id, storeId: getStoreId())
        } else {
            render(view: "add", model: [buttonGrid: buttonGrid, storeId: getStoreId()])
        }
    }

    ButtonGrid getButtonGrid() {
        ButtonGrid result
        if (params.id && Integer.parseInt(params.id) > 0) {
            result = buttonService.getButtonGrid(Integer.parseInt(params.id))

            // Ensure this is one of their button grids.
            if (!result) {
                flash.error = "Button grid not found."
                redirect(action: "index")
                return
            }
        } else {
            result = new ButtonGrid()
        }
        return result
    }

    def ajaxSyncButtonGrid() {
        ButtonGrid buttonGrid = getButtonGrid()

        SyncMessage syncMessage = buildButtonSyncMessage(SyncMessageType.BUTTON_GRID)
        syncMessage.setInsert(true)
        syncMessage.setButtonGrid(buttonGrid.getButtonGrid())

        rabbitService.sendMessage(syncMessage)
        return buttonGrid
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

    def getStoreId() {
        return springSecurityService.principal.storeId
    }
}