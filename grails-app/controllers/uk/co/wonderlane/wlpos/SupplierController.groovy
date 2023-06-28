package uk.co.wonderlane.wlpos

import uk.co.wonderlane.wlpos.entities.SnappyServiceMessage
import uk.co.wonderlane.wlpos.entities.SymbolGroupMessage
import uk.co.wonderlane.wlpos.enums.SnappyMessageType
import uk.co.wonderlane.wlpos.enums.SymbolGroupMessageType
import uk.co.wonderlane.wlpos.enums.SymbolGroupSubscriptionStatus
import uk.co.wonderlane.wlpos.supplier.Supplier
import uk.co.wonderlane.wlpos.supplier.SupplierSortParams
import uk.co.wonderlane.wlpos.supplier.SymbolGroupSubscription

class SupplierController {

    def springSecurityService
    def supplierService
    def rabbitService
    def gsonProvider

    private static final SUPPLIER_SORT_COLUMNS = [ "name", "reference", "customerReference", "contactName", "email", "phoneNumber"]

    def index() {}

    //This is for load symbol subscription (Affiliation) view initially
    def subscriptions() {}

    //search for suppliers
    def ajaxGetSearchSupplier(SupplierSortParams sortParams) {
        sortParams.validateParams(SUPPLIER_SORT_COLUMNS) //pre process supplier sorting column list
        def suppliers = [] //declare supplier list
        def suppliersResponse = supplierService.getSuppliers(params.searchTerm, params.searchBy, sortParams.offset ? sortParams.offset : 0, sortParams.max ? sortParams.max : 50, sortParams.sortColumn, sortParams.getSortOrder())
        def returnedSuppliers = suppliersResponse?.suppliers
        def totalCount = suppliersResponse?.totalCount
        if (returnedSuppliers != null && returnedSuppliers.size() > 0){
            suppliers = returnedSuppliers
        }
        render(template: "supplierSearchResults",
                model: [ suppliers: suppliers,
                         searchTerm: params.searchTerm,
                         searchBy: params.searchBy,
                         max: sortParams.max ?: 50,
                         offset: sortParams.offset,
                         sortParams  : sortParams,
                         totalCount : totalCount
                ])
    }

    def ajaxGetSymbolGroupSubscriptions() {
        def symbolGroupSubscriptions = supplierService.getSymbolGroupSubscriptions()
        def symbolGroups = supplierService.getSymbolGroups()
        if (!springSecurityService.principal.retailer.snappyShopperEnabled) {
            symbolGroupSubscriptions.removeAll { it.symbolGroup.id == 4 }
            symbolGroups.removeAll {it.symbolGroup.id == 4 }
        }
        render(template: "symbolGroupSubscriptionsSearchResults", model: [symbolGroupSubscriptions: symbolGroupSubscriptions])
    }

    //This will load save supplier view and initially pass enable save
    def ajaxAddSupplier() {
        render(template: "addSupplier", model: [enableSave : true])
    }

