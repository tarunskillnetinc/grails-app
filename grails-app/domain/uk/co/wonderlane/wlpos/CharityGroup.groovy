package uk.co.wonderlane.wlpos

class CharityGroup {
    Integer id
    Integer retailerId
    String organisationName
    String memberNumber
    String type
    Boolean active = true
    Boolean isDefault = false
    Boolean specialAppeals = false

    static mapping = {
        table 'charitygroup'
        id column: 'id', generator: 'native'
        retailerId column: 'retailerId'
        organisationName column: 'organisationName'
        memberNumber column: 'memberNumber'
        type column: 'type'
        active column: 'active'
        isDefault column: '`default`'
        specialAppeals column: 'specialAppeals'
    }

    static constraints = {
        retailerId nullable: true
        organisationName nullable: false, maxSize: 60
        memberNumber nullable: false, maxSize: 60
        type nullable: false, maxSize: 10, inList: ['CHARITY', 'GROUP']
        active nullable: false
        isDefault nullable: false
        specialAppeals nullable: false
    }

    static belongsTo = [retailer: Retailer]
}
