<div class="modal-header">
    <g:if test="${banking}">
        <h2>Banking</h2>
    </g:if>
    <g:else>
        <h2>Cash In</h2>
    </g:else>
</div>

<div class="modal-body">
    <g:if test="${banking}">
        <div class="text-center mt-4 mb-5">Please complete the total value of cash being banked.</div>
    </g:if>
    <g:else>
        <div class="text-center mt-4 mb-5">Please enter the total value of cash being added to the safe..</div>
    </g:else>

    <g:if test="${bankError}">
        <div class="alert alert-danger text-center alert-wl mx-0" role="alert">Unable to remove an amount greater than the contents of the safe.</div>
    </g:if>

    <g:if test="${zeroError}">
        <div class="alert alert-danger text-center alert-wl mx-0" role="alert">Cash Total Value must be greater than zero.</div>
    </g:if>

    <g:form name="bankingCashInForm">
        <div class="row form-group mb-4">
            <g:render template="/shift/textField" model="[name: 'cashTotal', label: 'Cash Total', value: 0.00, labelCols: '5']" />
        </div>
    </g:form>
</div>

<div class="modal-footer">
    <button type="button" id="cancelBankCashInModal" class="btn btn-wl" onclick="cancelModal();">Cancel</button>
    <button type="button" id="saveBankCashInButton" class="btn btn-success" onclick="saveModal(${banking});">Save</button>
</div>