package uk.co.wonderlane.wlpos

import uk.co.wonderlane.wlpos.enums.PromotionGroupType

class PromotionGroup {

    static belongsTo = [promotion: Promotion]

    int id
    PromotionGroupType type
    Promotion promotion
    Long sku
    Integer categoryId
    Integer tagId
    Integer requiredQuantity
    BigDecimal requiredValue
    boolean excessQuantity

    static mapping = {
        table "promotiongroup"
        version false

        id column: "id"
        type column: "type", sqlType: "enum", enumType: 'string'
        promotion column: "promotionId"
        sku column: "sku"
        categoryId column: "productCategoryId"
        tagId column: "tagId"
        requiredQuantity column: "requiredQuantity"
        requiredValue column: "requiredValue"
        excessQuantity column: "excessQuantity"
    }

    static constraints = {
        type nullable: false
        sku nullable: true
        categoryId nullable: true
        tagId nullable: true
        requiredQuantity nullable: true, range:1..999999999
        requiredValue nullable:true, range:1F..9999.99F
    }

    public uk.co.wonderlane.wlpos.entities.PromotionGroup getPromotionGroup() {
        uk.co.wonderlane.wlpos.entities.PromotionGroup promotionGroup = new uk.co.wonderlane.wlpos.entities.PromotionGroup()

        promotionGroup.setId(id)
        promotionGroup.setType(type)
        promotionGroup.setPromotionId(promotion.id)
        promotionGroup.setSku(sku)
        promotionGroup.setProductCategoryId(categoryId)
        promotionGroup.setTagId(tagId)
        promotionGroup.setRequiredQuantity(requiredQuantity)
        promotionGroup.setRequiredValue(requiredValue)
        promotionGroup.setExcessQuantity(excessQuantity)

        return promotionGroup
    }
}