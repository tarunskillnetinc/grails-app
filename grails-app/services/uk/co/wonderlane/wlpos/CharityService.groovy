package uk.co.wonderlane.wlpos

import grails.gorm.transactions.Transactional
import uk.co.wonderlane.wlpos.dataaccess.DatabaseCredentials
import uk.co.wonderlane.wlpos.dataaccess.MySqlDal

@Transactional
class CharityService extends MySqlDal {

    def springSecurityService
    def sessionFactory

    CharityService(DatabaseCredentials databaseCredentials) {
        super(databaseCredentials)
    }

    def getCharities() {
        def charities = CharityGroup.findAllByRetailerId(springSecurityService.principal.retailerId, false, [sort: "name", order: "asc"])
        return charities
    }

    //This method will load charities  based on provided arguments
    def getCharities(String organisationTypeTerm, String charityMemberNumberTerm, String charityGroupDescriptionTerm,String includeDeletedCharitiesTerm, int offset, int max, String sortColumn, String sortOrder) {
        //Load charities by db
        def result = CharityGroup.createCriteria().list([offset: offset, max: max, sort: sortColumn, order: sortOrder]) {
            eq("retailerId", springSecurityService.principal.retailerId)

            and {
                if (organisationTypeTerm && organisationTypeTerm.trim()) {
                    like("type", "%$organisationTypeTerm%")
                }
                if (charityMemberNumberTerm && charityMemberNumberTerm.trim()) {
                    like("memberNumber", "%$charityMemberNumberTerm%")
                }
                if (charityGroupDescriptionTerm && charityGroupDescriptionTerm.trim()) {
                    like("organisationName", "%$charityGroupDescriptionTerm%")
                }
            }
            if (includeDeletedCharitiesTerm == "true") {
                eq("active", false)
            }
        }
        def results = [:]
        results.charities = result //Add to supplier
        results.totalCount = result?.totalCount >= 0 ? result.totalCount : 0 //Add to total count
        return results
    }

}
