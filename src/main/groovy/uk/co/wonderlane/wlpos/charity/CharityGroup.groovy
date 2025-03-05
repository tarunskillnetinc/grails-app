package uk.co.wonderlane.wlpos.charity

import uk.co.wonderlane.wlpos.supplier.Supplier

class CharityGroup {
    int id
    Integer retailerId
    String organisationName
    String memberNumber
    String type//CharityGroupType type
    boolean active
    boolean defaultCharityGroup
    boolean specialAppeals

    static mapping = {
        table "charitygroup"
        version false

        id column: "id", sqlType: "int"
        retailerId column: "retailerId", sqlType: "tinyint"
        organisationName column: "organisationName"
        memberNumber column: "memberNumber"
        type column: "type"
        active column: "active"
        defaultCharityGroup column: "default"
        specialAppeals column: "specialAppeals"
    }

    static constraints = {
        retailerId nullable: true

        id nullable: false
        retailerId nullable: false
        organisationName nullable: false
        memberNumber nullable: false
        type nullable: false
        active nullable: false
        defaultCharityGroup nullable: false
        specialAppeals nullable: false
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
