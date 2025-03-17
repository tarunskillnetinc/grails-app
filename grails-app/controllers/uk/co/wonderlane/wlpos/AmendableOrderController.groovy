package uk.co.wonderlane.wlpos

import grails.validation.Validateable
import org.apache.logging.log4j.core.util.Integers
import uk.co.wonderlane.wlpos.reporting.ReportType

class AmendableOrderController extends BaseController {
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
        if (!params.reportType || params.reportType == ReportType.AMENDABLE_ORDER_SEARCH.toString()) {
            return amendableOrderService.getColumns()
        } else {
            return amendableOrderService.getCategoryViewColumns()
        }
    }

    def getCategoryViewColumns() {
        return amendableOrderService.getCategoryViewColumns()
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

        def storeIdToSearchBy
        if (params.storeId) {
            storeIdToSearchBy = Integer.parseInt((String)params.storeId)
        } else {
            storeIdToSearchBy = springSecurityService.principal.storeId
        }

        def productListItems = amendableOrderService.search(params.category, storeIdToSearchBy)
        def totalResults = productListItems.size()
        productListItems = productListItems.drop(offset)?.take(max)
        def stores = storeService.getStores((int)springSecurityService.principal.retailerId)?.sort { it.config.storeNumber + " - " + it.config.storeName }

        render(template: "orderSearchResults", model: [     storeId: springSecurityService.principal.storeId != null ? springSecurityService.principal.storeId: params.storeId,
                                                            orders: productListItems,
                                                            category: params.category,
                                                            stores: stores,
                                                            userColumns : getColumns(),
                                                            max         : max,
                                                            offset      : offset,
                                                            totalResults: totalResults])
    }

    def viewCategory(int categoryId, int storeId) {
        def category = categoryService.getCategory(categoryId)
        def store = storeService.getStore(springSecurityService.principal.retailerId, storeId)

        [category: category, store: store]
    }

    def ajaxViewCategoryOrders() {
        int offset = params.offset ? Integer.parseInt((String)params.offset) : 0
        int max = params.max ? Integer.parseInt((String)params.max) : 50

        def amendedLines = amendableOrderService.getOrdersForCategory(
                Integers.parseInt((String)params.categoryId),
                params.sku,
                params.productDescription,
                params.deliveryDate,
                Integer.parseInt((String)params.storeId))

        def groupedLines = amendedLines.groupBy { it.sku }.collectEntries {key, value -> [new GroupedLine(
                sku: key,
                productDescription: value[0].productDescription,
                price: value[0].price,
                packQuantity: value[0].packQuantity,
                demand: value[0].demand,
                available: value[0].available,
                messages: value[0].messages
        ), value.sort { it.deliveryDate} ]}

        def totalResults = groupedLines.size()
        groupedLines = groupedLines.drop(offset)?.take(max)

        render(template: "categoryResults", model: [
                amendedLines: groupedLines,
                categoryId: params.categoryId,
                sku: params.sku,
                productDescription: params.productDescription,
                deliveryDate: params.deliveryDate,
                storeId: params.storeId,
                userColumns : getCategoryViewColumns(),
                max: max,
                offset: offset,
                totalResults: totalResults])
    }

    def save(SaveAmendedLinesCommand saveCommand) {
        bindData(saveCommand, params)

        if (saveCommand.validate()) {
            saveCommand.amendedLines.forEach {
                if (it.amendedOrderQuantity) {
                    amendableOrderService.saveAmendedQuantity(it)
                }
            }

            flash.message = "Order amended successfully"
            redirect("controller": "amendableOrder", action: "index")
        }
    }

    class GroupedLine {
        String sku
        String productDescription
        BigDecimal price
        BigDecimal packQuantity
        BigDecimal demand
        BigDecimal available
        String messages
        List<AmendableOrderService.AmendedLine> lines
    }
}

class SaveAmendedLinesCommand implements Validateable  {
    List<AmendableOrderService.AmendedLine> amendedLines = [].withLazyDefault { new AmendableOrderService.AmendedLine(null)}
}

