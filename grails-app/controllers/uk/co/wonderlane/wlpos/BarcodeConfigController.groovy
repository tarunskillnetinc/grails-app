package uk.co.wonderlane.wlpos

import grails.plugin.springsecurity.annotation.Secured
import uk.co.wonderlane.wlpos.entities.EmbeddedData
import uk.co.wonderlane.wlpos.entities.SyncMessage
import uk.co.wonderlane.wlpos.enums.BarcodeSignifierType
import uk.co.wonderlane.wlpos.enums.EmbeddedDataType
import uk.co.wonderlane.wlpos.enums.SyncMessageType

class BarcodeConfigController {

    def springSecurityService
    def barcodeSignifierService
    def rabbitService

    static final int MAX = 50;

    @Secured(['ROLE_ENGINEER', 'ROLE_HEAD_OFFICE'])
    def index() {
        if (springSecurityService.principal.storeId) {
            flash.error = "You cannot access this page when logged in as a store."
            redirect(uri: "/")
            return
        }

        [signifierTypes: BarcodeSignifierType.values()]
    }

    @Secured(['ROLE_ENGINEER', 'ROLE_HEAD_OFFICE'])
    def ajaxSearchForBarcodeSignifiers() {

        String typeValue = params.typeFilter ? params.typeFilter : null
        String patternValue = params.patternFilter ? params.patternFilter : null
        String descriptionValue = params.descriptionFilter ? params.descriptionFilter: null
        Integer retailerIdValue = springSecurityService.principal.retailerId

        def sortParams = [:]

        if (!params.sort) {
            sortParams = [max: MAX, offset: 0, sort: "storeNumber", order: "ASC"]
        } else {
            sortParams.max = Integer.parseInt(params.max)
            sortParams.offset = Integer.parseInt(params.offset)
            sortParams.sort = params.sort
            sortParams.order = params.order
        }

        int offset = params.offset ? Integer.parseInt(params.offset) : 0
        int max = params.max ? Integer.parseInt(params.max) : MAX

        String sortBy = null;
        if (params.sort != null && params.order != null) {
            sortBy = String.format("%s %s", params.sort , params.order)
        }

        def signifiers = barcodeSignifierService.getSignifiersByFilters(retailerIdValue, typeValue,
                patternValue, descriptionValue, sortBy)

        def totalResults = signifiers.size()
        signifiers = signifiers.drop(offset).take(max);

        render (template: "signifiersSearchResults", model: [signifiers: signifiers, sortParams: sortParams, offset: offset, max: max, totalResults: totalResults])
    }

    @Secured(['ROLE_ENGINEER', 'ROLE_HEAD_OFFICE'])
    def ajaxAddSignifier() {
        render (template: 'addSignifier', model: [enableEdit: false, error:false, signifierTypes: BarcodeSignifierType.values()])
    }

    @Secured(['ROLE_ENGINEER'])
    def ajaxSaveSignifier() {
        def signifier = new BarcodeSignifier()
        signifier.id = params.id ? Integer.parseInt(params.id) : 0
        signifier.type = params.typeValue ? params.typeValue : null
        signifier.pattern = params.patternValue ? params.patternValue : ""
        signifier.length = params.lengthValue ? Integer.parseInt(params.lengthValue) : null
        signifier.description = params.descriptionValue ? params.descriptionValue : null
        signifier.receiptDescription = params.receiptDescriptionValue ? params.receiptDescriptionValue : null
        signifier.checkDigit = params.checkDigitValue ? params.checkDigitValue == "on" : false
        signifier.discountPercentage = params.discountPercentageValue ? Integer.parseInt(params.discountPercentageValue) : null
        signifier.retailerId = springSecurityService.principal.retailerId

        def result = barcodeSignifierService.saveSignifier(signifier)
        if (!result.success) {
            if (params.id == null) {
                render(template: "addSignifier", model: [signifier: signifier, error:true, errorMessages: result.errorMessages, signifierTypes: BarcodeSignifierType.values()])
            } else {
                // Render the editBarcodeSignifier GSP with errors
                render(view: "editBarcodeSignifier", model: [signifier: signifier, error: true,
                                                                 errorMessages: result.errorMessages, signifierTypes: BarcodeSignifierType.values()])
            }
            return
        }
        sendMessageToRabbit(true, [result.savedObject] as ArrayList)
        render "OK"
    }

