<div class="row mt-3">
    <label for="amenitiesSelector" class="col-4 col-form-label text-right pr-4">Amenities:</label>
    <div class="col-6">
        <div class="dropdown-content">
            <g:select id="amenitiesSelector"
                      size="6"
                      name="amenitiesSelector"
                      style="overflow-y: scroll; overflow-x: hidden;"
                      from="${amenities}"
                      optionValue="${{it?.name}}"
                      optionKey="${{it?.id}}"
                      class="form-control select-border"/>
        </div>
    </div>
%{--    <button class="btn btn-wl p-2 ml-3" onclick="addAmenities(null, null, null)">Add Amenity</button>--}%
    <div class="mt-3">
        <a href="#" onclick="addAmenities(null, null, null)" id="add-special-opening-hours" class="btn btn-wl p-1">Add Amenity</a>
    </div>
</div>