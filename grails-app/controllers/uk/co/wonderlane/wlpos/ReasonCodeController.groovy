package uk.co.wonderlane.wlpos

import grails.util.Pair
import org.springframework.context.MessageSource
import org.springframework.security.access.annotation.Secured
import org.springframework.web.servlet.support.RequestContextUtils as RCU
import uk.co.wonderlane.wlpos.enums.ReasonCodeType

import static groovy.json.JsonOutput.toJson

class ReasonCodeController {

    def springSecurityService
    def rabbitService
    ReasonCodeService reasonCodeService
    MessageSource messageSource

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
                reasonCode: null,
                errors: toJson([]),
                renderErrors: false,
        ])
    }

    @Secured(['ROLE_ENGINEER', 'ROLE_HEAD_OFFICE'])
    def ajaxEditReasonCode() {
        render(template: "addEditReasonCode", model: [
                retailerId: springSecurityService.principal.retailerId,
                editing: true,
                reasonCode: ReasonCode.get(params.id.toString().toInteger()),
                errors: toJson([]),
                renderErrors: false,
        ])
    }

    @Secured(['ROLE_ENGINEER', 'ROLE_HEAD_OFFICE'])
    def ajaxSaveReasonCode() {
        ReasonCode rc = null
        boolean newEntry = true
        String originalDesc = ""
        Locale locale = RCU.getLocale(request)
        ArrayList<String> errors = new ArrayList<>()

        if (!paramIsNullOrEmpty(params, "id", ["", "0"])) {
            rc = ReasonCode.get(params.id.toString().toInteger())
            newEntry = false
            originalDesc = rc.getDescription()
        }
        rc = rc ?: new ReasonCode()
        customBindParams(rc, params)

        if (rc.description == null || rc.description == "") {
            errors.add(messageSource.getMessage('reasonCode.description.nullable.error', null, locale))
        } else if (newEntry || originalDesc != rc.description) {
            // new reason code or the description has been changed on an existing one
            if (rc.description.size() >= 100) {
                errors.add(messageSource.getMessage('reasonCode.description.maxSize.exceeded', null, locale))
            }
            if (reasonCodeService.isDescriptionDuplicate(springSecurityService.principal.retailerId, rc.description)) {
                errors.add(messageSource.getMessage('reasonCode.description.duplicate.error', null, locale))
            }
        }

        if (errors.size() > 0) {
            render(template: "addEditReasonCode", model: [
                    retailerId: springSecurityService.principal.retailerId,
                    editing: !newEntry,
                    reasonCode: rc,
                    errors: toJson(errors),
                    renderErrors: true,
            ])
            return
        }
        reasonCodeService.saveReasonCode(rc)
        render "OK"
    }

    @Secured(['ROLE_ENGINEER', 'ROLE_HEAD_OFFICE'])
    def ajaxDeleteReasonCode() {
        ReasonCode rc
        if (!paramIsNullOrEmpty(params, "id", ["", "0"])) {
            rc = ReasonCode.get(params.id.toString().toInteger())
        }
        if (rc == null) {
            render "Error occurred trying to delete reason code."
            return
        }

        if (reasonCodeService.isLastOfType(springSecurityService.principal.retailerId, rc.type)) {
            render "Cannot delete, there must be at least one reason code per type."
            return
        }

        rc.deleted = true
        reasonCodeService.saveReasonCode(rc)
        render "OK"
    }

    def sendSyncMessage() {

    }

    def customBindParams(rc, params) {
        rc.type = !paramIsNullOrEmpty(params, "type", [""]) ? ReasonCodeType.valueOf(params.type) : ReasonCodeType.PAID_OUT
        rc.code = params.code
        rc.description = params.description
        rc.retailerId = params.description != null ? params.retailerId.toString().toInteger() : null
        rc.secret = params.secret
        rc.deleted = params.deleted != null ? params.deleted == "true" : false
        rc.preferredReasonCode = params.preferredReasonCode != null ? params.preferredReasonCode == "true" : false
        rc.additionalFunctionality = params.additionalFunctionality != null ? params.additionalFunctionality == "on" : false
        rc.promptForText = params.promptForText != null ? params.promptForText == "on" : false
    }

    def paramIsNullOrEmpty(params, key, empties) {
        return params.get(key) == null || empties.contains(params.get(key).toString())
    }
}
