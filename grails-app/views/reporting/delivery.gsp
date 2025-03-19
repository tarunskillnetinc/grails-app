<%--
  Created by IntelliJ IDEA.
  User: JacobBrewer
  Date: 26/08/2022
  Time: 12:02
--%>

<%@ page contentType="text/html;charset=UTF-8" %>
<!doctype html>
<html>
<head>
    <meta name="layout" content="main"/>

    <title>Trust Retail</title>

    <asset:stylesheet src="bootstrap-datepicker3.min.css"/>
    <asset:javascript src="bootstrap-datepicker.min.js"/>
    <asset:javascript src="reporting.js"/>

    <script type='text/javascript'>
        var reportType = "${reportType}";
        var getDataUrl = "${createLink(controller: 'reporting', action: 'ajaxDelivery')}";
        var acceptDeliveryUrl = "${createLink(controller: 'reporting', action: 'ajaxAcceptDelivery')}"
        var saveReportColumnsUrl = "${createLink(controller: 'reporting', action: 'ajaxSaveReportColumns')}";

        $(document).ready(function () {
            $("#descriptionFilter").keydown(function(event) {
                if (event.keyCode === 13) {
                    event.preventDefault();

                    filterReport();
                    return false;
                }
            });

            filterReport();
        });

        function resetForm() {
            document.getElementById('descriptionFilter').value = "";
        }

        function acceptDeliveryButtonPressed() {
            $("#confirmModalContent").html("Are you sure you wish to receipt this delivery?");

            var confirmModalYesButton = $('#confirmModalYesButton');
            var confirmModalNoButton = $('#confirmModalNoButton');

            confirmModalYesButton.click(acceptDelivery);
            confirmModalYesButton.prop("disabled", false);
            confirmModalNoButton.click(confirmNoButtonPressed);
            confirmModalNoButton.prop("disabled", false);

            $('#confirmModal').modal({ show: true });
        }

        function acceptDelivery() {
            var params = {
                productListId: ${productListId}
            }

            $("#confirmModalContent").html("<div class=\"modal-body\"><div class=\"row mb-4\"><div class=\"col-12\"><h3 class=\"text-center\">Please wait...</h3></div></div><div class=\"d-flex justify-content-center\"><div id=\"loadingIndicator\" class=\"spinner-border\" role=\"status\"><span class=\"sr-only\">Loading...</span></div></div></div>");

            var confirmModalYesButton = $('#confirmModalYesButton');
            var confirmModalNoButton = $('#confirmModalNoButton');

            confirmModalYesButton.off("click");
            confirmModalYesButton.prop("disabled", true);

            confirmModalNoButton.off("click");
            confirmModalNoButton.prop("disabled", true);

            $.ajax({
                url: acceptDeliveryUrl,
                method: "POST",
                data: params,
                success: function () {
                    $("#acceptDelivery").remove();

                    $("#confirmModal").modal("hide");

                    $('#successModal').modal({ show: true });
                }
            });
        }

        function confirmNoButtonPressed() {
            $("#confirmModal").modal("hide");
        }
    </script>
</head>

