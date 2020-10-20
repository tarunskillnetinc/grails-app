<!doctype html>
<html>
<head>
    <meta name="layout" content="main" />

    <title>WonderLane</title>
</head>
<body>
    <g:render template="/nav/reporting" model="[active: 'sales']" />

<section id="reporting-container" class="container-fluid">
    <div class="row header-wl">
        <h2 class="mx-auto">Sales Report</h2>
    </div>

    <div class="col-8 offset-2 mt-4">
        <div class="card bg-light border-wl">
            <div class="card-header" data-toggle="collapse" data-target="#filterCollapse" aria-expanded="false" aria-controls="collapseExample">
                Filters
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

    <div class="row mt-5 mb-2 ml-0 mr-0 table-wl">
        <div class="col-3 font-weight-bold"><g:link action="salesCategory" params="[categoryId: categoryId, max: max, offset: offset, sortColumn: 'description', sortOrder: sortColumn == 'description' ? sortOrder == 'asc' ? 'desc' : 'asc' : 'asc']">Description</g:link></div>
        <div class="col-1 font-weight-bold"><g:link action="salesProduct" params="[productId: productId, max: max, offset: offset, sortColumn: 'quantity', sortOrder: sortColumn == 'quantity' ? sortOrder == 'asc' ? 'desc' : 'asc' : 'asc']">Qty Sold</g:link></div>
        <div class="col font-weight-bold"><g:link action="salesProduct" params="[productId: productId, max: max, offset: offset, sortColumn: 'costPrice', sortOrder: sortColumn == 'costPrice' ? sortOrder == 'asc' ? 'desc' : 'asc' : 'asc']">Cost Price</g:link></div>
        <div class="col font-weight-bold"><g:link action="salesProduct" params="[productId: productId, max: max, offset: offset, sortColumn: 'netTotal', sortOrder: sortColumn == 'netTotal' ? sortOrder == 'asc' ? 'desc' : 'asc' : 'asc']">Net Total</g:link></div>
        <div class="col font-weight-bold"><g:link action="salesProduct" params="[productId: productId, max: max, offset: offset, sortColumn: 'vatAmount', sortOrder: sortColumn == 'vatAmount' ? sortOrder == 'asc' ? 'desc' : 'asc' : 'asc']">VAT Amount</g:link></div>
        <div class="col font-weight-bold"><g:link action="salesProduct" params="[productId: productId, max: max, offset: offset, sortColumn: 'profit', sortOrder: sortColumn == 'profit' ? sortOrder == 'asc' ? 'desc' : 'asc' : 'asc']">Profit</g:link></div>
        <div class="col font-weight-bold"><g:link action="salesProduct" params="[productId: productId, max: max, offset: offset, sortColumn: 'margin', sortOrder: sortColumn == 'margin' ? sortOrder == 'asc' ? 'desc' : 'asc' : 'asc']">Margin</g:link></div>
        <div class="col font-weight-bold"><g:link action="salesProduct" params="[productId: productId, max: max, offset: offset, sortColumn: 'usersName', sortOrder: sortColumn == 'usersName' ? sortOrder == 'asc' ? 'desc' : 'asc' : 'asc']">User</g:link></div>
        <div class="col font-weight-bold"><g:link action="salesProduct" params="[productId: productId, max: max, offset: offset, sortColumn: 'dateCreated', sortOrder: sortColumn == 'dateCreated' ? sortOrder == 'asc' ? 'desc' : 'asc' : 'asc']">Timestamp</g:link></div>
    </div>

    <div id="search-results" class="align-content-center">
        <g:if test="${!sales || sales?.size() == 0}">
            <div id="noResultsRow" class="col pt-2 text-center my-auto">No results found.</div>
        </g:if>

        <g:each in="${sales}" var="sale" status="i">
            <div class="row ml-0 mr-0 pt-2 pb-2 wl-striped${i%2}">
                <div class="col-3 my-auto">${sale.productItemCode} - ${sale.productDescription} - ${sale.productUnitSize}</div>
                <div class="col-1 my-auto">${sale.quantity}</div>
                <div class="col my-auto">&pound;${sale.costPrice}</div>
                <div class="col my-auto">&pound;${sale.retailPrice.subtract(sale.vatAmount)}</div>
                <div class="col my-auto">&pound;${sale.vatAmount}</div>
                <div class="col my-auto">&pound;${sale.retailPrice.subtract(sale.costPrice).subtract(sale.vatAmount)}</div>
                <div class="col my-auto">${sale.margin}&#37;</div>
                <div class="col my-auto">${sale.usersName}</div>
                <div class="col my-auto"><g:formatDate value="${sale.dateCreated}" format="dd/MM/yyyy HH:mm:ss" /></div>
            </div>
        </g:each>
    </div>

    <div class="my-3 text-right">
        <div class="text-right">Displaying ${sales.size()} of ${totalResults} results.</div>
        <g:paginate total="$totalResults" offset="$offset" max="$max" params="[productId: productId, sortColumn: sortColumn, sortOrder: sortOrder]" />
    </div>
</body>
</html>