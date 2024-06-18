<ul id="store-list" class="pl-0">
    <g:each in="${stores}" var="store" status="i">
        <div id="modal-store-result-${i + 1}" class="row ml-0 mr-0 pt-2 pb-2 wl-striped${i % 2} hoverable store-item" onclick="toggleSelectStore(${store.id}, ${i + 1})">

            <div id="modal-store-number-${i + 1}" class="col-4 my-auto">${store.config.storeNumber}</div>

            <div id="modal-store-name-${i + 1}" class="col-4 my-auto">${store.config.storeName}</div>

            <div class="col-4 my-auto text-right">
                <input class="form-check-input hidden" type="checkbox" value="${store.id}" id="store-${store.id}">
                <button id="modal-store-select-${i + 1}" class="btn btn-primary"
                        onclick="event.cancelBubble = true; toggleSelectStore(${store.id}, ${i + 1})">Select</button>
            </div>
        </div>
    </g:each>
    <!-- Pagination Controls -->
    <div class="my-3 text-left">
        <util:remotePaginate controller="promotion" action="getAllStores" total="${totalResults ?: 0}" update="store-list" offset="${sortParams?.offset ?: 0}" max="${sortParams?.max ?: 50}" params="[sort: sortParams?.sort, order: sortParams?.order, storeNumberFilter: storeNumberFilter, storeNameFilter: storeNameFilter]" />
    </div>
</ul>
