package uk.co.wonderlane.wlpos

class Retailer implements Serializable {

    int id
    boolean snappyShopperEnabled
    boolean twoStageSel
    boolean scoEnabled

    static mapping = {
        table "retailers"
        version false

        id column: "retailerId", sqlType: "tinyint"
        snappyShopperEnabled column: "snappyShopperEnabled"
        twoStageSel column: "twoStageSel"
        scoEnabled column: "scoEnabled"
    }

    static constraints = {
        id nullable: false
        snappyShopperEnabled nullable: false
        twoStageSel nullable: false
        scoEnabled nullable: false
    }
}