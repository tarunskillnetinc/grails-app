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
    <asset:javascript src="validators/input-validator.js"/>

    <style>
        .quantity__input {
            width: 60px;
            height: 40px;
            font-size: 20px;
            text-align: center;
        }

        .counterButton {
            width: 60px;
            height: 40px;
            font-size: 20px;
            text-align:center;
        }

        #packQty{
            font-size: 20px;
        }

        input[type="number"]::-webkit-inner-spin-button{
            display: none;
        }

    </style>

    <script type='text/javascript'>
        function increment(id, weighted) {
            const input = $("#packLines\\[" + id + "\\]\\.quantity");
            let value = weighted ? parseFloat(input.val()) : parseInt(input.val());
            value = isNaN(value) ? 0 : value;
            if (weighted) {
                value = (value + 0.001).toFixed(3)
            } else {
                value++
            }
            input.val(value > input[0].max ? input[0].max : value);
        }

        function decrement(id, weighted) {
            const input = $("#packLines\\[" + id + "\\]\\.quantity");
            let value = weighted ? parseFloat(input.val()) : parseInt(input.val());
            value = isNaN(value) ? 0 : value;
            if (value > 0 && weighted) {
                value = (value - 0.001).toFixed(3);
            } else if (value > 0) {
                value--
            }
            input.val(value);
        }

        function save(weighted) {
            var productListId = $('#productListId').val();
            var productListItemId = $('#productListItemId').val();
            var productVariantId = $('#productVariantId').val();
            var params = {
                productListId: productListId,
                productListItemId: productListItemId,
                productVariantId: productVariantId
            };
            let quantity = 0
            let packLineIndex = 0

            $("#variants").find("div").each(function () {
                var innerDivId = $(this).attr("id");
                var packLineSelector = "#packLines\\[" + innerDivId + "\\]\\.";
                if ($(packLineSelector + "quantity").val() > 0) {
                    params["packLines[" + packLineIndex + "].orderCode"] = $(packLineSelector + "orderCode").val();
                    params["packLines[" + packLineIndex + "].id"] = $(packLineSelector + "id").val();
                    params["packLines[" + packLineIndex + "].packId"] = $(packLineSelector + "packId").val();
                    params["packLines[" + packLineIndex + "].orderCode"] = $(packLineSelector + "orderCode").val();
                    params["packLines[" + packLineIndex + "].quantity"] = $(packLineSelector + "quantity").val();
                    quantity += weighted
                        ? parseFloat($(packLineSelector + "quantity").val()) * parseFloat($(packLineSelector + "size").val())
                        : parseInt($(packLineSelector + "quantity").val()) * parseInt($(packLineSelector + "size").val())
                    packLineIndex++;
                }
            });

            if (weighted) {
                quantity = quantity.toFixed(3)
            }

            params["quantity"] = quantity

            if (quantity > 0) {
                $.ajax({
                    url: "${createLink(controller: 'order', action: 'ajaxSavePackLines')}",
                    method: "POST",
                    data: params,
                    statusCode: {
                        500: function (response) {
                            $('#productListItemModal').modal({show: true});
                            $("#productListItemContent").html(response.responseText);
                        },
                        200: function (response) {
                            window.location.href = '${createLink(controller: 'order', action:'productList', id: productList.id)}';
                        }
                    }
                });
            } else {
                $.ajax({
                    url: "${createLink(controller: 'order', action: 'ajaxShowQuantityWarningWindow')}",
                    method: "GET",
                    statusCode: {
                        200: function (response) {
                            $('#productListItemModal').modal({show: true});
                            $("#productListItemContent").html(response);
                        }
                    }
                });
            }
        }

        function cancelPackLineSaveError(){
            $('#productListItemModal').modal('hide');
        }

        function cancelQuantityWarningError(){
            $('#productListItemModal').modal('hide');
        }
    </script>

