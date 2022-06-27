package uk.co.wonderlane.wlpos

import groovy.json.JsonSlurper
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import org.joda.time.format.DateTimeFormat
import org.joda.time.format.DateTimeFormatter
import uk.co.wonderlane.wlpos.enums.PromotionType
import uk.co.wonderlane.wlpos.enums.TillControlEventType
import uk.co.wonderlane.wlpos.reporting.*

class ReportingController {

    def reportingService
    def springSecurityService

    private static final SALES_REPORT_CATEGORY_SORT_COLUMNS = [ "description", "quantity", "avgCostPrice", "avgRetailPrice", "retailPrice", "vatAmount", "avgMargin" ]
    private static final SALES_REPORT_PRODUCT_SORT_COLUMNS = [ "usersName", "category", "description", "quantity", "costPrice", "netTotal", "vatAmount", "profit", "margin", "dateCreated" ]
    private static final SALES_REPORT_SORT_COLUMNS = [ "description", "quantity", "avgCostPrice", "avgRetailPrice", "retailPrice", "vatAmount", "avgMargin" ]
    private static final PROMOTIONS_REPORT_SORT_COLUMNS = [ "type", "description", "quantity", "fullPrice", "discount", "margin", "profit", "vat", "dateCreated" ]
    private static final PROMOTION_REPORT_SORT_COLUMNS = [ "itemCode", "description", "costPrice", "fullPrice", "fullPriceMargin", "fullPriceProfit", "discount", "discountedPrice", "discountedMargin", "discountedProfit", "vat" ]
    private static final TILL_CONTROL_EVENTS_REPORT_SORT_COLUMNS = [ "type", "quantity" ]
    private static final TILL_CONTROL_EVENT_REPORT_SORT_COLUMNS = [ "dateCreated", "type", "usersName", "reason", "amount" ]
    private static final PAYPOINT_SALE_REPORT_SORT_COLUMNS = [ "transactionDate", "storeId", "wlTransactionId", "ppTransactionId", "terminalId", "description", "type", "value", "status" ]

    def index() {

    }

    // The top level of the main sales report.
    def salesDepartment() {
        DateTimeFormatter dateFormatter = DateTimeFormat.forPattern("dd/MM/yyyy").withZoneUTC()
        DateTime startDate = params.startDate ? DateTime.parse(params.startDate, dateFormatter).withTimeAtStartOfDay() : DateTime.now(DateTimeZone.UTC).withTimeAtStartOfDay()
        DateTime endDate = params.startDate ? DateTime.parse(params.endDate, dateFormatter).withTimeAtStartOfDay() : DateTime.now(DateTimeZone.UTC).withTimeAtStartOfDay()

        [reportType : ReportType.SALES_DEPARTMENT,
         userColumns: reportingService.getReportColumns(ReportType.SALES_DEPARTMENT),
         startDate  : startDate,
         endDate    : endDate]
    }

    // The top level of the main sales report.
    def ajaxSalesDepartment(SortParams sortParams) {
        sortParams.validateParams(SALES_REPORT_CATEGORY_SORT_COLUMNS)

        DateTimeFormatter dateFormatter = DateTimeFormat.forPattern("dd/MM/yyyy").withZoneUTC()
        DateTime startDate = params.startDate ? DateTime.parse(params.startDate, dateFormatter).withTimeAtStartOfDay() : DateTime.now(DateTimeZone.UTC).withTimeAtStartOfDay()
        DateTime endDate = params.startDate ? DateTime.parse(params.endDate, dateFormatter).withTimeAtStartOfDay() : DateTime.now(DateTimeZone.UTC).withTimeAtStartOfDay()

        // Find all sales in the date range.
        def sales = reportingService.getSales(startDate, endDate.plusDays(1))

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

        [reportType : ReportType.SALES_CATEGORY,
         categoryId : categoryId,
         startDate  : startDate,
         endDate    : endDate,
         userColumns: reportingService.getReportColumns(ReportType.SALES_CATEGORY)]
    }

    // The middle level of the main sales report.
    def ajaxSalesCategory(SortParams sortParams) {
        int categoryId = getIntegerParam(params.categoryId)

        sortParams.validateParams(SALES_REPORT_CATEGORY_SORT_COLUMNS)

        DateTimeFormatter dateFormatter = DateTimeFormat.forPattern("dd/MM/yyyy").withZoneUTC()
        DateTime startDate = params.startDate ? DateTime.parse(params.startDate, dateFormatter).withTimeAtStartOfDay() : DateTime.now(DateTimeZone.UTC).withTimeAtStartOfDay()
        DateTime endDate = params.startDate ? DateTime.parse(params.endDate, dateFormatter).withTimeAtStartOfDay() : DateTime.now(DateTimeZone.UTC).withTimeAtStartOfDay()

        // Find all sales involving this category in the date range.
        def sales = reportingService.getSalesForCategory(categoryId, startDate,endDate.plusDays(1))

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

        [reportType : ReportType.SALES_PRODUCT,
         productId  : productId,
         startDate  : startDate,
         endDate    : endDate,
         userColumns: reportingService.getReportColumns(ReportType.SALES_PRODUCT)]
    }

