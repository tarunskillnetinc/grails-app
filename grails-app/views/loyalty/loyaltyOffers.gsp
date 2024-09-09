<%@ page contentType="text/html;charset=UTF-8" %>
<html>
<head>
    <meta name="layout" content="main" />

    <title>Loyalty Offers</title>

    <asset:javascript src="jquery-ui.js" />
    <asset:stylesheet src="jquery-ui.css" />

    <script type="application/javascript">

        var getDataUrl = "${createLink(controller: 'loyalty', action: 'ajaxSearchLoyaltyOffers')}";

        var globalSortParams = null;
        var globalFilterParams = null;

        $(document).ready(function () {
            $('#loyaltyOffersTerm').on('keyup', function(event) {
                if (event.key === 'Enter') {
                    search();
                }
            });

            // Create the close icon element
            var closeIcon = $('<span id="cancel-icon" class="close" aria-label="Close">&times;</span>');
            closeIcon.css({ // Apply CSS styles to the close icon
                "position": "absolute",
                "top": "-10px",
                "right": "1px",
                "margin": "0.5rem"
            });
            closeIcon.click(function () { $(this).parent().remove()}); // Remove the error message div when the cancel icon is clicked});
            $('.alert.alert-danger.alert-wl').append(closeIcon); // Append the close icon to the alert container
        });

        window.onload = function() {
            displaySuccessMessage();
        };

        function getQueryParams() {
            var queryParams = {};
            var queryString = window.location.search.substring(1);
            var pairs = queryString.split("&");
            for (var i = 0; i < pairs.length; i++) {
                var pair = pairs[i].split("=");
                var key = decodeURIComponent(pair[0]);
                var value = decodeURIComponent(pair[1]);
                queryParams[key] = value;
            }
            return queryParams;
        }

        // Function to display the success message
        function displaySuccessMessage() {
            var queryParams = getQueryParams();
            if (queryParams.hasOwnProperty('successMessage')) {
                var successMessage = decodeURIComponent(queryParams['successMessage']);
                // Find the success container element
                var successContainer = document.getElementById('success-container');
                // Check if the container exists
                if (successContainer) {
                    // Create a div to hold the success message and close icon
                    var successDiv = document.createElement('div');
                    successDiv.className = 'alert alert-success alert-wl mx-0 position-relative';
                    successDiv.setAttribute('role', 'alert');
                    // Set the success message as the inner HTML of the success container
                    successDiv.innerHTML = '<span id="cancel-icon" class="close" aria-label="Close">&times;</span>' + successMessage;

                    // Append the div to the success container
                    successContainer.appendChild(successDiv);

                    // Get the close icon
                    var closeIcon = successDiv.querySelector('#cancel-icon');
                    // Apply CSS styles to the close icon
                    closeIcon.style.position = "absolute";
                    closeIcon.style.top = "-10px";
                    closeIcon.style.right = "1px";
                    closeIcon.style.margin = "0.5rem";

                    // Add event listener to close the message when the close icon is clicked
                    closeIcon.addEventListener('click', function() {
                        successDiv.style.display = "none";
                    });
                }
            }
        }

        function searchButtonClicked() {
            $('#offset').val(0);
            search();
        }

        function resetForm() {
            document.getElementById('loyaltyOffersTerm').value = null;
            document.getElementById('loyaltyOffersSearchBy').value = 'ID';
        }

        function search() {
            var url = getDataUrl;
            var searchTerm = $('#loyaltyOffersTerm').val();
            var searchBy = $('#loyaltyOffersSearchBy').val();
            $("#loading-indicator").show();

            $('#search-results').html("<div class=\"d-flex justify-content-center\">\n" +
                "  <div class=\"spinner-border\" role=\"status\">\n" +
                "    <span class=\"sr-only\">Loading...</span>\n" +
                "  </div>\n" +
                "</div>");

            $.ajax({
                url: url,
                data: { searchTerm: searchTerm, searchBy: searchBy, max:20, offset:0 },
                statusCode: {
                    500: function (response) {
                        $('#search-results').html("<div class=\"d-flex justify-content-center\"><span class=\"text-muted\">No results found.</span></div>");
                        errorMessageDisplay(response)
                    },
                    200: function (response) {
                        $('#results-container').html(response);
                        $('#loyaltyOffersTerm').data('prev',$('#loyaltyOffersTerm').val())
                        $('#loyaltyOffersSearchBy').data('prev', $('#loyaltyOffersSearchBy').val())
                    }
                }
            });
        }

        function errorMessageDisplay(response){
            var errorList = response.responseJSON.error;
            if (errorList && errorList.length > 0) {
                var errorDiv = $('<div class="alert alert-danger alert-wl mx-0" role="alert"></div>');

                errorList.forEach(function(errorMessage) {
                    var errorMessageSpan = $('<span>' + errorMessage + '</span>');
                    errorDiv.append(errorMessageSpan);
                    errorDiv.append($('<br>'));
                });

                var closeIcon = $('<span id="cancel-icon" class="close" aria-label="Close">&times;</span>');

                closeIcon.click(function () {
                    errorDiv.remove(); // Remove the error message div when the cancel icon is clicked
                });

                errorDiv.append(closeIcon);
                $('#errors-container').html(errorDiv);

                // Adjust icon position to top-right corner
                closeIcon.css({
                    "position": "absolute",
                    "top": "-10px",
                    "right": "1px",
                    "margin": "0.5rem"
                });
            }
        }

        function cancelLoyaltyError(){
            $('#loyaltyOffersModal').modal('hide');
            $('#search-results').html("<div class=\"d-flex justify-content-center\"><span class=\"text-muted\">No results found.</span></div>");
        }

        function reOrderData(sortParams, filterParams) {

            var searchTerm = $('#loyaltyOffersTerm').val();
            var searchBy = $('#loyaltyOffersSearchBy').val();

            $("#search-results").hide();
            $("#loading-indicator").show();

            // Storing sort params globally because some parts of the page don't have access to these variables due to the async nature.
            if (sortParams != null) {
                globalSortParams = sortParams;
            }

            if (filterParams != null) {
                globalFilterParams = filterParams;
            }

            var params = { searchTerm: searchTerm, searchBy: searchBy };
            $.extend(params, globalSortParams, globalFilterParams);

            $.ajax({
                url: getDataUrl,
                method: "POST",
                data: params,
                success: function(resp) {
                    $("#results-container").html(resp);
                }
            });
        }
    </script>

    <style>

        #errors-container {
            margin-top: 20px; /* Adjust the value as needed */
            margin-bottom: 20px; /* Adjust the value as needed */
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
                    <li id="breadcrumb-2" class="breadcrumb-item active" aria-current="page">Loyalty Offer Management</li>
                </ol>
            </div>
        </div>
    </nav>
