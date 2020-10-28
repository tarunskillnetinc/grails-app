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
    <g:render template="/nav/reporting" model="[active: 'sales']" />

    <section id="reporting-container" class="container-fluid">
        <div class="row header-wl">
            <h2 class="mx-auto">Sales Report</h2>
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
                        <g:form class="form-inline" action="salesProduct" params="[productId: productId, max: max, offset: offset, sortColumn: sortColumn, sortOrder: sortOrder]">
                            <div class="form-group">
                                <g:textField name="searchText" placeholder="Description search" maxlength="100" value="${searchText}" class="form-control bottom-border" />
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
                                <g:checkBox name="columns" id="columnsDescription" class="form-check-input" value="description" checked="${!userColumns || userColumns?.columns?.find { it.column == 'description' }?.enabled}" />
                                <label class="form-check-label" for="columnsDescription">Description</label>
                            </div>
                            <div class="form-group form-check">
                                <g:checkBox name="columns" id="columnsQuantity" class="form-check-input" value="quantity" checked="${!userColumns || userColumns?.columns?.find { it.column == 'quantity' }?.enabled}" />
                                <label class="form-check-label" for="columnsQuantity">Total Qty</label>
                            </div>
                            <div class="form-group form-check">
                                <g:checkBox name="columns" id="columnsCostPrice" class="form-check-input" value="costPrice" checked="${!userColumns || userColumns?.columns?.find { it.column == 'costPrice' }?.enabled}" />
                                <label class="form-check-label" for="columnsCostPrice">Cost Price</label>
                            </div>
                            <div class="form-group form-check">
                                <g:checkBox name="columns" id="columnsNetTotal" class="form-check-input" value="netTotal" checked="${!userColumns || userColumns?.columns?.find { it.column == 'netTotal' }?.enabled}" />
                                <label class="form-check-label" for="columnsNetTotal">Net Total</label>
                            </div>
                            <div class="form-group form-check">
                                <g:checkBox name="columns" id="columnsVatAmount" class="form-check-input" value="vatAmount" checked="${!userColumns || userColumns?.columns?.find { it.column == 'vatAmount' }?.enabled}" />
                                <label class="form-check-label" for="columnsVatAmount">VAT Amount</label>
                            </div>
                            <div class="form-group form-check">
                                <g:checkBox name="columns" id="columnsProfit" class="form-check-input" value="profit" checked="${!userColumns || userColumns?.columns?.find { it.column == 'profit' }?.enabled}" />
                                <label class="form-check-label" for="columnsProfit">Profit</label>
                            </div>
                            <div class="form-group form-check">
                                <g:checkBox name="columns" id="columnsMargin" class="form-check-input" value="margin" checked="${!userColumns || userColumns?.columns?.find { it.column == 'margin' }?.enabled}" />
                                <label class="form-check-label" for="columnsMargin">Margin</label>
                            </div>
                            <div class="form-group form-check">
                                <g:checkBox name="columns" id="columnsUsersName" class="form-check-input" value="usersName" checked="${!userColumns || userColumns?.columns?.find { it.column == 'usersName' }?.enabled}" />
                                <label class="form-check-label" for="columnsUsersName">User</label>
                            </div>
                            <div class="form-group form-check">
                                <g:checkBox name="columns" id="columnsDateCreated" class="form-check-input" value="dateCreated" checked="${!userColumns || userColumns?.columns?.find { it.column == 'dateCreated' }?.enabled}" />
                                <label class="form-check-label" for="columnsDateCreated">Timestamp</label>
                            </div>

                            <button type="button" class="btn btn-wl" onclick="saveReportColumns(saveReportColumnsUrl, 'SALES_PRODUCT');">Apply</button>
                        </g:form>
                    </div>
                </div>
            </div>
        </div>

        <div class="row mt-5 mb-2 ml-0 mr-0 table-wl">
            <g:if test="${!userColumns || userColumns?.columns?.find { it.column == "description" }?.enabled}">
                <div class="col-3 font-weight-bold"><g:link action="salesCategory" params="[categoryId: categoryId, max: max, offset: offset, sortColumn: 'description', sortOrder: sortColumn == 'description' ? sortOrder == 'asc' ? 'desc' : 'asc' : 'asc']">Description</g:link></div>
            </g:if>
            <g:if test="${!userColumns || userColumns?.columns?.find { it.column == "quantity" }?.enabled}">
                <div class="col-1 font-weight-bold"><g:link action="salesProduct" params="[productId: productId, max: max, offset: offset, sortColumn: 'quantity', sortOrder: sortColumn == 'quantity' ? sortOrder == 'asc' ? 'desc' : 'asc' : 'asc']">Qty Sold</g:link></div>
            </g:if>
            <g:if test="${!userColumns || userColumns?.columns?.find { it.column == "costPrice" }?.enabled}">
                <div class="col font-weight-bold"><g:link action="salesProduct" params="[productId: productId, max: max, offset: offset, sortColumn: 'costPrice', sortOrder: sortColumn == 'costPrice' ? sortOrder == 'asc' ? 'desc' : 'asc' : 'asc']">Cost Price</g:link></div>
            </g:if>
            <g:if test="${!userColumns || userColumns?.columns?.find { it.column == "netTotal" }?.enabled}">
                <div class="col font-weight-bold"><g:link action="salesProduct" params="[productId: productId, max: max, offset: offset, sortColumn: 'netTotal', sortOrder: sortColumn == 'netTotal' ? sortOrder == 'asc' ? 'desc' : 'asc' : 'asc']">Net Total</g:link></div>
            </g:if>
            <g:if test="${!userColumns || userColumns?.columns?.find { it.column == "vatAmount" }?.enabled}">
                <div class="col font-weight-bold"><g:link action="salesProduct" params="[productId: productId, max: max, offset: offset, sortColumn: 'vatAmount', sortOrder: sortColumn == 'vatAmount' ? sortOrder == 'asc' ? 'desc' : 'asc' : 'asc']">VAT Amount</g:link></div>
            </g:if>
            <g:if test="${!userColumns || userColumns?.columns?.find { it.column == "profit" }?.enabled}">
                <div class="col font-weight-bold"><g:link action="salesProduct" params="[productId: productId, max: max, offset: offset, sortColumn: 'profit', sortOrder: sortColumn == 'profit' ? sortOrder == 'asc' ? 'desc' : 'asc' : 'asc']">Profit</g:link></div>
            </g:if>
            <g:if test="${!userColumns || userColumns?.columns?.find { it.column == "margin" }?.enabled}">
                <div class="col font-weight-bold"><g:link action="salesProduct" params="[productId: productId, max: max, offset: offset, sortColumn: 'margin', sortOrder: sortColumn == 'margin' ? sortOrder == 'asc' ? 'desc' : 'asc' : 'asc']">Margin</g:link></div>
            </g:if>
            <g:if test="${!userColumns || userColumns?.columns?.find { it.column == "usersName" }?.enabled}">
                <div class="col font-weight-bold"><g:link action="salesProduct" params="[productId: productId, max: max, offset: offset, sortColumn: 'usersName', sortOrder: sortColumn == 'usersName' ? sortOrder == 'asc' ? 'desc' : 'asc' : 'asc']">User</g:link></div>
            </g:if>
            <g:if test="${!userColumns || userColumns?.columns?.find { it.column == "dateCreated" }?.enabled}">
                <div class="col font-weight-bold"><g:link action="salesProduct" params="[productId: productId, max: max, offset: offset, sortColumn: 'dateCreated', sortOrder: sortColumn == 'dateCreated' ? sortOrder == 'asc' ? 'desc' : 'asc' : 'asc']">Timestamp</g:link></div>
            </g:if>
        </div>

        <div id="search-results" class="align-content-center">
            <g:if test="${!sales || sales?.size() == 0}">
                <div id="noResultsRow" class="col pt-2 text-center my-auto">No results found.</div>
            </g:if>

            <g:each in="${sales}" var="sale" status="i">
                <div class="row ml-0 mr-0 pt-2 pb-2 wl-striped${i%2}">
                    <g:if test="${!userColumns || userColumns?.columns?.find { it.column == "description" }?.enabled}">
                        <div class="col-3 my-auto">${sale.productItemCode} - ${sale.productDescription} - ${sale.productUnitSize}</div>
                    </g:if>
                    <g:if test="${!userColumns || userColumns?.columns?.find { it.column == "quantity" }?.enabled}">
                        <div class="col-1 my-auto">${sale.quantity}</div>
                    </g:if>
                    <g:if test="${!userColumns || userColumns?.columns?.find { it.column == "costPrice" }?.enabled}">
                        <div class="col my-auto">&pound;${sale.costPrice}</div>
                    </g:if>
                    <g:if test="${!userColumns || userColumns?.columns?.find { it.column == "netTotal" }?.enabled}">
                        <div class="col my-auto">&pound;${sale.retailPrice.subtract(sale.vatAmount)}</div>
                    </g:if>
                    <g:if test="${!userColumns || userColumns?.columns?.find { it.column == "vatAmount" }?.enabled}">
                        <div class="col my-auto">&pound;${sale.vatAmount}</div>
                    </g:if>
                    <g:if test="${!userColumns || userColumns?.columns?.find { it.column == "profit" }?.enabled}">
                        <div class="col my-auto">&pound;${sale.retailPrice.subtract(sale.costPrice).subtract(sale.vatAmount)}</div>
                    </g:if>
                    <g:if test="${!userColumns || userColumns?.columns?.find { it.column == "margin" }?.enabled}">
                        <div class="col my-auto">${sale.margin}&#37;</div>
                    </g:if>
                    <g:if test="${!userColumns || userColumns?.columns?.find { it.column == "usersName" }?.enabled}">
                        <div class="col my-auto">${sale.usersName}</div>
                    </g:if>
                    <g:if test="${!userColumns || userColumns?.columns?.find { it.column == "dateCreated" }?.enabled}">
                        <div class="col my-auto"><g:formatDate date="${sale.dateCreated}" format="dd/MM/yyyy HH:mm:ss" /></div>
                    </g:if>
                </div>
            </g:each>
        </div>

        <div class="my-3 text-right">
            <div class="text-right">Displaying ${sales.size()} of ${totalResults} results.</div>
            <g:paginate total="$totalResults" offset="$offset" max="$max" params="[productId: productId, sortColumn: sortColumn, sortOrder: sortOrder]" />
        </div>
    </section>
</body>
</html>