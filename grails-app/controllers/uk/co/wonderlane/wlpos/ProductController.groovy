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
import uk.co.wonderlane.wlpos.enums.ProductStatus
import uk.co.wonderlane.wlpos.enums.SyncMessageType

import java.lang.reflect.Type

class ProductController {

    def springSecurityService
    def productService

    def index() {
        render( view: "index", model: [products: null, storeId: springSecurityService.principal.storeId, page: 1, pageCount: 0, pageNumbers: null])
    }

    def maintenance() {
        def product = productService.getProduct(Integer.parseInt(params.productId))



        render(view: "maintenance", model: [product: product,
                                            storeId: springSecurityService.principal.storeId,
                                            statusValues: ProductStatus.values(),
                                            categoryValues: Category.findAllByRetailerId(springSecurityService.principal.retailerId),
                                            vatValues: VatCode.findAllByRetailerId(springSecurityService.principal.retailerId),
                                            navlink: "details"])
    }

    def add() {
        def product = new Product()

        product.restrictions = new Restrictions()

        ProductData productData = new ProductData(storeId: springSecurityService.principal.storeId, effectiveDate: new Date())
        product.addToProductDatas(productData)

        render(view: "maintenance", model: [product: product,
                                            storeId: springSecurityService.principal.storeId,
                                            statusValues: ProductStatus.values(),
                                            categoryValues: Category.findAllByRetailerId(springSecurityService.principal.retailerId),
                                            vatValues: VatCode.findAllByRetailerId(springSecurityService.principal.retailerId),
                                            navlink: "details",
                                            isNewProduct: true])
    }

    def search() {
        def products = productService.searchProductsNew(params.searchTerm, params.searchBy, 50, 0, "id", "asc")

        int totalResults = products[-1].getId()

        if (products.size() > 0) {
            products.removeLast()
        }

        render(template: "/product/productSearchResults", model: [ products: products, totalResults: totalResults, storeId: springSecurityService.principal.storeId ])
    }

    def maintenanceSearch() {
        def products = productService.searchProductsNew(params.searchTerm, params.searchBy, params.max ? Integer.parseInt(params.max) : 50, params.offset ? Integer.parseInt(params.offset) : 0, "id", "asc")

        int totalResults = products[-1].getId()

        if (products.size() > 0) {
            products.removeLast()
        }

        render(template: "/product/maintenanceSearchResults", model: [products: products, storeId: springSecurityService.principal.storeId, searchTerm: params.searchTerm, searchBy: params.searchBy, max: params.max ?: 50, offset: params.offset, totalResults: totalResults])
    }

