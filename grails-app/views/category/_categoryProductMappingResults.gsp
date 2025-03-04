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
    <div class="col-4 font-weight-bold">Product ID</div>
    <div class="col-2 font-weight-bold">Description</div>
</div>

<div class="d-flex justify-content-center">
    <div id="loading-indicator" class="spinner-border" role="status" style="display: none;">
        <span class="sr-only">Loading...</span>
    </div>
</div>

<div id="search-results">
    <g:if test="${products?.size() == 0}">
        <div class="px-0 text-center">
            <div id="noResultsRow" class="pt-2 pb-2 text-center my-auto wl-striped0">No products currently mapped.</div>
        </div>
    </g:if>
    
    <g:each in="${products}" var="product" status="i">
        <div id="product-category-result-${i+1}" class="row ml-0 mr-0 px-0 pt-2 pb-2 wl-striped${i%2}" >
            <div id="product-category-result-${i+1}-productId" class="col-4">${product?.itemCode}</div>
            <div id="product-category-result-${i+1}-description" class="col-2">${product?.description}</div>
        </div>
    </g:each>
</div>

<div class="my-3 text-right">
    <util:remotePaginate action="ajaxCategoryProductMapping" total="${totalResults ?: 0}" update="results-container" offset="${offset ?: 0}" max="${max ?: 50}" params="[id: category?.id]" />
</div>