    @Secured(['ROLE_HEAD_OFFICE'])
    def ajaxRestrictedSaveSignifier() {
        def id = params.id ? Integer.parseInt(params.id) : 0
        def signifier = barcodeSignifierService.getBarcodeSignifierById(id)
        signifier.description = params.descriptionValue ? params.descriptionValue : null
        signifier.receiptDescription = params.receiptDescriptionValue ? params.receiptDescriptionValue : null
        signifier.discountPercentage = params.discountPercentageValue ? Integer.parseInt(params.discountPercentageValue) : null

        def result = barcodeSignifierService.saveSignifier(signifier)
        if (!result.success) {
            // Render the editBarcodeSignifier GSP with errors
            render(view: "editBarcodeSignifier", model: [signifier: signifier, error: true,
                                                         errorMessages: result.errorMessages, signifierTypes: BarcodeSignifierType.values()])
            return
        }
        sendMessageToRabbit(true, [result.savedObject] as ArrayList)
        render "OK"
    }

    @Secured(['ROLE_ENGINEER'])
    def ajaxDeleteSignifier(int signifierId) {
        int retailerId = springSecurityService.principal.retailerId
        def result = barcodeSignifierService.deleteSignifier(retailerId, signifierId)
        if (result.success) {
            sendMessageToRabbit(false, [result.deletedObject] as ArrayList)
            render status: 200, text: "Barcode signifier has been deleted successfully."
        } else {
            render status: 500, text: "Error deleting Barcode Signifier."
        }
    }

    @Secured(['ROLE_ENGINEER', 'ROLE_HEAD_OFFICE'])
    def editBarcodeSignifier() {
        BarcodeSignifier barcodeSignifier = barcodeSignifierService.getBarcodeSignifierById(Integer.parseInt(params.signifierId))
        [signifierId:params.signifierId, signifierTypes: BarcodeSignifierType.values(), signifier:barcodeSignifier]
    }

    @Secured(['ROLE_ENGINEER', 'ROLE_HEAD_OFFICE'])
    def ajaxAddEmbeddedData() {
        render (template:"embeddedData/addEmbeddedData",
                model:[signifierId:params.signifierId, embeddedDataTypes: EmbeddedDataType.values(), enableEdit: false,
                       formatList:barcodeSignifierService.getEmbeddedDataFormats()])
    }

    @Secured(['ROLE_ENGINEER', 'ROLE_HEAD_OFFICE'])
    def ajaxEditEmbeddedData() {
        int embeddedDataId = params.embeddedDataId ? Integer.parseInt(params.embeddedDataId) : null
        BarcodeSignifierEmbeddedData embeddedData = barcodeSignifierService.getEmbeddedDataById(embeddedDataId)
        render (template:"embeddedData/addEmbeddedData",
                model:[embeddedData:embeddedData, embeddedDataTypes: EmbeddedDataType.values(),
                       formatList:barcodeSignifierService.getEmbeddedDataFormats(),signifierId:params.signifierId,enableEdit: true])
    }

    @Secured(['ROLE_ENGINEER'])
    def ajaxSaveEmbeddedData() {
        def embeddedData = new BarcodeSignifierEmbeddedData()
        embeddedData.id = params.id ? Integer.parseInt(params.id) : 0
        BarcodeSignifier barcodeSignifier = barcodeSignifierService.getBarcodeSignifierById(params.barcodeSignifierId ? Integer.parseInt(params.barcodeSignifierId) : 0)
        embeddedData.barcodeSignifier = barcodeSignifier
        embeddedData.type = params.typeValue ? params.typeValue : null
        embeddedData.format = params.formatValue ? params.formatValue : null
        embeddedData.startIndex = params.startIndexValue ? Integer.parseInt(params.startIndexValue) : null
        embeddedData.length = params.lengthValue ? Integer.parseInt(params.lengthValue) : null

        def result = barcodeSignifierService.saveEmbeddedData(embeddedData)
        if (!result.success) {
            if (embeddedData.id == 0) {
                render(template: "embeddedData/addEmbeddedData", model: [embeddedData: embeddedData,signifierId:barcodeSignifier.id,enableEdit: false,
                                                                         error:true, errorMessages: result.errorMessages, embeddedDataTypes: EmbeddedDataType.values(),
                                                                         formatList:barcodeSignifierService.getEmbeddedDataFormats()])
            } else {
//                 Render the editBarcodeSignifier GSP with errors
                render(template: "embeddedData/addEmbeddedData", model: [embeddedData: embeddedData,signifierId:barcodeSignifier.id,enableEdit: true,
                                                                         error:true, errorMessages: result.errorMessages, embeddedDataTypes: EmbeddedDataType.values(),
                                                                         formatList:barcodeSignifierService.getEmbeddedDataFormats()])
            }
            return
        }
        sendMessageToRabbit(true, result.savedObject as BarcodeSignifierEmbeddedData)
        render "OK"
    }

