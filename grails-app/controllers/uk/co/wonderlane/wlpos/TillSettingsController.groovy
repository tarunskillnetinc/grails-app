package uk.co.wonderlane.wlpos

import uk.co.wonderlane.wlpos.enums.PrintReceiptOption

class TillSettingsController {

    def index() {
        def tillSettings = TillSettings.findByRetailerIdAndStoreId(1, 23034)

        [tillSettings: tillSettings, availablePrintReceiptOptions: PrintReceiptOption.values()]
    }

    def save() {

    }
}