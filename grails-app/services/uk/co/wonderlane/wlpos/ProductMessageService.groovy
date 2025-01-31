package uk.co.wonderlane.wlpos

import grails.gorm.transactions.Transactional

@Transactional
class ProductMessageService {
    def saveProductMessage(ProductMessage productMessage) {
        productMessage.save(flush: true)
        return productMessage.id
    }
}
