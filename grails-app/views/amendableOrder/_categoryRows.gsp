<g:each in="${amendedLines}" var="amendedLine" status="i">
    <div id="amend-result-${i+1}" class="row ml-0 mr-0 px-0 pt-2 pb-2 wl-striped${row++%2}">
        <g:if test="${!userColumns || userColumns?.columns?.find { it.column == "sku" }?.enabled}">
            <div id="amend-result-${i+1}-sku" class="col-2">
                ${amendedLine.sku}
            </div>
        </g:if>
        <g:if test="${!userColumns || userColumns?.columns?.find { it.column == "productDescription" }?.enabled}">
            <div id="amend-result-${i+1}-productDescription" class="col-1">
                ${amendedLine.productDescription}
            </div>
        </g:if>
        <g:if test="${!userColumns || userColumns?.columns?.find { it.column == "price" }?.enabled}">
            <div id="amend-result-${i+1}-price" class="col-1">
                ${amendedLine.price}
            </div>
        </g:if>
        <g:if test="${!userColumns || userColumns?.columns?.find { it.column == "caseSize" }?.enabled}">
            <div id="amend-result-${i+1}-caseSize" class="col-1">
                ${amendedLine.packQuantity}
            </div>
        </g:if>
        <g:if test="${!userColumns || userColumns?.columns?.find { it.column == "deliveryDate" }?.enabled}">
            <div id="order-result-${i+1}-deliveryDate" class="col-1 text-truncate">
                ${g.formatDate(format:"dd/MM/yyyy", date:amendedLine?.deliveryDate?.toDate())}
            </div>
        </g:if>
        <g:if test="${!userColumns || userColumns?.columns?.find { it.column == "demand" }?.enabled}">
            <div id="amend-result-${i+1}-demand" class="col-1">
                ${amendedLine.demand}
            </div>
        </g:if>
        <g:if test="${!userColumns || userColumns?.columns?.find { it.column == "available" }?.enabled}">
            <div id="amend-result-${i+1}-available" class="col-1">
                ${amendedLine.available}
            </div>
        </g:if>
        <g:if test="${!userColumns || userColumns?.columns?.find { it.column == "orderQuantity" }?.enabled}">
            <div id="amend-result-${i+1}-orderQuantity" class="col-1">
                ${amendedLine.originalOrderQuantity}
            </div>
        </g:if>
        <g:if test="${!userColumns || userColumns?.columns?.find { it.column == "amendedOrderQuantity" }?.enabled}">
            <div id="amend-result-${i+1}-amendedOrderQuantity" class="col-1">
                <g:textField id="amendedLines[${i}].amendedOrderQuantity" name="amendedLines[${i}].amendedOrderQuantity" maxlength="100" value="${amendedLine.amendedOrderQuantity}" class="form-control" />
            </div>
        </g:if>
        <g:hiddenField name="amendedLines[${i}].originalOrderQuantity" value="${amendedLine.originalOrderQuantity}" />
        <g:hiddenField name="amendedLines[${i}].productListItemId" value="${amendedLine.productListItemId}" />
    </div>
</g:each>
