<div class="modal-header">
    <h2>Cash Lift</h2>
</div>

<div class="row mt-3 mb-2">
    <div class="col pr-0">
        <g:form name="modal-form">
            <g:if test="${safeLocations?.collect()?.size() > 1}">
                <!-- display to/from all safe locations and have options for banking and cash lift -->
                <div class="row form-group mb-4 justify-content-center">
                    <g:select name="fromLocation" from="${safeLocations}" optionKey="id" optionValue="description"  class="form-control select-border col-3"/>
                    <h3 class="col-1 text-center">-></h3>
                    <g:select name="toLocation" from="${safeLocations}" optionKey="id" optionValue="description" class="form-control select-border col-3"/>
                </div>
                <div class="row form-group mb-4 justify-content-center">
                    <g:render template="/shift/textField" model="[name: 'cashTotal', label: 'Cash Total', value: values?.cashTotal ?: 0.00, labelCols: 2]"/>
                    <g:render template="/shift/textField" model="[name: 'vouchersTotal', label: 'Vouchers Total', value: values?.vouchersTotal ?: 0.00, labelCols: 2]" />
                </div>
            </g:if>
            <g:else>
                <p>Not enough safe locations exist for this store. Please contact an administrator.</p>
            </g:else>
        </g:form>
    </div>
</div>

<div class="modal-footer">
    <g:if test="${error}">
        <section id="alerts-container" class="container-fluid">
            <div class="alert alert-danger alert-wl mx-0" role="alert">${error}</div>
        </section>
    </g:if>
    <button type="button" id="cancelCashLiftButton" class="btn btn-secondary" data-dismiss="modal">Close</button>
    <g:if test="${safeLocations?.collect()?.size() > 1}">
        <button type="button" id="saveCashLiftButton" class="btn btn-success" onclick="saveModal('CASH_LIFT')">Save</button>
    </g:if>
</div>