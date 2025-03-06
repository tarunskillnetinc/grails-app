package uk.co.wonderlane.wlpos

import grails.gorm.transactions.Transactional
import uk.co.wonderlane.wlpos.dataaccess.DatabaseCredentials
import uk.co.wonderlane.wlpos.dataaccess.MySqlDal
import uk.co.wonderlane.wlpos.enums.CharityGroupType

@Transactional
class CharityService extends MySqlDal {

    def springSecurityService
    def sessionFactory

    CharityService(DatabaseCredentials databaseCredentials) {
        super(databaseCredentials)
    }

    def getCharities() {
        def charities = CharityGroup.findAllByRetailerId(springSecurityService.principal.retailerId, false, [sort: "organisationName", order: "asc"])
        return charities
    }

    //This method will load charities  based on provided arguments
    def getCharities(String organisationTypeTerm, String charityMemberNumberTerm, String charityGroupDescriptionTerm,String includeDeletedCharitiesTerm, int offset, int max, String sortColumn, String sortOrder) {
        //Load charities by db
        def result = CharityGroup.createCriteria().list([offset: offset, max: max, sort: sortColumn, order: sortOrder]) {
            eq("retailerId", springSecurityService.principal.retailerId)

            and {
                if (organisationTypeTerm && organisationTypeTerm.trim()) {
                    CharityGroupType charityGroupType = CharityGroupType.valueOf(organisationTypeTerm);
                    eq("type", charityGroupType)
                }
                if (charityMemberNumberTerm && charityMemberNumberTerm.trim()) {
                    like("memberNumber", "%$charityMemberNumberTerm%")
                }
                if (charityGroupDescriptionTerm && charityGroupDescriptionTerm.trim()) {
                    like("organisationName", "%$charityGroupDescriptionTerm%")
                }
            }

            if (includeDeletedCharitiesTerm != "true") {
                eq("active", true)
            }
        }
        def results = [:]
        results.charities = result //Add to charity
        results.totalCount = result?.totalCount >= 0 ? result.totalCount : 0 //Add to total count
        return results
    }

    def getTypeOptions() {
        [
                [key: 'CHARITY', value: 'Charity'],
                [key: 'GROUP', value: 'Group']
        ]
    }

    void saveCharity(CharityGroup charityGroup) {
        charityGroup.save()
    }

    def getCharity(int id) {
        return CharityGroup.findByIdAndRetailerId(id, springSecurityService.principal.retailerId)
    }

}
