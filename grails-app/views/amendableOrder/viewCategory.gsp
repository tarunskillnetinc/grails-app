<%@ page contentType="text/html;charset=UTF-8" %>
<html>
<head>
    <meta name="layout" content="main" />

    <title>Order Amendments</title>

    <asset:javascript src="jquery-ui.js" />
    <asset:stylesheet src="jquery-ui.css" />

    <asset:stylesheet src="bootstrap-datepicker3.min.css" />
    <asset:javascript src="bootstrap-datepicker.min.js" />
    <asset:javascript src="moment-with-locales.min.js"/>
    <asset:javascript src="numberHelper.js" />
    <asset:javascript src="validators/input-validator.js" />

    <script type="application/javascript">
        $(document).ready(function() {
            $('#deliveryDateSearch').datepicker({
                format: "dd/mm/yyyy",
                weekStart: 1,
                todayHighlight: true,
                autoclose: true,
                todayBtn: "linked",
                orientation: "bottom auto"
            });

            search();
        });

        function saveViewCategoryColumns() {
            var url = "${createLink(controller: 'amendableOrder', action: 'ajaxSaveCategoryColumns')}";

            var checkboxValues = { };

            $("#reportColumnsForm input:checkbox").each(function() {
                checkboxValues[$(this).val()] = this.checked;
            }).get();

            $.ajax({
                url: url,
                method: "POST",
                data: { reportColumns: JSON.stringify(checkboxValues) },
                success: function(resp) {
                    $("#columnsCollapse").collapse('hide');
                    search();
                }
            });
        }

        function search() {
            const url = "${createLink(controller: 'amendableOrder', action: 'ajaxViewCategoryOrders')}";

            $("#search-results").hide();
            $("#loading-indicator").show();

            $.ajax({
                url: url,
                data: { categoryId: ${category.getId()},
                    sku: $('#skuSearch').val(),
                    productDescription: $('#productDescriptionSearch').val(),
                    deliveryDate: $('#deliveryDateSearch').val(),
                    storeId: ${store.getId()}},
                success: function(resp) {
                    $('#results-container').html(resp);
                }
            });
        }

        function resetForm() {
            document.getElementById('skuSearch').value = null;
            document.getElementById('productDescriptionSearch').value = '';
            document.getElementById('deliveryDateSearch').value = '';
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
                    <li id="breadcrumb-2" class="breadcrumb-item" aria-current="page"><g:link action="index">Order Amendments</g:link></li>
                    <li id="breadcrumb-3" class="breadcrumb-item active" aria-current="page">${category.getDescription()}</li>
                </ol>
            </div>
        </div>
    </nav>
</section>

<section id="orderAmendment" class="container-fluid">
    <div class="row header-wl mt-3">
        <div class="col-8 offset-2">
            <h2 id="page-title" class="mx-auto">Order Amendments</h2>
        </div>
    </div>

    <g:if test="${flash.message}">
        <div id="success-message" class="alert alert-success alert-wl mx-0" role="alert">${flash.message}</div>
    </g:if>

    <g:if test="${flash.error}">
        <div id="error-message" class="alert alert-danger alert-wl mx-0" role="alert">${flash.error}</div>
    </g:if>

    <div class="row mt-4">
        <div class="col-4">
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

                <div class="card-body collapse" id="filterCollapse">
                    <div class="form-group row">
                        <label for="skuSearch" class="col-3 col-form-label-sm text-right">Line Number</label>
                        <div class="col-9 input-group">
                            <g:textField id="skuSearch" name="skuSearch" maxlength="100" value="${sku}" class="form-control" aria-describedby="select-addon2" />
                        </div>
                    </div>
                    <div class="form-group row">
                        <label for="productDescriptionSearch" class="col-3 col-form-label-sm text-right">Description</label>
                        <div class="col-9 input-group">
                            <g:textField id="productDescriptionSearch" name="productDescriptionSearch" maxlength="100" value="${productDescription}" class="form-control" aria-describedby="select-addon2" />
                        </div>
                    </div>
                    <div class="form-group row">
                        <label for="deliveryDateSearch" class="col-3 col-form-label-sm text-right">Delivery Date</label>
                        <div class="col-5 input-group">
                            <g:textField name="deliveryDateSearch" id="deliveryDateSearch" type="text" class="col-8 form-control bottom-border"
                                         value="${g.formatDate(format: "dd/MM/yyyy", date: deliveryDate?.toDate())}"
                                         autocomplete="off"/>
                        </div>
                    </div>

                    <div class="form-group row">
                        <div class="col-8 offset-4 text-right">
                            <button id="reset-filters-btn" type="button" class="btn btn-danger text-right" onclick="resetForm()">Reset Filters</button>
                            <button id="filter-submit-button" type="button" class="btn btn-wl text-right" onclick="search()">Search</button>
                        </div>
                    </div>
                </div>
            </div>
        </div>

        <div class="col-2 text-right offset-4">
            <g:link elementId="cancel-btn" action="index" role="button" class="btn btn-danger">Cancel</g:link>

            <button id="save-btn" class="btn btn-success" name="save" onclick="$('#saveAmendedOrderForm').submit();">Save</button>
        </div>
        <div class="col-2">
            <div class="card bg-light border-wl">
                <div id="columns-collapse" class="card-header pointer" data-toggle="collapse" data-target="#columnsCollapse" aria-expanded="false" aria-controls="columnsCollapse">
                    <div class="row">
                        <div class="col-10">Columns</div>
                        <div class="col-2 text-right">
                            <svg width="1em" height="1em" viewBox="0 0 16 16" class="bi bi-caret-down-fill text-right" fill="currentColor" xmlns="http://www.w3.org/2000/svg">
                                <path d="M7.247 11.14L2.451 5.658C1.885 5.013 2.345 4 3.204 4h9.592a1 1 0 0 1 .753 1.659l-4.796 5.48a1 1 0 0 1-1.506 0z"/>
                            </svg>
                        </div>
                    </div>
                </div>
                <div class="card-body collapse" id="columnsCollapse">
                    <g:form name="reportColumnsForm" id="reportColumnsForm">
                        <div class="form-group form-check">
                            <g:checkBox name="columns" id="columnsSku" class="form-check-input" value="sku" checked="${!userColumns || userColumns?.columns?.find { it.column == 'sku' }?.enabled}" />
                            <label class="form-check-label" for="columnsSku">Line Number</label>
                        </div>
                        <div class="form-group form-check">
                            <g:checkBox name="columns" id="columnsProductDescription" class="form-check-input" value="productDescription" checked="${!userColumns || userColumns?.columns?.find { it.column == 'productDescription' }?.enabled}" />
                            <label class="form-check-label" for="columnsProductDescription">Description</label>
                        </div>
                        <div class="form-group form-check">
                            <g:checkBox name="columns" id="columnsPrice" class="form-check-input" value="price" checked="${!userColumns || userColumns?.columns?.find { it.column == 'price' }?.enabled}" />
                            <label class="form-check-label" for="columnsPrice">Price</label>
                        </div>
                        <div class="form-group form-check">
                            <g:checkBox name="columns" id="columnsCaseSize" class="form-check-input" value="caseSize" checked="${!userColumns || userColumns?.columns?.find { it.column == 'caseSize' }?.enabled}" />
                            <label class="form-check-label" for="columnsCaseSize">Case Size</label>
                        </div>
                        <div class="form-group form-check">
                            <g:checkBox name="columns" id="columnsDeliveryDate" class="form-check-input" value="deliveryDate" checked="${!userColumns || userColumns?.columns?.find { it.column == 'deliveryDate' }?.enabled}" />
                            <label class="form-check-label" for="columnsDeliveryDate">Delivery Date</label>
                        </div>
                        <div class="form-group form-check">
                            <g:checkBox name="columns" id="columnsDemand" class="form-check-input" value="demand" checked="${!userColumns || userColumns?.columns?.find { it.column == 'demand' }?.enabled}" />
                            <label class="form-check-label" for="columnsDemand">Demand</label>
                        </div>
                        <div class="form-group form-check">
                            <g:checkBox name="columns" id="columnsAvailable" class="form-check-input" value="available" checked="${!userColumns || userColumns?.columns?.find { it.column == 'available' }?.enabled}" />
                            <label class="form-check-label" for="columnsAvailable">Available</label>
                        </div>
                        <div class="form-group form-check">
                            <g:checkBox name="columns" id="columnsOrderQuantity" class="form-check-input" value="orderQuantity" checked="${!userColumns || userColumns?.columns?.find { it.column == 'orderQuantity' }?.enabled}" />
                            <label class="form-check-label" for="columnsOrderQuantity">Order Quantity</label>
                        </div>
                        <div class="form-group form-check">
                            <g:checkBox name="columns" id="columnsMessages" class="form-check-input" value="messages" checked="${!userColumns || userColumns?.columns?.find { it.column == 'messages' }?.enabled}" />
                            <label class="form-check-label" for="columnsMessages">Messages</label>
                        </div>

                        <button id="columns-submit-button" type="button" class="btn btn-wl" onclick="saveViewCategoryColumns();">Apply</button>
                    </g:form>
                </div>
            </div>
        </div>
    </div>

    <div id="results-container" class="align-content-center">
        <g:render template="categoryResults" />
    </div>
</section>
</body>
</html>