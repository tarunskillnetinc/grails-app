<%
    // Create a set of amenity IDs that are already in the store amenities list
    def existingAmenityIds = storeAmenities.collect { it.amenity?.id }.findAll { it != null } as Set

    // Filter the amenities list to only include those not in the existing set
    def availableAmenities = amenities.findAll { amenity -> !existingAmenityIds.contains(amenity.id) }
%>

<div class="row mt-3">
    <div class="col-12 d-flex justify-content-center align-items-center">
        <label for="amenitiesSelector" class="col-form-label text-right mr-3" style="width: 100px;">Amenities:</label>
        <div class="dropdown" style="width: 250px;">
            <div class="form-control select-border" id="amenities">
                <span id="selectedAmenities" style="display: inline-block; max-width: 100%; overflow: hidden; text-overflow: ellipsis; white-space: nowrap;">Select Amenities</span>
                <span class="caret"></span>
            </div>
            <div id="amenityIdSelect" class="dropdown-menu w-100">
                <g:each in="${availableAmenities}" var="amenity">
                    <div class="dropdown-item">
                        <label class="mb-0">
                            <input id="${amenity.id}" type="checkbox" name="amenities" value="${amenity.id}"
                                   data-name="${amenity.name}" default="${amenity.name}" />
                            ${amenity.name}
                        </label>
                    </div>
                </g:each>
            </div>
        </div>

        <div class="ml-2">
            <a href="#" onclick="saveAmenities(null, ${storeSettings?.id})" id="add-special-opening-hours" class="btn btn-wl">Add Amenities</a>
        </div>
    </div>
</div>

<div class="col-12 mt-5 amenities-container" style="padding: 0; overflow-y: auto; max-height: 500px;" id="storeAmenitiesContainer">
    <g:render template="storeAmenityDetails" model="[storeAmenities: storeAmenities]" />
</div>

<script type='text/javascript'>
    $(document).ready(function () {
        initializeAmenitiesDropdown();
    });

    function initializeAmenitiesDropdown() {
        function updateSelectedAmenities() {
            const selected = $('input[name="amenities"]:checked').map(function () {
                return $(this).data('name');
            }).get();

            if (selected.length > 0) {
                $('#add-special-opening-hours').prop("disabled", false);
                $('#selectedAmenities').text(selected.join(', '));
            } else {
                $('#add-special-opening-hours').prop("disabled", true);
                $('#selectedAmenities').text('Select Amenities');
            }
        }

        // Update selected amenities on page load
        updateSelectedAmenities();

        $('#amenities').off('click');

        $('#amenities').on('click', function (e) {
            e.preventDefault();
            $(this).parent().toggleClass('show');
            $(this).next('.dropdown-menu').toggleClass('show');
        });

        $(document).on('click', function (e) {
            if (!$(e.target).closest('.dropdown').length) {
                if ($('#amenityIdSelect .dropdown-item').length === 0) {
                    return;
                }
                $('.dropdown-menu').removeClass('show');
                $('.dropdown').removeClass('show');
            }
        });

        $(document).on('change', 'input[name="amenities"]', function() {
            updateSelectedAmenities();
        });
    }

</script>
