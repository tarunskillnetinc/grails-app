<g:if test="${!products || products?.size() == 0}">
    <tr id="noResultsRow"><td colspan="7" style="text-align: center;">No results found.</td></tr>
</g:if>

<g:each in="${products}" var="product">
    <tr>
        <td scope="row">${product.itemCode}</td>
        <td>${product.description}</td>
        <td>${product.category?.description}</td>
        <td>£${product.productDatas?.size() > 0 ? (product.productDatas?.get(0)?.costPrice ?: '0.00') : '0.00'}</td>
        <td>£${product.productDatas?.size() > 0 ? (product.productDatas?.get(0)?.retailPrice ?: '0'00') : '0.00'}</td>
        <td>${product.vatCode?.percentage}%</td>
        <td><button class="btn btn-wl" onClick="productSelected(${product.id}, '${product.itemCode}', '${product.description}');" data-dismiss="modal">Select</button></td>
    </tr>
</g:each>