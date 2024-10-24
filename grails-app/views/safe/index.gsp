<%@ page contentType="text/html;charset=UTF-8" %>
<html>
<head>
    <meta name="layout" content="main"/>

    <title>Safe Management</title>

    <asset:stylesheet src="bootstrap-datepicker3.min.css"/>
    <asset:javascript src="bootstrap-datepicker.min.js"/>
    <asset:javascript src="money-mask.js"/>
    <asset:javascript src="date-pickers.js"/>
    <asset:javascript src="co-utils.js"/>
    <asset:javascript src="validators/input-validator.js"/>
    <asset:javascript src="safeUrls.js"/>
    <asset:javascript src="safeManagement.js"/>

    <script type="text/javascript">

        $(document).ready(function () {
            $("#messages-container").html('');

            SafeUrls.init("${createLink(controller: 'safe', action: 'searchSafe')}",
                "${createLink(controller: 'safe', action: 'updatePrimarySafe')}"
            );

            if (${showInactiveSafes}) {
                $('#inactiveSafes').prop('checked', true);
            }

            //Loading safes and primary dropdown results
            searchSafe(null, false);
        });
    </script>

</head>

<body>
<section id="breadcrumb-container" class="container-fluid">
    <nav aria-label="breadcrumb">
        <div class="row mt-4">
            <div class="col">
                <ol class="breadcrumb">
                    <li id="breadcrumb-1" class="breadcrumb-item"><g:link uri="/">Home</g:link></li>
                    <li id="breadcrumb-2" class="breadcrumb-item active" aria-current="page">Safe Management</li>
                </ol>
            </div>
        </div>
    </nav>
</section>

<section id="safe-container" class="container-fluid">
    <div class="header-wl mt-3">
        <h2 id="page-title" class="mx-auto">Safe Management</h2>
    </div>

    <div id="messages-container"></div>

    <g:if test="${flash.message}">
        <div id="alerts-success-container-message" class="alert alert-success" role="alert">${flash.message}</div>
    </g:if>

    <g:if test="${flash.error}">
        <div id="alerts-success-container-message" class="alert alert-danger" role="alert">${flash.error}</div>
    </g:if>

    <div class="row mt-4">
        <div class="col-6">
            <div class="card bg-light border-wl">
                <div id="filter" class="card-header pointer" data-toggle="collapse" data-target="#filterCollapse"
                     aria-expanded="false" aria-controls="filterCollapse">
                    <div class="row">
                        <div class="col-10">Filters</div>

                        <div class="col-2 text-right">
                            <svg width="1em" height="1em" viewBox="0 0 16 16" class="bi bi-caret-down-fill text-right"
                                 fill="currentColor" xmlns="http://www.w3.org/2000/svg">
                                <path d="M7.247 11.14L2.451 5.658C1.885 5.013 2.345 4 3.204 4h9.592a1 1 0 0 1 .753 1.659l-4.796 5.48a1 1 0 0 1-1.506 0z"/>
                            </svg>
                        </div>
                    </div>
                </div>

                <div class="card-body collapse" id="filterCollapse">
                    <div class="d-flex justify-content-between align-items-center">
                        <!-- Inactive Offers -->
                        <div class="d-flex align-items-center">
                            <label for="inactiveSafes" class="mb-0 mr-2">Show Inactive Safes</label>
                            <input type="checkbox" id="inactiveSafes" name="inactiveSafes"
                                   style="transform: scale(1.3); margin-left: 5px;">
                        </div>

                        <!-- Buttons for Reset and Search -->
                        <div class="d-flex">
                            <button id="reset-filters-btn" type="button" class="btn btn-danger mr-2"
                                    onclick="resetSafeFilters()">Reset Filters</button>
                            <button id="filter-submit-button" type="button" class="btn btn-wl"
                                    onclick="searchSafe(null, false)">Search</button>
                        </div>
                    </div>
                </div>

            </div>
        </div>

        <div class="col-6">
            <div class="d-flex justify-content-end">
                <g:link elementId="count-safe-button" type="button" class="btn btn-wl p-2"
                        action="addSafe" params="[id: null, edit: false]"
                        onclick="return addInactiveSafesParam(this)">Add New Safe</g:link>
            </div>
        </div>
    </div>

    <div id="results-container" class="align-content-center">
        <g:render template="safeViewerResults" model="[safes: safes]"/>
    </div>

</section>

</body>
</html>