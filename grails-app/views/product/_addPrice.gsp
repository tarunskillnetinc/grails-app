<div class="row mx-5 pt-2 pb-2 wl-striped${skuIndex % 2} hoverable">
    <div class="col-3 my-auto" id="priceChanges[${skuIndex}].skuText">${sku}</div>

    <%
        def variantPrices = variant?.prices
    %>
    <g:each in="${priceBands}" var="priceBand" status="index">
        <div class="col">
            <g:hiddenField name="priceChanges[${skuIndex}].priceChanges[${index}].sku" value="${sku}" />
            <g:hiddenField name="priceChanges[${skuIndex}].priceChanges[${index}].priceBandId" value="${priceBand.id}" />

            <g:textField name="priceChanges[${skuIndex}].priceChanges[${index}].price" value="${variantPrices?.find { it.priceBand.id == priceBand.id }?.price}" class="form-control" />
        </div>
    </g:each>
</div>