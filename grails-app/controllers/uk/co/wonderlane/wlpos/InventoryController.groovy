package uk.co.wonderlane.wlpos

import grails.converters.JSON
import grails.plugin.springsecurity.annotation.Secured
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import uk.co.wonderlane.wlpos.entities.SyncMessage
import uk.co.wonderlane.wlpos.enums.JobStatus
import uk.co.wonderlane.wlpos.enums.JobType
import uk.co.wonderlane.wlpos.enums.ReasonCodeType
import uk.co.wonderlane.wlpos.enums.SyncMessageType
import uk.co.wonderlane.wlpos.enums.wlim.ProductListStatus
import uk.co.wonderlane.wlpos.enums.wlim.ProductListType
import uk.co.wonderlane.wlpos.ProductListStore

class InventoryController {
    def productService
    def reasonCodeService
    def springSecurityService
    def categoryService
    def storeService
    def rabbitService
    def jobService
    def productListService

    @Secured(['ROLE_ENGINEER', 'ROLE_HEAD_OFFICE'])
    def index() {
        List<ReasonCode> reasonCodes = reasonCodeService.getReasonCodesByRetailer(springSecurityService.principal.retailerId)
        def stockAdjustments = getStockAdjustments(params.statusSelect ?: 'All')

        [reasonCodes: reasonCodes, stockAdjustments: stockAdjustments]
    }

    private def getStockAdjustments(String status) {
        def criteria = ProductList.createCriteria()

        def stockAdjustments = criteria.list {
            eq("retailerId", springSecurityService.principal.retailerId)

            // Use 'or' to match any of the types that might be inventory adjustments
            or {
                eq("type", ProductListType.ORDER)  // Orders can be treated as stock adjustments
                eq("type", ProductListType.DELIVERY)  // Deliveries affect inventory
                eq("stockAdjustedOnCompletion", true)  // Any list that adjusts stock
            }

            if (status && status != 'All') {
                try {
                    eq("status", ProductListStatus.valueOf(status.toUpperCase()))
                } catch (IllegalArgumentException e) {
                    log.warn("Invalid status filter: ${status}")
                    // If status is invalid, default to show all
                }
            }

            // Eager fetch productListItems to avoid N+1 query issues
            fetchMode 'productListItems', org.hibernate.FetchMode.JOIN

            order("id", "desc")
            maxResults(50)
        }

        // Make sure all data is properly loaded
        stockAdjustments.each { adjustment ->
            // Force lazy collections to load
            if (adjustment.productListItems == null) {
                adjustment.productListItems = []
            }
        }

        return stockAdjustments
    }

    @Secured(['ROLE_ENGINEER', 'ROLE_HEAD_OFFICE'])
    def ajaxFilterAdjustments() {
        def allResults = getStockAdjustmentData()
        
        def filteredResults = []
        if (params.status && params.status != 'All') {
            filteredResults = allResults.stockAdjustmentResults.findAll { result ->
                def statusToMatch = params.status.toUpperCase()
                result.status?.equalsIgnoreCase(statusToMatch) ||
                result.status?.toString()?.toUpperCase()?.contains(statusToMatch)
            }
        } else {
            filteredResults = allResults.stockAdjustmentResults
        }

        render(template: "stockStatusSearchResults", model: [stockAdjustmentResults: filteredResults])
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
        if (!params.reasonCodeId) {
            flash.error = "Reason code is required"
            render view: 'createStockAdjustment'
            return
        }

        def reasonCodes = reasonCodeService.findReasonCodesByIds([params.reasonCodeId.toLong()])
        if (reasonCodes.empty) {
            flash.error = "Invalid reason code"
            render view: 'createStockAdjustment'
            return
        }

        def reasonCode = reasonCodes.first()
        def productList = new ProductList(
            retailerId: springSecurityService.principal.retailerId,
            userId: springSecurityService.principal.username,
            reasonId: reasonCode.id,
            reasonDescription: reasonCode.description,
            status: ProductListStatus.PENDING,
            type: ProductListType.STOCK_ADJUSTMENT,
            dateStarted: new DateTime(DateTimeZone.UTC)
        )

        if (!productList.validate()) {
            flash.error = "Failed to create stock adjustment: ${productList.errors}"
            render view: 'createStockAdjustment'
            return
        }

        productListService.saveProductList(productList)
        session.currentProductListId = productList.id

        flash.message = "Stock adjustment created successfully"
        render view: 'createStockAdjustment'
    }

