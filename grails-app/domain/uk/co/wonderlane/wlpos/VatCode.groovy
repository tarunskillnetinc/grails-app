package uk.co.wonderlane.wlpos

class VatCode {

    int id
    int retailerId
    char code
    String description
    BigDecimal percentage
    String retailerVatCode

    static mapping = {
        table "vatcode"
        version false

        retailerId column: "retailerId", sqlType: "tinyint"
        code column: "`code`"
        description column: "`description`"
        percentage column: "`percentage`"
        retailerVatCode column: "retailerVatCode"
    }

    static constraints = {
        description nullable: true
        retailerVatCode nullable: true
    }

    public uk.co.wonderlane.wlpos.entities.VatCode getVatCode() {
        uk.co.wonderlane.wlpos.entities.VatCode vatCode = new uk.co.wonderlane.wlpos.entities.VatCode()

        vatCode.setId(id)
        vatCode.setRetailerId(retailerId)
        vatCode.setCode(code)
        vatCode.setDescription(description)
        vatCode.setPercentage(percentage)
        vatCode.setRetailerVatCode(retailerVatCode)

        return vatCode
    }
}