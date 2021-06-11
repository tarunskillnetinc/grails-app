<div class="d-flex justify-content-center">
    <div id="loading-indicator" class="spinner-border" role="status" style="display: none;">
        <span class="sr-only">Loading...</span>
    </div>
</div>

<div id="search-results">
    <g:if test="${!rabbitQueues || rabbitQueues?.size() == 0}">
        <div id="noResultsRow" class="col pt-2 pb-2 text-center my-auto wl-striped0">No tills found.</div>
    </g:if>

    <g:each in="${rabbitQueues}" var="rabbitQueue" status="i">
        <div class="row ml-0 mr-0 pt-2 pb-2 wl-striped${i%2} hoverable">
            <div class="col-2 offset-1 my-auto">${rabbitQueue.storeId}</div>
            <div class="col-2 my-auto">${rabbitQueue.tillId}</div>
            <div class="col-2 my-auto">${rabbitQueue.messages}</div>
            <div class="col-2 my-auto"><g:formatDate format="dd/MM/yyyy HH:mm:ss" date="${rabbitQueue.idle_since.toDate()}" /></div>
            <div class="col-2 my-auto"><span class="badge badge-${rabbitQueue.consumers > 0 ? 'success' : 'danger'}">${rabbitQueue.consumers > 0 ? 'Online' : 'Offline'}</span></div>
        </div>
    </g:each>
</div>