    def save(Product product) {
        boolean newProduct
        if (product) {
            newProduct = false
        }  else {
            newProduct = true
        }

        if (newProduct) {
            product = new Product()
            product.retailerId = springSecurityService.principal.retailerId

            ProductData productData = new ProductData(storeId: springSecurityService.principal.storeId,
                                                        effectiveDate: new Date(),
                                                        createdDatetime: new Date(),
                                                        createdUserId: 0,
                                                        updateDatetime: new Date(),
                                                        updatedUserId: 0)

            product.addToProductDatas(productData)

            bindData(product, params)

            product.productDatas.get(0).retailPrice = (params.retailPrice.equals("") ? null : new BigDecimal(params.retailPrice))
            product.productDatas.get(0).costPrice = (params.costPrice.equals("") ? null : new BigDecimal(params.costPrice))
            product.retailerProductId = 0
        } else {
            Product baseProduct = Product.findById(product.id)

            if (params.retailPrice) {
                baseProduct.productDatas.sort { it.effectiveDate }.reverse().find {
                    it.storeId == springSecurityService.principal.storeId && it.effectiveDate <= new Date()
                }.retailPrice = new BigDecimal(params.retailPrice)
            }

            if (params.costPrice) {
                baseProduct.productDatas.sort { it.effectiveDate }.reverse().find {
                    it.storeId == springSecurityService.principal.storeId && it.effectiveDate <= new Date()
                }.costPrice = new BigDecimal(params.costPrice)
            }

            baseProduct.properties = product.properties as BindingResult
            product = baseProduct
        }

        if (product.variants == null || product.variants.size() == 0) {
            ProductVariant variant = new ProductVariant()
            variant.itemCode = product.itemCode ?: ""
            variant.storeId = springSecurityService.principal.storeId

            product.addToVariants(variant)
        }

        for (ProductVariant variant : product.variants) {
            if (variant.storeId == springSecurityService.principal.storeId) {
                List<Barcode> barcodes = variant.barcodes.collect()
                if (variant.delete) {
                    for (Barcode barcode : barcodes) {
                        variant.removeFromBarcodes(barcode)
                    }

                    product.removeFromVariants(variant)
                } else {
                    for (Barcode barcode : barcodes) {
                        if (barcode.delete) {
                            variant.removeFromBarcodes(barcode)
                        }
                    }

                    if ((variant.itemCode == null || variant.itemCode?.isEmpty() || variant.itemCode?.isAllWhitespace()) && (!product.itemCode?.isEmpty() || !product.itemCode?.isAllWhitespace())) {
                        variant.itemCode = product.itemCode
                    }
                }
            }
        }

        if (product.validate()) {
            productService.saveProduct(product)
            flash.message = "Product saved successfully"
        }

        if (!product.hasErrors()) {
            if (newProduct) {
                product.productDatas.get(0).id = product.id
                productService.saveProductData(product.productDatas.get(0))
            } else {
                product.productDatas.each {
                    productService.saveProductData(it)
                }
            }

            productService.populateCurrentProductData(product)

            BackOfficeRabbitService rabbitService = new BackOfficeRabbitService(grailsApplication.config.getProperty('rabbitmq.host'), Integer.parseInt(grailsApplication.config.getProperty('rabbitmq.port')), grailsApplication.config.getProperty('rabbitmq.username'), grailsApplication.config.getProperty('rabbitmq.password'))
            rabbitService.init()

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
            render(view: "maintenance", model: [product: product,
                                                storeId: springSecurityService.principal.storeId,
                                                statusValues: ProductStatus.values(),
                                                categoryValues: Category.findAllByRetailerId(springSecurityService.principal.retailerId),
                                                vatValues: VatCode.findAllByRetailerId(springSecurityService.principal.retailerId),
                                                navlink: "details"])
        }
    }

    def addVariant(Product product) {
        boolean newProduct
        if (product) {
            newProduct = false
        }  else {
            newProduct = true
        }

        if (newProduct) {
            product = new Product()
            ProductData productData = new ProductData(storeId: springSecurityService.principal.storeId, effectiveDate: new Date())
            product.addToProductDatas(productData)
            bindData(product, params)
            product.productDatas.get(0).retailPrice = (params.retailPrice.equals("") ? null : new BigDecimal(params.retailPrice))
            product.productDatas.get(0).costPrice = (params.costPrice.equals("") ? null : new BigDecimal(params.costPrice))
        } else {
            Product baseProduct = Product.findById(product.id)

            baseProduct.productDatas.sort { it.effectiveDate }.reverse().find {
                it.storeId == springSecurityService.principal.storeId && it.effectiveDate <= new Date()
            }.retailPrice = new BigDecimal(params.retailPrice)
            baseProduct.productDatas.sort { it.effectiveDate }.reverse().find {
                it.storeId == springSecurityService.principal.storeId && it.effectiveDate <= new Date()
            }.costPrice = new BigDecimal(params.costPrice)

            baseProduct.properties = product.properties as BindingResult
            product = baseProduct
        }

        // try to reuse a deleted variant if one exists rather than creating a new one
        List<ProductVariant> variants = product.variants.collect()
        def found = false
        for (int i = 0; i < variants.size(); i++) {
            if (variants.get(i).delete) {
                product.variants.get(i).delete = false
                product.variants.get(i).itemCode = ""
                product.variants.get(i).size = ""
                product.variants.get(i).colour = ""
                product.variants.get(i).balanceOnHand = 0
                product.variants.get(i).balanceOnOrder = 0
                List<Barcode> barcodes = product.variants.get(i).barcodes.collect()
                if (barcodes.size() > 1) {
                    for (int j = barcodes.size() - 1; j > 0; j--) {
                        barcodes.get(j).delete = true
                    }
                }
                barcodes.get(0).delete = false
                barcodes.get(0).barcode = ""
                found = true
                break
            }
        }

        if (!found) {
            ProductVariant productVariant = new ProductVariant()
            productVariant.storeId = springSecurityService.principal.storeId
//            Barcode barcode = new Barcode()
//            barcode.effectiveDate = new Date()
//            barcode.recordStatus = 'C'
//            productVariant.addToBarcodes(barcode)
            product.addToVariants(productVariant)
        }

        render(view: "maintenance", productId: product.id, model: [product: product,
                                            storeId: springSecurityService.principal.storeId,
                                            statusValues: ProductStatus.values(),
                                            categoryValues: Category.findAllByRetailerId(springSecurityService.principal.retailerId),
                                            vatValues: VatCode.findAllByRetailerId(springSecurityService.principal.retailerId),
                                            navlink: "variants"])
    }