</head>
<body>

    <section id="breadcrumb-container" class="container-fluid">
        <nav aria-label="breadcrumb">
            <div class="row mt-4">
                <div class="col">
                    <ol class="breadcrumb">
                        <li id="breadcrumb-1" class="breadcrumb-item"><g:link uri="/">Home</g:link></li>
                        <li id="breadcrumb-2" class="breadcrumb-item"><g:link controller="reporting" action="orders">All Orders</g:link></li>
                        <li id="breadcrumb-3" class="breadcrumb-item"><g:link controller="reporting" action="order" id="${productList?.id}">${productList?.supplierReference}</g:link></li>
                        <li id="breadcrumb-4" class="breadcrumb-item"><g:link controller="order" action="edit" id="${productList?.id}">Edit ${productList?.supplierReference}</g:link></li>
                        <li id="breadcrumb-5" class="breadcrumb-item active" aria-current="page">${productVariant?.product?.description}</li>
                    </ol>
                </div>
            </div>
        </nav>
    </section>

    <section id="order-list-container" class="container-fluid">

        <g:set var="isWeighted" value="${productVariant?.product?.weightedItem ?: false}" />

        <g:form method="post" action="saveProductListItem" class="mt-5" name="product-list-item-form">
            <div class="row header-wl mt-3">
                <div class="col-8 offset-2">
                    <h2 class="mx-auto my-auto">Order List Item</h2>
                </div>

                <div class="col-2 text-right">
                    <g:link id="cancel" class="btn btn-wl" controller="order" action="edit" id="${productList?.id}">Cancel</g:link>

                    <g:if test="${packs?.size() > 0 || !isSymbolGroupOrder}">
                        <button type="submit" id="save" class="btn btn-success" name="save">Save</button>
                    </g:if>
                    <g:else>
                        <button type="button" id="save" class="btn btn-success" disabled name="save">Save</button>
                    </g:else>
                </div>
            </div>

            <g:hiddenField name="productListId" id="productListId" value="${productList?.id}" />
            <g:hiddenField name="productListItemId" id="productListItemId" value="${productListItem?.id ?: 0}" />
            <g:hiddenField name="productVariantId" id="productVariantId" value="${productVariant?.id ?: 0}" />

            <div id="collapseProductVariants1" aria-labelledby="productVariants" data-parent="#accordion">
                <div class="card-body py-5">
                    <div class="row form-group">
                        <label for="sku" class="col-12 col-md-2 offset-md-1 col-form-label text-right">SKU</label>
                        <g:textField name="sku" class="col-12 col-md-2 form-control bottom-border" readonly="true" value="${productVariant?.sku}" autocomplete="off" />
                    </div>

                    <div class="row form-group">
                        <label for="productName" class="col-12 col-md-2 offset-md-1 col-form-label text-right">Product Name</label>
                        <g:textField name="productName" class="col-12 col-md-6 form-control bottom-border" readonly="true" value="${productVariant?.product?.description}" autocomplete="off" />
                    </div>

                    <div id="variantsContainer" style="max-height: 400px; overflow-y: auto;" class="row form-group">
                        <label for="variants" class="col-12 col-md-2 offset-md-1 col-form-label text-right">Order Quantity</label>

                        <div id="variants" class="col-12 col-md-7">
                            <!--This is for non symbol group orders, quantities are calculated in server and passed into view-->
                            <g:if test="${!isSymbolGroupOrder}">
                                <div class="row form-inline quantity--1 mb-3" id="-1">
                                    <g:hiddenField name="packLines[0].id" id="packLines[0].id" value="0" />
                                    <g:hiddenField name="packLines[0].packId" id="packLines[0].packId" value="0" />
                                    <g:hiddenField name="packLines[0].orderCode" id="packLines[0].orderCode" value="0" />
                                    <g:hiddenField name="packLines[0].size" id="packLines[0].size" value="1" />

                                    <button type="button" id="decrementSinglesButton" class="btn btn-wl" onclick="decrement(0, ${isWeighted})">-</button>
                                    <input name="packLines[0].quantity" id="packLines[0].quantity" type="number" class="form-control bottom-border text-center"
                                           value="${singleQuantity.remainder(BigDecimal.ONE) == BigDecimal.ZERO ? singleQuantity.setScale(0) : singleQuantity}"
                                           min="0" max="999999" style="width: 100px"
                                           onkeydown="acceptQuantity(event, ${isWeighted})" onkeyup="validateQuantity(this, 0, 999999, ${isWeighted})">
                                    <button type="button" id="incrementSinglesButton" class="btn btn-wl" onclick="increment(0, ${isWeighted})" >+</button>
                                    <span id="packQty" class="ml-2">x <g:if test="${isWeighted}">Kilograms</g:if><g:else>Singles</g:else></span>
                                </div>
                            </g:if>

                            <g:if test="${packs?.size() > 0 || !isSymbolGroupOrder}">
                                <g:each in="${packs}" var="pack" status="i">
                                    <div class="row form-inline quantity-${i+1} mb-3" id="${i+1}">
                                        <g:hiddenField name="packLines[${i+1}].id" id="packLines[${i+1}].id" value="${productListItem?.packLines?.find { it?.orderCode == pack?.orderCode }?.id ?: 0}" />
                                        <g:hiddenField name="packLines[${i+1}].packId" id="packLines[${i+1}].packId" value="${pack?.id ?: 0}" />
                                        <g:hiddenField name="packLines[${i+1}].orderCode" id="packLines[${i+1}].orderCode" value="${pack?.orderCode ?: ''}" />
                                        <g:hiddenField name="packLines[${i+1}].size" id="packLines[${i+1}].size" value="${pack?.quantity ?: 0}" />

                                        <button type="button" id="decrementButton" class="btn btn-wl" onclick="decrement(${i+1}, false)">-</button>
                                        <input name="packLines[${i+1}].quantity" id="packLines[${i+1}].quantity" type="number" class="form-control bottom-border text-center" value="${productListItem?.packLines?.find { it?.orderCode == pack?.orderCode }?.quantity?.setScale(0) ?: 0}"
                                            min="0" max="${pack.maximumOrderQuantity ?: 99999}" style="width: 100px" onkeydown="acceptQuantity(event, false)" onkeyup="validateQuantity(this, 0, 99999, false)">
                                        <button type="button" id="incrementButton" class="btn btn-wl" onclick="increment(${i+1}, false)" >+</button>
                                        <span id="packQty" class="ml-2">x ${pack.quantity?.setScale(isWeighted ? 3 : 0)} Packs</span>
                                    </div>
                                </g:each>
                            </g:if>
                            <g:else>
                                <!-- Only show this message if there are no packs and this is a symbol group order (which doesn't support singles). -->
                                <h3>No packs available for this product.</h3>
                            </g:else>
                        </div>
                    </div>
                </div>
            </div>
        </g:form>
    </section>

    <section id="productListItem-modal" class="container-fluid">
        <div class="modal fade" id="productListItemModal" tabindex="-1" role="dialog" aria-labelledby="productListItemModalLabel" data-backdrop="false"  aria-hidden="true">
            <div class="modal-dialog modal-lg" style="border: 2px black solid; margin-top: 120px" role="document">
                <div id="productListItemContent" class="modal-content"></div>
            </div>
        </div>
    </section>
</body>
</html>