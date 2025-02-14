package uk.co.wonderlane.wlpos

import grails.converters.JSON
import groovy.json.JsonSlurper
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import org.joda.time.format.DateTimeFormat
import org.joda.time.format.DateTimeFormatter
import org.springframework.security.access.annotation.Secured
import uk.co.wonderlane.wlpos.entities.SyncMessage
import uk.co.wonderlane.wlpos.enums.SyncMessageType

class ProductGroupController {

    def productGroupService
    def productService
    def springSecurityService
    def rabbitService
    def categoryService

    @Secured(['ROLE_ENGINEER', 'ROLE_HEAD_OFFICE'])
    def index() {
        session.SEARCH_TERM = null
        session.SEARCH_BY = null
        session.START_DATE = null
        session.END_DATE = null
        session.STATUS = null

        def productGroups = productGroupService.getProductGroups()
        [productGroups: productGroups]
    }

    def sanitizeParam(param) {
        return (param == "null" || param == "") ? null : param
    }

    @Secured(['ROLE_ENGINEER', 'ROLE_HEAD_OFFICE'])
    def ajaxGetProductGroups() {
        try {
            DateTimeFormatter dateFormatter = DateTimeFormat.forPattern("dd/MM/yyyy")
            String searchTerm = params.productGroupSearchTerm
            String searchBy = params.productGroupSearchBy
            String status = sanitizeParam(params.status)
            String sortColumn = sanitizeParam(params.sortColumn) ?: "id"
            String sortOrder = sanitizeParam(params.sortOrder) ?: "asc"

            DateTime startDate = sanitizeParam(params.startDate) ? DateTime.parse(params.startDate, dateFormatter).withZoneRetainFields(DateTimeZone.UTC) : null
            DateTime endDate = sanitizeParam(params.endDate) ? DateTime.parse(params.endDate, dateFormatter).withZoneRetainFields(DateTimeZone.UTC) : null

            session.SEARCH_TERM = searchTerm
            session.SEARCH_BY = searchBy
            session.START_DATE = params.startDate
            session.END_DATE = params.endDate
            session.STATUS = status

            def productGroups = productGroupService.getProductGroups(
                    searchTerm,
                    searchBy,
                    startDate,
                    endDate,
                    status,
                    params.offset ? Integer.parseInt(params.offset) : 0,
                    params.max ? Integer.parseInt(params.max) : 50,
                    sortColumn,
                    sortOrder)

            render(template: "productGroupSearchResults", model: [productGroups         : productGroups,
                                                                  productGroupSearchTerm: searchTerm == null ? "" : searchTerm,
                                                                  productGroupSearchBy  : searchBy == null ? "" : searchBy,
                                                                  startDate             : startDate == null ? "" : startDate,
                                                                  endDate               : endDate == null ? "" : endDate,
                                                                  status                : status == null ? "" : status,
                                                                  max                   : params.max ?: 50,
                                                                  offset                : params.offset ? Integer.parseInt(params.offset) : 0,
                                                                  sortColumn            : sortColumn,
                                                                  sortOrder             : sortOrder])
        } catch (Exception ex) {
            log.error("Error searching product group, Exception " + ex.getMessage(), ex)
            response.status = 400
        }

    }

    def edit(int id) {
        def productGroup = productGroupService.getProductGroup(id)

        if (!productGroup) {
            flash.error = "Product Group not found."
            redirect(action: "index")
            return
        }

        def productVariants = productService.getProductVariants(productGroup.productGroupProducts?.collect { it.sku })

        productGroup.productGroupProducts.each { productGroupProduct ->
            Integer productVariantId = productVariants?.find { it.sku == productGroupProduct.sku }?.id
            productGroupProduct.productVariantId = productVariantId ? productVariantId : 0
            productGroupProduct.productDescription = productVariants.find { it.sku == productGroupProduct.sku }?.product?.name
        }

        render(view: "addEdit", model: [productGroup: productGroup])
    }


