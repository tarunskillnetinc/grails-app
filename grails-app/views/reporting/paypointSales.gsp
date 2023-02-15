<%@ page contentType="text/html;charset=UTF-8" %>
<html>
<head>
    <meta name="layout" content="main" />

    <title>WonderLane</title>

    <asset:stylesheet src="bootstrap-datepicker3.min.css" />
    <asset:javascript src="bootstrap-datepicker.min.js" />
    <asset:javascript src="reporting.js" />

    <script type="application/javascript">
        var reportType = "${reportType}";
        var getDataUrl = "${createLink(controller: 'reporting', action: 'ajaxPayPointSales')}";
        var saveReportColumnsUrl = "${createLink(controller: 'reporting', action: 'ajaxSaveReportColumns')}";

        $(document).ready(function () {
            $('#startDate').on("change", function () {
                $('#startDate').val(this.value);
                $('#startDate').removeClass('is-invalid');
                $('#endDate').datepicker('setStartDate', this.value);
            });

            $('#endDate').on("change", function () {
                $('#endDate').val(this.value);
                $('#endDate').removeClass('is-invalid');
                $('#startDate').datepicker('setEndDate', this.value);
            });

            filterReport();
        });

        $(function() {
            $('#startDate').datepicker({
                format: "dd/mm/yyyy",
                weekStart: 1,
                startDate: "${(new Date() - 7).format("dd/MM/yyyy")}",
                endDate: "${new Date().format("dd/MM/yyyy")}",
                todayHighlight: true,
                autoclose: true,
                todayBtn: "linked",
                orientation: "bottom auto"
            });

            $('#endDate').datepicker({
                format: "dd/mm/yyyy",
                weekStart: 1,
                startDate: "${new Date().format("dd/MM/yyyy")}",
                endDate: "${new Date().format("dd/MM/yyyy")}",
                todayHighlight: true,
                autoclose: true,
                todayBtn: "linked",
                orientation: "bottom auto"
            });
        });

        function resetForm() {
            document.getElementById('startDate').value = "${new Date().format("dd/MM/yyyy")}";
            $('#startDate').datepicker('setStartDate', "${(new Date() - 7).format("dd/MM/yyyy")}");
            $('#startDate').datepicker('setEndDate', "${new Date().format("dd/MM/yyyy")}");

            document.getElementById("endDate").value = "${new Date().format("dd/MM/yyyy")}";
            $('#endDate').datepicker('setStartDate', "${new Date().format("dd/MM/yyyy")}");
            $('#endDate').datepicker('setEndDate', "${new Date().format("dd/MM/yyyy")}");

            document.getElementById('descriptionFilter').value = null;
            $('#storeFilter').prop("selectedIndex", 0);
            $('#statusFilter').prop("selectedIndex", 0);
        }
    </script>

</head>