<body>
    <section id="reporting-container" class="container-fluid">
        <g:reportBreadcrumb reportType="${reportType}" supplierName="${delivery?.supplierReference}" deliveryDate="${delivery?.dateStarted}" supplierId="${supplierId}" storeId="${storeId}" startDate="${startDate}" endDate="${endDate}" />

        <div class="header-wl mt-3">
            <h2 class="mx-auto">Delivery Report</h2>
        </div>

        <div class="row mt-4">
            <div class="col-lg-5 col-md-6">
                <div class="card bg-light border-wl">
                    <div class="card-header pointer" data-toggle="collapse" data-target="#filterCollapse"
                         aria-expanded="false" aria-controls="filterCollapse">
                        <div class="row">
                            <div class="col-10">Filters</div>

                            <div class="col-2 text-right">
                                <svg width="1em" height="1em" viewBox="0 0 16 16" class="bi bi-caret-down-fill text-right"
                                     fill="currentColor" xmlns="http://www.w3.org/2000/svg">
                                    <path d="M7.247 11.14L2.451 5.658C1.885 5.013 2.345 4 3.204 4h9.592a1 1 0 0 1 .753 1.659l-4.796 5.48a1 1 0 0 1-1.506 0z"/>
                                </svg>
                            </div>
                        </div>
                    </div>

                    <div class="card-body collapse" id="filterCollapse">
                        <g:form name="filtersForm" id="filtersForm">
                            <g:hiddenField name="productListId" value="${productListId}" />
                            <g:hiddenField name="startDate" value="${startDate?.toString("dd/MM/yyyy")}" />
                            <g:hiddenField name="endDate" value="${endDate?.toString("dd/MM/yyyy")}" />
                            <g:hiddenField name="supplierId" value="${supplierId}" />
                            <g:hiddenField name="storeId" value="${storeId}" />
                            <g:hiddenField name="caged" value="${caged}" />

                            <div class="form-group row">
                                <label for="descriptionFilter" class="col-2 col-form-label-sm text-right">Description</label>

                                <div class="col-10">
                                    <g:textField id="descriptionFilter" name="descriptionFilter" maxlength="100" value="${descriptionFilter}" class="form-control bottom-border" autocomplete="off" />
                                </div>
                            </div>

                            <div class="row">
                                <div class="col-12 text-right">
                                    <button id="reset-filters-btn" type="button" class="btn btn-danger text-right mr-2" onclick="resetForm();">Reset Filters</button>
                                    <button id="filter-submit-button" type="button" class="btn btn-wl text-right" onclick="filterReport();">Search</button>
                                </div>
                            </div>
                        </g:form>
                    </div>
                </div>
            </div>

            <div class="col-lg-3 offset-lg-2 col-md-3 text-right" style="margin-top: 8px;">
                <g:if test="${showAcceptDeliveryButton}">
                    <button id="acceptDelivery" class="btn btn-warning" onclick="acceptDeliveryButtonPressed();">Receipt Delivery</button>
                </g:if>

                <button class="btn btn-wl" onclick="exportToCsv();">Export to CSV</button>
            </div>

            <div class="col-lg-2 col-md-3">
                <div class="card bg-light border-wl">
                    <div class="card-header pointer" data-toggle="collapse" data-target="#columnsCollapse"
                         aria-expanded="false" aria-controls="columnsCollapse">
                        <div class="row">
                            <div class="col-10">Columns</div>

                            <div class="col-2 text-right">
                                <svg width="1em" height="1em" viewBox="0 0 16 16" class="bi bi-caret-down-fill text-right"
                                     fill="currentColor" xmlns="http://www.w3.org/2000/svg">
                                    <path d="M7.247 11.14L2.451 5.658C1.885 5.013 2.345 4 3.204 4h9.592a1 1 0 0 1 .753 1.659l-4.796 5.48a1 1 0 0 1-1.506 0z"/>
                                </svg>
                            </div>
                        </div>
                    </div>

                    <div class="card-body collapse" id="columnsCollapse">
                        <g:form name="reportColumnsForm" id="reportColumnsForm">
                            <div id="directDeliveryFilter">
                                <div class="form-group form-check">
                                    <g:checkBox name="columns" id="columnsSku" class="form-check-input"
                                                value="sku"
                                                checked="${!userColumns || userColumns?.columns?.find { it.column == 'sku' }?.enabled}"/>
                                    <label class="form-check-label" for="columnsSku">Product SKU</label>
                                </div>

                                <div class="form-group form-check">
                                    <g:checkBox name="columns" id="columnsDescription" class="form-check-input"
                                                value="description"
                                                checked="${!userColumns || userColumns?.columns?.find { it.column == 'description' }?.enabled}"/>
                                    <label class="form-check-label" for="columnsDescription">Product Description</label>
                                </div>

                                <div class="form-group form-check">
                                    <g:checkBox name="columns" id="columnsItemQuantity" class="form-check-input"
                                                value="itemQuantity"
                                                checked="${!userColumns || userColumns?.columns?.find { it.column == 'itemQuantity' }?.enabled}"/>
                                    <label class="form-check-label" for="columnsItemQuantity">Items Delivered</label>
                                </div>

                                <div class="form-group form-check">
                                    <g:checkBox name="columns" id="columnsLineValue" class="form-check-input"
                                                value="totalCost"
                                                checked="${!userColumns || userColumns?.columns?.find { it.column == 'totalCost' }?.enabled}"/>
                                    <label class="form-check-label" for="columnsLineValue">Total Cost</label>
                                </div>
                            </div>

                            <div id="cageDeliveryFilter">
                                <div class="form-group form-check">
                                    <g:checkBox name="columns" id="columnsCageBarcode" class="form-check-input"
                                                value="cageBarcode"
                                                checked="${!userColumns || userColumns?.columns?.find { it.column == 'cageBarcode' }?.enabled}"/>
                                    <label class="form-check-label" for="cageBarcode">Cage Barcode</label>
                                </div>

                                <div class="form-group form-check">
                                    <g:checkBox name="columns" id="columnsProcessingDate" class="form-check-input"
                                                value="processingDate"
                                                checked="${!userColumns || userColumns?.columns?.find { it.column == 'processingDate' }?.enabled}"/>
                                    <label class="form-check-label" for="columnsProcessingDate">Processing Date</label>
                                </div>

                                <div class="form-group form-check">
                                    <g:checkBox name="columns" id="columnsCases" class="form-check-input"
                                                value="cases"
                                                checked="${!userColumns || userColumns?.columns?.find { it.column == 'cases' }?.enabled}"/>
                                    <label class="form-check-label"
                                           for="columnsCases">${retailer?.config?.retailerTerminologyConfig?.packTerm}s in the Cage </label>
                                </div>
                            </div>

                            <button id="columns-submit-button" type="button" class="btn btn-wl"
                                    onclick="saveReportColumns();">Apply</button>
                        </g:form>
                    </div>
                </div>
            </div>
        </div>

        <div id="results-container" class="align-content-center">
        </div>
    </section>

    <!-- Confirmation modal -->
    <section id="confirm-modal" class="container-fluid">
        <div class="modal fade" id="confirmModal" tabindex="-1" role="dialog" aria-labelledby="confirmModalLabel" aria-hidden="true">
            <div class="modal-dialog" role="document">
                <div class="modal-content">
                    <div class="modal-header">
                        <h2 id="confirmModalHeader">Receipt Delivery</h2>
                    </div>

                    <div class="modal-body" id="confirmModalContent">Are you sure you wish to receipt this delivery?</div>

                    <div class="modal-footer">
                        <button type="button" id="confirmModalNoButton" class="btn btn-wl" data-dismiss="modal">No</button>
                        <button type="button" id="confirmModalYesButton" class="btn btn-success" onclick="acceptDelivery();">Yes</button>
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

                    <div class="modal-body">Delivery receipted.</div>

                    <div class="modal-footer">
                        <button type="button" id="closeSuccessModalButton" class="btn btn-secondary" data-dismiss="modal">Close</button>
                    </div>
                </div>
            </div>
        </div>
    </section>
</body>
</html>