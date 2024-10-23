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

    <script type="text/javascript">
        $(document).ready(function () {
            //Loading safes and primary dropdown results
            searchSafe(null, false);
        });

        function submitSaveSafe() {
            if (confirm('Confirm changes. Are you sure you wish to save these changes?')) {
                var activeCheckbox = document.getElementById('active');
                var hiddenActiveInput = document.getElementById('hiddenActive');

                // Copy the value of the checkbox to the hidden input
                hiddenActiveInput.name = 'active'; // Change the name to 'active' right before submission
                hiddenActiveInput.value = activeCheckbox.checked ? 'true' : 'false';

                // Remove the checkbox name temporarily if it is not disabled to avoid duplicate submissions
                if (!activeCheckbox.disabled) {
                    activeCheckbox.removeAttribute('name');
                }
                $('#safeDetails').submit();
            }
        }

        function updatePrimarySafe(selectedSafeId) {
            var searchTerm = $('#safeSearchTerm').val();
            var activeSafes = $('#activeSafes').prop("checked");
            var inactiveSafes = $('#inactiveSafes').prop("checked");
            $.ajax({
                url: "${createLink(controller: 'safe', action: 'updatePrimarySafe')}",
                method: "POST",
                data: {
                    searchTerm: searchTerm,
                    activeSafes: activeSafes,
                    inactiveSafes: inactiveSafes,
                    selectedSafeId: selectedSafeId
                },
                success: function (resp) {
                    $('#results-container').html(resp);
                },
                error: function (resp) {
                    $('#results-container').html(resp);
                }
            });
        }

        function searchSafe(sortParams, isPrimaryDropDownOnly) {
            $("#search-results").hide();
            $("#loading-indicator").show();

            var inactiveSafes = $('#inactiveSafes').prop("checked");
            var isDropdownOnly = isPrimaryDropDownOnly.toString().toLowerCase() === "true";
            $.ajax({
                url: "${createLink(controller: 'safe', action: 'searchSafe')}",
                method: "POST",
                data: {
                    inactiveSafes: inactiveSafes,
                    isDropdownOnly: isDropdownOnly
                },
                success: function (resp) {
                    $('#results-container').html(resp);
                    $('#safeSearchTerm').data('prev', $('#memberOfferSearchTerm').val());
                },
                error: function (resp) {
                    var errorMessage = resp.responseJSON && resp.responseJSON.message ? resp.responseJSON.message : "Safe search failed.";
                    displayMessage('error', errorMessage);
                    document.getElementById('alerts-success-container-message').style.display = 'none';
                }
            })
        }

        function displayMessage(type, message) {
            const containers = {
                success: document.getElementById('alerts-success-container-message'),
                error: document.getElementById('alerts-error-container-message')
            };

            for (const [key, container] of Object.entries(containers)) {
                if (container) {
                    if (key === type) {
                        container.textContent = message;
                        container.style.display = 'block';
                    } else {
                        container.style.display = 'none';
                    }
                }
            }
        }

    </script>

%{--    <style>--}%
%{--        .primary-safe-label {--}%
%{--            font-weight: 700;--}%
%{--            font-size: 1.2rem;--}%
%{--        }--}%

%{--        .primary-safe-select {--}%
%{--            border: 2px solid #ced4da;--}%
%{--            font-weight: 500;--}%
%{--        }--}%

%{--        .form-group {--}%
%{--            margin-bottom: 0; /* Remove margin between form groups */--}%
%{--        }--}%

%{--        .form-check-input {--}%
%{--            margin-left: 0; /* Adjust checkbox margin */--}%
%{--        }--}%

%{--        .form-check {--}%
%{--            display: flex;--}%
%{--            align-items: center;--}%
%{--        }--}%

%{--        .form-check-input.wl-checkbox {--}%
%{--            margin-right: 10px;--}%
%{--        }--}%
%{--    </style>--}%

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

    <div id="alerts-success-container-message" class="alert alert-success" role="alert" style="${flash.message ? '' : 'display: none;'}">
        ${flash.message ?: ''}
    </div>

    <div id="alerts-error-container-message" class="alert alert-danger" role="alert" style="${flash.error ? '' : 'display: none;'}">
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
                    <div class="d-flex justify-content-between align-items-center">
                        <!-- Inactive Offers -->
                        <div class="d-flex align-items-center">
                            <label for="inactiveSafes" class="mb-0 mr-2">Show Inactive Safes</label>
                            <input type="checkbox" id="inactiveSafes" name="inactiveSafes" style="transform: scale(1.3); margin-left: 5px;">
                        </div>

                        <!-- Buttons for Reset and Search -->
                        <div class="d-flex">
                            <button id="reset-filters-btn" type="button" class="btn btn-danger mr-2" onclick="resetForm()">Reset Filters</button>
                            <button id="filter-submit-button" type="button" class="btn btn-wl" onclick="searchSafe(null, false)">Search</button>
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

    <div id="results-container" class="align-content-center">
        <g:render template="safeViewerResults" model="[safes: safes]"/>
    </div>

</section>

</body>
</html>