<%@ page contentType="text/html;charset=UTF-8" %>
<html>
    <head>
        <meta name="layout" content="main" />

        <title>WonderLane Shift Management</title>

        <asset:stylesheet src="bootstrap-datepicker3.min.css" />
        <asset:javascript src="bootstrap-datepicker.min.js" />
        <asset:javascript src="money-mask.js" />
        <asset:javascript src="shiftUrls.js"/>
        <asset:javascript src="snapshotUrls.js"/>

        <script type="text/javascript">
            $(function() {
                ShiftUrls.init("${createLink(controller: 'shift', action: 'ajaxGetShifts')}",
                    "${createLink(controller: 'shift', action: 'ajaxGetCashDetails')}",
                    "${createLink(controller: 'shift', action: 'ajaxChangeCashUpType')}",
                    "${createLink(controller: 'shift', action: 'ajaxSaveCash')}",
                    "${createLink(controller: 'shift', action: 'ajaxSaveShift')}");

                SnapshotUrls.init("${createLink(controller: 'snapshot', action: 'ajaxGetSafe')}",
                    "${createLink(controller: 'snapshot', action: 'ajaxGetSnapshot')}",
                    "${createLink(controller: 'snapshot', action: 'ajaxGetSnapshot')}",
                    "${createLink(controller: 'snapshot', action: 'ajaxSaveSafeCount')}",
                    "${createLink(controller: 'snapshot', action: 'ajaxSaveSnapshot')}");

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

                getShifts();
            });
        </script>

        <asset:javascript src="shiftManagement.js"/>
        <asset:javascript src="safeCount.js"/>
    </head>

    <body>
        <section id="breadcrumb-container" class="container-fluid">
            <nav aria-label="breadcrumb">
                <div class="row mt-4">
                    <div class="col">
                        <ol class="breadcrumb">
                            <li id="breadcrumb-1" class="breadcrumb-item"><g:link uri="/">Home</g:link></li>
                            <li id="breadcrumb-2" class="breadcrumb-item active" aria-current="page">Shift Viewer</li>
                        </ol>
                    </div>
                </div>
            </nav>
        </section>

        <section id="shifts-container" class="container-fluid">
            <div class="header-wl mt-3">
                <h2 id="page-title" class="mx-auto">Shift Viewer</h2>
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
                                        <g:textField name="startDate" class="form-control bottom-border" value="${startDate.toString("dd/MM/yyyy")}" autocomplete="off" />
                                    </div>

                                    <label for="endDate" class="col-2 col-form-label text-right">End Date</label>
                                    <div class="col-4">
                                        <g:textField name="endDate" class="form-control bottom-border" value="${endDate.toString("dd/MM/yyyy")}" autocomplete="off" />
                                    </div>
                                </div>

                                <div class="form-group row">
                                    <label for="tillId" class="col-2 col-form-label text-right">Till Number</label>
                                    <div class="col-2">
                                        <g:field type="number" name="tillId" step="1" class="form-control bottom-border" autocomplete="off" />
                                    </div>

                                    <div class="col-4 offset-4 text-right">
                                        <button id="filter-submit-button" type="button" class="btn btn-wl text-right" onclick="getShifts();">Filter</button>
                                    </div>
                                </div>
                            </g:form>
                        </div>
                    </div>
                </div>
                <div class="offset-2 col-5">
                    <div class="row">
                        <div class="offset-4 col-4">
                            <button id="count-safe-button" type="button" class="btn btn-wl text-center w-100" onclick="showSafeModal()">Count Safe</button>
                        </div>
                        <div class="col-4">
                            <g:link controller="snapshot" action="index"  class="w-100">
                                <button id="snapshot-viewer-button" type="button" class="btn btn-wl text-center w-100">Snapshot Viewer</button>
                            </g:link>
                        </div>
                    </div>
                </div>
            </div>

            <div id="results-container" class="align-content-center">
                <g:render template="shiftViewerResults" />
            </div>
        </section>

        <section id="shift-modal" class="container-fluid">
            <!-- Cash modal -->
            <div class="modal fade" id="cashModal" tabindex="-1" role="dialog" aria-labelledby="cashModalLabel" aria-hidden="true">
                <div class="modal-dialog modal-xl" role="document">
                    <div class="modal-content">
                        <div id="cashModalHeader" class="modal-header">
                            <h2>Cash Management</h2>
                        </div>

                        <div id="cashModalContent"></div>

                        <div class="modal-footer">
                            <button type="button" id="cancelShiftButton" class="btn btn-secondary" data-dismiss="modal">Cancel</button>
                            <button type="button" id="saveShiftButton" class="btn btn-success">Save</button>
                        </div>
                    </div>
                </div>
            </div>
        </section>
    </body>
</html>