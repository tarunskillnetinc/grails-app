<g:if test="${!users || users?.size() == 0}">
    <div id="noResultsRow" class="col-10 pt-2 text-center">No results found.</div>
</g:if>

<g:each in="${users}" var="user" status="i">
    <div class="row ml-0 mr-0 pt-2 pb-2 wl-striped${i%2} hoverable" style="cursor: pointer;" onclick="document.location.href='${createLink(action:'show', id: user.id)}';">
        <div class="col-4">${user.username}</div>
        <div class="col-4">${user.name}</div>
        <div class="col-2"><g:formatDate format="dd/MM/yyyy" date="${user.dateOfBirth}" /></div>
    </div>
</g:each>

<div class="my-3 text-right">
    <util:remotePaginate action="ajaxGetUsers" total="${users?.totalCount ?: 0}" update="search-results" offset="${offset ?: 0}" max="${max ?: 50}" params="['searchTerm': searchTerm]" />
</div>