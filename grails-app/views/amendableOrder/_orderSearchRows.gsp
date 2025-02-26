<g:each in="${orders}" var="order" status="i">
    <div id="order-result-${i+1}" class="row ml-0 mr-0 px-0 pt-2 pb-2 wl-striped${row++%2} hoverable" title="Click to view products" style="cursor: pointer;" onclick="document.location.href='${createLink(controller:'amendableOrder', action: 'viewCategory', params: [categoryId: order.categoryId, storeId: order.storeId])}';">
        <g:if test="${!userColumns || userColumns?.columns?.find { it.column == "category" }?.enabled}">
            <div id="order-result-${i+1}-category" class="col-6">
                ${order.categoryDescription}
            </div>
        </g:if>
        <g:if test="${!userColumns || userColumns?.columns?.find { it.column == "store" }?.enabled}">
            <div id="order-result-${i+1}-store" class="col-2"
            >${order.storeNumber}
            </div>
        </g:if>
        <g:if test="${!userColumns || userColumns?.columns?.find { it.column == "amendableDate" }?.enabled}">
            <div id="order-result-${i+1}-amendableDate" class="col-2 text-truncate">
                ${g.formatDate(format:"dd/MM/yyyy", date:order.amendableDate.toDate())}
            </div>
        </g:if>
    </div>
</g:each>
