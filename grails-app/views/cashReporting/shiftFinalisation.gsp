<!doctype html>
<html>
<head>
    <meta name="layout" content="main" />

    <title>Trust Retail</title>

    <asset:stylesheet src="bootstrap-datepicker3.min.css" />
    <asset:javascript src="bootstrap-datepicker.min.js" />
    <asset:javascript src="reporting.js" />

    <script type='text/javascript'>
        var getDataUrl = "${createLink(controller: 'reporting', action: 'ajaxSalesDepartment')}";

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
        <section id="breadcrumb-container" class="container-fluid">
            <nav aria-label="breadcrumb">
                <div class="row mt-4">
                    <div class="col">
                        <ol class="breadcrumb">
                            <li id="breadcrumb-1" class="breadcrumb-item"><g:link uri="/">Home</g:link></li>
                            <li id="breadcrumb-2" class="breadcrumb-item active" aria-current="page">Shift Finalisation Report</li>
                        </ol>
                    </div>
                </div>
            </nav>
        </section>

        <div class="header-wl mt-3">
            <h2 id="page-title" class="mx-auto">Shift Finalisation Report</h2>
        </div>

        <div class="row mt-4">
            <g:render template="shiftFinalisationParams" />
        </div>

        <div id="result-container" class="align-content-center">
            <g:render template="shiftFinalisationReport" />
        </div>

        <div class="col mt-4 mb-4 text-right" >
            <g:link elementId="cancel-btn" class="btn btn-wl text-center" uri="/" role="button" >Cancel</g:link>
        </div>
    </section>

</body>
</html>