package uk.co.wonderlane.wlpos

import uk.co.wonderlane.wlpos.supplier.Supplier

class SupplierController {

    def supplierService
    def springSecurityService

    def index() { }

    def ajaxGetSuppliers() {
        def suppliers = supplierService.getSuppliers()

        render (template: "supplierSearchResults", model: [suppliers: suppliers])
    }

    def ajaxGetSymbolGroupSubscriptions() {
        def symbolGroupSubscriptions = supplierService.getSymbolGroupSubscriptions()

        render (template: "symbolGroupSubscriptionsSearchResults", model: [symbolGroupSubscriptions: symbolGroupSubscriptions])
    }

    def ajaxAddSupplier() {
        render (template: "addSupplier")
    }

    def ajaxSaveSupplier() {
        def supplier

        if (params.id && Integer.parseInt(params.id) > 0) {
            supplier = supplierService.getSupplier(params.id)
        } else {
            supplier = new Supplier()
            supplier.retailerId = springSecurityService.principal.retailerId
            supplier.storeId = springSecurityService.principal.storeId
        }

        bindData(supplier, params)

        if (supplier.validate()) {
            supplierService.saveSupplier(supplier)

            render "OK"
        } else {
            render (template: "addSupplier", model: [supplier: supplier])
        }
    }
}