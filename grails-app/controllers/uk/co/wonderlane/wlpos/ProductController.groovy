package uk.co.wonderlane.wlpos

import grails.plugin.springsecurity.annotation.Secured
import groovy.json.JsonSlurper
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import org.joda.time.format.DateTimeFormat
import org.joda.time.format.DateTimeFormatter
import uk.co.wonderlane.wlpos.entities.SnappyServiceMessage
import uk.co.wonderlane.wlpos.entities.SyncMessage
import uk.co.wonderlane.wlpos.enums.PackStatus
import uk.co.wonderlane.wlpos.enums.ProductHistoryType
import uk.co.wonderlane.wlpos.enums.ProductStatus
import uk.co.wonderlane.wlpos.enums.SnappyMessageType
import uk.co.wonderlane.wlpos.enums.SyncMessageType
import uk.co.wonderlane.wlpos.supplier.Pack
import uk.co.wonderlane.wlpos.supplier.Supplier
import uk.co.wonderlane.wlpos.reporting.ReportType
import uk.co.wonderlane.wlpos.reporting.ReportColumns
import uk.co.wonderlane.wlpos.reporting.ReportColumn

import java.util.stream.Collectors

class ProductController {

    def springSecurityService

    def productService
    def categoryService
    def restrictionsService
    def supplierService
    def tagService
    def rabbitService
    def gsonProvider

    /**
     * Landing page of the controller action - displays the product search screen.
     */
    def index() {
        [userColumns: productService.getColumns()]
    }

    def show(int id) {
        setEffectiveDate()
        def product = productService.getProduct(id)
        DateTime now = DateTime.now(DateTimeZone.UTC)
        if (!product) {
            flash.message = "Product not found"
            redirect(action: "index")
            return
        }
        def ranges = []
        def priceBands = []
        def productCategoryList = []
        def category = product.category
        def productId = product.id
        while (category) {
            productCategoryList.add(category.id)
            category = category.parentCategory
        }
        def userRoles = springSecurityService.principal.authorities*.authority
        if (userRoles.contains("ROLE_HEAD_OFFICE") || userRoles.contains("ROLE_ENGINEER")) {
            priceBands = PriceBand.findAllByRetailerId(springSecurityService.principal.retailerId, [sort: "description", order: "asc"])
            ranges = Range.findAllByRetailerId(springSecurityService.principal.retailerId, [sort: "description", order: "asc"])
        }

        render(view: "add", model: [product            : product,
                                    storeId            : springSecurityService.principal.storeId,
                                    statusValues       : ProductStatus.values(),
                                    categoryValues     : categoryService.getFullCategoryHierarchy(),
                                    productCategoryList: productCategoryList,
                                    vatValues          : VatCode.findAllByRetailerId(springSecurityService.principal.retailerId),
                                    ranges             : ranges,
                                    priceBands         : priceBands,
                                    effectiveDateIndex : session.effectiveDate,
                                    now                : now,
                                    navlink            : "details",
                                    snappyEnabled      : Retailer.findById(springSecurityService.principal.retailerId).isSnappyShopperEnabled()])
    }

    private void setEffectiveDate() {
        DateTimeFormatter formatter = DateTimeFormat.forPattern("dd MMMM yyyy").withZone(DateTimeZone.UTC)
        def effectiveDateSelected
        if (params.get("effectiveDate")) {
            effectiveDateSelected = params.get("effectiveDate") == "Current" ? DateTime.now(DateTimeZone.UTC).withTimeAtStartOfDay() : DateTime.parse(params.get("effectiveDate"), formatter).withTimeAtStartOfDay()
            session.effectiveDate = [effectiveDateSelected.toString(formatter), effectiveDateSelected]
        } else {
            session.effectiveDate = ["Current", DateTime.now(DateTimeZone.UTC)]
        }
    }

    def add() {
        setEffectiveDate()
        def ranges = []
        def priceBands = []
        def userRoles = springSecurityService.principal.authorities*.authority
        if (userRoles.contains("ROLE_HEAD_OFFICE") || userRoles.contains("ROLE_ENGINEER")) {
            priceBands = PriceBand.findAllByRetailerId(springSecurityService.principal.retailerId, [sort: "description", order: "asc"])
            ranges = Range.findAllByRetailerId(springSecurityService.principal.retailerId, [sort: "description", order: "asc"])
        }
        render(view: "add", model: [storeId: springSecurityService.principal.storeId,
                                    statusValues: ProductStatus.values(),
                                    categoryValues: categoryService.getFullCategoryHierarchy(),
                                    vatValues: VatCode.findAllByRetailerId(springSecurityService.principal.retailerId),
                                    ranges: ranges,
                                    priceBands: priceBands,
                                    now: DateTime.now(DateTimeZone.UTC).withTimeAtStartOfDay(),
                                    isNewProduct: true])
    }

    def search() {
        session.PRODUCT_SEARCH_TERM = params.searchTerm
        session.effectiveDate = ["Current", DateTime.now(DateTimeZone.UTC)]

        def products = productService.searchProducts(params.searchTerm, params.searchBy, 50, 0, "id", "asc")

        render(template: "addProductSearchResults", model: [ products: products, totalResults: products.totalCount, storeId: springSecurityService.principal.storeId ])
    }

    /**
     * Called from the main product maintenance search screen.
     */
    def ajaxSearchProducts() {
        session.PRODUCT_SEARCH_TERM = params.searchTerm
        session.effectiveDate = ["Current", DateTime.now(DateTimeZone.UTC)]

        def products = productService.searchProducts(params.searchTerm, params.searchBy, params.max ? Integer.parseInt(params.max) : 50, params.offset ? Integer.parseInt(params.offset) : 0, "id", "asc")

        render(template: "productSearchResults", model: [products: products,
                                                         storeId: springSecurityService.principal.storeId,
                                                         userColumns: productService.getColumns(),
                                                         searchTerm: params.searchTerm,
                                                         searchBy: params.searchBy,
                                                         max: params.max ?: 50,
                                                         offset: params.offset,
                                                         totalResults: products.totalCount])
    }

    @Secured(['ROLE_ENGINEER', 'ROLE_HEAD_OFFICE'])
    def prices() {
        def categories = categoryService.getFullCategoryHierarchy()
        def tags = tagService.getTags()
        def priceBands = PriceBand.findAllByRetailerId(springSecurityService.principal.retailerId, [sort: "description", order: "asc"])

        [categories: categories, tags: tags, priceBands: priceBands]
    }

    @Secured(['ROLE_ENGINEER', 'ROLE_HEAD_OFFICE'])
    def pricesSearch() {
        String searchTerm = params.searchTerm
        Integer categoryId = params.category ? Integer.parseInt(params.category) : null
        Integer tagId = params.tag ? Integer.parseInt(params.tag) : null

        def productPrices = productService.searchProductPrices(searchTerm, categoryId, tagId)
        def priceBands = PriceBand.findAllByRetailerId(springSecurityService.principal.retailerId, [sort: "description", order: "asc"])

        render(template: "pricesSearchResults", model: [productPrices: productPrices, priceBands: priceBands])
    }

    @Secured(['ROLE_ENGINEER', 'ROLE_HEAD_OFFICE'])
    def ranges() {
        def categories = categoryService.getFullCategoryHierarchy()
        def tags = tagService.getTags()
        def ranges = Range.findAllByRetailerId(springSecurityService.principal.retailerId, [sort: "description", order: "asc"])

        [categories: categories, tags: tags, ranges: ranges]
    }

