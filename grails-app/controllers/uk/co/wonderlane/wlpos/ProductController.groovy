package uk.co.wonderlane.wlpos

import com.opencsv.bean.CsvBindByName
import com.opencsv.bean.CsvToBeanBuilder
import grails.converters.JSON
import grails.plugin.springsecurity.annotation.Secured
import grails.validation.Validateable
import org.apache.commons.lang3.StringUtils
import org.codehaus.groovy.runtime.InvokerHelper
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import org.joda.time.format.DateTimeFormat
import org.joda.time.format.DateTimeFormatter
import org.springframework.http.HttpStatus
import org.springframework.validation.BeanPropertyBindingResult
import org.springframework.validation.Errors
import org.springframework.validation.ObjectError
import uk.co.wonderlane.wlpos.enums.LocationsType
import uk.co.wonderlane.wlpos.enums.PackStatus
import uk.co.wonderlane.wlpos.enums.ProductHistoryType
import uk.co.wonderlane.wlpos.enums.ProductStatus
import uk.co.wonderlane.wlpos.supplier.Pack
import uk.co.wonderlane.wlpos.supplier.Supplier

class ProductController extends BaseController {

    def springSecurityService
    def restrictionsService
    def supplierService
    def storeService
    def tagService
    def productHistoryService

    /**
     * Landing page of the controller action - displays the product search screen.
     */
    def index() {
        [userColumns: productService.getColumns()]
    }

    def show(int id) {
        setEffectiveDate()

        DateTime now = DateTime.now(DateTimeZone.UTC)

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

        def locationsEnabled = [LocationsType.SIMPLE, LocationsType.ADVANCED].contains(springSecurityService.principal.retailer.config.locationsType)

        render(view: "add", model: [product            : product,
                                    storeId            : springSecurityService.principal.storeId,
                                    statusValues       : ProductStatus.values(),
                                    categoryValues     : categoryService.getTopLevelCategories(),
                                    productCategoryList: productCategoryList,
                                    vatValues          : VatCode.findAllByRetailerId(springSecurityService.principal.retailerId),
                                    ranges             : ranges,
                                    priceBands         : priceBands,
                                    effectiveDateIndex : session.effectiveDate,
                                    now                : now,
                                    navlink            : "details",
                                    snappyEnabled      : springSecurityService.principal.retailer.config.snappyShopperEnabled,
                                    locationsEnabled   : locationsEnabled,
                                    locationsType      : springSecurityService.principal.retailer.config.locationsType.name()])
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

        render(view: "add", model: [storeId       : springSecurityService.principal.storeId,
                                    statusValues  : ProductStatus.values(),
                                    categoryValues: categoryService.getTopLevelCategories(),
                                    vatValues     : VatCode.findAllByRetailerId(springSecurityService.principal.retailerId),
                                    ranges        : ranges,
                                    priceBands    : priceBands,
                                    now           : DateTime.now(DateTimeZone.UTC).withTimeAtStartOfDay(),
                                    isNewProduct  : true,
                                    locationsType : springSecurityService.principal.retailer.config.locationsType.name()])
    }

    def search() {
        session.PRODUCT_SEARCH_TERM = params.searchTerm
        session.effectiveDate = ["Current", DateTime.now(DateTimeZone.UTC)]

        def products = productService.searchProductsHql(params.searchTerm, params.searchBy, 50, 0, "id", "asc")

        render(template: "addProductSearchResults", model: [products: products.products, totalResults: products.totalCount, storeId: springSecurityService.principal.storeId])
    }

    /**
     * Called from the main product maintenance search screen.
     */
    def ajaxSearchProducts() {
        session.PRODUCT_SEARCH_TERM = params.searchTerm
        session.effectiveDate = ["Current", DateTime.now(DateTimeZone.UTC)]

        def products = productService.searchProductsHql(params.searchTerm, params.searchBy, params.max ? Integer.parseInt(params.max) : 50, params.offset ? Integer.parseInt(params.offset) : 0, "id", "asc")

        render(template: "productSearchResults", model: [products    : products.products,
                                                         storeId     : springSecurityService.principal.storeId,
                                                         userColumns : productService.getColumns(),
                                                         searchTerm  : params.searchTerm,
                                                         searchBy    : params.searchBy,
                                                         max         : params.max ?: 50,
                                                         offset      : params.offset,
                                                         totalResults: products.totalCount])
    }

    @Secured(['ROLE_ENGINEER', 'ROLE_HEAD_OFFICE'])
    def prices() {
        def categories = categoryService.getTopLevelCategories()
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
        def categories = categoryService.getTopLevelCategories()
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
        def categories = categoryService.getTopLevelCategories()
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

        productService.syncProductUpdatesToAllStoresForRetailer(productIds)
        productService.sendProductPriceUpdate(productPrices, storeService.getStoresByPriceBand(springSecurityService.principal.retailerId, priceBand))
    }

    @Secured(['ROLE_ENGINEER', 'ROLE_HEAD_OFFICE'])
    def ajaxSavePriceChanges(SavePriceChangesCommand cmd) {
        def now = DateTime.now(DateTimeZone.UTC).withTimeAtStartOfDay()//Get current date as start of a day

        def priceBandMap = [:] //Declare price band map to keep price band id against price band

        //Loop over saved price list and group them by price band id [1:[] , 2:[], 3:[]]
        def savePriceCommandMap = cmd?.priceChanges?.groupBy { it?.priceBandId }

        //Loop over map and process every item belonging to price band id
        savePriceCommandMap?.each { k, v ->
            PriceBand priceBand

            if (!priceBandMap.containsKey(k)) { //If price band map do not have price band then load
                priceBand = PriceBand.findByIdAndRetailerId(k, springSecurityService.principal.retailerId)
            } else { //If price band map do have price band then get it by map
                priceBand = priceBandMap.get(k)
            }

            //Call supplier price and product history update procedure to persist changes
            supplierService.saveSupplierPriceUpdates(v, priceBand, now)

            //Sync by writing message to RabitMQ
            syncSupplierPriceUpdates(v, priceBand, now)
        }

        //After process make sure to empty map in case to avoid map growing
        priceBandMap = [:]

        render "OK"
    }

    @Secured(['ROLE_ENGINEER', 'ROLE_HEAD_OFFICE'])
    def ajaxSaveRangeProducts(SaveRangeProductsCommand cmd) {
        def ranges = Range.findAllByRetailerId(springSecurityService.principal.retailerId)

        def newlyRangedProducts = []
        def noLongerRangedProducts = []
        def productHistories = []
        def rangedProductsMap = [:]

        cmd.rangeProducts?.each { rangeProduct ->
            def existingRangeProduct = RangeProduct.findByProductIdAndRange(rangeProduct.productId, ranges.find { it.id == rangeProduct.rangeId })

            if (rangeProduct.isRanged() && !existingRangeProduct) {
                existingRangeProduct = new RangeProduct(range: ranges.find { it.id == rangeProduct.rangeId }, productId: rangeProduct.productId)

                newlyRangedProducts.add(existingRangeProduct)

                if (!rangedProductsMap.containsKey(rangeProduct.rangeId)) {
                    rangedProductsMap[rangeProduct.rangeId] = []
                }

                rangedProductsMap[rangeProduct.rangeId].add(rangeProduct)
                productHistories.add(handleProductRangeHistory(existingRangeProduct, true))
            } else if (!rangeProduct.isRanged() && existingRangeProduct) {
                noLongerRangedProducts.add(existingRangeProduct)
                productHistories.add(handleProductRangeHistory(existingRangeProduct, false))
            }
        }

        productService.saveRangeProducts(newlyRangedProducts)
        productService.deleteRangeProducts(noLongerRangedProducts)
        if (productHistories != null && productHistories.size() > 0){
            productService.saveProductHistories(productHistories)
        }

        // Send down those products for addition to the relevant stores for each range. Do not delete any products as stores may need to sell through stock etc.
        rangedProductsMap.each { rangeId, rangeProductChanges ->
            def allProducts = []

            rangeProductChanges.each { RangeProductCommand rangeProductCommand ->
                allProducts.add(productService.getProduct(rangeProductCommand.productId))
            }

            productService.sendProductUpdate(allProducts, storeService.getStoresByRange(springSecurityService.principal.retailerId, ranges.find { it.id == rangeId }))
        }

        render "OK"
    }

