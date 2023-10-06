<%@ page import="groovy.json.StringEscapeUtils" %>
<g:if test="${!products || products?.size() == 0}">
    <div class="row ml-0 mr-0 text-center">
        <div id="noResultsRow" class="col pt-2 pb-2 text-center my-auto wl-striped0">No results found.</div>
    </div>
</g:if>

<g:each in="${products}" var="product" status="i">
    <div id="product-result-${i+1}" class="row ml-0 mr-0 pt-2 pb-2 wl-striped${i%2} hoverable" title="Click to select." style="cursor: pointer;" onclick="productSelected(${product.variants?.sort { it.storeId }?.reverse()?.find { it.storeId == null || it.storeId == storeId }?.id}, '${product.variants?.sort { it.storeId }?.reverse()?.find { it.storeId == null || it.storeId == storeId }?.sku}', `${ groovy.json.StringEscapeUtils.escapeJavaScript(product.description)}`);" data-dismiss="modal">
        <div id="product-result-${i+1}-item-code" class="col-2 text-truncate">${product.itemCode}</div>
        <div id="product-result-${i+1}-description" class="col-4">${product.description}</div>
        <div id="product-result-${i+1}-category" class="col-2">${product.category?.description}</div>
        <div id="product-result-${i+1}-cost-price" class="col-2">£${product.costPrice}</div>
        <div id="product-result-${i+1}-retail-price" class="col-2">£${product.retailPrice}</div>
    </div>
</g:each>