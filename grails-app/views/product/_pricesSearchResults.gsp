<g:if test="${productPrices == null}">
    <div class="row ml-0 mr-0 text-center">
        <div class="col pt-2 pb-2 text-center my-auto wl-striped0">Please enter a search term.</div>
    </div>
</g:if>

<g:if test="${productPrices?.size() == 0}">
    <div class="row ml-0 mr-0 text-center">
        <div id="noResultsRow" class="col pt-2 pb-2 text-center my-auto wl-striped0">No results found.</div>
    </div>
</g:if>

<g:each in="${productPrices}" var="productPrice" status="i">
    <div class="row ml-0 mr-0 pt-2 pb-2 wl-striped${i%2}">
        <div class="col-2 my-auto">
            <div class="row form-group form-check mb-0">
                <g:checkBox name="product-${productPrice.key}" class="col-2 form-check-input wl-checkbox" style="margin-top: 8px;" />
                <label for="product-${productPrice.key}" class="col-10 col-form-label pl-4">${productPrice.key}</label>
            </div>
        </div>
        <div class="col-4 my-auto">${productPrice.value[0].productDescription}</div>
        <div class="col-1 my-auto"><g:formatNumber number="${productPrice.value[0].costPrice ?: BigDecimal.ZERO}" type="currency" /></div>

        <g:each in="${priceBands}" var="priceBand">
            <div class="col">
                <div class="input-group">
                    <div class="input-group-prepend">
                        <span class="input-group-text">&pound;</span>
                    </div>

                    <g:textField name="price-${productPrice.key}-${priceBand.id}" value="${productPrice.value.find { it.priceBandDescription == priceBand.description }?.price}" class="form-control mask-money" onkeyup="priceChanged(${productPrice.key}, ${priceBand.id}, ${productPrice.value[0].costPrice ?: BigDecimal.ZERO}, this.value);" />
                    <g:hiddenField name="oldPrice-${productPrice.key}-${priceBand.id}" value="${productPrice.value.find { it.priceBandDescription == priceBand.description }?.price}" />
                    <g:hiddenField name="productId-${productPrice.key}-${priceBand.id}" value="${productPrice.value[0].productId}" />

                    <div class="input-group-append">
                        <label id="margin-${productPrice.key}-${priceBand.id}" class="input-group-text" style="width: 80px;" for="price-${productPrice.key}-${priceBand.id}"><g:getMargin retailPrice="${productPrice.value.find { it.priceBandDescription == priceBand.description }?.price}" costPrice="${productPrice.value[0].costPrice ?: BigDecimal.ZERO}" quantity="${BigDecimal.ONE}" /></label>
                    </div>
                </div>
            </div>
        </g:each>
    </div>
</g:each>