<g:form id="${commandPrefix}-specialOpeningHoursForm" onsubmit="return saveSpecialOpeningHours(event);">
    <div class="modal fade" id="${commandPrefix}-addSpecialOpeningHoursModal" tabindex="100" role="dialog" aria-labelledby="specialHoursModalLabel">
        <div class="modal-dialog modal-lg" role="document">
            <div class="modal-content">
                <div class="modal-header">
                    <h5 class="modal-title" id="${commandPrefix}-specialHoursModalLabel">${specialOpeningHour ? 'Edit' : 'Add'} Special Store Opening/Closing</h5>
                </div>
                <div class="modal-body">
                    <div class="form-row mb-3">
                        <div class="col-md-6">
                            <label for="${commandPrefix}-specialDescription">Description</label>
                            <input type="text" id="${commandPrefix}-specialDescription" name="${commandPrefix}-specialDescription" class="form-control" value="${specialOpeningHour?.description ?: ''}" />
                        </div>
                        <div class="col-md-6">
                            <label for="${commandPrefix}-specialDate">Date</label>
                            <input
                                    type="date"
                                    id="${commandPrefix}-specialDate"
                                    name="${commandPrefix}-specialDate"
                                    class="form-control"
                                    value="${specialOpeningHour?.date ?: ''}"
                                    required
                            />
                        </div>
                    </div>
                    <div>
                        <div class="form-row mb-2">
                            <div class="col-md-3" id="${commandPrefix}-specialTimeFieldsStart" style="${specialOpeningHour?.closed ? 'display:none;' : ''}">
                                <label for="${commandPrefix}-special-startTime">Start time</label>
                                <input type="text" id="${commandPrefix}-special-startTime" name="${commandPrefix}-special-startTime" class="form-control form-control-sm time-input" placeholder="HH:mm" value="${specialOpeningHour?.startTime ?: ''}" />
                            </div>
                            <div class="col-md-3" id="${commandPrefix}-specialTimeFieldsEnd" style="${specialOpeningHour?.closed ? 'display:none;' : ''}">
                                <label for="${commandPrefix}-special-endTime">End time</label>
                                <input type="text" id="${commandPrefix}-special-endTime" name="${commandPrefix}-special-endTime" class="form-control form-control-sm time-input" placeholder="HH:mm" value="${specialOpeningHour?.endTime ?: ''}" />
                            </div>
                            <div class="col-md-6 d-flex align-items-end">
                                <div class="form-check">
                                    <label class="form-check-label" for="${commandPrefix}-specialClosedCheckbox">Closed</label>
                                    <input type="checkbox" id="${commandPrefix}-specialClosedCheckbox" name="${commandPrefix}-specialClosed" class="form-check-input wl-checkbox ml-6" onclick="${commandPrefix}toggleSpecialTimeFields()" ${specialOpeningHour?.closed ? 'checked' : ''} />
                                </div>
                            </div>
                        </div>
                    </div>
                    <g:if test="${specialOpeningHour}">
                        <input type="hidden" id="${commandPrefix}-editIndex" value="${openingHourIndexItem}" />
                    </g:if>
                </div>
                <div class="modal-footer">
                    <button type="button" class="btn btn-secondary" id="${commandPrefix}-cancelSpecialHoursBtn" data-dismiss="modal">Cancel</button>
                    <button type="button" class="btn btn-success" id="${commandPrefix}-saveSpecialHoursBtn">${specialOpeningHour ? 'Update' : 'Save'}</button>
                </div>
            </div>
        </div>
    </div>
</g:form>

<script>
    {
        const commandPrefix = "${commandPrefix}";
        var modal = document.getElementById(commandPrefix + '-addSpecialOpeningHoursModal');
        console.log(modal);

        $(modal).on('hidden.bs.modal', function (event) {
            $('.modal-backdrop').remove();
            $('body').removeClass('modal-open');
        });

        $(document).on('shown.bs.modal', '#'+commandPrefix+'-addSpecialOpeningHoursModal', function () {
            const startTimeInput = document.getElementById(commandPrefix + '-special-startTime');
            const endTimeInput = document.getElementById(commandPrefix + '-special-endTime');

            if (startTimeInput) ${commandPrefix}setupTimeInput(startTimeInput);
            if (endTimeInput) ${commandPrefix}setupTimeInput(endTimeInput);
        });

        $('#'+commandPrefix+'-saveSpecialHoursBtn').on('click', function () {
            const description = $('#'+commandPrefix+'-specialDescription').val();
            const closed = $('#'+commandPrefix+'-specialClosedCheckbox').prop('checked');
            const date = $('#'+commandPrefix+'-specialDate').val();
            const startTime = $('#'+commandPrefix+'-special-startTime').val();
            const endTime = $('#'+commandPrefix+'-special-endTime').val();
            const specialOpeningHoursIndex = $('#'+commandPrefix+'-editIndex').val();

            if (!date) {
                alert('Please select a date. This field is mandatory.');
                return;
            }

            const event = new CustomEvent(commandPrefix + 'SaveSpecialHours', {
                detail: {description, closed, date, startTime, endTime, specialOpeningHoursIndex}
            });
            document.dispatchEvent(event);

            $('#'+commandPrefix+'-addSpecialOpeningHoursModal').modal('hide');
            $('.modal-backdrop').remove();
            $('body').removeClass('modal-open');
        });

        function ${commandPrefix}toggleSpecialTimeFields() {
            const isClosed = document.getElementById(commandPrefix + '-specialClosedCheckbox').checked;
            const timeFieldsStart = document.getElementById(commandPrefix + '-specialTimeFieldsStart');
            const timeFieldsEnd = document.getElementById(commandPrefix + '-specialTimeFieldsEnd');
            timeFieldsStart.style.display = isClosed ? 'none' : 'block';
            timeFieldsEnd.style.display = isClosed ? 'none' : 'block';
        }

        function ${commandPrefix}dateInputSetup() {
            let specialDateInput = document.getElementById(commandPrefix +'-specialDate');
            let today = new Date().toISOString().split('T')[0];
            specialDateInput.min = today;
            specialDateInput.addEventListener('keydown', function (e) {
                e.preventDefault();
            });
            specialDateInput.addEventListener('click', function () {
                this.showPicker();
            });
        }

        ${commandPrefix}dateInputSetup();
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

label[for="specialDate"]:after {
    content: " *";
    color: red;
}
</style>