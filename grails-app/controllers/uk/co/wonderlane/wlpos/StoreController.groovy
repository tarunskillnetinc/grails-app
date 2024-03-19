package uk.co.wonderlane.wlpos

import grails.validation.Validateable
import uk.co.wonderlane.wlpos.entities.StoreConfig
import uk.co.wonderlane.wlpos.entities.SyncMessage
import uk.co.wonderlane.wlpos.entities.loyalty.LoyaltyStoreConfig
import uk.co.wonderlane.wlpos.enums.PrintReceiptOption
import uk.co.wonderlane.wlpos.enums.SyncMessageType

import java.math.MathContext
import java.math.RoundingMode

class StoreController {

    def springSecurityService

    def storeService
    def rabbitService
    def gsonProvider

    def availableParentStores
    def availablePriceBands
    def availableProductRanges

    protected final StoreSettingViewOptions viewOptions = new StoreSettingViewOptions()

    def index() {
        def store

        if (springSecurityService.principal.storeId) {
            store = storeService.getStore(springSecurityService.principal.retailerId, springSecurityService.principal.storeId)
        } else {
            store = storeService.getStoreByStoreNumber(springSecurityService.principal.retailerId, null)
        }

        (availablePriceBands, availableProductRanges, availableParentStores) = loadDropdownData(springSecurityService.principal.retailerId, springSecurityService.principal.storeNumber)

        setViewOptions(store.config.storeType.name())

        [storeSettings               : store,
         availablePriceBands         : availablePriceBands,
         availableProductRanges      : availableProductRanges,
         availablePrintReceiptOptions: PrintReceiptOption.values(),
         availableParentStores       : availableParentStores,
         viewOptions                 : viewOptions]
    }

