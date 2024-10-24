<!doctype html>
<html>
<head>
    <meta name="layout" content="main" />

    <title>Trust Retail</title>

    <asset:stylesheet src="bootstrap-datepicker3.min.css" />
    <asset:javascript src="bootstrap-datepicker.min.js" />
    <asset:javascript src="reporting.js" />
    <asset:javascript src="jquery-ui.js" />
    <asset:stylesheet src="jquery-ui.css" />

    <script type='text/javascript'>
        function productSelected(productId, productVariantId, sku, description) {
            var selectVariantUrl = "${createLink(controller: 'order', action: 'ajaxSelectVariant')}";

            $("#showSkuContent").html("<div class=\"modal-body\"><div class=\"d-flex justify-content-center\"><div id=\"loadingIndicator\" class=\"spinner-border\" role=\"status\"><span class=\"sr-only\">Loading...</span></div></div></div>");

            var supplierId = $('#supplierId').val();
            var productListId = $('#productListId').val();

            $.ajax({
                url: selectVariantUrl,
                data: { productId: productId, productListId: productListId },
                method: "GET",
                statusCode: {
                    200: function (response) {
                        $('#showSkuModal').modal({show: true});
                        $('#productSearchModal').modal('hide');
                        $("#showSkuContent").html(response);
                    }
                }
            });
        }

        function complete() {
            $("#productListContent").html("<div class=\"modal-body\"><div class=\"d-flex justify-content-center\"><div id=\"loadingIndicator\" class=\"spinner-border\" role=\"status\"><span class=\"sr-only\">Loading...</span></div></div></div>");
            $.ajax({
                url: "${createLink(controller: 'order', action: 'ajaxShowOrderConfirmWindow')}",
                method: "GET",
                success: function (resp) {
                    $('#productListModal').modal({show: true});
                    $("#productListContent").html(resp);
                }
            });
        }

        function deleteOrder() {
            $("#productListContent").html("<div class=\"modal-body\"><div class=\"d-flex justify-content-center\"><div id=\"loadingIndicator\" class=\"spinner-border\" role=\"status\"><span class=\"sr-only\">Loading...</span></div></div></div>");
            $.ajax({
                url: "${createLink(controller: 'order', action: 'ajaxShowOrderDeleteWindow')}",
                method: "GET",
                success: function (resp) {
                    $('#productListModal').modal({show: true});
                    $("#productListContent").html(resp);
                }
            });
        }

        function deleteOrderItem(event) {
            // Stop the event propagation to prevent the click event from reaching other elements
            event.stopPropagation();

            var productItemId = event.target.getAttribute('data-productItemId');
            $("#productListContent").html("<div class=\"modal-body\"><div class=\"d-flex justify-content-center\"><div id=\"loadingIndicator\" class=\"spinner-border\" role=\"status\"><span class=\"sr-only\">Loading...</span></div></div></div>");
            $.ajax({
                url: "${createLink(controller: 'order', action: 'ajaxShowOrderItemDeleteWindow')}",
                data: {productItemId: productItemId},
                method: "GET",
                success: function (resp) {
                    $('#productListModal').modal({show: true});
                    $("#productListContent").html(resp);
                }
            });
        }

        // Order approve functions --> Approve order confirm and cancel order confirm
        function approveConfirm() {
            $('#orderConfirmModal').modal({show: true});
            $("#orderConfirmContent").html("<div class=\"modal-body\"><div class=\"d-flex justify-content-center\">" +
                "<div id=\"loadingIndicator\" class=\"spinner-border\" role=\"status\"><span class=\"sr-only\">" +
                "Loading...</span></div></div></div>");
            var supplierId = $('#supplierId').val();
            var productListId = $('#productListId').val();
            $.ajax({
                url: "${createLink(controller: 'order', action: 'confirmOrder')}",
                data: {supplierId: supplierId, productListId: productListId},
                method: "POST",
                statusCode: {
                    500: function (response) {
                        $("#orderConfirmContent").html(response.responseText);
                    },
                    200: function (response) {
                        var noDisplayDiv = $('<div class="row col-8 offset-2 py-2 text-center my-auto wl-striped0" id="emptyProductListItems"><div class="col text-center">All products processed.</div></div>');

                        $("#search-results").html(noDisplayDiv);
                        $("#orderConfirmContent").html(response);
                    }
                }
            });
        }

        //Order delete functions --> confirm order delete and cancel order delete
        function confirmOrderDelete() {
            var productListId = $('#productListId').val();
            $.ajax({
                url: "${createLink(controller: 'order', action: 'deleteOrder')}",
                data: {productListId: productListId},
                method: "GET",
                statusCode: {
                    500: function (response) {
                        $('#orderConfirmModal').modal({show: true});
                        $("#orderConfirmContent").html(response.responseText);
                    },
                    200: function (response) {
                        window.location.href = "${createLink(controller: 'reporting', action: 'orders')}";
                    }
                }
            });
        }

        // Order delete functions --> confirm order delete and cancel order delete
        function confirmOrderItemDelete(productItemId) {
            var productListId = $('#productListId').val();

            $.ajax({
                url: "${createLink(controller: 'order', action: 'deleteOrderItem')}",
                data: {productListId: productListId,  productItemId: productItemId},
                method: "GET",
                statusCode: {
                    500: function (response) {
                        $('#orderConfirmModal').modal({show: true});
                        $("#orderConfirmContent").html(response.responseText);
                    },
                    200: function (response) {
                        window.location.href = "${createLink(controller: 'order', action: 'edit', id: productList?.id)}";
                    }
                }
            });
        }

        function cancelConfirmResponse() {
            window.location.href = "${createLink(controller: 'reporting', action: 'order', id: productList?.id)}";
        }
    </script>
