<%@ page import="uk.co.wonderlane.wlpos.enums.StockClassification" %>

<div class="row">
    <div class="form-group row col-5 offset-lg-1">
        <label for="varianceQuantity" class="col-5 col-form-label text-right pr-4 pl-1">Variance Quantity Threshold:</label>
        <g:field type="number" min="1" max="1000" step="1" name="varianceQuantity" value="${category?.varianceQuantity}" class="col-4 form-control" oninput="validateNumericInputField(this, 1, 1000, 1, false)" />
        <small id="varianceQuantityHelp" class="col-8 text-right form-text text-muted">Adjustments of this quantity will trigger a variance report.</small>
    </div>
    <div class="form-group row col-5">
        <label for="varianceValue" class="col-5 col-form-label text-right pr-4">Variance Value Threshold:</label>
        <g:field type="number" min="1" max="9999999.99" step=".01" name="varianceValue" value="${category?.varianceValue}" class="col-4 form-control" />
        <small id="varianceValueHelp" class="col-8 text-right form-text text-muted">Adjustments of this value will trigger a variance report.</small>
    </div>
</div>

<div class="row">
    <div class="form-group row col-5 offset-lg-1 form-check"">
        <div class="col-5 col-form-label text-right pr-4 pt-0 pb-0">
            <label for="restrictions.saleAllowed" class="col-form-label text-right wl-label">Prohibit From Sale:</label>
            <g:checkBox name="restrictions.saleAllowed" class="col-1 form-check-input wl-checkbox" checked="${!category?.restrictions?.saleAllowed}" />
        </div>
    </div>
    <div class="form-group row col-5 form-check">
        <div class="col-5 col-form-label text-right pr-4 pt-0 pb-0">
            <label for="restrictions.priceEntryRequired" class="col-form-label text-right wl-label">Price Entry Required:</label>
            <g:checkBox name="restrictions.priceEntryRequired" class="col-1 form-check-input wl-checkbox" checked="${category?.restrictions?.priceEntryRequired}" />
        </div>
    </div>
</div>

<div class="row">
    <div class="form-group row col-5 offset-lg-1">
        <label for="restrictions.minOpenPrice" class="col-5 col-form-label text-right pr-4">Minimum Open Price:</label>
        <div class="input-group-prepend">
            <span class="input-group-text">&pound;</span>
        </div>
        <g:textField name="restrictions.minOpenPrice" class="col-5 form-control mask-money" maxlength="9" value="${category?.restrictions?.minOpenPrice ?: '0.01'}" />
    </div>
    <div class="form-group row col-5">
        <label for="restrictions.maxOpenPrice" class="col-5 col-form-label text-right pr-4">Maximum Open Price:</label>
        <div class="input-group-prepend">
            <span class="input-group-text">&pound;</span>
        </div>
        <g:textField name="restrictions.maxOpenPrice" class="col-5 form-control mask-money" maxlength="9" value="${category?.restrictions?.maxOpenPrice ?: '99999.99'}" />
    </div>
</div>

<div class="row">
    <div class="form-group row col-5 offset-lg-1 form-check">
         <div class="col-5 col-form-label text-right pr-4 pt-0 pb-0">
            <label for="restrictions.buyerIdRequired" class="col-form-label text-right wl-label">Customer ID Required:</label>
            <g:checkBox name="restrictions.buyerIdRequired" class="col-1 form-check-input wl-checkbox" checked="${category?.restrictions?.buyerIdRequired}" />
         </div>
    </div>
    <div class="form-group row col-5 form-check">
        <div class="col-5 col-form-label text-right pr-4 pt-0 pb-0">
            <label for="restrictions.buyerIdForced" class="col-form-label text-right wl-label">Customer ID Forced:</label>
            <g:checkBox name="restrictions.buyerIdForced" class="col-1 form-check-input wl-checkbox" checked="${category?.restrictions?.buyerIdForced}" disabled="${!category?.restrictions?.buyerIdRequired}" />
        </div>
    </div>
</div>

