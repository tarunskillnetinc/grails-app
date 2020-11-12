<div class="row mt-5 mb-2 ml-0 mr-0 table-wl">
    <g:if test="${!userColumns || userColumns?.columns?.find { it.column == "description" }?.enabled}">
        <div class="col-2 font-weight-bold"><a href="#" onclick="getReportData({ max: ${sortParams?.max}, offset: ${sortParams?.offset}, sortColumn: 'description', sortOrder: ${sortParams?.sortColumn == 'description' ? sortParams?.sortOrder == 'asc' ? '\'desc\'' : '\'asc\'' : '\'asc\''} });">Description</a></div>
    </g:if>
    <g:if test="${!userColumns || userColumns?.columns?.find { it.column == "type" }?.enabled}">
        <div class="col font-weight-bold"><a href="#" onclick="getReportData({ max: ${sortParams?.max}, offset: ${sortParams?.offset}, sortColumn: 'type', sortOrder: ${sortParams?.sortColumn == 'type' ? sortParams?.sortOrder == 'asc' ? '\'desc\'' : '\'asc\'' : '\'asc\''} });">Type</a></div>
    </g:if>
    <g:if test="${!userColumns || userColumns?.columns?.find { it.column == "quantity" }?.enabled}">
        <div class="col-1 font-weight-bold"><a href="#" onclick="getReportData({ max: ${sortParams?.max}, offset: ${sortParams?.offset}, sortColumn: 'quantity', sortOrder: ${sortParams?.sortColumn == 'quantity' ? sortParams?.sortOrder == 'asc' ? '\'desc\'' : '\'asc\'' : '\'asc\''} });">Quantity</a></div>
    </g:if>
    <g:if test="${!userColumns || userColumns?.columns?.find { it.column == "fullPrice" }?.enabled}">
        <div class="col font-weight-bold"><a href="#" onclick="getReportData({ max: ${sortParams?.max}, offset: ${sortParams?.offset}, sortColumn: 'fullPrice', sortOrder: ${sortParams?.sortColumn == 'fullPrice' ? sortParams?.sortOrder == 'asc' ? '\'desc\'' : '\'asc\'' : '\'asc\''} });">Full Price</a></div>
    </g:if>
    <g:if test="${!userColumns || userColumns?.columns?.find { it.column == "discount" }?.enabled}">
        <div class="col font-weight-bold"><a href="#" onclick="getReportData({ max: ${sortParams?.max}, offset: ${sortParams?.offset}, sortColumn: 'discount', sortOrder: ${sortParams?.sortColumn == 'discount' ? sortParams?.sortOrder == 'asc' ? '\'desc\'' : '\'asc\'' : '\'asc\''} });">Discount</a></div>
    </g:if>
    <g:if test="${!userColumns || userColumns?.columns?.find { it.column == "profit" }?.enabled}">
        <div class="col font-weight-bold"><a href="#" onclick="getReportData({ max: ${sortParams?.max}, offset: ${sortParams?.offset}, sortColumn: 'profit', sortOrder: ${sortParams?.sortColumn == 'profit' ? sortParams?.sortOrder == 'asc' ? '\'desc\'' : '\'asc\'' : '\'asc\''} });">Profit</a></div>
    </g:if>
    <g:if test="${!userColumns || userColumns?.columns?.find { it.column == "margin" }?.enabled}">
        <div class="col font-weight-bold"><a href="#" onclick="getReportData({ max: ${sortParams?.max}, offset: ${sortParams?.offset}, sortColumn: 'margin', sortOrder: ${sortParams?.sortColumn == 'margin' ? sortParams?.sortOrder == 'asc' ? '\'desc\'' : '\'asc\'' : '\'asc\''} });">Margin</a></div>
    </g:if>
    <g:if test="${!userColumns || userColumns?.columns?.find { it.column == "vat" }?.enabled}">
        <div class="col font-weight-bold"><a href="#" onclick="getReportData({ max: ${sortParams?.max}, offset: ${sortParams?.offset}, sortColumn: 'vat', sortOrder: ${sortParams?.sortColumn == 'vat' ? sortParams?.sortOrder == 'asc' ? '\'desc\'' : '\'asc\'' : '\'asc\''} });">VAT</a></div>
    </g:if>
</div>

<div class="d-flex justify-content-center">
    <div id="loading-indicator" class="spinner-border" role="status" style="display: none;">
        <span class="sr-only">Loading...</span>
    </div>
</div>

<div id="search-results">
    <g:if test="${!promotionSales || promotionSales?.size() == 0}">
        <div id="noResultsRow" class="col pt-2 text-center my-auto">No results found.</div>
    </g:if>

    <g:each in="${promotionSales}" var="promotionSale" status="i">
        <div class="row ml-0 mr-0 pt-2 pb-2 wl-striped${i%2}" style="cursor: pointer;" onclick="document.location.href='${createLink(action:'promotions', params: [promotionId: promotionSale.promotionId, startDate: startDate?.format("dd/MM/yyyy"), endDate: endDate?.format("dd/MM/yyyy")])}';">
            <g:if test="${!userColumns || userColumns?.columns?.find { it.column == "description" }?.enabled}">
                <div class="col-2 my-auto">${promotionSale.description}</div>
            </g:if>
            <g:if test="${!userColumns || userColumns?.columns?.find { it.column == "type" }?.enabled}">
                <div class="col my-auto"><g:message code="PromotionType.${promotionSale.type}" /></div>
            </g:if>
            <g:if test="${!userColumns || userColumns?.columns?.find { it.column == "quantity" }?.enabled}">
                <div class="col-1 my-auto">${promotionSale.quantity}</div>
            </g:if>
            <g:if test="${!userColumns || userColumns?.columns?.find { it.column == "fullPrice" }?.enabled}">
                <div class="col my-auto"><g:formatNumber number="${promotionSale.fullPrice}" type="currency" /></div>
            </g:if>
            <g:if test="${!userColumns || userColumns?.columns?.find { it.column == "discount" }?.enabled}">
                <div class="col my-auto"><g:formatNumber number="${promotionSale.discount}" type="currency" /></div>
            </g:if>
            <g:if test="${!userColumns || userColumns?.columns?.find { it.column == "profit" }?.enabled}">
                <div class="col my-auto"><g:formatNumber number="${promotionSale.profit}" type="currency" /></div>
            </g:if>
            <g:if test="${!userColumns || userColumns?.columns?.find { it.column == "margin" }?.enabled}">
                <div class="col my-auto"><g:formatNumber number="${promotionSale.margin / 100}" type="percent" minFractionDigits="2" /></div>
            </g:if>
            <g:if test="${!userColumns || userColumns?.columns?.find { it.column == "vat" }?.enabled}">
                <div class="col my-auto"><g:formatNumber number="${promotionSale.vat}" type="currency" /></div>
            </g:if>
        </div>
    </g:each>
</div>

<g:if test="${totalResults > 0}">
    <div class="my-3 text-right">
        <div>Displaying ${sortParams?.offset ? sortParams?.offset + 1 : 1} - ${((sortParams?.offset ?: 0) + (promotionSales?.size() ?: 0))} of ${totalResults} result${totalResults > 1 ? 's' : ''}</div>
        <div class="mt-3"><g:paginateReport totalResults="${totalResults}" offset="${sortParams?.offset}" max="${sortParams?.max}" sortColumn="${sortParams?.sortColumn}" sortOrder="${sortParams?.sortOrder}" /></div>
    </div>
</g:if>