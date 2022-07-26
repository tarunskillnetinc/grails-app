package uk.co.wonderlane.wlpos

class Retailer {

    int id
    boolean snappyShopperEnabled
    boolean twoStageSel

    static mapping = {
        table "retailers"
        version false

        id column: "retailerId", sqlType: "tinyint"
        snappyShopperEnabled column: "snappyShopperEnabled"
        twoStageSel column: "twoStageSel"
    }

    static constraints = {
        retailerId nullable: false
        snappyShopperEnabled nullable: false
        twoStageSel nullable: false
    }
}