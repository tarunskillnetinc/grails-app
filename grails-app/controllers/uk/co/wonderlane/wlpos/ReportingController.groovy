package uk.co.wonderlane.wlpos

import groovy.json.JsonSlurper
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import org.joda.time.format.DateTimeFormat
import org.joda.time.format.DateTimeFormatter
import uk.co.wonderlane.wlpos.enums.PromotionType
import uk.co.wonderlane.wlpos.enums.TillControlEventType
import uk.co.wonderlane.wlpos.reporting.*
import uk.co.wonderlane.wlpos.supplier.Supplier

class ReportingController {

    def reportingService
    def productListService
    def springSecurityService

    private static final SALES_REPORT_CATEGORY_SORT_COLUMNS = [ "description", "quantity", "avgCostPrice", "avgRetailPrice", "retailPrice", "vatAmount", "avgMargin" ]
    private static final SALES_REPORT_PRODUCT_SORT_COLUMNS = [ "usersName", "category", "description", "quantity", "costPrice", "netTotal", "vatAmount", "profit", "margin", "dateCreated" ]
    private static final SALES_REPORT_SORT_COLUMNS = [ "description", "quantity", "avgCostPrice", "avgRetailPrice", "retailPrice", "vatAmount", "avgMargin" ]
    private static final PROMOTIONS_REPORT_SORT_COLUMNS = [ "type", "description", "quantity", "fullPrice", "discount", "margin", "profit", "vat", "dateCreated" ]
    private static final PROMOTION_REPORT_SORT_COLUMNS = [ "itemCode", "description", "costPrice", "fullPrice", "fullPriceMargin", "fullPriceProfit", "discount", "discountedPrice", "discountedMargin", "discountedProfit", "vat" ]
    private static final TILL_CONTROL_EVENTS_REPORT_SORT_COLUMNS = [ "type", "quantity" ]
    private static final TILL_CONTROL_EVENT_REPORT_SORT_COLUMNS = [ "dateCreated", "type", "usersName", "reason", "amount" ]
    private static final PAYPOINT_SALE_REPORT_SORT_COLUMNS = [ "transactionDate", "storeId", "wlTransactionId", "ppTransactionId", "terminalId", "description", "type", "value", "status" ]
    private static final ORDERS_REPORT_SORT_COLUMNS = [ "orderId", "storeId", "status", "dateCompleted", "supplierName", "numberOfItems", "value" ]
    private static final ORDER_REPORT_SORT_COLUMNS = [ "sku", "description", "orderedQuantity", "packQuantity", "lineValue" ]

    def index() {

    }

    // The top level of the main sales report.
    def salesDepartment() {
        DateTimeFormatter dateFormatter = DateTimeFormat.forPattern("dd/MM/yyyy").withZoneUTC()
        DateTime startDate = params.startDate ? DateTime.parse(params.startDate, dateFormatter).withTimeAtStartOfDay() : DateTime.now(DateTimeZone.UTC).withTimeAtStartOfDay()
        DateTime endDate = params.startDate ? DateTime.parse(params.endDate, dateFormatter).withTimeAtStartOfDay() : DateTime.now(DateTimeZone.UTC).withTimeAtStartOfDay()
        def stores = StoreSettings.findAllByRetailerIdAndStoreIdIsNotNull(springSecurityService.principal.retailerId)

        [reportType : ReportType.SALES_DEPARTMENT,
         userColumns: reportingService.getReportColumns(ReportType.SALES_DEPARTMENT),
         startDate  : startDate,
         endDate    : endDate,
         stores   : stores]
    }

    // The top level of the main sales report.
    def ajaxSalesDepartment(SortParams sortParams) {
        sortParams.validateParams(SALES_REPORT_CATEGORY_SORT_COLUMNS)

        DateTimeFormatter dateFormatter = DateTimeFormat.forPattern("dd/MM/yyyy").withZoneUTC()
        DateTime startDate = params.startDate ? DateTime.parse(params.startDate, dateFormatter).withTimeAtStartOfDay() : DateTime.now(DateTimeZone.UTC).withTimeAtStartOfDay()
        DateTime endDate = params.startDate ? DateTime.parse(params.endDate, dateFormatter).withTimeAtStartOfDay() : DateTime.now(DateTimeZone.UTC).withTimeAtStartOfDay()

        Integer storeId
        if (springSecurityService.principal.storeId) {
            storeId = springSecurityService.principal.storeId
        } else {
            storeId = params.storeFilter ? Integer.parseInt(params.storeFilter) : null
        }

        // Find all sales in the date range.
        def sales = reportingService.getSales(startDate, endDate.plusDays(1), storeId)

        // Filter our results.
        if (params.descriptionFilter) {
            sales = sales.findAll { it.salesCategories.first().categoryDescription.toLowerCase().contains(params.descriptionFilter.toLowerCase()) }
        }

        // Group them by department.
        def salesGrouped = sales.groupBy { sale ->
            sale.salesCategories.first().categoryId
        }

        def finalSales = []

        // Populating a dummy sale object for any of the sales which are not in this category (because they have summed values for everything in that category).
        salesGrouped.each { salesGroup ->
            Sale groupedSale = new Sale(
                    quantity: salesGroup.value.sum { it.quantity > 0 ? it.quantity : 0 },
                    costPrice: salesGroup.value.sum { it.quantity > 0 ? it.costPrice.setScale(2) : BigDecimal.ZERO.setScale(2) },
                    retailPrice: salesGroup.value.sum { it.quantity > 0 ? it.retailPrice.setScale(2) : BigDecimal.ZERO.setScale(2) },
                    vatAmount: salesGroup.value.sum { it.quantity > 0 ? it.vatAmount.setScale(2) : BigDecimal.ZERO.setScale(2) },
                    margin: salesGroup.value.sum { it.quantity > 0 ? it.margin.setScale(2) : BigDecimal.ZERO.setScale(2) },
                    productDescription: salesGroup.value[0].salesCategories.find { sc -> sc.categoryId == salesGroup.key }.categoryDescription,
                    productUnitSize: ""
            )

            groupedSale.refundQuantity = salesGroup.value.sum { it.quantity < 0 ? it.quantity : 0 } * -1

            // Also add a dummy category object so we know which category this is in the view.
            groupedSale.addToSalesCategories(new SaleCategory(categoryId: (int)salesGroup.key))

            finalSales.add(groupedSale)
        }

        // Sort into the required order.
        if (sortParams.sortColumn == "description") {
            finalSales.sort { it.productItemCode ? it.productItemCode + it.productDescription : it.productDescription }
        } else {
            finalSales.sort { it."${sortParams.sortColumn}" }
        }

        if (sortParams.sortOrder == "desc") {
            finalSales = finalSales.reverse()
        }

        if (params.csv != null && params.csv == "true") {
            def fileName = "SalesByDepartment-" + new Date().format("yyyy_MM_dd_HH_mm_ss") +".csv"
            response.setHeader("Content-Disposition", "attachment; filename=${fileName}")
            response.setHeader("Content-Type", "text/csv;")

            render getSalesByCategoryCsv(finalSales)
        } else {
            // Restrict the number of results.
            int totalResults = finalSales.size()
            finalSales = sortParams.offset < finalSales.size() ? finalSales.subList(sortParams.offset, (sortParams.offset + sortParams.max < finalSales.size() ? sortParams.offset + sortParams.max : finalSales.size())) : []

            render(template: "salesDepartmentResults", model: [sales       : finalSales,
                                                               userColumns : reportingService.getReportColumns(ReportType.SALES_DEPARTMENT),
                                                               startDate   : startDate,
                                                               endDate     : endDate,
                                                               sortParams  : sortParams,
                                                               totalResults: totalResults])
        }
    }

    // The middle level of the main sales report.
    def salesCategory() {
        int categoryId = getIntegerParam(params.categoryId)
        DateTimeFormatter dateFormatter = DateTimeFormat.forPattern("dd/MM/yyyy").withZoneUTC()
        DateTime startDate = params.startDate ? DateTime.parse(params.startDate, dateFormatter).withTimeAtStartOfDay() : DateTime.now(DateTimeZone.UTC).withTimeAtStartOfDay()
        DateTime endDate = params.startDate ? DateTime.parse(params.endDate, dateFormatter).withTimeAtStartOfDay() : DateTime.now(DateTimeZone.UTC).withTimeAtStartOfDay()
        def stores = StoreSettings.findAllByRetailerIdAndStoreIdIsNotNull(springSecurityService.principal.retailerId)

        [reportType : ReportType.SALES_CATEGORY,
         categoryId : categoryId,
         startDate  : startDate,
         endDate    : endDate,
         userColumns: reportingService.getReportColumns(ReportType.SALES_CATEGORY),
         stores     : stores]
    }

