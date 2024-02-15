<script type="application/javascript">
    $(document).ready(function() {
        // Assuming your page buttons have a specific class (e.g., "page-button")
        $('.step').on('click', function() {
            // Scroll to the top of the page with a smooth animation
            $('html, body').animate({ scrollTop: 0 }, 'fast');
        });
    });
</script>

<div class="row mt-5 pb-2 table-wl bottom-border"  style="margin-left: 20px; margin-right: 20px">
    <div class="col-3 font-weight-bold">Item Code</div>
    <div class="col-6 font-weight-bold">Description</div>
    <div class="col-3 font-weight-bold">Category</div>
</div>

<div class="d-flex justify-content-center">
    <div id="product-loading-indicator" class="spinner-border" role="status" style="display: none;">
        <span class="sr-only">Loading...</span>
    </div>
</div>

<div id="product-search-results" style="max-height: 400px; margin-left: 20px; margin-right: 20px; overflow-x: auto; overflow-y: auto;">
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
        <div id="product-result-${i+1}" class="row ml-0 mr-0 pt-2 pb-2 wl-striped${i%2} hoverable" style="cursor: pointer; overflow-x: auto; overflow-y: auto;" onclick="selectVariant(${product?.id});">
            <div id="product-result-${i+1}-item-code" class="col-3 text-truncate" style="overflow: hidden;">${product?.itemCode}</div>
            <div id="product-result-${i+1}-description" class="col-6" style="overflow: hidden;">${product?.description}</div>
            <div id="product-result-${i+1}-category" class="col-3" style="overflow: hidden;">${product?.category?.description}</div>
        </div>
    </g:each>
</div>

<div class="my-3 text-right" style="margin-right: 20px">
    <util:remotePaginate action="ajaxSearchProducts" total="${totalResults ?: 0}" update="product-search-results-container" offset="${offset ?: 0}" max="${max ?: 9}" params="[searchTerm: searchTerm, searchBy: searchBy]" />
</div>