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
    <div class="col-1 font-weight-bold"><a id="id" href="#" onclick="reOrderData({ max: ${sortParams?.max}, offset: ${sortParams?.offset}, sortColumn: 'id',
        sortOrder: ${sortParams?.sortColumn == 'id' ? sortParams?.sortOrder == 'asc' ? '\'desc\'' : '\'asc\'' : '\'asc\''} });" >Id</a></div>
    <div class="col-2 font-weight-bold"><a id="offerDescription" href="#" onclick="reOrderData({ max: ${sortParams?.max}, offset: ${sortParams?.offset}, sortColumn: 'offerDescription',
        sortOrder: ${sortParams?.sortColumn == 'offerDescription' ? sortParams?.sortOrder == 'asc' ? '\'desc\'' : '\'asc\'' : '\'asc\''} });">Description</a></div>
    <div class="col-2 font-weight-bold"><a id="startDate" href="#" onclick="reOrderData({ max: ${sortParams?.max}, offset: ${sortParams?.offset}, sortColumn: 'startDate',
        sortOrder: ${sortParams?.sortColumn == 'startDate' ? sortParams?.sortOrder == 'asc' ? '\'desc\'' : '\'asc\'' : '\'asc\''} });">Start Date</a></div>
    <div class="col-2 font-weight-bold"><a id="endDate"  href="#" onclick="reOrderData({ max: ${sortParams?.max}, offset: ${sortParams?.offset}, sortColumn: 'endDate',
        sortOrder: ${sortParams?.sortColumn == 'endDate' ? sortParams?.sortOrder == 'asc' ? '\'desc\'' : '\'asc\'' : '\'asc\''} });">End Date</a></div>
    <div class="col-1 font-weight-bold"><a id="currentCustomers" href="#" onclick="reOrderData({ max: ${sortParams?.max}, offset: ${sortParams?.offset}, sortColumn: 'currentCustomers',
        sortOrder: ${sortParams?.sortColumn == 'currentCustomers' ? sortParams?.sortOrder == 'asc' ? '\'desc\'' : '\'asc\'' : '\'asc\''} });">Assigned Member Count</a></div>
    <div class="col-1 font-weight-bold"><a id="currentRedemptions" href="#" onclick="reOrderData({ max: ${sortParams?.max}, offset: ${sortParams?.offset}, sortColumn: 'currentRedemptions',
        sortOrder: ${sortParams?.sortColumn == 'currentRedemptions' ? sortParams?.sortOrder == 'asc' ? '\'desc\'' : '\'asc\'' : '\'asc\''} });">Number Of Times Redeemed</a></div>
    <div class="col-1 font-weight-bold"><a id="maxRedemptions" href="#" onclick="reOrderData({ max: ${sortParams?.max}, offset: ${sortParams?.offset}, sortColumn: 'maxRedemptions',
        sortOrder: ${sortParams?.sortColumn == 'maxRedemptions' ? sortParams?.sortOrder == 'asc' ? '\'desc\'' : '\'asc\'' : '\'asc\''} });">Max Redemptions</a></div>
    <div class="col-1 font-weight-bold"><a id="maxBudget" href="#" onclick="reOrderData({ max: ${sortParams?.max}, offset: ${sortParams?.offset}, sortColumn: 'maxBudget',
        sortOrder: ${sortParams?.sortColumn == 'maxBudget' ? sortParams?.sortOrder == 'asc' ? '\'desc\'' : '\'asc\'' : '\'asc\''} });">Budget</a></div>
    <div class="col-1 font-weight-bold"><a id="remainingBudget" href="#" onclick="reOrderData({ max: ${sortParams?.max}, offset: ${sortParams?.offset}, sortColumn: 'remainingBudget',
        sortOrder: ${sortParams?.sortColumn == 'remainingBudget' ? sortParams?.sortOrder == 'asc' ? '\'desc\'' : '\'asc\'' : '\'asc\''} });">Remaining Budget</a></div>
</div>

<div id="search-results">
    <g:if test="${offers == null}">
        <div class="row ml-0 mr-0 text-center">
            <div class="col pt-2 pb-2 text-center my-auto wl-striped0">Please enter search criteria to recall offer data.</div>
        </div>
    </g:if>

    <g:if test="${offers?.size() == 0}">
        <div class="row ml-0 mr-0 text-center">
            <div id="noResultsRow" class="col pt-2 pb-2 text-center my-auto wl-striped0">No results found.</div>
        </div>
    </g:if>

    <g:each in="${offers}" var="offer" status="i">
        <div id="product-result-${i+1}" class="row ml-0 mr-0 pt-2 pb-2 wl-striped${i%2} hoverable" title="Click to edit." style="cursor: pointer;" onclick="document.location.href='${createLink(controller: 'loyalty', action:'showLoyaltyOffer', id: offer.id)}';">
            <div id="id-${i + 1}" class="col-1">${offer.id}</div>
            <div id="description-${i + 1}" class="col-2">${offer.offerDescription}</div>
            <div id="count-${i + 1}" class="col-2">
                <g:if test="${offer?.startDate}">
                    <g:formatDate format="dd/MM/yyyy" date="${offer?.startDate ?: new Date()}"/>
                </g:if>
                <g:else>&nbsp;</g:else>
            </div>
            <div id="count-${i + 1}" class="col-2">
                <g:if test="${offer?.endDate}">
                    <g:formatDate format="dd/MM/yyyy" date="${offer?.endDate ?: new Date()}"/>
                </g:if>
                <g:else>&nbsp;</g:else>
            </div>
            <div id="count-${i + 1}" class="col-1">${offer.currentCustomers}</div>
            <div id="count-${i + 1}" class="col-1">${offer.currentRedemptions}</div>
            <div id="count-${i + 1}" class="col-1">${offer.maxRedemptions}</div>
            <div id="count-${i + 1}" class="col-1">${offer.maxBudget}</div>
            <div id="count-${i + 1}" class="col-1">${offer?.maxBudget ? (offer.maxBudget - offer.currentBudget): ''}</div>
        </div>
    </g:each>
</div>

<div class="my-3 text-right">
    <util:remotePaginate action="ajaxSearchLoyaltyOffers" total="${totalCount ?: 0}" update="results-container" offset="${offset ?: 0}" max="${max ?: 50}" params="[searchTerm: loyaltyOffersTerm, searchBy: loyaltyOffersSearchBy, sortColumn: sortParams?.sortColumn, sortOrder: sortParams?.sortOrder]" />
</div>