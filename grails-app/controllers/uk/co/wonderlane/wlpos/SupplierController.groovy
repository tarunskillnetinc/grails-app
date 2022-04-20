package uk.co.wonderlane.wlpos

import uk.co.wonderlane.wlpos.entities.SymbolGroupMessage
import uk.co.wonderlane.wlpos.enums.SymbolGroupMessageType
import uk.co.wonderlane.wlpos.enums.SymbolGroupSubscriptionStatus
import uk.co.wonderlane.wlpos.supplier.Supplier
import uk.co.wonderlane.wlpos.supplier.SymbolGroupSubscription

class SupplierController {

    def springSecurityService

    def supplierService
    def rabbitService
    def gsonProvider

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

    def ajaxEditSupplier(int supplierId) {
        def supplier = supplierService.getSupplier(supplierId)

        render (template: "addSupplier", model: [supplier: supplier])
    }

    def ajaxSaveSupplier() {
        def supplier

        if (params.id && Integer.parseInt(params.id) > 0) {
            supplier = supplierService.getSupplier(Integer.parseInt(params.id))
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

    def ajaxAddSymbolGroupSubscription() {
        def symbolGroups = supplierService.getSymbolGroups()
        def subscribedSymbolGroupIds = supplierService.getSymbolGroupSubscriptions()?.collect { it.symbolGroup.id }

        symbolGroups.removeAll {subscribedSymbolGroupIds.contains(it.id) }

        render (template: "addSymbolGroupSubscription", model: [symbolGroups: symbolGroups])
    }

    def ajaxEditSymbolGroupSubscription(int symbolGroupSubscriptionId) {
        def symbolGroupSubscription = supplierService.getSymbolGroupSubscription(symbolGroupSubscriptionId)

        def symbolGroups = supplierService.getSymbolGroups()
        def subscribedSymbolGroupIds = supplierService.getSymbolGroupSubscriptions()?.collect { it.symbolGroup.id }

        symbolGroups.removeAll {subscribedSymbolGroupIds.contains(it.id) && it.id != symbolGroupSubscription.symbolGroup.id }

        render (template: "addSymbolGroupSubscription", model: [symbolGroupSubscription: symbolGroupSubscription, symbolGroups: symbolGroups])
    }

    def ajaxSaveSymbolGroupSubscription() {
        def symbolGroupSubscription

        if (params.id && Integer.parseInt(params.id) > 0) {
            symbolGroupSubscription = supplierService.getSymbolGroupSubscription(Integer.parseInt(params.id))
        } else {
            symbolGroupSubscription = new SymbolGroupSubscription()
        }

        bindData(symbolGroupSubscription, params)

        symbolGroupSubscription.retailerId = springSecurityService.principal.retailerId
        symbolGroupSubscription.storeId = springSecurityService.principal.storeId
        symbolGroupSubscription.status = SymbolGroupSubscriptionStatus.PENDING
        symbolGroupSubscription.active = true

        if (symbolGroupSubscription.validate()) {
            // Make sure the RabbitMQ connection is available, otherwise reject the save.
            try {
                if (!rabbitService.isOpen()) {
                    throw new Exception("Rabbit MQ not available")
                }

                supplierService.saveSymbolGroupSubscription(symbolGroupSubscription)

                // TODO Not always REGISTRATION
                SymbolGroupMessage symbolGroupMessage = new SymbolGroupMessage(SymbolGroupMessageType.REGISTRATION, springSecurityService.principal.retailerId, springSecurityService.principal.storeId)

                // TODO Considering using routing key to reach Nisa?
                rabbitService.sendExchangeMessage("SymbolGroups", gsonProvider.gson.toJson(symbolGroupMessage))

                render "OK"
            } catch (Exception e) {
                e.printStackTrace()

                def symbolGroups = supplierService.getSymbolGroups()
                def subscribedSymbolGroupIds = supplierService.getSymbolGroupSubscriptions()?.collect { it.symbolGroup.id }

                symbolGroups.removeAll {subscribedSymbolGroupIds.contains(it.id) && it.id != symbolGroupSubscription.symbolGroup.id }

                render (template: "addSymbolGroupSubscription", model: [symbolGroupSubscription: symbolGroupSubscription, symbolGroups: symbolGroups])
            }
        } else {
            def symbolGroups = supplierService.getSymbolGroups()
            def subscribedSymbolGroupIds = supplierService.getSymbolGroupSubscriptions()?.collect { it.symbolGroup.id }

            symbolGroups.removeAll {subscribedSymbolGroupIds.contains(it.id) && it.id != symbolGroupSubscription.symbolGroup?.id }

            render (template: "addSymbolGroupSubscription", model: [symbolGroupSubscription: symbolGroupSubscription, symbolGroups: symbolGroups])
        }
    }
}