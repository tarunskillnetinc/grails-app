package uk.co.wonderlane.wlpos

import grails.converters.JSON
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import uk.co.wonderlane.wlpos.enums.wlim.ProductListStatus
import uk.co.wonderlane.wlpos.enums.wlim.ProductListType
import uk.co.wonderlane.wlpos.supplier.Pack
import uk.co.wonderlane.wlpos.supplier.Supplier
import uk.co.wonderlane.wlpos.supplier.SupplierSortParams

class OrderController {

    def supplierService
    def productService
    def springSecurityService
    def orderService
    def userService

    def index() {}

    // This is responsible for returning product list view related data
    def productList() {
        //If user do not logged in store level then redirect user back to reporting page
        if (springSecurityService.principal.storeId == null || springSecurityService.principal.id <= 0){
            redirect(controller: "reporting", action: "orders")
        } else {
            def supplier =  null
            uk.co.wonderlane.wlpos.entities.wlim.ProductList productList = null

            User user = userService.getUser(springSecurityService.principal.id)
            productList = orderService.getActiveProductList(ProductListType.ORDER, user.getUsername())

            if (productList != null && productList.getSupplierId() != null){ //If user already have product list then return it
                supplier = supplierService.getSupplier(Integer.parseInt(productList.getSupplierId() as String)) //Load supplier
            } else {

                if (!params.supplierId || !params.supplierId.isNumber() || params.supplierId.length() > 8) {
                    params.supplierId = "-1"
                }

                if (!params.isNew || !params.isNew.isNumber() || params.isNew.length() > 8) {
                    params.isNew = "-1"
                }

                //If user do not have product list then create new one only if request is mark for new and valid supplier id
                if (Integer.parseInt(params.supplierId) > 0 && Integer.parseInt(params.isNew) == 1){
                    supplier = supplierService.getSupplier(Integer.parseInt(params.supplierId)) //Load supplier
                    if (supplier != null){
                        productList = orderService.createProductList(productList, ProductListType.ORDER, supplier)
                    }
                } else {
                    render(view: "_productList")
                }
            }

            render(view: "_productList", model: [supplier: supplier, productList: productList, productListItems: productList?.getProductListItems()])
        }
    }

    // This is responsible for returning product list item view related data
    def productListItem(){
        //If user do not logged in store level then redirect user back to reporting page
        if (springSecurityService.principal.storeId == null || springSecurityService.principal.id <= 0) {
            redirect(controller: "reporting", action: "orders")
        } else {
            boolean isNoSymbolOrders = false
            int singleQuantity = 0
            ArrayList<Pack> packs = new ArrayList<>()
            uk.co.wonderlane.wlpos.entities.wlim.ProductList productList = orderService.getProductListById(Integer.parseInt(params.productListId))

            //If there is pending product list only process this
            if (productList != null && productList.getStatus() == ProductListStatus.IN_PROGRESS) {
                //Load product list by id
                productList = orderService.getProductListById(Integer.parseInt(params.productListId))
                ProductVariant variant = productService.getProductVariant(Integer.parseInt(params.variantId))

                //Load active packs for selected supplier
                for (Pack pack : variant?.packs) {
                    if (pack?.supplier?.id == Integer.parseInt(params.supplierId)) {
                        packs.add(pack)
                    }
                }

                //Get stored pack lines if there are any
                def productItemList = productList?.getProductListItems()?.find({ it?.productVariantId == Integer.parseInt(params.variantId) })
                List<PackLine> packLinesList = productItemList?.getPackLines()
                int productItemId = productItemList != null ? productItemList?.getId() : 0

                //Load supplier
                Supplier supplier = supplierService.getSupplier(Integer.parseInt(params.supplierId)) //Load supplier

                //Calculate non symbol group 'singles' quantities
                if (supplier?.getSymbolGroup() == null) {
                    isNoSymbolOrders = true
                    int nonSingleQuantity = 0
                    for (uk.co.wonderlane.wlpos.entities.wlim.PackLine packLine : productItemList?.getPackLines()) {
                        for (Pack filterPack : packs) {
                            if (packLine.getOrderCode() == filterPack.getOrderCode()) {
                                nonSingleQuantity = nonSingleQuantity + filterPack.getQuantity() * packLine.getQuantity()
                                break
                            }
                        }
                    }

                    singleQuantity = (productItemList?.getQuantity() ?: 0) - nonSingleQuantity

                }

                render(view: "_productListItem", model: [variants        : variant,
                                                         packs           : packs,
                                                         effectiveDate   : DateTime.now(DateTimeZone.UTC),
                                                         storeId         : springSecurityService.principal.storeId,
                                                         supplierId      : params.supplierId,
                                                         packLinesList   : packLinesList,
                                                         productItemId   : productItemId,
                                                         productListId   : params.productListId,
                                                         productItemList : productItemList,
                                                         isNoSymbolOrders: isNoSymbolOrders,
                                                         singleQuantity  : singleQuantity,
                                                         packSingles     : -1])
            } else {
                redirect(controller: "order", action: "productList")
            }
        }
    }

    //When loading check is there any active product for user and if not popup supplier view to select
    def ajaxCheckActiveProducts(){
        def suppliers = null
        User user = userService.getUser(springSecurityService.principal.id)
        uk.co.wonderlane.wlpos.entities.wlim.ProductList productList = orderService.getActiveProductList(ProductListType.ORDER, user.getUsername())
        if ((productList == null) || (productList != null && productList.getSupplierId() == null)){
            suppliers = Supplier.findAllByRetailerId(springSecurityService.principal.retailerId, [sort: "name", order: "ASC"])
            response.setStatus(200)
        }else {
            response.setStatus(204)
        }
        render (template: "showSupplier", model: [suppliers: suppliers])
    }

