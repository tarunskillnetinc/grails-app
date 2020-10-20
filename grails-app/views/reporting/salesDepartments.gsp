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
                    <g:form class="form-inline" action="salesDepartments" params="[max: max, offset: offset, sortColumn: sortColumn, sortOrder: sortOrder]">
                        <div class="form-group">
                            <g:textField name="searchText" placeholder="Description search" maxlength="100" value="${searchText}" class="form-control bottom-border" />
                        </div>
                        <g:submitButton name="Search" class="btn btn-wl" />
                    </g:form>
                </div>
            </div>
        </div>

        <div class="row mt-5 mb-2 ml-0 mr-0 table-wl">
            <div class="col-3 font-weight-bold"><g:link action="salesDepartments" params="[max: max, offset: offset, sortColumn: 'description', sortOrder: sortColumn == 'description' ? sortOrder == 'asc' ? 'desc' : 'asc' : 'asc']">Description</g:link></div>
            <div class="col font-weight-bold"><g:link action="salesDepartments" params="[max: max, offset: offset, sortColumn: 'quantity', sortOrder: sortColumn == 'quantity' ? sortOrder == 'asc' ? 'desc' : 'asc' : 'asc']">Total Qty</g:link></div>
            <div class="col font-weight-bold"><g:link action="salesDepartments" params="[max: max, offset: offset, sortColumn: 'avgCostPrice', sortOrder: sortColumn == 'avgCostPrice' ? sortOrder == 'asc' ? 'desc' : 'asc' : 'asc']">Avg Cost Price</g:link></div>
            <div class="col font-weight-bold"><g:link action="salesDepartments" params="[max: max, offset: offset, sortColumn: 'avgRetailPrice', sortOrder: sortColumn == 'avgRetailPrice' ? sortOrder == 'asc' ? 'desc' : 'asc' : 'asc']">Avg Sales Price</g:link></div>
            <div class="col font-weight-bold"><g:link action="salesDepartments" params="[max: max, offset: offset, sortColumn: 'retailPrice', sortOrder: sortColumn == 'retailPrice' ? sortOrder == 'asc' ? 'desc' : 'asc' : 'asc']">Total Sales</g:link></div>
            <div class="col font-weight-bold"><g:link action="salesDepartments" params="[max: max, offset: offset, sortColumn: 'vatAmount', sortOrder: sortColumn == 'vatAmount' ? sortOrder == 'asc' ? 'desc' : 'asc' : 'asc']">VAT Amount</g:link></div>
            <div class="col font-weight-bold"><g:link action="salesDepartments" params="[max: max, offset: offset, sortColumn: 'avgMargin', sortOrder: sortColumn == 'avgMargin' ? sortOrder == 'asc' ? 'desc' : 'asc' : 'asc']">Avg Margin</g:link></div>
        </div>

        <div id="search-results" class="align-content-center">
            <g:if test="${!sales || sales?.size() == 0}">
                <div id="noResultsRow" class="col pt-2 text-center my-auto">No results found.</div>
            </g:if>

            <g:each in="${sales}" var="sale" status="i">
                <div class="row ml-0 mr-0 pt-2 pb-2 wl-striped${i%2}" style="cursor: pointer;" onclick="document.location.href='${createLink(action:'salesCategory', params: [categoryId: sale.salesCategories.first().categoryId])}';">
                    <div class="col-3 my-auto">${sale.productDescription}</div>
                    <div class="col my-auto">${sale.quantity}</div>
                    <div class="col my-auto">&pound;${sale.avgCostPrice}</div>
                    <div class="col my-auto">&pound;${sale.avgRetailPrice}</div>
                    <div class="col my-auto">&pound;${sale.retailPrice}</div>
                    <div class="col my-auto">&pound;${sale.vatAmount}</div>
                    <div class="col my-auto">${sale.avgMargin}&#37;</div>
                </div>
            </g:each>
        </div>

        <div class="my-3 text-right">
            <div class="text-right">Displaying ${sales.size()} of ${totalResults} results.</div>
            <g:paginate total="$totalResults" offset="$offset" max="$max" params="[sortColumn: sortColumn, sortOrder: sortOrder]" />
        </div>
    </section>
</body>
</html>