<g:if test="${!tags || tags?.size() == 0}">
    <div id="noResultsRow" class="col-8 offset-2 pt-2 pb-2 my-auto text-center wl-striped0">No results found.</div>
</g:if>

<g:each in="${tags}" var="tag" status="i">
    <div id="tag-${i+1}" class="row col-8 offset-2 pt-2 pb-2 wl-striped${i%2} hoverable pointer" title="Click to view." onclick="document.location.href='${createLink(action:'show', id: tag.id)}';">
        <div id="tag-${i+1}-id" class="col-2">${tag.id}</div>
        <div id="tag-${i+1}-description" class="col-6">${tag.description}</div>
        <div id="tag-${i+1}-prod-count" class="col-2">${tag.tagProducts?.size()}</div>
        <div id="tag-${i+1}-description" class="col-2">${tag.maxSellQuantity}</div>
    </div>
</g:each>

<div class="my-3 text-right">
    <util:remotePaginate action="ajaxGetTags" total="${tags?.totalCount ?: 0}" update="search-results" offset="${offset ?: 0}" max="${max ?: 50}" params="['searchTerm': searchTerm]" />
</div>