<div class="row mt-5 pb-2 ml-0 mr-0 table-wl bottom-border">
    <g:if test="${!userColumns || userColumns?.columns?.find { it.column == "storeNumber" }?.enabled}">
        <div class="col-2 font-weight-bold"><a id="storeNumber" href="#" onclick="getReportData({ max: ${sortParams?.max}, offset: ${sortParams?.offset}, sortColumn: 'storeNumber', sortOrder: ${sortParams?.sortColumn == 'storeNumber' ? sortParams?.sortOrder == 'asc' ? '\'desc\'' : '\'asc\'' : '\'asc\''} });">Store</a></div>
    </g:if>
    <g:if test="${!userColumns || userColumns?.columns?.find { it.column == "tillId" }?.enabled}">
        <div class="col-2 font-weight-bold"><a id="tillId" href="#" onclick="getReportData({ max: ${sortParams?.max}, offset: ${sortParams?.offset}, sortColumn: 'tillId', sortOrder: ${sortParams?.sortColumn == 'tillId' ? sortParams?.sortOrder == 'asc' ? '\'desc\'' : '\'asc\'' : '\'asc\''} });">Till Number</a></div>
    </g:if>
    <g:if test="${!userColumns || userColumns?.columns?.find { it.column == "transactionId" }?.enabled}">
        <div class="col-2 font-weight-bold"><a id="transactionId" href="#" onclick="getReportData({ max: ${sortParams?.max}, offset: ${sortParams?.offset}, sortColumn: 'transactionId', sortOrder: ${sortParams?.sortColumn == 'transactionId' ? sortParams?.sortOrder == 'asc' ? '\'desc\'' : '\'asc\'' : '\'asc\''} });">Transaction ID</a></div>
    </g:if>
    <g:if test="${!userColumns || userColumns?.columns?.find { it.column == "basketTotal" }?.enabled}">
        <div class="col-2 font-weight-bold"><a id="basketTotal" href="#" onclick="getReportData({ max: ${sortParams?.max}, offset: ${sortParams?.offset}, sortColumn: 'basketTotal', sortOrder: ${sortParams?.sortColumn == 'basketTotal' ? sortParams?.sortOrder == 'asc' ? '\'desc\'' : '\'asc\'' : '\'asc\''} });">Basket Total</a></div>
    </g:if>
    <g:if test="${!userColumns || userColumns?.columns?.find { it.column == "donationTotal" }?.enabled}">
        <div class="col-2 font-weight-bold"><a id="donationTotal" href="#" onclick="getReportData({ max: ${sortParams?.max}, offset: ${sortParams?.offset}, sortColumn: 'donationTotal', sortOrder: ${sortParams?.sortColumn == 'donationTotal' ? sortParams?.sortOrder == 'asc' ? '\'desc\'' : '\'asc\'' : '\'asc\''} });">Donation Amount</a></div>
    </g:if>
    <g:if test="${!userColumns || userColumns?.columns?.find { it.column == "dateCreated" }?.enabled}">
        <div class="col-2 font-weight-bold"><a id="dateCreated" href="#" onclick="getReportData({ max: ${sortParams?.max}, offset: ${sortParams?.offset}, sortColumn: 'dateCreated', sortOrder: ${sortParams?.sortColumn == 'dateCreated' ? sortParams?.sortOrder == 'asc' ? '\'desc\'' : '\'asc\'' : '\'asc\''} });">Date</a></div>
    </g:if>
</div>

<div class="d-flex justify-content-center">
    <div id="loading-indicator" class="spinner-border" role="status" style="display: none;">
        <span class="sr-only">Loading...</span>
    </div>
</div>

<div id="search-results">
    <g:if test="${!donations || donations?.size() == 0}">
        <div id="noResultsRow" class="col pt-2 pb-2 text-center my-auto wl-striped0">No results found.</div>
    </g:if>

    <g:each in="${donations}" var="donation" status="i">
        <div class="row ml-0 mr-0 pt-2 pb-2 wl-striped${i%2}">
            <g:if test="${!userColumns || userColumns?.columns?.find { it.column == "storeNumber" }?.enabled}">
                <div id="description-${i + 1}" class="col-2 my-auto">${donation.storeNumber}</div>
            </g:if>
            <g:if test="${!userColumns || userColumns?.columns?.find { it.column == "tillId" }?.enabled}">
                <div id="description-${i + 1}" class="col-2 my-auto">${donation.tillId}</div>
            </g:if>
            <g:if test="${!userColumns || userColumns?.columns?.find { it.column == "transactionId" }?.enabled}">
                <div id="description-${i + 1}" class="col-2 my-auto">${donation.transactionId}</div>
            </g:if>
            <g:if test="${!userColumns || userColumns?.columns?.find { it.column == "basketTotal" }?.enabled}">
                <div id="description-${i + 1}" class="col-2 my-auto">&pound;${donation.basketTotal}</div>
            </g:if>
            <g:if test="${!userColumns || userColumns?.columns?.find { it.column == "donationTotal" }?.enabled}">
                <div id="description-${i + 1}" class="col-2 my-auto">&pound;${donation.donationTotal}</div>
            </g:if>
            <g:if test="${!userColumns || userColumns?.columns?.find { it.column == "dateCreated" }?.enabled}">
                <div id="description-${i + 1}" class="col-2 my-auto">${donation.dateCreated?.toString("dd/MM/yyyy HH:mm")}</div>
            </g:if>
        </div>
    </g:each>
</div>

<g:if test="${totalResults > 0}">
    <div class="my-3 text-right">
        <div>Displaying ${sortParams?.offset ? sortParams?.offset + 1 : 1} - ${((sortParams?.offset ?: 0) + (donations?.size() ?: 0))} of ${totalResults} result${totalResults > 1 ? 's' : ''}</div>
        <div class="mt-3"><g:paginateReport totalResults="${totalResults}" offset="${sortParams?.offset}" max="${sortParams?.max}" sortColumn="${sortParams?.sortColumn}" sortOrder="${sortParams?.sortOrder}" /></div>
    </div>
</g:if>