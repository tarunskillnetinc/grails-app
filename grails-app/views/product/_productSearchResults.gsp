<g:if test="${!products || products?.size() == 0}">
    <div class="row ml-0 mr-0 text-center">
        <div id="noResultsRow" class="col pt-2 pb-2 text-center my-auto wl-striped0">No results found.</div>
    </div>
</g:if>

<g:each in="${products}" var="product" status="i">
    <div class="row ml-0 mr-0 pt-2 pb-2 wl-striped${i%2} hoverable" title="Click to select." style="cursor: pointer;" onclick="productSelected(${product.variants?.sort { it.storeId }?.reverse()?.findAll { it.storeId == null || it.storeId == storeId }?.first()?.id}, '${product.variants?.sort { it.storeId }?.reverse()?.findAll { it.storeId == null || it.storeId == storeId }?.first()?.sku}', '${product.description}');" data-dismiss="modal">
        <div class="col-2">${product.itemCode}</div>
        <div class="col-4">${product.description}</div>
        <div class="col-2">${product.category?.description}</div>
        <div class="col-2">£${product.variants?.sort { it.storeId }?.reverse()?.findAll { it.storeId == null || it.storeId == storeId }?.first()?.costPrice ?: '0.00'}</div>
        <div class="col-2">£${product.variants?.sort { it.storeId }?.reverse()?.findAll { it.storeId == null || it.storeId == storeId }?.first()?.currentPrice ?: '0.00'}</div>
    </div>
</g:each>