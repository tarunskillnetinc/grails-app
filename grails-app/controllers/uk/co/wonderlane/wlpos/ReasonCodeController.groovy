package uk.co.wonderlane.wlpos

import grails.util.Pair
import org.springframework.security.access.annotation.Secured
import uk.co.wonderlane.wlpos.enums.ReasonCodeType

class ReasonCodeController {

    def springSecurityService
    def rabbitService
    def reasonCodeService

    @Secured(['ROLE_ENGINEER', 'ROLE_HEAD_OFFICE'])
    def index() { }

    @Secured(['ROLE_ENGINEER', 'ROLE_HEAD_OFFICE'])
    def ajaxSearch() {
        int offset = params.offset ? Integer.parseInt(params.offset) : 0
        int max = params.max ? Integer.parseInt(params.max) : 50
        int retailerId = springSecurityService.principal.retailerId
        String typeStr = params.type

        ReasonCodeType type
        try {
            type = ReasonCodeType.valueOf(typeStr)
        } catch (IllegalArgumentException ignored) {
            // default to paid_out if invalid type provided (should be impossible)
            type = ReasonCodeType.PAID_OUT
        }

        Pair<Integer, List<ReasonCode>> searchResults = reasonCodeService.getReasonCodesOfType(retailerId, type, offset, max)
        render(template: "reasonCodeSearchResults", model: [
                reasonCodes: searchResults.getbValue(),
                max: max,
                offset: offset,
                type: type.name(),
                totalResults: searchResults.getaValue()
        ])
    }

    @Secured(['ROLE_ENGINEER', 'ROLE_HEAD_OFFICE'])
    def ajaxAddReasonCode() {
        render(template: "addEditReasonCode", model: [
                retailerId: springSecurityService.principal.retailerId,
                editing: false,
                reasonCode: null
        ])
    }

    @Secured(['ROLE_ENGINEER', 'ROLE_HEAD_OFFICE'])
    def ajaxEditReasonCode() {
        render(template: "addEditReasonCode", model: [
                retailerId: springSecurityService.principal.retailerId,
                editing: true,
                reasonCode: ReasonCode.get(params.id.toString().toInteger())
        ])
    }

    @Secured(['ROLE_ENGINEER', 'ROLE_HEAD_OFFICE'])
    def ajaxSaveReasonCode() {

    }

    def sendSyncMessage() {

    }
}
