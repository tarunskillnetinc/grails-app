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
        var getDataUrl = "${createLink(controller: 'reporting', action: 'ajaxPromotionsGrouped')}";
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
                endDate: "${new Date().format("dd/MM/yyyy")}",
                todayHighlight: true,
                autoclose: true,
                todayBtn: "linked",
                orientation: "bottom auto"
            });
        });

        function resetForm() {
            document.getElementById('startDate').value = "${new Date().format("dd/MM/yyyy")}";
            $('#startDate').datepicker('setStartDate', "${(new Date() - 90).format("dd/MM/yyyy")}");
            $('#startDate').datepicker('setEndDate', "${new Date().format("dd/MM/yyyy")}");

            document.getElementById("endDate").value = "${new Date().format("dd/MM/yyyy")}";
            $('#endDate').datepicker('setStartDate', "${new Date().format("dd/MM/yyyy")}");

            document.getElementById('promotionTypeFilter').value = null;
            document.getElementById('descriptionFilter').value = null;
        }
    </script>
</head>
<body>
    <section id="reporting-container" class="container-fluid">
        <g:reportBreadcrumb reportType="${reportType}" />

        <div class="header-wl mt-3">
            <h2 class="mx-auto">Promotional Sales Report</h2>
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
                            <div class="form-group row">
                                <label for="startDate" class="col-2 col-form-label-sm text-right">Start Date</label>
                                <div class="col-4">
                                    <g:textField name="startDate" onkeydown="return false" class="form-control bottom-border" value="${startDate?.toString("dd/MM/yyyy")}" autocomplete="off" />
                                </div>

                                <label for="endDate" class="col-2 col-form-label-sm text-right">End Date</label>
                                <div class="col-4">
                                    <g:textField name="endDate" onkeydown="return false" class="form-control bottom-border" value="${endDate?.toString("dd/MM/yyyy")}" autocomplete="off" />
                                </div>
                            </div>

                            <div class="form-group row">
                                <label for="descriptionFilter" class="col-2 col-form-label-sm text-right">Description</label>

                                <div class="col-4">
                                    <g:textField name="descriptionFilter" maxlength="100"
                                                 value="${descriptionFilter}" class="form-control bottom-border"
                                                 autocomplete="off"/>
                                </div>

                                <label for="promotionTypeFilter" class="col-2 col-form-label-sm text-right">Type</label>

                                <div class="col-4">
                                    <g:select name="promotionTypeFilter" from="${promotionTypes}"
                                              noSelection="['': '']" value="${promotionTypeFilter}"
                                              valueMessagePrefix="PromotionType"
                                              class="form-control select-border"/>
                                </div>
                            </div>

                            <div class="row">
                                <div class="col-4 offset-8 text-right">
                                    <button type="button" class="btn btn-danger text-right mr-2" onclick="resetForm()">Reset Filters</button>
                                    <button id="filter-submit-button" type="button" class="btn btn-wl text-right" onclick="filterReport()">Search</button>
                                </div>
                            </div>
                        </g:form>
                    </div>
                </div>
            </div>

            <div class="col-2 offset-3 text-right" style="margin-top: 8px;">
                <button class="btn btn-wl" onclick="exportToCsv();">Export to CSV</button>
            </div>

            <div class="col-2">
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
                                <g:checkBox name="columns" id="columnsType" class="form-check-input" value="type" checked="${!userColumns || userColumns?.columns?.find { it.column == 'type' }?.enabled}" />
                                <label class="form-check-label" for="columnsType">Type</label>
                            </div>
                            <div class="form-group form-check">
                                <g:checkBox name="columns" id="columnsQuantity" class="form-check-input" value="quantity" checked="${!userColumns || userColumns?.columns?.find { it.column == 'quantity' }?.enabled}" />
                                <label class="form-check-label" for="columnsQuantity">Quantity</label>
                            </div>
                            <div class="form-group form-check">
                                <g:checkBox name="columns" id="columnsFullPrice" class="form-check-input" value="fullPrice" checked="${!userColumns || userColumns?.columns?.find { it.column == 'fullPrice' }?.enabled}" />
                                <label class="form-check-label" for="columnsFullPrice">Full Price</label>
                            </div>
                            <div class="form-group form-check">
                                <g:checkBox name="columns" id="columnsDiscount" class="form-check-input" value="discount" checked="${!userColumns || userColumns?.columns?.find { it.column == 'discount' }?.enabled}" />
                                <label class="form-check-label" for="columnsDiscount">Discount</label>
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
                                <g:checkBox name="columns" id="columnsVat" class="form-check-input" value="vat" checked="${!userColumns || userColumns?.columns?.find { it.column == 'vat' }?.enabled}" />
                                <label class="form-check-label" for="columnsVat">VAT</label>
                            </div>

                            <button type="button" class="btn btn-wl" onclick="saveReportColumns();">Apply</button>
                        </g:form>
                    </div>
                </div>
            </div>
        </div>

        <div id="results-container" class="align-content-center">
            <g:render template="promotionsGroupedResults" />
        </div>
    </section>
</body>
</html>