<!doctype html>
<html>
<head>
    <meta name="layout" content="main" />

    <title>Trust Retail</title>

    <asset:stylesheet src="bootstrap-datepicker3.min.css"/>
    <asset:javascript src="bootstrap-datepicker.min.js"/>
    <asset:javascript src="cashReporting.js"/>

    <script type='text/javascript'>
        $(document).ready(function () {
            $('#storeIdSelect').on("change", function () {
                resetTillSelection();
                resetShiftSelection();
            });

            $('#tillIdSelect').on("change", function () {
                resetShiftSelection();
            });

            $('#startDate').on("change", function () {
                $('#startDate').val(this.value);
                $('#startDate').removeClass('is-invalid');
                $('#endDate').datepicker('setStartDate', this.value);

                var startDate = $('#startDate').datepicker('getDate');
                var endDate = $('#endDate').datepicker('getDate');
                var limit = addDays(startDate, 28)
                if (endDate > limit) {
                    $('#endDate').datepicker('setDate', limit);
                } else {
                    resetShiftSelection();
                }
            });

            $('#endDate').on("change", function () {
                $('#endDate').val(this.value);
                $('#endDate').removeClass('is-invalid');
                $('#startDate').datepicker('setEndDate', this.value);

                var startDate = $('#startDate').datepicker('getDate');
                var endDate = $('#endDate').datepicker('getDate');
                var limit = addDays(endDate, -28)
                if (startDate < limit) {
                    $('#startDate').datepicker('setDate', limit);
                } else {
                    resetShiftSelection();
                }
            });

            $('#shiftNumberSelect').on("change", function () {
                shiftNumber = $("#shiftNumberSelect").val();
                if (shiftNumber) {
                    document.getElementById("get-report-button").removeAttribute("disabled");
                } else {
                    document.getElementById("get-report-button").disabled = true;
                }
            });
        });

        var getTillsForStoreUrl = "${createLink(controller: 'cashReporting', action: 'ajaxGetTillsForStore')}";
        function resetTillSelection() {
            let tillSelection = document.getElementById("tillIdSelect");
            removeAllOptions(tillSelection);

            storeNumber = $("#storeIdSelect").val();
            if (!storeNumber) {
                tillSelection.appendChild(new Option('-', ''));
            } else {
                updateSelectionOptions(tillSelection, getTillsForStoreUrl,
                    { storeNumber: storeNumber }
                );
            }
        }

        var getShiftNumbersForTillUrl = "${createLink(controller: 'cashReporting', action: 'ajaxGetAllShiftsForTill')}";
        function resetShiftSelection() {
            $("#error-container").html('');
            document.getElementById("get-report-button").disabled = true;

            storeNumber = $("#storeIdSelect").val();
            tillId = $("#tillIdSelect").val();
            startDate = $('#startDate').val();
            endDate = $('#endDate').val();

            let shiftSelection = document.getElementById("shiftNumberSelect");
            removeAllOptions(shiftSelection);
            if (!tillId) {
                shiftSelection.appendChild(new Option('-', ''));
            } else {
                updateSelectionOptions(shiftSelection, getShiftNumbersForTillUrl,
                    {storeNumber: storeNumber, tillId: tillId, startDate: startDate, endDate: endDate}
                );
            }
        }

        var getReportUrl = "${createLink(controller: 'cashReporting', action: 'ajaxGetTillActivityReport')}";
        function getReport() {
            storeNumber = $("#storeIdSelect").val();
            tillId = $("#tillIdSelect").val();
            shiftNumber = $("#shiftNumberSelect").val();
            renderCashReportResult(getReportUrl, {storeNumber: storeNumber, tillId: tillId, shiftNumber: shiftNumber});
        }

        $(function() {
            $('#startDate').datepicker({
                format: "dd/mm/yyyy",
                weekStart: 1,
                startDate: "${(new Date() - 366).format("dd/MM/yyyy")}",
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

    </script>
</head>
<body>
    <section id="reporting-container" class="container-fluid">
        <g:render template="titleCrumbs" model="[title: 'Till Activity Report']" />

        <div class="row mt-4">
            <g:render template="tillActivityParams" />
        </div>

        <div id="error-container"></div>
        <div id="report-time"></div>
        <div id="report-container" class="align-content-center"></div>

        <div class="col mt-4 mb-4 text-right" >
            <g:link elementId="cancel-btn" class="btn btn-wl text-center" uri="/" role="button" >Cancel</g:link>
        </div>
    </section>
</body>
</html>