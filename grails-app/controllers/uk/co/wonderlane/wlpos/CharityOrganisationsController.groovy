package uk.co.wonderlane.wlpos

import org.springframework.security.access.annotation.Secured
import uk.co.wonderlane.wlpos.charity.CharitySortParams
import uk.co.wonderlane.wlpos.enums.CharityGroupType

@Secured(['ROLE_ENGINEER', 'ROLE_HEAD_OFFICE'])
class CharityOrganisationsController {
    def springSecurityService
    def charityService

    private static final CHARITY_SORT_COLUMNS = [ "id", "organisationName" , "type", "memberNumber" , "active" ]

    @Secured(['ROLE_ENGINEER', 'ROLE_HEAD_OFFICE'])
    def index() {
        [typeOptions: CharityGroupType.values()]
    }

    @Secured(['ROLE_ENGINEER', 'ROLE_HEAD_OFFICE'])
    def ajaxAddCharity() {
        render(template: "addCharity", model: [enableSave : true])
    }

    @Secured(['ROLE_ENGINEER', 'ROLE_HEAD_OFFICE'])
    def getCharity(int charityId) {
        CharityGroup.findByRetailerIdAndId(springSecurityService.principal.retailerId, charityId)
    }

    @Secured(['ROLE_ENGINEER', 'ROLE_HEAD_OFFICE'])
    def ajaxGetSearchCharity(CharitySortParams sortParams) {
        session.CHARITY_TYPE_SEARCH_TERM = params.organisationTypeTerm
        session.CHARITY_MEMBER_NUMBER_SEARCH_TERM = params.charityMemberNumberTerm
        session.CHARITY_DESCRIPTION_SEARCH_TERM = params.charityGroupDescriptionTerm
        session.INCLUDE_DELETED_CHARITIES = params.includeDeletedCharitiesTerm

        sortParams.validateParams(CHARITY_SORT_COLUMNS) //pre process charity sorting column list

        def charities = [] //declare charity list
        def charitiesResponse = charityService.getCharities(params.organisationTypeTerm, params.charityMemberNumberTerm, params.charityGroupDescriptionTerm, params.includeDeletedCharitiesTerm,
                sortParams.offset ? sortParams.offset : 0, sortParams.max ? sortParams.max : 50, sortParams.sortColumn, sortParams.getSortOrder())
        def returnedCharities = charitiesResponse?.charities
        def totalCount = charitiesResponse?.totalCount
        if (returnedCharities != null && returnedCharities.size() > 0){
            charities = returnedCharities
        }
        render(template: "charitySearchResults",
                model: [ charities: charities,
                         searchTerm: params.searchTerm,
                         max: sortParams.max ?: 50,
                         offset: sortParams.offset,
                         sortParams  : sortParams,
                         totalCount : totalCount
                ])
    }

    @Secured(['ROLE_ENGINEER', 'ROLE_HEAD_OFFICE'])
    def ajaxSetCharityEnabledFlag(int charityId, boolean charityEnabledFlag) {
        def charity = charityService.getCharity(charityId) //Load charity
        if (charity != null) {
            charity.active = charityEnabledFlag;
            charityService.saveCharity(charity)
            render "OK"
        }
    }

}