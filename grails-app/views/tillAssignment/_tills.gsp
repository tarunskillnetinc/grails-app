<div class="d-flex justify-content-center">
    <div id="loading-indicator" class="spinner-border" role="status" style="display: none;">
        <span class="sr-only">Loading...</span>
    </div>
</div>

<div id="search-results">
    <g:if test="${!tillList || tillList?.size() == 0}">
        <div id="noResultsRow" class="col pt-2 pb-2 text-center my-auto wl-striped0">No till configurations found.</div>
    </g:if>

    <g:each in="${tillList}" var="tills" status="i">
        <div class="row ml-0 mr-0 pt-2 pb-2 wl-striped${i%2} hoverable">
            <div id="store-id-${i + 1}" class="col-1 my-auto text-center">${tillList[i].storeId}</div>
            <div id="till-id-${i + 1}" class="col-1 my-auto text-center">${tillList[i].tillId}</div>
            <div id="serial-number-${i + 1}" class="col-2 my-auto text-center">${tillList[i].serialNumber}</div>
            <div class="col-3 my-auto text-center">
                <button id="clear-${i + 1}" class="btn btn-wl mx-2" onclick="purgeQueue(${tillList[i].storeId}, ${tillList[i].tillId})">Edit Till</button>
                <button id="sync-${i + 1}" class="btn btn-wl mx-2" onclick="forceSync(${tillList[i].storeId}, ${tillList[i].tillId})">Delete Till</button>
                <button id="delete-${i + 1}" class="btn btn-danger mx-2" onclick="deleteQueue(${tillList[i].storeId}, ${tillList[i].tillId})">Advanced Configuration</button>
            </div>
        </div>
    </g:each>
</div>