    def addEdit(Integer id) {
        if (id != null) {
            def productGroup = productGroupService.getProductGroup(id)
            if (!productGroup) {
                flash.error = "Product Group not found."
                redirect(action: "index")
            } else {
                def productvariants = productService.getProductVariants(productGroup?.productGroupProducts?.collect { it.sku })
                ProductGroupCommand productGroupCommand = new ProductGroupCommand()
                productGroup?.productGroupProducts?.each { productGroupProduct ->
                    def productGroupProductDisplayRow = makeProductGroupProductDisplayRow(productGroupProduct, productvariants);

                    productGroupCommand.productGroupProducts.add(productGroupProductDisplayRow)
                }

                def dateFormat = getDateFormat()
                productGroupCommand.id = id
                productGroupCommand.description = productGroup.description
                productGroupCommand.maxSellQuantity = productGroup.maxSellQuantity
                productGroupCommand.startDate = productGroup.startDate?.toString(dateFormat)
                productGroupCommand.endDate = productGroup.endDate?.toString(dateFormat)
                productGroupCommand.active = productGroup.active
                productGroupCommand.neverExpires = productGroup.endDate == null

                //Extract days and restrictionStartTime,restrictionEndTime
                if (productGroup.timeRestriction != null) {
                    def jsonSlurper = new JsonSlurper()
                    def timeRestrictionJson = jsonSlurper.parseText(productGroup.timeRestriction)
                    def timeRestrictionDays = timeRestrictionJson.timeRestrictionDays
                    List<Integer> daysList = new ArrayList<>()
                    timeRestrictionDays.eachWithIndex { boolean value, int index ->
                        if (value) {
                            daysList.add(index)
                        }
                    }
                    productGroupCommand.days = daysList.toArray(new int[0])
                    productGroupCommand.restrictionStartTime =
                            insertCharacter(timeRestrictionJson.startSellingTimeRestriction as String, (char) ':', 2)
                    productGroupCommand.restrictionEndTime =
                            insertCharacter(timeRestrictionJson.stopSellingTimeRestriction as String, (char) ':', 2)
                }

                [edit: true, productGroup: productGroupCommand]
            }
        } else {
            def categories = categoryService.getTopLevelCategories()
            [edit: false]
        }
    }

    @Secured(['ROLE_ENGINEER', 'ROLE_HEAD_OFFICE'])
    def ajaxSearchCategories(String searchTerm, String searchBy) {
        def categories = categoryService.searchCategories(searchTerm, searchBy)

        render(template: "categorySearchResults", model: [categories: categories, searchTerm: searchTerm, searchBy: searchBy, max: params.max ?: 50, offset: params.offset, totalResults: categories.totalCount])
    }

    @Secured(['ROLE_ENGINEER', 'ROLE_HEAD_OFFICE'])
    def ajaxAddProduct(int productVariantId, long sku, String productDescription) {
        def productGroupProductDisplayRow = new ProductGroupProduct()
        productGroupProductDisplayRow.sku = sku
        productGroupProductDisplayRow.productVariantId = productVariantId
        productGroupProductDisplayRow.productDescription = productDescription

        ProductVariant pv = ProductVariant.findById(productVariantId)
        def barcodes = []
        pv.getBarcodes()?.each {
            barcodes.add(it.barcode)
        }

        Product product = pv?.getProduct()

        productGroupProductDisplayRow.barcodes = barcodes.join(",")
        productGroupProductDisplayRow.itemCode = product.itemCode

        productGroupProductDisplayRow.categoryDescription = product?.getCategory()?.description

        render(template: "productGroupProductRow", model: [productGroupProduct: productGroupProductDisplayRow])
    }

