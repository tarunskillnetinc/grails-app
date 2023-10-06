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
        var getDataUrl = "${createLink(controller: 'reporting', action: 'ajaxDeliveryPackLines')}";
        var saveReportColumnsUrl = "${createLink(controller: 'reporting', action: 'ajaxSaveReportColumns')}";

        $(document).ready(function () {
            filterReport();
        });

        function resetForm() {

        }
    </script>
</head>

<body>
<section id="reporting-container" class="container-fluid">
    <g:reportBreadcrumb reportType="${reportType}" supplierName="${delivery?.supplierReference}" productListId="${productListId}" productListItemId="${productListItemId}" deliveryDate="${delivery?.dateStarted}" supplierId="${supplierId}" storeId="${storeId}" startDate="${startDate}" endDate="${endDate}" descriptionFilter="${descriptionFilter}" productDescription="${productListItem?.productVariant?.product?.description}" />

    <div class="header-wl mt-3">
        <h2 class="mx-auto">Delivery Report</h2>
    </div>

    <div class="row mt-4">
        <div class="col-lg-5 col-md-6">
            <g:form name="filtersForm" id="filtersForm">
                <g:hiddenField name="productListItemId" value="${productListItemId}" />
            </g:form>
        </div>

        <div class="col-lg-2 offset-lg-5 col-md-3 offset-md-3">
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
                        <div class="form-group form-check">
                            <g:checkBox name="columns" id="columnsDescription" class="form-check-input"
                                        value="description"
                                        checked="${!userColumns || userColumns?.columns?.find { it.column == 'description' }?.enabled}"/>
                            <label class="form-check-label" for="columnsDescription">Description</label>
                        </div>

                        <div class="form-group form-check">
                            <g:checkBox name="columns" id="columnsPackCost" class="form-check-input"
                                        value="packCost"
                                        checked="${!userColumns || userColumns?.columns?.find { it.column == 'packCost' }?.enabled}"/>
                            <label class="form-check-label" for="columnsPackCost">Pack Cost</label>
                        </div>

                        <div class="form-group form-check">
                            <g:checkBox name="columns" id="columnsPackSize" class="form-check-input" value="packSize"
                                        checked="${!userColumns || userColumns?.columns?.find { it.column == 'packSize' }?.enabled}"/>
                            <label class="form-check-label" for="columnsPackSize">Pack Size</label>
                        </div>

                        <div class="form-group form-check">
                            <g:checkBox name="columns" id="columnsDeliveryQuantity" class="form-check-input"
                                        value="deliveryQuantity"
                                        checked="${!userColumns || userColumns?.columns?.find { it.column == 'deliveryQuantity' }?.enabled}"/>
                            <label class="form-check-label" for="columnsDeliveryQuantity">Packs Delivered</label>
                        </div>

                        <div class="form-group form-check">
                            <g:checkBox name="columns" id="columnsTotalQuantity" class="form-check-input"
                                        value="totalQuantity"
                                        checked="${!userColumns || userColumns?.columns?.find { it.column == 'totalQuantity' }?.enabled}"/>
                            <label class="form-check-label" for="columnsTotalQuantity">Total Quantity</label>
                        </div>

                        <div class="form-group form-check">
                            <g:checkBox name="columns" id="columnsCurrentSell" class="form-check-input"
                                        value="currentSell"
                                        checked="${!userColumns || userColumns?.columns?.find { it.column == 'currentSell' }?.enabled}"/>
                            <label class="form-check-label" for="columnsCurrentSell">Retail Price</label>
                        </div>

                        <div class="form-group form-check">
                            <g:checkBox name="columns" id="columnsPackValue" class="form-check-input"
                                        value="totalSellValue"
                                        checked="${!userColumns || userColumns?.columns?.find { it.column == 'totalSellValue' }?.enabled}"/>
                            <label class="form-check-label" for="columnsPackValue">Total Sell Value</label>
                        </div>

                        <button id="columns-submit-button" type="button" class="btn btn-wl"
                                onclick="saveReportColumns();">Apply</button>
                    </g:form>
                </div>
            </div>
        </div>
    </div>

    <div id="results-container" class="align-content-center">
        <g:render template="deliveryPackLineResults"/>
    </div>
</section>
</body>
</html>