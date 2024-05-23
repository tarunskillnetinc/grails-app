<%@ page import="uk.co.wonderlane.wlpos.enums.TenderMovementType" %>
<div class="row mt-5 pb-2 ml-0 mr-0 table-wl bottom-border">
    <g:if test="${!userColumns || userColumns?.columns?.find { it.column == "timestamp" }?.enabled}">
        <div class="col font-weight-bold"><a href="#" onclick="getReportData({ max: ${sortParams?.max}, offset: ${sortParams?.offset}, sortColumn: 'timestamp', sortOrder: ${sortParams?.sortColumn == 'timestamp' ? sortParams?.sortOrder == 'asc' ? '\'desc\'' : '\'asc\'' : '\'asc\''} });">Timestamp</a></div>
    </g:if>
    <g:if test="${!userColumns || userColumns?.columns?.find { it.column == "storeId" }?.enabled}">
        <div class="col font-weight-bold"><a href="#" onclick="getReportData({ max: ${sortParams?.max}, offset: ${sortParams?.offset}, sortColumn: 'storeId', sortOrder: ${sortParams?.sortColumn == 'storeId' ? sortParams?.sortOrder == 'asc' ? '\'desc\'' : '\'asc\'' : '\'asc\''} });">Store</a></div>
    </g:if>
    <g:if test="${!userColumns || userColumns?.columns?.find { it.column == "fromLocation" }?.enabled}">
        <div class="col font-weight-bold"><a href="#" onclick="getReportData({ max: ${sortParams?.max}, offset: ${sortParams?.offset}, sortColumn: 'fromLocation', sortOrder: ${sortParams?.sortColumn == 'fromLocation' ? sortParams?.sortOrder == 'asc' ? '\'desc\'' : '\'asc\'' : '\'asc\''} });">From Location</a></div>
    </g:if>
    <g:if test="${!userColumns || userColumns?.columns?.find { it.column == "toLocation" }?.enabled}">
        <div class="col font-weight-bold"><a href="#" onclick="getReportData({ max: ${sortParams?.max}, offset: ${sortParams?.offset}, sortColumn: 'toLocation', sortOrder: ${sortParams?.sortColumn == 'toLocation' ? sortParams?.sortOrder == 'asc' ? '\'desc\'' : '\'asc\'' : '\'asc\''} });">To Location</a></div>
    </g:if>
    <g:if test="${!userColumns || userColumns?.columns?.find { it.column == "amount" }?.enabled}">
        <div class="col font-weight-bold"><a href="#" onclick="getReportData({ max: ${sortParams?.max}, offset: ${sortParams?.offset}, sortColumn: 'amount', sortOrder: ${sortParams?.sortColumn == 'amount' ? sortParams?.sortOrder == 'asc' ? '\'desc\'' : '\'asc\'' : '\'asc\''} });">Amount</a></div>
    </g:if>
    <g:if test="${!userColumns || userColumns?.columns?.find { it.column == "type" }?.enabled}">
        <div class="col font-weight-bold"><a href="#" onclick="getReportData({ max: ${sortParams?.max}, offset: ${sortParams?.offset}, sortColumn: 'type', sortOrder: ${sortParams?.sortColumn == 'type' ? sortParams?.sortOrder == 'asc' ? '\'desc\'' : '\'asc\'' : '\'asc\''} });">Type</a></div>
    </g:if>
    <g:if test="${!userColumns || userColumns?.columns?.find { it.column == "reason" }?.enabled}">
        <div class="col-2 font-weight-bold"><a href="#" onclick="getReportData({ max: ${sortParams?.max}, offset: ${sortParams?.offset}, sortColumn: 'reason', sortOrder: ${sortParams?.sortColumn == 'reason' ? sortParams?.sortOrder == 'asc' ? '\'desc\'' : '\'asc\'' : '\'asc\''} });">Reason</a></div>
    </g:if>
    <g:if test="${!userColumns || userColumns?.columns?.find { it.column == "userName" }?.enabled}">
        <div class="col font-weight-bold"><a href="#" onclick="getReportData({ max: ${sortParams?.max}, offset: ${sortParams?.offset}, sortColumn: 'userName', sortOrder: ${sortParams?.sortColumn == 'userName' ? sortParams?.sortOrder == 'asc' ? '\'desc\'' : '\'asc\'' : '\'asc\''} });">User</a></div>
    </g:if>