    // The middle level of the main sales report.
    def ajaxSalesCategory(SortParams sortParams) {
        int categoryId = getIntegerParam(params.categoryId)

        sortParams.validateParams(SALES_REPORT_CATEGORY_SORT_COLUMNS)

        DateTimeFormatter dateFormatter = DateTimeFormat.forPattern("dd/MM/yyyy").withZoneUTC()
        DateTime startDate = params.startDate ? DateTime.parse(params.startDate, dateFormatter).withTimeAtStartOfDay() : DateTime.now(DateTimeZone.UTC).withTimeAtStartOfDay()
        DateTime endDate = params.startDate ? DateTime.parse(params.endDate, dateFormatter).withTimeAtStartOfDay() : DateTime.now(DateTimeZone.UTC).withTimeAtStartOfDay()

        Integer storeId
        if (springSecurityService.principal.storeId) {
            storeId = springSecurityService.principal.storeId
        } else {
            storeId = params.storeFilter ? Integer.parseInt(params.storeFilter) : null
        }

        // Find all sales involving this category in the date range.
        def sales = reportingService.getSalesForCategory(categoryId, startDate,endDate.plusDays(1), storeId)

        // Group them by the next level down category ID if the sale is not directly in this category.
        def salesGrouped = sales?.groupBy { sale ->
            sale.salesCategories.find { saleCategory ->
                saleCategory.categoryLevel == (sale.salesCategories.find { saleCategory2 -> saleCategory2.categoryId == categoryId }.categoryLevel + 1)
            }?.categoryId
        }

        def finalSales = []

        // Populating a dummy sale object for any of the sales which are not in this category (because they have summed values for everything in that category).
        salesGrouped?.each { salesGroup ->
            if (salesGroup.key == null) {
                // This block are the products which are sold directly in this category. Filter the results and then group by product ID.
                def filteredProductSales = params.descriptionFilter ? salesGroup.value.findAll { (it.productItemCode.toLowerCase() + it.productDescription.toLowerCase()).contains(params.descriptionFilter.toLowerCase()) } : salesGroup.value

                def filteredGroupedProductSales = filteredProductSales?.groupBy { it.productId }

                filteredGroupedProductSales?.each { groupedProductSale ->
                    groupedProductSale.value[0].quantity = groupedProductSale.value.sum { it.quantity > 0 ? it.quantity : 0 }
                    groupedProductSale.value[0].refundQuantity = groupedProductSale.value.sum { it.quantity < 0 ? it.quantity : 0 } * -1
                    groupedProductSale.value[0].costPrice = groupedProductSale.value.sum { it.quantity > 0 ? it.costPrice : BigDecimal.ZERO }.setScale(2)
                    groupedProductSale.value[0].retailPrice = groupedProductSale.value.sum { it.quantity > 0 ? it.retailPrice : BigDecimal.ZERO }.setScale(2)
                    groupedProductSale.value[0].vatAmount = groupedProductSale.value.sum { it.quantity > 0 ? it.vatAmount : BigDecimal.ZERO }.setScale(2)
                    groupedProductSale.value[0].margin = groupedProductSale.value.sum { it.quantity > 0 ? it.margin : BigDecimal.ZERO }.setScale(2)

                    finalSales.add(groupedProductSale.value[0])
                }
            } else {
                if (!params.descriptionFilter || salesGroup.value[0].salesCategories.find { sc -> sc.categoryId == salesGroup.key }.categoryDescription.toLowerCase().contains(params.descriptionFilter?.toLowerCase())) {
                    Sale groupedSale = new Sale(
                            quantity: salesGroup.value.sum { it.quantity > 0 ? it.quantity : 0 },
                            costPrice: salesGroup.value.sum { it.quantity > 0 ? it.costPrice : BigDecimal.ZERO }.setScale(2),
                            retailPrice: salesGroup.value.sum { it.quantity > 0 ? it.retailPrice : BigDecimal.ZERO }.setScale(2),
                            vatAmount: salesGroup.value.sum { it.quantity > 0 ? it.vatAmount : BigDecimal.ZERO }.setScale(2),
                            margin: salesGroup.value.sum { it.quantity > 0 ? it.margin : BigDecimal.ZERO }.setScale(2),
                            productDescription: salesGroup.value[0].salesCategories.find { sc -> sc.categoryId == salesGroup.key }.categoryDescription,
                            productUnitSize: ""
                    )
                    groupedSale.refundQuantity = salesGroup.value.sum { it.quantity < 0 ? it.quantity : 0 } * -1

                    // Also add a dummy category object so we know which category this is in the view.
                    groupedSale.addToSalesCategories(new SaleCategory(categoryId: (int) salesGroup.key))

                    finalSales.add(groupedSale)
                }
            }
        }

        // Sort into the required order.
        if (sortParams.sortColumn == "description") {
            finalSales.sort { it.productItemCode ? it.productItemCode + it.productDescription : it.productDescription }
        } else {
            finalSales.sort { it."${sortParams.sortColumn}" }
        }

        if (sortParams.sortOrder == "desc") {
            finalSales = finalSales.reverse()
        }

        if (params.csv != null && params.csv == "true") {
            def fileName = "SalesByCategory-" + new Date().format("yyyy_MM_dd_HH_mm_ss") +".csv"
            response.setHeader("Content-Disposition", "attachment; filename=${fileName}")
            response.setHeader("Content-Type", "text/csv;")

            render getSalesByCategoryCsv(finalSales)
        } else {
            // Restrict the number of results.
            int totalResults = finalSales.size()
            finalSales = sortParams.offset < finalSales.size() ? finalSales.subList(sortParams.offset, (sortParams.offset + sortParams.max < finalSales.size() ? sortParams.offset + sortParams.max : finalSales.size())) : []

            render (template: "salesCategoryResults", model: [categoryId  : categoryId,
                                                              sales       : finalSales,
                                                              userColumns : reportingService.getReportColumns(ReportType.SALES_CATEGORY),
                                                              sortParams  : sortParams,
                                                              startDate   : startDate,
                                                              endDate     : endDate,
                                                              totalResults: totalResults])
        }
    }

    // The bottom level of the main sales report.
    def salesProduct() {
        int productId = getIntegerParam(params.productId)
        DateTimeFormatter dateFormatter = DateTimeFormat.forPattern("dd/MM/yyyy").withZoneUTC()
        DateTime startDate = params.startDate ? DateTime.parse(params.startDate, dateFormatter).withTimeAtStartOfDay() : DateTime.now(DateTimeZone.UTC).withTimeAtStartOfDay()
        DateTime endDate = params.startDate ? DateTime.parse(params.endDate, dateFormatter).withTimeAtStartOfDay() : DateTime.now(DateTimeZone.UTC).withTimeAtStartOfDay()
        def stores = StoreSettings.findAllByRetailerIdAndStoreIdIsNotNull(springSecurityService.principal.retailerId)

        [reportType : ReportType.SALES_PRODUCT,
         productId  : productId,
         startDate  : startDate,
         endDate    : endDate,
         userColumns: reportingService.getReportColumns(ReportType.SALES_PRODUCT),
         stores     : stores]
    }

    // The bottom level of the main sales report.
    def ajaxSalesProduct(SortParams sortParams) {
        int productId = getIntegerParam(params.productId)

        sortParams.validateParams(SALES_REPORT_PRODUCT_SORT_COLUMNS)

        DateTimeFormatter dateFormatter = DateTimeFormat.forPattern("dd/MM/yyyy").withZoneUTC()
        DateTime startDate = params.startDate ? DateTime.parse(params.startDate, dateFormatter).withTimeAtStartOfDay() : DateTime.now(DateTimeZone.UTC).withTimeAtStartOfDay()
        DateTime endDate = params.startDate ? DateTime.parse(params.endDate, dateFormatter).withTimeAtStartOfDay() : DateTime.now(DateTimeZone.UTC).withTimeAtStartOfDay()

        Integer storeId
        if (springSecurityService.principal.storeId) {
            storeId = springSecurityService.principal.storeId
        } else {
            storeId = params.storeFilter ? Integer.parseInt(params.storeFilter) : null
        }

        def sales = reportingService.getSalesForProduct(productId, startDate,endDate.plusDays(1), sortParams.max,
                sortParams.offset, sortParams.sortColumn, sortParams.sortOrder, params.descriptionFilter, storeId)

        def totalResults = reportingService.countSalesForProduct(productId, startDate,endDate.plusDays(1), params.descriptionFilter, storeId)

        if (params.csv != null && params.csv == "true") {
            def fileName = "SalesByProduct-" + new Date().format("yyyy_MM_dd_HH_mm_ss") +".csv"
            response.setHeader("Content-Disposition", "attachment; filename=${fileName}")
            response.setHeader("Content-Type", "text/csv;")
            render getSalesByProductCsv(sales)
        } else {
            render (template: "salesProductResults", model: [sales       : sales,
                                                             userColumns : reportingService.getReportColumns(ReportType.SALES_PRODUCT),
                                                             sortParams  : sortParams,
                                                             startDate   : startDate,
                                                             endDate     : endDate,
                                                             totalResults: totalResults])
        }
    }

    // The standalone category sales report.
    def categorySales() {
        DateTimeFormatter dateFormatter = DateTimeFormat.forPattern("dd/MM/yyyy").withZoneUTC()
        DateTime startDate = params.startDate ? DateTime.parse(params.startDate, dateFormatter).withTimeAtStartOfDay() : DateTime.now(DateTimeZone.UTC).withTimeAtStartOfDay()
        DateTime endDate = params.startDate ? DateTime.parse(params.endDate, dateFormatter).withTimeAtStartOfDay() : DateTime.now(DateTimeZone.UTC).withTimeAtStartOfDay()
        def stores = StoreSettings.findAllByRetailerIdAndStoreIdIsNotNull(springSecurityService.principal.retailerId)

        [reportType : ReportType.CATEGORY_SALES,
         startDate  : startDate,
         endDate    : endDate,
         userColumns: reportingService.getReportColumns(ReportType.CATEGORY_SALES),
         stores     : stores]
    }

