<div id="modal-header" class="modal-header">
    <h2>Cash Management</h2>
</div>

<div class="row mt-3 mb-2">
    <div class="col-8 pr-0" style="-ms-flex: 0 0 63%; flex: 0 0 63%; max-width: 63%;">
        <g:if test="${shift.reconciliationTotals.size() > 0}">
            <div class="row cash-up-by">
                <p class="mx-auto">Cash up by <a id="cashUpByValueLink" href="#" onclick="changeCashUpType('VALUE');">value</a>, <a id="cashUpByDenominationLink" href="#" onclick="changeCashUpType('DENOMINATION');">denomination</a> or <a id="cashUpByTotalsLink" href="#" class="disabled">totals</a></p>
            </div>

            <div id="cashUpContainer" class="mt-3 mr-4">
                <g:render template="cashUpByTotals" model="[values: [ cashTotal: (shift.reconciliationTotals.find { it.tenderType.name() == 'CASH' }?.value ?: BigDecimal.ZERO), vouchersTotal: shift.reconciliationTotals.find { it.tenderType.name() == 'VOUCHER' }?.value ?: BigDecimal.ZERO ]]" />
            </div>
        </g:if>
        <g:else>
            <div class="row cash-up-by">
                <p class="mx-auto">Cash up by <a id="cashUpByValueLink" href="#" class="disabled">value</a>, <a id="cashUpByDenominationLink" href="#" onclick="changeCashUpType('DENOMINATION');">denomination</a> or <a id="cashUpByTotalsLink" href="#" onclick="changeCashUpType('TOTALS');">totals</a></p>
            </div>

            <div id="cashUpContainer" class="mt-3 mr-4">
                <g:render template="cashUpByValue" />
            </div>
        </g:else>
    </div>

    <div class="col-4 pl-0" style="-ms-flex: 0 0 37%; flex: 0 0 37%; max-width: 37%;">
        <g:render template="shiftReport" model="[shift: shift]" />
    </div>
</div>


<div class="modal-footer">
    <button type="button" id="cancelShiftButton" class="btn btn-secondary" data-dismiss="modal" onclick="getShifts()">Cancel</button>
    <button type="button" id="saveShiftButton" class="btn btn-success" onclick="submitCash(${shift.id})">Save</button>
</div>