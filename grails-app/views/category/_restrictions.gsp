<h2 class="row col-2 mt-4">Restrictions</h2>
<div class="row">
    <div class="form-group row col-12 col-sm-6 offset-sm-1">
        <label for="restrictions.minOpenPrice" class="col-3 col-form-label text-right pr-4">Minimum Open Price</label>
        <g:textField name="restrictions.minOpenPrice" class="col-5 form-control mask-money bottom-border" value="${category?.restrictions?.minOpenPrice ?: '0.01'}" />
    </div>
    <div class="form-group row col-12 col-sm-5">
        <label for="restrictions.maxOpenPrice" class="col-3 col-form-label text-right pr-4">Maximum Open Price</label>
        <g:textField name="restrictions.maxOpenPrice" class="col-5 form-control mask-money bottom-border" value="${category?.restrictions?.maxOpenPrice ?: '9999.99'}" />
    </div>
</div>
<div class="row">
    <div class="form-group row col-12 col-sm-6 offset-sm-1 form-check">
         <div class="col-3 col-form-label text-right pr-4 pt-0 pb-0">
            <label for="restrictions.buyerIdRequired" class="col-form-label text-right wl-label">Customer ID Required</label>
            <g:checkBox name="restrictions.buyerIdRequired" class="col-1 form-check-input wl-checkbox" checked="${category?.restrictions?.buyerIdRequired}" />
         </div>
    </div>
    <div class="form-group row col-12 col-sm-5 form-check">
        <div class="col-3 col-form-label text-right pr-4 pt-0 pb-0">
            <label for="restrictions.buyerIdForced" class="col-form-label text-right wl-label">Customer ID Forced</label>
            <g:checkBox name="restrictions.buyerIdForced" class="col-1 form-check-input wl-checkbox" checked="${category?.restrictions?.buyerIdForced}" disabled="${!category?.restrictions?.buyerIdRequired}" />
        </div>
    </div>
</div>
<div class="row">
    <div class="form-group row col-12 col-sm-6 offset-sm-1">
        <label for="restrictions.buyerAgeRestriction" class="col-3 col-form-label text-right pr-4">Customer Age Restriction</label>
        <g:textField name="restrictions.buyerAgeRestriction" class="col-5 form-control bottom-border" required="true" value="${category?.restrictions?.buyerAgeRestriction}" readonly="${!category?.restrictions?.buyerIdRequired}"/>
    </div>
    <div class="form-group row col-12 col-sm-5">
        <label for="restrictions.buyerChallengeAge" class="col-3 col-form-label text-right pr-4">Customer Challenge Age</label>
        <g:textField name="restrictions.buyerChallengeAge" class="col-5 form-control bottom-border" required="true" value="${category?.restrictions?.buyerChallengeAge}" readonly="${!category?.restrictions?.buyerIdRequired}"/>
    </div>
</div>
<div class="row">
    <div class="form-group row col-12 col-sm-6 offset-sm-1">
        <label for="restrictions.sellerAgeRestriction" class="col-3 col-form-label text-right pr-4">Operator Age Restriction</label>
        <g:textField name="restrictions.sellerAgeRestriction" class="col-5 form-control bottom-border" required="true" value="${category?.restrictions?.sellerAgeRestriction}" readonly="${!category?.restrictions?.buyerIdRequired}"/>
    </div>
    <div class="form-group row col-12 col-sm-5 form-check">
            <div class="col-3 col-form-label text-right pr-4 pt-0 pb-0">
                <label for="restrictions.refundAllowed" class="col-form-label text-right wl-label">Refund Allowed</label>
                <g:checkBox name="restrictions.refundAllowed" class="col-1 form-check-input wl-checkbox" checked="${category?.restrictions?.refundAllowed}" />
            </div>
    </div>
</div>

<div class="row">
    <div class="form-group row col-12 col-sm-6 offset-sm-1 form-check">
         <div class="col-3 col-form-label text-right pr-4 pt-0 pb-0">
            <label for="restrictions.markdownAllowed" class="col-form-label text-right wl-label">Markdown Allowed</label>
            <g:checkBox name="restrictions.markdownAllowed" class="col-1 form-check-input wl-checkbox" checked="${category?.restrictions?.markdownAllowed}" />
         </div>
    </div>
    <div class="form-group row col-12 col-sm-5 form-check">
        <div class="col-3 col-form-label text-right pr-4 pt-0 pb-0">
            <label for="restrictions.discountAllowed" class="col-form-label text-right wl-label">Discount Allowed</label>
            <g:checkBox name="restrictions.discountAllowed" class="col-1 form-check-input wl-checkbox" checked="${category?.restrictions?.discountAllowed}" />
        </div>
    </div>
</div>
<div class="row">
    <div class="form-group row col-12 col-sm-6 offset-sm-1 form-check">
         <div class="col-3 col-form-label text-right pr-4 pt-0 pb-0">
            <label for="restrictions.creditPaymentAllowed" class="col-form-label text-right wl-label">Credit Payment Allowed</label>
            <g:checkBox name="restrictions.creditPaymentAllowed" class="col-1 form-check-input wl-checkbox" checked="${category?.restrictions?.creditPaymentAllowed}" />
         </div>
    </div>
    <div class="form-group row col-12 col-sm-5 form-check">
        <div class="col-3 col-form-label text-right pr-4 pt-0 pb-0">
            <label for="restrictions.quantityChangeAllowed" class="col-form-label text-right wl-label">Quantity Change Allowed</label>
            <g:checkBox name="restrictions.quantityChangeAllowed" class="col-1 form-check-input wl-checkbox" checked="${category?.restrictions?.quantityChangeAllowed}"/>
        </div>
    </div>
</div>
<div class="row">
    <div class="form-group row col-12 col-sm-6 offset-sm-1 form-check">
         <div class="col-3 col-form-label text-right pr-4 pt-0 pb-0">
            <label for="restrictions.quantityChangeForced" class="col-form-label text-right wl-label">Quantity Change Forced</label>
            <g:checkBox name="restrictions.quantityChangeForced" class="col-1 form-check-input wl-checkbox" checked="${category?.restrictions?.quantityChangeForced}" />
         </div>
    </div>
    <div class="form-group row col-12 col-sm-5 form-check">
        <div class="col-3 col-form-label text-right pr-4 pt-0 pb-0">
            <label for="restrictions.receiptPrintForced" class="col-form-label text-right wl-label">Receipt Print Forced</label>
            <g:checkBox name="restrictions.receiptPrintForced" class="col-1 form-check-input wl-checkbox" checked="${category?.restrictions?.receiptPrintForced}" />
        </div>
    </div>
</div>