    // The bottom level of the main sales report.
    def ajaxSalesProduct(SortParams sortParams) {
        int productId = getIntegerParam(params.productId)

        sortParams.validateParams(SALES_REPORT_PRODUCT_SORT_COLUMNS)

        DateTimeFormatter dateFormatter = DateTimeFormat.forPattern("dd/MM/yyyy").withZoneUTC()
        DateTime startDate = params.startDate ? DateTime.parse(params.startDate, dateFormatter).withTimeAtStartOfDay() : DateTime.now(DateTimeZone.UTC).withTimeAtStartOfDay()
        DateTime endDate = params.startDate ? DateTime.parse(params.endDate, dateFormatter).withTimeAtStartOfDay() : DateTime.now(DateTimeZone.UTC).withTimeAtStartOfDay()

        def sales = reportingService.getSalesForProduct(productId, startDate,endDate.plusDays(1), sortParams.max, sortParams.offset, sortParams.sortColumn, sortParams.sortOrder, params.descriptionFilter)

        def totalResults = reportingService.countSalesForProduct(productId, startDate,endDate.plusDays(1), params.descriptionFilter)

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

        [reportType : ReportType.CATEGORY_SALES,
         startDate  : startDate,
         endDate    : endDate,
         userColumns: reportingService.getReportColumns(ReportType.CATEGORY_SALES)]
    }

    // The standalone category sales report.
    def ajaxCategorySales(SortParams sortParams) {
        sortParams.validateParams(SALES_REPORT_CATEGORY_SORT_COLUMNS)

        int maxCategoryLevel = getIntegerParam(params.maxCategoryLevel)

        DateTimeFormatter dateFormatter = DateTimeFormat.forPattern("dd/MM/yyyy").withZoneUTC()
        DateTime startDate = params.startDate ? DateTime.parse(params.startDate, dateFormatter).withTimeAtStartOfDay() : DateTime.now(DateTimeZone.UTC).withTimeAtStartOfDay()
        DateTime endDate = params.startDate ? DateTime.parse(params.endDate, dateFormatter).withTimeAtStartOfDay() : DateTime.now(DateTimeZone.UTC).withTimeAtStartOfDay()

        // Find all sales in the date range.
        def sales = reportingService.getSales(springSecurityService.principal.storeId, startDate, endDate.plusDays(1))

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

            def salesGroupedCurrentLevel = salesGroup.value.groupBy { Sale sale -> sale.salesCategories?.find {it.categoryLevel == currentCategoryLevel + 1 }?.categoryId }

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

        [reportType : ReportType.SALES,
         startDate  : startDate,
         endDate    : endDate,
         userColumns: reportingService.getReportColumns(ReportType.SALES)]
    }

    // The standalone product sales report.
    def ajaxSales(SortParams sortParams) {
        Integer storeId = springSecurityService.principal.storeId

        sortParams.validateParams(SALES_REPORT_SORT_COLUMNS)

        DateTimeFormatter dateFormatter = DateTimeFormat.forPattern("dd/MM/yyyy").withZoneUTC()
        DateTime startDate = params.startDate ? DateTime.parse(params.startDate, dateFormatter).withTimeAtStartOfDay() : DateTime.now(DateTimeZone.UTC).withTimeAtStartOfDay()
        DateTime endDate = params.startDate ? DateTime.parse(params.endDate, dateFormatter).withTimeAtStartOfDay() : DateTime.now(DateTimeZone.UTC).withTimeAtStartOfDay()

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

        [reportType: ReportType.PROMOTIONS_GROUPED, userColumns: reportingService.getReportColumns(ReportType.PROMOTIONS_GROUPED), promotionTypes: PromotionType.values(), startDate: startDate, endDate: endDate]
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

        // Find all promotion sales in the date range.
        def promotionSales = reportingService.getPromotionSales(startDate,endDate.plusDays(1), params.descriptionFilter, params.promotionTypeFilter)

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

        // Restrict the number of results.
        int totalResults = finalPromotionSales.size()
        finalPromotionSales = sortParams.offset < finalPromotionSales.size() ? finalPromotionSales.subList(sortParams.offset, (sortParams.offset + sortParams.max < finalPromotionSales.size() ? sortParams.offset + sortParams.max : finalPromotionSales.size())) : []

        render (template: "promotionsGroupedResults", model: [promotionSales: finalPromotionSales, userColumns: reportingService.getReportColumns(ReportType.PROMOTIONS_GROUPED), sortParams: sortParams, startDate: startDate, endDate: endDate, totalResults: totalResults])
    }

