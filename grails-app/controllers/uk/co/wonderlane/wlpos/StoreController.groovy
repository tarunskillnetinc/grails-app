package uk.co.wonderlane.wlpos

import grails.plugin.springsecurity.SpringSecurityService
import grails.plugin.springsecurity.annotation.Secured
import grails.validation.Validateable
import uk.co.wonderlane.wlpos.entities.StoreAdditionalDetail
import uk.co.wonderlane.wlpos.entities.StoreConfig
import uk.co.wonderlane.wlpos.entities.SyncMessage
import uk.co.wonderlane.wlpos.enums.PrintReceiptOption
import uk.co.wonderlane.wlpos.enums.SyncMessageType

import java.math.MathContext
import java.math.RoundingMode
import java.util.regex.Matcher
import java.util.regex.Pattern

class StoreController {

    def springSecurityService

    def storeService
    def rabbitService
    def gsonProvider

    def availableParentStores
    def availablePriceBands
    def availableProductRanges

    protected final StoreSettingViewOptions viewOptions = new StoreSettingViewOptions()

    @Secured(['ROLE_ENGINEER', 'ROLE_HEAD_OFFICE'])
    def index() {

        [storeNumberFilter: params.storeNumberFilter,
         storeNameFilter: params.storeNameFilter,
         showDeletedFilter: params.showDeletedFilter,
         max: params.max,
         offset: params.offset,
         sort: params.sort,
         order: params.order]
    }

    @Secured(['ROLE_ENGINEER', 'ROLE_HEAD_OFFICE'])
    def ajaxGetStores() {
        Integer storeNumberFilter
        String storeNameFilter
        boolean showDeletedFilter = false

        def sortParams = [:]

        try {
            if (!params.sort) {
                sortParams = [max: 50, offset: 0, sort: "storeNumber", order: "ASC"]
            } else {
                sortParams.max = Integer.parseInt(params.max)
                sortParams.offset = Integer.parseInt(params.offset)
                sortParams.sort = params.sort
                sortParams.order = params.order
            }

            if (params.storeNumberFilter && params.storeNumberFilter.isNumber()) {
                storeNumberFilter = Integer.parseInt(params.storeNumberFilter)
            }
            if (params.storeNameFilter && params.storeNameFilter != "null") {
                storeNameFilter = params.storeNameFilter
            }
            if (params.showDeletedFilter == "true") {
                showDeletedFilter = true
            }

            def (stores, storeCount) = storeService.searchStores(springSecurityService.principal.retailerId, storeNumberFilter, storeNameFilter, showDeletedFilter, sortParams)

            render(template: "storeSearchResults", model: [stores: stores, totalResults: storeCount, sortParams: sortParams, storeNameFilter: storeNameFilter ?: "", storeNumberFilter: storeNumberFilter ?: "", showDeletedFilter: showDeletedFilter])
        } catch (Exception e) {
            render status: 500, text:" Error searching for stores."
        }
    }

    @Secured(['ROLE_ENGINEER', 'ROLE_HEAD_OFFICE'])
    def ajaxDeleteStore(int storeId, boolean deleted) {
        try {
            def store = storeService.getStore(springSecurityService.principal.retailerId, storeId)

            store.deleted = deleted

            storeService.saveStore(store)

            render status: 200, text: "Store $store.config.storeNumber has been ${deleted ? 'deleted' : 'reinstated'}."
        } catch (Exception e) {
            render status: 500, text: "Error ${deleted ? 'deleting' : 'reinstating'} store."
        }
    }

    @Secured(['ROLE_ENGINEER', 'ROLE_HEAD_OFFICE'])
    def add() {
        def parentStores = storeService.getStoresByType(springSecurityService.principal.retailerId, StoreType.STORE).sort { it.config.storeNumber }
        def storeTypes = StoreType.values().findAll { it != StoreType.HEAD_OFFICE }
        def priceBands = PriceBand.findAllByRetailerId(springSecurityService.principal.retailerId).sort { it.description }
        def ranges = Range.findAllByRetailerId(springSecurityService.principal.retailerId).sort { it.description }

        [storeTypes: storeTypes, parentStores: parentStores, priceBands: priceBands, ranges: ranges]
    }

