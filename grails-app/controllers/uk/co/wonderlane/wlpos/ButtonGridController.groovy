package uk.co.wonderlane.wlpos

import uk.co.wonderlane.wlpos.enums.ButtonGridType

class ButtonGridController {

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

            buttonGrid = ButtonGrid.findByTypeAndRetailerId(type, 1)
        }

        [buttonGrid: buttonGrid]
    }
}
//def UserList = ConferenceUser.executeQuery('from ConferenceUser cu where cu.user = ?', [user])