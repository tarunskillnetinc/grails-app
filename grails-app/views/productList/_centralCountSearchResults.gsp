<g:if test="${!productLists || productLists?.size() == 0}">
    <div id="noResultsRow" class="col pt-2 text-center">No results found.</div>
</g:if>

<g:each in="${productLists}" var="productList" status="i">
    <div class="row ml-0 mr-0 pt-2 pb-2 wl-striped${i%2} hoverable" style="cursor: pointer;" onclick="document.location.href='${createLink(action:'showCentralCount', id: productList.id)}';">
        <div class="col-1">${productList.id}</div>
        <div class="col">${productList.description}</div>
        <div class="col"><g:message code="ProductListStatus.${productList.status}" /></div>
        <div class="col"><g:formatDate format="dd/MM/yyyy" date="${productList.startDate}" /></div>
        <div class="col"><g:formatDate format="dd/MM/yyyy" date="${productList.endDate}" /></div>
        <div class="col">${productList.ownerUsersName ?: "N/A"}</div>
    </div>
</g:each>

<div class="my-3 text-right">
    <util:remotePaginate action="ajaxGetCentralCounts" total="${productLists?.totalCount ?: 0}" update="search-results" offset="${offset ?: 0}" max="${max ?: 50}" params="['searchTerm': searchTerm]" />
</div>