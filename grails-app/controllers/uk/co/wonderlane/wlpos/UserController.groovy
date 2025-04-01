package uk.co.wonderlane.wlpos

import grails.databinding.BindingFormat
import org.apache.commons.lang.StringUtils
import org.springframework.validation.BindingResult
import uk.co.wonderlane.wlpos.entities.SyncMessage
import uk.co.wonderlane.wlpos.enums.Role
import uk.co.wonderlane.wlpos.enums.SyncMessageType

class UserController {

    def springSecurityService
    def userService
    def rabbitService
    def storeService

    def index() {
        boolean isLoggedInFromStoreLevel = false
        Store defaultStore = null
        if (springSecurityService.principal.storeId != null){
            isLoggedInFromStoreLevel = true
            defaultStore = storeService.getStore(springSecurityService.principal.retailerId, springSecurityService.principal.storeId)
        }
        def stores = getStores()
        List<User> users = userService.getUsers("", defaultStore?.id, false,  0, 50) as List<User>
        [users: users, userNameFilter: "", homeStoreFilter: "", showInactiveUserFilter: false, stores: stores,
         isLoggedInFromStoreLevel: isLoggedInFromStoreLevel, defaultStore:defaultStore, offset: 0, max: 50]
    }

    def ajaxGetUsers() {
        Integer homeStoreFilter = -1
        String userNameFilter = params.userNameFilter
        if (params.homeStoreFilter && params.homeStoreFilter.isNumber()) {
            homeStoreFilter = Integer.parseInt(params.homeStoreFilter)
        }
        Integer offset = (params.offset != null && params.offset != "") ? Integer.parseInt(params.offset) : 0
        Integer max = (params.max != null && params.max != "") ? Integer.parseInt(params.max) : 50
        Boolean showInactiveUsers = params.showInactiveUserFilter != null ? Boolean.valueOf(params.showInactiveUserFilter) : false
        List<User> users = userService.getUsers(userNameFilter, homeStoreFilter, showInactiveUsers, offset, max) as List<User>
        render (template: "userSearchResults", model: [ users: users,
                                                        userNameFilter: userNameFilter,
                                                        homeStoreFilter: homeStoreFilter,
                                                        showInactiveUserFilter: showInactiveUsers,
                                                        offset: offset,
                                                        max: max])
    }

    def add() {
        renderAddUser(null)
    }

    def userEdit() {
        if (!params.id || !params.id.isNumber() || params.id.length() > 8) {
            params.id = "-1"
        }

        User user = userService.getUser(Integer.parseInt(params.id))

        if (!user) {
            flash.error = "User not found."
            redirect (action: "index")
            return
        }

        renderUserEdit(null, user)
    }


    def changePassword() {
        if (!params.id || !params.id.isNumber() || params.id.length() > 8) {
            params.id = "-1"
        }

        User user = userService.getUser(Integer.parseInt(params.id))

        if (!user) {
            flash.error = "User not found."
            redirect (action: "index")
            return
        }

        render(view: "changePassword", model: [userId: params.id, name : params.name,  isUserReadOnly: isUserReadOnly(user)])
    }

    def save(SaveUserCommand saveUserCommand) {
        try {
            saveUserCommand.retailerId = springSecurityService.principal.retailerId
            if (saveUserCommand?.validate()) {
                User user = saveUserCommand.id ? User.get(saveUserCommand.id) : new User()
                user.properties = saveUserCommand.properties
                userService.saveUser(user)
                flash.message = "User saved successfully"
                pushUserUpdatesToMq(user, true)
                redirect (action: "index")
            } else {
                renderAddUser(saveUserCommand)
            }
        } catch (Exception ex) {
            log.error("Error saving user, Exception " + ex)
            flash.error = "Unknown error when adding user"
            renderAddUser(saveUserCommand)
        }
    }

    //This method is responsible for edit selected user
    def editSelectedUser(SaveUserCommand saveUserCommand) {
        User user = null
        try {
            if (saveUserCommand != null && Integer.parseInt(saveUserCommand.getId().toString()) > 0){
                //Load user --> Before this method invoke verify user exists, Therefore chances of user not exists is very less
                user = User.get(saveUserCommand.getId())

                if (!user){
                    flash.error = "User not found. Failed to edit"
                    redirect(action: "index")
                    return
                } else if (!isValidToEdit(saveUserCommand)) {
                    flash.error = "Logged in user has no permission to promote user to ${saveUserCommand?.role}"
                    redirect(action: "index")
                    return
                } else if (!isLoggedInFromValidLocation(user)){ //Only HO logged in user has permission to edit user
                    flash.error = "User does not have permission for edit"
                    redirect(action: "index")
                    return
                }

                saveUserCommand.retailerId = user.retailerId
                saveUserCommand.password = user.password
                saveUserCommand.confirmPassword = user.password

                //Check for user parameters validations
                if (saveUserCommand.validate()) {
                    user.properties = saveUserCommand.properties
                    //Save user object
                    userService.saveUser(user)
                    pushUserUpdatesToMq(user, true)
                    flash.message = "User saved successfully"
                    redirect (action: "index")
                } else {
                    renderUserEdit(saveUserCommand, user)
                }
            } else {
                flash.error = "Edit user request has no id"
                redirect (action: "index")
            }
        } catch (Exception ex) {
            log.error("Error editing selected user ${saveUserCommand?.id}, Exception " + ex)
            flash.error = "Unknown error when editing user"
            renderUserEdit(saveUserCommand, user)
        }
    }

