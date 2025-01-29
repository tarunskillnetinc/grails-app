package uk.co.wonderlane.wlpos

import grails.validation.Validateable
import org.springframework.security.access.annotation.Secured
import uk.co.wonderlane.wlpos.entities.SyncMessage
import uk.co.wonderlane.wlpos.enums.SyncMessageType
import uk.co.wonderlane.wlpos.enums.VoucherType

class TenderTypeController {

    def springSecurityService
    def rabbitService
    def tenderTypeService

    @Secured(['ROLE_ENGINEER', 'ROLE_HEAD_OFFICE'])
    def index() {

    }

    @Secured(['ROLE_ENGINEER', 'ROLE_HEAD_OFFICE'])
    def ajaxSearch() {
        String sortColumn = params.sortColumn ?: "name"
        String sortOrder = params.sortOrder ?: "asc"
        int offset = params.offset ? Integer.parseInt(params.offset) : 0
        int max = params.max ? Integer.parseInt(params.max) : 50
        String tenderTypeFilter = params.tenderTypeFilter
        boolean includeDeleted = params.showDeletedFilter ? Boolean.parseBoolean(params.showDeletedFilter) : false

        def (searchResults, totalCount) = tenderTypeService.getTenderTypes(tenderTypeFilter, includeDeleted, sortColumn, sortOrder, offset, max)

        render(template: "tenderTypeSearchResults", model: [tenderTypes: searchResults, max: max, offset: offset, tenderTypeFilter: tenderTypeFilter, sortColumn: sortColumn, sortOrder: sortOrder, totalResults: totalCount])
    }

    @Secured(['ROLE_ENGINEER', 'ROLE_HEAD_OFFICE'])
    def ajaxAddTenderType() {
        render(template: "addEditTenderType", model: [availableVoucherTypes: VoucherType.values()])
    }

    @Secured(['ROLE_ENGINEER', 'ROLE_HEAD_OFFICE'])
    def ajaxEditTenderType() {
        int id = params.id ? Integer.parseInt(params.id) : 0

        render(template: "addEditTenderType", model: [tenderType: tenderTypeService.getTenderType(id), availableVoucherTypes: VoucherType.values()])
    }

    @Secured(['ROLE_ENGINEER', 'ROLE_HEAD_OFFICE'])
    def ajaxSaveTenderType(TenderTypeCommand tenderTypeCommand) {
        if (!tenderTypeCommand?.validate()) {
            render(status: 400, template: "addEditTenderType", model: [tenderType: tenderTypeCommand, availableVoucherTypes: VoucherType.values()])
            return
        }

        if (tenderTypeCommand?.validate()) {
            TenderType tenderType

            if (tenderTypeCommand.id > 0) {
                tenderType = tenderTypeService.getTenderType(tenderTypeCommand.id)

                if (!tenderType) {
                    render (status:400, text: "Tender type not found.")
                    return
                }
            } else {
                tenderType = new TenderType()
            }

            bindData(tenderType, tenderTypeCommand)

            tenderType.retailerId = springSecurityService.principal.retailerId

            tenderTypeService.saveTenderType(tenderType)
        }

        sendSyncMessage(tenderType, false)

        render(status: 200, text: "${tenderTypeCommand.name} saved successfully.")
    }

    @Secured(['ROLE_ENGINEER', 'ROLE_HEAD_OFFICE'])
    def ajaxDeleteTenderType(int id, boolean deleted) {
        TenderType tenderType = tenderTypeService.getTenderType(id)

        if (!tenderType) {
            render(status: 400, text: "Tender type not found.")
            return
        }

        tenderType.deleted = deleted

        tenderTypeService.saveTenderType(tenderType)

//        sendSyncMessage(tenderType)

        render(status: 200, text: "Successfully ${deleted ? 'deleted' : 'reinstated'} ${tenderType.name}.")
    }

    def sendSyncMessage(TenderType tenderType) {
        SyncMessage msg = new SyncMessage(SyncMessageType.TENDER_TYPE, springSecurityService.principal.retailerId, null, null, null)
        msg.setDelete(tenderType.deleted)
        msg.setInsert(!tenderType.deleted)
        msg.setTenderType(tenderType)

        rabbitService.sendMessage(msg)
    }
}

class TenderTypeCommand implements Validateable {

    int id
    String name
    String receiptDescription
    boolean autoReconcile
    boolean eligibleForBanking
    boolean eligibleForFloat
    boolean eligibleForCashLift
    boolean cashTender
    boolean cardPayment
    VoucherType voucherType
    boolean deleted
    boolean isProtected

    static constraints = {
        id nullable: false
        name size: 1..24, blank: false, nullable: false
        receiptDescription size: 1..24, blank: false, nullable: false
        autoReconcile nullable: false
        eligibleForBanking nullable: false
        eligibleForFloat nullable: false
        eligibleForCashLift nullable: false
        cashTender nullable: false, validator: { val, obj ->
            if (val == true && (obj.cardPayment || obj.voucherType != null)) {
                return ['tenderTypeCommand.multipleTypes']
            }

            return true
        }
        cardPayment nullable: false, validator: { val, obj ->
            if (val == true && (obj.cashTender || obj.voucherType != null)) {
                return ['tenderTypeCommand.multipleTypes']
            }

            return true
        }
        voucherType nullable: true, validator: { val, obj ->
            if (val != null && (obj.cashTender || obj.cardPayment)) {
                return ['tenderTypeCommand.multipleTypes']
            }

            return true
        }
        deleted nullable: false
        isProtected nullable: false
    }
}