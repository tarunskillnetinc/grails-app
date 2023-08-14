<g:if test="${!tillList || tillList?.size() == 0}">
    <div id="noResultsRow" class="col pt-2 pb-2 text-center my-auto wl-striped0">No till configurations found.</div>
</g:if>

<g:each in="${tillList}" var="till" status="i">
    <div class="row ml-0 mr-0 pt-2 pb-2 wl-striped${i%2} hoverable">
        <div id="store-id-${i + 1}" class="col-1 my-auto text-center">${till.storeId}</div>
        <div id="till-id-${i + 1}" class="col-1 my-auto text-center">${till.tillId}</div>
        <div id="serial-number-${i + 1}" class="col-2 my-auto text-center">${till.serialNumber}</div>
        <div class="col-8 my-auto text-left">
            <button id="edit-${i + 1}" class="btn btn-wl mx-2" onclick="editTill(${till.storeId}, ${till.tillId}, '${till.serialNumber}')">Edit Till</button>
            <g:if test="${till.serialNumber}">
                <button id="unassign-${i + 1}" class="btn btn-info mx-2" onclick="unassignSerial(${till.id}, '${till.serialNumber}', ${till.tillId});">Unassign Serial</button>
            </g:if>
            <button id="delete-${i + 1}" class="btn btn-wl mx-2" onclick="deleteTill(${till.storeId}, ${till.tillId}, '${till.serialNumber}')">Delete Till</button>
            <button id="configuration-${i + 1}" class="btn btn-danger mx-2" onclick="advancedConfiguration('${till.serialNumber}')">Advanced Configuration</button>
        </div>
    </div>
</g:each>