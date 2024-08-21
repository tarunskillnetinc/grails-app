package uk.co.wonderlane.wlpos

import org.springframework.validation.FieldError
import uk.co.wonderlane.wlpos.entities.SyncMessage
import uk.co.wonderlane.wlpos.enums.SyncMessageType

class TagController {

    def tagService
    def productService
    def springSecurityService
    def rabbitService
    def gsonProvider

    def index() {
        def tags = tagService.getTags()

        [tags: tags]
    }

    def show(int id) {
        def tag = tagService.getTag(id)

        if (!tag) {
            flash.error = "Tag not found."
            redirect(action: "index")
            return
        }

        def products = productService.getProductVariants(tag?.tagProducts?.collect { it.sku })

        tag?.tagProducts?.each {tagProduct ->
            Integer productVariantId = products?.find { it.sku == tagProduct.sku }?.id
            tagProduct.productVariantId = productVariantId ? productVariantId : 0
            tagProduct.productDescription = products?.find { it.sku == tagProduct.sku }?.product?.description
        }

        [tag: tag]
    }

    def ajaxGetTags(String searchTerm, String searchBy) {
        def tags = tagService.getTags(searchTerm, searchBy, params.offset ? Integer.parseInt(params.offset) : 0, params.max ? Integer.parseInt(params.max) : 50)

        render(template: "tagSearchResults", model: [tags      : tags,
                                                     searchTerm: searchTerm,
                                                     max       : params.max ?: 50,
                                                     offset    : params.offset])
    }

    def add() {

    }

    def edit(int id) {
        def tag = tagService.getTag(id)

        if (!tag) {
            flash.error = "Tag not found."
            redirect(action: "index")
            return
        }

        def productVariants = productService.getProductVariants(tag.tagProducts?.collect { it.sku })

        tag.tagProducts.each { tagProduct ->
            Integer productVariantId = productVariants?.find { it.sku == tagProduct.sku }?.id
            tagProduct.productVariantId = productVariantId ? productVariantId : 0
            tagProduct.productDescription = productVariants.find { it.sku == tagProduct.sku }?.product?.description
        }

        render (view: "add", model: [tag: tag])
    }

    def ajaxAddProduct(int productVariantId, long sku, String productDescription) {
        def tagProduct = new TagProduct()
        tagProduct.sku = sku
        tagProduct.productVariantId = productVariantId
        tagProduct.productDescription = productDescription

        render (template: "tagProductRow", model: [tagProduct: tagProduct])
    }

    def save(SaveTagCommand cmd) {
        def tag
        def tagProductsToRemove

        if (cmd.id) {
            tag = tagService.getTag(cmd.id)

            if (!tag) {
                flash.error = "Tag not found."
                render (action: "index")
                return
            }

            // Find the products that needs to be Removed upon successful save
            // If there are no products left the CMD will have no skus so we can just use the whole tag products list
            // which will fail save validation but lets the user rectify.
            if (!cmd.sku) {
                tagProductsToRemove = tag.tagProducts
            } else {
                // Remove any TagProducts which are no longer in the tag.
                tagProductsToRemove = tag.tagProducts?.findAll { !cmd.sku.contains(it.sku) }
            }
        } else {
            tag = new Tag()
        }

        tag.retailerId = springSecurityService.principal.retailerId
        tag.description = cmd.description
        tag.maxSellQuantity = cmd.maxSellQuantity

        def skusInTag = tag.tagProducts?.collect { it.sku }

        cmd.sku?.toUnique().each {
            if (!cmd.id || !skusInTag.contains(it)) {
                def tagProduct = new TagProduct()
                tagProduct.sku = it

                tag.addToTagProducts(tagProduct)
            }
        }

        if (cmd.validate() && tag.validate()) {
            // Commit the product deletion if the final tag is valid for saving
            //  and there are products to remove
            tagProductsToRemove?.each {
                tagService.deleteTagProduct(tag.id, it.sku)
            }

            tagService.saveTag(tag)

            // Send this update to the whole Retailer exchange!
            sendTag(tag)

            flash.message = "Tag saved successfully."

            redirect(action: "show", id: tag.id)
        } else {
            cmd.errors.allErrors.each { FieldError error ->
                final String field = error.field?.replace('profile.', '')
                final String code = "tag.$field.$error.code"

                tag.errors.rejectValue((field == "sku" ? "tagProducts" : field), code)
            }

            if (tag.tagProducts && tag.tagProducts?.size() > 0) {
                def productVariants = productService.getProductVariants(tag.tagProducts?.collect { it.sku })

                tag.tagProducts.each { tagProduct ->
                    Integer variantId = productVariants.find { it.sku == tagProduct.sku }?.id
                    tagProduct.productVariantId = variantId ? variantId : 0
                    tagProduct.productDescription = productVariants.find { it.sku == tagProduct.sku }?.product?.description
                }
            }

            render(view: "add", model: [tag: tag])
        }
    }

    private void sendTag(Tag tag) {
        // Make sure the RabbitMQ connection is available, otherwise reject the save.
        try {
            if (!rabbitService.isOpen()) {
                throw new Exception("Rabbit MQ not available")
            }

            SyncMessage syncMessage = new SyncMessage(SyncMessageType.TAG, springSecurityService.principal.retailerId, 0, 0, 0)
            syncMessage.setInsert(true)
            syncMessage.setTag(tag.getTag())

            rabbitService.sendMessage(syncMessage)
        } catch (Exception e) {
            e.printStackTrace()
        }
    }
}

class SaveTagCommand {

    int id
    String description
    Integer maxSellQuantity
    Long[] sku

    static constraints = {
        description nullable: false, blank: false, maxSize: 100
        maxSellQuantity nullable: true, min: 1, max: 999
        sku nullable: false
    }
}