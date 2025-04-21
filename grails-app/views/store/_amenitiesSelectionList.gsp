<ul id="amenity-list" class="pl-0">
    <g:each in="${Amenities}" var="amenity" status="i">
        <div id="modal-amenity-result-${i + 1}" class="row ml-0 mr-0 pt-2 pb-2 wl-striped${i % 2} hoverable amenity-item" onclick="toggleSelectStore(${amenity.id}, ${i + 1})">
            <div id="modal-amenity-number-${i + 1}" class="col-8 my-auto">${amenity?.name}</div>
            <div class="col-4 my-auto text-right">
                <input class="form-check-input hidden" type="checkbox" value="${amenity?.id}" id="amenity-${amenity?.id}">
                <button id="modal-amenity-select-${i + 1}" class="btn btn-secondary btn-sm float-right"
                        onclick="event.cancelBubble = true; event.preventDefault(); toggleSelectStore(${amenity?.id}, ${i + 1})">Select</button>
            </div>
        </div>
    </g:each>
<!-- Pagination Controls -->
    <div class="my-3 text-left">
        %{--        <util:remotePaginate controller="promotion" action="ajaxGetAllStores" total="${totalResults ?: 0}" update="store-list" offset="${sortParams?.offset ?: 0}" max="${sortParams?.max ?: 50}" params="[sort: sortParams?.sort, order: sortParams?.order, storeNumberFilter: storeNumberFilter, storeNameFilter: storeNameFilter]" />--}%
    </div>
</ul>
