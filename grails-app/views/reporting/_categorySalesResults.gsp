<div class="row mt-5 pb-2 ml-0 mr-0 table-wl bottom-border">
    <g:if test="${!userColumns || userColumns?.columns?.find { it.column == "description" }?.enabled}">
        <div class="col-4 font-weight-bold">Description</div>
    </g:if>
    <g:if test="${!userColumns || userColumns?.columns?.find { it.column == "quantity" }?.enabled}">
        <div class="col-1 font-weight-bold">Total Qty</div>
    </g:if>
    <g:if test="${!userColumns || userColumns?.columns?.find { it.column == "avgCostPrice" }?.enabled}">
        <div class="col font-weight-bold">Avg Cost Price</div>
    </g:if>
    <g:if test="${!userColumns || userColumns?.columns?.find { it.column == "avgRetailPrice" }?.enabled}">
        <div class="col font-weight-bold">Avg Sales Price</div>
    </g:if>
    <g:if test="${!userColumns || userColumns?.columns?.find { it.column == "retailPrice" }?.enabled}">
        <div class="col font-weight-bold">Total Sales</div>
    </g:if>
    <g:if test="${!userColumns || userColumns?.columns?.find { it.column == "vatAmount" }?.enabled}">
        <div class="col font-weight-bold">VAT Amount</div>
    </g:if>
    <g:if test="${!userColumns || userColumns?.columns?.find { it.column == "avgMargin" }?.enabled}">
        <div class="col font-weight-bold">Avg Margin</div>
    </g:if>
</div>

<div class="d-flex justify-content-center">
    <div id="loading-indicator" class="spinner-border" role="status" style="display: none;">
        <span class="sr-only">Loading...</span>
    </div>
</div>

<div id="search-results">
    <g:if test="${!sales || sales?.size() == 0}">
        <div id="noResultsRow" class="col pt-2 pb-2 text-center my-auto wl-striped0">No results found.</div>
    </g:if>

    <g:each in="${sales}" var="sale" status="i">
        <div class="row ml-0 mr-0 pt-2 pb-2 wl-striped${i%2} hoverable">

        <g:if test="${!userColumns || userColumns?.columns?.find { it.column == "description" }?.enabled}">
            <div class="col-4" style="<g:categorySalesIndent categoryLevel='${sale.salesCategories.first().categoryLevel}' />">${sale.productDescription}</div>
        </g:if>
        <g:if test="${!userColumns || userColumns?.columns?.find { it.column == "quantity" }?.enabled}">
            <div class="col-1 my-auto">${sale.quantity + sale.refundQuantity} (${sale.refundQuantity} refunds)</div>
        </g:if>
        <g:if test="${!userColumns || userColumns?.columns?.find { it.column == "avgCostPrice" }?.enabled}">
            <div class="col my-auto">&pound;${sale.avgCostPrice}</div>
        </g:if>
        <g:if test="${!userColumns || userColumns?.columns?.find { it.column == "avgRetailPrice" }?.enabled}">
            <div class="col my-auto">&pound;${sale.avgRetailPrice}</div>
        </g:if>
        <g:if test="${!userColumns || userColumns?.columns?.find { it.column == "retailPrice" }?.enabled}">
            <div class="col my-auto">&pound;${sale.retailPrice}</div>
        </g:if>
        <g:if test="${!userColumns || userColumns?.columns?.find { it.column == "vatAmount" }?.enabled}">
            <div class="col my-auto">&pound;${sale.vatAmount}</div>
        </g:if>
        <g:if test="${!userColumns || userColumns?.columns?.find { it.column == "avgMargin" }?.enabled}">
            <div class="col my-auto">${sale.avgMargin}&#37;</div>
        </g:if>
        </div><!-- This </div> matches the one in either the IF or ELSE blocks at the top of the each -->
    </g:each>
</div>

<g:if test="${totalResults > 0}">
    <div class="my-3 text-right">
        <div>Displaying ${sortParams?.offset ? sortParams?.offset + 1 : 1} - ${((sortParams?.offset ?: 0) + (sales?.size() ?: 0))} of ${totalResults} result${totalResults > 1 ? 's' : ''}</div>
        <div class="mt-3"><g:paginateReport totalResults="${totalResults}" offset="${sortParams?.offset}" max="${sortParams?.max}" sortColumn="${sortParams?.sortColumn}" sortOrder="${sortParams?.sortOrder}" /></div>
    </div>
</g:if>