    def deleteVariants(Product product) {
        boolean newProduct
        if (product) {
            newProduct = false
        }  else {
            newProduct = true
        }

        if (newProduct) {
            product = new Product()
            ProductData productData = new ProductData(storeId: springSecurityService.principal.storeId, effectiveDate: new Date())
            product.addToProductDatas(productData)
            bindData(product, params)
            product.productDatas.get(0).retailPrice = (params.retailPrice.equals("") ? null : new BigDecimal(params.retailPrice))
            product.productDatas.get(0).costPrice = (params.costPrice.equals("") ? null : new BigDecimal(params.costPrice))
        } else {
            Product baseProduct = Product.findById(product.id)

            baseProduct.productDatas.sort { it.effectiveDate }.reverse().find {
                it.storeId == springSecurityService.principal.storeId && it.effectiveDate <= new Date()
            }.retailPrice = new BigDecimal(params.retailPrice)
            baseProduct.productDatas.sort { it.effectiveDate }.reverse().find {
                it.storeId == springSecurityService.principal.storeId && it.effectiveDate <= new Date()
            }.costPrice = new BigDecimal(params.costPrice)

            baseProduct.properties = product.properties as BindingResult
            product = baseProduct
        }

        List<ProductVariant> variants = product.variants.collect()
        List<Integer> removals = new ArrayList<>()

        for (int i = 0 ; i < variants.size() ; i++) {
            if (Boolean.parseBoolean(params."variants[${i}].selected")) {
                removals.add(i)
            }
        }

        for (Integer i in removals) {
            if (product.variants.get(i).id != 0) {
                product.variants.get(i).delete = true
            } else {
                product.variants.removeAt(i)
            }
        }

        render(view: "maintenance", productId: product.id, model: [product: product,
                                            storeId: springSecurityService.principal.storeId,
                                            statusValues: ProductStatus.values(),
                                            categoryValues: Category.findAllByRetailerId(springSecurityService.principal.retailerId),
                                            vatValues: VatCode.findAllByRetailerId(springSecurityService.principal.retailerId),
                                            navlink: "variants"])
    }

