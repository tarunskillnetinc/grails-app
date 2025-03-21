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
    <g:if test="${!userColumns || userColumns?.columns?.find { it.column == "category" }?.enabled}">
        <div class="col-6 font-weight-bold">Category</div>
    </g:if>
    <g:if test="${!userColumns || userColumns?.columns?.find { it.column == "store" }?.enabled}">
        <div class="col-2 font-weight-bold">Store</div>
    </g:if>
    <g:if test="${!userColumns || userColumns?.columns?.find { it.column == "amendableDate" }?.enabled}">
        <div class="col-2 font-weight-bold">Amendable Date</div>
    </g:if>
</div>

<div class="d-flex justify-content-center">
    <div id="loading-indicator" class="spinner-border" role="status" style="display: none;">
        <span class="sr-only">Loading...</span>
    </div>
</div>

<div id="search-results">
    <g:if test="${orders == null}">
        <div class="px-0 text-center">
            <div class="pt-2 pb-2 text-center my-auto wl-striped0">Please enter a search term.</div>
        </div>
    </g:if>

    <g:if test="${orders?.size() == 0}">
        <div class="px-0 text-center">
            <div id="noResultsRow" class="pt-2 pb-2 text-center my-auto wl-striped0">No results found.</div>
        </div>
    </g:if>

    <g:set var="row" value="${0}" scope="request" />

    <g:render template="orderSearchRows" model="[orders: orders, userColumns: userColumns]"/>
</div>

<div class="my-3 text-right">
    <util:remotePaginate controller="amendableOrder" action="ajaxSearchOrders" total="${totalResults ?: 0}" update="results-container" offset="${offset ?: 0}" max="${max ?: 50}" params="[category: category, storeId: storeId]" />
</div>