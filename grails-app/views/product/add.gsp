<%@ page contentType="text/html;charset=UTF-8" %>
<html>
    <head>
        <meta name="layout" content="main" />

        <title>WonderLane Product Maintenance</title>

        <asset:stylesheet href="radio.css" />
        <asset:javascript src="money-mask.js" />

        <script type="text/javascript">
            var addVariantUrl = "${createLink(controller: 'product', action: 'ajaxAddVariant')}";
            var addBarcodeUrl = "${createLink(controller: 'product', action: 'ajaxAddBarcode')}";
            var saveVariantUrl = "${createLink(controller: 'product', action: 'ajaxSaveVariant')}";
            var suppliersUrl = "${createLink(controller: 'product', action: 'ajaxSuppliers')}";
            var addPackUrl = "${createLink(controller: 'product', action: 'ajaxAddPack')}";
            var savePackUrl = "${createLink(controller: 'product', action: 'ajaxSavePack')}";
            var getChildCategoriesUrl = "${createLink(controller: 'product', action: 'ajaxGetChildCategories')}";
            var getPromotionsUrl = "${createLink(controller: 'promotion', action: 'ajaxGetPromotionsForProduct')}";

            $(document).ready(function () {
                // Enable the VAT override when "Other" is selected.
                $('#vatCode').change(function() {
                    var vatCode = $('#vatCode option:selected').attr("data-code");

                    var vatPercentageOverride = $('#vatPercentageOverride');
                    vatPercentageOverride.attr("readonly", vatCode !== 'O');
                    vatPercentageOverride.val("0.00");
                });

                // Enable the min and max open price entries when open price is selected.
                $("#openPrice").change(function() {
                    $("#restrictions\\.minOpenPrice").attr("readonly", !this.checked);
                    $("#restrictions\\.maxOpenPrice").attr("readonly", !this.checked);
                });

                $("#restrictions\\.buyerIdRequired").change(function() {
                    $("#restrictions\\.buyerIdForced").prop("checked", false);
                    $("#restrictions\\.buyerIdForced").attr("disabled", !this.checked);
                    $("#restrictions\\.buyerAgeRestriction").val("");
                    $("#restrictions\\.buyerAgeRestriction").attr("readonly", !this.checked);
                    $("#restrictions\\.buyerChallengeAge").val("");
                    $("#restrictions\\.buyerChallengeAge").attr("readonly", !this.checked);
                    $("#restrictions\\.sellerAgeRestriction").val("");
                    $("#restrictions\\.sellerAgeRestriction").attr("readonly", !this.checked);
                });

                $(".mask-money").maskMoney({ allowZero: true });

                $('#collapsePromotions').on('show.bs.collapse', function () {
                    getPromotions(${product?.id});
                });
            });

            // Automatically populate the first SKU with the main product item code since it's mostly a 1-1 relationship.
            function itemCodeChanged(itemCode) {
                var sku = $("#variants\\[0\\]\\.sku");

                if (sku != null && (sku.val() === null || sku.val() === "")) {
                    sku.val(itemCode);

                    $("#variants\\[0\\]\\.skuText").html(itemCode);
                }
            }

            // Expand or collapse the category and show all children categories.
            function expandCollapseCategory(categoryId, level, selectedCategoryId) {
                event.preventDefault();

                var plusMinusButton = $("#plusMinus-" +categoryId);
                var expanded = plusMinusButton.attr("aria-expanded");

                if (expanded === "true") {
                    plusMinusButton.text("+");
                    plusMinusButton.attr("aria-expanded", "false");

                    $("#categoryContainer-" +categoryId).html("");
                } else {
                    var params = {};

                    params["categoryId"] = categoryId;
                    params["level"] = level;
                    params["selectedCategoryId"] = selectedCategoryId;

                    $.ajax({
                        url: getChildCategoriesUrl,
                        method: "GET",
                        data: params,
                        success: function(resp) {
                            plusMinusButton.text("-");
                            plusMinusButton.attr("aria-expanded", "true");

                            $("#categoryContainer-" +categoryId).html(resp);
                        }
                    });
                }
            }

            // Displays the add/edit variant modal depending whether you've clicked the add button or clicked an existing row.
            function addVariant(index) {
                $("#addVariantContent").html("<div class=\"modal-body\"><div class=\"d-flex justify-content-center\"><div id=\"loadingIndicator\" class=\"spinner-border\" role=\"status\"><span class=\"sr-only\">Loading...</span></div></div></div>");
                $('#addVariantModal').modal({ show: true });

                var params = {};

                if (index != null) {
                    var selector = "#variants\\[" +index +"\\]\\.";

                    params["id"] = $(selector + "id").val();
                    params["sku"] = $(selector + "sku").val();
                    params["retailPrice"] = $(selector + "retailPrice").val();
                    params["costPrice"] = $(selector + "costPrice").val();
                    params["size"] = $(selector + "size").val();
                    params["colour"] = $(selector + "colour").val();

                    var barcodeContainers = $($(selector + "barcodesContainer > div"));
                    barcodeContainers.each(function(loopIndex) {
                        var barcodeIndex = parseInt($(this).attr("id").substring(16));

                        params["barcodes[" +loopIndex +"].id"] = $(selector + "barcodes\\[" +barcodeIndex +"\\]\\.id").val();
                        params["barcodes[" +loopIndex +"].barcode"] = $(selector + "barcodes\\[" +barcodeIndex +"\\]\\.barcode").val();
                        params["barcodes[" +loopIndex +"].effectiveDate"] = $(selector + "barcodes\\[" +barcodeIndex +"\\]\\.effectiveDate").val();
                        params["barcodes[" +loopIndex +"].recordStatus"] = $(selector + "barcodes\\[" +barcodeIndex +"\\]\\.recordStatus").val();
                    });
                } else {
                    var lastVariantContainer = $("#variantsContainer > div:last-child");

                    if (lastVariantContainer.length > 0) {
                        index = parseInt(lastVariantContainer[0].id.substring(8)) + 1;
                    } else {
                        index = 0;
                    }
                }

                params["index"] = index;

                $.ajax({
                    url: addVariantUrl,
                    method: "POST",
                    data: params,
                    success: function(resp) {
                        $("#addVariantContent").html(resp);
                    }
                });
            }

            // Handle the "Ok" of the add/edit variant modal which puts the values into the form ready for submission as part of the whole page.
            function saveVariant(index) {
                var id = $("#addVariantId").val();
                var sku = $("#addVariantSku").val();
                var retailPrice = $("#addVariantRetailPrice").val();
                var costPrice = $("#addVariantCostPrice").val();
                var size = $("#addVariantSize").val();
                var colour = $("#addVariantColour").val();

                var params = { index: index, id: id, sku: sku, retailPrice: retailPrice, costPrice: costPrice, size: size, colour: colour };

                var addBarcodeContainers = $("#addBarcodesContainer > div");
                addBarcodeContainers.each(function(loopIndex) {
                    var barcodeIndex = $(this).attr("id").substring(10);

                    params["barcodes[" +loopIndex +"].id"] = $("#addVariantBarcodes\\[" +barcodeIndex +"\\]\\.id").val();
                    params["barcodes[" +loopIndex +"].barcode"] = $("#addVariantBarcodes\\[" +barcodeIndex +"\\]\\.barcode").val();
                    params["barcodes[" +loopIndex +"].effectiveDate"] = $("#addVariantBarcodes\\[" +barcodeIndex +"\\]\\.effectiveDate").val();
                    params["barcodes[" +loopIndex +"].recordStatus"] = $("#addVariantBarcodes\\[" +barcodeIndex +"\\]\\.recordStatus").val();
                });

                // TODO Go and look for existing packs and add those to the params otherwise they will be lost.

                $.ajax({
                    url: saveVariantUrl,
                    method: "POST",
                    data: params,
                    success: function(resp) {
                        var variantContainer = $("#variantsContainer > #variant-" +index);

                        if (variantContainer.length === 0) {
                            $("#variantsContainer").append("<div id=\"variant-" +index +"\"></div>");

                            variantContainer = $("#variantsContainer > #variant-" +index);
                        }

                        variantContainer.html(resp);
                    }
                });

                $('#addVariantModal').modal("hide");
            }

            // Delete variant button was clicked, we just delete the whole div and handle the removal server side (if it was an existing variant).
            function deleteVariant(index) {
                if (!confirm("This SKU will be deleted.")) {
                    return;
                }

                $("#variantsContainer > #variant-" +index).remove();

                $("#variantsContainer > div").each(function(i) {
                    var stripedDiv = $(this).find("div:first");

                    stripedDiv.removeClass("wl-striped0");
                    stripedDiv.removeClass("wl-striped1");
                    stripedDiv.addClass("wl-striped" +(i % 2));
                });
            }

            // Add barcode button was clicked, this just adds a new empty textbox.
            function addBarcode() {
                var lastBarcodeContainer = $("#addBarcodesContainer > div:last-child");

                if (lastBarcodeContainer.length > 0) {
                    index = parseInt(lastBarcodeContainer[0].id.substring(10)) + 1;
                } else {
                    index = 0;
                }

                var params = { index: index };

                $.ajax({
                    url: addBarcodeUrl,
                    method: "POST",
                    data: params,
                    success: function(resp) {
                        var barcodeContainer = $("#addBarcodesContainer > #addBarcode" +index);

                        if (barcodeContainer.length === 0) {
                            $("#addBarcodesContainer").append("<div id=\"addBarcode" +index +"\"></div>");

                            barcodeContainer = $("#addBarcodesContainer > #addBarcode" +index);
                        }

                        barcodeContainer.html(resp);
                    }
                });
            }

            // Delete barcode button was clicked, we just remove the div.
            function deleteBarcode(index) {
                $("#addBarcodesContainer > #addBarcode" +index).remove();
            }

            // The suppliers button was clicked, we display the suppliers modal for this variant.
            function showSuppliersModal(variantIndex) {
                $("#suppliersContent").html("<div class=\"modal-body\"><div class=\"d-flex justify-content-center\"><div id=\"loadingIndicator\" class=\"spinner-border\" role=\"status\"><span class=\"sr-only\">Loading...</span></div></div></div>");
                $('#suppliersModal').modal({ show: true });

                var params = {};
                params["index"] = variantIndex;

                var packContainers = $("#variants\\[" +variantIndex +"\\]\\.packsContainer > div");

                packContainers.each(function(loopIndex) {
                    var packIndex = $(this).attr("id").substring(13);
                    var packSelector = "#variants\\[" +variantIndex +"\\]\\.packs\\[" +packIndex +"\\]";

                    params["packs[" +loopIndex +"].index"] = loopIndex;
                    params["packs[" +loopIndex +"].id"] = $(packSelector +"\\.id").val();
                    params["packs[" +loopIndex +"].supplier.id"] = $(packSelector +"\\.supplier\\.id").val();
                    params["packs[" +loopIndex +"].supplier.name"] = $(packSelector +"\\.supplier\\.name").val();
                    params["packs[" +loopIndex +"].quantity"] = $(packSelector +"\\.quantity").val();
                    params["packs[" +loopIndex +"].price"] = $(packSelector +"\\.price").val();
                    params["packs[" +loopIndex +"].orderCode"] = $(packSelector +"\\.orderCode").val();
                    params["packs[" +loopIndex +"].barcode"] = $(packSelector +"\\.barcode").val();
                    params["packs[" +loopIndex +"].recommendedRetailPrice"] = $(packSelector +"\\.recommendedRetailPrice").val();
                    params["packs[" +loopIndex +"].effectiveDate"] = $(packSelector +"\\.effectiveDate").val();
                    params["packs[" +loopIndex +"].effectiveEndDate"] = $(packSelector +"\\.effectiveEndDate").val();
                    params["packs[" +loopIndex +"].status"] = $(packSelector +"\\.status").val();
                    params["packs[" +loopIndex +"].maximumOrderQuantity"] = $(packSelector +"\\.maximumOrderQuantity").val();
                    params["packs[" +loopIndex +"].allowSubstitutes"] = $(packSelector +"\\.allowSubstitutes").val();
                });

                $.ajax({
                    url: suppliersUrl,
                    method: "POST",
                    data: params,
                    success: function(resp) {
                        $("#suppliersContent").html(resp);
                    }
                });
            }

            // If an existing row was clicked, then hidden form is displayed, otherwise a whole new blank "add pack" row is added.
            function addPack(variantIndex, packIndex) {
                if (packIndex != null) {
                    var packContainer = $("#addPackTextContainer-" + variantIndex + "-" + packIndex);
                    var addPackContainer = $("#addPackFieldsContainer-" + variantIndex + "-" + packIndex);

                    packContainer.addClass("hidden");
                    addPackContainer.removeClass("hidden");
                } else {
                    var lastPackContainer = $("#addPacksContainer-" +variantIndex +" > div:last-child");
                    packIndex = 0;

                    if (lastPackContainer.length > 0) {
                        packIndex = parseInt(lastPackContainer[0].id.substring(lastPackContainer[0].id.lastIndexOf("-") + 1)) + 1;
                    }

                    $.ajax({
                        url: addPackUrl,
                        method: "POST",
                        data: { variantIndex: variantIndex, packIndex: packIndex },
                        success: function(resp) {
                            var addPacksContainer = $("#addPacksContainer-" +variantIndex);
                            addPacksContainer.append("<div id=\"addPackContainer-" +variantIndex +"-" +packIndex +"\"></div>");

                            var addPackContainer = $("#addPackContainer-" +variantIndex +"-" +packIndex);

                            addPackContainer.append(resp);
                        }
                    });
                }
            }

            // The "Ok" button was clicked on the suppliers modal, this adds all of those values back onto the form ready for saving as part of the overall page save.
            function savePacks(variantIndex) {
                var params = { index: variantIndex };

                var addPackContainers = $("#addPacksContainer-" +variantIndex +" > div");
                addPackContainers.each(function(loopIndex) {
                    var packIndex = $(this).attr("id").substring($(this).attr("id").lastIndexOf("-") + 1);
                    var packSelector = "#addPack\\[" +packIndex +"\\]";

                    params["packs[" +loopIndex +"].index"] = loopIndex;
                    params["packs[" +loopIndex +"].id"] = $(packSelector +"\\.id").val();
                    params["packs[" +loopIndex +"].supplier.id"] = $(packSelector +"\\.supplier\\.id").val();
                    params["packs[" +loopIndex +"].supplier.name"] = $(packSelector +"\\.supplier\\.name").val();
                    params["packs[" +loopIndex +"].quantity"] = $(packSelector +"\\.quantity").val();
                    params["packs[" +loopIndex +"].price"] = $(packSelector +"\\.price").val();
                    params["packs[" +loopIndex +"].orderCode"] = $(packSelector +"\\.orderCode").val();
                    params["packs[" +loopIndex +"].barcode"] = $(packSelector +"\\.barcode").val();
                    params["packs[" +loopIndex +"].recommendedRetailPrice"] = $(packSelector +"\\.recommendedRetailPrice").val();
                    params["packs[" +loopIndex +"].effectiveDate"] = $(packSelector +"\\.effectiveDate").val();
                    params["packs[" +loopIndex +"].effectiveEndDate"] = $(packSelector +"\\.effectiveEndDate").val();
                    params["packs[" +loopIndex +"].status"] = $(packSelector +"\\.status").val();
                    params["packs[" +loopIndex +"].maximumOrderQuantity"] = $(packSelector +"\\.maximumOrderQuantity").val();
                    params["packs[" +loopIndex +"].allowSubstitutes"] = $(packSelector +"\\.allowSubstitutes").val();
                });

                $.ajax({
                    url: savePackUrl,
                    method: "POST",
                    data: params,
                    success: function(resp) {
                        var packsContainer = $("#variants\\[" +variantIndex +"\\]\\.packsContainer");

                        packsContainer.html(resp);

                        $('#suppliersModal').modal("hide");
                    }
                });
            }

            function getPromotions(productId) {
                $('#promotionsContainer').html("<div class=\"d-flex justify-content-center\">\n" +
                    "  <div class=\"spinner-border\" role=\"status\">\n" +
                    "    <span class=\"sr-only\">Loading...</span>\n" +
                    "  </div>\n" +
                    "</div>");

                $.ajax({
                    url: getPromotionsUrl,
                    method: "GET",
                    data: { productId: productId },
                    success: function(resp) {
                        $("#promotionsContainer").html(resp);
                    }
                });
            }
        </script>
    </head>

    <body>
        <section id="breadcrumb-container" class="container-fluid">
            <nav aria-label="breadcrumb">
                <div class="row mt-4">
                    <div class="col">
                        <ol class="breadcrumb">
                            <li class="breadcrumb-item"><g:link uri="/">Home</g:link></li>
                            <li class="breadcrumb-item" aria-current="page"><g:link controller="product" action="index">Product Search</g:link></li>
                            <li class="breadcrumb-item active" aria-current="page">${product?.itemCode ?: "Add Product"}</li>
                        </ol>
                    </div>
                </div>
            </nav>
        </section>

        <div class="header-wl mt-3">
            <h2 class="mx-auto">Product Maintenance</h2>
        </div>

        <g:hasErrors bean="${product}">
            <section id="errors-container" class="container-fluid">
                <div class="alert alert-danger alert-wl mx-0" role="alert">
                    <g:renderErrors bean="${product}" as="list" />

                    <g:hasErrors bean="${product.restrictions}">
                        <g:renderErrors bean="${product.restrictions}" as="list" />
                    </g:hasErrors>

                    <g:each in="${product.variants.findAll { it.storeId == null || it.storeId == storeId }}" var="variant" status="i">
                        <g:hasErrors bean="${variant}">
                            <div class="ml-3 pl-3 border">
                                Variant ${i+1}
                                <g:renderErrors bean="${variant}" as="list" />

                                <g:each in="${variant.barcodes}" var="barcode" status="j">
                                    <g:hasErrors bean="${barcode}">
                                        <div class="ml-3">
                                            Barcode ${j+1}
                                            <g:renderErrors bean="${barcode}" as="list" />
                                        </div>
                                    </g:hasErrors>
                                </g:each>
                            </div>
                        </g:hasErrors>
                    </g:each>
                </div>
            </section>
        </g:hasErrors>

        <g:if test="${flash.message}">
            <section id="errors-container2" class="container-fluid">
                <div class="alert alert-success alert-wl mx-0" role="alert">${flash.message}</div>
            </section>
        </g:if>

        <section id="addProduct-section" class="container-fluid mt-4">
            <g:render template="addProductForm" model="[product: product,
                                                        storeId: storeId,
                                                        statusValues: statusValues,
                                                        categoryValues: categoryValues,
                                                        productCategoryList: productCategoryList,
                                                        vatValues: vatValues,
                                                        isNewProduct: isNewProduct]"/>
        </section>

        <section id="addVariant-modal" class="container-fluid">
            <!-- Add variant modal. -->
            <div class="modal fade" id="addVariantModal" tabindex="-1" role="dialog" aria-labelledby="addVariantModalLabel" aria-hidden="true">
                <div class="modal-dialog modal-lg" role="document">
                    <div id="addVariantContent" class="modal-content">

                    </div>
                </div>
            </div>
        </section>

        <section id="suppliers-modal" class="container-fluid">
            <!-- Suppliers modal. -->
            <div class="modal fade" id="suppliersModal" tabindex="-1" role="dialog" aria-labelledby="suppliersModalLabel" aria-hidden="true">
                <div class="modal-dialog modal-xl" role="document">
                    <div id="suppliersContent" class="modal-content">

                    </div>
                </div>
            </div>
        </section>
    </body>
</html>