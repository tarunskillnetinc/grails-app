<!doctype html>
<html>
<head>
    <meta name="layout" content="main" />

    <title>WonderLane</title>
</head>
<body>
    <g:render template="/nav/reporting" model="[active: 'promotions']" />

    <section id="reporting-container" class="container-fluid">
        <div class="row header-wl">
            <h2 class="mx-auto">Promotion Sales Report</h2>
        </div>

        <div class="col-8 offset-2 mt-4">
            <div class="card bg-light border-wl">
                <div class="card-header" data-toggle="collapse" data-target="#filterCollapse" aria-expanded="false" aria-controls="collapseExample">
                    Filters
                </div>
                <div class="card-body collapse" id="filterCollapse">
                    <g:form class="form-inline" action="promotion" params="[promotionSaleId: promotionSaleId, max: max, offset: offset, sortColumn: sortColumn, sortOrder: sortOrder]">
                        <div class="form-group">
                            <g:textField name="searchText" placeholder="Type search" maxlength="100" value="${searchText}" class="form-control bottom-border" />
                        </div>
                        <g:submitButton name="Search" class="btn btn-wl" />
                    </g:form>
                </div>
            </div>
        </div>

        <div class="row mt-5 mb-2 ml-0 mr-0 table-wl">
            <div class="col font-weight-bold"><g:link action="promotion" params="[promotionSaleId: promotionSaleId, max: max, offset: offset, sortColumn: 'itemCode', sortOrder: sortColumn == 'itemCode' ? sortOrder == 'asc' ? 'desc' : 'asc' : 'asc']">Item Code</g:link></div>
            <div class="col-2 font-weight-bold"><g:link action="promotion" params="[promotionSaleId: promotionSaleId, max: max, offset: offset, sortColumn: 'description', sortOrder: sortColumn == 'description' ? sortOrder == 'asc' ? 'desc' : 'asc' : 'asc']">Description</g:link></div>
            <div class="col font-weight-bold"><g:link action="promotion" params="[promotionSaleId: promotionSaleId, max: max, offset: offset, sortColumn: 'costPrice', sortOrder: sortColumn == 'costPrice' ? sortOrder == 'asc' ? 'desc' : 'asc' : 'asc']">Cost Price</g:link></div>
            <div class="col font-weight-bold"><g:link action="promotion" params="[promotionSaleId: promotionSaleId, max: max, offset: offset, sortColumn: 'fullPrice', sortOrder: sortColumn == 'fullPrice' ? sortOrder == 'asc' ? 'desc' : 'asc' : 'asc']">Full Price</g:link></div>
            <div class="col font-weight-bold"><g:link action="promotion" params="[promotionSaleId: promotionSaleId, max: max, offset: offset, sortColumn: 'fullPriceProfit', sortOrder: sortColumn == 'fullPriceProfit' ? sortOrder == 'asc' ? 'desc' : 'asc' : 'asc']">Full Price Profit</g:link></div>
            <div class="col font-weight-bold"><g:link action="promotion" params="[promotionSaleId: promotionSaleId, max: max, offset: offset, sortColumn: 'fullPriceMargin', sortOrder: sortColumn == 'fullPriceMargin' ? sortOrder == 'asc' ? 'desc' : 'asc' : 'asc']">Full Price Margin</g:link></div>
            <div class="col font-weight-bold"><g:link action="promotion" params="[promotionSaleId: promotionSaleId, max: max, offset: offset, sortColumn: 'discount', sortOrder: sortColumn == 'discount' ? sortOrder == 'asc' ? 'desc' : 'asc' : 'asc']">Discount</g:link></div>
            <div class="col font-weight-bold"><g:link action="promotion" params="[promotionSaleId: promotionSaleId, max: max, offset: offset, sortColumn: 'discountedPrice', sortOrder: sortColumn == 'discountedPrice' ? sortOrder == 'asc' ? 'desc' : 'asc' : 'asc']">Discounted Price</g:link></div>
            <div class="col font-weight-bold"><g:link action="promotion" params="[promotionSaleId: promotionSaleId, max: max, offset: offset, sortColumn: 'discountedProfit', sortOrder: sortColumn == 'discountedProfit' ? sortOrder == 'asc' ? 'desc' : 'asc' : 'asc']">Discounted Profit</g:link></div>
            <div class="col font-weight-bold"><g:link action="promotion" params="[promotionSaleId: promotionSaleId, max: max, offset: offset, sortColumn: 'discountedMargin', sortOrder: sortColumn == 'discountedMargin' ? sortOrder == 'asc' ? 'desc' : 'asc' : 'asc']">Discounted Margin</g:link></div>
            <div class="col font-weight-bold"><g:link action="promotion" params="[promotionSaleId: promotionSaleId, max: max, offset: offset, sortColumn: 'vat', sortOrder: sortColumn == 'vat' ? sortOrder == 'asc' ? 'desc' : 'asc' : 'asc']">VAT</g:link></div>
        </div>

        <div id="search-results" class="align-content-center">
            <g:if test="${!promotionSaleProducts || promotionSaleProducts.size() == 0}">
                <div id="noResultsRow" class="col pt-2 text-center my-auto">No results found.</div>
            </g:if>

            <g:each in="${promotionSaleProducts}" var="promotionSaleProduct" status="i">
                <div class="row ml-0 mr-0 pt-2 pb-2 wl-striped${i%2}">
                    <div class="col my-auto">${promotionSaleProduct.itemCode}</div>
                    <div class="col-2 my-auto">${promotionSaleProduct.description}</div>
                    <div class="col my-auto"><g:formatNumber number="${promotionSaleProduct.costPrice}" type="currency" /></div>
                    <div class="col my-auto"><g:formatNumber number="${promotionSaleProduct.fullPrice}" type="currency" /></div>
                    <div class="col my-auto"><g:formatNumber number="${promotionSaleProduct.fullPriceProfit}" type="currency" /></div>
                    <div class="col my-auto"><g:formatNumber number="${promotionSaleProduct.fullPriceMargin / 100}" type="percent" minFractionDigits="2" /></div>
                    <div class="col my-auto"><g:formatNumber number="${promotionSaleProduct.discount}" type="currency" /></div>
                    <div class="col my-auto"><g:formatNumber number="${promotionSaleProduct.discountedPrice}" type="currency" /></div>
                    <div class="col my-auto"><g:formatNumber number="${promotionSaleProduct.discountedProfit}" type="currency" /></div>
                    <div class="col my-auto"><g:formatNumber number="${promotionSaleProduct.discountedMargin / 100}" type="percent" minFractionDigits="2" /></div>
                    <div class="col my-auto"><g:formatNumber number="${promotionSaleProduct.vat}" type="currency" /></div>
                </div>
            </g:each>
        </div>

        <div class="my-3 text-right">
            <div class="text-right">Displaying ${promotionSaleProducts?.size()} of ${totalResults} results.</div>
            <g:paginate total="$totalResults" offset="$offset" max="$max" params="[promotionSaleId: promotionSaleId, sortColumn: sortColumn, sortOrder: sortOrder]" />
        </div>
    </section>
</body>
</html>