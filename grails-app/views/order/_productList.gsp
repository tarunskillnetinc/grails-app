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

    <style>
        .ui-dialog-titlebar-close {
            display: none;
        }

        .ui-resizable-handle {
            background-image: none;
        }

    </style>

    <script type='text/javascript'>

        $( document ).ready(function() {

            var checkProductUrl = "${createLink(controller: 'order', action: 'ajaxCheckActiveProducts')}";

            $.ajax({
                url: checkProductUrl,
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

            $("#dialog-order-confirm").dialog({
                autoOpen: false,
                resizable: false,
                height: "auto",
                width: 400,
                modal: true,
                buttons: {
                    Yes: function () {
                        $('#orderConfirmModal').modal('show');
                        $("#orderConfirmContent").html("<div class=\"modal-body\"><div class=\"d-flex justify-content-center\">" +
                            "<div id=\"loadingIndicator\" class=\"spinner-border\" role=\"status\"><span class=\"sr-only\">" +
                            "Loading...</span></div></div></div>");
                        var completeProductUrl = "${createLink(controller: 'order', action: 'confirmOrder')}";
                        var supplierId = $('#supplierId').val();
                        var productListId = $('#productListId').val();
                        $.ajax({
                            url: completeProductUrl,
                            data: {supplierId: supplierId, productListId: productListId},
                            method: "POST",
                            statusCode: {
                                500: function (response) {
                                    $('#dialog-order-confirm-error').dialog('open')
                                    $("#orderConfirmContent").hide();
                                    $("#orderConfirmModal").hide();
                                    $('#dialog-order-confirm').dialog("close");
                                },
                                200: function (response) {

                                    var noDisplayDiv = $('<div class="row col-8 offset-2 pt-2 pb-2 text-center emptyProductListItems" id="emptyProductListItems"> \
                                            <div class="col pt-2 pb-2 text-center my-auto wl-striped0">All products processed.</div> \
                                        </div>')

                                    $( "div" ).remove( ".productListItems" );
                                    $('#search-results').append(noDisplayDiv);
                                    $("#orderConfirmContent").html(response);
                                    $('#dialog-order-confirm').dialog("close");
                                }
                            }
                        });
                    },
                    No: function () {
                        $(this).dialog("close")
                    }
                }
            });

            $("#dialog-order-list-delete").dialog({
                autoOpen: false,
                resizable: false,
                height: "auto",
                width: 400,
                modal: true,
                buttons: {
                    Yes: function () {
                        var deleteProductUrl = "${createLink(controller: 'order', action: 'deleteOrder')}";
                        var productListId = $('#productListId').val();
                        $.ajax({
                            url: deleteProductUrl,
                            data: {productListId: productListId},
                            method: "GET",
                            statusCode: {
                                500: function (response) {
                                    $('#dialog-order-delete-error').dialog('open');
                                },
                                200: function (response) {
                                    window.location.href = "${createLink(controller: 'order', action: 'productList')}";
                                }
                            }
                        });
                    },
                    No: function () {
                        $(this).dialog("close");
                    }
                }
            });

            $("#dialog-order-confirm-error").dialog({
                autoOpen: false,
                resizable: false,
                height: "auto",
                width: 400,
                modal: true,
                buttons: {
                    Ok: function () {
                        $(this).dialog("close");
                    }
                }
            });

            $("#dialog-order-delete-error").dialog({
                autoOpen: false,
                resizable: false,
                height: "auto",
                width: 400,
                modal: true,
                buttons: {
                    Ok: function () {
                        $(this).dialog("close");
                        $('#dialog-order-list-delete').dialog("close");
                    }
                }
            });
        });

        function AddProduct() {
            $("#productSearchContent").html("<div class=\"modal-body\"><div class=\"d-flex justify-content-center\">" +
                "<div id=\"loadingIndicator\" class=\"spinner-border\" role=\"status\"><span class=\"sr-only\">" +
                "Loading...</span></div></div></div>");
            $('#productSearchModal').modal({show: true});
            var addProductUrl = "${createLink(controller: 'order', action: 'ajaxAddProduct')}";
            $.ajax({
                url: addProductUrl,
                method: "GET",
                success: function (resp) {
                    $("#productSearchContent").html(resp);
                }
            });
        }

        function searchProduct() {
            var url = "${createLink(controller: 'order', action: 'ajaxSearchProducts')}";
            var searchTerm = $('#productSearchTerm').val();
            var searchBy = $('#productSearchBy').val();
            var supplierId = $('#supplierId').val();
            var productListId = $('#productListId').val();
            if (searchBy === "barcode" && searchTerm.length < 4) {
                alert("Please enter at least 4 digits of a barcode.");
                return;
            }

            $("#product-search-results").hide();
            $("#product-loading-indicator").show();

            $.ajax({
                url: url,
                data: { searchTerm: searchTerm, searchBy: searchBy, supplierId: supplierId, productListId: productListId },
                success: function(resp) {
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
            $('#showSkuModal').modal({show: true});
            $('#productSearchModal').modal('hide');
            var supplierId = $('#supplierId').val();
            var productListId = $('#productListId').val();
            $.ajax({
                url: selecVariantUrl,
                data: { productId: productId, supplierId: supplierId, productListId: productListId },
                method: "GET",
                statusCode: {
                    200: function (response) {
                        $("#showSkuContent").html(response);
                    }
                }
            });
        }

        function complete(){
            $('#dialog-order-confirm').dialog('open');
        }

        function deleteProductList(){
            $('#dialog-order-list-delete').dialog('open');
        }

        function cancelSupplierView(){
            window.location.href = '${createLink(controller: 'reporting', action:'orders')}';
        }

        function cancelConfirmResponse(){
            window.location.href = "${createLink(controller: 'order', action: 'productList')}";
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

    <section id="order-list-container" class="container-fluid">

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

    <section id="productSearch-modal" class="container-fluid">
        <div class="modal fade" id="productSearchModal" tabindex="-1" role="dialog" aria-labelledby="productSearchModalLabel"
             aria-hidden="true">
            <div class="modal-dialog modal-lg" role="document" style="margin-top: 120px">
                <div id="productSearchContent" class="modal-content" ></div>
            </div>
        </div>
    </section>

    <section id="showPopUp-modal" class="container-fluid" >
        <div class="modal fade" id="showSupplierModal" tabindex="-1" role="dialog" aria-labelledby="showSupplierModalLabel"
             aria-hidden="true" >
            <div class="modal-dialog modal-lg" role="document" style="width: 300px; margin-top: 120px">
                <div id="showSupplierContent" class="modal-content" ></div>
            </div>
        </div>
    </section>

    <section id="showSku-modal" class="container-fluid" >
        <div class="modal fade" id="showSkuModal" tabindex="-1" role="dialog" aria-labelledby="showSkuModalLabel" aria-hidden="true">
            <div class="modal-dialog modal-lg" role="document" style="width: 300px; margin-top: 120px">
                <div id="showSkuContent" class="modal-content" ></div>
            </div>
        </div>
    </section>

    <section id="orderConfirm-modal" class="container-fluid" >
        <div class="modal fade" id="orderConfirmModal" tabindex="-1" role="dialog" aria-labelledby="orderConfirmModalLabel" data-backdrop="false" aria-hidden="true" style="margin-top: 120px">
            <div class="modal-dialog modal-lg" style="border: 2px black solid" role="document" >
                <div id="orderConfirmContent" class="modal-content" ></div>
            </div>
        </div>
    </section>

    <div id="dialog-order-confirm" title="Confirm completion" style="display:none;">
        <p><span class="ui-icon ui-icon-alert" style="float:left; margin:12px 12px 20px 0;"></span>Do you want to complete this order? </p>
    </div>

    <div id="dialog-order-confirm-error" title="Confirm error" style="display:none;">
        <p><span class="ui-icon ui-icon-alert" style="float:left; margin:12px 12px 20px 0;"></span>Error confirming order </p>
    </div>

    <div id="dialog-order-list-delete" title="Confirm deletion" style="display:none;">
        <p><span class="ui-icon ui-icon-alert" style="float:left; margin:12px 12px 20px 0;"></span>Do you want to delete this order? </p>
    </div>

    <div id="dialog-order-delete-error" title="Delete error" style="display:none;">
        <p><span class="ui-icon ui-icon-alert" style="float:left; margin:12px 12px 20px 0;"></span>Error in deleting </p>
    </div>

</body>
</html>