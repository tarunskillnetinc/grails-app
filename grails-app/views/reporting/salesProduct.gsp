<!doctype html>
<html>
<head>
    <meta name="layout" content="main" />

    <title>WonderLane</title>

    <asset:stylesheet src="bootstrap-datepicker3.min.css" />
    <asset:javascript src="bootstrap-datepicker.min.js" />
    <asset:javascript src="reporting.js" />

    <script type='text/javascript'>
        var reportType = "${reportType}";
        var getDataUrl = "${createLink(controller: 'reporting', action: 'ajaxSalesProduct')}";
        var saveReportColumnsUrl = "${createLink(controller: 'reporting', action: 'ajaxSaveReportColumns')}";

        $(document).ready(function () {
            filterReport();
        });

        $(function() {
            $('#startDate').datepicker({
                format: "dd/mm/yyyy",
                weekStart: 1,
                startDate: "${(new Date() - 90).format("dd/MM/yyyy")}",
                endDate: "${new Date().format("dd/MM/yyyy")}",
                todayHighlight: true,
                autoclose: true,
                todayBtn: "linked",
                orientation: "bottom auto"
            });

            $('#endDate').datepicker({
                format: "dd/mm/yyyy",
                weekStart: 1,
                startDate: "${(new Date() - 90).format("dd/MM/yyyy")}",
                endDate: "${new Date().format("dd/MM/yyyy")}",
                todayHighlight: true,
                autoclose: true,
                todayBtn: "linked",
                orientation: "bottom auto"
            });
        });
    </script>
</head>
<body>
    <section id="reporting-container" class="container-fluid">
        <g:reportBreadcrumb reportType="${reportType}" productId="${productId}" startDate="${startDate}" endDate="${endDate}" />

        <div class="header-wl mt-3">
            <h2 class="mx-auto">Sales Report</h2>
        </div>

        <div class="row mt-4">
            <div class="col-5">
                <div class="card bg-light border-wl">
                    <div class="card-header pointer" data-toggle="collapse" data-target="#filterCollapse" aria-expanded="false" aria-controls="filterCollapse">
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
                        <g:form name="filtersForm" id="filtersForm">
                            <g:hiddenField name="productId" value="${productId}" />

                            <div class="form-group row">
                                <label for="startDate" class="col-2 col-form-label-sm text-right">Start Date</label>
                                <div class="col-4">
                                    <g:textField name="startDate" class="form-control bottom-border" value="${startDate ? startDate.format("dd/MM/yyyy") : new Date().format("dd/MM/yyyy")}" autocomplete="off" />
                                </div>

                                <label for="endDate" class="col-2 col-form-label-sm text-right">End Date</label>
                                <div class="col-4">
                                    <g:textField name="endDate" class="form-control bottom-border" value="${endDate ? endDate.format("dd/MM/yyyy") : new Date().format("dd/MM/yyyy")}" autocomplete="off" />
                                </div>
                            </div>

                            <div class="form-group row">
                                <label for="descriptionFilter" class="col-2 col-form-label-sm text-right">Description</label>
                                <div class="col-6">
                                    <g:textField name="descriptionFilter" maxlength="100" value="${descriptionFilter}" class="form-control bottom-border" autocomplete="off" />
                                </div>

                                <div class="col-4 text-right">
                                    <button id="filter-submit-button" type="button" class="btn btn-wl text-right" onclick="filterReport();">Filter</button>
                                </div>
                            </div>
                        </g:form>
                    </div>
                </div>
            </div>

            <div class="col-2 offset-3 text-right" style="margin-top: 8px;">
                <button class="btn btn-wl" onclick="exportToCsv();">Export to CSV</button>
            </div>

            <div class="col-2 offset-10 position-fixed z-index-1">
                <div class="card bg-light border-wl">
                    <div class="card-header pointer" data-toggle="collapse" data-target="#columnsCollapse" aria-expanded="false" aria-controls="columnsCollapse">
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
                                <g:checkBox name="columns" id="columnsDescription" class="form-check-input" value="description" checked="${!userColumns || userColumns?.columns?.find { it.column == 'description' }?.enabled}" />
                                <label class="form-check-label" for="columnsDescription">Description</label>
                            </div>
                            <div class="form-group form-check">
                                <g:checkBox name="columns" id="columnsQuantity" class="form-check-input" value="quantity" checked="${!userColumns || userColumns?.columns?.find { it.column == 'quantity' }?.enabled}" />
                                <label class="form-check-label" for="columnsQuantity">Total Qty</label>
                            </div>
                            <div class="form-group form-check">
                                <g:checkBox name="columns" id="columnsCostPrice" class="form-check-input" value="costPrice" checked="${!userColumns || userColumns?.columns?.find { it.column == 'costPrice' }?.enabled}" />
                                <label class="form-check-label" for="columnsCostPrice">Cost Price</label>
                            </div>
                            <div class="form-group form-check">
                                <g:checkBox name="columns" id="columnsNetTotal" class="form-check-input" value="netTotal" checked="${!userColumns || userColumns?.columns?.find { it.column == 'netTotal' }?.enabled}" />
                                <label class="form-check-label" for="columnsNetTotal">Net Total</label>
                            </div>
                            <div class="form-group form-check">
                                <g:checkBox name="columns" id="columnsVatAmount" class="form-check-input" value="vatAmount" checked="${!userColumns || userColumns?.columns?.find { it.column == 'vatAmount' }?.enabled}" />
                                <label class="form-check-label" for="columnsVatAmount">VAT Amount</label>
                            </div>
                            <div class="form-group form-check">
                                <g:checkBox name="columns" id="columnsProfit" class="form-check-input" value="profit" checked="${!userColumns || userColumns?.columns?.find { it.column == 'profit' }?.enabled}" />
                                <label class="form-check-label" for="columnsProfit">Profit</label>
                            </div>
                            <div class="form-group form-check">
                                <g:checkBox name="columns" id="columnsMargin" class="form-check-input" value="margin" checked="${!userColumns || userColumns?.columns?.find { it.column == 'margin' }?.enabled}" />
                                <label class="form-check-label" for="columnsMargin">Margin</label>
                            </div>
                            <div class="form-group form-check">
                                <g:checkBox name="columns" id="columnsUsersName" class="form-check-input" value="usersName" checked="${!userColumns || userColumns?.columns?.find { it.column == 'usersName' }?.enabled}" />
                                <label class="form-check-label" for="columnsUsersName">User</label>
                            </div>
                            <div class="form-group form-check">
                                <g:checkBox name="columns" id="columnsDateCreated" class="form-check-input" value="dateCreated" checked="${!userColumns || userColumns?.columns?.find { it.column == 'dateCreated' }?.enabled}" />
                                <label class="form-check-label" for="columnsDateCreated">Timestamp</label>
                            </div>

                            <button type="button" class="btn btn-wl" onclick="saveReportColumns();">Apply</button>
                        </g:form>
                    </div>
                </div>
            </div>
        </div>

        <div id="results-container" class="align-content-center">
            <g:render template="salesProductResults" />
        </div>
    </section>
</body>
</html>