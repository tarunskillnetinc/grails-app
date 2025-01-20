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
        var getDataUrl = "${createLink(controller: 'reporting', action: 'ajaxBankingReport')}";

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
                startDate: "${(new Date() - 366).format("dd/MM/yyyy")}",
                endDate: "${new Date().format("dd/MM/yyyy")}",
                todayHighlight: true,
                autoclose: true,
                todayBtn: "linked",
                orientation: "bottom auto"
            }).on('changeDate', function(e) {
                let startDate = e.date;
                let endDate = new Date(startDate);
                let currentDate = new Date();
                currentDate.setHours(0,0,0,0);

                $('#endDate').datepicker('setStartDate', startDate);
                $('#endDate').datepicker('setEndDate', currentDate);
                $('#endDate').datepicker('setDate', currentDate);
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

            $('#startDate').datepicker('setStartDate', "${(new Date() - 366).format('dd/MM/yyyy')}");
            $('#startDate').datepicker('setEndDate', "${new Date().format('dd/MM/yyyy')}");

            $('#endDate').datepicker('setStartDate', "${new Date().format('dd/MM/yyyy')}");
            $('#endDate').datepicker('setEndDate', "${new Date().format('dd/MM/yyyy')}");

            document.getElementById('storeFilter').value = '';
            document.getElementById('bankingTypeId').value = '';
        }
    </script>
</head>

<body>
    <section id="reporting-container" class="container-fluid">
        <g:reportBreadcrumb reportType="${reportType}"/>

        <div class="header-wl mt-3">
            <h2 id="page-title" class="mx-auto">Banking Report</h2>
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
                                <label for="storeFilter" class="col-2 col-form-label-sm text-right">Store ID</label>
                                <div class="col-3">
                                    <g:select name="storeFilter" from="${stores}" optionValue="${{it.config.storeNumber}}"
                                              optionKey="id"
                                              noSelection="${sec.loggedInUserInfo(field: 'storeId') ? ['': sec.loggedInUserInfo(field: 'storeNumber')] : ['': 'All']}"
                                              class="form-control select-border"
                                              disabled="${sec.loggedInUserInfo(field: 'storeId') ? true : false}"></g:select>
                                </div>

                                <label for="storeFilter2" class="col-3 col-form-label-sm text-right">Banking Type</label>
                                <div class="col-3">
                                    <g:select id="bankingTypeId" name="bankingType" from="${bankingType}" noSelection="${['': 'All']}" value="everything" class="form-control select-border" style="z-index: 0;" />
                                </div>
                            </div>
                            <div class="form-group row">
                                <div class="col-12 text-right">
                                    <button id="reset-filters-btn" type="button" class="btn btn-danger text-right mr-2" onclick="resetForm()">Reset Filters</button>
                                    <button id="filter-submit-button" type="button" class="btn btn-wl text-right" onclick="filterReport()">Search</button>
                                </div>
                            </div>
                        </g:form>
                    </div>
                </div>
            </div>
        </div>

        <div id="results-container" class="align-content-center">
            <g:render template="bankingReportResults"/>
        </div>
    </section>
</body>
</html>