    // The standalone category sales report.
    def ajaxCategorySales(SortParams sortParams) {
        sortParams.validateParams(SALES_REPORT_CATEGORY_SORT_COLUMNS)

        int maxCategoryLevel = getIntegerParam(params.maxCategoryLevel)

        DateTimeFormatter dateFormatter = DateTimeFormat.forPattern("dd/MM/yyyy").withZoneUTC()
        DateTime startDate = params.startDate ? DateTime.parse(params.startDate, dateFormatter).withTimeAtStartOfDay() : DateTime.now(DateTimeZone.UTC).withTimeAtStartOfDay()
        DateTime endDate = params.startDate ? DateTime.parse(params.endDate, dateFormatter).withTimeAtStartOfDay() : DateTime.now(DateTimeZone.UTC).withTimeAtStartOfDay()

        Integer storeId
        if (springSecurityService.principal.storeId) {
            storeId = springSecurityService.principal.storeId
        } else {
            storeId = params.storeFilter ? Integer.parseInt(params.storeFilter) : null
        }

        // Find all sales in the date range.
        def sales = reportingService.getSales(storeId, startDate, endDate.plusDays(1))

        sales = sales.sort { it.salesCategories?.first()?.categoryDescription }

        // Group them by the next level down category ID if the sale is not directly in this category.
        def salesGrouped = sales?.groupBy { sale ->
            sale.salesCategories?.sort{ it.categoryLevel }?.first()?.categoryId
        }

        def finalSales = []

        populateCategorySalesFinalSales(0, salesGrouped, finalSales, maxCategoryLevel)

        if (params.csv != null && params.csv == "true") {
            def fileName = "CategorySales-" + new Date().format("yyyy_MM_dd_HH_mm_ss") +".csv"
            response.setHeader("Content-Disposition", "attachment; filename=${fileName}")
            response.setHeader("Content-Type", "text/csv;")
            render getSalesByCategoryCsv(finalSales)
        } else {
            int totalResults = finalSales.size()
            finalSales = sortParams.offset < finalSales.size() ? finalSales.subList(sortParams.offset, (sortParams.offset + sortParams.max < finalSales.size() ? sortParams.offset + sortParams.max : finalSales.size())) : []

            render (template: "categorySalesResults", model: [sales       : finalSales,
                                                              userColumns : reportingService.getReportColumns(ReportType.CATEGORY_SALES),
                                                              sortParams  : sortParams,
                                                              startDate   : startDate,
                                                              endDate     : endDate,
                                                              totalResults: totalResults])
        }
    }

    private void populateCategorySalesFinalSales(int currentCategoryLevel, salesGroupedByDepartment, List finalSales, int maxCategoryLevel) {
        salesGroupedByDepartment?.each { salesGroup ->
            // Anything with a null key is a group of sales from the level above so ignore it here, it should have already been added by the previous iteration of this method.
            if (salesGroup.key == null) {
                return
            }

            Sale groupedSale = new Sale(
                    quantity: salesGroup.value.sum { it.quantity > 0 ? it.quantity : 0 },
                    costPrice: salesGroup.value.sum { it.quantity > 0 ? it.costPrice : BigDecimal.ZERO }.setScale(2),
                    retailPrice: salesGroup.value.sum { it.quantity > 0 ? it.retailPrice : BigDecimal.ZERO }.setScale(2),
                    vatAmount: salesGroup.value.sum { it.quantity > 0 ? it.vatAmount : BigDecimal.ZERO }.setScale(2),
                    margin: salesGroup.value.sum { it.quantity > 0 ? it.margin : BigDecimal.ZERO }.setScale(2),
                    productDescription: salesGroup.value[0].salesCategories.find { it.categoryLevel == currentCategoryLevel }?.categoryDescription,
                    productUnitSize: ""
            )
            groupedSale.refundQuantity = salesGroup.value.sum { it.quantity < 0 ? it.quantity : 0 } * -1

            // Also add a dummy category object so we know which category this is in the view.
            groupedSale.addToSalesCategories(new SaleCategory(categoryId: (int) salesGroup.key, categoryLevel: currentCategoryLevel))

            finalSales.add(groupedSale)

            def salesGroupedCurrentLevel = salesGroup.value.groupBy { Sale sale -> sale.salesCategories?.find { it.categoryLevel == currentCategoryLevel + 1 }?.categoryId }

            if (salesGroupedCurrentLevel.size() > 1 && currentCategoryLevel < maxCategoryLevel) {
                populateCategorySalesFinalSales(currentCategoryLevel + 1, salesGroupedCurrentLevel, finalSales, maxCategoryLevel)
            }
        }
    }

    // The standalone product sales report.
    def sales() {
        DateTimeFormatter dateFormatter = DateTimeFormat.forPattern("dd/MM/yyyy").withZoneUTC()
        DateTime startDate = params.startDate ? DateTime.parse(params.startDate, dateFormatter).withTimeAtStartOfDay() : DateTime.now(DateTimeZone.UTC).withTimeAtStartOfDay()
        DateTime endDate = params.startDate ? DateTime.parse(params.endDate, dateFormatter).withTimeAtStartOfDay() : DateTime.now(DateTimeZone.UTC).withTimeAtStartOfDay()
        def stores = StoreSettings.findAllByRetailerIdAndStoreIdIsNotNull(springSecurityService.principal.retailerId)

        [reportType : ReportType.SALES,
         startDate  : startDate,
         endDate    : endDate,
         userColumns: reportingService.getReportColumns(ReportType.SALES),
         stores     : stores]
    }

    // The standalone product sales report.
    def ajaxSales(SortParams sortParams) {
        sortParams.validateParams(SALES_REPORT_SORT_COLUMNS)

        DateTimeFormatter dateFormatter = DateTimeFormat.forPattern("dd/MM/yyyy").withZoneUTC()
        DateTime startDate = params.startDate ? DateTime.parse(params.startDate, dateFormatter).withTimeAtStartOfDay() : DateTime.now(DateTimeZone.UTC).withTimeAtStartOfDay()
        DateTime endDate = params.startDate ? DateTime.parse(params.endDate, dateFormatter).withTimeAtStartOfDay() : DateTime.now(DateTimeZone.UTC).withTimeAtStartOfDay()

        Integer storeId
        if (springSecurityService.principal.storeId) {
            storeId = springSecurityService.principal.storeId
        } else {
            storeId = params.storeFilter ? Integer.parseInt(params.storeFilter) : null
        }

        // Find all sales in the date range.
        def sales = reportingService.getSales(storeId, startDate, endDate.plusDays(1))

        def filteredProductSales = params.descriptionFilter ? sales.findAll { (it.productItemCode.toLowerCase() + it.productDescription.toLowerCase()).contains(params.descriptionFilter.toLowerCase()) } : sales

        def finalSales = []

        def filteredGroupedProductSales = filteredProductSales?.groupBy { it.productId }

        filteredGroupedProductSales?.each { groupedProductSale ->
            groupedProductSale.value[0].quantity = groupedProductSale.value.sum { it.quantity > 0 ? it.quantity : 0 }
            groupedProductSale.value[0].refundQuantity = groupedProductSale.value.sum { it.quantity < 0 ? it.quantity : 0 } * -1
            groupedProductSale.value[0].costPrice = groupedProductSale.value.sum { it.quantity > 0 ? it.costPrice : BigDecimal.ZERO }.setScale(2)
            groupedProductSale.value[0].retailPrice = groupedProductSale.value.sum { it.quantity > 0 ? it.retailPrice : BigDecimal.ZERO }.setScale(2)
            groupedProductSale.value[0].vatAmount = groupedProductSale.value.sum { it.quantity > 0 ? it.vatAmount : BigDecimal.ZERO }.setScale(2)
            groupedProductSale.value[0].margin = groupedProductSale.value.sum { it.quantity > 0 ? it.margin : BigDecimal.ZERO }.setScale(2)

            finalSales.add(groupedProductSale.value[0])
        }

        // Sort into the required order.
        if (sortParams.sortColumn == "description") {
            finalSales.sort { it.productItemCode ? it.productItemCode + it.productDescription : it.productDescription }
        } else {
            finalSales.sort { it."${sortParams.sortColumn}" }
        }

        if (sortParams.sortOrder == "desc") {
            finalSales = finalSales.reverse()
        }

        if (params.csv != null && params.csv == "true") {
            def fileName = "Sales-" + new Date().format("yyyy_MM_dd_HH_mm_ss") +".csv"
            response.setHeader("Content-Disposition", "attachment; filename=${fileName}")
            response.setHeader("Content-Type", "text/csv;")

            render getSalesCsv(finalSales)
        } else {
            // Restrict the number of results.
            int totalResults = finalSales.size()
            finalSales = sortParams.offset < finalSales.size() ? finalSales.subList(sortParams.offset, (sortParams.offset + sortParams.max < finalSales.size() ? sortParams.offset + sortParams.max : finalSales.size())) : []

            render (template: "salesResults", model: [sales       : finalSales,
                                                      userColumns : reportingService.getReportColumns(ReportType.SALES),
                                                      sortParams  : sortParams,
                                                      startDate   : startDate,
                                                      endDate     : endDate,
                                                      totalResults: totalResults])
        }
    }

