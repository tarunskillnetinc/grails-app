package uk.co.wonderlane.wlpos

import groovy.json.JsonSlurper
import org.joda.time.DateTime
import uk.co.wonderlane.wlpos.enums.TillControlEventType
import uk.co.wonderlane.wlpos.reporting.PromotionSale
import uk.co.wonderlane.wlpos.reporting.ReportColumn
import uk.co.wonderlane.wlpos.reporting.ReportColumns
import uk.co.wonderlane.wlpos.reporting.ReportType
import uk.co.wonderlane.wlpos.reporting.Sale
import uk.co.wonderlane.wlpos.reporting.SaleCategory
import uk.co.wonderlane.wlpos.reporting.SortParams

class ReportingController {

    def reportingService
    def springSecurityService

    private static final SALES_REPORT_CATEGORY_SORT_COLUMNS = [ "description", "quantity", "avgCostPrice", "avgRetailPrice", "retailPrice", "vatAmount", "avgMargin" ]
    private static final SALES_REPORT_PRODUCT_SORT_COLUMNS = [ "usersName", "category", "description", "quantity", "costPrice", "netTotal", "vatAmount", "profit", "margin", "dateCreated" ]
    private static final PROMOTIONS_REPORT_SORT_COLUMNS = [ "type", "description", "quantity", "fullPrice", "discount", "margin", "profit", "vat", "dateCreated" ]
    private static final PROMOTION_REPORT_SORT_COLUMNS = [ "itemCode", "description", "costPrice", "fullPrice", "fullPriceMargin", "fullPriceProfit", "discount", "discountedPrice", "discountedMargin", "discountedProfit", "vat" ]
    private static final TILL_CONTROL_EVENTS_REPORT_SORT_COLUMNS = [ "type", "quantity" ]
    private static final TILL_CONTROL_EVENT_REPORT_SORT_COLUMNS = [ "type", "usersName", "reason", "dateCreated", "amount" ]

    def index() {

    }

    def salesDepartment() {
        [reportType: ReportType.SALES_DEPARTMENT, userColumns: reportingService.getReportColumns(ReportType.SALES_DEPARTMENT)]
    }

