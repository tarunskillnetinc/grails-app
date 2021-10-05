package uk.co.wonderlane.wlpos

class TagController {

    def tagService
    def productService

    def index() {
        def tags = tagService.getTags()

        [tags: tags]
    }

    def show(int id) {
        def tag = tagService.getTag(id)

        [tag: tag]
    }

    def ajaxGetTags(String searchTerm) {
        def tags = tagService.getTags(searchTerm)

        render (template: "tagSearchResults", model: [tags: tags, searchTerm: searchTerm])
    }

    def addTag() {

    }

    def ajaxAddProduct(int productId) {
        def product = productService.getProduct(productId)

        render (template: "tagProductRow", model: [product: product])
    }
}