    def promotionsGrouped() {
        DateTimeFormatter dateFormatter = DateTimeFormat.forPattern("dd/MM/yyyy").withZoneUTC()
        DateTime startDate = params.startDate ? DateTime.parse(params.startDate, dateFormatter).withTimeAtStartOfDay() : DateTime.now(DateTimeZone.UTC).withTimeAtStartOfDay()
        DateTime endDate = params.startDate ? DateTime.parse(params.endDate, dateFormatter).withTimeAtStartOfDay() : DateTime.now(DateTimeZone.UTC).withTimeAtStartOfDay()
        def stores = StoreSettings.findAllByRetailerIdAndStoreIdIsNotNull(springSecurityService.principal.retailerId)

        [reportType    : ReportType.PROMOTIONS_GROUPED,
         userColumns   : reportingService.getReportColumns(ReportType.PROMOTIONS_GROUPED),
         promotionTypes: PromotionType.values(),
         startDate     : startDate,
         endDate       : endDate,
         stores        : stores]
    }

    def ajaxPromotionsGrouped(SortParams sortParams) {
        sortParams.validateParams(PROMOTIONS_REPORT_SORT_COLUMNS)

        DateTimeFormatter dateFormatter = DateTimeFormat.forPattern("dd/MM/yyyy").withZoneUTC()
        DateTime startDate = params.startDate ? DateTime.parse(params.startDate, dateFormatter).withTimeAtStartOfDay() : DateTime.now(DateTimeZone.UTC).withTimeAtStartOfDay()
        DateTime endDate = params.startDate ? DateTime.parse(params.endDate, dateFormatter).withTimeAtStartOfDay() : DateTime.now(DateTimeZone.UTC).withTimeAtStartOfDay()

        // Validate the promotion type filter if passed in.
        if (!params.promotionTypeFilter?.isAllWhitespace()) {
            try {
                params.promotionTypeFilter = PromotionType.valueOf(params.promotionTypeFilter)
            } catch (Exception e) {
                params.promotionTypeFilter = null;
            }
        } else {
            params.promotionTypeFilter = null;
        }

        Integer storeId
        if (springSecurityService.principal.storeId) {
            storeId = springSecurityService.principal.storeId
        } else {
            storeId = params.storeFilter ? Integer.parseInt(params.storeFilter) : null
        }

        // Find all promotion sales in the date range.
        def promotionSales = reportingService.getPromotionSales(startDate,endDate.plusDays(1), params.descriptionFilter, params.promotionTypeFilter, storeId)

        // Group them by promotion ID.
        def promotionSalesGrouped = promotionSales.groupBy { it.promotionId }

        def finalPromotionSales = []

        // Populating a dummy promotion sale object for all sales of this promotion (because they have summed values).
        promotionSalesGrouped.each { promotionSaleGroup ->
            PromotionSale promotionSale = new PromotionSale(
                    id: promotionSaleGroup.value[0].id,
                    promotionId: promotionSaleGroup.value[0].promotionId,
                    type: promotionSaleGroup.value[0].type,
                    description: promotionSaleGroup.value[0].description,
                    fullPrice: promotionSaleGroup.value.sum { it.fullPrice },
                    discount: promotionSaleGroup.value.sum { it.discount },
                    margin: promotionSaleGroup.value.sum { it.margin } / promotionSaleGroup.value.size(),
                    profit: promotionSaleGroup.value.sum { it.profit },
                    vat: promotionSaleGroup.value.sum { it.vat }
            )

            promotionSale.quantity = promotionSaleGroup.value.size() // Setting a transient value to pass the quantity into the report.

            finalPromotionSales.add(promotionSale)
        }

        // Sort into the required order.
        finalPromotionSales.sort { it."${sortParams.sortColumn}" }

        if (sortParams.sortOrder == "desc") {
            finalPromotionSales = finalPromotionSales.reverse()
        }

        if (params.csv != null && params.csv == "true") {
            def fileName = "PromotionSalesGrouped-" + new Date().format("yyyy_MM_dd_HH_mm_ss") +".csv"
            response.setHeader("Content-Disposition", "attachment; filename=${fileName}")
            response.setHeader("Content-Type", "text/csv;")
            render getPromotionsGrouped(finalPromotionSales)
        }else {
            // Restrict the number of results.
            int totalResults = finalPromotionSales.size()
            finalPromotionSales = sortParams.offset < finalPromotionSales.size() ? finalPromotionSales.subList(sortParams.offset, (sortParams.offset + sortParams.max < finalPromotionSales.size() ? sortParams.offset + sortParams.max : finalPromotionSales.size())) : []
            render (template: "promotionsGroupedResults", model: [promotionSales: finalPromotionSales, userColumns: reportingService.getReportColumns(ReportType.PROMOTIONS_GROUPED), sortParams: sortParams, startDate: startDate, endDate: endDate, totalResults: totalResults])
        }
    }

    def promotions() {
        int promotionId = getIntegerParam(params.promotionId)
        DateTimeFormatter dateFormatter = DateTimeFormat.forPattern("dd/MM/yyyy").withZoneUTC()
        DateTime startDate = params.startDate ? DateTime.parse(params.startDate, dateFormatter).withTimeAtStartOfDay() : DateTime.now(DateTimeZone.UTC).withTimeAtStartOfDay()
        DateTime endDate = params.startDate ? DateTime.parse(params.endDate, dateFormatter).withTimeAtStartOfDay() : DateTime.now(DateTimeZone.UTC).withTimeAtStartOfDay()
        def stores = StoreSettings.findAllByRetailerIdAndStoreIdIsNotNull(springSecurityService.principal.retailerId)

        [reportType : ReportType.PROMOTIONS,
         promotionId: promotionId,
         startDate  : startDate,
         endDate    : endDate,
         userColumns: reportingService.getReportColumns(ReportType.PROMOTIONS),
         stores     : stores]
    }

    def ajaxPromotions(SortParams sortParams) {
        int promotionId = getIntegerParam(params.promotionId)

        sortParams.validateParams(PROMOTIONS_REPORT_SORT_COLUMNS)

        DateTimeFormatter dateFormatter = DateTimeFormat.forPattern("dd/MM/yyyy").withZoneUTC()
        DateTime startDate = params.startDate ? DateTime.parse(params.startDate, dateFormatter).withTimeAtStartOfDay() : DateTime.now(DateTimeZone.UTC).withTimeAtStartOfDay()
        DateTime endDate = params.startDate ? DateTime.parse(params.endDate, dateFormatter).withTimeAtStartOfDay() : DateTime.now(DateTimeZone.UTC).withTimeAtStartOfDay()

        Integer storeId
        if (springSecurityService.principal.storeId) {
            storeId = springSecurityService.principal.storeId
        } else {
            storeId = params.storeFilter ? Integer.parseInt(params.storeFilter) : null
        }

        // Find all promotion sales for this promotion in the date range.
        def promotionSales = reportingService.getPromotionSales(startDate,endDate.plusDays(1), promotionId, sortParams.max, sortParams.offset, sortParams.sortColumn, sortParams.sortOrder, storeId)

        if (params.csv != null && params.csv == "true") {
            def fileName = "Promotions-" + new Date().format("yyyy_MM_dd_HH_mm_ss") +".csv"
            response.setHeader("Content-Disposition", "attachment; filename=${fileName}")
            response.setHeader("Content-Type", "text/csv;")
            render getPromotions(promotionSales)
        }else {
            render (template: "promotionsResults", model: [promotionId: promotionId, promotionSales: promotionSales, userColumns: reportingService.getReportColumns(ReportType.PROMOTIONS), sortParams: sortParams, startDate: startDate, endDate: endDate, totalResults: promotionSales.totalCount])
        }
    }

    def promotion() {
        int promotionSaleId = getIntegerParam(params.promotionSaleId)

        DateTimeFormatter dateFormatter = DateTimeFormat.forPattern("dd/MM/yyyy").withZoneUTC()
        DateTime startDate = params.startDate ? DateTime.parse(params.startDate, dateFormatter).withTimeAtStartOfDay() : DateTime.now(DateTimeZone.UTC).withTimeAtStartOfDay()
        DateTime endDate = params.startDate ? DateTime.parse(params.endDate, dateFormatter).withTimeAtStartOfDay() : DateTime.now(DateTimeZone.UTC).withTimeAtStartOfDay()

        [reportType: ReportType.PROMOTION, promotionSaleId: promotionSaleId, startDate: startDate, endDate: endDate, userColumns: reportingService.getReportColumns(ReportType.PROMOTION)]
    }

