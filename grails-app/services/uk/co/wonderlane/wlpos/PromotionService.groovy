package uk.co.wonderlane.wlpos

import grails.gorm.transactions.Transactional

@Transactional
class PromotionService {

    def savePromotion(Promotion promotion) {
        promotion.save()
    }
}