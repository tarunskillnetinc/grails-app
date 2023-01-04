import uk.co.wonderlane.wlpos.Group
import uk.co.wonderlane.wlpos.Category
import uk.co.wonderlane.wlpos.enums.ProductHistoryType
import uk.co.wonderlane.wlpos.reporting.ReportType

import java.math.RoundingMode
import java.nio.file.Path

class EposTagLib {

    def springSecurityService
    def reportingService
    def buttonService
    def categoryService
    def productService
    def promotionService
    def imageService

    def quicksellMenu = { attrs, body ->
        def buttonGrids = buttonService.getOtherButtonGrids()

        buttonGrids.each { buttonGrid ->
            out << """<a class="dropdown-item" href="${createLink(controller: "buttonGrid", action: "show", id: buttonGrid.id)}">${buttonGrid.description}</a>"""
        }
    }

    def paginateReport = { attrs, body ->
        if (attrs.totalResults > attrs.max) {
            if (attrs.offset > 0) {
                out << """<a id="page-prev-btn" class="prevLink" href="#" onclick="getReportData({ max: ${attrs.max}, offset: ${attrs.offset - attrs.max}, sortColumn: '${attrs.sortColumn}', sortOrder: '${attrs.sortOrder}' });">Previous</a>"""
            }

            for (int i = 0 ; (i * attrs.max) < attrs.totalResults ; i++) {
                if (attrs.offset >= (i * attrs.max) && attrs.offset < ((i + 1) * attrs.max)) {
                    out << """<span class="currentStep">${i + 1}</span>"""
                } else {
                    out << """<a id="page-${i + 1}-btn" class="step" href="#" onclick="getReportData({ max: ${attrs.max}, offset: ${(i) * attrs.max}, sortColumn: '${attrs.sortColumn}', sortOrder: '${attrs.sortOrder}' });">${i + 1}</a>"""
                }
            }

            if ((attrs.offset + attrs.max) < attrs.totalResults) {
                out << """<a id="page-next-btn" class="nextLink" href="#" onclick="getReportData({ max: ${attrs.max}, offset: ${attrs.offset + attrs.max}, sortColumn: '${attrs.sortColumn}', sortOrder: '${attrs.sortOrder}' });">Next</a>"""
            }
        }
    }

    def wlPagination = { attrs, body ->
        if (attrs.totalResults > attrs.max) {
            if (attrs.offset > 0) {
                out << """<a id="page-prev-btn" class="prevLink" href="#" onclick="${attrs.searchFunction}(${attrs.offset - attrs.max}, ${attrs.max});">Previous</a>"""
            }

            for (int i = 0 ; (i * attrs.max) < attrs.totalResults ; i++) {
                if (attrs.offset >= (i * attrs.max) && attrs.offset < ((i + 1) * attrs.max)) {
                    out << """<span class="currentStep">${i + 1}</span>"""
                } else {
                    out << """<a id="page-${i + 1}-btn" class="step" href="#" onclick="${attrs.searchFunction}(${i * attrs.max}, ${attrs.max});">${i + 1}</a>"""
                }
            }

            if ((attrs.offset + attrs.max) < attrs.totalResults) {
                out << """<a id="page-next-btn" class="nextLink" href="#" onclick="${attrs.searchFunction}(${attrs.offset + attrs.max}, ${attrs.max});">Next</a>"""
            }
        }
    }

