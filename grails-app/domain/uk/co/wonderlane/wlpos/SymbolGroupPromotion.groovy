package uk.co.wonderlane.wlpos

import uk.co.wonderlane.wlpos.supplier.SymbolGroup

class SymbolGroupPromotion {

    static belongsTo = [promotion: Promotion]

    int id
    SymbolGroup symbolGroup
    boolean isLeaflet

    static mapping = {
        table "symbolgrouppromotion"
        version false

        id column: "id"
        symbolGroup column: "symbolGroupId"
        promotion column: "promotionId"
        isLeaflet column: "isLeaflet"
    }

    static constraints = {
        id nullable: false
        symbolGroupId nullable: false
        isLeaflet nullable: false
    }

    public uk.co.wonderlane.wlpos.entities.SymbolGroupPromotion getSymbolGroupPromotion() {
        uk.co.wonderlane.wlpos.entities.SymbolGroupPromotion symbolGroupPromotion = new uk.co.wonderlane.wlpos.entities.SymbolGroupPromotion()

        symbolGroupPromotion.setId(id)
        symbolGroupPromotion.setSymbolGroupId(symbolGroupId)
        symbolGroupPromotion.setLeafletPromotion(isLeaflet)

        return symbolGroupPromotion
    }
}