    @Secured(['ROLE_ENGINEER', 'ROLE_HEAD_OFFICE'])
    def rangesSearch() {
        String searchTerm = params.searchTerm
        Integer categoryId = params.category ? Integer.parseInt(params.category) : null
        Integer tagId = params.tag ? Integer.parseInt(params.tag) : null

        def rangeProducts = productService.searchRangeProducts(searchTerm, categoryId, tagId)
        def ranges = Range.findAllByRetailerId(springSecurityService.principal.retailerId, [sort: "description", order: "asc"])

        render(template: "/product/rangesSearchResults", model: [rangeProducts: rangeProducts, ranges: ranges])
    }

    @Secured(['ROLE_ENGINEER', 'ROLE_HEAD_OFFICE'])
    def supplierUpdates() {
        def suppliers = Supplier.findAllByRetailerId(springSecurityService.principal.retailerId, [sort: "name"])
        def categories = categoryService.getFullCategoryHierarchy()
        def priceBands = PriceBand.findAllByRetailerId(springSecurityService.principal.retailerId, [sort: "description"])

        [suppliers: suppliers, categories: categories, priceBands: priceBands]
    }

    @Secured(['ROLE_ENGINEER', 'ROLE_HEAD_OFFICE'])
    def supplierUpdatesSearch() {
        Integer supplierId = params.supplierId ? Integer.parseInt(params.supplierId) : null
        Integer categoryId = params.categoryId ? Integer.parseInt(params.categoryId) : null
        DateTimeFormatter dateFormatter = DateTimeFormat.forPattern("dd/MM/yyyy").withZone(DateTimeZone.UTC)
        DateTime sinceDate = params.sinceDate ? DateTime.parse(params.sinceDate, dateFormatter) : DateTime.now(DateTimeZone.UTC)
        Integer priceBandId = params.priceBandId ? Integer.parseInt(params.priceBandId) : null
        int offset = params.offset ? Integer.parseInt(params.offset) : 0
        int max = params.max ? Integer.parseInt(params.max) : 200

        def supplierPriceUpdates = supplierService.getSupplierPriceUpdates(sinceDate, priceBandId, supplierId, categoryId, offset, max)

        render(template: "/product/supplierUpdatesSearchResults", model: [supplierPriceUpdates: supplierPriceUpdates.results, totalCount: supplierPriceUpdates.totalCount])
    }

    @Secured(['ROLE_ENGINEER', 'ROLE_HEAD_OFFICE'])
    def ajaxSaveSupplierPriceUpdates() {
        Integer supplierId = params.supplierId ? Integer.parseInt(params.supplierId) : null
        Integer categoryId = params.categoryId ? Integer.parseInt(params.categoryId) : null
        DateTimeFormatter dateFormatter = DateTimeFormat.forPattern("dd/MM/yyyy").withZone(DateTimeZone.UTC)
        DateTime sinceDate = params.sinceDate ? DateTime.parse(params.sinceDate, dateFormatter) : DateTime.now(DateTimeZone.UTC)
        Integer priceBandId = params.priceBandId ? Integer.parseInt(params.priceBandId) : null
        DateTime effectiveDate = params.effectiveDate ? DateTime.parse(params.effectiveDate, dateFormatter) : DateTime.now(DateTimeZone.UTC)
        boolean acceptRrps = params.acceptRrps ? Boolean.valueOf(params.acceptRrps) : false
        int offset = params.offset ? Integer.parseInt(params.offset) : 0
        int max = 100000

        SavePriceChangesCommand savePriceChangesCommand = new SavePriceChangesCommand()
        bindData(savePriceChangesCommand, params)

        PriceBand priceBand = PriceBand.findByIdAndRetailerId(priceBandId, springSecurityService.principal.retailerId)

        if (!priceBand) {
            response.status = 400 // TODO Figure out how to handle errors with messages on the page.
            return
        }

        if (savePriceChangesCommand.priceChanges && savePriceChangesCommand.priceChanges.size() > 0 && acceptRrps) {
            log.println("Saving ${savePriceChangesCommand.priceChanges.size()} supplier price updates for retailer ${springSecurityService.principal.retailerId} accepting RRPs")

            // We were sent exact products to accept RRPs for.
            supplierService.saveSupplierPriceUpdates(savePriceChangesCommand.priceChanges, priceBand, effectiveDate)

            syncSupplierPriceUpdates(savePriceChangesCommand.priceChanges, priceBand, effectiveDate)
        } else if (savePriceChangesCommand.priceChanges && savePriceChangesCommand.priceChanges.size() > 0) {
            log.println("Saving ${savePriceChangesCommand.priceChanges.size()} supplier price updates for retailer ${springSecurityService.principal.retailerId}")

            // We were sent exact products and their prices.
            supplierService.saveSupplierPriceUpdates(savePriceChangesCommand.priceChanges, priceBand, effectiveDate)

            syncSupplierPriceUpdates(savePriceChangesCommand.priceChanges, priceBand, effectiveDate)
        } else if (acceptRrps) {
            // We were not sent any specific products, but it was the "Accept RRPs" button which was used.
            def supplierPriceUpdates = supplierService.getSupplierPriceUpdates(sinceDate, priceBand.id, supplierId, categoryId, offset, max)

            if (supplierPriceUpdates.totalCount > 0) {
                log.println("Saving ${supplierPriceUpdates.totalCount} supplier price updates for retailer ${springSecurityService.principal.retailerId} accepting all RRPs")

                supplierService.saveSupplierPriceUpdates(supplierPriceUpdates.results, priceBand, effectiveDate)

                syncSupplierPriceUpdates(supplierPriceUpdates.results, priceBand, effectiveDate)
            }
        } else {
            // We didn't select any products, and we used the "Save" button so we do nothing.
        }

        response.status = 204
    }

    private void syncSupplierPriceUpdates(List supplierPriceUpdates, PriceBand priceBand, DateTime effectiveDate) {
        def productPrices = []
        def productIds = []

        supplierPriceUpdates.eachWithIndex { priceUpdate, index ->
            uk.co.wonderlane.wlpos.entities.ProductPrice productPrice = new uk.co.wonderlane.wlpos.entities.ProductPrice()
            //productPrice.setId(id) CHECK IF THIS IS USED ON THE TILL, ASSUMING NOT.
            productPrice.setSku(priceUpdate.sku)
            productPrice.setPriceBandId(priceBand.id)
            productPrice.setEffectiveDate(effectiveDate)

            if (priceUpdate instanceof PriceChangeCommand) {
                productPrice.setPrice(priceUpdate.price)

                if ((!priceUpdate.oldPrice || priceUpdate.oldPrice == BigDecimal.ZERO) && priceUpdate.price != BigDecimal.ZERO) {
                    productIds.add(priceUpdate.productId)
                }
            } else if (priceUpdate.recommendedRetailPrice) {
                productPrice.setPrice(priceUpdate.recommendedRetailPrice)
            } else {
                return // Note this is return from this closure, i.e. more like a "continue" for the loop.
            }

            productPrices.add(productPrice)
        }

        syncProductUpdates(productIds)

        if (isSingleStageSel()) {
            SyncMessage syncMessage = new SyncMessage(SyncMessageType.PRICE_CHANGE, springSecurityService.principal.retailerId, 0, 0, 0)
            syncMessage.setInsert(true)
            syncMessage.setProductPrices(productPrices)

            def stores = StoreSettings.findAllByRetailerIdAndPriceBandAndStoreIdIsNotNull(springSecurityService.principal.retailerId, priceBand)

            stores?.each { store ->
                log.println("Syncing ${productPrices.size()} supplier price updates to store ${store.storeId}")

                syncMessage.setStoreNumber(store.storeId)
                syncMessage.setStoreId(store.id)

                rabbitService.declareExchange(String.format("R%d_S%d", syncMessage.getRetailerId(), syncMessage.getStoreNumber()))
                rabbitService.sendExchangeMessage(String.format("R%d_S%d", syncMessage.getRetailerId(), syncMessage.getStoreNumber()), gsonProvider.gson.toJson(syncMessage))
            }
        }
    }

