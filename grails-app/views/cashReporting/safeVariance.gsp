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
            $('#startDate').prop("disabled", true);
            $('#endDate').prop("disabled", true);
            $('#get-report-button').prop("disabled", true);

            $('#storeIdSelect').on("change", function () {
                var selectedValue = $(this).val();

                if (selectedValue !== '') {
                    resetSafeSelection();

                    $('#selectedSafes').prop("disabled", false);
                } else {
                    $('#safes').off('click');

                    $('#selectedSafes').text('Select Safes');
                    $('#selectedSafes').prop("disabled", true);

                    $('#startDate').prop("disabled", true);
                    $('#endDate').prop("disabled", true);
                    $('#get-report-button').prop("disabled", true);
                }
            });

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

            <g:if test="${safes != null}">
                initializeMultiSelect();
            </g:if>
        });

        function initializeMultiSelect() {
            function updateSelectedSafes() {
                const selected = $('input[name="safes"]:checked').map(function () {
                    return $(this).val();
                }).get();

                if (selected.length > 0) {
                    $('#startDate').prop("disabled", false);
                    $('#endDate').prop("disabled", false);
                    $('#get-report-button').prop("disabled", false);

                    $('#selectedSafes').text(selected.join(', '));
                } else {
                    $('#startDate').prop("disabled", true);
                    $('#endDate').prop("disabled", true);
                    $('#get-report-button').prop("disabled", true);

                    $('#selectedSafes').text('Select Safes');
                }
            }

            // Update selected safes on page load
            updateSelectedSafes();

            $('#safes').off('click');

            $('#safes').on('click', function (e) {
                e.preventDefault();
                $(this).parent().toggleClass('show');
                $(this).next('.dropdown-menu').toggleClass('show');
            });

            $(document).on('click', function (e) {
                if (!$(e.target).closest('.dropdown').length) {
                    if ($('#safeIdSelect .dropdown-item').length === 0) {
                        return;
                    }
                    $('.dropdown-menu').removeClass('show');
                    $('.dropdown').removeClass('show');
                }
            });

            $(document).on('change', 'input[name="safes"]', function() {
                updateSelectedSafes();
            });
        }

        function updateMultiSelectionOptions(select, url, data) {
            $.ajax({
                url: url, data: data,
                statusCode: {
                    401: function () {
                        window.location.href = '/';
                    },
                    200: function (response) {
                        let safeSelection = document.getElementById("safeIdSelect");
                        let safes = response?.options;

                        if (safes && safes.length > 0) {
                            safes.forEach(safe => {
                                let div = document.createElement('div');
                                div.className = 'dropdown-item';

                                let label = document.createElement('label');
                                label.className = 'mb-0';

                                let input = document.createElement('input');
                                input.type = 'checkbox';
                                input.id = safe.id;
                                input.name = 'safes';
                                input.value = safe.description;

                                label.appendChild(input);
                                label.appendChild(document.createTextNode(' ' + safe.description + (safe.active ? '' : ' - (Inactive Safe)')));

                                div.appendChild(label);
                                safeSelection.appendChild(div);
                            });

                            initializeMultiSelect();
                        }
                    }
                }
            });
        }

        function resetSafeSelection() {
            let getSafesForStoreUrl = "${createLink(controller: 'cashReporting', action: 'ajaxGetSafeListForStore')}";
            let safeSelection = document.getElementById("safeIdSelect");
            safeSelection.innerHTML = '';

            let storeNumber = $("#storeIdSelect").val();

            if (storeNumber) {
                updateMultiSelectionOptions(safeSelection, getSafesForStoreUrl, { storeNumber: storeNumber });
            }
        }

        function getReport() {
            let getReportUrl = "${createLink(controller: 'cashReporting', action: 'ajaxGetSafeSessionVarianceReport')}";
            let storeNumber = $("#storeIdSelect").val();
            let selectedSafes = $("#selectedSafes").text();
            let startDate = $("#startDate").val();
            let endDate = $("#endDate").val();

            renderCashReportResult(getReportUrl, {storeNumber: storeNumber, selectedSafes: selectedSafes, startDate: startDate, endDate: endDate});
        }
</script>
</head>
<body>
    <section id="reporting-container" class="container-fluid">
        <g:render template="titleCrumbs" model="[title: 'Safe Variance Report']" />

        <div class="row mt-4">
            <g:render template="safeVarianceParams" />
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