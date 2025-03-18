package uk.co.wonderlane.wlpos

import grails.gorm.transactions.Transactional

@Transactional
class UserService {

    def springSecurityService

    def getUsers(String userNameFilter, Integer homeStoreFilter, boolean showInactiveUsers,  int offset, int max) {
        return User.createCriteria().list([offset: offset, max: max]) {
            eq ("retailerId", springSecurityService.principal.retailerId)
            or {
                like ("username", "%$userNameFilter%")
                like ("name", "%$userNameFilter%")
            }
            if (homeStoreFilter > 0) {
                eq("defaultStoreId", homeStoreFilter)
            }

            if (!showInactiveUsers) {
                eq("active", true)
            }
        }
    }

    def getUser(int id) {
        return User.findByIdAndRetailerId(id, springSecurityService.principal.retailerId)
    }

    def getUserByUsername(String username) {
        return User.findByUsernameAndRetailerId(username, springSecurityService.principal.retailerId)
    }

    def saveUser(User user) {
        user.save()

    }

    def deleteUser(User user){
        user.delete()
    }
}