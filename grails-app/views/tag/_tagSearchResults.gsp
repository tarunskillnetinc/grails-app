<g:if test="${!tags || tags?.size() == 0}">
    <div id="noResultsRow" class="col-8 offset-2 pt-2 pb-2 my-auto text-center wl-striped0">No results found.</div>
</g:if>

<g:each in="${tags}" var="tag" status="i">
    <div class="row col-8 offset-2 pt-2 pb-2 wl-striped${i%2} hoverable pointer" title="Click to view." onclick="document.location.href='${createLink(action:'show', id: tag.id)}';">
        <div class="col-3">${tag.id}</div>
        <div class="col-6">${tag.description}</div>
        <div class="col-3">${tag.products?.size()}</div>
    </div>
</g:each>

<div class="my-3 text-right">
    <util:remotePaginate action="ajaxGetTaga" total="${tags?.totalCount ?: 0}" update="search-results" offset="${offset ?: 0}" max="${max ?: 50}" params="['searchTerm': searchTerm]" />
</div>