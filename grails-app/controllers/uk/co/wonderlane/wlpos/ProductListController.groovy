package uk.co.wonderlane.wlpos

import grails.databinding.BindingFormat
import org.springframework.validation.FieldError
import uk.co.wonderlane.wlpos.enums.wlim.ProductListStatus
import uk.co.wonderlane.wlpos.enums.wlim.ProductListType

class ProductListController {

    def springSecurityService
    def productListService
    def productService
    def storeService
    def availableStores

    def index() {
        redirect(action: "listCentralCounts")
    }

    def listCentralCounts() {
        def productLists = productListService.getCentralCounts()

        [productLists: productLists]
    }

    def showCentralCount(int id) {
        def productList = productListService.getProductList(id, springSecurityService.principal.retailerId)

        [productList: productList]
    }

    def ajaxGetCentralCounts(String searchTerm, String searchBy) {

        def productLists = productListService.getCentralCounts(
                searchTerm, searchBy,
                params.offset ? Integer.parseInt(params.offset) : 0,
                params.max ? Integer.parseInt(params.max) : 50
        )

        render(template: "centralCountSearchResults", model: [
                productLists: productLists,
                searchTerm: searchTerm,
                searchBy: searchBy,
                offset: params.offset ?: 0,
                max: params.max ?: 50
        ])
    }

    def addCentralCount() {
        def retailerId = springSecurityService.principal.retailerId
        availableStores = storeService.getActiveStores(retailerId)

        [availableStores: availableStores]
    }

    def saveCentralCount(SaveCentralCountCommand cmd) {
        if (!cmd.validate()) {
            ProductList productList = new ProductList()
            productList.properties = cmd.properties

            onError(cmd, productList)

            return
        }

        def productListsToBeSaved = new ArrayList()
        // Define an empty list to store failed product lists
        def failedProductLists = []

        for (int storeId : cmd.storeIdList) {
            def storeSettings = storeService.getStoreByStoreNumber(springSecurityService.principal.retailerId, storeId)

            def productList = new ProductList()
            productList.properties = cmd.properties

            productList.userId = springSecurityService.principal.id
            productList.retailerId = springSecurityService.principal.retailerId
            productList.store = storeSettings

            if (productList.startDate == productList.endDate) {
                productList.endDate = productList.endDate.plusDays(1)
            }

            if (cmd.productVariantId) {
                // Loop over each product variant
                cmd.productVariantId.each {
                    def productVariant = productService.getProductVariant(it)

                    if (productVariant) {
                        int quantityInStock = productVariant?.getProductStock(productList.store?.id)?.quantityInStock ?: 0

                        Product product = Product.findByItemCode(productVariant?.product?.itemCode)
                        if (product) {

                            Range range = Range.findById(storeSettings.getRangeId())
                            RangeProduct rangeProduct = RangeProduct.findByProductIdAndRange(product.getId(), range)

                            if (rangeProduct && !rangeProduct.getDeleted()) {
                                ProductListItem productListItem = new ProductListItem()
                                productListItem.productVariant = productVariant
                                productListItem.fillQuantity = 0
                                productListItem.productList = productList
                                productListItem.productQuantityInStock = quantityInStock
                                productList.productListItems.add(productListItem)
                            }
                        }

                    }
                }
            }


            if (!productList.validate()) {
                onError(cmd, productList);
                return
            }

            if (productList.getProductListItems().size() > 0) {
                productListsToBeSaved.add(productList)
            } else {
                failedProductLists.add(productList)
            }
        }

        try {
            productListService.saveProductLists(productListsToBeSaved)
            if (productListsToBeSaved.size() > 0) {
                flash.message = "Central count saved successfully."
            }
            if (failedProductLists.size() > 0) {
                flash.warning = failedProductLists.size() + " Central count could not be created due to product ranging"
            }
            redirect(action: "listCentralCounts")
        } catch (Exception e) {
            e.printStackTrace()
        }
    }

    private onError(SaveCentralCountCommand cmd, ProductList productList) {
        cmd.errors.allErrors.each { FieldError error ->
            final String field = error.field?.replace('profile.', '')
            final String code = "productList.$field.$error.code"

            if (field == "productVariantId") {
                productList.errors.rejectValue("productListItems", code)
            } else if (field == "storeIdList") {
                productList.errors.reject("productList.centralCount.noStoreSelected")
            } else if (field == "productListItems") {
                productList.errors.reject("productList.centralCount.noProductSelected")
            } else {
                productList.errors.rejectValue(field, code)
            }
        }

        render(view: "addCentralCount", model: [productList: productList, availableStores: availableStores])
    }

    def ajaxAddProduct(int productVariantId) {
        def productVariant = productService.getProductVariant(productVariantId)

        render(template: "centralCountProductRow", model: [productVariant: productVariant])
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
    List<Integer> storeIdList = new ArrayList<>()

    static constraints = {
        description nullable: false, blank: false, maxSize: 100
        startDate nullable: false
        endDate nullable: false
        productVariantId nullable: false
        storeIdList validator: {
            if (it.size() == 0) {
                ["productList.centralCount.noStoreSelected"]
            }
        }
    }
}