    //This method is responsible for delete selected user
    def deleteUser() {
        User user = null
        try {
            //Load user --> Before this method invoke verify user exists, Therefore chances of user not exists is very less
            user = User.get(params.id)

            if (!user) { //If user does not exist on DB
                flash.error = "User not found. Failed to edit"
                redirect(action: "index")
                return
            }

            if (!isValidUserToUpdate(user?.getRole())) { //Check if action perform user is eligible for delete user
                String errorMessage = "Logged in user has no permission to delete " + user.getRole().toString()
                flash.error = errorMessage
                renderUserEdit(null, user)
                return
            } else if (!isLoggedInFromValidLocation(user)){ //Only HO logged in user has permission to edit user
                flash.error = "Store user does not have permission for delete"
                renderUserEdit(null, user)
                return
            }

            userService.deleteUser(user)
            pushUserUpdatesToMq(user, false)
            flash.message = "User deleted successfully"
            redirect(action: "index")
        } catch (Exception ex) {
            log.error("Error deleting user, Exception " + ex)
            flash.error = "Unknown error when editing user"
            renderUserEdit(null, user)
        }
    }

    //This method is responsible for change user password
    def editUserPassword(SaveUserPasswordCommand saveUserPasswordCommand) {
        User user = null
        try {
            if (saveUserPasswordCommand != null && saveUserPasswordCommand.getId() != null && Integer.parseInt(saveUserPasswordCommand.getId().toString()) > 0){
                def isValidToChangePassword = true

                //Load user
                user = User.get(saveUserPasswordCommand.getId())

                if (!user){
                    flash.error = "User not found. Failed to delete"
                    redirect(action: "index")
                    return
                }

                if (!isValidUserToUpdate(user?.getRole())){ //Check logged in user has permission to update user role
                    String errorMessage = "Logged in user has no permission to change password of " + user?.getRole()?.toString()
                    flash.error = errorMessage
                    isValidToChangePassword = false
                } else if (!isLoggedInFromValidLocation(user)){ //Check logged in user logged as store user or HO user
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
                    render(view: "changePassword", model: [saveUserPasswordCommand: saveUserPasswordCommand,
                                                           userId: saveUserPasswordCommand.getId(),
                                                           name: saveUserPasswordCommand.getName(),
                                                           isUserReadOnly: isUserReadOnly(user)])
                }

            } else {
                flash.error = "Edit user password request has no valid id"
                redirect(action: "index")
            }
        } catch (Exception ex) {
            log.error("Error editing user password for user ${user?.id}, Exception " + ex)
            flash.error = "Unknown error when editing user password"
            redirect(action: "index")
        }

    }


    private boolean isValidToEdit(SaveUserCommand saveUserCommand) {
        if (!isValidUserToUpdate(saveUserCommand?.role)) {
            return false
        }
        return true
    }

    private void renderUserEdit(SaveUserCommand saveUserCommand, User user) {
        boolean isLoggedInFromStoreLevel = false
        Store defaultStore = null
        if (springSecurityService.principal.storeId != null) {
            isLoggedInFromStoreLevel = true
            defaultStore = storeService.getStore(springSecurityService.principal.retailerId, springSecurityService.principal.storeId)
        }
        def stores = getStores()
        render(view: "userEdit", model: [
                user: saveUserCommand ?: user,
                isUserReadOnly: isUserReadOnly(user),
                roleValues: getEligibleUserRoles(user?.role),
                stores: stores,
                isLoggedInFromStoreLevel: isLoggedInFromStoreLevel,
                defaultStore: defaultStore
        ])
    }

    private void renderAddUser(SaveUserCommand saveUserCommand){
        boolean isLoggedInFromStoreLevel = false
        Store defaultStore = null
        if (springSecurityService.principal.storeId != null){
            isLoggedInFromStoreLevel = true
            defaultStore = storeService.getStore(springSecurityService.principal.retailerId, springSecurityService.principal.storeId)
        }
        def stores = getStores()
        render(view: "add", model:  [user: saveUserCommand,
                                     stores: stores,
                                     roleValues: getEligibleUserRoles(),
                                     isLoggedInFromStoreLevel: isLoggedInFromStoreLevel,
                                     defaultStore: defaultStore])

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
        boolean isLoggedInFromValidLocationToEdit = false

        //Check logged in user logged in HO level
        if ((springSecurityService.principal.storeId == null) || (springSecurityService.principal.storeId == user?.defaultStoreId)){
            isLoggedInFromValidLocationToEdit = true
        }

        //Currently allow user to edit/ delete only if
        // 1. Deleting user should exists
        // 2. Logged in user should logged in HO level
        // 3. Editing user should not be logged in user
        // 4. Should be lower rank user than logged in user
        if (user && user?.getId() != springSecurityService.principal.id && isLoggedInFromValidLocationToEdit && isValidUserToUpdate(user?.getRole())) {
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

    private List getEligibleUserRoles() {
        User loggedInUser = User.get(springSecurityService.principal.id)

        if (loggedInUser != null) {
            return Role.values().findAll({it -> it.rank <= loggedInUser?.getRole()?.getRank()})
        } else {
            return new ArrayList()
        }
    }

    //Method to return logged in type (Store user / HO)
    private boolean isLoggedInFromValidLocation(User user){
        //Check logged in user logged in HO level or from same store as default store
        return ((springSecurityService.principal.storeId == null) || (springSecurityService.principal.storeId == user?.defaultStoreId))
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

    private getStores(){
        return storeService.getStore(springSecurityService.principal.retailerId)?.sort { it.config.storeNumber + "-" + it.config.storeName }
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

        password nullable: false, blank: false, password: true, minSize: 4, maxSize: 70
    }
}
