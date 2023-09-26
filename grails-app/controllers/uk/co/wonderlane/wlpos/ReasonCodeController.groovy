package uk.co.wonderlane.wlpos

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

        List<ReasonCode> total = reasonCodeService.getAllReasonCodesOfType(retailerId, type)
        List<ReasonCode> page = total.drop(offset).take(max)
        render(template: "reasonCodeSearchResults", model: [
                reasonCodes: page,
                max: max,
                offset: offset,
                type: type.name(),
                totalResults: total.size()
        ])
    }
}
