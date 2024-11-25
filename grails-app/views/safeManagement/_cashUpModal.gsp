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
                <div class="row cash-up-by">
                    <p class="mx-auto">
                        Cash up by <a id="cashUpByValueLink" href="#" class="disabled">value</a>,
                        <a id="cashUpByDenominationLink" href="#" onclick="changeCashUpType('DENOMINATION');">denomination</a> or
                        <a id="cashUpByTotalsLink" href="#" onclick="changeCashUpType('TOTALS');">totals</a></p>
                </div>

                <div id="cashUpContainer" class="mt-3 mr-4">
                    <g:render template="/shift/cashUpByValue" />
                </div>
            </div>
        </g:if>
    </div>
</div>

<div class="modal-footer">
    <button type="button" id="cancelSnapshotButton" class="btn btn-secondary" data-dismiss="modal">Cancel</button>
    <button type="button" id="saveSnapshotButton" class="btn btn-success" onclick="saveSafeSessionCashUrl(${safeSession.id}, ${safeSession.reconciledDate != null}, `${safeDescription}`)">Save</button>
</div>