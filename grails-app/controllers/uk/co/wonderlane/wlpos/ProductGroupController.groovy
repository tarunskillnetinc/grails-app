package uk.co.wonderlane.wlpos

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

    def index() {
        def productGroups = productGroupService.getProductGroups()
        [productGroups: productGroups]
    }

    def show(int id) {
        def productGroup = productGroupService.getProductGroup(id)
        if (!productGroup) {
            flash.error = "Product Group not found."
            redirect(action: "index")
        } else {
            def products = productService.getProductVariants(productGroup?.productGroupProducts?.collect { it.sku })
            productGroup?.productGroupProducts?.each { productGroupProduct ->
                Integer productVariantId = products?.find { it.sku == productGroupProduct.sku }?.id
                productGroupProduct.productVariantId = productVariantId ? productVariantId : 0
                productGroupProduct.productDescription = products?.find { it.sku == productGroupProduct.sku }?.product?.name
            }
            [productGroup: productGroup]
        }
    }

    def ajaxGetProductGroups() {
        try {
            DateTimeFormatter dateFormatter = DateTimeFormat.forPattern("dd/MM/yyyy")
            String searchTerm = params.productGroupSearchTerm
            String searchBy = params.productGroupSearchBy
            DateTime startDate = params.startDate ? DateTime.parse(params.startDate, dateFormatter).withZoneRetainFields(DateTimeZone.UTC) : null
            DateTime endDate = params.endDate ? DateTime.parse(params.endDate, dateFormatter).withZoneRetainFields(DateTimeZone.UTC) : null
            String status = params.status ? params.status : null
            String sortColumn = params.sortColumn ?: "id"
            String sortOrder = params.sortOrder ?: "asc"

            session.SEARCH_TERM = searchTerm
            session.SEARCH_BY = searchBy
            session.START_DATE = startDate
            session.END_DATE = endDate
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
                                                                  productGroupSearchTerm   : searchTerm,
                                                                  productGroupSearchBy : searchBy ,
                                                                  startDate    : endDate,
                                                                  endDate      : startDate,
                                                                  status       : status,
                                                                  max          : params.max ?: 50,
                                                                  offset       : params.offset ? Integer.parseInt(params.offset) : 0,
                                                                  sortColumn   : sortColumn,
                                                                  sortOrder    : sortOrder,])
        } catch (Exception ex) {
            log.error("Error searching product group, Exception " + ex.getMessage(), ex)
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

        render(view: "add", model: [productGroup: productGroup])
    }

    def ajaxAddProduct(int productVariantId, long sku, String productDescription) {
        def productGroupProduct = new ProductGroupProduct()
        productGroupProduct.sku = sku
        productGroupProduct.productVariantId = productVariantId
        productGroupProduct.productDescription = productDescription

        render(template: "productGroupProductRow", model: [productGroupProduct: productGroupProduct])
    }

    def save(SaveProductGroupCommand cmd) {
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
        productGroup.name = cmd.name
        productGroup.maxSellQuantity = cmd.maxSellQuantity

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

            redirect(action: "show", id: productGroup.id)
        } else {
            cmd.errors.allErrors.each { FieldError error ->
                final String field = error.field?.replace('profile.', '')
                final String code = "productGroup.$field.$error.code"

                productGroup.errors.rejectValue((field == "sku" ? "productGroupProducts" : field), code)
            }

            if (productGroup.productGroupProducts && productGroup.productGroupProducts?.size() > 0) {
                def productVariants = productService.getProductVariants(productGroup.productGroupProducts?.collect { it.sku })

                productGroup.productGroupProducts.each { productGroupProduct ->
                    Integer variantId = productVariants.find { it.sku == productGroupProduct.sku }?.id
                    productGroupProduct.productVariantId = variantId ? variantId : 0
                    productGroupProduct.productDescription = productVariants.find { it.sku == productGroupProduct.sku }?.product?.name
                }
            }

            render(view: "add", model: [productGroup: productGroup])
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
}

class SaveProductGroupCommand {

    int id
    String name
    Integer maxSellQuantity
    Long[] sku

    static constraints = {
        name nullable: false, blank: false, maxSize: 100
        maxSellQuantity nullable: true, min: 1, max: 999
        sku nullable: false
    }
}