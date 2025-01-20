<%@ page contentType="text/html;charset=UTF-8" %>
<html>
    <head>
        <meta name="layout" content="main" />

        <title>Product Maintenance</title>

        <asset:javascript src="money-mask.js" />
        <asset:javascript src="category-select.js" />
        <asset:stylesheet href="radio.css" />

        <script type="text/javascript">
            var getChildCategoriesUrl = "${createLink(controller: 'product', action: 'ajaxGetChildCategories')}";
            var categorySearchUrl = "${createLink(controller: 'product', action: 'ajaxSearchCategories')}";

            $(document).ready(function () {
                $('#searchTerm').on('keyup', function(event) {
                    if (event.key === 'Enter') {
                        searchButtonClicked2();
                    }
                });
            });

            function selectAll() {
                const uncheckedBoxes = $("input.selections:not(checked)");
                uncheckedBoxes.each(function(i, checkbox) {
                    const element = $(checkbox);
                    element.prop("checked", true);
                    toggleRowInputs(getIndexFromCheckBoxId(element.attr('id')));
                });
            }

            function deselectAll() {
                const checkedBoxes = $("input.selections:checked");
                checkedBoxes.each(function(i, checkbox) {
                    const element = $(checkbox);
                    element.prop("checked", false);
                    toggleRowInputs(getIndexFromCheckBoxId(element.attr('id')));
                });
            }

            function getIndexFromCheckBoxId(id) {
                // id should be "product-price-i-check-box" where i is numeric
                const splits = id.split('-');
                if (splits.length < 3) {
                    return 0;
                }
                return splits[2];
            }

            function toggleRowInputs(i) {
                const idPrefix = "product-price-" + i;
                const selectedBox = $("#" + idPrefix + "-check-box");
                const disabled = !selectedBox.is(':checked');

                // disable / enable all price inputs on this row
                $('[id^=' + idPrefix + "-band-" + ']').each((_, input) => {
                    $(input).prop("disabled", disabled);
                });
            }

            function searchButtonClicked2() {
                $('#offset').val(0);
                search();
            }

            function resetButtonClicked() {
                $('#productGroup').prop("selectedIndex", 0);
                $('input[name="category.id"]:checked').prop("checked", false);
                $('#searchTerm').val("");
            }

            function search() {
                var URL = "${createLink(controller: 'product', action: 'pricesSearch')}";

                var searchTerm = $('#searchTerm').val();
                var category = $('input[name="category.id"]:checked').val();
                var productGroup = $('#productGroup').val();

                $('#search-results').html("<div class=\"d-flex justify-content-center pt-2\">\n" +
                    "  <div class=\"spinner-border\" role=\"status\">\n" +
                    "    <span class=\"sr-only\">Loading...</span>\n" +
                    "  </div>\n" +
                    "</div>");

                $('#checkAllCheckbox').prop("checked", false);

                $.ajax({
                    url: URL,
                    data: {searchTerm: searchTerm, category: category, productGroup: productGroup},
                    success: function(resp) {
                        $('#search-results').html(resp);

                        $(".mask-money").maskMoney({ allowZero: true });
                        $(".mask-money").maskMoney('mask');
                    }
                });
            }

            function savePriceChanges() {
                var saveButton = $("save-changes-button");
                saveButton.prop("disabled", true);

                var data = { };

                var checkedBoxes = $("#search-results input:checked");

                checkedBoxes.each(function(i, checkbox) {
                    var prices = $("[name^=price-" + $(checkbox).attr("name").substring(8) + "-]");

                    prices.each(function (index, price) {
                        var name = $(price).attr("name");
                        var sku = name.substring(6, name.lastIndexOf("-"));
                        var priceBandId = name.substring(name.lastIndexOf("-") + 1);
                        var oldPrice = $("[id^=oldPrice-" + sku + "-" + priceBandId + "]").val();
                        var productId = $("[id^=productId-" + sku + "-" + priceBandId + "]").val();

                        data["priceChanges[" + ((i * 3) + index) + "].sku"] = sku;
                        data["priceChanges[" + ((i * 3) + index) + "].priceBandId"] = priceBandId;
                        data["priceChanges[" + ((i * 3) + index) + "].price"] = $(price).val();
                        data["priceChanges[" + ((i * 3) + index) + "].oldPrice"] = oldPrice;
                        data["priceChanges[" + ((i * 3) + index) + "].productId"] = productId;
                    });
                });

                var url = "${createLink(controller: 'product', action: 'ajaxSavePriceChanges')}";

                $.ajax({
                    url: url,
                    method: "POST",
                    data: data,
                    success: function(resp) {

                        $('#confirm-modal-success').attr("hidden", resp !== "OK" );
                        $('#confirm-modal-empty').attr("hidden", resp !== "EMPTY" );

                        $('#confirmModal').modal({ show: true });

                        checkedBoxes.each(function(i, checkbox) {
                            $(checkbox).prop("checked", false);
                        });

                        $('#checkAllCheckbox').prop("checked", false);
                        saveButton.prop("disabled", false);
                    }
                });
            }

            function priceChanged(sku, priceBandId, costPrice, retailPrice) {
                if (costPrice !== undefined && costPrice > 0 && retailPrice !== undefined && retailPrice > 0) {
                    var margin = (((retailPrice - costPrice) / retailPrice).toFixed(4) * 100).toFixed(2);

                    if (margin < 0) {
                        margin = "0.00";
                    }

                    $("#margin-" +sku +"-" +priceBandId).text(margin +"%")
                }
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
                            <li id="breadcrumb-2" class="breadcrumb-item active" aria-current="page">Product Price Changes</li>
                        </ol>
                    </div>
                </div>
            </nav>
        </section>

        <g:if test="${flash.message}">
            <section id="alerts-container" class="container-fluid">
                <div class="alert alert-success alert-wl mx-0" role="alert">${flash.message}</div>
            </section
        </g:if>

        <section id="maintenance-search" class="container-fluid">
            <div class="header-wl mt-3">
                <h2 id="page-title" class="mx-auto">Product Price Changes</h2>
            </div>

            <div class="row mt-4">
                <div class="col-6">
                    <div class="card bg-light border-wl">
                        <div id="filters-collapse" class="card-header pointer" data-toggle="collapse" data-target="#filterCollapse" aria-expanded="true" aria-controls="filterCollapse">
                            <div class="row">
                                <div id="filters-header" class="col-10">Filters</div>
                                <div class="col-2 text-right">
                                    <svg width="1em" height="1em" viewBox="0 0 16 16" class="bi bi-caret-down-fill text-right" fill="currentColor" xmlns="http://www.w3.org/2000/svg">
                                        <path d="M7.247 11.14L2.451 5.658C1.885 5.013 2.345 4 3.204 4h9.592a1 1 0 0 1 .753 1.659l-4.796 5.48a1 1 0 0 1-1.506 0z"/>
                                    </svg>
                                </div>
                            </div>
                        </div>
                        <div class="card-body collapse pb-0" id="filterCollapse">
                            <div class="row">
                                <div class="col-6">
                                    <div class="row">
                                        <label for="category" class="col-4 col-form-label text-right">Category</label>
                                        <div class="col-8">
                                            <g:render template="categorySelect" model="[categories: categories, productCategoryList: null, selectedCategoryId: null, level: 1]" />
                                        </div>
                                    </div>
                                </div>

                                <div class="col-6">
                                    <div class="form-group row">
                                        <label for="searchTerm" class="col-4 col-form-label text-right">Description</label>
                                        <div class="col-8">
                                            <g:textField name="searchTerm" class="form-control bottom-border" value="${searchTerm}" autocomplete="off" />
                                        </div>
                                    </div>

                                    <div class="form-group row">
                                        <label for="productGroup"
                                               class="col-4 col-form-label text-right">Product Group</label>
                                        <div class="col-8">
                                            <g:select name="productGroup" from="${productGroups}"
                                                      noSelection="['': 'All Product Groups']" value="${productGroup}"
                                                      optionValue="description" optionKey="id"
                                                      class="form-control select-border"/>
                                        </div>
                                    </div>

                                    <div class="form-group row">
                                        <div class="col-12 text-right">
                                            <button id="filter-reset-button" type="button" class="btn btn-danger text-right" onclick="resetButtonClicked()">Reset Filters</button>
                                            <button id="filter-submit-button" type="button" class="btn btn-wl text-right" onclick="searchButtonClicked2()">Search</button>
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>

                <div class="col-4 offset-2 text-right">
                    <button id="select-all-button" class="btn btn-wl" onclick="selectAll();">Select All</button>
                    <button id="deselect-all-button" class="btn btn-wl" onclick="deselectAll();">Deselect All</button>
                    <button id="save-changes-button" class="btn btn-wl" onclick="savePriceChanges();">Save Changes</button>
                </div>
            </div>

            <div class="row mt-5 pb-2 ml-0 mr-0 table-wl bottom-border">
                <div class="col-1 font-weight-bold text-center my-auto">Selected</div>
                <div class="col-2 font-weight-bold my-auto">Item Code</div>
                <div class="col-2 font-weight-bold my-auto">Description</div>
                <div class="col-1 font-weight-bold my-auto">Cost Price</div>
                <g:each in="${priceBands}" var="priceBand">
                    <div class="col-2 font-weight-bold my-auto">${priceBand.description}</div>
                </g:each>
            </div>

            <div id="search-results" class="align-content-center">
                <g:render template="pricesSearchResults" />
            </div>
        </section>

        <!-- Confirmation modal -->
        <section id="confirm-modal" class="container-fluid">
            <div class="modal fade" id="confirmModal" tabindex="-1" role="dialog" aria-labelledby="confirmModalLabel" aria-hidden="true">
                <div class="modal-dialog" role="document">
                    <div class="modal-content">
                        <div id="confirm-modal-success">
                            <div class="modal-header">
                                <h2 id="confirm-modal-title-success">Success</h2>
                            </div>
                            <div id="confirm-modal-message-success" class="modal-body">Price changes saved successfully.</div>
                        </div>
                        <div id="confirm-modal-empty">
                            <div class="modal-header">
                                <h2 id="confirm-modal-title-empty">Save failed</h2>
                            </div>
                            <div id="confirm-modal-message-empty" class="modal-body">Nothing was selected to be saved.</div>
                        </div>

                        <div class="modal-footer">
                            <button type="button" id="closeConfirmModalButton" class="btn btn-secondary" data-dismiss="modal">Close</button>
                        </div>
                    </div>
                </div>
            </div>
        </section>
    </body>
</html>