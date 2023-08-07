<!doctype html>
<html>
<head>
    <meta name="layout" content="main" />

    <title>Trust Retail</title>

    <script type='text/javascript'>
        var getQueuesUrl = "${createLink(controller: 'monitoring', action: 'ajaxGetQueues')}";
        var purgeQueueUrl = "${createLink(controller: 'monitoring', action: 'ajaxPurgeQueue')}";
        var deleteQueueUrl = "${createLink(controller: 'monitoring', action: 'ajaxDeleteQueue')}";
        var forceSyncUrl = "${createLink(controller: 'monitoring', action: 'ajaxForceSync')}";

        var intervalMillis = 10000;
        var intervalId = setInterval(getQueues, intervalMillis);

        $(function() {
            getQueues(false);
        });

        function getQueues(silent) {
            if (silent === false) {
                $("#search-results").hide();
                $("#loading-indicator").show();
            }

            // Retrieve our filters.
            var filterParams = { };

            $("#filtersForm input").each(function() {
                filterParams[$(this).attr("name")] = $(this).val();
            }).get();

            $("#filtersForm select").each(function() {
                filterParams[$(this).attr("name")] = $(this).find(":selected").val();
            }).get();

            $.ajax({
                url: getQueuesUrl,
                method: "GET",
                data: filterParams,
                success: function (data, textStatus, resp) {
                    $("#results-container").html(data);
                    $("#errors-container").html('');
                },
                error: function (resp) {
                    $("#results-container").html("");
                    $("#errors-container").html('<div class="alert alert-danger alert-wl mx-0" role="alert">' + resp.responseText + '</div>');
                }
            });
        }

        function purgeQueue(storeId, tillId) {
            if (confirm("This will clear all messages from the queue for store " +storeId +", till " +tillId +".")) {
                $.ajax({
                    url: purgeQueueUrl,
                    method: "DELETE",
                    data: { storeId: storeId, tillId: tillId },
                    success: function (data, textStatus, resp) {
                        $("#errors-container").html('<div class="alert alert-success alert-wl mx-0" role="alert">' + resp.responseText + '</div>');
                        resetInterval();
                    },
                    error: function (resp) {
                        $("#errors-container").html('<div class="alert alert-danger alert-wl mx-0" role="alert">' + resp.responseText + '</div>');
                        resetInterval();
                    }
                });
            }
        }

        function deleteQueue(storeId, tillId) {
            if (confirm("This will fully delete the queue for store " +storeId +", till " +tillId +".")) {
                $.ajax({
                    url: deleteQueueUrl,
                    method: "DELETE",
                    data: { storeId: storeId, tillId: tillId },
                    success: function (data, textStatus, resp) {
                        $("#errors-container").html('<div class="alert alert-success alert-wl mx-0" role="alert">' + resp.responseText + '</div>');
                        resetInterval();
                    },
                    error: function (resp) {
                        $("#errors-container").html('<div class="alert alert-danger alert-wl mx-0" role="alert">' + resp.responseText + '</div>');
                        resetInterval();
                    }
                });
            }
        }

        function forceSync(storeId, tillId) {
            if (confirm("This will force the queue for store " + storeId + ", till " + tillId + " to synchronise.")) {
                $.ajax({
                    url: forceSyncUrl,
                    method: "POST",
                    data: { storeId: storeId, tillId: tillId },
                    success: function (data, textStatus, resp) {
                        $("#errors-container").html('<div class="alert alert-success alert-wl mx-0" role="alert">' + resp.responseText + '</div>');
                        resetInterval();
                    },
                    error: function (resp) {
                        $("#errors-container").html('<div class="alert alert-danger alert-wl mx-0" role="alert">' + resp.responseText + '</div>');
                        resetInterval();
                    }
                });
            }
        }

        function clearFilters() {
            $("#storeIdFilter").val("");
            $("#tillIdFilter").val("");
            $("#statusFilter").val("");
            $("#errors-container").html('');
            getQueues(false);
        }

        function resetInterval() {
            clearInterval(intervalId);
            intervalId = setInterval(function() {
                getQueues(true);
            }, intervalMillis);
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
                        <li id="breadcrumb-3" class="breadcrumb-item active" aria-current="page">Till Connectivity</li>
                    </ol>
                </div>
            </div>
        </nav>
    </section>

    <section id="heading-container" class="container-fluid">
        <div class="header-wl mt-3">
            <h2 id="page-title" class="mx-auto">Till Connectivity</h2>
        </div>

        <div class="row mt-4 ml-0 mr-0">
            <div class="col-8 offset-2 text-center">
                <p id="page-message-1">This page displays the current status of the RabbitMQ till messaging system.</p>
                <p id="page-message-2">The Clear button will remove all messages currently on the queue for the affected till.
                    <br />The Sync button will send a message to the till to force it to synchronise with the central database.
                    <br />Any action taken here can be destructive and should only be performed with notice to the affected store.</p>
            </div>
        </div>

        <section id="errors-container" class="container-fluid">
            <g:if test="${flash.error}">
                <div class="alert alert-danger alert-wl mx-0" role="alert">${flash.error}</div>
            </g:if>
            <g:if test="${flash.message}">
                <div class="alert alert-success alert-wl mx-0" role="alert">${flash.message}</div>
            </g:if>
        </section>
    </section>

    <section id="filters-section" class="container-fluid">
        <div class="row mt-3">
            <div class="col-8 offset-2">
                <div id="filters" class="card bg-light border-wl">
                    <div class="card-header pointer" data-toggle="collapse" data-target="#filterCollapse" aria-expanded="false" aria-controls="collapseExample">
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
                                <label for="storeIdFilter" class="col-2 col-form-label-sm text-right">Store ID</label>
                                <div class="col-4">
                                    <g:textField name="storeIdFilter" class="form-control bottom-border" value="${storeId}" autocomplete="off" />
                                </div>

                                <label for="tillIdFilter" class="col-2 col-form-label-sm text-right">Till ID</label>
                                <div class="col-4">
                                    <g:textField name="tillIdFilter" class="form-control bottom-border" value="${tillId}" autocomplete="off" />
                                </div>
                            </div>

                            <div class="form-group row">
                                <label for="statusFilter" class="col-2 col-form-label-sm text-right">Status</label>
                                <div class="col-4">
                                    <g:select name="statusFilter" from="${['Online', 'Offline']}" noSelection="['':'All']" value="${statusFilter}" class="form-control select-border" />
                                </div>

                                <div class="col-6 text-right">
                                    <button id="filter-clear-button" type="button" class="btn btn-danger text-right" onclick="clearFilters();">Clear</button>
                                    <button id="filter-submit-button" type="button" class="btn btn-wl text-right" onclick="getQueues();">Filter</button>
                                </div>
                            </div>
                        </g:form>
                    </div>
                </div>
            </div>
            <div class="col-2 text-right">
                <a id="refresh" href="#" class="btn btn-wl mt-1" onclick="getQueues(true);">Refresh</a>
            </div>
        </div>
    </section>

    <section id="queues-container" class="container-fluid mb-3">
        <div class="row mt-5 pb-2 ml-0 mr-0 table-wl bottom-border">
            <div class="col-1 font-weight-bold text-center">Store ID</div>
            <div class="col-1 font-weight-bold text-center">Till ID</div>
            <div class="col-2 font-weight-bold text-center">Messages Waiting</div>
            <div class="col-2 font-weight-bold text-center">Latest Queue Activity</div>
            <div class="col-1 font-weight-bold text-center">Status</div>
            <div class="col-3 font-weight-bold">&nbsp;</div>
        </div>

        <div id="results-container">

        </div>
    </section>
</body>
</html>