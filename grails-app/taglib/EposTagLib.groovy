import uk.co.wonderlane.wlpos.ButtonGrid
import uk.co.wonderlane.wlpos.enums.ButtonGridType
import uk.co.wonderlane.wlpos.enums.TillControlEventType
import uk.co.wonderlane.wlpos.reporting.ReportType

class EposTagLib {

    def springSecurityService
    def reportingService
    def categoryService
    def productService
    def promotionService

    def quicksellMenu = { attrs, body ->
        def buttonGrids = ButtonGrid.findAllByTypeAndRetailerIdAndStoreId(ButtonGridType.OTHER, springSecurityService.principal.retailerId, springSecurityService.principal.storeId)

        buttonGrids.each { buttonGrid ->
            out << """<a class="dropdown-item" href="${createLink(controller: "buttonGrid", action: "show", id: buttonGrid.id)}">${buttonGrid.description}</a>"""
        }
    }

    def paginateReport = { attrs, body ->
        if (attrs.totalResults > attrs.max) {
            if (attrs.offset > 0) {
                out << """<a class="prevLink" href="#" onclick="getReportData({ max: ${attrs.max}, offset: ${attrs.offset - attrs.max}, sortColumn: '${attrs.sortColumn}', sortOrder: '${attrs.sortOrder}' });">Previous</a>"""
            }

            for (int i = 0 ; (i * attrs.max) < attrs.totalResults ; i++) {
                if (attrs.offset >= (i) * attrs.max && attrs.offset < ((i + 1) * attrs.max)) {
                    out << """<span class="currentStep">${i + 1}</span>"""
                } else {
                    out << """<a class="step" href="#" onclick="getReportData({ max: ${attrs.max}, offset: ${(i) * attrs.max}, sortColumn: '${attrs.sortColumn}', sortOrder: '${attrs.sortOrder}' });">${i + 1}</a>"""
                }
            }

            if ((attrs.offset + attrs.max) < attrs.totalResults) {
                out << """<a class="nextLink" href="#" onclick="getReportData({ max: ${attrs.max}, offset: ${attrs.offset + attrs.max}, sortColumn: '${attrs.sortColumn}', sortOrder: '${attrs.sortOrder}' });">Next</a>"""
            }
        }
    }

    def reportBreadcrumb = { attrs, body ->
        out << """<nav aria-label="breadcrumb"><div class="row mt-4"><div class="col"><ol class="breadcrumb">"""

        switch ((ReportType)attrs.reportType) {
            case ReportType.SALES_DEPARTMENT:
                out << """<li class="breadcrumb-item active" aria-current="page">All</li>"""

                break;
            case ReportType.SALES_CATEGORY:
                def categories = categoryService.getCategoryHierarchy(attrs.categoryId)

                out << """<li class="breadcrumb-item">${g.link(action:"salesDepartment", params:[startDate: attrs.startDate?.format('dd/MM/yyyy'), endDate: attrs.endDate?.format('dd/MM/yyyy')]) { "All" }}"""

                categories.each { category ->
                    if (category.id == attrs.categoryId) {
                        out << """<li class="breadcrumb-item active" aria-current="page">${category.description}</li>"""
                    } else {
                        out << """<li class="breadcrumb-item">${g.link(action:"salesCategory", params:[categoryId: category.id, startDate: attrs.startDate?.format('dd/MM/yyyy'), endDate: attrs.endDate?.format('dd/MM/yyyy')]) { category.description }}"""
                    }
                }

                break;
            case ReportType.SALES_PRODUCT:
                def product = productService.getProduct(attrs.productId)
                def categories = categoryService.getCategoryHierarchy(product.category.id)

                out << """<li class="breadcrumb-item">${g.link(action:"salesDepartment", params:[startDate: attrs.startDate?.format('dd/MM/yyyy'), endDate: attrs.endDate?.format('dd/MM/yyyy')]) { "All" }}"""

                categories.each { category ->
                    out << """<li class="breadcrumb-item">${g.link(action:"salesCategory", params:[categoryId: category.id, startDate: attrs.startDate?.format('dd/MM/yyyy'), endDate: attrs.endDate?.format('dd/MM/yyyy')]) { category.description }}"""
                }

                out << """<li class="breadcrumb-item active" aria-current="page">${product.description}</li>"""

                break;
            case ReportType.PROMOTIONS_GROUPED:
                out << """<li class="breadcrumb-item active" aria-current="page">All</li>"""

                break;
            case ReportType.PROMOTIONS:
                def promotion = promotionService.getPromotion(attrs.promotionId)

                out << """<li class="breadcrumb-item">${g.link(action:"promotionsGrouped", params:[startDate: attrs.startDate?.format('dd/MM/yyyy'), endDate: attrs.endDate?.format('dd/MM/yyyy')]) { "All" }}"""
                out << """<li class="breadcrumb-item active" aria-current="page">${promotion.description}</li>"""

                break;
            case ReportType.PROMOTION:
                def promotionSale = reportingService.getPromotionSale(attrs.promotionSaleId)
                def promotion = promotionService.getPromotion(promotionSale.promotionId)

                out << """<li class="breadcrumb-item">${g.link(action:"promotionsGrouped", params:[startDate: attrs.startDate?.format('dd/MM/yyyy'), endDate: attrs.endDate?.format('dd/MM/yyyy')]) { "All" }}"""
                out << """<li class="breadcrumb-item">${g.link(action:"promotions", params:[promotionId: promotion.id, startDate: attrs.startDate?.format('dd/MM/yyyy'), endDate: attrs.endDate?.format('dd/MM/yyyy')]) { promotion.description }}"""
                out << """<li class="breadcrumb-item active" aria-current="page">${promotion.description}</li>"""

                break;
            case ReportType.TILL_CONTROL_EVENTS:
                out << """<li class="breadcrumb-item active" aria-current="page">All</li>"""

                break;
            case ReportType.TILL_CONTROL_EVENT:
                out << """<li class="breadcrumb-item">${g.link(action:"tillControlEvents", params:[startDate: attrs.startDate?.format('dd/MM/yyyy'), endDate: attrs.endDate?.format('dd/MM/yyyy')]) { "All" }}"""
                out << """<li class="breadcrumb-item active" aria-current="page">${g.message(code: 'TillControlEventType.' +attrs.tillControlEventType)}</li>"""

                break;
            default:
                out << ""

                break;
        }

        out << """</ol></div></div></nav>"""
    }
}