<section id="modal-header">
    <div class="modal-header">
        <h2 id="page-title" class="mx-auto my-auto">Add Other Restrictions</h2>
    </div>
</section>
<section id="modal-error">
</section>
<section id="modal-form">
    <g:form name="addListItemForm" onsubmit="return false;">

        <asset:stylesheet src="font-awesome-all.css" />
        <asset:stylesheet src="bootstrap-datetimepicker.css" />
        <asset:stylesheet src="bootstrap-datetimepicker-standalone.css" />

        <section id="other-restrictions-errors-container" class="container-fluid"></section>

        <div class="row form-group mb-4 mt-4">
            <g:hiddenField name="otherRestrictionIndex" value="${index}" />

            <!-- Adjusted column widths to give more space to description -->
            <div class="col-5">
                <div class="row">
                    <label for="otherRestrictionDescription" class="col-4 col-form-label-mandatory text-right">Description</label>
                    <div class="col-8">
                        <g:field type="text" id="otherRestrictionDescription" name="otherRestrictionDescription" class="form-control select-border" maxlength="60" value="${description}"/>
                    </div>
                </div>
            </div>

            <div class="col-3">
                <div class="row">
                    <label for="startDateTimePicker" class="col-5 col-form-label text-center small">Start date & time</label>
                    <div class="col-7 pl-0">
                        <g:textField name="startDateTimePicker" class="form-control form-control-sm bottom-border" value="${startDateTime}"/>
                    </div>
                </div>
            </div>

            <div class="col-3">
                <div class="row">
                    <label for="endDateTimePicker" class="col-5 col-form-label text-center small">End date & time</label>
                    <div class="col-7 pl-0">
                        <g:textField name="endDateTimePicker" class="form-control form-control-sm bottom-border" value="${endDateTime}"/>
                    </div>
                </div>
            </div>
        </div>
    </g:form>

    <div class="modal-footer">
        <button type="button" id="closeAddOtherRestrictionButton" class="btn btn-wl" onclick="closeOtherRestrictionsAddModal();">Cancel</button>
        <button type="button" id="saveAddOtherRestrictionButton" class="btn btn-success" onclick="saveOtherRestrictions();">Save</button>
    </div>


    <!-- Include ALL required JS libraries in the correct order -->
    <asset:javascript src="jquery-ui.js"/>
    <asset:javascript src="jquery-2.2.4.min.js"/>
    <asset:javascript src="moment.min.js"/>
    <asset:javascript src="bootstrap-3.3.7.js"/>
    <asset:javascript src="bootstrap-datetimepicker.min.js"/>

    <script type="text/javascript">
        var datepickerJQuery = $.noConflict(true);

        datepickerJQuery(document).ready(function($) {
            $('#startDateTimePicker').datetimepicker({
                format: 'DD/MM/YYYY HH:mm',
                locale: 'en-gb',
                useCurrent: false,
                showTodayButton: true,
                icons: {
                    time: 'fa fa-clock-o',
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

            $('#endDateTimePicker').datetimepicker({
                format: 'DD/MM/YYYY HH:mm',
                locale: 'en-gb',
                useCurrent: false,
                showTodayButton: true,
                icons: {
                    time: 'fa fa-clock-o',
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
            $("#startDateTimePicker").on("dp.change", function (e) {
                $('#endDateTimePicker').data("DateTimePicker").minDate(e.date);
            });

            $("#endDateTimePicker").on("dp.change", function (e) {
                $('#startDateTimePicker').data("DateTimePicker").maxDate(e.date);
            });
        });

    </script>
</section>