    @Secured(['ROLE_ENGINEER', 'ROLE_HEAD_OFFICE'])
    def ajaxShowEmbeddedDataList() {
        def embeddedDataList = barcodeSignifierService.getEmbeddedData(params.barcodeSignifierId ?
                Integer.parseInt(params.barcodeSignifierId) : 0)
        render (template: "embeddedData/embeddedDataSearchResults", model: [embeddedDataList: embeddedDataList])
    }

    @Secured(['ROLE_ENGINEER'])
    def ajaxDeleteEmbeddedData() {
        int embeddedDataId = params.embeddedDataId ? Integer.parseInt(params.embeddedDataId) : 0
        def result = barcodeSignifierService.deleteEmbeddedData(embeddedDataId)
        if (result.success) {
            sendMessageToRabbit(true, result.deletedObject as BarcodeSignifierEmbeddedData)
            render "OK"
        } else {
            render "Fail"
        }
    }

    @Secured(['ROLE_ENGINEER'])
    def ajaxSyncAllMessageToRabbit() {
        List<BarcodeSignifier> barcodeSignifiers = barcodeSignifierService.getSignifiersByFilters(springSecurityService.principal.retailerId, null, null, null, null);
        sendMessageToRabbit(true, barcodeSignifiers)
        render "OK"
    }

    @Secured(['ROLE_ENGINEER'])
    private void sendMessageToRabbit(boolean insert, BarcodeSignifierEmbeddedData barcodeSignifierEmbeddedData) {
        BarcodeSignifier existingSignifier = barcodeSignifierService.getBarcodeSignifierById(barcodeSignifierEmbeddedData.barcodeSignifier.id);
        sendMessageToRabbit(insert, [existingSignifier] as ArrayList)
    }

    @Secured(['ROLE_ENGINEER'])
    private void sendMessageToRabbit(boolean insert, List<BarcodeSignifier> barcodeSignifiers) {
        final List<uk.co.wonderlane.wlpos.entities.BarcodeSignifier> barcodeSignifiersSync = new ArrayList<>()
        barcodeSignifiers.each {signifier ->
            uk.co.wonderlane.wlpos.entities.BarcodeSignifier barcodeSignifierSync = new uk.co.wonderlane.wlpos.entities.BarcodeSignifier();
            barcodeSignifierSync.id = signifier.id
            barcodeSignifierSync.retailerId = signifier.retailerId
            barcodeSignifierSync.pattern = signifier.pattern
            barcodeSignifierSync.length = signifier.length
            barcodeSignifierSync.type = signifier.type
            barcodeSignifierSync.description = signifier.description
            barcodeSignifierSync.receiptDescription= signifier.receiptDescription
            barcodeSignifierSync.checkDigit = signifier.checkDigit
            barcodeSignifierSync.discountPercentage = signifier.discountPercentage
            barcodeSignifierSync.embeddedData = new HashMap<>();
            if (signifier.barcodeSignifierEmbeddedDatas != null) {
                signifier.barcodeSignifierEmbeddedDatas.each {embeddedData ->
                    EmbeddedData embeddedDataSync = new EmbeddedData()
                    embeddedDataSync.id = embeddedData.id
                    embeddedDataSync.barcodeSignifierId = signifier.id
                    embeddedDataSync.type = embeddedData.type
                    embeddedDataSync.startIndex = embeddedData.startIndex
                    embeddedDataSync.length = embeddedData.length
                    embeddedDataSync.format = embeddedData.format
                    barcodeSignifierSync.embeddedData.put(embeddedData.type, embeddedDataSync)
                }
            }
            barcodeSignifiersSync.add(barcodeSignifierSync)
        }

        // Otherwise, delete the Signifiers and update Rabbit
        SyncMessage syncMessage = new SyncMessage(SyncMessageType.BARCODE_SIGNIFIER, springSecurityService.principal.retailerId, 0,0, 0)
        if (insert) {
            syncMessage.setInsert(true)
        } else {
            syncMessage.setDelete(true)
        }
        syncMessage.setBarcodeSignifiers(barcodeSignifiersSync)
        rabbitService.sendMessage(syncMessage)
    }

}
