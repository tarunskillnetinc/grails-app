package uk.co.wonderlane.wlpos

import grails.gorm.transactions.Transactional
import uk.co.wonderlane.wlpos.supplier.Supplier

@Transactional
class SupplierService {

    def springSecurityService

    def getSuppliers() {
        return Supplier.findAllByRetailerIdAndStoreId(springSecurityService.principal.retailerId, springSecurityService.principal.storeId, [sort: "name", order: "asc"])
    }

    def getSupplier(int id) {
        return Supplier.findByIdAndRetailerIdAndStoreId(id, springSecurityService.principal.retailerId, springSecurityService.principal.storeId)
    }

    def saveSupplier(Supplier supplier) {
        supplier.save()
    }

    def deleteSupplier(Supplier supplier) {
        supplier.delete()
    }

    def getSymbolGroupSubscriptions() {
//        return Supplier.findAllByRetailerIdAndStoreId(springSecurityService.principal.retailerId, springSecurityService.principal.storeId, [sort: "name", order: "asc"])
    }
}