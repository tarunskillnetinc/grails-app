package uk.co.wonderlane.wlpos.supplier

class Supplier {

    int id
    int retailerId
    int storeId
    String name
    String reference
    String contactName
    String phoneNumber
    String email
    String customerReference
    String addressBuildingNumberOrName
    String addressLine1
    String addressLine2
    String addressTown
    String addressCounty
    String addressCountry
    String addressPostCode
    SymbolGroup symbolGroup

    static mapping = {
        table "supplier"
        version false

        retailerId column: "retailerId"
        storeId column: "storeId"
        name column: "name"
        reference column: "reference"
        contactName column: "contactName"
        phoneNumber column: "phoneNumber"
        email column: "email"
        customerReference column: "customerReference"
        addressBuildingNumberOrName column: "addressBuildingNumberOrName"
        addressLine1 column: "addressLine1"
        addressLine2 column: "addressLine2"
        addressTown column: "addressTown"
        addressCounty column: "addressCounty"
        addressCountry column: "addressCountry"
        addressPostCode column: "addressPostCode"
        symbolGroup column: "symbolGroupId"
    }

    static constraints = {
        reference nullable: true, maxSize: 40
        contactName nullable: true, maxSize: 50
        phoneNumber nullable: true, maxSize: 30
        email nullable: true, maxSize: 50
        customerReference nullable: true, maxSize: 40
        addressBuildingNumberOrName nullable: true, maxSize: 45
        addressLine1 nullable: true, maxSize: 45
        addressLine2 nullable: true, maxSize: 45
        addressTown nullable: true, maxSize: 45
        addressCounty nullable: true, maxSize: 45
        addressCountry nullable: true, maxSize: 45
        addressPostCode nullable: true, maxSize: 10
        symbolGroup nullable: true
    }

    public uk.co.wonderlane.wlpos.entities.supplier.Supplier getSupplier() {
        uk.co.wonderlane.wlpos.entities.supplier.Supplier supplier = new uk.co.wonderlane.wlpos.entities.supplier.Supplier()

        supplier.setId(id)
        supplier.setRetailerId(retailerId)
        supplier.setStoreId(storeId)
        supplier.setName(name)
        supplier.setReference(reference)
        supplier.setContactName(contactName)
        supplier.setPhoneNumber(phoneNumber)
        supplier.setEmail(email)
        supplier.setCustomerReference(customerReference)
        supplier.setAddressBuildingNumberOrName(addressBuildingNumberOrName)
        supplier.setAddressLine1(addressLine1)
        supplier.setAddressLine2(addressLine2)
        supplier.setAddressTown(addressTown)
        supplier.setAddressCounty(addressCounty)
        supplier.setAddressCountry(addressCountry)
        supplier.setAddressPostCode(addressPostCode)
        supplier.setSymbolGroupId(symbolGroup?.getId())

        return supplier
    }
}