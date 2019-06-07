package uk.co.wonderlane.wlpos

import uk.co.wonderlane.wlpos.enums.Role

class User {

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

    static mapping = {
        table "user"
        version false

        id column: "id"
        retailerId column: "retailerId"
        username column: "username"
        password column: "password"
        defaultStoreId column: "defaultStoreId"
        name column: "`name`"
        dateOfBirth column: "dateOfBirth"
        active column: "`active`"
        ageRelatedSaleAllowed column: "ageRelatedSaleAllowed"
        securityKey column: "securityKey"
        role column: "`role`", sqlType: "enum", enumType: "string"
        retailerUserId column: "retailerUserId"
    }

    static constraints = {
        username nullable: false, blank: false, maxSize: 40
        password nullable: false, blank: false, maxSize: 60
        defaultStoreId nullable: false
        name nullable: true, maxSize: 50
        dateOfBirth nullable: true
        active nullable: false
        ageRelatedSaleAllowed nullable: true
        securityKey nullable: true, maxSize: 50
        role nullable: false
        retailerUserId nullable: true, maxSize: 30
    }
}