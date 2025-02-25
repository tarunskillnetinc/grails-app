package uk.co.wonderlane.wlpos

import grails.gorm.transactions.Transactional

@Transactional
class PricingClassificationService {

    def springSecurityService
    
    def getPricingClassificationsForRetailer() {
        PricingClassification.findAllByRetailerId(springSecurityService.principal.retailerId)
    }
    
    def getPricingClassificationById(Integer id) {
        PricingClassification.findById(id)
    }
}
