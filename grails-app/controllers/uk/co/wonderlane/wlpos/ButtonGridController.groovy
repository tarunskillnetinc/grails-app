package uk.co.wonderlane.wlpos

import uk.co.wonderlane.wlpos.enums.ButtonGridType

class ButtonGridController {

    def springSecurityService

    def index() {

    }

    def show() {
        def buttonGrid

        if (params.id && Integer.parseInt(params.id) > 0) {
            buttonGrid = ButtonGrid.get(params.id)

            if (buttonGrid == null) {
                redirect(action: "index")
                return
            }
        } else {
            ButtonGridType type = null
            try {
                type = ButtonGridType.valueOf(params.type)
            } catch (Exception e) {
                redirect(action: "index")
                return
            }

            buttonGrid = ButtonGrid.findByTypeAndRetailerIdAndStoreId(type, springSecurityService.principal.retailerId, springSecurityService.principal.storeId)
        }

        [buttonGrid: buttonGrid]
    }

    def add() {

    }

    def save() {
        def buttonGrid = new ButtonGrid()

        bindData(buttonGrid, params)

        buttonGrid.retailerId = springSecurityService.principal.retailerId
        buttonGrid.storeId = springSecurityService.principal.storeId

        if (buttonGrid.validate()) {
            buttonGrid.save(flush: true, failOnError: true)
            redirect(controller: "buttonGrid", action: "show", id: buttonGrid.id)
        } else {
            render(view: "add", model: [buttonGrid: buttonGrid])
        }
    }
}