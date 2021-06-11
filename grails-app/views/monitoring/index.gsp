<!doctype html>
<html>
<head>
    <meta name="layout" content="main" />

    <title>WonderLane</title>

    <script type='text/javascript'>
        var getQueuesUrl = "${createLink(controller: 'monitoring', action: 'ajaxGetQueues')}";

        $(function() {
            getQueues();
        });

        function getQueues() {
            $("#search-results").hide();
            $("#loading-indicator").show();

            $.ajax({
                url: getQueuesUrl,
                method: "GET",
                success: function(resp) {
                    $("#results-container").html(resp);
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
                        <li class="breadcrumb-item active" aria-current="page">Monitoring</li>
                        <li class="breadcrumb-item active" aria-current="page">Till Connectivity</li>
                    </ol>
                </div>
            </div>
        </nav>
    </section>

    <section id="heading-container" class="container-fluid">
        <div class="header-wl mt-3">
            <h2 class="mx-auto">Till Connectivity</h2>
        </div>
    </section>

    <g:if test="${flash.error}">
        <section id="errors-container" class="container-fluid">
            <div class="alert alert-danger alert-wl mx-0" role="alert">${flash.error}</div>
        </section>
    </g:if>

    <g:if test="${flash.message}">
        <section id="errors-container2" class="container-fluid">
            <div class="alert alert-success alert-wl mx-0" role="alert">${flash.message}</div>
        </section>
    </g:if>


    <section id="queues-container" class="container-fluid">
        <div class="row mt-4 ml-0 mr-0">
            <div class="col-2 offset-10 px-0 text-right">
                <a href="#" class="btn btn-wl" onclick="getQueues();">Refresh</a>
            </div>
        </div>

        <div class="row mt-5 pb-2 ml-0 mr-0 table-wl bottom-border">
            <div class="col-2 offset-1 font-weight-bold">Store Number</div>
            <div class="col-2 font-weight-bold">Till Number</div>
            <div class="col-2 font-weight-bold">Messages Waiting</div>
            <div class="col-2 font-weight-bold">Latest Queue Activity</div>
            <div class="col-2 font-weight-bold">Status</div>
        </div>

        <div id="results-container">

        </div>
    </section>
</body>
</html>