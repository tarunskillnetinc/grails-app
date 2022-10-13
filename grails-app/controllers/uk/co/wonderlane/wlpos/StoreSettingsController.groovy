package uk.co.wonderlane.wlpos

import uk.co.wonderlane.wlpos.entities.SyncMessage
import uk.co.wonderlane.wlpos.enums.PrintReceiptOption
import uk.co.wonderlane.wlpos.enums.SyncMessageType

class StoreSettingsController {

    def springSecurityService

    def storeSettingsService
    def rabbitService
    def gsonProvider

    def index() {
        def storeSettings = springSecurityService.principal.storeId ? StoreSettings.findById(springSecurityService.principal.storeId) : StoreSettings.findByRetailerIdAndStoreIdIsNull(springSecurityService.principal.retailerId)

        def availablePriceBands = PriceBand.findAllByRetailerId(springSecurityService.principal.retailerId)
        def availableProductRanges = Range.findAllByRetailerId(springSecurityService.principal.retailerId)

        [storeSettings: storeSettings, availablePriceBands: availablePriceBands, availableProductRanges: availableProductRanges, availablePrintReceiptOptions: PrintReceiptOption.values()]
    }

    def save() {
        def storeSettings = springSecurityService.principal.storeId ? StoreSettings.findById(springSecurityService.principal.storeId) : StoreSettings.findByRetailerIdAndStoreIdIsNull(springSecurityService.principal.retailerId)

        def oldPriceBand = storeSettings?.priceBand?.id
        def oldProductRange = storeSettings?.range?.id

        bindData(storeSettings, params)

        storeSettings.retailerId = springSecurityService.principal.retailerId

        if (storeSettings.validate()) {
            storeSettingsService.saveStoreSettings(storeSettings)

            // Only need to push this out if it's a store level change, there are no head office controlled settings.
            if (springSecurityService.principal.storeId) {
                SyncMessage syncMessage = new SyncMessage(SyncMessageType.STORE_SETTINGS, springSecurityService.principal.retailerId, springSecurityService.principal.storeNumber, springSecurityService.principal.storeId, 0)
                syncMessage.setInsert(true)
                syncMessage.setStoreSettings(storeSettings.getStoreSettings());

                rabbitService.sendExchangeMessage(String.format("R%d_S%d", syncMessage.getRetailerId(), syncMessage.getStoreNumber()), gsonProvider.gson.toJson(syncMessage))
            }

            if (oldPriceBand != storeSettings.priceBand.id || oldProductRange != storeSettings.range.id) {
                flash.message = ["Store settings saved successfully.", "As the store's range or price band have changed, the store's tills need to be synced in order to receive the necessary product changes.","Please perform this operation from the Till Connectivity page in the Monitoring menu."]
            } else {
                flash.message = ["Store settings saved successfully."]
            }

            redirect(action: "index")
        } else {
            def availablePriceBands = PriceBand.findAllByRetailerId(springSecurityService.principal.retailerId)
            def availableProductRanges = Range.findAllByRetailerId(springSecurityService.principal.retailerId)

            render(view: "index", model: [storeSettings               : storeSettings,
                                          availablePriceBands         : availablePriceBands,
                                          availableProductRanges      : availableProductRanges,
                                          availablePrintReceiptOptions: PrintReceiptOption.values()])
        }
    }
}