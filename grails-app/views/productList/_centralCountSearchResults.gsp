<script type="application/javascript">
    $(document).ready(function() {
        // Assuming your page buttons have a specific class (e.g., "page-button")
        $('.step').on('click', function() {
            // Scroll to the top of the page with a smooth animation
            $('html, body').animate({ scrollTop: 0 }, 'fast');
        });
    });
</script>

<g:if test="${!productLists || productLists?.size() == 0}">
    <div id="noResultsRow" class="col pt-2 pb-2 my-auto text-center wl-striped0">No results found.</div>
</g:if>

<g:each in="${productLists}" var="productList" status="i">
    <div id="prod-list-${i+1}" class="row ml-0 mr-0 pt-2 pb-2 wl-striped${i%2} hoverable" title="Click to view." style="cursor: pointer;" onclick="document.location.href='${createLink(action:'showCentralCount', id: productList.id)}';">
        <div id="prod-list-${i+1}-id" class="col-1">${productList.id}</div>
        <div id="prod-list-${i+1}-store" class="col-1">${productList.store ? productList.store.config.storeNumber : ""}</div>
        <div id="prod-list-${i+1}-description" class="col">${productList.description}</div>
        <div id="prod-list-${i+1}-status" class="col"><g:message code="ProductListStatus.${productList.status}" /></div>
        <div id="prod-list-${i+1}-start-date" class="col"><g:formatDate format="dd/MM/yyyy" date="${productList.startDate?.toDate()}" /></div>
        <div id="prod-list-${i+1}-end-date" class="col"><g:formatDate format="dd/MM/yyyy" date="${productList.endDate?.toDate()}" /></div>
        <div id="prod-list-${i+1}-owner" class="col">${productList.ownerUsersName ?: "N/A"}</div>
    </div>
</g:each>

<div class="my-3 text-right">
    <util:remotePaginate action="ajaxGetCentralCounts" total="${productLists?.totalCount ?: 0}" update="search-results" offset="${offset ?: 0}" max="${max ?: 50}" params="['searchTerm': searchTerm, 'searchBy': searchBy]" />
</div>