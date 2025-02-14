package uk.co.wonderlane.wlpos

class PricingClassification {

    int id
    int retailerId
    String classification
    Boolean active
    
    static mapping = {
        table "pricingclassification"
        version false

        retailerId column: "retailerId", sqlType: "tinyint"
        classification column: "classification"
        active column: "active"
    }
    
    static constraints = {
        retailerId nullable: false
        classification nullable: true
        active nullable: false
    }

    public uk.co.wonderlane.wlpos.entities.PricingClassification getPricingClassification() {
        uk.co.wonderlane.wlpos.entities.PricingClassification pricingClassification = new uk.co.wonderlane.wlpos.entities.PricingClassification()

        pricingClassification.setId(id)
        pricingClassification.setRetailerId(retailerId)
        pricingClassification.setClassification(classification)
        pricingClassification.setIsActive(active)
        
        return pricingClassification
    }
}