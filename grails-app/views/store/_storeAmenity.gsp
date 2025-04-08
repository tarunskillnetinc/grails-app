<script type='text/javascript'>
    $(document).ready(function () {
        initializeAmenitiesDropdown();
    });

    function initializeAmenitiesDropdown() {
        function updateSelectedAmenities() {
            const selected = $('input[name="amenities"]:checked').map(function () {
                return $(this).val();
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

    function addSelectedAmenities() {
        const selectedAmenities = $('input[name="amenities"]:checked').map(function() {
            return {
                id: $(this).data('id'),
                name: $(this).val()
            };
        }).get();

        if (selectedAmenities.length === 0) {
            alert('Please select at least one amenity');
            return;
        }

        console.log('Selected amenities:', selectedAmenities);

        // Call your existing function for each selected amenity
        selectedAmenities.forEach(amenity => {
            addAmenities(amenity.id, amenity.name, null);
        });

        // Clear selections after adding
        $('input[name="amenities"]').prop('checked', false);
        $('#selectedAmenities').text('Select Amenities');
    }
</script>

<div class="row mt-3">
    <label for="amenitiesSelector" class="col-4 col-form-label text-right pr-4">Amenities:</label>
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
                            <input id="${amenity.id}" type="checkbox" name="amenities" value="${amenity.name}" data-id="${amenity.id}">
                            <g:message code="Amenity.${amenity.name}" default="${amenity.name}" />
                        </label>
                    </div>
                </g:each>
            </div>
        </div>
    </div>
    <div class="col-3 pl-0">
        <a href="#" onclick="addAmenities(null, null, null)" id="add-special-opening-hours" class="btn btn-wl">Add Amenity</a>
    </div>
</div>