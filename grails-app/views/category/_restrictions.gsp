<h2 class="row col-2 mt-4">Restrictions</h2>
<div class="row">
    <div class="form-group row col-12 col-sm-6 offset-sm-1">
        <label for="restrictions.minOpenPrice" class="col-3 col-form-label text-right pr-4">Minimum Open Price</label>
        <g:textField name="restrictions.minOpenPrice" class="col-5 form-control mask-money bottom-border" value="${category?.restrictions?.minOpenPrice}" />
    </div>
    <div class="form-group row col-12 col-sm-5">
        <label for="restrictions.maxOpenPrice" class="col-3 col-form-label text-right pr-4">Maximum Open Price</label>
        <g:textField name="restrictions.maxOpenPrice" class="col-5 form-control mask-money bottom-border" value="${category?.restrictions?.maxOpenPrice}" />
    </div>
</div>
<div class="row">
    <div class="form-group row col-12 col-sm-6 offset-sm-1 form-check">
        <label for="restrictions.buyerIdRequired" class="col-3 col-form-label text-right pr-4">Customer ID Required</label>
        <g:checkBox name="restrictions.buyerIdRequired" class="col-1 form-check-input wl-checkbox" checked="${category?.restrictions?.buyerIdRequired}" />
    </div>
    <div class="form-group row col-12 col-sm-5 form-check">
        <label for="restrictions.buyerIdForced" class="col-3 col-form-label text-right pr-4">Customer ID Forced</label>
        <g:checkBox name="restrictions.buyerIdForced" class="col-1 form-check-input wl-checkbox" checked="${category?.restrictions?.buyerIdForced}" />
    </div>
</div>
<div class="row">
    <div class="form-group row col-12 col-sm-6 offset-sm-1">
        <label for="restrictions.buyerAgeRestriction" class="col-3 col-form-label text-right pr-4">Customer Age Restriction</label>
        <g:field name="restrictions.buyerAgeRestriction" type="number" class="col-5 form-control bottom-border" required="true" value="${category?.restrictions?.buyerAgeRestriction}"/>
    </div>
    <div class="form-group row col-12 col-sm-5">
        <label for="restrictions.buyerChallengeAge" class="col-3 col-form-label text-right pr-4">Customer Challenge Age</label>
        <g:field name="restrictions.buyerChallengeAge" type="number" class="col-5 form-control bottom-border" required="true" value="${category?.restrictions?.buyerChallengeAge}"/>
    </div>
</div>
<div class="row">
    <div class="form-group row col-12 col-sm-6 offset-sm-1">
        <label for="restrictions.sellerAgeRestriction" class="col-3 col-form-label text-right pr-4">Operator Age Restriction</label>
        <g:field name="restrictions.sellerAgeRestriction" type="number" class="col-5 form-control bottom-border" required="true" value="${category?.restrictions?.sellerAgeRestriction}"/>
    </div>
    <div class="form-group row col-12 col-sm-5 form-check">
        <label for="restrictions.refundAllowed" class="col-3 col-form-label text-right pr-4">Refund Allowed</label>
        <g:checkBox name="restrictions.refundAllowed" class="col-1 form-check-input wl-checkbox" checked="${category?.restrictions?.refundAllowed}" />
    </div>
</div>

<div class="row">
    <div class="form-group row col-12 col-sm-6 offset-sm-1 form-check">
        <label for="restrictions.markdownAllowed" class="col-3 col-form-label text-right pr-4">Markdown Allowed</label>
        <g:checkBox name="restrictions.markdownAllowed" class="col-1 form-check-input wl-checkbox" checked="${category?.restrictions?.markdownAllowed}" />
    </div>
    <div class="form-group row col-12 col-sm-5 form-check">
        <label for="restrictions.discountAllowed" class="col-3 col-form-label text-right pr-4">Discount Allowed</label>
        <g:checkBox name="restrictions.discountAllowed" class="col-1 form-check-input wl-checkbox" checked="${category?.restrictions?.discountAllowed}" />
    </div>
</div>
<div class="row">
    <div class="form-group row col-12 col-sm-6 offset-sm-1 form-check">
        <label for="restrictions.creditPaymentAllowed" class="col-3 col-form-label text-right pr-4">Credit Payment Allowed</label>
        <g:checkBox name="restrictions.creditPaymentAllowed" class="col-1 form-check-input wl-checkbox" checked="${category?.restrictions?.creditPaymentAllowed}" />
    </div>
    <div class="form-group row col-12 col-sm-5 form-check">
        <label for="restrictions.quantityChangeAllowed" class="col-3 col-form-label text-right pr-4">Quantity Change Allowed</label>
        <g:checkBox name="restrictions.quantityChangeAllowed" class="col-1 form-check-input wl-checkbox" checked="${category?.restrictions?.quantityChangeAllowed}" />
    </div>
</div>
<div class="row">
    <div class="form-group row col-12 col-sm-6 offset-sm-1 form-check">
        <label for="restrictions.quantityChangeForced" class="col-3 col-form-label text-right pr-4">Quantity Change Forced</label>
        <g:checkBox name="restrictions.quantityChangeForced" class="col-1 form-check-input wl-checkbox" checked="${category?.restrictions?.quantityChangeForced}" />
    </div>
    <div class="form-group row col-12 col-sm-5 form-check">
        <label for="restrictions.receiptPrintForced" class="col-3 col-form-label text-right pr-4">Receipt Print Forced</label>
        <g:checkBox name="restrictions.receiptPrintForced" class="col-1 form-check-input wl-checkbox" checked="${category?.restrictions?.receiptPrintForced}" />
    </div>
</div>