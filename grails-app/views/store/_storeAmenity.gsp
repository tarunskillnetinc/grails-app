<div class="row mt-3">
    <label for="amenitiesSelector" class="col-3 col-form-label text-right mr-9">Amenities:</label>
    <div class="col-5">
        <div class="dropdown">
            <div class="form-control select-border" id="amenities">
                <span id="selectedAmenities" style="display: inline-block; max-width: 100%; overflow: hidden; text-overflow: ellipsis; white-space: nowrap;">Select Amenities</span>
                <span class="caret"></span>
            </div>
            <div id="amenityIdSelect" class="dropdown-menu w-100">
                <g:each in="${amenities}" var="amenity">
                    <div class="dropdown-item">
                        <label class="mb-0">
                            <input id="${amenity.id}" type="checkbox" name="amenities" value="${amenity.id}"
                                   data-name="<g:message code="Amenity.${amenity.name}" default="${amenity.name}" />">
                            <g:message code="Amenity.${amenity.name}" default="${amenity.name}" />
                        </label>
                    </div>
                </g:each>
            </div>
        </div>
    </div>

    <div class="col-3 pl-0">
        <a href="#" onclick="addSelectedAmenities()" id="add-special-opening-hours" class="btn btn-wl">Add Amenities</a>
    </div>
</div>

<div class="col-12 mt-5" id="storeAmenitiesContainer">
    <g:render template="storeAmenityDetails" model="[storeAmenities: storeAmenities]" />
</div>

<style>
.dropdown-item {
    padding: 0.5rem 1rem;
}

.dropdown-item label {
    display: block;
    width: 100%;
    cursor: pointer;
}

.dropdown-menu {
    max-height: 300px;
    overflow-y: auto;
}

#amenities {
    cursor: pointer;
}

.caret {
    float: right;
    margin-top: 8px;
}

#storeAmenitiesContainer {
    display: flex;
    flex-direction: column;
    align-items: center;
    padding-left: 15%; /* This will push content to the right */
}

#storeAmenitiesContainer {
    display: flex;
    flex-direction: column;
    align-items: flex-end; /* Aligns to the right */
    padding-right: 15%; /* Adjusts how far from the right edge */
}
</style>