package uk.co.wonderlane.wlpos

import grails.databinding.BindingFormat
import uk.co.wonderlane.wlpos.entities.SyncMessage
import uk.co.wonderlane.wlpos.enums.Role
import uk.co.wonderlane.wlpos.enums.SyncMessageType

class UserController {

    def springSecurityService

    def userService
    def rabbitService
    def gsonProvider

    def index() {
        [users: userService.getUsers("", 0, 50), searchTerm: ""]
    }

    def ajaxGetUsers(String searchTerm, int offset, int max) {
        render (template: "userSearchResults", model: [users: userService.getUsers(searchTerm, offset, max), searchTerm: searchTerm])
    }

    def add() {
        [roleValues: Role.values()]
    }

    def show() {
        if (!params.id || !params.id.isNumber() || params.id.length() > 8) {
            params.id = "-1"
        }

        User user = userService.getUser(Integer.parseInt(params.id))

        if (!user) {
            user = null

            flash.error = "User not found."
        }

        [user: user]
    }

    def save(SaveUserCommand saveUserCommand) {
        saveUserCommand.retailerId = springSecurityService.principal.retailerId
        saveUserCommand.defaultStoreId = 0

        if (saveUserCommand.validate()) {
            User user = saveUserCommand.id ? User.get(saveUserCommand.id) : new User()
            user.properties = saveUserCommand.properties
            userService.saveUser(user)

            flash.message = "User saved successfully"

            List<uk.co.wonderlane.wlpos.entities.User> users = new ArrayList<uk.co.wonderlane.wlpos.entities.User>()
            users.add(user.getUser())

            SyncMessage syncMessage = new SyncMessage(SyncMessageType.USER, springSecurityService.principal.retailerId, springSecurityService.principal.storeNumber, springSecurityService.principal.storeId, 0)
            syncMessage.setInsert(true)
            syncMessage.setUsers(users)

            rabbitService.sendMessage(syncMessage)

            redirect (action: "index")
        } else {
            render (view: "add", model: [user: saveUserCommand, roleValues: Role.values()])
        }
    }
}

class SaveUserCommand {

    int id
    int retailerId
    String username
    String password
    String confirmPassword
    int defaultStoreId
    String name
    @BindingFormat('dd/MM/yyyy')
    Date dateOfBirth
    boolean active
    boolean ageRelatedSaleAllowed
    String securityKey
    Role role
    String retailerUserId

    static constraints = {
        importFrom User

        confirmPassword blank: false, nullable: false, validator: { val, obj ->
            return (val == obj.password) ? true : ["error.User.passwordsDoNotMatch"]
        }
        dateOfBirth validator: { val, obj ->
            return (val >= new Date()) ? ["error.User.dateOfBirthInFuture"] : true
        }
    }
}