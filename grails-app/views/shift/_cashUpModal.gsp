<div id="modal-header" class="modal-header">
    <h2>Cash Management</h2>
</div>

<div class="row mt-3 mb-2">
    <div class="col-12">
        <g:if test="${shift.reconciliationTotals.size() > 0}">
            <div class="row cash-up-by">
                <p class="mx-auto">Cash up by <a id="cashUpByValueLink" href="#" onclick="changeCashUpType('VALUE');">value</a>, <a id="cashUpByDenominationLink" href="#" onclick="changeCashUpType('DENOMINATION');">denomination</a> or <a id="cashUpByTotalsLink" href="#" class="disabled">totals</a></p>
            </div>

            <div id="cashUpContainer" class="mt-3 mr-4">
                <g:render template="cashUpByTotals" model="[values: [ cashTotal: (shift.reconciliationTotals.find { it.cashTender }?.value ?: BigDecimal.ZERO), totals: shift.reconciliationTotals.findAll { !it.cashTender }], tenderTypes: tenderTypes]" />
            </div>
        </g:if>
        <g:else>
            <div class="row cash-up-by">
                <p class="mx-auto">Cash up by <a id="cashUpByValueLink" href="#" class="disabled">value</a>, <a id="cashUpByDenominationLink" href="#" onclick="changeCashUpType('DENOMINATION');">denomination</a> or <a id="cashUpByTotalsLink" href="#" onclick="changeCashUpType('TOTALS');">totals</a></p>
            </div>

            <div id="cashUpContainer" class="mt-3 mr-4">
                <g:render template="cashUpByValue" model="[tenderTypes: tenderTypes]" />
            </div>
        </g:else>
    </div>
</div>


<div class="modal-footer">
    <button type="button" id="cancelShiftButton" class="btn btn-secondary" data-dismiss="modal" onclick="getShifts()">Cancel</button>
    <button type="button" id="saveShiftButton" class="btn btn-success" onclick="submitCash(${shift.id}, ${shift.reconciledDate != null})">Save</button>
</div>