    @Secured(['ROLE_ENGINEER', 'ROLE_HEAD_OFFICE'])
    def ajaxSavePriceChanges(SavePriceChangesCommand cmd) {
        def now = DateTime.now(DateTimeZone.UTC)
        def priceBands = PriceBand.findAllByRetailerId(springSecurityService.principal.retailerId)

        def productPrices = []
        def productHistories = []
        def priceUpdates = [:]
        def productIds = []

        cmd.priceChanges?.each {priceChange ->
            if (!priceUpdates.containsKey(priceChange.priceBandId)) {
                priceUpdates[priceChange.priceBandId] = []
            }

            ProductPrice productPrice = new ProductPrice(priceBand: priceBands.find { it.id == priceChange.priceBandId }, sku: priceChange.sku, price: priceChange.price, effectiveDate: now)
            productPrices.add(productPrice)

            if (priceChange.oldPrice != priceChange.price) {
                ProductHistory productHistory = new ProductHistory(retailerId: springSecurityService.principal.retailerId, productId: priceChange.productId, fromValue: priceChange.oldPrice.toString(), toValue: priceChange.price.toString(), productHistoryType: ProductHistoryType.PRICE, priceBandId: priceChange.priceBandId, storeId: springSecurityService.principal.storeId, userId: springSecurityService.principal.id, usersName: springSecurityService.principal.usersName, effectiveDate: now, updateDate: now)
                productHistories.add(productHistory)
            }

            if ((!priceChange.oldPrice || priceChange.oldPrice == BigDecimal.ZERO) && priceChange.price != BigDecimal.ZERO) {
                productIds.add(priceChange.productId)
            }

            priceUpdates[priceChange.priceBandId].add(productPrice.getProductPrice())
        }

        productService.saveProductPrices(productPrices, productHistories)

        syncProductUpdates(productIds)

        priceUpdates.each { priceBandId, priceChanges ->
            if (isSingleStageSel()) {
                SyncMessage syncMessage = new SyncMessage(SyncMessageType.PRICE_CHANGE, springSecurityService.principal.retailerId, 0, 0, 0)
                syncMessage.setInsert(true)
                syncMessage.setProductPrices(priceChanges)

                def stores = StoreSettings.findAllByRetailerIdAndPriceBandAndStoreIdIsNotNull(springSecurityService.principal.retailerId, priceBands.find { it.id == priceBandId })

                stores?.each { store ->
                    syncMessage.setStoreNumber(store.storeId)
                    syncMessage.setStoreId(store.id)

                    rabbitService.declareExchange(String.format("R%d_S%d", syncMessage.getRetailerId(), syncMessage.getStoreNumber()))
                    rabbitService.sendExchangeMessage(String.format("R%d_S%d", syncMessage.getRetailerId(), syncMessage.getStoreNumber()), gsonProvider.gson.toJson(syncMessage))
                }
            }
        }

        render "OK"
    }

    @Secured(['ROLE_ENGINEER', 'ROLE_HEAD_OFFICE'])
    def ajaxSaveRangeProducts(SaveRangeProductsCommand cmd) {
        def ranges = Range.findAllByRetailerId(springSecurityService.principal.retailerId)

        def newlyRangedProducts = []
        def noLongerRangedProducts = []

        def rangedProductsMap = [:]

        cmd.rangeProducts?.each {rangeProduct ->
            def existingRangeProduct = RangeProduct.findByProductIdAndRange(rangeProduct.productId, ranges.find { it.id == rangeProduct.rangeId })

            if (rangeProduct.isRanged() && !existingRangeProduct) {
                existingRangeProduct = new RangeProduct(range: ranges.find { it.id == rangeProduct.rangeId }, productId: rangeProduct.productId)

                newlyRangedProducts.add(existingRangeProduct)

                if (!rangedProductsMap.containsKey(rangeProduct.rangeId)) {
                    rangedProductsMap[rangeProduct.rangeId] = []
                }

                rangedProductsMap[rangeProduct.rangeId].add(rangeProduct)
            } else if (!rangeProduct.isRanged() && existingRangeProduct) {
                noLongerRangedProducts.add(existingRangeProduct)
            }
        }

        productService.saveRangeProducts(newlyRangedProducts)
        productService.deleteRangeProducts(noLongerRangedProducts)

        // Send down those products for addition to the relevant stores for each range. Do not delete any products as stores may need to sell through stock etc.
        rangedProductsMap.each { rangeId, rangeProductChanges ->
            def stores = StoreSettings.findAllByRetailerIdAndRangeAndStoreIdIsNotNull(springSecurityService.principal.retailerId, ranges.find { it.id == rangeId })

            def allProducts = []

            rangeProductChanges.each { RangeProductCommand rangeProductCommand ->
                allProducts.add(productService.getProduct(rangeProductCommand.productId))
            }

            stores?.each { StoreSettings store ->
                SyncMessage syncMessage = new SyncMessage(SyncMessageType.PRODUCT, springSecurityService.principal.retailerId, store.storeId, store.id, 0)
                syncMessage.setInsert(true)

                def storeProducts = []
                allProducts.each {
                    def productEntity = it.getProduct(store.storeId)
                    if (checkProductHasPriceForStore(productEntity, store.storeId)) {
                        storeProducts.add(productEntity)
                    }
                }

                syncMessage.setProducts(storeProducts)

                // TODO Just declaring the exchange doesn't help us, we also need to declare all of the till queues and bind them to the exchange, otherwise the message we're about to send goes nowhere.
                if (storeProducts.size() > 0) {
                    rabbitService.declareExchange(String.format("R%d_S%d", syncMessage.getRetailerId(), syncMessage.getStoreNumber()))
                    rabbitService.sendExchangeMessage(String.format("R%d_S%d", syncMessage.getRetailerId(), syncMessage.getStoreNumber()), gsonProvider.gson.toJson(syncMessage))
                }
            }
        }

        render "OK"
    }

