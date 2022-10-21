package uk.co.wonderlane.wlpos

import uk.co.wonderlane.wlpos.enums.PrintReceiptOption

class StoreSettings {

    def springSecurityService

    int id
    int retailerId
    Integer parentStoreId
    Integer storeId
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
    Integer varianceQuantity
    BigDecimal varianceValue
    Boolean pickListForceZeroCount

    Date createdDatetime
    Integer createdUserId
    Date updatedDatetime
    Integer updatedUserId
    PriceBand priceBand
    Range range
    BigDecimal selMarginLeft
    BigDecimal selMarginTop

    String primaryColour
    String secondaryColour
    String accentColour
    String primaryTextColour
    String secondaryTextColour
    String accentTextColour

    BigDecimal countIncrement

    // This constructor is required or dependency injection (springSecurityService) breaks.
    public StoreSettings() {}

    static mapping = {
        autowire true
        table "storesettings"
        version false

        id column: "id", sqlType: "smallint"
        retailerId column: "retailerId", sqlType: "tinyint"
        parentStoreId column: "parentStoreId", sqlType: "smallint"
        storeId column: "storeId", sqlType: "smallint"
        receiptMessage1 column: "receiptMessage1"
        receiptMessage2 column: "receiptMessage2"
        vatRegistrationNumber column: "vatRegistrationNumber"
        storeName column: "storeName"
        addressBuildingNumberOrName column: "addressBuildingNumberOrName"
        addressLine1 column: "addressLine1"
        addressLine2 column: "addressLine2"
        addressTown column: "addressTown"
        addressCounty column: "addressCounty"
        addressCountry column: "addressCountry"
        addressPostCode column: "addressPostCode"
        phoneNumber column: "phoneNumber"
        printReceiptOption column: "printReceiptOption", sqlType: "enum", enumType: "string"
        quantityPromptThreshold column: "quantityPromptThreshold"
        valuePromptThreshold column: "valuePromptThreshold"
        varianceQuantity column: "varianceQuantity"
        varianceValue column: "varianceValue"
        pickListForceZeroCount column: "pickListForceZeroCount"
        priceBand column: "priceBandId"
        range column: "rangeId"
        createdDatetime column: "createdDatetime"
        createdUserId column: "createdUserId"
        updatedDatetime column: "updatedDatetime"
        updatedUserId column: "updatedUserId"
        selMarginLeft column: "selMarginLeft"
        selMarginTop column: "selMarginTop"
        primaryColour column: "primaryColour", sqlType: "char", length: 6
        secondaryColour column: "secondaryColour", sqlType: "char", length: 6
        accentColour column: "accentColour", sqlType: "char", length: 6
        primaryTextColour column: "primaryTextColour", sqlType: "char", length: 6
        secondaryTextColour column: "secondaryTextColour", sqlType: "char", length: 6
        accentTextColour column: "accentTextColour", sqlType: "char", length: 6

        countIncrement column: "countIncrement"
    }

    static constraints = {
        id nullable: true
        retailerId nullable: false
        parentStoreId nullable: true, validator: { val, storeSettings -> storeSettings.parentStoreIdValidator(val) }
        storeId nullable: true
        receiptMessage1 nullable: true, maxSize: 100
        receiptMessage2 nullable: true, maxSize: 100
        vatRegistrationNumber nullable: true, maxSize: 45
        storeName nullable: true, maxSize: 45
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
        varianceQuantity nullable: true, min: 1, max: 999
        varianceValue nullable: true, min: BigDecimal.ONE, max: 9999.99
        pickListForceZeroCount nullable: true
        priceBand nullable: false
        range nullable: false
        createdDatetime nullable: true
        createdUserId nullable: true
        updatedDatetime nullable: true
        updatedUserId nullable: true
        selMarginLeft nullable: true
        selMarginTop nullable: true
        primaryColour nullable: true, validator: { value, storeSettings -> storeSettings.colorCodeValidator(value) }
        secondaryColour nullable: true, validator: { value, storeSettings -> storeSettings.colorCodeValidator(value) }
        accentColour nullable: true, validator: { value, storeSettings -> storeSettings.colorCodeValidator(value) }
        primaryTextColour nullable: true, validator: { value, storeSettings -> storeSettings.colorCodeValidator(value) }
        secondaryTextColour nullable: true, validator: { value, storeSettings -> storeSettings.colorCodeValidator(value) }
        accentTextColour nullable: true, validator: { value, storeSettings -> storeSettings.colorCodeValidator(value) }
        countIncrement nullable: false, min: new BigDecimal(0.01), max: BigDecimal.ONE, validator: { value ->
            if (value < new BigDecimal(0.01)) {
                return ['storeSettings.countIncrement.min.notmet']
            }

            if (value > BigDecimal.ONE) {
                return ['storeSettings.countIncrement.max.exceeded']
            }

            return true
        }
    }

