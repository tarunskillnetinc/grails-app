<div class="row col-12">
    <table class="table">
        <thead class="bg-light">
            <tr>
                <th id="reportData_header" scope="col" colspan="4" class="border">Till Finalised Report</th>
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
                <td id="reportData_shiftNumber" class="border">${shift.shiftNumber}</td>
            </tr>
            <tr>
                <td class="border font-weight-bold">Shift Open Date & Time:</td>
                <td id="reportData_shiftOpenTime" class="border">${shift.shiftOpenTime}</td>
                <td class="border font-weight-bold">Shift Close Date & Time:</td>
                <td id="reportData_shiftCloseTime" class="border">${shift.shiftCloseTime}</td>
            </tr>
            <tr>
                <td class="border font-weight-bold">Safe</td>
                <td id="reportData_safe" class="border">${shift.finalisedSafeDescription} - ${shift.finalisedSafeId}</td>
                <td class="border font-weight-bold">Finalised By:</td>
                <td id="reportData_user" class="border">${shift.finalisedUsersRealName} - ${shift.finalisedUserName}</td>
            </tr>
        </tbody>
    </table>
</div>
<div class="row col-12">
    <table class="table col-md-7 col-sm-12">
        <thead class="bg-light">
            <tr>
                <th scope="col" class="border">Tender</th>
                <th scope="col" class="border">Expected</th>
                <th scope="col" class="border">Declared</th>
                <th scope="col" class="border">Variance</th>
            </tr>
        </thead>
        <tbody>
            <g:each in="${values}" var="value" status="i">
                <tr id="reportData_${i + 1}">
                    <td id="reportData_${i + 1}_name" class="border">
                        <g:message code="TenderType.${value.tenderType}" />
                    </td>
                    <td id="reportData_${i + 1}_expected" class="border">
                        <g:set var="expectedValue" value="${value.value - value.variance}" />
                        <g:if test="${expectedValue < BigDecimal.ZERO}">-</g:if>
                        £${String.format("%.2f", expectedValue.abs())}
                    </td>
                    <td id="reportData_${i + 1}_declared" class="border">
                        £${String.format("%.2f", value.value)}
                    </td>
                    <td id="reportData_${i + 1}_variance" class="border ${(value.variance < BigDecimal.ZERO) ? 'text-danger' : ''}">
                        <g:if test="${value.variance <= BigDecimal.ZERO}">-</g:if>
                        <g:if test="${value.variance != BigDecimal.ZERO}">
                            £${String.format("%.2f", value.variance.abs())}
                        </g:if>
                    </td>
                </tr>
            </g:each>
        </tbody>
    </table>
    <table class="table col-md-5 col-sm-12">
        <thead class="bg-light">
            <tr>
                <th scope="col" class="border">Reason</th>
                <th scope="col" class="border">Additional Reason Code</th>
            </tr>
        </thead>
        <tbody>
            <tr>
                <td id="reportData_variance_reason" class="border" style="word-break: break-word; vertical-align: middle">${reason}</td>
                <td id="reportData_variance_additionalReason" class="border" style="word-break: break-word; vertical-align: middle">${additionalReason}</td>
            </tr>
        </tbody>
    </table>
</div>