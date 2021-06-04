<%@ page contentType="text/html;charset=UTF-8" %>
<html>
    <head>
        <meta name="layout" content="main" />

        <title>WonderLane Shift Management</title>

        <asset:stylesheet src="bootstrap-datepicker3.min.css" />
        <asset:javascript src="bootstrap-datepicker.min.js" />
        <asset:javascript src="money-mask.js" />

        <script type='text/javascript'>
            var getShiftsUrl = "${createLink(controller: 'shift', action: 'ajaxGetShifts')}";
            var getCashDetailsUrl = "${createLink(controller: 'shift', action: 'ajaxGetCashDetails')}";
            var changeCashUpTypeUrl = "${createLink(controller: 'shift', action: 'ajaxChangeCashUpType')}";
            var saveCashUrl = "${createLink(controller: 'shift', action: 'ajaxSaveCash')}";
            var saveShiftUrl = "${createLink(controller: 'shift', action: 'ajaxSaveShift')}";

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

                getShifts();
            });

            function getShifts() {
                $("#search-results").hide();
                $("#loading-indicator").show();

                var startDate = $("#startDate").val();
                var endDate = $("#endDate").val();
                var tillId = $("#tillId").val();

                $.ajax({
                    url: getShiftsUrl,
                    method: "POST",
                    data: { startDate: startDate, endDate: endDate, tillId: tillId },
                    success: function(resp) {
                        $("#results-container").html(resp);
                    }
                });
            }

            function showCashModal(shiftId, isReconciled) {
                $("#cashModalContent").html("<div class=\"modal-body\"><div class=\"d-flex justify-content-center\"><div id=\"loadingIndicator\" class=\"spinner-border\" role=\"status\"><span class=\"sr-only\">Loading...</span></div></div></div>");
                $('#cashModal').modal({ show: true });

                if (isReconciled) {
                    $("#saveShiftButton").prop("onclick", null).off("click");
                    $("#saveShiftButton").hide();

                    $("#cancelShiftButton").prop("onclick", null).off("click");
                    $("#cancelShiftButton").text("Close");
                } else {
                    $("#saveShiftButton").prop("onclick", null).off("click");
                    $("#saveShiftButton").click(function () {
                        submitCash(shiftId);
                    });

                    $("#saveShiftButton").show();
                    $("#cancelShiftButton").text("Cancel");
                }

                $.ajax({
                    url: getCashDetailsUrl,
                    method: "POST",
                    data: { shiftId: shiftId },
                    success: function(resp) {
                        $("#cashModalContent").html(resp);

                        $(".mask-money").maskMoney({ allowZero: true });
                        $(".mask-money").maskMoney('mask');
                    }
                });
            }

            function changeCashUpType(type) {
                var cashUpByValueLink = $("#cashUpByValueLink");
                var cashUpByDenominationLink = $("#cashUpByDenominationLink");
                var cashUpByTotalsLink = $("#cashUpByTotalsLink");

                cashUpByValueLink.removeClass("disabled");
                cashUpByDenominationLink.removeClass("disabled");
                cashUpByTotalsLink.removeClass("disabled");

                if (type === "VALUE") {
                    cashUpByValueLink.addClass("disabled");
                    cashUpByValueLink.prop("onclick", null).off("click");

                    cashUpByDenominationLink.click(function() {
                        changeCashUpType("DENOMINATION");
                    });

                    cashUpByTotalsLink.click(function() {
                        changeCashUpType("TOTALS");
                    });
                } else if (type === "DENOMINATION") {
                    cashUpByValueLink.click(function() {
                        changeCashUpType("VALUE");
                    });

                    cashUpByDenominationLink.addClass("disabled");
                    cashUpByDenominationLink.prop("onclick", null).off("click");

                    cashUpByTotalsLink.click(function() {
                        changeCashUpType("TOTALS");
                    });
                } else if (type === "TOTALS") {
                    cashUpByValueLink.click(function() {
                        changeCashUpType("VALUE");
                    });

                    cashUpByDenominationLink.click(function() {
                        changeCashUpType("DENOMINATION");
                    });

                    cashUpByTotalsLink.addClass("disabled");
                    cashUpByTotalsLink.prop("onclick", null).off("click");
                }

                var formValues = $("#cashUpForm").serialize();
                formValues = formValues + "&type=" +type;

                $.ajax({
                    url: changeCashUpTypeUrl,
                    method: "POST",
                    data: formValues,
                    success: function(resp) {
                        $("#cashUpContainer").html(resp);

                        $(".mask-money").maskMoney({ allowZero: true });
                        $(".mask-money").maskMoney('mask');
                    }
                });
            }

            function submitCash(shiftId) {
                var cashUpBy = $("#cashUpBy").val();

                if (cashUpBy === "VALUE" && !isFormValid()) {
                    return;
                }

                var formValues = $("#cashUpForm").serialize();
                formValues = formValues + "&shiftId=" +shiftId

                $.ajax({
                    url: saveCashUrl,
                    method: "POST",
                    data: formValues,
                    success: function(resp) {
                        $("#cashModalContent").html(resp);

                        $("#saveShiftButton").prop("onclick", null).off("click");
                        $("#saveShiftButton").click(function() {
                            submitShift(shiftId);
                        });
                    }
                });
            }

            function isFormValid() {
                var isFormValid = true;

                var fiftyPounds = $("#fiftyPounds");
                var twentyPounds = $("#twentyPounds");
                var tenPounds = $("#tenPounds");
                var fivePounds = $("#fivePounds");
                var twoPounds = $("#twoPounds");
                var onePounds = $("#onePounds");
                var fiftyPences = $("#fiftyPences");
                var twentyPences = $("#twentyPences");
                var tenPences = $("#tenPences");
                var fivePences = $("#fivePences");
                var twoPences = $("#twoPences");
                var onePences = $("#onePences");

                fiftyPounds.removeClass("is-invalid");
                twentyPounds.removeClass("is-invalid");
                tenPounds.removeClass("is-invalid");
                fivePounds.removeClass("is-invalid");
                twoPounds.removeClass("is-invalid");
                onePounds.removeClass("is-invalid");
                fiftyPences.removeClass("is-invalid");
                twentyPences.removeClass("is-invalid");
                tenPences.removeClass("is-invalid");
                fivePences.removeClass("is-invalid");
                twoPences.removeClass("is-invalid");
                onePences.removeClass("is-invalid");

                if (fiftyPounds.val() % 50 > 0) {
                    fiftyPounds.addClass("is-invalid");
                    isFormValid = false;
                }
                if (twentyPounds.val() % 20 > 0) {
                    twentyPounds.addClass("is-invalid");
                    isFormValid = false;
                }
                if (tenPounds.val() % 10 > 0) {
                    tenPounds.addClass("is-invalid");
                    isFormValid = false;
                }
                if (fivePounds.val() % 5 > 0) {
                    fivePounds.addClass("is-invalid");
                    isFormValid = false;
                }
                if (twoPounds.val() % 2 > 0) {
                    twoPounds.addClass("is-invalid");
                    isFormValid = false;
                }
                if (onePounds.val() % 1 > 0) {
                    onePounds.addClass("is-invalid");
                    isFormValid = false;
                }
                if (Math.round(fiftyPences.val() * 100) % 50 > 0) {
                    fiftyPences.addClass("is-invalid");
                    isFormValid = false;
                }

                if (Math.round(twentyPences.val() * 100) % 20 > 0) {
                    twentyPences.addClass("is-invalid");
                    isFormValid = false;
                }
                if (Math.round(tenPences.val() * 100) % 10 > 0) {
                    tenPences.addClass("is-invalid");
                    isFormValid = false;
                }
                if (Math.round(fivePences.val() * 100) % 5 > 0) {
                    fivePences.addClass("is-invalid");
                    isFormValid = false;
                }
                if (Math.round(twoPences.val() * 100) % 2 > 0) {
                    twoPences.addClass("is-invalid");
                    isFormValid = false;
                }
                if (Math.round(onePences.val() * 100) % 1 > 0) {
                    onePences.addClass("is-invalid");
                    isFormValid = false;
                }

                return isFormValid;
            }

            function submitShift() {
                var formValues = $("#shiftVarianceForm").serialize();

                $.ajax({
                    url: saveShiftUrl,
                    method: "POST",
                    data: formValues,
                    success: function(resp) {
                        $("#cashModalContent").html(resp);

                        $("#saveShiftButton").prop("onclick", null).off("click");
                        $("#saveShiftButton").hide();

                        $("#cancelShiftButton").text("Close");
                        $("#cancelShiftButton").prop("onclick", null).off("click");
                        $("#cancelShiftButton").click(function() {
                            getShifts();
                        });
                    }
                });
            }
        </script>
    </head>

    <body>
        <section id="breadcrumb-container" class="container-fluid">
            <nav aria-label="breadcrumb">
                <div class="row mt-4">
                    <div class="col">
                        <ol class="breadcrumb">
                            <li class="breadcrumb-item"><g:link uri="/">Home</g:link></li>
                            <li class="breadcrumb-item active" aria-current="page">Shift Viewer</li>
                        </ol>
                    </div>
                </div>
            </nav>
        </section>

        <section id="shifts-container" class="container-fluid">
            <div class="header-wl mt-3">
                <h2 class="mx-auto">Shift Viewer</h2>
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
                        <div class="modal-header">
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