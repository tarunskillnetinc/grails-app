package uk.co.wonderlane.wlpos

import grails.util.Pair
import org.springframework.context.MessageSource
import org.springframework.security.access.annotation.Secured
import org.springframework.web.servlet.support.RequestContextUtils as RCU
import uk.co.wonderlane.wlpos.entities.SyncMessage
import uk.co.wonderlane.wlpos.enums.ReasonCodeType
import uk.co.wonderlane.wlpos.enums.SyncMessageType

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

        Pair<Integer, List<ReasonCode>> searchResults = reasonCodeService.getReasonCodesOfType(retailerId, type, offset, max, params.order ?: "ASC")
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
        Locale locale = RCU.getLocale(request)
        ArrayList<String> errors = new ArrayList<>()

        boolean newEntry = true
        boolean updatedDesc = false
        boolean updatedSecret = false

        if (!paramIsNullOrEmpty(params, "id", ["", "0"])) {
            rc = ReasonCode.get(params.id.toString().toInteger())
            newEntry = false
            updatedDesc = rc.getDescription() != params.description
            updatedSecret = rc.getSecret() != params.secret
        }

        rc = rc != null ? rc : new ReasonCode()
        customBindParams(rc, params)
        rc.discard()

        if (rc.description == null || rc.description == "") {
            errors.add(messageSource.getMessage('reasonCode.description.nullable.error', null, locale))
        } else if (newEntry || updatedDesc) {
            // new reason code or the description has been changed on an existing one
            if (rc.description.size() >= 100) {
                errors.add(messageSource.getMessage('reasonCode.description.maxSize.exceeded', null, locale))
            }
            if (reasonCodeService.isDescriptionDuplicate(springSecurityService.principal.retailerId, rc.description)) {
                errors.add(messageSource.getMessage('reasonCode.description.duplicate.error', null, locale))
            }
        }

        if ((newEntry || updatedSecret) && rc.secret != null) {
            if (rc.secret.length() >= 20) {
                errors.add(messageSource.getMessage('reasonCode.secret.maxSize.exceeded', null, locale))
            }
            if (reasonCodeService.isDuplicateSecret(springSecurityService.principal.retailerId, rc.secret)) {
                errors.add(messageSource.getMessage('reasonCode.secret.duplicate.error', null, locale))
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
        sendSyncMessage(rc, false)
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
        sendSyncMessage(rc, true)
        render "OK"
    }

    def sendSyncMessage(ReasonCode rc, boolean deleted) {
        SyncMessage msg = new SyncMessage(
                SyncMessageType.REASON_CODE,
                springSecurityService.principal.retailerId,
                springSecurityService.principal.storeNumber,
                springSecurityService.principal.storeId,
                null
        )
        msg.setDelete(deleted)
        msg.setInsert(!deleted)
        msg.setReasonCode(rc.getReasonCode())
        rabbitService.sendMessage(msg)
    }

    def customBindParams(rc, params) {
        rc.type = !paramIsNullOrEmpty(params, "type", [""]) ? ReasonCodeType.valueOf(params.type) : ReasonCodeType.PAID_OUT
        rc.code = isNullOrEmpty(params.code) ? null : params.code
        rc.description = params.description
        rc.retailerId = params.description != null ? params.retailerId.toString().toInteger() : null
        rc.secret = isNullOrEmpty(params.secret) ? null : params.secret
        rc.deleted = params.deleted != null ? params.deleted == "true" : false
        rc.preferredReasonCode = params.preferredReasonCode != null ? params.preferredReasonCode == "true" : false
        rc.additionalFunctionality = params.additionalFunctionality != null ? params.additionalFunctionality == "on" : false
        rc.promptForText = params.promptForText != null ? params.promptForText == "on" : false
    }

    def isNullOrEmpty(str) {
        return str == null || str.trim().length() == 0
    }

    def paramIsNullOrEmpty(params, key, empties) {
        return params.get(key) == null || empties.contains(params.get(key).toString())
    }
}
