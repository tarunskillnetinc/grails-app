package uk.co.wonderlane.wlpos

import uk.co.wonderlane.wlpos.enums.PromotionGroupType

class PromotionGroup {

    static belongsTo = [promotion: Promotion]

    int id
    PromotionGroupType type
    Promotion promotion
    Long sku
    Integer categoryId
    Integer requiredQuantity
    Integer tagId
    boolean applyLoss
    BigDecimal value
    boolean excessQuantity

    static mapping = {
        table "promotiongroup"
        version false

        id column: "id"
        type column: "type", sqlType: "enum", enumType: 'string'
        promotion column: "promotionId"
        sku column: "sku"
        categoryId column: "productCategoryId"
        requiredQuantity column: "requiredQuantity"
        tagId column: "tagId"
        applyLoss column: "applyLoss"
        value column: "value"
        excessQuantity column: "excessQuantity"
    }

    static constraints = {
        type nullable: false
        sku nullable: true
        categoryId nullable: true
        tagId nullable: true
        requiredQuantity nullable: true, range:1..999999999
        value nullable:true, range:1F..9999.99F
    }

    public uk.co.wonderlane.wlpos.entities.PromotionGroup getPromotionGroup() {
        uk.co.wonderlane.wlpos.entities.PromotionGroup promotionGroup = new uk.co.wonderlane.wlpos.entities.PromotionGroup()

        promotionGroup.setId(id)
        promotionGroup.setPromotionGroupType(type)
        promotionGroup.setPromotionId(promotion.id)
        promotionGroup.setSku(sku)
        promotionGroup.setProductCategoryId(categoryId)
        promotionGroup.setTagId(tagId)
        promotionGroup.setRequiredQuantity(requiredQuantity)
        promotionGroup.setApplyLoss(applyLoss)
        promotionGroup.setValue(value)
        promotionGroup.setExcessQuantity(excessQuantity)

        return promotionGroup
    }
}