    @Secured(['ROLE_ENGINEER', 'ROLE_HEAD_OFFICE'])
    def config() {
        def store
        boolean viewingOwnStore = true

        // Logged in as a store so return only your store.
        if (springSecurityService.principal.storeId) {
            store = storeService.getStore(springSecurityService.principal.retailerId, springSecurityService.principal.storeId)
        } else if (params.id && params.id.isNumber()) {
            // If you're logged in at HO level you can access any store's config.
            def userRoles = springSecurityService.principal.authorities*.authority

            if (userRoles.contains("ROLE_HEAD_OFFICE") || userRoles.contains("ROLE_ENGINEER")) {
                store = storeService.getStore(springSecurityService.principal.retailerId, Integer.parseInt(params.id))

                viewingOwnStore = false
            }
        } else {
            // HO level accessing own store.
            store = storeService.getStoreByStoreNumber(springSecurityService.principal.retailerId, null)
        }

        (availablePriceBands, availableProductRanges, availableParentStores) = loadDropdownData(springSecurityService.principal.retailerId, springSecurityService.principal.storeNumber)

        setViewOptions(store?.config?.storeType?.name(), viewingOwnStore)

        [storeSettings               : store,
         availablePriceBands         : availablePriceBands,
         availableProductRanges      : availableProductRanges,
         availablePrintReceiptOptions: PrintReceiptOption.values(),
         availableParentStores       : availableParentStores,
         viewOptions                 : viewOptions,
         storeNumberFilter           : params.storeNumberFilter,
         storeNameFilter             : params.storeNameFilter,
         showDeletedFilter           : params.showDeletedFilter,
         max                         : params.max,
         offset                      : params.offset,
         sort                        : params.sort,
         order                       : params.order]
    }

    @Secured(['ROLE_ENGINEER', 'ROLE_HEAD_OFFICE'])
    def saveNewStore(NewStoreCommand newStoreCommand) {
        if (!newStoreCommand.validate()) {
            def parentStores = storeService.getStoresByType(springSecurityService.principal.retailerId, StoreType.STORE).sort { it.config.storeNumber }
            def storeTypes = StoreType.values().findAll { it != StoreType.HEAD_OFFICE }
            def priceBands = PriceBand.findAllByRetailerId(springSecurityService.principal.retailerId).sort { it.description }
            def ranges = Range.findAllByRetailerId(springSecurityService.principal.retailerId).sort { it.description }

            render(view: "add", model: [store : newStoreCommand, parentStores: parentStores, storeTypes: storeTypes, priceBands: priceBands, ranges: ranges])
        } else {
            // Validated.
            def storeCopyingConfigFrom = null
            if (newStoreCommand.copyConfigFrom) {
                storeCopyingConfigFrom = storeService.getStore(springSecurityService.principal.retailerId, newStoreCommand.copyConfigFrom)
            }

            Store store = new Store()
            store.retailerId = springSecurityService.principal.retailerId
            store.parentStoreId = newStoreCommand.parentStoreId

            if (storeCopyingConfigFrom) {
                store.range = storeCopyingConfigFrom.range
                store.priceBand = storeCopyingConfigFrom.priceBand
            } else {
                store.range = newStoreCommand.range
                store.priceBand = newStoreCommand.priceBand
            }

            StoreConfig storeConfig = storeCopyingConfigFrom ? storeCopyingConfigFrom.config : new StoreConfig()

            // These fields are taken from the UI, the rest of the config values will be inherited (assuming a "copy from" store was selected).
            storeConfig.storeType = uk.co.wonderlane.wlpos.enums.StoreType.valueOf(newStoreCommand.type.name())
            storeConfig.storeNumber = newStoreCommand.storeNumber
            storeConfig.storeName = newStoreCommand.storeName
            storeConfig.addressBuildingNumberOrName = newStoreCommand.addressBuildingNumberOrName
            storeConfig.addressLine1 = newStoreCommand.addressLine1
            storeConfig.addressLine2 = newStoreCommand.addressLine2
            storeConfig.addressLine3 = newStoreCommand.addressLine3
            storeConfig.addressTown = newStoreCommand.addressTown
            storeConfig.addressCounty = newStoreCommand.addressCounty
            storeConfig.addressCountry = newStoreCommand.addressCountry
            storeConfig.addressPostCode = newStoreCommand.addressPostCode
            storeConfig.phoneNumber = newStoreCommand.phoneNumber
            storeConfig.alternativePhoneNumber = newStoreCommand.alternativePhoneNumber
            storeConfig.emailAddress = newStoreCommand.emailAddress
            storeConfig.anaCode = newStoreCommand.anaCode
            storeConfig.netSalesArea = newStoreCommand.netSalesArea
            storeConfig.latitude = newStoreCommand.latitude
            storeConfig.longitude = newStoreCommand.longitude

            store.config = storeConfig

            storeService.saveStore(store)

            flash.message = "Store created successfully."
            redirect (action: "config", id: store.id)
        }
    }

