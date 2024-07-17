<div class="row mt-5 pb-2 ml-0 mr-0 table-wl bottom-border">
    <g:if test="${!userColumns || userColumns?.columns?.find { it.column == "description" }?.enabled}">
        <div class="col-4 font-weight-bold"><a href="#" onclick="getReportData({
            max: ${sortParams?.max},
            offset: ${sortParams?.offset},
            sortColumn: 'description',
            sortOrder: ${sortParams?.sortColumn == 'description' ? sortParams?.sortOrder == 'asc' ? '\'desc\'' : '\'asc\'' : '\'asc\''}
        });">Description</a></div>
    </g:if>
    <g:if test="${!userColumns || userColumns?.columns?.find { it.column == "packCost" }?.enabled}">
        <div class="col-1 font-weight-bold"><a href="#" onclick="getReportData({
            max: ${sortParams?.max},
            offset: ${sortParams?.offset},
            sortColumn: 'packCost',
            sortOrder: ${sortParams?.sortColumn == 'packCost' ? sortParams?.sortOrder == 'asc' ? '\'desc\'' : '\'asc\'' : '\'asc\''}
        });">Pack Cost</a></div>
    </g:if>
    <g:if test="${!userColumns || userColumns?.columns?.find { it.column == "packSize" }?.enabled}">
        <div class="col-1 font-weight-bold"><a href="#" onclick="getReportData({
            max: ${sortParams?.max},
            offset: ${sortParams?.offset},
            sortColumn: 'packSize',
            sortOrder: ${sortParams?.sortColumn == 'packSize' ? sortParams?.sortOrder == 'asc' ? '\'desc\'' : '\'asc\'' : '\'asc\''}
        });">Pack Size</a></div>
    </g:if>
    <g:if test="${!userColumns || userColumns?.columns?.find { it.column == "deliveryQuantity" }?.enabled}">
        <div class="col-2 font-weight-bold"><a href="#" onclick="getReportData({
            max: ${sortParams?.max},
            offset: ${sortParams?.offset},
            sortColumn: 'deliveryQuantity',
            sortOrder: ${sortParams?.sortColumn == 'deliveryQuantity' ? sortParams?.sortOrder == 'asc' ? '\'desc\'' : '\'asc\'' : '\'asc\''}
        });">Packs Delivered</a></div>
    </g:if>
    <g:if test="${!userColumns || userColumns?.columns?.find { it.column == "totalQuantity" }?.enabled}">
        <div class="col-1 font-weight-bold"><a href="#" onclick="getReportData({
            max: ${sortParams?.max},
            offset: ${sortParams?.offset},
            sortColumn: 'totalQuantity',
            sortOrder: ${sortParams?.sortColumn == 'totalQuantity' ? sortParams?.sortOrder == 'asc' ? '\'desc\'' : '\'asc\'' : '\'asc\''}
        });">Total Quantity</a></div>
    </g:if>
    <g:if test="${!userColumns || userColumns?.columns?.find { it.column == "currentSell" }?.enabled}">
        <div class="col-1 font-weight-bold"><a href="#" onclick="getReportData({
            max: ${sortParams?.max},
            offset: ${sortParams?.offset},
            sortColumn: 'price',
            sortOrder: ${sortParams?.sortColumn == 'currentSell' ? sortParams?.sortOrder == 'asc' ? '\'desc\'' : '\'asc\'' : '\'asc\''}
        });">Retail Price</a></div>
    </g:if>
    <g:if test="${!userColumns || userColumns?.columns?.find { it.column == "totalSellValue" }?.enabled}">
        <div class="col-2 font-weight-bold"><a href="#" onclick="getReportData({
            max: ${sortParams?.max},
            offset: ${sortParams?.offset},
            sortColumn: 'totalSellValue',
            sortOrder: ${sortParams?.sortColumn == 'totalSellValue' ? sortParams?.sortOrder == 'asc' ? '\'desc\'' : '\'asc\'' : '\'asc\''}
        });">Total Sell Value</a></div>
    </g:if>
</div>

<div class="d-flex justify-content-center">
    <div id="loading-indicator" class="spinner-border" role="status" style="display: none;">
        <span class="sr-only">Loading...</span>
    </div>
</div>

<div id="search-results">
    <g:if test="${!packLines || packLines?.size() == 0}">
        <div id="noResultsRow" class="col pt-2 pb-2 text-center my-auto wl-striped0">No results found.</div>
    </g:if>

    <g:each in="${packLines}" var="packLine" status="i">
        <div class="row ml-0 mr-0 pt-2 pb-2 wl-striped${i % 2}">
            <g:if test="${!userColumns || userColumns?.columns?.find { it.column == "description" }?.enabled}">
                <div class="col-4 my-auto">${packLine?.productListItem?.productVariant?.product?.description}</div>
            </g:if>
            <g:if test="${!userColumns || userColumns?.columns?.find { it.column == "packCost" }?.enabled}">
                <div class="col-1 my-auto"><g:formatNumber number="${packLine?.getPackCost()}" type="currency" /></div>
            </g:if>
            <g:if test="${!userColumns || userColumns?.columns?.find { it.column == "packSize" }?.enabled}">
                <div class="col-1 my-auto">${packLine?.getPackSize()}</div>
            </g:if>
            <g:if test="${!userColumns || userColumns?.columns?.find { it.column == "deliveryQuantity" }?.enabled}">
                <div class="col-2 my-auto">${packLine?.quantity?.intValue()}</div>
            </g:if>
            <g:if test="${!userColumns || userColumns?.columns?.find { it.column == "totalQuantity" }?.enabled}">
                <div class="col-1 my-auto">${packLine?.totalQuantity}${packLine?.isWeighted() ? " kg" : " ea (each)"}</div>
            </g:if>
            <g:if test="${!userColumns || userColumns?.columns?.find { it.column == "currentSell" }?.enabled}">
                <div class="col-1 my-auto"><g:formatNumber number="${packLine?.productListItem?.productVariant?.currentPrice}" type="currency" /></div>
            </g:if>
            <g:if test="${!userColumns || userColumns?.columns?.find { it.column == "totalSellValue" }?.enabled}">
                <div class="col-2 my-auto"><g:formatNumber number="${packLine?.totalValue}" type="currency" /></div>
            </g:if>
        </div>
    </g:each>
</div>

<g:if test="${totalResults > 0}">
    <div class="my-3 text-right">
        <div>Displaying ${sortParams?.offset ? sortParams?.offset + 1 : 1} - ${((sortParams?.offset ?: 0) + (packLines?.size() ?: 0))} of ${totalResults} result${totalResults > 1 ? 's' : ''}</div>

        <div class="mt-3"><g:paginateReport totalResults="${totalResults}" offset="${sortParams?.offset}"
                                            max="${sortParams?.max}" sortColumn="${sortParams?.sortColumn}"
                                            sortOrder="${sortParams?.sortOrder}"/></div>
    </div>
</g:if>