</div>

<div class="d-flex justify-content-center">
    <div id="loading-indicator" class="spinner-border" role="status" style="display: none;">
        <span class="sr-only">Loading...</span>
    </div>
</div>

<div id="search-results">
    <g:if test="${!tenderMovements || tenderMovements?.size() == 0}">
        <div id="noResultsRow" class="col pt-2 pb-2 text-center my-auto wl-striped0">No results found.</div>
    </g:if>

    <g:each in="${tenderMovements}" var="tenderMovement" status="i">
        <div class="row ml-0 mr-0 pt-2 pb-2 wl-striped${i%2}">
            <g:if test="${!userColumns || userColumns?.columns?.find { it.column == "timestamp" }?.enabled}">
                <div class="col my-auto"><g:formatDate date="${tenderMovement.timestamp.toDate()}" format="dd/MM/yy HH:mm:ss" timeZone="Europe/London" /></div>
            </g:if>
            <g:if test="${!userColumns || userColumns?.columns?.find { it.column == "storeId" }?.enabled}">
                <div class="col my-auto">${tenderMovement.store?.config?.storeNumber}</div>
            </g:if>
            <g:if test="${!userColumns || userColumns?.columns?.find { it.column == "fromLocation" }?.enabled}">
                <g:if test="${tenderMovement.type == TenderMovementType.CASH_INBOUND}"><div class="col my-auto">Bank</div></g:if>
                <g:else><div class="col my-auto">${tenderMovement.fromLocation?.description}</div></g:else>
            </g:if>
            <g:if test="${!userColumns || userColumns?.columns?.find { it.column == "toLocation" }?.enabled}">
                <g:if test="${tenderMovement.type == TenderMovementType.BANKING}"><div class="col my-auto">Bank</div></g:if>
                <g:else><div class="col my-auto">${tenderMovement.toLocation?.description}</div></g:else>
            </g:if>
            <g:if test="${!userColumns || userColumns?.columns?.find { it.column == "amount" }?.enabled}">
                <div class="col my-auto">
                    <g:if test="${tenderMovement.amount != null}"><g:formatNumber number="${tenderMovement.amount}" type="currency" /></g:if>
                    <g:else>N/A</g:else>
                </div>
            </g:if>
            <g:if test="${!userColumns || userColumns?.columns?.find { it.column == "type" }?.enabled}">
                <div class="col my-auto"><g:message code="TenderMovementType.${tenderMovement.type}" /></div>
            </g:if>
            <g:if test="${!userColumns || userColumns?.columns?.find { it.column == "reason" }?.enabled}">
                <div class="col-2 my-auto">
                    <g:if test="${!tenderMovement.reason}">N/A</g:if>
                    <g:elseif test="${tenderMovement.type == 'PAID_OUT'}"><g:message code="PaidOutReason.${tenderMovement.reason}" /></g:elseif>
                    <g:else>${tenderMovement.reason}</g:else>

                    <g:if test="${tenderMovement.reasonOther}">&nbsp;-&nbsp;${tenderMovement.reasonOther}</g:if>
                </div>
            </g:if>
            <g:if test="${!userColumns || userColumns?.columns?.find { it.column == "userName" }?.enabled}">
                <div class="col my-auto">${tenderMovement.userName}</div>
            </g:if>
        </div>
    </g:each>
</div>

<g:if test="${totalResults > 0}">
    <div class="my-3 text-right">
        <div>Displaying ${sortParams?.offset ? sortParams?.offset + 1 : 1} - ${((sortParams?.offset ?: 0) + (tenderMovements?.size() ?: 0))} of ${totalResults} result${totalResults > 1 ? 's' : ''}</div>
        <div class="mt-3"><g:paginateReport totalResults="${totalResults}" offset="${sortParams?.offset}" max="${sortParams?.max}" sortColumn="${sortParams?.sortColumn}" sortOrder="${sortParams?.sortOrder}" /></div>
    </div>
</g:if>