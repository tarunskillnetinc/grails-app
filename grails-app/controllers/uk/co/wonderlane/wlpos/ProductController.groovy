package uk.co.wonderlane.wlpos

import grails.plugin.springsecurity.annotation.Secured
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import uk.co.wonderlane.wlpos.entities.SyncMessage
import uk.co.wonderlane.wlpos.enums.PackStatus
import uk.co.wonderlane.wlpos.enums.ProductStatus
import uk.co.wonderlane.wlpos.enums.SyncMessageType
import uk.co.wonderlane.wlpos.supplier.Supplier

class ProductController {

    def springSecurityService

    def productService
    def categoryService
    def restrictionsService
    def tagService
    def rabbitService
    def gsonProvider

    def index() {
        render(view: "index", model: [products: null, storeId: springSecurityService.principal.storeId, page: 1, pageCount: 0, pageNumbers: null])
    }

    def show(int id) {
        def product = productService.getProduct(id)

        if (!product) {
            flash.message = "Product not found"
            redirect(action: "index")
            return
        }

        def ranges = []
        def priceBands = []

        def productCategoryList = []

        def category = product.category
        while (category) {
            productCategoryList.add(category.id)

            category = category.parentCategory
        }

        def userRoles = springSecurityService.principal.authorities*.authority
        if (userRoles.contains("ROLE_HEAD_OFFICE") || userRoles.contains("ROLE_ENGINEER")) {
            priceBands = PriceBand.findAllByRetailerId(springSecurityService.principal.retailerId, [sort: "description", order: "asc"])
            ranges = Range.findAllByRetailerId(springSecurityService.principal.retailerId, [sort: "description", order: "asc"])
        }

        render(view: "add", model: [product: product,
                                    storeId: springSecurityService.principal.storeId,
                                    statusValues: ProductStatus.values(),
                                    categoryValues: categoryService.getFullCategoryHierarchy(),
                                    productCategoryList: productCategoryList,
                                    vatValues: VatCode.findAllByRetailerId(springSecurityService.principal.retailerId),
                                    ranges: ranges,
                                    priceBands: priceBands,
                                    navlink: "details"])
    }

