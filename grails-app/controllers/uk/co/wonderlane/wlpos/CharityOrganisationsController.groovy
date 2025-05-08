package uk.co.wonderlane.wlpos

import org.springframework.security.access.annotation.Secured
import uk.co.wonderlane.wlpos.charity.CharitySortParams
import uk.co.wonderlane.wlpos.entities.SyncMessage
import uk.co.wonderlane.wlpos.enums.SyncMessageType
import uk.co.wonderlane.wlpos.enums.CharityGroupType

@Secured(['ROLE_ENGINEER', 'ROLE_HEAD_OFFICE'])
class CharityOrganisationsController {

    def springSecurityService
    def charityService
    def rabbitService

    private static final CHARITY_SORT_COLUMNS = [ "id", "organisationName" , "type", "memberNumber" , "active" ]

    @Secured(['ROLE_ENGINEER', 'ROLE_HEAD_OFFICE'])
    def index() {
        if (!springSecurityService.principal.retailer.config.charityEnabled) {
            flash.error = "Charity organisations are not configured for your retailer."
            redirect(uri: "/")
            return
        }

        if (springSecurityService.principal.storeId) {
            flash.error = "You cannot access this page when logged in as a store."
            redirect(uri: "/")
            return
        }

        [typeOptions: CharityGroupType.values()]
    }

    @Secured(['ROLE_ENGINEER', 'ROLE_HEAD_OFFICE'])
    def ajaxAddCharity() {
        render(template: "addCharity", model: [enableSave: true, isUpdate: false, charity: null, typeOptions: CharityGroupType.values()])
    }

    @Secured(['ROLE_ENGINEER', 'ROLE_HEAD_OFFICE'])
    def ajaxEditCharity() {
        int charityId = params.charityId ? Integer.parseInt(params.charityId) : 0

        render(template: "addCharity", model: [enableSave: true, isUpdate: true, charity: charityService.getCharity(charityId), typeOptions: CharityGroupType.values()])
    }

    @Secured(['ROLE_ENGINEER', 'ROLE_HEAD_OFFICE'])
    def ajaxSaveCharity() {
        def charity
        def isUpdate
        if (params.id && Integer.parseInt(params.id) > 0) {
            charity = charityService.getCharity(Integer.parseInt(params.id))
            isUpdate = true
        } else {
            charity = new CharityGroup()
            charity.retailerId = springSecurityService.principal.retailerId
            isUpdate = false
        }

        bindData(charity, params)
        if (charity.validate()) {
            if (charity.specialAppeals) {
                CharityGroup.findAllByRetailerIdAndIdNotEqualAndSpecialAppeals(springSecurityService.principal.retailerId, charity.id, true).each { charityUpdate ->
                    charityUpdate.specialAppeals = false

                    saveCharityAndSendToTills(charityUpdate)
                }
            }
            if (charity.isDefault) {
                CharityGroup.findAllByRetailerIdAndIdNotEqualAndIsDefault(springSecurityService.principal.retailerId, charity.id, true).each { charityUpdate ->
                    charityUpdate.isDefault = false

                    saveCharityAndSendToTills(charityUpdate)
                }
            }

            saveCharityAndSendToTills(charity)

            render "OK"
        } else {
            render(template: "addCharity", model: [enableSave: true, isUpdate: isUpdate, charity: charity, typeOptions: CharityGroupType.values()])
        }
    }

    private void saveCharityAndSendToTills(CharityGroup charity) {
        charityService.saveCharity(charity)
        sendSyncMessage(charity, !charity.active)
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

    def getCharity(int charityId) {
        CharityGroup.findByRetailerIdAndId(springSecurityService.principal.retailerId, charityId)
    }

    @Secured(['ROLE_ENGINEER', 'ROLE_HEAD_OFFICE'])
    def ajaxGetSearchCharity(CharitySortParams sortParams) {
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
            saveCharityAndSendToTills(charity)
            render "OK"
        }
    }

}