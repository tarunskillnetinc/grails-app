package uk.co.wonderlane.wlpos

import grails.converters.JSON
import grails.plugin.springsecurity.annotation.Secured
import uk.co.wonderlane.wlpos.enums.ReasonCodeType

class InventoryController {
    def productService
    def reasonCodeService
    def springSecurityService
    def categoryService
    def storeService

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
                def productName = parts[0]?.trim()

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


    @Secured(['ROLE_ENGINEER', 'ROLE_HEAD_OFFICE'])
    def ajaxResetInventoryTemplate() {
        try {
            String selectedStore = params.selectedStore
            if (!selectedStore) {
                render status: 400, text: "No store selected"
                return
            }

            def storeInfo = selectedStore.split('-')
            def storeNumber = storeInfo[0]
            def storeName = storeInfo.size() > 1 ? storeInfo[1] : ""

            render(template: "resetInventory", model: [
                    storeNumber: storeNumber,
                    storeName: storeName
            ])
        } catch (Exception e) {
            log.error("ajaxResetInventoryTemplate failed", e)
            render status: 500, text: "Error: ${e.message}"
        }
    }

    @Secured(['ROLE_ENGINEER', 'ROLE_HEAD_OFFICE'])
    def ajaxGetStores() {
        Integer storeNumberFilter = null
        String storeNameFilter = null
        boolean showDeletedFilter = false
        def sortParams = [:]

        try {
            try {
                sortParams.max = params.max?.toInteger() ?: 50
            } catch(NumberFormatException _) {
                sortParams.max = 50
            }
            try {
                sortParams.offset = params.offset?.toInteger() ?: 0
            } catch(NumberFormatException _) {
                sortParams.offset = 0
            }
            sortParams.sort  = params.sort ?: "storeNumber"
            sortParams.order = params.order ?: "ASC"

            if (params.storeNumberFilter) {
                try {
                    storeNumberFilter = params.storeNumberFilter.toInteger()
                } catch(NumberFormatException ignored) {
                }
            }

            if (params.storeNameFilter && params.storeNameFilter != "null") {
                storeNameFilter = params.storeNameFilter
            }
            if (params.showDeletedFilter == "true") {
                showDeletedFilter = true
            }

            def (stores, storeCount) = storeService.searchStores(
                    springSecurityService.principal.retailerId,
                    storeNumberFilter,
                    storeNameFilter,
                    showDeletedFilter,
                    sortParams
            )

            render(template: "storeSearchResults", model: [
                    stores:              stores,
                    totalResults:        storeCount,
                    sortParams:          sortParams,
                    storeNameFilter:     storeNameFilter ?: "",
                    storeNumberFilter:   storeNumberFilter ?: "",
                    showDeletedFilter:   showDeletedFilter
            ])
        } catch (Exception e) {
            log.error("ajaxGetStores failed", e)
            render status: 500, text: "DEBUG ERROR: ${e.class.simpleName}: ${e.message}"
        }
    }

}
