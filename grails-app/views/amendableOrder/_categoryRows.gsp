<g:set var="counter" value="${0}" scope="request"/>
<g:each in="${amendedLines?.entrySet()}" var="skuGrouping" status="i">
    <div id="amend-result-${counter+1}" class="row ml-0 mr-0 px-0 pt-2 pb-2 wl-striped${row++%2}">
        <g:if test="${!userColumns || userColumns?.columns?.find { it.column == "sku" }?.enabled}">
            <div id="amend-result-${i+1}-sku" class="col-1">
                ${skuGrouping.key.sku}
            </div>
        </g:if>
        <g:if test="${!userColumns || userColumns?.columns?.find { it.column == "productDescription" }?.enabled}">
            <div id="amend-result-${i+1}-productDescription" class="col-2">
                ${skuGrouping.key.productDescription}
            </div>
        </g:if>
        <g:if test="${!userColumns || userColumns?.columns?.find { it.column == "price" }?.enabled}">
            <div id="amend-result-${i+1}-price" class="col-1">
                ${skuGrouping.key.price}
            </div>
        </g:if>
        <g:if test="${!userColumns || userColumns?.columns?.find { it.column == "caseSize" }?.enabled}">
            <div id="amend-result-${i+1}-caseSize" class="col-1">
                ${skuGrouping.key.packQuantity}
            </div>
        </g:if>
        <div id="amend-result-${i+1}-deliveryDates" class="col-1">
            <g:each in="${skuGrouping.value}" var="amendedLine" >
                <div  style="margin-bottom:20px">${g.formatDate(format:"dd/MM/yyyy", date:amendedLine?.deliveryDate?.toDate())}</div>
            </g:each>
        </div>
        <g:if test="${!userColumns || userColumns?.columns?.find { it.column == "demand" }?.enabled}">
            <div id="amend-result-${i+1}-demand" class="col-1">
                ${skuGrouping.key.demand}
            </div>
        </g:if>
        <g:if test="${!userColumns || userColumns?.columns?.find { it.column == "available" }?.enabled}">
            <div id="amend-result-${i+1}-available" class="col-1">
                ${skuGrouping.key.available}
            </div>
        </g:if>
        <g:if test="${!userColumns || userColumns?.columns?.find { it.column == "orderQuantity" }?.enabled}">
            <div id="amend-result-${i+1}-originalOrder" class="col-1">
                <g:each in="${skuGrouping.value}" var="amendedLine" >
                    <div style="margin-bottom: 20px">${amendedLine.originalOrderQuantity}</div>
                </g:each>
            </div>
        </g:if>
        <div id="amend-result-${i+1}-amendedOrder" class="col-1">
            <g:each in="${skuGrouping.value}" var="amendedLine" >
                <g:textField id="amendedLines[${counter}].lines[amendedOrderQuantity" name="amendedLines[${counter}].amendedOrderQuantity" maxlength="100" value="${amendedLine.amendedOrderQuantity}" class="form-control"
                             onfocusout="enforceDecimalLimit(this, 3);" style="margin-bottom:5px"
                             onkeydown="acceptFloat(event)"
                             onkeyup="validateFloatQuantity(this, 0, 999.99, 3)"/>
                <g:hiddenField name="amendedLines[${counter}].originalOrderQuantity" value="${amendedLine.originalOrderQuantity}" />
                <g:hiddenField name="amendedLines[${counter}].productListItemId" value="${amendedLine.productListItemId}" />
                <g:hiddenField name="amendedLines[${counter}].packQuantity" value="${amendedLine.packQuantity}" />
                <g:set var="counter" value="${counter + 1}" />
            </g:each>
        </div>
        <g:if test="${!userColumns || userColumns?.columns?.find { it.column == "messages" }?.enabled}">
            <div id="amend-result-${i+1}-messages" class="col-1">
                ${skuGrouping.key.messages}
            </div>
        </g:if>
    </div>
</g:each>