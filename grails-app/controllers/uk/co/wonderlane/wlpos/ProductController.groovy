package uk.co.wonderlane.wlpos

class ProductController {

    def productService

    def search() {
        def products = productService.searchProducts(params.searchTerm, params.searchBy, 50, 0, "id", "asc")

        render(template: "/product/productSearchResults", model: [ products: products ])
    }

    def chrisTest() {
        def products = productService.searchProductsChrisTest("Hydro")

        [products: products]
    }
}