<div class="row mt-5 pb-2 ml-0 mr-0 table-wl bottom-border">
    <g:if test="${!userColumns || userColumns?.columns?.find { it.column == "sku" }?.enabled}">
        <div class="col-2 font-weight-bold"><a id="sku" href="#" onclick="getReportData({
            max: ${sortParams?.max},
            offset: ${sortParams?.offset},
            sortColumn: 'sku',
            sortOrder: ${sortParams?.sortColumn == 'sku' ? sortParams?.sortOrder == 'asc' ? '\'desc\'' : '\'asc\'' : '\'asc\''}
        });">Product SKU</a></div>
    </g:if>
    <g:if test="${!userColumns || userColumns?.columns?.find { it.column == "description" }?.enabled}">
        <div class="col-6 font-weight-bold"><a id="description" href="#" onclick="getReportData({
            max: ${sortParams?.max},
            offset: ${sortParams?.offset},
            sortColumn: 'description',
            sortOrder: ${sortParams?.sortColumn == 'description' ? sortParams?.sortOrder == 'asc' ? '\'desc\'' : '\'asc\'' : '\'asc\''}
        });">Product Description</a></div>
    </g:if>
    <g:if test="${!userColumns || userColumns?.columns?.find { it.column == "itemQuantity" }?.enabled}">
        <div class="col-2 font-weight-bold"><a id="item-quantity" href="#" onclick="getReportData({
            max: ${sortParams?.max},
            offset: ${sortParams?.offset},
            sortColumn: 'itemQuantity',
            sortOrder: ${sortParams?.sortColumn == 'itemQuantity' ? sortParams?.sortOrder == 'asc' ? '\'desc\'' : '\'asc\'' : '\'asc\''}
        });">Items Delivered</a></div>
    </g:if>
    <g:if test="${!userColumns || userColumns?.columns?.find { it.column == "totalCost" }?.enabled}">
        <div class="col-2 font-weight-bold"><a id="total-cost" href="#" onclick="getReportData({
            max: ${sortParams?.max},
            offset: ${sortParams?.offset},
            sortColumn: 'totalCost',
            sortOrder: ${sortParams?.sortColumn == 'totalCost' ? sortParams?.sortOrder == 'asc' ? '\'desc\'' : '\'asc\'' : '\'asc\''}
        });">Total Cost</a></div>
    </g:if>
</div>

<div class="d-flex justify-content-center">
    <div id="loading-indicator" class="spinner-border" role="status" style="display: none;">
        <span class="sr-only">Loading...</span>
    </div>
</div>

<div id="search-results">
    <g:if test="${!items || items?.size() == 0}">
        <div id="noResultsRow" class="col pt-2 pb-2 text-center my-auto wl-striped0">No results found.</div>
    </g:if>

    <g:each in="${items}" var="item" status="i">
        <g:set var="isWeighted" value="${item?.productVariant?.product?.weightedItem ?: false}"/>
        <div class="row ml-0 mr-0 pt-2 pb-2 wl-striped${i % 2} hoverable" style="cursor: pointer;" title="Click to view"
             onclick="document.location.href='${createLink(action:'deliveryPackLines', params: [productListId: item.productList.id, productListItemId: item.id, storeId: storeId, supplierId: supplierId, descriptionFilter: descriptionFilter, startDate: startDate?.toString("dd/MM/yyyy"), endDate: endDate?.toString("dd/MM/yyyy"), cageId: cageId, ])}';">
            <g:if test="${!userColumns || userColumns?.columns?.find { it.column == "sku" }?.enabled}">
                <div id="sku-${i + 1}" class="col-2 my-auto">${item?.productVariant?.sku}</div>
            </g:if>
            <g:if test="${!userColumns || userColumns?.columns?.find { it.column == "description" }?.enabled}">
                <div id="description-${i + 1}" class="col-6 my-auto">${item?.productVariant?.product?.description}</div>
            </g:if>
            <g:if test="${!userColumns || userColumns?.columns?.find { it.column == "itemQuantity" }?.enabled}">
                <div id="item-quantity-${i + 1}" class="col-2 my-auto">
                    ${(item.quantity ?: item.fillQuantity)?.setScale(isWeighted ? 3 : 0)}
                    ${isWeighted ? " kg" : " ea (each)"}
                </div>
            </g:if>
            <g:if test="${!userColumns || userColumns?.columns?.find { it.column == "totalCost" }?.enabled}">
                <div id="total-cost-${i + 1}" class="col-2 my-auto"><g:formatNumber number="${item.totalCost}" type="currency" /></div>
            </g:if>
        </div>
    </g:each>
</div>

<g:if test="${totalResults > 0}">
    <div class="my-3 text-right">
        <div>Displaying ${sortParams?.offset ? sortParams?.offset + 1 : 1} - ${((sortParams?.offset ?: 0) + (items?.size() ?: 0))} of ${totalResults} result${totalResults > 1 ? 's' : ''}</div>

        <div class="mt-3"><g:paginateReport totalResults="${totalResults}" offset="${sortParams?.offset}"
                                            max="${sortParams?.max}" sortColumn="${sortParams?.sortColumn}"
                                            sortOrder="${sortParams?.sortOrder}"/></div>
    </div>
</g:if>