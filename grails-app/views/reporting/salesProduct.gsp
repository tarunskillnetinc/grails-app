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
            <g:form class="form-inline" action="salesProduct" params="[productId: productId, max: max, offset: offset, sortColumn: sortColumn, sortOrder: sortOrder]">
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
            <th scope="col"><g:link action="salesProduct" params="[productId: productId, max: max, offset: offset, sortColumn: 'description', sortOrder: sortColumn == 'description' ? sortOrder == 'asc' ? 'desc' : 'asc' : 'asc']">Description</g:link></th>
            <th scope="col"><g:link action="salesProduct" params="[productId: productId, max: max, offset: offset, sortColumn: 'quantity', sortOrder: sortColumn == 'quantity' ? sortOrder == 'asc' ? 'desc' : 'asc' : 'asc']">Qty Sold</g:link></th>
            <th scope="col"><g:link action="salesProduct" params="[productId: productId, max: max, offset: offset, sortColumn: 'costPrice', sortOrder: sortColumn == 'costPrice' ? sortOrder == 'asc' ? 'desc' : 'asc' : 'asc']">Cost Price</g:link></th>
            <th scope="col"><g:link action="salesProduct" params="[productId: productId, max: max, offset: offset, sortColumn: 'netTotal', sortOrder: sortColumn == 'netTotal' ? sortOrder == 'asc' ? 'desc' : 'asc' : 'asc']">Net Total</g:link></th>
            <th scope="col"><g:link action="salesProduct" params="[productId: productId, max: max, offset: offset, sortColumn: 'vatAmount', sortOrder: sortColumn == 'vatAmount' ? sortOrder == 'asc' ? 'desc' : 'asc' : 'asc']">VAT Amount</g:link></th>
            <th scope="col"><g:link action="salesProduct" params="[productId: productId, max: max, offset: offset, sortColumn: 'profit', sortOrder: sortColumn == 'profit' ? sortOrder == 'asc' ? 'desc' : 'asc' : 'asc']">Profit</g:link></th>
            <th scope="col"><g:link action="salesProduct" params="[productId: productId, max: max, offset: offset, sortColumn: 'margin', sortOrder: sortColumn == 'margin' ? sortOrder == 'asc' ? 'desc' : 'asc' : 'asc']">Margin</g:link></th>
            <th scope="col"><g:link action="salesProduct" params="[productId: productId, max: max, offset: offset, sortColumn: 'usersName', sortOrder: sortColumn == 'usersName' ? sortOrder == 'asc' ? 'desc' : 'asc' : 'asc']">User</g:link></th>
            <th scope="col"><g:link action="salesProduct" params="[productId: productId, max: max, offset: offset, sortColumn: 'dateCreated', sortOrder: sortColumn == 'dateCreated' ? sortOrder == 'asc' ? 'desc' : 'asc' : 'asc']">Timestamp</g:link></th>
        </tr>
        </thead>
        <tbody>
            <g:each in="${sales}" var="sale">
                <tr>
                    <td>${sale.productItemCode} - ${sale.productDescription} - ${sale.productUnitSize}</td>
                    <td>${sale.quantity}</td>
                    <td>&pound;${sale.costPrice}</td>
                    <td>&pound;${sale.retailPrice.subtract(sale.vatAmount)}</td>
                    <td>&pound;${sale.vatAmount}</td>
                    <td>&pound;${sale.retailPrice.subtract(sale.costPrice).subtract(sale.vatAmount)}</td>
                    <td>${sale.margin}&#37;</td>
                    <td>${sale.usersName}</td>
                    <td>${sale.dateCreated}</td>
                </tr>
            </g:each>
        </tbody>
    </table>

    <div class="text-right">Displaying ${sales.size()} of ${totalResults} results.</div>
    <g:paginate total="$totalResults" offset="$offset" max="$max" params="[productId: productId, sortColumn: sortColumn, sortOrder: sortOrder]" />
</body>
</html>