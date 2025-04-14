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
                                    type="text"
                                    id="${commandPrefix}-specialDate"
                                    name="${commandPrefix}-specialDate"
                                    class="form-control"
                                    value="${specialOpeningHour?.date ?: ''}"
                                    required
                                    onkeydown="return false"
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

            $('#${commandPrefix}-addSpecialOpeningHoursModal').modal('hide');
            $('.modal-backdrop').remove();
            $('body').removeClass('modal-open');
        });

        function ${commandPrefix}toggleSpecialTimeFields() {
            const isClosed = $('#${commandPrefix}-specialClosedCheckbox').is(':checked');
            const timeFieldsStart = $('#${commandPrefix}-specialTimeFieldsStart');
            const timeFieldsEnd = $('#${commandPrefix}-specialTimeFieldsEnd');

            if (isClosed) {
                timeFieldsStart.hide();
                timeFieldsEnd.hide();
            } else {
                timeFieldsStart.show();
                timeFieldsEnd.show();
            }
        }

        function ${commandPrefix}dateInputSetup() {
            var specialDate = $('#${commandPrefix}-specialDate');

            specialDate.datepicker({
                format: "dd/mm/yyyy",
                weekStart: 1,
                startDate: "${new Date().format("dd/MM/yyyy")}",
                todayHighlight: true,
                autoclose: true,
                todayBtn: "linked",
                orientation: "bottom auto"
            });
        }

        ${commandPrefix}dateInputSetup();
    }
</script>
<style>
#
${commandPrefix} -addSpecialOpeningHoursModal .modal {
    z-index: 1050 !important;
}

#
${commandPrefix} -addSpecialOpeningHoursModal .modal-backdrop {
    z-index: 1040 !important;
}

/* Checkbox styling */
#
${commandPrefix} -addSpecialOpeningHoursModal .wl-checkbox {
    margin-top: -0.1rem !important;
}

#
${commandPrefix} -addSpecialOpeningHoursModal .form-check-label {
    font-size: 14px;
    line-height: 18px;
    margin-left: 5px;
}

#
${commandPrefix} -addSpecialOpeningHoursModal .form-group label {
    font-size: 14px;
    line-height: 18px;
}

#
${commandPrefix} -addSpecialOpeningHoursModal .form-check {
    display: flex;
    align-items: center;
    margin-bottom: 0;
}

#
${commandPrefix} -addSpecialOpeningHoursModal .form-check-input {
    margin-top: 0;
    margin-right: 5px;
}

#
${commandPrefix} -addSpecialOpeningHoursModal label[for="specialDate"]:after {
    content: " *";
    color: red;
}
</style>