    def save(ProductCommand editedProduct) {
        editedProduct.variants?.removeIf({ it == null})
        def product
        def builder

        DateTimeFormatter dateFormatter = DateTimeFormat.forPattern("dd/MM/yyyy").withZone(DateTimeZone.UTC)
        def effectiveDate = params.effectiveDate ? DateTime.parse(params.effectiveDate, dateFormatter) : DateTime.now(DateTimeZone.UTC)

        boolean newProduct
        boolean changeAffectsSel = false

        if (params.id && Integer.parseInt(params.id) > 0) {
            newProduct = false
        } else {
            newProduct = true
        }

        DateTime now = DateTime.now(DateTimeZone.UTC)

        if (newProduct) {
            changeAffectsSel = true
            product = new Product(params)
            product.retailerId = springSecurityService.principal.retailerId
            product.restrictions = new Restrictions()

            copyRestrictions(editedProduct.restrictions, product.restrictions)

            product.variants?.each { variant ->
                variant.storeId = springSecurityService.principal.storeId
                variant.effectiveDate = effectiveDate

                variant.barcodez?.each { barcode ->
                    barcode.retailerId = springSecurityService.principal.retailerId
                    barcode.sku = variant.sku
                    barcode.effectiveDate = barcode.effectiveDate ?: effectiveDate

                    if (!barcode.validate()) {
                        product.errors.reject('product.barcodes.notUnique', [barcode.barcode] as Object[], 'Barcode {0} already exists on another SKU.')
                    }
                }

                variant.packs?.each { pack ->
                    pack.effectiveDate = pack.effectiveDate ?: now
                    pack.updateDatetime = now
                }
            }
        } else {
            product = productService.getProduct(Integer.parseInt(params.id))

            builder = new ProductHistoryBuilder(product.id, springSecurityService, effectiveDate)
            doComparison(builder, product, editedProduct)

            product.itemCode = editedProduct.itemCode
            changeAffectsSel = checkChangeAffectsSel(changeAffectsSel, product.description, editedProduct.description)
            product.description = editedProduct.description
            product.receiptDescription = editedProduct.receiptDescription
            product.category = editedProduct.category
            changeAffectsSel = checkChangeAffectsSel(changeAffectsSel, product.unitSize, editedProduct.unitSize)
            product.unitSize = editedProduct.unitSize
            product.weightedItem = editedProduct.weightedItem
            product.openPrice = editedProduct.openPrice
            product.zeroPrice = editedProduct.zeroPrice
            product.pricePerKg = editedProduct.pricePerKg
            product.snappyProduct = editedProduct.snappyProduct
            product.deliItem = editedProduct.deliItem
            product.vatCode = editedProduct.vatCode
            product.vatPercentageOverride = editedProduct.vatPercentageOverride
            product.discreetMessage = editedProduct.discreetMessage
            product.status = editedProduct.status
            product.retailerProductId = editedProduct.retailerProductId

            if (isRestrictionsChanged(editedProduct.restrictions, product.restrictions)) {
                if (editedProduct.restrictions.id == product.category.restrictions.id) {
                    // Changed restrictions and the product was currently pointing at the category restrictions object. Create a new restrictions.
                    product.restrictions = new Restrictions()
                }

                copyRestrictions(editedProduct.restrictions, product.restrictions)
            }

            // Variants.
            editedProduct.variants?.each {editedVariant ->
                def existingVariant = product.variants?.find {existingVariant -> existingVariant.id == editedVariant.id }

                if (editedVariant.id != 0 && existingVariant) {
                    // Variant we saved is one which already exists, check for changes.
                    if (builder.getChangedProductVariantIds().contains(existingVariant.id)) {
                        // Variant has changed
                        ProductVariant newVariant = new ProductVariant()
                        newVariant.storeId = springSecurityService.principal.storeId
                        newVariant.sku = editedVariant.sku
                        newVariant.retailPrice = editedVariant.retailPrice
                        changeAffectsSel = checkChangeAffectsSel(changeAffectsSel, existingVariant.costPrice, editedVariant.costPrice)
                        newVariant.costPrice = editedVariant.costPrice
                        newVariant.size = editedVariant.size
                        newVariant.colour = editedVariant.colour
                        newVariant.minimumStockLevel = editedVariant.minimumStockLevel
                        newVariant.effectiveDate = effectiveDate
                        newVariant.shelfLifeDays = editedVariant.shelfLifeDays

                        product.addToVariants(newVariant)

                        checkProductVariantForPackChanges(newVariant, editedVariant, now)
                        checkProductVariantForBarcodeChanges(product, newVariant, editedVariant, effectiveDate)
                    } else {
                        checkProductVariantForPackChanges(existingVariant, editedVariant, now)
                        checkProductVariantForBarcodeChanges(product, existingVariant, editedVariant, effectiveDate)
                    }
                } else {
                    changeAffectsSel = true
                    ProductVariant newVariant = new ProductVariant()
                    newVariant.storeId = springSecurityService.principal.storeId
                    newVariant.sku = editedVariant.sku
                    newVariant.retailPrice = editedVariant.retailPrice
                    newVariant.costPrice = editedVariant.costPrice
                    newVariant.size = editedVariant.size
                    newVariant.colour = editedVariant.colour
                    newVariant.minimumStockLevel = editedVariant.minimumStockLevel
                    newVariant.effectiveDate = effectiveDate
                    newVariant.shelfLifeDays = editedVariant.shelfLifeDays

                    editedVariant.packs?.each { editedPack ->
                        Pack newPack = new Pack()
                        updatePack(newPack, editedPack, now)
                        newVariant.addToPacks(newPack)
                    }

                    editedVariant.barcodez.forEach({
                        barcode ->
                            Barcode newBarcode = new Barcode()
                            newBarcode.sku = editedVariant.sku
                            newBarcode.retailerId = springSecurityService.principal.retailerId
                            newBarcode.barcode = barcode.barcode
                            newBarcode.effectiveDate = effectiveDate
                            newBarcode.recordStatus = 'C'

                            newVariant.barcodez.add(newBarcode)

                            if (!newBarcode.validate()) {
                                product.errors.reject('product.barcodes.notUnique', [newBarcode.barcode] as Object[], 'Barcode {0} already exists on another SKU.')
                            }
                    })

                    product.addToVariants(newVariant)
                }
            }
        }

        // Add default supplier Id at the end
        for (int i = 0; i <= editedProduct?.variants?.size(); i++) {
            product?.variants[i]?.defaultSupplierId = editedProduct?.variants[i]?.defaultSupplierId
        }

        if (!product.hasErrors() && product.validate()) {
            restrictionsService.saveRestrictions(product.restrictions) // Restrictions are validated as part of product.validate()
            productService.saveProduct(product)

            productService.saveBarcodes(product)

            if (builder && builder.productHistories) {
                productService.saveProductHistories(builder.productHistories)
            }

            def userRoles = springSecurityService.principal.authorities*.authority
            if ((userRoles.contains("ROLE_HEAD_OFFICE") || userRoles.contains("ROLE_ENGINEER")) && !springSecurityService.principal.storeId) {
                def priceChanges = []
                editedProduct?.priceChanges?.find{ it != null }.each {
                    priceChanges.addAll(it.priceChanges)
                }

                savePriceUpdates(product.variants?.findAll { it.storeId == null }, priceChanges, effectiveDate)
                saveRangeUpdates(product, editedProduct.rangeId)
            }

            flash.message = "Product saved successfully"
        }

        if (!product.hasErrors()) {
            if (isSingleStageSel() || !changeAffectsSel) {
                if (springSecurityService.principal.storeId) {
                    if (!rabbitService.isOpen()) {
                        throw new Exception("Rabbit MQ not available")
                    }

                    def productEntity = product.getProduct(springSecurityService.principal.storeId)
                    if (checkProductHasPriceForStore(productEntity, springSecurityService.principal.storeId)) {
                        SyncMessage syncMessage = new SyncMessage(SyncMessageType.PRODUCT, springSecurityService.principal.retailerId, springSecurityService.principal.storeNumber ?: 0, springSecurityService.principal.storeId ?: 0, 0)
                        syncMessage.setInsert(true)

                        List<uk.co.wonderlane.wlpos.entities.Product> products = new ArrayList<uk.co.wonderlane.wlpos.entities.Product>()
                        products.add(productEntity)
                        syncMessage.setProducts(products)

                        rabbitService.declareExchange(String.format("R%d_S%d", syncMessage.getRetailerId(), syncMessage.getStoreNumber()))
                        rabbitService.sendExchangeMessage(String.format("R%d_S%d", syncMessage.getRetailerId(), syncMessage.getStoreNumber()), gsonProvider.gson.toJson(syncMessage))
                    }
                } else {
                    def rangeProducts = RangeProduct.findAllByProductId(product.id)

                    rangeProducts?.each { rangeProduct ->
                        def stores = StoreSettings.findAllByRetailerIdAndRangeAndStoreIdIsNotNull(springSecurityService.principal.retailerId, rangeProduct.range)

                        stores?.each { StoreSettings store ->
                            def productEntity = product.getProduct(store.storeId)
                            if (checkProductHasPriceForStore(productEntity, store.storeId)) {
                                SyncMessage syncMessage = new SyncMessage(SyncMessageType.PRODUCT, springSecurityService.principal.retailerId, store.storeId, store.id, 0)
                                syncMessage.setInsert(true)

                                syncMessage.setProducts([productEntity])

                                // TODO Just declaring the exchange doesn't help us, we also need to declare all of the till queues and bind them to the exchange, otherwise the message we're about to send goes nowhere.
                                rabbitService.declareExchange(String.format("R%d_S%d", syncMessage.getRetailerId(), syncMessage.getStoreNumber()))
                                rabbitService.sendExchangeMessage(String.format("R%d_S%d", syncMessage.getRetailerId(), syncMessage.getStoreNumber()), gsonProvider.gson.toJson(syncMessage))
                            }
                        }
                    }
                }
            }
            redirect(action: "index")
        } else {
            def productCategoryList = []

            def category = product.category
            while (category) {
                productCategoryList.add(category.id)

                category = category.parentCategory
            }

            render(view: "add", model: [product       : product,
                                        storeId       : springSecurityService.principal.storeId,
                                        statusValues  : ProductStatus.values(),
                                        categoryValues: categoryService.getFullCategoryHierarchy(),
                                        productCategoryList: productCategoryList,
                                        effectiveDateIndex : session.effectiveDate,
                                        vatValues     : VatCode.findAllByRetailerId(springSecurityService.principal.retailerId)])
        }
    }

