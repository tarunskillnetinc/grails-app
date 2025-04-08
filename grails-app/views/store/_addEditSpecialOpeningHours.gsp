<g:form id="specialOpeningHoursForm" onsubmit="return saveSpecialOpeningHours(event);">
    <div class="modal fade" id="addSpecialOpeningHoursModal" tabindex="100" role="dialog" aria-labelledby="specialHoursModalLabel" aria-hidden="true">
        <div class="modal-dialog modal-lg" role="document">
            <div class="modal-content">
                <div class="modal-header">
                    <h5 class="modal-title" id="specialHoursModalLabel">${specialOpeningHour ? 'Edit' : 'Add'} Special Store Opening/Closing</h5>
                    <button type="button" class="close" id="close-cross" data-dismiss="modal" aria-label="Close">
                        <span aria-hidden="true">&times;</span>
                    </button>
                </div>
                <div class="modal-body">
                    <div class="form-row mb-3">
                        <div class="col-md-6">
                            <label for="specialDescription">Description</label>
                            <input type="text" id="specialDescription" name="specialDescription" class="form-control" value="${specialOpeningHour?.description ?: ''}" />
                        </div>
                        <div class="col-md-6">
                            <label for="specialDate">Date</label>
                            <input
                                    type="date"
                                    id="specialDate"
                                    name="specialDate"
                                    class="form-control"
                                    value="${specialOpeningHour?.date ?: ''}"
                                    required
                            />
                        </div>
                    </div>
                    <div>
                        <div class="form-row mb-2">
                            <div class="col-md-3" id="specialTimeFieldsStart" style="${specialOpeningHour?.closed ? 'display:none;' : ''}">
                                <label for="special-startTime">Start time</label>
                                <input type="text" id="special-startTime" name="special-startTime" class="form-control form-control-sm time-input" placeholder="HH:mm" value="${specialOpeningHour?.startTime ?: ''}" />
                            </div>
                            <div class="col-md-3" id="specialTimeFieldsEnd" style="${specialOpeningHour?.closed ? 'display:none;' : ''}">
                                <label for="special-endTime">End time</label>
                                <input type="text" id="special-endTime" name="special-endTime" class="form-control form-control-sm time-input" placeholder="HH:mm" value="${specialOpeningHour?.endTime ?: ''}" />
                            </div>
                            <div class="col-md-6 d-flex align-items-end">
                                <div class="form-check">
                                    <label class="form-check-label" for="specialClosedCheckbox">Closed</label>
                                    <input type="checkbox" id="specialClosedCheckbox" name="specialClosed" class="form-check-input wl-checkbox ml-6" onclick="toggleSpecialTimeFields()" ${specialOpeningHour?.closed ? 'checked' : ''} />
                                </div>
                            </div>
                        </div>
                    </div>
                    <g:if test="${specialOpeningHour}">
                        <input type="hidden" id="editIndex" value="${openingHourIndexItem}" />
                    </g:if>
                </div>
                <div class="modal-footer">
                    <button type="button" class="btn btn-secondary" id="cancelSpecialHoursBtn" data-dismiss="modal">Cancel</button>
                    <button type="button" class="btn btn-success" id="saveSpecialHoursBtn">${specialOpeningHour ? 'Update' : 'Save'}</button>
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

    $(document).on('shown.bs.modal', '#addSpecialOpeningHoursModal', function() {
        const startTimeInput = document.getElementById('special-startTime');
        const endTimeInput = document.getElementById('special-endTime');

        if (startTimeInput) setupTimeInput(startTimeInput);
        if (endTimeInput) setupTimeInput(endTimeInput);
    });

    $('#saveSpecialHoursBtn').on('click', function () {
        const description = $('#specialDescription').val();
        const closed = $('#specialClosedCheckbox').prop('checked');
        const date = $('#specialDate').val();
        const startTime = $('#special-startTime').val();
        const endTime = $('#special-endTime').val();
        const specialOpeningHoursIndex = $('#editIndex').val();

        if (!date) {
            alert('Please select a date. This field is mandatory.');
            return;
        }

        const event = new CustomEvent('saveSpecialHours', {
            detail: {description, closed, date, startTime, endTime, specialOpeningHoursIndex}
        });
        document.dispatchEvent(event);

        $('#addSpecialOpeningHoursModal').modal('hide');
        $('.modal-backdrop').remove();
        $('body').removeClass('modal-open');
    });

    function toggleSpecialTimeFields() {
        const isClosed = document.getElementById('specialClosedCheckbox').checked;
        const timeFieldsStart = document.getElementById('specialTimeFieldsStart');
        const timeFieldsEnd = document.getElementById('specialTimeFieldsEnd');
        timeFieldsStart.style.display = isClosed ? 'none' : 'block';
        timeFieldsEnd.style.display = isClosed ? 'none' : 'block';
    }

    function dateInputSetup() {
        let specialDateInput = document.getElementById('specialDate');
        let today = new Date().toISOString().split('T')[0];
        specialDateInput.min = today;
        specialDateInput.addEventListener('keydown', function (e) {
            e.preventDefault();
        });
        specialDateInput.addEventListener('click', function () {
            this.showPicker();
        });
    }
    dateInputSetup();
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

label[for="specialDate"]:after {
    content: " *";
    color: red;
}
</style>