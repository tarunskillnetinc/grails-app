package uk.co.wonderlane.wlpos

import grails.gorm.transactions.Transactional

import javax.validation.constraints.NotNull

@Transactional
class ProductAttributesService {
    def springSecurityService
    def messageSource

    def getProductAttributes(int max, int offset, String sort, String order, long retailerId) {
        def query = ProductAttributes.where {
            retailerId == retailerId
        }

        def totalCount = query.count()

        def results = query.list(max: max, offset: offset)

        if (sort) {
            results = results.sort { it[sort] }
            if (order?.equalsIgnoreCase('desc')) {
                results = results.reverse()
            }
        }

        return [list: results, count: totalCount]
    }

    def saveProductAttribute(@NotNull ProductAttributes productAttribute) {
        def result = [:]
        productAttribute.retailerId = springSecurityService.principal.retailerId
        if (!productAttribute.validate()) {
            result.success = false
            def errorMessages = productAttribute.errors.fieldErrors.collectEntries { error ->
                [(error.field): messageSource.getMessage(error.code, error.arguments, Locale.default)]
            }
            result.errorMessages = errorMessages
            return result
        }
        productAttribute.save(flush: true)
        result.success = true
        return result
    }

    def getProductAttributeById(int id) {
        return ProductAttributes.findById(id);
    }
}