    def addBarcode(Product product) {
        boolean newProduct
        if(product) {
            newProduct = false
        }  else {
            newProduct = true
        }

        if (newProduct) {
            product = new Product()
            ProductData productData = new ProductData(storeId: springSecurityService.principal.storeId, effectiveDate: new Date())
            product.addToProductDatas(productData)
            bindData(product, params)
            product.productDatas.get(0).retailPrice = (params.retailPrice.equals("") ? null : new BigDecimal(params.retailPrice))
            product.productDatas.get(0).costPrice = (params.costPrice.equals("") ? null : new BigDecimal(params.costPrice))
        } else {
            Product baseProduct = Product.findById(product.id)

            if (params.retailPrice) {
                baseProduct.productDatas.sort { it.effectiveDate }.reverse().find {
                    it.storeId == springSecurityService.principal.storeId && it.effectiveDate <= new Date()
                }.retailPrice = new BigDecimal(params.retailPrice)
            }

            if (params.costPrice) {
                baseProduct.productDatas.sort { it.effectiveDate }.reverse().find {
                    it.storeId == springSecurityService.principal.storeId && it.effectiveDate <= new Date()
                }.costPrice = new BigDecimal(params.costPrice)
            }

            baseProduct.properties = product.properties as BindingResult
            product = baseProduct
        }

        // try to reuse a deleted barcode if one exists rather than creating a new one
        List<Barcode> barcodes = product.variants.get(Integer.parseInt(params.relevantVariant)).barcodes.collect()
        def found = false
        for (int i = 0; i < barcodes.size(); i++) {
            if (barcodes.get(i).delete) {
                product.variants.get(Integer.parseInt(params.relevantVariant)).barcodes.get(i).delete = false
                product.variants.get(Integer.parseInt(params.relevantVariant)).barcodes.get(i).barcode = ""
                found = true
                break
            }
        }

        if (!found) {
            Barcode barcode = new Barcode()
            barcode.effectiveDate = new Date()
            barcode.recordStatus = 'C'
            product.variants.get(Integer.parseInt(params.relevantVariant)).addToBarcodes(barcode)
        }

        render(view: "maintenance", model: [product: product,
                                            storeId: springSecurityService.principal.storeId,
                                            statusValues: ProductStatus.values(),
                                            categoryValues: Category.findAllByRetailerId(springSecurityService.principal.retailerId),
                                            vatValues: VatCode.findAllByRetailerId(springSecurityService.principal.retailerId),
                                            navlink: "variants"])
    }

    def deleteBarcodes(Product product) {
        boolean newProduct
        if (product) {
            newProduct = false
        }  else {
            newProduct = true
        }

        if (newProduct) {
            product = new Product()
            ProductData productData = new ProductData(storeId: springSecurityService.principal.storeId, effectiveDate: new Date())
            product.addToProductDatas(productData)
            bindData(product, params)
            product.productDatas.get(0).retailPrice = (params.retailPrice.equals("") ? null : new BigDecimal(params.retailPrice))
            product.productDatas.get(0).costPrice = (params.costPrice.equals("") ? null : new BigDecimal(params.costPrice))
        } else {
            Product baseProduct = Product.findById(product.id)

            baseProduct.productDatas.sort { it.effectiveDate }.reverse().find {
                it.storeId == springSecurityService.principal.storeId && it.effectiveDate <= new Date()
            }.retailPrice = new BigDecimal(params.retailPrice)
            baseProduct.productDatas.sort { it.effectiveDate }.reverse().find {
                it.storeId == springSecurityService.principal.storeId && it.effectiveDate <= new Date()
            }.costPrice = new BigDecimal(params.costPrice)

            baseProduct.properties = product.properties as BindingResult
            product = baseProduct
        }

        for (int i = 0 ; i < product.variants.size() ; i++) {
            Iterator<Barcode> barcodeIterator = product.variants[i].barcodes.listIterator()

            int j = 0
            while (barcodeIterator.hasNext()) {
                Barcode barcode = barcodeIterator.next()

                if (Boolean.parseBoolean(params."variants[${i}].barcodes[${j}].selected")) {
                    if (barcode.id != 0) {
                        barcode.delete = true
                    } else {
                        barcodeIterator.remove()
                    }
                }

                j++
            }
        }

        render(view: "maintenance", model: [product: product,
                                            storeId: springSecurityService.principal.storeId,
                                            statusValues: ProductStatus.values(),
                                            categoryValues: Category.findAllByRetailerId(springSecurityService.principal.retailerId),
                                            vatValues: VatCode.findAllByRetailerId(springSecurityService.principal.retailerId),
                                            navlink: "variants"])

    }
}