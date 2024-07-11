package uk.co.wonderlane.wlpos

class PromotionStore implements Serializable {

    static belongsTo = [promotion: Promotion]

    Promotion promotion
    int storeId

    static mapping = {
        table 'promotionstore'
        version false

        id composite: ['promotion', 'storeId']

        promotion column: 'promotionId'
        storeId column: 'storeId', sqlType: "smallint"
    }

    static constraints = {
        promotion nullable: false
        storeId nullable: false
    }
}
