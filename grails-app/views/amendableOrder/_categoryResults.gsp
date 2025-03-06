<script type="application/javascript">
    $(document).ready(function() {
        // Assuming your page buttons have a specific class (e.g., "page-button")
        $('.step, .nextLink, .prevLink').on('click', function() {
            // Scroll to the top of the page with a smooth animation
            $('html, body').animate({ scrollTop: 0 }, 'fast');
        });
    });
</script>
<g:form method="post" action="save" class="mt-5" name="saveAmendedOrderForm">
    <div class="row mt-5 pb-2 ml-0 mr-0 table-wl bottom-border">
        <g:if test="${!userColumns || userColumns?.columns?.find { it.column == "sku" }?.enabled}">
            <div class="col-2 font-weight-bold">Line Number</div>
        </g:if>
        <g:if test="${!userColumns || userColumns?.columns?.find { it.column == "productDescription" }?.enabled}">
            <div class="col-1 font-weight-bold">Description</div>
        </g:if>
        <g:if test="${!userColumns || userColumns?.columns?.find { it.column == "price" }?.enabled}">
            <div class="col-1 font-weight-bold">Price</div>
        </g:if>
        <g:if test="${!userColumns || userColumns?.columns?.find { it.column == "caseSize" }?.enabled}">
            <div class="col-1 font-weight-bold">Case Size</div>
        </g:if>
        <g:if test="${!userColumns || userColumns?.columns?.find { it.column == "deliveryDate" }?.enabled}">
            <div class="col-1 font-weight-bold">Delivery Date</div>
        </g:if>
        <g:if test="${!userColumns || userColumns?.columns?.find { it.column == "demand" }?.enabled}">
            <div class="col-1 font-weight-bold">Demand</div>
        </g:if>
        <g:if test="${!userColumns || userColumns?.columns?.find { it.column == "available" }?.enabled}">
            <div class="col-1 font-weight-bold">Available</div>
        </g:if>
        <g:if test="${!userColumns || userColumns?.columns?.find { it.column == "orderQuantity" }?.enabled}">
            <div class="col-1 font-weight-bold">Order Quantity</div>
        </g:if>
        <div class="col-1 font-weight-bold">Amend Qty</div>

    </div>

    <div class="d-flex justify-content-center">
        <div id="loading-indicator" class="spinner-border" role="status" style="display: none;">
            <span class="sr-only">Loading...</span>
        </div>
    </div>

    <div id="search-results">
        <g:if test="${amendedLines?.size() == 0}">
            <div class="px-0 text-center">
                <div id="noResultsRow" class="pt-2 pb-2 text-center my-auto wl-striped0">No results found.</div>
            </div>
        </g:if>

        <g:set var="row" value="${0}" scope="request" />

        <g:render template="categoryRows" model="[amendedLines: amendedLines, userColumns: userColumns, categoryId: categoryId]"/>
    </div>

    <div class="my-3 text-right">
        <util:remotePaginate controller="amendableOrder" action="ajaxViewCategoryOrders" total="${totalResults ?: 0}" update="results-container"
                             offset="${offset ?: 0}" max="${max ?: 50}"
                             params="[categoryId: categoryId, sku:  sku, productDescription:  productDescription, deliveryDate:  deliveryDate, storeId: storeId]" />
    </div>
</g:form>