    @Secured(['ROLE_ENGINEER', 'ROLE_HEAD_OFFICE'])
    def ajaxAddProduct(int productId) {
        def product = productService.getProductVariant(productId)
        def category = categoryService.getCategory(product.product.categoryId)
        
        // Get current product list from session
        def productListId = session.currentProductListId
        if (!productListId) {
            render status: 400, text: "No active product list found"
            return
        }

        // Create new product list item
        def productListItem = new ProductListItem(
            productList: ProductList.get(productListId),
            productVariant: product,
            productQuantityInStock: null,
            quantity: null,
            fillQuantity: 0.000
        )

        if (!productListService.saveProductListItem(productListItem)) {
            log.error("Failed to save product list item: ${productListItem.errors}")
            render status: 500, text: "Failed to add product to list"
            return
        }

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

//    def ajaxSearchProducts() {
//        session.PENDING_CHANGES = params.pendingChanges
//        session.PRODUCT_SEARCH_TERM = params.searchTerm
//        Boolean filterWithPendingChanges = Boolean.parseBoolean(params.pendingChanges)
//        session.effectiveDate = ["Current", DateTime.now(DateTimeZone.UTC)]
//
//        def products = productService.searchProductsHql(params.searchTerm, params.searchBy, params.max ? Integer.parseInt(params.max) : 50, params.offset ? Integer.parseInt(params.offset) : 0, "id", "asc", filterWithPendingChanges)
//
//        render(template: "productSearch", model: [products    : products.products,
//                                                         storeId     : springSecurityService.principal.storeId,
//                                                         userColumns : productService.getColumns(),
//                                                         searchTerm  : params.searchTerm,
//                                                         searchBy    : params.searchBy,
//                                                         max         : params.max ?: 50,
//                                                         offset      : params.offset,
//                                                         totalResults: products.totalCount])
//    }

    @Secured(['ROLE_ENGINEER', 'ROLE_HEAD_OFFICE'])
    def ajaxSearchProducts() {
        def searchTerm = params.searchTerm
        def searchBy = params.searchBy ?: "everything"
        def max = params.max ? params.int('max') : 50
        def offset = params.offset ? params.int('offset') : 0

        def products = productService.searchProductsHql(
                searchTerm,
                searchBy,
                max,
                offset,
                "id",
                "asc",
                false
        )

        render(template: "/inventory/addProductSearchResults", model: [
                products: products.products,
                totalResults: products.totalCount,
                searchTerm: searchTerm,
                searchBy: searchBy,
                max: max,
                offset: offset
        ])
    }

    @Secured(['ROLE_ENGINEER', 'ROLE_HEAD_OFFICE'])
    def ajaxGetStockAdjustments() {
        def results = getStockAdjustmentData()
        render(template: "/inventory/stockStatusSearchResults", model: results)
    }

    @Secured(['ROLE_ENGINEER', 'ROLE_HEAD_OFFICE'])
    def finalizeStockAdjustment() {
        def productListId = session.currentProductListId
        if (!productListId) {
            render status: 400, text: "Missing productListId in session"
            return
        }

        try {
            ProductList.withTransaction { status ->
                // Update ProductList status
                def productList = ProductList.get(productListId)
                productList.status = "SCHEDULED"
                productList.save(flush: true)

                def amendedQuantities = params.amendedQuantities ?
                    JSON.parse(params.amendedQuantities) : [:]
                
                ProductListItem.findAllByProductList(productList).each { item ->
                    if (amendedQuantities[item.productVariant.id.toString()]) {
                        item.fillQuantity = amendedQuantities[item.productVariant.id.toString()].toInteger()
                        item.save(flush: true)
                    }
                }

                def job = new Job()
                job.uuid = UUID.randomUUID()
                job.type = JobType.STOCK_ADJUSTMENT
                job.storeId= null
                job.status = JobStatus.PENDING
                job.dateCreated = DateTime.now()
                job.productListId = productListId
                job.retailerId = springSecurityService.principal.retailerId
                jobService.saveJob(job)

                def syncMessage = new SyncMessage(
                        SyncMessageType.STOCK_ADJUSTMENT,
                        springSecurityService.principal.retailerId,
                        null,
                        null,
                        null
                )
                syncMessage.setUuid(job.uuid)
                syncMessage.setStatus(JobStatus.PENDING)
                syncMessage.setProductListId(job.productListId)
                rabbitService.sendJobsMessage(syncMessage)

                render status: 200, text: "Stock adjustment scheduled successfully"
            }
        } catch (Exception e) {
            log.error("Failed to finalize stock adjustment", e)
            render status: 500, text: "Failed to schedule stock adjustment: ${e.message}"
        }
    }


    @Secured(['ROLE_ENGINEER', 'ROLE_HEAD_OFFICE'])
    def ajaxRemoveProduct() {
        def productId = params.long('productId')
        def productListId = session.currentProductListId
        
        if (!productId || !productListId) {
            render status: 400, text: "Missing required parameters"
            return
        }
    
        try {
            ProductList.withTransaction { status ->
                def productListItem = ProductListItem.findByProductListAndProductVariant(
                    ProductList.get(productListId),
                    ProductVariant.get(productId))
                
                if (productListItem) {
                    productListItem.delete(flush: true)
                    render status: 200, text: "Product removed successfully"
                } else {
                    render status: 404, text: "Product not found in list"
                }
            }
        } catch (Exception e) {
            log.error("Failed to remove product", e)
            render status: 500, text: "Failed to remove product: ${e.message}"
        }
    }

    private def getStockAdjustmentData() {
        def stockAdjustmentResults = []

        // Query ProductList entries filtered by retailerId
        def productLists = ProductList.createCriteria().list {
            eq("retailerId", springSecurityService.principal.retailerId)
        }

        // Populate the results with counts from related tables
        productLists.each { productList ->
            def storeCount = ProductListStore.countByProductList(productList)
            def productCount = ProductListItem.countByProductList(productList)

            stockAdjustmentResults << [
                    id: productList.id,
                    status: productList.status?.getFriendlyName() ?: productList.status,
                    totalStores: storeCount,
                    totalProducts: productCount,
                    dateActioned: productList.dateCompleted?.toDate() // Convert Joda DateTime to Java Date
            ]
        }

        return [stockAdjustmentResults: stockAdjustmentResults]
    }

    @Secured(['ROLE_ENGINEER', 'ROLE_HEAD_OFFICE'])
    def saveProductListStores() {
        def productListId = session.currentProductListId
        if (!productListId) {
            render status: 400, text: "No active product list"
            return
        }

        def storeData = request.JSON?.stores
        if (!storeData) {
            render status: 400, text: "No stores provided"
            return
        }

        try {
            ProductList.withTransaction { status ->
                def productList = ProductList.get(productListId)
                if (!productList) {
                    render status: 404, text: "Product list not found"
                    return
                }

                storeData.each { store ->
                    def storeInstance = Store.get(store)
                    if (!storeInstance) {
                        log.warn("Store not found with ID: ${store.id}")
                        return
                    }

                    def productListStore = new ProductListStore(
                        productList: productList,
                        store: storeInstance
                    )
                    
                    if (!productListStore.save(flush: true)) {
                        log.error("Failed to save ProductListStore: ${productListStore.errors}")
                        throw new RuntimeException("Failed to save store association")
                    }
                    log.debug("Saved ProductListStore: ${productList.id} -> ${storeInstance.id}")
                }
                render status: 200, text: "Stores saved successfully"
            }
        } catch (Exception e) {
            log.error("Failed to save stores", e)
            render status: 500, text: "Failed to save stores: ${e.message}"
        }
    }

}