    def save(StoreCommand storeCommand) {
        def store

        if (springSecurityService.principal.storeId) {
            store = storeService.getStore(springSecurityService.principal.retailerId, springSecurityService.principal.storeId)
        } else {
            store = storeService.getStoreByStoreNumber(springSecurityService.principal.retailerId, null)
        }

        def oldPriceBand = store?.priceBand?.id
        def oldProductRange = store?.range?.id

        // Note, this saving is deliberately being done completely outside of Hibernate and GORM because they don't handle JSON columns well (at all).
        if (storeCommand.validate() & storeCommand.config.validate()) { // Deliberately a single & so that both validates get called even if the first one fails.
            StoreConfig storeConfig = new StoreConfig()
            LoyaltyStoreConfig loyaltyStoreConfig = new LoyaltyStoreConfig()

            bindData(storeConfig, storeCommand.config)
            bindData(loyaltyStoreConfig, storeCommand.config.loyaltyStoreConfig)
            storeConfig.loyaltyStoreConfig = loyaltyStoreConfig

            storeService.saveStoreSettings(storeCommand, gsonProvider.gson.toJson(storeConfig))

            // Only need to push this out if it's a store level change, there are no head office controlled settings.
            if (springSecurityService.principal.storeId) {
                SyncMessage syncMessage = new SyncMessage(SyncMessageType.STORE_SETTINGS, springSecurityService.principal.retailerId, springSecurityService.principal.storeNumber, springSecurityService.principal.storeId, 0)
                syncMessage.setInsert(true)
                syncMessage.setStoreSettings(
                        storeService.getStore(springSecurityService.principal.retailerId, springSecurityService.principal.storeId).refresh().getStore()
                )

                rabbitService.sendMessage(syncMessage)
            }

            if (oldPriceBand != storeCommand.priceBand.id || oldProductRange != storeCommand.range.id) {
                // If we've edited the store we're logged in as, refresh our login session so that spring security knows about our new range/price band.
                if (springSecurityService.principal.storeNumber == store.config.storeNumber) {
                    springSecurityService.principal.priceBand = storeCommand.priceBand
                    springSecurityService.principal.range = storeCommand.range
                }

                flash.message = ["Store settings saved successfully.", "As the store's range or price band have changed, the store's tills need to be synced in order to receive the necessary product changes.", "Please perform this operation from the Till Connectivity page in the Monitoring menu."]
            } else {
                flash.message = ["Store settings saved successfully."]
            }

            redirect(action: "index")
        } else {
            (availablePriceBands, availableProductRanges, availableParentStores) = loadDropdownData(springSecurityService.principal.retailerId, springSecurityService.principal.storeNumber)

            render(view: "index", model: [storeSettings               : storeCommand,
                                          configErrors                : storeCommand.config,
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

        def allParentStores = storeService.getStoresByType(retailerId, StoreType.STORE)
        allParentStores.removeAll { it.config.storeNumber == storeNumber }

        [availablePriceBands, availableProductRanges, allParentStores]
    }

    private void setViewOptions(String storeType) {
        def userRoles = springSecurityService.principal.authorities*.authority

        boolean isHeadOffice = false
        boolean isHeadOfficeUser = false
        boolean isEngineerUser = false

        if (springSecurityService.principal.storeId == null) {
            isHeadOffice = true
        }

        if (userRoles && userRoles.size() > 0) {
            isHeadOfficeUser = userRoles.contains("ROLE_HEAD_OFFICE")
            isEngineerUser = userRoles.contains("ROLE_ENGINEER")
        }

        boolean isChildStore = Arrays.asList(StoreType.CAFE.getValue(), StoreType.CANTEEN.getValue()).contains(storeType)

        viewOptions.showUISettings = !isHeadOffice
        viewOptions.showParentStoreSettings = !isHeadOffice && (isHeadOfficeUser || isEngineerUser) && isChildStore
        viewOptions.showLoyaltySettings = !isHeadOffice
    }
}

class StoreSettingViewOptions {
    public boolean showUISettings
    public boolean showParentStoreSettings
    public boolean showLoyaltySettings
}

class StoreCommand implements Validateable {
    int id
    int retailerId
    Integer parentStoreId
    PriceBand priceBand
    Range range

    StoreConfigCommand config

    static constraints = {
        id nullable: true
        retailerId nullable: false
        parentStoreId nullable: true
        priceBand nullable: false
        range nullable: false
        config nullable: false
    }
}

class StoreConfigCommand implements Validateable {
    Integer storeNumber
    uk.co.wonderlane.wlpos.enums.StoreType storeType
    String receiptMessage1
    String receiptMessage2
    String vatRegistrationNumber
    String storeName
    String addressBuildingNumberOrName
    String addressLine1
    String addressLine2
    String addressTown
    String addressCounty
    String addressCountry
    String addressPostCode
    String phoneNumber
    PrintReceiptOption printReceiptOption
    Integer quantityPromptThreshold
    BigDecimal valuePromptThreshold
    Boolean pickListForceZeroCount
    String primaryColour
    String secondaryColour
    String accentColour
    String primaryTextColour
    String secondaryTextColour
    String accentTextColour
    String backgroundColour
    int stockLevelThreshold
    BigDecimal countIncrement
    String website
    String companyNumber
    String returnsMessage
    LoyaltyStoreConfigCommand loyaltyStoreConfig

    static constraints = {
        storeNumber nullable: true
        storeType nullable: true
        receiptMessage1 nullable: true, maxSize: 100
        receiptMessage2 nullable: true, maxSize: 100
        vatRegistrationNumber nullable: true, maxSize: 45
        storeName nullable: false, blank: false, maxSize: 45
        addressBuildingNumberOrName nullable: true, maxSize: 45
        addressLine1 nullable: true, maxSize: 45
        addressLine2 nullable: true, maxSize: 45
        addressTown nullable: true, maxSize: 45
        addressCounty nullable: true, maxSize: 45
        addressCountry nullable: true, maxSize: 45
        addressPostCode nullable: true, maxSize: 45
        phoneNumber nullable: true, maxSize: 45
        printReceiptOption nullable: false
        quantityPromptThreshold nullable: true, min: 1, max: 999
        valuePromptThreshold nullable: true, min: BigDecimal.ONE, max: 9999.99
        pickListForceZeroCount nullable: true
        primaryColour nullable: true
        secondaryColour nullable: true
        accentColour nullable: true
        primaryTextColour nullable: true
        secondaryTextColour nullable: true
        accentTextColour nullable: true
        backgroundColour nullable: true
        stockLevelThreshold nullable: false
//        selMarginLeft nullable: true
//        selMarginTop nullable: true
        primaryColour nullable: true, validator: { value, storeConfig -> storeConfig.colorCodeValidator(value) }
        secondaryColour nullable: true, validator: { value, storeConfig -> storeConfig.colorCodeValidator(value) }
        accentColour nullable: true, validator: { value, storeConfig -> storeConfig.colorCodeValidator(value) }
        primaryTextColour nullable: true, validator: { value, storeConfig -> storeConfig.colorCodeValidator(value) }
        secondaryTextColour nullable: true, validator: { value, storeConfig -> storeConfig.colorCodeValidator(value) }
        accentTextColour nullable: true, validator: { value, storeConfig -> storeConfig.colorCodeValidator(value) }
        backgroundColour nullable: true, validator: { value, storeConfig -> storeConfig.colorCodeValidator(value) }
        countIncrement nullable: false, min: new BigDecimal(0.01).round(new MathContext(1, RoundingMode.HALF_EVEN)), max: BigDecimal.ONE
        website nullable: true, maxsize: 40
        companyNumber nullable: true, maxSize: 10
        returnsMessage nullable: true, maxSize: 200
        loyaltyStoreConfig nullable: true
    }

    def colorCodeValidator(String colorCode) {
        if (colorCode == null || colorCode.trim().isEmpty()) {
            return true
        }

        if (colorCode.length() != 6) {
            return ['storeConfigCommand.colourCode.length.notmet', colorCode]
        }

        if (colorCode.startsWith('#')) {
            return ['storeConfigCommand.colourCode.format.startsWith.notmet', colorCode]
        }

        if (!isValidHexCode(colorCode)) {
            return ['storeConfigCommand.colourCode.format.notmet', colorCode]
        }
    }

    private boolean isValidHexCode(String s) {
        return s.chars().allMatch({ c -> "0123456789ABCDEFabcdef".indexOf(c) >= 0 });
    }
}

class LoyaltyStoreConfigCommand {
    boolean isLoyaltyEnable = false
}