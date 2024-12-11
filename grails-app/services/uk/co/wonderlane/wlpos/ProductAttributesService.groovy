package uk.co.wonderlane.wlpos

import grails.gorm.transactions.Transactional

@Transactional
class ProductAttributesService {
    def springSecurityService

    def serviceMethod() {
        ProductAttributes.findAllByRetailerId(springSecurityService.principal.retailerId)
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