    def ajaxSalesDepartment(SortParams sortParams) {
        sortParams.validateParams(SALES_REPORT_CATEGORY_SORT_COLUMNS)

        Date startDate = new DateTime().minusDays(7).withTimeAtStartOfDay().toDate()
        Date endDate = new DateTime().plusDays(1).withTimeAtStartOfDay().toDate()

        // Find all sales involving this category in the date range.
        def sales = reportingService.getSales(startDate, endDate)

        // Filter our results.
        if (params.searchText) {
            sales = sales.findAll { it.salesCategories.first().categoryDescription.toLowerCase().contains(params.searchText.toLowerCase()) }
        }

        // Group them by department.
        def salesGrouped = sales.groupBy { sale ->
            sale.salesCategories.first().categoryId
        }

        def finalSales = []

        // Populating a dummy sale object for any of the sales which are not in this category (because they have summed values for everything in that category).
        salesGrouped.each { salesGroup ->
            Sale groupedSale = new Sale(
                    quantity: salesGroup.value.sum { it.quantity },
                    costPrice: salesGroup.value.sum { it.costPrice },
                    retailPrice: salesGroup.value.sum { it.retailPrice },
                    vatAmount: salesGroup.value.sum { it.vatAmount },
                    margin: salesGroup.value.sum { it.margin },
                    productDescription: salesGroup.value[0].salesCategories.find { sc -> sc.categoryId == salesGroup.key }.categoryDescription,
                    productUnitSize: ""
            )

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

        // Restrict the number of results.
        int totalResults = finalSales.size()
        finalSales = sortParams.offset < finalSales.size() ? finalSales.subList(sortParams.offset, (sortParams.offset + sortParams.max < finalSales.size() ? sortParams.offset + sortParams.max : finalSales.size())) : []

        render (template: "salesDepartmentResults", model: [sales: finalSales, userColumns: reportingService.getReportColumns(ReportType.SALES_DEPARTMENT), sortParams: sortParams, searchText: params.searchText, totalResults: totalResults])
    }

    def salesCategory() {
        int categoryId = getIntegerParam(params.categoryId)

        [reportType: ReportType.SALES_CATEGORY, categoryId: categoryId, userColumns: reportingService.getReportColumns(ReportType.SALES_CATEGORY)]
    }

    def ajaxSalesCategory(SortParams sortParams) {
        int categoryId = getIntegerParam(params.categoryId)

        sortParams.validateParams(SALES_REPORT_CATEGORY_SORT_COLUMNS)

        Date startDate = new DateTime().minusDays(7).withTimeAtStartOfDay().toDate()
        Date endDate = new DateTime().plusDays(1).withTimeAtStartOfDay().toDate()

        // Find all sales involving this category in the date range.
        def sales = reportingService.getSalesForCategory(categoryId, startDate, endDate)

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
                def filteredProductSales = params.searchText ? salesGroup.value.findAll { (it.productItemCode.toLowerCase() + it.productDescription.toLowerCase()).contains(params.searchText.toLowerCase()) } : salesGroup.value

                def filteredGroupedProductSales = filteredProductSales?.groupBy { it.productId }

                filteredGroupedProductSales?.each { groupedProductSale ->
                    groupedProductSale.value[0].quantity = groupedProductSale.value.sum { it.quantity }
                    groupedProductSale.value[0].costPrice = groupedProductSale.value.sum { it.costPrice }
                    groupedProductSale.value[0].retailPrice = groupedProductSale.value.sum { it.retailPrice }
                    groupedProductSale.value[0].vatAmount = groupedProductSale.value.sum { it.vatAmount }
                    groupedProductSale.value[0].margin = groupedProductSale.value.sum { it.margin }

                    finalSales.add(groupedProductSale.value[0])
                }
            } else {
                if (!params.searchText || salesGroup.value[0].salesCategories.find { sc -> sc.categoryId == salesGroup.key }.categoryDescription.toLowerCase().contains(params.searchText.toLowerCase())) {
                    Sale groupedSale = new Sale(
                            quantity: salesGroup.value.sum { it.quantity },
                            costPrice: salesGroup.value.sum { it.costPrice },
                            retailPrice: salesGroup.value.sum { it.retailPrice },
                            vatAmount: salesGroup.value.sum { it.vatAmount },
                            margin: salesGroup.value.sum { it.margin },
                            productDescription: salesGroup.value[0].salesCategories.find { sc -> sc.categoryId == salesGroup.key }.categoryDescription,
                            productUnitSize: ""
                    )

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

        // Restrict the number of results.
        int totalResults = finalSales.size()
        finalSales = sortParams.offset < finalSales.size() ? finalSales.subList(sortParams.offset, (sortParams.offset + sortParams.max < finalSales.size() ? sortParams.offset + sortParams.max : finalSales.size())) : []

        render (template: "salesCategoryResults", model: [categoryId: categoryId, sales: finalSales, userColumns: reportingService.getReportColumns(ReportType.SALES_CATEGORY), sortParams: sortParams, searchText: params.searchText, totalResults: totalResults])
    }

    def salesProduct() {
        int productId = getIntegerParam(params.productId)

        [reportType: ReportType.SALES_PRODUCT, productId: productId, userColumns: reportingService.getReportColumns(ReportType.SALES_PRODUCT)]
    }

    def ajaxSalesProduct(SortParams sortParams) {
        int productId = getIntegerParam(params.productId)

        sortParams.validateParams(SALES_REPORT_PRODUCT_SORT_COLUMNS)

        Date startDate = new DateTime().minusDays(7).withTimeAtStartOfDay().toDate()
        Date endDate = new DateTime().plusDays(1).withTimeAtStartOfDay().toDate()

        def sales = reportingService.getSalesForProduct(productId, startDate, endDate, sortParams.max, sortParams.offset, sortParams.sortColumn, sortParams.sortOrder)

        // Filter our results.
        if (params.searchText) {
            sales = sales findAll { (it.productItemCode + it.productDescription).toLowerCase().contains(params.searchText.toLowerCase()) }
        }

        // Restrict the number of results.
        int totalResults = sales.size()
        sales = sortParams.offset < sales.size() ? sales.subList(sortParams.offset, (sortParams.offset + sortParams.max < sales.size() ? sortParams.offset + sortParams.max : sales.size())) : []

        render (template: "salesProductResults", model: [sales: sales, userColumns: reportingService.getReportColumns(ReportType.SALES_PRODUCT), sortParams: sortParams, searchText: params.searchText, totalResults: totalResults])
    }

    def tillControlEvents() {
        [reportType: ReportType.TILL_CONTROL_EVENTS, userColumns: reportingService.getReportColumns(ReportType.TILL_CONTROL_EVENTS)]
    }

    def ajaxTillControlEvents(SortParams sortParams) {
        sortParams.validateParams(TILL_CONTROL_EVENTS_REPORT_SORT_COLUMNS)

        Date startDate = new DateTime().minusDays(14).withTimeAtStartOfDay().toDate()
        Date endDate = new DateTime().plusDays(1).withTimeAtStartOfDay().toDate()

        // Find all till control events in the date range.
        def tillControlEvents = reportingService.getTillControlEvents(startDate, endDate)

        // Filter our results.
        if (params.searchText) {
            tillControlEvents = tillControlEvents.findAll { it.type.toString().toLowerCase().contains(params.searchText.toLowerCase()) }
        }

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

        render (template: "tillControlEventsResults", model: [tillControlEvents: tillControlEventsGrouped, userColumns: reportingService.getReportColumns(ReportType.TILL_CONTROL_EVENTS), sortParams: sortParams, searchText: params.searchText, totalResults: totalResults])
    }

    def tillControlEvent() {
        TillControlEventType type = null

        try {
            type = TillControlEventType.valueOf(params.type)
        } catch (Exception e) {
            // No action, simply return no results.
        }

        [reportType: ReportType.TILL_CONTROL_EVENT, type: type, userColumns: reportingService.getReportColumns(ReportType.TILL_CONTROL_EVENT)]
    }

    def ajaxTillControlEvent(SortParams sortParams) {
        TillControlEventType type = null

        try {
            type = TillControlEventType.valueOf(params.type)
        } catch (Exception e) {
            // No action, simply return no results.
        }

        sortParams.validateParams(TILL_CONTROL_EVENT_REPORT_SORT_COLUMNS)

        Date startDate = new DateTime().minusDays(14).withTimeAtStartOfDay().toDate()
        Date endDate = new DateTime().plusDays(1).withTimeAtStartOfDay().toDate()

        // Find all till control events in the date range.
        def tillControlEvents = reportingService.getTillControlEvents(startDate, endDate, type, sortParams.max, sortParams.offset, sortParams.sortColumn, sortParams.sortOrder)
        // Filter our results.
//        if (params.searchText) {
//            tillControlEvents = tillControlEvents.findAll { it.type.toString().toLowerCase().contains(params.searchText.toLowerCase()) }
//        }

        // Sort into the required order.
//        tillControlEvents = tillControlEvents.sort { it."${sortColumn}"}

//        if (sortOrder == "desc") {
//            tillControlEvents = tillControlEvents.reverse();
//        }
//
        // Restrict the number of results.
        int totalResults = tillControlEvents.totalCount
//        tillControlEvents = offset < tillControlEvents.size() ? tillControlEvents.subList(offset, (offset + max < tillControlEvents.size() ? offset + max : tillControlEvents.size())) : []

        render (template: "tillControlEventResults", model: [tillControlEvents: tillControlEvents, userColumns: reportingService.getReportColumns(ReportType.TILL_CONTROL_EVENT), sortParams: sortParams, searchText: params.searchText, totalResults: totalResults])
    }

    def promotionsGrouped() {
        [reportType: ReportType.PROMOTIONS_GROUPED, userColumns: reportingService.getReportColumns(ReportType.PROMOTIONS_GROUPED)]
    }

    def ajaxPromotionsGrouped(SortParams sortParams) {
        sortParams.validateParams(PROMOTIONS_REPORT_SORT_COLUMNS)

        Date startDate = new DateTime().minusDays(7).withTimeAtStartOfDay().toDate()
        Date endDate = new DateTime().plusDays(1).withTimeAtStartOfDay().toDate()

        // Find all promotion sales in the date range.
        def promotionSales = reportingService.getPromotionSales(startDate, endDate)

        // Filter our results.
        if (params.searchText) {
            promotionSales = promotionSales.findAll { it.description.toLowerCase().contains(params.searchText.toLowerCase()) }
        }

        // Group them by promotion ID.
        def promotionSalesGrouped = promotionSales.groupBy { it.promotionId }

        def finalPromotionSales = []

        // Populating a dummy sale object for any of the sales which are not in this category (because they have summed values for everything in that category).
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

        render (template: "promotionsGroupedResults", model: [promotionSales: finalPromotionSales, userColumns: reportingService.getReportColumns(ReportType.PROMOTIONS_GROUPED), sortParams: sortParams, searchText: params.searchText, totalResults: totalResults])
    }

    def promotions() {
        int promotionId = getIntegerParam(params.promotionId)

        [promotionId: promotionId, userColumns: reportingService.getReportColumns(ReportType.PROMOTIONS)]
    }

    def ajaxPromotions(SortParams sortParams) {
        int promotionId = getIntegerParam(params.promotionId)

        sortParams.validateParams(PROMOTIONS_REPORT_SORT_COLUMNS)

        Date startDate = new DateTime().minusDays(7).withTimeAtStartOfDay().toDate()
        Date endDate = new DateTime().plusDays(1).withTimeAtStartOfDay().toDate()

        // Find all promotion sales for this promotion in the date range.
        def promotionSales = reportingService.getPromotionSales(startDate, endDate, promotionId)

        // Filter our results.
        if (params.searchText) {
            promotionSales = promotionSales.findAll { it.description.toLowerCase().contains(params.searchText.toLowerCase()) }
        }

        // Sort into the required order.
        promotionSales.sort { it."${sortParams.sortColumn}" }

        if (sortParams.sortOrder == "desc") {
            promotionSales = promotionSales.reverse()
        }

        // Restrict the number of results.
        int totalResults = promotionSales.size()
        promotionSales = sortParams.offset < promotionSales.size() ? promotionSales.subList(sortParams.offset, (sortParams.offset + sortParams.max < promotionSales.size() ? sortParams.offset + sortParams.max : promotionSales.size())) : []

        render (template: "promotionsResults", model: [promotionId: promotionId, promotionSales: promotionSales, userColumns: reportingService.getReportColumns(ReportType.PROMOTIONS), sortParams: sortParams, searchText: params.searchText, totalResults: totalResults])
    }

    def promotion() {
        int promotionSaleId = getIntegerParam(params.promotionSaleId)

        [promotionSaleId: promotionSaleId, userColumns: reportingService.getReportColumns(ReportType.PROMOTION)]
    }

    def ajaxPromotion(SortParams sortParams) {
        int promotionSaleId = getIntegerParam(params.promotionSaleId)

        sortParams.validateParams(PROMOTION_REPORT_SORT_COLUMNS)

        // Find all promotion sale products for this promotion sale.
        def promotionSaleProducts = reportingService.getPromotionSaleProducts(promotionSaleId)

        // Filter our results.
        if (params.searchText) {
            promotionSaleProducts = promotionSaleProducts.findAll { it.description.toLowerCase().contains(params.searchText.toLowerCase()) }
        }

        // Sort into the required order.
        promotionSaleProducts.sort { it."${sortParams.sortColumn}" }

        if (sortParams.sortOrder == "desc") {
            promotionSaleProducts = promotionSaleProducts.reverse()
        }

        // Restrict the number of results.
        int totalResults = promotionSaleProducts.size()
        promotionSaleProducts = sortParams.offset < promotionSaleProducts.size() ? promotionSaleProducts.subList(sortParams.offset, (sortParams.offset + sortParams.max < promotionSaleProducts.size() ? sortParams.offset + sortParams.max : promotionSaleProducts.size())) : []

        render (template: "promotionResults", model: [promotionSaleProducts: promotionSaleProducts, userColumns: reportingService.getReportColumns(ReportType.PROMOTION), sortParams: sortParams, searchText: params.searchText, totalResults: totalResults])
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