</section>

<section id="reasonCodeMaintenance" class="container-fluid">

    <section id="success-container"></section>

    <section id="errors-container" class="container-fluid mb-20"></section>


    <g:if test="${flash.error}">
        <section id="failure-container">
            <div class="alert alert-danger alert-wl mx-0" role="alert">${flash.error}</div>
        </section>
    </g:if>

    <section id="filters-section" class="container-fluid">

        <div class="row header-wl mt-3">
            <div class="col-6 offset-3">
                <h2 id="page-title" class="mx-auto my-auto">Loyalty Offers</h2>
            </div>

            <div class="col-3 text-right d-inline-flex flex-row justify-content-end">
                <g:link elementId="add-new-product-btn" controller="loyalty" action="showLoyaltyOffer" class="btn btn-wl p-2">Add New Offer</g:link>
            </div>
        </div>

        <div class="row mt-4">
            <div class="col-8">
                <div class="card bg-light border-wl">
                    <div id="filters-collapse" class="card-header pointer" data-toggle="collapse" data-target="#filterCollapse" aria-expanded="true" aria-controls="filterCollapse">
                        <div class="row">
                            <div id="filters-header" class="col-10">Filters</div>
                            <div class="col-2 text-right">
                                <svg width="1em" height="1em" viewBox="0 0 16 16" class="bi bi-caret-down-fill text-right" fill="currentColor" xmlns="http://www.w3.org/2000/svg">
                                    <path d="M7.247 11.14L2.451 5.658C1.885 5.013 2.345 4 3.204 4h9.592a1 1 0 0 1 .753 1.659l-4.796 5.48a1 1 0 0 1-1.506 0z"/>
                                </svg>
                            </div>
                        </div>
                    </div>
                    <div class="card-body collapse show" id="filterCollapse">
                        <div class="form-group row">
                            <label for="loyaltyOffersTerm" class="col-2 col-form-label-sm text-right">Search Term</label>
                            <div class="col-10 input-group">
                                <g:textField id="loyaltyOffersTerm" name="loyaltyOffersTerm" maxlength="100" value="${session.PRODUCT_SEARCH_TERM}" class="form-control" aria-describedby="select-addon2" />
                                <div class="input-group-append">
                                    <g:select id="loyaltyOffersSearchBy" name="loyaltyOffersSearchBy" from="${['ID', 'Description']}" value="everything" valueMessagePrefix="loyaltyOffersSearchBy" class="form-control select-border" style="z-index: 0;" />
                                </div>
                            </div>
                        </div>
                        <div class="form-group row">
                            <div class="col-sm-8 col-xl-6 offset-sm-4 offset-xl-6 text-right">
                                <button id="reset-filters-btn" type="button" class="btn btn-danger text-right mr-2" onclick="resetForm()">Reset Filters</button>
                                <button id="filter-submit-button" type="button" class="btn btn-wl text-right" onclick="searchButtonClicked()">Search</button>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </section>

    <section id="loyaltyOffers-modal" class="container-fluid" >
        <div class="modal fade" id="loyaltyOffersModal" tabindex="-1" role="dialog" aria-labelledby="loyaltyOffersModalLabel" data-backdrop="false" aria-hidden="true" style="margin-top: 120px">
            <div class="modal-dialog modal-lg" style="border: 2px black solid ; margin-top: 120px" role="document" >
                <div id="loyaltyOffersContent" class="modal-content" ></div>
            </div>
        </div>
    </section>

    <div id="results-container" class="align-content-center">
        <g:render template="loyaltyOffersSearchResults" />
    </div>

</section>

</body>
</html>