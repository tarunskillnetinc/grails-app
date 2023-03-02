package uk.co.wonderlane.wlpos

import org.joda.time.DateTime
import uk.co.wonderlane.wlpos.enums.Role

class User {

    def springSecurityService

    int id
    int retailerId
    String username
    String password
    int defaultStoreId
    String name
    Date dateOfBirth
    boolean active
    boolean ageRelatedSaleAllowed
    String securityKey
    Role role
    String retailerUserId

    // Constructor required for dependency injection (ie, to make springSecurityService work).
    public User() {

    }

    static mapping = {
        autowire true
        table "user"
        version false

        id column: "id"
        retailerId column: "retailerId", sqlType: "tinyint"
        username column: "username"
        password column: "password"
        defaultStoreId column: "defaultStoreId", sqlType: "smallint"
        name column: "`name`"
        dateOfBirth column: "dateOfBirth"
        active column: "`active`"
        ageRelatedSaleAllowed column: "ageRelatedSaleAllowed"
        securityKey column: "securityKey"
        role column: "`role`", sqlType: "enum", enumType: "string"
        retailerUserId column: "retailerUserId"
    }

    static constraints = {
        username nullable: false, blank: false, minSize: 3, maxSize: 40, unique: true
        password nullable: false, blank: false, password: true, minSize: 5, maxSize: 70
        defaultStoreId nullable: false
        name nullable: false, maxSize: 50
        dateOfBirth nullable: false
        active nullable: false
        ageRelatedSaleAllowed nullable: true
        securityKey nullable: true, minSize: 8, maxSize: 50
        role nullable: false
        retailerUserId nullable: true, maxSize: 30
    }

    public uk.co.wonderlane.wlpos.entities.User getUser() {
        uk.co.wonderlane.wlpos.entities.User user = new uk.co.wonderlane.wlpos.entities.User()

        user.setId(id)
        user.setUsername(username)
        user.setPassword(password)
        user.setDefaultStoreId(defaultStoreId)
        user.setName(name)
        user.setDateOfBirth(new DateTime(dateOfBirth))
        user.setActive(active)
        user.setAgeRelatedSaleAllowed(ageRelatedSaleAllowed)
        user.setSecurityKey(securityKey)
        user.setRole(role)
        user.setRetailerUserId(retailerUserId)

        return user
    }
}