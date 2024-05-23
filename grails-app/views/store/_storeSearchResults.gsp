<div class="row mt-5 pb-2 ml-0 mr-0 table-wl bottom-border">
    <div class="col-2 font-weight-bold"><a id="product-list-store-number" href="#" onclick="getStores({
        max: ${sortParams?.max},
        offset: ${sortParams?.offset},
        sort: 'storeNumber',
        order: ${sortParams?.sort == 'storeNumber' ? sortParams?.order == 'asc' ? '\'desc\'' : '\'asc\'' : '\'asc\''}
    });">Store Number</a></div>
    <div class="col-2 font-weight-bold"><a id="product-list-store-name" href="#" onclick="getStores({
        max: ${sortParams?.max},
        offset: ${sortParams?.offset},
        sort: 'storeName',
        order: ${sortParams?.sort == 'storeName' ? sortParams?.order == 'asc' ? '\'desc\'' : '\'asc\'' : '\'asc\''}
    });">Store Name</a></div>
    <div class="col-2 font-weight-bold"><a id="product-list-store-type" href="#" onclick="getStores({
        max: ${sortParams?.max},
        offset: ${sortParams?.offset},
        sort: 'storeType',
        order: ${sortParams?.sort == 'storeType' ? sortParams?.order == 'asc' ? '\'desc\'' : '\'asc\'' : '\'asc\''}
    });">Store Type</a></div>
</div>

<div class="d-flex justify-content-center">
    <div id="loading-indicator" class="spinner-border" role="status" style="display: none;">
        <span class="sr-only">Loading...</span>
    </div>
</div>

<div id="search-results">
    <g:if test="${!stores || stores?.size() == 0}">
        <div id="noResultsRow" class="col pt-2 pb-2 text-center my-auto wl-striped0">No stores found.</div>
    </g:if>

    <g:each in="${stores}" var="store" status="i">
        <div id="store-result-${i+1}" class="row ml-0 mr-0 pt-2 pb-2 wl-striped${i%2} hoverable" title="Click to edit." style="cursor: pointer;" onclick="document.location.href='${createLink(action:'config', id: store.id, params:[storeNumberFilter: storeNumberFilter, storeNameFilter: storeNameFilter, showDeletedFilter: showDeletedFilter, max: sortParams.max, offset: sortParams.offset, sort: sortParams.sort, order: sortParams.order])}';">
            <div id="store-number-${i + 1}" class="col-2 my-auto">${store.config.storeNumber}</div>
            <div id="store-name-${i + 1}" class="col-2 my-auto">${store.config.storeName}</div>
            <div id="store-type-${i + 1}" class="col-2 my-auto"><g:message code="StoreType.${store.config.storeType}" /></div>

            <div class="col-6 my-auto text-right">
                <g:if test="${store.deleted}">
                    <button id="store-delete-${i + 1}" class="btn btn-info" onclick="event.stopPropagation(); deleteStore(${store.id}, ${store.config.storeNumber}, false);">Reinstate Store</button>
                </g:if>
                <g:else>
                    <button id="store-delete-${i + 1}" class="btn btn-danger" onclick="event.stopPropagation(); deleteStore(${store.id}, ${store.config.storeNumber}, true);">Delete Store</button>
                </g:else>
            </div>
        </div>
    </g:each>
</div>

<div class="my-3 text-right">
    <util:remotePaginate controller="store" action="ajaxGetStores" total="${totalResults ?: 0}" update="results-container" offset="${sortParams?.offset ?: 0}" max="${sortParams?.max ?: 50}" params="[sort: sortParams?.sort, order: sortParams?.order, storeNumberFilter: storeNumberFilter, storeNameFilter: storeNameFilter, showDeletedFilter: showDeletedFilter]" />
</div>