    def ajaxPromotion(SortParams sortParams) {
        int promotionSaleId = getIntegerParam(params.promotionSaleId)

        sortParams.validateParams(PROMOTION_REPORT_SORT_COLUMNS)

        // Find all promotion sale products for this promotion sale.
        def promotionSaleProducts = reportingService.getPromotionSaleProducts(promotionSaleId, params.descriptionFilter, sortParams.max, sortParams.offset, sortParams.sortColumn, sortParams.sortOrder)

        if (params.csv != null && params.csv == "true") {
            def fileName = "Promotion-" + new Date().format("yyyy_MM_dd_HH_mm_ss") +".csv"
            response.setHeader("Content-Disposition", "attachment; filename=${fileName}")
            response.setHeader("Content-Type", "text/csv;")
            render getPromotionSaleProduct(promotionSaleProducts)
        }else {
            render (template: "promotionResults", model: [promotionSaleProducts: promotionSaleProducts, userColumns: reportingService.getReportColumns(ReportType.PROMOTION), sortParams: sortParams, totalResults: promotionSaleProducts.totalCount])
        }
    }

    def tillControlEvents() {
        DateTimeFormatter dateFormatter = DateTimeFormat.forPattern("dd/MM/yyyy").withZoneUTC()
        DateTime startDate = params.startDate ? DateTime.parse(params.startDate, dateFormatter).withTimeAtStartOfDay() : DateTime.now(DateTimeZone.UTC).withTimeAtStartOfDay()
        DateTime endDate = params.startDate ? DateTime.parse(params.endDate, dateFormatter).withTimeAtStartOfDay() : DateTime.now(DateTimeZone.UTC).withTimeAtStartOfDay()

        [reportType: ReportType.TILL_CONTROL_EVENTS, userColumns: reportingService.getReportColumns(ReportType.TILL_CONTROL_EVENTS), startDate: startDate, endDate: endDate]
    }

    def ajaxTillControlEvents(SortParams sortParams) {
        sortParams.validateParams(TILL_CONTROL_EVENTS_REPORT_SORT_COLUMNS)

        DateTimeFormatter dateFormatter = DateTimeFormat.forPattern("dd/MM/yyyy").withZoneUTC()
        DateTime startDate = params.startDate ? DateTime.parse(params.startDate, dateFormatter).withTimeAtStartOfDay() : DateTime.now(DateTimeZone.UTC).withTimeAtStartOfDay()
        DateTime endDate = params.startDate ? DateTime.parse(params.endDate, dateFormatter).withTimeAtStartOfDay() : DateTime.now(DateTimeZone.UTC).withTimeAtStartOfDay()

        // Find all till control events in the date range.
        def tillControlEvents = reportingService.getTillControlEvents(startDate, endDate.plusDays(1))

        // Group them by type.
        def tillControlEventsGrouped = tillControlEvents.groupBy { it.type }

        // Sort into the required order.
        Comparator comparator

        if (sortParams.sortColumn == "type") {
            comparator = [compare: { a, b ->
                if (sortParams.sortOrder == "desc") {
                    a.compareTo(b)
                } else {
                    b.compareTo(a)
                }
            }] as Comparator

            tillControlEventsGrouped = tillControlEventsGrouped.sort(comparator)
        } else if (sortParams.sortColumn == "quantity") {
            comparator = [compare: { a, b ->
                if (sortParams.sortOrder == "desc") {
                    if (tillControlEventsGrouped.get(b).size() < tillControlEventsGrouped.get(a).size()) {
                        return -1
                    } else {
                        return 1
                    }
                } else {
                    if (tillControlEventsGrouped.get(a).size() < tillControlEventsGrouped.get(b).size()) {
                        return -1
                    } else {
                        return 1
                    }
                }
            }] as Comparator

            tillControlEventsGrouped = tillControlEventsGrouped.sort(comparator)
        }

        if (params.csv != null && params.csv == "true") {
            def fileName = "TillControlEvents-" + new Date().format("yyyy_MM_dd_HH_mm_ss") + ".csv"
            response.setHeader("Content-Disposition", "attachment; filename=${fileName}")
            response.setHeader("Content-Type", "text/csv;")
            render getTillControlEventsCsv(tillControlEventsGrouped)
        } else {
            // Restrict the number of results.
            int totalResults = tillControlEventsGrouped.size()
            render(template: "tillControlEventsResults", model: [tillControlEvents: tillControlEventsGrouped, userColumns: reportingService.getReportColumns(ReportType.TILL_CONTROL_EVENTS), sortParams: sortParams, startDate: startDate, endDate: endDate, totalResults: totalResults])

        }
    }

    def tillControlEvent() {
        DateTimeFormatter dateFormatter = DateTimeFormat.forPattern("dd/MM/yyyy").withZoneUTC()
        DateTime startDate = params.startDate ? DateTime.parse(params.startDate, dateFormatter) : DateTime.now(DateTimeZone.UTC)
        DateTime endDate = params.startDate ? DateTime.parse(params.endDate, dateFormatter) : DateTime.now(DateTimeZone.UTC)

        TillControlEventType type = null

        try {
            type = TillControlEventType.valueOf(params.type)
        } catch (Exception e) {
            // No action, simply return no results.
        }

        [reportType: ReportType.TILL_CONTROL_EVENT, tillControlEventType: type, startDate: startDate, endDate: endDate, userColumns: reportingService.getReportColumns(ReportType.TILL_CONTROL_EVENT)]
    }

    def ajaxTillControlEvent(SortParams sortParams) {
        TillControlEventType type = null

        try {
            type = TillControlEventType.valueOf(params.tillControlEventType)
        } catch (Exception e) {
            // No action, simply return no results.
        }

        sortParams.validateParams(TILL_CONTROL_EVENT_REPORT_SORT_COLUMNS)

        DateTimeFormatter dateFormatter = DateTimeFormat.forPattern("dd/MM/yyyy").withZoneUTC()
        DateTime startDate = params.startDate ? DateTime.parse(params.startDate, dateFormatter).withTimeAtStartOfDay() : DateTime.now(DateTimeZone.UTC).withTimeAtStartOfDay()
        DateTime endDate = params.startDate ? DateTime.parse(params.endDate, dateFormatter).withTimeAtStartOfDay() : DateTime.now(DateTimeZone.UTC).withTimeAtStartOfDay()

        // Find all till control events in the date range.
        def tillControlEvents = reportingService.getTillControlEvents(startDate,endDate.plusDays(1), type, sortParams.max, sortParams.offset, sortParams.sortColumn, sortParams.sortOrder)

        if (params.csv != null && params.csv == "true") {
            def fileName = "TillControlEvent-" + new Date().format("yyyy_MM_dd_HH_mm_ss") + ".csv"
            response.setHeader("Content-Disposition", "attachment; filename=${fileName}")
            response.setHeader("Content-Type", "text/csv;")
            render getTillControlEventCsv(tillControlEvents)
        }else {
            render (template: "tillControlEventResults", model: [tillControlEvents: tillControlEvents, userColumns: reportingService.getReportColumns(ReportType.TILL_CONTROL_EVENT), sortParams: sortParams, totalResults: tillControlEvents.totalCount])
        }
    }

    // The top level of the main orders report.
    def orders() {
        DateTimeFormatter dateFormatter = DateTimeFormat.forPattern("dd/MM/yyyy").withZoneUTC()
        DateTime startDate = params.startDate ? DateTime.parse(params.startDate, dateFormatter).withTimeAtStartOfDay() : DateTime.now(DateTimeZone.UTC).withTimeAtStartOfDay()
        DateTime endDate = params.startDate ? DateTime.parse(params.endDate, dateFormatter).withTimeAtStartOfDay() : DateTime.now(DateTimeZone.UTC).withTimeAtStartOfDay()

        def suppliers = Supplier.findAllByRetailerId(springSecurityService.principal.retailerId, [sort: "name"])

        [reportType : ReportType.ORDERS,
         suppliers  : suppliers,
         userColumns: reportingService.getReportColumns(ReportType.ORDERS),
         startDate  : startDate,
         endDate    : endDate]
    }

    // The top level of the main orders report.
    def ajaxOrders(SortParams sortParams) {
        sortParams.validateParams(ORDERS_REPORT_SORT_COLUMNS)

        Integer storeId = null
        if (params.storeId && !params.storeId.isEmpty()) {
            storeId = getIntegerParam(params.storeId)
        }

        Integer supplierId = null
        if (params.supplier && !params.supplier.isEmpty()) {
            supplierId = getIntegerParam(params.supplier)
        }

        DateTimeFormatter dateFormatter = DateTimeFormat.forPattern("dd/MM/yyyy").withZoneUTC()
        DateTime startDate = params.startDate ? DateTime.parse(params.startDate, dateFormatter).withTimeAtStartOfDay() : DateTime.now(DateTimeZone.UTC).withTimeAtStartOfDay()
        DateTime endDate = params.startDate ? DateTime.parse(params.endDate, dateFormatter).withTimeAtStartOfDay() : DateTime.now(DateTimeZone.UTC).withTimeAtStartOfDay()

        def orders = productListService.getOrders(storeId, supplierId, startDate, endDate.plusDays(1))

        if (params.csv != null && params.csv == "true") {
            def fileName = "Orders-" + new Date().format("yyyy_MM_dd_HH_mm_ss") + ".csv"
            response.setHeader("Content-Disposition", "attachment; filename=${fileName}")
            response.setHeader("Content-Type", "text/csv;")
            render getOrdersCsv(orders)
        }else {
            render(template: "ordersResults", model: [orders      : orders,
                                                      userColumns : reportingService.getReportColumns(ReportType.ORDERS),
                                                      startDate   : startDate,
                                                      endDate     : endDate,
                                                      sortParams  : sortParams,
                                                      totalResults: orders.totalCount])
        }
    }

