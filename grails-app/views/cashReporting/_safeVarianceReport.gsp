<%
    def hasFinancialWeek = safeSessions?.any { it.financialWeek?.weekNumber != null }
%>

<div class="row col-12">
    <table class="table col-md-12 col-sm-12">
        <thead class="bg-light">
            <tr>
                <th id="reportData_header" scope="col" colspan="4" class="border">Safe Variance Report</th>
            </tr>
        </thead>
        <tbody>
            <tr class="border font-weight-bold">
                <td id="reportData_store" style="width: 40%;">Store ID: ${store.id}</td>
                <td id="reportData_store" style="width: 40%;">Store Name: ${store.config.storeName}</td>
                <td id="reportData_dateRange" style="width: 20%;">Date: ${startDate} - ${endDate}</td>
            </tr>
        </tbody>
    </table>
</div>
<div class="row col-12">
    <table class="table col-md-12 col-sm-12">
        <thead class="bg-light">
            <tr>
                <th scope="col" class="border">Safe No</th>
                <th scope="col" class="border">Safe Description</th>
                <th scope="col" class="border">Session Number</th>
                <% if (hasFinancialWeek) { %>
                    <th scope="col" class="border">Financial Week</th>
                <% } %>
                <th scope="col" class="border">Finalized by User name</th>
                <th scope="col" class="border">Finalized by User ID</th>
                <th scope="col" class="border">Tender</th>
                <th scope="col" class="border">Variance Amount</th>
                <th scope="col" class="border">Reason code</th>
                <th scope="col" class="border">Comments</th>
            </tr>
        </thead>
        <tbody>
            <g:each in="${safeSessions}" var="session" status="i">
                <tr id="reportData_${i + 1}">
                    <td id="reportData_${i + 1}_safeId" scope="row" class="border">${session?.safeId}</td>
                    <td id="reportData_${i + 1}_safeDescription" scope="row" class="border">${safes[session?.safeId]}</td>
                    <td id="reportData_${i + 1}_sessionNumber" scope="row" class="border">${session?.sessionNumber}</td>
                    <% if (hasFinancialWeek) { %>
                        <td id="reportData_${i + 1}_financialWeek" scope="row" class="border">${session?.financialWeek?.weekNumber}</td>
                    <% } %>
                    <td id="reportData_${i + 1}_usersName" scope="row" class="border">${session?.finalisedUsersRealName}</td>
                    <td id="reportData_${i + 1}_userId" scope="row" class="border">${session?.finalisedUsername}</td>
                    <td id="reportData_${i + 1}_tender" scope="row" class="border" style="padding: 0;">
                        <g:each in="${session.reconciliationTotals}" var="total" status="j">
                            <div style="width: 100%; border-bottom: 1px solid #c0c0c0; padding: 5px 0 5px 10px;">
                                <g:message code="TenderType.${total?.tenderType}" />
                            </div>
                        </g:each>
                    </td>
                    <td id="reportData_${i + 1}_varianceAmount" scope="row" class="border" style="padding: 0;">
                        <g:each in="${session.reconciliationTotals}" var="total" status="j">
                            <div style="width: 100%; border-bottom: 1px solid #c0c0c0; padding: 5px 0 5px 10px; ${total?.variance < BigDecimal.ZERO ? 'color: red;' : ''}">
                                <g:if test="${total?.variance < BigDecimal.ZERO}">-</g:if>
                                £${String.format("%.2f", total?.variance.abs())}
                            </div>
                        </g:each>
                    </td>
                    <td id="reportData_${i + 1}_tender" scope="row" class="border">
                        <g:each in="${session?.reconciliationTotals}" var="total" status="j">
                            <div>${total?.varianceReasonText}</div>
                        </g:each>
                    </td>
                    <td id="reportData_${i + 1}_tender" scope="row" class="border">
                        <g:each in="${session?.reconciliationTotals}" var="total" status="j">
                            <div>${reasonCodes[total?.varianceReason]}</div>
                        </g:each>
                    </td>
                </tr>
            </g:each>
        </tbody>
    </table>
</div>