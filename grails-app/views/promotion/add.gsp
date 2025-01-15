<%@ page contentType="text/html;charset=UTF-8" %>
<html>
    <head>
        <meta name="layout" content="main" />

        <title>Promotion Maintenance</title>

        <asset:stylesheet src="bootstrap-datepicker3.min.css" />
        <asset:javascript src="bootstrap-datepicker.min.js" />
        <asset:javascript src="money-mask.js" />

        <script type='text/javascript'>
            var selectedPromotionGroupType = null;
            let tempSelectedStoreIds = [];

            $(document).ready(function () {
                $("#startDateInput").datepicker({
                    format: "DD dd MM yyyy",
                    weekStart: 1,
                    todayHighlight: true,
                    autoclose: true,
                    todayBtn: "linked",
                    orientation: "bottom auto"
                }).on('changeDate', function(event) {
                    let options = [{year: 'numeric'}, {month: '2-digit'}, {day: '2-digit'}];
                    let formatted = formatDate(event.date, options, '-');

                    $("#startDate").val(formatted);
                });

                $("#endDateInput").datepicker({
                    format: "DD dd MM yyyy",
                    weekStart: 1,
                    todayHighlight: true,
                    autoclose: true,
                    todayBtn: "linked",
                    orientation: "bottom auto"
                }).on('changeDate', function(event) {
                    let options = [{year: 'numeric'}, {month: '2-digit'}, {day: '2-digit'}];
                    let formatted = formatDate(event.date, options, '-');

                    $("#endDate").val(formatted);
                });

                $(".mask-money").maskMoney({ allowZero: true });

                $("#type").on("change", function() {
                    showOrHideAmountField($(this).val());
                    showOrHideBuyGroups($(this).val());
                });

                $("#description").on("change", function() {
                    var receiptDescription = $('#receiptDescription');
                    if (receiptDescription.val() === "") {
                        receiptDescription.val(this.value.substring(0, 50));
                    }
                });

                enableOrDisablePromotionGroupButtons("required");
                enableOrDisablePromotionGroupButtons("offer");

                viewOptionForLoyaltyEnable()

            });

            function viewOptionForLoyaltyEnable(){
                $("#loyalty").attr("disabled", !${loyaltyEnabled});
            }

            function resetTempSelectedStoreIds() {
                tempSelectedStoreIds = []
            }

            function formatDate(date, options, separator) {
                function format(option) {
                    let formatter = new Intl.DateTimeFormat('en', option);
                    return formatter.format(date);
                }

                return options.map(format).join(separator);
            }

            function showOrHideAmountField(promoType) {
                var amountDiv = $("#amount-div");
                var amountField = $("#amount");
                var amountLabel = $("#amount-label");

                amountField.val("");

                if (promoType === "BOGOF") {
                    amountDiv.hide();
                } else if (promoType === "FIXED_AMOUNT_DISCOUNT") {
                    amountLabel.text("Discount Amount");
                    amountDiv.show();
                } else if (promoType === "PERCENTAGE_DISCOUNT") {
                    amountLabel.text("Discount Percentage");
                    amountDiv.show();
                } else if (promoType === "X_FOR_Y") {
                    amountDiv.hide();
                } else if (promoType === "FIXED_PRICE") {
                    amountLabel.text("Fixed Price");
                    amountDiv.show();
                }
            }

            // Shows or hides the offer and required groups sections depending on the promotion type.
            function showOrHideBuyGroups(promoType) {
                var requiredGroupsDiv = $("#requiredGroupsDiv");
                var requiredGroupsHeading = $("#requiredGroupsHeading");
                var offerGroupsHeading = $("#offerGroupsHeading");

                // Remove any already selected buy groups (i.e. changing promo type largely resets the form).
                $("#promotionOfferGroupsContainer").html("");
                $("#promotionRequiredGroupsContainer").html("");

                // Ensure all add Tag or Category buttons are re-enabled.
                enableOrDisablePromotionGroupButtons("offer");
                enableOrDisablePromotionGroupButtons("required");

                // Do the hiding or showing of the accordion sections.
                if (promoType === "BOGOF") {
                    requiredGroupsDiv.hide();

                    offerGroupsHeading.text("Customer Buys & Receives One Free");
                } else if (promoType === "FIXED_AMOUNT_DISCOUNT") {
                    requiredGroupsDiv.hide();

                    offerGroupsHeading.text("Customer Buys & Receives Amount Off");
                } else if (promoType === "PERCENTAGE_DISCOUNT") {
                    requiredGroupsDiv.hide();

                    offerGroupsHeading.text("Customer Buys & Receives % Off");
                } else if (promoType === "X_FOR_Y") {
                    requiredGroupsHeading.text("Customer Buys");
                    requiredGroupsDiv.show();

                    offerGroupsHeading.text("Customer Receives Free");
                } else if (promoType === "FIXED_PRICE") {
                    requiredGroupsDiv.hide();

                    offerGroupsHeading.text("Customer Buys For Amount");
                }
            }

            // Add the selected product to the relevant promotion group section.
            function addPromotionGroupProduct(sku) {
                var url = "${createLink(controller: 'promotion', action: 'ajaxGetSku')}";

                addPromotionGroup(sku, url);
            }

            // Add the selected productGroup to the relevant promotion group section.
            function addPromotionGroupTag(id) {
                var url = "${createLink(controller: 'promotion', action: 'ajaxGetTag')}";

                addPromotionGroup(id, url);
            }

            // Add the selected category to the relevant promotion group section.
            function addPromotionGroupCategory(id) {
                var url = "${createLink(controller: 'promotion', action: 'ajaxGetCategory')}";

                addPromotionGroup(id, url);
            }

            function addPromotionGroup(id, url) {
                var promotionType = $("#type").val();

                // Figure out what group ID (position) this will be based on how many groups already exist.
                var groupId = 0;

                if (selectedPromotionGroupType === "required") {
                    groupId = $("#promotionRequiredGroupsContainer").children("div").length;
                } else if (selectedPromotionGroupType === "offer") {
                    groupId = $("#promotionOfferGroupsContainer").children("div").length;
                }

                $.ajax({
                    url: url,
                    data: { id: id, promotionType: promotionType, promotionGroupType: selectedPromotionGroupType, groupId: groupId },
                    success: function(resp) {
                        if (selectedPromotionGroupType === "required") {
                            $("#promotionRequiredGroupsContainer").append(resp);
                        } else if (selectedPromotionGroupType === "offer") {
                            $("#promotionOfferGroupsContainer").append(resp);
                        }

                        enableOrDisablePromotionGroupButtons(selectedPromotionGroupType);
                    }
                });
            }

            // Enable or disable the Add Tag/Category buttons depending on the promo type and how many groups are already selected.
            function enableOrDisablePromotionGroupButtons(promotionGroupType) {
                var promoType = $("#type").val();

                var promoGroupsCount = 0;

                if (promotionGroupType === "offer") {
                    promoGroupsCount = $("#promotionOfferGroupsContainer").children("div").length;
                } else if (promotionGroupType === "required") {
                    promoGroupsCount = $("#promotionRequiredGroupsContainer").children("div").length;
                }

                if (promoGroupsCount < 1) {
                    setPromotionGroupButtonEnabledness(promotionGroupType, true, true, true);
                } else {
                    if (promoType === "BOGOF") {
                        enableOrDisablePromotionGroupButtonsForBogof(promotionGroupType);
                    } else if (promoType === "FIXED_AMOUNT_DISCOUNT") {
                        enableOrDisablePromotionGroupButtonsForFixedAmount(promotionGroupType);
                    } else if (promoType === "PERCENTAGE_DISCOUNT") {
                        enableOrDisablePromotionGroupButtonsForPercentage(promotionGroupType);
                    } else if (promoType === "X_FOR_Y") {
                        enableOrDisablePromotionGroupButtonsForXForY(promotionGroupType);
                    } else if (promoType === "FIXED_PRICE") {
                        enableOrDisablePromotionGroupButtonsForFixedPrice(promotionGroupType);
                    }
                }
            }

            function enableOrDisablePromotionGroupButtonsForBogof(promotionGroupType) {
                // Single product, category or productGroup allowed at a time.
                setPromotionGroupButtonEnabledness(promotionGroupType, false, false, false);
            }

            function enableOrDisablePromotionGroupButtonsForFixedAmount(promotionGroupType) {
                // Single product, category or productGroup allowed at a time.
                setPromotionGroupButtonEnabledness(promotionGroupType, false, false, false);
            }

            function enableOrDisablePromotionGroupButtonsForPercentage(promotionGroupType) {
                // Single product, category or productGroup allowed at a time.
                setPromotionGroupButtonEnabledness(promotionGroupType, false, false, false);
            }

            function enableOrDisablePromotionGroupButtonsForXForY(promotionGroupType) {
                // Single product, category or productGroup allowed at a time.
                setPromotionGroupButtonEnabledness(promotionGroupType, false, false, false);
            }

            function enableOrDisablePromotionGroupButtonsForFixedPrice(promotionGroupType) {
                // Multiple of either a product, productGroup or category allowed.

                // Find out which group type we've used so far and only enable that button.
                if ($("#" +promotionGroupType +"Groups\\[0\\]\\.sku").val() > 0) {
                    setPromotionGroupButtonEnabledness(promotionGroupType, true, false, false);
                } else if ($("#" +promotionGroupType +"Groups\\[0\\]\\.categoryId").val() > 0) {
                    setPromotionGroupButtonEnabledness(promotionGroupType, false, true, false);
                } else if ($("#" +promotionGroupType +"Groups\\[0\\]\\.tagId").val() > 0) {
                    setPromotionGroupButtonEnabledness(promotionGroupType, false, false, true);
                } else {
                    // Shouldn't be hitting this.
                    setPromotionGroupButtonEnabledness(promotionGroupType, false, false, false);
                }
            }

            function setPromotionGroupButtonEnabledness(promotionGroupType, productButtonEnabled, categoryButtonEnabled, tagButtonEnabled) {
                $("#" +promotionGroupType +"AddProductButton").attr("disabled", !productButtonEnabled);
                $("#" +promotionGroupType +"AddCategoryButton").attr("disabled", !categoryButtonEnabled);
                $("#" +promotionGroupType +"AddTagButton").attr("disabled", !tagButtonEnabled);
            }

            function deletePromotionGroup(promotionGroupName, promotionGroupType) {
                $("#" +promotionGroupName).remove();

                var promotionGroupsContainer;

                if (promotionGroupType === "offer") {
                    promotionGroupsContainer = $("#promotionOfferGroupsContainer");
                } else if (promotionGroupType === "required") {
                    promotionGroupsContainer = $("#promotionRequiredGroupsContainer");
                }

                promotionGroupsContainer.children("div").each(function(index) {
                    var deleteButton = $("#" + $(this).attr("id") + "-DeleteButton");
                    deleteButton.attr("id", promotionGroupType + "PromoGroup-" + index + "-DeleteButton")
                    deleteButton.prop("onclick", null).off("click");
                    deleteButton.on("click", function () {
                        deletePromotionGroup(promotionGroupType + "PromoGroup-" + index, "offer");
                    });

                    var currentIndex = $(this).attr("id").slice(-1);

                    $("#" +promotionGroupType +"Groups\\[" +currentIndex +"\\]\\.id").attr("id", promotionGroupType +"Groups[" +index +"].id").attr("name", promotionGroupType +"Groups[" +index +"].id");
                    $("#" +promotionGroupType +"Groups\\[" +currentIndex +"\\]\\.type").attr("id", promotionGroupType +"Groups[" +index +"].type").attr("name", promotionGroupType +"Groups[" +index +"].type");
                    $("#" +promotionGroupType +"Groups\\[" +currentIndex +"\\]\\.sku").attr("id", promotionGroupType +"Groups[" +index +"].sku").attr("name", promotionGroupType +"Groups[" +index +"].sku");
                    $("#" +promotionGroupType +"Groups\\[" +currentIndex +"\\]\\.categoryId").attr("id", promotionGroupType +"Groups[" +index +"].categoryId").attr("name", promotionGroupType +"Groups[" +index +"].categoryId");
                    $("#" +promotionGroupType +"Groups\\[" +currentIndex +"\\]\\.tagId").attr("id", promotionGroupType +"Groups[" +index +"].tagId").attr("name", promotionGroupType +"Groups[" +index +"].tagId");
                    $("#" +promotionGroupType +"Groups\\[" +currentIndex +"\\]\\.requiredValue").attr("id", promotionGroupType +"Groups[" +index +"].requiredValue").attr("name", promotionGroupType +"Groups[" +index +"].requiredValue");
                    $("#" +promotionGroupType +"Groups\\[" +currentIndex +"\\]\\.requiredQuantity").attr("id", promotionGroupType +"Groups[" +index +"].requiredQuantity").attr("name", promotionGroupType +"Groups[" +index +"].requiredQuantity");

                    $(this).attr("id", promotionGroupType + "PromoGroup-" + index);
                });

                enableOrDisablePromotionGroupButtons(promotionGroupType);
            }

            function disableSaveButton() {
                $("#save-btn").attr("disabled", true);
            }

            function getAllStores() {
                var filterParams = {};

                $("#filters input").each(function () {
                    filterParams[$(this).attr("name")] = $(this).val();
                }).get();

                $.ajax({
                    url: '${createLink(controller: "promotion", action: 'ajaxGetAllStores')}',
                    data: filterParams,
                    success: function (response) {
                        $('#store-selection-list').html(response);
                    },
                    error: function (xhr, status, error) {
                        console.log('Error: ' + error);
                    }
                });
            }

            function toggleSelectStore(storeId, index) {
                console.log(`Toggling store with ID: ` + storeId + ` at index: ` + index);
                const button = $('#modal-store-select-' + index);
                if (!button.length) {
                    console.error(`Button with ID modal-store-select-` + index + ` not found`);
                    return;
                }

                const checkbox = $('#store-' + storeId);
                if (!checkbox.length) {
                    console.error(`Checkbox with ID store-` + storeId + ` not found`);
                    return;
                }

                console.log(checkbox.prop('checked'))
                if (checkbox.prop('checked')) {
                    checkbox.prop('checked', false);
                    button.removeClass('btn-primary').addClass('btn-secondary').text('Select');
                    tempSelectedStoreIds.splice(tempSelectedStoreIds.indexOf(storeId), 1);
                } else {
                    checkbox.prop('checked', true);
                    button.removeClass('btn-secondary').addClass('btn-primary').text('Selected');
                    tempSelectedStoreIds.push(storeId);
                }
            }

            function addSelectedStores() {
                $.ajax({
                    url: '${createLink(controller: "promotion", action: 'ajaxAddStores')}',
                    type: 'POST',
                    data: { storeIds: tempSelectedStoreIds },
                    success: function(response) {
                        $('.store-search-results').html(response)
                        $('#promotionStoreSearchModal').modal('hide');
                        $('#StoresSection').removeClass("is-invalid")
                    },
                    error: function(xhr, status, error) {
                        console.log('Error: ' + error);
                    }
                });

                resetTempSelectedStoreIds()
            }

            function submitPromotion() {
                var submit = true;

                <g:if test="${promotion != null}">
                    /* If before any changes were made the promotion was set as active and loyalty */
                    <g:if test="${promotion.loyalty && promotion.active}">
                        /* Get the potentially updated active flag value */
                        var isActive = $('#active').prop('checked');

                        if (!isActive) {
                            submit = confirm('Disabling a loyalty associated promotion will update any underlying offers. Are you sure you wish to save these changes?')
                        }
                    </g:if>
                </g:if>

                if (submit) {
                    $('#add-promotion-form').submit();
                }
            }

            function addAllStores() {
                $.ajax({
                    url: '${createLink(controller: "promotion", action: 'ajaxAddAllStores')}',
                    type: 'POST',
                    success: function(response) {
                        $('.store-search-results').html(response);
                        $('#StoresSection').removeClass("is-invalid")
                    }
                });

                resetTempSelectedStoreIds()
            }

            function removeAllStores() {
                $.ajax({
                    url: '${createLink(controller: "promotion", action: 'ajaxRemoveAllStores')}',
                    type: 'POST',
                    success: function(response) {
                        $('.store-search-results').html(response);
                    }
                });
            }

            function removeStore(storeId, index) {
                $.ajax({
                    url: "${createLink(controller: 'promotion', action: 'ajaxRemoveStores')}",
                    type: 'POST',
                    data: { storeId: storeId },
                    success: function(response) {
                        // Update the list of added stores
                        tempSelectedStoreIds.splice(tempSelectedStoreIds.indexOf(storeId), 1);
                        console.log(tempSelectedStoreIds.length)
                        $('.store-search-results').html(response);
                    },
                    error: function(xhr, status, error) {
                        console.error("Error removing store: " + error);
                    }
                });

                resetTempSelectedStoreIds()
            }

            // Store filters.
            function clearFilters() {
                $("#storeNumberFilter").val("");
                $("#storeNameFilter").val("");
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
                            <li id="breadcrumb-2" class="breadcrumb-item" aria-current="page"><g:link controller="promotion" action="index">Promotion Search</g:link></li>
                            <li id="breadcrumb-3" class="breadcrumb-item active" aria-current="page">${promotion?.description ?: "Add Promotion"}</li>
                        </ol>
                    </div>
                </div>
            </nav>
        </section>

        <section id="header-container" class="container-fluid">
            <div class="row header-wl mt-3">
                <div class="col-8 offset-2">
                    <h2 id="page-title" class="mx-auto my-auto">Promotion Maintenance</h2>
                </div>

                <div class="col-2 text-right">
                    <g:link elementId="cancel-btn" controller="promotion" action="index" tabindex="-1" role="button" class="btn btn-wl">Cancel</g:link>
                    <button id="save-btn" class="btn btn-success" name="save" onclick="submitPromotion();">Save</button>
                </div>
            </div>
        </section>

        <g:hasErrors bean="${promotion}">
            <section id="errors-container" class="container-fluid">
                <div class="alert alert-danger alert-wl mx-0" role="alert">
                    <g:renderErrors bean="${promotion}" as="list" />
                </div>
            </section>
        </g:hasErrors>

        <g:if test="${flash.message}">
            <section id="success-container" class="container-fluid">
                <div class="alert alert-success alert-wl mx-0" role="alert">${flash.message}</div>
            </section>
        </g:if>

        <section id="add-edit-section" class="container-fluid mt-4">
            <g:render template="addEditForm" model="[promotion: promotion, canEdit: canEdit, associatedOffers: associatedOffers]"/>
        </section>

        <g:render template="productSearch" />
        <g:render template="categorySearch" />
        <g:render template="tagSearch" />
        <g:render template="addStoresModal"/>
    </body>
</html>