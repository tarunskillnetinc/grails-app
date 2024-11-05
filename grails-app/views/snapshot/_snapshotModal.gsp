<div class="modal-header">
    <h2>Snapshot Management</h2>
</div>

<div class="modal-body">
    <div class="row mt-3 mb-2">
        <g:if test="${snapshot}">
            <div class="col-8 pr-0" style="-ms-flex: 0 0 63%; flex: 0 0 63%; max-width: 63%;">
                <g:hiddenField name="snapshotId" value="${snapshot.id}"/>
                <g:if test="${safeLocations?.collect()?.size() > 1}">
                    <div class="row form-group mb-4 justify-content-center">
                        <div class="col-2 text-right my-auto">Safe: </div>
                        <g:select name="safeLocation" from="${safeLocations}" optionKey="id" optionValue="description" value="${snapshot.getSafeId()}" class="form-control select-border col-5" onchange="showSafeModal(this.value)"/>
                    </div>
                </g:if>
                <div class="row cash-up-by">
                    <p class="mx-auto">Cash up by <a id="cashUpByValueLink" href="#" class="disabled">value</a>, <a id="cashUpByDenominationLink" href="#" onclick="changeCashUpType('DENOMINATION');">denomination</a> or <a id="cashUpByTotalsLink" href="#" onclick="changeCashUpType('TOTALS');">totals</a></p>
                </div>

                <div id="cashUpContainer" class="mt-3 mr-4">
                    <g:render template="/shift/cashUpByValue" />
                </div>
            </div>
            <div class="col-4 pl-0" style="-ms-flex: 0 0 37%; flex: 0 0 37%; max-width: 37%;">
                <g:render template="safeReport" model="[snapshot: snapshot]" />
            </div>
        </g:if>
        <g:else>
            <div class="col-8 pr-0" style="-ms-flex: 0 0 63%; flex: 0 0 63%; max-width: 63%;">
                <div class="row form-group mb-4 justify-content-center">
                    <div class="col-2 text-right my-auto">Safe: </div>
                    <g:select name="safeLocation" from="${safeLocations}" optionKey="id" optionValue="description" noSelection="['':'Please select a safe']" class="form-control select-border col-5" onchange="showSafeModal(this.value)"/>
                </div>
            </div>
            <div class="col-4 pl-0" style="-ms-flex: 0 0 37%; flex: 0 0 37%; max-width: 37%;">
            </div>
        </g:else>
    </div>
</div>

<div class="modal-footer">
    <button type="button" id="cancelSnapshotButton" class="btn btn-secondary" data-dismiss="modal">Cancel</button>
    <g:if test="${snapshot?.countDate == null}">
        <button type="button" id="saveSnapshotButton" class="btn btn-success" onclick="submitSafeCount()">Save</button>
    </g:if>
</div>