<div class="row">
    <div class="form-group row col-5 offset-lg-1">
        <label for="restrictions.buyerAgeRestriction" class="col-5 col-form-label text-right pr-4">Customer Age Restriction:</label>
        <g:textField name="restrictions.buyerAgeRestriction" class="col-5 form-control numberField" maxlength="2" required="true" value="${category?.restrictions?.buyerAgeRestriction}" readonly="${!category?.restrictions?.buyerIdRequired}"/>
    </div>
    <div class="form-group row col-5">
        <label for="restrictions.buyerChallengeAge" class="col-5 col-form-label text-right pr-4">Customer Challenge Age:</label>
        <g:textField name="restrictions.buyerChallengeAge" class="col-5 form-control numberField" maxlength="2" required="true" value="${category?.restrictions?.buyerChallengeAge}" readonly="${!category?.restrictions?.buyerIdRequired}"/>
    </div>
</div>

<div class="row">
    <div class="form-group row col-5 offset-lg-1">
        <label for="restrictions.sellerAgeRestriction" class="col-5 col-form-label text-right pr-4">Operator Age Restriction:</label>
        <g:textField name="restrictions.sellerAgeRestriction" class="col-5 form-control numberField" maxlength="2" required="true" value="${category?.restrictions?.sellerAgeRestriction}" readonly="${!category?.restrictions?.buyerIdRequired}"/>
    </div>
    <div class="form-group row col-5">
        <label for="restrictions.dividendRate" class="col-5 col-form-label text-right pr-4">Dividend Rate:</label>
        <g:textField name="restrictions.dividendRate" class="col-5 form-control" required="true" value="" />
    </div>
</div>

<div class="row">
    <div class="form-group row col-5 offset-lg-1 form-check">
        <div class="col-5 col-form-label text-right pr-4 pt-0 pb-0">
            <label for="restrictions.allowPriceChange" class="col-form-label text-right wl-label">Allow Price Change:</label>
            <g:checkBox name="restrictions.allowPriceChange" class="col-1 form-check-input wl-checkbox" checked="${category?.restrictions?.allowPriceChange}" />
        </div>
    </div>
    <div class="form-group row col-5 form-check">
        <g:if test="${sec.loggedInUserInfo(field: 'retailer.config.loyaltyRetailerConfig.isLoyaltyEnabled').toBoolean()}">
            <div class="col-5 col-form-label text-right pr-4 pt-0 pb-0">
                <label for="restrictions.allowsLoyaltyPointsCollection" class="col-form-label text-right wl-label">Loyalty Points Issued:</label>
                <g:checkBox name="restrictions.allowsLoyaltyPointsCollection" class="col-1 form-check-input wl-checkbox" checked="${category?.restrictions?.allowsLoyaltyPointsCollection}" />
            </div>
        </g:if>
    </div>
</div>

<div class="row">
    <div class="form-group row col-5 offset-lg-1 form-check">
        <div class="col-5 col-form-label text-right pr-4 pt-0 pb-0">
            <label for="restrictions.discountAllowed" class="col-form-label text-right wl-label">Allow Discount:</label>
            <g:checkBox name="restrictions.discountAllowed" class="col-1 form-check-input wl-checkbox" checked="${category?.restrictions?.discountAllowed}" />
        </div>
    </div>
    <div class="form-group row col-5 form-check">
        <div class="col-5 col-form-label text-right pr-4 pt-0 pb-0">
            <label for="restrictions.markdownAllowed" class="col-form-label text-right wl-label">Allow Markdown:</label>
            <g:checkBox name="restrictions.markdownAllowed" class="col-1 form-check-input wl-checkbox" checked="${category?.restrictions?.markdownAllowed}" />
        </div>
    </div>
</div>

<div class="row">
    <div class="form-group row col-5 offset-lg-1 form-check">
        <div class="col-5 col-form-label text-right pr-4 pt-0 pb-0">
            <label for="restrictions.refundAllowed" class="col-form-label text-right wl-label">Allow Refund:</label>
            <g:checkBox name="restrictions.refundAllowed" class="col-1 form-check-input wl-checkbox" checked="${category?.restrictions?.refundAllowed}" />
        </div>
    </div>
    <div class="form-group row col-5">
        <label for="restrictions.maximumMarkdownPercentage" class="col-5 col-form-label text-right pr-4">Maximum Markdown:</label>
        <g:textField  name="restrictions.maximumMarkdownPercentage" class="col-5 form-control" maxlength="3" value="${category?.restrictions?.maximumMarkdownPercentage?.intValue()}" readonly="${!category?.restrictions?.markdownAllowed}" oninput="validatePercentageInput(this)" />
        <div class="input-group-postpend">
            <span class="input-group-text">%</span>
        </div>
    </div>
