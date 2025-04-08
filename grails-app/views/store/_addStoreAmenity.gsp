<script>
    function setupAllTimeInputs() {
        const timeInputs = document.querySelectorAll('.time-input');
        timeInputs.forEach((input, index) => {setupTimeInput(input);});
    }

    function setupTimeInput(input) {
        // Remove any existing event listeners first to prevent duplicates
        input.removeEventListener('input', handleTimeInput);
        input.removeEventListener('blur', validateTimeFormat);

        // Add the event listeners
        input.addEventListener('input', handleTimeInput);
        input.addEventListener('blur', validateTimeFormat);

        input.readOnly = false;
        input.disabled = false;
    }

    function handleTimeInput(e) {
        let value = e.target.value.replace(/[^0-9]/g, '');

        if (value.length > 2) {
            let hours = parseInt(value.slice(0, 2));
            let minutes = parseInt(value.slice(2));

            hours = Math.min(hours, 23);
            if (minutes > 59) {
                minutes = 59;
            }

            value = hours.toString().padStart(2, '0') + ':' + minutes.toString().padStart(2, '0');
        }

        if (value.length > 5) {
            value = value.slice(0, 5);
        }

        e.target.value = value;
    }

    function validateTimeFormat(e) {
        const value = e.target.value;
        if (value && !/^([01]\d|2[0-3]):[0-5]\d$/.test(value)) {
            alert('Please enter a valid time in HH:mm format');
            e.target.value = '';
        }
    }

    // Initial setup when DOM is loaded
    document.addEventListener('DOMContentLoaded', function() {
        setupAllTimeInputs();
    });

    // For dynamically loaded content
    function initializeTimeInputs() {
        setTimeout(function() {
            setupAllTimeInputs();
        }, 300); // Increased delay to ensure DOM is updated
    }

    // Call this function after loading the modal content
    $(document).ready(function () {
        // Setup any initial time inputs
        setupAllTimeInputs();

        // Monitor for modal shown events
        $(document).on('shown.bs.modal', function() {
            initializeTimeInputs();
        });
    });
</script>

<section id="modal-header">
    <div class="modal-header">
        <h2 id="page-title" class="mx-auto my-auto">Add Amenities</h2>
    </div>
</section>

<section id="modal-error"></section>

<section id="modal-form">
    <g:form name="addListItemForm">
        <section id="additional-details-errors-container" class="container-fluid"></section>

        <!-- First row: Description and Quantity -->
        <div class="row mt-5">
            <!-- Description -->
            <div class="col-md-6">
                <div class="form-group row">
                    <label for="addStoreAdditionalDetailDescription" class="col-4 col-form-label text-right" style="text-align: right; padding-right: 15px;">Description</label>
                    <div class="col-8">
                        <g:field type="text" id="addStoreAdditionalDetailDescription" name="addStoreAdditionalDetailDescription" class="form-control select-border" max="9999" maxlength="4" value="${description}"/>
                    </div>
                </div>
            </div>

            <!-- Quantity -->
            <div class="col-md-6">
                <div class="form-group row">
                    <label id="value" for="addStoreAdditionalDetailValue" class="col-3 col-form-label text-right pr-4">Quantity</label>
                    <div class="col-8">
                        <g:field id="addStoreAdditionalDetailValue" name="addStoreAdditionalDetailValue" class="form-control select-border" maxlength="240" value="${value}" />
                    </div>
                </div>
            </div>
        </div>

        <!-- Second row: Available hours -->
        <div class="row mt-5">
            <div class="col-md-12">
                <div class="form-group row">
                    <label for="addAmenityAvailableHours" class="col-2 col-form-label text-right" style="text-align: right; padding-right: 15px;">Available hours</label>
                    <div class="col-10" style="padding-left: 0;">
                        <!-- Grid layout for fixed positioning -->
                        <div style="display: grid; grid-template-columns: 40px 100px 120px 50px 120px; grid-gap: 15px; width: 100%; margin-left: 7px;">
                            <g:each var="openingTime" in="${initialRegularHours}" status="i">
                                <!-- Checkbox -->
                                <div style="grid-column: 1; display: flex; align-items: center; justify-content: center;">
                                    <g:checkBox name="openingTime[${i}].closed" value="${openingTime.closed}" class="form-check-input" style="width: 20px; height: 20px; cursor: pointer; margin: 0;" />
                                </div>

                                <!-- Day name - with text overflow handling -->
                                <div style="grid-column: 2; display: flex; align-items: center; overflow: hidden;">
                                    <span id="openingTime[${i}].day" class="form-control-plaintext" style="white-space: nowrap; overflow: hidden; text-overflow: ellipsis;">${openingTime.day}</span>
                                </div>

                                <!-- Start Time -->
                                <div style="grid-column: 3;">
                                    <g:textField name="openingTime[${i}].startTime" value="${openingTime.startTime}" class="form-control form-control-sm time-input" placeholder="HH:mm"/>
                                </div>

                                <!-- "to" label -->
                                <div style="grid-column: 4; display: flex; align-items: center; justify-content: center;">
                                    <span>to</span>
                                </div>

                                <!-- End Time -->
                                <div style="grid-column: 5;">
                                    <g:textField name="openingTime[${i}].endTime" value="${openingTime.endTime}" class="form-control form-control-sm time-input" placeholder="HH:mm"/>
                                </div>
                            </g:each>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </g:form>

    <div class="modal-footer">
        <button type="button" id="closeListItemModal" class="btn btn-wl" onclick="closeStoreAmenityAddModal();">Cancel</button>
        <button type="button" id="saveAddSupplierButton" class="btn btn-success" onclick="addStoreAmenity();">Save</button>
    </div>
</section>