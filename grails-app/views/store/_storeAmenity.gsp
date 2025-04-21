<div class="row mt-3">
    <div class="col-12 d-flex justify-content-center align-items-center">
        <button id="add-amenities-btn" type="button" class="btn btn-wl mr-1" data-toggle="modal" data-target="#amenitiesSearchModal" onclick="getAllAmenities();">Add Amenities</button>
    </div>
</div>

<div class="col-12 mt-5 amenities-container" style="padding: 0; overflow-y: auto; max-height: 500px;" id="storeAmenitiesContainer">
    <g:render template="storeAmenityDetails" model="[storeAmenities: storeAmenities]" />
</div>

<section>
    <div class="modal fade" id="amenitiesSearchModal" tabindex="-1" role="dialog" aria-labelledby="amenitiesSearchModalLabel" aria-hidden="true">
        <div class="modal-dialog modal-lg modal-dialog-scrollable custom-width-modal" role="document" style="max-width: 600px;">
            <div class="modal-content">
                <div class="modal-header">
                    <h5 class="modal-title" id="amenitiesSearchModalLabel">Select Amenities</h5>
                    <button type="button" class="close" data-dismiss="modal" aria-label="Close">
                        <span aria-hidden="true">&times;</span>
                    </button>
                </div>

                <div class="modal-body">
                    <div id="filters" class="card bg-light border-wl mb-5">
                        <div class="card-header pointer" data-toggle="collapse" data-target="#filterCollapse" aria-expanded="false" aria-controls="filterCollapse">
                            <div class="row">
                                <div class="col-10">Filters</div>
                                <div class="col-2 text-right">
                                    <svg width="1em" height="1em" viewBox="0 0 16 16" class="bi bi-caret-down-fill text-right" fill="currentColor" xmlns="http://www.w3.org/2000/svg">
                                        <path d="M7.247 11.14L2.451 5.658C1.885 5.013 2.345 4 3.204 4h9.592a1 1 0 0 1 .753 1.659l-4.796 5.48a1 1 0 0 1-1.506 0z"/>
                                    </svg>
                                </div>
                            </div>
                        </div>
                        <div class="collapse" id="filterCollapse">
                            <div class="card-body">
                                <g:form name="filtersForm" id="filtersForm">
                                    <div class="form-group row align-items-center">
                                        <label for="amenityNameFilter" class="col-auto pr-2 col-form-label-sm">Amenity</label>
                                        <div class="col-5 pl-0">
                                            <g:textField id="amenityNameFilter" name="amenityNameFilter" value="${storeNameFilter}" class="form-control bottom-border" />
                                        </div>
                                        <div class="col ml-auto text-right">
                                            <button id="filter-clear-button" type="button" class="btn btn-danger mr-2" onclick="clearAmenityFilters();">Reset</button>
                                            <button id="filter-submit-button" type="button" class="btn btn-wl" onclick="getAllAmenities();">Filter</button>
                                        </div>
                                    </div>
                                </g:form>
                            </div>
                        </div>
                    </div>

                    <!-- Store List to Select From -->
                    <div id="store-selection-list">
                        <!-- Store list will be loaded here via AJAX -->
                    </div>
                </div>

                <div class="modal-footer">
                    <button type="button" class="btn btn-secondary" onclick="tempSelectedStoreIds = []" data-dismiss="modal">Close</button>
                    <button type="button" class="btn btn-success" onclick="saveAmenities(null, ${storeSettings?.id})">Add Selected Stores</button>
                </div>
            </div>
        </div>
    </div>
</section>

<script type='text/javascript'>
    $(document).ready(function () {
        initializeAmenitiesDropdown();
        $('#amenitiesSearchModal .card-header').on('click', function() {
            $('#filterCollapse').collapse('toggle');
        });
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

        $(document).on('change', 'input[name="amenities"]', function () {
            updateSelectedAmenities();
        });
    }

</script>
