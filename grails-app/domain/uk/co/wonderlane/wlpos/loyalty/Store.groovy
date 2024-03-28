package uk.co.wonderlane.wlpos.loyalty

import uk.co.wonderlane.wlpos.enums.StoreStatus

class Store {

    int id
    int retailerId
    int clientStoreId
    String name
    String description
    String address1
    String address2
    String address3
    String address4
    String postcode
    String country
    String contact
    Double latitude
    Double longitude
    StoreStatus status
    Date dateCreated
    Date dateModified

    static hasMany = [transactions: MemberTransaction]

    static mapping = {
        datasources(["loyalty"])

        table '`store`'
        version false

        retailerId column: "retailer_id"
        clientStoreId column: "CLIENT_STORE_ID"
        name column: "NAME", type: "text"
        description column: "DESCRIPTION", type: "text"
        address1 column: "ADDRESS_1", type: "text"
        address2 column: "ADDRESS_2", type: "text"
        address3 column: "ADDRESS_3", type: "text"
        address4 column: "ADDRESS_4", type: "text"
        postcode column: "POSTCODE", type: "text"
        country column: "COUNTRY", type: "text"
        contact column: "CONTACT", type: "text"
        latitude column: "LAT"
        longitude column: "LNG"
        status column: "STATUS", sqlType: "enum", enumType: "string"
        dateCreated column: "DATE_CREATED"
        dateModified column: "DATE_MODIFIED"
    }

    static constraints = {
        name nullable: true
        description nullable: true
        address1 nullable: true
        address2 nullable: true
        address3 nullable: true
        address4 nullable: true
        postcode nullable: true
        country nullable: true
        contact nullable: true
        latitude nullable: true
        longitude nullable: true
        dateModified nullable: true
    }
}