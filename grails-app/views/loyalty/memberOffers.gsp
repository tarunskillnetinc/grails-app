<!doctype html>
<html>
    <head>
        <meta name="layout" content="main" />
        <title>Member Offers</title>

        <asset:stylesheet src="bootstrap-datepicker3.min.css" />
        <asset:javascript src="bootstrap-datepicker.min.js" />

        <script type="text/javascript">
            function resetForm() {
                document.getElementById('memberOfferSearchTerm').value = null;
                document.getElementById('memberOfferSearchBy').value = 'description';
                document.getElementById("activeOffers").checked = false;
                document.getElementById("inactiveOffers").checked = false;
            }

            function searchOffers(sortParams) {
                $("#search-results").hide();
                $("#loading-indicator").show();

                let url = "${createLink(controller: 'loyalty', action: 'ajaxMemberOffers')}";
                let cardNumber = $('#cardNumber').val();

                let searchTerm = $('#memberOfferSearchTerm').val();

                let activeOffers = $('#activeOffers').prop("checked");
                let inactiveOffers = $('#inactiveOffers').prop("checked");

                $.ajax({
                    url: url,
                    data: {
                        cardNumber: cardNumber,
                        searchTerm: searchTerm,
                        activeOffers: activeOffers,
                        inactiveOffers: inactiveOffers,
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
                            <li id="breadcrumb-4" class="breadcrumb-item active" aria-current="page">Member Offers</li>
                        </ol>
                    </div>
                </div>
            </nav>
        </section>

        <section id="alerts-container" class="container-fluid">
            <g:if test="${flash.message}">
                <div id="alerts-container-message" class="alert alert-success" role="alert">${flash.message}</div>
            </g:if>
        </section>

        <section id="member-offers-search" class="container-fluid">
            <div class="row header-wl mt-3">
                <div class="col-8 offset-2">
                    <h2 id="page-title" class="mx-auto my-auto">Member Offers</h2>
                </div>
                <div class="col text-right d-inline-flex flex-row justify-content-end">
                    <g:link elementId="add-offer-btn" class="btn btn-wl p-2 ml-2" action="addMemberOffer" params="[cardNumber: cardNumber]">Add Offer</g:link>
                </div>
            </div>
            <div class="row mt-4">
                <div class="col-6">
                    <div class="card bg-light border-wl">
                        <div id="filters-collapse" class="card-header pointer" data-toggle="collapse" data-target="#filterCollapse" aria-expanded="true" aria-controls="filterCollapse">
                            <div class="row">
                                <div class="col-10">Filters</div>
                                <div class="col-2 text-right">
                                    <svg width="1em" height="1em" viewBox="0 0 16 16" class="bi bi-caret-down-fill text-right" fill="currentColor" xmlns="http://www.w3.org/2000/svg">
                                        <path d="M7.247 11.14L2.451 5.658C1.885 5.013 2.345 4 3.204 4h9.592a1 1 0 0 1 .753 1.659l-4.796 5.48a1 1 0 0 1-1.506 0z"/>
                                    </svg>
                                </div>
                            </div>
                        </div>
                        <div class="card-body collapse show" id="filterCollapse">
                            <div class="form-group row">
                                <label class="col-2 col-form-label-sm text-right">Search By Description</label>
                                <div class="col-10 input-group">
                                    <g:textField id="memberOfferSearchTerm" name="memberOfferSearchTerm" maxlength="100" class="form-control" aria-describedby="select-addon2" />
                                </div>
                            </div>
                            <div class="row form-group form-check pl-0">
                                 <div class="col-4 col-form-label text-right pr-4 pt-0 pb-0">
                                     <label class="col-form-label text-right wl-label">Active Offers</label>
                                     <g:checkBox id="activeOffers" name="activeOffers" class="col-1 form-check-input wl-checkbox" />
                                 </div>
                                 <div class="col-4 col-form-label text-right pr-4 pt-0 pb-0">
                                     <label class="col-form-label text-right wl-label">Inactive Offers</label>
                                     <g:checkBox id="inactiveOffers" name="inactiveOffers" class="col-1 form-check-input wl-checkbox" />
                                 </div>
                             </div>
                             <div class="form-group row">
                                 <div class="col-9 offset-2 text-right">
                                     <button id="reset-filters-btn" type="button" class="btn btn-danger text-right mr-2" onclick="resetForm()">Reset Filters</button>
                                     <button id="filter-submit-button" type="button" class="btn btn-wl text-right" onclick="searchOffers()">Search</button>
                                 </div>
                             </div>
                        </div>
                    </div>
                </div>
            </div>

            <div id="results-container" class="align-content-center">
                <g:render template="memberOffersSearchResults"
                          model="[cardNumber: cardNumber,
                                  searchTerm: searchTerm,
                                  searchBy  : searchBy,
                                  offset    : offset,
                                  max: max,
                                  sortColumn: sortColumn,
                                  sortOrder : sortOrder,
                                  offers: offers,
                                  totalResults: totalResults,
                                  activeOffers: activeOffers,
                                  inactiveOffers: inactiveOffers]"

                />
            </div>
        </section>
    </body>
</html>