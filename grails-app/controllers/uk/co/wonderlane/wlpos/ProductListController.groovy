package uk.co.wonderlane.wlpos

import grails.databinding.BindingFormat
import org.springframework.validation.FieldError
import uk.co.wonderlane.wlpos.enums.wlim.ProductListStatus
import uk.co.wonderlane.wlpos.enums.wlim.ProductListType

class ProductListController {

    def springSecurityService
    def productListService
    def productService

    def index() {
        redirect (action: "listCentralCounts")
    }

    def listCentralCounts() {
        def productLists = productListService.getCentralCounts()

        [productLists: productLists]
    }

    def showCentralCount(int id) {
        def productList = productListService.getProductList(id)

        [productList: productList]
    }

    def ajaxGetCentralCounts(String searchTerm) {
        def productLists = productListService.getCentralCounts(searchTerm)

        render (template: "centralCountSearchResults", model: [productLists: productLists, searchTerm: searchTerm])
    }

    def addCentralCount() {

    }

    def saveCentralCount(SaveCentralCountCommand cmd) {
        def productList = new ProductList()

        productList.properties = cmd.properties

        productList.userId = springSecurityService.principal.id
        productList.retailerId = springSecurityService.principal.retailerId
        productList.storeId = springSecurityService.principal.storeId

        cmd.productVariantId?.each {
            def productVariant = productService.getProductVariant(it)

            if (productVariant) {
                productList.addToProductListItems(new ProductListItem(productVariant: productVariant))
            }
        }

        if (cmd.validate() && productList.validate()) {
            productListService.saveProductList(productList)

            flash.message = "Central count saved successfully."

            redirect(action: "listCentralCounts")
        } else {
            cmd.errors.allErrors.each { FieldError error ->
                final String field = error.field?.replace('profile.', '')
                final String code = "productList.$field.$error.code"

                productList.errors.rejectValue((field == "productVariantId" ? "productListItems" : field), code)
            }

            render(view: "addCentralCount", model: [productList: productList])
        }
    }

    def ajaxAddProduct(int productVariantId) {
        def productVariant = productService.getProductVariant(productVariantId)

        render (template: "centralCountProductRow", model: [productVariant: productVariant])
    }
}

class SaveCentralCountCommand {

    String description
    @BindingFormat('dd/MM/yyyy')
    Date startDate
    @BindingFormat('dd/MM/yyyy')
    Date endDate
    ProductListType type = ProductListType.SCHEDULED_COUNT
    ProductListStatus status = ProductListStatus.PENDING
    Integer[] productVariantId

    static constraints = {
        description nullable: false, blank: false, maxSize: 100
        startDate nullable: false
        endDate nullable: false
        productVariantId nullable: false
    }
}