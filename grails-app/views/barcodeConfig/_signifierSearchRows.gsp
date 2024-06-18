<g:if test="${!signifiers || signifiers?.size() == 0}">
    <div id="noResultsRow" class="col pt-2 pb-2 text-center my-auto wl-striped0">No Barcode Signifiers found.</div>
</g:if>

<g:each in="${signifiers}" var="signifier" status="i">
    <div class="row ml-0 mr-0 pt-2 pb-2 wl-striped${i%2} hoverable z-index-1" style="cursor: pointer;" title="Click to edit." onclick="loadEditSignifier(${signifier['id']})">
        <div id="description-id-${i + 1}" class="col-3 my-auto text-center">${signifier['description']}</div>
        <div id="type-id-${i + 1}" class="col-1 my-auto text-center">${message(code: 'BarcodeSignifierType.' + signifier['type'])}</div>
        <div id="pattern-id-${i + 1}" class="col-1 my-auto text-center">${signifier['pattern']}</div>
        <div id="length-id-${i + 1}" class="col-1 my-auto text-center">${signifier['length']}</div>
        <div id="receiptDescription-id-${i + 1}" class="col-2 my-auto text-center">${signifier['receiptDescription']}</div>
        <div id="embedded-id-${i + 1}" class="col-2 my-auto text-center">
            <g:if test="${signifier.barcodeSignifierEmbeddedDatas.size() > 0}">
                yes
            </g:if>
            <g:else>
                No
            </g:else>
        </div>
        <div class="col-2 my-auto text-right z-index-2">
            <button id="delete-${i + 1}" class="btn btn-danger mx-2" onclick="event.stopPropagation(); deleteSignifier(${signifier['id']});">Delete</button>
        </div>
    </div>
</g:each>
