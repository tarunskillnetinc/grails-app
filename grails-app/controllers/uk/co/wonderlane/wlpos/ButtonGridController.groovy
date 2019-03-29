package uk.co.wonderlane.wlpos

class ButtonGridController {

    def index() {
        def buttonGrids = ButtonGrid.list()

        [buttonGrids: buttonGrids]
    }

    def show() {
        [buttonGrid: ButtonGrid.get(params.id)]
    }
}