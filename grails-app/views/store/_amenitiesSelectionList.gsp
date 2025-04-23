<ul id="amenity-list" class="pl-0">
    <g:each in="${Amenities}" var="amenity" status="i">
        <g:set var="isSelected" value="${selectedIds?.contains(amenity.id)}" />
        <div id="modal-amenity-result-${i + 1}" class="row ml-0 mr-0 pt-2 pb-2 wl-striped${i % 2} hoverable amenity-item" onclick="toggleSelectAmenity(${amenity.id}, ${i + 1})">
            <div id="modal-amenity-number-${i + 1}" class="col-8 my-auto">${amenity?.name}</div>
            <div class="col-4 my-auto text-right">
                <input class="form-check-input hidden" type="checkbox" value="${amenity?.id}" id="amenity-${amenity?.id}" ${isSelected ? 'checked' : ''}>
                <button id="modal-amenity-select-${i + 1}" class="btn ${isSelected ? 'btn-primary' : 'btn-secondary'} btn-sm float-right"
                        onclick="event.cancelBubble = true; event.preventDefault(); toggleSelectAmenity(${amenity?.id}, ${i + 1})">
                    ${isSelected ? 'Selected' : 'Select'}
                </button>
            </div>
        </div>
    </g:each>
</ul>