<%@ page contentType="text/html;charset=UTF-8" %>
<html>
<head>
    <meta name="layout" content="main" />
    <title>Snapshot Management</title>

    <asset:stylesheet src="bootstrap-datepicker3.min.css" />
    <asset:javascript src="bootstrap-datepicker.min.js" />
    <asset:javascript src="money-mask.js" />
    <asset:javascript src="snapshotUrls.js"/>
    <asset:javascript src="snapshotManagement.js"/>
    <asset:javascript src="date-pickers.js"/>

    <script type='text/javascript'>
        $(function() {
            SnapshotUrls.init("${createLink(controller: 'snapshot', action: 'ajaxSelectSafe')}",
                "${createLink(controller: 'snapshot', action: 'ajaxGetSafe')}",
                "${createLink(controller: 'snapshot', action: 'ajaxGetSnapshots')}",
                "${createLink(controller: 'snapshot', action: 'ajaxGetSnapshot')}",
                "${createLink(controller: 'snapshot', action: 'ajaxSaveSafeCount')}",
                "${createLink(controller: 'snapshot', action: 'ajaxSaveSnapshot')}",
                "${createLink(controller: 'snapshot', action: 'ajaxStartCashLift')}",
                "${createLink(controller: 'snapshot', action: 'ajaxSaveCashLift')}");

            initDatePickers(
                'startDate',
                'endDate',
                "${(new Date() - 90).format("dd/MM/yyyy")}",
                "${new Date().format("dd/MM/yyyy")}"
            );
            getSnapshots();
        });
    </script>
</head>

<body>
    <section id="breadcrumb-container" class="container-fluid">
        <nav aria-label="breadcrumb">
            <div class="row mt-4">
                <div class="col">
                    <ol class="breadcrumb">
                        <li id="breadcrumb-1" class="breadcrumb-item"><g:link uri="/">Home</g:link></li>
                        <li id="breadcrumb-2" class="breadcrumb-item"><g:link controller="shift" action="index">Shift Viewer</g:link></li>
                        <li id="breadcrumb-3" class="breadcrumb-item active" aria-current="page">Snapshot Viewer</li>
                    </ol>
                </div>
            </div>
        </nav>
    </section>

    <section id="snapshots-container" class="container-fluid">
        <div class="header-wl mt-3">
            <h2 id="page-title" class="mx-auto">Snapshot Viewer</h2>
        </div>

        <div class="row mt-4">
            <div class="col-5">
                <div class="card bg-light border-wl">
                    <div id="filter" class="card-header pointer" data-toggle="collapse" data-target="#filterCollapse" aria-expanded="false" aria-controls="filterCollapse">
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
                                <label for="startDate" class="col-2 col-form-label text-right">Start Date</label>
                                <div class="col-4">
                                    <g:textField name="startDate" class="form-control bottom-border" value="${startDate.toString("dd/MM/yyyy")}" onkeydown="return false" autocomplete="off" />
                                </div>

                                <label for="endDate" class="col-2 col-form-label text-right">End Date</label>
                                <div class="col-4">
                                    <g:textField name="endDate" class="form-control bottom-border" value="${endDate.toString("dd/MM/yyyy")}" onkeydown="return false" autocomplete="off" />
                                </div>
                            </div>

                            <div class="form-group row">
                                <div class="col-4 offset-6 text-right">
                                    <button id="filter-reset-button" type="button" class="btn btn-danger text-right" onclick="resetSnapshotFilters('${startDate.toString("dd/MM/yyyy")}','${endDate.toString("dd/MM/yyyy")}');">Reset Filters</button>
                                    <button id="filter-submit-button" type="button" class="btn btn-wl text-right" onclick="getSnapshots();">Filter</button>
                                </div>
                            </div>
                        </g:form>
                    </div>
                </div>
            </div>
            <div class="offset-2 col-5">
                <div class="row justify-content-end">
                    <div class="col-4">
                        <button id="cash-lift-button" type="button" class="btn btn-wl text-center w-100" onclick="showCashLiftModal()">Cash Lift</button>
                    </div>
                </div>
            </div>
        </div>

        <div id="results-container" class="align-content-center">
            <g:render template="snapshotViewerResults" />
        </div>
    </section>

    <section id="snapshot-modal" class="container-fluid">
        <!-- Snapshot modal -->
        <div class="modal fade" id="snapshotModal" tabindex="-1" role="dialog" aria-labelledby="snapshotModalLabel" aria-hidden="true">
            <div class="modal-dialog modal-xl" role="document">
                <div class="modal-content">
                    <div class="modal-header">
                        <h2>Snapshot Management</h2>
                    </div>

                    <div id="snapshotModalContent"></div>

                    <div class="modal-footer">
                        <button type="button" id="cancelSnapshotButton" class="btn btn-secondary" data-dismiss="modal">Close</button>
                    </div>
                </div>
            </div>
        </div>
    </section>

    <section id="cashLift-modal" class="container-fluid">
        <div class="modal fade" id="cashLiftModal" tabindex="-1" role="dialog" aria-labelledby="cashLiftModalLabel" aria-hidden="true">
            <div class="modal-dialog modal-xl" role="document">
                <div class="modal-content">
                    <div class="modal-header">
                        <h2>Cash Lift</h2>
                    </div>

                    <div id="cashLiftModalContent"></div>
                </div>
            </div>
        </div>
    </section>
</body>
</html>
