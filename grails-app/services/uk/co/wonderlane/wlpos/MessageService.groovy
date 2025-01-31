package uk.co.wonderlane.wlpos

import grails.gorm.transactions.Transactional

@Transactional
class MessageService {
    def saveMessage(Message message) {
        message.save(flush: true)
        return message.id
    }
}
