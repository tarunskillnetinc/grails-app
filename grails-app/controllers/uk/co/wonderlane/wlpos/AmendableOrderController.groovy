package uk.co.wonderlane.wlpos

import org.apache.logging.log4j.core.util.Integers
import uk.co.wonderlane.wlpos.reporting.ReportType

class AmendableOrderController extends BaseController {
    // TODO - What security do we want to put on all of this

    def springSecurityService
    def storeService
    def categoryService

    def index() {
        def stores = storeService.getStores((int)springSecurityService.principal.retailerId)?.sort { it.config.storeNumber + " - " + it.config.storeName }

        [userColumns: getColumns(),
         stores: stores]
    }

    @Override
    def getColumns() {
        return amendableOrderService.getColumns()
    }

    def ajaxSaveColumns() {
        params.reportType = ReportType.AMENDABLE_ORDER_SEARCH.toString()
        super.ajaxSaveColumns()
    }

    def ajaxSaveCategoryColumns() {
        params.reportType = ReportType.AMENDABLE_ORDER_VIEW_CATEGORY.toString()
        super.ajaxSaveColumns()
    }

    def ajaxSearchOrders() {
        int offset = params.offset ? Integer.parseInt((String)params.offset) : 0
        int max = params.max ? Integer.parseInt((String)params.max) : 50

        def storeId = params.storeId
        if(storeId) {
            storeId = Integer.parseInt((String)storeId)
        }

        def productListItems = amendableOrderService.search(params.category, storeId)
        def stores = storeService.getStores((int)springSecurityService.principal.retailerId)?.sort { it.config.storeNumber + " - " + it.config.storeName }

        render(template: "orderSearchResults", model: [     storeId: springSecurityService.principal.storeId != null ? springSecurityService.principal.storeId: params.storeId,
                                                            orders: productListItems,
                                                            category: params.category,
                                                            stores: stores,
                                                            userColumns : getColumns(),
                                                            max         : max,
                                                            offset      : offset])
    }

    def viewCategory(int categoryId) {
        def category = categoryService.getCategory(categoryId)

        [category: category]
    }

    def ajaxViewCategoryOrders() {
        int offset = params.offset ? Integer.parseInt((String)params.offset) : 0
        int max = params.max ? Integer.parseInt((String)params.max) : 50

        def amendedLines = amendableOrderService.getOrdersForCategory(
                Integers.parseInt((String)params.categoryId),
                params.sku,
                params.productDescription,
                params.deliveryDate)

        def groupedLines = amendedLines.groupBy { it.sku }.collectEntries {key, value -> [new GroupedLine(
                sku: key,
                productDescription: value[0].productDescription,
                price: value[0].price,
                packQuantity: value[0].packQuantity,
                demand: value[0].demand,
                available: value[0].available
        ), value ]}

        render(template: "categoryResults", model: [
                amendedLines: groupedLines,
                categoryId: params.categoryId,
                sku: params.sku,
                productDescription: params.productDescription,
                deliveryDate: params.deliveryDate,
                max: max,
                offset: offset
        ])
    }

    def save(SaveAmendedLinesCommand saveCommand) {
        bindData(saveCommand, params)

        saveCommand.amendedLines.forEach {
            if (it.amendedOrderQuantity) {
                amendableOrderService.saveAmendedQuantity(it)
            }
        }

        flash.message = "Order amended successfully"
        redirect("controller": "amendableOrder", action:"index")
    }

    class SaveAmendedLinesCommand {
        List<AmendableOrderService.AmendedLine> amendedLines = [].withLazyDefault { new AmendableOrderService.AmendedLine(null)}
    }

    class GroupedLine {
        String sku
        String productDescription
        BigDecimal price
        BigDecimal packQuantity
        BigDecimal demand
        BigDecimal available
        List<AmendableOrderService.AmendedLine> lines
    }
}
