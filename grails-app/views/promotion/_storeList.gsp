<g:if test="${!addedStores || addedStores?.size() == 0}">
    <div id="noResultsRow" class="col pt-2 pb-2 text-center my-auto wl-striped0">No stores found.</div>
</g:if>

<g:hiddenField name="store-required" value="${addedStores ? addedStores?.size() : 0}"/>
<g:each in="${addedStores}" var="store" status="i">
    <div id="store-result-${i+1}" class="row ml-0 mr-0 pt-2 pb-2 wl-striped${i%2} hoverable">
        <div id="store-number-${i + 1}" class="col-2 my-auto">${store.config.storeNumber}</div>
        <div id="store-name-${i + 1}" class="col-2 my-auto">${store.config.storeName}</div>
        <div class="col-6 my-auto text-right">
            <button id="store-delete-${i + 1}" type="button" class="btn btn-danger" onclick="removeStore(${store.id}, ${i + 1})">Remove</button>
        </div>
    </div>
</g:each>
