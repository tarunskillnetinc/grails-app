package uk.co.wonderlane.wlpos

import grails.databinding.BindingFormat
import org.joda.time.DateTimeZone
import org.springframework.validation.FieldError
import uk.co.wonderlane.wlpos.enums.wlim.ProductListStatus
import uk.co.wonderlane.wlpos.enums.wlim.ProductListType

class ProductListController {

    static def timeZone = DateTimeZone.forID("Europe/London")

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

        session.CENTRAL_COUNT_SEARCH_TERM = searchTerm

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

        [availableStores: availableStores, filterHospitalityAndNoStockSalesAllowed: true]
    }

    def saveCentralCount(SaveCentralCountCommand cmd) {
        if (!cmd.validate()) {
            ProductList productList = new ProductList()
            productList.properties = cmd.properties

            onError(cmd, productList)

            return
        }

        def productListsToBeSaved = new ArrayList()
        def productList = new ProductList()
        productList.properties = cmd.properties

        productList.userId = springSecurityService.principal.id
        productList.retailerId = springSecurityService.principal.retailerId

        productList.setEndDate(productList.getEndDate()
                .plusHours(23)
                .plusMinutes(59)
                .plusSeconds(59)
        )

        if (cmd.productVariantId) {
            // Loop over each product variant
            cmd.productVariantId.each {
                def productVariant = productService.getProductVariant(it)

                if (productVariant) {
                    ProductListItem productListItem = new ProductListItem()
                    productListItem.productVariant = productVariant
                    productListItem.fillQuantity = 0
                    productListItem.productList = productList
                    productListItem.productQuantityInStock = productVariant?.getProductStock(productList.store?.id)?.quantityInStock ?: 0
                    productList.productListItems.add(productListItem)
                }
            }
        }

        if (!productList.validate()) {
            onError(cmd, productList);
            return
        }

        productListsToBeSaved.add(productList)
        
        try {
            productListService.saveProductLists(productListsToBeSaved)
            if (productListsToBeSaved.size() > 0) {
                flash.message = "Central count saved successfully."
            }

            def productListStoresToBeSaved = new ArrayList()

            for (int storeId : cmd.storeIdList) {
                ProductListStore productListStore = new ProductListStore()
                productListStore.productList = productList
                productListStore.store = Store.load(storeId)
                productListStoresToBeSaved.add(productListStore)
            }
            productListService.saveProductListStores(productListStoresToBeSaved)

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

        def unsavedProductVariants = new ArrayList<uk.co.wonderlane.wlpos.entities.ProductVariant>()
        for (productVariantId in cmd.productVariantId) {
            if (productList.productListItems.contains { productListItem -> productListItem.productVariant.id == productVariantId }) {
                continue
            }

            ProductVariant foundProductVariant = productService.getProductVariant(productVariantId)
            if (!foundProductVariant || unsavedProductVariants.contains { unsavedProductVariant -> unsavedProductVariant.id == foundProductVariant.id }) {
                continue
            }

            unsavedProductVariants.add(foundProductVariant)
        }

        render(view: "addCentralCount", model: [productList: productList, availableStores: availableStores, command: cmd, unsavedVariants: unsavedProductVariants])
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