    private void checkProductVariantForBarcodeChanges(def product, def existingVariant, def editedVariant, DateTime effectiveDate) {
        editedVariant.barcodez?.each { editedBarcode ->
            def existingBarcode = existingVariant.barcodes?.find { existingBarcode -> existingBarcode.id == editedBarcode.id }

            if (!existingBarcode) {  // If no existing barcode then treat as newly added barcodes.
                Barcode barcode = new Barcode()
                barcode.sku = existingVariant.sku
                barcode.retailerId = springSecurityService.principal.retailerId
                barcode.barcode = editedBarcode.barcode
                barcode.effectiveDate = effectiveDate
                barcode.recordStatus = 'C'

                existingVariant.barcodez.add(barcode)

                if (!barcode.validate()) {
                    product.errors.reject(
                            'product.barcodes.notUnique',
                            [barcode.barcode] as Object[],
                            'Barcode {0} already exists on another SKU.')
                }
            } else { // If barcode do exists change update existing values
                existingBarcode.barcode = editedBarcode.barcode

                if (!existingBarcode.validate()) {
                    product.errors.reject(
                            'product.barcodes.notUnique',
                            [existingBarcode.barcode] as Object[],
                            'Barcode {0} already exists on another SKU.')
                }

                existingVariant.barcodez.add(existingBarcode)
            }
        }

        // Mark any barcodes which no longer exist as deleted.
        existingVariant.barcodes?.each { existingBarcode ->
            def editedBarcode = editedVariant.barcodez?.find { editedBarcode -> editedBarcode.id == existingBarcode.id }

            if (!editedBarcode) {
                existingBarcode.delete = true
                existingBarcode.effectiveDate = effectiveDate
                existingVariant.barcodez.add(existingBarcode)
            }
        }
    }

    private void checkProductVariantForPackChanges(def existingVariant, def editedVariant, def now) {
        // Check for new/edited packs.
        editedVariant.packs?.each { editedPack ->
            def existingPack = existingVariant.packs?.find { existingPack -> existingPack.id == editedPack.id }

            if (existingPack) {
                updatePack(existingPack, editedPack, now)
            } else {
                Pack newPack = new Pack()
                updatePack(newPack, editedPack, now)
                existingVariant.addToPacks(newPack)
            }
        }

        // Remove any packs which no longer exist.
        existingVariant.packs?.each { existingPack ->
            // If the ID is not set then this must be a new pack added as part of this save, so don't remove it!
            if (existingPack.id > 0) {
                def editedPack = editedVariant.packs?.find { editedPack -> editedPack.id == existingPack.id }

                if (!editedPack) {
                    existingVariant.removeFromPacks(existingPack)
                }
            }
        }
    }

    private void updatePack(def packToBeUpdated, def editedPack, def now) {
        packToBeUpdated.supplier = editedPack.supplier
        packToBeUpdated.quantity = editedPack.quantity
        packToBeUpdated.price = editedPack.price
        packToBeUpdated.orderCode = editedPack.orderCode
        packToBeUpdated.barcode = editedPack.barcode
        packToBeUpdated.recommendedRetailPrice = editedPack.recommendedRetailPrice
        packToBeUpdated.effectiveDate = now
        packToBeUpdated.effectiveEndDate = editedPack.effectiveEndDate
        packToBeUpdated.status = editedPack.status
        packToBeUpdated.maximumOrderQuantity = editedPack.maximumOrderQuantity
        packToBeUpdated.allowSubstitutes = editedPack.allowSubstitutes
        if (packToBeUpdated.hasProperty('updateDatetime')) {
            packToBeUpdated.updateDatetime = now
        }
    }

