package uk.co.wonderlane.wlpos

import grails.databinding.BindingFormat
import uk.co.wonderlane.wlpos.enums.Role

class UserController {

    def springSecurityService
    def userService

    def index() {
        [users: userService.getUsers(0, 50)]
    }

    def ajaxGetUsers(int offset, int max) {
        render (template: "userSearchResults", model: [users: userService.getUsers(offset, max)])
    }

    def add() {
        [roleValues: Role.values()]
    }

    def view() {
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
            user.save(flush: true)

            flash.message = "User saved successfully"

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
            return val == obj.password ? true : ["error.User.passwordsDoNotMatch"]
        }
    }
}