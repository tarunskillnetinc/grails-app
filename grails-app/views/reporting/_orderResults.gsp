<div class="row mt-5 pb-2 ml-0 mr-0 table-wl bottom-border">
    <g:if test="${!userColumns || userColumns?.columns?.find { it.column == "sku" }?.enabled}">
        <div class="col-2 font-weight-bold"><a href="#" onclick="getReportData({ max: ${sortParams?.max}, offset: ${sortParams?.offset}, sortColumn: 'sku', sortOrder: ${sortParams?.sortColumn == 'sku' ? sortParams?.sortOrder == 'asc' ? '\'desc\'' : '\'asc\'' : '\'asc\''} });">Product SKU</a></div>
    </g:if>
    <g:if test="${!userColumns || userColumns?.columns?.find { it.column == "description" }?.enabled}">
        <div class="col-4 font-weight-bold"><a href="#" onclick="getReportData({ max: ${sortParams?.max}, offset: ${sortParams?.offset}, sortColumn: 'description', sortOrder: ${sortParams?.sortColumn == 'description' ? sortParams?.sortOrder == 'asc' ? '\'desc\'' : '\'asc\'' : '\'asc\''} });">Description</a></div>
    </g:if>
    <g:if test="${!userColumns || userColumns?.columns?.find { it.column == "orderedQuantity" }?.enabled}">
        <div class="col-2 font-weight-bold"><a href="#" onclick="getReportData({ max: ${sortParams?.max}, offset: ${sortParams?.offset}, sortColumn: 'orderedQuantity', sortOrder: ${sortParams?.sortColumn == 'orderedQuantity' ? sortParams?.sortOrder == 'asc' ? '\'desc\'' : '\'asc\'' : '\'asc\''} });">Ordered Quantity</a></div>
    </g:if>
    <g:if test="${!userColumns || userColumns?.columns?.find { it.column == "packQuantity" }?.enabled}">
        <div class="col-2 font-weight-bold"><a href="#" onclick="getReportData({ max: ${sortParams?.max}, offset: ${sortParams?.offset}, sortColumn: 'packQuantity', sortOrder: ${sortParams?.sortColumn == 'packQuantity' ? sortParams?.sortOrder == 'asc' ? '\'desc\'' : '\'asc\'' : '\'asc\''} });">Pack Quantity</a></div>
    </g:if>
    <g:if test="${!userColumns || userColumns?.columns?.find { it.column == "lineValue" }?.enabled}">
        <div class="col-2 font-weight-bold"><a href="#" onclick="getReportData({ max: ${sortParams?.max}, offset: ${sortParams?.offset}, sortColumn: 'lineValue', sortOrder: ${sortParams?.sortColumn == 'lineValue' ? sortParams?.sortOrder == 'asc' ? '\'desc\'' : '\'asc\'' : '\'asc\''} });">Line Value</a></div>
    </g:if>
</div>

<div class="d-flex justify-content-center">
    <div id="loading-indicator" class="spinner-border" role="status" style="display: none;">
        <span class="sr-only">Loading...</span>
    </div>
</div>

<div id="search-results">
    <g:if test="${!orders || orders?.size() == 0}">
        <div id="noResultsRow" class="col pt-2 pb-2 text-center my-auto wl-striped0">No results found.</div>
    </g:if>

    <g:each in="${orders}" var="order" status="i">
        <div class="row ml-0 mr-0 pt-2 pb-2 wl-striped${i%2}">
            <g:if test="${!userColumns || userColumns?.columns?.find { it.column == "sku" }?.enabled}">
                <div class="col-2 my-auto">${order.productListItem?.productVariant?.sku}</div>
            </g:if>
            <g:if test="${!userColumns || userColumns?.columns?.find { it.column == "description" }?.enabled}">
                <div class="col-4 my-auto">${order.productListItem?.productVariant?.product?.description}</div>
            </g:if>
            <g:if test="${!userColumns || userColumns?.columns?.find { it.column == "orderedQuantity" }?.enabled}">
                <div class="col-2 my-auto">${order.pack?.quantity?.multiply(order.quantity)}</div>
            </g:if>
            <g:if test="${!userColumns || userColumns?.columns?.find { it.column == "packQuantity" }?.enabled}">
                <div class="col-2 my-auto">${order.pack?.quantity}</div>
            </g:if>
            <g:if test="${!userColumns || userColumns?.columns?.find { it.column == "lineValue" }?.enabled}">
                <div class="col-2 my-auto">${order.pack?.price?.multiply(order.quantity)}</div>
            </g:if>
        </div>
    </g:each>
</div>

<g:if test="${totalResults > 0}">
    <div class="my-3 text-right">
        <div>Displaying ${sortParams?.offset ? sortParams?.offset + 1 : 1} - ${((sortParams?.offset ?: 0) + (orders?.size() ?: 0))} of ${totalResults} result${totalResults > 1 ? 's' : ''}</div>
        <div class="mt-3"><g:paginateReport totalResults="${totalResults}" offset="${sortParams?.offset}" max="${sortParams?.max}" sortColumn="${sortParams?.sortColumn}" sortOrder="${sortParams?.sortOrder}" /></div>
    </div>
</g:if>