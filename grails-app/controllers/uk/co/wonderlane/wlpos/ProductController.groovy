package uk.co.wonderlane.wlpos

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonParseException
import com.google.gson.JsonPrimitive
import com.google.gson.JsonSerializationContext
import com.google.gson.JsonSerializer
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import org.joda.time.format.ISODateTimeFormat
import uk.co.wonderlane.wlpos.entities.SyncMessage
import uk.co.wonderlane.wlpos.enums.PackStatus
import uk.co.wonderlane.wlpos.enums.ProductStatus
import uk.co.wonderlane.wlpos.enums.SyncMessageType
import uk.co.wonderlane.wlpos.supplier.Supplier

import java.lang.reflect.Type

class ProductController {

    def springSecurityService

    def productService
    def categoryService
    def tagService
    def rabbitService

    def index() {
        render(view: "index", model: [products: null, storeId: springSecurityService.principal.storeId, page: 1, pageCount: 0, pageNumbers: null])
    }

    def show(int id) {
        def product = productService.getProduct(id)

        render(view: "add", model: [product: product,
                                            storeId: springSecurityService.principal.storeId,
                                            statusValues: ProductStatus.values(),
                                            categoryValues: categoryService.getFullCategoryHierarchy(),
                                            vatValues: VatCode.findAllByRetailerId(springSecurityService.principal.retailerId),
                                            navlink: "details"])
    }

