package uk.co.wonderlane.wlpos

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonParseException
import com.google.gson.JsonPrimitive
import com.google.gson.JsonSerializationContext
import com.google.gson.JsonSerializer
import grails.databinding.BindingFormat
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import org.joda.time.format.ISODateTimeFormat
import uk.co.wonderlane.wlpos.entities.SyncMessage
import uk.co.wonderlane.wlpos.enums.Role
import uk.co.wonderlane.wlpos.enums.SyncMessageType

import java.lang.reflect.Type

class UserController {

    def springSecurityService
    def userService

    def index() {
        [users: userService.getUsers("", 0, 50), searchTerm: ""]
    }

    def ajaxGetUsers(String searchTerm, int offset, int max) {
        render (template: "userSearchResults", model: [users: userService.getUsers(searchTerm, offset, max), searchTerm: searchTerm])
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
            userService.saveUser(user)

            flash.message = "User saved successfully"

            // TODO I think this service needs to be made into an injectable dependency if we go ahead with Grails implementation.
            BackOfficeRabbitService rabbitService = new BackOfficeRabbitService(grailsApplication.config.getProperty('rabbitmq.host'), Integer.parseInt(grailsApplication.config.getProperty('rabbitmq.port')), grailsApplication.config.getProperty('rabbitmq.username'), grailsApplication.config.getProperty('rabbitmq.password'))
            rabbitService.init()

            if (!rabbitService.isOpen()) {
                throw new Exception("Rabbit MQ not available")
            }

            List<uk.co.wonderlane.wlpos.entities.User> users = new ArrayList<uk.co.wonderlane.wlpos.entities.User>()
            users.add(user.getUser())

            SyncMessage syncMessage = new SyncMessage(SyncMessageType.USER, springSecurityService.principal.retailerId, springSecurityService.principal.storeId, 0)
            syncMessage.setInsert(true)
            syncMessage.setUsers(users)

            Gson gson = new GsonBuilder()
                    .registerTypeAdapter(DateTime.class, new JsonSerializer<DateTime>() {
                        @Override
                        public JsonElement serialize(DateTime json, Type typeOfSrc, JsonSerializationContext context) {
                            return new JsonPrimitive(ISODateTimeFormat.dateTime().print(json));
                        }
                    })
                    .registerTypeAdapter(DateTime.class, new JsonDeserializer<DateTime>() {
                        @Override
                        public DateTime deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
                            return ISODateTimeFormat.dateTime().parseDateTime(json.getAsString()).withZone(DateTimeZone.UTC);
                        }
                    }).create()

            rabbitService.sendExchangeMessage(String.format("R%d_S%d", syncMessage.getRetailerId(), syncMessage.getStoreId()), gson.toJson(syncMessage))

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