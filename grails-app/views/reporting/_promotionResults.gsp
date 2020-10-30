<div class="row mt-5 mb-2 ml-0 mr-0 table-wl">
    <g:if test="${!userColumns || userColumns?.columns?.find { it.column == "itemCode" }?.enabled}">
        <div class="col font-weight-bold"><a href="#" onclick="getReportData({ max: ${sortParams?.max}, offset: ${sortParams?.offset}, sortColumn: 'itemCode', sortOrder: ${sortParams?.sortColumn == 'itemCode' ? sortParams?.sortOrder == 'asc' ? '\'desc\'' : '\'asc\'' : '\'asc\''} });">Item Code</a></div>
    </g:if>
    <g:if test="${!userColumns || userColumns?.columns?.find { it.column == "description" }?.enabled}">
        <div class="col-2 font-weight-bold"><a href="#" onclick="getReportData({ max: ${sortParams?.max}, offset: ${sortParams?.offset}, sortColumn: 'description', sortOrder: ${sortParams?.sortColumn == 'description' ? sortParams?.sortOrder == 'asc' ? '\'desc\'' : '\'asc\'' : '\'asc\''} });">Description</a></div>
    </g:if>
    <g:if test="${!userColumns || userColumns?.columns?.find { it.column == "costPrice" }?.enabled}">
        <div class="col font-weight-bold"><a href="#" onclick="getReportData({ max: ${sortParams?.max}, offset: ${sortParams?.offset}, sortColumn: 'costPrice', sortOrder: ${sortParams?.sortColumn == 'costPrice' ? sortParams?.sortOrder == 'asc' ? '\'desc\'' : '\'asc\'' : '\'asc\''} });">Cost Price</a></div>
    </g:if>
    <g:if test="${!userColumns || userColumns?.columns?.find { it.column == "fullPrice" }?.enabled}">
        <div class="col font-weight-bold"><a href="#" onclick="getReportData({ max: ${sortParams?.max}, offset: ${sortParams?.offset}, sortColumn: 'fullPrice', sortOrder: ${sortParams?.sortColumn == 'fullPrice' ? sortParams?.sortOrder == 'asc' ? '\'desc\'' : '\'asc\'' : '\'asc\''} });">Full Price</a></div>
    </g:if>
    <g:if test="${!userColumns || userColumns?.columns?.find { it.column == "fullPriceProfit" }?.enabled}">
        <div class="col font-weight-bold"><a href="#" onclick="getReportData({ max: ${sortParams?.max}, offset: ${sortParams?.offset}, sortColumn: 'fullPriceProfit', sortOrder: ${sortParams?.sortColumn == 'fullPriceProfit' ? sortParams?.sortOrder == 'asc' ? '\'desc\'' : '\'asc\'' : '\'asc\''} });">Full Price Profit</a></div>
    </g:if>
    <g:if test="${!userColumns || userColumns?.columns?.find { it.column == "fullPriceMargin" }?.enabled}">
        <div class="col font-weight-bold"><a href="#" onclick="getReportData({ max: ${sortParams?.max}, offset: ${sortParams?.offset}, sortColumn: 'fullPriceMargin', sortOrder: ${sortParams?.sortColumn == 'fullPriceMargin' ? sortParams?.sortOrder == 'asc' ? '\'desc\'' : '\'asc\'' : '\'asc\''} });">Full Price Margin</a></div>
    </g:if>
    <g:if test="${!userColumns || userColumns?.columns?.find { it.column == "discount" }?.enabled}">
        <div class="col font-weight-bold"><a href="#" onclick="getReportData({ max: ${sortParams?.max}, offset: ${sortParams?.offset}, sortColumn: 'discount', sortOrder: ${sortParams?.sortColumn == 'discount' ? sortParams?.sortOrder == 'asc' ? '\'desc\'' : '\'asc\'' : '\'asc\''} });">Discount</a></div>
    </g:if>
    <g:if test="${!userColumns || userColumns?.columns?.find { it.column == "discountedPrice" }?.enabled}">
        <div class="col font-weight-bold"><a href="#" onclick="getReportData({ max: ${sortParams?.max}, offset: ${sortParams?.offset}, sortColumn: 'discountedPrice', sortOrder: ${sortParams?.sortColumn == 'discountedPrice' ? sortParams?.sortOrder == 'asc' ? '\'desc\'' : '\'asc\'' : '\'asc\''} });">Discounted Price</a></div>
    </g:if>
    <g:if test="${!userColumns || userColumns?.columns?.find { it.column == "discountedProfit" }?.enabled}">
        <div class="col font-weight-bold"><a href="#" onclick="getReportData({ max: ${sortParams?.max}, offset: ${sortParams?.offset}, sortColumn: 'discountedProfit', sortOrder: ${sortParams?.sortColumn == 'discountedProfit' ? sortParams?.sortOrder == 'asc' ? '\'desc\'' : '\'asc\'' : '\'asc\''} });">Discounted Profit</a></div>
    </g:if>
    <g:if test="${!userColumns || userColumns?.columns?.find { it.column == "discountedMargin" }?.enabled}">
        <div class="col font-weight-bold"><a href="#" onclick="getReportData({ max: ${sortParams?.max}, offset: ${sortParams?.offset}, sortColumn: 'discountedMargin', sortOrder: ${sortParams?.sortColumn == 'discountedMargin' ? sortParams?.sortOrder == 'asc' ? '\'desc\'' : '\'asc\'' : '\'asc\''} });">Discounted Margin</a></div>
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
    <g:if test="${!promotionSaleProducts || promotionSaleProducts.size() == 0}">
        <div id="noResultsRow" class="col pt-2 text-center my-auto">No results found.</div>
    </g:if>

    <g:each in="${promotionSaleProducts}" var="promotionSaleProduct" status="i">
        <div class="row ml-0 mr-0 pt-2 pb-2 wl-striped${i%2}">
            <g:if test="${!userColumns || userColumns?.columns?.find { it.column == "itemCode" }?.enabled}">
                <div class="col my-auto">${promotionSaleProduct.itemCode}</div>
            </g:if>
            <g:if test="${!userColumns || userColumns?.columns?.find { it.column == "description" }?.enabled}">
                <div class="col-2 my-auto">${promotionSaleProduct.description}</div>
            </g:if>
            <g:if test="${!userColumns || userColumns?.columns?.find { it.column == "costPrice" }?.enabled}">
                <div class="col my-auto"><g:formatNumber number="${promotionSaleProduct.costPrice}" type="currency" /></div>
            </g:if>
            <g:if test="${!userColumns || userColumns?.columns?.find { it.column == "fullPrice" }?.enabled}">
                <div class="col my-auto"><g:formatNumber number="${promotionSaleProduct.fullPrice}" type="currency" /></div>
            </g:if>
            <g:if test="${!userColumns || userColumns?.columns?.find { it.column == "fullPriceProfit" }?.enabled}">
                <div class="col my-auto"><g:formatNumber number="${promotionSaleProduct.fullPriceProfit}" type="currency" /></div>
            </g:if>
            <g:if test="${!userColumns || userColumns?.columns?.find { it.column == "fullPriceMargin" }?.enabled}">
                <div class="col my-auto"><g:formatNumber number="${promotionSaleProduct.fullPriceMargin / 100}" type="percent" minFractionDigits="2" /></div>
            </g:if>
            <g:if test="${!userColumns || userColumns?.columns?.find { it.column == "discount" }?.enabled}">
                <div class="col my-auto"><g:formatNumber number="${promotionSaleProduct.discount}" type="currency" /></div>
            </g:if>
            <g:if test="${!userColumns || userColumns?.columns?.find { it.column == "discountedPrice" }?.enabled}">
                <div class="col my-auto"><g:formatNumber number="${promotionSaleProduct.discountedPrice}" type="currency" /></div>
            </g:if>
            <g:if test="${!userColumns || userColumns?.columns?.find { it.column == "discountedProfit" }?.enabled}">
                <div class="col my-auto"><g:formatNumber number="${promotionSaleProduct.discountedProfit}" type="currency" /></div>
            </g:if>
            <g:if test="${!userColumns || userColumns?.columns?.find { it.column == "discountedMargin" }?.enabled}">
                <div class="col my-auto"><g:formatNumber number="${promotionSaleProduct.discountedMargin / 100}" type="percent" minFractionDigits="2" /></div>
            </g:if>
            <g:if test="${!userColumns || userColumns?.columns?.find { it.column == "vat" }?.enabled}">
                <div class="col my-auto"><g:formatNumber number="${promotionSaleProduct.vat}" type="currency" /></div>
            </g:if>
        </div>
    </g:each>
</div>

<div class="my-3 text-right">
    <div>Displaying ${sortParams?.offset ? sortParams?.offset + 1 : 1} - ${((sortParams?.offset ?: 0) + (promotionSaleProducts?.size() ?: 0))} of ${totalResults} result${totalResults > 1 ? 's' : ''}</div>
    <div class="mt-3"><g:paginateReport totalResults="${totalResults}" offset="${sortParams?.offset}" max="${sortParams?.max}" sortColumn="${sortParams?.sortColumn}" sortOrder="${sortParams?.sortOrder}" /></div>
</div>