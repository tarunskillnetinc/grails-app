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
import org.springframework.validation.FieldError
import org.springframework.validation.ObjectError
import uk.co.wonderlane.wlpos.enums.LocationsType
import uk.co.wonderlane.wlpos.enums.PackStatus
import uk.co.wonderlane.wlpos.enums.ProductHistoryType
import uk.co.wonderlane.wlpos.enums.ProductStatus
import uk.co.wonderlane.wlpos.enums.StockSale
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
        def selTypeValues = SelType.list().sort { it.id }
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

        def locationsType = springSecurityService.principal.retailer.config.locationsType.name()
        def locationsEnabled = [LocationsType.SIMPLE, LocationsType.ADVANCED].contains(springSecurityService.principal.retailer.config.locationsType)
        def loyaltyEnabled = springSecurityService.principal.retailer.config?.loyaltyRetailerConfig?.isLoyaltyEnabled ? true : false

        render(view: "add", model: [product            : product,
                                    storeId            : springSecurityService.principal.storeId,
                                    statusValues       : ProductStatus.values(),
                                    selTypeValues      : selTypeValues,
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
                                    locationsType      : locationsType,
                                    loyaltyEnabled     : loyaltyEnabled])
    }

    private void setEffectiveDate() {
        DateTimeFormatter formatter = DateTimeFormat.forPattern("dd MMMM yyyy").withZone(DateTimeZone.UTC)

        setEffectiveDate(formatter)
    }

    private void setEffectiveDate(DateTimeFormatter formatter) {
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
        def selTypeValues = SelType.list().sort { it.id }
        def userRoles = springSecurityService.principal.authorities*.authority

        if (userRoles.contains("ROLE_HEAD_OFFICE") || userRoles.contains("ROLE_ENGINEER")) {
            priceBands = PriceBand.findAllByRetailerId(springSecurityService.principal.retailerId, [sort: "description", order: "asc"])
            ranges = Range.findAllByRetailerId(springSecurityService.principal.retailerId, [sort: "description", order: "asc"])
        }

        def locationsEnabled = [LocationsType.SIMPLE, LocationsType.ADVANCED].contains(springSecurityService.principal.retailer.config.locationsType)
        def loyaltyEnabled = springSecurityService.principal.retailer.config?.loyaltyRetailerConfig?.isLoyaltyEnabled ? true : false

        render(view: "add", model: [storeId         : springSecurityService.principal.storeId,
                                    statusValues    : ProductStatus.values(),
                                    selTypeValues   : selTypeValues,
                                    categoryValues  : categoryService.getTopLevelCategories(),
                                    vatValues       : VatCode.findAllByRetailerId(springSecurityService.principal.retailerId),
                                    ranges          : ranges,
                                    priceBands      : priceBands,
                                    now             : DateTime.now(DateTimeZone.UTC).withTimeAtStartOfDay(),
                                    isNewProduct    : true,
                                    locationsEnabled: locationsEnabled,
                                    locationsType   : springSecurityService.principal.retailer.config.locationsType.name(),
                                    loyaltyEnabled  : loyaltyEnabled])
    }

    def search() {
        session.PRODUCT_SEARCH_TERM = params.searchTerm
        session.effectiveDate = ["Current", DateTime.now(DateTimeZone.UTC)]

        def products = productService.searchProductsHql(params.searchTerm, params.searchBy, 50, params.offset ? Integer.parseInt(params.offset) : 0, "id", "asc")

        render(template: "addProductSearchResults", model: [products: products.products, totalResults: products.totalCount, storeId: springSecurityService.principal.storeId])
    }

    /**
     * Called from the main product maintenance search screen.
     */
    def ajaxSearchProducts() {
        session.PENDING_CHANGES = params.pendingChanges
        session.PRODUCT_SEARCH_TERM = params.searchTerm
        Boolean filterWithPendingChanges = Boolean.parseBoolean(params.pendingChanges)
        session.effectiveDate = ["Current", DateTime.now(DateTimeZone.UTC)]

        def products = productService.searchProductsHql(params.searchTerm, params.searchBy, params.max ? Integer.parseInt(params.max) : 50, params.offset ? Integer.parseInt(params.offset) : 0, "id", "asc", filterWithPendingChanges)

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
        def suppliers = supplierService.getSortedRetailerSuppliers([sort: "name"])
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

        if (cmd?.priceChanges === null) {
            render "EMPTY"
            return
        }

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

        if (cmd?.rangeProducts === null) {
            render "EMPTY"
            return
        }

        def ranges = Range.findAllByRetailerId(springSecurityService.principal.retailerId)

        def newlyRangedProducts = []
        def noLongerRangedProducts = []
        def productHistories = []
        def rangedProductsMap = [:]
        def unrangedProductsMap = [:]

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

                if (!unrangedProductsMap.containsKey(rangeProduct.rangeId)) {
                    unrangedProductsMap[rangeProduct.rangeId] = []
                }

                unrangedProductsMap[rangeProduct.rangeId].add(rangeProduct)
                productHistories.add(handleProductRangeHistory(existingRangeProduct, false))
            }
        }

        productService.saveRangeProducts(newlyRangedProducts)
        productService.deleteRangeProducts(noLongerRangedProducts)
        if (productHistories != null && productHistories.size() > 0) {
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

        unrangedProductsMap.each { rangeId, rangeProductChanges ->
            def allProducts = []

            rangeProductChanges.each { RangeProductCommand rangeProductCommand ->
                allProducts.addAll(productService.getProduct(rangeProductCommand.productId))
            }

            productService.sendProductUpdate(
                    allProducts,
                    storeService.getStoresByRange(springSecurityService.principal.retailerId, ranges.find { it.id == rangeId }),
                    false
            )
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
        boolean duplicateItemCode = false

        newProduct = !(isRequest ? paramsMap.id && Integer.parseInt(paramsMap.id) > 0 : editedProduct.id && editedProduct.id > 0)

        DateTime now = DateTime.now(DateTimeZone.UTC)
        List<ProductVariant> productVariantsList = new ArrayList<>()

        List<RangeProduct> existingRangeProducts = new ArrayList<>()

        if (newProduct) {
            changeAffectsSel = true
            if (isRequest) {
                if (paramsMap["itemCode"] != null || paramsMap["itemCode"].toString().trim().length() > 0) {
                    duplicateItemCode = Product.countByRetailerIdAndItemCode(springSecurityService.principal.retailerId, paramsMap["itemCode"]) > 0
                    if (duplicateItemCode) {
                        // CORE-2916 - the new Product(map) loads in the product variants from the itemCode, even if it already exists and this is a new product
                        // so we need to manually correct this by creating a new product object without the item code.
                        paramsMap["itemCode"] = ""
                        paramsMap["variants[0].sku"] = null
                        paramsMap["variants[0].retailPrice"] = null
                        paramsMap["variants[0].costPrice"] = null
                        paramsMap["variants[0].shelfLifeDays"] = null
                        paramsMap["variants[0].shelfCapacity"] = null
                        paramsMap["variants[0].minimumDisplayQuantity"] = null
                        paramsMap["variants[0].effectiveDate"] = null
                        paramsMap["variants[0].defaultSupplierId"] = null
                        paramsMap["variants[0]"] = null
                        paramsMap.remove("variants[0]")
                    }
                }
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

                // Check whether the SKU is used elsewhere
                if (!isValidSku(variant.sku)) {
                    product.errors.reject('product.productVariants.notUnique', [variant.sku] as Object[], 'SKU {0} already exists on another product.')
                }

                variant.barcodez?.each { barcode ->
                    barcode.retailerId = springSecurityService.principal.retailerId
                    barcode.sku = variant.sku
                    barcode.effectiveDate = barcode.effectiveDate ?: effectiveDate

                    if (!barcode.validate()) {
                        handleBarcodeValidation(barcode, product)
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
            product.stockSale = editedProduct.stockSale
            product.selType = editedProduct.selType
            product.selDescription = editedProduct.selDescription
            product.productImgUrl = editedProduct.productImgUrl

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

            // Range Products
            for (RangeProduct rangeProduct in product.ranges) {
                // Copy the items without copying the list itself for later reference to which products have been unranged
                existingRangeProducts.add(rangeProduct)
            }
        }

        // check for errors added manually from barcode and category checks or validate can remove them
        //  before they are handled
        if (product.hasErrors()) {
            reapplyLostProductUpdates(newProduct, product, productVariantsList, editedProduct)
            return product
        }

        product.validate()
        if (duplicateItemCode) {
            product.errors.rejectValue("itemCode", "product.itemCode.validator.error")
        } else if (product.itemCode == null || product.itemCode.trim().isEmpty()) {
            product.errors.rejectValue("itemCode", "product.itemCode.nullable.error")
        }

        if (editedProduct.effectiveDate == null) {
            product.errors.reject('error.Product.badEffectiveDate')
        }

        if (!product.hasErrors() && product.validate() && productService.isLocationValid(product)) {
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

                savePriceUpdates(product.currentVariants, product, priceChanges, effectiveDate)
                if (product.hasErrors()) {
                    return product
                }

                saveRangeUpdates(product, editedProduct.rangeId?.toSet() as HashSet<Integer>)
            }

            if (isRequest) {
                flash.message = "Product saved successfully"
            }
        } else {
            reapplyLostProductUpdates(newProduct, product, productVariantsList, editedProduct)
        }

        if (!product.hasErrors()) {
            if (productService.isSingleStageSel() || !changeAffectsSel) {
                List<RangeProduct> unrangedRangeProducts = []
                def currentRangeProducts = RangeProduct.findAllByProductId(product.id)
                for (RangeProduct existingRangeProduct in existingRangeProducts) {
                    if (!currentRangeProducts.find {x -> x.id == existingRangeProduct.id }) {
                        // This Range Product existed before updating and no longer does, the product bust have been unranged
                        unrangedRangeProducts.add(existingRangeProduct)
                    }
                }

                if (springSecurityService.principal.storeId) {
                    boolean insert = !unrangedRangeProducts.find { x -> x.productId == product.id}
                    productService.sendProductUpdate([product], [Store.findById(springSecurityService.principal.storeId)], insert)
                } else {
                    currentRangeProducts?.each { rangeProduct ->
                        productService.sendProductUpdate(
                                [product],
                                storeService.getStoresByRange(springSecurityService.principal.retailerId, rangeProduct.range)
                        )
                    }

                    unrangedRangeProducts?.each { rangeProduct ->
                        productService.sendProductUpdate(
                                [product],
                                storeService.getStoresByRange(springSecurityService.principal.retailerId, rangeProduct.range),
                                false
                        )
                    }
                }
            }
        }

        return product
    }

    private void reapplyLostProductUpdates(Boolean newProduct, Product product, List<ProductVariant> productVariantsList, editedProduct) {
        if (!newProduct) {
            // productVariantsList is only the new variants so addAll works here
            product.variants.addAll(productVariantsList)
            product.variants.forEach {
                variant ->
                    {
                        editedProduct.variants.forEach {
                            editedVariant ->
                                {
                                    if (variant.sku == editedVariant.sku) {
                                        variant.locationz = editedVariant.locationz ?: variant.locations
                                    }
                                }
                        }
                    }
            }
        }
    }

    def getColumns() {
        return productService.getColumns()
    }

    def save(ProductCommand editedProduct) {
        // Domain calls moved prior to Save Product in case of EntityInsertAction was vetoed error that prevents further Domain Calls.
        def topLevelCategories = categoryService.getTopLevelCategories()
        def vatValues = VatCode.findAllByRetailerId(springSecurityService.principal.retailerId)

        try {
            DateTimeFormatter formatter = DateTimeFormat.forPattern("dd/MM/yyyy").withZone(DateTimeZone.UTC)
            editedProduct.setEffectiveDate(formatter.parseDateTime(params.effectiveDate))
        } catch (UnsupportedOperationException | IllegalArgumentException | NullPointerException ex) {
            log.println("exception parsing user provided date: ${ex.getMessage()}")
            editedProduct.setEffectiveDate(null)
        }

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
            def selTypeValues = SelType.list().sort { it.id }
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
            def locationsEnabled = [LocationsType.SIMPLE, LocationsType.ADVANCED].contains(springSecurityService.principal.retailer.config.locationsType)
            def loyaltyEnabled = springSecurityService.principal.retailer.config?.loyaltyRetailerConfig?.isLoyaltyEnabled ? true : false

            render(view: "add", model: [product            : product,
                                        storeId            : springSecurityService.principal.storeId,
                                        statusValues       : ProductStatus.values(),
                                        selTypeValues      : selTypeValues,
                                        categoryValues     : topLevelCategories,
                                        productCategoryList: productCategoryList,
                                        effectiveDateIndex : session.effectiveDate,
                                        ranges             : ranges,
                                        selectedRanges     : editedProduct.rangeId,
                                        priceBands         : priceBands,
                                        editedPrices       : editedPrices,
                                        vatValues          : vatValues,
                                        locationsType      : springSecurityService.principal.retailer.config.locationsType.name(),
                                        locationsEnabled   : locationsEnabled,
                                        loyaltyEnabled     : loyaltyEnabled])
        }
    }

    private List<ProductVariant> getUpdatedProductVariantsOnSave(ProductCommand editedProduct, product, builder, boolean changeAffectsSel, effectiveDate) {
        DateTime now = DateTime.now(DateTimeZone.UTC)
        List<ProductVariant> productVariantList = new ArrayList<>()

        editedProduct.variants?.each { editedVariant ->

            def existingVariant = product.variants?.find { variant -> variant.id == editedVariant.id }

            // If the variant we're editing is the current one for our store and the effective date is today or the same as the one we're editing, we update it. Otherwise we need a new variant.
            if (editedVariant.id != 0 && existingVariant &&
                    editedVariant.storeId == springSecurityService.principal.storeId &&
                    (!effectiveDate.isAfter(DateTime.now(DateTimeZone.UTC).withTimeAtStartOfDay()) || effectiveDate.isEqual(new DateTime(editedVariant.effectiveDate).withZone(DateTimeZone.UTC).withTimeAtStartOfDay()))) {

                // Variant we saved is one which already exists, check for changes.
                if (builder.getChangedProductVariantIds().contains(existingVariant.id)) {
                    // Variant has changed
                    existingVariant.storeId = springSecurityService.principal.storeId
                    existingVariant.sku = editedVariant.sku
                    existingVariant.retailPrice = editedVariant.retailPrice
                    changeAffectsSel = checkChangeAffectsSel(changeAffectsSel, existingVariant.costPrice, editedVariant.costPrice)
                    existingVariant.costPrice = editedVariant.costPrice
                    existingVariant.size = editedVariant.size
                    existingVariant.colour = editedVariant.colour
                    existingVariant.minimumStockLevel = editedVariant.minimumStockLevel
                    existingVariant.effectiveDate = effectiveDate
                    existingVariant.shelfLifeDays = editedVariant.shelfLifeDays
                    existingVariant.shelfCapacity = editedVariant.shelfCapacity
                    existingVariant.minimumDisplayQuantity = editedVariant.minimumDisplayQuantity
                    existingVariant.defaultSupplierId = editedVariant.defaultSupplierId
                    if (existingVariant.getShelfCapacity() != null
                            && !(existingVariant.getShelfCapacity() >= 1 && existingVariant.getShelfCapacity() <= 999)) {
                        product.errors.reject('productVariant.shelfCapacity.size.error', 'Shelf Capacity must be between 1 to 999.')
                    }

                    if (existingVariant.getMinimumDisplayQuantity() != null && !(existingVariant.getMinimumDisplayQuantity() >= 1 && existingVariant.getMinimumDisplayQuantity() <= 999)) {
                        product.errors.reject('productVariant.minimumDisplayQuantity.size.error', 'Minimum Display Quantity must be between 1 to 999.')
                    }

                    checkProductVariantForPackChanges(product, existingVariant, editedVariant, now, false)
                    checkProductVariantForLocationChanges(product, existingVariant, editedVariant)
                    checkProductVariantForBarcodeChanges(product, existingVariant, editedVariant, effectiveDate)
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

                        if (!newBarcode.validate()) {
                            handleBarcodeValidation(newBarcode, product)
                        }
                })

                productVariantList.add(newVariant)
            }
        }

        return productVariantList
    }

    private DateTime getEffectiveDate(def effectiveDate) {
        try {
            if (effectiveDate) {
                DateTimeFormatter dateFormatter = DateTimeFormat.forPattern("dd/MM/yyyy").withZone(DateTimeZone.UTC)
                DateTime selectedDate = DateTime.parse(effectiveDate, dateFormatter)
                return selectedDate.withTimeAtStartOfDay()
            }
        } catch (UnsupportedOperationException | IllegalArgumentException | NullPointerException ex) {
            log.println("exception parsing user provided date: ${ex.getMessage()}")
        }
        return DateTime.now(DateTimeZone.UTC).withTimeAtStartOfDay()
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

                 if (!barcode.validate()) {
                     handleBarcodeValidation(barcode, product)
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

                    if (!futureBarcode.validate()) {
                        handleBarcodeValidation(futureBarcode, product)
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

    private void checkPackForBarcodeChanges(def packs, def product, def existingPack, def editedPack, DateTime effectiveDate, int variantId) {
        editedPack.barcodez?.each { editedBarcode ->
            def existingDbBarcode = existingPack.barcodes?.find { existingBarcode ->
                existingBarcode.barcode == editedBarcode.barcode
            }
            def existingLocalBarcode = existingPack.barcodez?.find { existingBarcode ->
                existingBarcode.barcode == editedBarcode.barcode
            }

            if (!existingDbBarcode) {  // If no existing barcode then treat as newly added barcodes.
                Barcode barcode = new Barcode()
                barcode.pack = existingPack
                barcode.retailerId = springSecurityService.principal.retailerId
                barcode.barcode = editedBarcode.barcode
                barcode.effectiveDate = effectiveDate
                barcode.recordStatus = 'C'


                if (!isValidBarcode(barcode)) {
                    rejectProduct(product, barcode.barcode, 'product.barcodes.notUnique', 'Barcode {0} already exists on another SKU.')
                } else if (existingPack.supplier != null && !existingLocalBarcode && doesBarcodeExistForSupplier(barcode.barcode, existingPack.id, (int) existingPack.supplier.id, packs, variantId)) {
                    rejectProductByPackBarcode(product, barcode.barcode)
                } else if (doesBarcodeExistForOtherProductsInSupplier(barcode.barcode, (int) existingPack.supplier.id, existingPack.id, variantId)) {
                    rejectProductByProductVariantBarcode(product, barcode.barcode)
                } else if (!existingLocalBarcode) {
                    existingPack.barcodez.add(barcode)
                }
            } else if (existingDbBarcode.barcode != null && existingDbBarcode.barcode != editedBarcode.barcode) {
                // If barcode do exists change update existing values
                // Only update if user has changed barcode value or else skip
                //Mark current barcode to delete this will insert new mark delete entry to DB
                existingDbBarcode.delete = true
                existingDbBarcode.effectiveDeleteDate = effectiveDate
                //New effective date needed to be set as effective date of mark delete entry

                //Add new barcode to replacing existing
                Barcode futureBarcode = new Barcode()
                futureBarcode.pack = existingPack
                futureBarcode.retailerId = springSecurityService.principal.retailerId
                futureBarcode.barcode = editedBarcode.barcode
                futureBarcode.effectiveDate = effectiveDate
                futureBarcode.recordStatus = 'C'

                if (!isValidBarcode(futureBarcode)) {
                    product.errors.reject(
                            'product.barcodes.notUnique',
                            [futureBarcode.barcode] as Object[],
                            'Barcode {0} already exists on another SKU.')
                } else if (existingPack.supplier != null && doesBarcodeExistForSupplier(futureBarcode.barcode, existingPack.id, (int) existingPack.supplier.id, packs)) {
                    rejectProductByPackBarcode(product, futureBarcode.barcode)
                } else {
                    //Add mark deleted barcode and newly updated barcode to add into DB
                    existingPack.barcodez.add(existingDbBarcode)
                    existingPack.barcodez.add(futureBarcode)
                }
            }
        }

        // Mark any barcodes which no longer exist as deleted.
        existingPack.barcodes?.each { existingBarcode ->
            def editedBarcode = editedPack.barcodez?.find { editedBarcode -> editedBarcode.barcode == existingBarcode.barcode }

            if (!editedBarcode) {
                existingBarcode.delete = true
                existingBarcode.effectiveDeleteDate = effectiveDate
                //New effective date needed to be set as effective date of mark delete entry
                existingPack.barcodez.add(existingBarcode)
            } else if (doesBarcodeExistForSupplier(editedBarcode.barcode, existingPack.id, editedPack.supplier.id, packs, variantId)) {
                rejectProductByPackBarcode(product, editedBarcode.barcode)
            }
        }
    }

    private rejectProductByPackBarcode(def product, String barcode) {
        rejectProduct(product, barcode, 'pack.barcodes.notUnique', 'Barcode {0} already exists on another pack.')
    }

    private rejectProductByProductVariantBarcode(def product, String barcode) {
        rejectProduct(product, barcode, 'pack.barcodes.notUnique', 'Barcode {0} is used by another product.')
    }

    private boolean doesBarcodeExistForSupplier(String barcode, def packId, int supplierId, def packs, int variantId) {
        boolean existsInPacks = packs.any { pack ->
            if (pack.id != packId && pack.supplier.id == supplierId) {
                pack.barcodez.any { packBarcode ->
                    return packBarcode.barcode == barcode
                }
            }
        }

        def allMatchingBarcodesDeleted = checkBarcodesForSupplierDeleted(barcode, supplierId, packId, variantId)

        return existsInPacks && !allMatchingBarcodesDeleted
    }

    private boolean doesBarcodeExistForOtherProductsInSupplier(String barcode, int supplierId, int packId, int variantId) {
        def barcodes = productService.getBarcodesExists(barcode, supplierId, packId, variantId)
        Map<String, List<Barcode>> groupedBarcodes = barcodes.groupBy {[it.barcode, it.packId]}
        boolean existingBarcode = false

        // Need to check if more created records exist than deleted records for other barcodes (NOTE - this is subject to change as this isn't the original intention)
        for (Map.Entry<String, List<Barcode>> barcodeGrouping in groupedBarcodes) {
            if (barcodeGrouping.value.count({it.recordStatus =='C'}) > barcodeGrouping.value.count({it.recordStatus =='D'}))
            {
                existingBarcode = true
                break
            }
        }
        return existingBarcode
    }

    private boolean checkBarcodesForSupplierDeleted(String barcode, int supplierId, int packId, int variantId) {
        def barcodes = productService.getBarcodes(barcode, supplierId, packId, variantId, false)
        if (!barcodes.isEmpty()) {
            LinkedHashMap<Long, Integer> createDeleteMap = [:]
            for (barcodeEntry in barcodes) {
                if (!checkBarcodePackIsActive(barcodeEntry)) {
                    // Pack for this barcode is not active, we can ignore it
                    continue
                }

                if (createDeleteMap[barcodeEntry.packId] == null) {
                    createDeleteMap[barcodeEntry.packId] = 0
                }

                if (barcodeEntry.recordStatus == 'C') {
                    createDeleteMap[barcodeEntry.packId] = createDeleteMap[barcodeEntry.packId] + 1
                } else if (barcodeEntry.recordStatus == 'D') {
                    createDeleteMap[barcodeEntry.packId] = createDeleteMap[barcodeEntry.packId] - 1
                }
            }

            for (packEntry in createDeleteMap.values()) {
                if (packEntry > 0) {
                    return false
                }
            }
        }
        return true
    }

    private boolean checkBarcodePackIsActive(Barcode barcode) {
        if (barcode.pack != null) {
            if (barcode.pack.status != PackStatus.ACTIVE) {
                return false
            }

            //Check if pack is not active yet
            if (barcode.pack.effectiveDate != null && barcode.pack.effectiveDate > DateTime.now()) {
                return false
            }

            // Check if pack is no longer active
            if (barcode.pack.effectiveEndDate != null && barcode.pack.effectiveEndDate < DateTime.now()) {
                return false
            }
        }
        return true;
    }

    private static void rejectProduct(def product, String barcode, String  errorCode, String defaultMessage) {
        product.errors.reject(
                errorCode,
                new Object[] { barcode },
                defaultMessage)
    }

    private void checkProductVariantForPackChanges(def product, def existingVariant, def editedVariant, def now, boolean newVariant) {
        if (product.hasErrors()) {
            return
        }

        if (newVariant) {
            editedVariant.packs?.each { editedPack ->
                Pack newPack = new Pack()
                editedPack.barcodez.each { barcode ->
                    Barcode newBarcode = new Barcode()
                    newBarcode.retailerId = springSecurityService.principal.retailerId
                    newBarcode.effectiveDate = effectiveDate
                    newBarcode.pack = newPack
                    newBarcode.barcode = barcode
                    newBarcode.recordStatus = 'C'
                    newPack.barcodez.add(newBarcode)
                }
                updatePack(newPack, editedPack, now)
                existingVariant.addToPacks(newPack)
            }

            return
        }

        List<Integer> newPacksIds = new ArrayList<>()

        editedVariant.packs?.each { editedPack ->
            def existingPack = existingVariant.packs?.find { existingPack -> existingPack.id == editedPack.id }

            if (existingPack && packChanged(editedPack, existingPack)) {
                updatePack(existingPack, editedPack, now)
                checkPackForBarcodeChanges(editedVariant.packs, product, existingPack, editedPack, effectiveDate, (int) editedVariant.id)
            } else if (!existingPack) {
                Pack newPack = new Pack()
                editedPack.barcodez.each { barcode ->
                    Barcode newBarcode = new Barcode()
                    newBarcode.retailerId = springSecurityService.principal.retailerId
                    newBarcode.effectiveDate = effectiveDate
                    newBarcode.pack = newPack
                    newBarcode.barcode = barcode.barcode
                    newBarcode.recordStatus = 'C'
                    newPack.barcodez.add(newBarcode)
                }
                updatePack(newPack, editedPack, now)
                existingVariant.addToPacks(newPack)
                checkPackForBarcodeChanges(editedVariant.packs, product, newPack, editedPack, effectiveDate, (int) editedVariant.id)
                if (newPack.id > 0) {
                    // New pack id got set when retrieving barcodes from DB
                    newPacksIds.add(newPack.id)
                }
            } else {
                checkPackForBarcodeChanges(editedVariant.packs, product, existingPack, editedPack, effectiveDate, (int) editedVariant.id)
            }
        }
        def packsToRemove = []

        // Remove any packs which no longer exist.
        existingVariant.packs?.each { existingPack ->
            if (existingPack.isActive()) {
                // If the ID is not set then this must be a new pack added as part of this save, so don't remove it!
                if (existingPack.id > 0 && !newPacksIds.contains(existingPack.id)) {
                    def editedPack = editedVariant.packs?.find { editedPack -> editedPack.id == existingPack.id }

                    if (!editedPack) {
                        packsToRemove << existingPack
                    }
                }
            }
        }

        packsToRemove.each { packToRemove ->
            existingVariant.removeFromPacks(packToRemove)
        }
    }

    private void checkProductVariantForLocationChanges(def product, def existingVariant, def editedVariant) {
        if (product.hasErrors()) {
            return
        }

        def variantLocations = Location.findAllByStoreIdAndSkuAndDeleted(springSecurityService.principal.storeId, editedVariant.sku, false)
        def builder = new ProductHistoryBuilder(product.id, springSecurityService, effectiveDate)

        editedVariant.locationz?.each { editedLocation ->
            def existingLocation = variantLocations?.find { existingLocation -> existingLocation.id == editedLocation.id }

            if (existingLocation && existingLocation.id > 0 && locationChanged(editedLocation, existingLocation)) {
                Location newLocation = productService.deepCopyExistingLocation(existingLocation)
                compareLocationFields(builder, newLocation, editedLocation, ProductHistoryType.LOCATION_EDIT)
                productService.updateLocation(newLocation, editedLocation, editedVariant.sku)
                existingVariant.locationz.add(newLocation)
            } else if (!existingLocation) {
                Location newLocation = new Location()
                productService.updateLocation(newLocation, editedLocation, editedVariant.sku)
                existingVariant.locationz.add(newLocation)
                compareLocationFields(builder, new Location(), newLocation, ProductHistoryType.LOCATION_ADD)
            }
        }

        ArrayList<Location> deleteLocations = new ArrayList<>()
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
        if (product.validate()) {
            productService.saveProductHistories(builder.productHistories)
        }

        deleteLocations.each { location ->
            location.deleted = true
        }
    }

    /**
     * Manually check each field visible in the UI for detect the updated packs
     * @param newPack
     * @param existingPack
     * @return
     */
    def packChanged(def newPack, def existingPack) {
        return newPack.barcodez != existingPack.barcodez
                || newPack.supplier != existingPack.supplier
                || newPack.quantity != existingPack.quantity
                || newPack.price != existingPack.price
                || newPack.orderCode != existingPack.orderCode
                || newPack.recommendedRetailPrice != existingPack.recommendedRetailPrice
                || newPack.status != existingPack.status
                || newPack.maximumOrderQuantity != existingPack.maximumOrderQuantity
    }

    def locationChanged(def newLocation, def existingLocation) {
        return newLocation.aisle != existingLocation.aisle ||
                newLocation.bay != existingLocation.bay ||
                newLocation.shelf != existingLocation.shelf ||
                newLocation.position != existingLocation.position ||
                newLocation.location != existingLocation.location ||
                newLocation.shelfCapacity != existingLocation.shelfCapacity ||
                newLocation.minimumDisplayQuantity != existingLocation.minimumDisplayQuantity ||
                newLocation.locationHierarchy != existingLocation.locationHierarchy ||
                newLocation.locationDescription != existingLocation.locationDescription ||
                newLocation.locationNumber != existingLocation.locationNumber
    }

    private static void updatePack(def packToBeUpdated, def editedPack, def now) {
        packToBeUpdated.supplier = editedPack.supplier
        packToBeUpdated.quantity = editedPack.quantity
        packToBeUpdated.price = editedPack.price
        packToBeUpdated.orderCode = editedPack.orderCode
        packToBeUpdated.recommendedRetailPrice = editedPack.recommendedRetailPrice
        packToBeUpdated.effectiveDate = now
        packToBeUpdated.effectiveEndDate = editedPack.effectiveEndDate
        packToBeUpdated.status = editedPack.status
        packToBeUpdated.maximumOrderQuantity = editedPack.maximumOrderQuantity
        packToBeUpdated.allowSubstitutes = editedPack.allowSubstitutes
        packToBeUpdated.primaryCase = editedPack.primaryCase

        if (packToBeUpdated.hasProperty('updateDatetime')) {
            packToBeUpdated.updateDatetime = now
        }
    }

    private void updateLocation(def locationToBeUpdated, def editedLocation, def editedVariant) {
        def locationsType = springSecurityService.principal.retailer.config.locationsType
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

        builder.compare("stockSale", product.stockSale, editedProduct.stockSale)

        builder.compare("selDescription", product.selDescription, editedProduct.selDescription)
        builder.compare("selType", product.selType?.id, editedProduct.selType?.id)
        builder.compare("productImgUrl", product.productImgUrl, editedProduct.productImgUrl)

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
        builder.compare("allowsLoyaltyPointsCollection", product.restrictions.allowsLoyaltyPointsCollection, editedProduct.restrictions.allowsLoyaltyPointsCollection)

        builder.compare("vatCode", product.vatCode?.description, editedProduct.vatCode?.description)

        List<String> deletedBarcodes = new ArrayList<>()
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

        product?.variants?.stream()?.filter({ v -> v.effectiveDate == editedProduct.effectiveDate })?.each { existingVariants ->
            def editedVariant = editedProduct?.variants?.find { editedVariant -> editedVariant.id == existingVariants.id }
            if (!editedVariant) {
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
        if (oldVariant.costPrice != null && variant.costPrice != null) {
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
            if (editedBarcode == null || (editedBarcode.barcode == null && editedBarcode.recordStatus != 'D')) {
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

            if (!editedBarcode && !deletedBarcodes.contains(existingBarcode.barcode)) {
                //if edited barcode not exists means old barcode has been deleted
                deletedBarcodes.add(existingBarcode.barcode)
                builder.compare("barcode", existingBarcode.barcode, null)
            }
        }

        //---------------------------- Update history for pack fields --------------------------------//

        variant?.packs?.each { editedPack ->
            def existingPack = oldVariant?.packs?.find { existingPack -> existingPack != null && existingPack.id == editedPack.id }
            
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

    void comparePackFields(ProductHistoryBuilder builder, Pack oldPack, PackCommand pack) {
        builder.compare("packSupplier", oldPack.supplier, pack.supplier)
        builder.compare("packQuantity", oldPack.quantity, pack.quantity)
        builder.compare("packPrice", oldPack.price, pack.price)
        builder.compare("packOrderCode", oldPack.orderCode, pack.orderCode)
        builder.compare("packBarcodez", oldPack.barcodez, pack.barcodez)
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

    private void savePriceUpdates(def variants, Product product, List<PriceChangeCommand> priceChanges, DateTime effectiveDate) {
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

                    if (priceBand && priceChange.sku && priceChange.price >= 0) {
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
            productService.saveProductPrices(product, changedProductPrices, productHistories)

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

    private void saveRangeUpdates(Product product, HashSet<Integer> savedRanges) {
        def productRanges = RangeProduct.getExistingProductRanges(product.id)
        def ranges = Range.getExistingRetailerRanges(springSecurityService.principal.retailerId)
        def productHistories = []

        savedRanges?.each { Integer rangeId ->
            if (!productRanges.containsKey(rangeId)) {
                // Range doesn't exist for product, so add it.
                addRange(product, ranges.get(rangeId), productHistories)
            } else if (productRanges.get(rangeId).deleted) {
                // Range exists, but is soft deleted, un-delete it.
                undeleteRange(product, productRanges.get(rangeId), ranges.get(rangeId), productHistories)
            }
        }

        // Delete all ranges that have been unselected, except those already soft-deleted
        productRanges?.each {
            if (!savedRanges?.contains(it.key) && !it.value.deleted) {
                deleteRange(it.value, productHistories)
            }
        }

        if (productHistories.size() > 0) {
            productService.saveProductHistories(productHistories)
        }
    }

    private void addRange(Product product, Range range, ArrayList<ProductHistory> history) {
        RangeProduct rangeProduct = new RangeProduct(range: range, productId: product.id)
        history.add(handleProductRangeHistory(rangeProduct, true))
        productService.saveRangeProduct(rangeProduct)
        productService.sendProductUpdate([product], storeService.getStoresByRange(springSecurityService.principal.retailerId, range))
    }

    private void undeleteRange(Product product, RangeProduct rangeProduct, Range range, ArrayList<ProductHistory> history) {
        history.add(handleProductRangeHistory(rangeProduct, true))
        productService.undeleteRangeProduct(rangeProduct)
        productService.sendProductUpdate([product], storeService.getStoresByRange(springSecurityService.principal.retailerId, range))
    }

    private void deleteRange(RangeProduct rangeProduct, ArrayList<ProductHistory> history) {
        history.add(handleProductRangeHistory(rangeProduct, false))
        productService.deleteRangeProduct(rangeProduct)
    }

    private ProductHistory handleProductRangeHistory(RangeProduct rangeProduct, boolean isNew) {
        def now = DateTime.now(DateTimeZone.UTC)
        ProductHistoryType productHistoryType = isNew ? ProductHistoryType.PRODUCT_RANGE_ADD : ProductHistoryType.PRODUCT_RANGE_DELETE

        ProductHistory productHistory =
                new ProductHistory(retailerId: springSecurityService.principal.retailerId, productId: rangeProduct.getProductId(),
                        fromValue: null, toValue: rangeProduct.getRange() != null ? rangeProduct.getRange().getDescription() : -1,
                        productHistoryType: productHistoryType,
                        storeId: springSecurityService.principal.storeId, userId: springSecurityService.principal.id, usersName: springSecurityService.principal?.usersName,
                        effectiveDate: effectiveDate, updateDate: now)

        return productHistory
    }

    def ajaxSearchCategories(String searchTerm, boolean triggerOnCategoryChange, int level, int selectedCategoryId) {
        def searchResults = baseSearchCategories(searchTerm)
        boolean isSearch = searchTerm?.length() > 0
        render(template: "/product/categorySelectInputs", model: [categories: searchResults.aValue.unique(), level: isSearch ? level : 1, productCategoryList: searchResults.bValue, selectedCategoryId: selectedCategoryId, triggerOnCategoryChange: triggerOnCategoryChange, isSearch: isSearch])
    }

    def ajaxGetChildCategories(int categoryId, int level, int selectedCategoryId, boolean triggerOnCategoryChange) {
        def category = categoryService.getCategory(categoryId)

        render(template: "categorySelectInputs", model: [categories: category?.childCategories, level: level, selectedCategoryId: selectedCategoryId, triggerOnCategoryChange: triggerOnCategoryChange])
    }

    def ajaxAddVariant(AddVariantCommand cmd, boolean isNewVariant) {
        render(template: "addVariant", model: [variant: cmd, zeroPrice: cmd.zeroPrice, isEditMode: cmd.operationMode == OperationMode.EDIT.value, isNewVariant: isNewVariant])
    }

    def ajaxAddBarcode(int index, String selector) {
        render(template: "addBarcode", model: [index: index, selector: selector])
    }

    def ajaxSaveVariant(AddVariantCommand cmd) {
        def storeId = springSecurityService.principal.storeId

        render(template: "variant", model: [index: cmd.index, variant: cmd, barcodes: cmd.barcodez, storeId: storeId])
    }

    def ajaxAddTempLocation(AddVariantCommand cmd) {
        def storeId = springSecurityService.principal.storeId
        def locationsEnabled = [LocationsType.SIMPLE, LocationsType.ADVANCED].contains(springSecurityService.principal.retailer.config.locationsType)
        def locationsType = springSecurityService.principal.retailer.config.locationsType.name()

        render(template: "locationVariant", model: [index: cmd.index, locationsType: locationsType, variant: cmd, locationsEnabled: locationsEnabled, storeId: storeId])
    }

    def ajaxAddPrice(int index, long sku, boolean zeroPrice) {
        def priceBands = PriceBand.findAllByRetailerId(springSecurityService.principal.retailerId, [sort: "description", order: "asc"])

        render(template: "addPrice", model: [skuIndex: index, sku: sku, variant: null, priceBands: priceBands, zeroPrice: zeroPrice])
    }

    def ajaxSuppliers(SuppliersCommand cmd) {
        def defaultSuppliers = supplierService.getSuppliers()

        def suppliers = defaultSuppliers.findAll { it.symbolGroup == null }

        // Get IDs of already saved Packs
        ArrayList<Integer> existingPackIds = new ArrayList<Integer>()
        cmd.getPacks().each {
            if (Pack.findById(it.id) != null) {
                existingPackIds.add(it.id)
            }
        }

        render(template: "suppliers", model: [suppliers: suppliers, statuses: PackStatus.values(), variant: cmd, variantIndex: cmd.index, defaultSupplier: params.defaultSupplier, defaultSuppliers: defaultSuppliers, existingPackIds: existingPackIds, retailerId: springSecurityService.principal.retailerId])
    }

    def ajaxLocations(LocationsCommand cmd) {
        def locations = Location.findAllByStoreIdAndSkuAndDeleted(springSecurityService.principal.storeId, params.sku, false)

        render(template: "locations", model: [locations: locations, variant: cmd, variantIndex: cmd.index, locationsType: springSecurityService.principal.retailer.config.locationsType.name()])
    }

    def ajaxAddPack(int variantIndex, int packIndex, int productVariantId) {
        def suppliers = supplierService.getSuppliers()

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
            def defaultSuppliers = supplierService.getSuppliers()
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

    private static boolean checkChangeAffectsSel(boolean changeAffectsSel, Object left, Object right) {
        if (changeAffectsSel) {
            return true
        }
        if (left == right) {
            return false
        }
        return true
    }

    private static boolean isRestrictionsChanged(RestrictionsCommand first, Restrictions second) {
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
                first.receiptPrintForced != second.receiptPrintForced ||
                first.allowsLoyaltyPointsCollection != second.allowsLoyaltyPointsCollection
    }

    private static void copyRestrictions(RestrictionsCommand from, Restrictions to) {
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
        to.allowsLoyaltyPointsCollection = from.allowsLoyaltyPointsCollection
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
        List<ProductVariant> variants = new ArrayList<>()
        Map<Long, ProductVariant> existingVariants = new HashMap<>()

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

            List<Barcode> barcodes = new ArrayList<>()
            Map<String, Barcode> existingBarcodes = new HashMap<>()
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

    def handleBarcodeValidation(Barcode barcode, Product product) {
        if (barcode == null || StringUtils.isEmpty(barcode.getBarcode())) {
            product.errors.reject('product.barcodes.empty', 'Barcode is empty.')
        }
        if (barcode.hasErrors() && barcode.errors != null && barcode.errors.allErrors.size() > 0) {
            barcode.errors.allErrors
                    .each { FieldError error ->
                        final String field = error.field?.replace('profile.', '')
                        final String code = "barcode.$field.$error.code"
                        if (field == "barcode") {
                            if (code == "barcode.barcode.patternMismatch") {
                                product.errors.reject('product.barcodes.patternMismatch', [barcode.barcode] as Object[], 'Barcode {0} pattern is not valid for product barcode.')
                            } else {
                                product.errors.reject('product.barcodes.notUnique', [barcode.barcode] as Object[], 'Barcode {0} already exists on another SKU.')
                            }
                        } else {
                            product.errors.rejectValue(field, code)
                        }
                    }
        }
    }

    def isValidBarcode(Barcode barcode) {
        barcode == null || StringUtils.isEmpty(barcode.getBarcode()) || barcode.validate()
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

    def isValidSku(long sku) {
        def existingVariant = ProductVariant.findBySku(sku)
        return existingVariant == null
    }
}

class AddVariantCommand {
    def springSecurityService

    int index
    Integer id
    Integer storeId
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
    BigDecimal quantity
    BigDecimal price
    String orderCode
    BigDecimal recommendedRetailPrice
    DateTime effectiveDate
    DateTime effectiveEndDate
    PackStatus status
    Integer maximumOrderQuantity
    Boolean allowSubstitutes
    boolean primaryCase
    boolean isNewPack = false
    boolean isWeighted = false
    Integer productVariantId
    List<AddBarcodeCommand> barcodez

    static constraints = {
        importFrom Pack
        id nullable: true
        productVariantId nullable: true
        allowSubstitutes nullable: true
        primaryCase nullable: true
        supplier nullable: false, blank: false, validator: { supplier, pack ->
            if (!supplier.id) return ["addPackCommand.supplier.empty"]
        }
        price validator: {
            if (BigDecimal.ZERO == it) return ['addPackCommand.price.zero']
            if (it >= 10000) return ['addPackCommand.price.max']
        }
        quantity validator: { quantity, pack ->
            if (!pack.isWeighted && quantity.remainder(BigDecimal.ONE) != BigDecimal.ZERO) return ['addPackCommand.packQuantity.integer']
            if (quantity <= BigDecimal.ZERO) return ['addPackCommand.packQuantity.zero']
            if (quantity > BigDecimal.valueOf(Integer.MAX_VALUE)) return ['addPackCommand.packQuantity.maxValue']
        }
        recommendedRetailPrice validator: {
            if (BigDecimal.ZERO == it) return ['addPackCommand.recommendedRetailPrice.zero']
            if (it >= 10000) return ['addPackCommand.recommendedRetailPrice.max']
        }
        maximumOrderQuantity validator: {
            if (it >= 100000) return ['addPackCommand.maxOrderQuantity.maxValue']
        }
    }

    // pack is active if the current datetime is after the pack effectiveDate and before the pack effectiveEndDate
    boolean isActive() {
        DateTime now = DateTime.now(DateTimeZone.UTC)
        return !supplier.deleted && (effectiveDate == null || now > effectiveDate) && (effectiveEndDate == null || now < effectiveEndDate)
    }
}

class LocationsCommand {
    int index
    int productVariantId
    List<AddLocationCommand> locationz
    Boolean hasErrors = Boolean.FALSE
    String locationsType
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
    Integer locationHierarchy
    String locationDescription
    String locationNumber
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
    Integer locationHierarchy
    String locationDescription
    String locationNumber
}

class SupplierCommand {
    int id
    String name
    Integer symbolGroupId
    boolean deleted
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
    StockSale stockSale
    String selDescription
    SelType selType
    String productImgUrl

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
    Boolean allowsLoyaltyPointsCollection

    static constraints = {
        importFrom Restrictions
    }
}

class ProductVariantCommand {
    int id
    Integer storeId
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
    BigDecimal quantity
    BigDecimal price
    String orderCode
    List<AddBarcodeCommand> barcodez
    BigDecimal recommendedRetailPrice
    DateTime effectiveDate
    DateTime effectiveEndDate
    PackStatus status
    Integer maximumOrderQuantity
    boolean allowSubstitutes
    boolean primaryCase

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
    Integer packId
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
        ProductCommand productCommand = new ProductCommand()
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

        Restrictions categoryRestrictions = category.restrictions
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

        String priceBands = this.getPriceBands()
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