    def save(StoreCommand storeCommand) {
        def store

        // Logged in as a store so return only your store.
        if (springSecurityService.principal.storeId) {
            store = storeService.getStore(springSecurityService.principal.retailerId, springSecurityService.principal.storeId)
        } else if (params.id && params.id.isNumber()) {
            // If you're logged in at HO level you can access any store's config.
            def userRoles = springSecurityService.principal.authorities*.authority

            if (userRoles.contains("ROLE_HEAD_OFFICE") || userRoles.contains("ROLE_ENGINEER")) {
                store = storeService.getStore(springSecurityService.principal.retailerId, Integer.parseInt(params.id))
            }
        } else {
            // HO level accessing own store.
            store = storeService.getStoreByStoreNumber(springSecurityService.principal.retailerId, null)
        }

        def oldPriceBand = store?.priceBand?.id
        def oldProductRange = store?.range?.id

        // Note, this saving is deliberately being done completely outside of Hibernate and GORM because they don't handle JSON columns well (at all).
        if (storeCommand.validate() & storeCommand.config.validate()) { // Deliberately a single & so that both validates get called even if the first one fails.
            StoreConfig storeConfig = new StoreConfig()

            bindData(storeConfig, storeCommand.config)


            storeService.saveStore(storeCommand, gsonProvider.gson.toJson(storeConfig))

            // Only need to push this out if it's a store level change, there are no head office controlled settings.
            if (springSecurityService.principal.storeId) {
                SyncMessage syncMessage = new SyncMessage(SyncMessageType.STORE_SETTINGS, springSecurityService.principal.retailerId, springSecurityService.principal.storeNumber, springSecurityService.principal.storeId, 0)
                syncMessage.setInsert(true)
                syncMessage.setStoreSettings(storeService.getStore(springSecurityService.principal.retailerId, springSecurityService.principal.storeId).refresh().getStore())

                rabbitService.sendMessage(syncMessage)
            }

            if (oldPriceBand != storeCommand.priceBand.id || oldProductRange != storeCommand.range.id) {
                // If we've edited the store we're logged in as, refresh our login session so that spring security knows about our new range/price band.
                if (springSecurityService.principal.storeNumber == store.config.storeNumber) {
                    springSecurityService.principal.priceBand = storeCommand.priceBand
                    springSecurityService.principal.range = storeCommand.range
                }

                flash.message = "Store settings saved successfully. \nAs the store's range or price band have changed, the store's tills need to be synced in order to receive the necessary product changes. \nPlease perform this operation from the Till Connectivity page in the Monitoring menu."
            } else {
                flash.message = "Store settings saved successfully."
            }

            redirect(action: "config", id: store.id)
        } else {
            (availablePriceBands, availableProductRanges, availableParentStores) = loadDropdownData(springSecurityService.principal.retailerId, springSecurityService.principal.storeNumber)

            render(view: "config", model: [storeSettings               : storeCommand,
                                           configErrors                : storeCommand.config,
                                           availablePriceBands         : availablePriceBands,
                                           availableProductRanges      : availableProductRanges,
                                           availableParentStores       : availableParentStores,
                                           availablePrintReceiptOptions: PrintReceiptOption.values(),
                                           viewOptions                 : viewOptions])
        }
    }

    def ajaxAddStoreAdditionalDetail() {
        render(template: "addStoreAdditionalDetail", model: [index : params?.index, description: params?.description, value: params?.value])
    }

    def ajaxSaveStoreAdditionalDetail(AddStoreAdditionalDetailCommand additionalDetailCommand) {
        render(template: "storeAdditionalDetail", model: [index: additionalDetailCommand?.index, detail: additionalDetailCommand?.storeAdditionalDetails])
    }

    private List loadDropdownData(retailerId, storeNumber) {
        def availablePriceBands = PriceBand.findAllByRetailerId(retailerId)
        def availableProductRanges = Range.findAllByRetailerId(retailerId)

        def allParentStores = storeService.getStoresByType(retailerId, StoreType.STORE)
        allParentStores.removeAll { it.config.storeNumber == storeNumber }

        [availablePriceBands, availableProductRanges, allParentStores]
    }

    private void setViewOptions(String storeType, boolean viewingOwnStore) {
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

        boolean isChildStore = Arrays.asList(StoreType.CAFE.name(), StoreType.CANTEEN.name()).contains(storeType)

        viewOptions.showUISettings = !isHeadOffice || !viewingOwnStore
        viewOptions.showParentStoreSettings = (!isHeadOffice || !viewingOwnStore) && (isHeadOfficeUser || isEngineerUser) && isChildStore
    }
}

