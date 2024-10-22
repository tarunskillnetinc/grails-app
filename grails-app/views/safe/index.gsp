<%@ page contentType="text/html;charset=UTF-8" %>
<html>
<head>
    <meta name="layout" content="main"/>

    <title>Safe Management</title>

    <asset:stylesheet src="bootstrap-datepicker3.min.css"/>
    <asset:javascript src="bootstrap-datepicker.min.js"/>
    <asset:javascript src="money-mask.js"/>
    <asset:javascript src="snapshotUrls.js"/>
    <asset:javascript src="safeCount.js"/>
    <asset:javascript src="date-pickers.js"/>
    <asset:javascript src="co-utils.js"/>
    <asset:javascript src="validators/input-validator.js"/>


    <script type="text/javascript">


        function updatePrimarySafe(selectedSafeId) {
            $.ajax({
                url: "${createLink(controller: 'safe', action: 'updatePrimarySafe')}",
                method: "POST",
                data: {selectedSafeId: selectedSafeId},
                success: function (resp) {
                    var successMessage = resp.responseJSON && resp.responseJSON.message ? resp.responseJSON.message : "Successfully update primary safe.";
                    displayMessage('success', successMessage);
                    document.getElementById('alerts-error-container-message').style.display = 'none';
                },
                error: function (resp) {
                    var errorMessage = resp.responseJSON && resp.responseJSON.message ? resp.responseJSON.message : "Failed to update primary safe.";
                    displayMessage('error', errorMessage);
                    document.getElementById('alerts-success-container-message').style.display = 'none';
                }
            });
        }

        function searchSafe(){
            $("#search-results").hide();
            $("#loading-indicator").show();

            let url = "${createLink(controller: 'safe', action: 'searchSafe')}";
            let searchTerm = $('#safeSearchTerm').val();
            let activeSafes = $('#activeOffers').prop("checked");
            let inactiveSafes = $('#inactiveOffers').prop("checked");

            $.ajax({
                url: url,
                data: {
                    searchTerm: searchTerm,
                    activeSafes: activeSafes,
                    inactiveSafes: inactiveOffers,
                    max: sortParams ? sortParams["max"] : null,
                    offset: sortParams ? sortParams.offset : null,
                    sortColumn: sortParams ? sortParams.sortColumn : null,
                    sortOrder: sortParams ? sortParams.sortOrder : null
                },
                success: function(resp) {
                    $('#results-container').html(resp);
                    $('#memberOfferSearchTerm').data('prev',$('#memberOfferSearchTerm').val());
                    $('#memberOfferSearchBy').data('prev', $('#memberOfferSearchBy').val());
                }
            })
        }

        function displayMessage(type, message) {
            const containerId = type === 'success' ? 'alerts-success-container-message' : 'alerts-error-container-message';
            const container = document.getElementById(containerId);
            if (container) {
                container.textContent = message;
                container.style.display = 'block';
            }
        }

    </script>

    <style>
        .primary-safe-label {
            font-weight: 700;
            font-size: 1.2rem;
        }

        .primary-safe-select {
            border: 2px solid #ced4da;
            font-weight: 500;
        }

        .form-group {
            margin-bottom: 0; /* Remove margin between form groups */
        }

        .form-check-input {
            margin-left: 0; /* Adjust checkbox margin */
        }
    </style>

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

    <div id="alerts-success-container-message" class="alert alert-success" role="alert"
         style="${flash.message ? '' : 'display: none;'}">
        ${flash.message ?: ''}
    </div>

    <div id="alerts-error-container-message" class="alert alert-danger" role="alert"
         style="${flash.error ? '' : 'display: none;'}">
        ${flash.error ?: ''}
    </div>


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
                    <!-- Search By Description -->
                    <div class="form-group row align-items-center mb-0">
                        <label class="col-4 col-form-label text-right mb-0">Search By Description</label>
                        <div class="col-6">
                            <g:textField id="safeSearchTerm" name="safeSearchTerm" maxlength="100"
                                         class="form-control" aria-describedby="select-addon2"/>
                        </div>
                    </div>

                    <!-- Active Offers -->
                    <div class="form-group row align-items-center mb-0">
                        <label class="col-4 col-form-label text-right mb-0">Active Safes</label>
                        <div class="col-6 d-flex align-items-center">
                            <g:checkBox id="activeSafes" name="activeSafes" class="form-check-input wl-checkbox"/>
                        </div>
                    </div>

                    <!-- Inactive Offers -->
                    <div class="form-group row align-items-center mb-0">
                        <label class="col-4 col-form-label text-right mb-0">Inactive Safes</label>
                        <div class="col-6 d-flex align-items-center">
                            <g:checkBox id="inactiveSafes" name="inactiveSafes" class="form-check-input wl-checkbox"/>
                        </div>
                    </div>

                    <!-- Buttons for Reset and Search -->
                    <div class="form-group row mb-0">
                        <div class="col-12 text-right">
                            <button id="reset-filters-btn" type="button" class="btn btn-danger mr-2" onclick="resetForm()">Reset Filters</button>
                            <button id="filter-submit-button" type="button" class="btn btn-wl" onclick="searchSafe()">Search</button>
                        </div>
                    </div>
                </div>
            </div>
        </div>

        <div class="col-6">
            <div class="d-flex justify-content-end">
                <g:link elementId="count-safe-button" type="button" class="btn btn-wl text-center mr-2"
                        action="segmentDetails" params="[id: null, edit: false]"
                        style="width: 200px; min-width: 150px;">Add New Safe</g:link>
            </div>
        </div>
    </div>


    <!-- Move the Primary Safe dropdown to the middle -->
    <div class="row justify-content-center mt-5">
        <div class="col-6">
            <div id="primarySafeForm" class="text-center">
                <label for="primarySafe" class="mr-3 primary-safe-label">Primary Safe</label>
                <g:select name="defaultSupplier"
                          from="${safeDescriptions}"
                          optionKey="key"
                          optionValue="value"
                          value="${primaryDescription ? safeDescriptions?.find { it.value == primaryDescription }?.key : ''}"
                          class="form-control d-inline-block primary-safe-select"
                          style="width: auto; min-width: 200px;"
                          onchange="updatePrimarySafe(this.value)"/>
            </div>
        </div>
    </div>

    <div id="results-container" class="align-content-center">
        <g:render template="safeViewerResults" model="[safes: safes]"/>
    </div>

</section>

</body>
</html>