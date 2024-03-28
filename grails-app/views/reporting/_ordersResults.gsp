<div class="row mt-5 pb-2 ml-0 mr-0 table-wl bottom-border">
    <g:if test="${!userColumns || userColumns?.columns?.find { it.column == "orderId" }?.enabled}">
        <div class="col-1 font-weight-bold"><a id="order-id" href="#" onclick="getReportData({ max: ${sortParams?.max}, offset: ${sortParams?.offset}, sortColumn: 'orderId', sortOrder: ${sortParams?.sortColumn == 'orderId' ? sortParams?.sortOrder == 'asc' ? '\'desc\'' : '\'asc\'' : '\'asc\''} });">Order ID</a></div>
    </g:if>
    <g:if test="${!userColumns || userColumns?.columns?.find { it.column == "storeId" }?.enabled}">
        <div class="col-1 font-weight-bold"><a id="store-id" href="#" onclick="getReportData({ max: ${sortParams?.max}, offset: ${sortParams?.offset}, sortColumn: 'storeId', sortOrder: ${sortParams?.sortColumn == 'storeId' ? sortParams?.sortOrder == 'asc' ? '\'desc\'' : '\'asc\'' : '\'asc\''} });">Store</a></div>
    </g:if>
    <g:if test="${!userColumns || userColumns?.columns?.find { it.column == "status" }?.enabled}">
        <div class="col-2 font-weight-bold"><a id="status" href="#" onclick="getReportData({ max: ${sortParams?.max}, offset: ${sortParams?.offset}, sortColumn: 'status', sortOrder: ${sortParams?.sortColumn == 'status' ? sortParams?.sortOrder == 'asc' ? '\'desc\'' : '\'asc\'' : '\'asc\''} });">Status</a></div>
    </g:if>
    <g:if test="${!userColumns || userColumns?.columns?.find { it.column == "dateCompleted" }?.enabled}">
        <div class="col-2 font-weight-bold"><a id="date-completed" href="#" onclick="getReportData({ max: ${sortParams?.max}, offset: ${sortParams?.offset}, sortColumn: 'dateCompleted', sortOrder: ${sortParams?.sortColumn == 'dateCompleted' ? sortParams?.sortOrder == 'asc' ? '\'desc\'' : '\'asc\'' : '\'asc\''} });">Date Completed</a></div>
    </g:if>
    <g:if test="${!userColumns || userColumns?.columns?.find { it.column == "supplierName" }?.enabled}">
        <div class="col-2 font-weight-bold"><a id="supplier-name" href="#" onclick="getReportData({ max: ${sortParams?.max}, offset: ${sortParams?.offset}, sortColumn: 'supplierName', sortOrder: ${sortParams?.sortColumn == 'supplierName' ? sortParams?.sortOrder == 'asc' ? '\'desc\'' : '\'asc\'' : '\'asc\''} });">Supplier Reference</a></div>
    </g:if>
    <g:if test="${!userColumns || userColumns?.columns?.find { it.column == "numberOfItems" }?.enabled}">
        <div class="col-2 font-weight-bold"><a id="quantity" href="#" onclick="getReportData({ max: ${sortParams?.max}, offset: ${sortParams?.offset}, sortColumn: 'numberOfItems', sortOrder: ${sortParams?.sortColumn == 'numberOfItems' ? sortParams?.sortOrder == 'asc' ? '\'desc\'' : '\'asc\'' : '\'asc\''} });">Quantity</a></div>
    </g:if>
    <g:if test="${!userColumns || userColumns?.columns?.find { it.column == "value" }?.enabled}">
        <div class="col-2 font-weight-bold"><a id="value" href="#" onclick="getReportData({ max: ${sortParams?.max}, offset: ${sortParams?.offset}, sortColumn: 'value', sortOrder: ${sortParams?.sortColumn == 'value' ? sortParams?.sortOrder == 'asc' ? '\'desc\'' : '\'asc\'' : '\'asc\''} });">Value of Order</a></div>
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
        <div class="row ml-0 mr-0 pt-2 pb-2 wl-striped${i%2} hoverable" style="cursor: pointer;" onclick="document.location.href='${createLink(action:'order', params: [productListId: order.id, startDate: startDate?.toString("dd/MM/yyyy"), endDate: endDate?.toString("dd/MM/yyyy")])}';">
            <g:if test="${!userColumns || userColumns?.columns?.find { it.column == "orderId" }?.enabled}">
                <div id="order-id-${i + 1}" class="col-1 my-auto">${order.orderId}</div>
            </g:if>
            <g:if test="${!userColumns || userColumns?.columns?.find { it.column == "storeId" }?.enabled}">
                <div id="store-id-${i + 1}" class="col-1 my-auto">${order.storeId}</div>
            </g:if>
            <g:if test="${!userColumns || userColumns?.columns?.find { it.column == "status" }?.enabled}">
                <div id="status-${i + 1}" class="col-2 my-auto"><g:message code="OrderStatus.${order.status}" /></div>
            </g:if>
            <g:if test="${!userColumns || userColumns?.columns?.find { it.column == "dateCompleted" }?.enabled}">
                <div id="date-completed-${i + 1}" class="col-2 my-auto">${order.dateCompleted?.toString("dd/MM/yyyy HH:mm:ss")}</div>
            </g:if>
            <g:if test="${!userColumns || userColumns?.columns?.find { it.column == "supplierName" }?.enabled}">
                <div id="supplier-name-${i + 1}" class="col-2 my-auto">${order.supplierReference}</div>
            </g:if>
            <g:if test="${!userColumns || userColumns?.columns?.find { it.column == "numberOfItems" }?.enabled}">
                <div id="quantity-${i + 1}" class="col-2 my-auto">${order.totalQuantity}</div>
            </g:if>
            <g:if test="${!userColumns || userColumns?.columns?.find { it.column == "value" }?.enabled}">
                <div id="value-${i + 1}"class="col-2 my-auto"><g:formatNumber number="${order.totalValue}" type="currency"/></div>
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