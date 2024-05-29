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
        function increment(id) {
            var packLineSelector = "#packLines\\[" + id + "\\]\\.";
            var value = parseInt($(packLineSelector + "quantity").val());
            value = isNaN(value) ? 0 : value;
            value++;
            $(packLineSelector + "quantity").val(value);
            if (value > $(packLineSelector + "quantity")[0].max) {
                $(packLineSelector + "quantity").val($(packLineSelector + "quantity")[0].max);
            }
        }

        function decrement(id) {
            var packLineSelector = "#packLines\\[" + id + "\\]\\.";
            var value = parseInt($(packLineSelector + "quantity").val());
            value = isNaN(value) ? 0 : value;
            if (value > 0) {
                value--;
            }
            $(packLineSelector + "quantity").val(value);
        }

        function save() {
            var supplierId = $('#supplierId').val();
            var productListId = $('#productListId').val();
            var productVariantId = $('#productVariantId').val();
            var productItemId = $('#productItemId').val();
            var params = {
                supplierId: supplierId,
                productListId: productListId,
                productVariantId: productVariantId,
                productItemId: productItemId
            };
            quantity = 0
            packLineIndex = 0
            $("#variants").find("div").each(function () {
                var innerDivId = $(this).attr("id");
                var packLineSelector = "#packLines\\[" + innerDivId + "\\]\\.";
                if ($(packLineSelector + "quantity").val() > 0) {
                    params["packLines[" + packLineIndex + "].orderCode"] = $(packLineSelector + "orderCode").val();
                    params["packLines[" + packLineIndex + "].id"] = $(packLineSelector + "id").val();
                    params["packLines[" + packLineIndex + "].orderCode"] = $(packLineSelector + "orderCode").val();
                    params["packLines[" + packLineIndex + "].quantity"] = $(packLineSelector + "quantity").val();
                    quantity = quantity + parseInt($(packLineSelector + "quantity").val()) * parseInt($(packLineSelector + "size").val())
                    packLineIndex++;
                }
            });
            params["quantity"] = quantity
            if(quantity > 0){
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
                            window.location.href = window.location.href = '${createLink(controller: 'order', action:'productList')}';
                        }
                    }
                });
            }else {
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
                        <li id="breadcrumb-2" class="breadcrumb-item"><g:link controller="reporting" action="orders" >Orders Report</g:link></li>
                        <li id="breadcrumb-3" class="breadcrumb-item"><g:link controller="order" action="productList" >Orders List</g:link></li>
                        <li id="breadcrumb-4" class="breadcrumb-item active" aria-current="page">${variants?.product?.description}</li>
                    </ol>
                </div>
            </div>
        </nav>
    </section>

    <section id="order-list-container" class="container-fluid">

        <div class="row header-wl mt-3">
            <div class="col-8 offset-2">
                <h2 class="mx-auto my-auto">Order List Item</h2>
            </div>

            <div class="col-2 text-right">
                <button id="cancel" class="btn btn-wl" name="save" onclick="document.location.href='${createLink(controller: 'order', action:'productList')}';">Cancel</button>
                <g:if test="${(packs && packs?.size()>0) || isNoSymbolOrders}">
                    <button id="save" class="btn btn-success" name="save" onclick="save()">Save</button>
                </g:if>
                <g:else>
                    <button id="save" class="btn btn-success" disabled name="save" onclick="save()">Save</button>
                </g:else>

            </div>

        </div>

        <g:hiddenField name="id" value="${product?.id ?: 0}" />
        <g:hiddenField name="supplierId"  id="supplierId" value="${supplierId ?: 0}" />
        <g:hiddenField name="productListId"  id="productListId" value="${productListId ?: 0}" />
        <g:hiddenField name="productVariantId" id="productVariantId" value="${variants?.id ?: 0}" />
        <g:hiddenField name="productItemId" id="productItemId" value="${productItemId ?: 0}" />

        <g:set var="isWeighted" value="${variants?.product?.weightedItem ?: false}"/>

        <div id="collapseProductVariants1"  aria-labelledby="productVariants" data-parent="#accordion">
            <div class="card-body py-5">

                <div class="row mx-5" style="width: 100%; margin-bottom: 50px">
                    <label for="productName" class="col-2 col-form-label text-left">Product Name </label>
                    <g:textField name="productName" class="col-8 form-control bottom-border text-left" readonly="true" value="${variants?.product?.description}" autocomplete="off" />
                </div>

                <div class="row mx-5" style="width: 100%; margin-bottom: 50px">
                    <label for="productName" class="col-2 col-form-label text-left">SKU </label>
                    <g:textField name="sku" class="col-8 form-control bottom-border text-left" readonly="true" value="${variants?.sku}" autocomplete="off" />
                </div>

                <div id="variantsContainer" style="max-height: 400px; overflow-x: auto; overflow-y: auto;">
                    <div class="row mx-5  hoverable" title="Click to edit." style="cursor: pointer;" >
                        <label for="productName" class="col-2 col-form-label text-left"> </label>
                        <div id="variants" class="col-9 my-auto" >
                            <!--This is for non symbol group orders, quantities are calculated in server and passed into view-->
                            <g:if test="${isNoSymbolOrders}">
                                <div class="quantity-${packSingles}" id="${packSingles}" style="width: 100%; margin-bottom: 30px" >
                                    <button id="decrementSinglesButton" class="counterButton" onclick="decrement(${packSingles})">-</button>
                                    <g:hiddenField name="packLines[${packSingles}].id" id="packLines[${packSingles}].id" value="0" />
                                    <g:hiddenField name="packLines[${packSingles}].orderCode" id="packLines[${packSingles}].orderCode" value="-1" />
                                    <g:hiddenField name="packLines[${packSingles}].size" id="packLines[${packSingles}].size" value="1" />
                                    <input name="packLines[${packSingles}].quantity" id="packLines[${packSingles}].quantity" type="number" class="quantity__input" value="${singleQuantity}"
                                           min="0" max="999999" style="width: 100px"
                                           onkeydown="acceptQuantity(event, ${isWeighted})" onkeyup="validateQuantity(this, 0, 999999, ${isWeighted})">
                                    <button id="incrementSinglesButton" class="counterButton" onclick="increment(${packSingles})" >+</button>
                                    <span id="packQty">x <g:if test="${isWeighted}">Kilograms</g:if><g:else>Singles</g:else></span>
                                </div>
                            </g:if>
                            <g:if test="${(packs && packs?.size()>0) || isNoSymbolOrders}">
                                <g:each in="${packs}" var="pack" status="i">
                                    <div class="quantity-${pack.id}" id="${pack.id}" style="width: 100%; margin-bottom: 30px" >
                                        <button id="decrementButton" class="counterButton" onclick="decrement(${pack.id})">-</button>
                                        <g:hiddenField name="packLines[${pack.id}].id" id="packLines[${pack.id}].id" value="${pack?.id ?: 0}" />
                                        <g:hiddenField name="packLines[${pack.id}].orderCode" id="packLines[${pack.id}].orderCode" value="${pack?.orderCode ?: ''}" />
                                        <g:hiddenField name="packLines[${pack.id}].size" id="packLines[${pack.id}].size" value="${pack?.quantity ?: 0}" />
                                        <input name="packLines[${pack.id}].quantity" id="packLines[${pack.id}].quantity" type="number" class="quantity__input" value="${pack?.getQuantity(packLinesList)}"
                                            min="0" max="${pack.maximumOrderQuantity}" style="width: 100px" onkeydown="acceptQuantity(event, false)" onkeyup="validateQuantity(this, 0, 999999, false)">
                                        <button id="incrementButton" class="counterButton" onclick="increment(${pack.id})" >+</button>
                                        <span id="packQty">x ${pack.quantity} Packs</span>
                                    </div>
                                </g:each>
                            </g:if>
                            <g:else>
                                <h3>No Packs Available For This Product</h3>
                            </g:else>
                        </div>
                    </div>
                </div>

            </div>
        </div>

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