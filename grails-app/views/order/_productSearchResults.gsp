<div class="row mt-5 pb-2 ml-0 mr-0 table-wl bottom-border">
    <div class="col-2 font-weight-bold">Item Code</div>
    <div class="col-7 font-weight-bold">Description</div>
    <div class="col-3 font-weight-bold">Category</div>
</div>

<div class="d-flex justify-content-center">
    <div id="product-loading-indicator" class="spinner-border" role="status" style="display: none;">
        <span class="sr-only">Loading...</span>
    </div>
</div>

<div id="product-search-results" style="max-height: 400px; overflow-x: auto; overflow-y: auto;">
    <g:if test="${products == null}">
        <div class="row ml-0 mr-0 text-center">
            <div class="col pt-2 pb-2 text-center my-auto wl-striped0">Please enter a search term.</div>
        </div>
    </g:if>

    <g:if test="${products?.size() == 0}">
        <div class="row ml-0 mr-0 text-center">
            <div id="noResultsRow" class="col pt-2 pb-2 text-center my-auto wl-striped0">No results found.</div>
        </div>
    </g:if>

    <g:each in="${products}" var="product" status="i">
        <div class="row ml-0 mr-0 pt-2 pb-2 wl-striped${i%2} hoverable" style="cursor: pointer;" onclick="selectVariant(${product?.id});">
            <div id="product-result-${i+1}-item-code" class="col-2 text-truncate">${product?.itemCode}</div>
            <div id="product-result-${i+1}-description" class="col-7">${product?.description}</div>
            <div id="product-result-${i+1}-category" class="col-3">${product?.category?.description}</div>
        </div>
    </g:each>
</div>

<div class="my-3 text-right">
    <util:remotePaginate action="ajaxSearchProducts" total="${totalResults ?: 0}" update="product-search-results-container" offset="${offset ?: 0}" max="${max ?: 25}" params="[searchTerm: searchTerm, searchBy: searchBy]" />
</div>