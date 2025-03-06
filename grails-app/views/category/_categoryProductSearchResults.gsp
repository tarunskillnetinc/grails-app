<script type="application/javascript">
    $(document).ready(function() {
        // Assuming your page buttons have a specific class (e.g., "page-button")
        $('.step, .nextLink, .prevLink').on('click', function() {
            // Scroll to the top of the page with a smooth animation
            $('html, body').animate({ scrollTop: 0 }, 'fast');
        });
    });
</script>

<div class="row mt-5 pb-2 ml-0 mr-0 table-wl bottom-border">
    <div class="col-3 font-weight-bold">Product ID</div>
    <div class="col-7 font-weight-bold">Description</div>
    <div class="col-2 font-weight-bold" style="display:flex; justify-content: center; align-items: center;">Select Product</div>
</div>

<div class="d-flex justify-content-center">
    <div id="loading-indicator" class="spinner-border" role="status" style="display: none;">
        <span class="sr-only">Loading...</span>
    </div>
</div>

<div id="search-results">

    <g:if test="${products == null}">
        <div class="row ml-0 mr-0 text-center">
            <div class="col pt-2 pb-2 text-center my-auto wl-striped0">Please enter a search term.</div>
        </div>
    </g:if>
    
    <g:if test="${products?.size() == 0}">
        <div class="px-0 text-center">
            <div id="noResultsRow" class="pt-2 pb-2 text-center my-auto wl-striped0">No results found.</div>
        </div>
    </g:if>

    <g:each in="${products}" var="product" status="i">
        <div id="product-result-${i+1}" class="row ml-0 mr-0 px-0 pt-2 pb-2 wl-striped${i%2}">
            <div id="product-result-${i+1}-productId" class="col-3">${product?.itemCode}</div>
            <div id="product-result-${i+1}-description" class="col-7">${product?.description}</div>
            <div id="product-result-${i+1}-select" class="col-2" style="display:flex; justify-content: center; align-items: center;">
                <g:checkBox name="select" class="form-check-input" value="${session.CATEGORY_PRODUCT_LIST.contains(product?.id)}" onclick="toggleProductSelection(${product?.id})" disabled="${category?.id == product?.category.id}"/>
            </div>
        </div>
    </g:each>    
</div>

<div class="my-3 text-right">
    <util:remotePaginate action="ajaxCategoryProductSearch" total="${totalResults ?: 0}" update="results-container" offset="${offset ?: 0}" max="${max ?: 50}" params="[searchTerm: searchTerm, searchBy: searchBy, categoryId: category?.id]" />
</div>