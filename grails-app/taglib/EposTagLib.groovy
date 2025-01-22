import uk.co.wonderlane.wlpos.Category
import uk.co.wonderlane.wlpos.Group
import uk.co.wonderlane.wlpos.ImageRecord
import uk.co.wonderlane.wlpos.enums.ImageType
import uk.co.wonderlane.wlpos.enums.ProductHistoryType
import uk.co.wonderlane.wlpos.enums.PromotionType
import uk.co.wonderlane.wlpos.reporting.ReportType

import java.math.RoundingMode

class EposTagLib {

    static returnObjectForTags = ['showQuantityField', 'showValueField']

    def springSecurityService
    def reportingService
    def buttonService
    def categoryService
    def tagService
    def productService
    def promotionService
    def imageService
    def imageRecordService

    def quicksellMenu = { attrs, body ->
        def buttonGrids = buttonService.getOtherButtonGrids()

        buttonGrids.each { buttonGrid ->
            out << """<a id = ${buttonGrid.description} class="dropdown-item" href="${createLink(controller: "buttonGrid", action: "show", id: buttonGrid.id)}">${buttonGrid.description}</a>"""
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

    def reportBreadcrumb = { attrs, body ->
        out << """<nav aria-label="breadcrumb"><div class="row mt-4"><div class="col"><ol class="breadcrumb">"""

        out << """<li id="breadcrumb-1" class="breadcrumb-item">${g.link(uri:"/") { "Home" }}"""

        switch ((ReportType)attrs.reportType) {
            case ReportType.SALES_DEPARTMENT:
                out << """<li id="breadcrumb-2" class="breadcrumb-item active" aria-current="page">All Department Sales</li>"""

                break
            case ReportType.SALES_CATEGORY:
                def category = categoryService.getCategory(attrs.categoryId)

                def hierarchy = [category]

                while (category?.parentCategory != null) {
                    hierarchy.add(category.parentCategory)

                    category = category.parentCategory
                }

                hierarchy = hierarchy.reverse()

                out << """<li id="breadcrumb-2" class="breadcrumb-item">${g.link(action:"salesDepartment", params:[startDate: attrs.startDate?.toString('dd/MM/yyyy'), endDate: attrs.endDate?.toString('dd/MM/yyyy')]) { "All Department Sales" }}"""

                int i = 2;
                hierarchy.each { cat ->
                    i++
                    if (cat == null || cat?.id == attrs.categoryId) {
                        out << """<li id="breadcrumb-${i}" class="breadcrumb-item active" aria-current="page">${cat?.description ?: "Invalid Category"}</li>"""
                    } else {
                        out << """<li id="breadcrumb-${i}" class="breadcrumb-item">${g.link(action:"salesCategory", params:[categoryId: cat?.id, startDate: attrs.startDate?.toString('dd/MM/yyyy'), endDate: attrs.endDate?.toString('dd/MM/yyyy')]) { cat?.description ?: "Invalid Category" }}"""
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

                out << """<li id="breadcrumb-2" class="breadcrumb-item">${g.link(action:"salesDepartment", params:[startDate: attrs.startDate?.toString('dd/MM/yyyy'), endDate: attrs.endDate?.toString('dd/MM/yyyy')]) { "All Department Sales" }}"""

                int i = 3;
                hierarchy.each { cat ->
                    out << """<li id="breadcrumb-${i}" class="breadcrumb-item">${g.link(action:"salesCategory", params:[categoryId: cat?.id, startDate: attrs.startDate?.toString('dd/MM/yyyy'), endDate: attrs.endDate?.toString('dd/MM/yyyy')]) { cat?.description ?: "Invalid Category" }}"""
                    i++
                }

                out << """<li id="breadcrumb-${i}" class="breadcrumb-item active" aria-current="page">${product?.description ?: "Invalid Product"}</li>"""

                break
            case ReportType.CATEGORY_SALES:
                out << """<li id="breadcrumb-2" class="breadcrumb-item active" aria-current="page">All Category Sales</li>"""
                break
            case ReportType.SALES:
                out << """<li id="breadcrumb-2" class="breadcrumb-item active" aria-current="page">All Product Sales</li>"""
                break
            case ReportType.PROMOTIONS_GROUPED:
                out << """<li id="breadcrumb-2" class="breadcrumb-item active" aria-current="page">All Promotional Sales</li>"""
                break
            case ReportType.BANKING_REPORT:
                out << """<li id="breadcrumb-2" class="breadcrumb-item active" aria-current="page">Banking Report</li>"""
                break
            case ReportType.PROMOTIONS:
                def promotion = promotionService.getPromotion(attrs.promotionId)

                out << """<li id="breadcrumb-2" class="breadcrumb-item">${g.link(action:"promotionsGrouped", params:[startDate: attrs.startDate?.toString('dd/MM/yyyy'), endDate: attrs.endDate?.toString('dd/MM/yyyy')]) { "All Promotional Sales" }}"""
                out << """<li id="breadcrumb-3" class="breadcrumb-item active" aria-current="page">${promotion.description}</li>"""

                break
            case ReportType.PROMOTION:
                def promotionSale = reportingService.getPromotionSale(attrs.promotionSaleId)
                def promotion = promotionService.getPromotion(promotionSale.promotionId)

                out << """<li id="breadcrumb-2" class="breadcrumb-item">${g.link(action:"promotionsGrouped", params:[startDate: attrs.startDate?.toString('dd/MM/yyyy'), endDate: attrs.endDate?.toString('dd/MM/yyyy')]) { "All Promotional Sales" }}"""
                out << """<li id="breadcrumb-3" class="breadcrumb-item">${g.link(action:"promotions", params:[promotionId: promotion.id, startDate: attrs.startDate?.toString('dd/MM/yyyy'), endDate: attrs.endDate?.toString('dd/MM/yyyy')]) { promotion.description }}"""
                out << """<li id="breadcrumb-4" class="breadcrumb-item active" aria-current="page">${promotion.description}</li>"""

                break
            case ReportType.TILL_CONTROL_EVENTS:
                out << """<li id="breadcrumb-2" class="breadcrumb-item active" aria-current="page">All Events</li>"""

                break
            case ReportType.TILL_CONTROL_EVENT:
                out << """<li id="breadcrumb-2" class="breadcrumb-item">${g.link(action:"tillControlEvents", params:[startDate: attrs.startDate?.toString('dd/MM/yyyy'), endDate: attrs.endDate?.toString('dd/MM/yyyy')]) { "All Events" }}"""
                out << """<li id="breadcrumb-3" class="breadcrumb-item active" aria-current="page">${g.message(code: 'TillControlEventType.' +attrs.tillControlEventType)}</li>"""

                break
            case ReportType.PAYPOINT_SALES:
                out << """<li id="breadcrumb-2" class="breadcrumb-item active" aria-current="page">All PayPoint Sales</li>"""

                break
            case ReportType.ORDERS:
                out << """<li id="breadcrumb-2" class="breadcrumb-item active" aria-current="page">All Orders</li>"""

                break
            case ReportType.ORDER:
                out << """<li id="breadcrumb-2" class="breadcrumb-item">${g.link(action:"orders", params:[productListId: attrs.productListId, startDate: attrs.startDate?.toString('dd/MM/yyyy'), endDate: attrs.endDate?.toString('dd/MM/yyyy')]) { "All Orders" }}"""
                out << """<li id="breadcrumb-3" class="breadcrumb-item active" aria-current="page">${attrs.supplierReference}</li>"""

                break
            case ReportType.DELIVERIES:
                out << """<li id="breadcrumb-2" class="breadcrumb-item active" aria-current="page">Deliveries Report</li>"""

                break
            case ReportType.DELIVERY:
                out << """<li id="breadcrumb-2" class="breadcrumb-item">${g.link(action:"deliveries", params:[storeId: attrs.storeId, supplierId: attrs.supplierId, startDate: attrs.startDate?.toString('dd/MM/yyyy'), endDate: attrs.endDate?.toString('dd/MM/yyyy')]) { "Deliveries Report" }}"""
                out << """<li id="breadcrumb-3" class="breadcrumb-item active" aria-current="page">${attrs.supplierName} (${attrs.deliveryDate?.toString("dd/MM/yyyy") ?: 'Unknown date'})</li>"""
                break

            case ReportType.DELIVERY_ITEM:
                out << """<li id="breadcrumb-2" class="breadcrumb-item">${g.link(action:"deliveries", params:[productListId: attrs.productListId, storeId: attrs.storeId, supplierId: attrs.supplierId, startDate: attrs.startDate?.toString('dd/MM/yyyy'), endDate: attrs.endDate?.toString('dd/MM/yyyy')]) { "Deliveries Report" }}"""
                out << """<li id="breadcrumb-3" class="breadcrumb-item">${g.link(action:"delivery", params:[productListId: attrs.productListId, productListItemId: attrs.productListItemId, storeId: attrs.storeId, supplierId: attrs.supplierId, descriptionFilter: attrs.descriptionFilter, startDate: attrs.startDate?.toString('dd/MM/yyyy'), endDate: attrs.endDate?.toString('dd/MM/yyyy')]) { "${attrs.supplierName} (${attrs.deliveryDate?.toString("dd/MM/yyyy") ?: 'Unknown date'})" }}"""
                out << """<li id="breadcrumb-4" class="breadcrumb-item active" aria-current="page">${attrs.productDescription}</li>"""

                break
            case ReportType.PRODUCT_LISTS:
                out << """<li id="breadcrumb-2" class="breadcrumb-item active" aria-current="page">Product Lists Report</li>"""

                break
            case ReportType.PRODUCT_LIST:
                out << """<li id="breadcrumb-2" class="breadcrumb-item">${g.link(action:"productLists", params:[storeId: attrs.storeId, type: attrs.typeFilter, startDate: attrs.startDate?.toString('dd/MM/yyyy'), endDate: attrs.endDate?.toString('dd/MM/yyyy')]) { "Product Lists Report" }}"""
                out << """<li id="breadcrumb-3" class="breadcrumb-item active" aria-current="page">${g.message(code:"ProductListType." +attrs.type)} (${attrs.dateStarted?.toString("dd/MM/yyyy") ?: 'Unknown date'})</li>"""
                break
            case ReportType.TENDER_MOVEMENTS:
                out << """<li class="breadcrumb-item active" aria-current="page">All Tender Movements</li>"""
                break;
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
                breadcrumb += """<li id="breadcrumb-1" class="breadcrumb-item"><a href=# onclick="navigateToGroup(0, 1);">Home</a></li>"""
            }

            if (index == groups.size() - 1) {
                breadcrumb += """<li id="breadcrumb-${index + 1}" class="breadcrumb-item active" aria-current="page">${it.name}</li>"""
                breadcrumb += """</ol></nav>"""
            } else {
                breadcrumb += """<li id="breadcrumb-${index + 1}" class="breadcrumb-item"><a href=# onclick="navigateToGroup(${it.id}, ${it.level.level});">${it.name}</a></li>"""
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
        ImageRecord imageRecord = imageRecordService.getImageRecordByImageId(ImageType.BUTTON, attrs.buttonId as int)
        def buttonImage = imageService.getImage(imageRecord)

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
                            from ${productHistory?.fromValue} to ${productHistory?.toValue} at ${productHistory?.updateDate?.toString('dd/MM/yyyy HH:mm:ss')}"""
                break
            case ProductHistoryType.PRICE:
                out << """User ${productHistory?.usersName} changed price from ${productHistory?.fromValue} to ${productHistory?.toValue} at ${productHistory?.updateDate?.toString('dd/MM/yyyy HH:mm:ss')}"""
                break
            case ProductHistoryType.PRODUCT_RANGE_ADD:
                out << """User ${productHistory?.usersName} added product range ${productHistory?.toValue} at ${productHistory?.updateDate?.toString('dd/MM/yyyy HH:mm:ss')}"""
                break
            case ProductHistoryType.PRODUCT_RANGE_DELETE:
                out << """User ${productHistory?.usersName} deleted product range ${productHistory?.toValue} at ${productHistory?.updateDate?.toString('dd/MM/yyyy HH:mm:ss')}"""
                break
            case ProductHistoryType.LOCATION_ADD:
                out << """User ${productHistory?.usersName} added new location with 
                        ${(g.message(code: productHistory?.field) != null && !g.message(code: productHistory?.field).isEmpty()) ? g.message(code: getLocationField(productHistory?.field)) : getLocationField(productHistory?.field)} 
                            from ${(productHistory?.fromValue) == "0" ? "unset" : productHistory?.fromValue} to ${productHistory?.toValue} at ${productHistory?.updateDate?.toString('dd/MM/yyyy HH:mm:ss')}"""
                break
            case ProductHistoryType.LOCATION_EDIT:
                out << """User ${productHistory?.usersName} changed location with 
                        ${(g.message(code: productHistory?.field) != null && !g.message(code: productHistory?.field).isEmpty()) ? g.message(code: getLocationField(productHistory?.field)) : getLocationField(productHistory?.field)} 
                            from ${productHistory?.fromValue} to ${productHistory?.toValue} at ${productHistory?.updateDate?.toString('dd/MM/yyyy HH:mm:ss')}"""
                break
            case ProductHistoryType.LOCATION_DELETE:
                out << """User ${productHistory?.usersName} deleted location with 
                        ${(g.message(code: productHistory?.field) != null && !g.message(code: productHistory?.field).isEmpty()) ? g.message(code: getLocationField(productHistory?.field)) : getLocationField(productHistory?.field)} 
                            from ${productHistory?.fromValue} to ${(productHistory?.toValue) == "0" ? "unset" : productHistory?.toValue} at ${productHistory?.updateDate?.toString('dd/MM/yyyy HH:mm:ss')}"""
                break
            case ProductHistoryType.PRODUCT_CREATED:
                out << """Product created at ${productHistory?.updateDate?.toString('dd/MM/yyyy HH:mm:ss')}"""
                break
            case ProductHistoryType.PREFERRED_SKU:
                out << """User ${productHistory?.usersName} changed Preferred SKU from ${productHistory?.fromValue} to ${productHistory?.toValue} at ${productHistory?.updateDate?.toString('dd/MM/yyyy HH:mm:ss')}"""
                break;
            case ProductHistoryType.PRODUCT_ATTRIBUTE:
                out << """User ${productHistory?.usersName} changed product attribute field
                        ${productHistory?.field} 
                            from ${productHistory?.fromValue} to ${productHistory?.toValue} at ${productHistory?.updateDate?.toString('dd/MM/yyyy HH:mm:ss')}"""
                break
            default:
                out << """User ${productHistory?.usersName} changed 
                        ${(g.message(code: 'ProductHistory.' + productHistory?.field) != null && !g.message(code: 'ProductHistory.' + productHistory?.field).isEmpty())  ? g.message(code: 'ProductHistory.' + productHistory?.field) : productHistory?.field} 
                            from ${productHistory?.fromValue} to ${productHistory?.toValue} at ${productHistory?.updateDate?.toString('dd/MM/yyyy HH:mm:ss')}"""
                break
        }
    }

    def promotionTypeAmountDisplay = { attrs, body ->
        def promotionType = attrs.type

        if (promotionType) {
            switch ((PromotionType)promotionType) {
                case PromotionType.BOGOF:
                    out << "display: none;"
                    break;
                case PromotionType.FIXED_AMOUNT_DISCOUNT:
                    out << "display: block;"
                    break;
                case PromotionType.PERCENTAGE_DISCOUNT:
                    out << "display: block;"
                    break;
                case PromotionType.X_FOR_Y:
                    out << "display: none;"
                    break;
                case PromotionType.FIXED_PRICE:
                    out << "display: block;"
                    break;
            }
        } else {
            // New promotion, type will be null.
            out << "display: none;"
        }
    }

    def promotionTypeAmountLabel = { attrs, body ->
        def promotionType = attrs.type

        if (promotionType) {
            switch ((PromotionType)promotionType) {
                case PromotionType.BOGOF:
                    out << ""
                    break;
                case PromotionType.FIXED_AMOUNT_DISCOUNT:
                    out << "Discount Amount"
                    break;
                case PromotionType.PERCENTAGE_DISCOUNT:
                    out << "Discount Percentage"
                    break;
                case PromotionType.X_FOR_Y:
                    out << "display: none;"
                    break;
                case PromotionType.FIXED_PRICE:
                    out << "Fixed Price"
                    break;
            }
        } else {
            // New promotion, type will be null.
            out << "display: none;"
        }
    }

    def promotionTypeRequiredGroupsDisplay = { attrs, body ->
        def promotionType = attrs.type

        if (promotionType) {
            switch ((PromotionType)promotionType) {
                case PromotionType.BOGOF:
                    out << "display: none;"
                    break;
                case PromotionType.FIXED_AMOUNT_DISCOUNT:
                    out << "display: none;"
                    break;
                case PromotionType.PERCENTAGE_DISCOUNT:
                    out << "display: none;"
                    break;
                case PromotionType.X_FOR_Y:
                    out << "display: block;"
                    break;
                case PromotionType.FIXED_PRICE:
                    out << "display: none;"
                    break;
            }
        } else {
            // New promotion, type will be null.
            out << "display: none;"
        }
    }

    def promotionTypeRequiredGroupsHeading = { attrs, body ->
        def promotionType = attrs.type

        if (promotionType) {
            switch ((PromotionType)promotionType) {
                case PromotionType.BOGOF:
                    out << "Required Groups"
                    break;
                case PromotionType.FIXED_AMOUNT_DISCOUNT:
                    out << "Required Groups"
                    break;
                case PromotionType.PERCENTAGE_DISCOUNT:
                    out << "Required Groups"
                    break;
                case PromotionType.X_FOR_Y:
                    out << "Customer Buys"
                    break;
                case PromotionType.FIXED_PRICE:
                    out << "Required Groups"
                    break;
            }
        } else {
            // New promotion, type will be null (can't happen).
            out << "Customer Buys"
        }
    }

    def promotionTypeOfferGroupsHeading = { attrs, body ->
        def promotionType = attrs.type

        if (promotionType) {
            switch ((PromotionType)promotionType) {
                case PromotionType.BOGOF:
                    out << "Customer Buys & Receives One Free"
                    break;
                case PromotionType.FIXED_AMOUNT_DISCOUNT:
                    out << "Customer Buys & Receives Amount Off"
                    break;
                case PromotionType.PERCENTAGE_DISCOUNT:
                    out << "Customer Buys & Receives % Off"
                    break;
                case PromotionType.X_FOR_Y:
                    out << "Customer Receives Free"
                    break;
                case PromotionType.FIXED_PRICE:
                    out << "Customer Buys For Amount"
                    break;
            }
        } else {
            // New promotion, type may be null?
            out << "Customer Buys & Receives One Free"
        }
    }

    def showQuantityField = { attrs, body ->
        if (attrs.promotionType == PromotionType.BOGOF) {
            return false
        } else {
            return true
        }
    }

    def showValueField = { attrs, body ->
        if (attrs.promotionType == PromotionType.FIXED_AMOUNT_DISCOUNT) {
            return true
        } else {
            return false
        }
    }

    def promotionGroupHeader = { attrs, body ->
        if (attrs.promotionGroup.sku) {
            def productVariant = productService.getProductVariant(attrs.promotionGroup.sku)

            out << productVariant?.product?.description
        } else if (attrs.promotionGroup.categoryId) {
            def category = categoryService.getCategory(attrs.promotionGroup.categoryId)

            out << category?.description
        } else if (attrs.promotionGroup.tagId) {
            def tag = tagService.getTag(attrs.promotionGroup.tagId)

            out << tag?.description
        }
    }

    def formatStringDate = { attrs, body ->
        def dateString = attrs.date
        def inputFormat = attrs.inputFormat ?: "yyyy-MM-dd HH:mm:ss"
        def outputFormat = attrs.outputFormat ?: "dd/MM/yyyy HH:mm:ss"
        def timeZone = attrs.timeZone ?: "Europe/London"

        if (dateString) {
            try {
                def date = new java.text.SimpleDateFormat(inputFormat).parse(dateString)
                out << g.formatDate(format: outputFormat, date: date, timeZone: timeZone)
            } catch (Exception e) {
                log.error("Error parsing date: ${dateString}", e)
                out << g.formatDate(format: outputFormat, date: new Date(), timeZone: timeZone)
            }
        } else {
            out << ""
        }
    }

    def renderSafeInfo = { attrs ->
        def safe = attrs.safe
        if (safe) {
            out << '<div class="d-flex flex-column align-items-center">'

            if (safe.primary) {
                out << '<div>Primary Safe</div>'
            }
            if (safe.type == uk.co.wonderlane.wlpos.enums.SafeType.SMART) {
                out << '<div>Smart Safe</div>'
            }

            out << "<div>${safe.description}</div>"
            out << '</div>'
        }
    }
    
    private static String getLocationField(String field) {
        def formattedFieldArray = field?.split("(?=\\p{Upper})")
        return String.join(" ", formattedFieldArray).toLowerCase()
    }
}
