package uk.co.wonderlane.wlpos

import org.joda.time.DateTime
import org.joda.time.DateTimeZone
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
        int offset = params.offset ? Integer.parseInt(params.offset) : 0
        int max = params.max ? Integer.parseInt(params.max) : 50

        def storeId = params.storeId
        if(storeId) {
            storeId = Integer.parseInt((String)storeId)
        }

        def productListItems = amendableOrderService.search(params.category, storeId)
        def stores = storeService.getStores((int)springSecurityService.principal.retailerId)?.sort { it.config.storeNumber + " - " + it.config.storeName }

        render(template: "orderSearchResults", model: [storeId     : springSecurityService.principal.storeId,
                                                            orders: productListItems,
                                                            stores: stores,
                                                            userColumns : getColumns(),
                                                            max         : max,
                                                            offset      : offset])
    }

    def viewCategory(int categoryId) {
        def category = categoryService.getCategory(categoryId)

        [categoryDescription: category.description]
    }

    def ajaxViewCategoryOrders() {
        int offset = params.offset ? Integer.parseInt(params.offset) : 0
        int max = params.max ? Integer.parseInt(params.max) : 50

        def amendOrderCommand = amendableOrderService.getOrdersForCategory(
                params.categoryId,
                params.sku,
                params.productDescription,
                params.deliveryDate)

        render(template: "categoryView", model: [
                amendCommand: amendOrderCommand
        ])
    }

    class AmendOrderCommand {
        Integer productListItemId
        String sku
        String productDescription
        BigDecimal price
        BigDecimal packQuantity
        DateTime deliveryDate
        BigDecimal originalOrderQuantity
        BigDecimal amendedOrderQuantity
        BigDecimal demand
        BigDecimal available
    }
}
