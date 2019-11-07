package uk.co.wonderlane.wlpos

import grails.gorm.transactions.Transactional

@Transactional
class UserService {

    def springSecurityService

    def getUsers(int offset, int max) {
        return User.createCriteria().list([offset: offset, max: max]) {
            eq ("retailerId", springSecurityService.principal.retailerId)
        }
    }

    def getUser(int id) {
        return User.findByIdAndRetailerId(id, springSecurityService.principal.retailerId)
    }
}