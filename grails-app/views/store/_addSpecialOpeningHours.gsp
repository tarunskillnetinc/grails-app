<g:form id="specialOpeningHoursForm" onsubmit="return saveSpecialOpeningHours(event);">
    <div class="modal fade" id="addSpecialOpeningHoursModal" tabindex="100" role="dialog" aria-labelledby="specialHoursModalLabel" aria-hidden="true">
        <div class="modal-dialog modal-lg" role="document">
            <div class="modal-content">
                <div class="modal-header">
                    <h5 class="modal-title" id="specialHoursModalLabel">Add Special Store Opening/Closing</h5>
                    <button type="button" class="close" data-dismiss="modal" aria-label="Close">
                        <span aria-hidden="true">&times;</span>
                    </button>
                </div>
                <div class="modal-body">
                    <div class="form-row mb-3">
                        <div class="col-md-6">
                            <label for="specialDescription">Description</label>
                            <input type="text" id="specialDescription" name="specialDescription" class="form-control" />
                        </div>
                        <div class="col-md-6">
                            <label for="specialDate">Date</label>
                            <input type="date" id="specialDate" name="specialDate" class="form-control" />
                        </div>
                    </div>
                    <div >
                        <div class="form-row mb-2">
                            <div class="col-md-3" id="specialTimeFieldsStart">
                                <label for="specialStartTime">Start time</label>
                                <input type="text" id="specialStartTime" name="specialStartTime" class="form-control form-control-sm time-input" placeholder="HH:mm" />
                            </div>
                            <div class="col-md-3" id="specialTimeFieldsEnd">
                                <label for="specialEndTime">End time</label>
                                <input type="text" id="specialEndTime" name="specialEndTime" class="form-control form-control-sm time-input" placeholder="HH:mm" />
                            </div>
                            <div class="col-md-6 d-flex align-items-end">
                                <div class="form-check">
                                    <label class="form-check-label" for="specialClosedCheckbox">Closed</label>
                                    <input type="checkbox" id="specialClosedCheckbox" name="specialClosed" class="form-check-input wl-checkbox ml-6" onclick="toggleSpecialTimeFields()" />
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
                <div class="modal-footer">
                    <button type="button" class="btn btn-secondary" data-dismiss="modal">Cancel</button>
                    <button type="button" class="btn btn-success" id="saveSpecialHoursBtn">Save</button>
                </div>
            </div>
        </div>
    </div>
</g:form>

<script>
    var modal = document.getElementById('addSpecialOpeningHoursModal');
    $(modal).on('hidden.bs.modal', function (event) {
        $('.modal-backdrop').remove();
        $('body').removeClass('modal-open');
    });

    $(modal).on('shown.bs.modal', function (event) {
        // Setup time inputs when modal is shown
        setupTimeInput(document.getElementById('specialStartTime'));
        setupTimeInput(document.getElementById('specialEndTime'));
    });

    $('#saveSpecialHoursBtn').on('click', function() {
        const description = $('#specialDescription').val();
        const closed = $('#specialClosedCheckbox').prop('checked');
        const date = $('#specialDate').val();
        const startTime = $('#specialStartTime').val();
        const endTime = $('#specialEndTime').val();

        // Dispatch a custom event with the form data
        const event = new CustomEvent('saveSpecialHours', {
            detail: { description, closed, date, startTime, endTime }
        });
        document.dispatchEvent(event);

        // Close the modal
        $('#addSpecialOpeningHoursModal').modal('hide');
        $('.modal-backdrop').remove();
        $('body').removeClass('modal-open');
    });

    // Add this function to the modal script
    function setupTimeInput(input) {
        input.addEventListener('input', function(e) {
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
        });

        input.addEventListener('blur', function(e) {
            const value = e.target.value;
            if (value && !/^([01]\d|2[0-3]):[0-5]\d$/.test(value)) {
                alert('Please enter a valid time in HH:mm format');
                e.target.value = '';
            }
        });
    }
</script>
<style>
/* Add this to your existing styles */
.modal {
    z-index: 1050 !important;
}
.modal-backdrop {
    z-index: 1040 !important;
}

/* Checkbox styling */
.wl-checkbox {
    width: 18px;
    height: 18px;
    margin-top: 0;
}

.form-check-label {
    font-size: 14px;
    line-height: 18px;
    margin-left: 5px;
}

.form-group label {
    font-size: 14px;
    line-height: 18px;
}

.form-check {
    display: flex;
    align-items: center;
    margin-bottom: 0;
}

.form-check-input {
    margin-top: 0;
    margin-right: 5px;
}
</style>