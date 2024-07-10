<script type="application/javascript">
    $(document).ready(function() {
        // Assuming your page buttons have a specific class (e.g., "page-button")
        $('.step').on('click', function() {
            // Scroll to the top of the page with a smooth animation
            $('html, body').animate({ scrollTop: 0 }, 'fast');
        });
    });
</script>

<div class="row mt-5 pb-2 ml-0 mr-0 table-wl bottom-border">
    <div class="col-2 font-weight-bold"><a href="#" onclick="searchOffers({max: '${max}', offset: '${offset}', sortColumn: 'offerDescription',
        sortOrder: ${sortColumn == 'offerDescription' ? sortOrder == 'asc' ? '\'desc\'' : '\'asc\'' : '\'asc\''} })">Offer Description</a></div>
    <div class="col-2 font-weight-bold"><a href="#" onclick="searchOffers({max: '${max}', offset: '${offset}', sortColumn: 'currentRedemptions',
        sortOrder: ${sortColumn == 'currentRedemptions' ? sortOrder == 'asc' ? '\'desc\'' : '\'asc\'' : '\'asc\''} })">Number Of Times Redeemed</a></div>
    <div class="col-2 font-weight-bold"><a href="#" onclick="searchOffers({max: '${max}', offset: '${offset}', sortColumn: 'remainingRedemptions',
        sortOrder: ${sortColumn == 'remainingRedemptions' ? sortOrder == 'asc' ? '\'desc\'' : '\'asc\'' : '\'asc\''} })">Remaining Redemptions</a></div>
    <div class="col-2 font-weight-bold"><a href="#" onclick="searchOffers({max: '${max}', offset: '${offset}', sortColumn: 'startDate',
        sortOrder: ${sortColumn == 'startDate' ? sortOrder == 'asc' ? '\'desc\'' : '\'asc\'' : '\'asc\''} })">Start Date</a></div>
    <div class="col-2 font-weight-bold"><a href="#" onclick="searchOffers({max: '${max}', offset: '${offset}', sortColumn: 'endDate',
        sortOrder: ${sortColumn == 'endDate' ? sortOrder == 'asc' ? '\'desc\'' : '\'asc\'' : '\'asc\''} })">End Date</a></div>
    <div class="col-2 font-weight-bold"><a href="#" onclick="searchOffers({max: '${max}', offset: '${offset}', sortColumn: 'status',
        sortOrder: ${sortColumn == 'status' ? sortOrder == 'asc' ? '\'desc\'' : '\'asc\'' : '\'asc\''} })">Status</a></div>
</div>

<div class="d-flex justify-content-center">
    <div id="loading-indicator" class="spinner-border" role="status" style="display: none;">
        <span class="sr-only">Loading...</span>
    </div>
</div>

<div id="search-results">
    <g:if test="${offers == null}">
        <div class="row ml-0 mr-0 text-center">
            <div class="col pt-2 pb-2 text-center my-auto wl-striped0">Please enter search criteria to recall member offer results</div>
        </div>
    </g:if>

    <g:if test="${offers?.size() == 0}">
        <div class="row ml-0 mr-0 text-center">
            <div id="noResultsRow" class="col pt-2 pb-2 text-center my-auto wl-striped0">No offers found.</div>
        </div>
    </g:if>

    <g:each in="${offers}" var="offer" status="i">
        <div id="offer-result-${i+1}" class="row ml-0 mr-0 pt-2 pb-2 wl-striped${i%2} hoverable" title="Click to edit." style="cursor: pointer;" onclick="document.location.href='${createLink(action:'offerDetails', params:[cardNumber: cardNumber, id: offer.id])}';">
            <div id="offer-result-${i+1}-description" class="col-2 text-truncate">${offer.offerDescription}</div>
            <div id="offer-result-${i+1}-currentRedemptions" class="col-2 text-truncate">${offer.currentRedemptions}</div>
            <div id="offer-result-${i+1}-remainingRedemptions" class="col-2 text-truncate">${offer.maxRedemptions - offer.currentRedemptions}</div>
            <div id="offer-result-${i+1}-startDate" class="col-2 text-truncate">${offer.startDate.toString("HH:mm:ss dd/MM/yyyy")}</div>
            <div id="offer-result-${i+1}-endDate" class="col-2 text-truncate">${offer.endDate.toString("HH:mm:ss dd/MM/yyyy")}</div>
            <div id="offer-result-${i+1}-description" class="col-2 text-truncate">${offer.status}</div>
        </div>
    </g:each>
</div>

<div class="my-3 text-right">
    <util:remotePaginate action="ajaxMemberOffers" total="${totalResults ?: 0}" update="results-container" offset="${offset ?: 0}" max="${max ?: 5}" params="[cardNumber: cardNumber,
                                                                                                                                                               searchTerm: searchTerm,
                                                                                                                                                               searchBy: searchBy,
                                                                                                                                                               activeOffers: activeOffers,
                                                                                                                                                               inactiveOffers: inactiveOffers,
                                                                                                                                                               sortColumn: sortColumn,
                                                                                                                                                               sortOrder: sortOrder]" />
</div>