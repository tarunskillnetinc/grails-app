package uk.co.wonderlane.wlpos

import org.springframework.security.access.annotation.Secured
import uk.co.wonderlane.wlpos.charity.CharitySortParams
import uk.co.wonderlane.wlpos.entities.SyncMessage
import uk.co.wonderlane.wlpos.enums.SyncMessageType

@Secured(['ROLE_ENGINEER'])
class CharityOrganisationsController {
    def springSecurityService
    def charityService
    def rabbitService

    private static final CHARITY_SORT_COLUMNS = [ "id", "organisationName" , "type", "memberNumber" , "active" ]

    @Secured(['ROLE_ENGINEER'])
    def index() {
        [typeOptions: charityService.getTypeOptions()]
    }

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
            charity = charityService.getCharity(Integer.parseInt(params.id))
        } else {
            charity = new CharityGroup()
            charity.retailerId = springSecurityService.principal.retailerId
        }

        bindData(charity, params)
        if (charity.validate()) {
            charityService.saveSupplier(charity)
            sendSyncMessage(charity, !charity.active)

            render "OK"
        } else {
            render(template: "addCharity", model: [enableSave: true, isUpdate: charity ? true : false, charity: charity, typeOptions: charityService.getTypeOptions()])
        }
    }

    def sendSyncMessage(CharityGroup charityGroup, boolean deleted) {
        SyncMessage msg = new SyncMessage(
                SyncMessageType.CHARITY_GROUP,
                springSecurityService.principal.retailerId,
                springSecurityService.principal.storeNumber,
                springSecurityService.principal.storeId,
                null
        )

        msg.setDelete(deleted)
        msg.setInsert(!deleted)
        msg.setCharityGroup(charityGroup.getCharityGroup())

        rabbitService.sendMessage(msg)
    }

    def getTypeOptions() {
        [
                [key: 'CHARITY', value: 'Charity'],
                [key: 'GROUP', value: 'Group']
        ]
    }

    def getCharity(int charityId) {
        CharityGroup.findByRetailerIdAndId(springSecurityService.principal.retailerId, charityId)
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

    @Secured(['ROLE_ENGINEER', 'ROLE_HEAD_OFFICE'])
    def ajaxToggleCharityDeletedFlag(int charityId) {
        def charity = charityService.getCharity(charityId) //Load charity
        if (charity != null) {
            charity.active = !charity.active;
            charityService.saveCharity(charity)
            render "OK"
        }
    }

}