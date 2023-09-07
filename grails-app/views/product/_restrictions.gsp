<g:hiddenField name="restrictions.id" value="${restrictions?.id}" />
<div class="card-body py-5">
    <div class="row">
        <div class="col-12 col-lg-5 offset-lg-1">
            <div class="row form-group form-check pl-0">
                <label for="restrictions.buyerIdRequired" class="col-4 col-form-label text-right pr-4">Age Restricted Item</label>
                <g:checkBox name="restrictions.buyerIdRequired" class="col-1 form-check-input wl-checkbox" checked="${restrictions?.buyerIdRequired}" />
            </div>
            <div class="row form-group form-check pl-0">
                <label for="restrictions.buyerIdForced" class="col-4 col-form-label text-right pr-4">ID Check Forced</label>
                <g:checkBox name="restrictions.buyerIdForced" class="col-1 form-check-input wl-checkbox" checked="${restrictions?.buyerIdForced}" disabled="${!restrictions?.buyerIdRequired}" />
            </div>
            <div class="row form-group">
                <label for="restrictions.buyerAgeRestriction" class="col-4 col-form-label text-right pr-4">Customer Age Required</label>
                <g:field name="restrictions.buyerAgeRestriction" type="number" value="${restrictions?.buyerAgeRestriction}" class="col-2 form-control bottom-border" readonly="${!restrictions?.buyerIdRequired}" />
            </div>
            <div class="row form-group">
                <label for="restrictions.buyerChallengeAge" class="col-4 col-form-label text-right pr-4">Customer Challenge Age</label>
                <g:field name="restrictions.buyerChallengeAge" type="number" value="${restrictions?.buyerChallengeAge}" class="col-2 form-control bottom-border" readonly="${!restrictions?.buyerIdRequired}" />
            </div>
            <div class="row form-group">
                <label for="restrictions.sellerAgeRestriction" class="col-4 col-form-label text-right pr-4">Operator Age Required</label>
                <g:field name="restrictions.sellerAgeRestriction" type="number" value="${restrictions?.sellerAgeRestriction}" class="col-2 form-control bottom-border" readonly="${!restrictions?.buyerIdRequired}" />
            </div>
            <div class="row form-group">
                <label for="restrictions.minOpenPrice" class="col-4 col-form-label text-right pr-4">Minimum Open Price</label>

                <div class="input-group col-3 px-0">
                    <div class="input-group-prepend">
                        <span class="input-group-text">&pound;</span>
                    </div>
                    <g:textField name="restrictions.minOpenPrice" value="${restrictions?.minOpenPrice ?: '0.01'}" class="form-control mask-money open-price" readonly="${!productOpenPrice}" />
                </div>
            </div>
            <div class="row form-group">
                <label for="restrictions.maxOpenPrice" class="col-4 col-form-label text-right pr-4">Maximum Open Price</label>

                <div class="input-group col-3 px-0">
                    <div class="input-group-prepend">
                        <span class="input-group-text">&pound;</span>
                    </div>
                    <g:textField name="restrictions.maxOpenPrice" value="${restrictions?.maxOpenPrice ?: '9999.99'}" class="form-control mask-money open-price"  readonly="${!productOpenPrice}" />
                </div>
            </div>
        </div>

        <div class="col-12 col-lg-6">
            <div class="row form-group form-check pl-0">
                <label for="restrictions.refundAllowed" class="col-4 col-form-label text-right pr-4">Allow Refunds</label>
                <g:checkBox name="restrictions.refundAllowed" class="col-1 form-check-input wl-checkbox" checked="${isNewProduct || restrictions?.refundAllowed == null || restrictions?.refundAllowed}" />
            </div>
            <div class="row form-group form-check pl-0">
                <label for="restrictions.discountAllowed" class="col-4 col-form-label text-right pr-4">Allow Discounts</label>
                <g:checkBox name="restrictions.discountAllowed" class="col-1 form-check-input wl-checkbox" checked="${isNewProduct || restrictions?.discountAllowed == null || restrictions?.discountAllowed}" />
            </div>
            <div class="row form-group form-check pl-0">
                <label for="restrictions.markdownAllowed" class="col-4 col-form-label text-right pr-4">Allow Price Changes</label>
                <g:checkBox name="restrictions.markdownAllowed" class="col-1 form-check-input wl-checkbox" checked="${isNewProduct || restrictions?.markdownAllowed == null || restrictions?.markdownAllowed}" />
            </div>
            <div class="row form-group form-check pl-0">
                <label for="restrictions.creditPaymentAllowed" class="col-4 col-form-label text-right pr-4">Allow Credit Payments</label>
                <g:checkBox name="restrictions.creditPaymentAllowed" class="col-1 form-check-input wl-checkbox" checked="${isNewProduct || restrictions?.creditPaymentAllowed == null || restrictions?.creditPaymentAllowed}"  />
            </div>
            <div class="row form-group form-check pl-0">
                <label for="restrictions.quantityChangeAllowed" class="col-4 col-form-label text-right pr-4">Allow Quantity Changes</label>
                <g:checkBox name="restrictions.quantityChangeAllowed" class="col-1 form-check-input wl-checkbox" checked="${isNewProduct || restrictions?.quantityChangeAllowed == null || restrictions?.quantityChangeAllowed}" />
            </div>
            <div class="row form-group form-check pl-0">
                <label for="restrictions.quantityChangeForced" class="col-4 col-form-label text-right pr-4">Force Quantity Changes</label>
                <g:checkBox name="restrictions.quantityChangeForced" class="col-1 form-check-input wl-checkbox" checked="${restrictions?.quantityChangeForced}" />
            </div>
            <div class="row form-group form-check pl-0">
                <label for="restrictions.receiptPrintForced" class="col-4 col-form-label text-right pr-4">Force Receipt Print</label>
                <g:checkBox name="restrictions.receiptPrintForced" class="col-1 form-check-input wl-checkbox" checked="${restrictions?.receiptPrintForced}" />
            </div>
        </div>
    </div>
</div>