    private void doComparison(ProductHistoryBuilder builder, Product product, ProductCommand editedProduct) {
        builder.compare("itemCode", product.itemCode, editedProduct.itemCode)
        builder.compare("description", product.description, editedProduct.description)
        builder.compare("receiptDescription", product.receiptDescription, editedProduct.receiptDescription)
        builder.compare("unitSize", product.unitSize, editedProduct.unitSize)
        builder.compare("weightedItem", product.weightedItem, editedProduct.weightedItem)
        builder.compare("pricePerKg", product.pricePerKg, editedProduct.pricePerKg)
        builder.compare("snappyProduct", product.snappyProduct, editedProduct.snappyProduct)
        builder.compare("deliItem", product.deliItem, editedProduct.deliItem)
        builder.compare("openPrice", product.openPrice, editedProduct.openPrice)
        builder.compare("zeroPrice", product.zeroPrice, editedProduct.zeroPrice)
        builder.compare("vatPercentageOverride", product.vatPercentageOverride, editedProduct.vatPercentageOverride)
        builder.compare("discreetMessage", product.discreetMessage, editedProduct.discreetMessage)
        builder.compare("status", product.status, editedProduct.status)

        builder.compare("category", product.category?.description, editedProduct.category?.description)

        // Restrictions
        builder.compare("minOpenPrice", product.restrictions.minOpenPrice, editedProduct.restrictions.minOpenPrice)
        builder.compare("maxOpenPrice", product.restrictions.maxOpenPrice, editedProduct.restrictions.maxOpenPrice)
        builder.compare("buyerIdRequired", product.restrictions.buyerIdRequired, editedProduct.restrictions.buyerIdRequired)
        builder.compare("buyerIdForced", product.restrictions.buyerIdForced, editedProduct.restrictions.buyerIdForced)
        builder.compare("buyerAgeRestriction", product.restrictions.buyerAgeRestriction, editedProduct.restrictions.buyerAgeRestriction)
        builder.compare("buyerChallengeAge", product.restrictions.buyerChallengeAge, editedProduct.restrictions.buyerChallengeAge)
        builder.compare("sellerAgeRestriction", product.restrictions.sellerAgeRestriction, editedProduct.restrictions.sellerAgeRestriction)
        builder.compare("refundAllowed", product.restrictions.refundAllowed, editedProduct.restrictions.refundAllowed)
        builder.compare("markdownAllowed", product.restrictions.markdownAllowed, editedProduct.restrictions.markdownAllowed)
        builder.compare("discountAllowed", product.restrictions.discountAllowed, editedProduct.restrictions.discountAllowed)
        builder.compare("creditPaymentAllowed", product.restrictions.creditPaymentAllowed, editedProduct.restrictions.creditPaymentAllowed)
        builder.compare("quantityChangeAllowed", product.restrictions.quantityChangeAllowed, editedProduct.restrictions.quantityChangeAllowed)
        builder.compare("quantityChangeForced", product.restrictions.quantityChangeForced, editedProduct.restrictions.quantityChangeForced)
        builder.compare("receiptPrintForced", product.restrictions.receiptPrintForced, editedProduct.restrictions.receiptPrintForced)

        builder.compare("vatCode", product.vatCode?.description, editedProduct.vatCode?.description)

        editedProduct.variants.stream().filter({ variant -> variant != null }).forEach({ variant ->
            product.variants.stream().filter({ v -> v.id == variant.id}).findAny().ifPresentOrElse({ oldVariant ->
                if (variant.delete) {
                    doVariantComparison(builder, variant.id, oldVariant, new ProductVariantCommand())
                } else {
                    doVariantComparison(builder, variant.id, oldVariant, variant)
                }
            }, {
                doVariantComparison(builder, variant.id, new ProductVariant(), variant)
            })
        })
    }

    private void doVariantComparison(ProductHistoryBuilder builder, Integer id, ProductVariant oldVariant, ProductVariantCommand variant) {
        builder.compare(id, "sku", oldVariant.sku, variant.sku)
        builder.compare(id, "retailPrice", oldVariant.retailPrice, variant.retailPrice)
        builder.compare(id, "costPrice", oldVariant.costPrice, variant.costPrice)
        builder.compare(id, "size", oldVariant.size, variant.size)
        builder.compare(id, "colour", oldVariant.colour, variant.colour)
        builder.compare(id, "minimumStockLevel", oldVariant.minimumStockLevel, variant.minimumStockLevel)
        builder.compare(id, "shelfLifeDays", oldVariant.shelfLifeDays, variant.shelfLifeDays)

        //loop over edited variant barcodes to find out if barcode been edited or newly added
        variant?.barcodez?.each { editedBarcode ->
            def existingBarcode = oldVariant?.barcodes?.find { existingBarcode -> existingBarcode.id == editedBarcode.id }
            if (existingBarcode){ //if barcode already existed
                builder.compare("barcode", existingBarcode.barcode, editedBarcode.barcode)
            } else {//if barcode is newly created
                builder.compare("barcode", null, editedBarcode.barcode)
            }
        }

        //loop over existing variant barcodes to find out if barcode been deleted
        oldVariant?.barcodes?.each { existingBarcode ->
            def editedBarcode = variant?.barcodez?.find { editedBarcode -> editedBarcode.id == existingBarcode.id }
            if (!editedBarcode){ //if edited barcode not exists means old barcode has been deleted
                builder.compare("barcode", existingBarcode.barcode, null)
            }
        }
    }

    private void savePriceUpdates(def variants, List<PriceChangeCommand> priceChanges, DateTime effectiveDate) {
        def priceBands = PriceBand.findAllByRetailerId(springSecurityService.principal.retailerId)
        def now = DateTime.now(DateTimeZone.UTC)

        def changedProductPrices = []
        def productHistories = []
        def productIds = []

        variants?.each { ProductVariant variant ->
            def prices = variant.prices

            priceChanges.findAll { it.sku == variant.sku }?.each { PriceChangeCommand priceChange ->
                ProductPrice currentPrice = prices.find { it.priceBand.id == priceChange.priceBandId }

                if (!currentPrice || currentPrice.price != priceChange.price) {
                    if (( !currentPrice || currentPrice == BigDecimal.ZERO) && priceChange.price && priceChange.price != BigDecimal.ZERO) {
                        productIds.add(variant.productId)
                    }

                    def priceBand = priceBands.find { it.id == priceChange.priceBandId }

                    if (priceBand && priceChange.sku && priceChange.price) {
                        def fromValue = currentPrice ? currentPrice.price : null
                        ProductPrice productPrice = new ProductPrice(priceBand: priceBand, sku: priceChange.sku, price: priceChange.price, effectiveDate: effectiveDate)
                        ProductHistory productHistory =  new ProductHistory(retailerId: springSecurityService.principal.retailerId, productId: variant.product.id, fromValue: fromValue, toValue: priceChange.price, productHistoryType: ProductHistoryType.PRICE, priceBandId: priceChange.priceBandId, storeId: variant.storeId, userId: springSecurityService.principal.id, usersName: springSecurityService.principal?.usersName, effectiveDate: effectiveDate, updateDate: now)
                        changedProductPrices.add(productPrice)
                        productHistories.add(productHistory)
                    }
                }
            }
        }

        syncProductUpdates(productIds)

        if (changedProductPrices.size() > 0) {
            productService.saveProductPrices(changedProductPrices, productHistories)

            def priceChangesGroupedByPriceBand = changedProductPrices.groupBy { it.priceBand }
            priceChangesGroupedByPriceBand?.each {

                // Change from our domain objects into a ProductPrice object from the Common library.
                def commonProductPrices = []
                it.value.each { ProductPrice pp ->
                    commonProductPrices.add(pp.getProductPrice())
                }

                def stores = StoreSettings.findAllByRetailerIdAndPriceBandAndStoreIdIsNotNull(springSecurityService.principal.retailerId, it.key)

                stores?.each { StoreSettings store ->
                    if (isSingleStageSel()) {
                        SyncMessage syncMessage = new SyncMessage(SyncMessageType.PRICE_CHANGE, springSecurityService.principal.retailerId, store.storeId, store.id, 0)
                        syncMessage.setInsert(true)

                        syncMessage.setProductPrices(commonProductPrices)

                        // TODO Just declaring the exchange doesn't help us, we also need to declare all of the till queues and bind them to the exchange, otherwise the message we're about to send goes nowhere.
                        rabbitService.declareExchange(String.format("R%d_S%d", syncMessage.getRetailerId(), syncMessage.getStoreNumber()))
                        rabbitService.sendExchangeMessage(String.format("R%d_S%d", syncMessage.getRetailerId(), syncMessage.getStoreNumber()), gsonProvider.gson.toJson(syncMessage))
                    }
                }
            }
        }
    }

