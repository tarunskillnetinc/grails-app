<div class="row mt-3">
    <label for="amenitiesSelector" class="col-4 col-form-label text-right pr-4">Amenities:</label>
    <div class="col-5">
        <g:select name="amenitiesSelector"
                  from="${amenities}"
                  noSelection="['': '--Select Amenity--']"
                  optionKey="${{it?.id}}"
                  optionValue="${{g.message(code: "Amenity.${it?.name}", default: it?.name)}}"
                  valueMessagePrefix="Amenity"
                  class="form-control select-border"/>
    </div>
    <div class="col-3 pl-0">
        <a href="#" onclick="addAmenities(null, null, null)" id="add-special-opening-hours" class="btn btn-wl">Add Amenity</a>
    </div>
</div>