<section id="modal-header">
    <div class="modal-header">
        <h2 id="page-title" class="mx-auto my-auto">${index && index > 0 ? 'Edit' : 'Add'} Other Restrictions</h2>
    </div>
</section>
<section id="modal-error">
</section>
<section id="modal-form">
    <g:form name="addListItemForm" onsubmit="return false;">

        <asset:stylesheet src="bootstrap-4.min.css" />
        <asset:stylesheet src="font-awesome-all.css" />

        <section id="other-restrictions-errors-container" class="container-fluid"></section>

        <div class="row form-group mb-3 mt-4">
            <g:hiddenField name="otherRestrictionIndex" value="${index}" />

            <div class="col-6">
                <div class="row">
                    <label for="otherRestrictionDescription" class="col-5 col-form-label text-right">Description</label>
                    <div class="col-7">
                        <g:field type="text" id="otherRestrictionDescription" name="otherRestrictionDescription" class="form-control select-border" maxlength="60" value="${description}"/>
                    </div>
                </div>
            </div>

            <!-- Start date & time field -->
            <div class="col-6">
                <div class="row mr-4">
                    <label for="startDateTimePicker" class="col-5 col-form-label text-right">Start Date & Time</label>
                    <div class="col-7">
                        <div class="input-group date" id="startDateTimePickerDiv" data-target-input="nearest">
                            <input type="text" id="startDateTimePicker" name="startDateTimePicker"
                                   class="form-control datetimepicker-input bottom-border"
                                   data-target="#startDateTimePickerDiv" value="${startDateTime}"/>
                        </div>
                    </div>
                </div>
            </div>
        </div>

        <!-- Second row for End date & time -->
        <div class="row form-group mb-4">
            <!-- End date & time field -->
            <div class="col-6  mt-3">
                <div class="row">
                    <label for="endDateTimePicker" class="col-5 col-form-label text-right">End Date & Time</label>
                    <div class="col-7">
                        <div class="input-group date" id="endDateTimePickerDiv" data-target-input="nearest">
                            <input type="text" id="endDateTimePicker" name="endDateTimePicker"
                                   class="form-control datetimepicker-input bottom-border"
                                   data-target="#endDateTimePickerDiv" value="${endDateTime}"/>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </g:form>

    <div class="modal-footer">
        <button type="button" id="closeAddOtherRestrictionButton" class="btn btn-wl" onclick="closeOtherRestrictionsAddModal();">Cancel</button>
        <button type="button" id="saveAddOtherRestrictionButton" class="btn btn-success" onclick="saveOtherRestrictions();">Save</button>
    </div>

    <asset:javascript src="moment.min.js" />
    <asset:javascript src="bootstrap-4.min.js" />

    <script type="text/javascript">
        $(document).ready(function() {
            // Initialize the datetime pickers
            $('#startDateTimePickerDiv').datetimepicker({
                format: 'DD/MM/YYYY HH:mm',
                locale: 'en-gb',
                useCurrent: false,
                buttons: {
                    showToday: true
                },
                icons: {
                    time: 'fa fa-clock',
                    date: 'fa fa-calendar',
                    up: 'fa fa-chevron-up',
                    down: 'fa fa-chevron-down',
                    previous: 'fa fa-chevron-left',
                    next: 'fa fa-chevron-right',
                    today: 'fa fa-crosshairs',
                    clear: 'fa fa-trash',
                    close: 'fa fa-times'
                }
            });

            $('#endDateTimePickerDiv').datetimepicker({
                format: 'DD/MM/YYYY HH:mm',
                locale: 'en-gb',
                useCurrent: false,
                buttons: {
                    showToday: true
                },
                icons: {
                    time: 'fa fa-clock',
                    date: 'fa fa-calendar',
                    up: 'fa fa-chevron-up',
                    down: 'fa fa-chevron-down',
                    previous: 'fa fa-chevron-left',
                    next: 'fa fa-chevron-right',
                    today: 'fa fa-crosshairs',
                    clear: 'fa fa-trash',
                    close: 'fa fa-times'
                }
            });

            // Link the two datetime pickers
            $("#startDateTimePickerDiv").on("change.datetimepicker", function (e) {
                $('#endDateTimePickerDiv').datetimepicker('minDate', e.date);
            });

            $("#endDateTimePickerDiv").on("change.datetimepicker", function (e) {
                $('#startDateTimePickerDiv').datetimepicker('maxDate', e.date);
            });

            // Prevent datetime picker from closing the collapse
            $('.datetimepicker-input, .bootstrap-datetimepicker-widget').on('click mousedown', function(e) {
                e.stopPropagation();
            });

            // Make the calendar popup when clicking on the input field
            $('#startDateTimePicker, #endDateTimePicker').on('focus', function() {
                const targetDiv = $(this).data('target');
                $(targetDiv).datetimepicker('show');
            });
        });
    </script>
</section>