package uk.co.wonderlane.wlpos

import grails.web.http.HttpHeaders
import org.joda.time.DateTime
import uk.co.wonderlane.wlpos.enums.wlim.PrintProcess
import uk.co.wonderlane.wlpos.enums.wlim.PrintType
import uk.co.wonderlane.wlpos.labelling.LabelTemplate

class ShelfEdgeLabelController {

    def springSecurityService
    def productListService
    def shelfEdgeLabelService

    def index() {
        def labelTemplates = shelfEdgeLabelService.getLabelTemplates(PrintProcess.SHELF_EDGE_LABEL_BATCH, PrintType.PDF)

        [labelTemplates: labelTemplates]
    }

    def ajaxGetAdHocBatches() {
        def adHocBatches = productListService.getAdHocBatches()

        render (template: "adHocResults", model: [productLists: adHocBatches])
    }

    def ajaxGetScheduledBatches() {
        render (template: "scheduledResults")
    }

    def ajaxGenerateAdHocPdf() {
        ProductList productList = productListService.getProductList(Integer.parseInt(params.productListId))
        LabelTemplate labelTemplate = shelfEdgeLabelService.getLabelTemplate(Integer.parseInt(params.labelTemplateId))
        PrintProcess printProcess = PrintProcess.valueOf(params.printProcess)
        PrintType printType = PrintType.valueOf(params.printType)
        StoreSettings storeSettings = StoreSettings.findByIdAndRetailerId(springSecurityService.principal.storeId, springSecurityService.principal.retailerId)

        def documentBytes = shelfEdgeLabelService.generatePdf(productList, labelTemplate, printProcess, printType, storeSettings)

        response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=AdHocBatch-" + DateTime.now().toString("yyyy_MM_dd_HH_mm_ss") +".pdf")
        response.setContentType("application/pdf")
        response.setCharacterEncoding("UTF-8")
        response.contentLength = documentBytes.size()
        response.outputStream << documentBytes
        response.outputStream.flush()
        response.outputStream.close()
    }
}
