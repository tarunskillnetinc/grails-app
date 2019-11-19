<%@ page import="java.util.stream.Collectors" %>

<g:if test="${products == null}">
    <div class="row text-center">
        <div class="col-12">Please enter a search term.</div>
    </div>
</g:if>

<g:if test="${products?.size() == 0}">
    <div class="row text-center">
        <div class="col-12">No results found.</div>
    </div>
</g:if>

<g:each in="${products}" var="product" status="i">
    <div class="row ml-0 mr-0 pt-2 pb-2 wl-striped${i%2}">
        <div class="col-2 my-auto">${product.itemCode}</div>
        <div class="col-6 my-auto">${product.description}</div>
        <div class="col-1 my-auto">${java.text.NumberFormat.currencyInstance.format(product.retailPrice)}</div>
        <div class="col-2 my-auto">${product.category?.description}</div>
        <a href="#" class="col-1 btn btn-wl my-auto" onclick="addPromotionProduct(${product.id}, ${product.itemCode}, '${product.description}')" data-dismiss="modal">Select</a>
    </div>
</g:each>

<div class="my-3 text-right">
    <util:remotePaginate action="productSearch" total="${totalResults ?: 0}" update="search-results" offset="${offset ?: 0}" max="${max ?: 50}" params="[searchTerm: searchTerm, searchBy: searchBy]" />
</div>