package uk.co.wonderlane.wlpos

import grails.gorm.transactions.Transactional
import org.joda.time.DateTime
import org.joda.time.DateTimeZone

@Transactional
class PromotionService {

    def springSecurityService

    def savePromotion(Promotion promotion) {
        promotion.save()
    }

    def getPromotion(int promotionId) {
        def promotionCriteria = Promotion.createCriteria()

        return promotionCriteria.get() {
            eq ("id", promotionId)
            eq ("retailerId", springSecurityService.principal.retailerId)
        }
    }

    def getPromotionsForProduct(int productId) {
        Product product = Product.findByIdAndRetailerId(productId, springSecurityService.principal.retailerId)

        if (!product) {
            return null
        }

        def allSkus = product.variants?.collect { it.sku }
        def allTags = product.tags?.collect { it.id }

        def promotionCriteria = Promotion.createCriteria()

        def promotions = promotionCriteria.list([sort: "description", order: "ASC"]) {
            eq ("retailerId", springSecurityService.principal.retailerId)
            eq ("active", true)
            lte ("startDate", DateTime.now(DateTimeZone.UTC).withTimeAtStartOfDay().toDate())
            or {
                eq ("endDate", null)
                gte ("endDate", DateTime.now(DateTimeZone.UTC).withTimeAtStartOfDay().plusDays(1).toDate())
            }
        }

        def relevantPromotions = []

        promotions.each { promotion ->
            promotion.groups.each { promotionGroup ->
                if (promotionGroup.sku && allSkus?.contains(promotionGroup.sku)) {
                    relevantPromotions.add(promotion)
                } else if (promotionGroup.categoryId && promotionGroup.categoryId == product.category.id) {
                    relevantPromotions.add(promotion)
                } else if (promotionGroup.tagId && allTags?.contains(promotionGroup.tagId)) {
                    relevantPromotions.add(promotion)
                }
            }
        }

        return relevantPromotions.unique()
    }
}