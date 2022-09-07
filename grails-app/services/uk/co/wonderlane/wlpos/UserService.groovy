package uk.co.wonderlane.wlpos

import grails.gorm.transactions.Transactional

@Transactional
class UserService {

    def springSecurityService

    def getUsers(String searchTerm, int offset, int max) {
        return User.createCriteria().list([offset: offset, max: max]) {
            eq ("retailerId", springSecurityService.principal.retailerId)
            or {
                like ("username", "%$searchTerm%")
                like ("name", "%$searchTerm%")
            }
        }
    }

    def getUser(int id) {
        return User.findByIdAndRetailerId(id, springSecurityService.principal.retailerId)
    }

    def saveUser(User user) {
        user.save()

    }

    def deleteUser(User user){
        user.delete()
    }
}