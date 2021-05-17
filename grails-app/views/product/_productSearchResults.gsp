<g:if test="${!products || products?.size() == 0}">
    <tr id="noResultsRow" class="wl-striped0"><td colspan="7" style="text-align: center;">No results found.</td></tr>
</g:if>

<g:each in="${products}" var="product">
    <tr>
        <td scope="row">${product.itemCode}</td>
        <td>${product.description}</td>
        <td>${product.category?.description}</td>
        <td>£${product?.costPrice ?: '0.00'}</td>
        <td>£${product?.retailPrice ?: '0.00'}</td>
        <td>${product.vatCode?.percentage}%</td>
        <td><button class="btn btn-wl" onClick="productSelected(${product.variants?.findAll{it.storeId == storeId}?.first()?.id}, '${product.variants?.findAll{it.storeId == storeId}?.first()?.itemCode}', '${product.description}');" data-dismiss="modal">Select</button></td>
    </tr>
</g:each>