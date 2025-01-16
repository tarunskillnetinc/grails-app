<%@ page import="uk.co.wonderlane.wlpos.enums.TenderMovementType" %>

<div class="row mt-5 pb-2 ml-0 mr-0 table-wl bottom-border" style="display: grid; grid-template-columns: 10% 10% 10% 10% 10% 17% 10% 17% 6%;">
    <div class="col font-weight-bold" style="text-align: center;"><a id="financialWeek">Financial Week</a></div>
    <div class="col font-weight-bold" style="text-align: center;"><a id="date">Date</a></div>
    <div class="col font-weight-bold" style="text-align: center;"><a id="type">Banking Type</a></div>
    <div class="col font-weight-bold" style="text-align: center;"><a id="safe">Safe</a></div>
    <div class="col font-weight-bold" style="text-align: center;"><a id="bank">Bank</a></div>
    <div class="col font-weight-bold" style="text-align: center;"><a id="user">User</a></div>
    <div class="col font-weight-bold" style="text-align: center;"><a id="amount">Amount</a></div>
    <div class="col font-weight-bold" style="text-align: center;"><a id="comments">Comments</a></div>
</div>

<div class="d-flex justify-content-center">
    <div id="loading-indicator" class="spinner-border" role="status" style="display: none;">
        <span class="sr-only">Loading...</span>
    </div>
</div>

<div id="search-results">
    <g:if test="${!bankingReports || bankingReports?.size() == 0}">
        <div id="noResultsRow" class="col pt-2 pb-2 text-center my-auto wl-striped0">No results found.</div>
    </g:if>

    <g:each in="${bankingReports}" var="bankingReport" status="i">
        <div class="row ml-0 mr-0 pt-2 pb-2 wl-striped${i%2}" style="display: grid; grid-template-columns: 10% 10% 10% 10% 10% 17% 10% 17% 6%;">
            <div id="financialWeek-${i+1}" style="text-align: center;">
                ${bankingReport.financialWeekNumber}
            </div>
            <div id="date-${i+1}" style="text-align: center;">
                <g:formatDate format="dd/MM/yyyy" date="${bankingReport.bankingDate?.toDate()}" timeZone="Europe/London" />
            </div>
            <div id="type-${i+1}" style="text-align: center;">
                ${bankingReport.type == TenderMovementType.BANKING ? 'Banking Deposit' : bankingReport.type == TenderMovementType.CASH_INBOUND ? 'Banking Receipt' : bankingReport.type}
            </div>
            <div id="safe-${i+1}" style="text-align: center; white-space: nowrap; overflow: hidden; text-overflow: ellipsis;">
                ${bankingReport.type == TenderMovementType.BANKING ? locationMap[bankingReport?.fromLocation?.id] : locationMap[bankingReport?.toLocation?.id]}
            </div>
            <div id="bank-${i+1}" style="text-align: center; white-space: nowrap; overflow: hidden; text-overflow: ellipsis;">
                <div class="text-truncate">${bankingReport.bankName}</div>
                <div class="text-truncate">${bankingReport.bankReference}</div>
            </div>
            <div id="user-${i+1}" style="text-align: center; white-space: nowrap; overflow: hidden; text-overflow: ellipsis;">
                <div class="text-truncate" style="overflow: hidden; text-overflow: ellipsis; white-space: nowrap;">${bankingReport.userName}</div>
                <div class="text-truncate" style="overflow: hidden; text-overflow: ellipsis; white-space: nowrap;">${bankingReport.usersRealName}</div>
            </div>
            <div id="amount-${i+1}" style="text-align: center;">
                <div class="text-truncate">${bankingReport.tenderType}</div>
                <div class="text-truncate">£${bankingReport.amount}</div>
            </div>
            <div id="comments-${i+1}" style="text-align: center; overflow: hidden; word-wrap: break-word;">
                <div>${bankingReport.comment}</div>
            </div>
            <div id="padding-${i+1}"></div>
        </div>
    </g:each>
</div>

<g:if test="${totalResults > 0}">
    <div class="my-3 text-right">
        <div>Displaying ${sortParams?.offset ? sortParams?.offset + 1 : 1} - ${((sortParams?.offset ?: 0) + (bankingReports?.size() ?: 0))} of ${totalResults} result${totalResults > 1 ? 's' : ''}</div>
        <div class="mt-3"><g:paginateReport totalResults="${totalResults}" offset="${sortParams?.offset}" max="${sortParams?.max}" sortColumn="${sortParams?.sortColumn}" sortOrder="${sortParams?.sortOrder}" /></div>
    </div>
</g:if>