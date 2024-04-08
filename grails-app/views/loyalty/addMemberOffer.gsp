<%@ page contentType="text/html;charset=UTF-8" %>
<html>
    <head>
        <meta name="layout" content="main" />
        <title>Add Member Offer</title>

        <asset:stylesheet src="bootstrap-datepicker3.min.css" />
        <asset:javascript src="bootstrap-datepicker.min.js" />

        <script type='text/javascript'>
            function getSelectedOffer() {
                let selectedOffer = document.getElementById('selectedOffer');
                let selectedOption = selectedOffer.options[selectedOffer.selectedIndex];

                let selectedId = selectedOption.value;
                let selectedDescription = selectedOption.textContent;

                return { id: selectedId, description: selectedDescription };
            }

            function SelectOffer() {
                $("#search-results").hide();
                $("#loading-indicator").show();

                let offer = getSelectedOffer();
                let cardNumber = $('#cardNumber').val();

                let url = "${createLink(controller: 'loyalty', action: 'ajaxSelectedOffer')}";

                $.ajax({
                    url: url,
                    data: { id: offer.id, cardNumber: cardNumber },
                    success: function(resp) {
                        $('#results-container').html(resp);
                    },
                    error: function (resp) {
                        console.log(resp);
                    }
                })
            }

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
                    <g:if test="${offers.size() > 0}">
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
                                    <label for="selectedOffer" class="col-2 col-form-label-sm text-right">Offers</label>
                                    <div class="col-10 input-group">
                                        <g:select id="selectedOffer" name="selectedOffer" class="form-control select-border" from="${offers}" optionKey="id" optionValue="offerDescription" />
                                    </div>
                                </div>
                                <div class="form-group row">
                                    <div class="col-9 offset-2 text-right">
                                        <button id="select-button" type="button" class="btn btn-wl text-right" onclick="SelectOffer()">Select Offer</button>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </g:if>
                    <g:else>
                        <div id="no-valid-offers" class="alert alert-danger alert-wl mx-0" role="alert">There are no valid offers currently that can be applied for this member</div>
                    </g:else>
                </div>
            </div>

            <div id="results-container" class="align-content-center">
                <g:render template="addMemberOfferSelect" />
            </div>
        </section>
    </body>
</html>