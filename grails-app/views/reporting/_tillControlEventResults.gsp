<div class="row mt-5 pb-2 ml-0 mr-0 table-wl bottom-border">
    <g:if test="${!userColumns || userColumns?.columns?.find { it.column == "type" }?.enabled}">
        <div class="col font-weight-bold"><a href="#" onclick="getReportData({ max: ${sortParams?.max}, offset: ${sortParams?.offset}, sortColumn: 'type', sortOrder: ${sortParams?.sortColumn == 'type' ? sortParams?.sortOrder == 'asc' ? '\'desc\'' : '\'asc\'' : '\'asc\''} });">Type</a></div>
    </g:if>
    <g:if test="${!userColumns || userColumns?.columns?.find { it.column == "usersName" }?.enabled}">
        <div class="col font-weight-bold"><a href="#" onclick="getReportData({ max: ${sortParams?.max}, offset: ${sortParams?.offset}, sortColumn: 'usersName', sortOrder: ${sortParams?.sortColumn == 'usersName' ? sortParams?.sortOrder == 'asc' ? '\'desc\'' : '\'asc\'' : '\'asc\''} });">User</a></div>
    </g:if>
    <g:if test="${!userColumns || userColumns?.columns?.find { it.column == "reason" }?.enabled}">
        <div class="col font-weight-bold"><a href="#" onclick="getReportData({ max: ${sortParams?.max}, offset: ${sortParams?.offset}, sortColumn: 'reason', sortOrder: ${sortParams?.sortColumn == 'reason' ? sortParams?.sortOrder == 'asc' ? '\'desc\'' : '\'asc\'' : '\'asc\''} });">Reason</a></div>
    </g:if>
    <g:if test="${!userColumns || userColumns?.columns?.find { it.column == "dateCreated" }?.enabled}">
        <div class="col font-weight-bold"><a href="#" onclick="getReportData({ max: ${sortParams?.max}, offset: ${sortParams?.offset}, sortColumn: 'dateCreated', sortOrder: ${sortParams?.sortColumn == 'dateCreated' ? sortParams?.sortOrder == 'asc' ? '\'desc\'' : '\'asc\'' : '\'asc\''} });">Date</a></div>
    </g:if>
    <g:if test="${!userColumns || userColumns?.columns?.find { it.column == "amount" }?.enabled}">
        <div class="col font-weight-bold"><a href="#" onclick="getReportData({ max: ${sortParams?.max}, offset: ${sortParams?.offset}, sortColumn: 'amount', sortOrder: ${sortParams?.sortColumn == 'amount' ? sortParams?.sortOrder == 'asc' ? '\'desc\'' : '\'asc\'' : '\'asc\''} });">Amount</a></div>
    </g:if>
</div>

<div class="d-flex justify-content-center">
    <div id="loading-indicator" class="spinner-border" role="status" style="display: none;">
        <span class="sr-only">Loading...</span>
    </div>
</div>

<div id="search-results">
    <g:if test="${!tillControlEvents || tillControlEvents?.size() == 0}">
        <div id="noResultsRow" class="col pt-2 pb-2 text-center my-auto wl-striped0">No results found.</div>
    </g:if>

    <g:each in="${tillControlEvents}" var="tillControlEvent" status="i">
        <div class="row ml-0 mr-0 pt-2 pb-2 wl-striped${i%2}">
            <g:if test="${!userColumns || userColumns?.columns?.find { it.column == "type" }?.enabled}">
                <div class="col my-auto"><g:message code="TillControlEventType.${tillControlEvent.type}" /></div>
            </g:if>
            <g:if test="${!userColumns || userColumns?.columns?.find { it.column == "usersName" }?.enabled}">
                <div class="col my-auto">${tillControlEvent.usersName}</div>
            </g:if>
            <g:if test="${!userColumns || userColumns?.columns?.find { it.column == "reason" }?.enabled}">
                <div class="col my-auto">
                    <g:if test="${!tillControlEvent.reason}">N/A</g:if>
                    <g:elseif test="${tillControlEvent.type.name() == 'CUSTOMER_REFUSAL'}"><g:message code="CustomerRefusalReason.${tillControlEvent.reason}" /></g:elseif>
                    <g:elseif test="${tillControlEvent.type.name() == 'REFUND'}"><g:message code="RefundReason.${tillControlEvent.reason}" /></g:elseif>
                    <g:elseif test="${tillControlEvent.type.name() == 'MARKDOWN'}"><g:message code="MarkdownReason.${tillControlEvent.reason}" /></g:elseif>
                    <g:elseif test="${tillControlEvent.type.name() == 'LINE_VOID'}"><g:message code="LineVoidReason.${tillControlEvent.reason}" /></g:elseif>
                    <g:elseif test="${tillControlEvent.type.name() == 'PAID_OUT'}"><g:message code="PaidOutReason.${tillControlEvent.reason}" /></g:elseif>
                    <g:else>${tillControlEvent.reason}</g:else>
                </div>
            </g:if>
            <g:if test="${!userColumns || userColumns?.columns?.find { it.column == "dateCreated" }?.enabled}">
                <div class="col my-auto">${tillControlEvent.dateCreated.toString("dd/MM/yy HH:mm:ss")}</div>
            </g:if>
            <g:if test="${!userColumns || userColumns?.columns?.find { it.column == "amount" }?.enabled}">
                <div class="col my-auto">
                    <g:if test="${tillControlEvent.amount != null}">
                        <g:formatNumber number="${tillControlEvent.amount}" type="currency" />
                    </g:if>
                    <g:else>N/A</g:else>
                </div>
            </g:if>
        </div>
    </g:each>
</div>

<g:if test="${totalResults > 0}">
    <div class="my-3 text-right">
        <div>Displaying ${sortParams?.offset ? sortParams?.offset + 1 : 1} - ${((sortParams?.offset ?: 0) + (tillControlEvents?.size() ?: 0))} of ${totalResults} result${totalResults > 1 ? 's' : ''}</div>
        <div class="mt-3"><g:paginateReport totalResults="${totalResults}" offset="${sortParams?.offset}" max="${sortParams?.max}" sortColumn="${sortParams?.sortColumn}" sortOrder="${sortParams?.sortOrder}" /></div>
    </div>
</g:if>