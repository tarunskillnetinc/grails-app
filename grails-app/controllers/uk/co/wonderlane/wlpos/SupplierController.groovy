package uk.co.wonderlane.wlpos

import org.springframework.security.access.annotation.Secured
import uk.co.wonderlane.wlpos.entities.SnappyServiceMessage
import uk.co.wonderlane.wlpos.entities.SymbolGroupMessage
import uk.co.wonderlane.wlpos.enums.SnappyMessageType
import uk.co.wonderlane.wlpos.enums.SymbolGroupMessageType
import uk.co.wonderlane.wlpos.enums.SymbolGroupSubscriptionStatus
import uk.co.wonderlane.wlpos.supplier.Supplier
import uk.co.wonderlane.wlpos.supplier.SupplierCaseRate
import uk.co.wonderlane.wlpos.supplier.SupplierSortParams
import uk.co.wonderlane.wlpos.supplier.SymbolGroupSubscription

import java.text.NumberFormat
import java.text.SimpleDateFormat

class SupplierController {

    def springSecurityService
    def supplierService
    def rabbitService
    def gsonProvider

    private static final SUPPLIER_SORT_COLUMNS = [ "id", "name", "reference", "customerReference", "contactName", "email", "phoneNumber", "deleted" ]

    @Secured(['ROLE_ENGINEER', 'ROLE_HEAD_OFFICE'])
    def index() {}

    //This is for load symbol subscription (Affiliation) view initially
    def subscriptions() {}


    //search for suppliers
    @Secured(['ROLE_ENGINEER', 'ROLE_HEAD_OFFICE'])
    def ajaxGetSearchSupplier(SupplierSortParams sortParams) {
        session.SUPPLIER_CUSTOMER_REFERENCE_SEARCH_TERM = params.customerReferenceTerm
        session.SUPPLIER_REFERENCE_SEARCH_TERM = params.supplierReferenceTerm
        session.SUPPLIER_NAME_SEARCH_TERM = params.supplierNameTerm
        session.INCLUDE_DELETED_SUPPLIERS = params.includeDeletedSuppliers

        sortParams.validateParams(SUPPLIER_SORT_COLUMNS) //pre process supplier sorting column list

        def suppliers = [] //declare supplier list
        def suppliersResponse = supplierService.getSuppliers(params.supplierNameTerm, params.supplierReferenceTerm, params.customerReferenceTerm, params.includeDeletedSuppliers,
                sortParams.offset ? sortParams.offset : 0, sortParams.max ? sortParams.max : 50, sortParams.sortColumn, sortParams.getSortOrder())
        def returnedSuppliers = suppliersResponse?.suppliers
        def totalCount = suppliersResponse?.totalCount
        if (returnedSuppliers != null && returnedSuppliers.size() > 0){
            suppliers = returnedSuppliers
        }
        render(template: "supplierSearchResults",
                model: [ suppliers: suppliers,
                         searchTerm: params.searchTerm,
                         max: sortParams.max ?: 50,
                         offset: sortParams.offset,
                         sortParams  : sortParams,
                         totalCount : totalCount
                ])
    }

    def ajaxGetSymbolGroupSubscriptions() {
        def symbolGroupSubscriptions = supplierService.getSymbolGroupSubscriptions()
        def symbolGroups = supplierService.getSymbolGroups()
        if (!springSecurityService.principal.retailer.config.snappyShopperEnabled) {
            symbolGroupSubscriptions.removeAll { it.symbolGroup.id == 4 }
            symbolGroups.removeAll {it.symbolGroup.id == 4 }
        }
        render(template: "symbolGroupSubscriptionsSearchResults", model: [symbolGroupSubscriptions: symbolGroupSubscriptions])
    }

    //This will load save supplier view and initially pass enable save
    @Secured(['ROLE_ENGINEER', 'ROLE_HEAD_OFFICE'])
    def ajaxAddSupplier() {
        render(template: "addSupplier", model: [enableSave: true, isUpdate: false])
    }

    //This will load edit supplier with supplier details
    @Secured(['ROLE_ENGINEER', 'ROLE_HEAD_OFFICE'])
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

        SupplierCaseRate[] supplierCaseRates = SupplierCaseRate.getAllCaseRates(springSecurityService.principal.retailerId, supplier)

