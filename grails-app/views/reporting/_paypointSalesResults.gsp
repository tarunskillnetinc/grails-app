<div class="row mt-5 pb-2 ml-0 mr-0 table-wl bottom-border">
    <g:if test="${!userColumns || userColumns?.columns?.find { it.column == "storeId" }?.enabled}">
        <div class="col-1 font-weight-bold"><a href="#" onclick="getReportData({ max: ${sortParams?.max}, offset: ${sortParams?.offset}, sortColumn: 'storeId', sortOrder: ${sortParams?.sortColumn == 'storeId' ? sortParams?.sortOrder == 'asc' ? '\'desc\'' : '\'asc\'' : '\'asc\''} });">StoreId</a></div>
    </g:if>
    <g:if test="${!userColumns || userColumns?.columns?.find { it.column == "wlTransactionId" }?.enabled}">
        <div class="col-1 font-weight-bold"><a href="#" onclick="getReportData({ max: ${sortParams?.max}, offset: ${sortParams?.offset}, sortColumn: 'wlTransactionId', sortOrder: ${sortParams?.sortColumn == 'wlTransactionId' ? sortParams?.sortOrder == 'asc' ? '\'desc\'' : '\'asc\'' : '\'asc\''} });">Txn Id</a></div>
    </g:if>
    <g:if test="${!userColumns || userColumns?.columns?.find { it.column == "ppTransactionId" }?.enabled}">
        <div class="col-1 font-weight-bold"><a href="#" onclick="getReportData({ max: ${sortParams?.max}, offset: ${sortParams?.offset}, sortColumn: 'ppTransactionId', sortOrder: ${sortParams?.sortColumn == 'ppTransactionId' ? sortParams?.sortOrder == 'asc' ? '\'desc\'' : '\'asc\'' : '\'asc\''} });">PP Txn Id</a></div>
    </g:if>
    <g:if test="${!userColumns || userColumns?.columns?.find { it.column == "terminalId" }?.enabled}">
        <div class="col-1 font-weight-bold"><a href="#" onclick="getReportData({ max: ${sortParams?.max}, offset: ${sortParams?.offset}, sortColumn: 'terminalId', sortOrder: ${sortParams?.sortColumn == 'terminalId' ? sortParams?.sortOrder == 'asc' ? '\'desc\'' : '\'asc\'' : '\'asc\''} });">TID</a></div>
    </g:if>
    <g:if test="${!userColumns || userColumns?.columns?.find { it.column == "description" }?.enabled}">
        <div class="col-3 font-weight-bold"><a href="#" onclick="getReportData({ max: ${sortParams?.max}, offset: ${sortParams?.offset}, sortColumn: 'description', sortOrder: ${sortParams?.sortColumn == 'description' ? sortParams?.sortOrder == 'asc' ? '\'desc\'' : '\'asc\'' : '\'asc\''} });">Description</a></div>
    </g:if>
    <g:if test="${!userColumns || userColumns?.columns?.find { it.column == "type" }?.enabled}">
        <div class="col-1 font-weight-bold"><a href="#" onclick="getReportData({ max: ${sortParams?.max}, offset: ${sortParams?.offset}, sortColumn: 'type', sortOrder: ${sortParams?.sortColumn == 'type' ? sortParams?.sortOrder == 'asc' ? '\'desc\'' : '\'asc\'' : '\'asc\''} });">Type</a></div>
    </g:if>
    <g:if test="${!userColumns || userColumns?.columns?.find { it.column == "value" }?.enabled}">
        <div class="col-1 font-weight-bold"><a href="#" onclick="getReportData({ max: ${sortParams?.max}, offset: ${sortParams?.offset}, sortColumn: 'value', sortOrder: ${sortParams?.sortColumn == 'value' ? sortParams?.sortOrder == 'asc' ? '\'desc\'' : '\'asc\'' : '\'asc\''} });">Value</a></div>
    </g:if>
    <g:if test="${!userColumns || userColumns?.columns?.find { it.column == "status" }?.enabled}">
        <div class="col-1 font-weight-bold"><a href="#" onclick="getReportData({ max: ${sortParams?.max}, offset: ${sortParams?.offset}, sortColumn: 'status', sortOrder: ${sortParams?.sortColumn == 'status' ? sortParams?.sortOrder == 'asc' ? '\'desc\'' : '\'asc\'' : '\'asc\''} });">Status</a></div>
    </g:if>
    <g:if test="${!userColumns || userColumns?.columns?.find { it.column == "transactionDate" }?.enabled}">
        <div class="col-2 font-weight-bold"><a href="#" onclick="getReportData({ max: ${sortParams?.max}, offset: ${sortParams?.offset}, sortColumn: 'transactionDate', sortOrder: ${sortParams?.sortColumn == 'transactionDate' ? sortParams?.sortOrder == 'asc' ? '\'desc\'' : '\'asc\'' : '\'asc\''} });">Txn Date</a></div>
    </g:if>
