package uk.co.wonderlane.wlpos

import org.apache.commons.lang3.EnumUtils
import uk.co.wonderlane.wlpos.enums.ButtonGridType

class ButtonGridController {

    def springSecurityService
    def buttonService

    def index() {

    }

    def show() {
        def buttonGrid

        if (params.id && Integer.parseInt(params.id) > 0) {
            buttonGrid = buttonService.getButtonGrid(Integer.parseInt(params.id))

            if (buttonGrid == null) {
                flash.error = "Button grid not found."
                redirect(action: "index")
                return
            }
        } else {
            ButtonGridType type = null
            if (!EnumUtils.isValidEnum(ButtonGridType.class, params.type)) {
                flash.error = "Button grid not found. "
                redirect(action: "index")
                return
            }
            type = ButtonGridType.valueOf(params.type)
            if (type.equals(ButtonGridType.SCO_QUICK_SELL) && !springSecurityService.principal.retailer.scoEnabled) {
                flash.error = "Button grid SCO not enabled for current retailer. "
                redirect(action: "index")
                return
            }
            buttonGrid = buttonService.getButtonGrid(type)
            if (!buttonGrid) {
                ButtonGrid btnGridTemp = new ButtonGrid()
                btnGridTemp.setRetailerId(springSecurityService.principal.retailerId)
                btnGridTemp.setStoreId(springSecurityService.principal.storeId)
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
                        btnGridTemp.setRows(4)
                        btnGridTemp.setColumns(4)
                        break
                    case 'MANAGER_FUNCTIONS':
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
                } else {
                    flash.error = "Button grid not found."
                    redirect(action: "index")
                    return
                }
                buttonGrid = buttonService.getButtonGrid(type)
            }
        }

        [buttonGrid: buttonGrid]
    }

    def edit(int id) {
        def buttonGrid = buttonService.getButtonGrid(id)

        if (buttonGrid) {
            render (view: "add", model: [buttonGrid: buttonGrid])
        } else {
            flash.error = "Button grid not found."
            redirect(action: "index")
        }
    }

    def save() {
        def buttonGrid

        if (params.id && Integer.parseInt(params.id) > 0) {
            buttonGrid = buttonService.getButtonGrid(Integer.parseInt(params.id))

            // Ensure this is one of their button grids.
            if (!buttonGrid) {
                flash.error = "Button grid not found."
                redirect(action: "index")
                return
            }
        } else {
            buttonGrid = new ButtonGrid()
        }

        int previousColumns = buttonGrid.columns
        int previousRows = buttonGrid.rows

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

            redirect(controller: "buttonGrid", action: "show", id: buttonGrid.id)
        } else {
            render(view: "add", model: [buttonGrid: buttonGrid])
        }
    }
}