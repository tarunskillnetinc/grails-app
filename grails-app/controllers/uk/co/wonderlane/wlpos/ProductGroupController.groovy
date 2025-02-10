package uk.co.wonderlane.wlpos

import grails.converters.JSON
import groovy.json.JsonBuilder
import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import org.apache.commons.lang.StringUtils
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import org.joda.time.format.DateTimeFormat
import org.joda.time.format.DateTimeFormatter
import org.springframework.validation.FieldError
import uk.co.wonderlane.wlpos.entities.SyncMessage
import uk.co.wonderlane.wlpos.enums.SyncMessageType

class ProductGroupController {

    def productGroupService
    def productService
    def springSecurityService
    def rabbitService
    def categoryService

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

            render(template: "productGroupSearchResults", model: [productGroups: productGroups,
                                                                  productGroupSearchTerm   : searchTerm == null ? "" : searchTerm,
                                                                  productGroupSearchBy : searchBy == null ? "" : searchBy,
                                                                  startDate    : startDate == null ? "" : startDate,
                                                                  endDate      : endDate == null ? "" : endDate,
                                                                  status       : status == null ? "" : status,
                                                                  max: params.max ?: 50,
                                                                  offset       : params.offset ? Integer.parseInt(params.offset) : 0,
                                                                  sortColumn   : sortColumn,
                                                                  sortOrder: sortOrder])
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
                def products = productService.getProductVariants(productGroup?.productGroupProducts?.collect { it.sku })
                ProductGroupCommand productGroupCommand = new ProductGroupCommand()
                productGroup?.productGroupProducts?.each { productGroupProduct ->
                    Integer productVariantId = products?.find { it.sku == productGroupProduct.sku }?.id
                    productGroupProduct.productVariantId = productVariantId ? productVariantId : 0
                    productGroupProduct.productDescription = products?.find { it.sku == productGroupProduct.sku }?.product?.description
                    productGroupCommand.productGroupProducts.add(productGroupProduct)
                }

                productGroupCommand.id = id
                productGroupCommand.description = productGroup.description
                productGroupCommand.maxSellQuantity = productGroup.maxSellQuantity
                productGroupCommand.startDate = productGroup.startDate
                productGroupCommand.endDate = productGroup.endDate
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
                            insertCharacter(timeRestrictionJson.startSellingTimeRestriction as String, (char)':', 2)
                    productGroupCommand.restrictionEndTime =
                            insertCharacter(timeRestrictionJson.stopSellingTimeRestriction as String, (char)':', 2)
                }
                [productGroup: productGroupCommand, edit : true]
            }
        } else {
            def categories = categoryService.getTopLevelCategories()
            [edit: false]
        }
    }

    def ajaxAddProduct(int productVariantId, long sku, String productDescription) {
        def productGroupProduct = new ProductGroupProduct()
        productGroupProduct.sku = sku
        productGroupProduct.productVariantId = productVariantId
        productGroupProduct.productDescription = productDescription

        render(template: "productGroupProductRow", model: [productGroupProduct: productGroupProduct])
    }

    def save(ProductGroupCommand cmd) {
        def errorMessages = []

        // need to do all the validation on the server side level.
//        if( StringUtils.isEmpty(cmd.description) ) {
//            errorMessages << "Description must be between 1 and 60 characters."
//        }
//        if (errorMessages != null && !errorMessages.isEmpty()) {
//            flash.error = errorMessages
//            redirect(action: "addEdit", model: [productGroup: cmd])
//        }

        def productGroup
        def productGroupProductsToRemove

        if (cmd.id) {
            productGroup = productGroupService.getProductGroup(cmd.id)

            if (!productGroup) {
                flash.error = "Product Group not found."
                render (action: "index")
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
        if (cmd.days != null && cmd.days.size() > 0) {
            def jsonMap = [
                    timeRestrictionDays: (0..6).collect { day -> cmd.days.contains(day) },
                    startSellingTimeRestriction: cmd.restrictionStartTime?.replace(":", "") ?: "",
                    stopSellingTimeRestriction : cmd.restrictionEndTime?.replace(":", "") ?: ""
            ]
            productGroup.timeRestriction = (jsonMap as JSON).toString()
        }
        DateTimeFormatter formatter = DateTimeFormat.forPattern("EEEE dd MMMM yyyy")
        productGroup.startDate =  formatter.parseDateTime(cmd.startDate)
        if (cmd.neverExpires) {
            productGroup.endDate = null
        } else if (cmd.endDate != null){
            productGroup.endDate =  formatter.parseDateTime(cmd.endDate)
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

            redirect(action: "addEdit", id: productGroup.id)
        } else {
            if (productGroup.productGroupProducts && productGroup.productGroupProducts?.size() > 0) {
                def productVariants = productService.getProductVariants(productGroup.productGroupProducts?.collect { it.sku })

                productGroup.productGroupProducts.each { productGroupProduct ->
                    Integer variantId = productVariants.find { it.sku == productGroupProduct.sku }?.id
                    productGroupProduct.productVariantId = variantId ? variantId : 0
                    productGroupProduct.productDescription = productVariants.find { it.sku == productGroupProduct.sku }?.product?.description
                    cmd.productGroupProducts.add(productGroupProduct)
                }
            }

            render(view: "addEdit", model: [productGroup: cmd])
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

    DateTime parseDate(String dateString) {
        if (dateString) {
            try {
                DateTimeFormatter formatter = DateTimeFormat.forPattern("EEEE dd MMMM yyyy")
                return formatter.parseDateTime(dateString)
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
        description nullable: false, blank: false, maxSize: 100
        maxSellQuantity nullable: true, min: 1, max: 999
        sku nullable: false
        days nullable: false
        restrictionStartTime nullable: false
        restrictionEndTime nullable: true
        startDate nullable: false
        endDate nullable: true
    }
}