</div>

<div class="d-flex justify-content-center">
    <div id="loading-indicator" class="spinner-border" role="status" style="display: none;">
        <span class="sr-only">Loading...</span>
    </div>
</div>

<div id="search-results">
    <g:if test="${!sales || sales?.size() == 0}">
        <div id="noResultsRow" class="col pt-2 pb-2 text-center my-auto wl-striped0">No results found.</div>
    </g:if>

    <g:each in="${sales}" var="sale" status="i">
        <div class="row ml-0 mr-0 pt-2 pb-2 wl-striped${i%2} hoverable">
            <g:if test="${!userColumns || userColumns?.columns?.find { it.column == "storeId" }?.enabled}">
                <div id="store-id-${i + 1}" class="col-1 my-auto">${sale.visibleStoreId}</div>
            </g:if>
            <g:if test="${!userColumns || userColumns?.columns?.find { it.column == "wlTransactionId" }?.enabled}">
                <div id="txn-id-${i + 1}" class="col-1 my-auto">${sale.wlTransactionId}</div>
            </g:if>
            <g:if test="${!userColumns || userColumns?.columns?.find { it.column == "ppTransactionId" }?.enabled}">
                <div id="pp-txn-id-${i + 1}" class="col-1 my-auto">${sale.ppTransactionId}</div>
            </g:if>
            <g:if test="${!userColumns || userColumns?.columns?.find { it.column == "terminalId" }?.enabled}">
                <div id="tid-${i + 1}" class="col-1 my-auto">${sale.terminalId}</div>
            </g:if>
            <g:if test="${!userColumns || userColumns?.columns?.find { it.column == "description" }?.enabled}">
                <div id="description-${i + 1}" class="col-3 my-auto" style="white-space: nowrap; text-overflow: ellipsis;">${sale.description}</div>
            </g:if>
            <g:if test="${!userColumns || userColumns?.columns?.find { it.column == 'type'}?.enabled}">
                <div id="type-${i + 1}" class="col-1 my-auto"><g:message code="PPItemType.${sale.type}"/></div>
            </g:if>
            <g:if test="${!userColumns || userColumns?.columns?.find { it.column == "value" }?.enabled}">
                <div id="value-${i + 1}" class="col-1 my-auto">&pound;${sale.value}</div>
            </g:if>
            <g:if test="${!userColumns || userColumns?.columns?.find { it.column == "status" }?.enabled}">
                <div id="status-${i + 1}" class="col-1 my-auto"><g:message code="PPStatus.${sale.status}"/></div>
            </g:if>
            <g:if test="${!userColumns || userColumns?.columns?.find { it.column == "transactionDate" }?.enabled}">
                <div id="txn-date-${i + 1}" class="col-2 my-auto">${sale.transactionDate.toString("dd/MM/yyyy HH:mm:ss")}</div>
            </g:if>
        </div>
    </g:each>
</div>

<g:if test="${totalResults > 0}">
    <div class="my-3 text-right">
        <div>Displaying ${sortParams?.offset ? sortParams?.offset + 1 : 1} - ${((sortParams?.offset ?: 0) + (sales?.size() ?: 0))} of ${totalResults} result${totalResults > 1 ? 's' : ''}</div>
        <div class="mt-3"><g:paginateReport totalResults="${totalResults}" offset="${sortParams?.offset}" max="${sortParams?.max}" sortColumn="${sortParams?.sortColumn}" sortOrder="${sortParams?.sortOrder}" /></div>
    </div>
</g:if>