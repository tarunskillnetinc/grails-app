package uk.co.wonderlane.wlpos

import grails.gorm.transactions.Transactional

@Transactional
class ProductAttributesService {
    def springSecurityService

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

    def saveProductAttribute(ProductAttributes productAttribute) {
        try {
            if (productAttribute != null) {
                productAttribute.retailerId = springSecurityService.principal.retailerId
                if (productAttribute.validate()) {
                    productAttribute.save(flush: true)
                }
            }
        } catch (Exception ex) {
            def a = 1
        }

    }
}