        render(template: "addSupplier", model: [supplier: supplier, supplierCaseRates: supplierCaseRates, enableSave: enableSave, isUpdate: supplier ? true : false])
    }

    @Secured(['ROLE_ENGINEER', 'ROLE_HEAD_OFFICE'])
    def ajaxSetSupplierDeletedFlag(int supplierId, boolean supplierDeletedFlag) {
        def supplier = supplierService.getSupplier(supplierId) //Load supplier
        if (supplier != null) {
            supplier.deleted = supplierDeletedFlag;
            supplierService.saveSupplier(supplier)
            render "OK"
        }
    }

    def parseAmount(String value) throws Exception {
        new BigDecimal(NumberFormat.getInstance(Locale.UK).parse(value)?.toString())
    }

    @Secured(['ROLE_ENGINEER', 'ROLE_HEAD_OFFICE'])
    def ajaxSaveSupplier() {
        def supplier
        def isUpdate
        if (params.id && Integer.parseInt(params.id) > 0) {
            supplier = supplierService.getSupplier(Integer.parseInt(params.id))
            isUpdate = true
        } else {
            supplier = new Supplier()
            supplier.retailerId = springSecurityService.principal.retailerId
            supplier.storeId = springSecurityService.principal.storeId
            isUpdate = false
        }

        def newSupplierCaseRate

        if (!(params.caserateeffectivedate?.empty && (params.caserate == null || params.caserate?.empty || params.caserate == "0.00"))) {
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");

            newSupplierCaseRate = new SupplierCaseRate()
            newSupplierCaseRate.supplier = supplier
            try {
                newSupplierCaseRate.caseRateEffectiveDate = dateFormat.parse(params.caserateeffectivedate)
            }
            catch (Exception ignored) {
                newSupplierCaseRate.errors.reject("supplier.suppliercaserate.date.invalid")
            }
            try {
                newSupplierCaseRate.caseRate = parseAmount(params.caserate)

                if (newSupplierCaseRate.caseRate == BigDecimal.ZERO) {
                    newSupplierCaseRate.errors.reject("supplier.suppliercaserate.caserate.must.be.not.zero")
                }
            }
            catch (Exception ignored) {
                newSupplierCaseRate.errors.reject("supplier.suppliercaserate.caserate.invalid")
            }
        }

        bindData(supplier, params)
        if (supplier.validate() && (newSupplierCaseRate == null || !newSupplierCaseRate.hasErrors())) {
            supplier = supplierService.saveSupplier(supplier)

            if (newSupplierCaseRate != null) {
                newSupplierCaseRate.supplier = supplier
                supplierService.saveSupplierCaseRate(newSupplierCaseRate)
            } else {
                supplierService.clearCurrentAndFutureSupplierCaseRates(supplier.id)
            }

            render "OK"
        } else {
            render(template: "addSupplier", model: [supplier: supplier, supplierCaseRate: newSupplierCaseRate, enableSave: true, isUpdate: isUpdate])
        }
    }

    def ajaxAddSymbolGroupSubscription() {
        def symbolGroups = supplierService.getSymbolGroups()

        if (!springSecurityService.principal.storeId || !springSecurityService.principal.retailer.config.snappyShopperEnabled) {
            symbolGroups.removeAll {it.id == 4 }
        }

        def subscribedSymbolGroupIds = supplierService.getSymbolGroupSubscriptions()?.collect { it.symbolGroup.id }

        symbolGroups.removeAll { subscribedSymbolGroupIds.contains(it.id) }

        render(template: "addSymbolGroupSubscription", model: [symbolGroups: symbolGroups])
    }

    def ajaxEditSymbolGroupSubscription(int symbolGroupSubscriptionId) {
        def symbolGroupSubscription = supplierService.getSymbolGroupSubscription(symbolGroupSubscriptionId)

        def symbolGroups = supplierService.getSymbolGroups()

        if (!springSecurityService.principal.storeId || !springSecurityService.principal.retailer.config.snappyShopperEnabled) {
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
                if (springSecurityService.principal.retailer.config.snappyShopperEnabled) {
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
                supplierService.saveSymbolGroupSubscription(symbolGroupSubscription)

                // TODO Not always REGISTRATION
                switch (symbolGroupSubscription.getSymbolGroupId()) {
                    case 1: // Nisa
                        SymbolGroupMessage symbolGroupMessage = new SymbolGroupMessage(SymbolGroupMessageType.REGISTRATION, springSecurityService.principal.retailerId, springSecurityService.principal.storeId)

                        // TODO Considering using routing key to reach Nisa?
                        rabbitService.setVirtualHost("/")
                        rabbitService.init()
                        rabbitService.sendExchangeMessage("SymbolGroups", gsonProvider.gson.toJson(symbolGroupMessage))
                        break;
                    case 4: // Snappy
                        SnappyServiceMessage snappyServiceMessage = new SnappyServiceMessage(SnappyMessageType.REGISTRATION, springSecurityService.principal.retailerId, springSecurityService.principal.storeId)

                        rabbitService.setVirtualHost("/")
                        rabbitService.init()
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