    def reportBreadcrumb = { attrs, body ->
        out << """<nav aria-label="breadcrumb"><div class="row mt-4"><div class="col"><ol class="breadcrumb">"""

        out << """<li class="breadcrumb-item">${g.link(uri:"/") { "Home" }}"""

        switch ((ReportType)attrs.reportType) {
            case ReportType.SALES_DEPARTMENT:
                out << """<li class="breadcrumb-item active" aria-current="page">All Department Sales</li>"""

                break
            case ReportType.SALES_CATEGORY:
                def category = categoryService.getCategory(attrs.categoryId)

                def hierarchy = [category]

                while (category?.parentCategory != null) {
                    hierarchy.add(category.parentCategory)

                    category = category.parentCategory
                }

                hierarchy = hierarchy.reverse()

                out << """<li class="breadcrumb-item">${g.link(action:"salesDepartment", params:[startDate: attrs.startDate?.toString('dd/MM/yyyy'), endDate: attrs.endDate?.toString('dd/MM/yyyy')]) { "All Department Sales" }}"""

                hierarchy.each { cat ->
                    if (cat == null || cat?.id == attrs.categoryId) {
                        out << """<li class="breadcrumb-item active" aria-current="page">${cat?.description ?: "Invalid Category"}</li>"""
                    } else {
                        out << """<li class="breadcrumb-item">${g.link(action:"salesCategory", params:[categoryId: cat?.id, startDate: attrs.startDate?.toString('dd/MM/yyyy'), endDate: attrs.endDate?.toString('dd/MM/yyyy')]) { cat?.description ?: "Invalid Category" }}"""
                    }
                }

                break
            case ReportType.SALES_PRODUCT:
                def product = productService.getProduct(attrs.productId)
                def category = product?.category

                def hierarchy = [category]

                while (category?.parentCategory != null) {
                    hierarchy.add(category.parentCategory)

                    category = category.parentCategory
                }

                hierarchy = hierarchy.reverse()

                out << """<li class="breadcrumb-item">${g.link(action:"salesDepartment", params:[startDate: attrs.startDate?.toString('dd/MM/yyyy'), endDate: attrs.endDate?.toString('dd/MM/yyyy')]) { "All Department Sales" }}"""

                hierarchy.each { cat ->
                    out << """<li class="breadcrumb-item">${g.link(action:"salesCategory", params:[categoryId: cat?.id, startDate: attrs.startDate?.toString('dd/MM/yyyy'), endDate: attrs.endDate?.toString('dd/MM/yyyy')]) { cat?.description ?: "Invalid Category" }}"""
                }

                out << """<li class="breadcrumb-item active" aria-current="page">${product?.description ?: "Invalid Product"}</li>"""

                break
            case ReportType.CATEGORY_SALES:
                out << """<li class="breadcrumb-item active" aria-current="page">All Category Sales</li>"""
                break
            case ReportType.SALES:
                out << """<li class="breadcrumb-item active" aria-current="page">All Product Sales</li>"""
                break
            case ReportType.PROMOTIONS_GROUPED:
                out << """<li class="breadcrumb-item active" aria-current="page">All Promotional Sales</li>"""

                break
            case ReportType.PROMOTIONS:
                def promotion = promotionService.getPromotion(attrs.promotionId)

                out << """<li class="breadcrumb-item">${g.link(action:"promotionsGrouped", params:[startDate: attrs.startDate?.toString('dd/MM/yyyy'), endDate: attrs.endDate?.toString('dd/MM/yyyy')]) { "All Promotional Sales" }}"""
                out << """<li class="breadcrumb-item active" aria-current="page">${promotion.description}</li>"""

                break
            case ReportType.PROMOTION:
                def promotionSale = reportingService.getPromotionSale(attrs.promotionSaleId)
                def promotion = promotionService.getPromotion(promotionSale.promotionId)

                out << """<li class="breadcrumb-item">${g.link(action:"promotionsGrouped", params:[startDate: attrs.startDate?.toString('dd/MM/yyyy'), endDate: attrs.endDate?.toString('dd/MM/yyyy')]) { "All Promotional Sales" }}"""
                out << """<li class="breadcrumb-item">${g.link(action:"promotions", params:[promotionId: promotion.id, startDate: attrs.startDate?.toString('dd/MM/yyyy'), endDate: attrs.endDate?.toString('dd/MM/yyyy')]) { promotion.description }}"""
                out << """<li class="breadcrumb-item active" aria-current="page">${promotion.description}</li>"""

                break
            case ReportType.TILL_CONTROL_EVENTS:
                out << """<li class="breadcrumb-item active" aria-current="page">All Events</li>"""

                break
            case ReportType.TILL_CONTROL_EVENT:
                out << """<li class="breadcrumb-item">${g.link(action:"tillControlEvents", params:[startDate: attrs.startDate?.toString('dd/MM/yyyy'), endDate: attrs.endDate?.toString('dd/MM/yyyy')]) { "All Events" }}"""
                out << """<li class="breadcrumb-item active" aria-current="page">${g.message(code: 'TillControlEventType.' +attrs.tillControlEventType)}</li>"""

                break
            case ReportType.PAYPOINT_SALES:
                out << """<li class="breadcrumb-item active" aria-current="page">All PayPoint Sales</li>"""

                break
            case ReportType.ORDERS:
                out << """<li class="breadcrumb-item active" aria-current="page">All Orders</li>"""

                break
            case ReportType.ORDER:
                out << """<li class="breadcrumb-item">${g.link(action:"orders", params:[productListId: attrs.productListId, startDate: attrs.startDate?.toString('dd/MM/yyyy'), endDate: attrs.endDate?.toString('dd/MM/yyyy')]) { "All Orders" }}"""
                out << """<li class="breadcrumb-item active" aria-current="page">Order</li>"""

                break
            default:
                out << ""

                break
        }

        out << """</ol></div></div></nav>"""
    }

