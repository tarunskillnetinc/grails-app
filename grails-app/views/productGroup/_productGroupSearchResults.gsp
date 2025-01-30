<script type="application/javascript">
    $(document).ready(function() {
        // Assuming your page buttons have a specific class (e.g., "page-button")
        $('.step').on('click', function() {
            // Scroll to the top of the page with a smooth animation
            $('html, body').animate({ scrollTop: 0 }, 'fast');
        });
    });
</script>

<g:if test="${!productGroups || productGroups?.size() == 0}">
    <div id="noResultsRow" class="col pt-2 pb-2 my-auto text-center wl-striped0">No results found.</div>
</g:if>

<g:each in="${productGroups}" var="productGroup" status="i">
    <div id="productGroup-${i + 1}" class="row ml-0 mr-0 pt-2 pb-2 wl-striped${i % 2} hoverable pointer"
         title="Click to view." onclick="document.location.href = '${createLink(action:'show', id: productGroup?.id)}';">
        <div id="productGroup-${i + 1}-id" class="col-1">${productGroup?.id}</div>
        <div id="productGroup-${i + 1}-description" class="col-2">${productGroup?.name}</div>
        <div id="productGroup-${i + 1}-description" class="col-2">
            <g:if test="${productGroup?.startDate}">
                <g:formatDate format="dd/MM/yyyy" date="${productGroup?.startDate?.toDate()}"/>
            </g:if>
            <g:else>&nbsp;</g:else>
        </div>
        <div id="productGroup-${i + 1}-description" class="col-2">
            <g:if test="${productGroup?.endDate}">
                <g:formatDate format="dd/MM/yyyy" date="${productGroup?.endDate?.toDate()}"/>
            </g:if>
            <g:else>&nbsp;</g:else>
        </div>
        <div id="productGroup-${i + 1}-description" class="col-2">${productGroup?.timeRestriction}</div>
        <div id="productGroup-${i + 1}-prod-count" class="col-2">${productGroup?.productGroupProducts?.size()}</div>
        <div id="productGroup-${i + 1}-max-sell-quantity" class="col-1">
            <g:if test="${productGroup?.active}">Active</g:if>
            <g:else>Inactive</g:else>
        </div>
    </div>
</g:each>

<div class="my-3 text-right">
    <util:remotePaginate action="ajaxGetProductGroups" total="${productGroups?.totalCount ?: 0}" update="search-results"
                         offset="${offset ?: 0}" max="${max ?: 50}" params="['searchTerm': searchTerm]"/>
</div>