    def colorCodeValidator(String colorCode) {
        if (colorCode == null || colorCode.trim().isEmpty()) {
            return true
        }

        if (colorCode.length() != 6) {
            return ['storeSettings.colourCode.length.notmet', colorCode]
        }

        if (colorCode.startsWith('#')) {
            return ['storeSettings.colourCode.format.startsWith.notmet', colorCode]
        }

        if (!isValidHexCode(colorCode)) {
            return ['storeSettings.colourCode.format.notmet', colorCode]
        }
    }

    def parentStoreIdValidator(Integer storeNumber) {
        if (storeNumber == '' || storeNumber == null) {
            return true;
        }

        if (storeNumber == this.storeId) {
            return ["error.StoreSettings.cannotSetParentStoreToItself"]
        }
        // smallint maximum value is 32767
        if (storeNumber > Short.MAX_VALUE) {
            return ["error.StoreSettings.invalidParentStore"]
        }

        def parentStore = StoreSettings.findByStoreIdAndRetailerId(storeNumber, this.retailerId)

        if (parentStore == null) {
            return ["error.StoreSettings.invalidParentStore"]
        } else if (parentStore.parentStoreId == this.storeId) {
            return ["error.StoreSettings.invalidParentStore.circularHierarchy", storeNumber]
        }
    }

    def beforeInsert() {
        createdDatetime = new Date()
        createdUserId = springSecurityService.principal.id
    }

    def beforeUpdate() {
        updatedDatetime = new Date()
        updatedUserId = springSecurityService.principal.id
    }

    public uk.co.wonderlane.wlpos.entities.StoreSettings getStoreSettings() {
        uk.co.wonderlane.wlpos.entities.StoreSettings storeSettings = new uk.co.wonderlane.wlpos.entities.StoreSettings()

        storeSettings.setReceiptMessage1(receiptMessage1)
        storeSettings.setReceiptMessage2(receiptMessage2)
        storeSettings.setVatRegistrationNumber(vatRegistrationNumber)
        storeSettings.setStoreName(storeName)
        storeSettings.setAddressBuildingNumberOrName(addressBuildingNumberOrName)
        storeSettings.setAddressLine1(addressLine1)
        storeSettings.setAddressLine2(addressLine2)
        storeSettings.setAddressTown(addressTown)
        storeSettings.setAddressCounty(addressCounty)
        storeSettings.setAddressCountry(addressCountry)
        storeSettings.setAddressPostCode(addressPostCode)
        storeSettings.setPhoneNumber(phoneNumber)
        storeSettings.setPrintReceiptOption(printReceiptOption)
        storeSettings.setQuantityPromptThreshold(quantityPromptThreshold)
        storeSettings.setValuePromptThreshold(valuePromptThreshold)
        storeSettings.setVarianceQuantity(varianceQuantity)
        storeSettings.setVarianceValue(varianceValue)
        storeSettings.setParentStoreId(parentStoreId)
        storeSettings.setPrimaryColour(primaryColour)
        storeSettings.setSecondaryColour(secondaryColour)
        storeSettings.setAccentColour(accentColour)
        storeSettings.setPrimaryTextColour(primaryTextColour)
        storeSettings.setSecondaryTextColour(secondaryTextColour)
        storeSettings.setAccentTextColour(accentTextColour)
        storeSettings.setCountIncrement(countIncrement)

        return storeSettings
    }

    private boolean isValidHexCode(String s) {
        return s.chars()
                .allMatch({ c -> "0123456789ABCDEFabcdef".indexOf(c) >= 0 });
    }
}