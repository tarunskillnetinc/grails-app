<div class="modal-header">
    <h2>Safe Session Management</h2>
</div>

<div class="modal-body">
    <div class="row mt-3 mb-2">
        <g:if test="${safeSession}">
            <div class="col-12">
                <div class="row">
                    <p class="mx-auto"><strong>${safeDescription}</strong> count reconciliation</p>
                </div>

                <g:hiddenField name="safeSessionId" value="${safeSession.id}"/>
                <g:hiddenField name="versionId" value="${safeSession.versionId}" />

                <g:if test="${safeSession.reconciliationTotals.size() > 0}">
                    <div class="row cash-up-by">
                        <p class="mx-auto">
                            Cash up by <a id="cashUpByValueLink" href="#" onclick="changeCashUpType('VALUE');">value</a>,
                            <a id="cashUpByDenominationLink" href="#" onclick="changeCashUpType('DENOMINATION');">denomination</a> or
                            <a id="cashUpByTotalsLink" href="#" class="disabled">totals</a></p>
                    </div>

                    <div id="cashUpContainer" class="mt-3 mr-4">
                        <g:render template="/shift/cashUpByTotals" model="[values: [ cashTotal: (safeSession.reconciliationTotals.find { it.cashTender }?.value ?: BigDecimal.ZERO), totals: safeSession.reconciliationTotals.findAll { !it.cashTender }], tenderTypes: tenderTypes]" />
                    </div>
                </g:if>
                <g:else>
                    <div class="row cash-up-by">
                        <p class="mx-auto">Cash up by <a id="cashUpByValueLink" href="#" class="disabled">value</a>,
                            <a id="cashUpByDenominationLink" href="#" onclick="changeCashUpType('DENOMINATION');">denomination</a> or
                            <a id="cashUpByTotalsLink" href="#" onclick="changeCashUpType('TOTALS');">totals</a></p>
                    </div>

                    <div id="cashUpContainer" class="mt-3 mr-4">
                        <g:render template="/shift/cashUpByValue" model="[tenderTypes: tenderTypes]" />
                    </div>
                </g:else>
            </div>
        </g:if>
    </div>
</div>

<div class="modal-footer">
    <button type="button" id="cancelSafeSessionButton" class="btn btn-secondary" data-dismiss="modal">Cancel</button>
    <button type="button" id="saveSafeSessionButton" class="btn btn-success" onclick="saveSafeSessionCashUrl(${safeSession.id}, `${safeSession.versionId}`, ${safeSession.reconciledDate != null}, `${safeDescription.replace("'","\\\'")}`)">Save</button>
</div>