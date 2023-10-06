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
        var getDataUrl = "${createLink(controller: 'reporting', action: 'ajaxProductLists')}";
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

        $(function () {
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
                startDate: "${new Date().format("dd/MM/yyyy")}",
                endDate: "${(new Date() + 7).format("dd/MM/yyyy")}",
                todayHighlight: true,
                autoclose: true,
                todayBtn: "linked",
                orientation: "bottom auto"
            });
        });

        function resetForm() {
            document.getElementById('startDate').value = "${(new Date()).format("dd/MM/yyyy")}";
            $('#startDate').datepicker('setStartDate', "${(new Date() - 90).format("dd/MM/yyyy")}");
            $('#startDate').datepicker('setEndDate', "${new Date().format("dd/MM/yyyy")}");

            document.getElementById("endDate").value = "${new Date().format("dd/MM/yyyy")}";
            $('#endDate').datepicker('setStartDate', "${new Date().format("dd/MM/yyyy")}");
            $('#endDate').datepicker('setEndDate', "${(new Date() + 7).format("dd/MM/yyyy")}");

            document.getElementById('storeFilter').value = '';
            document.getElementById('typeFilter').value = '';
        }
    </script>
</head>

<body>
<section id="reporting-container" class="container-fluid">
    <g:reportBreadcrumb reportType="${reportType}"/>

    <div class="header-wl mt-3">
        <h2 id="page-title" class="mx-auto">Product Lists Report</h2>
    </div>

    <div class="row mt-4">
        <div class="col-lg-5 col-md-6">
            <div class="card bg-light border-wl">
                <div id="filter-collapse" class="card-header pointer" data-toggle="collapse" data-target="#filterCollapse"
                     aria-expanded="false" aria-controls="filterCollapse">
                    <div class="row">
                        <div id="filter-text" class="col-10">Filters</div>

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
                        <div class="form-group row">
                            <label for="startDate" class="col-2 col-form-label-sm text-right">Start Date</label>

                            <div class="col-4">
                                <g:textField id="startDate" name="startDate" onkeydown="return false"
                                             class="form-control bottom-border"
                                             value="${startDate?.toString("dd/MM/yyyy")}" autocomplete="off"/>
                            </div>

                            <label for="endDate" class="col-2 col-form-label-sm text-right">End Date</label>

                            <div class="col-4">
                                <g:textField id="endDate" name="endDate" onkeydown="return false"
                                             class="form-control bottom-border"
                                             value="${endDate?.toString("dd/MM/yyyy")}" autocomplete="off"/>
                            </div>
                        </div>

                        <div class="form-group row">
                            <label for="storeFilter" class="col-2 col-form-label-sm text-right">Store</label>

                            <div class="col-4">
                                <g:select name="storeFilter" from="${stores}" optionValue="${{it.config.storeNumber}}"
                                          optionKey="id"
                                          noSelection="${sec.loggedInUserInfo(field: 'storeId') ? ['': sec.loggedInUserInfo(field: 'storeNumber')] : ['': 'All']}"
                                          value="${storeId}"
                                          class="form-control select-border"
                                          disabled="${sec.loggedInUserInfo(field: 'storeId') ? true : false}"></g:select>
                            </div>

                            <label for="typeFilter" class="col-2 col-form-label-sm text-right">Type</label>

                            <div class="col-4">
                                <g:select name="typeFilter" from="${types}" valueMessagePrefix="ProductListType"
                                          noSelection="['': 'All']"
                                          value="${type}"
                                          class="form-control select-border"></g:select>
                            </div>
                        </div>

                        <div class="row">
                            <div class="col-12 text-right">
                                <button id="reset-filters-btn" type="button" class="btn btn-danger text-right mr-2"
                                        onclick="resetForm();">Reset Filters</button>
                                <button id="filter-submit-button" type="button" class="btn btn-wl text-right"
                                        onclick="filterReport();">Search</button>
                            </div>
                        </div>
                    </g:form>
                </div>
            </div>
        </div>

        <div class="col-lg-2 offset-lg-3 col-md-3 text-right" style="margin-top: 8px;">
            <button id="export-to-csv" class="btn btn-wl" onclick="exportToCsv();">Export to CSV</button>
        </div>

        <div class="col-lg-2 col-md-3">
            <div class="card bg-light border-wl">
                <div id="columns-collapse" class="card-header pointer" data-toggle="collapse" data-target="#columnsCollapse"
                     aria-expanded="false" aria-controls="columnsCollapse">
                    <div class="row">
                        <div id="columns-text" class="col-10">Columns</div>

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
                            <g:checkBox name="columns" id="columnsId" class="form-check-input" value="id"
                                        checked="${!userColumns || userColumns?.columns?.find { it.column == 'id' }?.enabled}"/>
                            <label class="form-check-label" for="columnsId">ID</label>
                        </div>

                        <div class="form-group form-check">
                            <g:checkBox name="columns" id="columnsStoreNumber" class="form-check-input" value="storeId"
                                        checked="${!userColumns || userColumns?.columns?.find { it.column == 'storeId' }?.enabled}"/>
                            <label class="form-check-label" for="columnsStoreNumber">Store</label>
                        </div>

                        <div class="form-group form-check">
                            <g:checkBox name="columns" id="columnsType" class="form-check-input" value="type"
                                        checked="${!userColumns || userColumns?.columns?.find { it.column == 'type' }?.enabled}"/>
                            <label class="form-check-label" for="columnsType">Type</label>
                        </div>

                        <div class="form-group form-check">
                            <g:checkBox name="columns" id="columnsStatus" class="form-check-input" value="status"
                                        checked="${!userColumns || userColumns?.columns?.find { it.column == 'status' }?.enabled}"/>
                            <label class="form-check-label" for="columnsStatus">Status</label>
                        </div>

                        <div class="form-group form-check">
                            <g:checkBox name="columns" id="columnsStartDate" class="form-check-input"
                                        value="startDate"
                                        checked="${!userColumns || userColumns?.columns?.find { it.column == 'startDate' }?.enabled}"/>
                            <label class="form-check-label" for="columnsStartDate">Start Date</label>
                        </div>

                        <div class="form-group form-check">
                            <g:checkBox name="columns" id="columnsNumberOfItems" class="form-check-input"
                                        value="numberOfItems"
                                        checked="${!userColumns || userColumns?.columns?.find { it.column == 'numberOfItems' }?.enabled}"/>
                            <label class="form-check-label" for="columnsNumberOfItems">Number of Items</label>
                        </div>

                        <button id="columns-submit-button" type="button" class="btn btn-wl" onclick="saveReportColumns();">Apply</button>
                    </g:form>
                </div>
            </div>
        </div>
    </div>

    <div id="results-container" class="align-content-center">
        <g:render template="productListsResults" />
    </div>
</section>
</body>
</html>