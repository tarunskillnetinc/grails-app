<div class="row mt-5 pb-2 ml-0 mr-0 table-wl bottom-border">
    <g:if test="${!userColumns || userColumns?.columns?.find { it.column == "sku" }?.enabled}">
        <div class="col-2 font-weight-bold" ><a id="sku" href="#" onclick="getReportData({ max: ${sortParams?.max}, offset: ${sortParams?.offset}, sortColumn: 'sku', sortOrder: ${sortParams?.sortColumn == 'sku' ? sortParams?.sortOrder == 'asc' ? '\'desc\'' : '\'asc\'' : '\'asc\''} });">Product SKU</a></div>
    </g:if>
    <g:if test="${!userColumns || userColumns?.columns?.find { it.column == "description" }?.enabled}">
        <div class="col-4 font-weight-bold"><a id="description" href="#" onclick="getReportData({ max: ${sortParams?.max}, offset: ${sortParams?.offset}, sortColumn: 'description', sortOrder: ${sortParams?.sortColumn == 'description' ? sortParams?.sortOrder == 'asc' ? '\'desc\'' : '\'asc\'' : '\'asc\''} });">Description</a></div>
    </g:if>
    <g:if test="${!userColumns || userColumns?.columns?.find { it.column == "orderedQuantity" }?.enabled}">
        <div class="col-2 font-weight-bold"><a id="ordered-quantity" href="#" onclick="getReportData({ max: ${sortParams?.max}, offset: ${sortParams?.offset}, sortColumn: 'orderedQuantity', sortOrder: ${sortParams?.sortColumn == 'orderedQuantity' ? sortParams?.sortOrder == 'asc' ? '\'desc\'' : '\'asc\'' : '\'asc\''} });">Ordered Quantity</a></div>
    </g:if>
    <g:if test="${!userColumns || userColumns?.columns?.find { it.column == "packQuantity" }?.enabled}">
        <div class="col-2 font-weight-bold"><a id="pack-quantity" href="#" onclick="getReportData({ max: ${sortParams?.max}, offset: ${sortParams?.offset}, sortColumn: 'packQuantity', sortOrder: ${sortParams?.sortColumn == 'packQuantity' ? sortParams?.sortOrder == 'asc' ? '\'desc\'' : '\'asc\'' : '\'asc\''} });">Pack Quantity</a></div>
    </g:if>
    <g:if test="${!userColumns || userColumns?.columns?.find { it.column == "lineValue" }?.enabled}">
        <div class="col-2 font-weight-bold"><a id="line-value" href="#" onclick="getReportData({ max: ${sortParams?.max}, offset: ${sortParams?.offset}, sortColumn: 'lineValue', sortOrder: ${sortParams?.sortColumn == 'lineValue' ? sortParams?.sortOrder == 'asc' ? '\'desc\'' : '\'asc\'' : '\'asc\''} });">Line Value</a></div>
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
        <g:set var="isWeighted" value="${order.productListItem?.productVariant?.product?.weightedItem ?: false}"/>

        <div class="row ml-0 mr-0 pt-2 pb-2 wl-striped${i%2}">
            <g:if test="${!userColumns || userColumns?.columns?.find { it.column == "sku" }?.enabled}">
                <div id="sku-${i + 1}" class="col-2 my-auto" style="overflow: hidden;">${order.productListItem?.productVariant?.sku}</div>
            </g:if>
            <g:if test="${!userColumns || userColumns?.columns?.find { it.column == "description" }?.enabled}">
                <div id="description-${i + 1}" class="col-4 my-auto" style="overflow: hidden;">${order.productListItem?.productVariant?.product?.description}</div>
            </g:if>
            <g:if test="${!userColumns || userColumns?.columns?.find { it.column == "orderedQuantity" }?.enabled}">
                <!-- If pack exists can get value from packs. If pack does not exist mean it is singles-->
                <g:if test="${order.pack}">
                    <div id="ordered-quantity-${i + 1}" class="col-2 my-auto" style="overflow: hidden;">
                        ${order.pack?.quantity?.multiply(order.quantity)?.setScale(isWeighted ? 3 : 0)}
                        <g:if test="${isWeighted}"> kg</g:if><g:else> ea (each)</g:else>
                    </div>
                </g:if>
                <g:else>
                    <div id="ordered-quantity-${i + 1}" class="col-2 my-auto" style="overflow: hidden;">
                        ${order.quantity}
                        <g:if test="${isWeighted}"> kg</g:if><g:else> ea (each)</g:else>
                    </div>
                </g:else>
            </g:if>
            <g:if test="${!userColumns || userColumns?.columns?.find { it.column == "packQuantity" }?.enabled}">
                <!-- If pack exists can get value from packs. If pack does not exist mean it is singles-->
                <g:if test="${order.pack}">
                    <div id="pack-quantity-${i + 1}" class="col-2 my-auto" style="overflow: hidden;">${order.pack?.quantity?.setScale(isWeighted ? 3 : 0)}</div>
                </g:if>
                <g:else>
                    <div id="pack-quantity-${i + 1}" class="col-2 my-auto" style="overflow: hidden;">1</div>
                </g:else>
            </g:if>
            <g:if test="${!userColumns || userColumns?.columns?.find { it.column == "lineValue" }?.enabled}">
                <g:if test="${order.pack}">
                    <div id="line-value-${i + 1}" class="col-2 my-auto"><g:formatNumber number="${order.pack?.price?.multiply(order.quantity)}" type="currency" style="overflow: hidden;"/></div>
                </g:if>
                <g:else>
                    <div id="line-value-${i + 1}" class="col-2 my-auto"><g:formatNumber number="${(order?.productListItem?.productVariant?.costPrice?:0).multiply(order?.quantity)}" type="currency" style="overflow: hidden;"/></div>
                </g:else>
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