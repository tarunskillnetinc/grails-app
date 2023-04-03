package uk.co.wonderlane.wlpos

import grails.databinding.BindingFormat
import org.apache.commons.lang.StringUtils
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

    def userEdit() {
        if (!params.id || !params.id.isNumber() || params.id.length() > 8) {
            params.id = "-1"
        }

        User user = userService.getUser(Integer.parseInt(params.id))

        if (!user) {
            user = null
            flash.error = "User not found."
        }

        [user: user, isUserReadOnly: isUserReadOnly(user), roleValues: getEligibleUserRoles(user?.getRole())]
    }

    def changePassword() {
        if (!params.id || !params.id.isNumber() || params.id.length() > 8) {
            params.id = "-1"
        }

        User user = userService.getUser(Integer.parseInt(params.id))

        if (!user) {
            user = null
            flash.error = "User not found."
            [user: user, isUserReadOnly: isUserReadOnly(user), roleValues: getEligibleUserRoles(user?.getRole())]
        } else {
            render(view: "changePassword", model: [userId: params.id, name : params.name,  isUserReadOnly: isUserReadOnly(user)])
        }

    }

    def save(SaveUserCommand saveUserCommand) {
        saveUserCommand.retailerId = springSecurityService.principal.retailerId
        saveUserCommand.defaultStoreId = 0

        if (saveUserCommand.validate()) {
            User user = saveUserCommand.id ? User.get(saveUserCommand.id) : new User()
            user.properties = saveUserCommand.properties
            userService.saveUser(user)

            flash.message = "User saved successfully"

            pushUserUpdatesToMq(user, true)

            redirect (action: "index")
        } else {
            render (view: "add", model: [user: saveUserCommand, roleValues: Role.values()])
        }
    }

    //This method is responsible for edit selected user
    def editSelectedUser(SaveUserCommand saveUserCommand) {
        boolean isValidToEdit = true

        if (saveUserCommand != null && saveUserCommand.getId() != null && Integer.parseInt(saveUserCommand.getId().toString()) > 0){

            //Load user --> Before this method invoke verify user exists, Therefore chances of user not exists is very less
            User user = User.get(saveUserCommand.getId())

            if (!user){
                flash.error = "User not found. Failed to delete"
                isValidToEdit = false
            } else if (!isValidUserToUpdate(saveUserCommand?.getRole())){ //Check logged in user has permission to update user role
                String errorMessage = "Logged in user has no permission to promote user to " + saveUserCommand?.getRole()?.toString()
                flash.error = errorMessage
                isValidToEdit = false
            } else if (isLoggedInAsStoreUser()){ //Check logged in user logged as store user or HO user
                flash.error = "Store user does not have permission for edit user"
                isValidToEdit = false
            }

            if (isValidToEdit){

                //Set pre existing values to save user object
                saveUserCommand.retailerId = user.retailerId
                saveUserCommand.defaultStoreId = user.getDefaultStoreId()
                saveUserCommand.password = user.getPassword()
                saveUserCommand.confirmPassword = user.getPassword()

                //Check for user parameters validations
                if (saveUserCommand.validate()) {

                    //Update user values to pass parameters
                    user.properties = saveUserCommand.properties

                    //Save user object
                    userService.saveUser(user)

                    pushUserUpdatesToMq(user, true)

                    flash.message = "User saved successfully"

                    redirect (action: "index")
                }else {
                    render (view: "userEdit", model: [user: saveUserCommand, roleValues: getEligibleUserRoles(user?.getRole()), isUserReadOnly: isUserReadOnly(user)])
                }
            } else {
                render (view: "userEdit", model: [user: saveUserCommand, roleValues: getEligibleUserRoles(user?.getRole()), isUserReadOnly: isUserReadOnly(user)])
            }

        } else {
            redirect (action: "index")
        }
    }

    //This method is responsible for delete selected user
    def deleteUser() {
        boolean isValidToDelete = true

        //Load user --> Before this method invoke verify user exists, Therefore chances of user not exists is very less
        User user = User.get(params.id)

        if (!user) { //If user does not exist on DB
            flash.error = "User not found. Failed to delete"
            isValidToDelete = false
        } else if (!isValidUserToUpdate(user?.getRole())) { //Check if action perform user is eligible for delete user
            String errorMessage = "Logged in user has no permission to delete " + user.getRole().toString()
            flash.error = errorMessage
            isValidToDelete = false
        } else if (isLoggedInAsStoreUser()){ //Only HO logged in user has permission to edit user
            flash.error = "Store user does not have permission for delete"
            isValidToDelete = false
        }

        if (isValidToDelete) {
            userService.deleteUser(user)
            pushUserUpdatesToMq(user, false)
            flash.message = "User deleted successfully"
            redirect(action: "index")
        } else {
            render(view: "userEdit", model: [user: user, isUserReadOnly: isUserReadOnly(user), roleValues: getEligibleUserRoles(user?.getRole())])
        }
    }

    //This method is responsible for change user password
    def editUserPassword(SaveUserPasswordCommand saveUserPasswordCommand) {
        if (saveUserPasswordCommand != null && saveUserPasswordCommand.getId() != null && Integer.parseInt(saveUserPasswordCommand.getId().toString()) > 0){
            def isValidToChangePassword = true

            //Load user
            User user = User.get(saveUserPasswordCommand.getId())

            if (!user){
                flash.error = "User not found. Failed to delete"
                isValidToChangePassword = false
            } else if (!isValidUserToUpdate(user?.getRole())){ //Check logged in user has permission to update user role
                String errorMessage = "Logged in user has no permission to change password of " + user?.getRole()?.toString()
                flash.error = errorMessage
                isValidToChangePassword = false
            } else if (isLoggedInAsStoreUser()){ //Check logged in user logged as store user or HO user
                flash.error = "Store user does not have permission for change user password"
                isValidToChangePassword = false
            }

            if (saveUserPasswordCommand.validate() && isValidToChangePassword) {
                user.password = saveUserPasswordCommand.getPassword()

                //Save user object
                userService.saveUser(user)

                //Push notification to MQ
                pushUserUpdatesToMq(user, true)

                flash.message = "User password saved successfully"

                redirect(action: "index")
            } else {
                render(view: "changePassword", model: [saveUserPasswordCommand: saveUserPasswordCommand, userId: saveUserPasswordCommand.getId(), name: saveUserPasswordCommand.getName(), isUserReadOnly: isUserReadOnly(user)])
            }

        } else {

            redirect(action: "index")
        }
    }

    //Check logged in user rank and updating user rank
    private boolean isValidUserToUpdate(Role markedUserRole){
        User loggedInUser = User.get(springSecurityService.principal.id)

        //Only allow higher rank users to delete lower rank users
        //ENGINEER > HEAD_OFFICE > STORE_MANAGER > SUPERVISOR > USER
        if (markedUserRole != null && loggedInUser != null && markedUserRole?.getRank() <= loggedInUser?.getRole()?.getRank()){
            return true
        }

        return false
    }

    //Check selected user is eligible to edit or delete
    private boolean isUserReadOnly(User user) {
        boolean isLoggedInFromHO = false

        //Check logged in user logged in HO level
        if (springSecurityService.principal.storeId == null){
            isLoggedInFromHO = true
        }

        //Currently allow user to edit/ delete only if
        // 1. Deleting user should exists
        // 2. Logged in user should logged in HO level
        // 3. Editing user should not be logged in user
        // 4. Should be lower rank user than logged in user
        if (user && user?.getId() != springSecurityService.principal.id && isLoggedInFromHO && isValidUserToUpdate(user?.getRole())) {
            return false
        }
        return true
    }

    //Logged in user not allow to update role above the logged in users role. Therefore only pass eligible user roles to server
    private List getEligibleUserRoles(Role markUserRole){
        User loggedInUser = User.get(springSecurityService.principal.id)

        if (loggedInUser != null && markUserRole != null && markUserRole?.getRank() <= loggedInUser?.getRole()?.getRank()){
            return Role.values().findAll({it -> it.rank <= loggedInUser?.getRole()?.getRank()})
        } else if (markUserRole != null){
            return new ArrayList(Arrays.asList(markUserRole))
        } else {
            return new ArrayList()
        }

    }

    //Method to return logged in type (Store user / HO)
    private boolean isLoggedInAsStoreUser(){
        if (springSecurityService.principal.storeId == null){
            return false
        }

        return true
    }

    //This method is to send updated request to MQ
    private pushUserUpdatesToMq(User user, boolean isInsert) {
        //Process to send user save response to MQ
        List<uk.co.wonderlane.wlpos.entities.User> users = new ArrayList<uk.co.wonderlane.wlpos.entities.User>()
        users.add(user.getUser())

        SyncMessage syncMessage = new SyncMessage(SyncMessageType.USER, springSecurityService.principal.retailerId, springSecurityService.principal.storeNumber, springSecurityService.principal.storeId, 0)
        //This is to check whether it is insert or delete --> If insert is true then it is insert or else delete
        syncMessage.setInsert(isInsert)
        syncMessage.setUsers(users)

        rabbitService.sendMessage(syncMessage)
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

        dateOfBirth blank: false, nullable: false, validator: { val, obj ->
            if (val >= new Date()) {
                return ["error.User.dateOfBirthInFuture"]
            }

             return true
        }

        username blank: false, validator: {val, obj ->
            if (obj.id == 0) {
                def user = User.findByUsername(val)

                return user == null
            }

            return true
        }

        securityKey nullable: true, validator: {val, obj ->
            if (!StringUtils.isEmpty(val)) {
                def user = User.findBySecurityKey(val)
                if (user && obj.id != User.findBySecurityKey(val).getId())
                    return ["error.User.securityKeyAlreadyTaken"]
                else
                    return true
            } else {
                return true
            }
        }
    }
}


class SaveUserPasswordCommand {

    int id
    String password
    String confirmPassword
    String name

    static constraints = {

        confirmPassword blank: false, nullable: false, validator: { val, obj ->
            return (val == obj.password) ? true : ["error.User.passwordsDoNotMatch"]
        }

        password nullable: false, blank: false, password: true, minSize: 5, maxSize: 70
    }
}
