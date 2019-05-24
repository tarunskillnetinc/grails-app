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

        retailerId column: "retailerId"
        code column: "`code`"
        description column: "`description`"
        percentage column: "`percentage`"
        retailerVatCode column: "retailerVatCode"
    }
}