<body>
    <section id="reporting-container" class="container-fluid">
        <g:reportBreadcrumb reportType="${reportType}" />

        <div class="header-wl mt-3">
            <h2 id="page-title" class="mx-auto">PayPoint Sales Report</h2>
        </div>

        <div class="row mt-4">
            <div class="col-5">
                <div class="card bg-light border-wl">
                    <div id="filter-collapse" class="card-header pointer" data-toggle="collapse" data-target="#filterCollapse" aria-expanded="false" aria-controls="filterCollapse">
                        <div class="row">
                            <div id="filter-text" class="col-10">Filters</div>
                            <div class="col-2 text-right">
                                <svg width="1em" height="1em" viewBox="0 0 16 16" class="bi bi-caret-down-fill text-right" fill="currentColor" xmlns="http://www.w3.org/2000/svg">
                                    <path d="M7.247 11.14L2.451 5.658C1.885 5.013 2.345 4 3.204 4h9.592a1 1 0 0 1 .753 1.659l-4.796 5.48a1 1 0 0 1-1.506 0z"/>
                                </svg>
                            </div>
                        </div>
                    </div>
                    <div class="card-body collapse" id="filterCollapse">
                        <g:form name="filtersForm" id="filtersForm">
                            <div class="form-group row">
                                <label for="startDate" class="col-2 col-form-label-sm text-right">Start Date</label>
                                <div class="col-4">
                                    <g:textField id="startDate" name="startDate" onkeydown="return false" class="form-control bottom-border" value="${startDate.toString("dd/MM/yyyy")}" autocomplete="off" />
                                </div>

                                <label for="endDate" class="col-2 col-form-label-sm text-right">End Date</label>
                                <div class="col-4">
                                    <g:textField id="endDate" name="endDate" onkeydown="return false" class="form-control bottom-border" value="${endDate.toString("dd/MM/yyyy")}" autocomplete="off" />
                                </div>
                            </div>

                            <div class="form-group row">
                                <label for="storeFilter" class="col-2 col-form-label-sm text-right">Store Id</label>
                                <div class="col-4">
                                    <g:select name="storeFilter" from="${stores}" optionValue="storeId"
                                              optionKey="id"
                                              noSelection="${sec.loggedInUserInfo(field: 'storeId') ? ['': sec.loggedInUserInfo(field: 'storeNumber')] : ['': 'All']}"
                                              class="form-control select-border"
                                              disabled="${sec.loggedInUserInfo(field: 'storeId') ? true : false}"></g:select>
                                </div>

                                <label for="statusFilter" class="col-2 col-form-label-sm text-right">Status</label>
                                <div class="col-4">
                                    <g:select id="statusFilter" name="statusFilter" from="${['Success', 'Failure']}" noSelection="['':'']" value="${statusFilter}" class="form-control select-border" />
                                </div>
                            </div>

                            <div class="form-group row">
                                <label for="descriptionFilter" class="col-2 col-form-label-sm text-right">Description</label>
                                <div class="col-6">
                                    <g:textField id="descriptionFilter" name="descriptionFilter" maxlength="100" value="${descriptionFilter}" class="form-control bottom-border" autocomplete="off" />
                                </div>

                                <div class="col-4 text-right">
                                    <button type="button" class="btn btn-danger text-right mr-2" onclick="resetForm()">Reset Filters</button>
                                    <button id="filter-submit-button" type="button" class="btn btn-wl text-right" onclick="filterReport()">Search</button>
                                </div>
                            </div>
                        </g:form>
                    </div>
                </div>
            </div>

            <div class="col-2 offset-2 text-right" style="margin-top: 8px;">
                <button id="export-to-pp-csv" class="btn btn-wl" onclick="exportToPPCsv();">Export weekly PP Report</button>
                <button id="export-to-csv" class="btn btn-wl mt-2" onclick="exportToCsv();">Export filtered CSV</button>
            </div>

            <div class="col-2">
                <div class="card bg-light border-wl">
                    <div id="columns-collapse" class="card-header pointer" data-toggle="collapse" data-target="#columnsCollapse" aria-expanded="false" aria-controls="columnsCollapse">
                        <div class="row">
                            <div id="columns-text" class="col-10">Columns</div>
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
                                <g:checkBox name="columns" id="columnsStoreId" class="form-check-input" value="storeId" checked="${!userColumns || userColumns?.columns?.find { it.column == 'storeId' }?.enabled}" />
                                <label class="form-check-label" for="columnsStoreId">StoreId</label>
                            </div>
                            <div class="form-group form-check">
                                <g:checkBox name="columns" id="columnsTransactionId" class="form-check-input" value="wlTransactionId" checked="${!userColumns || userColumns?.columns?.find { it.column == 'wlTransactionId' }?.enabled}" />
                                <label class="form-check-label" for="columnsTransactionId">Txn Id</label>
                            </div>
                            <div class="form-group form-check">
                                <g:checkBox name="columns" id="columnsPPTransactionId" class="form-check-input" value="ppTransactionId" checked="${!userColumns || userColumns?.columns?.find { it.column == 'ppTransactionId' }?.enabled}" />
                                <label class="form-check-label" for="columnsPPTransactionId">PP Txn Id</label>
                            </div>
                            <div class="form-group form-check">
                                <g:checkBox name="columns" id="columnsTerminalId" class="form-check-input" value="terminalId" checked="${!userColumns || userColumns?.columns?.find { it.column == 'terminalId' }?.enabled}" />
                                <label class="form-check-label" for="columnsTerminalId">TID</label>
                            </div>
                            <div class="form-group form-check">
                                <g:checkBox name="columns" id="columnsDescription" class="form-check-input" value="description" checked="${!userColumns || userColumns?.columns?.find { it.column == 'description' }?.enabled}" />
                                <label class="form-check-label" for="columnsDescription">Description</label>
                            </div>
                            <div class="form-group form-check">
                                <g:checkBox name="columns" id="columnsType" class="form-check-input" value="type" checked="${!userColumns || userColumns?.columns?.find { it.column == 'type' }?.enabled}" />
                                <label class="form-check-label" for="columnsType">Type</label>
                            </div>
                            <div class="form-group form-check">
                                <g:checkBox name="columns" id="columnsValue" class="form-check-input" value="value" checked="${!userColumns || userColumns?.columns?.find { it.column == 'value' }?.enabled}" />
                                <label class="form-check-label" for="columnsValue">Value</label>
                            </div>
                            <div class="form-group form-check">
                                <g:checkBox name="columns" id="columnsStatus" class="form-check-input" value="status" checked="${!userColumns || userColumns?.columns?.find { it.column == 'status' }?.enabled}" />
                                <label class="form-check-label" for="columnsStatus">Status</label>
                            </div>
                            <div class="form-group form-check">
                                <g:checkBox name="columns" id="columnsTransactionDate" class="form-check-input" value="transactionDate" checked="${!userColumns || userColumns?.columns?.find { it.column == 'transactionDate' }?.enabled}" />
                                <label class="form-check-label" for="columnsTransactionDate">Txn Date</label>
                            </div>

                            <button id="columns-submit-button" type="button" class="btn btn-wl" onclick="saveReportColumns();">Apply</button>
                        </g:form>
                    </div>
                </div>
            </div>
        </div>

        <div id="results-container" class="align-content-center">
            <g:render template="paypointSalesResults" />
        </div>
    </section>
</body>
</html>