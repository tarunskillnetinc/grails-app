package uk.co.wonderlane.wlpos

import groovy.json.JsonSlurper
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import org.joda.time.format.DateTimeFormat
import org.joda.time.format.DateTimeFormatter
import uk.co.wonderlane.wlpos.enums.PromotionType
import uk.co.wonderlane.wlpos.enums.TenderMovementType
import uk.co.wonderlane.wlpos.enums.TenderType
import uk.co.wonderlane.wlpos.enums.TillControlEventType
import uk.co.wonderlane.wlpos.enums.wlim.ProductListStatus
import uk.co.wonderlane.wlpos.enums.wlim.ProductListType
import uk.co.wonderlane.wlpos.reporting.*

import java.math.RoundingMode

class ReportingController {

    def reportingService
    def supplierService
    def productListService
    def storeService
    def springSecurityService

    private static final SALES_REPORT_CATEGORY_SORT_COLUMNS = ["description", "quantity", "avgCostPrice", "avgRetailPrice", "retailPrice", "vatAmount", "avgMargin"]
    private static final SALES_REPORT_PRODUCT_SORT_COLUMNS = ["usersName", "category", "description", "quantity", "costPrice", "netTotal", "vatAmount", "profit", "margin", "dateCreated"]
    private static final SALES_REPORT_SORT_COLUMNS = ["description", "quantity", "avgCostPrice", "avgRetailPrice", "retailPrice", "vatAmount", "avgMargin"]
    private static final PROMOTIONS_REPORT_SORT_COLUMNS = ["type", "description", "quantity", "fullPrice", "discount", "margin", "profit", "vat", "dateCreated"]
    private static final PROMOTION_REPORT_SORT_COLUMNS = ["itemCode", "description", "costPrice", "fullPrice", "fullPriceMargin", "fullPriceProfit", "discount", "discountedPrice", "discountedMargin", "discountedProfit", "vat"]
    private static final TILL_CONTROL_EVENTS_REPORT_SORT_COLUMNS = ["type", "quantity"]
    private static final TILL_CONTROL_EVENT_REPORT_SORT_COLUMNS = ["dateCreated", "type", "tillId", "usersName", "reason", "amount"]
    private static final PAYPOINT_SALE_REPORT_SORT_COLUMNS = ["transactionDate", "storeId", "wlTransactionId", "ppTransactionId", "terminalId", "description", "type", "value", "status"]
    private static final ORDERS_REPORT_SORT_COLUMNS = ["orderId", "storeId", "status", "dateCompleted", "supplierName", "numberOfItems", "value"]
    private static final ORDER_REPORT_SORT_COLUMNS = ["sku", "description", "orderedQuantity", "packQuantity", "lineValue"]
    private static final DELIVERIES_REPORT_SORT_COLUMNS = ["deliveryId", "storeId", "status", "deliveryDate", "supplierName", "numberOfItems", "totalCost"]
    private static final DELIVERY_REPORT_SORT_COLUMNS = ["sku", "description", "itemQuantity", "totalCost"]
    private static final DELIVERY_PACK_REPORT_SORT_COLUMNS = ["description", "price", "packCost", "packSize", "deliveryQuantity", "totalQuantity", "totalSellValue"]
    private static final PRODUCT_LISTS_REPORT_SORT_COLUMNS = ["productListId", "storeId", "type", "status", "startDate", "numberOfItems"]
    private static final PRODUCT_LIST_REPORT_SORT_COLUMNS = ["sku", "description", "itemQuantity", "totalCost"]
    private static final TENDER_MOVEMENT_REPORT_SORT_COLUMNS = ["timestamp", "storeId", "fromLocation", "toLocation", "amount", "type", "reason", "userName"]

    def index() {

    }

    // The top level of the main sales report.
    def salesDepartment() {
        DateTimeFormatter dateFormatter = DateTimeFormat.forPattern("dd/MM/yyyy").withZoneUTC()
        DateTime startDate = params.startDate ? DateTime.parse(params.startDate, dateFormatter).withTimeAtStartOfDay() : DateTime.now(DateTimeZone.UTC).withTimeAtStartOfDay()
        DateTime endDate = params.startDate ? DateTime.parse(params.endDate, dateFormatter).withTimeAtStartOfDay() : DateTime.now(DateTimeZone.UTC).withTimeAtStartOfDay()
        def stores = storeService.getStores(springSecurityService.principal.retailerId)

        [reportType : ReportType.SALES_DEPARTMENT,
         userColumns: reportingService.getReportColumns(ReportType.SALES_DEPARTMENT),
         startDate  : startDate,
         endDate    : endDate,
         stores     : stores]
    }

    // The top level of the main sales report.
    def ajaxSalesDepartment(SortParams sortParams) {
        sortParams.validateParams(SALES_REPORT_CATEGORY_SORT_COLUMNS)

        DateTimeFormatter dateFormatter = DateTimeFormat.forPattern("dd/MM/yyyy").withZoneUTC()
        DateTime startDate = params.startDate ? DateTime.parse(params.startDate, dateFormatter).withTimeAtStartOfDay() : DateTime.now(DateTimeZone.UTC).withTimeAtStartOfDay()
        DateTime endDate = params.endDate ? DateTime.parse(params.endDate, dateFormatter).withTimeAtStartOfDay() : DateTime.now(DateTimeZone.UTC).withTimeAtStartOfDay()

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
                    costPrice: salesGroup.value.sum { it.quantity > 0 ? it.costPrice.setScale(2) : BigDecimal.ZERO.setScale(2) },
                    retailPrice: salesGroup.value.sum { it.quantity > 0 ? it.retailPrice.setScale(2) : BigDecimal.ZERO.setScale(2) },
                    vatAmount: salesGroup.value.sum { it.quantity > 0 ? it.vatAmount.setScale(2) : BigDecimal.ZERO.setScale(2) },
                    margin: salesGroup.value.sum { it.quantity > 0 ? it.margin.setScale(2) : BigDecimal.ZERO.setScale(2) },
                    productDescription: salesGroup.value[0].salesCategories.find { sc -> sc.categoryId == salesGroup.key }.categoryDescription,
                    productUnitSize: ""
            )

            groupedSale.quantity = BigDecimal.ZERO
            groupedSale.refundQuantity = BigDecimal.ZERO

            salesGroup.value.each {
                Product product = Product.findById(it?.productId)
                if (it.quantity < 0) {
                    groupedSale.refundQuantity -= product.weightedItem ? -1 : it?.quantity.intValue()
                } else {
                    groupedSale.quantity += product.weightedItem ? 1 : it?.quantity.intValue()
                }
            }

            // Also add a dummy category object so we know which category this is in the view.
            groupedSale.addToSalesCategories(new SaleCategory(categoryId: (int) salesGroup.key))

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
            def fileName = "SalesByDepartment-" + new Date().format("yyyy_MM_dd_HH_mm_ss") + ".csv"
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
        def stores = storeService.getStores(springSecurityService.principal.retailerId)

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
        DateTime endDate = params.endDate ? DateTime.parse(params.endDate, dateFormatter).withTimeAtStartOfDay() : DateTime.now(DateTimeZone.UTC).withTimeAtStartOfDay()

        Integer storeId
        if (springSecurityService.principal.storeId) {
            storeId = springSecurityService.principal.storeId
        } else {
            storeId = params.storeFilter ? Integer.parseInt(params.storeFilter) : null
        }

        // Find all sales involving this category in the date range.
        def sales = reportingService.getSalesForCategory(categoryId, startDate, endDate.plusDays(1), storeId)

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
                    BigDecimal initQuantity = groupedProductSale.value[0].quantity

                    groupedProductSale.value[0].costPrice = groupedProductSale.value.sum { it.quantity > 0 ? it.costPrice : BigDecimal.ZERO }.setScale(2)
                    groupedProductSale.value[0].retailPrice = groupedProductSale.value.sum { it.quantity > 0 ? it.retailPrice : BigDecimal.ZERO }.setScale(2)
                    groupedProductSale.value[0].vatAmount = groupedProductSale.value.sum { it.quantity > 0 ? it.vatAmount : BigDecimal.ZERO }.setScale(2)
                    groupedProductSale.value[0].margin = groupedProductSale.value.sum { it.quantity > 0 ? it.margin : BigDecimal.ZERO }.setScale(2)

                    groupedProductSale.value[0].quantity = BigDecimal.ZERO
                    groupedProductSale.value[0].refundQuantity = BigDecimal.ZERO

                    groupedProductSale.value.each {
                        if (it.quantity < 0) {
                            groupedProductSale.value[0].refundQuantity -= it.quantity
                        } else {
                            groupedProductSale.value[0].quantity += it.quantity
                        }
                    }
                    initQuantity < 0 ? (groupedProductSale.value[0].refundQuantity -= initQuantity) : (groupedProductSale.value[0].quantity += initQuantity)

