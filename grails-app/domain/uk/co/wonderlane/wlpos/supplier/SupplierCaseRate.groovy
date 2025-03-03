package uk.co.wonderlane.wlpos.supplier

class SupplierCaseRate {
    static belongsTo = [supplier: Supplier]

    int retailerId;
    BigDecimal caseRate;
    Date caseRateEffectiveDate;

    static mapping = {
        table "suppliercaserate"
        version false

        supplier column: "supplierId"

        id column: "id", sqlType: "int"
        retailerId column: "retailerId", sqlType: "tinyint"
        caseRate column: "caseRate"
        caseRateEffectiveDate column: "caseRateEffectiveDate"
    }

    static constraints = {
        retailerId nullable: false
        caseRate nullable: false
        caseRateEffectiveDate nullable: false
    }

    public uk.co.wonderlane.wlpos.entities.supplier.Supplier getSupplierCaseRate() {
        uk.co.wonderlane.wlpos.entities.supplier.SupplierCaseRate supplierCaseRate = new uk.co.wonderlane.wlpos.entities.supplier.SupplierCaseRate()

        supplierCaseRate.setId(id)
        supplierCaseRate.setRetailerId(retailerId)
        supplierCaseRate.setSupplierId(supplierId)
        supplierCaseRate.setCaseRate(caseRate)
        supplierCaseRate.setCaseRateEffectiveDate(caseRateEffectiveDate)

        return supplierCaseRate
    }
}
