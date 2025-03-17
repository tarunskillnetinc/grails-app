<!doctype html>
<html>
<head>
    <meta name="layout" content="main" />

    <title>Trust Retail - Safe Activity Report</title>

    <asset:stylesheet src="bootstrap-datepicker3.min.css"/>
    <asset:javascript src="bootstrap-datepicker.min.js"/>
    <asset:javascript src="cashReporting.js"/>

    <script type='text/javascript'>
        $(document).ready(function () {
            $('#storeIdSelect').on("change", function () {
                resetSafeSelection();
                resetSafeSessionSelection();
            });

            $('#safeIdSelect').on("change", function () {
                resetSafeSessionSelection();
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
                    resetSafeSessionSelection();
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
                    resetSafeSessionSelection();
                }
            });

            $('#safeSessionSelect').on("change", function () {
                shiftNumber = $("#safeSessionSelect").val();
                if (shiftNumber) {
                    document.getElementById("get-report-button").removeAttribute("disabled");
                } else {
                    document.getElementById("get-report-button").disabled = true;
                }
            });
        });

        var getSafesForStoreUrl = "${createLink(controller: 'cashReporting', action: 'ajaxGetSafesForStore')}";
        function resetSafeSelection() {
            let safeSelection = document.getElementById("safeIdSelect");
            removeAllOptions(safeSelection);

            storeNumber = $("#storeIdSelect").val();
            if (!storeNumber) {
                safeSelection.appendChild(new Option('-', ''));
            } else {
                updateSelectionOptions(safeSelection, getSafesForStoreUrl, { storeNumber: storeNumber });
            }
        }

        var getSafeSessionsForSafeUrl = "${createLink(controller: 'cashReporting', action: 'ajaxGetSafeSessionsForSafe')}";
        function resetSafeSessionSelection() {
            $("#error-container").html('');
            document.getElementById("get-report-button").disabled = true;

            storeNumber = $("#storeIdSelect").val();
            safeId = $("#safeIdSelect").val();
            startDate = $('#startDate').val();
            endDate = $('#endDate').val();

            let safeSessionSelection = document.getElementById("safeSessionSelect");
            removeAllOptions(safeSessionSelection);
            if (!safeId) {
                safeSessionSelection.appendChild(new Option('-', ''));
            } else {
                updateSelectionOptions(safeSessionSelection, getSafeSessionsForSafeUrl, {storeNumber: storeNumber, safeId: safeId, startDate: startDate, endDate: endDate});
            }
        }

        var getReportUrl = "${createLink(controller: 'cashReporting', action: 'ajaxGetSafeActivityReport')}";
        function getReport() {
            storeNumber = $("#storeIdSelect").val();
            safeId = $("#safeIdSelect").val();
            sessionNumber = $("#safeSessionSelect").val();

            renderCashReportResult(getReportUrl, {storeNumber: storeNumber, safeId: safeId, sessionNumber: sessionNumber});
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
        <g:render template="titleCrumbs" model="[title: 'Safe Activity Report']" />

        <div class="row mt-4">
            <g:render template="safeFinalisationParams" />
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