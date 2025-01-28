<div class="row col-12">
    <table class="table">
        <thead class="bg-light">
            <tr>
                <th id="reportData_header" scope="col" colspan="4" class="border">Safe Activity Report</th>
            </tr>
        </thead>
        <tbody>
            <tr>
                <td class="border font-weight-bold">Store ID & Name</td>
                <td id="reportData_store" class="border">
                    ${storeText}
                </td>
                <td class="border font-weight-bold">Safe ID & Description:</td>
                <td id="reportData_safeId" class="border">
                    ${safeText}
                </td>
            </tr>
            <tr>
                <td class="border font-weight-bold">Financial Week</td>
                <td id="reportData_financialWeek" class="border">
                    ${safeSession?.financialWeek?.weekNumber?: 'n/a'}
                </td>
                <td class="border font-weight-bold">Session Number</td>
                <td id="reportData_sessionNumber" class="border">
                    ${safeSession?.sessionNumber} - <g:message code="SafeSessionStatus.${safeSession.sessionStatus}" />
                </td>
            </tr>
            <tr>
                <td class="border font-weight-bold">Open Date & Time</td>
                <td id="reportData_auditStartTime" class="border">
                    ${safeSession.openTime}
                </td>
                <td class="border font-weight-bold">Last Reconciled Date & Time</td>
                <td id="reportData_auditEndTime" class="border">
                    ${(safeSession.reReconciledDate?: safeSession.reconciledDate)?.toString("dd/MM/yyyy HH:mm")?:"n/a"}
                </td>
            </tr>
        </tbody>
    </table>
</div>
<div class="row col-12">
    <table class="table">
        <thead class="bg-light">
            <tr>
                <th scope="col" class="border">Transaction</th>
                <th scope="col" class="border">Transaction Date & Time</th>
                <th scope="col" class="border">User Name</th>
                <th scope="col" class="border">User ID</th>
                <th scope="col" class="border">Transaction Type</th>
                <th scope="col" class="border">Tender</th>
                <th scope="col" class="border">Amount</th>
            </tr>
        </thead>
        <tbody>
            <g:each in="${reportLines}" var="line" status="i">
                <g:set var="span" value="${line.rowspan}" />
                <td id="reportData_${i + 1}_id" rowspan="${span}" class="border">${line.id}</td>
                <td id="reportData_${i + 1}_timestamp" rowspan="${span}" class="border">
                    ${line.timestamp?.toString("dd/MM/yyyy HH:mm")}
                </td>
                <td id="reportData_${i + 1}_name" rowspan="${span}" class="border">${line.name}</td>
                <td id="reportData_${i + 1}_username" rowspan="${span}" class="border">${line.username}</td>
                <td id="reportData_${i + 1}_action" rowspan="${span}" class="border">
                    <g:message code="SafeSessionAction.${line.transactionType}" />
                </td>

                <g:if test="${line.tenderValues.size == 0}">
                    <td id="reportData_${i + 1}_1_type" class="border"></td>
                    <td id="reportData_${i + 1}_1_value" class="border"></td>
                </g:if>
                <g:else>
                    <g:each in="${line.tenderValues}" var="tender" status="j">
                        <g:if test="${j == 0}">
                            <td id="reportData_${i + 1}_${j + 1}_type" class="border">
                                <g:message code="TenderType.${tender.type}" />
                            </td>
                            <td id="reportData_${i + 1}_${j + 1}_value" class="border">
                                <g:if test="${tender.value < BigDecimal.ZERO}">-</g:if>
                                £${String.format("%.2f", tender.value.abs())}
                            </td>
                        </g:if>
                        <g:else>
                            <tr>
                                <td id="reportData_${i + 1}_${j + 1}_type" class="border">
                                    <g:message code="TenderType.${tender.type}" />
                                </td>
                                <td id="reportData_${i + 1}_${j + 1}_value" class="border">
                                    <g:if test="${tender.value < BigDecimal.ZERO}">-</g:if>
                                    £${String.format("%.2f", tender.value.abs())}
                                </td>
                            </tr>
                        </g:else>
                    </g:each>
                </g:else>
                <tr></tr>
            </g:each>
        </tbody>
    </table>
</div>