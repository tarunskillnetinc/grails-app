<section id="modal-header">
    <div class="modal-header">
        <h2 id="page-title" class="mx-auto my-auto">Edit ${selectedAmenity?.amenity?.name}</h2>
    </div>
</section>

<section id="modal-form">
    <g:form name="addListItemForm">
        <g:hiddenField id="selected[${index}].amenity.id" name="selected[${index}].amenity.id" value="${selectedAmenity?.amenity?.id}"/>
        <g:hiddenField id="selected[${index}].amenity.retailerId" name="selected[${index}].amenity.retailerId" value="${selectedAmenity?.amenity?.retailerId}"/>
        <g:hiddenField id="selected[${index}].amenity.name" name="selected[${index}].amenity.name" value="${selectedAmenity?.amenity?.name}"/>
        <g:hiddenField id="selected[${index}].storeId" name="selected[${index}].storeId" value="${selectedAmenity?.storeId}"/>

        <section id="amenity-edit-errors-container" class="container-fluid mt-3"></section>

        <!-- First row: Description and Quantity -->
        <div class="row mt-5">
            <!-- Description -->
            <div class="col-md-6">
                <div class="form-group row">
                    <label for="selected.amenity.description" class="col-4 col-form-label text-right" style="text-align: right; padding-right: 15px;">Additional Detail</label>
                    <div class="col-8">
                        <g:field type="text" id="selected[${index}].amenity.description" name="selected[${index}].amenity.description" class="form-control select-border" maxlength="30" value="${selectedAmenity?.additionalDetail}"/>
                    </div>
                </div>
            </div>

            <!-- Quantity -->
            <div class="col-md-6">
                <div class="form-group row">
                    <label id="value" for="selected.amenity.quantity" class="col-3 col-form-label text-right pr-4">Quantity</label>
                    <div class="col-8">
                        <g:field type="number" id="selected[${index}].amenity.quantity" name="selected[${index}].amenity.quantity" class="form-control select-border" max="9999" maxlength="4" value="${selectedAmenity?.count > 0 ? selectedAmenity?.count : ''}" />
                    </div>
                </div>
            </div>
        </div>

        <!-- Second row: Available hours -->
        <div class="row mt-5">
            <div class="col-md-12">
                <div class="form-group row">
                    <label for="selected.amenity.availability" class="col-2 col-form-label text-right" style="text-align: right; padding-right: 15px;">Available Hours</label>
                    <div class="col-10" style="padding-left: 0;">
                        <!-- Grid layout for fixed positioning -->
                        <g:each var="regularHours" in="${selectedAmenity?.availability}" status="i">
                            <g:if test="${regularHours.day}">
                                <div id="storeAmenities[${index}].regularHours[${i}].days" style="display: grid; grid-template-columns: 40px 100px auto; grid-gap: 15px; width: 100%; margin-left: 7px;">
                                    <!-- Checkbox -->
                                    <div style="grid-column: 1; display: flex; align-items: center; justify-content: center;">
                                        <g:checkBox name="storeAmenities[${index}].regularHours[${i}].closed"
                                                    value="false"
                                                    checked="${!regularHours.closed}"
                                                    class="form-check-input restriction-checkbox"
                                                    style="width: 20px; height: 20px; cursor: pointer; margin: 0;"/>
                                    </div>

                                    <!-- Day name -->
                                    <div style="grid-column: 2; display: flex; align-items: center; overflow: hidden;">
                                        <span class="form-control-plaintext" style="white-space: nowrap; overflow: hidden; text-overflow: ellipsis;">${regularHours.day}</span>
                                        <g:hiddenField id="storeAmenities[${index}].regularHours[${i}].day" name="storeAmenities[${index}].regularHours[${i}].day" value="${regularHours.day}"/>
                                    </div>

                                    <!-- Time inputs container -->
                                    <div id="timeFields_${i}" class="restriction-time-inputs" style="grid-column: 3; display: ${!regularHours.closed ? 'flex' : 'none'}; align-items: center; gap: 0px;">
                                        <div style="display: flex; align-items: center; white-space: nowrap;">
                                            <g:field type="time" name="storeAmenities[${index}].regularHours[${i}].startTime"
                                                     id="storeAmenities[${index}].regularHours[${i}].startTime"
                                                     value="${regularHours.startTime}"
                                                     class="form-control form-control-sm time-input"
                                                     style="width: 7rem !important; flex: none !important;"
                                                     placeholder="HH:mm"/>
                                            <span class="validity"></span>
                                            <span style="margin: 0 8px;">to</span>
                                            <g:field type="time" name="storeAmenities[${index}].regularHours[${i}].endTime"
                                                     id="storeAmenities[${index}].regularHours[${i}].endTime"
                                                     value="${regularHours.endTime}"
                                                     class="form-control form-control-sm time-input"
                                                     style="width: 7rem !important; flex: none !important;"
                                                     placeholder="HH:mm"/>
                                            <span class="validity"></span>
                                        </div>
                                    </div>
                                </div>
                            </g:if>
                        </g:each>

                    </div>
                </div>
            </div>
        </div>
    </g:form>

    <div class="modal-footer">
        <button type="button" id="closeListItemModal" class="btn btn-wl" onclick="closeStoreAmenityAddModal();">Cancel</button>
        <button type="button" id="saveAddSupplierButton" class="btn btn-success" onclick="saveAmenities(${index}, ${selectedAmenity?.storeId});">Save</button>
    </div>
