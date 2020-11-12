package uk.co.wonderlane.wlpos

import grails.gorm.transactions.Transactional

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
}