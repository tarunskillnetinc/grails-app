<g:form id="${commandPrefix}-specialOpeningHoursForm" onsubmit="return saveSpecialOpeningHours(event);">
    <div class="modal fade" id="${commandPrefix}-addSpecialOpeningHoursModal" tabindex="100" role="dialog" aria-labelledby="specialHoursModalLabel">
        <div class="modal-dialog modal-lg" role="document">
            <div class="modal-content">
                <div class="modal-header">
                    <h5 class="modal-title"
                        id="${commandPrefix}-specialHoursModalLabel">${specialOpeningHour ? 'Edit' : 'Add'} Special Store Opening / Closing</h5>
                </div>
                <div class="modal-body">
                    <section id="${commandPrefix}-specialOpeningHours-errors-container" class="container-fluid"
                             style="display:none">
                        <div class="alert alert-danger alert-wl mx-0" role="alert">
                            <div id="${commandPrefix}-specialOpeningHours-errors"></div>
                        </div>
                    </section>
                    <div class="form-row mb-3">
                        <div class="col-md-8">
                            <label for="${commandPrefix}-specialDescription">Description</label>
                            <input type="text" id="${commandPrefix}-specialDescription" name="${commandPrefix}-specialDescription" class="form-control" value="${specialOpeningHour?.description ?: ''}" />
                        </div>

                        <div class="col-md-4">
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
                            <div class="col-md-4">
                                <div class="form-check pl-0 mb-3">
                                    <label class="form-check-label"
                                           for="${commandPrefix}-specialClosedCheckbox">Closed</label>
                                    <input type="checkbox" id="${commandPrefix}-specialClosedCheckbox"
                                           name="${commandPrefix}-specialClosed"
                                           class="form-check-input wl-checkbox mt-1 form-check ml-0 mt-2"
                                           onclick="${commandPrefix}toggleSpecialTimeFields()" ${specialOpeningHour?.closed ? 'checked' : ''}/>
                                </div>
                            </div>

                            <div class="col-md-4" id="${commandPrefix}-specialTimeFieldsStart"
                                 style="${specialOpeningHour?.closed ? 'display:none;' : ''}">
                                <label for="${commandPrefix}-special-startTime">Start time</label>
                                <input type="time" id="${commandPrefix}-special-startTime"
                                       name="${commandPrefix}-special-startTime"
                                       class="form-control form-control-sm time-input" placeholder="HH:mm"
                                       value="${specialOpeningHour?.startTime ?: ''}"/>
                            </div>

                            <div class="col-md-4" id="${commandPrefix}-specialTimeFieldsEnd"
                                 style="${specialOpeningHour?.closed ? 'display:none;' : ''}">
                                <label for="${commandPrefix}-special-endTime">End time</label>
                                <input type="time" id="${commandPrefix}-special-endTime"
                                       name="${commandPrefix}-special-endTime"
                                       class="form-control form-control-sm time-input" placeholder="HH:mm"
                                       value="${specialOpeningHour?.endTime ?: ''}"/>
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

        $('#${commandPrefix}-saveSpecialHoursBtn').on('click', function () {
            const description = $('#'+commandPrefix+'-specialDescription').val();
            const closed = $('#' + commandPrefix + '-specialClosedCheckbox').is(':checked');
            const date = $('#'+commandPrefix+'-specialDate').val();
            const startTime = $('#'+commandPrefix+'-special-startTime').val();
            const endTime = $('#'+commandPrefix+'-special-endTime').val();
            const specialOpeningHoursIndex = $('#'+commandPrefix+'-editIndex').val();

            let errors = []

            if (!description) {
                errors.push('Please enter a description. This field is mandatory.');
            }

            if (!date) {
                errors.push('Please select a date. This field is mandatory.')
            }

            if (!closed) { // its open, check the start/end times.
                if (!startTime) {
                    errors.push('If the store is open, start time must be set and be in 24 hour format (00:00 to 23:59).')
                }

                if (!endTime) {
                    errors.push('If the store is open, end time must be set and be in 24 hour format (00:00 to 23:59)')
                }
            }

            if (errors.length > 0) {
                ${commandPrefix}displayErrors(errors);
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

        function ${commandPrefix}clearErrors() {
            $('#${commandPrefix}-specialOpeningHours-errors-container>div').html('');
            $('#${commandPrefix}-specialOpeningHours-errors-container').hide();
        }

        function ${commandPrefix}displayErrors(errors) {
            let text = "<ul>";
            for (let i = 0; i < errors.length; i++) {
                text += "<li>" + errors[i] + "</li>";
            }
            text += "</ul>";

            $('#${commandPrefix}-specialOpeningHours-errors-container>div').html(text);
            $('#${commandPrefix}-specialOpeningHours-errors-container').show();
        }


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
            var prefix = '${commandPrefix}';
            var specialDate = $('#' + prefix + '-specialDate');

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
        ${commandPrefix}clearErrors();
    }
</script>
<style>
#${commandPrefix}-addSpecialOpeningHoursModal .form-check checkbox {
    left: -0.5rem;
}
</style>