import uk.co.wonderlane.wlpos.ButtonGrid
import uk.co.wonderlane.wlpos.Group
import uk.co.wonderlane.wlpos.enums.ButtonGridType
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

        out << """<li class="breadcrumb-item">${g.link(uri:"/") { "Home" }}"""

        switch ((ReportType)attrs.reportType) {
            case ReportType.SALES_DEPARTMENT:
                out << """<li class="breadcrumb-item active" aria-current="page">All Sales</li>"""

                break;
            case ReportType.SALES_CATEGORY:
                def categories = categoryService.getCategoryHierarchy(attrs.categoryId)

                out << """<li class="breadcrumb-item">${g.link(action:"salesDepartment", params:[startDate: attrs.startDate?.format('dd/MM/yyyy'), endDate: attrs.endDate?.format('dd/MM/yyyy')]) { "All Sales" }}"""

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

                out << """<li class="breadcrumb-item">${g.link(action:"salesDepartment", params:[startDate: attrs.startDate?.format('dd/MM/yyyy'), endDate: attrs.endDate?.format('dd/MM/yyyy')]) { "All Sales" }}"""

                categories.each { category ->
                    out << """<li class="breadcrumb-item">${g.link(action:"salesCategory", params:[categoryId: category.id, startDate: attrs.startDate?.format('dd/MM/yyyy'), endDate: attrs.endDate?.format('dd/MM/yyyy')]) { category.description }}"""
                }

                out << """<li class="breadcrumb-item active" aria-current="page">${product.description}</li>"""

                break;
            case ReportType.PROMOTIONS_GROUPED:
                out << """<li class="breadcrumb-item active" aria-current="page">All Sales</li>"""

                break;
            case ReportType.PROMOTIONS:
                def promotion = promotionService.getPromotion(attrs.promotionId)

                out << """<li class="breadcrumb-item">${g.link(action:"promotionsGrouped", params:[startDate: attrs.startDate?.format('dd/MM/yyyy'), endDate: attrs.endDate?.format('dd/MM/yyyy')]) { "All Sales" }}"""
                out << """<li class="breadcrumb-item active" aria-current="page">${promotion.description}</li>"""

                break;
            case ReportType.PROMOTION:
                def promotionSale = reportingService.getPromotionSale(attrs.promotionSaleId)
                def promotion = promotionService.getPromotion(promotionSale.promotionId)

                out << """<li class="breadcrumb-item">${g.link(action:"promotionsGrouped", params:[startDate: attrs.startDate?.format('dd/MM/yyyy'), endDate: attrs.endDate?.format('dd/MM/yyyy')]) { "All Sales" }}"""
                out << """<li class="breadcrumb-item">${g.link(action:"promotions", params:[promotionId: promotion.id, startDate: attrs.startDate?.format('dd/MM/yyyy'), endDate: attrs.endDate?.format('dd/MM/yyyy')]) { promotion.description }}"""
                out << """<li class="breadcrumb-item active" aria-current="page">${promotion.description}</li>"""

                break;
            case ReportType.TILL_CONTROL_EVENTS:
                out << """<li class="breadcrumb-item active" aria-current="page">All Events</li>"""

                break;
            case ReportType.TILL_CONTROL_EVENT:
                out << """<li class="breadcrumb-item">${g.link(action:"tillControlEvents", params:[startDate: attrs.startDate?.format('dd/MM/yyyy'), endDate: attrs.endDate?.format('dd/MM/yyyy')]) { "All Events" }}"""
                out << """<li class="breadcrumb-item active" aria-current="page">${g.message(code: 'TillControlEventType.' +attrs.tillControlEventType)}</li>"""

                break;
            default:
                out << ""

                break;
        }

        out << """</ol></div></div></nav>"""
    }

    def groupHierarchyPadding = { attrs, body ->
        if (attrs.type == GroupType.DIVISION) {
            out << "pad-left-25"
        } else if (attrs.type == GroupType.REGION) {
            out << "pad-left-40"
        }
    }

    def groupBreadcrumb = { attrs, body ->
        def group = Group.get(attrs.groupId)

        def groups = []

        if (group) {
            groups.add(group)
        }

        while (group?.parentGroup) {
            group = group.parentGroup

            groups.add(group)
        }

        groups = groups.reverse()

        def breadcrumb = ""
        groups.eachWithIndex { it, index ->
            if (index == 0) {
                breadcrumb += """<nav aria-label="breadcrumb"><ol class="breadcrumb">"""
                breadcrumb += """<li class="breadcrumb-item"><a href=# onclick="navigateToGroup(0, 1);">Home</a></li>"""
            }

            if (index == groups.size() - 1) {
                breadcrumb += """<li class="breadcrumb-item active" aria-current="page">${it.name}</li>"""
                breadcrumb += """</ol></nav>"""
            } else {
                breadcrumb += """<li class="breadcrumb-item"><a href=# onclick="navigateToGroup(${it.id}, ${it.level.level});">${it.name}</a></li>"""
            }
        }

        out << breadcrumb
    }
}