<div class="modal-header">
    <g:if test="${tenderType?.id}">
        <h2>Edit Tender Type</h2>
    </g:if>
    <g:else>
        <h2>Add Tender Type</h2>
    </g:else>
</div>

<div class="modal-body">
    <g:hasErrors bean="${tenderType}">
        <div class="alert alert-danger alert-wl" role="alert">
            <g:renderErrors bean="${tenderType}" as="list" />
        </div>
    </g:hasErrors>

    <form id="edit-tender-type-form" name="edit-tender-type-form">
        <g:hiddenField name="id" value="${tenderType?.id ?: 0}" />
        <g:hiddenField name="deleted" value="${tenderType?.deleted}" />
        <g:hiddenField name="isProtected" value="${tenderType?.isProtected}" />

        <div class="row form-group mb-4">
            <label for="name" class="col-3 offset-1 col-form-label text-right pointer">Name:</label>
            <div class="input-group col-4">
                <g:textField name="name" value="${tenderType?.name}" class="form-control bottom-border" maxLength="24" />
            </div>
        </div>

        <div class="row form-group mb-4">
            <label for="receiptDescription" class="col-3 offset-1 col-form-label text-right pointer">Receipt Description:</label>
            <div class="input-group col-4">
                <g:textField name="receiptDescription" value="${tenderType?.receiptDescription}" class="form-control bottom-border" maxLength="24" />
            </div>
        </div>

        <div class="row form-group mb-4" id="auto-reconcile">
            <label for="autoReconcile" class="col-3 offset-1 col-form-label text-right pointer">Auto Reconcile:</label>
            <div class="input-group col-8">
                <g:checkBox name="autoReconcile" value="${tenderType?.autoReconcile}" class="form-check-input wl-checkbox mx-0 pointer" onclick="autoReconcileChanged(this);" disabled="${tenderType?.cashTender || tenderType?.eligibleForBanking || tenderType?.eligibleForFloat || tenderType?.eligibleForCashLift}" />
                <label class="col-form-label-sm wl-label-right">Tender will be automatically reconciled when cashing up.</label>
            </div>
        </div>

        <div class="row form-group mb-4" id="eligible-for-banking">
            <label for="eligibleForBanking" class="col-3 offset-1 col-form-label text-right pointer">Eligible for Banking:</label>
            <div class="input-group col-8">
                <g:checkBox name="eligibleForBanking" value="${tenderType?.eligibleForBanking}" class="form-check-input wl-checkbox mx-0 pointer" onclick="eligibleOptionChanged(this);" disabled="${tenderType?.autoReconcile}" />
                <label class="col-form-label-sm wl-label-right">Tender can be lifted from the safe to the bank.</label>
            </div>
        </div>

        <div class="row form-group mb-4" id="eligible-for-float">
            <label for="eligibleForFloat" class="col-3 offset-1 col-form-label text-right pointer">Eligible for Float:</label>
            <div class="input-group col-8">
                <g:checkBox name="eligibleForFloat" value="${tenderType?.eligibleForFloat}" class="form-check-input wl-checkbox mx-0 pointer" onclick="eligibleOptionChanged(this);" disabled="${tenderType?.autoReconcile}" />
                <label class="col-form-label-sm wl-label-right">Tender can be added to tills as a float.</label>
            </div>
        </div>

        <div class="row form-group mb-4" id="eligible-for-cash-lift">
            <label for="eligibleForCashLift" class="col-3 offset-1 col-form-label text-right pointer">Eligible for Tender Lift:</label>
            <div class="input-group col-8">
                <g:checkBox name="eligibleForCashLift" value="${tenderType?.eligibleForCashLift}" class="form-check-input wl-checkbox mx-0 pointer" onclick="eligibleOptionChanged(this);" disabled="${tenderType?.autoReconcile}" />
                <label class="col-form-label-sm wl-label-right">Tender can be lifted from tills to the safe.</label>
            </div>
        </div>

        <div class="row form-group mb-4" id="cash-tender">
            <label for="cashTender" class="col-3 offset-1 col-form-label text-right pointer">Cash Tender:</label>
            <div class="input-group col-8">
                <g:checkBox name="cashTender" value="${tenderType?.cashTender}" class="form-check-input wl-checkbox mx-0 pointer" onclick="cashTenderChanged(this);" disabled="${tenderType?.autoReconcile || tenderType?.cardPayment || tenderType?.voucherType}" />
                <label class="col-form-label-sm wl-label-right">Cash tender used for rolling floats.</label>
            </div>
        </div>

        <div class="row form-group mb-4" id="card-payment">
            <label for="cardPayment" class="col-3 offset-1 col-form-label text-right pointer">Card Payment:</label>
            <div class="input-group col-8">
                <g:checkBox name="cardPayment" value="${tenderType?.cardPayment}" class="form-check-input wl-checkbox mx-0 pointer" onclick="cardPaymentChanged(this);" disabled="${tenderType?.cashTender || tenderType?.voucherType}" />
                <label class="col-form-label-sm wl-label-right">Tender will trigger the payment device.</label>
            </div>
        </div>

        <div class="row form-group mb-4" id="voucher-type">
            <label for="voucherType" class="col-3 offset-1 col-form-label text-right pointer">Voucher Type:</label>
            <div class="input-group col-4">
                <g:select name="voucherType" from="${availableVoucherTypes}" valueMessagePrefix="VoucherType" value="${tenderType?.voucherType}" noSelection="['':'None']" class="form-control select-border pointer" onchange="voucherTypeChanged(this);" disabled="${tenderType?.cashTender || tenderType?.cardPayment}" />
            </div>

        </div>
    </form>
</div>

<div class="modal-footer">
    <button type="button" id="cancel-edit-btn" class="btn btn-wl" onclick="closeAddTenderTypeModal();">Cancel</button>
    <button type="button" id="save-code-btn" class="btn btn-success" onclick="saveTenderType();">Save</button>
</div>