</head>
<body>
    <g:hiddenField name="productListId" id="productListId" value="${productList?.id ?: 0}" />

    <section id="breadcrumb-container" class="container-fluid">
        <nav aria-label="breadcrumb">
            <div class="row mt-4">
                <div class="col">
                    <ol class="breadcrumb">
                        <li id="breadcrumb-1" class="breadcrumb-item"><g:link uri="/">Home</g:link></li>
                        <li id="breadcrumb-2" class="breadcrumb-item"><g:link controller="reporting" action="orders">All Orders</g:link></li>
                        <li id="breadcrumb-3" class="breadcrumb-item"><g:link controller="reporting" action="order" id="${productList?.id}">${productList?.supplierReference}</g:link></li>
                        <li id="breadcrumb-4" class="breadcrumb-item active" aria-current="page">Edit ${productList?.supplierReference}</li>
                    </ol>
                </div>
            </div>
        </nav>
    </section>

    <section id="order-list-container" class="container-fluid" style="z-index: 1;">
        <div class="row header-wl mt-3" >
            <div class="col-8 offset-2">
                <h2 class="mx-auto my-auto">Edit Order</h2>
            </div>

            <div class="col-2 text-right">
                <button id="add-product-button" class="btn btn-wl" data-toggle="modal" data-target="#productSearchModal">Add Product</button>
            </div>
        </div>

        <div id="order-list-results-container" class="align-content-center container-fluid" >
            <g:render template="/order/productListResults" model="[productList: productList]" />
        </div>
    </section>

    <g:render template="/modal/productSearch" />

    <section id="showPopUp-modal" class="container-fluid">
        <div class="modal fade" id="showSupplierModal" tabindex="-1"  role="dialog" aria-labelledby="showSupplierModalLabel" data-backdrop="false" aria-hidden="true" >
            <div class="modal-dialog modal-lg" role="document"style="border: 2px black solid; width: 350px; margin-top: 120px">
                <div id="showSupplierContent" class="modal-content" ></div>
            </div>
        </div>
    </section>

    <section id="showSku-modal" class="container-fluid" >
        <div class="modal fade" id="showSkuModal" tabindex="-1" role="dialog" aria-labelledby="showSkuModalLabel" data-backdrop="false" aria-hidden="true">
            <div class="modal-dialog modal-lg" role="document" style="border: 2px black solid; width: 300px; margin-top: 120px " >
                <div id="showSkuContent" class="modal-content" ></div>
            </div>
        </div>
    </section>

    <section id="orderConfirm-modal" class="container-fluid" >
        <div class="modal fade" id="orderConfirmModal" tabindex="-1" role="dialog" aria-labelledby="orderConfirmModalLabel" data-backdrop="false" aria-hidden="true" style="margin-top: 120px">
            <div class="modal-dialog modal-lg" style="border: 2px black solid ; margin-top: 120px" role="document" >
                <div id="orderConfirmContent" class="modal-content" ></div>
            </div>
        </div>
    </section>

    <section id="addSupplier-modal" class="container-fluid" >
        <div class="modal fade" id="addSupplierModal" tabindex="-1" role="dialog" aria-labelledby="addSupplierModalLabel" data-backdrop="false" aria-hidden="true">
            <div class="modal-dialog modal-lg" style="border: 2px black solid ; margin-top: 120px" role="document">
                <div id="addSupplierContent" class="modal-content"></div>
            </div>
        </div>
    </section>

    <section id="productList-modal" class="container-fluid" >
        <div class="modal fade" id="productListModal" tabindex="-1" role="dialog" aria-labelledby="productListModalLabel" data-backdrop="false" aria-hidden="true">
            <div class="modal-dialog modal-lg" style="border: 2px black solid; margin-top: 120px " role="document">
                <div id="productListContent" class="modal-content"></div>
            </div>
        </div>
    </section>
</body>
</html>