    // The bottom level of the main orders report.
    def order() {
        int productListId = getIntegerParam(params.productListId)
        DateTimeFormatter dateFormatter = DateTimeFormat.forPattern("dd/MM/yyyy").withZoneUTC()
        DateTime startDate = params.startDate ? DateTime.parse(params.startDate, dateFormatter) : DateTime.now(DateTimeZone.UTC)
        DateTime endDate = params.startDate ? DateTime.parse(params.endDate, dateFormatter) : DateTime.now(DateTimeZone.UTC)

        def suppliers = Supplier.findAllByRetailerId(springSecurityService.principal.retailerId, [sort: "name"])

        [reportType   : ReportType.ORDER,
         productListId: productListId,
         suppliers    : suppliers,
         startDate    : startDate,
         endDate      : endDate,
         userColumns  : reportingService.getReportColumns(ReportType.ORDER)]
    }

    // The bottom level of the main orders report.
    def ajaxOrder(SortParams sortParams) {
        sortParams.validateParams(ORDER_REPORT_SORT_COLUMNS)
        Integer productListId = getIntegerParam(params.productListId)

        Integer storeId = null
        if (params.storeId && !params.storeId.isEmpty()) {
            storeId = getIntegerParam(params.storeId)
        }

        Integer supplierId = null
        if (params.supplier && !params.supplier.isEmpty()) {
            supplierId = getIntegerParam(params.supplier)
        }

        DateTimeFormatter dateFormatter = DateTimeFormat.forPattern("dd/MM/yyyy").withZoneUTC()
        DateTime startDate = params.startDate ? DateTime.parse(params.startDate, dateFormatter).withTimeAtStartOfDay() : DateTime.now(DateTimeZone.UTC).withTimeAtStartOfDay()
        DateTime endDate = params.startDate ? DateTime.parse(params.endDate, dateFormatter).withTimeAtStartOfDay() : DateTime.now(DateTimeZone.UTC).withTimeAtStartOfDay()

        def orders = productListService.getOrder(productListId, storeId, supplierId, startDate, endDate.plusDays(1))


        //sort pack lines based on sort column (sku / description / packQuantity / orderedQuantity / lineValue)
        def packLines = []
        packLines = orders[0]?.totalPackLines
        if (sortParams.sortColumn == "description") {
            packLines.sort { it.productListItem?.productVariant?.product?.description }
        } else if (sortParams.sortColumn == "packQuantity") {
            packLines.sort { it.pack?.quantity }
        } else if (sortParams.sortColumn == "orderedQuantity") {
            packLines.sort { it.pack?.quantity.multiply(it.quantity) }
        } else if (sortParams.sortColumn == "lineValue") {
            packLines.sort { it.pack?.price?.multiply(it.quantity) }
        } else { //If no sort column found then by default sort by sku
            packLines.sort { it.productListItem?.productVariant?.sku }
        }

        if (sortParams.sortOrder == "desc") {
            packLines = packLines.reverse()
        }

        if (params.csv != null && params.csv == "true") {
            def fileName = "Order-" + new Date().format("yyyy_MM_dd_HH_mm_ss") + ".csv"
            response.setHeader("Content-Disposition", "attachment; filename=${fileName}")
            response.setHeader("Content-Type", "text/csv;")
            render getOrderCsv(packLines)
        } else {
            render(template: "orderResults", model: [orders      : packLines,
                                                     userColumns : reportingService.getReportColumns(ReportType.ORDER),
                                                     startDate   : startDate,
                                                     endDate     : endDate,
                                                     sortParams  : sortParams,
                                                     totalResults: orders.size()])
        }
    }


    def paypointSales() {
        DateTimeFormatter dateFormatter = DateTimeFormat.forPattern("dd/MM/yyyy").withZoneUTC()
        DateTime startDate = params.startDate ? DateTime.parse(params.startDate, dateFormatter) : DateTime.now(DateTimeZone.UTC)
        DateTime endDate = params.startDate ? DateTime.parse(params.endDate, dateFormatter) : DateTime.now(DateTimeZone.UTC)

        [reportType : ReportType.PAYPOINT_SALES,
         startDate  : startDate,
         endDate    : endDate,
         userColumns: reportingService.getReportColumns(ReportType.PAYPOINT_SALES)]
    }

    def ajaxPayPointSales(SortParams sortParams) {
        sortParams.validateParams(PAYPOINT_SALE_REPORT_SORT_COLUMNS)

        DateTimeFormatter dateFormatter = DateTimeFormat.forPattern("dd/MM/yyyy").withZoneUTC()
        DateTime startDate = params.startDate ? DateTime.parse(params.startDate, dateFormatter).withTimeAtStartOfDay() : DateTime.now(DateTimeZone.UTC).withTimeAtStartOfDay()
        DateTime endDate = params.startDate ? DateTime.parse(params.endDate, dateFormatter).withTimeAtStartOfDay() : DateTime.now(DateTimeZone.UTC).withTimeAtStartOfDay()

        String description = params.descriptionFilter ? ("%" + params.descriptionFilter + "%") : null
        Integer storeId = null
        if (params.storeFilter && !params.storeFilter.isEmpty()) {
            StoreSettings storeSettings = StoreSettings.findByStoreId(Integer.parseInt(params.storeFilter))
            storeId = storeSettings ? storeSettings.id : -1
        }
        String status = null
        if (params.statusFilter && !params.statusFilter.isEmpty()) {
            status = params.statusFilter == "Success" ? "SUCCESS" : "FAILURE"
        }

        if (params.csv != null && params.csv == "true") {
            if (params.standard != null && params.standard == "true") {
                def sales = reportingService.getPayPointSales(DateTime.now(DateTimeZone.UTC).minusDays(7).withTimeAtStartOfDay(), DateTime.now(DateTimeZone.UTC).plusDays(1).withTimeAtStartOfDay(), storeId, null, null, Integer.MAX_VALUE, 0, 'transactionDate', 'ASC')

                def fileName = "PayPointWeeklyReport-" + params.storeFilter + "-" + new Date().format("yyyy_MM_dd_HH_mm_ss") + ".csv"
                response.setHeader("Content-Disposition", "attachment; filename=${fileName}")
                response.setHeader("Content-Type", "text/csv;")

                render getPayPointWeeklyReportCsv(sales)
            } else {
                def sales = reportingService.getPayPointSales(startDate, endDate.plusDays(1), storeId, status, description, Integer.MAX_VALUE, 0, sortParams.sortColumn, sortParams.sortOrder)
                def fileName = "PayPoint-Filtered-" + new Date().format("yyyy_MM_dd_HH_mm_ss") + ".csv"
                response.setHeader("Content-Disposition", "attachment; filename=${fileName}")
                response.setHeader("Content-Type", "text/csv;")

                render getPayPointSalesCsv(sales)
            }
        } else {
            def sales = reportingService.getPayPointSales(startDate, endDate.plusDays(1), storeId, status, description, sortParams.max, sortParams.offset, sortParams.sortColumn, sortParams.sortOrder)

            render(template: "paypointSalesResults",
                    model: [sales       : sales,
                            totalResults: sales.totalCount,
                            userColumns : reportingService.getReportColumns(ReportType.PAYPOINT_SALES),
                            sortParams  : sortParams]
            )
        }
    }

    def ajaxSaveReportColumns() {
        try {
            if (params.reportColumns && params.reportType) {
                def userReportColumns = new JsonSlurper().parseText(params.reportColumns)
                def reportType = ReportType.valueOf(params.reportType)

                def reportColumns = reportingService.getReportColumns(reportType)

                if (!reportColumns) {
                    reportColumns = new ReportColumns(userId: springSecurityService.principal.id, reportType: reportType)
                }

                userReportColumns?.each { userReportColumn ->
                    if (reportColumns?.columns?.find { it.column == userReportColumn.key }) {
                        reportColumns?.columns?.find { it.column == userReportColumn.key }?.enabled = userReportColumn.value
                    } else {
                        reportColumns.addToColumns(new ReportColumn(column: userReportColumn.key, enabled: userReportColumn.value))
                    }
                }

                reportingService.saveReportColumns(reportColumns)

                render (status: 200)
            }
        } catch (Exception e) {
            e.printStackTrace()
            render (status: 500, text: "An error occurred saving your report column preferences.")
        }
    }

    private String getSalesByCategoryCsv(List<Sale> sales) {
        StringBuilder stringBuilder = new StringBuilder()
        stringBuilder.append("Description,Total Quantity,Refund Quantity,Avg Cost Price,Avg Sales Price,Total Sales,VAT Amount,Avg Margin\n")
        sales?.each {
            stringBuilder.append(it.productDescription?.replace("'", "\\'"))
            stringBuilder.append(",")
            stringBuilder.append(it.quantity + it.refundQuantity)
            stringBuilder.append(",")
            stringBuilder.append(it.refundQuantity)
            stringBuilder.append(",")
            stringBuilder.append("£" + it.avgCostPrice?.setScale(2))
            stringBuilder.append(",")
            stringBuilder.append("£" + it.avgRetailPrice?.setScale(2))
            stringBuilder.append(",")
            stringBuilder.append("£" + it.retailPrice?.setScale(2))
            stringBuilder.append(",")
            stringBuilder.append("£" + it.vatAmount?.setScale(2))
            stringBuilder.append(",")
            stringBuilder.append(it.avgMargin?.setScale(2) + "%")
            stringBuilder.append("\n")
        }
        return stringBuilder.toString()
    }

