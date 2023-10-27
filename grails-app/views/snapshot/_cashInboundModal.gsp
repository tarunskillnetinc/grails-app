<div class="modal-header">
    <h2>Cash In</h2>
</div>

<div class="modal-body">
    <div class="text-center mt-4 mb-5">Please enter the total value of cash being added to the safe.</div>

    <g:form name="modal-form">
        <g:if test="${safeLocations.collect().size() > 1}">
            <div class="row form-group mb-4 justify-content-center">
                <g:select name="toLocation" from="${safeLocations}" optionKey="id" optionValue="description"  class="form-control select-border col-3"/>
            </div>
        </g:if>
        <g:else>
            <g:hiddenField name="toLocation" value="${safeLocations.collect()[0].id}"/>
        </g:else>
        <div class="row form-group mb-4">
            <g:render template="/shift/textField" model="[name: 'cashTotal', label: 'Cash Total', value: 0.00, labelCols: '5']" />
        </div>
    </g:form>
</div>

<div class="modal-footer">
    <g:if test="${error}">
        <section id="alerts-container" class="container-fluid">
            <div class="alert alert-danger alert-wl mx-0" role="alert">${error}</div>
        </section>
    </g:if>
    <button type="button" id="cancelModalButton" class="btn btn-secondary" data-dismiss="modal">Close</button>
    <g:if test="${!safeLocations.collect().isEmpty()}">
        <button type="button" id="saveBankCashInButton" class="btn btn-success" onclick="saveModal('CASH_INBOUND');">Save</button>
    </g:if>
</div>