<div class="row col-12">
    <table class="table">
        <thead class="bg-light">
            <tr>
                <th id="reportData_header" scope="col" colspan="4" class="border">Safe Finalised Report</th>
            </tr>
        </thead>
        <tbody>
            <tr class="border font-weight-bold">
                <td>Store ID & Name</td>
                <td colspan="3" id="reportData_store">${storeText}</td>
            </tr>
            <tr>
                <td class="border font-weight-bold">Financial Week</td>
                <td id="reportData_financialWeek" class="border">${safeSession.financialWeek?.weekNumber?: 'n/a'}</td>
                <td class="border font-weight-bold">Session No.:</td>
                <td id="reportData_sessionNumber" class="border">${safeSession.sessionNumber}</td>
            </tr>
            <tr>
                <td class="border font-weight-bold">Session Open Date & Time:</td>
                <td id="reportData_sessionOpenTime" class="border">${safeSession.openTime}</td>
                <td class="border font-weight-bold">Counted Date & Time:</td>
                <td id="reportData_sessionCountedTime" class="border">
                    ${(safeSession.reReconciledDate?: safeSession.reconciledDate)?.toString("dd/MM/yyyy HH:mm")}
                </td>
            </tr>
            <tr>
                <td class="border font-weight-bold">Safe:</td>
                <td id="reportData_safe" class="border">${safeText}</td>
                <td class="border font-weight-bold">Finalised By:</td>
                <td id="reportData_user" class="border">${safeSession.finalisedUsersRealName} - ${safeSession.finalisedUsername}</td>
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
                    <td id="reportData_${i + 1}_name" class="border">${value.tenderTypeName}</td>
                    <td id="reportData_${i + 1}_expected" class="border">
                        <g:set var="expectedValue" value="${value.value - value.variance}" />
                        <g:if test="${expectedValue < BigDecimal.ZERO}">-</g:if>
                        <g:formatNumber number="${expectedValue?.abs()}" type="currency" />
                    </td>
                    <td id="reportData_${i + 1}_declared" class="border">
                        <g:formatNumber number="${value?.value}" type="currency" />
                    </td>
                    <td id="reportData_${i + 1}_variance" class="border ${(value.variance < BigDecimal.ZERO) ? 'text-danger' : ''}">
                        <g:if test="${value.variance < BigDecimal.ZERO}">-</g:if>
                        <g:formatNumber number="${value?.variance?.abs()}" type="currency" />
                    </td>
                </tr>
            </g:each>
        </tbody>
    </table>
    <table class="table col-md-5 col-sm-12">
        <thead class="bg-light">
            <tr>
                <th scope="col" class="border">Reason</th>
                <th scope="col" class="border">Comments</th>
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