    def promotions() {
        int promotionId = getIntegerParam(params.promotionId)
        DateTimeFormatter dateFormatter = DateTimeFormat.forPattern("dd/MM/yyyy").withZoneUTC()
        DateTime startDate = params.startDate ? DateTime.parse(params.startDate, dateFormatter).withTimeAtStartOfDay() : DateTime.now(DateTimeZone.UTC).withTimeAtStartOfDay()
        DateTime endDate = params.startDate ? DateTime.parse(params.endDate, dateFormatter).withTimeAtStartOfDay() : DateTime.now(DateTimeZone.UTC).withTimeAtStartOfDay()

        [reportType: ReportType.PROMOTIONS, promotionId: promotionId, startDate: startDate, endDate: endDate, userColumns: reportingService.getReportColumns(ReportType.PROMOTIONS)]
    }

    def ajaxPromotions(SortParams sortParams) {
        int promotionId = getIntegerParam(params.promotionId)

        sortParams.validateParams(PROMOTIONS_REPORT_SORT_COLUMNS)

        DateTimeFormatter dateFormatter = DateTimeFormat.forPattern("dd/MM/yyyy").withZoneUTC()
        DateTime startDate = params.startDate ? DateTime.parse(params.startDate, dateFormatter).withTimeAtStartOfDay() : DateTime.now(DateTimeZone.UTC).withTimeAtStartOfDay()
        DateTime endDate = params.startDate ? DateTime.parse(params.endDate, dateFormatter).withTimeAtStartOfDay() : DateTime.now(DateTimeZone.UTC).withTimeAtStartOfDay()

        // Find all promotion sales for this promotion in the date range.
        def promotionSales = reportingService.getPromotionSales(startDate,endDate.plusDays(1), promotionId, sortParams.max, sortParams.offset, sortParams.sortColumn, sortParams.sortOrder)

        render (template: "promotionsResults", model: [promotionId: promotionId, promotionSales: promotionSales, userColumns: reportingService.getReportColumns(ReportType.PROMOTIONS), sortParams: sortParams, startDate: startDate, endDate: endDate, totalResults: promotionSales.totalCount])
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

        render (template: "promotionResults", model: [promotionSaleProducts: promotionSaleProducts, userColumns: reportingService.getReportColumns(ReportType.PROMOTION), sortParams: sortParams, totalResults: promotionSaleProducts.totalCount])
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
        def tillControlEvents = reportingService.getTillControlEvents(startDate,endDate.plusDays(1))

        // Group them by type.
        def tillControlEventsGrouped = tillControlEvents.groupBy { it.type }

        // Sort into the required order.
        Comparator comparator

        if (sortParams.sortColumn == "type") {
            comparator = [ compare: { a, b ->
                if (sortParams.sortOrder == "desc") {
                    a.compareTo(b)
                } else {
                    b.compareTo(a)
                }
            }] as Comparator

            tillControlEventsGrouped = tillControlEventsGrouped.sort(comparator)
        } else if (sortParams.sortColumn == "quantity") {
            comparator = [ compare: { a, b ->
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

        // Restrict the number of results.
        int totalResults = tillControlEventsGrouped.size()
//        tillControlEventsGrouped = offset < tillControlEventsGrouped.size() ? tillControlEventsGrouped.subList(offset, (offset + max < tillControlEventsGrouped.size() ? offset + max : tillControlEventsGrouped.size())) : []

        render (template: "tillControlEventsResults", model: [tillControlEvents: tillControlEventsGrouped, userColumns: reportingService.getReportColumns(ReportType.TILL_CONTROL_EVENTS), sortParams: sortParams, startDate: startDate, endDate: endDate, totalResults: totalResults])
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

        render (template: "tillControlEventResults", model: [tillControlEvents: tillControlEvents, userColumns: reportingService.getReportColumns(ReportType.TILL_CONTROL_EVENT), sortParams: sortParams, totalResults: tillControlEvents.totalCount])
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

        stringBuilder.append("Description,Total Quantity,Avg Cost Price,Avg Sales Price,Total Sales,VAT Amount,Avg Margin\n")

        sales?.each {
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
