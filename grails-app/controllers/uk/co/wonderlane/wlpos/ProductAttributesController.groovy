package uk.co.wonderlane.wlpos

import grails.converters.JSON
import org.springframework.security.access.annotation.Secured
import uk.co.wonderlane.wlpos.enums.ProductAttributeType;

class ProductAttributesController extends BaseController {

    def springSecurityService
    def productAttributesService
    def messageSource

    @Override
    def getColumns() {
        return null
    }

    @Secured(['ROLE_ENGINEER', 'ROLE_HEAD_OFFICE'])
    def productAttributes() {
        if (springSecurityService.principal.storeId) {
            flash.error = "You cannot access this page while logged in with a store."
            redirect(uri: "/")
            return
        }

        int max = params.int('max') ?: 50
        int offset = params.int('offset') ?: 0
        String sort = params.sort ?: 'name'
        String order = params.order?.toLowerCase() ?: 'asc'
        long retailerId = springSecurityService.principal.retailerId

        def paginatedResults = productAttributesService.getProductAttributes(max, offset, sort, order, retailerId)

        [productAttributes: paginatedResults.list, productAttributesCount: paginatedResults.count]
    }

    @Secured(['ROLE_ENGINEER', 'ROLE_HEAD_OFFICE'])
    def ajaxProductAttributes() {
        int max = params.int('max') ?: 50
        int offset = params.int('offset') ?: 0
        String sort = params.sort ?: 'name'
        String order = params.order?.toLowerCase() ?: 'asc'
        long retailerId = springSecurityService.principal.retailerId

        def paginatedResults = productAttributesService.getProductAttributes(max, offset, sort, order, retailerId)

        render(template: "productAttributesResultsView", model: [productAttributes: paginatedResults.list, productAttributesCount: paginatedResults.count])
    }

    @Secured(['ROLE_ENGINEER', 'ROLE_HEAD_OFFICE'])
    def ajaxSaveProductAttributeChanges() {
        def a = params
        return;
    }

    @Secured(['ROLE_ENGINEER', 'ROLE_HEAD_OFFICE'])
    def addProductAttribute() {
        def types = ProductAttributeType.values();
        [attributeTypes: types]
    }

    @Secured(['ROLE_ENGINEER', 'ROLE_HEAD_OFFICE'])
    def saveProductAttribute() {
        def productAttributes = new ProductAttributes()
        productAttributes.name = params.attributeName
        productAttributes.type = params.type ? ProductAttributeType.valueOf(params.type) : null
        productAttributes.defaultValue = params.defaultValue
        productAttributes.displayAttribute = params.displayAttribute != null ? params.displayAttribute == "on" : false

        def result = productAttributesService.saveProductAttribute(productAttributes)
        if (!result.success) {
            render(template: "/errors/errorMessage", model: [errorMessages: result.errorMessages, error: true], status: 400)
        } else {
            render "OK"
        }
    }

    @Secured(['ROLE_ENGINEER', 'ROLE_HEAD_OFFICE'])
    def ajaxAddAttributeListItem() {
        def attributeId = params.attributeId
        render(template: "productAttributeAddListItem", model: [attributeId: attributeId])
    }

    @Secured(['ROLE_ENGINEER', 'ROLE_HEAD_OFFICE'])
    def saveAttributeListItem() {
        def attributeId = params.attributeId ? Integer.parseInt(params.attributeId) : null
        if (attributeId == null) {
            render(template: "/errors/errorMessage", model: [errorMessages: ["attributeId": messageSource.getMessage("productAttribute.id.empty", null, Locale.default)],
                                                             error: true
            ], status: 400)
            return
        }

        String itemName = params.itemName
        if (itemName == null || itemName.isEmpty() || itemName.isBlank()) {
            render(template: "/errors/errorMessage", model: [errorMessages: ["attributeId": messageSource.getMessage("productAttribute.listitem.empty", null, Locale.default)],
                                                             error: true
            ], status: 400)
            return
        }

        List<String> currentList = productAttributesService.getListValues(attributeId) ?: []
        if (currentList.stream().anyMatch(itemName::equalsIgnoreCase)) {
            render(template: "/errors/errorMessage", model: [errorMessages: ["attributeId": messageSource.getMessage("productAttribute.listitem.not.unique", null, Locale.default)],
                                                             error: true
            ], status: 400)
            return
        }

        def allowedCharactersRegex= /^[a-zA-Z0-9 \\\\/.,()\-]*$/
        if (!(itemName ==~ allowedCharactersRegex)) {
            render(template: "/errors/errorMessage", model: [errorMessages: ["attributeId": messageSource.getMessage("productAttribute.name.invalid.characters", null, Locale.default)],
                                                             error: true
            ], status: 400)
            return
        }

        currentList.add(itemName)
        def result = productAttributesService.updateListValues(attributeId, currentList)

        if (!result.success) {
            render(template: "/errors/errorMessage", model: [errorMessages: result.errorMessages,
                                                             error: true
            ], status: 400)
        } else {
            render "OK"
        }
    }

    @Secured(['ROLE_ENGINEER', 'ROLE_HEAD_OFFICE'])
    def bulkUpdateAttributes() {
        def result = [success: false]

        try {
            def updates = request.JSON.updates

            if (updates) {
                result = productAttributesService.bulkUpdateAttributes(updates)
            } else {
                result.errorMessages = [general: messageSource.getMessage("productAttribute.updates.empty", null, Locale.default)]
            }
        } catch (Exception e) {
            log.error "Error updating product attributes: ${e.message}", e
            result.errorMessages = [general: messageSource.getMessage("productAttribute.update.error", null, Locale.default)]
        }

        render result as JSON
    }
}