    //Search product by given term and criteria
    def ajaxSearchProducts() {
        session.PRODUCT_SEARCH_TERM = params.searchTerm
        session.effectiveDate = ["Current", DateTime.now(DateTimeZone.UTC)]

        def products = productService.searchProductsHql(params.searchTerm, params.searchBy, params.max ? Integer.parseInt(params.max) : 25, params.offset ? Integer.parseInt(params.offset) : 0, "id", "asc")

        render(template: "productSearchResults", model: [products: products.products,
                                                         storeId: springSecurityService.principal.storeId,
                                                         userColumns: productService.getColumns(),
                                                         searchTerm: params.searchTerm,
                                                         searchBy: params.searchBy,
                                                         max: params.max ?: 25,
                                                         offset: params.offset,
                                                         totalResults: products.totalCount,
                                                         supplierId:  params.supplierId,
                                                         productListId: params.productListId])
    }

    //This will load available variants user can select
    //For non symbol group orders user will shown all available variants
    //For symbol group orders only variants belonging to supplier will shown
    def ajaxSelectVariant(){
        ArrayList<ProductVariant> variants = new ArrayList<>()
        def product = productService.getProduct(Integer.parseInt(params.productId))

        Supplier supplier = supplierService.getSupplier(Integer.parseInt(params.supplierId)) //Load supplier

        //For symbol group orders filter variants which only belonging to selected supplier
        if (supplier?.symbolGroup != null){
            for (ProductVariant productVariant : product?.variants){
                for (Pack pack: productVariant?.packs){
                    if (pack?.supplier?.id == Integer.parseInt(params.supplierId)){
                        variants.add(productVariant)
                        break
                    }
                }
            }
        } else { //For non symbol group orders add all available variants
            variants.addAll(product?.variants)
        }

        render(view: "selectSku", model: [product : product,
                                          variants : variants,
                                          variantSize : variants?.size(),
                                          totalVariantCount : variants.size(),
                                          effectiveDate: DateTime.now(DateTimeZone.UTC),
                                          storeId :springSecurityService.principal.storeId,
                                          supplierId: params.supplierId,
                                          productListId: params.productListId])

    }

    //This is to save or update product list items and pack lines
    def ajaxSavePackLines(PackLineRequestCommand packLineRequestCommand){
        try {
            //saving product order request --> save list item + pack lines
            orderService.saveProductOrder(packLineRequestCommand)
            response.setStatus(200)
            redirect(action: "productList")
        }catch(Exception ex){
            ex.printStackTrace()
            log.error("Order create exception found when saving order list item and pack lines, request is rollback , Exception " + ex)
            response.sendError(500)
        }
    }

    //This is to confirm place order This will
    // 1. Update product stock
    // 2. Update product list status
    // 3. If non symbol order then create deliveries Or else Send request to NISA API
    def confirmOrder(){
        try {
            Supplier supplier = supplierService.getSupplier(Integer.parseInt(params.supplierId)) //Load supplier
            String orderResponse = orderService.confirmOrder(Integer.parseInt(params.productListId), supplier)
            response.setStatus(200)
            render (template: "orderConfirmResponse", model: [orderResponse: orderResponse])
        }catch(Exception ex){
            ex.printStackTrace()
            log.error("Order create exception found when confirming order, request is rollback , Exception " + ex)
            response.sendError(500)
        }
    }

    //This is to delete orders from database
    //This will delete all product list / product list items and corresponding pack lines
    def deleteOrder(){
        try {
            orderService.deleteProductList(Integer.parseInt(params.productListId))
            response.setStatus(200)
            redirect(controller: "order", action: "productList")
        }catch(Exception ex){
            ex.printStackTrace()
            log.error("Order create exception found when confirming order, request is rollback , Exception " + ex)
            response.sendError(500)
        }
    }

    // Remove an individual item from an order
    def deleteOrderItem() {
        try {
            orderService.deleteProductListItem(Integer.parseInt(params.productListId), Integer.parseInt(params.productItemId))
            response.setStatus(200)
            redirect(controller: "order", action: "productList")
        } catch (Exception ex) {
            ex.printStackTrace()
            log.error("Error removing item from order , Exception " + ex)
            response.sendError(500)
        }
    }

    def ajaxSupplierSearch(SupplierSortParams sortParams){
        def suppliers = [] //declare supplier list
        if (params.searchTerm != null){
            def suppliersResponse = supplierService.getSuppliers(params.searchTerm, params.searchBy, sortParams.offset ? sortParams.offset : 0, sortParams.max ? sortParams.max : 50, sortParams.sortColumn, sortParams.getSortOrder())
            def returnedSuppliers = suppliersResponse?.suppliers
            if (returnedSuppliers != null && returnedSuppliers.size() > 0){
                suppliers = returnedSuppliers
            }
        } else {
            suppliers = Supplier.findAllByRetailerId(springSecurityService.principal.retailerId, [sort: "name", order: "ASC"])
        }
        render (template: "supplierListView", model: [suppliers: suppliers])
    }

    def ajaxAddProduct(){
        render(view: "productSearch", model: [])
    }

    def ajaxShowOrderConfirmWindow(){
        render(view: "_orderConfirm", model: [])
    }

    def ajaxShowOrderDeleteWindow(){
        render(view: "_orderDelete", model: [])
    }

}

class PackLineRequestCommand {
    int supplierId
    int productListId
    int productItemId
    int productVariantId
    int quantity
    int fillQuantity
    List<PackLinesCommand> packLines
}

class PackLinesCommand{
    int id
    int quantity
    String orderCode
}
