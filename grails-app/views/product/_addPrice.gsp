<div class="row mx-5 pt-2 pb-2 wl-striped${skuIndex % 2} hoverable">
    <div class="col-3 my-auto" id="priceChanges[${skuIndex}].skuText">${sku}</div>

    <%
        def variantPrices = editedPrices ? editedPrices : variant?.prices
    %>
    <g:each in="${priceBands}" var="priceBand" status="index">
        <div class="col input-group">
            <g:hiddenField name="priceChanges[${skuIndex}].priceChanges[${index}].sku" value="${sku}" />
            <g:hiddenField name="priceChanges[${skuIndex}].priceChanges[${index}].priceBandId" value="${priceBand.id}" />

            <div class="input-group-prepend">
                <span class="input-group-text">&pound;</span>
            </div>

            <g:textField name="priceChanges[${skuIndex}].priceChanges[${index}].price" value="${variantPrices?.find { it.priceBandId == priceBand.id && it.sku == sku }?.price}" class="form-control mask-money" disabled="${zeroPrice}" />
        </div>
    </g:each>
</div>