    @Secured(['ROLE_ENGINEER', 'ROLE_HEAD_OFFICE'])
    def ajaxAddProductsFromCategory(int id, int groupId) {// Normally I'ld do this by a join, but.
        def category = categoryService.getCategory(id)

        // Search for any products that use this category ID
        def products = Product.findAllByCategory(category)

        def productGroupProducts = []
        for (Product product in products) {
            def productVariants = product?.getCurrentVariants()

            for (ProductVariant pv in productVariants) {
                def productGroupProduct = new ProductGroupProduct()
                productGroupProduct.sku = pv.sku
                productGroupProduct.productVariantId = pv.id
                productGroupProduct.productDescription = product.description

                def barcodes = []
                pv.getBarcodes()?.each {
                    barcodes.add(it.barcode)
                }

                productGroupProduct.barcodes = barcodes.join(",")
                productGroupProduct.categoryDescription = category.description
                productGroupProduct.itemCode = product.itemCode

                productGroupProducts.add(productGroupProduct)
            }
        }

        render(template: "productGroupProductRows", model: [productGroupProducts: productGroupProducts])
    }

    @Secured(['ROLE_ENGINEER', 'ROLE_HEAD_OFFICE'])
    def save(ProductGroupCommand cmd) {
        def productGroup
        def productGroupProductsToRemove

        if (cmd.id) {
            productGroup = productGroupService.getProductGroup(cmd.id)

            if (!productGroup) {
                flash.error = "Product Group not found."
                render(action: "index")
                return
            }

            // Find the products that needs to be Removed upon successful save
            // If there are no products left the CMD will have no skus so we can just use the whole productGroup products list
            // which will fail save validation but lets the user rectify.
            if (!cmd.sku) {
                productGroupProductsToRemove = productGroup.productGroupProducts
            } else {
                // Remove any ProductGroupProducts which are no longer in the productGroup.
                productGroupProductsToRemove = productGroup.productGroupProducts?.findAll { !cmd.sku.contains(it.sku) }
            }
        } else {
            productGroup = new ProductGroup()
        }

        productGroup.retailerId = springSecurityService.principal.retailerId
        productGroup.description = cmd.description
        productGroup.maxSellQuantity = cmd.maxSellQuantity
        productGroup.active = cmd.active
        if (cmd.days != null && cmd.days.size() > 0) { // multiple days of the week have been selected as restricted.
            def jsonMap = [
                    timeRestrictionDays: (0..6).collect { day -> cmd.days.contains(day) },
                    startSellingTimeRestriction: cmd.restrictionStartTime?.replace(":", "") ?: "",
                    stopSellingTimeRestriction : cmd.restrictionEndTime?.replace(":", "") ?: ""
            ]
            productGroup.timeRestriction = (jsonMap as JSON).toString()
        } else if (cmd.restrictionStartTime?.isEmpty() || cmd.restrictionEndTime?.isEmpty()) {
            def jsonMap = [
                    startSellingTimeRestriction: cmd.restrictionStartTime?.replace(":", "") ?: "",
                    stopSellingTimeRestriction : cmd.restrictionEndTime?.replace(":", "") ?: ""
            ]
            productGroup.timeRestriction = (jsonMap as JSON).toString()
        }

        def dateFormatter = getDateFormat()
        if (cmd.startDate != null) {
            productGroup.startDate = dateFormatter.parseDateTime(cmd.startDate)
        }
        if (cmd.neverExpires) {
            productGroup.endDate = null
        } else if (cmd.endDate != null) {
            productGroup.endDate = dateFormatter.parseDateTime(cmd.endDate)
        }

        def skusInProductGroup = productGroup.productGroupProducts?.collect { it.sku }

        cmd.sku?.toUnique().each {
            if (!cmd.id || !skusInProductGroup.contains(it)) {
                def productGroupProduct = new ProductGroupProduct()
                productGroupProduct.sku = it

                productGroup.addToProductGroupProducts(productGroupProduct)
            }
        }

        if (cmd.validate() && productGroup.validate()) {
            // Commit the product deletion if the final productGroup is valid for saving
            //  and there are products to remove
            productGroupProductsToRemove?.each {
                productGroupService.deleteProductGroupProduct(productGroup.id, it.sku)
            }

            productGroupService.saveProductGroup(productGroup)

            // Send this update to the whole Retailer exchange!
            sendProductGroup(productGroup)

            flash.message = "Product Group saved successfully."

            redirect(action: "index", id: productGroup.id)
        } else {
            if (productGroup.productGroupProducts && productGroup.productGroupProducts?.size() > 0) {
                def productsvariants = productService.getProductVariants(productGroup?.productGroupProducts?.collect { it.sku })

                productGroup.productGroupProducts.each { productGroupProduct ->
                    def productGroupProductForDisplay = makeProductGroupProductDisplayRow(productGroupProduct, productsvariants)

                    cmd.productGroupProducts.add(productGroupProductForDisplay)
                }
            }

            render(view: "addEdit", model: [productGroup: cmd, productGroupErrors: productGroup])
        }
    }