                    finalSales.add(groupedProductSale.value[0])
                }
            } else {
                if (!params.descriptionFilter || salesGroup.value[0].salesCategories.find { sc -> sc.categoryId == salesGroup.key }.categoryDescription.toLowerCase().contains(params.descriptionFilter?.toLowerCase())) {
                    Sale groupedSale = new Sale(
                            costPrice: salesGroup.value.sum { it.quantity > 0 ? it.costPrice : BigDecimal.ZERO }.setScale(2),
                            retailPrice: salesGroup.value.sum { it.quantity > 0 ? it.retailPrice : BigDecimal.ZERO }.setScale(2),
                            vatAmount: salesGroup.value.sum { it.quantity > 0 ? it.vatAmount : BigDecimal.ZERO }.setScale(2),
                            margin: salesGroup.value.sum { it.quantity > 0 ? it.margin : BigDecimal.ZERO }.setScale(2),
                            productDescription: salesGroup.value[0].salesCategories.find { sc -> sc.categoryId == salesGroup.key }.categoryDescription,
                            productUnitSize: "",
                            "refundQuantity": BigDecimal.ZERO,
                            "quantity": BigDecimal.ZERO
                    )

                    salesGroup.value.each {
                        if (it.quantity < 0) {
                            groupedSale.refundQuantity -= it.quantity
                        } else {
                            groupedSale.quantity += it.quantity
                        }
                    }

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
            def fileName = "SalesByCategory-" + new Date().format("yyyy_MM_dd_HH_mm_ss") + ".csv"
            response.setHeader("Content-Disposition", "attachment; filename=${fileName}")
            response.setHeader("Content-Type", "text/csv;")

            render getSalesByCategoryCsv(finalSales)
        } else {
            // Restrict the number of results.
            int totalResults = finalSales.size()
            finalSales = sortParams.offset < finalSales.size() ? finalSales.subList(sortParams.offset, (sortParams.offset + sortParams.max < finalSales.size() ? sortParams.offset + sortParams.max : finalSales.size())) : []

            render(template: "salesCategoryResults", model: [categoryId  : categoryId,
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

        def stores = storeService.getStores(springSecurityService.principal.retailerId)

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
        DateTime endDate = params.endDate ? DateTime.parse(params.endDate, dateFormatter).withTimeAtStartOfDay() : DateTime.now(DateTimeZone.UTC).withTimeAtStartOfDay()

        Integer storeId
        if (springSecurityService.principal.storeId) {
            storeId = springSecurityService.principal.storeId
        } else {
            storeId = params.storeFilter ? Integer.parseInt(params.storeFilter) : null
        }

        def sales = reportingService.getSalesForProduct(productId, startDate, endDate.plusDays(1), sortParams.max,
                sortParams.offset, sortParams.sortColumn, sortParams.sortOrder, params.descriptionFilter, storeId)

        def totalResults = reportingService.countSalesForProduct(productId, startDate, endDate.plusDays(1), params.descriptionFilter, storeId)

        if (params.csv != null && params.csv == "true") {
            def fileName = "SalesByProduct-" + new Date().format("yyyy_MM_dd_HH_mm_ss") + ".csv"
            response.setHeader("Content-Disposition", "attachment; filename=${fileName}")
            response.setHeader("Content-Type", "text/csv;")
            render getSalesByProductCsv(sales)
        } else {
            render(template: "salesProductResults", model: [sales       : sales,
                                                            userColumns : reportingService.getReportColumns(ReportType.SALES_PRODUCT),
                                                            sortParams  : sortParams,
                                                            startDate   : startDate,
                                                            endDate     : endDate,
                                                            totalResults: totalResults,
                                                            userTimeZone: DateTimeZone.forID("Europe/London"),
                                                            isWeighted  : Product.findById(productId)?.weightedItem])
        }
    }

    // The standalone category sales report.
    def categorySales() {
        DateTimeFormatter dateFormatter = DateTimeFormat.forPattern("dd/MM/yyyy").withZoneUTC()
        DateTime startDate = params.startDate ? DateTime.parse(params.startDate, dateFormatter).withTimeAtStartOfDay() : DateTime.now(DateTimeZone.UTC).withTimeAtStartOfDay()
        DateTime endDate = params.startDate ? DateTime.parse(params.endDate, dateFormatter).withTimeAtStartOfDay() : DateTime.now(DateTimeZone.UTC).withTimeAtStartOfDay()
        def stores = storeService.getStores(springSecurityService.principal.retailerId)

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
            sale.salesCategories?.sort { it.categoryLevel }?.first()?.categoryId
        }

        def finalSales = []

        populateCategorySalesFinalSales(0, salesGrouped, finalSales, maxCategoryLevel)

        if (params.csv != null && params.csv == "true") {
            def fileName = "CategorySales-" + new Date().format("yyyy_MM_dd_HH_mm_ss") + ".csv"
            response.setHeader("Content-Disposition", "attachment; filename=${fileName}")
            response.setHeader("Content-Type", "text/csv;")
            render getSalesByCategoryCsv(finalSales)
        } else {
            int totalResults = finalSales.size()
            finalSales = sortParams.offset < finalSales.size() ? finalSales.subList(sortParams.offset, (sortParams.offset + sortParams.max < finalSales.size() ? sortParams.offset + sortParams.max : finalSales.size())) : []

            // Sort into the required order.
            if (sortParams.sortColumn == "description") {
                finalSales.sort { it.salesCategories?.first()?.categoryDescription }
            } else {
                finalSales.sort { it."${sortParams.sortColumn}" }
            }

            if (sortParams.sortOrder == "desc") {
                finalSales = finalSales.reverse()
            }

            render(template: "categorySalesResults", model: [sales       : finalSales,
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
                    costPrice: salesGroup.value.sum { it.quantity > 0 ? it.costPrice : BigDecimal.ZERO }.setScale(2),
                    retailPrice: salesGroup.value.sum { it.quantity > 0 ? it.retailPrice : BigDecimal.ZERO }.setScale(2),
                    vatAmount: salesGroup.value.sum { it.quantity > 0 ? it.vatAmount : BigDecimal.ZERO }.setScale(2),
                    margin: salesGroup.value.sum { it.quantity > 0 ? it.margin : BigDecimal.ZERO }.setScale(2),
                    productDescription: salesGroup.value[0].salesCategories.find { it.categoryLevel == currentCategoryLevel }?.categoryDescription,
                    productUnitSize: ""
            )

            groupedSale.refundQuantity = BigDecimal.ZERO
            groupedSale.quantity = BigDecimal.ZERO

            salesGroup.value.each {
                Product product = Product.findById(it?.productId)
                if (it.quantity < 0) {
                    groupedSale.refundQuantity -= product.weightedItem ? -1 : it?.quantity.intValue()
                } else {
                    groupedSale.quantity += product.weightedItem ? 1 : it?.quantity.intValue()
                }
            }

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
        def stores = storeService.getStores(springSecurityService.principal.retailerId)

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
            int initQuantity = groupedProductSale.value[0].quantity

            groupedProductSale.value[0].costPrice = groupedProductSale.value.sum { it.quantity > 0 ? it.costPrice : BigDecimal.ZERO }.setScale(2)
            groupedProductSale.value[0].retailPrice = groupedProductSale.value.sum { it.quantity > 0 ? it.retailPrice : BigDecimal.ZERO }.setScale(2)
            groupedProductSale.value[0].vatAmount = groupedProductSale.value.sum { it.quantity > 0 ? it.vatAmount : BigDecimal.ZERO }.setScale(2)
            groupedProductSale.value[0].margin = groupedProductSale.value.sum { it.quantity > 0 ? it.margin : BigDecimal.ZERO }.setScale(2)

            groupedProductSale.value[0].quantity = 0
            groupedProductSale.value[0].refundQuantity = 0

            groupedProductSale.value.each {
                if (it.quantity < 0) {
                    groupedProductSale.value[0].refundQuantity -= it.quantity
                } else {
                    groupedProductSale.value[0].quantity += it.quantity
                }
            }
            initQuantity < 0 ? (groupedProductSale.value[0].refundQuantity -= initQuantity) : (groupedProductSale.value[0].quantity += initQuantity)

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
            def fileName = "Sales-" + new Date().format("yyyy_MM_dd_HH_mm_ss") + ".csv"
            response.setHeader("Content-Disposition", "attachment; filename=${fileName}")
            response.setHeader("Content-Type", "text/csv;")

            render getSalesCsv(finalSales)
        } else {
            // Restrict the number of results.
            int totalResults = finalSales.size()
            finalSales = sortParams.offset < finalSales.size() ? finalSales.subList(sortParams.offset, (sortParams.offset + sortParams.max < finalSales.size() ? sortParams.offset + sortParams.max : finalSales.size())) : []

            render(template: "salesResults", model: [sales       : finalSales,
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

        def stores = storeService.getStores(springSecurityService.principal.retailerId)

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
        def promotionSales = reportingService.getPromotionSales(startDate, endDate.plusDays(1), params.descriptionFilter, params.promotionTypeFilter, storeId)

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
            def fileName = "PromotionSalesGrouped-" + new Date().format("yyyy_MM_dd_HH_mm_ss") + ".csv"
            response.setHeader("Content-Disposition", "attachment; filename=${fileName}")
            response.setHeader("Content-Type", "text/csv;")
            render getPromotionsGrouped(finalPromotionSales)
        } else {
            // Restrict the number of results.
            int totalResults = finalPromotionSales.size()
            finalPromotionSales = sortParams.offset < finalPromotionSales.size() ? finalPromotionSales.subList(sortParams.offset, (sortParams.offset + sortParams.max < finalPromotionSales.size() ? sortParams.offset + sortParams.max : finalPromotionSales.size())) : []
            render(template: "promotionsGroupedResults", model: [promotionSales: finalPromotionSales, userColumns: reportingService.getReportColumns(ReportType.PROMOTIONS_GROUPED), sortParams: sortParams, startDate: startDate, endDate: endDate, totalResults: totalResults])
        }
    }

    def promotions() {
        int promotionId = getIntegerParam(params.promotionId)
        DateTimeFormatter dateFormatter = DateTimeFormat.forPattern("dd/MM/yyyy").withZoneUTC()
        DateTime startDate = params.startDate ? DateTime.parse(params.startDate, dateFormatter).withTimeAtStartOfDay() : DateTime.now(DateTimeZone.UTC).withTimeAtStartOfDay()
        DateTime endDate = params.startDate ? DateTime.parse(params.endDate, dateFormatter).withTimeAtStartOfDay() : DateTime.now(DateTimeZone.UTC).withTimeAtStartOfDay()
        def stores = storeService.getStores(springSecurityService.principal.retailerId)

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
        def promotionSales = reportingService.getPromotionSales(startDate, endDate.plusDays(1), promotionId, sortParams.max, sortParams.offset, sortParams.sortColumn, sortParams.sortOrder, storeId)

        if (params.csv != null && params.csv == "true") {
            def fileName = "Promotions-" + new Date().format("yyyy_MM_dd_HH_mm_ss") + ".csv"
            response.setHeader("Content-Disposition", "attachment; filename=${fileName}")
            response.setHeader("Content-Type", "text/csv;")
            render getPromotions(promotionSales)
        } else {
            render(template: "promotionsResults", model: [promotionId: promotionId, promotionSales: promotionSales, userColumns: reportingService.getReportColumns(ReportType.PROMOTIONS), sortParams: sortParams, startDate: startDate, endDate: endDate, totalResults: promotionSales.totalCount, userTimeZone: DateTimeZone.forID("Europe/London")])
        }
    }

    def promotion() {
        int promotionSaleId = getIntegerParam(params.promotionSaleId)

        DateTimeFormatter dateFormatter = DateTimeFormat.forPattern("dd/MM/yyyy").withZoneUTC()
        DateTime startDate = params.startDate ? DateTime.parse(params.startDate, dateFormatter).withTimeAtStartOfDay() : DateTime.now(DateTimeZone.UTC).withTimeAtStartOfDay()
        DateTime endDate = params.startDate ? DateTime.parse(params.endDate, dateFormatter).withTimeAtStartOfDay() : DateTime.now(DateTimeZone.UTC).withTimeAtStartOfDay()
        def stores = storeService.getStores(springSecurityService.principal.retailerId)

        [reportType     : ReportType.PROMOTION,
         promotionSaleId: promotionSaleId,
         startDate      : startDate,
         endDate        : endDate,
         userColumns    : reportingService.getReportColumns(ReportType.PROMOTION),
         stores         : stores]
    }

    def ajaxPromotion(SortParams sortParams) {
        int promotionSaleId = getIntegerParam(params.promotionSaleId)

        sortParams.validateParams(PROMOTION_REPORT_SORT_COLUMNS)

        Integer storeId
        if (springSecurityService.principal.storeId) {
            storeId = springSecurityService.principal.storeId
        } else {
            storeId = params.storeFilter ? Integer.parseInt(params.storeFilter) : null
        }

        // Find all promotion sale products for this promotion sale.
        def promotionSaleProducts = reportingService.getPromotionSaleProducts(promotionSaleId, params.descriptionFilter, sortParams.max, sortParams.offset, sortParams.sortColumn, sortParams.sortOrder, storeId)

        if (params.csv != null && params.csv == "true") {
            def fileName = "Promotion-" + new Date().format("yyyy_MM_dd_HH_mm_ss") + ".csv"
            response.setHeader("Content-Disposition", "attachment; filename=${fileName}")
            response.setHeader("Content-Type", "text/csv;")
            render getPromotionSaleProduct(promotionSaleProducts)
        } else {
            render(template: "promotionResults", model: [promotionSaleProducts: promotionSaleProducts,
                                                         userColumns          : reportingService.getReportColumns(ReportType.PROMOTION),
                                                         sortParams           : sortParams,
                                                         totalResults         : promotionSaleProducts.totalCount])
        }
    }

    def tillControlEvents() {
        DateTimeFormatter dateFormatter = DateTimeFormat.forPattern("dd/MM/yyyy").withZoneUTC()
        DateTime startDate = params.startDate ? DateTime.parse(params.startDate, dateFormatter).withTimeAtStartOfDay() : DateTime.now(DateTimeZone.UTC).withTimeAtStartOfDay()
        DateTime endDate = params.endDate ? DateTime.parse(params.endDate, dateFormatter).withTimeAtStartOfDay() : DateTime.now(DateTimeZone.UTC).withTimeAtStartOfDay()

        [reportType: ReportType.TILL_CONTROL_EVENTS, userColumns: reportingService.getReportColumns(ReportType.TILL_CONTROL_EVENTS), startDate: startDate, endDate: endDate]
    }

    def ajaxTillControlEvents(SortParams sortParams) {
        sortParams.validateParams(TILL_CONTROL_EVENTS_REPORT_SORT_COLUMNS)

        DateTimeFormatter dateFormatter = DateTimeFormat.forPattern("dd/MM/yyyy").withZoneUTC()
        DateTime startDate = params.startDate ? DateTime.parse(params.startDate, dateFormatter).withTimeAtStartOfDay() : DateTime.now(DateTimeZone.UTC).withTimeAtStartOfDay()
        DateTime endDate = params.endDate ? DateTime.parse(params.endDate, dateFormatter).withTimeAtStartOfDay() : DateTime.now(DateTimeZone.UTC).withTimeAtStartOfDay()

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
        DateTime endDate = params.endDate ? DateTime.parse(params.endDate, dateFormatter).withTimeAtStartOfDay() : DateTime.now(DateTimeZone.UTC).withTimeAtStartOfDay()

        // Find all till control events in the date range.
        def tillControlEvents = reportingService.getTillControlEvents(startDate, endDate.plusDays(1), type, sortParams.max, sortParams.offset, sortParams.sortColumn, sortParams.sortOrder)

        if (params.csv != null && params.csv == "true") {
            def fileName = "TillControlEvent-" + new Date().format("yyyy_MM_dd_HH_mm_ss") + ".csv"
            response.setHeader("Content-Disposition", "attachment; filename=${fileName}")
            response.setHeader("Content-Type", "text/csv;")
            render getTillControlEventCsv(tillControlEvents)
        } else {
            render(template: "tillControlEventResults", model: [tillControlEvents: tillControlEvents, userColumns: reportingService.getReportColumns(ReportType.TILL_CONTROL_EVENT), sortParams: sortParams, totalResults: tillControlEvents.totalCount, userTimeZone: DateTimeZone.forID("Europe/London")])
        }
    }

    // The top level of the main orders report.
    def orders() {
        DateTimeFormatter dateFormatter = DateTimeFormat.forPattern("dd/MM/yyyy").withZoneUTC()
        DateTime startDate = params.startDate ? DateTime.parse(params.startDate, dateFormatter).withTimeAtStartOfDay() : DateTime.now(DateTimeZone.UTC).minusDays(6).withTimeAtStartOfDay()
        DateTime endDate = params.startDate ? DateTime.parse(params.endDate, dateFormatter).withTimeAtStartOfDay() : DateTime.now(DateTimeZone.UTC).withTimeAtStartOfDay()
        def stores = storeService.getStores(springSecurityService.principal.retailerId)

        def suppliers = supplierService.getSuppliers()

        boolean enableOrderCreate = false
        if (springSecurityService.principal.storeId  != null &&  springSecurityService.principal.storeId > 0){
            enableOrderCreate = true
        }

        [reportType : ReportType.ORDERS,
         suppliers  : suppliers,
         userColumns: reportingService.getReportColumns(ReportType.ORDERS),
         startDate  : startDate,
         endDate    : endDate,
         stores     : stores,
         enableOrderCreate : enableOrderCreate]
    }

    // The top level of the main orders report.
    def ajaxOrders(SortParams sortParams) {
        sortParams.validateParams(ORDERS_REPORT_SORT_COLUMNS)

        Integer storeId
        if (springSecurityService.principal.storeId) {
            storeId = springSecurityService.principal.storeId
        } else {
            storeId = params.storeFilter ? Integer.parseInt(params.storeFilter) : null
        }

        Integer supplierId = null
        if (params.supplier && !params.supplier.isEmpty()) {
            supplierId = getIntegerParam(params.supplier)
        }

        DateTimeFormatter dateFormatter = DateTimeFormat.forPattern("dd/MM/yyyy").withZoneUTC()
        DateTime startDate = params.startDate ? DateTime.parse(params.startDate, dateFormatter).withTimeAtStartOfDay() : DateTime.now(DateTimeZone.UTC).withTimeAtStartOfDay()
        DateTime endDate = params.startDate ? DateTime.parse(params.endDate, dateFormatter).withTimeAtStartOfDay() : DateTime.now(DateTimeZone.UTC).withTimeAtStartOfDay()

        def orders = productListService.getOrders(storeId, supplierId, startDate, endDate.plusDays(1)).toList()

        // Sort into the required order.
        if (orders) {
            switch (sortParams.sortColumn) {
                case "supplierName":
                    orders = orders.sort { it.supplierReference }
                    break
                case "numberOfItems":
                    orders = orders.sort { it.totalQuantity }
                    break
                case "value":
                    orders = orders.sort { it.totalValue }
                    break
                default:
                    orders = orders.sort { it."${sortParams.sortColumn}" }
            }

            if (sortParams.sortOrder.equalsIgnoreCase("desc")) {
                orders = orders?.reverse()
            }
        }

        if (params.csv != null && params.csv == "true") {
            def fileName = "Orders-" + new Date().format("yyyy_MM_dd_HH_mm_ss") + ".csv"
            response.setHeader("Content-Disposition", "attachment; filename=${fileName}")
            response.setHeader("Content-Type", "text/csv;")
            render getOrdersCsv(orders)
        } else {
            render(template: "ordersResults", model: [orders      : orders,
                                                      userColumns : reportingService.getReportColumns(ReportType.ORDERS),
                                                      startDate   : startDate,
                                                      endDate     : endDate,
                                                      sortParams  : sortParams,
                                                      totalResults: orders.size()])
        }
    }

    // The bottom level of the main orders report.
    def order() {
        int productListId = getIntegerParam(params.productListId)
        DateTimeFormatter dateFormatter = DateTimeFormat.forPattern("dd/MM/yyyy").withZoneUTC()
        DateTime startDate = params.startDate ? DateTime.parse(params.startDate, dateFormatter) : DateTime.now(DateTimeZone.UTC).minusDays(6)
        DateTime endDate = params.startDate ? DateTime.parse(params.endDate, dateFormatter) : DateTime.now(DateTimeZone.UTC)
        def stores = storeService.getStores(springSecurityService.principal.retailerId)

        def suppliers = supplierService.getSuppliers()

        [reportType   : ReportType.ORDER,
         productListId: productListId,
         suppliers    : suppliers,
         startDate    : startDate,
         endDate      : endDate,
         userColumns  : reportingService.getReportColumns(ReportType.ORDER),
         stores       : stores]
    }

    // The bottom level of the main orders report.
    def ajaxOrder(SortParams sortParams) {
        sortParams.validateParams(ORDER_REPORT_SORT_COLUMNS)
        Integer productListId = getIntegerParam(params.productListId)

        Integer storeId
        if (springSecurityService.principal.storeId) {
            storeId = springSecurityService.principal.storeId
        } else {
            storeId = params.storeFilter ? Integer.parseInt(params.storeFilter) : null
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
            packLines?.sort { it.productListItem?.productVariant?.product?.description }
        } else if (sortParams.sortColumn == "packQuantity") {
            packLines?.sort { it.pack?.quantity }
        } else if (sortParams.sortColumn == "orderedQuantity") {
            packLines?.sort { it.pack?.quantity.multiply(it.quantity) }
        } else if (sortParams.sortColumn == "lineValue") {
            packLines?.sort { it.pack?.price?.multiply(it.quantity) }
        } else { //If no sort column found then by default sort by sku
            packLines?.sort { it.productListItem?.productVariant?.sku }
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

    // The top level of the main deliveries report.
    def deliveries() {
        DateTimeFormatter dateFormatter = DateTimeFormat.forPattern("dd/MM/yyyy").withZoneUTC()
        DateTime startDate = params.startDate ? DateTime.parse(params.startDate, dateFormatter).withTimeAtStartOfDay() : DateTime.now(DateTimeZone.UTC).minusDays(6).withTimeAtStartOfDay()
        DateTime endDate = params.startDate ? DateTime.parse(params.endDate, dateFormatter).withTimeAtStartOfDay() : DateTime.now(DateTimeZone.UTC).withTimeAtStartOfDay()
        Integer storeId = params.storeId ? getIntegerParam(params.storeId) : null
        Integer supplierId = params.supplierId ? getIntegerParam(params.supplierId) : null

        def stores = storeService.getStores(springSecurityService.principal.retailerId)

        def suppliers = supplierService.getSuppliers()

        [reportType : ReportType.DELIVERIES,
         suppliers : suppliers,
         userColumns: reportingService.getReportColumns(ReportType.DELIVERIES),
         startDate : startDate,
         endDate : endDate,
         storeId : storeId,
         supplierId : supplierId,
         stores : stores]
    }

    // The top level of the main deliveries report.
    def ajaxDeliveries(SortParams sortParams) {
        // If no sort is set (first time load) then default to reverse order.
        if (sortParams.sortColumn == "id") {
            sortParams.sortOrder = "desc"
        }

        sortParams.validateParams(DELIVERIES_REPORT_SORT_COLUMNS)

        Integer storeId
        if (springSecurityService.principal.storeId) {
            storeId = springSecurityService.principal.storeId
        } else {
            storeId = params.storeFilter ? Integer.parseInt(params.storeFilter) : null
        }

        Integer supplierId = null
        if (params.supplier && !params.supplier.isEmpty()) {
            supplierId = getIntegerParam(params.supplier)
        }

        DateTimeFormatter dateFormatter = DateTimeFormat.forPattern("dd/MM/yyyy").withZoneUTC()
        DateTime startDate = params.startDate ? DateTime.parse(params.startDate, dateFormatter).withTimeAtStartOfDay() : DateTime.now(DateTimeZone.UTC).withTimeAtStartOfDay()
        DateTime endDate = params.startDate ? DateTime.parse(params.endDate, dateFormatter).withTimeAtStartOfDay() : DateTime.now(DateTimeZone.UTC).withTimeAtStartOfDay()

        def deliveries = productListService.getDeliveries(storeId, supplierId, startDate, endDate.plusDays(1))
        def totalDeliveries = []

        if (deliveries) {
            switch (sortParams.sortColumn) {
                case "deliveryId":
                    deliveries = deliveries.sort { it.orderId }
                    break
                case "storeId":
                    deliveries = deliveries.sort { it.storeId }
                    break
                case "status":
                    deliveries = deliveries.sort { it.status }
                    break
                case "deliveryDate":
                    deliveries = deliveries.sort { a, b ->
                        a.dateStarted <=> b.dateStarted
                    }
                    break
                case "supplierName":
                    deliveries = deliveries.sort { it.supplierReference ?: "" }
                    break
                case "numberOfItems":
                    deliveries = deliveries.sort { it.productListItems.size() }
                    break
                case "totalCost":
                    deliveries = deliveries?.sort { it.totalCost }
                    break
            }

            totalDeliveries.addAll(deliveries)

            if (sortParams.sortOrder.equalsIgnoreCase("desc")) {
                totalDeliveries = totalDeliveries.reverse()
            }
        }

        if (params.csv != null && params.csv == "true") {
            def fileName = "deliveries-" + new Date().format("yyyy_MM_dd_HH_mm_ss") + ".csv"
            response.setHeader("Content-Disposition", "attachment; filename=${fileName}")
            response.setHeader("Content-Type", "text/csv;")
            render getDeliveriesCsv(totalDeliveries)
        } else {
            def dels = sortParams.offset < totalDeliveries.size() ? totalDeliveries.subList(sortParams.offset, (sortParams.offset + sortParams.max < totalDeliveries.size() ? sortParams.offset + sortParams.max : totalDeliveries.size())) : []

            render(template: "deliveriesResults", model: [deliveries : dels,
                                                          userColumns : reportingService.getReportColumns(ReportType.DELIVERIES),
                                                          storeId : storeId,
                                                          supplierId : supplierId,
                                                          startDate : startDate,
                                                          endDate : endDate,
                                                          sortParams : sortParams,
                                                          totalResults : totalDeliveries.size()])
        }
    }

    // The bottom level of the main delivery report.
    def delivery() {
        int productListId = getIntegerParam(params.productListId)
        DateTimeFormatter dateFormatter = DateTimeFormat.forPattern("dd/MM/yyyy").withZoneUTC()
        DateTime startDate = params.startDate ? DateTime.parse(params.startDate, dateFormatter) : DateTime.now(DateTimeZone.UTC)
        DateTime endDate = params.startDate ? DateTime.parse(params.endDate, dateFormatter) : DateTime.now(DateTimeZone.UTC)
        Integer supplierId = params.supplierId ? getIntegerParam(params.supplierId) : null
        Integer storeId = params.storeId ? getIntegerParam(params.storeId) : null
        String descriptionFilter = params.descriptionFilter

        def delivery = productListService.getProductList(productListId)

        [reportType : ReportType.DELIVERY,
         delivery : delivery,
         productListId : productListId,
         startDate : startDate,
         endDate : endDate,
         supplierId : supplierId,
         storeId : storeId,
         descriptionFilter: descriptionFilter,
         userColumns : reportingService.getReportColumns(ReportType.DELIVERY),
         showAcceptDeliveryButton : [ProductListStatus.PENDING, ProductListStatus.IN_PROGRESS].contains(delivery.status)]
    }

    // The mid level of the main delivery report.
    def ajaxDelivery(SortParams sortParams) {
        sortParams.validateParams(DELIVERY_REPORT_SORT_COLUMNS)

        Integer productListId = getIntegerParam(params.productListId)
        Integer supplierId = params.supplierId ? getIntegerParam(params.supplierId) : null
        Integer storeId = params.storeId ? getIntegerParam(params.storeId) : null

        String descriptionFilter = null
        if (params.descriptionFilter && !params.descriptionFilter.isEmpty()) {
            descriptionFilter = params.descriptionFilter
        }

        DateTimeFormatter dateFormatter = DateTimeFormat.forPattern("dd/MM/yyyy").withZoneUTC()
        DateTime startDate = params.startDate ? DateTime.parse(params.startDate, dateFormatter).withTimeAtStartOfDay() : DateTime.now(DateTimeZone.UTC).withTimeAtStartOfDay()
        DateTime endDate = params.startDate ? DateTime.parse(params.endDate, dateFormatter).withTimeAtStartOfDay() : DateTime.now(DateTimeZone.UTC).withTimeAtStartOfDay()

        def delivery = productListService.getProductList(productListId)

        def items = []

        if (descriptionFilter) {
            items.addAll(delivery?.productListItems?.findAll{ it.productVariant.product.description.toLowerCase().contains(descriptionFilter.toLowerCase()) })
        } else {
            items.addAll(delivery?.productListItems)
        }

        int totalResults = items.size()

        if (delivery) {
            switch (sortParams.sortColumn) {
                case "sku":
                    items = items.sort { it.productVariant.sku }
                    break
                case "description":
                    items = items.sort { it.productVariant.product.description }
                    break
                case "itemQuantity":
                    items = items.sort { it.quantity }
                    break
                case "totalCost":
                    items = items.sort { it.totalCost }
                    break
            }

            if (sortParams.sortOrder.equalsIgnoreCase("desc")) {
                items = items.reverse()
            }
        }

        items = sortParams.offset < items.size() ? items.subList(sortParams.offset, (sortParams.offset + sortParams.max < items.size() ? sortParams.offset + sortParams.max : items.size())) : []

        if (params.csv != null && params.csv == "true") {
            def fileName = "delivery-" + new Date().format("yyyy_MM_dd_HH_mm_ss") + ".csv"
            response.setHeader("Content-Disposition", "attachment; filename=${fileName}")
            response.setHeader("Content-Type", "text/csv;")
            render getDeliveryCsv(items)
        } else {
            render(template: "deliveryResults", model: [items : items,
                                                        userColumns : reportingService.getReportColumns(ReportType.DELIVERY),
                                                        startDate : startDate,
                                                        endDate : endDate,
                                                        storeId : storeId,
                                                        supplierId : supplierId,
                                                        productListId : productListId,
                                                        descriptionFilter : descriptionFilter,
                                                        sortParams : sortParams,
                                                        totalResults : totalResults])
        }
    }

    def ajaxAcceptDelivery() {
        def productListId = getIntegerParam(params.productListId)

        def productList = productListService.getProductList(productListId)

        if (!productList) {
            flash.error = "Delivery not found"
        }

        // Head office or correct store level can accept this delivery.
        if (springSecurityService.principal.storeId == null || (springSecurityService.principal.storeId == productList?.store?.id)) {
            productListService.acceptDelivery(productListId)
        }

        response.status = 200
    }

    def deliveryPackLines() {
        int productListId = getIntegerParam(params.productListId)
        int productListItemId = getIntegerParam(params.productListItemId)
        DateTimeFormatter dateFormatter = DateTimeFormat.forPattern("dd/MM/yyyy").withZoneUTC()
        DateTime startDate = params.startDate ? DateTime.parse(params.startDate, dateFormatter).withTimeAtStartOfDay() : DateTime.now(DateTimeZone.UTC).withTimeAtStartOfDay()
        DateTime endDate = params.endDate ? DateTime.parse(params.endDate, dateFormatter).withTimeAtStartOfDay() : DateTime.now(DateTimeZone.UTC).withTimeAtStartOfDay()
        Integer supplierId = params.supplierId ? getIntegerParam(params.supplierId) : null
        Integer storeId = params.storeId ? getIntegerParam(params.storeId) : null
        String descriptionFilter = params.descriptionFilter

        def delivery = ProductList.findById(productListId)
        def productListItem = productListService.getProductListItem(productListItemId)

        [reportType : ReportType.DELIVERY_ITEM,
         delivery : delivery,
         productListId : productListId,
         productListItemId: productListItemId,
         productListItem : productListItem,
         userColumns : reportingService.getReportColumns(ReportType.DELIVERY_ITEM),
         startDate : startDate,
         endDate : endDate,
         storeId : storeId,
         supplierId : supplierId,
         descriptionFilter : descriptionFilter]
    }

    // The bottom level of the main delivery report with the packs for an item in a delivery.
    def ajaxDeliveryPackLines(SortParams sortParams) {
        sortParams.validateParams(DELIVERY_PACK_REPORT_SORT_COLUMNS)

        Integer productListItemId = getIntegerParam(params.productListItemId)

        def productListItem = productListService.getProductListItem(productListItemId)

        def packLines = productListItem?.packLines

        if (productListItem) {
            switch (sortParams.sortColumn) {
                case "description":
                    packLines = packLines?.sort { it.productListItem?.productVariant?.product?.description }
                    break
                case "price":
                    packLines = packLines?.sort { it.productListItem?.productVariant?.currentPrice }
                    break
                case "packCost":
                    packLines = packLines?.sort { it.pack?.price }
                    break
                case "packSize":
                    packLines = packLines?.sort { it.pack?.quantity }
                    break
                case "deliveryQuantity":
                    packLines = packLines?.sort { it.productListItem?.fillQuantity }
                    break
                case "totalQuantity":
                    packLines = packLines?.sort { it.totalQuantity }
                    break
                case "totalSellValue":
                    packLines = packLines?.sort { it.totalValue }
                    break
            }

            if (sortParams.sortOrder.equalsIgnoreCase("desc")) {
                packLines = packLines.reverse()
            }
        }

        int totalCount = packLines.size()

        packLines = sortParams.offset < packLines.size() ? packLines.subList(sortParams.offset, (sortParams.offset + sortParams.max < packLines.size() ? sortParams.offset + sortParams.max : packLines.size())) : []

        render(template: "deliveryPackLineResults", model: [packLines : packLines,
                                                            userColumns : reportingService.getReportColumns(ReportType.DELIVERY_ITEM),
                                                            sortParams : sortParams,
                                                            totalResults: totalCount])
    }

    def paypointSales() {
        DateTimeFormatter dateFormatter = DateTimeFormat.forPattern("dd/MM/yyyy").withZoneUTC()
        DateTime startDate = params.startDate ? DateTime.parse(params.startDate, dateFormatter) : DateTime.now(DateTimeZone.UTC)
        DateTime endDate = params.startDate ? DateTime.parse(params.endDate, dateFormatter) : DateTime.now(DateTimeZone.UTC)

        def stores = storeService.getStores(springSecurityService.principal.retailerId)

        [reportType : ReportType.PAYPOINT_SALES,
         startDate  : startDate,
         endDate    : endDate,
         userColumns: reportingService.getReportColumns(ReportType.PAYPOINT_SALES),
         stores     : stores]
    }

    def ajaxPayPointSales(SortParams sortParams) {
        sortParams.validateParams(PAYPOINT_SALE_REPORT_SORT_COLUMNS)

        DateTimeFormatter dateFormatter = DateTimeFormat.forPattern("dd/MM/yyyy").withZoneUTC()
        DateTime startDate = params.startDate ? DateTime.parse(params.startDate, dateFormatter).withTimeAtStartOfDay() : DateTime.now(DateTimeZone.UTC).withTimeAtStartOfDay()
        DateTime endDate = params.startDate ? DateTime.parse(params.endDate, dateFormatter).withTimeAtStartOfDay() : DateTime.now(DateTimeZone.UTC).withTimeAtStartOfDay()

        String description = params.descriptionFilter ? ("%" + params.descriptionFilter + "%") : null

        Integer storeId = springSecurityService.principal.storeId
        if (!storeId && params.storeFilter && !params.storeFilter.isEmpty() && params.storeFilter.isNumber()) {
            storeId = Integer.parseInt(params.storeFilter)
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

    def productLists() {
        DateTimeFormatter dateFormatter = DateTimeFormat.forPattern("dd/MM/yyyy").withZoneUTC()
        DateTime startDate = params.startDate ? DateTime.parse(params.startDate, dateFormatter) : DateTime.now(DateTimeZone.UTC)
        DateTime endDate = params.startDate ? DateTime.parse(params.endDate, dateFormatter) : DateTime.now(DateTimeZone.UTC)

        Integer storeId
        if (springSecurityService.principal.storeId) {
            storeId = springSecurityService.principal.storeId
        } else {
            storeId = params.storeId ? Integer.parseInt(params.storeId) : null
        }

        ProductListType type = params.type ? ProductListType.valueOf(params.type) : null

        def stores = storeService.getStores(springSecurityService.principal.retailerId)

        [reportType : ReportType.PRODUCT_LISTS,
         startDate  : startDate,
         endDate    : endDate,
         userColumns: reportingService.getReportColumns(ReportType.PRODUCT_LISTS),
         stores     : stores,
         types      : ProductListType.values(),
         storeId    : storeId,
         type       : type]
    }

    // The top level of the main product lists report.
    def ajaxProductLists(SortParams sortParams) {
        // If no sort is set (first time load) then default to reverse order.
        if (sortParams.sortColumn == "id") {
            sortParams.sortOrder = "desc"
        }

        sortParams.validateParams(PRODUCT_LISTS_REPORT_SORT_COLUMNS)

        Integer storeId
        if (springSecurityService.principal.storeId) {
            storeId = springSecurityService.principal.storeId
        } else {
            storeId = params.storeFilter ? Integer.parseInt(params.storeFilter) : null
        }

        ProductListType type = params.typeFilter ? ProductListType.valueOf(params.typeFilter) : null

        DateTimeFormatter dateFormatter = DateTimeFormat.forPattern("dd/MM/yyyy").withZoneUTC()
        DateTime startDate = params.startDate ? DateTime.parse(params.startDate, dateFormatter).withTimeAtStartOfDay() : DateTime.now(DateTimeZone.UTC).withTimeAtStartOfDay()
        DateTime endDate = params.startDate ? DateTime.parse(params.endDate, dateFormatter).withTimeAtStartOfDay() : DateTime.now(DateTimeZone.UTC).withTimeAtStartOfDay()

        def productLists = productListService.getProductLists(storeId, type, startDate, endDate.plusDays(1))
        def totalProductLists = []

        if (productLists) {
            switch (sortParams.sortColumn) {
                case "productListId":
                    productLists = productLists.sort { it.id }
                    break
                case "storeId":
                    productLists = productLists.sort { it.storeId }
                    break
                case "status":
                    productLists = productLists.sort { it.status }
                    break
                case "startDate":
                    productLists = productLists.sort { a, b ->
                        a.dateStarted <=> b.dateStarted
                    }
                    break
                case "numberOfItems":
                    productLists = productLists.sort { it.productListItems.size() }
                    break
            }

            totalProductLists.addAll(productLists)

            if (sortParams.sortOrder.equalsIgnoreCase("desc")) {
                totalProductLists = totalProductLists.reverse()
            }
        }

        if (params.csv != null && params.csv == "true") {
            def fileName = "productlists-" + new Date().format("yyyy_MM_dd_HH_mm_ss") + ".csv"
            response.setHeader("Content-Disposition", "attachment; filename=${fileName}")
            response.setHeader("Content-Type", "text/csv;")
            render getProductListsCsv(totalProductLists)
        } else {
            def finalProductLists = sortParams.offset < totalProductLists.size() ? totalProductLists.subList(sortParams.offset, (sortParams.offset + sortParams.max < totalProductLists.size() ? sortParams.offset + sortParams.max : totalProductLists.size())) : []

            render(template: "productListsResults", model: [productLists : finalProductLists,
                                                            userColumns : reportingService.getReportColumns(ReportType.PRODUCT_LISTS),
                                                            storeId : storeId,
                                                            startDate : startDate,
                                                            endDate : endDate,
                                                            sortParams : sortParams,
                                                            type : type,
                                                            totalResults : totalProductLists.size()])
        }
    }

    // The bottom level of the main product list report.
    def productList() {
        int productListId = getIntegerParam(params.productListId)
        DateTimeFormatter dateFormatter = DateTimeFormat.forPattern("dd/MM/yyyy").withZoneUTC()
        DateTime startDate = params.startDate ? DateTime.parse(params.startDate, dateFormatter) : DateTime.now(DateTimeZone.UTC)
        DateTime endDate = params.startDate ? DateTime.parse(params.endDate, dateFormatter) : DateTime.now(DateTimeZone.UTC)

        Integer storeId
        if (springSecurityService.principal.storeId) {
            storeId = springSecurityService.principal.storeId
        } else {
            storeId = params.storeId ? Integer.parseInt(params.storeId) : null
        }

        ProductListType type = params.type ? ProductListType.valueOf(params.type) : null

        def productList = productListService.getProductList(productListId)

        [reportType : ReportType.PRODUCT_LIST,
         productList : productList,
         productListId : productListId,
         startDate : startDate,
         endDate : endDate,
         storeId : storeId,
         type : type,
         userColumns : reportingService.getReportColumns(ReportType.PRODUCT_LIST)]
    }

    // The mid level of the main delivery report.
    def ajaxProductList(SortParams sortParams) {
        sortParams.validateParams(PRODUCT_LIST_REPORT_SORT_COLUMNS)

        Integer productListId = getIntegerParam(params.productListId)
        Integer storeId = params.storeId ? getIntegerParam(params.storeId) : null // Not used but needed to be passed back in to the view for the breadcrumb.

        DateTimeFormatter dateFormatter = DateTimeFormat.forPattern("dd/MM/yyyy").withZoneUTC()
        DateTime startDate = params.startDate ? DateTime.parse(params.startDate, dateFormatter).withTimeAtStartOfDay() : DateTime.now(DateTimeZone.UTC).withTimeAtStartOfDay()
        DateTime endDate = params.startDate ? DateTime.parse(params.endDate, dateFormatter).withTimeAtStartOfDay() : DateTime.now(DateTimeZone.UTC).withTimeAtStartOfDay()

        def productList = productListService.getProductList(productListId)

        def items = []

        items.addAll(productList?.productListItems)

        int totalResults = items.size()

        if (productList) {
            switch (sortParams.sortColumn) {
                case "sku":
                    items = items.sort { it.productVariant.sku }
                    break
                case "description":
                    items = items.sort { it.productVariant.product.description }
                    break
                case "itemQuantity":
                    items = items.sort { it.quantity }
                    break
                case "totalCost":
                    items = items.sort { it.totalCost }
                    break
            }

            if (sortParams.sortOrder.equalsIgnoreCase("desc")) {
                items = items.reverse()
            }
        }

        items = sortParams.offset < items.size() ? items.subList(sortParams.offset, (sortParams.offset + sortParams.max < items.size() ? sortParams.offset + sortParams.max : items.size())) : []

        if (params.csv != null && params.csv == "true") {
            def fileName = "productlist-" + new Date().format("yyyy_MM_dd_HH_mm_ss") + ".csv"
            response.setHeader("Content-Disposition", "attachment; filename=${fileName}")
            response.setHeader("Content-Type", "text/csv;")
            render getProductListCsv(items)
        } else {
            render(template: "productListResults", model: [items : items,
                                                           userColumns : reportingService.getReportColumns(ReportType.PRODUCT_LIST),
                                                           startDate : startDate,
                                                           endDate : endDate,
                                                           storeId : storeId,
                                                           productListId : productListId,
                                                           sortParams : sortParams,
                                                           totalResults : totalResults])
        }
    }

    def tenderMovements() {
        DateTimeFormatter dateFormatter = DateTimeFormat.forPattern("dd/MM/yyyy").withZoneUTC()
        DateTime startDate = params.startDate ? DateTime.parse(params.startDate, dateFormatter).withTimeAtStartOfDay() : DateTime.now(DateTimeZone.UTC).withTimeAtStartOfDay()
        DateTime endDate = params.endDate ? DateTime.parse(params.endDate, dateFormatter).withTimeAtStartOfDay() : DateTime.now(DateTimeZone.UTC).withTimeAtStartOfDay()

        Integer storeId
        if (springSecurityService.principal.storeId) {
            storeId = springSecurityService.principal.storeId
        } else {
            storeId = params.storeFilter ? Integer.parseInt(params.storeFilter) : null
        }

        def stores = storeService.getStores(springSecurityService.principal.retailerId)

        [reportType: ReportType.TENDER_MOVEMENTS, tenderTypes: TenderType.values(), tenderMovementTypes: TenderMovementType.values(), stores: stores, startDate: startDate, endDate: endDate, storeId: storeId, userColumns: reportingService.getReportColumns(ReportType.TENDER_MOVEMENTS)]
    }

    def ajaxTenderMovements(SortParams sortParams) {
        sortParams.validateParams(TENDER_MOVEMENT_REPORT_SORT_COLUMNS)

        DateTimeFormatter dateFormatter = DateTimeFormat.forPattern("dd/MM/yyyy").withZoneUTC()
        DateTime startDate = params.startDate ? DateTime.parse(params.startDate, dateFormatter).withTimeAtStartOfDay() : DateTime.now(DateTimeZone.UTC).withTimeAtStartOfDay()
        DateTime endDate = params.endDate ? DateTime.parse(params.endDate, dateFormatter).withTimeAtStartOfDay() : DateTime.now(DateTimeZone.UTC).withTimeAtStartOfDay()

        TenderMovementType tenderMovementType = params.tenderMovementType ? TenderMovementType.valueOf(params.tenderMovementType) : null
        TenderType tenderType = params.tenderType ? TenderType.valueOf(params.tenderType) : null
        Integer storeId = params.storeFilter ? getIntegerParam(params.storeFilter) : null

        def tenderMovements = reportingService.getTenderMovements(startDate, endDate.plusDays(1), tenderMovementType, tenderType, storeId, sortParams.max, sortParams.offset, sortParams.sortColumn, sortParams.sortOrder).toList()

        if (params.csv != null && params.csv == "true") {
            def fileName = "TenderMovements-" + new Date().format("yyyy_MM_dd_HH_mm_ss") + ".csv"
            response.setHeader("Content-Disposition", "attachment; filename=${fileName}")
            response.setHeader("Content-Type", "text/csv;")
            render getTenderMovementsCsv(tenderMovements)
        } else {
            render (template: "tenderMovementsResults", model: [tenderMovements: tenderMovements,
                                                                userColumns: reportingService.getReportColumns(ReportType.TENDER_MOVEMENTS),
                                                                sortParams: sortParams,
                                                                startDate: startDate,
                                                                endDate: endDate,
                                                                tenderMovementType: tenderMovementType,
                                                                tenderType: tenderType,
                                                                storeId: storeId,
                                                                totalResults: tenderMovements.size()])
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

                render(status: 200)
            }
        } catch (Exception e) {
            e.printStackTrace()
            render(status: 500, text: "An error occurred saving your report column preferences.")
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
        String pattern = "dd/MM/yy HH:mm:ss"
        DateTimeFormatter formatter = DateTimeFormat.forPattern(pattern)
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
            stringBuilder.append(it.dateCreated ? formatter.print(it.dateCreated) : "N/A")
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

        if (!columns || columns?.columns?.find { it.column == "storeId" }?.enabled) {
            stringBuilder.append("Store Id").append(",")
            enabledCols.add("storeId")
        }
        if (!columns || columns?.columns?.find { it.column == "wlTransactionId" }?.enabled) {
            stringBuilder.append("Txn Id").append(",")
            enabledCols.add("wlTransactionId")
        }
        if (!columns || columns?.columns?.find { it.column == "ppTransactionId" }?.enabled) {
            stringBuilder.append("PP Txn Id").append(",")
            enabledCols.add("ppTransactionId")
        }
        if (!columns || columns?.columns?.find { it.column == "terminalId" }?.enabled) {
            stringBuilder.append("Terminal Id").append(",")
            enabledCols.add("terminalId")
        }
        if (!columns || columns?.columns?.find { it.column == "description" }?.enabled) {
            stringBuilder.append("Description").append(",")
            enabledCols.add("description")
        }
        if (!columns || columns?.columns?.find { it.column == "type" }?.enabled) {
            stringBuilder.append("Type").append(",")
            enabledCols.add("type")
        }
        if (!columns || columns?.columns?.find { it.column == "value" }?.enabled) {
            stringBuilder.append("Value").append(",")
            enabledCols.add("value")
        }
        if (!columns || columns?.columns?.find { it.column == "status" }?.enabled) {
            stringBuilder.append("Status").append(",")
            enabledCols.add("status")
        }
        if (!columns || columns?.columns?.find { it.column == "transactionDate" }?.enabled) {
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

            BigDecimal orderedQuantity
            BigDecimal packQuantity
            BigDecimal lineValue
            if (it?.pack) {
                // (pack line)
                orderedQuantity = it.pack?.quantity?.multiply(it.quantity)
                packQuantity = it.pack?.quantity
                lineValue = it.pack?.price?.multiply(it.quantity)
            } else {
                // (singles line)
                orderedQuantity = it.quantity
                packQuantity = 1
                lineValue = (it?.productListItem?.productVariant?.costPrice ?: BigDecimal.ZERO) * (it?.quantity ?: BigDecimal.ZERO)
            }

            stringBuilder.append(orderedQuantity)
            stringBuilder.append(",")
            stringBuilder.append(packQuantity)
            stringBuilder.append(",")
            stringBuilder.append("£" + lineValue)
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
            stringBuilder.append(it.store?.id)
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

    private String getDeliveriesCsv(List<ProductList> deliveries) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("Delivery ID,Store,Status,Delivery Date,Supplier Name,Number of Products,Total Cost\n")

        deliveries?.each { delivery ->
            stringBuilder.append(delivery?.orderId)
            stringBuilder.append(",")
            stringBuilder.append(delivery?.store?.id)
            stringBuilder.append(",")
            stringBuilder.append(g.message(code: "DeliveryStatus.${delivery?.status}"))
            stringBuilder.append(",")
            stringBuilder.append(delivery?.dateStarted?.toString("dd/MM/yyyy")) // Using Date started as the date of the delivery.
            stringBuilder.append(",")
            stringBuilder.append(delivery?.supplierReference)
            stringBuilder.append(",")
            stringBuilder.append(delivery?.productListItems?.size())
            stringBuilder.append(",")
            stringBuilder.append(delivery?.totalCost)
            stringBuilder.append("\n")
        }

        return stringBuilder.toString()
    }

    private String getDeliveryCsv(List<ProductListItem> delivery) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("Product SKU,Product Description,Items Delivered,Total Cost\n")

        delivery?.each { item ->
            stringBuilder.append(item?.productVariant?.sku)
            stringBuilder.append(",")
            stringBuilder.append(item?.productVariant?.product?.description)
            stringBuilder.append(",")
            stringBuilder.append(item.quantity ?: item.fillQuantity)
            stringBuilder.append(",")
            stringBuilder.append(item.totalCost)
            stringBuilder.append("\n")
        }

        return stringBuilder.toString()
    }

    private String getProductListsCsv(List<ProductList> productLists) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("ID,Store,Type,Status,Start Date,Number of Items\n")

        productLists?.each { productList ->
            stringBuilder.append(productList?.id)
            stringBuilder.append(",")
            stringBuilder.append(productList?.store?.config?.storeNumber)
            stringBuilder.append(",")
            stringBuilder.append(g.message(code: "ProductListType.${productList?.type}"))
            stringBuilder.append(",")
            stringBuilder.append(g.message(code: "ProductListStatus.${productList?.status}"))
            stringBuilder.append(",")
            stringBuilder.append(productList?.dateStarted?.toString("dd/MM/yyyy HH:mm"))
            stringBuilder.append(",")
            stringBuilder.append(productList?.productListItems?.size())
            stringBuilder.append("\n")
        }

        return stringBuilder.toString()
    }

    private String getProductListCsv(List<ProductListItem> productListItems) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("Product SKU,Product Description,Number of Items,Total Cost\n")

        productListItems?.each { item ->
            stringBuilder.append(item?.productVariant?.sku)
            stringBuilder.append(",")
            stringBuilder.append(item?.productVariant?.product?.description)
            stringBuilder.append(",")
            stringBuilder.append(item.quantity ?: item.fillQuantity)
            stringBuilder.append(",")
            stringBuilder.append(item.totalCost)
            stringBuilder.append("\n")
        }

        return stringBuilder.toString()
    }

    private String getTillControlEventsCsv(TreeMap<TillControlEventType, ArrayList> tillControlEventMap) {
        StringBuilder stringBuilder = new StringBuilder()
        stringBuilder.append("Type,Total Quantity\n")
        for (Map.Entry<TillControlEventType, ArrayList> set : tillControlEventMap.entrySet()) {
            //build till event
            String tillEventType = getMappingFromResource("TillControlEventType." + set.getKey()) != null ?
                    getMappingFromResource("TillControlEventType." + set.getKey()) : "TillControlEventType." + set.getKey()
            stringBuilder.append(tillEventType)
            stringBuilder.append(",")
            stringBuilder.append(set.getValue() != null ? set.getValue().size() : 0)
            stringBuilder.append("\n")
        }
        return stringBuilder.toString()
    }

    private String getTillControlEventCsv(List<TillControlEvent> tillControlEventList) {
        StringBuilder stringBuilder = new StringBuilder()
        stringBuilder.append("Type,Till ID,User,Reason,Date,Amount\n")
        tillControlEventList?.each {
            String type = getMappingFromResource("TillControlEventType." + it.type) != null ?
                    getMappingFromResource("TillControlEventType." + it.type) : "TillControlEventType." + it.type
            stringBuilder.append(type.toString()?.replace("'", "\\'"))
            stringBuilder.append(",")
            stringBuilder.append(it.tillId)
            stringBuilder.append(",")
            stringBuilder.append(it.usersName?.replace("'", "\\'"))
            stringBuilder.append(",")
            //build till event reason since
            String reason = it.reason
            if (it.reason == null) {
                reason = "N/A";
            } else if (it.type.name() == "CUSTOMER_REFUSAL") {
                reason = getMappingFromResource("CustomerRefusalReason." + it.reason) != null ?
                        getMappingFromResource("CustomerRefusalReason." + it.reason) : "CustomerRefusalReason." + it.reason
            } else if (it.type.name() == "REFUND") {
                reason = getMappingFromResource("RefundReason." + it.reason) != null ?
                        getMappingFromResource("RefundReason." + it.reason) : "RefundReason." + it.reason
            } else if (it.type.name() == "MARKDOWN") {
                reason = getMappingFromResource("MarkdownReason." + it.reason) != null ?
                        getMappingFromResource("MarkdownReason." + it.reason) : "MarkdownReason." + it.reason
            } else if (it.type.name() == "LINE_VOID") {
                reason = getMappingFromResource("LineVoidReason." + it.reason) != null ?
                        getMappingFromResource("LineVoidReason." + it.reason) : "LineVoidReason." + it.reason
            } else if (it.type.name() == "PAID_OUT") {
                reason = getMappingFromResource("PaidOutReason." + it.reason) != null ?
                        getMappingFromResource("PaidOutReason." + it.reason) : "PaidOutReason." + it.reason
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

    private String getTenderMovementsCsv(List<TenderMovement> tenderMovementList) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("Timestamp,Store,From Location,To Location,Amount,Type,Reason,User\n")

        tenderMovementList?.each { item ->
            stringBuilder.append(item?.timestamp)
            stringBuilder.append(",")
            stringBuilder.append(item?.storeId)
            stringBuilder.append(",")
            stringBuilder.append(item.fromLocation?.description)
            stringBuilder.append(",")
            stringBuilder.append(item.toLocation?.description)
            stringBuilder.append(",")
            stringBuilder.append(item.amount)
            stringBuilder.append(",")
            stringBuilder.append(item.type)
            stringBuilder.append(",")
            stringBuilder.append(item.reason)
            stringBuilder.append(",")
            stringBuilder.append(item.userName)
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
            stringBuilder.append("£" + (it.fullPrice != null ? it.fullPrice.setScale(2, RoundingMode.HALF_UP) : BigDecimal.ZERO))
            stringBuilder.append(",")
            stringBuilder.append("£" + (it.discount != null ? it.discount.setScale(2, RoundingMode.HALF_UP) : BigDecimal.ZERO))
            stringBuilder.append(",")
            stringBuilder.append("£" + (it.profit != null ? it.profit.setScale(2, RoundingMode.HALF_UP) : BigDecimal.ZERO))
            stringBuilder.append(",")
            stringBuilder.append(it.margin?.setScale(2, RoundingMode.HALF_UP) + "%")
            stringBuilder.append(",")
            stringBuilder.append("£" + (it.vat != null ? it.vat.setScale(2, RoundingMode.HALF_UP) : BigDecimal.ZERO))
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
            stringBuilder.append("£" + (it.fullPrice != null ? it.fullPrice.setScale(2, RoundingMode.HALF_UP) : BigDecimal.ZERO))
            stringBuilder.append(",")
            stringBuilder.append("£" + (it.discount != null ? it.discount.setScale(2, RoundingMode.HALF_UP) : BigDecimal.ZERO))
            stringBuilder.append(",")
            stringBuilder.append("£" + (it.profit != null ? it.profit.setScale(2, RoundingMode.HALF_UP) : BigDecimal.ZERO))
            stringBuilder.append(",")
            stringBuilder.append(it.margin?.setScale(2, RoundingMode.HALF_UP) + "%")
            stringBuilder.append(",")
            stringBuilder.append("£" + (it.vat != null ? it.vat.setScale(2, RoundingMode.HALF_UP) : BigDecimal.ZERO))
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
            stringBuilder.append("£" + (it.costPrice != null ? it.costPrice.setScale(2, RoundingMode.HALF_UP) : BigDecimal.ZERO))
            stringBuilder.append(",")
            stringBuilder.append("£" + (it.fullPrice != null ? it.fullPrice.setScale(2, RoundingMode.HALF_UP) : BigDecimal.ZERO))
            stringBuilder.append(",")
            stringBuilder.append("£" + (it.fullPriceProfit != null ? it.fullPriceProfit.setScale(2, RoundingMode.HALF_UP) : BigDecimal.ZERO))
            stringBuilder.append(",")
            stringBuilder.append(it.fullPriceMargin?.setScale(2) + "%")
            stringBuilder.append(",")
            stringBuilder.append("£" + (it.discount != null ? it.discount.setScale(2, RoundingMode.HALF_UP) : BigDecimal.ZERO))
            stringBuilder.append(",")
            stringBuilder.append("£" + (it.discountedPrice != null ? it.discountedPrice.setScale(2, RoundingMode.HALF_UP) : BigDecimal.ZERO))
            stringBuilder.append(",")
            stringBuilder.append("£" + (it.discountedProfit != null ? it.discountedProfit.setScale(2, RoundingMode.HALF_UP) : BigDecimal.ZERO))
            stringBuilder.append(",")
            stringBuilder.append(it.discountedMargin?.setScale(2, RoundingMode.HALF_UP) + "%")
            stringBuilder.append(",")
            stringBuilder.append("£" + (it.vat != null ? it.vat.setScale(2, RoundingMode.HALF_UP) : BigDecimal.ZERO))
            stringBuilder.append("\n")
        }
        return stringBuilder.toString()
    }

    private String getMappingFromResource(String key) {
        //load resource bundle to get value from messages properties file
        ResourceBundle bundle = ResourceBundle.getBundle("messages", Locale.US)
        try {
            return bundle.getString(key)
        } catch (MissingResourceException e) { //if missing resource found mean not configured in message file
            return null //return null if no resource found in message property file
        }
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
}
