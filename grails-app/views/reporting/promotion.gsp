<!doctype html>
<html>
<head>
    <meta name="layout" content="main" />

    <title>WonderLane</title>

    <asset:javascript src="reporting.js" />

    <script type='text/javascript'>
        var saveReportColumnsUrl = "${createLink(controller: 'reporting', action: 'ajaxSaveReportColumns')}";
    </script>
</head>
<body>
    <g:render template="/nav/reporting" model="[active: 'promotions']" />

    <section id="reporting-container" class="container-fluid">
        <div class="row header-wl">
            <h2 class="mx-auto">Promotion Sales Report</h2>
        </div>

        <div class="row mt-4">
            <div class="col-6">
                <div class="card bg-light border-wl">
                    <div class="card-header pointer" data-toggle="collapse" data-target="#filterCollapse" aria-expanded="false" aria-controls="collapseExample">
                        <div class="row">
                            <div class="col-10">Filters</div>
                            <div class="col-2 text-right">
                                <svg width="1em" height="1em" viewBox="0 0 16 16" class="bi bi-caret-down-fill text-right" fill="currentColor" xmlns="http://www.w3.org/2000/svg">
                                    <path d="M7.247 11.14L2.451 5.658C1.885 5.013 2.345 4 3.204 4h9.592a1 1 0 0 1 .753 1.659l-4.796 5.48a1 1 0 0 1-1.506 0z"/>
                                </svg>
                            </div>
                        </div>
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

            <div class="col-2 offset-4">
                <div class="card bg-light border-wl">
                    <div class="card-header pointer" data-toggle="collapse" data-target="#columnsCollapse" aria-expanded="false" aria-controls="columnsCollapse">
                        <div class="row">
                            <div class="col-10">Columns</div>
                            <div class="col-2 text-right">
                                <svg width="1em" height="1em" viewBox="0 0 16 16" class="bi bi-caret-down-fill text-right" fill="currentColor" xmlns="http://www.w3.org/2000/svg">
                                    <path d="M7.247 11.14L2.451 5.658C1.885 5.013 2.345 4 3.204 4h9.592a1 1 0 0 1 .753 1.659l-4.796 5.48a1 1 0 0 1-1.506 0z"/>
                                </svg>
                            </div>
                        </div>
                    </div>
                    <div class="card-body collapse" id="columnsCollapse">
                        <g:form name="reportColumnsForm" id="reportColumnsForm">
                            <div class="form-group form-check">
                                <g:checkBox name="columns" id="columnsItemCode" class="form-check-input" value="itemCode" checked="${!userColumns || userColumns?.columns?.find { it.column == 'itemCode' }?.enabled}" />
                                <label class="form-check-label" for="columnsItemCode">Item Code</label>
                            </div>
                            <div class="form-group form-check">
                                <g:checkBox name="columns" id="columnsDescription" class="form-check-input" value="description" checked="${!userColumns || userColumns?.columns?.find { it.column == 'description' }?.enabled}" />
                                <label class="form-check-label" for="columnsDescription">Description</label>
                            </div>
                            <div class="form-group form-check">
                                <g:checkBox name="columns" id="columnsCostPrice" class="form-check-input" value="costPrice" checked="${!userColumns || userColumns?.columns?.find { it.column == 'costPrice' }?.enabled}" />
                                <label class="form-check-label" for="columnsCostPrice">Cost Price</label>
                            </div>
                            <div class="form-group form-check">
                                <g:checkBox name="columns" id="columnsFullPrice" class="form-check-input" value="fullPrice" checked="${!userColumns || userColumns?.columns?.find { it.column == 'fullPrice' }?.enabled}" />
                                <label class="form-check-label" for="columnsFullPrice">Full Price</label>
                            </div>
                            <div class="form-group form-check">
                                <g:checkBox name="columns" id="columnsFullPriceProfit" class="form-check-input" value="fullPriceProfit" checked="${!userColumns || userColumns?.columns?.find { it.column == 'fullPriceProfit' }?.enabled}" />
                                <label class="form-check-label" for="columnsFullPriceProfit">Full Price Profit</label>
                            </div>
                            <div class="form-group form-check">
                                <g:checkBox name="columns" id="columnsFullPriceMargin" class="form-check-input" value="fullPriceMargin" checked="${!userColumns || userColumns?.columns?.find { it.column == 'fullPriceMargin' }?.enabled}" />
                                <label class="form-check-label" for="columnsFullPriceMargin">Full Price Margin</label>
                            </div>
                            <div class="form-group form-check">
                                <g:checkBox name="columns" id="columnsDiscount" class="form-check-input" value="discount" checked="${!userColumns || userColumns?.columns?.find { it.column == 'discount' }?.enabled}" />
                                <label class="form-check-label" for="columnsDiscount">Discount</label>
                            </div>
                            <div class="form-group form-check">
                                <g:checkBox name="columns" id="columnsDiscountedPrice" class="form-check-input" value="discountedPrice" checked="${!userColumns || userColumns?.columns?.find { it.column == 'discountedPrice' }?.enabled}" />
                                <label class="form-check-label" for="columnsDiscountedPrice">Discounted Price</label>
                            </div>
                            <div class="form-group form-check">
                                <g:checkBox name="columns" id="columnsDiscountedProfit" class="form-check-input" value="discountedProfit" checked="${!userColumns || userColumns?.columns?.find { it.column == 'discountedProfit' }?.enabled}" />
                                <label class="form-check-label" for="columnsDiscountedProfit">Discounted Profit</label>
                            </div>
                            <div class="form-group form-check">
                                <g:checkBox name="columns" id="columnsDiscountedMargin" class="form-check-input" value="discountedMargin" checked="${!userColumns || userColumns?.columns?.find { it.column == 'discountedMargin' }?.enabled}" />
                                <label class="form-check-label" for="columnsDiscountedMargin">Discounted Margin</label>
                            </div>
                            <div class="form-group form-check">
                                <g:checkBox name="columns" id="columnsVat" class="form-check-input" value="vat" checked="${!userColumns || userColumns?.columns?.find { it.column == 'vat' }?.enabled}" />
                                <label class="form-check-label" for="columnsVat">VAT</label>
                            </div>

                            <button type="button" class="btn btn-wl" onclick="saveReportColumns(saveReportColumnsUrl, 'PROMOTION');">Apply</button>
                        </g:form>
                    </div>
                </div>
            </div>
        </div>

        <div class="row mt-5 mb-2 ml-0 mr-0 table-wl">
            <g:if test="${!userColumns || userColumns?.columns?.find { it.column == "itemCode" }?.enabled}">
                <div class="col font-weight-bold"><g:link action="promotion" params="[promotionSaleId: promotionSaleId, max: max, offset: offset, sortColumn: 'itemCode', sortOrder: sortColumn == 'itemCode' ? sortOrder == 'asc' ? 'desc' : 'asc' : 'asc']">Item Code</g:link></div>
            </g:if>
            <g:if test="${!userColumns || userColumns?.columns?.find { it.column == "description" }?.enabled}">
                <div class="col-2 font-weight-bold"><g:link action="promotion" params="[promotionSaleId: promotionSaleId, max: max, offset: offset, sortColumn: 'description', sortOrder: sortColumn == 'description' ? sortOrder == 'asc' ? 'desc' : 'asc' : 'asc']">Description</g:link></div>
            </g:if>
            <g:if test="${!userColumns || userColumns?.columns?.find { it.column == "costPrice" }?.enabled}">
                <div class="col font-weight-bold"><g:link action="promotion" params="[promotionSaleId: promotionSaleId, max: max, offset: offset, sortColumn: 'costPrice', sortOrder: sortColumn == 'costPrice' ? sortOrder == 'asc' ? 'desc' : 'asc' : 'asc']">Cost Price</g:link></div>
            </g:if>
            <g:if test="${!userColumns || userColumns?.columns?.find { it.column == "fullPrice" }?.enabled}">
                <div class="col font-weight-bold"><g:link action="promotion" params="[promotionSaleId: promotionSaleId, max: max, offset: offset, sortColumn: 'fullPrice', sortOrder: sortColumn == 'fullPrice' ? sortOrder == 'asc' ? 'desc' : 'asc' : 'asc']">Full Price</g:link></div>
            </g:if>
            <g:if test="${!userColumns || userColumns?.columns?.find { it.column == "fullPriceProfit" }?.enabled}">
                <div class="col font-weight-bold"><g:link action="promotion" params="[promotionSaleId: promotionSaleId, max: max, offset: offset, sortColumn: 'fullPriceProfit', sortOrder: sortColumn == 'fullPriceProfit' ? sortOrder == 'asc' ? 'desc' : 'asc' : 'asc']">Full Price Profit</g:link></div>
            </g:if>
            <g:if test="${!userColumns || userColumns?.columns?.find { it.column == "fullPriceMargin" }?.enabled}">
                <div class="col font-weight-bold"><g:link action="promotion" params="[promotionSaleId: promotionSaleId, max: max, offset: offset, sortColumn: 'fullPriceMargin', sortOrder: sortColumn == 'fullPriceMargin' ? sortOrder == 'asc' ? 'desc' : 'asc' : 'asc']">Full Price Margin</g:link></div>
            </g:if>
            <g:if test="${!userColumns || userColumns?.columns?.find { it.column == "discount" }?.enabled}">
                <div class="col font-weight-bold"><g:link action="promotion" params="[promotionSaleId: promotionSaleId, max: max, offset: offset, sortColumn: 'discount', sortOrder: sortColumn == 'discount' ? sortOrder == 'asc' ? 'desc' : 'asc' : 'asc']">Discount</g:link></div>
            </g:if>
            <g:if test="${!userColumns || userColumns?.columns?.find { it.column == "discountedPrice" }?.enabled}">
                <div class="col font-weight-bold"><g:link action="promotion" params="[promotionSaleId: promotionSaleId, max: max, offset: offset, sortColumn: 'discountedPrice', sortOrder: sortColumn == 'discountedPrice' ? sortOrder == 'asc' ? 'desc' : 'asc' : 'asc']">Discounted Price</g:link></div>
            </g:if>
            <g:if test="${!userColumns || userColumns?.columns?.find { it.column == "discountedProfit" }?.enabled}">
                <div class="col font-weight-bold"><g:link action="promotion" params="[promotionSaleId: promotionSaleId, max: max, offset: offset, sortColumn: 'discountedProfit', sortOrder: sortColumn == 'discountedProfit' ? sortOrder == 'asc' ? 'desc' : 'asc' : 'asc']">Discounted Profit</g:link></div>
            </g:if>
            <g:if test="${!userColumns || userColumns?.columns?.find { it.column == "discountedMargin" }?.enabled}">
                <div class="col font-weight-bold"><g:link action="promotion" params="[promotionSaleId: promotionSaleId, max: max, offset: offset, sortColumn: 'discountedMargin', sortOrder: sortColumn == 'discountedMargin' ? sortOrder == 'asc' ? 'desc' : 'asc' : 'asc']">Discounted Margin</g:link></div>
            </g:if>
            <g:if test="${!userColumns || userColumns?.columns?.find { it.column == "vat" }?.enabled}">
                <div class="col font-weight-bold"><g:link action="promotion" params="[promotionSaleId: promotionSaleId, max: max, offset: offset, sortColumn: 'vat', sortOrder: sortColumn == 'vat' ? sortOrder == 'asc' ? 'desc' : 'asc' : 'asc']">VAT</g:link></div>
            </g:if>
        </div>

        <div id="search-results" class="align-content-center">
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
            <div class="text-right">Displaying ${promotionSaleProducts?.size()} of ${totalResults} results.</div>
            <g:paginate total="$totalResults" offset="$offset" max="$max" params="[promotionSaleId: promotionSaleId, sortColumn: sortColumn, sortOrder: sortOrder]" />
        </div>
    </section>
</body>
</html>