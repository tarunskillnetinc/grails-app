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
        var getDataUrl = "${createLink(controller: 'reporting', action: 'ajaxTenderMovement')}";
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
    <g:reportBreadcrumb reportType="${reportType}" tenderMovementType="${tenderMovementType}" startDate="${startDate}" endDate="${endDate}"/>

    <div class="header-wl mt-3">
        <h2 class="mx-auto">Tender Movements Report</h2>
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
                        <g:hiddenField name="tenderMovementType" value="${tenderMovementType}" />
                        <div class="form-group row">
                            <label for="startDate" class="col-2 col-form-label-sm text-right">Start Date</label>
                            <div class="col-4">
                                <g:textField name="startDate" class="form-control bottom-border" value="${startDate.format("dd/MM/yyyy")}" autocomplete="off" />
                            </div>

                            <label for="endDate" class="col-2 col-form-label-sm text-right">End Date</label>
                            <div class="col-4">
                                <g:textField name="endDate" class="form-control bottom-border" value="${endDate.format("dd/MM/yyyy")}" autocomplete="off" />
                            </div>
                        </div>

                        <div class="form-group row">
                            <div class="col-12 text-right">
                                <button id="filter-submit-button" type="button" class="btn btn-wl text-right" onclick="filterReport();">Filter</button>
                            </div>
                        </div>
                    </g:form>
                </div>
            </div>
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
                            <g:checkBox name="columns" id="columnsTimestamp" class="form-check-input" value="timestamp" checked="${!userColumns || userColumns?.columns?.find { it.column == 'timestamp' }?.enabled}" />
                            <label class="form-check-label" for="columnsTimestamp">Timestamp</label>
                        </div>
                        <div class="form-group form-check">
                            <g:checkBox name="columns" id="columnsFromLocationType" class="form-check-input" value="fromLocationType" checked="${!userColumns || userColumns?.columns?.find { it.column == 'fromLocationType' }?.enabled}" />
                            <label class="form-check-label" for="columnsFromLocationType">From Location Type</label>
                        </div>
                        <div class="form-group form-check">
                            <g:checkBox name="columns" id="columnsFromLocation" class="form-check-input" value="fromLocation" checked="${!userColumns || userColumns?.columns?.find { it.column == 'fromLocation' }?.enabled}" />
                            <label class="form-check-label" for="columnsFromLocation">From Location</label>
                        </div>
                        <div class="form-group form-check">
                            <g:checkBox name="columns" id="columnsToLocationType" class="form-check-input" value="toLocationType" checked="${!userColumns || userColumns?.columns?.find { it.column == 'toLocationType' }?.enabled}" />
                            <label class="form-check-label" for="columnsToLocationType">To Location Type</label>
                        </div>
                        <div class="form-group form-check">
                            <g:checkBox name="columns" id="columnsToLocation" class="form-check-input" value="toLocation" checked="${!userColumns || userColumns?.columns?.find { it.column == 'toLocation' }?.enabled}" />
                            <label class="form-check-label" for="columnsToLocation">To Location</label>
                        </div>
                        <div class="form-group form-check">
                            <g:checkBox name="columns" id="columnsAmount" class="form-check-input" value="amount" checked="${!userColumns || userColumns?.columns?.find { it.column == 'amount' }?.enabled}" />
                            <label class="form-check-label" for="columnsAmount">Amount</label>
                        </div>
                        <div class="form-group form-check">
                            <g:checkBox name="columns" id="columnsType" class="form-check-input" value="type" checked="${!userColumns || userColumns?.columns?.find { it.column == 'type' }?.enabled}" />
                            <label class="form-check-label" for="columnsType">Type</label>
                        </div>
                        <div class="form-group form-check">
                            <g:checkBox name="columns" id="columnsType" class="form-check-input" value="reason" checked="${!userColumns || userColumns?.columns?.find { it.column == 'reason' }?.enabled}" />
                            <label class="form-check-label" for="columnsType">Reason</label>
                        </div>
                        <div class="form-group form-check">
                            <g:checkBox name="columns" id="columnsType" class="form-check-input" value="reasonOther" checked="${!userColumns || userColumns?.columns?.find { it.column == 'reasonOther' }?.enabled}" />
                            <label class="form-check-label" for="columnsType">Reason Other</label>
                        </div>
                        <button type="button" class="btn btn-wl" onclick="saveReportColumns();">Apply</button>
                    </g:form>
                </div>
            </div>
        </div>
    </div>

    <div id="results-container" class="align-content-center">
        <g:render template="tenderMovementResults" />
    </div>
</section>
</body>
</html>