package uk.co.wonderlane.wlpos

import org.joda.time.DateTime
import uk.co.wonderlane.wlpos.enums.PromotionGroupType
import uk.co.wonderlane.wlpos.enums.PromotionType

class Promotion {

    int id
    int retailerId
    String description
    String receiptDescription
    DateTime startDate
    DateTime endDate
    PromotionType type
    BigDecimal amount
    Integer lossCategoryId
    boolean active
    DateTime updateDatetime
    Integer retailerPromotionId
    Collection<PromotionGroup> groups = new ArrayList<>()
    String rpidAsString

    static hasMany = [groups: PromotionGroup]

    static hasOne = [symbolGroupPromotion : SymbolGroupPromotion]

    static mapping = {
        table "promotion"
        version false

        id column: "id"
        retailerId column: "retailerId", sqlType: "tinyint"
        description column: "description"
        receiptDescription column: "receiptDescription"
        startDate column: "startDate", sqlType: "datetime"
        endDate column: "endDate", sqlType: "datetime"
        type column: "type", sqlType: "enum", enumType: 'string'
        amount column: "amount"
        lossCategoryId column: "lossCategoryId"
        active column: "active"
        updateDatetime column: "updateDatetime", sqlType: "datetime"
        retailerPromotionId column: "retailerPromotionId"
        rpidAsString formula: "cast(retailerPromotionId as CHAR(50))"
    }

    static constraints = {
        retailerId nullable: false
        description nullable: false, size: 1..200
        receiptDescription nullable: false, size: 1..50
        startDate nullable: false
        endDate nullable: true
        type nullable: false
        amount nullable: false, range: 0F..9999.99F, validator: {val, obj ->
            if (obj.type == PromotionType.FIXED_PRICE && val <= BigDecimal.ZERO) {
                return 'error.Promotion.fixedAmountNotSet'
            }
        }
        lossCategoryId nullable: true
        active nullable: false
        updateDatetime nullable: false
        retailerPromotionId nullable: true, range: 0..999999999
        symbolGroupPromotion nullable: true
    }

    public uk.co.wonderlane.wlpos.entities.Promotion getPromotion() {
        uk.co.wonderlane.wlpos.entities.Promotion promotion = new uk.co.wonderlane.wlpos.entities.Promotion()

        promotion.setId(id)
        promotion.setRetailerId(retailerId)
        promotion.setDescription(description)
        promotion.setReceiptDescription(receiptDescription)
        promotion.setStartDate(new DateTime(startDate))
        promotion.setEndDate(new DateTime(endDate))
        promotion.setType(type)
        promotion.setAmount(amount)
        promotion.setLossCategoryId(lossCategoryId)
        promotion.setActive(active)
        promotion.setUpdateDatetime(new DateTime(updateDatetime))
        promotion.setRetailerPromotionId(retailerPromotionId)
        groups.each {
            if (it.type == PromotionGroupType.REQUIRED) {
                promotion.getPromotionRequiredGroups().add(it.getPromotionGroup())
            } else {
                promotion.getPromotionOfferGroups().add(it.getPromotionGroup())
            }
        }

        return promotion
    }
}
