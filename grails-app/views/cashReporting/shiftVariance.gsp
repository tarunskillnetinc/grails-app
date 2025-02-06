<!doctype html>
<html>
<head>
    <meta name="layout" content="main" />

    <title>Trust Retail - Shift Variance Report</title>

    <asset:stylesheet src="bootstrap-datepicker3.min.css"/>
    <asset:javascript src="bootstrap-datepicker.min.js"/>
    <asset:javascript src="cashReporting.js"/>

    <script type='text/javascript'>
        $(document).ready(function () {
            $('#startDate').prop("disabled", true);
            $('#endDate').prop("disabled", true);
            $('#shift-variance-report').prop("disabled", true);

            $('#storeIdSelect').on("change", function () {
                var selectedValue = $(this).val();

                if (selectedValue !== '') {
                    resetTillSelection();

                    $('#selectedTills').prop("disabled", false);
                } else {
                    $('#tills').off('click');

                    $('#selectedTills').text('Select Tills');
                    $('#selectedTills').prop("disabled", true);

                    $('#startDate').prop("disabled", true);
                    $('#endDate').prop("disabled", true);
                    $('#shift-variance-report').prop("disabled", true);
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

            <g:if test="${tills != null}">
                initializeMultiSelect();
            </g:if>
        });

        function initializeMultiSelect() {
            function updateSelectedTills() {
                const selected = $('input[name="tills"]:checked').map(function () {
                    return $(this).val();
                }).get();

                if (selected.length > 0) {
                    $('#startDate').prop("disabled", false);
                    $('#endDate').prop("disabled", false);
                    $('#shift-variance-report').prop("disabled", false);

                    $('#selectedTills').text(selected.join(', '));
                } else {
                    $('#startDate').prop("disabled", true);
                    $('#endDate').prop("disabled", true);
                    $('#shift-variance-report').prop("disabled", true);

                    $('#selectedTills').text('Select Tills');
                }
            }

            // Update selected safes on page load
            updateSelectedTills();

            $('#tills').off('click');

            $('#tills').on('click', function (e) {
                e.preventDefault();
                $(this).parent().toggleClass('show');
                $(this).next('.dropdown-menu').toggleClass('show');
            });

            $(document).on('click', function (e) {
                if (!$(e.target).closest('.dropdown').length) {
                    if ($('#tillIdSelect .dropdown-item').length === 0) {
                        return;
                    }
                    $('.dropdown-menu').removeClass('show');
                    $('.dropdown').removeClass('show');
                }
            });

            $(document).on('change', 'input[name="tills"]', function() {
                updateSelectedTills();
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
                        let tillSelection = document.getElementById("tillIdSelect");
                        let tills = response?.options;

                        if (tills && tills.length > 0) {
                            tills.forEach(till => {
                                let div = document.createElement('div');
                                div.className = 'dropdown-item';

                                let label = document.createElement('label');
                                label.className = 'mb-0';

                                let input = document.createElement('input');
                                input.type = 'checkbox';
                                input.id = till.id;
                                input.name = 'tills';
                                input.value = till.description;

                                label.appendChild(input);
                                label.appendChild(document.createTextNode(' ' + till.description));

                                div.appendChild(label);
                                tillSelection.appendChild(div);
                            });

                            initializeMultiSelect();
                        }
                    }
                }
            });
        }
        
        function resetTillSelection() {
            let getTillsForStoreUrl = "${createLink(controller: 'cashReporting', action: 'ajaxGetTillsForStoreNumber')}";
            let tillSelection = document.getElementById("tillIdSelect");
            tillSelection.innerHTML = '';

            let storeNumber = $("#storeIdSelect").val();

            if (storeNumber) {
                updateMultiSelectionOptions(tillSelection, getTillsForStoreUrl, { storeNumber: storeNumber });
            }
        }

        function getReport() {
            let getReportUrl = "${createLink(controller: 'cashReporting', action: 'ajaxGetShiftVarianceReport')}";
            let storeNumber = $("#storeIdSelect").val();
            let selectedTills = $("#selectedTills").text();
            let startDate = $("#startDate").val();
            let endDate = $("#endDate").val();

            renderCashReportResult(getReportUrl, {storeNumber: storeNumber, selectedTills: selectedTills, startDate: startDate, endDate: endDate});
        }
    </script>
</head>

<body>
    <section id="reporting-container" class="container-fluid">
        <g:render template="titleCrumbs" model="[title: 'Shift Variance Report']" />

        <div class="row mt-4">
            <g:render template="shiftVarianceParams" />
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