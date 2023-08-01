package uk.co.wonderlane.wlpos

import grails.web.http.HttpHeaders
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat
import org.joda.time.format.DateTimeFormatter
import uk.co.wonderlane.wlpos.enums.wlim.PrintProcess
import uk.co.wonderlane.wlpos.enums.wlim.PrintType
import uk.co.wonderlane.wlpos.enums.wlim.ProductListStatus
import uk.co.wonderlane.wlpos.labelling.LabelTemplate

class ShelfEdgeLabelController {

    def springSecurityService
    def productListService
    def shelfEdgeLabelService
    def productService

    def index() {

    }

    def ajaxGetAdHocBatches() {
        def adHocBatches = productListService.getAdHocBatches()
        def labelTemplates = shelfEdgeLabelService.getLabelTemplates(PrintProcess.SHELF_EDGE_LABEL_BATCH, PrintType.PDF)

        render (template: "adHocResults", model: [productLists: adHocBatches, labelTemplates: labelTemplates, deletableStatuses: [ProductListStatus.PARTIALLY_COMPLETE]])
    }

    def ajaxGetScheduledBatches() {
        def scheduledBatches = productListService.getScheduledBatches(null)

        def batchesToBePrinted = scheduledBatches.toBePrinted.groupBy { it.effectiveDate }
        def batchesToBeConfirmed = scheduledBatches.toBeConfirmed.groupBy { it.effectiveDate }

        def labelTemplates = shelfEdgeLabelService.getLabelTemplates(PrintProcess.SHELF_EDGE_LABEL_BATCH, PrintType.PDF)

        render (template: "scheduledResults", model: [batchesToBePrinted: batchesToBePrinted, batchesToBeConfirmed: batchesToBeConfirmed, labelTemplates: labelTemplates])
    }

    def ajaxGenerateAdHocPdf() {
        ProductList productList = productListService.getProductList(Integer.parseInt(params.productListId))
        LabelTemplate labelTemplate = shelfEdgeLabelService.getLabelTemplate(Integer.parseInt(params.labelTemplateId))
        PrintProcess printProcess = PrintProcess.valueOf(params.printProcess)
        PrintType printType = PrintType.valueOf(params.printType)
        Store storeSettings = Store.findByIdAndRetailerId(springSecurityService.principal.storeId, springSecurityService.principal.retailerId)

        def documentBytes = shelfEdgeLabelService.generatePdf(productList, labelTemplate, printProcess, printType, storeSettings)

        response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=AdHocBatch-" + DateTime.now().toString("yyyy_MM_dd_HH_mm_ss") +".pdf")
        response.setContentType("application/pdf")
        response.setCharacterEncoding("UTF-8")
        response.contentLength = documentBytes.size()
        response.outputStream << documentBytes
        response.outputStream.flush()
        response.outputStream.close()
    }

    def ajaxGenerateScheduledPdf() {
        DateTimeFormatter dateFormatter = DateTimeFormat.forPattern("dd/MM/yyyy").withZoneUTC()
        DateTime effectiveDate = params.effectiveDate ? DateTime.parse(params.effectiveDate, dateFormatter).withTimeAtStartOfDay() : null
        LabelTemplate labelTemplate = shelfEdgeLabelService.getLabelTemplate(Integer.parseInt(params.labelTemplateId))
        PrintProcess printProcess = params.printProcess ? PrintProcess.valueOf(params.printProcess) : null
        PrintType printType = params.printType ? PrintType.valueOf(params.printType) : null

        if (!effectiveDate || !labelTemplate || !printProcess || !printType) {
            response.status = 400
            return
        }

        Store storeSettings = Store.findByIdAndRetailerId(springSecurityService.principal.storeId, springSecurityService.principal.retailerId)

        def documentBytes = shelfEdgeLabelService.generatePdf(effectiveDate, labelTemplate, printProcess, printType, storeSettings)

        response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=AdHocBatch-" + DateTime.now().toString("yyyy_MM_dd_HH_mm_ss") +".pdf")
        response.setContentType("application/pdf")
        response.setCharacterEncoding("UTF-8")
        response.contentLength = documentBytes.size()
        response.outputStream << documentBytes
        response.outputStream.flush()
        response.outputStream.close()
    }

    def ajaxConfirmAdHocBatchPrintSuccessful() {
        ProductList productList = productListService.getProductList(Integer.parseInt(params.productListId))
        productList.status = ProductListStatus.COMPLETE
        productListService.saveProductList(productList)

        response.status = 204
    }

    def ajaxConfirmScheduledBatchPrintSuccessful() {
        DateTimeFormatter dateFormatter = DateTimeFormat.forPattern("dd/MM/yyyy").withZoneUTC()
        DateTime effectiveDate = params.effectiveDate ? DateTime.parse(params.effectiveDate, dateFormatter).withTimeAtStartOfDay() : null

        // TODO Get all products which have changed on this date and send down to this store exchange.

        // Mark all of the relevant producthistory records as "printed".
        shelfEdgeLabelService.setProductHistoryPrintStatus(effectiveDate, 1)

        response.status = 204
    }

    def ajaxDeleteProductList() {
        ProductList productList = productListService.getProductList(Integer.parseInt(params.productListId))

        productListService.deleteProductList(productList)

        response.status = 204
    }

    def ajaxConfirmApplyChangesToBatch() {
        def effectiveDate = DateTimeFormat.forPattern("dd/MM/yyyy").parseDateTime(params.effectiveDate)
        def batchType = params.batchType

        session.effectiveDate = [effectiveDate.toString(DateTimeFormat.forPattern("dd MMMM yyyy")), effectiveDate]

        log.println("Clicked with ${effectiveDate} and ${batchType}")

        def productIds = []
        productListService.getScheduledBatches(effectiveDate).toBeConfirmed.each {
            product -> productIds.add(product.productId)
        }
        productService.syncProductUpdatesToSingleStore(productIds, springSecurityService.principal.storeId)
        shelfEdgeLabelService.setProductHistoryPrintStatus(effectiveDate, 2)

        response.status = 204
    }
}