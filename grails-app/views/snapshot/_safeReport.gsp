<div class="shift-report mr-4 px-3 py-5">
    <div class="row mb-3">
        <h2 class="mx-auto">Safe Report</h2>
    </div>

    <div class="row font-weight-bolder">
        <h3 class="mx-auto">Expected Totals</h3>
    </div>

    <div class="row font-weight-bolder">
        <div class="col-6">
            Description
        </div>
        <div class="col-6 text-right">
            Total
        </div>
    </div>

    <g:each in="${snapshot.expectedTotals?.sort { it.tenderType.name() }}" var="safeExpectedTotal">
        <div class="row">
            <div class="col-6">
                <g:message code="TenderType.${safeExpectedTotal.tenderType}" />
            </div>
            <div class="col-6 text-right text-truncate">
                <g:formatNumber number="${safeExpectedTotal.value}" type="currency" />
            </div>
        </div>
    </g:each>
</div>