    private String getSalesByProductCsv(List<Sale> sales) {
        StringBuilder stringBuilder = new StringBuilder()
        stringBuilder.append("Description,Quantity Sold,Cost Price,Net Total,VAT Amount,Profit,Margin,User,Timestamp\n")
        sales?.each {
            stringBuilder.append(it.productItemCode?.replace("'", "\\'") + " - " + it.productDescription?.replace("'", "\\'") + " - " + it.productUnitSize?.replace("'", "\\'"))
            stringBuilder.append(",")
            stringBuilder.append(it.quantity)
            stringBuilder.append(",")
            stringBuilder.append("£" + it.costPrice?.setScale(2))
            stringBuilder.append(",")
            stringBuilder.append("£" + it.retailPrice.subtract(it.vatAmount)?.setScale(2))
            stringBuilder.append(",")
            stringBuilder.append("£" + it.vatAmount?.setScale(2))
            stringBuilder.append(",")
            stringBuilder.append("£" + it.retailPrice.subtract(it.costPrice).subtract(it.vatAmount)?.setScale(2))
            stringBuilder.append(",")
            stringBuilder.append(it.margin?.setScale(2) + "%")
            stringBuilder.append(",")
            stringBuilder.append(it.usersName)
            stringBuilder.append(",")
            stringBuilder.append(it.dateCreated?.format("dd/MM/yy HH:mm:ss"))
            stringBuilder.append("\n")
        }
        return stringBuilder.toString()
    }

    private String getSalesCsv(List<Sale> sales) {
        StringBuilder stringBuilder = new StringBuilder()

        stringBuilder.append("Item Code,Description,Total Quantity,Avg Cost Price,Avg Sales Price,Total Sales,VAT Amount,Avg Margin\n")

        sales?.each {
            stringBuilder.append(it.productItemCode?.replace("'", "\\'"))
            stringBuilder.append(",")
            stringBuilder.append(it.productDescription?.replace("'", "\\'"))
            stringBuilder.append(",")
            stringBuilder.append(it.quantity)
            stringBuilder.append(",")
            stringBuilder.append("£" + it.avgCostPrice?.setScale(2))
            stringBuilder.append(",")
            stringBuilder.append("£" + it.avgRetailPrice?.setScale(2))
            stringBuilder.append(",")
            stringBuilder.append("£" + it.retailPrice?.setScale(2))
            stringBuilder.append(",")
            stringBuilder.append("£" + it.vatAmount?.setScale(2))
            stringBuilder.append(",")
            stringBuilder.append(it.avgMargin?.setScale(2) + "%")
            stringBuilder.append("\n")
        }

        return stringBuilder.toString()
    }

    private String getPayPointWeeklyReportCsv(List<PayPointSale> sales) {
        StringBuilder stringBuilder = new StringBuilder()

        stringBuilder.append("Date,Time,PP TID,PP TXN ID,Description,Amount,Status\n")

        sales?.each {
            stringBuilder.append(it.getTransactionDate().toString("dd/MM/yyyy"))
            stringBuilder.append(",")
            stringBuilder.append(it.getTransactionDate().toString("HH:mm:ss"))
            stringBuilder.append(",")
            stringBuilder.append(it.getTerminalId())
            stringBuilder.append(",")
            stringBuilder.append(it.getPpTransactionId())
            stringBuilder.append(",")
            stringBuilder.append(it.getDescription())
            stringBuilder.append(",")
            stringBuilder.append("£").append(it.getValue().toString())
            stringBuilder.append(",")
            stringBuilder.append(it.getStatus() == "SUCCESS" ? "Success" : "Failure")
            stringBuilder.append("\n")
        }

        return stringBuilder.toString()
    }

    private String getPayPointSalesCsv(List<PayPointSale> sales) {
        StringBuilder stringBuilder = new StringBuilder()
        List<String> enabledCols = new ArrayList<>();

        def columns = reportingService.getReportColumns(ReportType.PAYPOINT_SALES)

        if (columns.columns?.find { it.column == "storeId" }?.enabled) {
            stringBuilder.append("Store Id").append(",")
            enabledCols.add("storeId")
        }
        if (columns.columns?.find { it.column == "wlTransactionId" }?.enabled) {
            stringBuilder.append("Txn Id").append(",")
            enabledCols.add("wlTransactionId")
        }
        if (columns.columns?.find { it.column == "ppTransactionId" }?.enabled) {
            stringBuilder.append("PP Txn Id").append(",")
            enabledCols.add("ppTransactionId")
        }
        if (columns.columns?.find { it.column == "terminalId" }?.enabled) {
            stringBuilder.append("Terminal Id").append(",")
            enabledCols.add("terminalId")
        }
        if (columns.columns?.find { it.column == "description" }?.enabled) {
            stringBuilder.append("Description").append(",")
            enabledCols.add("description")
        }
        if (columns.columns?.find { it.column == "type" }?.enabled) {
            stringBuilder.append("Type").append(",")
            enabledCols.add("type")
        }
        if (columns.columns?.find { it.column == "value" }?.enabled) {
            stringBuilder.append("Value").append(",")
            enabledCols.add("value")
        }
        if (columns.columns?.find { it.column == "status" }?.enabled) {
            stringBuilder.append("Status").append(",")
            enabledCols.add("status")
        }
        if (columns.columns?.find { it.column == "transactionDate" }?.enabled) {
            stringBuilder.append("Transaction Date").append(",")
            enabledCols.add("transactionDate")
        }

        if (stringBuilder.length() > 0) {
            // all columns aren't disabled, continue
            stringBuilder.replace(stringBuilder.length() - 1, stringBuilder.length(), "\n")

            sales.each {
                for (String col : enabledCols) {
                    switch (col) {
                        case "storeId":
                            stringBuilder.append(it.visibleStoreId).append(",")
                            break
                        case "wlTransactionId":
                            stringBuilder.append(it.wlTransactionId).append(",")
                            break
                        case "ppTransactionId":
                            stringBuilder.append(it.ppTransactionId).append(",")
                            break
                        case "terminalId":
                            stringBuilder.append(it.terminalId).append(",")
                            break
                        case "description":
                            stringBuilder.append(it.description).append(",")
                            break
                        case "type":
                            stringBuilder.append(it.type).append(",")
                            break
                        case "value":
                            stringBuilder.append("£" + it.value.toString()).append(",")
                            break
                        case "status":
                            stringBuilder.append(it.getStatus() == "SUCCESS" ? "Success" : "Failure").append(",")
                            break
                        case "transactionDate":
                            stringBuilder.append(it.transactionDate.toString("dd/MM/yyyy HH:mm:ss")).append(",")
                            break;
                    }
                }
                stringBuilder.replace(stringBuilder.length() - 1, stringBuilder.length(), "\n")
            }
        }
        return stringBuilder.toString()
    }

    private String getOrderCsv(List<PackLine> packLines) {
        StringBuilder stringBuilder = new StringBuilder()
        stringBuilder.append("Product SKU,Description,Ordered Quantity,Pack Quantity,Line Value\n")
        packLines?.each {
            stringBuilder.append(it.productListItem?.productVariant?.sku)
            stringBuilder.append(",")
            stringBuilder.append(it.productListItem?.productVariant?.product?.description)
            stringBuilder.append(",")
            stringBuilder.append(it.pack?.quantity.multiply(it.quantity))
            stringBuilder.append(",")
            stringBuilder.append(it.pack?.quantity)
            stringBuilder.append(",")
            //in reports line value represent in dollars ($)
            stringBuilder.append("£" + (it.pack?.price?.multiply(it.quantity)))
            stringBuilder.append("\n")
        }
        return stringBuilder.toString()
    }

    private String getOrdersCsv(List<ProductList> productListList) {
        StringBuilder stringBuilder = new StringBuilder()
        stringBuilder.append("Order ID,Store ID,Status,Date Completed,Supplier Name,Quantity,Value of Order\n")
        productListList?.each {
            stringBuilder.append(it.getOrderId())
            stringBuilder.append(",")
            stringBuilder.append(it.getStoreId())
            stringBuilder.append(",")
            stringBuilder.append(it.status)
            stringBuilder.append(",")
            stringBuilder.append(it.dateCompleted?.toString("dd/MM/yyyy HH:mm:ss"))
            stringBuilder.append(",")
            stringBuilder.append(it.supplierReference)
            stringBuilder.append(",")
            stringBuilder.append(it.totalQuantity)
            stringBuilder.append(",")
            //in reports total value represent in dollars ($)
            stringBuilder.append("£" + it.totalValue)
            stringBuilder.append("\n")
        }
        return stringBuilder.toString()
    }

