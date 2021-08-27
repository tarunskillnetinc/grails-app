<!doctype html>
<html>
<head>
    <meta name="layout" content="main" />

    <title>WonderLane</title>

    <script type='text/javascript'>
        var getQueuesUrl = "${createLink(controller: 'monitoring', action: 'ajaxGetQueues')}";
        var purgeQueueUrl = "${createLink(controller: 'monitoring', action: 'ajaxPurgeQueue')}";
        var deleteQueueUrl = "${createLink(controller: 'monitoring', action: 'ajaxDeleteQueue')}";

        $(function() {
            getQueues();
        });

        function getQueues() {
            $("#search-results").hide();
            $("#loading-indicator").show();

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
                success: function(resp) {
                    $("#results-container").html(resp);
                }
            });
        }

        function purgeQueue(storeId, tillId) {
            if (confirm("This will clear all messages from the queue for store " +storeId +", till " +tillId +".")) {
                $.ajax({
                    url: purgeQueueUrl,
                    method: "DELETE",
                    data: { storeId: storeId, tillId: tillId },
                    success: function(resp) {
                        getQueues();
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
                    success: function(resp) {
                        getQueues();
                    }
                });
            }
        }

        function clearFilters() {
            $("#storeIdFilter").val("");
            $("#tillIdFilter").val("");
            $("#statusFilter").val("");

            getQueues();
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

    <section id="heading-container-2" class="container-fluid">
        <div class="row mt-4 ml-0 mr-0">
            <div class="col-8 offset-2 text-center">
                <p>This page displays the current status of the RabbitMQ till messaging system.</p>
            </div>

            <div class="col-2 px-0 text-right">
                <a href="#" class="btn btn-wl" onclick="getQueues();">Refresh</a>
            </div>
        </div>
    </section>

    <section id="filters-section" class="container-fluid">
        <div class="row mt-3">
            <div class="col-8 offset-2">
                <div class="card bg-light border-wl">
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
        </div>
    </section>

    <section id="queues-container" class="container-fluid">
        <div class="row mt-5 pb-2 ml-0 mr-0 table-wl bottom-border">
            <div class="col-2 font-weight-bold">Store ID</div>
            <div class="col-2 font-weight-bold">Till ID</div>
            <div class="col-2 font-weight-bold">Messages Waiting</div>
            <div class="col-2 font-weight-bold">Latest Queue Activity</div>
            <div class="col-2 font-weight-bold">Status</div>
            <div class="col-2 font-weight-bold">&nbsp;</div>
        </div>

        <div id="results-container">

        </div>
    </section>
</body>
</html>