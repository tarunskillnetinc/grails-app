<div class="row mt-5 pb-2 ml-0 mr-0 table-wl bottom-border">
    <g:if test="${!userColumns || userColumns?.columns?.find { it.column == "deliveryId" }?.enabled}">
        <div class="col-1 font-weight-bold"><a href="#" onclick="getReportData({
            max: ${sortParams?.max},
            offset: ${sortParams?.offset},
            sortColumn: 'deliveryId',
            sortOrder: ${sortParams?.sortColumn == 'deliveryId' ? sortParams?.sortOrder == 'asc' ? '\'desc\'' : '\'asc\'' : '\'asc\''}
        });">Delivery ID</a></div>
    </g:if>
    <g:if test="${!userColumns || userColumns?.columns?.find { it.column == "storeId" }?.enabled}">
        <div class="col-1 font-weight-bold"><a href="#" onclick="getReportData({
            max: ${sortParams?.max},
            offset: ${sortParams?.offset},
            sortColumn: 'storeId',
            sortOrder: ${sortParams?.sortColumn == 'storeId' ? sortParams?.sortOrder == 'asc' ? '\'desc\'' : '\'asc\'' : '\'asc\''}
        });">Store</a></div>
    </g:if>
    <g:if test="${!userColumns || userColumns?.columns?.find { it.column == "status" }?.enabled}">
        <div class="col-2 font-weight-bold"><a href="#" onclick="getReportData({
            max: ${sortParams?.max},
            offset: ${sortParams?.offset},
            sortColumn: 'status',
            sortOrder: ${sortParams?.sortColumn == 'status' ? sortParams?.sortOrder == 'asc' ? '\'desc\'' : '\'asc\'' : '\'asc\''}
        });">Status</a></div>
    </g:if>
    <g:if test="${!userColumns || userColumns?.columns?.find { it.column == "deliveryDate" }?.enabled}">
        <div class="col-2 font-weight-bold"><a href="#" onclick="getReportData({
            max: ${sortParams?.max},
            offset: ${sortParams?.offset},
            sortColumn: 'deliveryDate',
            sortOrder: ${sortParams?.sortColumn == 'deliveryDate' ? sortParams?.sortOrder == 'asc' ? '\'desc\'' : '\'asc\'' : '\'asc\''}
        });">Delivery Date</a></div>
    </g:if>
    <g:if test="${!userColumns || userColumns?.columns?.find { it.column == "supplierName" }?.enabled}">
        <div class="col-2 font-weight-bold"><a href="#" onclick="getReportData({
            max: ${sortParams?.max},
            offset: ${sortParams?.offset},
            sortColumn: 'supplierName',
            sortOrder: ${sortParams?.sortColumn == 'supplierName' ? sortParams?.sortOrder == 'asc' ? '\'desc\'' : '\'asc\'' : '\'asc\''}
        });">Supplier Name</a></div>
    </g:if>
    <g:if test="${!userColumns || userColumns?.columns?.find { it.column == "numberOfItems" }?.enabled}">
        <div class="col-2 font-weight-bold"><a href="#" onclick="getReportData({
            max: ${sortParams?.max},
            offset: ${sortParams?.offset},
            sortColumn: 'numberOfItems',
            sortOrder: ${sortParams?.sortColumn == 'numberOfItems' ? sortParams?.sortOrder == 'asc' ? '\'desc\'' : '\'asc\'' : '\'asc\''}
        });">Quantity</a></div>
    </g:if>
    <g:if test="${!userColumns || userColumns?.columns?.find { it.column == "totalCost" }?.enabled}">
        <div class="col-2 font-weight-bold"><a href="#" onclick="getReportData({
            max: ${sortParams?.max},
            offset: ${sortParams?.offset},
            sortColumn: 'totalCost',
            sortOrder: ${sortParams?.sortColumn == 'totalCost' ? sortParams?.sortOrder == 'asc' ? '\'desc\'' : '\'asc\'' : '\'asc\''}
        });">Cost of Delivery</a></div>
    </g:if>
</div>

<div class="d-flex justify-content-center">
    <div id="loading-indicator" class="spinner-border" role="status" style="display: none;">
        <span class="sr-only">Loading...</span>
    </div>
</div>

<div id="search-results">
    <g:if test="${!deliveries || deliveries?.size() == 0}">
        <div id="noResultsRow" class="col pt-2 pb-2 text-center my-auto wl-striped0">No results found.</div>
    </g:if>

    <g:each in="${deliveries}" var="delivery" status="i">
        <div class="row ml-0 mr-0 pt-2 pb-2 wl-striped${i % 2} hoverable" style="cursor: pointer;"
             onclick="document.location.href = '${createLink(action:'delivery', params: [productListId: delivery.id, startDate: startDate?.toString("dd/MM/yyyy"), endDate: endDate?.toString("dd/MM/yyyy")])}';">
            <g:if test="${!userColumns || userColumns?.columns?.find { it.column == "deliveryId" }?.enabled}">
                <div class="col-1 my-auto">${delivery?.orderId}</div>
            </g:if>
            <g:if test="${!userColumns || userColumns?.columns?.find { it.column == "storeId" }?.enabled}">
                <div class="col-1 my-auto">${delivery?.storeId}</div>
            </g:if>
            <g:if test="${!userColumns || userColumns?.columns?.find { it.column == "status" }?.enabled}">
                <div class="col-2 my-auto"><g:message code="DeliveryStatus.${delivery?.status}"/></div>
            </g:if>
            <g:if test="${!userColumns || userColumns?.columns?.find { it.column == "deliveryDate" }?.enabled}">
                <div class="col-2 my-auto">
                    <g:if test="${delivery?.startDate}">
                        <g:formatDate format="dd/MM/yyyy" date="${delivery?.startDate?.toDate() ?: new Date()}"/>
                    </g:if>
                    <g:else>&nbsp;</g:else>
                </div>
            </g:if>
            <g:if test="${!userColumns || userColumns?.columns?.find { it.column == "supplierName" }?.enabled}">
                <div class="col-2 my-auto">${delivery?.supplierReference}</div>
            </g:if>
            <g:if test="${!userColumns || userColumns?.columns?.find { it.column == "numberOfItems" }?.enabled}">
                <div class="col-2 my-auto">${delivery?.totalQuantity}</div>
            </g:if>
            <g:if test="${!userColumns || userColumns?.columns?.find { it.column == "totalCost" }?.enabled}">
                <div class="col-2 my-auto"><g:formatNumber number="${delivery?.totalCost}" type="currency"/></div>
            </g:if>
        </div>
    </g:each>
</div>

<g:if test="${totalResults > 0}">
    <div class="my-3 text-right">
        <div>Displaying ${sortParams?.offset ? sortParams?.offset + 1 : 1} - ${((sortParams?.offset ?: 0) + (deliveries?.size() ?: 0))} of ${totalResults} result${totalResults > 1 ? 's' : ''}</div>

        <div class="mt-3"><g:paginateReport totalResults="${totalResults}" offset="${sortParams?.offset}"
                                            max="${sortParams?.max}" sortColumn="${sortParams?.sortColumn}"
                                            sortOrder="${sortParams?.sortOrder}"/></div>
    </div>
</g:if>