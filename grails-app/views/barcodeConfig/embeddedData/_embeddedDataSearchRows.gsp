<g:if test="${!embeddedDataList || embeddedDataList?.size() == 0}">
    <div id="noResultsRow" class="col pt-2 pb-2 text-center my-auto wl-striped0">No Embedded Data found.</div>
</g:if>

<g:each in="${embeddedDataList}" var="embeddedData" status="i">
    <div class="row ml-0 mr-0 pt-2 pb-2 wl-striped${i%2} hoverable" style="cursor: pointer;" title="Click to edit." onclick="editEmbeddedData(${embeddedData['id']})">
        <div id="type-id-${i + 1}" class="col-2 my-auto text-left">${message(code: 'EmbeddedDataType.' + embeddedData['type'])}</div>
        <div id="startIndex-id-${i + 1}" class="col-2 my-auto text-center">${embeddedData['startIndex']}</div>
        <div id="length-id-${i + 1}" class="col-2 my-auto text-center">${embeddedData['length']}</div>
        <div id="format-id-${i + 1}" class="col-2 my-auto text-center">${message(code: 'EmbeddedDataFormats.' + embeddedData['format'])}</div>
        <div class="col-4 my-auto text-right" onclick="">
            <sec:ifAnyGranted roles='ROLE_ENGINEER'>
                <button id="delete-${i + 1}" class="btn btn-danger mx-2" onclick="event.stopPropagation();deleteEmbeddedData(${embeddedData['id']})">Delete</button>
            </sec:ifAnyGranted>
        </div>
    </div>
</g:each>