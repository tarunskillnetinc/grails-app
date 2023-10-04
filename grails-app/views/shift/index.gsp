<%@ page contentType="text/html;charset=UTF-8" %>
<html>
    <head>
        <meta name="layout" content="main" />

        <title>Shift Management</title>

        <asset:stylesheet src="bootstrap-datepicker3.min.css" />
        <asset:javascript src="bootstrap-datepicker.min.js" />
        <asset:javascript src="money-mask.js" />
        <asset:javascript src="shiftUrls.js"/>
        <asset:javascript src="snapshotUrls.js"/>
        <asset:javascript src="shiftManagement.js"/>
        <asset:javascript src="safeCount.js"/>
        <asset:javascript src="date-pickers.js"/>
        <asset:javascript src="validators/input-validator.js" />

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

                initDatePickers(
                    'startDate',
                    'endDate',
                    "${(new Date() - 90).format("dd/MM/yyyy")}",
                    "${new Date().format("dd/MM/yyyy")}"
                );
                getShifts();

                function updateSnapshotLink() {
                    let startDate = document.getElementById('startDate').value;
                    let endDate = document.getElementById('endDate').value;
                    let tillId = document.getElementById('tillId').value;
                    let snapShotLink = document.getElementById('snapShotLink');
                    let url = "/snapshot/index?shiftStartDate=" + encodeURIComponent(startDate) + "&shiftEndDate=" +
                        encodeURIComponent(endDate) + "&shiftTillId=" + tillId;
                    snapShotLink.href = url;
                }

                $('#startDate').on('change', updateSnapshotLink);
                $('#endDate').on('change', updateSnapshotLink);
                document.getElementById('tillId').addEventListener('change', updateSnapshotLink);

            });

            $(document).ready(function () {
                intListener("tillId");
            });

            function intListener(elementId) {
                var element = document.getElementById(elementId)
                var maxLength = 10
                var maxValue = 2147483647

                if (element != null) {
                    element.addEventListener("input", function () {
                        if (element.value.length > maxLength) {
                            element.value = element.value.slice(0, maxLength)
                        }
                        if (element.value > maxValue) {
                            element.value = maxValue
                        }
                    });
                }
            }

            function resetShiftFilters() {
                setDatePickers(
                    'startDate',
                    'endDate',
                    "${(new Date() - 7).format("dd/MM/yyyy")}",
                    "${new Date().format("dd/MM/yyyy")}"
                );
                $("#tillId").val("");
                getShifts();
            }
        </script>
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
                                        <g:textField name="startDate" id="startDate" class="form-control bottom-border" value="${startDate}" onkeydown="return false" autocomplete="off" />
                                    </div>

                                    <label for="endDate" class="col-2 col-form-label text-right">End Date</label>
                                    <div class="col-4">
                                        <g:textField name="endDate" id="endDate" class="form-control bottom-border" value="${endDate}" onkeydown="return false" autocomplete="off" />
                                    </div>
                                </div>

                                <div class="form-group row">
                                    <label for="tillId" class="col-2 col-form-label text-right">Till Number</label>
                                    <div class="col-2">
                                        <g:field id="tillId" type="number" min="0" max = "2147483647" name="tillId" step="1" class="form-control bottom-border" autocomplete="off" value="${tillId}" onkeydown="acceptNumeric(event);"/>
                                    </div>

                                    <div class="col-4 offset-4 text-right">
                                        <button id="filter-reset-button" type="button" class="btn btn-danger text-right" onclick="resetShiftFilters()">
                                            Reset Filters
                                        </button>
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
                            <g:link elementId="snapShotLink" controller="snapshot" action="index"  class="w-100">
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