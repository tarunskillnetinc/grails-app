package uk.co.wonderlane.wlpos

import org.joda.time.DateTime
import uk.co.wonderlane.wlpos.enums.TillControlEventType

class ReportingController {

    def reportingService

    private static final SALES_REPORT_TRANSACTION_SORT_COLUMNS = [ "usersName", "category", "description", "quantity", "costPrice", "netTotal", "vatAmount", "profit", "margin", "dateCreated" ]
    private static final SALES_REPORT_CATEGORY_SORT_COLUMNS = [ "description", "quantity", "avgCostPrice", "avgRetailPrice", "retailPrice", "vatAmount", "avgMargin" ]
    private static final TILL_CONTROL_EVENTS_REPORT_SORT_COLUMNS = [ "type", "quantity" ]
    private static final TILL_CONTROL_EVENT_REPORT_SORT_COLUMNS = [ "type", "usersName", "reason", "dateCreated", "amount" ]

    def index() {

    }

    def salesDepartments() {
        int max = getMax(params.max)
        int offset = getOffset(params.offset)
        String sortColumn = getSortColumn(SALES_REPORT_CATEGORY_SORT_COLUMNS, params.sortColumn)
        String sortOrder = getSortOrder(params.sortOrder)

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
        if (sortColumn == "description") {
            finalSales.sort { it.productItemCode ? it.productItemCode + it.productDescription : it.productDescription }
        } else {
            finalSales.sort { it."$sortColumn" }
        }

        if (sortOrder == "desc") {
            finalSales = finalSales.reverse()
        }

        // Restrict the number of results.
        int totalResults = finalSales.size()
        finalSales = offset < finalSales.size() ? finalSales.subList(offset, (offset + max < finalSales.size() ? offset + max : finalSales.size())) : []

        [sales: finalSales, max: max, searchText: params.searchText, offset: offset, totalResults: totalResults, sortColumn: sortColumn, sortOrder: sortOrder]
    }

    def salesCategory() {
        int categoryId = getIntegerParam(params.categoryId)
        int max = getMax(params.max)
        int offset = getOffset(params.offset)
        String sortColumn = getSortColumn(SALES_REPORT_CATEGORY_SORT_COLUMNS, params.sortColumn)
        String sortOrder = getSortOrder(params.sortOrder)

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
        if (sortColumn == "description") {
            finalSales.sort { it.productItemCode ? it.productItemCode + it.productDescription : it.productDescription }
        } else {
            finalSales.sort { it."$sortColumn" }
        }

        if (sortOrder == "desc") {
            finalSales = finalSales.reverse()
        }

        // Restrict the number of results.
        int totalResults = finalSales.size()
        finalSales = offset < finalSales.size() ? finalSales.subList(offset, (offset + max < finalSales.size() ? offset + max : finalSales.size())) : []

        [categoryId: categoryId, sales: finalSales, searchText: params.searchText, max: max, offset: offset, totalResults: totalResults, sortColumn: sortColumn, sortOrder: sortOrder]
    }

    def salesProduct() {
        int productId = getIntegerParam(params.productId)
        int max = getMax(params.max)
        int offset = getOffset(params.offset)
        String sortColumn = getSortColumn(SALES_REPORT_TRANSACTION_SORT_COLUMNS, params.sortColumn)
        String sortOrder = getSortOrder(params.sortOrder)

        Date startDate = new DateTime().minusDays(7).withTimeAtStartOfDay().toDate()
        Date endDate = new DateTime().plusDays(1).withTimeAtStartOfDay().toDate()

        def sales = reportingService.getSalesForProduct(productId, startDate, endDate, max, offset, sortColumn, sortOrder)

        // Filter our results.
        if (params.searchText) {
            sales = sales findAll { (it.productItemCode + it.productDescription).toLowerCase().contains(params.searchText.toLowerCase()) }
        }

        // Restrict the number of results.
        int totalResults = sales.size()
        sales = offset < sales.size() ? sales.subList(offset, (offset + max < sales.size() ? offset + max : sales.size())) : []

        [productId: productId, sales: sales, searchText: params.searchText, max: max, offset: offset, totalResults: totalResults, sortColumn: sortColumn, sortOrder: sortOrder]
    }

    def tillControlEvents() {
        int max = getMax(params.max)
        int offset = getOffset(params.offset)
        String sortColumn = getSortColumn(TILL_CONTROL_EVENTS_REPORT_SORT_COLUMNS, params.sortColumn ?: "type")
        String sortOrder = getSortOrder(params.sortOrder)

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

        if (sortColumn == "type") {
            comparator = [ compare: { a, b ->
                if (sortOrder == "desc") {
                    a.compareTo(b)
                } else {
                    b.compareTo(a)
                }
            }] as Comparator

            tillControlEventsGrouped = tillControlEventsGrouped.sort(comparator)
        } else if (sortColumn == "quantity") {
            comparator = [ compare: { a, b ->
                if (sortOrder == "desc") {
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

        [tillControlEvents: tillControlEventsGrouped, max: max, searchText: params.searchText, offset: offset, totalResults: totalResults, sortColumn: sortColumn, sortOrder: sortOrder]
    }

    def tillControlEvent() {
        int max = getMax(params.max)
        int offset = getOffset(params.offset)
        String sortColumn = getSortColumn(TILL_CONTROL_EVENT_REPORT_SORT_COLUMNS, params.sortColumn)
        String sortOrder = getSortOrder(params.sortOrder)

        Date startDate = new DateTime().minusDays(14).withTimeAtStartOfDay().toDate()
        Date endDate = new DateTime().plusDays(1).withTimeAtStartOfDay().toDate()

        TillControlEventType type = null

        try {
            type = TillControlEventType.valueOf(params.type)
        } catch (Exception e) {
            // No action, simply return no results.
        }

        // Find all till control events in the date range.
        def tillControlEvents = reportingService.getTillControlEvents(startDate, endDate, type, max, offset, sortColumn, sortOrder)

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
        int totalResults = tillControlEvents.size()
//        tillControlEvents = offset < tillControlEvents.size() ? tillControlEvents.subList(offset, (offset + max < tillControlEvents.size() ? offset + max : tillControlEvents.size())) : []

        [tillControlEvents: tillControlEvents, type: type, max: max, searchText: params.searchText, offset: offset, totalResults: totalResults, sortColumn: sortColumn, sortOrder: sortOrder]
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
    private static int getMax(max) {
        if (!max || !max.isNumber() || max.length() > 9) {
            return 50
        }

        return Integer.parseInt(max)
    }

    /**
     * Perform some sense checking on our input parameters.
     *
     * @param offset
     * @return
     */
    private static int getOffset(offset) {
        if (!offset || !offset.isNumber() || offset.length() > 9) {
            return 0
        }

        return Integer.parseInt(offset)
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
