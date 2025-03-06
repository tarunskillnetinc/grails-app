package uk.co.wonderlane.wlpos.supplier

class Supplier {

    int id
    int retailerId
    Integer storeId
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
    String retailerSupplierId
    boolean deleted

    static mapping = {
        table "supplier"
        version false

        retailerId column: "retailerId", sqlType: "tinyint"
        storeId column: "storeId", sqlType: "smallint"
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
        retailerSupplierId column: "retailerSupplierId", sqlType: "char(15)"
        deleted column: "deleted"
    }

    static constraints = {
        storeId nullable: true
        reference nullable: true, maxSize: 20
        name nullable: false, maxSize: 60
        contactName nullable: true, maxSize: 40
        phoneNumber nullable: true, maxSize: 12
        email nullable: true, maxSize: 50
        customerReference nullable: true, maxSize: 40
        addressBuildingNumberOrName nullable: true, maxSize: 40
        addressLine1 nullable: true, maxSize: 20
        addressLine2 nullable: true, maxSize: 20
        addressTown nullable: true, maxSize: 20
        addressCounty nullable: true, maxSize: 20
        addressCountry nullable: true, maxSize: 20
        addressPostCode nullable: true, maxSize: 8
        symbolGroup nullable: true
        retailerSupplierId nullable: true, maxSize: 15
        deleted nullable: false
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
        supplier.setRetailerSupplierId(retailerSupplierId)
        return supplier
    }
}