    def categorySalesIndent = { attrs, body ->
        out << "padding-left: ${attrs.categoryLevel > 0 ? attrs.categoryLevel * 30 : 15}px;"
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

    def categorySelect = { attrs, body ->
        out << """<select name="${attrs.name}" id=${attrs.name} class="form-control select-border">"""
        out << """<option value="">${attrs.noSelectionValue ?: ""}</option>"""

        attrs.categories?.each {
            categorySelectChildren(it, 0)
        }

        out << """</select>"""
    }

    private void categorySelectChildren(Category category, int indent) {
        out << """<option value="${category.id}">"""

        for (int i = 0 ; i < (indent * 5) ; i++) {
            out << "&nbsp;"
        }

        out << """${category.description}</option>"""

        category.childCategories?.each {
            categorySelectChildren(it, indent + 1)
        }
    }

    def buttonImage = {attrs, body ->
        def path = Path.of(grailsApplication.config.getProperty('wlpos.buttonImageDirectory'), String.valueOf(springSecurityService.principal.retailerId), String.valueOf(attrs.buttonId) + ".png", File.separator)

        def buttonImage = imageService.getImageFromFile(path.toString())

        if (buttonImage != null) {
            out << """<img src="data:image/png;base64,${buttonImage.encodeBase64()}" class="mx-auto my-auto button-grid-button-image" />"""
        }
    }

    def getMargin = { attrs, body ->
        def retailPrice = attrs.retailPrice
        def costPrice = attrs.costPrice
        def quantity = attrs.quantity

        if (retailPrice && costPrice && quantity) {
            BigDecimal margin = retailPrice.subtract(costPrice.divide(quantity, 2, RoundingMode.HALF_UP)).divide(retailPrice, 4, RoundingMode.HALF_UP).multiply(100).setScale(2)

            out << """${margin.compareTo(BigDecimal.ZERO) < 0 ? "0.00" : margin}%"""
        } else {
            out << "0.00%"
        }
    }

    def productHistory = { attrs, body ->
        def productHistory = attrs.productHistory
        switch ((ProductHistoryType)productHistory?.productHistoryType) {
            case ProductHistoryType.FIELD:
                out << """User ${productHistory?.usersName} changed 
                        ${(g.message(code: 'ProductHistory.' + productHistory?.field) != null && !g.message(code: 'ProductHistory.' + productHistory?.field).isEmpty())  ? g.message(code: 'ProductHistory.' + productHistory?.field) : productHistory?.field} 
                            from ${productHistory?.fromValue} to ${productHistory?.toValue} at ${productHistory?.updateDate?.toString('dd/MM/yyyy HH:mm:ss')}."""
                break
            case ProductHistoryType.PRICE:
                out << """User ${productHistory?.usersName} changed price from ${productHistory?.fromValue} to ${productHistory?.toValue} at ${productHistory?.updateDate?.toString('dd/MM/yyyy HH:mm:ss')}."""
                break
            case ProductHistoryType.PRODUCT_RANGE_ADD:
                out << """User ${productHistory?.usersName} added product range ${productHistory?.toValue} at ${productHistory?.updateDate?.toString('dd/MM/yyyy HH:mm:ss')}."""
                break
            case ProductHistoryType.PRODUCT_RANGE_DELETE:
                out << """User ${productHistory?.usersName} deleted product range ${productHistory?.toValue} at ${productHistory?.updateDate?.toString('dd/MM/yyyy HH:mm:ss')}."""
                break
            default:
                out << """User ${productHistory?.usersName} changed 
                        ${(g.message(code: 'ProductHistory.' + productHistory?.field) != null && !g.message(code: 'ProductHistory.' + productHistory?.field).isEmpty())  ? g.message(code: 'ProductHistory.' + productHistory?.field) : productHistory?.field} 
                            from ${productHistory?.fromValue} to ${productHistory?.toValue} at ${productHistory?.updateDate?.toString('dd/MM/yyyy HH:mm:ss')}."""
                break
        }
    }
}