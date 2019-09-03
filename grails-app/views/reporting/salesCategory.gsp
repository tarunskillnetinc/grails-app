<!doctype html>
<html>
<head>
    <meta name="layout" content="main" />

    <title>WonderLane</title>
</head>
<body>

    <g:render template="/nav/epos" model="[active: 'reporting']" />

    <br /><br />

    <div class="card bg-light">
        <div class="card-header">
            Filter
        </div>
        <div class="card-body">
            <g:form class="form-inline" action="salesCategory" params="[categoryId: categoryId, max: max, offset: offset, sortColumn: sortColumn, sortOrder: sortOrder]">
                <div class="form-group">
                    <g:textField name="searchText" placeholder="Description search" maxlength="100" value="${searchText}" class="form-control bottom-border" />
                </div>
                <g:submitButton name="Search" class="btn btn-primary" />
            </g:form>
        </div>
    </div>

    <br /><br />

    <table class="table">
        <thead>
        <tr>
            <th scope="col"><g:link action="salesCategory" params="[categoryId: categoryId, max: max, offset: offset, sortColumn: 'description', sortOrder: sortColumn == 'description' ? sortOrder == 'asc' ? 'desc' : 'asc' : 'asc']">Description</g:link></th>
            <th scope="col"><g:link action="salesCategory" params="[categoryId: categoryId, max: max, offset: offset, sortColumn: 'quantity', sortOrder: sortColumn == 'quantity' ? sortOrder == 'asc' ? 'desc' : 'asc' : 'asc']">Total Qty</g:link></th>
            <th scope="col"><g:link action="salesCategory" params="[categoryId: categoryId, max: max, offset: offset, sortColumn: 'avgCostPrice', sortOrder: sortColumn == 'avgCostPrice' ? sortOrder == 'asc' ? 'desc' : 'asc' : 'asc']">Avg Cost Price</g:link></th>
            <th scope="col"><g:link action="salesCategory" params="[categoryId: categoryId, max: max, offset: offset, sortColumn: 'avgRetailPrice', sortOrder: sortColumn == 'avgRetailPrice' ? sortOrder == 'asc' ? 'desc' : 'asc' : 'asc']">Avg Sales Price</g:link></th>
            <th scope="col"><g:link action="salesCategory" params="[categoryId: categoryId, max: max, offset: offset, sortColumn: 'retailPrice', sortOrder: sortColumn == 'retailPrice' ? sortOrder == 'asc' ? 'desc' : 'asc' : 'asc']">Total Sales</g:link></th>
            <th scope="col"><g:link action="salesCategory" params="[categoryId: categoryId, max: max, offset: offset, sortColumn: 'vatAmount', sortOrder: sortColumn == 'vatAmount' ? sortOrder == 'asc' ? 'desc' : 'asc' : 'asc']">VAT Amount</g:link></th>
            <th scope="col"><g:link action="salesCategory" params="[categoryId: categoryId, max: max, offset: offset, sortColumn: 'avgMargin', sortOrder: sortColumn == 'avgMargin' ? sortOrder == 'asc' ? 'desc' : 'asc' : 'asc']">Avg Margin</g:link></th>
        </tr>
        </thead>
        <tbody>
            <g:each in="${sales}" var="sale">
                <tr>
                    <td>
                        <g:if test="${sale.productItemCode}">
                            <g:link action="salesProduct" params="[productId: sale.productId]">${sale.productItemCode} - ${sale.productDescription} - ${sale.productUnitSize}</g:link>
                        </g:if>
                        <g:else>
                            <g:link action="salesCategory" params="[categoryId: sale.salesCategories.first().categoryId]">${sale.productDescription}</g:link>
                        </g:else>
                    </td>
                    <td>${sale.quantity}</td>
                    <td>&pound;${sale.avgCostPrice}</td>
                    <td>&pound;${sale.avgRetailPrice}</td>
                    <td>&pound;${sale.retailPrice}</td>
                    <td>&pound;${sale.vatAmount}</td>
                    <td>${sale.avgMargin}&#37;</td>
                </tr>
            </g:each>
        </tbody>
    </table>

    <div class="text-right">Displaying ${sales.size()} of ${totalResults} results.</div>
    <g:paginate total="$totalResults" offset="$offset" max="$max" params="[categoryId: categoryId, sortColumn: sortColumn, sortOrder: sortOrder]" />
</body>
</html>