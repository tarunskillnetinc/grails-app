package uk.co.wonderlane.wlpos


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
    def productListService

    def index() {

    }

    def add() {
        // Only allowed at store level currently.
        if (springSecurityService.principal.storeId == null || springSecurityService.principal.id <= 0){
            flash.error = "This function is not available at head office level."
            redirect(controller: "reporting", action: "orders")
            return
        }

        if (!params.supplierId || !params.supplierId.isNumber()) {
            flash.error = "Invalid supplier ID."
            redirect(controller: "reporting", action: "orders")
            return
        }

        def supplier = supplierService.getSupplier(Integer.parseInt(params.supplierId))

        if (!supplier) {
            flash.error = "Supplier not found."
            redirect(controller: "reporting", action: "orders")
            return
        }

        def productList = new ProductList()
        productList.retailerId = springSecurityService.principal.retailerId
        productList.userId = springSecurityService.principal.id
        productList.store = Store.get(springSecurityService.principal.storeId)
        productList.type = ProductListType.ORDER
        productList.status = ProductListStatus.IN_PROGRESS
        productList.dateStarted = DateTime.now(DateTimeZone.UTC)
        productList.ownerUserId = springSecurityService.principal.id
        productList.ownerUsersName = springSecurityService.principal.usersName
        productList.supplierId = supplier.id
        productList.supplierReference = supplier.reference

        productListService.saveProductList(productList)

        redirect(action: "edit", id: productList.id)
    }

    def edit(int id) {
        // Only allowed at store level currently.
        if (springSecurityService.principal.storeId == null || springSecurityService.principal.id <= 0){
            flash.error = "This function is not available at head office level."
            redirect(controller: "reporting", action: "orders")
            return
        }

        def productList = productListService.getProductList(id)

        if (!productList) {
            flash.error = "Order not found."
            redirect(controller: "reporting", action: "orders")
            return
        } else if (productList.status != ProductListStatus.IN_PROGRESS) {
            flash.error = "Only in-progress orders can be edited."
            redirect(controller: "reporting", action: "orders")
            return
        }

        [productList: productList]
    }

    // This is responsible for returning product list item view related data
    def productListItem(int id) {
        // If user do not logged in store level then redirect user back to reporting page
        if (springSecurityService.principal.storeId == null || springSecurityService.principal.id <= 0) {
            flash.error = "This function is not available at head office level."
            redirect(controller: "reporting", action: "orders")
            return
        }

        def productList = productListService.getProductList(Integer.parseInt(params.productListId))

        if (productList != null && productList.getStatus() == ProductListStatus.IN_PROGRESS) {
            boolean isSymbolGroupOrder = false
            ArrayList<Pack> packs = new ArrayList<>()

            def productListItem = productList?.productListItems?.find { it.id == id }

            def productVariant = productListItem?.productVariant ?: productService.getProductVariant(Integer.parseInt(params.productVariantId))

            BigDecimal singleQuantity = productListItem?.quantity ?: BigDecimal.ZERO

            // Load active packs for selected supplier
            for (Pack pack : productVariant?.packs) {
                if (pack?.supplier?.id == Integer.parseInt(productList.supplierId) && pack?.isActive()) {
                    packs.add(pack)
                }
            }

            // Load supplier
            Supplier supplier = supplierService.getSupplier(Integer.parseInt(productList.supplierId))

            // Calculate non symbol group 'singles' quantities
            if (supplier?.symbolGroup != null) {
                isSymbolGroupOrder = true
            } else {
                for (PackLine packLine : productListItem?.packLines) {
                    def pack = packs.find { it.orderCode == packLine.orderCode && it.id == packLine?.pack?.id }

                    singleQuantity = singleQuantity.subtract(pack?.quantity?.multiply(packLine.quantity))
                }
            }

            [productVariant: productVariant, packs: packs, productList: productList, productListItem : productListItem, isSymbolGroupOrder: isSymbolGroupOrder, singleQuantity : singleQuantity]
        } else {
            flash.error = "Only in-progress orders can be edited."
            redirect(controller: "reporting", action: "orders")
            return
        }
    }

    // This will load available variants user can select.
    // For non symbol group orders user will shown all available variants.
    // For symbol group orders only variants belonging to supplier will shown.
    def ajaxSelectVariant() {
        ArrayList<ProductVariant> variants = new ArrayList<>()
        def product = productService.getProduct(Integer.parseInt(params.productId))

        def productList = productListService.getProductList(Integer.parseInt(params.productListId))

        Supplier supplier = supplierService.getSupplier(Integer.parseInt(productList.supplierId)) //Load supplier

        // For symbol group orders filter variants which only belonging to selected supplier
        if (supplier?.symbolGroup != null) {
            for (ProductVariant productVariant : product?.variants) {
                for (Pack pack: productVariant?.packs) {
                    if (pack?.supplier?.id == Integer.parseInt(params.supplierId) && pack?.isActive()) {
                        variants.add(productVariant)
                        break
                    }
                }
            }
        } else { //For non symbol group orders add all available variants
            variants.addAll(product?.variants)
        }

        render(template: "selectSku", model: [product: product,
                                          variants: variants,
                                          variantSize: variants?.size(),
                                          totalVariantCount: variants.size(),
                                          effectiveDate: DateTime.now(DateTimeZone.UTC),
                                          storeId: springSecurityService.principal.storeId,
                                          productList: productList])

    }

    // Save or update our product list items with their pack lines.
    def saveProductListItem(PackLineRequestCommand packLineRequestCommand) {
        def productList = productListService.getProductList(packLineRequestCommand.productListId)

        if (!productList) {
            flash.message = "Order not found."
            redirect(controller: "reporting", action: "orders")
            return
        }

        def productVariant = productService.getProductVariant(packLineRequestCommand.productVariantId)

        def productListItem = packLineRequestCommand.productListItemId > 0 ? productList.productListItems.find { it.id == packLineRequestCommand.productListItemId } : new ProductListItem()
        productListItem.productVariant = productVariant
        productListItem.productQuantityInStock = productVariant.getProductStock(springSecurityService.principal.storeId)?.quantityInStock ?: 0
        productListItem.fillQuantity = BigDecimal.ZERO
        productListItem.productList = productList

        productListService.saveProductListItem(productListItem)

        productList.addToProductListItems(productListItem)

        productListService.saveProductList(productList)

        BigDecimal quantity = BigDecimal.ZERO.setScale(3)

        for (PackLinesCommand packLineCommand : packLineRequestCommand.packLines) {
            // Singles go on screen with dummy order code of 0 and don't get their own pack line.
            if (packLineCommand.orderCode != "0") {
                def packLine = productListItem?.packLines?.find { it.id = packLineCommand.id } ?: new PackLine()
                packLine.type = "ORDER"
                packLine.quantity = packLineCommand.quantity
                packLine.orderCode = packLineCommand.orderCode
                packLine.productListId = productList.id
                packLine.pack = productVariant.packs?.find { it.id == packLineCommand.packId }
                packLine.productListItem = productListItem

                productListService.savePackLine(packLine)

                productListItem.addToPackLines(packLine)

                quantity = quantity.add(packLine.quantity.multiply(packLine.pack.quantity))
            } else {
                quantity = quantity.add(packLineCommand.quantity)
            }
        }

        productListItem.quantity = quantity

        productListService.saveProductListItem(productListItem)

        redirect(action: "edit", id: productList.id)
    }

    //This is to confirm place order This will
    // 1. Update product stock
    // 2. Update product list status
    // 3. If non symbol order then create deliveries Or else Send request to NISA API
    def confirmOrder() {
        try {
            def productList = productListService.getProductList(Integer.parseInt(params.productListId))
            def supplier = supplierService.getSupplier(Integer.parseInt(productList.supplierId))

            String orderResponse = orderService.confirmOrder(productList, supplier)
            response.setStatus(200)
            render (template: "orderConfirmResponse", model: [orderResponse: orderResponse])
        } catch(Exception ex) {
            ex.printStackTrace()
            log.error("Order create exception found when confirming order, request is rollback , Exception " + ex)
            response.setStatus(500)
            render (view: "_orderConfirmError", contentType: "text/html")
        }
    }

    //This is to delete orders from database
    //This will delete all product list / product list items and corresponding pack lines
    def deleteOrder() {
        try {
            orderService.deleteProductList(Integer.parseInt(params.productListId))
            response.setStatus(200)
            redirect(controller: "reporting", action: "orders")
        } catch(Exception ex) {
            ex.printStackTrace()
            log.error("Order create exception found when confirming order, request is rollback , Exception " + ex)
            response.setStatus(500)
            render (view: "_orderDeleteError", contentType: "text/html")
        }
    }

    // Remove an individual item from an order
    def deleteOrderItem() {
        try {
            orderService.deleteProductListItem(Integer.parseInt(params.productListId), Integer.parseInt(params.productItemId))
            response.setStatus(200)
            redirect(action: "edit", id: params.productListId)
        } catch (Exception ex) {
            ex.printStackTrace()
            log.error("Error removing item from order , Exception " + ex)
            response.setStatus(500)
            render (view: "_orderDeleteError", contentType: "text/html")
        }
    }

    def ajaxShowOrderConfirmWindow() {
        render(view: "_orderConfirm")
    }

    def ajaxShowOrderDeleteWindow() {
        render(view: "_orderDelete")
    }

    def ajaxShowQuantityWarningWindow() {
        render(view: "_quantityWarning")
    }

    def ajaxShowOrderItemDeleteWindow() {
        render(view: "_orderItemDelete", model: [productItemId : Integer.parseInt(params.productItemId)])
    }
}

class PackLineRequestCommand {
    int productListId
    int productListItemId
    int productVariantId
    BigDecimal quantity
    BigDecimal fillQuantity
    List<PackLinesCommand> packLines
}

class PackLinesCommand {
    int id
    int packId
    BigDecimal quantity
    String orderCode
}