    private void sendProductGroup(ProductGroup productGroup) {
        // Make sure the RabbitMQ connection is available, otherwise reject the save.
        try {
            if (!rabbitService.isOpen()) {
                throw new Exception("Rabbit MQ not available")
            }

            SyncMessage syncMessage = new SyncMessage(SyncMessageType.TAG, springSecurityService.principal.retailerId, 0, 0, 0)
            syncMessage.setInsert(true)
            syncMessage.setProductGroup(productGroup.getProductGroup())

            rabbitService.sendMessage(syncMessage)
        } catch (Exception e) {
            e.printStackTrace()
        }
    }

    def parseDate(String dateString) {
        if (dateString) {
            try {
                return getDateFormat().parseDateTime(dateString)
            } catch (Exception e) {
                println("Error parsing date: ${e.message}")
                return null
            }
        }
        return null
    }

    private static def insertCharacter(String original, char charToInsert, int index) {
        def chars = original.toCharArray() as List
        chars.add(index, charToInsert)
        return chars.join()
    }

    private static def getDateFormat() {
        return DateTimeFormat.forPattern("dd/MM/yyyy")
    }

    private static def makeProductGroupProductDisplayRow(ProductGroupProduct productGroupProduct, List<Long> products) {
        Integer productVariantId = products?.find { it.sku == productGroupProduct.sku }?.id

        def productGroupProductDisplayRow = new ProductGroupProduct()
        productGroupProductDisplayRow.sku = productGroupProduct.sku
        productGroupProductDisplayRow.productVariantId = productGroupProduct.productVariantId
        productGroupProductDisplayRow.productDescription = products?.find { it.sku == productGroupProduct.sku }?.product?.description

        ProductVariant pv = ProductVariant.findById(productVariantId)
        def barcodes = []
        pv.getBarcodes()?.each {
            barcodes.add(it.barcode)
        }

        Product product = pv?.getProduct()

        productGroupProductDisplayRow.barcodes = barcodes.join(",")
        productGroupProductDisplayRow.itemCode = product.itemCode

        productGroupProductDisplayRow.categoryDescription = product?.getCategory()?.description

        productGroupProductDisplayRow
    }
}

class ProductGroupCommand {

    int id
    String description
    Integer maxSellQuantity
    Long[] sku
    boolean active
    int[] days
    String restrictionStartTime
    String restrictionEndTime
    String startDate
    String endDate
    boolean neverExpires
    Set<ProductGroupProduct> productGroupProducts = new HashSet<>()

    static constraints = {
        sku nullable: false
        days nullable: true
        restrictionStartTime nullable: true
        restrictionEndTime nullable: true

        description nullable: false, size: 1..60, validator: { val, obj ->
            if (!val || val.trim().length() < 1 || val.trim().length() > 60) {
                return ['producthistory.description.size']
            }
        }
        startDate nullable: false, validator: { val, obj ->
            if (val == null) {
                return ['producthistory.startdate.empty']
            }
        }
        endDate nullable: true
        maxSellQuantity nullable: true, validator: { val, obj ->
            if (val ?: 0 < 0 || val ?: 0 > 999999) {
                return ['producthistory.maxSellQuantity.invalid']
            }
        }
        active nullable: false, validator: { val, obj ->
            if (val == null) {
                return ['producthistory.active.null']
            }
        }
        productGroupProducts nullable: false, validator: { val, obj ->
            if (val?.size() == 0) {
                return ['producthistory.productgroupproducts.nullorempty']
            }
        }
    }
}