</div>

<div class="row">
    <div class="form-group row col-5 offset-lg-1 form-check">
        <div class="col-5 col-form-label text-right pr-4 pt-0 pb-0">
            <label for="restrictions.quantityChangeAllowed" class="col-form-label text-right wl-label">Allow Quantity Change:</label>
            <g:checkBox name="restrictions.quantityChangeAllowed" class="col-1 form-check-input wl-checkbox" checked="${category?.restrictions?.quantityChangeAllowed}"/>
        </div>
    </div>
    <div class="form-group row col-5 form-check">
        <div class="col-5 col-form-label text-right pr-4 pt-0 pb-0">
            <label for="restrictions.receiptPrintForced" class="col-form-label text-right wl-label">Force Receipt Print:</label>
            <g:checkBox name="restrictions.receiptPrintForced" class="col-1 form-check-input wl-checkbox" checked="${category?.restrictions?.receiptPrintForced}" />
        </div>
    </div>
</div>

<div class="row">
    <div class="form-group row col-5 offset-lg-1 form-check">
        <div class="col-5 col-form-label text-right pr-4 pt-0 pb-0">
            <label for="restrictions.quantityChangeForced" class="col-form-label text-right wl-label">Force Quantity Change:</label>
            <g:checkBox name="restrictions.quantityChangeForced" class="col-1 form-check-input wl-checkbox" checked="${category?.restrictions?.quantityChangeForced}" disabled="${!category?.restrictions?.quantityChangeAllowed}" />
        </div>
    </div>
    <div class="form-group row col-5 form-check">
        <div class="col-5 col-form-label text-right pr-4 pt-0 pb-0">
            <label for="restrictions.alwaysOpenCashDrawer" class="col-form-label text-right wl-label">Open Cash Drawer:</label>
            <g:checkBox name="restrictions.alwaysOpenCashDrawer" class="col-1 form-check-input wl-checkbox" checked="${category?.restrictions?.alwaysOpenCashDrawer}" />
        </div>
    </div>
</div>

<div class="row">
    <div class="form-group row col-5 offset-lg-1">
        <label for="restrictions.quantityChangeRestriction" class="col-5 col-form-label text-right pr-4">Quantity Change Restriction:</label>
        <g:textField name="restrictions.quantityChangeRestriction" class="col-5 form-control numberField" maxlength="2" required="true" value="${category?.restrictions?.quantityChangeRestriction}" readonly="${!category?.restrictions?.quantityChangeAllowed}" />
    </div>
    <div class="form-group row col-5 form-check">
        <div class="col-5 col-form-label text-right pr-4 pt-0 pb-0">
            <label for="restrictions.promptForMarkdown" class="col-form-label text-right wl-label">Prompt For Markdown:</label>
            <g:checkBox name="restrictions.promptForMarkdown" class="col-1 form-check-input wl-checkbox" checked="${category?.restrictions?.promptForMarkdown}" disabled="${!category?.restrictions?.markdownAllowed}" />
        </div>
    </div>
</div>

<div class="row">
    <div class="form-group row col-5 offset-lg-1">
        <label for="restrictions.pricingClassification" class="col-5 col-form-label text-right pr-4">Pricing Classification:</label>
        <g:select name="restrictions.pricingClassification" from="${pricingClassifications}" optionKey="id" optionValue="classification" value="${category?.restrictions?.pricingClassificationId}" valueMessagePrefix="pricingClassification" class="col-5 form-control select-border" />
    </div>
    <div class="form-group row col-5">
        <label for="restrictions.promptedDaysFrom" class="col-5 col-form-label text-right pr-4">Prompted Days From:</label>
        <g:textField name="restrictions.promptedDaysFrom" class="col-5 form-control numberField" maxlength="2" required="true" value="${category?.restrictions?.promptedDaysFrom}" />
    </div>
</div>

<div class="row">
    <div class="form-group row col-5 form-check offset-lg-1">
        <div class="col-5 col-form-label text-right pr-4 pt-0 pb-0">
            <label for="restrictions.creditPaymentAllowed" class="col-form-label text-right wl-label">Credit Payment Allowed:</label>
            <g:checkBox name="restrictions.creditPaymentAllowed" class="col-1 form-check-input wl-checkbox" checked="${category?.restrictions?.creditPaymentAllowed}" />
        </div>
    </div>
</div>