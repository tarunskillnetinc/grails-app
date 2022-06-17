<div class="row mt-5 pb-2 ml-0 mr-0 table-wl bottom-border">
    <g:if test="${!userColumns || userColumns?.columns?.find { it.column == "description" }?.enabled}">
        <div class="col-4 font-weight-bold"><a href="#" onclick="getReportData({ max: ${sortParams?.max}, offset: ${sortParams?.offset}, sortColumn: 'description', sortOrder: ${sortParams?.sortColumn == 'description' ? sortParams?.sortOrder == 'asc' ? '\'desc\'' : '\'asc\'' : '\'asc\''} });">Description</a></div>
    </g:if>
    <g:if test="${!userColumns || userColumns?.columns?.find { it.column == "quantity" }?.enabled}">
        <div class="col-1 font-weight-bold"><a href="#" onclick="getReportData({ max: ${sortParams?.max}, offset: ${sortParams?.offset}, sortColumn: 'quantity', sortOrder: ${sortParams?.sortColumn == 'quantity' ? sortParams?.sortOrder == 'asc' ? '\'desc\'' : '\'asc\'' : '\'asc\''} });">Total Qty</a></div>
    </g:if>
    <g:if test="${!userColumns || userColumns?.columns?.find { it.column == "avgCostPrice" }?.enabled}">
        <div class="col font-weight-bold"><a href="#" onclick="getReportData({ max: ${sortParams?.max}, offset: ${sortParams?.offset}, sortColumn: 'avgCostPrice', sortOrder: ${sortParams?.sortColumn == 'avgCostPrice' ? sortParams?.sortOrder == 'asc' ? '\'desc\'' : '\'asc\'' : '\'asc\''} });">Avg Cost Price</a></div>
    </g:if>
    <g:if test="${!userColumns || userColumns?.columns?.find { it.column == "avgRetailPrice" }?.enabled}">
        <div class="col font-weight-bold"><a href="#" onclick="getReportData({ max: ${sortParams?.max}, offset: ${sortParams?.offset}, sortColumn: 'avgRetailPrice', sortOrder: ${sortParams?.sortColumn == 'avgRetailPrice' ? sortParams?.sortOrder == 'asc' ? '\'desc\'' : '\'asc\'' : '\'asc\''} });">Avg Sales Price</a></div>
    </g:if>
    <g:if test="${!userColumns || userColumns?.columns?.find { it.column == "retailPrice" }?.enabled}">
        <div class="col font-weight-bold"><a href="#" onclick="getReportData({ max: ${sortParams?.max}, offset: ${sortParams?.offset}, sortColumn: 'retailPrice', sortOrder: ${sortParams?.sortColumn == 'retailPrice' ? sortParams?.sortOrder == 'asc' ? '\'desc\'' : '\'asc\'' : '\'asc\''} });">Total Sales</a></div>
    </g:if>
    <g:if test="${!userColumns || userColumns?.columns?.find { it.column == "vatAmount" }?.enabled}">
        <div class="col font-weight-bold"><a href="#" onclick="getReportData({ max: ${sortParams?.max}, offset: ${sortParams?.offset}, sortColumn: 'vatAmount', sortOrder: ${sortParams?.sortColumn == 'vatAmount' ? sortParams?.sortOrder == 'asc' ? '\'desc\'' : '\'asc\'' : '\'asc\''} });">VAT Amount</a></div>
    </g:if>
    <g:if test="${!userColumns || userColumns?.columns?.find { it.column == "avgMargin" }?.enabled}">
        <div class="col font-weight-bold"><a href="#" onclick="getReportData({ max: ${sortParams?.max}, offset: ${sortParams?.offset}, sortColumn: 'avgMargin', sortOrder: ${sortParams?.sortColumn == 'avgMargin' ? sortParams?.sortOrder == 'asc' ? '\'desc\'' : '\'asc\'' : '\'asc\''} });">Avg Margin</a></div>
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
                <g:if test="${sale.productItemCode}">
                    <div class="col-4">${sale.productItemCode} - ${sale.productDescription} - ${sale.productUnitSize}</div>
                </g:if>
                <g:else>
                    <div class="col-4">${sale.productDescription}</div>
                </g:else>
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
        </div>
    </g:each>
</div>

<g:if test="${totalResults > 0}">
    <div class="my-3 text-right">
        <div>Displaying ${sortParams?.offset ? sortParams?.offset + 1 : 1} - ${((sortParams?.offset ?: 0) + (sales?.size() ?: 0))} of ${totalResults} result${totalResults > 1 ? 's' : ''}</div>
        <div class="mt-3"><g:paginateReport totalResults="${totalResults}" offset="${sortParams?.offset}" max="${sortParams?.max}" sortColumn="${sortParams?.sortColumn}" sortOrder="${sortParams?.sortOrder}" /></div>
    </div>
</g:if>