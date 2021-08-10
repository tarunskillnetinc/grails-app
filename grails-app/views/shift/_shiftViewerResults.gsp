<div class="row mt-5 pb-2 ml-0 mr-0 table-wl bottom-border">
%{--    <div class="col-2 font-weight-bold"><a href="#" onclick="getShifts('tillId', ${sortColumn == 'tillId' ? sortOrder == 'asc' ? '\'desc\'' : '\'asc\'' : '\'asc\''} );">Till Number</a></div>--}%
%{--    <div class="col-2 font-weight-bold"><a href="#" onclick="getShifts('shiftNumber', ${sortColumn == 'shiftNumber' ? sortOrder == 'asc' ? '\'desc\'' : '\'asc\'' : '\'asc\''} );">Shift Number</a></div>--}%
%{--    <div class="col-2 font-weight-bold"><a href="#" onclick="getShifts('date', ${sortColumn == 'date' ? sortOrder == 'asc' ? '\'desc\'' : '\'asc\'' : '\'asc\''} );">Shift Date</a></div>--}%
%{--    <div class="col-2 font-weight-bold"><a href="#" onclick="getShifts('status', ${sortColumn == 'status' ? sortOrder == 'asc' ? '\'desc\'' : '\'asc\'' : '\'asc\''} );">Status</a></div>--}%
%{--    <div class="col-2 font-weight-bold"><a href="#" onclick="getShifts('total', ${sortColumn == 'total' ? sortOrder == 'asc' ? '\'desc\'' : '\'asc\'' : '\'asc\''} );">Total</a></div>--}%
%{--    <div class="col-2 font-weight-bold"><a href="#" onclick="getShifts('variance', ${sortColumn == 'variance' ? sortOrder == 'asc' ? '\'desc\'' : '\'asc\'' : '\'asc\''} );">Variance</a></div>--}%
    <div class="col-2 font-weight-bold">Till Number</div>
    <div class="col-2 font-weight-bold">Shift Number</div>
    <div class="col-2 font-weight-bold">Shift Date</div>
    <div class="col-2 font-weight-bold">Status</div>
    <div class="col-2 font-weight-bold">Total</div>
    <div class="col-2 font-weight-bold">Variance</div>
</div>

<div class="d-flex justify-content-center">
    <div id="loading-indicator" class="spinner-border" role="status" style="display: none;">
        <span class="sr-only">Loading...</span>
    </div>
</div>

<div id="search-results">
    <g:if test="${!shifts || shifts?.size() == 0}">
        <div id="noResultsRow" class="col pt-2 pb-2 text-center my-auto wl-striped0">No shifts found.</div>
    </g:if>

    <g:each in="${shifts}" var="shift" status="i">
        <div class="row ml-0 mr-0 pt-2 pb-2 wl-striped${i%2} hoverable" title="Click to view." style="cursor: pointer;" onclick="showCashModal(${shift.id}, ${shift.reconciledDate != null});">
            <div class="col-2 my-auto">${shift.tillId}</div>
            <div class="col-2 my-auto">${shift.id}</div><!-- TODO shift.shiftNumber? -->
            <div class="col-2 my-auto"><g:formatDate format="dd/MM/yyyy" date="${shift.firstTransactionDate.toDate()}" /></div>
            <div class="col-2 my-auto">${shift.reconciledDate != null ? "Reconciled" : "Unreconciled"}</div>
            <div class="col-2 my-auto"><g:formatNumber number="${(shift.sales.sum { it.value } ?: BigDecimal.ZERO) - (shift.refunds.sum { it.value } ?: BigDecimal.ZERO)}" type="currency" /></div>
            <div class="col-2 my-auto"><g:formatNumber number="${shift.reconciliationTotals.find { it.tenderType.name() == 'CASH' }?.variance ?: BigDecimal.ZERO}" type="currency" /></div>
        </div>
    </g:each>
</div>