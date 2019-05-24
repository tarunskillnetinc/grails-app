package uk.co.wonderlane.wlpos

class ProductController {

    def productService

    def search() {
        def products = productService.searchProducts(params.searchTerm)

        render(template: "/product/productSearchResults", model: [ products: products ])
    }
}