    //This will load edit supplier with supplier details
    def ajaxEditSupplier(int supplierId) {
        boolean enableSave = false; //Initially mark as disable edit
        def supplier = supplierService.getSupplier(supplierId) //Load supplier
        if (supplier != null){
            def supplierStoreId = supplier.storeId
            def currentStoreId = springSecurityService.principal.storeId
            if (supplierStoreId == currentStoreId){ //If supplier store id and logged in store id is same then enable save
                enableSave = true
            }
        }
        render(template: "addSupplier", model: [supplier: supplier, enableSave : enableSave])
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
            render(template: "addSupplier", model: [supplier: supplier, enableSave : true])
        }
    }

    def ajaxAddSymbolGroupSubscription() {
        def symbolGroups = supplierService.getSymbolGroups()

        if (!springSecurityService.principal.storeId || !springSecurityService.principal.retailer.snappyShopperEnabled) {
            symbolGroups.removeAll {it.id == 4 }
        }

        def subscribedSymbolGroupIds = supplierService.getSymbolGroupSubscriptions()?.collect { it.symbolGroup.id }

        symbolGroups.removeAll { subscribedSymbolGroupIds.contains(it.id) }

        render(template: "addSymbolGroupSubscription", model: [symbolGroups: symbolGroups])
    }

    def ajaxEditSymbolGroupSubscription(int symbolGroupSubscriptionId) {
        def symbolGroupSubscription = supplierService.getSymbolGroupSubscription(symbolGroupSubscriptionId)

        def symbolGroups = supplierService.getSymbolGroups()

        if (!springSecurityService.principal.storeId || !springSecurityService.principal.retailer.snappyShopperEnabled) {
            symbolGroups.removeAll { it.id == 4 }
        }

        def subscribedSymbolGroupIds = supplierService.getSymbolGroupSubscriptions()?.collect { it.symbolGroup.id }

        symbolGroups.removeAll { subscribedSymbolGroupIds.contains(it.id) && it.id != symbolGroupSubscription.symbolGroup.id }

        render(template: "addSymbolGroupSubscription", model: [symbolGroupSubscription: symbolGroupSubscription, symbolGroups: symbolGroups])
    }

    def ajaxSymbolGroupAction() {
        switch (Integer.parseInt(params.symbolGroupId)) {
            case 4: // Snappy
                SnappyServiceMessage snappyServiceMessage = new SnappyServiceMessage(SnappyMessageType.SYNC, springSecurityService.principal.retailerId, springSecurityService.principal.storeId)

                rabbitService.setVirtualHost("/")
                rabbitService.sendQueueMessage("SnappyService", gsonProvider.gson.toJson(snappyServiceMessage))
                break;
        }

        render status: 200, text: "Sync should begin shortly for Snappy Service in Store " + springSecurityService.principal.storeNumber + "."
    }

    def ajaxGetSymbolGroupForm(int symbolGroupId) {
        def symbolGroupSubscription = supplierService.getSymbolGroupSubscription(symbolGroupId)
        def symbolGroups = supplierService.getSymbolGroups()

        if (symbolGroupSubscription != null) {
            def subscribedSymbolGroupIds = supplierService.getSymbolGroupSubscriptions()?.collect { it.symbolGroup.id }

            symbolGroups.removeAll { subscribedSymbolGroupIds.contains(it.id) && it.id != symbolGroupSubscription.symbolGroup.id }
        }

        switch (symbolGroupId) {
            case 1: // Nisa
                render(template: "addSymbolGroupSubscriptionNisa", model: [symbolGroupSubscription: symbolGroupSubscription,
                                                                           symbolGroups           : symbolGroups])
                break;
            case 4: // Snappy
                if (springSecurityService.principal.retailer.snappyShopperEnabled) {
                    render(template: "addSymbolGroupSubscriptionSnappy", model: [symbolGroupSubscription: symbolGroupSubscription,
                                                                                 symbolGroups           : symbolGroups])
                }
                break;
        }
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
                switch (symbolGroupSubscription.getSymbolGroupId()) {
                    case 1: // Nisa
                        SymbolGroupMessage symbolGroupMessage = new SymbolGroupMessage(SymbolGroupMessageType.REGISTRATION, springSecurityService.principal.retailerId, springSecurityService.principal.storeId)

                        // TODO Considering using routing key to reach Nisa?
                        rabbitService.setVirtualHost("/")
                        rabbitService.sendExchangeMessage("SymbolGroups", gsonProvider.gson.toJson(symbolGroupMessage))
                        break;
                    case 4: // Snappy
                        SnappyServiceMessage snappyServiceMessage = new SnappyServiceMessage(SnappyMessageType.REGISTRATION, springSecurityService.principal.retailerId, springSecurityService.principal.storeId)

                        rabbitService.setVirtualHost("/")
                        rabbitService.sendQueueMessage("SnappyService", gsonProvider.gson.toJson(snappyServiceMessage))
                        break;
                }

                render "OK"
            } catch (Exception e) {
                e.printStackTrace()

                def symbolGroups = supplierService.getSymbolGroups()
                def subscribedSymbolGroupIds = supplierService.getSymbolGroupSubscriptions()?.collect { it.symbolGroup.id }

                symbolGroups.removeAll { subscribedSymbolGroupIds.contains(it.id) && it.id != symbolGroupSubscription.symbolGroup.id }

                render(template: "addSymbolGroupSubscription", model: [symbolGroupSubscription: symbolGroupSubscription, symbolGroups: symbolGroups])
            }
        } else {
            def symbolGroups = supplierService.getSymbolGroups()
            def subscribedSymbolGroupIds = supplierService.getSymbolGroupSubscriptions()?.collect { it.symbolGroup.id }

            symbolGroups.removeAll { subscribedSymbolGroupIds.contains(it.id) && it.id != symbolGroupSubscription.symbolGroup?.id }

            render(template: "addSymbolGroupSubscription", model: [symbolGroupSubscription: symbolGroupSubscription, symbolGroups: symbolGroups])
        }
    }
}