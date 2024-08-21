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
    <div id="product-price-${i+1}" class="row ml-0 mr-0 pt-2 pb-2 wl-striped${i%2}">
        <div class="col-1 form-group form-check mb-0 text-center">
            <g:checkBox id="product-price-${i+1}-check-box" name="product-${productPrice.key}" class="selections col form-check-input wl-checkbox-no-label" oninput="toggleRowInputs(${i+1});"/>
        </div>
        <label id="product-price-${i+1}-item-code" for="product-${productPrice.key}" class="col-2 col-form-label my-auto text-truncate text-left">${productPrice.value[0].itemCode}</label>
        <div id="product-price-${i+1}-description" class="col-2 my-auto text-truncate">${productPrice.value[0].productDescription}</div>
        <div id="product-price-${i+1}-cost-price" class="col-1 my-auto"><g:formatNumber number="${productPrice.value[0].costPrice ?: BigDecimal.ZERO}" type="currency" /></div>

        <g:each in="${priceBands}" var="priceBand" status="b">
            <div class="col-2">
                <div class="input-group">
                    <div class="input-group-prepend">
                        <span class="input-group-text">&pound;</span>
                    </div>

                    <g:textField id="product-price-${i+1}-band-${b+1}-price"  name="price-${productPrice.key}-${priceBand.id}" value="${productPrice.value.find { it.priceBandDescription == priceBand.description }?.price}" class="form-control mask-money" style="min-width: 60px;" onkeyup="priceChanged(${productPrice.key}, ${priceBand.id}, ${productPrice.value[0].costPrice ?: BigDecimal.ZERO}, this.value);" disabled="true"/>
                    <g:hiddenField name="oldPrice-${productPrice.key}-${priceBand.id}" value="${productPrice.value.find { it.priceBandDescription == priceBand.description }?.price}" />
                    <g:hiddenField name="productId-${productPrice.key}-${priceBand.id}" value="${productPrice.value[0].productId}" />

                    <div class="input-group-append">
                        <label id="product-price-${i+1}-band-${b+1}-margin" class="input-group-text" style="width:70px;" for="price-${productPrice.key}-${priceBand.id}"><g:getMargin retailPrice="${productPrice.value.find { it.priceBandDescription == priceBand.description }?.price}" costPrice="${productPrice.value[0].costPrice ?: BigDecimal.ZERO}" quantity="${BigDecimal.ONE}" /></label>
                    </div>
                </div>
            </div>
        </g:each>
    </div>
</g:each>