    def add() {
        render(view: "add", model: [storeId: springSecurityService.principal.storeId,
                                    statusValues: ProductStatus.values(),
                                    categoryValues: categoryService.getFullCategoryHierarchy(),
                                    vatValues: VatCode.findAllByRetailerId(springSecurityService.principal.retailerId),
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

    def prices() {
        def categories = categoryService.getFullCategoryHierarchy()
        def tags = tagService.getTags()
        def priceBands = PriceBand.findAllByRetailerId(springSecurityService.principal.retailerId)

        [categories: categories, tags: tags, priceBands: priceBands]
    }

    def pricesSearch() {
        String searchTerm = params.searchTerm
        Integer categoryId = params.category ? Integer.parseInt(params.category) : null
        Integer tagId = params.tag ? Integer.parseInt(params.tag) : null

        def productPrices = productService.searchProductPrices(searchTerm, categoryId, tagId)
        def priceBands = PriceBand.findAllByRetailerId(springSecurityService.principal.retailerId)

        render(template: "/product/pricesSearchResults", model: [productPrices: productPrices, priceBands: priceBands])
    }

    def ajaxSavePriceChanges(SavePriceChangesCommand cmd) {
        def now = DateTime.now(DateTimeZone.UTC)
        def priceBands = PriceBand.findAllByRetailerId(springSecurityService.principal.retailerId)

        def productPrices = []
        def priceUpdates = [:]

        Gson gson = new GsonBuilder()
                .registerTypeAdapter(DateTime.class, new JsonSerializer<DateTime>() {
                    @Override
                    public JsonElement serialize(DateTime json, Type typeOfSrc, JsonSerializationContext context) {
                        return new JsonPrimitive(ISODateTimeFormat.dateTime().print(json));
                    }
                })
                .registerTypeAdapter(DateTime.class, new JsonDeserializer<DateTime>() {
                    @Override
                    public DateTime deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
                        return ISODateTimeFormat.dateTime().parseDateTime(json.getAsString()).withZone(DateTimeZone.UTC);
                    }
                }).create()

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
            SyncMessage syncMessage = new SyncMessage(SyncMessageType.PRICE_CHANGE, springSecurityService.principal.retailerId, springSecurityService.principal.storeId, 0)
            syncMessage.setInsert(true)
            syncMessage.setProductPrices(priceChanges)

            def stores = StoreSettings.findAllByRetailerIdAndPriceBand(springSecurityService.principal.retailerId, priceBands.find { it.id == priceBandId })

            stores?.each { store ->
                syncMessage.setStoreId(store.storeId)

                rabbitService.declareExchange(String.format("R%d_S%d", syncMessage.getRetailerId(), syncMessage.getStoreId()))
                rabbitService.sendExchangeMessage(String.format("R%d_S%d", syncMessage.getRetailerId(), syncMessage.getStoreId()), gson.toJson(syncMessage))
            }
        }

        render "OK"
    }

    def save() {
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

            product.variants?.each {
                it.storeId = springSecurityService.principal.storeId
                it.effectiveDate = now
                it.createdUserId = springSecurityService.principal.id
                it.updatedUserId = springSecurityService.principal.id

                // TODO Won't work anymore.
//                it.barcodes?.each { barcode ->
//                    barcode.effectiveDate = barcode.effectiveDate ?: now
//                }

                it.packs?.each { pack ->
                    pack.effectiveDate = pack.effectiveDate ?: now
                }
            }
        } else {
            // TODO This all needs finishing.
            // TODO We should introduce an effective date entry.

            product = productService.getProduct(Integer.parseInt(params.id))

            // TODO ProductCommand and all of the sub objects need to be command objects as well.
            def editedProduct = new ProductCommand()
            bindData(editedProduct, params)

            editedProduct.variants?.each {editedVariant ->
                product.variants?.find {existingVariant -> existingVariant.id == editedVariant.id }?.retailPrice = editedVariant.retailPrice
            }

            copyRestrictions(editedProduct.restrictions, product.restrictions)

            product.vatCode = editedProduct.vatCode
            product.vatPercentageOverride = editedProduct.vatPercentageOverride
            product.discreetMessage = editedProduct.discreetMessage
            product.status = editedProduct.status
            product.weightedItem = editedProduct.weightedItem
            product.openPrice = editedProduct.openPrice
            product.zeroPrice = editedProduct.zeroPrice
            // TODO Handle saving over the rest of the properties in a product, also handle adding new variants and such.
        }

//        for (ProductVariant variant : product.variants) {
//            if (variant.storeId == springSecurityService.principal.storeId) {
//                List<Barcode> barcodes = variant.barcodes.collect()
//                if (variant.delete) {
//                    for (Barcode barcode : barcodes) {
//                        variant.removeFromBarcodes(barcode)
//                    }
//
//                    product.removeFromVariants(variant)
//                } else {
//                    for (Barcode barcode : barcodes) {
//                        if (barcode.delete) {
//                            variant.removeFromBarcodes(barcode)
//                        }
//                    }
//
//                    if ((variant.sku == null || variant.sku?.isEmpty() || variant.sku?.isAllWhitespace()) && (!product.itemCode?.isEmpty() || !product.itemCode?.isAllWhitespace())) {
//                        variant.sku = product.itemCode
//                    }
//                }
//            }
//        }

        if (product.validate()) {
            productService.saveProduct(product)
            flash.message = "Product saved successfully"
        }

        if (!product.hasErrors()) {
            if (!rabbitService.isOpen()) {
                throw new Exception("Rabbit MQ not available")
            }

            SyncMessage syncMessage = new SyncMessage(SyncMessageType.PRODUCT, springSecurityService.principal.retailerId, springSecurityService.principal.storeId, 0)
            syncMessage.setInsert(true)

            List<uk.co.wonderlane.wlpos.entities.Product> products = new ArrayList<uk.co.wonderlane.wlpos.entities.Product>()
            products.add(product.getProduct(springSecurityService.principal.storeId))
            syncMessage.setProducts(products)

            Gson gson = new GsonBuilder()
                    .registerTypeAdapter(DateTime.class, new JsonSerializer<DateTime>() {
                        @Override
                        public JsonElement serialize(DateTime json, Type typeOfSrc, JsonSerializationContext context) {
                            return new JsonPrimitive(ISODateTimeFormat.dateTime().print(json));
                        }
                    })
                    .registerTypeAdapter(DateTime.class, new JsonDeserializer<DateTime>() {
                        @Override
                        public DateTime deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
                            return ISODateTimeFormat.dateTime().parseDateTime(json.getAsString()).withZone(DateTimeZone.UTC);
                        }
                    }).create()

            rabbitService.sendExchangeMessage(String.format("R%d_S%d", syncMessage.getRetailerId(), syncMessage.getStoreId()), gson.toJson(syncMessage))
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
    String size
    String colour
    DateTime effectiveDate
    List<AddBarcodeCommand> barcodes
    List<AddPackCommand> packs

    BigDecimal getCurrentPrice() {
        if (retailPrice != null) {
            return retailPrice
        } else {
            def storeSettings = StoreSettings.findByStoreId(springSecurityService.principal.storeId)
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