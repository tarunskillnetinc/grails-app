<div class="row">
    <div class="form-group row col-12 col-sm-6 offset-sm-1">
        <label for="varianceQuantity" class="col-3 col-form-label text-right pr-4">Variance Quantity Threshold</label>
        <g:field type="number" min="0" max="1000" step="1" name="varianceQuantity" value="${category?.varianceQuantity}" class="col-4 form-control bottom-border" />
        <small id="varianceQuantityHelp" class="col-5 form-text text-muted">Adjustments of this quantity will trigger a variance report.</small>
    </div>
</div>

<div class="row">
    <div class="form-group row col-12 col-sm-6 offset-sm-1">
        <label for="varianceValue" class="col-3 col-form-label text-right pr-4">Variance Value Threshold</label>
        <g:field type="number" min="0" max="9999999.99" step=".01" name="varianceValue" value="${category?.varianceValue}" class="col-4 form-control bottom-border" />
        <small id="varianceValueHelp" class="col-5 form-text text-muted">Adjustments of this value will trigger a variance report.</small>
    </div>
</div>

<div id="categoryRestrictions">
    <g:render template="restrictions" model="[category: category]"></g:render>
</div>