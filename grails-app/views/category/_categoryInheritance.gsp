<div class="row">
    <div class="form-group row col-5 offset-lg-1">
        <label for="varianceQuantity" class="col-5 col-form-label text-right pr-4 pl-1">Variance Quantity Threshold:</label>
        <g:field type="number" min="0" max="99" step="1" name="varianceQuantity" value="${category?.varianceQuantity}" class="col-4 form-control" />
        <small id="varianceQuantityHelp" class="col-8 text-right form-text text-muted">Adjustments of this quantity will trigger a variance report.</small>
    </div>
    <div class="form-group row col-5">
        <label for="varianceValue" class="col-5 col-form-label text-right pr-4">Variance Value Threshold:</label>
        <g:field type="number" min="0" max="99999.99" step=".01" name="varianceValue" value="${category?.varianceValue}" class="col-4 form-control" />
        <small id="varianceValueHelp" class="col-8 text-right form-text text-muted">Adjustments of this value will trigger a variance report.</small>
    </div>
</div>

<g:render template="restrictions" model="[category: category]"></g:render>