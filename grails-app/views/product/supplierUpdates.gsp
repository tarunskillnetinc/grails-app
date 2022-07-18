<%@ page contentType="text/html;charset=UTF-8" %>
<html>
    <head>
        <meta name="layout" content="main" />

        <title>WonderLane Supplier Updates</title>

        <asset:stylesheet src="bootstrap-datepicker3.min.css" />
        <asset:javascript src="bootstrap-datepicker.min.js" />
        <asset:javascript src="money-mask.js" />

        <script type="text/javascript">
            $(document).ready(function () {
                $('#sinceDate').datepicker({
                    format: "dd/mm/yyyy",
                    weekStart: 1,
                    startDate: "${(new Date() - 30).format("dd/MM/yyyy")}",
                    endDate: "${new Date().format("dd/MM/yyyy")}",
                    todayHighlight: true,
                    autoclose: true,
                    todayBtn: "linked",
                    orientation: "bottom auto"
                });

                $('#effectiveDate').datepicker({
                    format: "dd/mm/yyyy",
                    weekStart: 1,
                    startDate: "${new Date().format("dd/MM/yyyy")}",
                    endDate: "${(new Date() + 14).format("dd/MM/yyyy")}",
                    todayHighlight: true,
                    autoclose: true,
                    todayBtn: "linked",
                    orientation: "bottom auto"
                });

                $('#checkAllCheckbox').change(function() {
                    if (this.checked) {
                        var uncheckedBoxes = $("input:not(:checked)");

                        uncheckedBoxes.each(function(i, checkbox) {
                            $(checkbox).prop("checked", true);
                        });
                    } else {
                        var checkedBoxes = $("input:checked");

                        checkedBoxes.each(function(i, checkbox) {
                            $(checkbox).prop("checked", false);
                        });
                    }
                });

                search();
            });

            function resetButtonClicked() {
                $('#supplier').prop("selectedIndex", 0);
                $('#category').prop("selectedIndex", 0);
                $('#sinceDate').val("${new Date().format("dd/MM/yyyy")}");

                search();
            }

            function search() {
                var URL = "${createLink(controller: 'product', action: 'supplierUpdatesSearch')}";

                var supplierId = $('#supplier').val();
                var categoryId = $('#category').val();
                var sinceDate = $('#sinceDate').val();
                var priceBandId = $('#priceBand').val();

                $('#search-results').html("<div class=\"d-flex justify-content-center pt-2\">\n" +
                    "  <div class=\"spinner-border\" role=\"status\">\n" +
                    "    <span class=\"sr-only\">Loading...</span>\n" +
                    "  </div>\n" +
                    "</div>");

                $('#checkAllCheckbox').prop("checked", false);

                $.ajax({
                    url: URL,
                    data: { supplierId: supplierId, categoryId: categoryId, sinceDate: sinceDate, priceBandId: priceBandId },
                    success: function(resp) {
                        $('#search-results').html(resp);

                        $(".mask-money").maskMoney({ allowZero: true });
                        $(".mask-money").maskMoney('mask');
                    }
                });
            }

            function savePricesButtonClicked(acceptRrps) {
                var checkedBoxes = $("input:checked:not(#checkAllCheckbox)");

                // Change the confirm message depending which button you pressed and whether you have any products selected.
                if (checkedBoxes.length > 0 && acceptRrps) {
                    $("#confirmModalHeader").html("Accept RRPs");
                    $("#confirmModalContent").html("Are you sure you wish to accept the recommended retail price for the selected products?");
                } else if (checkedBoxes.length === 0 && acceptRrps) {
                    if ($(":checkbox:not(#checkAllCheckbox)").length === 0) {
                        alert("No products found.");
                        return;
                    }

                    $("#confirmModalHeader").html("Accept RRPs");
                    $("#confirmModalContent").html("Are you sure you wish to accept the recommended retail price for ALL products?");
                } else if (checkedBoxes.length > 0 && !acceptRrps) {
                    $("#confirmModalHeader").html("Save Prices");
                    $("#confirmModalContent").html("Are you sure you wish to accept the entered retail price for the selected products?");
                } else {
                    if ($(":checkbox:not(#checkAllCheckbox)").length === 0) {
                        alert("No products found.");
                        return;
                    }

                    alert("Please select some products.");
                    return;
                }

                var confirmModalYesButton = $('#confirmModalYesButton');
                var confirmModalNoButton = $('#confirmModalNoButton');

                confirmModalYesButton.click({acceptRrps: acceptRrps}, confirmModalYesButtonClicked);
                confirmModalYesButton.prop("disabled", false);
                confirmModalNoButton.click(confirmModalNoButtonClicked);
                confirmModalNoButton.prop("disabled", false);

                $('#confirmModal').modal({ show: true });
            }

            function confirmModalYesButtonClicked(event) {
                var confirmModalYesButton = $('#confirmModalYesButton');
                var confirmModalNoButton = $('#confirmModalNoButton');

                confirmModalYesButton.off("click");
                confirmModalYesButton.prop("disabled", true);

                confirmModalNoButton.off("click");
                confirmModalNoButton.prop("disabled", true);

                confirmRrps(event.data.acceptRrps);
            }

            function confirmModalNoButtonClicked() {
                $('#confirmModal').modal("hide");
            }

            function confirmRrps(acceptRrps) {
                $("#confirmModalContent").html("<div class=\"modal-body\"><div class=\"row mb-4\"><div class=\"col-12\"><h3 class=\"text-center\">Please wait...</h3></div></div><div class=\"d-flex justify-content-center\"><div id=\"loadingIndicator\" class=\"spinner-border\" role=\"status\"><span class=\"sr-only\">Loading...</span></div></div></div>");

                var URL = "${createLink(controller: 'product', action: 'ajaxSaveSupplierPriceUpdates')}";

                var supplierId = $('#supplier').val();
                var sinceDate = $('#sinceDate').val();
                var priceBandId = $('#priceBand').val();
                var effectiveDate = $('#effectiveDate').val();

                var data = { supplierId: supplierId, sinceDate: sinceDate, priceBandId: priceBandId, effectiveDate: effectiveDate, acceptRrps: acceptRrps };

                var checkedBoxes = $("input:checked:not(#checkAllCheckbox)");

                checkedBoxes.each(function(i, checkbox) {
                    var packId = $("[id^=product-" +$(checkbox).attr("id").substring(8) +"-packId]").val();
                    var sku = $("[id^=product-" +$(checkbox).attr("id").substring(8) +"-sku]").val();
                    var rrp = $("[id^=product-" +$(checkbox).attr("id").substring(8) +"-rrp]").val();
                    var price = $("[id^=product-" +$(checkbox).attr("id").substring(8) +"-price]").val();
                    var oldPrice = $("[id^=product-" +$(checkbox).attr("id").substring(8) +"-oldPrice]").val();
                    var productId = $("[id^=product-" +$(checkbox).attr("id").substring(8) +"-productId]").val();

                    data["priceChanges[" +i +"].packId"] = packId;
                    data["priceChanges[" +i +"].sku"] = sku;
                    data["priceChanges[" +i +"].oldPrice"] = oldPrice;
                    data["priceChanges[" +i +"].productId"] = productId;

                    if (acceptRrps) {
                        data["priceChanges[" +i +"].price"] = rrp;
                    } else {
                        data["priceChanges[" +i +"].price"] = price;
                    }
                });

                $.ajax({
                    url: URL,
                    data: data,
                    success: function(resp) {
                        $('#confirmModal').modal("hide");
                        $('#successModal').modal({ show: true });

                        search();
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
                            <li class="breadcrumb-item active" aria-current="page">Supplier Price Updates</li>
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

        <section id="header-container" class="container-fluid">
            <div class="row header-wl mt-3">
                <div class="col-8 offset-2">
                    <h2 class="mx-auto my-auto">Supplier Price Updates</h2>
                </div>

                <div class="col-2 text-right">
                    <button id="accept-rrps-button" class="btn btn-wl mr-3" onclick="savePricesButtonClicked(true);">Accept RRPs</button>
                    <button id="save-changes-button" class="btn btn-wl" onclick="savePricesButtonClicked(false);">Save Prices</button>
                </div>
            </div>
        </section>

        <section id="maintenance-search" class="container-fluid">
            <div class="row mt-4">
                <div class="col-5">
                    <div class="card bg-light border-wl">
                        <div class="card-header pointer" data-toggle="collapse" data-target="#filterCollapse" aria-expanded="true" aria-controls="filterCollapse">
                            <div class="row">
                                <div class="col-10">Filters</div>
                                <div class="col-2 text-right">
                                    <svg width="1em" height="1em" viewBox="0 0 16 16" class="bi bi-caret-down-fill text-right" fill="currentColor" xmlns="http://www.w3.org/2000/svg">
                                        <path d="M7.247 11.14L2.451 5.658C1.885 5.013 2.345 4 3.204 4h9.592a1 1 0 0 1 .753 1.659l-4.796 5.48a1 1 0 0 1-1.506 0z"/>
                                    </svg>
                                </div>
                            </div>
                        </div>
                        <div class="card-body collapse" id="filterCollapse">
                            <div class="form-group row">
                                <label for="supplier" class="col-2 col-form-label-sm text-right">Supplier</label>
                                <div class="col-4">
                                    <g:select name="supplier" from="${suppliers}" noSelection="['':'All Suppliers']" value="${supplier}" optionValue="name" optionKey="id" class="form-control select-border" />
                                </div>

                                <label for="sinceDate" class="col-2 col-form-label-sm text-right">Updates Since</label>
                                <div class="col-4">
                                    <g:textField name="sinceDate" class="form-control bottom-border" value="${new Date().format("dd/MM/yyyy")}" autocomplete="off" />
                                </div>
                            </div>

                            <div class="form-group row">
                                <label for="category" class="col-2 col-form-label-sm text-right">Category</label>
                                <div class="col-4">
                                    <g:categorySelect name="category" categories="${categories}" noSelectionValue="All Categories" />
                                </div>

                                <div class="col-4 offset-2 text-right">
                                    <button id="filter-reset-button" type="button" class="btn btn-danger text-right" onclick="resetButtonClicked()">Reset</button>
                                    <button id="filter-submit-button" type="button" class="btn btn-wl text-right" onclick="search()">Search</button>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>

                <div class="col-3 offset-4">
                    <div class="form-group row mt-5">
                        <label for="priceBand" class="col-4 col-form-label-sm text-right">Price Band</label>
                        <div class="col-8">
                            <g:select name="priceBand" from="${priceBands}" optionValue="description" optionKey="id" class="form-control select-border" onchange="search();" />
                        </div>
                    </div>

                    <div class="form-group row">
                        <label for="effectiveDate" class="col-4 col-form-label-sm text-right">Effective Date</label>
                        <div class="col-8">
                            <g:textField name="effectiveDate" class="form-control bottom-border" value="${(new Date() + 1).format("dd/MM/yyyy")}" autocomplete="off" />
                        </div>
                    </div>
                </div>
            </div>

            <div class="row mt-5 pb-2 ml-0 mr-0 table-wl bottom-border">
                <div class="col-1 font-weight-bold my-auto">
                    <g:checkBox name="checkAllCheckbox" class="col-12 wl-checkbox my-auto" />
                </div>
                <div class="col-3 font-weight-bold my-auto">Product</div>
                <div class="col-1 text-center font-weight-bold my-auto">Pack Size</div>
                <div class="col-1 text-center font-weight-bold my-auto">Effective Date</div>
                <div class="col-1 text-center font-weight-bold my-auto">Price Marked</div>
                <div class="col-1 text-center font-weight-bold my-auto">Old Pack Cost Price</div>
                <div class="col-1 text-center font-weight-bold my-auto">New Pack Cost Price</div>
                <div class="col-1 text-center font-weight-bold my-auto">New RRP</div>
                <div class="col-1 text-center font-weight-bold my-auto">Margin</div>
                <div class="col-1 text-center font-weight-bold my-auto">Retail Price</div>
            </div>

            <div id="search-results" class="align-content-center">
                <g:render template="supplierUpdatesSearchResults" />
            </div>
        </section>

        <!-- Confirmation modal -->
        <section id="confirm-modal" class="container-fluid">
            <div class="modal fade" id="confirmModal" tabindex="-1" role="dialog" aria-labelledby="confirmModalLabel" aria-hidden="true">
                <div class="modal-dialog" role="document">
                    <div class="modal-content">
                        <div class="modal-header">
                            <h2 id="confirmModalHeader">Accept RRPs?</h2>
                        </div>

                        <div class="modal-body" id="confirmModalContent">Are you sure you wish to accept the recommended retail price for all products?</div>

                        <div class="modal-footer">
                            <button type="button" id="confirmModalNoButton" class="btn btn-wl" data-dismiss="modal">No</button>
                            <button type="button" id="confirmModalYesButton" class="btn btn-success">Yes</button>
                        </div>
                    </div>
                </div>
            </div>
        </section>

        <!-- Success modal -->
        <section id="success-modal" class="container-fluid">
            <div class="modal fade" id="successModal" tabindex="-1" role="dialog" aria-labelledby="successModalLabel" aria-hidden="true">
                <div class="modal-dialog" role="document">
                    <div class="modal-content">
                        <div class="modal-header">
                            <h2>Success</h2>
                        </div>

                        <div class="modal-body">Prices saved successfully.</div>

                        <div class="modal-footer">
                            <button type="button" id="closeSuccessModalButton" class="btn btn-secondary" data-dismiss="modal">Close</button>
                        </div>
                    </div>
                </div>
            </div>
        </section>
    </body>
</html>