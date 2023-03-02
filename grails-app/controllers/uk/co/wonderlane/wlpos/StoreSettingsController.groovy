package uk.co.wonderlane.wlpos

import uk.co.wonderlane.wlpos.entities.SyncMessage
import uk.co.wonderlane.wlpos.enums.PrintReceiptOption
import uk.co.wonderlane.wlpos.enums.SyncMessageType

class StoreSettingsController {

    def springSecurityService

    def storeSettingsService
    def rabbitService
    def gsonProvider

    def storeId
    def storeNumber
    def retailerId

    def availableParentStores
    def availablePriceBands
    def availableProductRanges

    private StoreSettings storeSettings

    protected final StoreSettingViewOptions viewOptions = new StoreSettingViewOptions()

    def index() {
        storeId = springSecurityService.principal.storeId
        storeNumber = springSecurityService.principal.storeNumber
        retailerId = springSecurityService.principal.retailerId

        storeSettings = getStoreSettings(storeId, retailerId)
        (availablePriceBands, availableProductRanges, availableParentStores) = loadDropdownData(retailerId, storeNumber)

        setViewOptions();

        [storeSettings               : storeSettings,
         availablePriceBands         : availablePriceBands,
         availableProductRanges      : availableProductRanges,
         availablePrintReceiptOptions: PrintReceiptOption.values(),
         availableParentStores       : availableParentStores,
         viewOptions                 : viewOptions]
    }

    def save() {
        def storeSettings = getStoreSettings(storeId, retailerId)
        def oldPriceBand = storeSettings?.priceBand?.id
        def oldProductRange = storeSettings?.range?.id

        bindData(storeSettings, params)
        storeSettings.retailerId = retailerId

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
                flash.message = ["Store settings saved successfully.", "As the store's range or price band have changed, the store's tills need to be synced in order to receive the necessary product changes.", "Please perform this operation from the Till Connectivity page in the Monitoring menu."]
            } else {
                flash.message = ["Store settings saved successfully."]
            }

            redirect(action: "index")
        } else {
            (availablePriceBands, availableProductRanges, availableParentStores) = loadDropdownData(retailerId, storeNumber)

            render(view: "index", model: [storeSettings               : storeSettings,
                                          availablePriceBands         : availablePriceBands,
                                          availableProductRanges      : availableProductRanges,
                                          availableParentStores       : availableParentStores,
                                          availablePrintReceiptOptions: PrintReceiptOption.values(),
                                          viewOptions                 : viewOptions])
        }
    }

    private List loadDropdownData(retailerId, storeNumber) {
        def availablePriceBands = PriceBand.findAllByRetailerId(retailerId)
        def availableProductRanges = Range.findAllByRetailerId(retailerId)
        def availableParentStores = StoreSettings.findAllByRetailerIdAndTypeAndStoreIdNotEqual(retailerId, StoreType.STORE.getValue(), storeNumber)

        [availablePriceBands, availableProductRanges, availableParentStores]
    }

    private StoreSettings getStoreSettings(storeId, retailerId) {
        storeId ? StoreSettings.findById(storeId) : StoreSettings.findByRetailerIdAndStoreIdIsNull(retailerId)
    }

    private void setViewOptions() {
        def userRoles = springSecurityService.principal.authorities*.authority

        boolean isHeadOffice = false
        boolean isHeadOfficeUser = false
        boolean isEngineerUser = false
        if (storeId == null){isHeadOffice = true}
        if (userRoles && userRoles.size() > 0) {
            isHeadOfficeUser = userRoles.contains("ROLE_HEAD_OFFICE")
            isEngineerUser = userRoles.contains("ROLE_ENGINEER")
        }
        boolean isChildStore = Arrays.asList(StoreType.CAFE.getValue(), StoreType.CANTEEN.getValue()).contains(storeSettings.type)

        viewOptions.showUISettings = !isHeadOffice
        viewOptions.showParentStoreSettings = !isHeadOffice && (isHeadOfficeUser || isEngineerUser) && isChildStore
    }
}

class StoreSettingViewOptions {
    public boolean showUISettings
    public boolean showParentStoreSettings
}