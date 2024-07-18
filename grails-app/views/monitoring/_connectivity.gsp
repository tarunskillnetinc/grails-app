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
            <div id="store-id-${i + 1}" class="col-1 my-auto text-center">${rabbitQueue.storeId}</div>
            <div id="till-id-${i + 1}" class="col-1 my-auto text-center">${rabbitQueue.tillId}</div>
            <div id="messages-waiting-${i + 1}" class="col-2 my-auto text-center">${rabbitQueue.messages}</div>
            <div id="latest-queue-activity-${i + 1}" class="col-2 my-auto text-center"><g:formatDate format="dd/MM/yyyy HH:mm:ss" date="${rabbitQueue?.idle_since?.toDate() ?: new Date()}"  timeZone="Europe/London"/></div>
            <div class="col-1 my-auto text-center"><span id="status-${i + 1}" class="badge badge-${rabbitQueue.consumers > 0 ? 'success' : 'danger'}">${rabbitQueue.consumers > 0 ? 'Online' : 'Offline'}</span></div>
            <div class="col-3 my-auto text-center">
                <button id="clear-${i + 1}" class="btn btn-wl mx-2" onclick="purgeQueue(${rabbitQueue.storeId}, ${rabbitQueue.tillId})">Clear</button>
                <button id="sync-${i + 1}" class="btn btn-wl mx-2" onclick="forceSync(${rabbitQueue.storeId}, ${rabbitQueue.tillId})">Sync</button>
                <button id="delete-${i + 1}" class="btn btn-danger mx-2" onclick="deleteQueue(${rabbitQueue.storeId}, ${rabbitQueue.tillId})">Delete</button>
            </div>
        </div>
    </g:each>
</div>