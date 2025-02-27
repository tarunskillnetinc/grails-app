<div class="row col-12">
    <table class="table">
        <thead class="bg-light">
            <tr>
                <th id="reportData_header" scope="col" colspan="4" class="border">Till Activity Report</th>
            </tr>
        </thead>
        <tbody>
            <tr class="border font-weight-bold">
                <td>Store ID & Name</td>
                <td id="reportData_store">${storeText}</td>
                <td></td>
                <td id="reportData_till">Till No: ${shift.tillId}</td>
            </tr>
            <tr>
                <td class="border font-weight-bold">Financial Week</td>
                <td id="reportData_financialWeek" class="border">${shift.financialWeek?.weekNumber?: 'n/a'}</td>
                <td class="border font-weight-bold">Shift No.:</td>
                <td id="reportData_shift" class="border">
                    ${shift.shiftNumber} - <g:message code="ShiftStatus.${shift.shiftStatus}" />
                </td>
            </tr>
            <tr>
                <td class="border font-weight-bold">Shift Open Date & Time:</td>
                <td id="reportData_shiftOpenTime" class="border">${shift.shiftOpenTime}</td>
                <td class="border font-weight-bold">Shift Close Date & Time:</td>
                <td id="reportData_shiftCloseTime" class="border">${shift.shiftCloseTime?: 'n/a'}</td>
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
                <th scope="col" class="border">Source</th>
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
                <td id="reportData_${i + 1}_source" rowspan="${span}" class="border">${line.source}</td>
                <td id="reportData_${i + 1}_action" rowspan="${span}" class="border">
                    <g:message code="ShiftAction.${line.action}" />
                </td>

                <g:if test="${line.tenderValues.size == 0}">
                    <td id="reportData_${i + 1}_1_type" class="border"></td>
                    <td id="reportData_${i + 1}_1_value" class="border"></td>
                </g:if>
                <g:else>
                    <g:each in="${line.tenderValues}" var="tender" status="j">
                        <g:if test="${j == 0}">
                            <td id="reportData_${i + 1}_${j + 1}_type" class="border">${tender.tenderTypeName}</td>
                            <td id="reportData_${i + 1}_${j + 1}_value" class="border">
                                <g:if test="${tender.value < BigDecimal.ZERO}">-</g:if>
                                <g:formatNumber number="${tender?.value?.abs()}" type="currency" />
                            </td>
                        </g:if>
                        <g:else>
                            <tr>
                                <td id="reportData_${i + 1}_${j + 1}_type" class="border">${tender.tenderTypeName}</td>
                                <td id="reportData_${i + 1}_${j + 1}_value" class="border">
                                    <g:if test="${tender.value < BigDecimal.ZERO}">-</g:if>
                                    <g:formatNumber number="${tender?.value?.abs()}" type="currency" />
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