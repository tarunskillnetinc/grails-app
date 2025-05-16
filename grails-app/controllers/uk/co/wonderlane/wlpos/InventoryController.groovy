package uk.co.wonderlane.wlpos

import grails.converters.JSON
import grails.plugin.springsecurity.annotation.Secured
import uk.co.wonderlane.wlpos.enums.ReasonCodeType

class InventoryController {
    def productService
    def reasonCodeService
    def springSecurityService
    def categoryService

    @Secured(['ROLE_ENGINEER', 'ROLE_HEAD_OFFICE'])
    def index() {
        List<ReasonCode> reasonCodes = reasonCodeService.getReasonCodesByType(springSecurityService.principal.retailerId, ReasonCodeType.PAID_OUT)
        [reasonCodes: reasonCodes]
    }
    @Secured(['ROLE_ENGINEER', 'ROLE_HEAD_OFFICE'])
    def ajaxImportCSV() {
        def file = request.getFile("csvFile")

        if (!file || file.empty) {
            render status: 400, text: "No file uploaded"
            return
        }

        def totalLines = 0
        def successLines = 0
        def errorLog = []

        file.inputStream.withReader { reader ->

            reader.eachLine { line, index ->
                if(index==1){
                    println("Line 1 ${line}")
                    return
                }
                totalLines++
                def parts = line.split(",")
                def productName = parts[0]?.trim()  // Assuming product name is first column

                if (productName) {
                    def product = productService.getProduct(productName)
                    if (!product) {
                        errorLog << "Unknown Item ID ${productName} in line ${totalLines}"
                    }
                    else {
                        successLines++
                    }
                }else{
                    errorLog << "Missing Item ID in line ${totalLines}"
                }

            }
        }

        def response = [
                file      : file.originalFilename,
                total     : totalLines,
                success   : successLines,
                errors    : totalLines - successLines,
                errorLog  : errorLog
        ]

        render response as JSON
    }

    @Secured(['ROLE_ENGINEER', 'ROLE_HEAD_OFFICE'])
    def createStockAdjustment() {
        println(params.reasonCodeId)
        render view: 'createStockAdjustment'
    }

    @Secured(['ROLE_ENGINEER', 'ROLE_HEAD_OFFICE'])
    def ajaxAddProduct(int productId) {
        def product = productService.getProductVariant(productId)
        def category = categoryService.getCategory(product.product.categoryId)

        render(template: "stockSearchResults", model: [product: product, category:category])
    }

}
