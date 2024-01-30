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

        $( document ).ready(function() {

            $.ajax({
                url: "${createLink(controller: 'order', action: 'ajaxCheckActiveProducts')}",
                method: "GET",
                statusCode: {
                    204: function (response) {
                        $('#showSupplierModal').hide();
                        $("#showSupplierContent").hide();
                    },
                    200: function (response) {
                        $('#showSupplierModal').modal({backdrop: 'static', keyboard: false}, 'show');
                        $("#showSupplierContent").html(response);
                    }
                }
            })

        });

        function AddProduct() {
            $("#productSearchContent").html("<div class=\"modal-body\"><div class=\"d-flex justify-content-center\">" +
                "<div id=\"loadingIndicator\" class=\"spinner-border\" role=\"status\"><span class=\"sr-only\">" +
                "Loading...</span></div></div></div>");
            $.ajax({
                url: "${createLink(controller: 'order', action: 'ajaxAddProduct')}",
                method: "GET",
                success: function (resp) {
                    $('#productSearchModal').modal({show: true});
                    $("#productSearchContent").html(resp);
                }
            });
        }

        function searchProduct() {
            var searchTerm = $('#productSearchTerm').val();
            var searchBy = $('#productSearchBy').val();
            var supplierId = $('#supplierId').val();
            var productListId = $('#productListId').val();
            if (searchBy === "barcode" && searchTerm.length < 4) {
                alert("Please enter at least 4 digits of a barcode.");
                return;
            }

            $.ajax({
                url: "${createLink(controller: 'order', action: 'ajaxSearchProducts')}",
                data: { searchTerm: searchTerm, searchBy: searchBy, supplierId: supplierId, productListId: productListId },
                success: function(resp) {
                    $("#product-search-results").hide();
                    $("#product-loading-indicator").show();
                    $('#product-search-results-container').html(resp);
                    $('#productSearchTerm').data('prev',$('#productSearchTerm').val())
                    $('#productSearchBy').data('prev', $('#productSearchBy').val())
                }
            });
        }

        function selectSupplier(supplierId){
            var selectSupplierUrl = "${createLink(controller: 'order', action:'productList')}"
            $.ajax({
                url: selectSupplierUrl,
                data: { supplierId: supplierId, isNew: 1 },
                method: "GET",
                statusCode: {
                    200: function (response) {
                        //Here re-route to remove browser appearing URL --> Remove supplier id and isNew Params
                        window.location.href = '${createLink(controller: 'order', action:'productList')}';
                    }
                }
            });
        }

        function selectVariant(productId) {
            var selecVariantUrl = "${createLink(controller: 'order', action: 'ajaxSelectVariant')}";
            $("#showSkuContent").html("<div class=\"modal-body\"><div class=\"d-flex justify-content-center\">" +
                "<div id=\"loadingIndicator\" class=\"spinner-border\" role=\"status\"><span class=\"sr-only\">" +
                "Loading...</span></div></div></div>");
            var supplierId = $('#supplierId').val();
            var productListId = $('#productListId').val();
            $.ajax({
                url: selecVariantUrl,
                data: { productId: productId, supplierId: supplierId, productListId: productListId },
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

        function ajaxSearchSuppliers() {
            var searchInput = document.getElementById('supplierSearchInput').value;
            var params = {searchTerm: searchInput, offset: 0, max: 50, sortColumn: "name", sortOrder: "asc"};
            $.ajax({
                url: "${createLink(controller: 'order', action: 'ajaxSupplierSearch')}",
                method: 'GET',
                data: params,
                success: function(response) {
                    $("#supplierListView").html(response);
                }
            });
        }

        function complete(){
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

        function deleteOrder(){
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

        function deleteOrderItem(event){
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

            // Stop the event propagation to prevent the click event from reaching other elements
            event.stopPropagation();
        }

        //Order approve functions --> Approve order confirm and cancel order confirm
        function approveConfirm(){
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
                        var noDisplayDiv = $('<div class="row col-8 offset-2 pt-2 pb-2 text-center emptyProductListItems" id="emptyProductListItems"> \
                                            <div class="col pt-2 pb-2 text-center my-auto wl-striped0">All products processed.</div> \
                                        </div>')

                        $( "div" ).remove( ".productListItems" );
                        $('#search-results').append(noDisplayDiv);
                        $("#orderConfirmContent").html(response);
                    }
                }
            });
        }

        //Order delete functions --> confirm order delete and cancel order delete
        function confirmOrderDelete(){
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
                        window.location.href = "${createLink(controller: 'order', action: 'productList')}";
                    }
                }
            });
        }

        //Order delete functions --> confirm order delete and cancel order delete
        function confirmOrderItemDelete(productItemId){
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
                        window.location.href = "${createLink(controller: 'order', action: 'productList')}";
                    }
                }
            });
        }

        function cancelSupplierView(){
            window.location.href = '${createLink(controller: 'reporting', action:'orders')}';
        }

        function cancelConfirmResponse(){
            window.location.href = "${createLink(controller: 'reporting', action: 'orders')}";
        }

        function cancelOrderDelete(){
            $('#productListModal').modal('hide');
        }

        function cancelOrderConfirm(){
            $('#productListModal').modal('hide');
        }

        function cancelOrderConfirmError(){
            $('#orderConfirmModal').modal('hide');
        }

        function cancelOrderDeleteError(){
            $('#orderConfirmModal').modal('hide');
        }

        function cancelOrderItemDelete(){
            $('#productListModal').modal('hide');
        }

    </script>
</head>
<body>

    <g:hiddenField name="supplierId" id="supplierId" value="${supplier?.id ?: 0}" />
    <g:hiddenField name="productListId" id="productListId" value="${productList?.id ?: 0}" />

    <section id="breadcrumb-container" class="container-fluid">
        <nav aria-label="breadcrumb">
            <div class="row mt-4">
                <div class="col">
                    <ol class="breadcrumb">
                        <li id="breadcrumb-1" class="breadcrumb-item"><g:link uri="/">Home</g:link></li>
                        <li id="breadcrumb-2" class="breadcrumb-item"><g:link controller="reporting" action="orders" >Orders Report</g:link></li>
                        <li id="breadcrumb-3" class="breadcrumb-item active" aria-current="page">${supplier?.name}</li>
                    </ol>
                </div>
            </div>
        </nav>
    </section>

    <section id="order-list-container" class="container-fluid" style="z-index: 1;">

        <div class="row header-wl mt-3" >
            <div class="col-8 offset-2">
                <h2 class="mx-auto my-auto">Order List</h2>
            </div>

            <div class="col-2 text-right">
                    <button id="add-product-button" class="btn btn-wl" onclick="AddProduct();">Add Product</button>
            </div>

        </div>

        <div id="order-list-results-container" class="align-content-center container-fluid" >
            <g:render template="/order/productListResults" model="[supplier: supplier, productList: productList,
                                                                   productListItems: productList?.getProductListItems()]" />
        </div>

    </section>

    <section id="productSearch-modal" class="container-fluid" >
        <div class="modal fade" id="productSearchModal" tabindex="-1" role="dialog" aria-labelledby="productSearchModalLabel" data-backdrop="false" aria-hidden="true">
            <div class="modal-dialog modal-lg" role="document" style="border: 2px black solid; margin-top: 120px">
                <div id="productSearchContent" class="modal-content" ></div>
            </div>
        </div>
    </section>

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