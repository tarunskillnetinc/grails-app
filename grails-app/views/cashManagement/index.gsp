<!doctype html>
<html>
<head>
    <meta name="layout" content="main" />
    <title>Retailer Settings</title>

    <asset:stylesheet src="multi-select-checks.css" />

    <asset:javascript src="validators/input-validator.js" />
    <asset:javascript src="popper.min.js" />
    <asset:javascript src="multi-select-checks.js" />

    <script type='text/javascript'>

        $(document).ready(function () {
            $('#selectedItemsDisplay').click(function() {
                $('#weekdayDropdown').toggleClass('show');
            });

            $('.weekday-checkbox').change(function() {
                updateSelectedItems();
            });

            function updateSelectedItems() {
                var selectedItems = [];
                $('.weekday-checkbox:checked').each(function() {
                    selectedItems.push($(this).val());
                });
                $('#selectedItemsDisplay > span:first-child').html(selectedItems.join(', ') || 'Select Weekdays');
            }

            $(document).click(function(event) {
                if(!$(event.target).closest('.multiselect-container').length) {
                    if($('#weekdayDropdown').hasClass('show')) {
                        $('#weekdayDropdown').removeClass('show');
                    }
                }
            });

            const weekdays = ['Sunday', 'Monday', 'Tuesday', 'Wednesday', 'Thursday', 'Friday', 'Saturday'];
            createMultiSelectorChecks('automaticCloseDaysSelector', 'automaticCloseDays', weekdays, "Select Days");
            createMultiSelectorChecks('tillAutoSnapshotDaysSelector', 'tillAutoSnapshotDays', weekdays, "Select Days");
            createMultiSelectorChecks('safeAutoSnapshotDaysSelector', 'safeAutoSnapshotDays', weekdays, "Select Days");

            validateTimeInputs('automaticCloseTime');
            validateTimeInputs('tillAutoSnapshotTime');
            validateTimeInputs('safeAutoSnapshotTime');
        });

        function validateTimeInputs(inputId) {
            $('#' + inputId).on('keydown', function(event) {
                const key = event.key;
                const value = $(this).val();
                const position = this.selectionStart;

                // Allow control keys: backspace, delete, arrow keys, etc.
                const controlKeys = ['Backspace', 'ArrowLeft', 'ArrowRight', 'Delete', 'Tab'];
                if (controlKeys.includes(key)) {
                    return true;
                }

                // Prevent entering non-numeric or multiple colons
                if (!/\d/.test(key) && key !== ':') {
                    event.preventDefault();
                    return;
                }

                // Prevent entering more than one colon
                if (key === ':' && value.includes(':')) {
                    event.preventDefault();
                    return;
                }

                // Automatically insert the colon if the user tries to type it manually
                if (key === ':' && (position === 0)) {
                    $(this).val('00:');
                    event.preventDefault();
                } else if(key === ':' && (position === 1)) {
                    $(this).val('0'+value.slice(0,1)+':');
                    event.preventDefault();
                } else if (key === ':' && position === 2) {
                    $(this).val(value.slice(0, 2) + ':' + value.slice(2));
                    event.preventDefault();
                }

                // Prevent entering invalid numbers based on position
                if (/\d/.test(key)) {
                    if (position === 0 && !/[0-9]/.test(key)) {
                        event.preventDefault();
                    } if(position === 1 && parseInt(value[0]) > 2) {
                        event.preventDefault();
                    } else if (position === 1 && value[0] === '2' && !/[0-3]/.test(key)) {
                        event.preventDefault();
                    } else if (position === 3 && !/[0-5]/.test(key)) {
                        event.preventDefault();
                    }
                }
            });

            $('#' + inputId).on('input', function(event) {
                let value = $(this).val();
                let formattedValue = value.replace(/[^0-9:]/g, ''); // Allow only digits and colon

                // Automatically add colon if typed at first or second position
                if (formattedValue.length === 1 && formattedValue[0] === ':') {
                    formattedValue = '00:';
                } else if (formattedValue.length === 2 && formattedValue[1] === ':') {
                    formattedValue = '0' + formattedValue[0] + ':';
                }

                // Automatically insert colon at the correct position
                if (formattedValue.length > 2 && formattedValue[2] !== ':') {
                    formattedValue = formattedValue.slice(0, 2) + ':' + formattedValue.slice(2, 4);
                }

                // Limit length to 5 characters (HH:mm)
                if (formattedValue.length > 5) {
                    formattedValue = formattedValue.slice(0, 5);
                }
                $(this).val(formattedValue);

            });

        }
    </script>
    <style>
        h5 {
            margin-left: -220px;
        }
    </style>
