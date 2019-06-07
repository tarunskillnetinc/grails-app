package uk.co.wonderlane.wlpos

import uk.co.wonderlane.wlpos.enums.PrintReceiptOption

class TillSettings {

    def springSecurityService

    int id
    int retailerId
    int storeId
    String receiptMessage1
    String receiptMessage2
    String vatRegistrationNumber
    String addressBuildingNumberOrName
    String addressLine1
    String addressLine2
    String addressTown
    String addressCounty
    String addressCountry
    String addressPostCode
    String phoneNumber
    PrintReceiptOption printReceiptOption

    Date createdDatetime
    Integer createdUserId
    Date updatedDatetime
    Integer updatedUserId

    // This constructor is required or dependency injection (springSecurityService) breaks.
    public TillSettings() { }

    static mapping = {
        autowire true
        table "tillsettings"
        version false

        id column: "id"
        retailerId column: "retailerId"
        storeId column: "storeId"
        receiptMessage1 column: "receiptMessage1"
        receiptMessage2 column: "receiptMessage2"
        vatRegistrationNumber column: "vatRegistrationNumber"
        addressBuildingNumberOrName column: "addressBuildingNumberOrName"
        addressLine1 column: "addressLine1"
        addressLine2 column: "addressLine2"
        addressTown column: "addressTown"
        addressCounty column: "addressCounty"
        addressCountry column: "addressCountry"
        addressPostCode column: "addressPostCode"
        phoneNumber column: "phoneNumber"
        printReceiptOption column: "printReceiptOption", sqlType: "enum", enumType: "string"
        createdDatetime column: "createdDatetime"
        createdUserId column: "createdUserId"
        updatedDatetime column: "updatedDatetime"
        updatedUserId column: "updatedUserId"
    }

    static constraints = {
        id nullable: true
        retailerId nullable: false
        storeId nullable: false
        receiptMessage1 nullable: true, maxSize: 45
        receiptMessage2 nullable: true, maxSize: 45
        vatRegistrationNumber nullable: true, maxSize: 45
        addressBuildingNumberOrName nullable: true, maxSize: 45
        addressLine1 nullable: true, maxSize: 45
        addressLine2 nullable: true, maxSize: 45
        addressTown nullable: true, maxSize: 45
        addressCounty nullable: true, maxSize: 45
        addressCountry nullable: true, maxSize: 45
        addressPostCode nullable: true, maxSize: 45
        phoneNumber nullable: true, maxSize: 45
        printReceiptOption nullable: false
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

    public uk.co.wonderlane.wlpos.entities.TillSettings getTillSettings() {
        uk.co.wonderlane.wlpos.entities.TillSettings tillSettings = new uk.co.wonderlane.wlpos.entities.TillSettings()

        tillSettings.setReceiptMessage1(receiptMessage1)
        tillSettings.setReceiptMessage2(receiptMessage2)
        tillSettings.setVatRegistrationNumber(vatRegistrationNumber)
        tillSettings.setAddressBuildingNumberOrName(addressBuildingNumberOrName)
        tillSettings.setAddressLine1(addressLine1)
        tillSettings.setAddressLine2(addressLine2)
        tillSettings.setAddressTown(addressTown)
        tillSettings.setAddressCounty(addressCounty)
        tillSettings.setAddressCountry(addressCountry)
        tillSettings.setAddressPostCode(addressPostCode)
        tillSettings.setPhoneNumber(phoneNumber)
        tillSettings.setPrintReceiptOption(printReceiptOption)

        return tillSettings
    }
}