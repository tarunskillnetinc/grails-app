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
import org.springframework.validation.BindingResult
import uk.co.wonderlane.wlpos.entities.SyncMessage
import uk.co.wonderlane.wlpos.enums.PackStatus
import uk.co.wonderlane.wlpos.enums.ProductStatus
import uk.co.wonderlane.wlpos.enums.SyncMessageType
import uk.co.wonderlane.wlpos.supplier.Supplier

import java.lang.reflect.Type

class ProductController {

    def springSecurityService
    def productService

    def index() {
        render(view: "index", model: [products: null, storeId: springSecurityService.principal.storeId, page: 1, pageCount: 0, pageNumbers: null])
    }

    def show(int id) {
        def product = productService.getProduct(id)

        render(view: "add", model: [product: product,
                                            storeId: springSecurityService.principal.storeId,
                                            statusValues: ProductStatus.values(),
                                            categoryValues: Category.findAllByRetailerId(springSecurityService.principal.retailerId),
                                            vatValues: VatCode.findAllByRetailerId(springSecurityService.principal.retailerId),
                                            navlink: "details"])
    }

    def add() {
        def product = new Product()
        def now = DateTime.now(DateTimeZone.UTC)

        product.restrictions = new Restrictions()

        ProductVariant productVariant = new ProductVariant(storeId: springSecurityService.principal.storeId, effectiveDate: now)

        Barcode barcode = new Barcode(effectiveDate: now)

        productVariant.addToBarcodes(barcode)
        product.addToVariants(productVariant)

        render(view: "add", model: [product: product,
                                    storeId: springSecurityService.principal.storeId,
                                    statusValues: ProductStatus.values(),
                                    categoryValues: Category.findAllByRetailerId(springSecurityService.principal.retailerId),
                                    vatValues: VatCode.findAllByRetailerId(springSecurityService.principal.retailerId),
                                    isNewProduct: true])
    }

    def search() {
        session.PRODUCT_SEARCH_TERM = params.searchTerm

        def products = productService.searchProducts(params.searchTerm, params.searchBy, 50, 0, "id", "asc")

//        int totalResults = products[-1].getId()

//        if (products.size() > 0) {
//            products.removeLast()
//        }

        render(template: "/product/productSearchResults", model: [ products: products, totalResults: products.totalCount, storeId: springSecurityService.principal.storeId ])
    }

    def maintenanceSearch() {
        session.PRODUCT_SEARCH_TERM = params.searchTerm

        def products = productService.searchProducts(params.searchTerm, params.searchBy, params.max ? Integer.parseInt(params.max) : 50, params.offset ? Integer.parseInt(params.offset) : 0, "id", "asc")

//        int totalResults = products[-1].getId()
//
//        if (products.size() > 0) {
//            products.removeLast()
//        }

        render(template: "/product/maintenanceSearchResults", model: [products: products, storeId: springSecurityService.principal.storeId, searchTerm: params.searchTerm, searchBy: params.searchBy, max: params.max ?: 50, offset: params.offset, totalResults: products.totalCount])
    }

    def save() {
        def product

        boolean newProduct

        // TODO check existing product, currently always newProduct
        if (product) {
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

                it.barcodes?.each { barcode ->
                    barcode.effectiveDate = barcode.effectiveDate ?: now
                }

                it.packs?.each { pack ->
                    pack.effectiveDate = pack.effectiveDate ?: now
                }
            }
        } else {
            def baseProduct = productService.getProduct(id)

            if (params.retailPrice) {
                baseProduct.variants.sort { it.effectiveDate }.reverse().find {
                    it.storeId == springSecurityService.principal.storeId && it.effectiveDate <= now
                }.retailPrice = new BigDecimal(params.retailPrice)
            }

            if (params.costPrice) {
                baseProduct.variants.sort { it.effectiveDate }.reverse().find {
                    it.storeId == springSecurityService.principal.storeId && it.effectiveDate <= now
                }.costPrice = new BigDecimal(params.costPrice)
            }

            baseProduct.properties = product.properties as BindingResult
            product = baseProduct
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
//                    if ((variant.itemCode == null || variant.itemCode?.isEmpty() || variant.itemCode?.isAllWhitespace()) && (!product.itemCode?.isEmpty() || !product.itemCode?.isAllWhitespace())) {
//                        variant.itemCode = product.itemCode
//                    }
//                }
//            }
//        }

        if (product.validate()) {
            productService.saveProduct(product)
            flash.message = "Product saved successfully"
        }

        if (!product.hasErrors()) {
//            if (newProduct) {
//                product.productDatas.get(0).id = product.id
//                productService.saveProductData(product.productDatas.get(0))
//            } else {
//                product.productDatas.each {
//                    productService.saveProductData(it)
//                }
//            }

//            productService.populateCurrentProductData(product)

//            BackOfficeRabbitService rabbitService = new BackOfficeRabbitService(grailsApplication.config.getProperty('rabbitmq.host'), Integer.parseInt(grailsApplication.config.getProperty('rabbitmq.port')), grailsApplication.config.getProperty('rabbitmq.username'), grailsApplication.config.getProperty('rabbitmq.password'))
//            rabbitService.init()

//            if (!rabbitService.isOpen()) {
//                throw new Exception("Rabbit MQ not available")
//            }

//            SyncMessage syncMessage = new SyncMessage(SyncMessageType.PRODUCT, springSecurityService.principal.retailerId, springSecurityService.principal.storeId, 0)
//            syncMessage.setInsert(true)
//            List<uk.co.wonderlane.wlpos.entities.Product> products = new ArrayList<uk.co.wonderlane.wlpos.entities.Product>()
//            products.add(product.getProduct(springSecurityService.principal.storeId))
//            syncMessage.setProducts(products)

//            Gson gson = new GsonBuilder()
//                    .registerTypeAdapter(DateTime.class, new JsonSerializer<DateTime>() {
//                        @Override
//                        public JsonElement serialize(DateTime json, Type typeOfSrc, JsonSerializationContext context) {
//                            return new JsonPrimitive(ISODateTimeFormat.dateTime().print(json));
//                        }
//                    })
//                    .registerTypeAdapter(DateTime.class, new JsonDeserializer<DateTime>() {
//                        @Override
//                        public DateTime deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
//                            return ISODateTimeFormat.dateTime().parseDateTime(json.getAsString()).withZone(DateTimeZone.UTC);
//                        }
//                    }).create()

//            rabbitService.sendExchangeMessage(String.format("R%d_S%d", syncMessage.getRetailerId(), syncMessage.getStoreId()), gson.toJson(syncMessage))
        }

        if (!product.hasErrors()) {
            redirect(action: "index")
        } else {
            render(view: "add", model: [product       : product,
                                        storeId       : springSecurityService.principal.storeId,
                                        statusValues  : ProductStatus.values(),
                                        categoryValues: Category.findAllByRetailerId(springSecurityService.principal.retailerId),
                                        vatValues     : VatCode.findAllByRetailerId(springSecurityService.principal.retailerId)])
        }
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
}

class AddVariantCommand {
    int index
    Integer id
    String itemCode
    BigDecimal retailPrice
    BigDecimal costPrice
    String size
    String colour
    DateTime effectiveDate
    List<AddBarcodeCommand> barcodes
    List<AddPackCommand> packs
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