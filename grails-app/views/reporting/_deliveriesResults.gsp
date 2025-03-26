<div class="row mt-5 pb-2 ml-0 mr-0 table-wl bottom-border">
    <g:if test="${!userColumns || userColumns?.columns?.find { it.column == "deliveryId" }?.enabled}">
        <div class="col-1 font-weight-bold"><a id="delivery-id" href="#" onclick="getReportData({
            max: ${sortParams?.max},
            offset: ${sortParams?.offset},
            sortColumn: 'deliveryId',
            sortOrder: ${sortParams?.sortColumn == 'deliveryId' ? sortParams?.sortOrder == 'asc' ? '\'desc\'' : '\'asc\'' : '\'asc\''}
        });">Delivery ID</a></div>
    </g:if>
    <g:if test="${!userColumns || userColumns?.columns?.find { it.column == "type" }?.enabled}">
        <div class="col-1 font-weight-bold"><a id="type" href="#" onclick="getReportData({
            max: ${sortParams?.max},
            offset: ${sortParams?.offset},
            sortColumn: 'type',
            sortOrder: ${sortParams?.sortColumn == 'type' ? sortParams?.sortOrder == 'asc' ? '\'desc\'' : '\'asc\'' : '\'asc\''}
        });">Type</a></div>
    </g:if>
    <g:if test="${!userColumns || userColumns?.columns?.find { it.column == "storeId" }?.enabled}">
        <div class="col-1 font-weight-bold"><a id="store-id" href="#" onclick="getReportData({
            max: ${sortParams?.max},
            offset: ${sortParams?.offset},
            sortColumn: 'storeId',
            sortOrder: ${sortParams?.sortColumn == 'storeId' ? sortParams?.sortOrder == 'asc' ? '\'desc\'' : '\'asc\'' : '\'asc\''}
        });">${retailer?.config?.retailerTerminologyConfig?.storeTerm} No.</a></div>
    </g:if>
    <g:if test="${!userColumns || userColumns?.columns?.find { it.column == "storeName" }?.enabled}">
        <div class="col-1 font-weight-bold"><a id="store-name" href="#" onclick="getReportData({
            max: ${sortParams?.max},
            offset: ${sortParams?.offset},
            sortColumn: 'storeName',
            sortOrder: ${sortParams?.sortColumn == 'storeName' ? sortParams?.sortOrder == 'asc' ? '\'desc\'' : '\'asc\'' : '\'asc\''}
        });">${retailer?.config?.retailerTerminologyConfig?.storeTerm} Name</a></div>
    </g:if>
    <g:if test="${!userColumns || userColumns?.columns?.find { it.column == "status" }?.enabled}">
        <div class="col-1 font-weight-bold"><a id="status" href="#" onclick="getReportData({
            max: ${sortParams?.max},
            offset: ${sortParams?.offset},
            sortColumn: 'status',
            sortOrder: ${sortParams?.sortColumn == 'status' ? sortParams?.sortOrder == 'asc' ? '\'desc\'' : '\'asc\'' : '\'asc\''}
        });">Status</a></div>
    </g:if>
    <g:if test="${!userColumns || userColumns?.columns?.find { it.column == "deliveryDate" }?.enabled}">
        <div class="col-1 font-weight-bold"><a id="delivery-date" href="#" onclick="getReportData({
            max: ${sortParams?.max},
            offset: ${sortParams?.offset},
            sortColumn: 'deliveryDate',
            sortOrder: ${sortParams?.sortColumn == 'deliveryDate' ? sortParams?.sortOrder == 'asc' ? '\'desc\'' : '\'asc\'' : '\'asc\''}
        });">Delivery Date</a></div>
    </g:if>
    <g:if test="${!userColumns || userColumns?.columns?.find { it.column == "supplierName" }?.enabled}">
        <div class="col-1 font-weight-bold"><a id="supplier-name" href="#" onclick="getReportData({
            max: ${sortParams?.max},
            offset: ${sortParams?.offset},
            sortColumn: 'supplierName',
            sortOrder: ${sortParams?.sortColumn == 'supplierName' ? sortParams?.sortOrder == 'asc' ? '\'desc\'' : '\'asc\'' : '\'asc\''}
        });">Supplier</a></div>
    </g:if>
    <g:if test="${!userColumns || userColumns?.columns?.find { it.column == "supplierRef" }?.enabled}">
        <div class="col-1 font-weight-bold"><a id="supplier-ref" href="#" onclick="getReportData({
            max: ${sortParams?.max},
            offset: ${sortParams?.offset},
            sortColumn: 'supplierRef',
            sortOrder: ${sortParams?.sortColumn == 'supplierRef' ? sortParams?.sortOrder == 'asc' ? '\'desc\'' : '\'asc\'' : '\'asc\''}
        });">Supplier Reference</a></div>
    </g:if>
    <g:if test="${!userColumns || userColumns?.columns?.find { it.column == "shipmentRef" }?.enabled}">
        <div class="col-1 font-weight-bold"><a id="shipment-ref" href="#" onclick="getReportData({
            max: ${sortParams?.max},
            offset: ${sortParams?.offset},
            sortColumn: 'shipmentRef',
            sortOrder: ${sortParams?.sortColumn == 'shipmentRef' ? sortParams?.sortOrder == 'asc' ? '\'desc\'' : '\'asc\'' : '\'asc\''}
        });">Shipment Reference</a></div>
    </g:if>

    <g:if test="${!userColumns || userColumns?.columns?.find { it.column == "numberOfCages" }?.enabled}">
        <div class="col-1 font-weight-bold"><a id="number-of-cages" href="#" onclick="getReportData({
            max: ${sortParams?.max},
            offset: ${sortParams?.offset},
            sortColumn: 'numberOfCages',
            sortOrder: ${sortParams?.sortColumn == 'numberOfCages' ? sortParams?.sortOrder == 'asc' ? '\'desc\'' : '\'asc\'' : '\'asc\''}
        });">Number of Cages</a></div>
    </g:if>

    <g:if test="${!userColumns || userColumns?.columns?.find { it.column == "numberOfPacks" }?.enabled}">
        <div class="col-1 font-weight-bold"><a id="number-of-packs" href="#" onclick="getReportData({
            max: ${sortParams?.max},
            offset: ${sortParams?.offset},
            sortColumn: 'numberOfPacks',
            sortOrder: ${sortParams?.sortColumn == 'numberOfPacks' ? sortParams?.sortOrder == 'asc' ? '\'desc\'' : '\'asc\'' : '\'asc\''}
        });">Number of ${retailer?.config?.retailerTerminologyConfig?.packTerm}s</a></div>
    </g:if>
    <g:if test="${!userColumns || userColumns?.columns?.find { it.column == "totalCost" }?.enabled}">
        <div class="col-1 font-weight-bold"><a id="total-cost" href="#" onclick="getReportData({
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
        <div id="delivery-search-results-${i + 1}" class="row ml-0 mr-0 pt-2 pb-2 wl-striped${i % 2} hoverable" style="cursor: pointer;" title="Click to view"
            <g:if test="${delivery?.productListItemGroups?.size() <= 0}">
                onclick="document.location.href = '${createLink(action:'delivery', params: [productListId: delivery.id, storeId: storeId, supplierId: supplierId, startDate: startDate?.toString("dd/MM/yyyy"), endDate: endDate?.toString("dd/MM/yyyy"), caged: false])}';"
            </g:if>
            <g:else>
                 onclick="document.location.href = '${createLink(action:'delivery', params: [productListId: delivery.id, storeId: storeId, supplierId: supplierId, startDate: startDate?.toString("dd/MM/yyyy"), endDate: endDate?.toString("dd/MM/yyyy"), caged: true])}';"
            </g:else>
        >
            <g:if test="${!userColumns || userColumns?.columns?.find { it.column == "deliveryId" }?.enabled}">
                <div id="delivery-id-${i + 1}" class="col-1 my-auto">${delivery?.orderId}</div>
            </g:if>
            <g:if test="${!userColumns || userColumns?.columns?.find { it.column == "type" }?.enabled}">
                <div id="type-${i + 1}" class="col-1 my-auto">${delivery?.productListItemGroups?.size() > 0 ? "Caged Delivery" : "Direct Delivery"}</div>
            </g:if>
            <g:if test="${!userColumns || userColumns?.columns?.find { it.column == "storeId" }?.enabled}">
                <div id="store-id-${i + 1}" class="col-1 my-auto">${delivery?.store?.config?.storeNumber}</div>
            </g:if>
            <g:if test="${!userColumns || userColumns?.columns?.find { it.column == "storeName" }?.enabled}">
                <div id="store-name-${i + 1}" class="col-1 my-auto">${delivery?.store?.config?.storeName}</div>
            </g:if>
            <g:if test="${!userColumns || userColumns?.columns?.find { it.column == "status" }?.enabled}">
                <div id="status-${i + 1}" class="col-1 my-auto"><g:message code="DeliveryStatus.${delivery?.status}" /></div>
            </g:if>
            <g:if test="${!userColumns || userColumns?.columns?.find { it.column == "deliveryDate" }?.enabled}">
                <div id="delivery-date-${i + 1}" class="col-1 my-auto">
                    <g:if test="${delivery?.startDate}">
                        <g:formatDate format="dd/MM/yyyy" date="${delivery?.dateStarted?.toDate() ?: new Date()}"/>
                    </g:if>
                </div>
            </g:if>
            <g:if test="${!userColumns || userColumns?.columns?.find { it.column == "supplierName" }?.enabled}">
                <div id="supplier-name-${i + 1}" class="col-1 my-auto text-truncate">${delivery?.supplierName}</div>
            </g:if>
            <g:if test="${!userColumns || userColumns?.columns?.find { it.column == "supplierRef" }?.enabled}">
                <div id="supplier-ref-${i + 1}" class="col-1 my-auto">${delivery?.supplierReference}</div>
            </g:if>
            <g:if test="${!userColumns || userColumns?.columns?.find { it.column == "shipmentRef" }?.enabled}">
                <div id="shipment-ref-${i + 1}" class="col-1 my-auto">${delivery?.shipmentReference}</div>
            </g:if>
            <g:if test="${!userColumns || userColumns?.columns?.find { it.column == "numberOfCages" }?.enabled}">
                <div id="number-of-cages-${i + 1}" class="col-1 my-auto">${delivery?.productListItemGroups?.size()  ?: ""}</div>
            </g:if>
            <g:if test="${!userColumns || userColumns?.columns?.find { it.column == "numberOfPacks" }?.enabled}">
                <div id="number-of-packs-${i + 1}" class="col-1 my-auto">${delivery?.productListItems?.size() ?: "" }</div>
            </g:if>
            <g:if test="${!userColumns || userColumns?.columns?.find { it.column == "totalCost" }?.enabled}">
                <div id="total-cost-${i + 1}" class="col-1 my-auto"><g:formatNumber number="${delivery?.totalCost}" type="currency"/></div>
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