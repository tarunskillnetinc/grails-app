<%@ page import="uk.co.wonderlane.wlpos.enums.TenderMovementType" %>
<% def hasFinancialWeek = bankingReports?.any { it.financialWeekNumber != null } %>

<div class="d-flex justify-content-center">
    <div id="loading-indicator" class="spinner-border" role="status" style="display: none;">
        <span class="sr-only">Loading...</span>
    </div>
</div>

<div id="search-results">
    <g:if test="${!bankingReports || bankingReports?.size() == 0}">
        <div id="noResultsRow" class="col pt-2 pb-2 text-center my-auto wl-striped0">No results found.</div>
    </g:if>

    <g:if test="${bankingReports && bankingReports.size() > 0}">
        <div class="row col-12">
            <table class="table col-md-12 col-sm-12">
                <thead class="bg-light">
                    <tr>
                        <th id="reportData_header" scope="col" colspan="4" class="border">Banking Report</th>
                    </tr>
                </thead>
                <tbody>
                    <tr class="border font-weight-bold">
                        <td id="reportData_store">Store ID & Name: ${storeNumber} ${storeName}</td>
                    </tr>
                </tbody>
            </table>
        </div>

        <div class="row col-12">
            <table class="table col-md-12 col-sm-12">
                <thead class="bg-light">
                    <tr>
                        <% if (hasFinancialWeek) { %>
                            <th scope="col" class="border">Financial Week</th>
                        <% } %>
                        <th scope="col" class="border">Date</th>
                        <th scope="col" class="border">Banking Type</th>
                        <th scope="col" class="border">Safe</th>
                        <th scope="col" class="border">Bank</th>
                        <th scope="col" class="border">User</th>
                        <th scope="col" class="border">Amount</th>
                        <th scope="col" class="border">Comments</th>
                    </tr>
                </thead>

                <div class="row col-12">
                    <tbody>
                        <g:each in="${bankingReports}" var="bankingReport" status="i">
                            <tr id="reportData_${i + 1}">
                                <% if (hasFinancialWeek) { %>
                                    <td id="reportData_${i + 1}_financialWeek" scope="row" class="border">${bankingReport?.financialWeekNumber ?: 'n/a'}</td>
                                <% } %>
                                <td id="reportData_${i + 1}_date" scope="row" class="border">
                                    <g:formatDate format="dd/MM/yyyy" date="${bankingReport?.bankingDate?.toDate()}" timeZone="Europe/London" />
                                </td>
                                <td id="reportData_${i + 1}_bankingType" scope="row" class="border">
                                    ${bankingReport?.type == TenderMovementType.BANKING ? 'Bank Deposit' : bankingReport?.type == TenderMovementType.CASH_INBOUND ? 'Bank Receipt' : bankingReport?.type}
                                </td>
                                <td id="reportData_${i + 1}_safe" scope="row" class="border">
                                    ${bankingReport?.type == TenderMovementType.BANKING ? locationMap[bankingReport?.fromLocation?.id] : locationMap[bankingReport?.toLocation?.id]}
                                </td>
                                <td id="reportData_${i + 1}_bank" scope="row" class="border">
                                    <div class="text-truncate">${bankingReport?.bankName}</div>
                                    <div class="text-truncate">${bankingReport?.bankReference}</div>
                                </td>
                                <td id="reportData_${i + 1}_user" scope="row" class="border">
                                    <div class="text-truncate" style="overflow: hidden; text-overflow: ellipsis; white-space: nowrap;">${bankingReport?.userName}</div>
                                    <div class="text-truncate" style="overflow: hidden; text-overflow: ellipsis; white-space: nowrap;">${bankingReport?.usersRealName}</div>
                                </td>
                                <td id="reportData_${i + 1}_amount" scope="row" class="border">
                                    <div class="text-truncate">${bankingReport?.tenderType}</div>
                                    <div class="text-truncate">£${bankingReport?.amount}</div>
                                </td>
                                <td id="reportData_${i + 1}_comments scope="row" class="border">
                                ${bankingReport?.comment}
                                </td>
                            </tr>
                        </g:each>
                    </tbody>
                </div>
            </table>
        </div>
    </g:if>
</div>

<g:if test="${totalResults > 0}">
    <div class="my-3 text-right">
        <div>Displaying ${sortParams?.offset ? sortParams?.offset + 1 : 1} - ${((sortParams?.offset ?: 0) + (bankingReports?.size() ?: 0))} of ${totalResults} result${totalResults > 1 ? 's' : ''}</div>
        <div class="mt-3"><g:paginateReport totalResults="${totalResults}" offset="${sortParams?.offset}" max="${sortParams?.max}" sortColumn="${sortParams?.sortColumn}" sortOrder="${sortParams?.sortOrder}" /></div>
    </div>
</g:if>