    private Product saveProduct(ProductCommand editedProduct, def paramsMap, boolean isRequest) {
        editedProduct.variants?.removeIf({ it == null })
        def product
        def builder

        def effectiveDate = getEffectiveDate()

        boolean newProduct
        boolean changeAffectsSel = false

        if (isRequest ? paramsMap.id && Integer.parseInt(paramsMap.id) > 0 : editedProduct.id && editedProduct.id > 0) {
            newProduct = false
        } else {
            newProduct = true
        }

        DateTime now = DateTime.now(DateTimeZone.UTC)
        List<ProductVariant> productVariantsList = new ArrayList<>()

        if (newProduct) {
            changeAffectsSel = true
            if (isRequest) {
                product = new Product(paramsMap)
            } else {
                product = new Product()
                copyProduct(editedProduct, product)
                copyProductVariants(editedProduct, product)
            }

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

                    if (!isValidBarcode(barcode)) {
                        product.errors.reject('product.barcodes.notUnique', [barcode.barcode] as Object[], 'Barcode {0} already exists on another SKU.')
                    }
                }

                variant.packs?.each { pack ->
                    pack.effectiveDate = pack.effectiveDate ?: now
                    pack.updateDatetime = now
                }

                variant.locationz?.each { location ->
                    location.storeId = springSecurityService.principal.storeId
                    location.sku = variant.sku
                }
            }
        } else {
            product = productService.getProduct(Integer.parseInt(isRequest ? paramsMap.id : editedProduct.id as String))

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
                if (product.category != null) {
                    if (editedProduct.restrictions.validate() && editedProduct.restrictions.id == product.category.restrictions.id) {
                        // Changed restrictions and the product was currently pointing at the category restrictions object. Create a new restrictions.
                        product.restrictions = new Restrictions()
                    }

                    copyRestrictions(editedProduct.restrictions, product.restrictions)
                } else {
                    product.errors.reject('product.category.nullable.error', 'No Category Selected')
                }
            }

            // Variants.
            productVariantsList = getUpdatedProductVariantsOnSave(editedProduct, product, builder, changeAffectsSel, effectiveDate)
        }

        if (!product.hasErrors() && product.validate()) {
            // Restrictions are validated as part of product.validate()
            restrictionsService.saveRestrictions(product.restrictions)

            // Check for errors after each save, otherwise the BO will report a 500 - EntityInsertAction was vetoed error.
            productService.saveProduct(product, productVariantsList)
            if (product.hasErrors()) {
                return product
            }

            productService.saveBarcodes(product)
            if (product.hasErrors()) {
                return product
            }

            productService.saveLocations(product)
            if (product.hasErrors()) {
                return product
            }


            if (builder && builder.productHistories) {
                productService.saveProductHistories(builder.productHistories)
            }

            def userRoles = springSecurityService.principal.authorities*.authority
            if ((userRoles.contains("ROLE_HEAD_OFFICE") || userRoles.contains("ROLE_ENGINEER")) && !springSecurityService.principal.storeId) {
                def priceChanges = []
                editedProduct?.priceChanges?.findAll { it != null }?.each {
                    priceChanges.addAll(it.priceChanges)
                }

                savePriceUpdates(product.variants?.findAll { it.storeId == null }, priceChanges, effectiveDate)
                saveRangeUpdates(product, editedProduct.rangeId)
            }

            if (isRequest) {
                flash.message = "Product saved successfully"
            }
        }

        if (!product.hasErrors()) {
            if (productService.isSingleStageSel() || !changeAffectsSel) {
                if (springSecurityService.principal.storeId) {
                    productService.sendProductUpdate([product], [Store.findById(springSecurityService.principal.storeId)])
                } else {
                    def rangeProducts = RangeProduct.findAllByProductId(product.id)

                    rangeProducts?.each { rangeProduct ->
                        productService.sendProductUpdate([product], storeService.getStoresByRange(springSecurityService.principal.retailerId, rangeProduct.range))
                    }
                }
            }

        }

        return product
    }

    def getColumns() {
        return productService.getColumns()
    }

    def save(ProductCommand editedProduct) {
        // Domain calls moved prior to Save Product in case of EntityInsertAction was vetoed error that prevents further Domain Calls.
        def topLevelCategories = categoryService.getTopLevelCategories()
        def vatValues = VatCode.findAllByRetailerId(springSecurityService.principal.retailerId)

        Product product = saveProduct(editedProduct, params, true)

        if (!product.hasErrors()) {
            redirect(action: "index")
        } else {
            def productCategoryList = []

            def category = product.category
            while (category) {
                productCategoryList.add(category.id)

                category = category.parentCategory
            }

            def ranges = []
            def priceBands = []
            def editedPrices = []

            def userRoles = springSecurityService.principal.authorities*.authority
            if (userRoles.contains("ROLE_HEAD_OFFICE") || userRoles.contains("ROLE_ENGINEER")) {
                priceBands = PriceBand.findAllByRetailerId(springSecurityService.principal.retailerId, [sort: "description", order: "asc"])
                ranges = Range.findAllByRetailerId(springSecurityService.principal.retailerId, [sort: "description", order: "asc"])

                editedProduct?.priceChanges?.findAll { it != null }.each {
                    editedPrices.addAll(it.priceChanges)
                }
            }

            product.discard()

            render(view: "add", model: [product            : product,
                                        storeId            : springSecurityService.principal.storeId,
                                        statusValues       : ProductStatus.values(),
                                        categoryValues     : topLevelCategories,
                                        productCategoryList: productCategoryList,
                                        effectiveDateIndex : session.effectiveDate,
                                        ranges             : ranges,
                                        selectedRanges     : editedProduct.rangeId,
                                        priceBands         : priceBands,
                                        editedPrices       : editedPrices,
                                        vatValues          : vatValues,
                                        locationsType      : springSecurityService.principal.retailer.config.locationsType.name()])
        }
    }

    private List<ProductVariant> getUpdatedProductVariantsOnSave(ProductCommand editedProduct, product, builder, boolean changeAffectsSel, effectiveDate) {
        DateTime now = DateTime.now(DateTimeZone.UTC)
        List<ProductVariant> productVariantList = new ArrayList<>()

        editedProduct.variants?.each { editedVariant ->
            def existingVariant = product.variants?.find { existingVariant -> existingVariant.id == editedVariant.id }

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
                    newVariant.shelfCapacity = editedVariant.shelfCapacity
                    newVariant.minimumDisplayQuantity = editedVariant.minimumDisplayQuantity
                    newVariant.defaultSupplierId = editedVariant.defaultSupplierId
                    if (newVariant.getShelfCapacity() != null
                            && !(newVariant.getShelfCapacity() >= 1 && newVariant.getShelfCapacity() <= 999)) {
                        product.errors.reject('productVariant.shelfCapacity.size.error', 'Shelf Capacity must be between 1 to 999.')
                    }

                    if (newVariant.getMinimumDisplayQuantity() != null
                            && !(newVariant.getMinimumDisplayQuantity() >= 1 && newVariant.getMinimumDisplayQuantity() <= 999)) {
                        product.errors.reject('productVariant.minimumDisplayQuantity.size.error', 'Minimum Display Quantity must be between 1 to 999.')
                    }

                    checkProductVariantForPackChanges(product, newVariant, editedVariant, now, true)
                    checkProductVariantForLocationChanges(product, newVariant, editedVariant)
                    checkProductVariantForBarcodeChanges(product, newVariant, editedVariant, effectiveDate)
                    productVariantList.add(newVariant)
                } else {
                    checkProductVariantForPackChanges(product, existingVariant, editedVariant, now, false)
                    checkProductVariantForLocationChanges(product, existingVariant, editedVariant)
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
                newVariant.shelfCapacity = editedVariant.shelfCapacity
                newVariant.minimumDisplayQuantity = editedVariant.minimumDisplayQuantity
                newVariant.defaultSupplierId = editedVariant.defaultSupplierId

                editedVariant.packs?.each { editedPack ->
                    Pack newPack = new Pack()
                    updatePack(newPack, editedPack, now)
                    newVariant.addToPacks(newPack)
                }

                editedVariant.locationz?.each { editedLocation ->
                    Location newLocation = new Location()
                    updateLocation(newLocation, editedLocation, editedVariant)
                    newVariant.locationz.add(newLocation)
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

                        if (!isValidBarcode(newBarcode)) {
                            product.errors.reject('product.barcodes.notUnique', [newBarcode.barcode] as Object[], 'Barcode {0} already exists on another SKU.')
                        }
                })

                // Check whether the SKU is used elsewhere
                if (!isValidSku(newVariant.sku)) {
                    product.errors.reject('product.productVariants.notUnique', [newVariant.sku] as Object[], 'SKU already exists on another product.')
                }


                productVariantList.add(newVariant);
            }
        }

        return productVariantList;
    }

    private DateTime getEffectiveDate(def effectiveDate) {
        if (effectiveDate) {
            DateTimeFormatter dateFormatter = DateTimeFormat.forPattern("dd/MM/yyyy").withZone(DateTimeZone.UTC)
            DateTime selectedDate = DateTime.parse(effectiveDate, dateFormatter)
            return selectedDate.withTimeAtStartOfDay()
        } else {
            return DateTime.now(DateTimeZone.UTC).withTimeAtStartOfDay()
        }
    }

    private DateTime getEffectiveDate() {
        return getEffectiveDate(params.effectiveDate)
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

                if (!isValidBarcode(barcode)) {
                    product.errors.reject(
                            'product.barcodes.notUnique',
                            [barcode.barcode] as Object[],
                            'Barcode {0} already exists on another SKU.')
                }
            } else { // If barcode do exists change update existing values

                //Only update if user has changed barcode value or else skip
                if (existingBarcode.barcode != null && existingBarcode.barcode != editedBarcode.barcode) {

                    //Mark current barcode to delete this will insert new mark delete entry to DB
                    existingBarcode.delete = true
                    existingBarcode.effectiveDeleteDate = effectiveDate
                    //New effective date needed to be set as effective date of mark delete entry

                    //Add new barcode to replacing existing
                    Barcode futureBarcode = new Barcode()
                    futureBarcode.sku = existingVariant.sku
                    futureBarcode.retailerId = springSecurityService.principal.retailerId
                    futureBarcode.barcode = editedBarcode.barcode
                    futureBarcode.effectiveDate = effectiveDate
                    futureBarcode.recordStatus = 'C'

                    if (!isValidBarcode(futureBarcode)) {
                        product.errors.reject(
                                'product.barcodes.notUnique',
                                [futureBarcode.barcode] as Object[],
                                'Barcode {0} already exists on another SKU.')
                    } else {
                        //Add mark deleted barcode and newly updated barcode to add into DB
                        existingVariant.barcodez.add(existingBarcode)
                        existingVariant.barcodez.add(futureBarcode)
                    }

                }
            }
        }

        // Mark any barcodes which no longer exist as deleted.
        existingVariant.barcodes?.each { existingBarcode ->
            def editedBarcode = editedVariant.barcodez?.find { editedBarcode -> editedBarcode.id == existingBarcode.id }

            if (!editedBarcode) {
                existingBarcode.delete = true
                existingBarcode.effectiveDeleteDate = effectiveDate
                //New effective date needed to be set as effective date of mark delete entry
                existingVariant.barcodez.add(existingBarcode)
            }
        }
    }

    private void checkProductVariantForPackChanges(def product, def existingVariant, def editedVariant, def now, boolean newVariant) {
        if (product.hasErrors()) {
            return
        }

        if (newVariant){
            editedVariant.packs?.each { editedPac ->
                Pack newPack = new Pack()
                updatePack(newPack, editedPac, now)
                existingVariant.addToPacks(newPack)
            }
            return
        }

        editedVariant.packs?.each { editedPack ->
            def existingPack = existingVariant.packs?.find { existingPack -> existingPack.id == editedPack.id }

            if (existingPack && packChanged(editedPack, existingPack)) {
                updatePack(existingPack, editedPack, now)
            } else if (!existingPack) {
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

    private void checkProductVariantForLocationChanges(def product, def existingVariant, def editedVariant) {
        if (product.hasErrors()) {
            return
        }

        def variantLocations = Location.findAllByStoreIdAndSku(springSecurityService.principal.storeId, editedVariant.sku)
        def builder = new ProductHistoryBuilder(product.id, springSecurityService, effectiveDate)

        editedVariant.locationz?.each { editedLocation ->
            def existingLocation = variantLocations?.find { existingLocation -> existingLocation.id == editedLocation.id }

            if (existingLocation && existingLocation.id > 0 && locationChanged(editedLocation, existingLocation)) {
                compareLocationFields(builder, existingLocation, editedLocation, ProductHistoryType.LOCATION_EDIT)
                updateLocation(existingLocation, editedLocation, editedVariant)
            } else if (!existingLocation) {
                Location newLocation = new Location()
                updateLocation(newLocation, editedLocation, editedVariant)
                existingVariant.locationz.add(newLocation)
                compareLocationFields(builder, new Location(), newLocation, ProductHistoryType.LOCATION_ADD)
            }
        }

        ArrayList<Location> deleteLocations = new ArrayList<>();
        // Remove any locations which no longer exist.
        variantLocations?.each { existingLocation ->
            // If the ID is not set then this must be a new location added as part of this save, so don't remove it!
            if (existingLocation.id > 0) {
                def editedLocation = editedVariant.locationz?.find { editedLocation -> editedLocation.id == existingLocation.id }

                if (!editedLocation && editedLocation?.sku != 0 && editedLocation?.storeId != 0) {
                    compareLocationFields(builder, existingLocation, new Location(), ProductHistoryType.LOCATION_DELETE)
                    deleteLocations.add(existingLocation)
                }
            }
        }

        // Don't save histories unless the product is valid otherwise this triggers a product save due to it being dirty
        //  even when restrictions fail.
        if(product.validate()) {
            productService.saveProductHistories(builder.productHistories)
        }

        for (int i = 0; i < deleteLocations.size(); i++) {
            deleteLocations.get(i).delete()
        }
    }

    /**
     * Manually check each field visible in the UI for detect the updated packs
     * @param newPack
     * @param existingPack
     * @return
     */
    def packChanged(def newPack, def existingPack) {
        return newPack.barcode != existingPack.barcode ||
                !newPack.supplier.equals(existingPack.supplier) ||
                newPack.quantity != existingPack.quantity ||
                newPack.price != existingPack.price ||
                newPack.orderCode != existingPack.orderCode ||
                newPack.barcode != existingPack.barcode ||
                newPack.recommendedRetailPrice != existingPack.recommendedRetailPrice ||
                newPack.status != existingPack.status ||
                newPack.maximumOrderQuantity != existingPack.maximumOrderQuantity
    }

    def locationChanged(def newLocation, def existingLocation) {
        return newLocation.aisle != existingLocation.aisle ||
                newLocation.bay != existingLocation.bay ||
                newLocation.shelf != existingLocation.shelf ||
                newLocation.position != existingLocation.position ||
                newLocation.location != existingLocation.location ||
                newLocation.shelfCapacity != existingLocation.shelfCapacity ||
                newLocation.minimumDisplayQuantity != existingLocation.minimumDisplayQuantity
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

    private void updateLocation(def locationToBeUpdated, def editedLocation, def editedVariant) {
        def locationsType = Retailer.findById(springSecurityService.principal.retailerId).locationsType
        locationToBeUpdated.storeId = springSecurityService.principal.storeId
        locationToBeUpdated.sku = editedVariant.sku

        if (locationToBeUpdated.id == 0 || locationsType == LocationsType.ADVANCED) {
            locationToBeUpdated.aisle = editedLocation.aisle
            locationToBeUpdated.bay = editedLocation.bay
            locationToBeUpdated.shelf = editedLocation.shelf
            locationToBeUpdated.position = editedLocation.position
        }

        if (locationToBeUpdated.id == 0 || locationsType == LocationsType.SIMPLE) {
            locationToBeUpdated.location = editedLocation.location
        }
        locationToBeUpdated.shelfCapacity = editedLocation.shelfCapacity
        locationToBeUpdated.minimumDisplayQuantity = editedLocation.minimumDisplayQuantity
    }

    private void doComparison(ProductHistoryBuilder builder, Product product, ProductCommand editedProduct) {
        builder.compare("itemCode", product.itemCode, editedProduct.itemCode)
        builder.compare("description", product.description, editedProduct.description)
        builder.compare("receiptDescription", product.receiptDescription, editedProduct.receiptDescription)
        builder.compare("unitSize", product.unitSize, editedProduct.unitSize)
        builder.compare("weightedItem", product.weightedItem, editedProduct.weightedItem)
        builder.compare("pricePerKg", (!product.weightedItem && product.pricePerKg) ? false : product.pricePerKg, editedProduct.pricePerKg)
        builder.compare("snappyProduct", product.snappyProduct, editedProduct.snappyProduct)
        builder.compare("deliItem", product.deliItem, editedProduct.deliItem)
        builder.compare("openPrice", product.openPrice, editedProduct.openPrice)
        builder.compare("zeroPrice", product.zeroPrice, editedProduct.zeroPrice)
        builder.compare("vatPercentageOverride", product.vatPercentageOverride == null ? BigDecimal.ZERO.setScale(2) : product.vatPercentageOverride, editedProduct.vatPercentageOverride)
        builder.compare("discreetMessage", product.discreetMessage, editedProduct.discreetMessage)
        builder.compare("status", product.status, editedProduct.status)

        builder.compare("category", product.category?.description, editedProduct.category?.description)

        // Restrictions
        builder.compare("minOpenPrice", product.restrictions.minOpenPrice == null ? product.restrictions.getDefaultMinOpenPrice() : product.restrictions.minOpenPrice, editedProduct.restrictions.minOpenPrice)
        builder.compare("maxOpenPrice", product.restrictions.maxOpenPrice == null ? product.restrictions.getDefaultMaxOpenPrice() : product.restrictions.maxOpenPrice, editedProduct.restrictions.maxOpenPrice)
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

        List<String> deletedBarcodes = new ArrayList<>();
        editedProduct.variants.stream().filter({ variant -> variant != null }).forEach({ variant ->
            product.variants.stream().filter({ v -> v.id == variant.id }).findAny().ifPresentOrElse({ oldVariant ->
                if (variant.delete) {
                    doVariantComparison(builder, variant.id, oldVariant, new ProductVariantCommand(), deletedBarcodes)
                } else {
                    doVariantComparison(builder, variant.id, oldVariant, variant, deletedBarcodes)
                }
            }, {
                doVariantComparison(builder, variant.id, new ProductVariant(), variant, deletedBarcodes)
            })
        })

        product?.variants?.stream().filter ({v -> v.effectiveDate == editedProduct.effectiveDate}).each { existingVariants ->
            def editedVariant = editedProduct?.find {editedVariant -> editedVariant.id == existingVariants.id}
            if (!editedVariant){
                // Variant deleted
                doVariantComparison(builder, existingVariants.id, existingVariants, new ProductVariantCommand(), deletedBarcodes)
            }
        }
    }

    private void doVariantComparison(ProductHistoryBuilder builder, Integer id, ProductVariant oldVariant, ProductVariantCommand variant, List<String> deletedBarcodes) {

        //---------------------------- Update history for variant fields --------------------------------//
        if (variant.sku != 0) {
            builder.compare(id, "sku", oldVariant.sku, variant.sku)
        }

        // Only compare retail price if there wasn't one before or there was and it's changed - it should not be possible to unset retail price
        if ((oldVariant.retailPrice == null && variant.retailPrice != null) || (oldVariant.retailPrice != null && variant.retailPrice != null)) {
            builder.compare(id, "retailPrice", oldVariant.retailPrice ?: BigDecimal.ZERO, variant.retailPrice ?: BigDecimal.ZERO)
        }
        if (oldVariant.costPrice != null && variant.costPrice!= null) {
            builder.compare(id, "costPrice", oldVariant.costPrice ?: BigDecimal.ZERO, variant.costPrice ?: BigDecimal.ZERO)
        }
        builder.compare(id, "size", oldVariant.size, variant.size)
        builder.compare(id, "colour", oldVariant.colour, variant.colour)
        builder.compare(id, "minimumStockLevel", oldVariant.minimumStockLevel, variant.minimumStockLevel)
        builder.compare(id, "shelfLifeDays", oldVariant.shelfLifeDays, variant.shelfLifeDays)
        if ((oldVariant.shelfCapacity == null && variant.shelfCapacity != null) || (oldVariant.shelfCapacity != null && variant.shelfCapacity != null)) {
            builder.compare(id, "shelfCapacity", oldVariant.shelfCapacity, variant.shelfCapacity, ProductHistoryType.LOCATION_EDIT)
        }
        if ((oldVariant.minimumDisplayQuantity == null && variant.minimumDisplayQuantity != null) || (oldVariant.minimumDisplayQuantity != null && variant.minimumDisplayQuantity != null)) {
            builder.compare(id, "minimumDisplayQuantity", oldVariant.minimumDisplayQuantity, variant.minimumDisplayQuantity, ProductHistoryType.LOCATION_EDIT)
        }
        builder.compare(id, "defaultSupplierId", oldVariant.defaultSupplierId, variant.defaultSupplierId)

        //---------------------------- Update history for barcode fields --------------------------------//

        // loop over edited variant barcodes to find out if barcode been edited or newly added
        variant?.barcodez?.each { editedBarcode ->
            // Can't set barcode to null so this shouldn't appear in change history (means something else has changed)
            if(editedBarcode == null || (editedBarcode.barcode == null && editedBarcode.recordStatus != 'D')) {
                return
            }
            def existingBarcode = oldVariant?.barcodes?.find { existingBarcode -> existingBarcode.id == editedBarcode.id }

            if (existingBarcode) { //if barcode already existed
                builder.compare("barcode", existingBarcode.barcode, editedBarcode.barcode)
            } else {//if barcode is newly created
                builder.compare("barcode", null, editedBarcode.barcode)
            }
        }

        // loop over existing variant barcodes to find out if barcode been deleted
        oldVariant?.barcodes?.each { existingBarcode ->
            def editedBarcode = variant?.barcodez?.find { editedBarcode -> editedBarcode.id == existingBarcode.id }

            if (!editedBarcode && !deletedBarcodes.contains(existingBarcode.barcode)) { //if edited barcode not exists means old barcode has been deleted
                deletedBarcodes.add(existingBarcode.barcode);
                builder.compare("barcode", existingBarcode.barcode, null)
            }
        }

        //---------------------------- Update history for pack fields --------------------------------//

        variant?.packs?.each { editedPack ->
            def existingPack = oldVariant?.packs?.find { existingPack -> existingPack.id == editedPack.id }
            if (existingPack) { //Pack already existed
                comparePackFields(builder, existingPack, editedPack)
            } else { //Pack newly added
                comparePackFields(builder, new Pack(), editedPack)
            }
        }

        // Remove any packs which no longer exist.
        oldVariant?.packs?.each { existingPack ->
            // If the ID is not set then this must be a new pack added as part of this save
            if (existingPack.id > 0) {
                def editedPack = variant?.packs?.find { editedPack -> editedPack.id == existingPack.id }
                if (!editedPack) { //Pack is removed
                    comparePackFields(builder, existingPack, new PackCommand())
                }
            }
        }
    }

    void comparePackFields(ProductHistoryBuilder builder, Pack oldPack, PackCommand pack){
        builder.compare("packSupplier", oldPack.supplier, pack.supplier)
        builder.compare("packQuantity", oldPack.quantity, pack.quantity)
        builder.compare("packPrice", oldPack.price, pack.price)
        builder.compare("packOrderCode", oldPack.orderCode, pack.orderCode)
        builder.compare("packBarcode", oldPack.barcode, pack.barcode)
        builder.compare("packRecommendedRetailPrice", oldPack.recommendedRetailPrice, pack.recommendedRetailPrice)
        builder.compare("packStatus", oldPack.status, pack.status)
        builder.compare("packMaximumOrderQuantity", oldPack.maximumOrderQuantity, pack.maximumOrderQuantity)
    }

    void compareLocationFields(ProductHistoryBuilder builder, Location oldLocation, def location, ProductHistoryType productHistoryType) {
        builder.compare(null, "aisle", oldLocation.aisle, location.aisle, productHistoryType)
        builder.compare(null, "bay", oldLocation.bay, location.bay, productHistoryType)
        builder.compare(null, "shelf", oldLocation.shelf, location.shelf, productHistoryType)
        builder.compare(null, "position", oldLocation.position, location.position, productHistoryType)
        builder.compare(null, "location", oldLocation.location, location.location, productHistoryType)
        builder.compare(null, "shelfCapacity", oldLocation.shelfCapacity, location.shelfCapacity, productHistoryType)
        builder.compare(null, "minimumDisplayQuantity", oldLocation.minimumDisplayQuantity, location.minimumDisplayQuantity, productHistoryType)
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
                    if ((!currentPrice || currentPrice.price?.compareTo(BigDecimal.ZERO) == 0) && priceChange.price && priceChange.price != BigDecimal.ZERO) {
                        productIds.add(variant.productId)
                    }

                    def priceBand = priceBands.find { it.id == priceChange.priceBandId }

                    if (priceBand && priceChange.sku && priceChange.price) {
                        def fromValue = currentPrice ? currentPrice.price : null

                        ProductPrice productPrice
                        if (currentPrice?.effectiveDate == effectiveDate) {
                            productPrice = currentPrice
                            productPrice.price = priceChange.price
                        } else {
                            productPrice = new ProductPrice(priceBand: priceBand, sku: priceChange.sku, price: priceChange.price, effectiveDate: effectiveDate)
                        }

                        ProductHistory productHistory =
                                new ProductHistory(retailerId: springSecurityService.principal.retailerId, productId: variant.product.id, fromValue: fromValue, toValue: priceChange.price, productHistoryType: ProductHistoryType.PRICE, priceBandId: priceChange.priceBandId, storeId: variant.storeId, userId: springSecurityService.principal.id, usersName: springSecurityService.principal?.usersName, effectiveDate: effectiveDate, updateDate: now)

                        changedProductPrices.add(productPrice)
                        productHistories.add(productHistory)
                    }
                }
            }
        }

        productService.syncProductUpdatesToAllStoresForRetailer(productIds)

        if (changedProductPrices.size() > 0) {
            productService.saveProductPrices(changedProductPrices, productHistories)

            def priceChangesGroupedByPriceBand = changedProductPrices.groupBy { it.priceBand }
            priceChangesGroupedByPriceBand?.each {

                // Change from our domain objects into a ProductPrice object from the Common library.
                def commonProductPrices = []
                it.value.each { ProductPrice pp ->
                    commonProductPrices.add(pp.getProductPrice())
                }

                productService.sendProductPriceUpdate(commonProductPrices, storeService.getStoresByPriceBand(springSecurityService.principal.retailerId, it.key))
            }
        }
    }

    private void saveRangeUpdates(Product product, int[] savedRanges) {
        def rangesRemovedFrom = []
        def rangesAddedTo = []
        def productHistories = []

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
            RangeProduct rangeProductDelete = rangeProducts.find { it.rangeId == rangeId }
            productHistories.add(handleProductRangeHistory(rangeProductDelete, false))
            productService.deleteRangeProduct(rangeProductDelete)
        }

        def ranges = Range.findAllByRetailerId(springSecurityService.principal.retailerId)
        rangesAddedTo.each { Integer rangeId ->
            RangeProduct rangeProduct = new RangeProduct(range: ranges?.find { it.id == rangeId }, productId: product.id)
            productHistories.add(handleProductRangeHistory(rangeProduct, true))
            productService.saveRangeProduct(rangeProduct)
            productService.sendProductUpdate([product], storeService.getStoresByRange(springSecurityService.principal.retailerId, ranges.find { it.id == rangeId }))
        }

        if (productHistories != null && productHistories.size() > 0){
            productService.saveProductHistories(productHistories)
        }
    }

    private ProductHistory handleProductRangeHistory(RangeProduct rangeProduct, boolean isNew){
        def now = DateTime.now(DateTimeZone.UTC)
        ProductHistoryType productHistoryType = isNew ? ProductHistoryType.PRODUCT_RANGE_ADD : ProductHistoryType.PRODUCT_RANGE_DELETE

        ProductHistory productHistory =
                new ProductHistory(retailerId: springSecurityService.principal.retailerId, productId: rangeProduct.getProductId(),
                        fromValue: null, toValue: rangeProduct.getRange()!= null ? rangeProduct.getRange().getDescription() : -1,
                        productHistoryType: productHistoryType,
                        storeId: springSecurityService.principal.storeId, userId: springSecurityService.principal.id, usersName: springSecurityService.principal?.usersName,
                        effectiveDate: effectiveDate, updateDate: now)

        return productHistory
    }

    def ajaxSearchCategories(String searchTerm, boolean triggerOnCategoryChange, int level) {
        def searchResults = baseSearchCategories(searchTerm)
        boolean isSearch = searchTerm?.length() > 0
        render(template: "/product/categorySelectInputs", model: [categories: searchResults.aValue.unique(), level: isSearch ? level : 1, productCategoryList: searchResults.bValue, selectedCategoryId: null, triggerOnCategoryChange: triggerOnCategoryChange, isSearch: isSearch])
    }

    def ajaxGetChildCategories(int categoryId, int level, int selectedCategoryId, boolean triggerOnCategoryChange) {
        def category = categoryService.getCategory(categoryId)

        render(template: "categorySelectInputs", model: [categories: category?.childCategories, level: level, selectedCategoryId: selectedCategoryId, triggerOnCategoryChange: triggerOnCategoryChange])
    }

    def ajaxAddVariant(AddVariantCommand cmd) {
        render(template: "addVariant", model: [variant: cmd, zeroPrice: cmd.zeroPrice, isEditMode: cmd.operationMode == OperationMode.EDIT.value])
    }

    def ajaxAddBarcode(int index) {
        render(template: "addBarcode", model: [index: index])
    }

    def ajaxSaveVariant(AddVariantCommand cmd) {
        def storeId = springSecurityService.principal.storeId

        render(template: "variant", model: [index: cmd.index, variant: cmd, barcodes: cmd.barcodez, storeId: storeId])
    }

    def ajaxAddTempLocation(AddVariantCommand cmd) {
        def storeId = springSecurityService.principal.storeId
        def locationsEnabled = [LocationsType.SIMPLE, LocationsType.ADVANCED].contains(springSecurityService.principal.retailer.config.locationsType)

        render(template: "locationVariant", model: [index: cmd.index, variant: cmd, locationsEnabled: locationsEnabled, storeId: storeId])
    }

    def ajaxAddPrice(int index, long sku, boolean zeroPrice) {
        def priceBands = PriceBand.findAllByRetailerId(springSecurityService.principal.retailerId, [sort: "description", order: "asc"])

        render(template: "addPrice", model: [skuIndex: index, sku: sku, variant: null, priceBands: priceBands, zeroPrice: zeroPrice])
    }

    def ajaxSuppliers(SuppliersCommand cmd) {
        def defaultSuppliers = Supplier.findAllByRetailerId(springSecurityService.principal.retailerId)

        def suppliers = defaultSuppliers.findAll { it.symbolGroup == null }

        // Get IDs of already saved Packs
        ArrayList<Integer> existingPackIds = new ArrayList<Integer>()
        cmd.getPacks().each {
            if (Pack.findById(it.id) != null) {
                existingPackIds.add(it.id)
            }
        }

        render(template: "suppliers", model: [suppliers: suppliers, statuses: PackStatus.values(), variant: cmd, variantIndex: cmd.index, defaultSupplier: params.defaultSupplier, defaultSuppliers: defaultSuppliers, existingPackIds: existingPackIds])
    }

    def ajaxLocations(LocationsCommand cmd) {
        def locations = Location.findAllByStoreIdAndSku(springSecurityService.principal.storeId, params.sku)

        render(template: "locations", model: [locations: locations, variant: cmd, variantIndex: cmd.index, locationsType: springSecurityService.principal.retailer.config.locationsType.name()])
    }

    def ajaxAddPack(int variantIndex, int packIndex, int productVariantId) {
        def suppliers = Supplier.findAllByRetailerId(springSecurityService.principal.retailerId)

        suppliers.removeAll { it.symbolGroup != null }

        render(template: "addPack", model: [variantIndex: variantIndex, productVariantId: productVariantId, packIndex: packIndex, suppliers: suppliers, statuses: PackStatus.values(), isNewPack: true])
    }

    def ajaxAddLocation(int variantIndex, int locationIndex, int productVariantId) {
        render(template: "addLocation", model: [variantIndex: variantIndex, productVariantId: productVariantId, locationIndex: locationIndex, isNewLocation: true, locationsType: springSecurityService.principal.retailer.config.locationsType.name()])
    }

    def ajaxSavePack(SuppliersCommand cmd) {
        List<AddPackCommand> packs = cmd.getPacks()
        if (packs) {
            packs.each { pack ->
                // We dont want to save NISA packs
                if (pack.supplier.symbolGroupId == null) {
                    if (!pack.validate()) {
                        if (!cmd.hasErrors)
                            cmd.hasErrors = Boolean.TRUE
                        pack.isNewPack = Boolean.TRUE
                    }
                }
            }
        }
        if (cmd.hasErrors) {
            def defaultSuppliers = Supplier.findAllByRetailerId(springSecurityService.principal.retailerId)
            def suppliers = defaultSuppliers.findAll { it.symbolGroup == null }

            // Get IDs of already saved Packs
            ArrayList<Integer> existingPackIds = new ArrayList<Integer>()
            cmd.getPacks().each {
                if (Pack.findById(it.id) != null) {
                    existingPackIds.add(it.id)
                }
            }

            render(status: HttpStatus.BAD_REQUEST, template: "suppliers", model: [suppliers: suppliers, defaultSuppliers: defaultSuppliers, existingPackIds: existingPackIds, statuses: PackStatus.values(), variant: cmd, variantIndex: cmd.index, defaultSupplier: params.defaultSupplier, packs: cmd.packs])
        } else {
            render(status: HttpStatus.OK, template: "packs", model: [variantIndex: cmd.index, packs: cmd.packs, defaultSupplier: params.defaultSupplier])
        }
    }

    def ajaxSaveLocation(LocationsCommand cmd) {
        cmd.getLocationz()?.forEach({ location ->
            if (!location.validate()) {
                if (!cmd.hasErrors)
                    cmd.hasErrors = Boolean.TRUE
                location.isNewLocation = Boolean.TRUE
            }
        })

        render(status: HttpStatus.OK, template: "locationz", model: [locations: cmd.locationz, variantIndex: cmd.index, locationsType: springSecurityService.principal.retailer.config.locationsType.name()])
    }

    //This will render category mapped restrictions for new products
    def ajaxGetRestrictions(int selectedCategoryId, boolean productOpenPrice) {
        Restrictions restrictions = null
        def category = categoryService.getCategory(selectedCategoryId)
        if (category != null) {
            restrictions = category.restrictions
        }
        //when rendering restriction tab manually set isNewProduct to false since category mapped restriction should be loaded rather default values
        render(view: "/product/_restrictions", model: [restrictions: restrictions, productOpenPrice: productOpenPrice, isNewProduct: false])
    }

    //This will render product history for selected product
    def ajaxGetProductHistory(int productId) {
        def productHistoryMap = [:]
        if (productId > 0) { // If product id does not exists there can not be any history to return
            def effectiveDate = DateTime.now(DateTimeZone.UTC) // Take default effective date as current date
            if (session != null && session.effectiveDate != null && session.effectiveDate[1] != null) {
                effectiveDate = session.effectiveDate[1]//replace effective date if it already has one
            }

            def productHistoryList = productHistoryService.getProductHistory(productId, effectiveDate)
            // Load product history from db
            productHistoryList = productHistoryList?.sort {
                it?.effectiveDate
            }

            productHistoryList = productHistoryList?.reverse() // Convert into descending order

            String nullString = "null"
            productHistoryList?.each { item ->
                if (item?.fromValue == null || item?.fromValue == nullString) {
                    item?.fromValue = "unset"
                } else if (item?.productHistoryType?.equals(ProductHistoryType.PRICE)) {
                    item?.fromValue = String.format("£%s", item?.fromValue)
                }

                if (item?.toValue == null || item?.toValue == nullString) {
                    item?.toValue = "unset"
                } else if (item?.productHistoryType?.equals(ProductHistoryType.PRICE)) {
                    item?.toValue = String.format("£%s", item?.toValue)
                }
            }

            //convert product list into product map by group by using effective date
            productHistoryMap = productHistoryList?.groupBy {
                it?.effectiveDate?.toDate()?.format('dd/MM/yyyy')
            }
        }

        render(view: "/product/_productHistory", model: [productHistoryMap: productHistoryMap])
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

    private void copyProduct(ProductCommand from, Product to) {
        to.springSecurityService = springSecurityService

        to.id = from.id
        to.retailerId = from.retailerId
        to.itemCode = from.itemCode
        to.description = from.description
        to.receiptDescription = from.receiptDescription
        to.category = from.category
        to.unitSize = from.unitSize
        to.weightedItem = from.weightedItem
        to.openPrice = from.openPrice
        to.zeroPrice = from.zeroPrice
        to.pricePerKg = from.pricePerKg
        to.snappyProduct = from.snappyProduct
        to.deliItem = from.deliItem
        to.vatCode = from.vatCode
        to.vatPercentageOverride = from.vatPercentageOverride
        to.discreetMessage = from.discreetMessage
        to.status = from.status
        to.retailerProductId = from.retailerProductId

    }

    private void copyProductVariants(ProductCommand from, Product to) {
        List<ProductVariant> variants = new ArrayList<>();
        Map<Long, ProductVariant> existingVariants = new HashMap<>();

        if (to.variants && !to.variants.isEmpty()) {
            to.variants.forEach({ variant ->
                existingVariants.put(variant.sku, variant)
            })
        }

        from.variants.forEach({ variant ->
            ProductVariant productVariant
            if (existingVariants.containsKey(variant.sku)) {
                productVariant = existingVariants.get(variant.sku)
            } else {
                productVariant = new ProductVariant()
            }
            productVariant.id = variant.id
            productVariant.storeId = springSecurityService.principal.storeId
            productVariant.sku = variant.sku
            productVariant.costPrice = variant.costPrice
            productVariant.effectiveDate = variant.effectiveDate
            productVariant.shelfLifeDays = variant.shelfLifeDays
            productVariant.shelfCapacity = variant.shelfCapacity
            productVariant.minimumDisplayQuantity = variant.minimumDisplayQuantity
            productVariant.setProduct(to)

            List<Barcode> barcodes = new ArrayList<>();
            Map<String, Barcode> existingBarcodes = new HashMap<>();
            if (productVariant.barcodez && !productVariant.barcodez.isEmpty()) {
                productVariant.barcodez.forEach({ barcode ->
                    existingBarcodes.put(barcode.barcode, barcode)
                })
            }

            variant.barcodez.forEach({ barcode ->
                Barcode productBarcode

                if (existingBarcodes.containsKey(barcode.barcode)) {
                    productBarcode = existingBarcodes.get(barcode.barcode)
                } else {
                    productBarcode = new Barcode()
                }

                productBarcode.id = barcode.id
                productBarcode.sku = barcode.sku
                productBarcode.retailerId = barcode.retailerId
                productBarcode.barcode = barcode.barcode
                productBarcode.effectiveDate = variant.effectiveDate
                productBarcode.recordStatus = barcode.recordStatus

                barcodes.add(productBarcode)
            })

            productVariant.barcodez.clear()
            productVariant.barcodez.addAll(barcodes)
            variants.add(productVariant)
        })

        to.variants.clear()
        to.variants.addAll(variants)

    }

    def isValidBarcode(Barcode barcode) {
        barcode == null || StringUtils.isEmpty(barcode.getBarcode()) || barcode.validate()
    }

    def isValidSku(Long sku) {
        def existingVariant = ProductVariant.findBySku(sku)
        return existingVariant == null
    }

    def ajaxCSVProductUpload() {
        def file = request.getFile('file')
        def is = file.inputStream
        List<Errors> errors = new ArrayList<>()

        try {
            List<CSVUploadProduct> rows = new CsvToBeanBuilder(is.newReader())
                    .withType(CSVUploadProduct)
                    .build().parse()
            rows.forEach({ CSVUploadProduct row ->
                Integer retailerId = springSecurityService.principal.retailerId
                DateTime effectiveDate = getEffectiveDate(row.effectiveDate)
                ProductCommand productCommand = row.getProduct(retailerId, effectiveDate)

                Product product = saveProduct(productCommand, null, false)

                if (product.hasErrors()) {
                    Errors productError = product.getErrors()
                    BeanPropertyBindingResult error = new BeanPropertyBindingResult(this, productError.getObjectName())
                    ObjectError objectError = new ObjectError(productError.getObjectName(),
                            String.format("Validation errors for product code - %s", product.getItemCode()))
                    error.addError(objectError)
                    error.addAllErrors(productError)
                    errors.add(error)
                }
            })
        } catch (Exception e) {
            e.printStackTrace()
            BeanPropertyBindingResult error = new BeanPropertyBindingResult(this, "Error parsing CSV File")
            ObjectError objectError = new ObjectError("", "Error while processing the CSV file")
            error.addError(objectError)
            errors.add(error)
        }
        render([status: errors.isEmpty() ? "SUCCESS" : "FAILED", errors: errors] as JSON)
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
    List<AddLocationCommand> locationz
    boolean zeroPrice
    Integer defaultSupplierId
    int operationMode
    Integer shelfCapacity
    Integer minimumDisplayQuantity

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
    int productVariantId
    List<AddPackCommand> packs
    Boolean hasErrors = Boolean.FALSE
}

class AddPackCommand implements Validateable {
    int index
    Integer id
    SupplierCommand supplier
    Integer quantity
    BigDecimal price
    String orderCode
    String barcode
    BigDecimal recommendedRetailPrice
    DateTime effectiveDate
    DateTime effectiveEndDate
    PackStatus status
    Integer maximumOrderQuantity
    Boolean allowSubstitutes
    boolean isNewPack = false
    Integer productVariantId

    static constraints = {
        importFrom Pack
        id nullable: true
        productVariantId nullable: true
        allowSubstitutes nullable: true
        supplier nullable: false, blank: false, validator: { supplier, pack ->
            if (!supplier.id) return ["addPackCommand.supplier.empty"]
        }
        price validator: {
            if (BigDecimal.ZERO == it) return ['addPackCommand.price.zero']
            if (it >= 10000) return ['addPackCommand.price.max']
        }
        quantity validator: {
            if (it <= 0) return ['addPackCommand.packQuantity.zero']
            if (it > Integer.MAX_VALUE) return ['addPackCommand.packQuantity.maxValue']
        }
        recommendedRetailPrice validator: {
            if (BigDecimal.ZERO == it) return ['addPackCommand.recommendedRetailPrice.zero']
            if (it >= 10000) return ['addPackCommand.recommendedRetailPrice.max']
        }
        maximumOrderQuantity validator: {
            if (it >= 100000) return ['addPackCommand.maxOrderQuantity.maxValue']
        }
    }
}

class LocationsCommand {
    int index
    int productVariantId
    List<AddLocationCommand> locationz
    Boolean hasErrors = Boolean.FALSE
}

class AddLocationCommand implements Validateable {
    int index
    int id
    LocationCommand locationCommand
    int storeId
    int sku
    String aisle
    String bay
    String shelf
    String position
    String location
    int shelfCapacity
    int minimumDisplayQuantity
    boolean isNewLocation = false
    int productVariantId
}

class LocationCommand {
    int id
    int storeId
    int sku
    String aisle
    String bay
    String shelf
    String position
    String location
    int shelfCapacity
    int minimumDisplayQuantity
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

class RestrictionsCommand implements Validateable {
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

    static constraints = {
        importFrom Restrictions
    }
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
    Integer shelfCapacity
    Integer minimumDisplayQuantity
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
    Collection<LocationCommand> locationz = new ArrayList<>()
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

    static constraints = {
        importFrom Pack
    }
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

class CSVUploadProduct {

    @CsvBindByName(column = 'id')
    Integer id

    @CsvBindByName(column = 'effective_date')
    String effectiveDate

    @CsvBindByName(column = 'product_description')
    String productDescription

    @CsvBindByName(column = 'receipt_description')
    String receiptDescription

    @CsvBindByName(column = 'plu_item_code')
    String pluItemCode

    @CsvBindByName(column = 'retailer_category_code')
    String retailerCategoryCode

    @CsvBindByName(column = 'variant_id')
    Integer variantId

    @CsvBindByName(column = 'default_sku')
    Integer defaultSKU

    @CsvBindByName(column = 'unit_size')
    String unitSize

    @CsvBindByName(column = 'price_bands')
    String priceBands

    @CsvBindByName(column = 'def_cost_price')
    Float defaultCostPrice

    @CsvBindByName(column = 'def_barcode')
    String defaultBarcode

    @CsvBindByName(column = 'shelf_life_days')
    Integer shelfLifeDays

    @CsvBindByName(column = 'vat_code')
    Integer vatCode

    @CsvBindByName(column = 'vat_override')
    Double vatOverride

    @CsvBindByName(column = 'discreet_message')
    String discreetMessage

    @CsvBindByName(column = 'status')
    ProductStatus status

    @CsvBindByName(column = 'weighted_item')
    String weightedItem

    @CsvBindByName(column = 'weighted_pricing_type')
    Integer weightedPricingType

    @CsvBindByName(column = 'snappy_item')
    String snappyItem

    @CsvBindByName(column = 'deli_item')
    String deliItem

    @CsvBindByName(column = 'open_price')
    String openPrice

    @CsvBindByName(column = 'zero_price')
    String zeroPrice

    ProductCommand getProduct(Integer retailerId, DateTime effectiveDate) {
        ProductCommand productCommand = new ProductCommand();
        productCommand.setId(this.id)
        productCommand.setItemCode(this.pluItemCode)
        productCommand.setDescription(this.productDescription)
        productCommand.setReceiptDescription(this.receiptDescription)
        Category category = Category.findByRetailerCategoryCode(this.retailerCategoryCode)
        productCommand.setCategory(category)
        productCommand.setUnitSize(this.unitSize)
        productCommand.setWeightedItem("YES".equalsIgnoreCase(this.weightedItem))
        productCommand.setOpenPrice("YES".equalsIgnoreCase(this.openPrice))
        productCommand.setZeroPrice("YES".equalsIgnoreCase(this.zeroPrice))
        productCommand.setPricePerKg(1 == this.weightedPricingType)
        productCommand.setSnappyProduct("YES".equalsIgnoreCase(this.snappyItem))
        productCommand.setDeliItem("YES".equalsIgnoreCase(this.deliItem))
        productCommand.setVatCode(VatCode.findById(this.vatCode))
        if (this.getVatOverride() != null) {
            productCommand.setVatPercentageOverride(new BigDecimal(this.getVatOverride()))
        }

        Restrictions categoryRestrictions = category.restrictions;
        RestrictionsCommand restrictionsCommand = new RestrictionsCommand()
        InvokerHelper.setProperties(restrictionsCommand, categoryRestrictions.properties)
        productCommand.setRestrictions(restrictionsCommand)

        productCommand.setDiscreetMessage(this.getDiscreetMessage())
        productCommand.setStatus(this.getStatus())

        ProductVariantCommand productVariantCommand = new ProductVariantCommand()
        if (this.getDefaultCostPrice() != null) {
            productVariantCommand.setCostPrice(new BigDecimal(this.getDefaultCostPrice()))
        }

        BarcodeCommand barcodeCommand = new BarcodeCommand()
        barcodeCommand.setSku(this.getDefaultSKU())
        barcodeCommand.setBarcode(this.getDefaultBarcode())
        barcodeCommand.setRetailerId(retailerId)
        barcodeCommand.setEffectiveDate(effectiveDate)
        char defRecordStatus = 'C'
        barcodeCommand.setRecordStatus(defRecordStatus)

        productVariantCommand.setBarcodez(new ArrayList<>(List.of(barcodeCommand)))
        productVariantCommand.setSku(this.getDefaultSKU())
        if (this.getVariantId() != null) {
            productVariantCommand.setId(this.getVariantId())
        }
        productVariantCommand.setSku(this.getDefaultSKU())
        productVariantCommand.setShelfLifeDays(this.getShelfLifeDays())

        productCommand.setVariants(new ArrayList<>(List.of(productVariantCommand)))

        String priceBands = this.getPriceBands();
        String[] bands = priceBands != null ? priceBands.split("\\|") : []
        List<PriceChangeCommand> priceChanges = new ArrayList<>()
        for (String band : bands) {
            String[] bandValues = band.split("=")
            if (bandValues.size() != 2) {
                throw new RuntimeException("Cannot parse price band information")
            }
            PriceBand priceBand = PriceBand.findByDescriptionAndRetailerId(bandValues[0].strip(), retailerId)

            PriceChangeCommand priceChangeCommand = new PriceChangeCommand()
            priceChangeCommand.setSku(this.getDefaultSKU())
            priceChangeCommand.setPriceBandId(priceBand.getId())
            priceChangeCommand.setPrice(new BigDecimal(Double.parseDouble(bandValues[1].strip())))
            priceChanges.add(priceChangeCommand)
        }

        SavePriceChangesCommand savePriceChangesCommand = new SavePriceChangesCommand()
        savePriceChangesCommand.setPriceChanges(priceChanges)

        productCommand.setPriceChanges(new ArrayList<>(List.of(savePriceChangesCommand)))

        return productCommand
    }

}