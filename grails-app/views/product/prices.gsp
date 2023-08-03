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

            function searchButtonClicked2() {
                $('#offset').val(0);
                search();
            }

            function resetButtonClicked() {
                $('#tag').prop("selectedIndex", 0);
                $('input[name="category.id"]:checked').prop("checked", false);
                $('#searchTerm').val("");
            }

            function search() {
                var URL = "${createLink(controller: 'product', action: 'pricesSearch')}";

                var searchTerm = $('#searchTerm').val();
                var category = $('input[name="category.id"]:checked').val();
                var tag = $('#tag').val();

                $('#search-results').html("<div class=\"d-flex justify-content-center pt-2\">\n" +
                    "  <div class=\"spinner-border\" role=\"status\">\n" +
                    "    <span class=\"sr-only\">Loading...</span>\n" +
                    "  </div>\n" +
                    "</div>");

                $.ajax({
                    url: URL,
                    data: { searchTerm: searchTerm, category: category, tag: tag },
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

                var checkedBoxes = $("input:checked");

                checkedBoxes.each(function(i, checkbox) {
                    var prices = $("[id^=price-" +$(checkbox).attr("id").substring(8) +"-]");

                    prices.each(function(index, price) {
                        var id = $(price).attr("id");
                        var sku = id.substring(6, id.lastIndexOf("-"));
                        var priceBandId = id.substring(id.lastIndexOf("-") + 1);
                        var oldPrice = $("[id^=oldPrice-" + sku + "-" + priceBandId + "]").val();
                        var productId = $("[id^=productId-" + sku + "-" + priceBandId + "]").val();

                        data["priceChanges[" +((i * 3) + index) +"].sku"] = sku;
                        data["priceChanges[" +((i * 3) + index) +"].priceBandId"] = priceBandId;
                        data["priceChanges[" +((i * 3) + index) +"].price"] = $(price).val();
                        data["priceChanges[" +((i * 3) + index) +"].oldPrice"] = oldPrice;
                        data["priceChanges[" +((i * 3) + index) +"].productId"] = productId;
                    });
                });

                var url = "${createLink(controller: 'product', action: 'ajaxSavePriceChanges')}";

                $.ajax({
                    url: url,
                    method: "POST",
                    data: data,
                    success: function(resp) {
                        $('#confirmModal').modal({ show: true });

                        checkedBoxes.each(function(i, checkbox) {
                            $(checkbox).prop("checked", false);
                        });

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
                                        <label for="tag" class="col-4 col-form-label text-right">Tag</label>
                                        <div class="col-8">
                                            <g:select name="tag" from="${tags}" noSelection="['':'All tags']" value="${tag}" optionValue="description" optionKey="id" class="form-control select-border" />
                                        </div>
                                    </div>

                                    <div class="form-group row">
                                        <div class="col-12 text-right">
                                            <button id="filter-reset-button" type="button" class="btn btn-danger text-right" onclick="resetButtonClicked()">Reset</button>
                                            <button id="filter-submit-button" type="button" class="btn btn-wl text-right" onclick="searchButtonClicked2()">Search</button>
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>

                <div class="col-2 offset-4 text-right">
                    <button id="save-changes-button" class="btn btn-wl" onclick="savePriceChanges();">Save Changes</button>
                </div>
            </div>

            <div class="row mt-5 pb-2 ml-0 mr-0 table-wl bottom-border">
                <div class="col-2 font-weight-bold">Item Code</div>
                <div class="col-4 font-weight-bold">Description</div>
                <div class="col-1 font-weight-bold">Cost Price</div>
                <g:each in="${priceBands}" var="priceBand">
                    <div class="col font-weight-bold">${priceBand.description}</div>
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
                        <div class="modal-header">
                            <h2 id="confirm-modal-title">Success</h2>
                        </div>

                        <div id="confirm-modal-message" class="modal-body">Price changes saved successfully.</div>

                        <div class="modal-footer">
                            <button type="button" id="closeConfirmModalButton" class="btn btn-secondary" data-dismiss="modal">Close</button>
                        </div>
                    </div>
                </div>
            </div>
        </section>
    </body>
</html>