</head>
<body>
<section id="breadcrumb-container" class="container-fluid">
    <nav aria-label="breadcrumb">
        <div class="row mt-4">
            <div class="col">
                <ol class="breadcrumb">
                    <li id="breadcrumb-1" class="breadcrumb-item"><g:link uri="/">Home</g:link></li>
                    <li id="breadcrumb-2" class="breadcrumb-item active" aria-current="page">Cash Management</li>
                </ol>
            </div>
        </div>
    </nav>
</section>

<section id="header-container" class="container-fluid">
    <div class="row header-wl mt-3">
        <div class="col-8 offset-2">
            <h2 id="page-title" class="mx-auto my-auto">Cash Management</h2>
        </div>

        <div class="col-2 text-right">
            <g:link elementId="cancel-btn" controller="retailer" action="index" tabindex="-1" role="button" class="btn btn-wl">Cancel</g:link>
            <button id="save-btn" class="btn btn-success" name="save" onclick="$('#save-button').submit();">Save</button>
        </div>
    </div>
</section>

<g:if test="${flash.error}">
    <section id="errors-container2" class="container-fluid">
        <div class="alert alert-danger alert-wl mx-0" role="alert">
            <ul>
                <g:each in="${flash.error}" var="error" status="i">
                    <li>${error}</li>
                </g:each>
            </ul>
        </div>
    </section>
</g:if>

<g:if test="${flash.message}">
    <section id="errors-container2" class="container-fluid">
        <div class="alert alert-success alert-wl mx-0" role="alert">
            <g:each in="${flash.message}" var="message" status="i">
                ${message}<br/>
            </g:each>
        </div>
    </section>
</g:if>


