<%@ page contentType="text/html;charset=UTF-8" %>
<html>
    <head>
        <meta name="layout" content="main" />
        <title>Add Member Offer</title>

        <asset:stylesheet src="bootstrap-datepicker3.min.css" />
        <asset:javascript src="bootstrap-datepicker.min.js" />

        <script type='text/javascript'>
            $(document).ready(function() {
                let selectedId;
                let selectedDescription;

                // Function to handle dropdown item click
                function handleDropdownItemClick() {
                    // Get the text of the clicked dropdown item
                    let newText = $(this).text();
                    let dataIndex = $(this).data('index');

                    // Set the input box value to the clicked dropdown item's text
                    $('#searchInput').val(newText);

                    selectedId = dataIndex;
                    selectedDescription = newText;

                    $('#dropdownMenu').removeClass('show');

                    selectOffer();
                }

                // Event listener for dropdown item click
                $(document).on('click', '.dropdown-item', handleDropdownItemClick);

                setTimeout(function() {
                    $('#searchInput').trigger('input');
                }, 100);

                $('#searchInput').on('input', function() {
                    let url = "${createLink(controller: 'loyalty', action: 'ajaxSearchForAvailableOffers')}";

                    let cardNumber = $('#cardNumber').val();
                    let searchTerm = $(this).val();

                    $.ajax({
                        url: url,
                        method: 'GET',
                        data: { cardNumber: cardNumber, searchTerm: searchTerm },
                        success: function(data) {

                            // Update the dropdown menu with search results
                            let dropdownMenu = $('#dropdownMenu');
                            dropdownMenu.empty();

                            if (data.length > 0) {
                                $.each(data, function(index, option) {
                                    dropdownMenu.append($('<a class="dropdown-item" href="#" data-index="' + option.id + '">').text(option.offerDescription));
                                });

                                // Show the dropdown menu
                                dropdownMenu.addClass('show');
                            } else {
                                dropdownMenu.removeClass('show');
                            }
                        }
                    });
                });

                // Hide the dropdown menu when user clicks outside of it
                $(document).on('click', function(e) {
                    if (!$(e.target).closest('.input-group').length) {
                        $('.dropdown-menu').removeClass('show');
                    }
                });

                function selectOffer() {
                    $("#search-results").hide();
                    $("#loading-indicator").show();

                    let offer = { id: selectedId, description: selectedDescription };
                    let cardNumber = $('#cardNumber').val();

                    let url = "${createLink(controller: 'loyalty', action: 'ajaxSelectedOffer')}";

                    $.ajax({
                        url: url,
                        data: { id: offer.id, cardNumber: cardNumber },
                        success: function(resp) {
                            $('#results-container').html(resp);
                        }
                    })
                }
            });

            function saveOffer() {
                let error = false;
                let errorString = "";

                if ($('#remainingRedemptions').val() == "")  {
                    errorString = errorString.concat("<li>Please enter a value for the remaining redemptions</li>");
                    error = true;
                }

                if (!error) {
                    $('#saveMemberOffer').submit();
                } else {
                    let errorHeader = "All mandatory fields must be present before data can be saved."
                    $('#validation-errors').html("<ul>" + errorHeader + errorString + "\n</ul>");
                    $('#validation-errors').prop("hidden", false);
                }
            }
        </script>
    </head>
    <body>
        <g:hiddenField name="cardNumber" id="cardNumber" value="${cardNumber}" />

        <section id="breadcrumb-container" class="container-fluid">
            <nav aria-label="breadcrumb">
                <div class="row mt-4">
                    <div class="col">
                        <ol class="breadcrumb">
                            <li id="breadcrumb-1" class="breadcrumb-item"><g:link uri="/">Home</g:link></li>
                            <li id="breadcrumb-2" class="breadcrumb-item active" aria-current="page"><g:link action="loyaltyMembers">Membership Management</g:link></li>
                            <li id="breadcrumb-3" class="breadcrumb-item active" aria-current="page"><g:link action="showMemberDetails" params="[cardNumber: cardNumber]">${cardNumber}</g:link></li>
                            <li id="breadcrumb-4" class="breadcrumb-item active" aria-current="page"><g:link action="offers" params="[cardNumber: cardNumber]">Member Offers</g:link></li>
                            <li id="breadcrumb-5" class="breadcrumb-item active" aria-current="page">Add Member Offer</li>
                        </ol>
                    </div>
                </div>
            </nav>
        </section>

    <g:if test="${flash.message}">
        <section id="alerts-container" class="container-fluid">
            <div id="alerts-container-message" class="alert alert-success alert-wl mx-0" role="alert">${flash.message}</div>
        </section>
    </g:if>

        <section id="add-member-offer" class="container-fluid">
            <div class="row header-wl mt-3">
                <div class="col-8 offset-2">
                    <h2 id="page-title" class="mx-auto my-auto">Add Member Offer</h2>
                </div>
            </div>

            <div class="row mt-4">
                <div class="col-6">
                    <div class="card bg-light border-wl">
                        <div id="filters-collapse" class="card-header pointer" data-toggle="collapse" data-target="#filterCollapse" aria-expanded="true" aria-controls="filterCollapse">
                            <div class="row">
                                <div class="col-10">Search for existing offers to add for member</div>
                                <div class="col-2 text-right">
                                    <svg width="1em" height="1em" viewBox="0 0 16 16" class="bi bi-caret-down-fill text-right" fill="currentColor" xmlns="http://www.w3.org/2000/svg">
                                        <path d="M7.247 11.14L2.451 5.658C1.885 5.013 2.345 4 3.204 4h9.592a1 1 0 0 1 .753 1.659l-4.796 5.48a1 1 0 0 1-1.506 0z"/>
                                    </svg>
                                </div>
                            </div>
                        </div>
                        <div class="card-body collapse show" id="filterCollapse">
                            <div class="form-group row">
                                <label id="selectedOffer" for="selectedOffer" class="col-2 col-form-label-sm text-right">Offers</label>
                                <div class="col-10 input-group">
                                    <input id="searchInput" class="form-control" type="text" placeholder="Search...">
                                    <div id="dropdownMenu" class="dropdown-menu dropdown-menu-left" aria-labelledby="dropdownMenuButton" style="left: 0; top: 100%;">
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>

            <div id="results-container" class="align-content-center">
                <g:render template="addMemberOfferSelect" />
            </div>
        </section>
    </body>
</html>