class StoreSettingViewOptions {
    public boolean showUISettings
    public boolean showParentStoreSettings
}

class NewStoreCommand implements Validateable {

    SpringSecurityService springSecurityService
    StoreService storeService

    Integer storeNumber
    StoreType type
    String storeName
    String addressBuildingNumberOrName
    String addressLine1
    String addressLine2
    String addressLine3
    String addressTown
    String addressCounty
    String addressCountry
    String addressPostCode
    String phoneNumber
    String alternativePhoneNumber
    String emailAddress
    String anaCode
    String netSalesArea
    String longitude
    String latitude
    Integer parentStoreId
    Integer copyConfigFrom
    Range range
    PriceBand priceBand

    static constraints = {
        storeNumber nullable: false,blank: false, min:1, max: 999999, validator: { val, obj ->
            def existingStore = obj.storeService.getStoreByStoreNumber(obj.springSecurityService.principal.retailerId, val)

            if (existingStore) {
                return false
            }
        }
        type nullable: false
        storeName nullable: false, blank: false, maxSize: 30
        addressBuildingNumberOrName nullable: true, maxSize: 30
        addressLine1 nullable: true, maxSize: 20
        addressLine2 nullable: true, maxSize: 20
        addressLine3 nullable: true, maxSize: 20
        addressTown nullable: true, maxSize: 20
        addressCounty nullable: true, maxSize: 20
        addressCountry nullable: true, maxSize: 20
        addressPostCode nullable: true, maxSize: 8, validator: {val, obj ->
            if (val != null && Pattern.compile("[^a-z0-9 ]", Pattern.CASE_INSENSITIVE).matcher(val).find())
                return false
        }
        phoneNumber nullable: true, maxSize: 12, validator: {val, obj ->
            if(val != null && !val.isNumber()){
                return false
            }
        }
        alternativePhoneNumber nullable: true, maxSize: 12, validator: {val, obj ->
            if(val != null && !val.isNumber()){
                return false
            }
        }
        emailAddress email: true, maxSize: 254, nullable: true
        anaCode nullable: true, maxSize: 30
        netSalesArea nullable: true, validator: { val ->
            if (val == null) return true // Allow null values

            try {
                BigDecimal value = new BigDecimal(val)
                if (value >= new BigDecimal("9999999.9999")) {
                    return false
                }
                //Fail if the value has more than 4 decimal places
                if (value.scale() > 4) {
                    return false
                }
                return true
            } catch (NumberFormatException e) {
                return false
            }
        }
        longitude nullable: true, maxSize: 20
        latitude nullable: true, maxSize: 20
        parentStoreId nullable: true
        copyConfigFrom nullable: true
        range nullable: true
        priceBand nullable: true
    }
}

class StoreCommand implements Validateable {
    int id
    Integer parentStoreId
    PriceBand priceBand
    Range range
    String retailerStoreId
    boolean deleted

    StoreConfigCommand config
    List<StoreAdditionalDetailCommand> storeAdditionalDetails

    static constraints = {
        id nullable: true
        parentStoreId nullable: true
        priceBand nullable: false
        range nullable: false
        retailerStoreId nullable: true
        config nullable: false
        storeAdditionalDetails nullable: true
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
    String addressLine3
    String addressTown
    String addressCounty
    String addressCountry
    String addressPostCode
    String phoneNumber
    String alternativePhoneNumber
    String emailAddress
    String anaCode
    String netSalesArea
    String longitude
    String latitude
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

    static constraints = {
        importFrom NewStoreCommand, include: [
                "storeName", "addressBuildingNumberOrName", "addressLine1", "addressLine2", "addressLine3",
                "addressTown", "addressCounty", "addressCountry", "addressPostCode", "phoneNumber",
                "alternativePhoneNumber", "emailAddress", "anaCode", "netSalesArea", "longitude", "latitude"]
        storeNumber nullable: true
        storeType nullable: true
        receiptMessage1 nullable: true, maxSize: 100
        receiptMessage2 nullable: true, maxSize: 100
        vatRegistrationNumber nullable: true, maxSize: 45
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

class StoreAdditionalDetailCommand implements Validateable {
    String description
    String value
}

class AddStoreAdditionalDetailCommand implements Validateable {
    int index
    StoreAdditionalDetailCommand storeAdditionalDetails
}