<section id="addProduct-section" class="container-fluid mt-4">
    <g:uploadForm name="save-button" action="save" method="POST" enctype="multipart/form-data">
        <div id="accordion">
            <!-- General information. -->
            <div class="card bg-light border-wl accordion-card col-12 col-lg-10 offset-lg-1 px-0">
                <div class="card-header pointer" id="generalDetails" data-toggle="collapse" data-target="#collapseGeneralDetails" aria-expanded="true" aria-controls="collapseGeneralDetails">
                    <div class="row">
                        <div class="col-10 font-weight-bold">Cash Management</div>
                        <div class="col-2 text-right">
                            <svg width="1em" height="1em" viewBox="0 0 16 16" class="bi bi-caret-down-fill text-right" fill="currentColor" xmlns="http://www.w3.org/2000/svg">
                                <path d="M7.247 11.14L2.451 5.658C1.885 5.013 2.345 4 3.204 4h9.592a1 1 0 0 1 .753 1.659l-4.796 5.48a1 1 0 0 1-1.506 0z"/>
                            </svg>
                        </div>
                    </div>
                </div>

                <div id="collapseGeneralDetails" class="collapse show" aria-labelledby="generalDetails" data-parent="#accordion">
                    <div class="card-body py-5">
                        <div class="col-12">
                            <h5 class="text-center">Till Shifts</h5>
                            <div class="form-group row">
                                <label for="isManualOpen" class="col-5 col-lg-3 offset-lg-2 col-form-label text-right pr-4">Manual Open</label>
                                <div class="col-7 col-lg-4">
                                    <input type="checkbox" class="col-1 form-check-input wl-checkbox" name="isManualOpen" id="isManualOpen" ${config?.tillShiftsManualOpen ? 'checked' : ''} />
                                </div>
                            </div>
                            <div class="form-group row">
                                <label for="isManualClose" class="col-5 col-lg-3 offset-lg-2 col-form-label text-right pr-4">Manual Close</label>
                                <div class="col-7 col-lg-4">
                                    <input type="checkbox" class="col-1 form-check-input wl-checkbox" name="isManualClose" id="isManualClose" ${config?.tillShiftsManualClose ? 'checked' : ''} />
                                </div>
                            </div>
                            <div class="form-group row">
                                <label for="automaticCloseDays" class="col-5 col-lg-3 offset-lg-2 col-form-label text-right pr-4">Automatic Close Days</label>
                                <div class="col-7 col-lg-4">
                                    <input type="hidden" id="automaticCloseDays" name="automaticCloseDays" value="${config?.automaticCloseDaysFormatted ? config?.automaticCloseDaysFormatted : ''}"/>
                                    <div id="automaticCloseDaysSelector"></div>
                                </div>
                            </div>

                            <div class="form-group row">
                                <label for="automaticCloseTime" class="col-5 col-lg-3 offset-lg-2 col-form-label text-right pr-4">Automatic Close Time</label>
                                <div class="col-7 col-lg-4">
                                    <input type="text" class="col-5 form-control bottom-border" name="automaticCloseTime" id="automaticCloseTime" value="${config?.tillShiftsAutoCloseTime}" placeholder="HH:mm"/>
                                </div>
                            </div>

                            <h5 class="text-center mt-5">Rolling Float</h5>
                            <div class="form-group row">
                                <label for="isRollingFloatEnable" class="col-5 col-lg-3 offset-lg-2 col-form-label text-right pr-4">Rolling Float Enable</label>
                                <div class="col-7 col-lg-4">
                                    <input type="checkbox" class="col-1 form-check-input wl-checkbox" name="isRollingFloatEnable" id="isRollingFloatEnable" ${config?.rollingFloatEnabled ? 'checked' : ''} />
                                </div>
                            </div>
                            <div class="form-group row">
                                <label for="rollingFloatValue" class="col-5 col-lg-3 offset-lg-2 col-form-label text-right pr-4">Rolling Float Value</label>
                                <div class="col-7 col-lg-4">
                                    <input type="number" class="col-5 form-control bottom-border" name="rollingFloatValue" id="rollingFloatValue" value="${config?.rollingFloatValue}" oninput="validateInput(this);" onkeydown="acceptNumeric(event);"/>
                                </div>
                            </div>

                            <h5 class="text-center mt-5">Reconciliation</h5>
                            <div class="form-group row">
                                <label for="tillShiftRecountLimit" class="col-5 col-lg-3 offset-lg-2 col-form-label text-right pr-4">Till Shift Recount Limit</label>
                                <div class="col-7 col-lg-4">
                                    <input type="number" class="col-5 form-control bottom-border" name="tillShiftRecountLimit" id="tillShiftRecountLimit" value="${config?.tillShiftRecountLimit}" oninput="validateInput(this);" onkeydown="acceptNumeric(event);"/>
                                </div>
                            </div>
                            <div class="form-group row">
                                <label for="tillShiftVarianceLimit" class="col-5 col-lg-3 offset-lg-2 col-form-label text-right pr-4">Till Shift Variance Limit</label>
                                <div class="col-7 col-lg-4">
                                    <input type="number" class="col-5 form-control bottom-border" name="tillShiftVarianceLimit" id="tillShiftVarianceLimit" value="${config?.tillShiftVarianceLimit}" oninput="validateInput(this);" onkeydown="acceptNumeric(event);"/>
                                </div>
                            </div>
                            <div class="form-group row">
                                <label for="safeRecountLimit" class="col-5 col-lg-3 offset-lg-2 col-form-label text-right pr-4">Safe Recount Limit</label>
                                <div class="col-7 col-lg-4">
                                    <input type="number" class="col-5 form-control bottom-border" name="safeRecountLimit" id="safeRecountLimit" value="${config?.tillShiftRecountLimit}" oninput="validateInput(this);" onkeydown="acceptNumeric(event);"/>
                                </div>
                            </div>
                            <div class="form-group row">
                                <label for="safeVarianceLimit" class="col-5 col-lg-3 offset-lg-2 col-form-label text-right pr-4">Safe Variance Limit</label>
                                <div class="col-7 col-lg-4">
                                    <input type="number" class="col-5 form-control bottom-border" name="safeVarianceLimit" id="safeVarianceLimit" value="${config?.safeVarianceLimit}" oninput="validateInput(this);" onkeydown="acceptNumeric(event);"/>
                                </div>
                            </div>
                            <div class="form-group row">
                                <label for="isOpenShiftWithoutFloat" class="col-5 col-lg-3 offset-lg-2 col-form-label text-right pr-4">Open Shift Without Float</label>
                                <div class="col-7 col-lg-4">
                                    <input type="checkbox" class="col-1 form-check-input wl-checkbox" name="isOpenShiftWithoutFloat" id="isOpenShiftWithoutFloat" ${config?.openShiftWithoutFloat ? 'checked' : ''} />
                                </div>
                            </div>

                            <h5 class="text-center mt-5">Snapshots</h5>
                            <div class="form-group row">
                                <label for="tillAutoSnapshotDays" class="col-5 col-lg-3 offset-lg-2 col-form-label text-right pr-4">Till Auto Snapshot Days</label>
                                <div class="col-7 col-lg-4">
                                    <input type="hidden" id="tillAutoSnapshotDays" name="tillAutoSnapshotDays" value="${config?.tillAutoSnapshotDaysFormatted}"/>
                                    <div id="tillAutoSnapshotDaysSelector"></div>
                                </div>
                            </div>
                            <div class="form-group row">
                                <label for="tillAutoSnapshotTime" class="col-5 col-lg-3 offset-lg-2 col-form-label text-right pr-4">Till Auto Snapshot Time</label>
                                <div class="col-7 col-lg-4">
                                    <input type="text" class="col-5 form-control bottom-border" name="tillAutoSnapshotTime" id="tillAutoSnapshotTime" value="${config?.tillAutoSnapshotTime}" placeholder="HH:mm"/>
                                </div>
                            </div>
                            <div class="form-group row">
                                <label for="safeAutoSnapshotDays" class="col-5 col-lg-3 offset-lg-2 col-form-label text-right pr-4">Safe Auto Snapshot Days</label>
                                <div class="col-7 col-lg-4">
                                    <input type="hidden" id="safeAutoSnapshotDays" name="safeAutoSnapshotDays" value="${config?.safeAutoSnapshotDaysFormatted}"/>
                                    <div id="safeAutoSnapshotDaysSelector"></div>
                                </div>
                            </div>
                            <div class="form-group row">
                                <label for="safeAutoSnapshotTime" class="col-5 col-lg-3 offset-lg-2 col-form-label text-right pr-4">Safe Auto Snapshot Time</label>
                                <div class="col-7 col-lg-4">
                                    <input type="text" class="col-5 form-control bottom-border" name="safeAutoSnapshotTime" id="safeAutoSnapshotTime" value="${config?.safeAutoSnapshotTime}" placeholder="HH:mm"/>
                                </div>
                            </div>

                            <h5 class="text-center mt-5">Holding Limit</h5>
                            <div class="form-group row">
                                <label for="tillCashHoldingLimit" class="col-5 col-lg-3 offset-lg-2 col-form-label text-right pr-4">Till Cash Holding Limit</label>
                                <div class="col-7 col-lg-4">
                                    <input type="number" class="col-5 form-control bottom-border" name="tillCashHoldingLimit" id="tillCashHoldingLimit" value="${config?.tillsCashHoldingLimit}" oninput="validateInput(this);" onkeydown="acceptNumeric(event);"/>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </g:uploadForm>
</section>
</body>
</html>