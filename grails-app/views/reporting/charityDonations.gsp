<%@ page contentType="text/html;charset=UTF-8" %>
<html>
<head>
    <meta name="layout" content="main"/>

    <title>Trust Retail</title>

    <asset:stylesheet src="bootstrap-datepicker3.min.css"/>
    <asset:javascript src="bootstrap-datepicker.min.js"/>
    <asset:javascript src="reporting.js"/>

    <script type="application/javascript">
        var reportType = "${reportType}";
        var getDataUrl = "${createLink(controller: 'reporting', action: 'ajaxCharityDonations')}";
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
                /* Allow up to a year in the past */
                startDate: "${(new Date() - 365).format("dd/MM/yyyy")}",
                endDate: "${new Date().format("dd/MM/yyyy")}",
                todayHighlight: true,
                autoclose: true,
                todayBtn: "linked",
                orientation: "bottom auto"
            }).on('changeDate', function(e) {
                var startDate = e.date;
                var maxEndDate = new Date(startDate);
                /* Is set to 91 so that the last day is included in the 90 day period */
                maxEndDate.setDate(startDate.getDate() + 91);

                var today = new Date();
                if (maxEndDate > today) {
                    maxEndDate = today;
                }

                $('#endDate').datepicker('setStartDate', startDate);
                $('#endDate').datepicker('setEndDate', maxEndDate);
                $('#endDate').datepicker('setDate', startDate);
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
            $('#startDate').val("${new Date().format("dd/MM/yyyy")}");
            $('#endDate').val("${new Date().format("dd/MM/yyyy")}");

            $('#startDate').datepicker('setStartDate', "${(new Date() - 365).format('dd/MM/yyyy')}");
            $('#startDate').datepicker('setEndDate', "${new Date().format('dd/MM/yyyy')}");

            $('#endDate').datepicker('setStartDate', "${new Date().format('dd/MM/yyyy')}");
            $('#endDate').datepicker('setEndDate', "${new Date().format('dd/MM/yyyy')}");

            document.getElementById('storeFilter').value = '';
        }
    </script>
</head>
<body>
    <section id="reporting-container" class="container-fluid">
        <g:reportBreadcrumb reportType="${reportType}"/>

        <div class="header-wl mt-3">
            <h2 id="page-title" class="mx-auto">Charity Donations Report</h2>
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
                                <label for="storeFilter" class="col-2 col-form-label-sm text-right">Store</label>
                                <div class="col-3">
                                    <g:select name="storeFilter" from="${stores}" optionValue="${{it.config.storeNumber}}"
                                              optionKey="id"
                                              noSelection="${sec.loggedInUserInfo(field: 'storeId') ? ['': sec.loggedInUserInfo(field: 'storeNumber')] : ['': 'All']}"
                                              class="form-control select-border"
                                              disabled="${sec.loggedInUserInfo(field: 'storeId') ? true : false}"></g:select>
                                </div>

                                <div class="col-7 text-right">
                                    <button id="reset-filters-btn" type="button" class="btn btn-danger text-right mr-2" onclick="resetForm()">Reset Filters</button>
                                    <button id="filter-submit-button" type="button" class="btn btn-wl text-right" onclick="filterReport()">Search</button>
                                </div>
                            </div>
                        </g:form>
                    </div>
                </div>
            </div>

            <div class="col-5 text-right" style="margin-top: 8px;">
                <button id="export-to-csv" class="btn btn-wl" onclick="exportToCsv();">Export to CSV</button>
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
                                <g:checkBox name="columns" id="columnStoreNumber" class="form-check-input" value="storeNumber" checked="${!userColumns || userColumns?.columns?.find { it.column == 'storeNumber' }?.enabled}" />
                                <label class="form-check-label" for="columnStoreNumber">Store</label>
                            </div>
                            <div class="form-group form-check">
                                <g:checkBox name="columns" id="columnTillId" class="form-check-input" value="tillId" checked="${!userColumns || userColumns?.columns?.find { it.column == 'tillId' }?.enabled}" />
                                <label class="form-check-label" for="columnTillId">Till Number</label>
                            </div>
                            <div class="form-group form-check">
                                <g:checkBox name="columns" id="columnTransactionId" class="form-check-input" value="transactionId" checked="${!userColumns || userColumns?.columns?.find { it.column == 'transactionId' }?.enabled}" />
                                <label class="form-check-label" for="columnTransactionId">Transaction ID</label>
                            </div>
                            <div class="form-group form-check">
                                <g:checkBox name="columns" id="columnBasketTotal" class="form-check-input" value="basketTotal" checked="${!userColumns || userColumns?.columns?.find { it.column == 'basketTotal' }?.enabled}" />
                                <label class="form-check-label" for="columnBasketTotal">Basket Total</label>
                            </div>
                            <div class="form-group form-check">
                                <g:checkBox name="columns" id="columnDonationTotal" class="form-check-input" value="donationTotal" checked="${!userColumns || userColumns?.columns?.find { it.column == 'donationTotal' }?.enabled}" />
                                <label class="form-check-label" for="columnDonationTotal">Donation Amount</label>
                            </div>
                            <div class="form-group form-check">
                                <g:checkBox name="columns" id="columnDateCreated" class="form-check-input" value="dateCreated" checked="${!userColumns || userColumns?.columns?.find { it.column == 'dateCreated' }?.enabled}" />
                                <label class="form-check-label" for="columnDateCreated">Date</label>
                            </div>

                            <button id="columns-submit-button" type="button" class="btn btn-wl" onclick="saveReportColumns();">Apply</button>
                        </g:form>
                    </div>
                </div>
            </div>
        </div>

        <div id="results-container" class="align-content-center">
            <g:render template="charityDonationsResults"/>
        </div>
    </section>
</body>
</html>