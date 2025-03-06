package uk.co.wonderlane.wlpos

import uk.co.wonderlane.wlpos.enums.CharityGroupType

class CharityGroup {
    Integer id
    Integer retailerId
    String organisationName
    String memberNumber
    CharityGroupType type
    Boolean active = true
    Boolean isDefault = false
    Boolean specialAppeals = false

    static mapping = {
        table 'charitygroup'
        version false
        id column: 'id'
        retailerId column: 'retailerId', sqlType: "tinyint"
        organisationName column: 'organisationName'
        memberNumber column: 'memberNumber'
        type column: 'type', sqlType: "enum", enumType: "string"
        active column: 'active'
        isDefault column: '`default`'
        specialAppeals column: 'specialAppeals'
    }

    static constraints = {
        retailerId nullable: false
        organisationName nullable: false, maxSize: 60
        memberNumber nullable: false, maxSize: 60
        type nullable: false, maxSize: 10
        active nullable: false
        isDefault nullable: false
        specialAppeals nullable: false
    }

    public uk.co.wonderlane.wlpos.entities.CharityGroup getCharityGroup() {
        uk.co.wonderlane.wlpos.entities.CharityGroup charityGroup = new uk.co.wonderlane.wlpos.entities.CharityGroup();
        charityGroup.setId(id)
        charityGroup.setRetailerId(retailerId)
        charityGroup.setOrganisationName(organisationName)
        charityGroup.setMemberNumber(memberNumber)
        charityGroup.setType(type)
        charityGroup.setActive(active)
        charityGroup.setDefault(isDefault)
        charityGroup.setSpecialAppeals(specialAppeals)

        return charityGroup
    }

}