    def add() {
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
                                    isNewProduct: true])
    }

    def search() {
        session.PRODUCT_SEARCH_TERM = params.searchTerm

        def products = productService.searchProducts(params.searchTerm, params.searchBy, 50, 0, "id", "asc")

        render(template: "/product/productSearchResults", model: [ products: products, totalResults: products.totalCount, storeId: springSecurityService.principal.storeId ])
    }

    def maintenanceSearch() {
        session.PRODUCT_SEARCH_TERM = params.searchTerm

        def products = productService.searchProducts(params.searchTerm, params.searchBy, params.max ? Integer.parseInt(params.max) : 50, params.offset ? Integer.parseInt(params.offset) : 0, "id", "asc")

        render(template: "/product/maintenanceSearchResults", model: [products: products, storeId: springSecurityService.principal.storeId, searchTerm: params.searchTerm, searchBy: params.searchBy, max: params.max ?: 50, offset: params.offset, totalResults: products.totalCount])
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

        render(template: "/product/pricesSearchResults", model: [productPrices: productPrices, priceBands: priceBands])
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
    def ajaxSavePriceChanges(SavePriceChangesCommand cmd) {
        def now = DateTime.now(DateTimeZone.UTC)
        def priceBands = PriceBand.findAllByRetailerId(springSecurityService.principal.retailerId)

        def productPrices = []
        def priceUpdates = [:]

        cmd.priceChanges?.each {priceChange ->
            if (!priceUpdates.containsKey(priceChange.priceBandId)) {
                priceUpdates[priceChange.priceBandId] = []
            }

            ProductPrice productPrice = new ProductPrice(priceBand: priceBands.find { it.id == priceChange.priceBandId }, sku: priceChange.sku, price: priceChange.price, effectiveDate: now)
            productPrices.add(productPrice)

            priceUpdates[priceChange.priceBandId].add(productPrice.getProductPrice())
        }

        productService.saveProductPrices(productPrices)

        priceUpdates.each { priceBandId, priceChanges ->
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
                    storeProducts.add(it.getProduct(store.storeId))
                }

                syncMessage.setProducts(storeProducts)

                // TODO Just declaring the exchange doesn't help us, we also need to declare all of the till queues and bind them to the exchange, otherwise the message we're about to send goes nowhere.
                rabbitService.declareExchange(String.format("R%d_S%d", syncMessage.getRetailerId(), syncMessage.getStoreNumber()))
                rabbitService.sendExchangeMessage(String.format("R%d_S%d", syncMessage.getRetailerId(), syncMessage.getStoreNumber()), gsonProvider.gson.toJson(syncMessage))
            }
        }

        render "OK"
    }

    def save(ProductCommand editedProduct) {
        def product

        boolean newProduct

        if (params.id && Integer.parseInt(params.id) > 0) {
            newProduct = false
        } else {
            newProduct = true
        }

        DateTime now = DateTime.now(DateTimeZone.UTC)

        if (newProduct) {
            product = new Product(params)
            product.retailerId = springSecurityService.principal.retailerId

            product.variants?.each { variant ->
                variant.storeId = springSecurityService.principal.storeId
                variant.effectiveDate = now

                variant.barcodez?.each { barcode ->
                    barcode.sku = variant.sku
                    barcode.effectiveDate = barcode.effectiveDate ?: now
                }

                variant.packs?.each { pack ->
                    pack.effectiveDate = pack.effectiveDate ?: now
                }
            }
        } else {
            // TODO This all needs finishing.
            // TODO We need to introduce an effective date entry.

            product = productService.getProduct(Integer.parseInt(params.id))

            // TODO ProductCommand and all of the sub objects need to be command objects as well.
//            def editedProduct = new ProductCommand()
//            bindData(editedProduct, params)

            editedProduct.variants?.each {editedVariant ->
                product.variants?.find {existingVariant -> existingVariant.id == editedVariant.id }?.retailPrice = editedVariant.retailPrice
            }

            product.itemCode = editedProduct.itemCode
            product.description = editedProduct.description
            product.receiptDescription = editedProduct.receiptDescription
            product.category = editedProduct.category
            product.unitSize = editedProduct.unitSize
            product.weightedItem = editedProduct.weightedItem
            product.openPrice = editedProduct.openPrice
            product.zeroPrice = editedProduct.zeroPrice
            product.vatCode = editedProduct.vatCode
            product.vatPercentageOverride = editedProduct.vatPercentageOverride
            product.discreetMessage = editedProduct.discreetMessage
            product.status = editedProduct.status
            product.retailerProductId = editedProduct.retailerProductId

            copyRestrictions(editedProduct.restrictions, product.restrictions)

            // TODO Handle saving over the rest of the properties in a product, also handle adding new variants and such.
        }

        if (product.validate()) {
            restrictionsService.saveRestrictions(product.restrictions) // Restrictions are validated as part of product.validate()
            productService.saveProduct(product)

            if (newProduct) {
                productService.saveBarcodes(product)
            }

            def userRoles = springSecurityService.principal.authorities*.authority
            if ((userRoles.contains("ROLE_HEAD_OFFICE") || userRoles.contains("ROLE_ENGINEER")) && !springSecurityService.principal.storeId) {
                def priceChanges = []
                editedProduct?.priceChanges?.each {
                    priceChanges.addAll(it.priceChanges)
                }

                savePriceUpdates(product.variants?.findAll { it.storeId == null }, priceChanges)
                saveRangeUpdates(product, editedProduct.rangeId)
            }

            flash.message = "Product saved successfully"
        }

        if (!product.hasErrors()) {
            // TODO Send this update to all tills which are ranged.
            if (springSecurityService.principal.storeId) {
                if (!rabbitService.isOpen()) {
                    throw new Exception("Rabbit MQ not available")
                }

                SyncMessage syncMessage = new SyncMessage(SyncMessageType.PRODUCT, springSecurityService.principal.retailerId, springSecurityService.principal.storeNumber ?: 0, springSecurityService.principal.storeId ?: 0, 0)
                syncMessage.setInsert(true)

                List<uk.co.wonderlane.wlpos.entities.Product> products = new ArrayList<uk.co.wonderlane.wlpos.entities.Product>()
                products.add(product.getProduct(springSecurityService.principal.storeId))
                syncMessage.setProducts(products)

                rabbitService.sendExchangeMessage(String.format("R%d_S%d", syncMessage.getRetailerId(), syncMessage.getStoreNumber()), gsonProvider.gson.toJson(syncMessage))
            } else {
                def rangeProducts = RangeProduct.findAllByProductId(product.id)
            }
        }

        if (!product.hasErrors()) {
            redirect(action: "index")
        } else {
            render(view: "add", model: [product       : product,
                                        storeId       : springSecurityService.principal.storeId,
                                        statusValues  : ProductStatus.values(),
                                        categoryValues: categoryService.getFullCategoryHierarchy(),
                                        vatValues     : VatCode.findAllByRetailerId(springSecurityService.principal.retailerId)])
        }
    }

    private void savePriceUpdates(def variants, List<PriceChangeCommand> priceChanges) {
        def priceBands = PriceBand.findAllByRetailerId(springSecurityService.principal.retailerId)
        def now = DateTime.now(DateTimeZone.UTC)

        def changedProductPrices = []

        variants?.each { ProductVariant variant ->
            def prices = variant.prices

            priceChanges.findAll { it.sku == variant.sku }?.each { PriceChangeCommand priceChange ->
                ProductPrice currentPrice = prices.find { it.priceBand.id == priceChange.priceBandId }

                if (!currentPrice || currentPrice.price != priceChange.price) {
                    def priceBand = priceBands.find { it.id == priceChange.priceBandId }

                    if (priceBand && priceChange.sku && priceChange.price) {
                        ProductPrice productPrice = new ProductPrice(priceBand: priceBand, sku: priceChange.sku, price: priceChange.price, effectiveDate: now)

                        changedProductPrices.add(productPrice)
                    }
                }
            }
        }

        if (changedProductPrices.size() > 0) {
            productService.saveProductPrices(changedProductPrices)

            def priceChangesGroupedByPriceBand = changedProductPrices.groupBy { it.priceBand }
            priceChangesGroupedByPriceBand?.each {
                def stores = StoreSettings.findAllByRetailerIdAndPriceBandAndStoreIdIsNotNull(springSecurityService.principal.retailerId, it.key)

                // Change from our domain objects into a ProductPrice object from the Common library.
                def commonProductPrices = []
                it.value.each { ProductPrice pp ->
                    commonProductPrices.add(pp.getProductPrice())
                }

                stores?.each { StoreSettings store ->
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
                SyncMessage syncMessage = new SyncMessage(SyncMessageType.PRODUCT, springSecurityService.principal.retailerId, store.storeId, store.id, 0)
                syncMessage.setInsert(true)

                syncMessage.setProducts([product.getProduct(store.storeId)])

                // TODO Just declaring the exchange doesn't help us, we also need to declare all of the till queues and bind them to the exchange, otherwise the message we're about to send goes nowhere.
                rabbitService.declareExchange(String.format("R%d_S%d", syncMessage.getRetailerId(), syncMessage.getStoreNumber()))
                rabbitService.sendExchangeMessage(String.format("R%d_S%d", syncMessage.getRetailerId(), syncMessage.getStoreNumber()), gsonProvider.gson.toJson(syncMessage))
            }
        }
    }

    def ajaxGetChildCategories(int categoryId, int level, int selectedCategoryId) {
        def category = categoryService.getCategory(categoryId)

        render (template: "categorySelect", model: [categories: category?.childCategories, level: level, selectedCategoryId: selectedCategoryId])
    }

    def ajaxAddVariant(AddVariantCommand cmd) {
        render (template: "addVariant", model: [variant: cmd])
    }

    def ajaxAddBarcode(int index) {
        render (template: "addBarcode", model: [index: index])
    }

    def ajaxSaveVariant(AddVariantCommand cmd) {
        render (template: "variant", model: [index: cmd.index, variant: cmd])
    }

    def ajaxAddPrice(int index, long sku) {
        def priceBands = PriceBand.findAllByRetailerId(springSecurityService.principal.retailerId, [sort: "description", order: "asc"])

        render (template: "addPrice", model: [skuIndex: index, sku: sku, variant: null, priceBands: priceBands])
    }

    def ajaxSuppliers(SuppliersCommand cmd) {
        def suppliers = Supplier.findAllByRetailerId(springSecurityService.principal.retailerId)

        render (template: "suppliers", model: [suppliers: suppliers, statuses: PackStatus.values(), variant: cmd, variantIndex: cmd.index])
    }

    def ajaxAddPack(int variantIndex, int packIndex) {
        def suppliers = Supplier.findAllByRetailerId(springSecurityService.principal.retailerId)

        render (template: "addPack", model: [variantIndex: variantIndex, packIndex: packIndex, suppliers: suppliers, statuses: PackStatus.values(), isNewPack: true])
    }

    def ajaxSavePack(SuppliersCommand cmd) {
        render (template: "packs", model: [variantIndex: cmd.index, packs: cmd.packs])
    }

    private void copyRestrictions(Restrictions from, Restrictions to) {
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
    List<AddBarcodeCommand> barcodes
    List<AddPackCommand> packs

    BigDecimal getCurrentPrice() {
        if (retailPrice != null) {
            return retailPrice
        } else {
            def storeSettings

            if (springSecurityService.principal.storeId) {
                storeSettings = StoreSettings.findByRetailerIdAndId(springSecurityService.principal.retailerId, springSecurityService.principal.storeId)
            } else {
                storeSettings = StoreSettings.findByRetailerIdAndStoreIdIsNull(springSecurityService.principal.retailerId)
            }

            def productPrice = ProductPrice.findBySkuAndPriceBandAndEffectiveDateLessThanEquals(sku, storeSettings.priceBand, DateTime.now(DateTimeZone.UTC), [sort: "effectiveDate", order: "desc", max: 1])

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
    VatCode vatCode
    BigDecimal vatPercentageOverride
    Restrictions restrictions
    String discreetMessage
    ProductStatus status
    String retailerProductId

    List<SavePriceChangesCommand> priceChanges // When editing price bands as a head office user or engineer.
    int[] rangeId // When editing the ranges this product is in as a head office user or engineer.

//    Collection<Tag> tags = new ArrayList<>()
//    Collection<Message> saleMessages = new ArrayList<>()
//    Collection<Message> refundMessages = new ArrayList<>()
//    Collection<DiscountRate> discountRates = new ArrayList<>()
    Collection<ProductVariantCommand> variants = new ArrayList<>()
}

class ProductVariantCommand {
    int id
    int storeId
    String sku
    BigDecimal retailPrice
    BigDecimal costPrice
    String size
    String colour
    int balanceOnHand
    int balanceOnOrder
    int minimumStockLevel
    DateTime effectiveDate
    DateTime createdDatetime
    int createdUserId
    DateTime updatedDatetime
    int updatedUserId
    boolean delete

//    Collection<Barcode> barcodes = new ArrayList<>()
//    Collection<Pack> packs = new ArrayList<>()
}

class SavePriceChangesCommand {
    List<PriceChangeCommand> priceChanges
}

class PriceChangeCommand {
    long sku
    int priceBandId
    BigDecimal price
}

class SaveRangeProductsCommand {
    List<RangeProductCommand> rangeProducts
}

class RangeProductCommand {
    int productId
    int rangeId
    boolean ranged
}