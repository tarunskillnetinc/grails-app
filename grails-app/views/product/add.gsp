<%@ page contentType="text/html;charset=UTF-8" %>
<html>
    <head>
        <meta name="layout" content="main" />

        <title>WonderLane Product Maintenance</title>

        <asset:stylesheet href="radio.css" />
        <asset:stylesheet src="bootstrap-datepicker3.min.css" />
        <asset:javascript src="money-mask.js" />
        <asset:javascript src="bootstrap-datepicker.min.js" />

        <script type="text/javascript">
            var addVariantUrl = "${createLink(controller: 'product', action: 'ajaxAddVariant')}";
            var addBarcodeUrl = "${createLink(controller: 'product', action: 'ajaxAddBarcode')}";
            var saveVariantUrl = "${createLink(controller: 'product', action: 'ajaxSaveVariant')}";
            var addPriceUrl = "${createLink(controller: 'product', action: 'ajaxAddPrice')}";
            var suppliersUrl = "${createLink(controller: 'product', action: 'ajaxSuppliers')}";
            var locationsUrl = "${createLink(controller: 'product', action: 'ajaxLocations')}";
            var addPackUrl = "${createLink(controller: 'product', action: 'ajaxAddPack')}";
            var addLocationUrl = "${createLink(controller: 'product', action: 'ajaxAddLocation')}";
            var savePackUrl = "${createLink(controller: 'product', action: 'ajaxSavePack')}";
            var saveLocationUrl = "${createLink(controller: 'product', action: 'ajaxSaveLocation')}";
            var getChildCategoriesUrl = "${createLink(controller: 'product', action: 'ajaxGetChildCategories')}";
            var getPromotionsUrl = "${createLink(controller: 'promotion', action: 'ajaxGetPromotionsForProduct')}";

            $(document).ready(function () {
                $('#effectiveDate').datepicker({
                    format: "dd/mm/yyyy",
                    weekStart: 1,
                    startDate: new Date().toString(),
                    todayHighlight: true,
                    autoclose: true,
                    todayBtn: "linked",
                    orientation: "bottom auto"
                });

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

                $("#weightedItem").change(function() {
                    $("#pricePerKg").prop("checked", this.checked);
                    $("#pricePerKg").attr("disabled", !this.checked);
                    $("#pricePer100g").prop("checked", false);
                    $("#pricePer100g").attr("disabled", !this.checked);

                    var deliItem = $("#deliItem");

                    $("#openPrice").prop("checked", false);
                    $("#openPrice").attr("disabled", (this.checked || deliItem.prop("checked")));
                    $("#zeroPrice").prop("checked", false);
                    $("#zeroPrice").attr("disabled", (this.checked || deliItem.prop("checked")));
                });

                $("#deliItem").change(function() {
                    var weightedItem = $("#weightedItem");

                    $("#openPrice").prop("checked", false);
                    $("#openPrice").attr("disabled", (this.checked || weightedItem.prop("checked")));
                    $("#zeroPrice").prop("checked", false);
                    $("#zeroPrice").attr("disabled", (this.checked || weightedItem.prop("checked")));
                });

                $("#openPrice").change(function() {
                    $("#weightedItem").prop("checked", false);
                    $("#weightedItem").attr("disabled", this.checked);
                    $("#pricePerKg").prop("checked", false);
                    $("#pricePer100g").prop("checked", false);
                    $("#deliItem").prop("checked", false);
                    $("#deliItem").attr("disabled", this.checked);
                });

                $("#zeroPrice").change(function() {
                    var checked = this.checked;

                    $("#weightedItem").prop("checked", false);
                    $("#weightedItem").attr("disabled", checked);
                    $("#pricePerKg").prop("checked", false);
                    $("#pricePer100g").prop("checked", false);
                    $("#deliItem").prop("checked", false);
                    $("#deliItem").attr("disabled", checked);


                    $("#pricesContainer .mask-money").each(function() {
                        $(this).prop("disabled", checked);
                    });
                });

                $(".mask-money").maskMoney({ allowZero: true });

                $('#collapsePromotions').on('show.bs.collapse', function () {
                    getPromotions(${product?.id});
                });

                $('#collapseProductHistory').on('show.bs.collapse', function () {
                    getProductHistory(${product?.id});
                });

                $('#effectiveDatesPicker').on('change', function () {
                    var effectiveDate = $(this).val()
                    var getProductUrl = '${createLink(controller: 'product', action: 'show')}/' + ${product?.id} + '?effectiveDate=' + encodeURI(effectiveDate);
                    if (getProductUrl) { // require a URL
                        window.location = getProductUrl; // redirect
                    }
                    return false;
                });

                $('.add-product-desc').on("change", function() {
                    $('.add-product-desc').val(this.value);
                    if ( $('.add-product-receiptDesc').val() === "") {
                        $('.add-product-receiptDesc').val(this.value);
                        $('.add-product-receiptDesc').removeClass("is-invalid");
                    }
                    $('.add-product-desc').removeClass("is-invalid");
                });

                $('.add-product-receiptDesc').on("change", function() {
                    $('.add-product-receiptDesc').val(this.value);
                    if ( $('.add-product-desc').val() === "") {
                        $('.add-product-desc').val(this.value);
                        $('.add-product-desc').removeClass("is-invalid");
                    }
                    $('.add-product-receiptDesc').removeClass("is-invalid");
                });
            });

            // Automatically populate the first SKU with the main product item code since it's mostly a 1-1 relationship.
            function itemCodeChanged(itemCode) {

                if (itemCode.match(/[^0-9]/)) {
                    return
                }

                var sku = $("#variants\\[0\\]\\.sku");

                // Only change the SKU the first time we change the main item code.
                if (sku != null && (sku.val() === null || sku.val() === "" || sku.val() === "0")) {
                    sku.val(itemCode);
                    $("#variants\\[0\\]\\.skuText").html(itemCode);

                    // If the "Price Changes" section is enabled (logged in as HO), then update the SKU there too.
                    var priceChangeSku = $("#priceChanges\\[0\\]\\.skuText");

                    if (priceChangeSku !== null) {
                        priceChangeSku.html(itemCode);
                    }

                    var bandSkus = $('[id ^="priceChanges\\[0\\]"][id $="\\]\\.sku"]')

                    if (bandSkus !== null && bandSkus.length > 0) {
                        bandSkus.each(function() {
                            $(this).val(itemCode);
                        })
                    }
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

                    params["operationMode"] = ${uk.co.wonderlane.wlpos.OperationMode.EDIT.value};
                    params["id"] = $(selector + "id").val();
                    params["sku"] = $(selector + "sku").val();
                    params["retailPrice"] = $(selector + "retailPrice").val();
                    params["costPrice"] = $(selector + "costPrice").val();
                    params["shelfLifeDays"] = $(selector + "shelfLifeDays").val();
                    params["shelfCapacity"] = $(selector + "shelfCapacity").val();
                    params["minimumDisplayQuantity"] = $(selector + "minimumDisplayQuantity").val();
                    params["zeroPrice"] = $("#zeroPrice").prop("checked");

                    var barcodeContainers = $($(selector + "barcodesContainer > div"));
                    barcodeContainers.each(function(loopIndex) {
                        var barcodeIndex = parseInt($(this).attr("id").substring(16));

                        params["barcodez[" +loopIndex +"].id"] = $(selector + "barcodez\\[" +barcodeIndex +"\\]\\.id").val();
                        params["barcodez[" +loopIndex +"].barcode"] = $(selector + "barcodez\\[" +barcodeIndex +"\\]\\.barcode").val();
                        params["barcodez[" +loopIndex +"].effectiveDate"] = $(selector + "barcodez\\[" +barcodeIndex +"\\]\\.effectiveDate").val();
                        params["barcodez[" +loopIndex +"].recordStatus"] = $(selector + "barcodez\\[" +barcodeIndex +"\\]\\.recordStatus").val();
                    });
                } else {
                    params["operationMode"] = ${uk.co.wonderlane.wlpos.OperationMode.ADD.value};

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

                        $("#addVariantContent .mask-money").maskMoney({ allowZero: true });
                    }
                });
            }

            // Handle the "Ok" of the add/edit variant modal which puts the values into the form ready for submission as part of the whole page.
            function saveVariant(index) {
                var id = $("#addVariantId").val();
                var sku = $("#addVariantSku").val();
                var retailPrice = $("#addVariantRetailPrice").val();
                var costPrice = $("#addVariantCostPrice").val();
                var shelfLifeDays = $("#addVariantShelfLifeDays").val();
                var shelfCapacity = $("#addVariantShelfCapacity").val();
                var minimumDisplayQuantity = $("#addVariantMinimumDisplayQuantity").val();
                var defaultSupplierId = $("#variants\\[" + index + "\\]\\.defaultSupplierId").val();

                var params = { index: index, id: id, sku: sku, retailPrice: retailPrice, costPrice: costPrice, shelfLifeDays: shelfLifeDays, shelfCapacity: shelfCapacity, minimumDisplayQuantity: minimumDisplayQuantity, defaultSupplierId: defaultSupplierId };

                var addBarcodeContainers = $("#addBarcodesContainer > div");

                addBarcodeContainers.each(function(loopIndex) {
                    var barcodeIndex = $(this).attr("id").substring(10);

                    params["barcodez[" +loopIndex +"].id"] = $("#addVariantBarcodes\\[" +barcodeIndex +"\\]\\.id").val();
                    params["barcodez[" +loopIndex +"].barcode"] = $("#addVariantBarcodes\\[" +barcodeIndex +"\\]\\.barcode").val();
                    params["barcodez[" +loopIndex +"].effectiveDate"] = $("#addVariantBarcodes\\[" +barcodeIndex +"\\]\\.effectiveDate").val();
                    params["barcodez[" +loopIndex +"].recordStatus"] = $("#addVariantBarcodes\\[" +barcodeIndex +"\\]\\.recordStatus").val();
                });

                // Need to add all of the pack data for this variant to the save call otherwise we lose the packs from the screen when re-rendering the variant row.
                var packContainers = $("#variants\\[" +index +"\\]\\.packsContainer > div");

                packContainers.each(function(loopIndex) {
                    var packIndex = $(this).attr("id").substring(13);
                    var packSelector = "#variants\\[" +index +"\\]\\.packs\\[" +packIndex +"\\]";

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

                var locationContainers = $("#variants\\[" +index +"\\]\\.locationsContainer > div");

                locationContainers.each(function(loopIndex) {
                    var locationIndex = $(this).attr("id").substring(17);
                    var locationSelector = "#variants\\[" +index +"\\]\\.locationz\\[" +locationIndex +"\\]";

                    params["locationz[" +loopIndex +"].index"] = loopIndex;
                    params["locationz[" +loopIndex +"].id"] = $(locationSelector +"\\.id").val();
                    params["locationz[" +loopIndex +"].storeId"] = $(locationSelector +"\\.storeId").val();
                    params["locationz[" +loopIndex +"].sku"] = $(locationSelector +"\\.sku").val();
                    params["locationz[" +loopIndex +"].location"] = $(locationSelector +"\\.location").val();
                    params["locationz[" +loopIndex +"].aisle"] = $(locationSelector +"\\.aisle").val();
                    params["locationz[" +loopIndex +"].bay"] = $(locationSelector +"\\.bay").val();
                    params["locationz[" +loopIndex +"].shelf"] = $(locationSelector +"\\.shelf").val();
                    params["locationz[" +loopIndex +"].position"] = $(locationSelector +"\\.position").val();
                    params["locationz[" +loopIndex +"].shelfCapacity"] = $(locationSelector +"\\.shelfCapacity").val();
                    params["locationz[" +loopIndex +"].minimumDisplayQuantity"] = $(locationSelector +"\\.minimumDisplayQuantity").val();
                });

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

                        skuChanged(index, sku);
                    }
                });

                $('#addVariantModal').modal("hide");
            }

            // If we change the SKU we may need to update the SKU in the price changes section too.
            function skuChanged(index, skuValue) {
                var skuText = $("#priceChanges\\[" +index +"\\]\\.skuText");

                if (skuText !== null) {
                    skuText.html(skuValue);
                }

                var bandSkus = $('[id ^="priceChanges\\[' +index +'\\]"][id $="\\]\\.sku"]');

                if (bandSkus !== null && bandSkus.length > 0) {
                    bandSkus.each(function() {
                        $(this).val(skuValue);
                    });
                } else {
                    var params = { index: index, sku: skuValue, zeroPrice: $("#zeroPrice").prop("checked") };

                    $.ajax({
                        url: addPriceUrl,
                        method: "POST",
                        data: params,
                        success: function(resp) {
                            var pricesContainer = $("#pricesContainer");

                            if (pricesContainer !== null) {
                                pricesContainer.append(resp);

                                $("#pricesContainer .mask-money").maskMoney({ allowZero: true });
                            }
                        }
                    });
                }
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
                            $("#addBarcodesContainer").append("<div id=\"addBarcode" +index +"\" class=\"input-group py-1\"></div>");

                            barcodeContainer = $("#addBarcodesContainer > #addBarcode" +index);
                        }

                        barcodeContainer.html(resp);
                    }
                });
            }

            // Delete barcode button was clicked, we just remove the div.
            function deleteBarcode(index) {
                if (!confirm("This barcode will be deleted.")) {
                    return;
                }

                $("#addBarcodesContainer > #addBarcode" +index).remove();
            }

            // Delete location button was clicked, we just remove the div.
            function deleteLocation(variantIndex, locationIndex) {
                if (!confirm("This location will be deleted.\nAre you sure you want to delete this location?")) {
                    return;
                }

                $("#addLocationContainer-"+ variantIndex + "-" + locationIndex).remove();
            }

            // The suppliers button was clicked, we display the suppliers modal for this variant.
            function showSuppliersModal(variantIndex) {
                $("#suppliersContent").html("<div class=\"modal-body\"><div class=\"d-flex justify-content-center\"><div id=\"loadingIndicator\" class=\"spinner-border\" role=\"status\"><span class=\"sr-only\">Loading...</span></div></div></div>");
                $('#suppliersModal').modal({ show: true });

                var params = {};
                params["index"] = variantIndex;

                var defaultSupplier = $("#variants\\[" + variantIndex + "\\]\\.defaultSupplierId").val();
                var variantId = $("#variants\\[" + variantIndex + "\\]\\.id").val();
                params["defaultSupplier"] = defaultSupplier;
                params["productVariantId"] = variantId;

                var packContainers = $("#variants\\[" +variantIndex +"\\]\\.packsContainer > div");

                packContainers.each(function(loopIndex) {
                    var packIndex = $(this).attr("id").substring(13);
                    var packSelector = "#variants\\[" +variantIndex +"\\]\\.packs\\[" +packIndex +"\\]";

                    params["packs[" +loopIndex +"].index"] = loopIndex;
                    params["packs[" +loopIndex +"].id"] = $(packSelector +"\\.id").val();
                    params["packs[" +loopIndex +"].supplier.id"] = $(packSelector +"\\.supplier\\.id").val();
                    params["packs[" +loopIndex +"].supplier.name"] = $(packSelector +"\\.supplier\\.name").val();
                    params["packs[" +loopIndex +"].supplier.symbolGroupId"] = $(packSelector +"\\.supplier\\.symbolGroupId").val();
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

            // The locations button was clicked, we display the locations modal for this variant.
            function showLocationsModal(variantIndex, sku) {
                $("#locationsContent").html("<div class=\"modal-body\"><div class=\"d-flex justify-content-center\"><div id=\"loadingIndicator\" class=\"spinner-border\" role=\"status\"><span class=\"sr-only\">Loading...</span></div></div></div>");
                $('#locationsModal').modal({ show: true });

                var params = {};
                params["index"] = variantIndex;
                params["sku"] = sku;

                var variantId = $("#variants\\[" + variantIndex + "\\]\\.id").val();
                params["productVariantId"] = variantId;

                var locationContainers = $("#variants\\[" +variantIndex +"\\]\\.locationsContainer > div");

                locationContainers.each(function(loopIndex) {
                    var locationIndex = $(this).attr("id").substring(17);
                    var locationSelector = "#variants\\[" +variantIndex +"\\]\\.locationz\\[" +locationIndex +"\\]";

                    params["locationz[" +loopIndex +"].index"] = loopIndex;
                    params["locationz[" +loopIndex +"].id"] = $(locationSelector +"\\.id").val();
                    params["locationz[" +loopIndex +"].storeId"] = $(locationSelector +"\\.storeId").val();
                    params["locationz[" +loopIndex +"].sku"] = $(locationSelector +"\\.sku").val();
                    params["locationz[" +loopIndex +"].location"] = $(locationSelector +"\\.location").val();
                    params["locationz[" +loopIndex +"].aisle"] = $(locationSelector +"\\.aisle").val();
                    params["locationz[" +loopIndex +"].bay"] = $(locationSelector +"\\.bay").val();
                    params["locationz[" +loopIndex +"].shelf"] = $(locationSelector +"\\.shelf").val();
                    params["locationz[" +loopIndex +"].position"] = $(locationSelector +"\\.position").val();
                    params["locationz[" +loopIndex +"].shelfCapacity"] = $(locationSelector +"\\.shelfCapacity").val();
                    params["locationz[" +loopIndex +"].minimumDisplayQuantity"] = $(locationSelector +"\\.minimumDisplayQuantity").val();
                });

                $.ajax({
                    url: locationsUrl,
                    method: "POST",
                    data: params,
                    success: function(resp) {
                        $("#locationsContent").html(resp);
                    }
                });
            }

            // If an existing row was clicked, then hidden form is displayed, otherwise a whole new blank "add location" row is added.
            function addLocation(variantIndex, locationIndex, productVariantId) {
                if (locationIndex != null) {
                    var locationContainer = $("#addLocationTextContainer-" + variantIndex + "-" + locationIndex);
                    var addLocationContainer = $("#addLocationFieldsContainer-" + variantIndex + "-" + locationIndex);

                    locationContainer.addClass("hidden");
                    addLocationContainer.removeClass("hidden");
                } else {
                    var lastLocationContainer = $("#addLocationsContainer-" +variantIndex +" > div:last-child");
                    locationIndex = 0;

                    if (lastLocationContainer.length > 0) {
                        locationIndex = parseInt(lastLocationContainer[0].id.substring(lastLocationContainer[0].id.lastIndexOf("-") + 1)) + 1;
                    }

                    $.ajax({
                        url: addLocationUrl,
                        method: "POST",
                        data: { variantIndex: variantIndex, locationIndex: locationIndex, productVariantId: productVariantId },
                        success: function(resp) {
                            var addLocationsContainer = $("#addLocationsContainer-" +variantIndex);
                            addLocationsContainer.append("<div id=\"addLocationContainer-" +variantIndex +"-" +locationIndex +"\"></div>");

                            var addLocationContainer = $("#addLocationContainer-" +variantIndex +"-" +locationIndex);

                            addLocationContainer.append(resp);
                        }
                    });
                }
            }

            // If an existing row was clicked, then hidden form is displayed, otherwise a whole new blank "add pack" row is added.
            function addPack(variantIndex, packIndex, productVariantId) {
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
                        data: { variantIndex: variantIndex, packIndex: packIndex, productVariantId: productVariantId },
                        success: function(resp) {
                            var addPacksContainer = $("#addPacksContainer-" +variantIndex);
                            addPacksContainer.append("<div id=\"addPackContainer-" +variantIndex +"-" +packIndex +"\"></div>");

                            var addPackContainer = $("#addPackContainer-" +variantIndex +"-" +packIndex);

                            addPackContainer.append(resp);

                            $("#addPackContainer-" +variantIndex +"-" +packIndex +" .mask-money").maskMoney({ allowZero: true });
                        }
                    });
                }
            }

            // The "Ok" button was clicked on the suppliers modal, this adds all of those values back onto the form ready for saving as part of the overall page save.
            function savePacks(variantIndex) {
                var filterValues = {};
                $("#defaultSupplierForm select").each(function () {
                    filterValues[$(this).attr("name")] = $(this).find(":selected").val();
                }).get();

                var params = { index: variantIndex };
                var variantId = $("#variants\\[" + variantIndex + "\\]\\.id").val();

                params["defaultSupplier"] = filterValues["defaultSupplier"];
                params["productVariantId"] = variantId;

                var addPackContainers = $("#addPacksContainer-" +variantIndex +" > div");
                addPackContainers.each(function(loopIndex) {
                    var packIndex = $(this).attr("id").substring($(this).attr("id").lastIndexOf("-") + 1);
                    var packSelector = "#addPack\\[" +packIndex +"\\]";

                    params["packs[" +loopIndex +"].index"] = loopIndex;
                    params["packs[" +loopIndex +"].id"] = $(packSelector +"\\.id").val();
                    params["packs[" +loopIndex +"].supplier.id"] = $(packSelector +"\\.supplier\\.id").val();
                    params["packs[" +loopIndex +"].supplier.name"] = $(packSelector +"\\.supplier\\.name").val();
                    params["packs[" +loopIndex +"].supplier.symbolGroupId"] = $(packSelector +"\\.supplier\\.symbolGroupId").val();
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
                    params["packs[" +loopIndex +"].productVariantId"] = $(packSelector +"\\.productVariantId").val();
                });
                    $.ajax({
                        url: savePackUrl,
                        method: "POST",
                        data: params,
                        success: function(resp) {
                            var packsContainer = $("#variants\\[" +variantIndex +"\\]\\.packsContainer");
                            packsContainer.html(resp);
                            $('#suppliersModal').modal("hide");
                        },
                        error : function(xhr, exception) {
                            $("#suppliersContent").html(xhr.responseText);
                        }
                    });
            }

            // The "Ok" button was clicked on the locations modal, this adds all of those values back onto the form ready for saving as part of the overall page save.
            function saveLocations(variantIndex, locationsType) {

                var params = { index: variantIndex };
                var variantId = $("#variants\\[" + variantIndex + "\\]\\.id").val();
                params["productVariantId"] = variantId;

                // For 'SIMPLE' location type, mandatory fields are location description, shelf capacity and minimum display quantity
                // For 'ADVANCED' location type, mandatory fields are aisle, bay, shelf, position, shelf capacity and minimum display quantity
                var mandatoryLocationFields = true;

                var addLocationContainers = $("#addLocationsContainer-" +variantIndex +" > div");
                addLocationContainers.each(function(loopIndex) {
                    if (!mandatoryLocationFields) {
                        return
                    }
                    var locationIndex = $(this).attr("id").substring($(this).attr("id").lastIndexOf("-") + 1);
                    var locationSelector = "#addLocation\\[" +locationIndex +"\\]";

                    if (locationsType === "SIMPLE") {
                        if ($(locationSelector + "\\.location").val() === '') {
                            confirm("Location can not be empty.")
                            mandatoryLocationFields = false
                            return
                        }
                    } else if (locationsType === "ADVANCED") {
                        if ($(locationSelector + "\\.aisle").val() === '') {
                            confirm("Aisle can not be empty.")
                            mandatoryLocationFields = false
                            return
                        }

                        if ($(locationSelector + "\\.bay").val() === '') {
                            confirm("Bay can not be empty.")
                            mandatoryLocationFields = false
                            return
                        }

                        if ($(locationSelector + "\\.shelf").val() === '') {
                            confirm("Shelf can not be empty.")
                            mandatoryLocationFields = false
                            return
                        }

                        if ($(locationSelector + "\\.position").val() === '') {
                            confirm("Position can not be empty.")
                            mandatoryLocationFields = false
                            return
                        }
                    }

                    if ($(locationSelector + "\\.shelfCapacity").val() === '') {
                        confirm("Shelf Capacity can not be empty.")
                        mandatoryLocationFields = false
                        return
                    }

                    if ($(locationSelector + "\\.minimumDisplayQuantity").val() === '') {
                        confirm("Minimum Display Quantity can not be empty.")
                        mandatoryLocationFields = false
                        return
                    }

                    params["locationz[" +loopIndex +"].index"] = loopIndex;
                    params["locationz[" +loopIndex +"].id"] = $(locationSelector +"\\.id").val() !== "" ? $(locationSelector +"\\.id").val() : (loopIndex + 1).toString();
                    params["locationz[" +loopIndex +"].storeId"] = $(locationSelector +"\\.storeId").val();
                    params["locationz[" +loopIndex +"].sku"] = $(locationSelector +"\\.sku").val();
                    params["locationz[" +loopIndex +"].location"] = $(locationSelector +"\\.location").val();
                    params["locationz[" +loopIndex +"].aisle"] = $(locationSelector +"\\.aisle").val();
                    params["locationz[" +loopIndex +"].bay"] = $(locationSelector +"\\.bay").val();
                    params["locationz[" +loopIndex +"].shelf"] = $(locationSelector +"\\.shelf").val();
                    params["locationz[" +loopIndex +"].position"] = $(locationSelector +"\\.position").val();
                    params["locationz[" +loopIndex +"].shelfCapacity"] = $(locationSelector +"\\.shelfCapacity").val();
                    params["locationz[" +loopIndex +"].minimumDisplayQuantity"] = $(locationSelector +"\\.minimumDisplayQuantity").val();
                });

                if (!mandatoryLocationFields) {
                    return;
                }

                $.ajax({
                    url: saveLocationUrl,
                    method: "POST",
                    data: params,
                    success: function(resp) {
                        var locationsContainer = $("#variants\\[" +variantIndex +"\\]\\.locationsContainer");
                        locationsContainer.html(resp);
                        $('#locationsModal').modal('hide');
                    },
                    error : function(xhr, exception) {
                        $("#locationsContent").html(xhr.responseText);
                    }
                });
            }

            // The "Cancel" button was clicked on the locations modal, add a confirmation cancel pop-up.
            function cancelLocations() {
                if (!confirm("All unsaved changes will be lost, are you sure you want to cancel?")) {
                    return;
                }

                $('#locationsModal').modal("hide");
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

            //trigger this when category is selected
            function onCategoryChanged(selectedCategoryId) {
                //call category map restrictions only when adding new product and restriction tab is not change by manually
                var getRestrictionsUrl = "${createLink(controller: 'product', action: 'ajaxGetRestrictions')}";
                var productOpenPrice = $("#openPrice").prop("checked");
                $.ajax({
                    url: getRestrictionsUrl,
                    method: "GET",
                    data: {
                        selectedCategoryId: selectedCategoryId,
                        productOpenPrice: productOpenPrice
                    },
                    success: function (resp) {
                        $("#restrictionsContainer").html(resp);
                    }
                });
            }

            function getProductHistory(productId) {
                $('#productHistoryContainer').html("<div class=\"d-flex justify-content-center\">\n" +
                    "  <div class=\"spinner-border\" role=\"status\">\n" +
                    "    <span class=\"sr-only\">Loading...</span>\n" +
                    "  </div>\n" +
                    "</div>");

                var getProductHistoryUrl = "${createLink(controller: 'product', action: 'ajaxGetProductHistory')}";

                $.ajax({
                    url: getProductHistoryUrl,
                    method: "GET",
                    data: { productId: productId },
                    success: function(resp) {
                        $("#productHistoryContainer").html(resp);
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
                            <li id="breadcrumb-1" class="breadcrumb-item"><g:link uri="/">Home</g:link></li>
                            <li id="breadcrumb-2" class="breadcrumb-item" aria-current="page"><g:link controller="product" action="index">Product Search</g:link></li>
                            <li id="breadcrumb-3" class="breadcrumb-item active" aria-current="page">${product?.itemCode ?: "Add Product"}</li>
                        </ol>
                    </div>
                </div>
            </nav>
        </section>

        <section id="header-container" class="container-fluid">
            <div class="row header-wl mt-3">
                <div class="col-8 offset-2">
                    <h2 class="mx-auto my-auto">Product Maintenance</h2>
                </div>

                <div class="col-2 text-right">
                    <g:link elementId="product-maintenance-cancel" action="index" role="button" class="btn btn-wl">Cancel</g:link>
                    <button id="add-product-save-btn" class="btn btn-success" name="save" onclick="$('#add-product-form').submit();">Save</button>
                </div>
            </div>
        </section>

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
            <g:render template="addProductForm" model="[product            : product,
                                                        storeId            : storeId,
                                                        statusValues       : statusValues,
                                                        categoryValues     : categoryValues,
                                                        productCategoryList: productCategoryList,
                                                        vatValues          : vatValues,
                                                        effectiveDateIndex : effectiveDateIndex,
                                                        ranges             : ranges,
                                                        selectedRanges     : selectedRanges,
                                                        priceBands         : priceBands,
                                                        editedPrices       : editedPrices,
                                                        isNewProduct       : isNewProduct,
                                                        snappyEnabled      : snappyEnabled,
                                                        locationsType      : locationsType]"/>
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

        <section id="locations-modal" class="container-fluid">
            <!-- Locations modal. -->
            <div class="modal fade" id="locationsModal" tabindex="-1" role="dialog" aria-labelledby="locationsModalLabel" aria-hidden="true">
                <div class="modal-dialog modal-xl" role="document">
                    <div id="locationsContent" class="modal-content">

                    </div>
                </div>
            </div>
        </section>
    </body>
</html>