<!doctype html>
<html>
<head>
    <meta name="layout" content="main" />

    <title>Trust Retail</title>

    <asset:stylesheet src="bootstrap-datepicker3.min.css" />
    <asset:javascript src="bootstrap-datepicker.min.js" />

    <script type='text/javascript'>
        var getDataUrl = "${createLink(controller: 'monitoring', action: 'ajaxGetTransactionServiceStatus')}";

        $(document).ready(function () {
            getServiceStatus();
        });

        function getServiceStatus() {
            $("#serviceStatus").hide();
            $("#loadingIndicator").show();

            $.ajax({
                url: getDataUrl,
                method: "GET",
                success: function(resp) {
                    $("#loadingIndicator").hide();
                    $("#serviceStatus").html(resp);
                    $("#serviceStatus").show();
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
                        <li id="breadcrumb-1" class="breadcrumb-item"><g:link uri="/">Home</g:link></li>
                        <li id="breadcrumb-2" class="breadcrumb-item active" aria-current="page">Monitoring</li>
                        <li id="breadcrumb-3" class="breadcrumb-item active" aria-current="page">Transaction Service Status</li>
                    </ol>
                </div>
            </div>
        </nav>
    </section>

    <section id="reporting-container" class="container-fluid">
        <div class="header-wl mt-3">
            <h2 id="page-title" class="mx-auto">Transaction Service Status</h2>
        </div>

        <div class="row mt-4">
            <p id="page-message" class="mx-auto">This page displays the status of all services which process transactions.</p>
        </div>

        <div class="col-2 offset-10 text-right">
            <a id="refresh" href="#" class="btn btn-wl mt-1" onclick="getServiceStatus();">Refresh</a>
        </div>

        <div class="d-flex justify-content-center">
            <div id="loadingIndicator" class="spinner-border mt-2" role="status">
                <span class="sr-only">Loading...</span>
            </div>
        </div>

        <div id="serviceStatus" class="mb-5">

        </div>
    </section>
</body>
</html>