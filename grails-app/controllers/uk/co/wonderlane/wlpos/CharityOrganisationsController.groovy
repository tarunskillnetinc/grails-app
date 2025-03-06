package uk.co.wonderlane.wlpos


import groovy.json.JsonOutput
import org.springframework.security.access.annotation.Secured
import uk.co.wonderlane.wlpos.charity.CharitySortParams

@Secured(['ROLE_ENGINEER'])
class CharityOrganisationsController {
    def springSecurityService
    def charityService

    private static final CHARITY_SORT_COLUMNS = [ "id", "organisationName" , "type", "memberNumber" , "active" ]

    @Secured(['ROLE_ENGINEER'])
    def index() {}

    @Secured(['ROLE_ENGINEER', 'ROLE_HEAD_OFFICE'])
    def ajaxAddCharity() {
        render(template: "addCharity", model: [enableSave: true, isUpdate: false, charity: null, typeOptions: charityService.getTypeOptions()])
    }

    @Secured(['ROLE_ENGINEER', 'ROLE_HEAD_OFFICE'])
    def ajaxEditCharity() {
        int charityId = params.charityId ? Integer.parseInt(params.charityId) : 0

        render(template: "addCharity", model: [enableSave: true, isUpdate: true, charity: charityService.getCharity(charityId), typeOptions: charityService.getTypeOptions()])
    }

    @Secured(['ROLE_ENGINEER', 'ROLE_HEAD_OFFICE'])
    def ajaxSaveCharity() {
        def charity
        if (params.id && Integer.parseInt(params.id) > 0) {
            charity = supplierService.getSupplier(Integer.parseInt(params.id))
        } else {
            charity = new CharityGroup()
            charity.retailerId = springSecurityService.principal.retailerId
        }

        bindData(charity, params)
        if (charity.validate()) {
            charityService.saveSupplier(charity)

            render "OK"
        } else {
            render(template: "addCharity", model: [enableSave: true, isUpdate: charity ? true : false, charity: charity, typeOptions: charityService.getTypeOptions()])
        }
    }

    @Secured(['ROLE_ENGINEER', 'ROLE_HEAD_OFFICE'])
    def ajaxGetSearchCharity(CharitySortParams sortParams) {
        session.CHARITY_TYPE_SEARCH_TERM = params.organisationTypeTerm
        session.CHARITY_MEMBER_NUMBER_SEARCH_TERM = params.charityMemberNumberTerm
        session.CHARITY_DESCRIPTION_SEARCH_TERM = params.charityGroupDescriptionTerm
        session.INCLUDE_DELETED_CHARITIES = params.includeDeletedCharitiesTerm

        sortParams.validateParams(CHARITY_SORT_COLUMNS) //pre process supplier sorting column list

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

}