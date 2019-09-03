package uk.co.wonderlane.wlpos

import uk.co.wonderlane.wlpos.enums.PrintReceiptOption

class StoreSettings {

    def springSecurityService

    int id
    int retailerId
    int storeId
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

    // This constructor is required or dependency injection (springSecurityService) breaks.
    public StoreSettings() { }

    static mapping = {
        autowire true
        table "storesettings"
        version false

        id column: "id"
        retailerId column: "retailerId"
        storeId column: "storeId"
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
        createdDatetime column: "createdDatetime"
        createdUserId column: "createdUserId"
        updatedDatetime column: "updatedDatetime"
        updatedUserId column: "updatedUserId"
    }

    static constraints = {
        id nullable: true
        retailerId nullable: false
        storeId nullable: false
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
        valuePromptThreshold nullable: true, min: BigDecimal.ONE, max: new BigDecimal(9999)
        varianceQuantity nullable: true, min: 1, max: 999
        varianceValue nullable:true, min: BigDecimal.ONE, max: new BigDecimal(9999)
        pickListForceZeroCount nullable: true
        createdDatetime nullable: true
        createdUserId nullable: true
        updatedDatetime nullable: true
        updatedUserId nullable: true
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

        return storeSettings
    }
}