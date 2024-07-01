<div class="row mt-5 pb-2 ml-0 mr-0 table-wl bottom-border">
    <g:if test="${!userColumns || userColumns?.columns?.find { it.column == "description" }?.enabled}">
        <div class="col-3 font-weight-bold"><a id="description" href="#" onclick="getReportData({ max: ${sortParams?.max}, offset: ${sortParams?.offset}, sortColumn: 'description', sortOrder: ${sortParams?.sortColumn == 'description' ? sortParams?.sortOrder == 'asc' ? '\'desc\'' : '\'asc\'' : '\'asc\''} });">Description</a></div>
    </g:if>
    <g:if test="${!userColumns || userColumns?.columns?.find { it.column == "quantity" }?.enabled}">
        <div class="col-1 font-weight-bold"><a id="quantity" href="#" onclick="getReportData({ max: ${sortParams?.max}, offset: ${sortParams?.offset}, sortColumn: 'quantity', sortOrder: ${sortParams?.sortColumn == 'quantity' ? sortParams?.sortOrder == 'asc' ? '\'desc\'' : '\'asc\'' : '\'asc\''} });">Qty Sold</a></div>
    </g:if>
    <g:if test="${!userColumns || userColumns?.columns?.find { it.column == "costPrice" }?.enabled}">
        <div class="col font-weight-bold"><a id="cost-price" href="#" onclick="getReportData({ max: ${sortParams?.max}, offset: ${sortParams?.offset}, sortColumn: 'costPrice', sortOrder: ${sortParams?.sortColumn == 'costPrice' ? sortParams?.sortOrder == 'asc' ? '\'desc\'' : '\'asc\'' : '\'asc\''} });">Cost Price</a></div>
    </g:if>
    <g:if test="${!userColumns || userColumns?.columns?.find { it.column == "netTotal" }?.enabled}">
        <div class="col font-weight-bold"><a id="net-total" href="#" onclick="getReportData({ max: ${sortParams?.max}, offset: ${sortParams?.offset}, sortColumn: 'netTotal', sortOrder: ${sortParams?.sortColumn == 'netTotal' ? sortParams?.sortOrder == 'asc' ? '\'desc\'' : '\'asc\'' : '\'asc\''} });">Net Total</a></div>
    </g:if>
    <g:if test="${!userColumns || userColumns?.columns?.find { it.column == "vatAmount" }?.enabled}">
        <div class="col font-weight-bold"><a id="vat-amount" href="#" onclick="getReportData({ max: ${sortParams?.max}, offset: ${sortParams?.offset}, sortColumn: 'vatAmount', sortOrder: ${sortParams?.sortColumn == 'vatAmount' ? sortParams?.sortOrder == 'asc' ? '\'desc\'' : '\'asc\'' : '\'asc\''} });">VAT Amount</a></div>
    </g:if>
    <g:if test="${!userColumns || userColumns?.columns?.find { it.column == "profit" }?.enabled}">
        <div class="col font-weight-bold"><a id="profit" href="#" onclick="getReportData({ max: ${sortParams?.max}, offset: ${sortParams?.offset}, sortColumn: 'profit', sortOrder: ${sortParams?.sortColumn == 'profit' ? sortParams?.sortOrder == 'asc' ? '\'desc\'' : '\'asc\'' : '\'asc\''} });">Profit</a></div>
    </g:if>
    <g:if test="${!userColumns || userColumns?.columns?.find { it.column == "margin" }?.enabled}">
        <div class="col font-weight-bold"><a id="margin" href="#" onclick="getReportData({ max: ${sortParams?.max}, offset: ${sortParams?.offset}, sortColumn: 'margin', sortOrder: ${sortParams?.sortColumn == 'margin' ? sortParams?.sortOrder == 'asc' ? '\'desc\'' : '\'asc\'' : '\'asc\''} });">Margin</a></div>
    </g:if>
    <g:if test="${!userColumns || userColumns?.columns?.find { it.column == "usersName" }?.enabled}">
        <div class="col font-weight-bold"><a id="users-name" href="#" onclick="getReportData({ max: ${sortParams?.max}, offset: ${sortParams?.offset}, sortColumn: 'usersName', sortOrder: ${sortParams?.sortColumn == 'usersName' ? sortParams?.sortOrder == 'asc' ? '\'desc\'' : '\'asc\'' : '\'asc\''} });">User</a></div>
    </g:if>
    <g:if test="${!userColumns || userColumns?.columns?.find { it.column == "dateCreated" }?.enabled}">
        <div class="col font-weight-bold"><a id="date-created" href="#" onclick="getReportData({ max: ${sortParams?.max}, offset: ${sortParams?.offset}, sortColumn: 'dateCreated', sortOrder: ${sortParams?.sortColumn == 'dateCreated' ? sortParams?.sortOrder == 'asc' ? '\'desc\'' : '\'asc\'' : '\'asc\''} });">Timestamp</a></div>
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
        <div class="row ml-0 mr-0 pt-2 pb-2 wl-striped${i%2}">
            <g:if test="${!userColumns || userColumns?.columns?.find { it.column == "description" }?.enabled}">
                <div id="description-${i + 1}" class="col-3 my-auto">${sale.productItemCode} - ${sale.productDescription} - ${sale.productUnitSize}</div>
            </g:if>
            <g:if test="${!userColumns || userColumns?.columns?.find { it.column == "quantity" }?.enabled}">
                <div id="quantity-${i + 1}" class="col-1 my-auto">
                    ${(sale.quantity.remainder(BigDecimal.ONE) == BigDecimal.ZERO ? sale.quantity.setScale(0) : sale.quantity) + (isWeighted ? " kg" : " ea (each)")}
                </div>
            </g:if>
            <g:if test="${!userColumns || userColumns?.columns?.find { it.column == "costPrice" }?.enabled}">
                <div id="cost-price-${i + 1}" class="col my-auto">&pound;${sale.costPrice}</div>
            </g:if>
            <g:if test="${!userColumns || userColumns?.columns?.find { it.column == "netTotal" }?.enabled}">
                <div id="net-total-${i + 1}" class="col my-auto">&pound;${sale.retailPrice >= 0 ? sale.retailPrice.subtract(sale.costPrice) : (sale.retailPrice.negate().subtract(sale.costPrice)).negate()}</div>
            </g:if>
            <g:if test="${!userColumns || userColumns?.columns?.find { it.column == "vatAmount" }?.enabled}">
                <div id="vat-amount-${i + 1}" class="col my-auto">&pound;${sale.vatAmount}</div>
            </g:if>
            <g:if test="${!userColumns || userColumns?.columns?.find { it.column == "profit" }?.enabled}">
                <div id="profit-${i + 1}" class="col my-auto">&pound;${sale.retailPrice >= 0 ? sale.retailPrice.subtract(sale.costPrice).subtract(sale.vatAmount) : (sale.retailPrice.negate().subtract(sale.costPrice).add(sale.vatAmount)).negate()}</div>
            </g:if>
            <g:if test="${!userColumns || userColumns?.columns?.find { it.column == "margin" }?.enabled}">
                <div id="margin-${i + 1}" class="col my-auto">${sale.margin}&#37;</div>
            </g:if>
            <g:if test="${!userColumns || userColumns?.columns?.find { it.column == "usersName" }?.enabled}">
                <div id="users-name-${i + 1}" class="col my-auto">${sale.usersName}</div>
            </g:if>
            <g:if test="${!userColumns || userColumns?.columns?.find { it.column == "dateCreated" }?.enabled}">
                <div id="date-created-${i + 1}" class="col my-auto">${sale.dateCreated?.withZone(userTimeZone)?.toString("dd/MM/yyyy HH:mm:ss")}</div>
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