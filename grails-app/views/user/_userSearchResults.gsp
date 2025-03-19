<g:if test="${!users || users?.size() == 0}">
    <div id="noResultsRow" class="col-12 pt-2 pb-2 text-center wl-striped0">No results found.</div>
</g:if>

<g:each in="${users}" var="user" status="i">
    <div class="row ml-0 mr-0 pt-2 pb-2 wl-striped${i%2} hoverable" title="Click to edit." style="cursor: pointer;" onclick="document.location.href='${createLink(action:'userEdit', id: user.id)}';">
        <div id="username-${i + 1}" class="col-4" style="word-break: break-word;">${user.username}</div>
        <div id="name-${i + 1}" class="col-4" style="word-break: break-word;">${user.name}</div>
        <div id="homeStore-${i + 1}" class="col-2">${user?.getStoreIdentifier() ?: 'N/A'}</div>
        <div id="status-${i + 1}" class="col-2">${user.active ? 'Active' : 'Inactive'}</div>
    </div>
</g:each>

<div class="my-3 text-right">
    <util:remotePaginate action="ajaxGetUsers" total="${users?.totalCount ?: 0}" update="search-results" offset="${offset ?: 0}" max="${max ?: 50}" params="[
            userNameFilter: userNameFilter,
            homeStoreFilter: homeStoreFilter,
            showInactiveUserFilter: showInactiveUserFilter

    ]" />
</div>