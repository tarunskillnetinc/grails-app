<!doctype html>
<html>
<head>
    <meta name="layout" content="main" />

    <title>Trust Retail</title>

    <asset:javascript src="reporting.js" />

    <script type='text/javascript'>
        var reportType = "${reportType}";
        var getDataUrl = "${createLink(controller: 'reporting', action: 'ajaxPromotion')}";
        var saveReportColumnsUrl = "${createLink(controller: 'reporting', action: 'ajaxSaveReportColumns')}";

        $(document).ready(function () {
            filterReport();
        });

        function resetForm() {
            $("#descriptionFilter").val("");

            document.getElementById('storeFilter').value = '';
        }
    </script>
</head>
<body>
    <section id="reporting-container" class="container-fluid">
        <g:reportBreadcrumb reportType="${reportType}" promotionSaleId="${promotionSaleId}" startDate="${startDate}" endDate="${endDate}" />

        <div class="header-wl mt-3">
            <h2 id="page-title" class="mx-auto">Promotional Sales Report</h2>
        </div>

        <div class="row mt-4">
            <div class="col-5">
                <div class="card bg-light border-wl">
                    <div id="filter-collapse" class="card-header pointer" data-toggle="collapse" data-target="#filterCollapse" aria-expanded="false" aria-controls="filterCollapse">
                        <div class="row">
                            <div id="filter-text" class="col-10">Filters</div>
                            <div class="col-2 text-right">
                                <svg width="1em" height="1em" viewBox="0 0 16 16" class="bi bi-caret-down-fill text-right" fill="currentColor" xmlns="http://www.w3.org/2000/svg">
                                    <path d="M7.247 11.14L2.451 5.658C1.885 5.013 2.345 4 3.204 4h9.592a1 1 0 0 1 .753 1.659l-4.796 5.48a1 1 0 0 1-1.506 0z"/>
                                </svg>
                            </div>
                        </div>
                    </div>
                    <div class="card-body collapse" id="filterCollapse">
                        <g:form name="filtersForm" id="filtersForm">
                            <g:hiddenField name="promotionSaleId" value="${promotionSaleId}" />

                            <div class="form-group row">
                                <label for="descriptionFilter" class="col-2 col-form-label-sm text-right">Product</label>
                                <div class="col-10">
                                    <g:textField name="descriptionFilter" maxlength="100" value="${descriptionFilter}" class="form-control bottom-border" autocomplete="off" />
                                </div>
                            </div>

                            <div class="form-group row">
                                <label for="storeFilter" class="col-2 col-form-label-sm text-right">Store</label>
                                <div class="col-3">
                                    <g:select name="storeFilter" from="${stores}" optionValue="${{it.config.storeNumber}}"
                                              optionKey="id"
                                              noSelection="${sec.loggedInUserInfo(field: 'storeId') ? ['': sec.loggedInUserInfo(field: 'storeNumber')] : ['': 'All']}"
                                              class="form-control select-border"
                                              disabled="${sec.loggedInUserInfo(field: 'storeId') ? true : false}"></g:select>
                                </div>


                                <div class="col-7 text-right">
                                    <button id="reset-filters-btn" type="button" class="btn btn-danger text-right mr-2" onclick="resetForm()">Reset Filters</button>
                                    <button id="filter-submit-button" type="button" class="btn btn-wl text-right" onclick="filterReport()">Search</button>
                                </div>
                            </div>
                        </g:form>
                    </div>
                </div>
            </div>

            <div class="col-2 offset-3 text-right" style="margin-top: 8px;">
                <button id="export-to-csv" class="btn btn-wl" onclick="exportToCsv();">Export to CSV</button>
            </div>

            <div class="col-2">
                <div class="card bg-light border-wl">
                    <div id="columns-collapse" class="card-header pointer" data-toggle="collapse" data-target="#columnsCollapse" aria-expanded="false" aria-controls="columnsCollapse">
                        <div class="row">
                            <div id="columns-text" class="col-10">Columns</div>
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

                            <button id="columns-submit-button" type="button" class="btn btn-wl" onclick="saveReportColumns();">Apply</button>
                        </g:form>
                    </div>
                </div>
            </div>
        </div>

        <div id="results-container" class="align-content-center">
            <g:render template="promotionsResults" />
        </div>
    </section>
</body>
</html>