</section>

<style>
.amenity-time-input {
    width: 70px !important;
    max-width: 70px !important;
}

/* If the above doesn't work, try this more specific selector */
.restriction-time-inputs .amenity-time-input {
    width: 70px !important;
    max-width: 70px !important;
}
</style>

<script type="text/javascript">
    // Function to toggle time input fields based on checkbox state
    function toggleTimeInputs() {
        // Find the closest grid container that contains this checkbox
        const gridContainer = this.closest('div[style*="grid-template-columns"]');

        if (gridContainer) {
            // Find the time inputs container within the same grid container
            const timeInputsContainer = gridContainer.querySelector('.restriction-time-inputs');

            if (timeInputsContainer) {
                // Set display based on checkbox state
                timeInputsContainer.style.display = this.checked ? 'flex' : 'none';
            }
        }
    }

    // Function to attach event listeners to all checkboxes
    function attachCheckboxListeners() {
        // Select all checkboxes with names matching the pattern
        const checkboxes = document.querySelectorAll('input[type="checkbox"][name*="regularHours"][name*="closed"]');
        checkboxes.forEach(function(checkbox) {
            // First remove any existing listeners to prevent duplicates
            checkbox.removeEventListener('click', toggleTimeInputs);

            // Then add the click event listener (more reliable than change)
            checkbox.addEventListener('click', toggleTimeInputs);

            // Also set initial state
            const gridContainer = checkbox.closest('div[style*="grid-template-columns"]');
            if (gridContainer) {
                const timeInputsContainer = gridContainer.querySelector('.restriction-time-inputs');
                if (timeInputsContainer) {
                    timeInputsContainer.style.display = checkbox.checked ? 'flex' : 'none';
                }
            }
        });
    }

    // Initialize when the DOM is loaded
    document.addEventListener('DOMContentLoaded', function() {
        attachCheckboxListeners();
    });

    // Call immediately in case the DOM is already loaded
    if (document.readyState === 'complete' || document.readyState === 'interactive') {
        setTimeout(attachCheckboxListeners, 100);
    }

    // For modals/popups that might load content dynamically
    // This ensures the listeners are attached when the modal is shown
    if (typeof jQuery !== 'undefined') {
        jQuery(document).ready(function($) {
            // For Bootstrap modals
            $(document).on('shown.bs.modal', function() {
                setTimeout(attachCheckboxListeners, 200);
            });

            // Ensure listeners are attached after any AJAX operations
            $(document).ajaxComplete(function() {
                setTimeout(attachCheckboxListeners, 200);
            });
        });
    }

    function setupQuantityLimit() {
        // Use a more flexible selector to find quantity inputs
        const inputs = document.querySelectorAll('input[id*="amenity.quantity"]');

        inputs.forEach(function(input) {
            if (!input) return;

            // Remove any existing event listeners to prevent duplicates
            input.removeEventListener('input', handleQuantityInput);

            // Set HTML attributes for native browser validation
            input.setAttribute('max', '9999');
            input.setAttribute('maxlength', '4');

            // Add the event listener
            input.addEventListener('input', handleQuantityInput);

            // Apply validation immediately in case there's an initial value
            handleQuantityInput.call(input);
        });
    }

    // Separate handler function for quantity input
    function handleQuantityInput() {
        // Remove non-numeric characters
        this.value = this.value.replace(/\D/g, '');

        // Limit to 4 digits
        if (this.value.length > 4) {
            this.value = this.value.slice(0, 4);
        }

        // Ensure value doesn't exceed 9999
        const num = parseInt(this.value, 10);
        if (num > 9999) {
            this.value = '9999';
        }
    }

    // Apply immediately and after DOM loads
    setupQuantityLimit();
    document.addEventListener('DOMContentLoaded', setupQuantityLimit);

    if (typeof jQuery !== 'undefined') {
        jQuery(document).on('shown.bs.modal', function() {
            setTimeout(setupQuantityLimit, 100);
        });

        // Also apply after any AJAX completion
        jQuery(document).ajaxComplete(function() {
            setTimeout(setupQuantityLimit, 100);
        });
    }

</script>