    private void saveRangeUpdates(Product product, int[] savedRanges) {
        def rangesRemovedFrom = []
        def rangesAddedTo = []

        def rangeProducts = RangeProduct.findAllByProductId(product.id)
        rangeProducts.each { RangeProduct rangeProduct ->
            if (!savedRanges?.contains(rangeProduct.rangeId)) {
                rangesRemovedFrom.add(rangeProduct.rangeId)
            }
        }

        savedRanges?.each { Integer rangeId ->
            if (!rangeProducts.any { it.rangeId == rangeId }) {
                rangesAddedTo.add(rangeId)
            }
        }

        rangesRemovedFrom.each { Integer rangeId ->
            productService.deleteRangeProduct(rangeProducts.find { it.rangeId == rangeId })
        }

        def ranges = Range.findAllByRetailerId(springSecurityService.principal.retailerId)
        rangesAddedTo.each { Integer rangeId ->
            RangeProduct rangeProduct = new RangeProduct(range: ranges?.find { it.id == rangeId }, productId: product.id)

            productService.saveRangeProduct(rangeProduct)

            def stores = StoreSettings.findAllByRetailerIdAndRangeAndStoreIdIsNotNull(springSecurityService.principal.retailerId, ranges.find { it.id == rangeId })

            stores?.each { StoreSettings store ->
                def productEntity = product.getProduct(store.storeId)
                if (checkProductHasPriceForStore(productEntity, store.storeId)) {
                    SyncMessage syncMessage = new SyncMessage(SyncMessageType.PRODUCT, springSecurityService.principal.retailerId, store.storeId, store.id, 0)
                    syncMessage.setInsert(true)

                    syncMessage.setProducts([productEntity])

                    // TODO Just declaring the exchange doesn't help us, we also need to declare all of the till queues and bind them to the exchange, otherwise the message we're about to send goes nowhere.
                    rabbitService.declareExchange(String.format("R%d_S%d", syncMessage.getRetailerId(), syncMessage.getStoreNumber()))
                    rabbitService.sendExchangeMessage(String.format("R%d_S%d", syncMessage.getRetailerId(), syncMessage.getStoreNumber()), gsonProvider.gson.toJson(syncMessage))
                }
            }
        }
    }

    def ajaxGetChildCategories(int categoryId, int level, int selectedCategoryId) {
        def category = categoryService.getCategory(categoryId)
        render (template: "categorySelect", model: [categories: category?.childCategories, level: level, selectedCategoryId: selectedCategoryId])
    }

    def ajaxAddVariant(AddVariantCommand cmd) {
        render (template: "addVariant", model: [variant: cmd, zeroPrice: cmd.zeroPrice])
    }

    def ajaxAddBarcode(int index) {
        render (template: "addBarcode", model: [index: index])
    }

    def ajaxSaveVariant(AddVariantCommand cmd) {
        render (template: "variant", model: [index: cmd.index, variant: cmd, barcodes: cmd.barcodez])
    }

    def ajaxAddPrice(int index, long sku, boolean zeroPrice) {
        def priceBands = PriceBand.findAllByRetailerId(springSecurityService.principal.retailerId, [sort: "description", order: "asc"])

        render (template: "addPrice", model: [skuIndex: index, sku: sku, variant: null, priceBands: priceBands, zeroPrice: zeroPrice])
    }

    def ajaxSuppliers(SuppliersCommand cmd) {
        def suppliers = Supplier.findAllByRetailerId(springSecurityService.principal.retailerId)

        suppliers.removeAll { it.symbolGroup != null  }

        render (template: "suppliers", model: [suppliers: suppliers, statuses: PackStatus.values(), variant: cmd, variantIndex: cmd.index, defaultSupplier: params.defaultSupplier])
    }

    def ajaxAddPack(int variantIndex, int packIndex) {
        def suppliers = Supplier.findAllByRetailerId(springSecurityService.principal.retailerId)

        suppliers.removeAll { it.symbolGroup != null  }

        render (template: "addPack", model: [variantIndex: variantIndex, packIndex: packIndex, suppliers: suppliers, statuses: PackStatus.values(), isNewPack: true])
    }

    def ajaxSavePack(SuppliersCommand cmd) {
        render (template: "packs", model: [variantIndex: cmd.index, packs: cmd.packs, defaultSupplier: params.defaultSupplier])
    }

    //This will render category mapped restrictions for new products
    def ajaxGetRestrictions(int selectedCategoryId, boolean productOpenPrice) {
        Restrictions restrictions = null
        def category = categoryService.getCategory(selectedCategoryId)
        if (category != null){
            restrictions = category.restrictions
        }
        //when rendering restriction tab manually set isNewProduct to false since category mapped restriction should be loaded rather default values
        render (view: "/product/_restrictions", model: [restrictions: restrictions, productOpenPrice: productOpenPrice, isNewProduct: false])
    }

    /**
     * Action for saving selected columns on product search screen.
     */
    def ajaxSaveColumns() {
        try {
            if (params.reportColumns && params.reportType) {
                def userReportColumns = new JsonSlurper().parseText(params.reportColumns)
                def reportType = ReportType.valueOf(params.reportType)

                def reportColumns = productService.getColumns()

                if (!reportColumns) {
                    reportColumns = new ReportColumns(userId: springSecurityService.principal.id, reportType: reportType)
                }

                userReportColumns?.each { userReportColumn ->
                    if (reportColumns?.columns?.find { it.column == userReportColumn.key }) {
                        reportColumns?.columns?.find { it.column == userReportColumn.key }?.enabled = userReportColumn.value
                    } else {
                        reportColumns.addToColumns(new ReportColumn(column: userReportColumn.key, enabled: userReportColumn.value))
                    }
                }

                productService.saveColumns(reportColumns)

                render (status: 200)
            }
        } catch (Exception e) {
            e.printStackTrace()
            render (status: 500, text: "An error occurred saving your report column preferences.")
        }
    }

    private boolean checkProductHasPriceForStore(def product, def storeId) {
        return product.variants.findAll { it.storeId == null || it.storeId == storeId }
                .stream().map({it.getRetailPrice()})
        .collect(Collectors.toList()).findAll({ it != null && it > BigDecimal.ZERO }).size() > 0
    }

    private void syncProductUpdates(List<Integer> productIds) {
        def productIdsAsInt = productIds.findAll{it != null && it > 0 }.stream().map({it.intValue()}).collect(Collectors.toSet())
        productIdsAsInt.removeAll(Collections.singleton(null))
        if (isSingleStageSel() && productIdsAsInt && productIdsAsInt?.size() > 0) {
            def products = Product.findAllByIdInList(new ArrayList<>(productIdsAsInt))
            def stores = StoreSettings.findAllByRetailerId(springSecurityService.principal.retailerId)
            stores.forEach({ store ->
                def productEntities = []
                productEntities.addAll(products.stream().map({ it.getProduct(store.storeId) }).collect(Collectors.toList()))

                SyncMessage syncMessage = new SyncMessage(SyncMessageType.PRODUCT, springSecurityService.principal.retailerId, store.storeId, store.id, 0)
                syncMessage.setInsert(true)

                syncMessage.setProducts(productEntities)

                // TODO Just declaring the exchange doesn't help us, we also need to declare all of the till queues and bind them to the exchange, otherwise the message we're about to send goes nowhere.
                rabbitService.declareExchange(String.format("R%d_S%d", syncMessage.getRetailerId(), syncMessage.getStoreNumber()))
                rabbitService.sendExchangeMessage(String.format("R%d_S%d", syncMessage.getRetailerId(), syncMessage.getStoreNumber()), gsonProvider.gson.toJson(syncMessage))
            })
        }
    }

