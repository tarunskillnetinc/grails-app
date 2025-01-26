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
                <td id="reportData_store" style="width: 40%;">Store ID: ${store?.config?.storeNumber}</td>
                <td id="reportData_store" style="width: 40%;">Store Name: ${store?.config?.storeName}</td>
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
                        <td id="reportData_${i + 1}_financialWeek" scope="row" class="border">${session.financialWeek?.weekNumber?: 'n/a'}</td>
                    <% } %>
                    <td id="reportData_${i + 1}_usersName" scope="row" class="border">${session?.finalisedUsersRealName}</td>
                    <td id="reportData_${i + 1}_userId" scope="row" class="border">${session?.finalisedUsername}</td>
                    <td id="reportData_${i + 1}_tender" scope="row" class="border" style="padding: 0; border-top-style: none; border-style: none;">
                        <table class="table" style="margin-bottom: 0px">
                            <g:each in="${session.reconciliationTotals}" var="total" status="j">
                                <tr>
                                    <td style="padding-bottom: 4px;padding-top: 4px; ${j == 0 ? 'border-top: none !important;' : ''}">
                                        <g:message code="TenderType.${total?.tenderType}" />
                                    </td>
                                </tr>
                            </g:each>
                        </table>
                    </td>
                    <td id="reportData_${i + 1}_varianceAmount" scope="row" class="border" style="padding: 0;">
                        <table class="table" style="margin-bottom: 0px">
                            <g:each in="${session.reconciliationTotals}" var="total" status="j">
                                <tr>
                                    <td style="padding-bottom: 4px;padding-top: 4px; ${total?.variance < BigDecimal.ZERO ? 'color: red;' : ''} ${j == 0 ? 'border-top: none !important;' : ''}">
                                        <g:if test="${total?.variance < BigDecimal.ZERO}">-</g:if>
                                        £${String.format("%.2f", total?.variance.abs())}
                                    </td>
                                </tr>
                            </g:each>
                        </table>
                    </td>
                    <td id="reportData_${i + 1}_reasonCode" scope="row" class="border">
                        <%
                            boolean displayedReasonCode = false;
                            for (reasoncode in session?.reconciliationTotals) {
                                if (!displayedReasonCode && reasonCodes?.get(reasoncode?.varianceReason)) {
                        %>
                                    <div>${reasonCodes[reasoncode?.varianceReason]}</div>
                        <%
                                    displayedReasonCode = true;
                                    break;
                                }
                            }
                        %>
                    </td>
                    <td id="reportData_${i + 1}_comments" scope="row" class="border">
                        <%
                            boolean displayedComment = false;
                            for (comment in session?.reconciliationTotals) {
                                if (!displayedComment && comment?.varianceReasonText) {
                        %>
                                    <div>${comment?.varianceReasonText}</div>
                        <%
                                    displayedComment = true;
                                    break;
                                }
                            }
                        %>
                    </td>
                </tr>
            </g:each>
        </tbody>
    </table>
</div>