    private String getTillControlEventsCsv(TreeMap<TillControlEventType, ArrayList> tillControlEventMap) {
        StringBuilder stringBuilder = new StringBuilder()
        //load resource bundle to get value from messages properties file
        ResourceBundle bundle = ResourceBundle.getBundle("messages", Locale.US);
        stringBuilder.append("Type,Total Quantity\n")
        for (Map.Entry<TillControlEventType, ArrayList> set : tillControlEventMap.entrySet()) {
            //build till event
            String tillEventType = bundle.getString("TillControlEventType." + set.getKey()) != null ?
                    bundle.getString("TillControlEventType." + set.getKey()) : "TillControlEventType." + set.getKey()
            stringBuilder.append(tillEventType)
            stringBuilder.append(",")
            stringBuilder.append(set.getValue() != null ? set.getValue().size(): 0)
            stringBuilder.append("\n")
        }
        return stringBuilder.toString()
    }

    private String getTillControlEventCsv(List<TillControlEvent> tillControlEventList) {
        //load resource bundle to get value from messages properties file
        ResourceBundle bundle = ResourceBundle.getBundle("messages", Locale.US)
        StringBuilder stringBuilder = new StringBuilder()
        stringBuilder.append("Type,User,Reason,Date,Amount\n")
        tillControlEventList?.each {
            String type  = bundle.getString("TillControlEventType." + it.type) != null ?
                    bundle.getString("TillControlEventType." + it.type) : "TillControlEventType." + it.type
            stringBuilder.append(type.toString()?.replace("'", "\\'"))
            stringBuilder.append(",")
            stringBuilder.append(it.usersName?.replace("'", "\\'"))
            stringBuilder.append(",")
            //build till event reason since
            String reason = it.reason
            if (it.reason == null){
                reason = "N/A";
            } else if (it.type.name() == "CUSTOMER_REFUSAL"){
                reason = bundle.getString("CustomerRefusalReason." + it.reason) != null ?
                        bundle.getString("CustomerRefusalReason." + it.reason) : "CustomerRefusalReason." + it.reason
            } else if (it.type.name() == "REFUND"){
                reason = bundle.getString("RefundReason." + it.reason) != null ?
                        bundle.getString("RefundReason." + it.reason) : "RefundReason." +it.reason
            } else if (it.type.name() == "MARKDOWN"){
                reason = bundle.getString("MarkdownReason." + it.reason) != null ?
                        bundle.getString("MarkdownReason." + it.reason) : "MarkdownReason." + it.reason
            } else if (it.type.name() == "LINE_VOID"){
                reason = bundle.getString("LineVoidReason." + it.reason) != null ?
                        bundle.getString("LineVoidReason." + it.reason) : "LineVoidReason." + it.reason
            } else if (it.type.name() == "PAID_OUT"){
                reason = bundle.getString("PaidOutReason." + it.reason) != null ?
                        bundle.getString("PaidOutReason." + it.reason) : "PaidOutReason." +  it.reason
            } else {
                reason = it.reason
            }
            stringBuilder.append(reason?.replace("'", "\\'"))
            stringBuilder.append(",")
            stringBuilder.append(it.dateCreated?.toString("dd/MM/yy HH:mm:ss"))
            stringBuilder.append(",")
            //in reports amount represent in dollars ($)
            stringBuilder.append(it.amount != null ? "£" + it.amount : "N/A")
            stringBuilder.append("\n")
        }
        return stringBuilder.toString()
    }

    private String getPromotionsGrouped(List<PromotionSale> promotionSales) {
        StringBuilder stringBuilder = new StringBuilder()
        stringBuilder.append("Description,Type,Quantity,Full Price,Discount,Profit,Margin,VAT\n")
        promotionSales?.each {
            stringBuilder.append(it.description?.replace("'", "\\'"))
            stringBuilder.append(",")
            stringBuilder.append(it.type?.getFriendlyName()?.replace("'", "\\'"))
            stringBuilder.append(",")
            stringBuilder.append(it.quantity)
            stringBuilder.append(",")
            stringBuilder.append("£" + (it.fullPrice != null ? it.fullPrice.setScale(2) : BigDecimal.ZERO))
            stringBuilder.append(",")
            stringBuilder.append("£" + (it.discount != null ? it.discount.setScale(2) : BigDecimal.ZERO))
            stringBuilder.append(",")
            stringBuilder.append("£" + (it.profit != null ? it.profit.setScale(2) : BigDecimal.ZERO))
            stringBuilder.append(",")
            stringBuilder.append(it.margin?.setScale(2) + "%")
            stringBuilder.append(",")
            stringBuilder.append("£" + (it.vat != null ? it.vat.setScale(2)  : BigDecimal.ZERO))
            stringBuilder.append("\n")
        }
        return stringBuilder.toString()
    }

    private String getPromotions(List<PromotionSale> promotionSales) {
        StringBuilder stringBuilder = new StringBuilder()
        stringBuilder.append("Description,Type,Full Price,Discount,Profit,Margin,VAT,Date\n")
        promotionSales?.each {
            stringBuilder.append(it.description?.replace("'", "\\'"))
            stringBuilder.append(",")
            stringBuilder.append(it.type?.getFriendlyName()?.replace("'", "\\'"))
            stringBuilder.append(",")
            stringBuilder.append("£" + (it.fullPrice != null ? it.fullPrice.setScale(2) : BigDecimal.ZERO))
            stringBuilder.append(",")
            stringBuilder.append("£" + (it.discount != null ? it.discount.setScale(2) : BigDecimal.ZERO))
            stringBuilder.append(",")
            stringBuilder.append("£" + (it.profit != null ? it.profit.setScale(2) : BigDecimal.ZERO))
            stringBuilder.append(",")
            stringBuilder.append(it.margin?.setScale(2) + "%")
            stringBuilder.append(",")
            stringBuilder.append("£" + (it.vat != null ? it.vat.setScale(2) : BigDecimal.ZERO))
            stringBuilder.append(",")
            stringBuilder.append(it.dateCreated?.toString("dd/MM/yyyy HH:mm"))
            stringBuilder.append("\n")
        }
        return stringBuilder.toString()
    }

    private String getPromotionSaleProduct(List<PromotionSaleProduct> promotionSaleProductList) {
        StringBuilder stringBuilder = new StringBuilder()
        stringBuilder.append("Item Code,Description,Cost Price,Full Price,Full Price Profit,Full Price Margin,Discount,Discounted Price," +
                "Discounted Profit,Discounted Margin,VAT\n")
        promotionSaleProductList?.each {
            stringBuilder.append(it.itemCode?.replace("'", "\\'"))
            stringBuilder.append(",")
            stringBuilder.append(it.description?.replace("'", "\\'"))
            stringBuilder.append(",")
            stringBuilder.append("£" + (it.costPrice != null ? it.costPrice.setScale(2) : BigDecimal.ZERO))
            stringBuilder.append(",")
            stringBuilder.append("£" +  (it.fullPrice != null ? it.fullPrice.setScale(2) : BigDecimal.ZERO))
            stringBuilder.append(",")
            stringBuilder.append("£" + (it.fullPriceProfit != null ? it.fullPriceProfit.setScale(2) : BigDecimal.ZERO))
            stringBuilder.append(",")
            stringBuilder.append(it.fullPriceMargin?.setScale(2) + "%")
            stringBuilder.append(",")
            stringBuilder.append("£" +  (it.discount != null ? it.discount.setScale(2) : BigDecimal.ZERO))
            stringBuilder.append(",")
            stringBuilder.append("£" + (it.discountedPrice != null ? it.discountedPrice.setScale(2) : BigDecimal.ZERO))
            stringBuilder.append(",")
            stringBuilder.append("£" + (it.discountedProfit != null ? it.discountedProfit.setScale(2) : BigDecimal.ZERO))
            stringBuilder.append(",")
            stringBuilder.append(it.discountedMargin?.setScale(2) + "%")
            stringBuilder.append(",")
            stringBuilder.append("£" + (it.vat != null ? it.vat.setScale(2) : BigDecimal.ZERO))
            stringBuilder.append("\n")
        }
        return stringBuilder.toString()
    }

    /**
     * Perform some sense checking on our input parameters.
     *
     * @param productId
     * @return
     */
    private static int getIntegerParam(paramValue) {
        if (!paramValue || !paramValue.isNumber() || paramValue.length() > 9) {
            return -1
        }

        return Integer.parseInt(paramValue)
    }

    /**
     * Perform some sense checking on our input parameters.
     *
     * @param max
     * @return
     */
    private static int getMax(int max) {
        if (!max || max > 500) {
            return 50
        }

        return max
    }

    /**
     * Perform some sense checking on our input parameters.
     *
     * @param offset
     * @return
     */
    private static int getOffset(int offset) {
        if (!offset || offset > 5000) {
            return 0
        }

        return offset
    }

    /**
     * Perform some sense checking on our input parameters.
     *
     * @param sortColumn
     * @return
     */
    private static String getSortColumn(availableColumns, sortColumn) {
        if (!sortColumn) {
            return "id"
        }

        if (!availableColumns.contains(sortColumn)) {
            return "id"
        }

        return sortColumn
    }

    /**
     * Perform some sense checking on our input parameters.
     *
     * @param sortOrder
     * @return
     */
    private static String getSortOrder(sortOrder) {
        if (!sortOrder) {
            return "asc"
        }

        if (!sortOrder.equals("asc") && !sortOrder.equals("desc")) {
            return "asc"
        }

        return sortOrder
    }
}