    private boolean isSingleStageSel() {
        if (springSecurityService.principal.retailer && springSecurityService.principal.retailer.twoStageSel) {
            return false
        }
        return true
    }

    private boolean checkChangeAffectsSel(boolean changeAffectsSel, Object left, Object right) {
        if (changeAffectsSel) {
            return true
        }
        if (left == right) {
            return false
        }
        return true
    }

    private boolean isRestrictionsChanged(RestrictionsCommand first, Restrictions second) {
        return first.minOpenPrice != second.minOpenPrice ||
                first.maxOpenPrice != second.maxOpenPrice ||
                first.buyerIdRequired != second.buyerIdRequired ||
                first.buyerIdForced != second.buyerIdForced ||
                first.buyerAgeRestriction != second.buyerAgeRestriction ||
                first.buyerChallengeAge != second.buyerChallengeAge ||
                first.sellerAgeRestriction != second.sellerAgeRestriction ||
                first.refundAllowed != second.refundAllowed ||
                first.markdownAllowed != second.markdownAllowed ||
                first.discountAllowed != second.discountAllowed ||
                first.creditPaymentAllowed != second.creditPaymentAllowed ||
                first.quantityChangeAllowed != second.quantityChangeAllowed ||
                first.quantityChangeForced != second.quantityChangeForced ||
                first.receiptPrintForced != second.receiptPrintForced
    }

    private void copyRestrictions(RestrictionsCommand from, Restrictions to) {
        to.minOpenPrice = from.minOpenPrice
        to.maxOpenPrice = from.maxOpenPrice
        to.buyerIdRequired = from.buyerIdRequired
        to.buyerIdForced = from.buyerIdForced
        to.buyerAgeRestriction = from.buyerAgeRestriction
        to.buyerChallengeAge = from.buyerChallengeAge
        to.sellerAgeRestriction = from.sellerAgeRestriction
        to.refundAllowed = from.refundAllowed
        to.markdownAllowed = from.markdownAllowed
        to.discountAllowed = from.discountAllowed
        to.creditPaymentAllowed = from.creditPaymentAllowed
        to.quantityChangeAllowed = from.quantityChangeAllowed
        to.quantityChangeForced = from.quantityChangeForced
        to.receiptPrintForced = from.receiptPrintForced
    }
}

class AddVariantCommand {
    def springSecurityService

    int index
    Integer id
    Long sku
    BigDecimal retailPrice
    BigDecimal costPrice
    Integer shelfLifeDays
    DateTime effectiveDate
    List<AddBarcodeCommand> barcodez
    List<AddPackCommand> packs
    boolean zeroPrice
    Integer defaultSupplierId

    BigDecimal getCurrentPrice() {
        if (retailPrice != null) {
            return retailPrice
        } else {
            def productPrice = ProductPrice.findBySkuAndPriceBandAndEffectiveDateLessThanEquals(sku, springSecurityService.principal.priceBand, DateTime.now(DateTimeZone.UTC), [sort: "effectiveDate", order: "desc", max: 1])

            return productPrice?.price ?: BigDecimal.ZERO
        }
    }
}

class AddBarcodeCommand {
    int index
    Integer id
    String barcode
    DateTime effectiveDate
    char recordStatus
}

class SuppliersCommand {
    int index
    List<AddPackCommand> packs
}

class AddPackCommand {
    int index
    Integer id
    SupplierCommand supplier
    int quantity
    BigDecimal price
    String orderCode
    String barcode
    BigDecimal recommendedRetailPrice
    DateTime effectiveDate
    DateTime effectiveEndDate
    PackStatus status
    Integer maximumOrderQuantity
    boolean allowSubstitutes
}

class SupplierCommand {
    int id
    String name
    Integer symbolGroupId
}

class ProductCommand {
    int id
    int retailerId
    String itemCode
    String description
    String receiptDescription
    Category category
    boolean dumpCode
    String unitSize
    boolean weightedItem
    boolean openPrice
    boolean zeroPrice
    boolean pricePerKg
    boolean snappyProduct
    boolean deliItem
    VatCode vatCode
    BigDecimal vatPercentageOverride
    RestrictionsCommand restrictions
    String discreetMessage
    ProductStatus status
    String retailerProductId
    DateTime effectiveDate

    List<SavePriceChangesCommand> priceChanges // When editing price bands as a head office user or engineer.
    int[] rangeId // When editing the ranges this product is in as a head office user or engineer.

//    Collection<Tag> tags = new ArrayList<>()
//    Collection<Message> saleMessages = new ArrayList<>()
//    Collection<Message> refundMessages = new ArrayList<>()
//    Collection<DiscountRate> discountRates = new ArrayList<>()
    Collection<ProductVariantCommand> variants = new ArrayList<>()
}

class RestrictionsCommand {
    int id
    BigDecimal minOpenPrice
    BigDecimal maxOpenPrice
    Boolean buyerIdRequired
    Boolean buyerIdForced
    Integer buyerAgeRestriction
    Integer buyerChallengeAge
    Integer sellerAgeRestriction
    Boolean refundAllowed
    Boolean markdownAllowed
    Boolean discountAllowed
    Boolean creditPaymentAllowed
    Boolean quantityChangeAllowed
    Boolean quantityChangeForced
    Boolean receiptPrintForced
}

class ProductVariantCommand {
    int id
    int storeId
    Integer defaultSupplierId
    long sku
    BigDecimal retailPrice
    BigDecimal costPrice
    String size
    String colour
    Integer shelfLifeDays
    int balanceOnHand
    int balanceOnOrder
    int minimumStockLevel
    DateTime effectiveDate
    DateTime createdDatetime
    int createdUserId
    DateTime updatedDatetime
    int updatedUserId
    boolean delete

    Collection<PackCommand> packs = new ArrayList<>()
    Collection<BarcodeCommand> barcodez = new ArrayList<>()
}

class PackCommand {
    int id
    Supplier supplier
    int quantity
    BigDecimal price
    String orderCode
    String barcode
    BigDecimal recommendedRetailPrice
    DateTime effectiveDate
    DateTime effectiveEndDate
    PackStatus status
    Integer maximumOrderQuantity
    boolean allowSubstitutes
}

class BarcodeCommand {
    int id
    long sku
    int retailerId
    String barcode
    DateTime effectiveDate
    char recordStatus
}

class SavePriceChangesCommand {
    List<PriceChangeCommand> priceChanges
}

class PriceChangeCommand {
    long sku
    int packId // Used on the supplier price updates screen only.
    int priceBandId
    BigDecimal price
    BigDecimal oldPrice
    Integer productId
}

class SaveRangeProductsCommand {
    List<RangeProductCommand> rangeProducts
}

class RangeProductCommand {
    int productId
    int rangeId
    boolean ranged
}