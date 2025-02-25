package uk.co.wonderlane.wlpos

import grails.gorm.transactions.Transactional

@Transactional
class ProductMessageService {
    def saveProductMessage(ProductMessage productMessage) {
        productMessage.save(flush: true)
        return productMessage.id
    }

    def deleteProductMessage(Product product, Message message) {
        def productMessage = ProductMessage.findByProductAndMessage(product, message)
        productMessage.delete()
    }
}
