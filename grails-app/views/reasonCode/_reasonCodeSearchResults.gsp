<g:if test="${!reasonCodes || reasonCodes?.size() == 0}">
    <div id="noResultsRow" class="col pt-2 pb-2 text-center my-auto wl-striped0">No results found.</div>
</g:if>

<g:each in="${reasonCodes}" var="code" status="i">
    <div class="row ml-0 mr-0 pt-2 pb-2 wl-striped${i%2} hoverable">
        <div id="desc-${i + 1}" class="col-4 my-auto text-truncate">${code.description}</div>
        <div id="secret-${i + 1}" class="col-4 my-auto text-truncate">${code.secret}</div>
        <div class="col-4 my-auto text-right">
            <button id="edit-${i + 1}" class="btn btn-wl mx-2" onclick='ajaxEdit("${code.id}");'>Edit</button>
            <button id="delete-${i + 1}" class="btn btn-danger mx-2" onclick='ajaxDelete("${code.id}", "${code.description}")'>Delete</button>
        </div>
    </div>
</g:each>

<div class="my-3 text-right">
    <util:remotePaginate controller="reasonCode" action="ajaxSearch" total="${totalResults ?: 0}" update="search-results" offset="${offset ?: 0}" max="${max ?: 50}" params="[type: type]" onSuccess="\$('html, body').animate({ scrollTop: 0 }, 'fast')"/>
</div>